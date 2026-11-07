#!/usr/bin/env bash
# Запускает настоящий сервер Paper с собранным плагином и проверяет, что плагин включился.
# Запуск из корня репозитория после ./gradlew build в tests/minecraft/paper-plugin:
#   bash tests/minecraft/paper_smoke.sh
set -euo pipefail
MC=26.3
BUILD=244   # та же сборка, что paper-api 26.3.build.244-stable в build.gradle.kts
cd "$(dirname "$0")/paper-plugin"
rm -rf run && mkdir -p run/plugins
cp build/libs/*.jar run/plugins/
cd run

meta=$(curl -sf "https://fill.papermc.io/v3/projects/paper/versions/$MC/builds/$BUILD")
url=$(jq -r '.downloads."server:default".url' <<<"$meta")
sha=$(jq -r '.downloads."server:default".checksums.sha256' <<<"$meta")
curl -sfL -o paper.jar "$url"
echo "$sha  paper.jar" | sha256sum -c -
echo "eula=true" > eula.txt

# stdin сервера: ждём строку «Done (» в логе и отправляем stop
touch server.log
timeout 600 java -Xms1G -Xmx2G -jar paper.jar --nogui > server.log 2>&1 < <(
    until grep -q 'Done (' server.log; do sleep 2; done
    echo stop
) || { echo "server did not stop in time"; tail -50 server.log; exit 1; }

grep -E 'Enabling ExamplePlugin|Done \(' server.log
if grep -q 'Error occurred while enabling ExamplePlugin\|Could not load plugin' server.log; then
    tail -80 server.log
    exit 1
fi
grep -q 'Enabling ExamplePlugin' server.log
echo "paper smoke test passed"
