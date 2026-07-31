#!/usr/bin/env bash
# 正式版视频卡片进/出详情过渡采样：start 清零计数，stop 在过渡完成后导出结果。
set -euo pipefail

PKG="${PKG:-com.android.purebilibili}"
DEVICE=""
OUT_DIR="${OUT_DIR:-docs/perf/raw}"

usage() {
  cat <<'EOF'
Usage:
  ./scripts/release_card_transition_sample.sh start [--device SERIAL]
  ./scripts/release_card_transition_sample.sh stop  [--device SERIAL]

Workflow:
  1) On the release app, open Home and wait for a video card to be ready.
  2) Run start, then repeat card -> detail -> back exactly 8 times.
  3) Return to the card source screen and run stop.

Notes:
  - Defaults to com.android.purebilibili and refuses debug packages.
  - The sampling window performs no ADB reads or log collection.
  - stop reports platform jank, recent framestats FPS/latency, and PSS change.
EOF
}

MODE="${1:-}"
[[ -n "$MODE" ]] && shift || true
case "$MODE" in
  start|stop) ;;
  -h|--help|"") usage; exit 0 ;;
  *) echo "Unknown mode: $MODE" >&2; usage >&2; exit 1 ;;
esac

while [[ $# -gt 0 ]]; do
  case "$1" in
    --device) DEVICE="${2:-}"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown argument: $1" >&2; usage >&2; exit 1 ;;
  esac
done

command -v adb >/dev/null 2>&1 || { echo "[release-transition] adb not found" >&2; exit 1; }
command -v python3 >/dev/null 2>&1 || { echo "[release-transition] python3 not found" >&2; exit 1; }
[[ "$PKG" != *.debug ]] || { echo "[release-transition] refusing debug package: $PKG" >&2; exit 1; }
[[ -n "$DEVICE" ]] || DEVICE="$(adb devices | awk 'NR>1 && $2=="device" { print $1; exit }')"
[[ -n "$DEVICE" ]] || { echo "[release-transition] no online adb device" >&2; exit 1; }

adb_cmd() {
  adb -s "$DEVICE" "$@"
}

require_foreground() {
  if ! adb_cmd shell dumpsys activity activities | grep -q "topResumedActivity=.* $PKG/"; then
    echo "[release-transition] keep $PKG in the foreground while starting and stopping" >&2
    exit 1
  fi
}

adb_cmd shell pm path "$PKG" 2>/dev/null | grep -q '^package:' || {
  echo "[release-transition] release package $PKG is not installed on $DEVICE" >&2
  exit 1
}
if adb_cmd shell dumpsys package "$PKG" | grep -q 'DEBUGGABLE'; then
  echo "[release-transition] refusing debuggable package: $PKG" >&2
  exit 1
fi

mkdir -p "$OUT_DIR"
STATE_FILE="$OUT_DIR/release-transition-${DEVICE}.session"

pss_kb() {
  sed -nE 's/.*TOTAL PSS:[[:space:]]*([0-9,]+).*/\1/p' "$1" | head -n1 | tr -d ','
}

if [[ "$MODE" == "start" ]]; then
  require_foreground
  PID="$(adb_cmd shell pidof "$PKG" 2>/dev/null | tr -d '\r')"
  [[ -n "$PID" ]] || {
    echo "[release-transition] open release Home and wait for a card before starting" >&2
    exit 1
  }

  STAMP="$(date +%Y%m%d-%H%M%S)"
  MEM_BEFORE_FILE="$OUT_DIR/release-transition-${DEVICE}-${STAMP}-mem-before.txt"
  adb_cmd shell dumpsys meminfo "$PKG" > "$MEM_BEFORE_FILE"
  adb_cmd shell dumpsys gfxinfo "$PKG" reset >/dev/null 2>&1 || true
  printf 'STAMP=%s\nMEM_BEFORE_FILE=%s\n' "$STAMP" "$MEM_BEFORE_FILE" > "$STATE_FILE"

  echo "[release-transition] started: device=$DEVICE package=$PKG"
  echo "[release-transition] now repeat card -> detail -> back exactly 8 times, then run:"
  echo "  ./scripts/release_card_transition_sample.sh stop --device $DEVICE"
  exit 0
fi

[[ -f "$STATE_FILE" ]] || {
  echo "[release-transition] no active sample for $DEVICE; run start first" >&2
  exit 1
}

STAMP="$(sed -n 's/^STAMP=//p' "$STATE_FILE")"
MEM_BEFORE_FILE="$(sed -n 's/^MEM_BEFORE_FILE=//p' "$STATE_FILE")"
[[ -n "$STAMP" && -f "$MEM_BEFORE_FILE" ]] || {
  echo "[release-transition] invalid session file: $STATE_FILE" >&2
  exit 1
}

GFX_FILE="$OUT_DIR/release-transition-${DEVICE}-${STAMP}-gfxinfo.txt"
MEM_AFTER_FILE="$OUT_DIR/release-transition-${DEVICE}-${STAMP}-mem-after.txt"
require_foreground
adb_cmd shell dumpsys gfxinfo "$PKG" framestats > "$GFX_FILE"
if grep -q 'Failure while dumping the app' "$GFX_FILE"; then
  echo "[release-transition] gfxinfo was unavailable; sample remains active, return to the app and run stop again" >&2
  exit 1
fi
adb_cmd shell dumpsys meminfo "$PKG" > "$MEM_AFTER_FILE"
rm -f "$STATE_FILE"

python3 - "$GFX_FILE" "$MEM_BEFORE_FILE" "$MEM_AFTER_FILE" <<'PY'
from pathlib import Path
import math, os, re, statistics, sys

gfx_path, mem_before_path, mem_after_path = map(Path, sys.argv[1:4])
text = gfx_path.read_text(errors="ignore")
failures = []
max_over_budget_percent = float(os.environ.get("MAX_OVER_BUDGET_PERCENT", "5"))
max_over_two_budgets_percent = float(os.environ.get("MAX_OVER_TWO_BUDGETS_PERCENT", "1"))
max_pss_delta_mib = float(os.environ.get("MAX_PSS_DELTA_MIB", "16"))

print("[release-transition] platform summary:")
patterns = (
    "Total frames rendered", "Janky frames", "50th percentile", "90th percentile",
    "95th percentile", "99th percentile", "Number Missed Vsync",
    "Number High input latency", "Number Slow UI thread", "Number Slow bitmap uploads",
    "Number Slow issue draw commands", "Number Frame deadline missed",
)
seen = set()
for line in text.splitlines():
    stripped = line.strip()
    if stripped.startswith("Pipeline="):
        break
    if any(pattern in stripped for pattern in patterns):
        label = stripped.split(":", 1)[0]
        if label not in seen:
            print(f"  {stripped}")
            seen.add(label)

frames, header, in_profile = [], None, False
for line in text.splitlines():
    stripped = line.strip()
    if stripped == "---PROFILEDATA---":
        in_profile = not in_profile
        header = None
        continue
    if not in_profile or not stripped:
        continue
    if stripped.startswith("Flags,"):
        header = [part.strip() for part in stripped.split(",") if part.strip()]
        continue
    if header is None or not stripped[0].isdigit():
        continue
    parts = [part.strip() for part in stripped.split(",") if part.strip()]
    if len(parts) != len(header):
        continue
    try:
        row = dict(zip(header, map(int, parts)))
    except ValueError:
        continue
    intended = row.get("IntendedVsync", 0)
    completed = row.get("FrameCompleted", 0)
    interval = row.get("FrameInterval", 0)
    budget = row.get("WorkloadTarget", interval) or interval
    flags = row.get("Flags", 0)
    if not flags & 8 and intended > 0 and completed >= intended and interval > 0:
        frames.append((intended, completed, interval, budget))

if frames:
    latest = {}
    for frame in frames:
        if frame[0] not in latest or frame[1] > latest[frame[0]][1]:
            latest[frame[0]] = frame
    frames = sorted(latest.values())
    durations = [(frame[1] - frame[0]) / 1_000_000 for frame in frames]
    budgets = [frame[3] / 1_000_000 for frame in frames]
    interval_ns = int(statistics.median(frame[2] for frame in frames))
    active_deltas = [
        later[0] - earlier[0]
        for earlier, later in zip(frames, frames[1:])
        if 0 < later[0] - earlier[0] <= interval_ns * 4
    ]
    active_fps = 1_000_000_000 / statistics.mean(active_deltas) if active_deltas else math.nan

    def percentile(values, fraction):
        ordered = sorted(values)
        return ordered[max(0, math.ceil(len(ordered) * fraction) - 1)]

    over_budget = sum(duration > budget for duration, budget in zip(durations, budgets))
    over_two_budgets = sum(
        duration > budget * 2 for duration, budget in zip(durations, budgets)
    )
    p90 = percentile(durations, .90)
    median_budget = statistics.median(budgets)
    over_budget_percent = over_budget / len(frames) * 100
    over_two_budgets_percent = over_two_budgets / len(frames) * 100
    print("[release-transition] recent framestats:")
    print(
        f"  valid_frames={len(frames)} target_refresh≈{1_000_000_000 / interval_ns:.1f}Hz "
        f"active_render_rate≈{active_fps:.1f}fps"
    )
    print(
        "  completion_latency="
        f"p50 {percentile(durations, .50):.2f}ms / "
        f"p90 {percentile(durations, .90):.2f}ms / "
        f"p95 {percentile(durations, .95):.2f}ms / "
        f"p99 {percentile(durations, .99):.2f}ms"
    )
    print(
        f"  over_frame_budget={over_budget} ({over_budget_percent:.2f}%) "
        f"over_2x_budget={over_two_budgets} ({over_two_budgets_percent:.2f}%) "
        f"median_workload_target={median_budget:.2f}ms"
    )
    if over_budget_percent > max_over_budget_percent:
        failures.append(
            f"over-budget {over_budget_percent:.2f}% > {max_over_budget_percent:.2f}%"
        )
    if over_two_budgets_percent > max_over_two_budgets_percent:
        failures.append(
            "over-2x-budget "
            f"{over_two_budgets_percent:.2f}% > {max_over_two_budgets_percent:.2f}%"
        )
    if p90 > median_budget:
        failures.append(f"p90 {p90:.2f}ms > one workload target {median_budget:.2f}ms")
else:
    print("[release-transition] framestats: no valid PROFILEDATA rows")
    failures.append("no valid framestats")

def total_pss_kb(path):
    match = re.search(r"TOTAL PSS:\s*([0-9,]+)", path.read_text(errors="ignore"))
    return int(match.group(1).replace(",", "")) if match else None

before, after = total_pss_kb(mem_before_path), total_pss_kb(mem_after_path)
if before is not None and after is not None:
    pss_delta_mib = (after - before) / 1024
    print(f"[release-transition] memory: total_pss={before / 1024:.1f}->{after / 1024:.1f}MiB delta={pss_delta_mib:+.1f}MiB")
    if pss_delta_mib > max_pss_delta_mib:
        failures.append(f"PSS delta {pss_delta_mib:.1f}MiB > {max_pss_delta_mib:.1f}MiB")
else:
    failures.append("TOTAL PSS unavailable")

if failures:
    print("[release-transition] HARD GATE FAILED:")
    for failure in failures:
        print(f"  - {failure}")
    raise SystemExit(2)
print("[release-transition] HARD GATE PASSED")
PY

echo "[release-transition] raw: $GFX_FILE"
echo "[release-transition] raw: $MEM_BEFORE_FILE"
echo "[release-transition] raw: $MEM_AFTER_FILE"
