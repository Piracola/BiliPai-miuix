[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$failures = [System.Collections.Generic.List[string]]::new()

function Add-Failure([string]$message) {
    $failures.Add($message)
}

function Get-RelativePath([string]$path) {
    if ($path.StartsWith($repositoryRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
        $relative = $path.Substring($repositoryRoot.Length) -replace '^[\\/]+' , ""
        return $relative.Replace("\", "/")
    }
    return $path.Replace("\", "/")
}

function Test-ExcludedPath([string]$path) {
    $relative = Get-RelativePath $path
    return $relative -match '(^|/)(\.git|\.gradle|\.codegraph|\.rtk|\.agent|\.trae|\.idea|\.kotlin|\.zcode|build|node_modules)(/|$)'
}

function Get-MarkdownAnchor([string]$heading) {
    $anchor = $heading -replace '^#+\s*', ""
    $anchor = $anchor.ToLowerInvariant()
    $anchor = [regex]::Replace($anchor, '[^\p{L}\p{N} -]', "")
    $anchor = $anchor.Replace(" ", "-")
    return [regex]::Replace($anchor, '-+', '-')
}

function Test-MarkdownAnchor([string]$content, [string]$fragment) {
    $explicitAnchor = 'id="' + [regex]::Escape($fragment) + '"'
    if ($content -match $explicitAnchor) {
        return $true
    }

    foreach ($line in ($content -split "`r?`n")) {
        if ($line -match '^#+\s+' -and (Get-MarkdownAnchor $line) -eq $fragment.ToLowerInvariant()) {
            return $true
        }
    }
    return $false
}

$documents = Get-ChildItem -LiteralPath $repositoryRoot -Recurse -File |
    Where-Object {
        $_.Extension -in ".md", ".adoc", ".txt" -and -not (Test-ExcludedPath $_.FullName)
    }

$markdownLinkPattern = [regex]'(?<!\!)\[[^\]]+\]\(([^)]+)\)'
foreach ($source in $documents | Where-Object { $_.Extension -eq ".md" }) {
    $content = Get-Content -LiteralPath $source.FullName -Raw -Encoding utf8
    foreach ($match in $markdownLinkPattern.Matches($content)) {
        $rawTarget = $match.Groups[1].Value.Trim()
        if ($rawTarget.StartsWith("<") -and $rawTarget.EndsWith(">")) {
            $rawTarget = $rawTarget.Substring(1, $rawTarget.Length - 2)
        }
        if ([string]::IsNullOrWhiteSpace($rawTarget) -or
            $rawTarget -match '^(https?://|mailto:|file:)') {
            continue
        }

        $pathPart, $fragment = $rawTarget -split '#', 2
        if ([string]::IsNullOrWhiteSpace($pathPart)) {
            $targetPath = $source.FullName
        } else {
            $targetPath = [System.IO.Path]::GetFullPath(
                [System.IO.Path]::Combine($source.DirectoryName, $pathPart)
            )
        }

        if (-not (Test-Path -LiteralPath $targetPath)) {
            Add-Failure ("{0} -> {1} (target missing)" -f (Get-RelativePath $source.FullName), $rawTarget)
            continue
        }

        if ($fragment -and [System.IO.Path]::GetExtension($targetPath) -eq ".md") {
            $targetContent = Get-Content -LiteralPath $targetPath -Raw -Encoding utf8
            if (-not (Test-MarkdownAnchor $targetContent $fragment)) {
                Add-Failure ("{0} -> {1} (anchor missing)" -f (Get-RelativePath $source.FullName), $rawTarget)
            }
        }
    }
}

$buildFile = Join-Path $repositoryRoot "app/build.gradle.kts"
$buildContent = Get-Content -LiteralPath $buildFile -Raw -Encoding utf8
$versionName = [regex]::Match($buildContent, '(?m)^\s*versionName\s*=\s*"([^"]+)"').Groups[1].Value
$versionCode = [regex]::Match($buildContent, '(?m)^\s*versionCode\s*=\s*(\d+)').Groups[1].Value
if (-not $versionName -or -not $versionCode) {
    Add-Failure "Cannot read versionName/versionCode from app/build.gradle.kts"
} else {
    $changelogContent = Get-Content -LiteralPath (Join-Path $repositoryRoot "CHANGELOG.md") -Raw -Encoding utf8
    if ($changelogContent -notmatch ("(?m)^## v{0} \(" -f [regex]::Escape($versionName))) {
        Add-Failure ("CHANGELOG.md has no entry for versionName {0}" -f $versionName)
    }
    $entryPattern = '(?ms)^## v' + [regex]::Escape($versionName) + ' \([^)]*\)(?<entry>.*?)(?=^## |\z)'
    $entry = [regex]::Match($changelogContent, $entryPattern)
    if (-not $entry.Success -or $entry.Groups["entry"].Value -notmatch ('versionCode\s+' + [regex]::Escape($versionCode))) {
        Add-Failure ("CHANGELOG.md has no matching versionCode for {0}: {1}" -f $versionName, $versionCode)
    }
}

$navKeySource = Get-Content -LiteralPath (Join-Path $repositoryRoot "app/src/main/java/com/android/purebilibili/navigation3/BiliPaiNavKey.kt") -Raw -Encoding utf8
$catalogContent = Get-Content -LiteralPath (Join-Path $repositoryRoot "docs/wiki/ui-design/pages/PAGE_CATALOG.md") -Raw -Encoding utf8
$sourceKeys = @([regex]::Matches($navKeySource, '(?m)^\s*data\s+(?:object|class)\s+(\w+)') | ForEach-Object { $_.Groups[1].Value } | Sort-Object -Unique)
$catalogMatches = @([regex]::Matches($catalogContent, '\[NAVKEY:([A-Za-z0-9_]+)\]'))
$catalogKeys = @($catalogMatches | ForEach-Object { $_.Groups[1].Value })
$duplicateCatalogKeys = @($catalogKeys | Group-Object | Where-Object Count -ne 1 | ForEach-Object Name)
$missingCatalogKeys = @($sourceKeys | Where-Object { $_ -notin $catalogKeys })
$extraCatalogKeys = @($catalogKeys | Where-Object { $_ -notin $sourceKeys })
if ($duplicateCatalogKeys.Count -gt 0) {
    Add-Failure ("Duplicate PAGE_CATALOG NavKey markers: {0}" -f ($duplicateCatalogKeys -join ", "))
}
if ($missingCatalogKeys.Count -gt 0 -or $extraCatalogKeys.Count -gt 0) {
    Add-Failure ("PAGE_CATALOG mismatch. Missing={0}; Extra={1}" -f ($missingCatalogKeys -join ", "), ($extraCatalogKeys -join ", "))
}

if ($failures.Count -gt 0) {
    $message = "Documentation verification failed:`n" + ($failures -join "`n")
    throw $message
}

Write-Host ("Documentation verification passed: {0} documents, version {1} ({2}), {3} NavKey entries." -f $documents.Count, $versionName, $versionCode, $sourceKeys.Count)
