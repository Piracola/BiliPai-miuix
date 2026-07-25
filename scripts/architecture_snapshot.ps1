param(
    [string]$OutputPath = ""
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$sourceRoot = Join-Path $repoRoot "app\src\main\java\com\android\purebilibili"

if (-not (Test-Path -LiteralPath $sourceRoot)) {
    throw "Main source directory not found: $sourceRoot"
}

if ([string]::IsNullOrWhiteSpace($OutputPath)) {
    $OutputPath = Join-Path $repoRoot "build\reports\architecture\architecture-snapshot.md"
} elseif (-not [System.IO.Path]::IsPathRooted($OutputPath)) {
    $OutputPath = Join-Path $repoRoot $OutputPath
}

$layers = @("shell", "app", "core", "data", "domain", "feature", "navigation", "navigation3")
$layerStats = @()
$edgeCounts = @{}
$crossFeatureCounts = @{}
$crossFeatureFiles = @{}

foreach ($layer in $layers) {
    if ($layer -eq "shell") {
        $files = @(Get-ChildItem -LiteralPath $sourceRoot -File -Filter "*.kt")
    } else {
        $layerPath = Join-Path $sourceRoot $layer
        $files = @(Get-ChildItem -LiteralPath $layerPath -Recurse -File -Filter "*.kt")
    }
    $lineCount = 0
    foreach ($file in $files) {
        foreach ($unusedLine in [System.IO.File]::ReadLines($file.FullName)) {
            if (-not [string]::IsNullOrWhiteSpace($unusedLine)) {
                $lineCount++
            }
        }
    }
    $layerStats += [pscustomobject]@{
        Layer = $layer
        Files = $files.Count
        Lines = $lineCount
    }
}

$sourceFiles = @(Get-ChildItem -LiteralPath $sourceRoot -Recurse -File -Filter "*.kt")
foreach ($file in $sourceFiles) {
    $relativePath = $file.FullName.Substring($sourceRoot.Length + 1)
    $pathSegments = @($relativePath -split "[\\/]")
    $fromLayer = if ($pathSegments.Count -eq 1) { "shell" } else { $pathSegments[0] }
    if ($fromLayer -notin $layers) {
        continue
    }

    foreach ($line in [System.IO.File]::ReadLines($file.FullName)) {
        if ($line -notmatch '^import com\.android\.purebilibili\.([A-Za-z0-9_]+)') {
            continue
        }

        $toLayer = $Matches[1]
        if ($toLayer -in $layers -and $toLayer -ne $fromLayer) {
            $edgeKey = "$fromLayer -> $toLayer"
            if (-not $edgeCounts.ContainsKey($edgeKey)) {
                $edgeCounts[$edgeKey] = 0
            }
            $edgeCounts[$edgeKey]++
        }

        if ($fromLayer -eq "feature" -and $line -match '^import com\.android\.purebilibili\.feature\.([A-Za-z0-9_]+)') {
            $featureRelativePath = $file.FullName.Substring((Join-Path $sourceRoot "feature").Length + 1)
            $fromFeature = ($featureRelativePath -split "[\\/]")[0]
            $toFeature = $Matches[1]
            if ($fromFeature -ne $toFeature) {
                $featureEdgeKey = "$fromFeature -> $toFeature"
                if (-not $crossFeatureCounts.ContainsKey($featureEdgeKey)) {
                    $crossFeatureCounts[$featureEdgeKey] = 0
                }
                $crossFeatureCounts[$featureEdgeKey]++
                $crossFeatureFiles[$file.FullName] = $true
            }
        }
    }
}

$outputDirectory = Split-Path -Parent $OutputPath
New-Item -ItemType Directory -Force -Path $outputDirectory | Out-Null

$totalCrossFeatureImports = 0
foreach ($count in $crossFeatureCounts.Values) {
    $totalCrossFeatureImports += $count
}

$lines = [System.Collections.Generic.List[string]]::new()
$lines.Add("# BiliPai Architecture Snapshot")
$lines.Add("")
$lines.Add("Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss zzz')")
$lines.Add("")
$lines.Add("## Top-level Package Size")
$lines.Add("")
$lines.Add("| Package | Kotlin files | Non-empty lines |")
$lines.Add("| --- | ---: | ---: |")
foreach ($stat in $layerStats) {
    $lines.Add("| ``$($stat.Layer)`` | $($stat.Files) | $($stat.Lines) |")
}

$lines.Add("")
$lines.Add("## Top-level Package Imports")
$lines.Add("")
$lines.Add("| Direction | Imports |")
$lines.Add("| --- | ---: |")
foreach ($edge in ($edgeCounts.GetEnumerator() | Sort-Object Name)) {
    $lines.Add("| ``$($edge.Name)`` | $($edge.Value) |")
}

$lines.Add("")
$lines.Add("## Cross-feature Hotspots")
$lines.Add("")
$lines.Add("- Cross-feature imports: $totalCrossFeatureImports")
$lines.Add("- Files involved: $($crossFeatureFiles.Count)")
$lines.Add("- Distinct directions: $($crossFeatureCounts.Count)")
$lines.Add("")
$lines.Add("| Direction | Imports |")
$lines.Add("| --- | ---: |")
foreach ($edge in ($crossFeatureCounts.GetEnumerator() | Sort-Object Value -Descending | Select-Object -First 30)) {
    $lines.Add("| ``$($edge.Name)`` | $($edge.Value) |")
}

$lines.Add("")
$lines.Add("> Static snapshot based on Kotlin ``import`` statements. Reflection, generated code, and runtime dependencies are not included.")

$lines | Set-Content -LiteralPath $OutputPath -Encoding utf8
Write-Host "Architecture snapshot generated: $OutputPath"
