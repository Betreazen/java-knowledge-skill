#!/usr/bin/env bash
# Бенчмарк серверной логики Minecraft командой /tick sprint (Minecraft 1.20.3+, в том числе 26.x).
# Запускает сервер, ставит принудительную загрузку чанков вокруг 0,0, N раз выполняет
# «tick sprint <ticks>» и пишет результат в формате JMH JSON, который читает BenchCompare.java.
#
# Запуск из папки сервера (там лежат eula.txt, мир с фиксированным seed и ваши моды или плагины):
#   bash tick_sprint_bench.sh <server.jar | "команда запуска"> <out.json> [runs=5] [ticks=1200] [radius_blocks=64, до 128] [name=world] [warmup=2]
# Первый аргумент — jar сервера (ванильный, Paper) или полная команда запуска, если сервер стартует иначе:
#   Fabric:   "java -Xmx2G -jar fabric-server-launch.jar nogui"   (лаунчер с meta.fabricmc.net, моды в mods/)
#   NeoForge: "bash run.sh nogui"   (после java -jar neoforge-<версия>-installer.jar --installServer;
#             память задаётся в user_jvm_args.txt)
# Сравнение «до» и «после»: java BenchCompare.java bench/before bench/after
#
# Строку результата сервер печатает так (язык en_us):
#   Sprint completed with 1834.2 ticks per second, or 0.55 ms per tick
# Метрика — ms per tick (меньше — лучше), поэтому в JSON режим avgt.
# Один прогон скрипта — один запуск JVM: первые спринты включают прогрев JIT, поэтому первые
# warmup спринтов отбрасываются, а оценкой берётся медиана остальных. В проверке на Paper 26.3 второй
# спринт ещё шёл медленнее остальных, поэтому по умолчанию прогрев — 2 спринта.
set -euo pipefail
SERVER=${1:?server jar or launch command}
OUT=${2:?output json}
RUNS=${3:-5}
TICKS=${4:-1200}
R=${5:-64}
NAME=${6:-world}
WARMUP=${7:-2}

# /forceload принимает не больше 256 чанков за раз; при большем радиусе команда откажет, и замер пойдёт по пустому миру
CHUNKS=$(( (((R - 1) >> 4) - ((-R) >> 4) + 1) ** 2 ))
if [ "$CHUNKS" -gt 256 ]; then
    echo "radius_blocks=$R covers $CHUNKS chunks, /forceload allows at most 256 (use radius_blocks <= 128)" >&2
    exit 2
fi

LOG=$(mktemp)
trap 'rm -f "$LOG"' EXIT

wait_for() { # шаблон, номер вхождения
    until [ "$(grep -c -- "$1" "$LOG" 2>/dev/null || true)" -ge "$2" ]; do
        # скрипт завершился (лог удалён) или убит, даже через kill -9: остановить сервер и не висеть
        if [ ! -f "$LOG" ] || ! kill -0 "$$" 2>/dev/null; then
            echo stop
            exit 1
        fi
        sleep 1
    done
}

if [[ "$SERVER" == *.jar ]]; then
    LAUNCH=(java -Xms2G -Xmx2G -jar "$SERVER" nogui)
else
    read -r -a LAUNCH <<<"$SERVER"
fi

timeout 1800 "${LAUNCH[@]}" > "$LOG" 2>&1 < <(
    wait_for 'Done (' 1
    echo "forceload add -$R -$R $((R - 1)) $((R - 1))"
    sleep 5
    for i in $(seq 1 $((WARMUP + RUNS))); do
        echo "tick sprint $TICKS"
        wait_for 'Sprint completed' "$i"
    done
    echo stop
) || rc=$?

# Падение сервера или таймаут не должны проходить молча или давать медиану по неполной серии.
done_sprints=$(grep -c 'Sprint completed' "$LOG" || true)
if [ "${rc:-0}" -ne 0 ] || [ "$done_sprints" -lt $((WARMUP + RUNS)) ]; then
    echo "server exited with code ${rc:-0} after $done_sprints of $((WARMUP + RUNS)) sprints (124 = timeout)" >&2
    tail -40 "$LOG" >&2
    exit 1
fi
mapfile -t ms < <(grep -o 'or [0-9.]* ms per tick' "$LOG" | awk '{print $2}' | tail -n +$((WARMUP + 1)))
printf '%s\n' "${ms[@]}" | sort -g | awk -v name="$NAME" -v ticks="$TICKS" -v r="$R" '
    { v[NR] = $1 }
    END {
        n = NR
        med = (n % 2) ? v[(n + 1) / 2] : (v[n / 2] + v[n / 2 + 1]) / 2
        printf "[{\"benchmark\": \"tick_sprint.%s\", \"mode\": \"avgt\", ", name
        printf "\"params\": {\"ticks\": \"%s\", \"forceload_radius\": \"%s\"}, ", ticks, r
        printf "\"primaryMetric\": {\"score\": %s, \"scoreError\": \"NaN\", ", med
        printf "\"scoreConfidence\": [%s, %s], \"scoreUnit\": \"ms/tick\"}}]\n", v[1], v[n]
    }' > "$OUT"
echo "sprints (ms per tick): ${ms[*]}"
cat "$OUT"
