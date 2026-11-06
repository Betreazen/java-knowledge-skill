#!/usr/bin/env bash
# Регрессионные тесты scripts/BenchCompare.java на настоящих прогонах JMH 1.37 (Temurin 25, Windows 11).
# Запуск из корня репозитория: bash tests/bench_compare_test.sh
# before/after   — 5+5 прогонов в порядке ABBA: `+=` в цикле против StringBuilder (avgt)
# same-a/same-b  — 5+5 прогонов одного и того же кода
# thrpt-*        — 3+3 прогона в режиме thrpt
# edge/          — тот же прогон с другими единицами (ms.json), с NaN (nan.json) и с BOM (bom.json)
set -u
cd "$(dirname "$0")/.."
SCRIPT=java-knowledge/scripts/BenchCompare.java
D=tests/bench-compare
failures=0

check() { # name, expected exit, expected text in output, args...
    local name=$1 code=$2 text=$3
    shift 3
    local out
    out=$(java "$SCRIPT" "$@" 2>&1)
    local got=$?
    if [ "$got" != "$code" ] || ! grep -q -- "$text" <<<"$out"; then
        echo "FAIL $name: exit $got (want $code), text '$text'"
        echo "$out" | sed 's/^/    /'
        failures=$((failures + 1))
    else
        echo "ok   $name"
    fi
}

check "faster on real speed-up"      0 "faster"        "$D/before" "$D/after"
check "same on identical code"       0 "regressions: 0" "$D/same-a" "$D/same-b"
check "no faster on identical code"  0 "p = 0.8%"      "$D/same-a" "$D/same-b"
check "slower on regression"         1 "slower"        "$D/after" "$D/before"
check "thrpt: higher is better"      0 "faster"        "$D/thrpt-before" "$D/thrpt-after"
check "thrpt: regression"            1 "slower"        "$D/thrpt-after" "$D/thrpt-before"
check "few runs warning"             0 "too few runs"  "$D/thrpt-before" "$D/thrpt-after"
check "alloc per op printed"         0 "3136.0"        "$D/before" "$D/after"
check "single run warning"           0 "one run per side" "$D/same-a/run1.json" "$D/same-b/run1.json"
check "1 vs 5 runs"                  0 "same"          "$D/same-a/run1.json" "$D/same-b"
check "unit mismatch"                2 "unit-mismatch" "$D/same-a/run1.json" "$D/edge/ms.json"
check "NaN score"                    2 "invalid"       "$D/same-a/run1.json" "$D/edge/nan.json"
check "BOM accepted"                 0 "same"          "$D/edge/bom.json" "$D/same-a/run1.json"
check "no common benchmarks"         2 "no benchmark is present on both sides" "$D/before" "$D/thrpt-after"
check "not JSON"                     2 "cannot read input" "$SCRIPT" "$D/after"
check "negative threshold"           2 "non-negative"  "$D/before" "$D/after" --threshold -3
check "usage"                        2 "usage"         "$D/before"

if [ $failures -gt 0 ]; then
    echo "$failures test(s) failed"
    exit 1
fi
echo "all tests passed"
