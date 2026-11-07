# Моды Minecraft: платформы, загрузчики, тулчейн и публикация

Входной файл ветки Minecraft: всё, что нужно автору мода Minecraft: Java Edition вокруг самого кода. Покрыты: нумерация версий Minecraft с 2025–2026 годов, удаление обфускации в 26.1 и судьба маппингов (Mojmap, Yarn, Intermediary, Parchment, Quilt Mappings), загрузчики (Fabric, NeoForge, MinecraftForge, Quilt, Sinytra Connector, Architectury) и их Gradle-плагины (Loom, ModDevGradle, NeoGradle, ForgeGradle), создание проекта, IDE, отладка и hot swap, тесты и gametest без графики, мультилоадерные и мультиверсионные сборки, зависимости и jar-in-jar, публикация на Modrinth и CurseForge, лицензии и EULA Minecraft, GitHub Actions, сборки модов (packwiz, .mrpack, Prism, серверные паки). В конце даны шаблоны Fabric и NeoForge, собранные и запущенные в CI. Выбор загрузчика зависит от аудитории и нужного API, а не от жанра: тот же тулчейн обслуживает контентные, технические, магические моды, генерацию мира, QoL и интерфейс, клиентские рендер-моды, серверные моды, оптимизацию, библиотеки и сборки.

Актуально на ноябрь 2026: Minecraft 26.3 (2026-09-15), Java 25 для 26.1+ (Java 21 для 1.20.5–1.21.11), Fabric Loader 0.20.1, Fabric Loom 1.19.4, Fabric API 0.165.2+26.3, NeoForge 26.3.0.101, ModDevGradle 2.0.148, Gradle 9.9.0 (шаблоны закреплены на 9.8.2 и 9.2.1) (JDK 25 LTS подходит для 26.x, JDK 27 как последняя версия для модов не нужен). Пометки: `[не проверено]` — факт из одного вторичного источника, страница недоступна для чтения или код не собирался в CI; `[устарело]` — рядом названа замена; `проверено в CI` — собрано и запущено на GitHub Actions (ubuntu-latest, Temurin 25, 2026-11-07). Строки без пометки прочитаны в первоисточнике или в метаданных (Mojang, maven-metadata.xml, meta.fabricmc.net, Modrinth API). Соседние файлы: API игры, Mixin, данные и сеть — [minecraft-mod-api.md](minecraft-mod-api.md); измерение, профилирование и GameTest — [minecraft-performance-testing.md](minecraft-performance-testing.md); серверные плагины и датапаки — [minecraft-server-plugins.md](minecraft-server-plugins.md); документация, книги и эталонные кодовые базы — [minecraft-resources.md](minecraft-resources.md); общий тулчейн Java — [toolchain.md](toolchain.md); тесты — [testing.md](testing.md); конвейер работы — [pipeline.md](pipeline.md).

- [Стек по умолчанию](#стек-по-умолчанию)
- [Версии на ноябрь 2026](#версии-на-ноябрь-2026)
- [Что изменилось в 2025–2026](#что-изменилось-в-20252026)
- [1. Версии Minecraft с 2025–2026 годов](#1-версии-minecraft-с-20252026-годов)
- [2. Деобфускация в 26.1 и маппинги](#2-деобфускация-в-261-и-маппинги)
- [3. Загрузчики в ноябре 2026 и доли рынка](#3-загрузчики-в-ноябре-2026-и-доли-рынка)
- [4. Проект Fabric (26.x и 1.21.x)](#4-проект-fabric-26x-и-121x)
- [5. Проект NeoForge (и MinecraftForge)](#5-проект-neoforge-и-minecraftforge)
- [6. IDE, отладка, hot swap, отладка Mixin](#6-ide-отладка-hot-swap-отладка-mixin)
- [7. Тесты: модульные и gametest без графики](#7-тесты-модульные-и-gametest-без-графики)
- [8. Мультилоадерные и мультиверсионные сборки](#8-мультилоадерные-и-мультиверсионные-сборки)
- [9. Зависимости: репозитории модов, jar-in-jar, shading](#9-зависимости-репозитории-модов-jar-in-jar-shading)
- [10. Публикация, версии, changelog и лицензии](#10-публикация-версии-changelog-и-лицензии)
- [11. CI на GitHub Actions](#11-ci-на-github-actions)
- [12. Сборки: packwiz, .mrpack, лаунчеры, серверные паки](#12-сборки-packwiz-mrpack-лаунчеры-серверные-паки)
- [Шаблоны](#шаблоны)
- [Что в старых туториалах неверно](#что-в-старых-туториалах-неверно)
- [Чеклист ревью проекта мода](#чеклист-ревью-проекта-мода)
- [Открытые вопросы](#открытые-вопросы)

---

## Стек по умолчанию

Таблица для нового публичного мода на последней версии (26.3). Решение зависит от аудитории и нужного API, жанр не влияет.

| Решение | По умолчанию | Альтернатива | Не брать в новый проект |
|---|---|---|---|
| Загрузчик | Fabric (тонкий API, выход на день релиза) или NeoForge (большой встроенный API, аудитория 1.21.1) | Оба сразу через одну кодовую базу (см. «Мультилоадер»); Forge 47.x только для старых сборок 1.20.1 | Quilt с QSL (для 26.1+ не выпускается); Sinytra Connector как способ «портировать» мод на NeoForge |
| Gradle-плагин | Fabric: Loom 1.19.4, id `net.fabricmc.fabric-loom`; NeoForge: ModDevGradle 2.0.148, id `net.neoforged.moddev` | NeoGradle 7.1.39; ForgeGradle 7 для Forge; Architectury Loom (`dev.architectury.loom-no-remap`) | id `fabric-loom` и `modImplementation` на 26.x; ForgeGradle 6 для текущего Forge `[устарело]`: ForgeGradle 7 |
| Маппинги | Имена Mojang прямо в jar (26.1+), строки `mappings` нет | ≤1.21.11: `loom.officialMojangMappings()`, при желании со слоем Parchment | Yarn, Intermediary, Quilt Mappings `[устарело]`: Mojang names |
| Мультилоадер и мультиверсия | MultiLoader-Template (без сторонних плагинов) плюс Stonecutter для нескольких версий Minecraft | Architectury (быстрее писать, но лишняя зависимость для игроков); Cloche, Unimined, ModStitch | Слитые в один jar сборки загрузчиков (Forgix) как вариант по умолчанию |
| Конфигурация | NeoForge: встроенная система конфигов; Fabric: решение в `minecraft-mod-api.md` §15 | Библиотека конфигов как отдельная зависимость мода (например Cloth Config) | Вшивать библиотеку конфигов в свой jar |
| Публикация | mod-publish-plugin 2.2.1 (`me.modmuss50.mod-publish-plugin`) | Minotaur 2.10.0 (только Modrinth); CurseForgeGradle 1.3.33 (только CurseForge); Action `Kir-Antipov/mc-publish@v3.3` | CurseGradle `[устарело]`: mod-publish-plugin или CurseForgeGradle |
| CI | GitHub Actions: `checkout@v7`, `setup-java@v6` (Temurin 25), `gradle/actions/setup-gradle@v6`, `./gradlew build` | `neoforged/actions-modding` для общего кэша ресурсов Minecraft | Старые мажоры действий из примеров (checkout v4–v6, setup-java v4–v5), `1.19-SNAPSHOT` в релизном конвейере |
| Java | 25 для 26.1+ | 21 для 1.20.5–1.21.11 | 17 и 21 для 26.x |

## Версии на ноябрь 2026

Проверено 2026-11-07 по Mojang `version_manifest_v2.json`, maven-metadata.xml и meta.fabricmc.net.

| Что | Версия и дата | Примечание |
|---|---|---|
| Minecraft, последний релиз | **26.3** (2026-09-15) | поле `latest.release` манифеста |
| Minecraft, последний снапшот | 26.4-snapshot-7 (2026-11-03) | снапшоты называются `<цель>-snapshot-N`, затем `-pre-N` и `-rc-N` |
| Релизы 2026 года | 26.1 (03-24), 26.1.1 (04-01), 26.1.2 (04-09), 26.2 (06-16), 26.3 (09-15) | формат год.дроп.хотфикс |
| Последний «1.x» | **1.21.11** (2025-12-09) | последняя обфусцированная версия, последняя с `client_mappings` в JSON версии |
| Первая необфусцированная сборка | 26.1-snapshot-1 (2025-12-16) | в JSON только `client` и `server`, файлов `*_mappings` нет |
| Java игры | 1.18–1.20.4: 17; 1.20.5–1.21.11: **21**; 26.1+: **25** | поле `javaVersion` в JSON версий; в клиенте Microsoft Build of OpenJDK 25 |
| Fabric Loader | **0.20.1** (2026-10-19) | один jar для версий от 1.14 до снапшотов 26.4 |
| Fabric Loom | **1.19.4** (2026-11-06); 1.20.0-alpha.19 в тесте | маркеры плагинов `net.fabricmc.fabric-loom` и `net.fabricmc.fabric-loom-remap` оба на 1.19.4 |
| Fabric API | 26.3: **0.165.2+26.3**; 26.2: 0.161.0+26.2; 26.1.2: 0.155.3+26.1.2; 1.21.11: 0.141.6+1.21.11; 1.21.1: 0.116.17+1.21.1 | для снапшотов 26.4 есть 0.165.4+26.4 |
| Yarn / Intermediary | последние 1.21.11+build.6 / 1.21.11 | для 26.x не выпускаются |
| NeoForge | 26.3: **26.3.0.101**; 26.2: 26.2.0.93; 26.1.2: 26.1.2.126; 1.21.11: 21.11.48; 1.21.1: 21.1.265 | четыре части `MC.дроп.хотфикс.сборка` с 26.1; первые сборки 26.3 были `-beta` |
| ModDevGradle | **2.0.148** (2026-09-29) | id `net.neoforged.moddev` и `net.neoforged.moddev.legacyforge` |
| NeoGradle | 7.1.39 (2026-09-15) | id `net.neoforged.gradle.userdev` |
| Gradle | **9.9.0** (2026-11-06); 9.8.2 (2026-10-19); 8.14.6 в сопровождении | MDK NeoForge 26.3 закреплён на 9.2.1 |
| MinecraftForge | 26.3-66.1.5; 26.2-65.1.6; 26.1.2-64.1.6; 1.21.11-61.2.5; 1.21.1-52.1.21 | ForgeGradle 7.0.40 (id `net.minecraftforge.gradle`) |
| Quilt Loader | 0.30.2 стабильный, 0.31.0-beta.6 (2026-11-04) | QSL и QFAPI для 26.1+ не выпускаются |
| Sinytra Connector | 3.1.0-beta.4+26.2 | только NeoForge, на один дроп позади |
| Architectury API | 22.0.5 для 26.3; 21.1.12 для 26.2 | Architectury Loom 1.17.493, architectury-plugin 3.5.170 |
| Parchment | последние данные: parchment-1.21.11 2025.12.20 | артефактов для 26.x нет (не нужны) |
| JetBrains Runtime | JBR 25.0.4.1 (2026-10) | поддерживает `-XX:+AllowEnhancedClassRedefinition` |
| mod-publish-plugin / Minotaur / CurseForgeGradle | 2.2.1 / 2.10.0 / 1.3.33 | см. раздел 10 |
| Stonecutter | 0.9.8 (2026-08-31) | см. раздел 8 |
| GitHub Actions | checkout v7, setup-java v6, gradle/actions v6, upload-artifact v7 | мажоры проверены 2026-11-07 |

## Что изменилось в 2025–2026

**Ключевые выводы**

- **Версии по годам.** С 26.1-snapshot-1 (2025-12-16) версии называются `YY.дроп.хотфикс`: 26.1, 26.2, 26.3 — три дропа 2026 года, примерно один в квартал. Выпуска «1.22» нет. Парсеры версий с префиксом `1.` ломаются на `26.3`.
- **Без обфускации с 26.1.** Клиент и сервер выходят с оригинальными именами Mojang: классы, поля, методы, параметры и локальные переменные. Файлов маппингов в JSON версий нет. EULA и Usage Guidelines действуют так же, исходники читаемы, но не открыты.
- **Java 25.** Игра 26.1+ требует Java 25 (`java-runtime-epsilon`). Мод компилируется с `--release 25` (Fabric) или toolchain 25 (NeoForge). Для линии 1.21.x остаётся Java 21.
- **Yarn и Intermediary заканчиваются на 1.21.11.** Последняя сборка Yarn `1.21.11+build.6`, последний Intermediary `1.21.11`. Fabric рекомендует имена Mojang для всех новых модов. Quilt Mappings тоже закрыты для новых версий.
- **Два плагина Loom.** `net.fabricmc.fabric-loom` — без ремаппинга, для 26.1+. `net.fabricmc.fabric-loom-remap` — для обфусцированных версий ≤1.21.11. Старый id `fabric-loom` остаётся совместимым алиасом ремапящего плагина.
- **Нет `modImplementation`, `remapJar` и `mappings` на 26.x.** Вместо `modImplementation` и `modCompileOnly` используются `implementation` и `compileOnly`. Вместо `remapJar` берётся `jar`. Строки `mappings` нет совсем.
- **Идентификатор `fabric` исчез.** В `fabric.mod.json` зависимость пишется как `"fabric-api": "*"`, алиас `fabric` убран в 26.1.
- **Class tweakers.** Access wideners Fabric стали class tweakers (`*.classtweaker`, заголовок `classTweaker v1 official`; Loader ≥ 0.18.0, Loom ≥ 1.12). Ключ `accessWidener` в манифесте сохранён для совместимости. Проверка: `./gradlew validateAccessWidener`.
- **Версии NeoForge из четырёх частей.** `26.3.0.101` = Minecraft 26.3, хотфикс 0, сборка 101. Суффикс `-beta` означает, что ломающие изменения ещё допустимы. Для 1.21.x остаётся схема `21.1.x`.
- **Quilt.** Quilt Loader поддерживается и запускает большинство модов Fabric. Quilt Mappings, QSL, Quilt Kotlin Libraries и Quilted Fabric API для 26.1+ закрыты. Для нового мода берите Fabric.
- **Forge.** MinecraftForge не мёртв: сборки есть для каждого релиза (26.3-66.1.5), тулчейн ForgeGradle 7 с «Mavenizer» и EventBus 7. Центр тяжести экосистемы Forge остался на 1.20.1.
- **Мод 1.21.11 и ранее не запустится на 26.x** даже с тем же кодом: он ссылается на имена intermediary или SRG. Нужна минимум перекомпиляция.
- **Тулчейн NeoForge.** Parchment на 26.x убирается, нужен Gradle ≥ 9.1 и Java 25. MDG пропускает декомпиляцию и перекомпиляцию Minecraft при `CI=true`.

## 1. Версии Minecraft с 2025–2026 годов

**Ключевые выводы**

- **Схема.** `YY.дроп.хотфикс`: 26.1 — первый дроп 2026 года, 26.1.1 и 26.1.2 — его хотфиксы. Bedrock делит только префикс года, номера дропов у него свои.
- **Снапшоты.** Раньше `25w41a`, теперь `26.1-snapshot-1` … `26.1-snapshot-11`, `26.1-pre-1`, `26.1-rc-1` (с дефисами, в отличие от `1.21.11-pre1`). Исключение: апрельский снапшот `26w14a` (2026-04-01) назван по-старому.
- **Ритм 2026 года.** 26.1 в марте, 26.2 в июне, 26.3 в сентябре, снапшоты 26.4 с 2026-09-22. Пререлизы выходят примерно за две недели до релиза. Закладывайте время на портирование каждого дропа.
- **Сравнение версий.** В `fabric.mod.json` пишите `"minecraft": "~26.3"` (любой 26.3.x). В `neoforge.mods.toml` снапшоты, пререлизы и релиз-кандидаты не гарантированно правильно сортируются в Maven-диапазонах вроде `[26.3]` (предупреждение MDK).
- **Java по версиям** (поле `javaVersion` JSON версии): 1.17 — 16; 1.18–1.20.4 — 17; **1.20.5–1.21.11 — 21**; **26.1+ — 25**. Сборка 1.21.1 возможна на JDK 25 с toolchain 21.
- **Какие версии востребованы** (по числу модов на Modrinth, см. раздел 3): 1.20.1 (хаб Forge), 1.21.1 (долгоживущий хаб NeoForge), 1.21.11 (последняя обфусцированная, много Fabric), 26.1.2, 26.3. Мультиверсионная поддержка обычно охватывает 1.20.1 / 1.21.1 / 26.1.2 / последнюю.
- **Что путают старые туториалы:** «следующая 1.22», снапшоты `YYwWWa` в матрицах CI, Java 21 для «последней» версии, разбор версий регулярным выражением с префиксом `1.`.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Minecraft new version numbering system](https://www.minecraft.net/en-us/article/minecraft-new-version-numbering-system) | Mojang, 2025 | Официальное объяснение схемы год.дроп.хотфикс и имён снапшотов |
| [Minecraft 26.1 Snapshot 1](https://www.minecraft.net/en-us/article/minecraft-26-1-snapshot-1) | Mojang, 2025-12-16 | Требование Java 25, встроенный Microsoft OpenJDK 25, все сборки без обфускации |
| [Minecraft Java Edition 26.1](https://www.minecraft.net/en-us/article/minecraft-java-edition-26-1) | Mojang, 2026-03 | Заметки к первому релизу с годовой нумерацией |
| [Minecraft Java Edition 26.1.1](https://www.minecraft.net/en-us/article/minecraft-java-edition-26-1-1) | Mojang, 2026-04 | Пример нумерации хотфикса |
| [Minecraft 26.1 Pre-Release 1](https://www.minecraft.net/en-us/article/minecraft-26-1-pre-release-1) | Mojang, 2026-03 | Именование пререлизов `26.1-pre-1` |
| [version_manifest_v2.json](https://piston-meta.mojang.com/mc/game/version_manifest_v2.json) | Mojang, живой | Машиночитаемый список версий, времена релизов, JSON версий с `javaVersion` |
| [Versioning – NeoForged docs](https://docs.neoforged.net/docs/gettingstarted/versioning) | NeoForged, 2026 | Календарные версии Minecraft, схема NeoForge из четырёх частей, советы по версиям мода |
| [2025: Big Changes are Coming](https://neoforged.net/news/2025-retrospection/) | NeoForged, 2026-01-01 | Схема версий, удаление обфускации, планы по установщику и тулчейну |
| [Where Did Minecraft 1.22 Go? The 26.x Version Numbers Explained](https://space-node.net/blog/minecraft-26-version-numbers-explained) | Space-Node, 2026 | Вторичное объяснение для игроков и админов серверов `[не проверено]` |
| [Minecraft Adopts a New Version Numbering System](https://beebom.com/minecraft-new-version-numbering-system-for-java-edition/) | Beebom, 2025 | Вторичное новостное освещение `[не проверено]` |

## 2. Деобфускация в 26.1 и маппинги

**Ключевые выводы**

- **Что изменил Mojang** (статья «Removing obfuscation in Java Edition», 2025-10-30): jar клиента и сервера содержат оригинальные имена, включая параметры методов и локальные переменные. Карты обфускации из JSON убраны, в каждый jar добавлен файл LICENSE со ссылкой на EULA. Экспериментальные необфусцированные сборки выходили рядом со снапшотами 1.21.11, чтобы авторы тулчейнов успели подготовиться.
- **Проверено по метаданным.** JSON версии 1.21.11 содержит загрузки `client`, `client_mappings`, `server`, `server_mappings`. JSON версий 26.1-snapshot-1 … 26.4-snapshot-7 — только `client` и `server`.
- **Mojmap** больше не маппинг: это просто имена в jar. Все загрузчики компилируются и работают с одними именами. Строка `mappings loom.officialMojangMappings()` на 26.x неверна: в невремапящем Loom строки `mappings` нет.
- **Intermediary** («стабильные» имена `class_1234`) для 26.x не выпускается, последний артефакт `net.fabricmc:intermediary:1.21.11`. Моды Fabric для 26.x не ремапятся при сборке.
- **Yarn.** Fabric не видит смысла поддерживать Yarn в прежнем виде. Последняя сборка `1.21.11+build.6`, вклады для старых версий принимаются.
- **Quilt Mappings** закрыты для новых версий (блог Quilt, 2026-02-03). Enigma остаётся, Hashed mappings в режиме сопровождения, форки Unpick, Stitch и Tiny Remapper заархивированы.
- **Parchment** (имена параметров и javadoc поверх Mojmap) нужен для ≤1.21.11, последние данные `parchment-1.21.11:2025.12.20`. Для 26.x официальные имена параметров есть, NeoForge пишет, что Parchment можно убрать (оставить только ради javadoc). Артефактов `parchment-26.x` на maven.parchmentmc.org нет.
- **SRG и MCP** (имена времени выполнения Forge ≤1.20.1) остались только в старых тулчейнах Forge. MDG Legacy реобфусцирует в SRG для 1.17–1.20.1.
- **Ответ Fabric.** Loom разделён на два id: `net.fabricmc.fabric-loom` (26.1+) и `net.fabricmc.fabric-loom-remap` (≤1.21.11). В скриптах 26.x `modImplementation` становится `implementation`, `remapJar` — `jar`. Имена Fabric API переименованы по терминологии Mojang (`ItemGroupEvents` → `CreativeModeTabEvents`). Для Mixin на 26.1 нужна IntelliJ IDEA 2025.3+.
- **Перенос мода с Yarn.** Сначала выполните на 1.21.11, потом поднимайте Minecraft. Сделайте резервную копию: исходники перезаписываются.

```bash
# Loom 1.13+, ещё на 1.21.11 с yarn
./gradlew migrateMappings --mappings "net.minecraft:mappings:1.21.11" --overrideInputsIHaveABackup
./gradlew migrateClientMappings --mappings "net.minecraft:mappings:1.21.11" --overrideInputsIHaveABackup   # раздельный клиентский source set
./gradlew migrateClassTweakerMappings --mappings "net.minecraft:mappings:1.21.11" --overrideInputsIHaveABackup # access wideners и class tweakers
# затем замените   mappings "net.fabricmc:yarn:${project.yarn_mappings}:v2"
# на               mappings loom.officialMojangMappings()
```

- **Ограничения миграции.** Loom не умеет Kotlin: для него есть плагин IntelliJ **Ravel** с графическим переносом. Цели Mixin (строковые дескрипторы) проверяйте вручную. Имена ищите через mappings.dev или Linkie.
- **Ответ NeoForge.** NeoForge и Forge используют имена Mojang с 1.17, миграции маппингов не было. Видимые изменения: убрать строки Parchment (`neoForge.parchment.minecraftVersion`, `neoForge.parchment.mappingsVersion`), Java 25, Gradle ≥ 9.1, новая схема версий.
- **Access transformers и class tweakers.** Fabric: `*.classtweaker` с заголовком `classTweaker v1 official`, проверка `validateAccessWidener`. NeoForge: `META-INF/accesstransformer.cfg` определяется автоматически, в MDG рекомендуется `validateAccessTransformers = true`.
- **Mixin на 26.x.** Refmap и ремаппинг не нужны: имена в разработке и в рабочем окружении совпадают. Loom 1.12+ уже отключил устаревший Mixin AP (`useLegacyMixinAp`). Стектрейсы и краш-репорты читаются без перевода имён.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Removing obfuscation in Java Edition](https://www.minecraft.net/en-us/article/removing-obfuscation-in-java-edition) | Mojang, 2025-10-30 | Первоисточник: что убрано, файл LICENSE, EULA остаётся в силе |
| [Removing Obfuscation from Fabric](https://fabricmc.net/2025/10/31/obfuscation.html) | FabricMC, 2025-10-31 | Судьба Yarn и Intermediary, планы Loom, рекомендация имён Mojang |
| [Fabric for Minecraft 26.1](https://fabricmc.net/2026/03/14/261.html) | FabricMC, 2026-03-14 | Смена id плагина, `modImplementation` → `implementation`, `remapJar` → `jar`, переименования API |
| [Migrating Mappings (porting)](https://docs.fabricmc.net/develop/porting/mappings/) | Fabric Docs, 2026 | Обзор миграции через Loom и Ravel |
| [Migrating Mappings using Loom (1.21.11)](https://docs.fabricmc.net/1.21.11/develop/porting/mappings/loom) | Fabric Docs, 2026 | Точные команды `migrateMappings` и флаги |
| [Porting index](https://docs.fabricmc.net/develop/porting/) | Fabric Docs, 2026 | Точка входа в портирование на каждый дроп |
| [Class Tweakers](https://docs.fabricmc.net/develop/class-tweakers/) | Fabric Docs, 2026 | От access wideners к class tweakers, формат файла, транзитивные записи |
| [Interface injection](https://docs.fabricmc.net/develop/class-tweakers/interface-injection) | Fabric Docs, 2026 | Внедрение интерфейсов через class tweaker |
| [Enum extension](https://docs.fabricmc.net/develop/class-tweakers/enum-extension) | Fabric Docs, 2026 | Расширение enum, новое в Loader 0.19 и Loom 1.17 (26.2) |
| [NeoForge for 26.1 Snapshots](https://neoforged.net/news/26.1snapshots/) | NeoForged, 2025-12-26 | Убрать Parchment, MDG ≥ 2.0.134, Gradle ≥ 9.1, Java 25 |
| [NeoForge for Minecraft 26.1](https://neoforged.net/news/26.1release/) | NeoForged, 2026-03-24 | Версии из четырёх частей, ItemStackTemplate, переименования GuiGraphicsExtractor |
| [Bringing Quilt into the non-obfuscated era](https://quiltmc.org/en/blog/2026-02-03-non-obfuscated-updates/) | QuiltMC, 2026-02-03 | Конец Quilt Mappings, QSL и QFAPI закрыты для 26.1 |
| [Parchment – Getting Started](https://parchmentmc.org/docs/getting-started) | ParchmentMC, живой | Слоистые маппинги для ≤1.21.11, примеры для Loom и ForgeGradle |
| [Parchment maven data index](https://maven.parchmentmc.org/org/parchmentmc/data/) | ParchmentMC, живой | Проверить, для каких версий Minecraft есть данные |
| [Ravel (mapping migration plugin)](https://github.com/badasintended/ravel) | badasintended, 2025 | Плагин IntelliJ для переноса маппингов, умеет Kotlin `[не проверено]` |
| [mappings.dev](https://mappings.dev/) | сообщество, живой | Поиск имён между наборами маппингов для старых версий |
| [Minecraft Java modding is about to get a lot easier](https://www.gamingonlinux.com/2025/10/minecraft-java-modding-is-about-to-get-a-lot-easier-and-more-interesting) | GamingOnLinux, 2025 | Вторичное освещение анонса `[не проверено]` |

## 3. Загрузчики в ноябре 2026 и доли рынка

**Ключевые выводы**

- **Fabric** (Fabric Loader, Fabric API, Fabric Loom). Лёгкий загрузчик с упором на Mixin, быстро обновляется: сборки Fabric API есть для каждого снапшота 26.4. Loader 0.20.1 работает на всех версиях от 1.14 до снапшотов 26.4. Fabric API — отдельный мод (`fabric-api`, `0.165.2+26.3`). Исторически силён в клиентских, рендер-, производительных и QoL-модах, но используется во всех жанрах.
- **NeoForge** (FancyModLoader, NeoForge, ModDevGradle или NeoGradle). Форк Forge с 2023 года. Большой встроенный API: capabilities и transfer API (переработан в 1.21.9), реестры, вложения данных, события, сетевые пакеты, конфигурация, datagen. 1.21.1 — его долгоживущий хаб (21 978 модов на Modrinth). Сотрудники NeoForged ожидали, что 26.1 станет «следующей стабильной версией для модов» (личная оценка, не обещание).
- **MinecraftForge.** Выпускает сборки для каждого релиза: 26.1.2 (64.1.6), 26.2 (65.1.6), 26.3 (66.1.5, 2026-11-03). Мажор растёт с каждой версией Minecraft (62 для 26.1, 66 для 26.3). Тулчейн ForgeGradle 7 (`net.minecraftforge.gradle` `[7.0.17,8)`, последний 7.0.40), EventBus 7 и аннотационный процессор `eventbus-validator`. Центр экосистемы — **1.20.1** (25 543 мода Forge на Modrinth против 2 689 для 26.3).
- **Quilt.** Loader поддерживается (0.30.2, 0.31.0-beta.6) и грузит большинство модов Fabric. Но Quilt Mappings, QSL, Quilt Kotlin Libraries и Quilted Fabric API для 26.1+ закрыты. Для нового мода целитесь в Fabric, а тег Quilt на Modrinth ставьте только после теста.
- **Sinytra Connector** запускает моды Fabric на NeoForge (с Forgified Fabric API). Активен, но в бете и на один дроп позади (сборки для 26.3 нет). Полезен игрокам, разработчикам не заменяет честный порт на NeoForge.
- **Architectury** (API, Architectury Loom, architectury-plugin): общий модуль плюс кросс-загрузчиковый API (события, реестры, сеть). Для 26.x нужен id `dev.architectury.loom-no-remap` (Loom 1.17+). Документация указывает, что 26.1 и 26.1.1 не поддерживаются (26.1.2 поддерживается).
- **Другие мультилоадерные тулчейны:** Unimined, Cloche, ModStitch (раздел 8).
- **Доли рынка** (Modrinth search API, `total_hits`, только моды, 2026-11-07):

| Загрузчик | Все версии | 26.3 | 26.1.2 | 1.21.11 | 1.21.1 | 1.20.1 |
|---|---|---|---|---|---|---|
| Fabric | 47 751 | 9 413 | 13 697 | 18 310 | 20 026 | 17 945 |
| NeoForge | 30 622 | 5 360 | 8 538 | 8 392 | 21 978 | 5 167 |
| Forge | 35 648 | 2 689 | 3 542 | 4 587 | 5 577 | 25 543 |
| Quilt | 10 631 | 2 744 | 3 517 | 4 495 | 4 869 | 4 997 |

- **Как читать таблицу.** У проектов несколько тегов загрузчиков, поэтому столбцы пересекаются. Fabric лидирует на свежих версиях, NeoForge — на 1.21.1, Forge — на 1.20.1. Переход на 26.x ещё идёт: у 26.3 около двух третей числа модов Fabric для 26.1.2. У CurseForge нет публичного API статистики без ключа, его цифры `[не проверено]`. Всего на Modrinth 167 161 проект, 1,55 млн версий, 72 114 авторов. Воспроизведение: `GET https://api.modrinth.com/v2/search?limit=1&facets=[["categories:neoforge"],["project_type:mod"],["versions:1.21.1"]]`, поле `total_hits`.
- **Как выбрать загрузчик.** Нужен новейший дроп в день релиза и тонкий API — Fabric. Нужен большой встроенный API (capabilities, transfer, экраны конфигов, события реестров) и аудитория сборок на 1.21.1 — NeoForge. Старые сборки 1.20.1 — Forge 47.x или MDG Legacy. Лучший вариант для публичного мода: один репозиторий, общий код, выходы Fabric и NeoForge (раздел 8). Серверные плагины (Paper) — отдельный стек, см. [minecraft-server-plugins.md](minecraft-server-plugins.md).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Fabric – Develop](https://fabricmc.net/develop/) | FabricMC, живой | Актуальные Loader, Loom и API для каждой версии Minecraft |
| [Fabric for Minecraft 26.2](https://fabricmc.net/2026/06/15/262.html) | FabricMC, 2026-06-15 | Loom 1.17, расширения enum, заметки о бэкенде Vulkan |
| [Fabric for Minecraft 26.3](https://fabricmc.net/2026/09/15/263.html) | FabricMC, 2026-09-15 | Удалённые реестры, трансформеры блоков, ввод GLFW → SDL |
| [Fabric for Minecraft 1.21.11](https://fabricmc.net/2025/12/05/12111.html) | FabricMC, 2025-12-05 | Заметки о последней обфусцированной версии |
| [Fabric Loader (docs)](https://docs.fabricmc.net/develop/loader/) | Fabric Docs, 2026 | Понятия Loader, точки входа |
| [fabric.mod.json reference](https://docs.fabricmc.net/develop/loader/fabric-mod-json) | Fabric Docs, 2026 | Полная схема метаданных |
| [NeoForged news index](https://neoforged.net/) | NeoForged, живой | Релизные записи по версиям Minecraft |
| [NeoForge 21.11 for Minecraft 1.21.11](https://neoforged.net/news/21.11release/) | NeoForged, 2025-12-09 | Последняя обфусцированная линия NeoForge |
| [The Transfer Rework](https://neoforged.net/news/21.9-transfer-rework/) | NeoForged, 2025-09-30 | Новый API передачи предметов, жидкостей и энергии |
| [Mitigating Network Vulnerabilities](https://neoforged.net/news/mitigating-vulnerabilities-network/) | NeoForged, 2026-05-11 | Исправления уязвимостей сетевого слоя |
| [NeoForge project listing](https://projects.neoforged.net/neoforged/neoforge) | NeoForged, живой | Последние версии NeoForge |
| [MinecraftForge files](https://files.minecraftforge.net/) | команда Forge, живой | Загрузки Forge и рекомендуемые сборки |
| [Forge 26.3 changelog](https://maven.minecraftforge.net/net/minecraftforge/forge/26.3-66.1.5/forge-26.3-66.1.5-changelog.txt) | команда Forge, 2026-11 | Актуальные изменения Forge (FG7, устаревания EventBus) |
| [Forge 26.1 changelog 62.0.2](https://maven.minecraftforge.net/net/minecraftforge/forge/26.1-62.0.2/forge-26.1-62.0.2-changelog.txt) | команда Forge, 2026-03 | Переход Forge на необфусцированный код и Java 25, MDK с FG 7.0.17 `[не проверено]` |
| [MinecraftForge GitHub](https://github.com/MinecraftForge/MinecraftForge) | команда Forge, живой | Исходники, задачи |
| [Quilt blog](https://quiltmc.org/en/blog/1/) | QuiltMC, живой | Записи о статусе проекта |
| [Sinytra Connector](https://github.com/Sinytra/Connector) | Sinytra, 2026 | Слой Fabric-на-NeoForge, статус и версии |
| [Connector docs & compatibility list](https://connector.sinytra.org) | Sinytra, живой | Какие моды Fabric работают под Connector |
| [Forgified Fabric API](https://github.com/Sinytra/ForgifiedFabricAPI) | Sinytra, 2026 | Fabric API, переписанный для NeoForge |
| [Architectury docs – Setup (source)](https://github.com/architectury/architectury-docs/blob/main/docs/getting-started/setup.mdx) | Architectury, 2026 | Таблица требований для 26.3, `loom-no-remap` |
| [Architectury API](https://github.com/architectury/architectury-api) | Architectury, 2026 | Исходники кросс-загрузчикового API |
| [Modrinth API v2 docs](https://docs.modrinth.com/api/) | Modrinth, живой | Фильтры поиска для приведённых цифр |
| [Modrinth statistics endpoint](https://api.modrinth.com/v2/statistics) | Modrinth, живой | Общие цифры платформы |

## 4. Проект Fabric (26.x и 1.21.x)

**Ключевые выводы**

- **Старт.** [Генератор шаблона](https://fabricmc.net/develop/template/) (имя мода, пакет, версия Minecraft; в «Advanced Options» Kotlin, Kotlin DSL и datagen) или клон `FabricMC/fabric-example-mod` (ветка на каждую версию Minecraft, текущая `26.3`). Плагин IntelliJ Minecraft Development 1.9.0 (2026-10-17, IDEA 2026.1–2026.3) тоже имеет мастер Fabric и шаблоны классов «Fabric 26.1+»; с 1.8.20 он прячет выбор Yarn для 26.1+.
- **Требования (26.3).** JDK 25 в Gradle JVM (IntelliJ → Settings → Build Tools → Gradle → Gradle JVM), Gradle 9.x через wrapper (в примере 9.8.2), IntelliJ IDEA ≥ 2025.3. Путь проекта и имя пользователя ОС должны быть ASCII: документация предупреждает, что не-ASCII ломает часть инструментов.
- **Два варианта Loom.**

```groovy
// 26.1+ (без обфускации): без mappings и ремаппинга
plugins { id 'net.fabricmc.fabric-loom' version "${loom_version}" }
dependencies {
    minecraft "com.mojang:minecraft:${minecraft_version}"
    implementation "net.fabricmc:fabric-loader:${loader_version}"
    implementation "net.fabricmc.fabric-api:fabric-api:${fabric_api_version}"
}

// <= 1.21.11 (обфусцировано): ремапящий Loom
plugins { id 'net.fabricmc.fabric-loom-remap' version "${loom_version}" }
dependencies {
    minecraft "com.mojang:minecraft:1.21.11"
    mappings loom.officialMojangMappings()   // или loom.layered { officialMojangMappings(); parchment("org.parchmentmc.data:parchment-1.21.11:2025.12.20@zip") }
    modImplementation "net.fabricmc:fabric-loader:${loader_version}"
    modImplementation "net.fabricmc.fabric-api:fabric-api:0.141.6+1.21.11"
}
```

- **Какой jar публиковать.** `jar` на 26.x, `remapJar` на ≤1.21.11. Пример Fabric использует `loom_version=1.19-SNAPSHOT`; для воспроизводимых сборок закрепляйте `1.19.4` (так сделано в шаблоне ниже).
- **Разрешение плагина.** Loom лежит на maven.fabricmc.net, поэтому в `settings.gradle` нужен `pluginManagement { repositories { maven { name = 'Fabric'; url = 'https://maven.fabricmc.net/' }; mavenCentral(); gradlePluginPortal() } }`.
- **Раздельные source sets.** `loom { splitEnvironmentSourceSets(); mods { "modid" { sourceSet sourceSets.main; sourceSet sourceSets.client } } }` даёт гарантию при компиляции, что общий код не трогает клиентские классы (классическая причина `NoClassDefFoundError` на выделенном сервере). Клиентская точка входа лежит в `src/client/java`, jar остаётся один.
- **Главное в `fabric.mod.json` (26.3).** `"schemaVersion": 1`, `id`, `"environment": "*"|"client"|"server"`, `entrypoints.main/client/server`, `mixins`, `depends: { "fabricloader": ">=0.20.1", "minecraft": "~26.3", "java": ">=25", "fabric-api": "*" }`. Зависите от `fabric-api`, не от `fabric`. В конфиге Mixin `"compatibilityLevel": "JAVA_25"`.
- **Задачи и запуски.** `runClient`, `runServer` (каталог `run/`, VM-аргумент `-Dfabric.development=true`), `genSources` (на 26.x исходники с именами Mojang и параметрами), `runDatagen` (через `fabricApi { configureDataGeneration { client = true } }`, вывод в `src/main/generated`), `runClientGameTest` (через `fabricApi { configureTests { ... } }`), `validateAccessWidener`, `migrateMappings` (≤1.21.11). Свои запуски: `loom { runs { … } }` (`inherit client`, `jvmArguments`, `programArguments`, `runDirectory`).
- **Datagen.** Точка входа `"fabric-datagen"` в `fabric.mod.json`, генератор лежит в клиентском source set. С 26.2 ванильный datagen работает по ID-держателям (`BlockIds`, `ItemIds`), а не по экземплярам `Block` и `Item`.
- **Продакшн-запуски.** `net.fabricmc.loom.task.prod.ClientProductionRunTask` и `ServerProductionRunTask` запускают собранный jar в настоящей установке Fabric: используйте их для дымовых тестов в CI (`useXVFB = true` для безголового клиента, опционально захват Tracy). Серверному варианту нужны `installerVersion`, `loaderVersion`, `minecraftVersion`. Дополнительные моды для таких запусков кладутся в конфигурацию `productionRuntimeMods`.
- **Отладочное логирование.** `loom.log4jConfigs.from "log4j-dev.xml"` с вашим логгером на `level="debug"`. Свойства Loader: `-Dfabric.log.level=debug`, `-Dfabric.debug.disableModIds=modA,modB`, `-Dfabric.debug.throwDirectly=true`, `-Dfabric.debug.logClassLoadErrors=true`, `-Dfabric.debug.replaceVersion=mod:1.2.3`, `-Dfabric.addMods=<путь>`.
- **Что неверно в старых туториалах:** `modImplementation` на 26.x, `mappings "net.fabricmc:yarn:…"`, `remapJar` как файл для загрузки, id `fabric-loom`, toolchain Java 17 или 21, зависимость `"fabric": "*"`, `HudRenderCallback` (удалён, теперь `HudElementRegistry`), `ItemGroupEvents` (теперь `CreativeModeTabEvents`), `ColorProviderRegistry` (→ `BlockColorRegistry`), `TradeOfferHelper` (торговля теперь через данные), `FuelRegistry`, `CompostingChanceRegistry`, `StrippableBlockRegistry` (удалены в 26.3, вместо них компоненты данных и трансформеры блоков).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Fabric Template Mod Generator](https://fabricmc.net/develop/template/) | FabricMC, живой | Генерирует готовый архив проекта |
| [fabric-example-mod](https://github.com/FabricMC/fabric-example-mod) | FabricMC, 2026 | Эталонные файлы сборки по веткам Minecraft |
| [Creating a Project](https://docs.fabricmc.net/develop/getting-started/creating-a-project) | Fabric Docs, 2026 | Пошаговая работа с генератором |
| [Setting up a Development Environment](https://docs.fabricmc.net/develop/getting-started/setting-up) | Fabric Docs, 2026 | Настройка JDK и IDE |
| [Project Structure](https://docs.fabricmc.net/develop/getting-started/project-structure) | Fabric Docs, 2026 | Где лежат файлы |
| [Launching the Game (IntelliJ)](https://docs.fabricmc.net/develop/getting-started/intellij-idea/launching-the-game) | Fabric Docs, 2026 | Конфигурации запуска, hot swap в JBR, агент Mixin |
| [Generating Sources (IntelliJ)](https://docs.fabricmc.net/develop/getting-started/intellij-idea/generating-sources) | Fabric Docs, 2026 | Использование `genSources` |
| [IDE Tips and Tricks](https://docs.fabricmc.net/develop/getting-started/intellij-idea/tips-and-tricks) | Fabric Docs, 2026 | Навигация по исходникам Minecraft |
| [VS Code setup](https://docs.fabricmc.net/develop/getting-started/vscode/setting-up) | Fabric Docs, 2026 | Работа без IntelliJ |
| [Building a Mod](https://docs.fabricmc.net/develop/getting-started/building-a-mod) | Fabric Docs, 2026 | `./gradlew build`, какой jar выпускать |
| [Loom overview](https://docs.fabricmc.net/develop/loom/) | Fabric Docs, 2026 | Id плагинов, конфигурации, include, раздельные source sets |
| [Loom Options](https://docs.fabricmc.net/develop/loom/options) | Fabric Docs, 2026 | Справочник DSL `loom {}` (запуски, mixin, декомпиляторы) |
| [Fabric API DSL](https://docs.fabricmc.net/develop/loom/fabric-api) | Fabric Docs, 2026 | Опции `configureDataGeneration` и `configureTests` |
| [Loom Tasks](https://docs.fabricmc.net/develop/loom/tasks) | Fabric Docs, 2026 | Дополнительные типы задач (FabricModJsonV1Task, DownloadTask) |
| [Production Run Tasks](https://docs.fabricmc.net/develop/loom/production-run-tasks) | Fabric Docs, 2026 | Продакшн-запуски клиента и сервера, XVFB, Tracy |
| [Classpath groups](https://docs.fabricmc.net/develop/loom/classpath-groups) | Fabric Docs, 2026 | Classpath разработки для нескольких проектов |
| [Data Generation Setup](https://docs.fabricmc.net/develop/data-generation/setup) | Fabric Docs, 2026 | Точка входа `fabric-datagen`, `runDatagen` |
| [Fabric Loom source](https://github.com/FabricMC/fabric-loom) | FabricMC, 2026 | Первоисточник DSL и задач |
| [fabric-loader SystemProperties.java](https://github.com/FabricMC/fabric-loader/blob/master/src/main/java/net/fabricmc/loader/impl/util/SystemProperties.java) | FabricMC, 2026 | Все отладочные свойства `fabric.*` |
| [Fabric API source](https://github.com/FabricMC/fabric) | FabricMC, 2026 | Список модулей, testmods как примеры |
| [Minecraft Development plugin](https://plugins.jetbrains.com/plugin/8327-minecraft-development) | minecraft-dev, 2026 | Мастера IntelliJ, инспекции Mixin, поддержка AT и AW |
| [MinecraftDev changelog](https://github.com/minecraft-dev/MinecraftDev/blob/dev/changelog.md) | minecraft-dev, 2026 | 1.9.0: шаблоны Fabric 26.1+, квантификаторы Mixin |

## 5. Проект NeoForge (и MinecraftForge)

**Ключевые выводы**

- **Старт.** [NeoForge Mod Generator](https://neoforged.net/mod-generator/) (имя, id, пакет, версия Minecraft, выбор Gradle-плагина) или MDK-репозитории в организации `NeoForgeMDKs` на GitHub: `MDK-26.3-ModDevGradle`, `MDK-26.3-NeoGradle`, `MDK-26.1.2-ModDevGradle`, `MDK-1.21.1-ModDevGradle` и другие (боты обновляли их в октябре–ноябре 2026). Для Forge 1.20.1 через MDG Legacy есть `MDK-Forge-1.20.1-ModDevGradle`.
- **Требования.** JDK 25 (64-бит, документация советует Microsoft OpenJDK) для 26.x. MDK 26.3 закреплён на **Gradle 9.2.1**, для 26.1 нужен Gradle ≥ 9.1.0. Официально поддерживаются IntelliJ IDEA и Eclipse (просмотр исходников работает только в них).
- **MDG и NeoGradle.** MDG (`net.neoforged.moddev` 2.0.148) — выбор по умолчанию в генераторе, MDK и шаблонах сообщества: проще, поддерживает configuration cache, с версии 2.0.124 умеет пропускать декомпиляцию и перекомпиляцию (`disableRecompilation = true`), а с 2.0.136 **автоматически пропускает при `CI=true`**, что сильно ускоряет CI. NeoGradle (`net.neoforged.gradle.userdev` 7.1.39) выпускает MDK, но отстаёт от MDG (к снапшотам 26.1 не был готов). Для нового проекта берите MDG.
- **Минимальная сборка MDG** (из MDK-26.3; полный проверенный вариант в разделе «Шаблоны»):

```groovy
plugins {
    id 'java-library'
    id 'net.neoforged.moddev' version '2.0.148'
}
java.toolchain.languageVersion = JavaLanguageVersion.of(25)
neoForge {
    version = project.neo_version            // 26.3.0.101
    runs {
        client { client(); systemProperty 'neoforge.enabledGameTestNamespaces', project.mod_id }
        server { server(); programArgument '--nogui'; systemProperty 'neoforge.enabledGameTestNamespaces', project.mod_id }
        gameTestServer { type = "gameTestServer"; systemProperty 'neoforge.enabledGameTestNamespaces', project.mod_id }
        data { clientData(); programArguments.addAll '--mod', project.mod_id, '--all',
                 '--output', file('src/generated/resources/').absolutePath,
                 '--existing', file('src/main/resources/').absolutePath }
    }
    mods { "${mod_id}" { sourceSet(sourceSets.main) } }
}
```

- **Типы запусков:** `client`, `server`, `data` (до 1.21.4), `clientData` и `serverData` (с 1.21.4, раздельный datagen), `gameTestServer`. Каждый превращается в задачу `run<Имя>` (`runClient`, `runServer`, `runGameTestServer`, `runData`) и в конфигурацию IDE. Задача `runGameTestServer` проверена в CI.
- **Метаданные.** `src/main/templates/META-INF/neoforge.mods.toml` раскрывается задачей `generateModMetadata` (`ProcessResources`, зарегистрирована как `ideSyncTask`, чтобы синхронизация IntelliJ её перегенерировала). Обязательны `license` и `[[mods]] modId/version/displayName`. Зависимости: `type = "required"|"optional"|"incompatible"|"discouraged"`, `versionRange` (диапазоны Maven), `ordering`, `side`. Mixin: `[[mixins]] config="modid.mixins.json"`. AT определяются автоматически по `META-INF/accesstransformer.cfg`, расширения enum — `enumExtensions="META-INF/enumextensions.json"`. Регулярное выражение id мода: `[a-z][a-z0-9_]{1,63}`.
- **`gradle.properties` MDK 26.3:** `minecraft_version=26.3`, `minecraft_version_range=[26.3]`, `neo_version=26.3.0.101`, `org.gradle.configuration-cache=true`, `org.gradle.caching=true`, `org.gradle.parallel=true`. Конфигурация MDK `localRuntime` (расширяет `runtimeClasspath`) хранит тестовые моды только для запуска, чтобы они не попали в опубликованный POM.
- **Библиотеки не из Minecraft.** С NeoForge 1.21.9 загружается всё, что есть в classpath разработки; для продакшна оборачивайте в `jarJar(...)`. На ≤1.21.8 MDG требует `additionalRuntimeClasspath 'group:artifact:ver'`.
- **Access transformers и interface injection.** `neoForge { validateAccessTransformers = true }`; interface injection через `interfaceInjectionData.from "interfaces.json"` (только на этапе компиляции, на стороне времени выполнения нужен Mixin). AT и данные II можно публиковать зависимым проектам.
- **Модульные тесты с MDG.** `neoForge { unitTest { enable(); testedMod = mods.<id> } }` плюс JUnit 5; `net.neoforged:testframework:<версия neo>` даёт `@ExtendWith(EphemeralTestServerProvider.class)` для получения живого `MinecraftServer` в тесте.
- **Режим vanilla и общие проекты.** `neoForge { neoFormVersion = "26.3-1" }` компилирует против чистого vanilla (без NeoForge): так собирается проект `common` в MultiLoader-Template. Последний NeoForm: `26.3-1`, для снапшотов `26.4-snapshot-7-1`.
- **MDG Legacy.** Плагин `net.neoforged.moddev.legacyforge` работает с MinecraftForge и vanilla 1.17–1.20.1 с автоматической реобфускацией в SRG (на загрузку идёт результат `reobfJar`). Подходит для веток Forge 1.20.1 с тем же инструментарием, что у NeoForge.
- **Запуск с авторизацией.** `clientAuth { client(); devLogin = true }` (DevLogin, вход Microsoft по коду устройства) для проверки серверов с online-mode и скинов.
- **MinecraftForge 26.3 (ForgeGradle 7).** `id 'net.minecraftforge.gradle' version '[7.0.17,8)'`, `repositories { minecraft.mavenizer(it); maven fg.forgeMaven; maven fg.minecraftLibsMaven; mavenCentral() }`, `implementation minecraft.dependency('net.minecraftforge:forge:26.3-66.1.5')`, `annotationProcessor 'net.minecraftforge:eventbus-validator:7.0.6'`, запуски `client`, `server`, `gameTestServer`, `data`, wrapper Gradle 9.7.1, свойство `forge.enabledGameTestNamespaces`. ForgeGradle 6 (`fg.deobf`) `[устарело]` для текущего Forge, замена ForgeGradle 7; для проектов 1.20.1 он ещё используется.
- **Серверные паки NeoForge и Forge.** Установщик пишет `run.sh`, `run.bat` и `user_jvm_args.txt` (файла `server.jar` с 1.17 нет). Хостингу, которому нужен jar, подойдёт **ServerStarterJar** от NeoForged (`java @user_jvm_args.txt -jar server.jar`, `--installer <версия neo>`).
- **Что неверно в старых туториалах:** `mods.toml` (с 20.5 `neoforge.mods.toml`), `@Mod.EventBusSubscriber(bus = Bus.MOD)` (теперь `net.neoforged.fml.common.EventBusSubscriber` только с `value` (Dist[]) и `modid`; шина выбирается автоматически), `FMLJavaModLoadingContext.get()` (внедряйте `IEventBus` и `ModContainer` в конструктор мода), строки Parchment на 26.x, `JavaLanguageVersion.of(21)` для 26.x, версии NeoForge `21.1.x` для 26.x, `GuiGraphics` и `Screen#render` (в 26.1 `GuiGraphicsExtractor`, `extractRenderState`), `new ChunkPos(blockPos)` (в 26.1 `ChunkPos.containing`).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [NeoForge Mod Generator](https://neoforged.net/mod-generator/) | NeoForged, живой | Генерирует проекты MDG и NeoGradle |
| [Getting Started with NeoForge](https://docs.neoforged.net/docs/gettingstarted/) | NeoForged docs, 2026 | Java 25, IDE, генератор, запуски |
| [Mod Files](https://docs.neoforged.net/docs/gettingstarted/modfiles) | NeoForged docs, 2026 | Справочник по `gradle.properties` и `neoforge.mods.toml` |
| [Structuring Your Mod](https://docs.neoforged.net/docs/gettingstarted/structuring) | NeoForged docs, 2026 | Пакеты, разделение клиента и общего кода |
| [MDK-26.3-ModDevGradle](https://github.com/NeoForgeMDKs/MDK-26.3-ModDevGradle) | NeoForged, 2026-11 | Эталонные файлы сборки 26.3 и CI |
| [MDK-1.21.1-ModDevGradle](https://github.com/NeoForgeMDKs/MDK-1.21.1-ModDevGradle) | NeoForged, 2026-10 | Образец для долгоживущей линии 1.21.1 |
| [NeoForgeMDKs organisation](https://github.com/NeoForgeMDKs) | NeoForged, живой | Все MDK по версиям Minecraft и плагинам |
| [ModDevGradle README](https://github.com/neoforged/ModDevGradle/blob/main/README.md) | NeoForged, 2026 | Запуски, jarJar, AT, II, модульные тесты, переключатель перекомпиляции для CI |
| [MDG breaking changes](https://github.com/neoforged/ModDevGradle/blob/main/BREAKING_CHANGES.md) | NeoForged, 2025 | Переход MDG 1 → 2 |
| [MDG Legacy Forge plugin](https://github.com/neoforged/ModDevGradle/blob/main/LEGACY.md) | NeoForged, 2025 | Forge и vanilla 1.17–1.20.1, реобфускация |
| [ModDevGradle project listing](https://projects.neoforged.net/neoforged/ModDevGradle) | NeoForged, живой | Последние версии MDG, список изменений |
| [NeoGradle](https://github.com/neoforged/NeoGradle) | NeoForged, 2026 | Альтернативный плагин, ещё поддерживается |
| [Jar-in-Jar (toolchain docs)](https://docs.neoforged.net/toolchain/docs/dependencies/jarinjar) | NeoForged docs, 2026 | Конфигурация `jarJar`, metadata.json |
| [Non-Minecraft Dependencies](https://docs.neoforged.net/toolchain/docs/dependencies/nonmclibs) | NeoForged docs, 2026 | Библиотеки в 1.21.9+ и ≤1.21.8 |
| [Access Transformers](https://docs.neoforged.net/docs/advanced/accesstransformers) | NeoForged docs, 2026 | Синтаксис AT и валидация |
| [Extensible Enums](https://docs.neoforged.net/docs/advanced/extensibleenums) | NeoForged docs, 2026 | JSON `enumExtensions` |
| [Game Tests (NeoForge)](https://docs.neoforged.net/docs/misc/gametest) | NeoForged docs, 2026 | Экземпляры тестов, окружения, `runGameTestServer` |
| [NeoForge porting primers](https://github.com/neoforged/.github/tree/main/primers) | NeoForged, 2026 | Списки изменений ванильной игры по версиям (на них ссылается и Fabric) `[не проверено]` |
| [ServerStarterJar](https://github.com/neoforged/ServerStarterJar) | NeoForged, 2026 | Обёртка `server.jar` для хостингов |
| [NeoForge repository](https://github.com/neoforged/NeoForge) | NeoForged, живой | Исходники, ветки по версиям Minecraft |
| [FancyModLoader](https://github.com/neoforged/FancyModLoader) | NeoForged, живой | Устройство загрузчика, поиск модов, раннее окно |
| [Forge 26.3 MDK zip](https://maven.minecraftforge.net/net/minecraftforge/forge/26.3-66.1.5/forge-26.3-66.1.5-mdk.zip) | команда Forge, 2026-11 | Эталонный скрипт сборки FG7 |
| [ForgeGradle](https://github.com/MinecraftForge/ForgeGradle) | команда Forge, 2026 | Исходники и документация FG7 |
| [Forge documentation](https://docs.minecraftforge.net/) | команда Forge, живой | Документация API, специфичная для Forge |

## 6. IDE, отладка, hot swap, отладка Mixin

**Ключевые выводы**

- **IntelliJ IDEA** — де-факто основная IDE: оба загрузчика создают конфигурации запуска при синхронизации Gradle (MDG применяет плагин `idea` только при синхронизации из IntelliJ). Fabric на 26.1 требует IDEA **2025.3+**. Плагин **Minecraft Development** (1.9.0 для 2026.1–2026.3) добавляет мастера проектов, автодополнение и инспекции целей Mixin (`@Mixin(target)`, отладка выражений MixinExtras, квантификаторы), поддержку языков AT, AW и class tweaker, превью цветов. VS Code и Eclipse годятся (у Fabric есть страницы про VS Code; MultiLoader-Template не советует Eclipse).
- **Gradle JVM и JDK проекта.** Для 26.x выставьте оба на JDK 25. MDG и MDK используют toolchains Gradle с foojay-resolver (`org.gradle.toolchains.foojay-resolver-convention` 1.0.0), поэтому отсутствующий JDK скачивается сам. Пример Fabric закрепляет `options.release = 25` и ждёт JDK 25 для запуска Gradle.
- **Отладка.** Запускайте сгенерированную конфигурацию в режиме Debug. Точки останова работают в декомпилированных исходниках Minecraft (`genSources` у Fabric; MDG создаёт `minecraft-patched-<версия>-merged.jar` с исходниками). На 26.x стектрейсы в логах игроков содержат настоящие имена, краш-репорты читаются сразу.
- **Hot swap.** Обычный HotSpot допускает только изменения тел методов. **JetBrains Runtime** (JBR 25 для 26.x, JBR 21 для 1.21.x) с `-XX:+AllowEnhancedClassRedefinition` позволяет добавлять и удалять методы, поля и классы. IntelliJ: JRE = JBR и VM-опция `-XX:+AllowEnhancedClassRedefinition`. MDG: `runs { configureEach { jvmArgument '-XX:+AllowEnhancedClassRedefinition' } }`. Loom: `loom { runs { client { jvmArguments.add('-XX:+AllowEnhancedClassRedefinition') } } }`. Замена кода запускается сборкой (Ctrl+F9). DCEVM и TravaOpenJDK `[устарело]`: JBR. Реестры замораживаются после запуска, поэтому новые блоки и предметы требуют перезапуска; ресурсы перезагружаются по F3+T, данные по `/reload`. Проверка JBR 25 с Minecraft 26.x не проводилась, README JBR заявляет поддержку в общем виде.
- **Hot swap Mixin.** Добавьте jar библиотеки Mixin как Java-агент: `-javaagent:"<путь к jar sponge-mixin из External Libraries>"` (документация Fabric) и при необходимости `-Dmixin.hotSwap=true`. Надёжно перезагружаются только тела методов миксинов.
- **Флаги отладки Mixin** (системные свойства, любое `true`): `mixin.debug` (всё), `mixin.debug.export` (выгрузка преобразованных классов в `.mixin.out/`; `mixin.debug.export.decompile` ещё и декомпилирует), `mixin.debug.verbose`, `mixin.debug.countInjections` (ошибка при несовпадении числа внедрений), `mixin.debug.strict`, `mixin.dumpTargetOnFailure`, `mixin.checks` и `mixin.checks.interfaces` (проверка реализованных интерфейсов), `mixin.hotSwap`. Когда «инжектор ничего не делает», начните с `mixin.debug.export=true`. Пример конфига Fabric задаёт `"injectors": { "defaultRequire": 1 }`, так что пропущенное внедрение падает с ошибкой.
- **Логирование.** MDK NeoForge включает маркерное логирование (`forge.logging.markers=REGISTRIES`, варианты `SCAN`, `REGISTRIES`, `REGISTRYDUMP`) и `logLevel = DEBUG`; Fabric использует свою конфигурацию log4j через `loom.log4jConfigs`.
- **Профилирование модифицированной игры.** Продакшн-задачи Fabric поддерживают захват Tracy (`tracyCapture`, `output = file("profile.tracy")`); spark (мод) — выборочное профилирование в игре; NeoForge `RenderNurse` — запуски под RenderDoc. Подробно — в [minecraft-performance-testing.md](minecraft-performance-testing.md).
- **Ловушка: в IntelliJ «Attach sources» ничего не делает или нет `idePostSync`.** README MDG предлагает: заново синхронизировать проект через окно Gradle и убедиться, что IDE собирает через Gradle.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Launching the Game in IntelliJ IDEA](https://docs.fabricmc.net/develop/getting-started/intellij-idea/launching-the-game) | Fabric Docs, 2026 | Расширенная переопределение классов в JBR, `-javaagent` для Mixin |
| [Debugging Mods](https://docs.fabricmc.net/develop/debugging) | Fabric Docs, 2026 | Точки останова, пределы hot swap, отладочное логирование |
| [JetBrains Runtime](https://github.com/JetBrains/JetBrainsRuntime) | JetBrains, 2026 | Сборки JBR 25 и 21 с расширенным переопределением классов |
| [JetBrains Runtime releases](https://github.com/JetBrains/JetBrainsRuntime/releases) | JetBrains, 2026-10 | Загрузка JBR 25.0.4.1 |
| [Mixin Java System Properties](https://github.com/SpongePowered/Mixin/wiki/Mixin-Java-System-Properties) | SpongePowered, вики | Все отладочные флаги `mixin.*` |
| [Mixin wiki](https://github.com/SpongePowered/Mixin/wiki) | SpongePowered, вики | Справочник по инжекторам, введение в Mixin |
| [Fabric Mixin fork](https://github.com/FabricMC/Mixin) | FabricMC, 2026 | Mixin, который поставляют Fabric и NeoForge (`sponge-mixin 0.17.x+mixin.0.8.7`) |
| [MixinExtras](https://github.com/LlamaLad7/MixinExtras) | LlamaLad7, 2026 | `@WrapOperation`, `@ModifyExpressionValue`, выражения; встроен в Fabric и NeoForge |
| [Minecraft Development plugin](https://plugins.jetbrains.com/plugin/8327-minecraft-development) | minecraft-dev, 2026 | Инспекции Mixin и мастера |
| [MinecraftDev repository](https://github.com/minecraft-dev/MinecraftDev) | minecraft-dev, 2026 | Задачи, заметки к релизам |
| [DevLogin](https://github.com/covers1624/DevLogin) | covers1624, 2025 | Клиент разработки с авторизацией |
| [spark profiler](https://spark.lucko.me/) | lucko, живой | Профилировщик CPU и памяти для клиента и сервера |
| [RenderNurse](https://github.com/neoforged/RenderNurse) | NeoForged, 2026 | Интеграция RenderDoc для рендер-модов |

## 7. Тесты: модульные и gametest без графики

**Ключевые выводы**

- **Два слоя.** Модульные тесты (JUnit 5) для чистой логики и кодеков. **Gametest** (ванильный фреймворк GameTest, переработан в 1.21.5 в управляемые данными *экземпляры тестов* и *окружения тестов*) для поведения в мире: установка блоков, машины, ИИ мобов, фичи генерации мира, команды, лут. У Fabric есть ещё **клиентские gametest** (настоящий клиент, скриншоты) для интерфейса, HUD и рендера.
- **Модульные тесты Fabric.** `testImplementation "net.fabricmc:fabric-loader-junit:${loader_version}"`, `test { useJUnitPlatform() }`. Для классов, зависящих от реестров, вызовите в `@BeforeAll` `SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();`, иначе будет `ExceptionInInitializerError` «Not bootstrapped». Тесты запускаются при каждом `build`.
- **Серверные gametest Fabric.** `fabricApi { configureTests { createSourceSet = true; modId = "modid-test"; eula = true } }` создаёт `src/gametest/{java,resources}` со своим `fabric.mod.json`, где объявлены `"fabric-gametest"` (сервер, методы `@GameTest`, пакет `net.fabricmc.fabric.api.gametest.v1`) и `"fabric-client-gametest"` (`FabricClientGameTest`, `context.worldBuilder().create()`, скриншоты). **Серверные gametest запускаются автоматически при `./gradlew build`** (проверено в CI). Клиентские: `./gradlew runClientGameTest` `[не проверено]` локально либо `ClientProductionRunTask` с `jvmArgs.add("-Dfabric.client.gametest")` и `useXVFB = true` в CI (Linux и переменная `CI` включают XVFB по умолчанию, нужен пакет `xvfb`). Известная проблема GitHub Actions: добавьте `-Dfabric.client.gametest.disableNetworkSynchronizer=true`, если падает сетевой синхронизатор.
- **Gametest NeoForge.** Экземпляры регистрируются данными (`data/<ns>/test_instance/*.json`, `test_environment/*.json`, структура `.nbt`) либо через `RegisterGameTestsEvent` (`registerEnvironment`, `registerTest`). Запуск: `./gradlew runGameTestServer`. Сервер завершается с **кодом, равным числу проваленных обязательных тестов** (ненулевой код роняет CI). MDK предупреждает: «By default, the server will crash when no gametests are provided», поэтому добавьте хотя бы один тест до подключения CI. Ограничение пространством имён — `neoforge.enabledGameTestNamespaces`. В игре: `/test runall`, `/test runclosest`, `/test runfailed`, `/test pos <var>`. Структуру для теста нужно создать самому, см. раздел «Шаблоны».
- **Gametest Forge.** MDK с FG7 регистрирует запуск `gameTestServer` со свойством `forge.enabledGameTestNamespaces`.
- **Модульные тесты NeoForge на живом сервере.** MDG `unitTest { enable(); testedMod = mods.<id> }` плюс `net.neoforged:testframework` и `EphemeralTestServerProvider`.
- **EULA в CI.** Fabric требует `eula = true` в `configureTests`. Для запусков gameTestServer в NeoForge соглашение с EULA в CI не потребовалось (проверено в CI); для обычных запусков `server` нужен `eula.txt`.
- **Охват по жанрам.** Gametest подходят для технических модов (пропускная способность труб, передача энергии), контентных (взаимодействие блоков, дроп по таблицам лута), мобов (поиск пути за N тиков), генерации мира (поставить сконфигурированную фичу в структуру и проверить блоки), серверных модов (команды, права). Клиентские gametest подходят для HUD, интерфейса и рендера (сравнение скриншотов). Подробнее — [minecraft-performance-testing.md](minecraft-performance-testing.md) и [testing.md](testing.md).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Automated Testing (Fabric)](https://docs.fabricmc.net/develop/automatic-testing) | Fabric Docs, 2026 | JUnit Loader, серверные и клиентские gametest, GitHub Actions |
| [Fabric API DSL – Tests](https://docs.fabricmc.net/develop/loom/fabric-api#tests) | Fabric Docs, 2026 | Все опции `configureTests` |
| [Fabric docs reference build.gradle](https://github.com/FabricMC/fabric-docs/blob/main/reference/latest/build.gradle) | FabricMC, 2026 | Рабочая задача `runProductionClientGameTest` |
| [Fabric docs CI workflow](https://github.com/FabricMC/fabric-docs/blob/main/.github/workflows/build.yaml) | FabricMC, 2026 | Пример задания `client_game_test` |
| [Game Tests (NeoForge)](https://docs.neoforged.net/docs/misc/gametest) | NeoForged docs, 2026 | Данные тестов, окружения, код завершения `runGameTestServer` |
| [Fabric API gametest module](https://github.com/FabricMC/fabric/tree/HEAD/fabric-gametest-api-v1) | FabricMC, 2026 | Исходники API серверных gametest |
| [Fabric client gametest module](https://github.com/FabricMC/fabric/tree/HEAD/fabric-client-gametest-api-v1) | FabricMC, 2026 | Исходники API клиентских gametest |
| [fabric-loader-junit on Maven](https://maven.fabricmc.net/net/fabricmc/fabric-loader-junit/) | FabricMC, 2026 | Список версий (0.20.1) |
| [Minecraft Wiki – GameTest](https://minecraft.wiki/w/GameTest) | Minecraft Wiki, 2026 | Ванильный фреймворк, справочник по команде `/test` |

## 8. Мультилоадерные и мультиверсионные сборки

**Ключевые выводы**

- **Общая форма.** Модуль `common` (только ванильный код, компилируется против имён Mojang) плюс тонкие подпроекты `fabric` и `neoforge` (при необходимости `forge`) с точками входа, событиями загрузчика и метаданными; они компилируют исходники `common` в свой jar. С 26.1 это проще: все загрузчики видят **одни имена во время выполнения**, ремаппинг общему коду не нужен. Платформенные вызовы идут через небольшой сервисный интерфейс (`ServiceLoader`, `@ExpectPlatform` или статический класс `Services`).
- **MultiLoader-Template (jaredlll08).** Без сторонних плагинов: `common` использует MDG в vanilla-режиме (`neoForm`), `fabric` — Loom, `neoforge` — MDG; общая логика в плагинах-соглашениях `build-logic` (`multiloader-common`, `multiloader-loader`). Ветка на каждую версию Minecraft (1.17 … 26.3, ветка по умолчанию `26.3`). `gradle.properties` на 26.3: `java_version=25`, `minecraft_version_range=[26.3, 26.4)`, `neo_form_version=26.3-1`, `fabric_loader_version=0.19.5`, `neoforge_version=26.3.0.1-beta`. `common` компилируется с `compileOnly 'net.fabricmc:sponge-mixin:0.17.3+mixin.0.8.7'` и MixinExtras (оба загрузчика их поставляют). Использует атрибут Gradle **`io.github.mcgradleconventions.loader`**, чтобы варианты `common`, `fabric`, `neoforge` разрешались правильно. Плюсы: никакой магии, официальные тулчейны. Минусы: больше Gradle в вашей ответственности, нет общего API для реестров и событий.
- **Architectury.** `architectury-plugin` + Architectury Loom (`dev.architectury.loom-no-remap` для 26.x) + необязательный Architectury API (`dev.architectury:architectury:22.0.5`, `-fabric`, `-neoforge`). Статические методы `@ExpectPlatform` реализуются по платформам по соглашению об именах, трансформер переписывает общие jar под каждый загрузчик. Генератор шаблонов: generate.architectury.dev. Плюсы: кросс-загрузчиковый API реестров, событий и сети, один DSL в духе Loom. Минусы: игрокам нужна лишняя зависимость (если вы используете API), отстаёт от новых дропов (26.1 и 26.1.1 не поддерживаются), Architectury Loom — форк Fabric Loom.
- **Cloche** (terrarium-earth), **Unimined**, **ModStitch** (isXander, modstitch-toolkit) — кросс-загрузчиковые сборки одним плагином; все три активны в октябре–ноябре 2026. Cloche и modstitch-multiloader реализуют атрибуты mcgradleconventions. Сообщества меньше: проверьте в их документации статус 26.x до внедрения.
- **Мультиверсия: Stonecutter** (`dev.kikugie.stonecutter` 0.9.8, 2026-08-31) — основной инструмент. Одно дерево исходников, версионные подпроекты (`versions/<mc>-<loader>`), директивы-комментарии, переключаемые активной версией: `//? if >=1.21.11 {` … `//?} else {` … `//?}`, `//? if fabric {`, строковые `replacements` (например `ResourceLocation` → `Identifier` для ≥1.21.11, `classTweaker v2 named` → `classTweaker v2 official` для ≥26.1), `swaps`, `constants` и общий `stonecutter.properties.toml` на версию и загрузчик. Активную версию переключает строка `stonecutter active "26.3-fabric"` или задача Gradle; в VCS коммитьте только состояние версии. Официальные шаблоны `stonecutter-template-fabric`, `-neoforge`, `-multiloader` (целятся в 1.21.1, 1.21.11, 26.2, 26.3). Имена задач для CI (`chiseledBuild` и др.) для 0.9.x заново не проверялись `[не проверено]`.
- **Смесь обфусцированных и необфусцированных версий Fabric** в одной сборке: **loom-back-compat** KikuGie (`dev.kikugie.loom-back-compat` 0.4.x, Codeberg) выбирает `net.fabricmc.fabric-loom-remap` или `net.fabricmc.fabric-loom` на подпроект (`loomx.unobfuscated` или автоматически `sc.current.parsed > "26"` со Stonecutter) и сглаживает разницу `modImplementation` и `implementation`.
- **Другие препроцессоры.** **Препроцессор ReplayMod** (`//#if MC>=12100`, Java и Kotlin, активен в 2026-11) старше, его используют крупные клиентские моды. **Препроцессор Manifold** (плагин javac, `#if`/`#elif`/`#define`, символы из `build.properties` или `-Akey=value`, v2026.1.16) работает в обычном javac с поддержкой IntelliJ через плагин Manifold, но не знает про Minecraft: символы вроде `MC_26_3` задаёте сами. Применение в современных модах 26.x не изучалось.
- **Слияние jar загрузчиков (Forgix, `io.github.pacifistmc.forgix` 2.0.0).** Переносит классы каждого загрузчика в отдельные пакеты и объединяет jar Fabric, NeoForge, Forge, Quilt (и плагинов) в один файл. Удобно пользователям, но: больше jar, дублирование общего кода, путаница с тегами загрузчиков и зависимостями на Modrinth и CurseForge, тяжелее разбирать падения. Крупные моды чаще выпускают **отдельные jar на каждый загрузчик**.
- **Итоговое сравнение.** (1) один загрузчик и одна версия — проще всего; (2) MultiLoader-Template — больше контроля, нет зависимостей времени выполнения; (3) Architectury — быстрее писать кросс-загрузчиковый контент, добавляет зависимость от API; (4) Stonecutter поверх (1)–(3) для нескольких версий Minecraft; (5) отдельные ветки git на версию Minecraft с cherry-pick — распространено у больших модов, сильно меняющихся между дропами. При квартальных дропах в 2026 году закладывайте время на портирование (блоги Fabric и NeoForge, примеры NeoForge primers).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template) | Jared, 2026 | Шаблон common/fabric/neoforge без плагинов, ветка на версию Minecraft |
| [mcgradleconventions](https://github.com/mcgradleconventions) | сообщество, 2026 | Общий атрибут Gradle `loader` для мультилоадерных вариантов |
| [Architectury template generator](https://generate.architectury.dev/) | Architectury, живой | Генерирует проекты common/fabric/neoforge |
| [Architectury docs source](https://github.com/architectury/architectury-docs) | Architectury, 2026 | Настройка, портирование 26.1 → 26.2 → 26.3 |
| [Architectury Loom](https://github.com/architectury/architectury-loom) | Architectury, 2026 | Форк Loom с поддержкой NeoForge и Forge |
| [architectury-plugin](https://github.com/architectury/architectury-plugin) | Architectury, 2026 | Плагин-трансформер общего модуля |
| [Stonecutter docs – getting started](https://stonecutter.kikugie.dev/wiki/start/) | KikuGie, 2026 | Настройка, выбор версии, директивы |
| [Stonecutter repository](https://github.com/stonecutter-versioning/stonecutter) | KikuGie, 2026 | Исходники, релизы |
| [stonecutter-template-multiloader](https://github.com/stonecutter-versioning/stonecutter-template-multiloader) | KikuGie, 2026-09 | Шаблон 1.21.1 → 26.3 для Fabric и NeoForge |
| [stonecutter-template-fabric](https://github.com/stonecutter-versioning/stonecutter-template-fabric) | KikuGie, 2026-09 | Мультиверсионный шаблон только для Fabric |
| [stonecutter-template-neoforge](https://github.com/stonecutter-versioning/stonecutter-template-neoforge) | KikuGie, 2026-09 | Мультиверсионный шаблон только для NeoForge |
| [loom-back-compat](https://codeberg.org/KikuGie/loom-back-compat) | KikuGie, 2026 | Один скрипт сборки для ремапящего и невремапящего Loom |
| [ReplayMod preprocessor](https://github.com/ReplayMod/preprocessor) | ReplayMod, 2026 | Препроцессор `//#if MC>=…` для Java и Kotlin |
| [Manifold preprocessor](https://github.com/manifold-systems/manifold/tree/master/manifold-deps-parent/manifold-preprocessor) | Manifold Systems, 2026 | Препроцессор как плагин javac, символы, поддержка IntelliJ |
| [Forgix](https://github.com/PacifistMC/Forgix) | PacifistMC, 2026 | Слияние jar загрузчиков в один |
| [Cloche](https://github.com/terrarium-earth/cloche) | Terrarium Earth, 2026 | Кроссплатформенный плагин Gradle |
| [Unimined](https://github.com/unimined/Unimined) | unimined, 2026 | Расширение Gradle для нескольких загрузчиков и маппингов |
| [ModStitch toolkit](https://github.com/isXander/modstitch-toolkit) | isXander, 2026 | Небольшие плагины Gradle, включая modstitch-multiloader |
| [NeoForge porting primers](https://github.com/neoforged/.github/tree/main/primers) | NeoForged, 2026 | Изменения ванильной игры по дропам (папки 26.1, 26.2, 26.3) |

## 9. Зависимости: репозитории модов, jar-in-jar, shading

**Ключевые выводы**

- **Предпочитайте Maven автора мода**, если он есть: в нём POM-зависимости, API и jar с исходниками. Примеры: Fabric Maven, NeoForged Maven, BlameJared (`maven.blamejared.com`, JEI), TerraformersMC, Shedaniel, Architectury, Kikugie, Su5eD. Объявляйте их через `exclusiveContent { forRepository { maven { url = … } }; filter { includeGroupAndSubgroups(…) } }`, чтобы Gradle не опрашивал каждый репозиторий по каждому артефакту (так сделано в MultiLoader-Template для `net.fabricmc`).
- **Modrinth Maven:** `https://api.modrinth.com/maven`, группа всегда `maven.modrinth`, артефакт — slug или id проекта, версия — номер или id версии, например `implementation "maven.modrinth:sodium:mc26.3-0.9.3-beta.3-fabric"`. Транзитивных зависимостей нет (Modrinth их не знает), группы отличаются от Maven авторов, поэтому возможны дубликаты. Используйте `exclusiveContent` с фильтром `maven.modrinth`.
- **CurseMaven:** `https://cursemaven.com`, группа `curse.maven`, координаты `curse.maven:<любое-описание>-<projectId>:<fileId>`, классификаторы вида `-sources-<fileId>`. Сторонний сервис (не связан с CurseForge), бета.
- **jar-in-jar в Fabric:** `include "group:artifact:version"` (нетранзитивно) кладёт jar в `META-INF/jars/` и регистрирует в `fabric.mod.json`. Loader убирает дубликаты по id мода и наибольшей версии. Для библиотек, не являющихся модами, Loom сам создаёт `fabric.mod.json`. На 26.x добавляется в `jar`, на ≤1.21.11 в `remapJar`. Типичная запись: `implementation(include("com.example:lib:1.0"))`.
- **jar-in-jar в NeoForge:** `jarJar(implementation("org.commonmark:commonmark")) { version { strictly '[0.1, 1.0)'; prefer '0.21.0' } }` пишет `META-INF/jarjar/metadata.json`. FML выбирает одну версию на все моды в объявленных диапазонах, поэтому всегда указывайте диапазон. Локальные jar: `jarJar files(pluginJar)` (артефакт — имя файла, версия — MD5); подпроекты: `jarJar project(":coremod")` (имена модулей уникальны).
- **Ловушки shading.** (1) Два мода с одной нерелоцированной библиотекой дают split packages и `LinkageError`: релоцируйте (`com.gradleup.shadow` 9.6.1, `relocate`) или используйте jar-in-jar. (2) Не вшивайте библиотеки, которые уже есть в Minecraft (Gson, Guava, Netty, fastutil, JOML, SLF4J, Log4j, Brigadier, DataFixerUpper). (3) На Fabric ≤1.21.11 вывод Shadow не ремапится, а `include`-jar не попадают в `shadowJar`: свяжите `remapJar { inputFile = shadowJar.archiveFile }`. (4) В NeoForge вшитые классы входят в модуль JPMS мода: два мода с одним пакетом падают при разрешении модулей, JarJar этого избегает. (5) Не включайте через jar-in-jar другие *моды*, которые пользователь ставит отдельно (Fabric API, Architectury API, Cloth Config): объявляйте их зависимостями. Пункты 2 и 4 — общая практика `[не проверено]`.
- **Необязательная совместимость.** `compileOnly` на API-jar (например JEI `jei-<mc>-common-api`), `localRuntime` или `runtimeOnly` на полный мод для тестов (MDK использует `localRuntime`, поэтому в POM он не попадает), защита `ModList.get().isLoaded("jei")` и `FabricLoader.getInstance().isModLoaded("emi")`, объявление `optional` или `suggests`.
- **Языковые адаптеры.** Kotlin требует `fabric-language-kotlin` (1.14.2+kotlin.2.4.21) на Fabric и **Kotlin for Forge** (6.4.0 для 26.2 и 26.3, NeoForge): добавляйте их как обязательные зависимости, не вшивайте стандартную библиотеку Kotlin.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Modrinth Maven](https://support.modrinth.com/en/articles/8801191-modrinth-maven) | Modrinth, 2024–2026 | Координаты, фрагмент exclusiveContent, ограничения |
| [CurseMaven](https://www.cursemaven.com/) | Wyn Price, живой | Координаты `curse.maven` и классификаторы |
| [Loom – include (jar-in-jar)](https://docs.fabricmc.net/develop/loom/#include) | Fabric Docs, 2026 | Нетранзитивный include, автоматический fabric.mod.json |
| [MDG – Jar-in-Jar](https://github.com/neoforged/ModDevGradle#jar-in-jar) | NeoForged, 2026 | Диапазоны версий, локальные файлы, подпроекты |
| [NeoForged JarJar library](https://github.com/neoforged/JarJar) | NeoForged, 2026 | Логика выбора версии во время выполнения |
| [Gradle repository content filtering](https://docs.gradle.org/current/userguide/filtering_repository_content.html) | Gradle, 2026 | `exclusiveContent`, `includeGroupAndSubgroups` |
| [Shadow plugin (GradleUp)](https://gradleup.com/shadow/) | GradleUp, 2026 | Релокация, minimize |
| [Kotlin for Forge](https://modrinth.com/mod/kotlin-for-forge) | thedarkcolour, 2026 | Языковой провайдер Kotlin для NeoForge |
| [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin) | FabricMC, 2026 | Адаптер Kotlin и стандартная библиотека для Fabric |
| [JEI on BlameJared Maven](https://maven.blamejared.com/mezz/jei/) | mezz, 2026 | Пример настройки компиляции против API |

## 10. Публикация, версии, changelog и лицензии

**Ключевые выводы**

- **mod-publish-plugin** (`me.modmuss50.mod-publish-plugin` **2.2.1**, 2026-09-21) — рекомендуемый единый плагин для CurseForge, Modrinth, GitHub, GitLab, Gitea/Forgejo/Codeberg и объявлений в Discord. Версия 2.0.0 требует **Gradle ≥ 9.0** и делает обязательными флаги `client` и `server` для CurseForge; 2.1.0 добавила поле окружения Modrinth; 2.2.0 — типы файлов Modrinth. Основные опции: `file = jar.archiveFile` (Fabric 26.1+, NeoForge, Forge) или `remapJar.archiveFile` (Fabric ≤1.21.11), `changelog`, `type = STABLE|BETA|ALPHA`, `modLoaders.add("neoforge")`, `dryRun = providers.environmentVariable("MODRINTH_TOKEN").getOrNull() == null`, `maxRetries`, версии Minecraft на каждую платформу (или `minecraftVersionRange { start = "26.1.2"; end = "latestRelease" }`), Modrinth `environment = CLIENT_ONLY`, синхронизация `projectDescription`, `requires("fabric-api")`, `optional`, `incompatible`, `embeds`. Общие блоки опций (`curseforgeOptions {}`, `modrinthOptions {}`) для корня мультилоадерного проекта, `file project(":fabric")`. Запуск: `./gradlew publishMods`.
- **Minotaur** (`com.modrinth.minotaur` 2.10.0, официальный плагин Modrinth): только Modrinth; `modrinth { token, projectId, versionNumber, versionType, uploadFile, gameVersions, loaders, dependencies { required.project "fabric-api" } }`, плюс `modrinthSyncBody` для переноса README на страницу проекта. Области токена: `CREATE_VERSION` (и `PROJECT_WRITE` для синхронизации описания). README плагина всё ещё пишет `uploadFile = remapJar` для Loom, что неверно для невремапящего Loom 26.x (нужен `jar`).
- **CurseForge.** **CurseForgeGradle** (`net.darkhax.curseforgegradle` 1.3.33, Darkhax) — поддерживаемый плагин только для CurseForge. **CurseGradle** (`com.matthewprenger.cursegradle` 1.4.0, 2019, репозиторий заархивирован в 2021) `[устарело]`: mod-publish-plugin или CurseForgeGradle. Токен API берётся в настройках аккаунта CurseForge (по документации плагина `legacy.curseforge.com/account/api-tokens`).
- **Альтернатива на уровне workflow.** `Kir-Antipov/mc-publish@v3.3` (v3.3.2, 2026-10-22) публикует в Modrinth, CurseForge и GitHub без настройки Gradle; Modrinth отмечает, что mc-publish не поддерживает.
- **Версионирование.** SemVer для мода; Minecraft и загрузчик указывайте в имени файла или версии: `modid-1.4.2+26.3-neoforge.jar` или `modid-neoforge-26.3-1.4.2.jar`. Fabric сравнивает версии по SemVer (метаданные сборки после `+` не влияют на порядок); NeoForge использует диапазоны Maven, поэтому схема должна сортироваться по правилам Maven. Modrinth требует уникальный номер версии в проекте: добавляйте суффикс Minecraft и загрузчика, когда выпускаете одну версию мода для нескольких целей (Minotaur падает на дубликатах). Для сравнения: Fabric API `0.165.2+26.3`, NeoForge `26.3.0.101`, Forge `26.3-66.1.5`.
- **Changelog.** Ведите `CHANGELOG.md` (формат Keep a Changelog) и передавайте текущий раздел в `publishMods { changelog = … }`; плагин принимает Markdown. Тип выпуска (alpha, beta, release) определяет канал обновлений в лаунчерах Modrinth и CurseForge.
- **Лицензии.** Укажите SPDX-идентификатор: `license = "MIT"` в `neoforge.mods.toml` обязателен, `"license"` в `fabric.mod.json`. Частый выбор: MIT и Apache-2.0 (разрешительные, библиотеки и API), LGPL-3.0 (библиотеки, изменения которых нужно делиться), MPL-2.0 (копилефт на уровне файлов), GPL-3.0 и AGPL (копилефт; он касается только вашего кода, не Minecraft), ARR (All Rights Reserved — значение MDK по умолчанию, юридически блокирует форки и распространение в сборках без разрешения). Fabric API — Apache-2.0, NeoForge — LGPL-2.1. У ресурсов может быть отдельная лицензия (CC-BY-4.0 и др.); разрешение на сборки указывайте явно. Рекомендации по выбору — `[не проверено]`.
- **EULA и Usage Guidelines Minecraft (действуют и после деобфускации).** Мод должен быть «an original plug-in … that doesn't contain a substantial part of our copyrightable code or content»; распространять можно «the mod only and not a modded version of Minecraft»; нельзя play-to-earn и NFT, и нельзя привязывать контент к внеигровой собственности. Не размещайте в репозитории и jar Minecraft, декомпилированные исходники и ванильные ресурсы: публикуйте только патчи и миксины. Брендинг: не изображайте официальный статус; правила просят указывать «NOT AN OFFICIAL MINECRAFT [PRODUCT]. NOT APPROVED BY OR ASSOCIATED WITH MOJANG OR MICROSOFT». С 26.1 в каждом jar лежит файл LICENSE со ссылкой на EULA. Монетизация: пожертвования допустимы; платный доступ к функциям мода явно не разрешён, а серверные правила ограничивают продажи, влияющие на геймплей. Явного положения о платных модах (Patreon с ранним доступом) в правилах нет, толкование остаётся за читателем.
- **Ловушки.** Загружайте правильный jar (не `-sources`, не `-dev`, не путайте `reobf` и обычный на MDG Legacy). Для сборок NeoForge ставьте тег загрузчика `neoforge`, а не `forge`. CurseForge и Modrinth называют снапшоты по-разному, поэтому mod-publish-plugin просит версии на каждую платформу. Задавайте флаги окружения `client` и `server` (Modrinth использует их для `env` сборок и серверных паков).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Mod Publish Plugin docs](https://modmuss50.github.io/mod-publish-plugin/) | modmuss50, 2026 | Полный DSL, платформы, пример GitHub Actions |
| [mod-publish-plugin repository](https://github.com/modmuss50/mod-publish-plugin) | modmuss50, 2026 | История изменений (требование Gradle 9 в 2.0) |
| [Minotaur](https://github.com/modrinth/minotaur) | Modrinth, 2026 | Официальный плагин Gradle для Modrinth |
| [CurseForgeGradle](https://github.com/Darkhax/CurseForgeGradle) | Darkhax, 2026 | Поддерживаемый загрузчик на CurseForge |
| [CurseGradle (archived)](https://github.com/matthewprenger/CurseGradle) | matthewprenger, 2019 | `[устарело]`: заменён на mod-publish-plugin и CurseForgeGradle |
| [mc-publish GitHub Action](https://github.com/Kir-Antipov/mc-publish) | Kir-Antipov, 2026 | Публикация на уровне workflow |
| [Modrinth API – create version](https://docs.modrinth.com/api/operations/createversion/) | Modrinth, живой | Поля: loaders, game_versions, dependencies, version_type |
| [Modrinth personal access tokens](https://modrinth.com/settings/pats) | Modrinth, живой | Создание токена (области доступа) |
| [CurseForge Upload API](https://support.curseforge.com/en/support/solutions/articles/9000197321-curseforge-upload-api) | CurseForge, живой | Точка загрузки и идентификаторы версий игры |
| [Removing obfuscation – EULA section](https://www.minecraft.net/en-us/article/removing-obfuscation-in-java-edition) | Mojang, 2025 | EULA и Usage Guidelines остаются в силе, LICENSE в jar |
| [Minecraft Usage Guidelines](https://www.minecraft.net/en-us/usage-guidelines) | Mojang, живой | Определение мода и правила, оговорка о брендинге |
| [Minecraft EULA](https://www.minecraft.net/en-us/eula) | Mojang, живой | Условия лицензии |
| [Choose a License](https://choosealicense.com/) | GitHub, живой | Ссылка из обоих MDK |
| [SPDX License List](https://spdx.org/licenses/) | SPDX, живой | Допустимые идентификаторы лицензий |
| [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) | O. Lacan, 2023 | Формат changelog |
| [Semantic Versioning 2.0.0](https://semver.org/) | T. Preston-Werner | Семантика версий, на которую опирается Fabric |
| [Maven version range spec (MDG table)](https://github.com/neoforged/ModDevGradle#external-dependencies) | NeoForged, 2026 | Синтаксис диапазонов NeoForge |

## 11. CI на GitHub Actions

**Ключевые выводы**

- **Базовое задание для обоих загрузчиков:** `actions/checkout`, `gradle/actions/wrapper-validation`, `actions/setup-java` с **Java 25** (`temurin` или `microsoft`), `gradle/actions/setup-gradle`, `./gradlew build`, `actions/upload-artifact` для `build/libs/`. Рабочий пример для одного мода — в разделе «Шаблоны». Пример Fabric использует `checkout@v6`, `wrapper-validation@v6`, `setup-java@v5` (microsoft), `upload-artifact@v7` на `ubuntu-24.04`, MDK NeoForge — `checkout@v4` (с `fetch-depth: 0`, `fetch-tags: true` для версий по тегам), `setup-java@v4`, `setup-gradle@v4`. Актуальные мажоры на 2026-11-07: checkout v7.0.1, setup-java v6.2.0, gradle/actions v6.4.1, upload-artifact v7.0.2, cache v6.1.0. Проверено в CI: работают `checkout@v7` и `setup-java@v6`.
- **Кэш.** `setup-gradle` кэширует Gradle User Home, куда входят **кэш Loom (`~/.gradle/caches/fabric-loom`)** и **кэш NeoFormRuntime от MDG (`~/.gradle/caches/neoformruntime`)**; дополнительный `actions/cache` не нужен. Кэш записывается только из ветки по умолчанию, ветки PR его читают. В v6 «расширенный» провайдер по умолчанию проприетарный (бесплатен для публичных репозиториев, для частных превью); `cache-provider: basic` — альтернатива с лицензией MIT поверх `actions/cache` (без очистки, только точное совпадение ключа). Включите `org.gradle.caching=true` и `org.gradle.configuration-cache=true` (оба MDK так и делают).
- **Скорость CI NeoForge.** MDG ≥ 2.0.136 пропускает декомпиляцию и перекомпиляцию Minecraft при `CI=true` (GitHub Actions задаёт её сам): компиляция идёт против бинарников. В нашем CI сборка NeoForge заняла около минуты. Для многих версий и заданий `neoforged/actions-modding/minecraft-assets-cache/create@v1` (ночью на main) и `.../use@v1` делят один кэш ресурсов Minecraft между ветками (нужен для запусков клиента и gametest, загружающих ресурсы).
- **Gametest без графики.**
  - Fabric, серверные: запускаются в составе `./gradlew build` при `fabricApi { configureTests { eula = true } }`. Клиентские: `./gradlew runProductionClientGameTest` с XVFB (`sudo apt-get install -y xvfb`), скриншоты `build/run/clientGameTest/screenshots` загружайте с `if: always()`; при нестабильности на Actions добавьте `-Dfabric.client.gametest.disableNetworkSynchronizer=true`.
  - NeoForge: `./gradlew runGameTestServer`; ненулевой код возврата равен числу проваленных обязательных тестов, задание падает. Нужен хотя бы один тест (иначе MDK предупреждает о падении сервера), ограничение пространством имён — `neoforge.enabledGameTestNamespaces`.
  - Forge: `./gradlew runGameTestServer` со свойством `forge.enabledGameTestNamespaces` (MDK с FG7); имя задачи не проверялось `[не проверено]`.
- **Матрицы сборок.** Для проектов Stonecutter собирайте каждый узел или используйте матрицу GitHub по версиям Minecraft с разными JDK (21 для ≤1.21.11, 25 для 26.x): `setup-java` принимает несколько версий (`java-version: |` с двумя строками 21 и 25), нужный JDK выберет toolchain Gradle `[не проверено]`.
- **Workflow публикации.** Триггер по тегам (`on: push: tags: ['v*']`) или `workflow_dispatch`, `permissions: contents: write`, команда `./gradlew build publishMods` с секретами `MODRINTH_TOKEN`, `CURSEFORGE_TOKEN`, `GITHUB_TOKEN` (в примере документации mod-publish-plugin переменная названа `CURSEFORGE_API_KEY`, имя выбираете вы). `dryRun` включайте для PR. Пример — в разделе «Шаблоны».
- **Ловушка YAML.** Простой скаляр YAML, содержащий `": "` (например `run: unzip -p … | grep '"version": "1.0.0"'`), ломает весь файл workflow («workflow file issue»). Запись `run: |` (блочный скаляр) решает это. Нашли в CI.
- **Гигиена цепочки поставок.** Проверяйте wrapper (`wrapper-validation`), закрепляйте версии плагинов (без `1.19-SNAPSHOT` и `2.+` в релизном конвейере), храните токены в секретах GitHub и передавайте через `providers.environmentVariable(...)` (не `System.getenv` на этапе конфигурации при configuration cache), используйте `exclusiveContent` для сторонних Maven. Часть рекомендаций — `[не проверено]`.
- **Граф зависимостей.** `gradle/actions/dependency-submission@v6` заполняет граф зависимостей GitHub для оповещений Dependabot по библиотекам мода.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [setup-gradle documentation](https://github.com/gradle/actions/blob/main/docs/setup-gradle.md) | Gradle, 2026 | Провайдеры кэша, ветки только для чтения, очистка |
| [gradle/actions](https://github.com/gradle/actions) | Gradle, 2026-10 | setup-gradle, wrapper-validation, dependency-submission v6 |
| [actions/setup-java](https://github.com/actions/setup-java) | GitHub, 2026-11 | Дистрибутивы JDK, несколько версий |
| [Fabric example mod workflow](https://github.com/FabricMC/fabric-example-mod/blob/26.3/.github/workflows/build.yml) | FabricMC, 2026 | Эталонный CI для Fabric |
| [NeoForge MDK workflow](https://github.com/NeoForgeMDKs/MDK-26.3-ModDevGradle/blob/main/.github/workflows/build.yml) | NeoForged, 2026 | Эталонный CI для NeoForge |
| [neoforged/actions-modding](https://github.com/neoforged/actions-modding) | NeoForged, 2025 | Общие действия кэша ресурсов Minecraft |
| [MDG – Disabling Decompilation and Recompilation](https://github.com/neoforged/ModDevGradle#disabling-decompilation-and-recompilation) | NeoForged, 2026 | Конвейер только с бинарниками при `CI=true` |
| [NeoFormRuntime](https://github.com/neoforged/NeoFormRuntime) | NeoForged, 2026 | Каталог кэша, загрузка ресурсов |
| [Run Game Tests on GitHub Actions (Fabric)](https://docs.fabricmc.net/develop/automatic-testing#run-game-tests-on-github-actions) | Fabric Docs, 2026 | Задание клиентских gametest и XVFB |
| [Mod Publish Plugin – GitHub Actions](https://modmuss50.github.io/mod-publish-plugin/github_actions/) | modmuss50, 2026 | Релизный workflow и секреты |
| [Gradle configuration cache](https://docs.gradle.org/current/userguide/configuration_cache.html) | Gradle, 2026 | Почему `providers.environmentVariable` |
| [GitHub Actions cache restrictions](https://docs.github.com/en/actions/using-workflows/caching-dependencies-to-speed-up-workflows#restrictions-for-accessing-a-cache) | GitHub, живой | Ограничение кэша по веткам |

## 12. Сборки: packwiz, .mrpack, лаунчеры, серверные паки

**Ключевые выводы**

- **Форматы.** Modrinth **`.mrpack`** — zip с `modrinth.index.json` (`formatVersion: 1`, `game: "minecraft"`, `versionId`, `name`, `files[]` с `path`, `hashes.sha1` и `sha512`, `env.client/server` = `required|optional|unsupported`, `downloads` (HTTPS; для сборок, размещённых на Modrinth, допустимы только `cdn.modrinth.com`, `github.com`, `raw.githubusercontent.com`, `gitlab.com`), `fileSize`; `dependencies` с ключами `minecraft`, `forge`, `neoforge`, `fabric-loader`, `quilt-loader`) и каталогами `overrides/`, `client-overrides/`, `server-overrides/`. Сборки CurseForge — zip с `manifest.json` (версия Minecraft, id загрузчика вроде `neoforge-26.3.0.101`, `files` как projectID/fileID) и `overrides/` `[не проверено]`.
- **packwiz** (CLI, дружелюбный к git TOML: `pack.toml`, `index.toml`, по одному `.pw.toml` на мод; активен, последний коммит 2026-10-14, тегированных релизов нет, ставится через Go или артефакты CI): `packwiz init`, `packwiz modrinth install <slug>` и `packwiz curseforge install <slug>`, `packwiz update --all`, `packwiz refresh`, `packwiz modrinth export` (→ .mrpack), `packwiz curseforge export`, `packwiz serve` (localhost:8080/pack.toml). В файле каждого мода можно указать `side = "client"|"server"|"both"`.
- **Самообновляющиеся экземпляры.** packwiz-installer-bootstrap как команда перед запуском в Prism и MultiMC: `"$INST_JAVA" -jar packwiz-installer-bootstrap.jar https://example.org/pack.toml`. Серверы: `java -jar packwiz-installer-bootstrap.jar -g -s server https://example.org/pack.toml` (`-g` — без GUI, `-s server` — только серверные моды).
- **Лаунчеры.** Prism Launcher (11.1.1, 2026-09-28) импортирует `.mrpack` и zip CurseForge, управляет Fabric, NeoForge, Forge и Quilt, Java задаётся на экземпляр (для 26.x нужна Java 25, Prism умеет скачивать). Modrinth App (с открытым кодом, `modrinth/code`) и приложение CurseForge — лаунчеры платформ.
- **Серверные паки.** (1) Пометьте клиентские моды (`env.server = "unsupported"` в mrpack, `side = "client"` в packwiz): самая частая причина падения сервера — клиентский мод (шейдеры, HUD, миникарты) на выделенном сервере. (2) Fabric: `fabric-server-launch.jar` через установщик Fabric или jar «server launcher» с fabricmc.net. (3) NeoForge и Forge: установщик `--install-server` даёт `run.sh` и `user_jvm_args.txt`, либо `server.jar` от ServerStarterJar. (4) Контейнеры: `itzg/minecraft-server` поддерживает сборки Modrinth и CurseForge через переменные окружения (активен, 2026.10.1); `mrpack-install` (nothub) ставит серверные паки .mrpack (последний релиз v0.16.10, 2024 — проверьте сопровождение).
- **Что должен сделать сам мод для сборок.** Объявить верное окружение (`"environment": "client"` в `fabric.mod.json` или поведение `side` и `displayTest` в NeoForge), сделать конфиги синхронизируемыми с сервером, не падать при отсутствии необязательных интеграций, хранить стабильные id мода и порядок версий, выбрать лицензию, допускающую распространение в сборках (ARR блокирует *включение* в packwiz и mrpack, но не *ссылки на загрузку*) `[не проверено]`.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Modrinth Modpack Format (.mrpack)](https://support.modrinth.com/en/articles/8802351-modrinth-modpack-format-mrpack) | Modrinth, живой | Поля индекса, overrides, разрешённые домены |
| [packwiz](https://github.com/packwiz/packwiz) | packwiz, 2026 | Исходники CLI и README |
| [packwiz – Getting Started](https://packwiz.infra.link/tutorials/getting-started/) | packwiz, живой | Шпаргалка по командам |
| [packwiz – Installing with packwiz-installer](https://packwiz.infra.link/tutorials/installing/packwiz-installer/) | packwiz, живой | Запуск перед стартом в Prism и на сервере |
| [packwiz-installer](https://github.com/packwiz/packwiz-installer) | packwiz, 2024 | Установщик клиента и сервера (v0.5.14) |
| [Prism Launcher](https://prismlauncher.org/) | Prism, живой | Лаунчер с открытым кодом, импорт mrpack и CF |
| [Prism Launcher releases](https://github.com/PrismLauncher/PrismLauncher/releases) | Prism, 2026-09 | Версия 11.1.1 |
| [Modrinth monorepo (App, web)](https://github.com/modrinth/code) | Modrinth, 2026 | Исходники Modrinth App |
| [itzg/docker-minecraft-server](https://github.com/itzg/docker-minecraft-server) | itzg, 2026 | Серверы в контейнерах со сборками Modrinth и CF |
| [docker-minecraft-server docs – Modrinth modpacks](https://docker-minecraft-server.readthedocs.io/en/latest/types-and-platforms/mod-platforms/modrinth-modpacks/) | itzg, живой | Использование `MODRINTH_MODPACK` |
| [mrpack-install](https://github.com/nothub/mrpack-install) | nothub, 2024 | CLI-установщик серверных паков .mrpack |
| [ServerStarterJar](https://github.com/neoforged/ServerStarterJar) | NeoForged, 2026 | `server.jar` для хостингов NeoForge и Forge |
| [Fabric server installation](https://fabricmc.net/use/server/) | FabricMC, живой | Серверный launcher Fabric |
| [Installing Fabric (players)](https://docs.fabricmc.net/players/installing-fabric/) | Fabric Docs, 2026 | Шаги установки для игроков |
| [Dependency overrides (Fabric)](https://docs.fabricmc.net/players/troubleshooting/dependency-overrides) | Fabric Docs, 2026 | Как составители сборок переопределяют неверные диапазоны версий |

## Шаблоны

Два проекта собраны и запущены в CI на GitHub Actions (ubuntu-latest, Temurin 25, 2026-11-07). Файлы лежат в `tests/minecraft/fabric-mod/` и `tests/minecraft/neoforge-mod/` репозитория навыка. Не хватает файлов Gradle wrapper: скопируйте `gradlew`, `gradlew.bat` и `gradle/wrapper/gradle-wrapper.jar` из соответствующего официального шаблона или выполните `gradle wrapper --gradle-version <версия>` на локальном Gradle ≥ 9.1.

### Мод Fabric

Minecraft **26.3**, Fabric Loader **0.20.1**, Loom **1.19.4** (`net.fabricmc.fabric-loom`), Fabric API **0.165.2+26.3**, Gradle **9.8.2**, Java **25**. Проверено в CI на GitHub Actions:

- `./gradlew build` сам запускает задачу `runGameTest` (безголовый сервер); в логе строка «All 2 required tests passed :)».
- В jar файл `fabric.mod.json` содержит раскрытую версию (`"version": "1.0.0"`).
- Если изменить проверяемый блок в тесте, `runGameTest` падает (негативная проверка в CI).

`settings.gradle`
<!-- template: tests/minecraft/fabric-mod/settings.gradle -->
```groovy
pluginManagement {
    repositories {
        maven { name = 'Fabric'; url = 'https://maven.fabricmc.net/' }
        mavenCentral()
        gradlePluginPortal()
    }
}
rootProject.name = 'examplemod'
```

`gradle.properties`
<!-- template: tests/minecraft/fabric-mod/gradle.properties -->
```properties
org.gradle.jvmargs=-Xmx2G
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=true

minecraft_version=26.3
loader_version=0.20.1
loom_version=1.19.4
fabric_api_version=0.165.2+26.3

mod_version=1.0.0
maven_group=com.example
archives_base_name=examplemod
```

`gradle/wrapper/gradle-wrapper.properties`
<!-- template: tests/minecraft/fabric-mod/gradle/wrapper/gradle-wrapper.properties -->
```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-9.8.2-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

`build.gradle`
<!-- template: tests/minecraft/fabric-mod/build.gradle -->
```groovy
plugins {
    id 'net.fabricmc.fabric-loom' version "${loom_version}"
    id 'maven-publish'
}

version = project.mod_version
group = project.maven_group
base { archivesName = project.archives_base_name }

loom {
    splitEnvironmentSourceSets()
    mods {
        "examplemod" {
            sourceSet sourceSets.main
            sourceSet sourceSets.client
        }
    }
}

fabricApi {
    configureTests {
        createSourceSet = true            // src/gametest/{java,resources}
        modId = "examplemod-test"
        enableGameTests = true            // server gametests run during `build`
        enableClientGameTests = false     // see optional CI job below
        eula = true                       // you agree to https://aka.ms/MinecraftEULA
    }
}

dependencies {
    minecraft "com.mojang:minecraft:${project.minecraft_version}"
    implementation "net.fabricmc:fabric-loader:${project.loader_version}"
    implementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_api_version}"

    testImplementation "net.fabricmc:fabric-loader-junit:${project.loader_version}"
}

processResources {
    def version = project.version
    inputs.property "version", version
    filesMatching("fabric.mod.json") { expand "version": version }
}

tasks.withType(JavaCompile).configureEach { it.options.release = 25 }

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

test { useJUnitPlatform() }
```

`src/main/resources/fabric.mod.json`
<!-- template: tests/minecraft/fabric-mod/src/main/resources/fabric.mod.json -->
```json
{
  "schemaVersion": 1,
  "id": "examplemod",
  "version": "${version}",
  "name": "Example Mod",
  "description": "Minimal Fabric 26.3 template.",
  "license": "MIT",
  "environment": "*",
  "entrypoints": {
    "main": ["com.example.ExampleMod"],
    "client": ["com.example.client.ExampleModClient"]
  },
  "depends": {
    "fabricloader": ">=0.20.1",
    "minecraft": "~26.3",
    "java": ">=25",
    "fabric-api": "*"
  }
}
```

`src/main/java/com/example/ExampleMod.java`
<!-- template: tests/minecraft/fabric-mod/src/main/java/com/example/ExampleMod.java -->
```java
package com.example;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ExampleMod implements ModInitializer {
    public static final String MOD_ID = "examplemod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Example mod initialised");
    }
}
```

`src/client/java/com/example/client/ExampleModClient.java`
<!-- template: tests/minecraft/fabric-mod/src/client/java/com/example/client/ExampleModClient.java -->
```java
package com.example.client;

import net.fabricmc.api.ClientModInitializer;

public final class ExampleModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
    }
}
```

`src/gametest/resources/fabric.mod.json`
<!-- template: tests/minecraft/fabric-mod/src/gametest/resources/fabric.mod.json -->
```json
{
  "schemaVersion": 1,
  "id": "examplemod-test",
  "version": "1.0.0",
  "name": "Example Mod Tests",
  "environment": "*",
  "entrypoints": {
    "fabric-gametest": ["com.example.test.ExampleModGameTest"]
  },
  "depends": { "examplemod": "*" }
}
```

`src/gametest/java/com/example/test/ExampleModGameTest.java` (форма скопирована из `ExampleFabricTestSuite` Fabric API: публичный нестатический метод с одним параметром `GameTestHelper`, структура по умолчанию `fabric-gametest-api-v1:empty`, окружение `minecraft:default`)
<!-- template: tests/minecraft/fabric-mod/src/gametest/java/com/example/test/ExampleModGameTest.java -->
```java
package com.example.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;

public class ExampleModGameTest {
    @GameTest
    public void placesDiamondBlock(GameTestHelper helper) {
        helper.setBlock(0, 1, 0, Blocks.DIAMOND_BLOCK);
        helper.succeedWhen(() -> helper.assertBlock(new BlockPos(0, 1, 0),
                block -> block == Blocks.DIAMOND_BLOCK,
                block -> Component.literal("Expected a diamond block")));
    }
}
```

### Мод NeoForge

Minecraft **26.3**, NeoForge **26.3.0.101**, ModDevGradle **2.0.148**, foojay resolver **1.0.0**, Gradle **9.2.1** (wrapper MDK), toolchain Java **25**. Проверено в CI на GitHub Actions:

- `./gradlew build`, затем `./gradlew runGameTestServer`: в логе «All 2 required tests passed :)». Если сломать проверку, запуск падает.
- Тесту нужна структура. Встроенной пустой структуры в NeoForge нет, поэтому CI генерирует `data/examplemod/structure/empty.nbt` скриптом навыка [`../scripts/EmptyStructure.java`](../scripts/EmptyStructure.java): `java EmptyStructure.java <out.nbt> 5023 3`. Число 5023 — DataVersion версии 26.3, взятая из `version.json` внутри серверного jar 26.3 (там же ресурспак 97.1 и датапак 121.0). Для другой версии Minecraft возьмите DataVersion из её серверного jar. Размер 3 даёт структуру 3x3x3.
- Сборка с ModDevGradle при `CI=true` пропустила перекомпиляцию Minecraft и заняла около минуты.

`settings.gradle`
<!-- template: tests/minecraft/neoforge-mod/settings.gradle -->
```groovy
pluginManagement {
    repositories { gradlePluginPortal() }
}
plugins {
    id 'org.gradle.toolchains.foojay-resolver-convention' version '1.0.0'
}
rootProject.name = 'examplemod'
```

`gradle.properties`
<!-- template: tests/minecraft/neoforge-mod/gradle.properties -->
```properties
org.gradle.jvmargs=-Xmx2G
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=true

minecraft_version=26.3
minecraft_version_range=[26.3]
neo_version=26.3.0.101

mod_id=examplemod
mod_name=Example Mod
mod_license=MIT
mod_version=1.0.0
mod_group_id=com.example.examplemod
```

`gradle/wrapper/gradle-wrapper.properties`
<!-- template: tests/minecraft/neoforge-mod/gradle/wrapper/gradle-wrapper.properties -->
```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-9.2.1-bin.zip
networkTimeout=10000
retries=0
retryBackOffMs=500
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

`build.gradle`
<!-- template: tests/minecraft/neoforge-mod/build.gradle -->
```groovy
plugins {
    id 'java-library'
    id 'net.neoforged.moddev' version '2.0.148'
}

version = mod_version
group = mod_group_id
base { archivesName = mod_id }

java.toolchain.languageVersion = JavaLanguageVersion.of(25)

neoForge {
    version = project.neo_version
    validateAccessTransformers = true

    runs {
        client {
            client()
            systemProperty 'neoforge.enabledGameTestNamespaces', project.mod_id
        }
        server {
            server()
            programArgument '--nogui'
            systemProperty 'neoforge.enabledGameTestNamespaces', project.mod_id
        }
        gameTestServer {
            type = "gameTestServer"          // ./gradlew runGameTestServer, exit code = failed required tests
            systemProperty 'neoforge.enabledGameTestNamespaces', project.mod_id
        }
        configureEach {
            logLevel = org.slf4j.event.Level.INFO
        }
    }

    mods {
        "${mod_id}" { sourceSet(sourceSets.main) }
    }
}

var generateModMetadata = tasks.register("generateModMetadata", ProcessResources) {
    var replaceProperties = [
            minecraft_version_range: minecraft_version_range,
            neo_version            : neo_version,
            mod_id                 : mod_id,
            mod_name               : mod_name,
            mod_license            : mod_license,
            mod_version            : mod_version,
    ]
    inputs.properties replaceProperties
    expand replaceProperties
    from "src/main/templates"
    into "build/generated/sources/modMetadata"
}
sourceSets.main.resources.srcDir generateModMetadata
neoForge.ideSyncTask generateModMetadata

tasks.withType(JavaCompile).configureEach { options.encoding = 'UTF-8' }
```

`src/main/templates/META-INF/neoforge.mods.toml`
<!-- template: tests/minecraft/neoforge-mod/src/main/templates/META-INF/neoforge.mods.toml -->
```toml
license="${mod_license}"

[[mods]]
modId="${mod_id}"
version="${mod_version}"
displayName="${mod_name}"
description='''
Minimal NeoForge 26.3 template.
'''

[[dependencies.${mod_id}]]
    modId="neoforge"
    type="required"
    versionRange="[${neo_version},)"
    ordering="NONE"
    side="BOTH"

[[dependencies.${mod_id}]]
    modId="minecraft"
    type="required"
    versionRange="${minecraft_version_range}"
    ordering="NONE"
    side="BOTH"
```

`src/main/java/com/example/examplemod/ExampleMod.java` (тестовая функция регистрируется через `BuiltInRegistries.TEST_FUNCTION`, как в документации NeoForge Game Tests)
<!-- template: tests/minecraft/neoforge-mod/src/main/java/com/example/examplemod/ExampleMod.java -->
```java
package com.example.examplemod;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(ExampleMod.MOD_ID)
public final class ExampleMod {
    public static final String MOD_ID = "examplemod";

    private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(BuiltInRegistries.TEST_FUNCTION, MOD_ID);

    static {
        TEST_FUNCTIONS.register("smoke", () -> ExampleMod::smoke);
    }

    public ExampleMod(IEventBus modBus) {
        TEST_FUNCTIONS.register(modBus);
    }

    private static void smoke(GameTestHelper helper) {
        helper.setBlock(0, 1, 0, Blocks.DIAMOND_BLOCK);
        helper.succeedWhen(() -> helper.assertBlock(new BlockPos(0, 1, 0),
                block -> block == Blocks.DIAMOND_BLOCK,
                block -> Component.literal("Expected a diamond block")));
    }
}
```

`src/main/resources/data/examplemod/test_instance/smoke.json`
<!-- template: tests/minecraft/neoforge-mod/src/main/resources/data/examplemod/test_instance/smoke.json -->
```json
{
  "type": "minecraft:function",
  "function": "examplemod:smoke",
  "environment": "minecraft:default",
  "structure": "examplemod:empty",
  "max_ticks": 100
}
```

Тестовые данные в `src/main/resources` попадают в jar мода; если это нежелательно, вынесите их в отдельный source set или датапак.

### Workflow CI для одного мода

Собран по проверенному заданию CI: адаптация заданий `fabric-mod` и `neoforge-mod` из `.github/workflows/minecraft.yml` репозитория навыка под мод в корне репозитория. Текущие мажоры действий: `actions/checkout@v7`, `actions/setup-java@v6` (в примерах Fabric и в исходной заметке стояли v6 и v5, проверено: v7 и v6). Fabric:

```yaml
name: build
on: [push, pull_request]
permissions:
  contents: read

jobs:
  fabric-mod:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v7
      - uses: actions/setup-java@v6
        with:
          distribution: temurin
          java-version: '25'
      - uses: gradle/actions/setup-gradle@v6
      - name: Build, unit tests and server gametests (headless server)
        run: ./gradlew build --stacktrace
      - name: Version expanded in fabric.mod.json
        run: |
          unzip -p build/libs/examplemod-1.0.0.jar fabric.mod.json | grep '"version": "1.0.0"'
      - name: A broken assertion must fail the gametest run
        run: |
          sed -i 's/block == Blocks.DIAMOND_BLOCK/block == Blocks.GOLD_BLOCK/' src/gametest/java/com/example/test/ExampleModGameTest.java
          if ./gradlew runGameTest; then echo 'gametest did not fail'; exit 1; fi
```

NeoForge (скрипт `EmptyStructure.java` скопируйте из навыка в `.github/scripts/`):

```yaml
name: build
on: [push, pull_request]
permissions:
  contents: read

jobs:
  neoforge-mod:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v7
      - uses: actions/setup-java@v6
        with:
          distribution: temurin
          java-version: '25'
      - uses: gradle/actions/setup-gradle@v6
      - name: Empty 3x3x3 structure for the gametest
        run: java .github/scripts/EmptyStructure.java src/main/resources/data/examplemod/structure/empty.nbt 5023 3
      - name: Build (ModDevGradle skips recompilation because CI=true)
        run: ./gradlew build --stacktrace
      - name: Run gametests on a headless server
        run: ./gradlew runGameTestServer --stacktrace
      - name: A broken assertion must fail the gametest run
        run: |
          sed -i 's/block == Blocks.DIAMOND_BLOCK/block == Blocks.GOLD_BLOCK/' src/main/java/com/example/examplemod/ExampleMod.java
          if ./gradlew runGameTestServer; then echo 'gametest did not fail'; exit 1; fi
          git checkout -- src/main/java/com/example/examplemod/ExampleMod.java
      - if: always()
        uses: actions/upload-artifact@v7
        with:
          name: neoforge-gametest-logs
          path: run/logs/
```

Для `grep` с `"version": "1.0.0"` нужен блочный скаляр `run: |`: простая строка YAML с `": "` ломает файл workflow.

### Релизный workflow (любой загрузчик) `[не проверено]`

Набросок из исходной заметки, в CI не запускался. Дополнение к `build.gradle`:

```groovy
plugins { id 'me.modmuss50.mod-publish-plugin' version '2.2.1' }
publishMods {
    file = jar.archiveFile                       // Fabric 26.1+, NeoForge, Forge; remapJar на Fabric <= 1.21.11
    changelog = providers.fileContents(layout.projectDirectory.file("CHANGELOG.md")).asText
    type = STABLE
    modLoaders.add("neoforge")                   // или "fabric"
    dryRun = providers.environmentVariable("MODRINTH_TOKEN").getOrNull() == null
    modrinth {
        projectId = "xxxxxxxx"
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        minecraftVersions.add("26.3")
    }
    curseforge {
        projectId = "123456"
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        minecraftVersions.add("26.3")
        client = true
        server = true
    }
}
```

```yaml
name: release
on:
  push:
    tags: ['v*']
permissions:
  contents: write
jobs:
  publish:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v7
      - uses: actions/setup-java@v6
        with: { distribution: temurin, java-version: '25' }
      - uses: gradle/actions/setup-gradle@v6
      - run: ./gradlew build publishMods
        env:
          MODRINTH_TOKEN: ${{ secrets.MODRINTH_TOKEN }}
          CURSEFORGE_TOKEN: ${{ secrets.CURSEFORGE_TOKEN }}
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

## Что в старых туториалах неверно

| Старый совет | Актуально (для какой версии) |
|---|---|
| «Java 17» и «Java 21 для последней» | Java 21 для 1.20.5–1.21.11, **Java 25 для 26.1+** |
| Следующая версия 1.22; снапшоты `YYwWWa` | 26.1, 26.2, 26.3 …; `26.4-snapshot-N`, `-pre-N`, `-rc-N` |
| Маппинги Yarn, `mappings "net.fabricmc:yarn:…"` | На 26.x маппингов нет, имена Mojang в jar; Yarn заканчивается на 1.21.11 |
| `id 'fabric-loom'`, `modImplementation`, `remapJar` | 26.x: `net.fabricmc.fabric-loom`, `implementation`, `jar`; ≤1.21.11: `net.fabricmc.fabric-loom-remap` |
| Зависимость `"fabric": "*"` | `"fabric-api": "*"` (26.1+) |
| Access wideners `accessWidener v2 named` | Class tweakers `classTweaker v1 official` (Loader ≥ 0.18, Loom ≥ 1.12) |
| Parchment для имён параметров | На 26.x не нужен (официальные имена параметров); для ≤1.21.11 полезен |
| Версии NeoForge вида `21.1.x` везде | 26.x: `26.3.0.101` (четыре части, `-beta` пока нестабильно) |
| NeoGradle как плагин NeoForge | ModDevGradle по умолчанию, NeoGradle ещё поддерживается |
| `mods.toml` | `neoforge.mods.toml` (NeoForge 20.5+) `[не проверено]` |
| `@Mod.EventBusSubscriber(bus = Bus.MOD)` | `@EventBusSubscriber(modid = …)` без `bus` |
| «Forge мёртв» и ForgeGradle 6 с `fg.deobf` | Forge 66.1.x для 26.3 с ForgeGradle 7 и Mavenizer |
| Quilt и QSL для новых модов | QSL и QFAPI закрыты для 26.1+, Quilt Loader по-прежнему запускает моды Fabric |
| CurseGradle для загрузок на CurseForge | mod-publish-plugin 2.2.1 или CurseForgeGradle |
| DCEVM для hot swap | JetBrains Runtime 25 и `-XX:+AllowEnhancedClassRedefinition` |
| `GuiGraphics`, `Screen#render`, `new ChunkPos(pos)` | 26.1: `GuiGraphicsExtractor`, `extractRenderState`, `ChunkPos.containing` |
| `ResourceLocation` | `Identifier` (переименован в 1.21.11) |
| Прямые вызовы OpenGL в рендер-модах | API Blaze3D; в 26.2 экспериментальный бэкенд Vulkan; удаление OpenGL планируется |
| Константы GLFW для клавиш и мыши | В 26.3 ввод перенесён на SDL; используйте `InputConstants` |
| `uses: actions/checkout@v6`, `setup-java@v5` | Актуальные мажоры: checkout v7, setup-java v6 |
| Простая строка YAML в `run:` с `": "` внутри (например grep по `"version": "1.0.0"`) | Блочный скаляр (`run:` и далее символ вертикальной черты): простая строка с `": "` ломает файл workflow |

## Чеклист ревью проекта мода

- [ ] Версия Minecraft и Java согласованы: Java 25 для 26.1+, Java 21 для 1.20.5–1.21.11; `--release` или toolchain совпадают с требованием игры.
- [ ] Fabric на 26.x использует id `net.fabricmc.fabric-loom` без `mappings`, `modImplementation` и `remapJar`; на ≤1.21.11 — `net.fabricmc.fabric-loom-remap`.
- [ ] В проекте нет Yarn, Intermediary и Quilt Mappings для 26.x; Parchment убран с 26.x.
- [ ] `fabric.mod.json` зависит от `fabric-api`, а не от `fabric`; диапазон Minecraft записан как `~26.3` (любой 26.3.x).
- [ ] Для NeoForge указана версия из четырёх частей, `minecraft_version_range` соответствует поддерживаемым версиям, `-beta` в релизе не используется.
- [ ] Выбран ModDevGradle (или осознанно NeoGradle); в MDG включён `validateAccessTransformers = true`.
- [ ] Access wideners заменены class tweakers (`classTweaker v1 official`), проверка `validateAccessWidener` проходит.
- [ ] Клиентский код отделён: `splitEnvironmentSourceSets()` у Fabric, нет клиентских классов в общем коде (иначе `NoClassDefFoundError` на сервере).
- [ ] Версии плагинов и зависимостей закреплены (`1.19.4`, а не `1.19-SNAPSHOT`; без `2.+`), wrapper проверяется `wrapper-validation`.
- [ ] Gradle не старше 9.1 для NeoForge 26.x и 9.0 для mod-publish-plugin 2.x.
- [ ] Включены `org.gradle.caching` и `org.gradle.configuration-cache`; токены читаются через `providers.environmentVariable`.
- [ ] Есть хотя бы один gametest; серверные gametest Fabric запускаются в `build`, NeoForge — через `runGameTestServer` с ненулевым кодом при провале.
- [ ] Для gametest NeoForge в репозитории или CI создаётся структура `.nbt` с верным DataVersion версии Minecraft.
- [ ] В CI есть негативная проверка: сломанное утверждение теста действительно роняет задание.
- [ ] YAML workflow не содержит простых скаляров с `": "`; команды с такими строками записаны через `run: |`.
- [ ] Мажоры действий актуальны (checkout v7, setup-java v6, gradle/actions v6, upload-artifact v7); версии Java в CI соответствуют матрице версий Minecraft.
- [ ] Библиотеки, которые уже есть в Minecraft (Gson, Guava, Netty, fastutil, JOML, SLF4J, Log4j, Brigadier, DataFixerUpper), не вшиты.
- [ ] Вшитые библиотеки идут через `include` (Fabric) или `jarJar` с диапазоном версий (NeoForge), а не через нерелоцированный shading; другие моды (Fabric API, Architectury API) объявлены зависимостями.
- [ ] Сторонние Maven объявлены через `exclusiveContent`; зависимости с Modrinth и CurseMaven не дублируют артефакты авторов.
- [ ] Необязательные интеграции защищены проверкой `isLoaded`/`isModLoaded` и не роняют игру при отсутствии мода.
- [ ] Окружение мода задано верно (`environment` в `fabric.mod.json`, `side` в зависимостях NeoForge), клиентские моды помечены для серверных паков.
- [ ] В релиз уходит нужный jar (`jar` на 26.x, не `-sources` и не `-dev`), тег загрузчика `neoforge`, а не `forge`.
- [ ] Номера версий уникальны на платформе и содержат Minecraft и загрузчик; changelog ведётся и передаётся в публикацию.
- [ ] Указан SPDX-идентификатор лицензии; решение по ARR и распространению в сборках принято осознанно.
- [ ] В репозитории и jar нет jar Minecraft, декомпилированных исходников и ванильных ресурсов; нет заявлений об официальном статусе.
- [ ] Для мультиверсионных проектов (Stonecutter) и мультилоадерных (MultiLoader-Template, Architectury) проверено, что выбранный инструмент поддерживает нужную версию 26.x (Architectury не поддерживает 26.1 и 26.1.1).
- [ ] Порты для старых версий не получаются простой перекомпиляцией: мод 1.21.11 и ранее на 26.x не запустится без пересборки.
- [ ] Поведение при разработке проверено: hot swap через JBR с `-XX:+AllowEnhancedClassRedefinition`, при проблемах с Mixin включён `mixin.debug.export`.

## Открытые вопросы

- **Версии шаблонов.** Gradle 9.9.0 (2026-11-06) не проверялся с Loom 1.19 и MDG 2.0.148: шаблоны закреплены на 9.8.2 и 9.2.1. Работает ли Loom 1.19.4 (а не `1.19-SNAPSHOT`) с Gradle 9.8.2 на 26.3, показано сборкой в CI.
- **Loom 1.20.** Есть альфы (1.20.0-alpha.19); тулчейн 26.4 может потребовать Loom 1.20, проверьте fabricmc.net/develop после релиза 26.4.
- **Совместимость API.** `GameTestHelper#assertBlock(BlockPos, Predicate, Function)` и `setBlock(int,int,int,Block)` взяты из тестового набора Fabric 26.3; сборка NeoForge в CI подтвердила их работу и там.
- **Статус NeoForge 26.3.** Сборки потеряли суффикс `-beta` (26.3.0.101), но записи о релизе 26.3 на neoforged.net не найдено (последние записи: релиз 26.1, уязвимости сети в мае 2026).
- **Architectury на 26.1 и 26.1.1.** Документация называет их неподдерживаемыми, 26.1.2 поддерживается; неясно, ведётся ли ещё ветка документации 26.1.x.
- **Имена задач Stonecutter** для CI (`chiseledBuild` и др.) для 0.9.x не перепроверялись.
- **Препроцессоры ReplayMod и Manifold** в современных модах 26.x не изучались, проверена только активность репозиториев.
- **JBR 25 и Minecraft 26.x.** Расширенное переопределение классов с Minecraft 26.x не тестировалось, README JBR заявляет поддержку в общем виде.
- **Доля CurseForge.** Цифры не собирались (нужен ключ API), проверены только данные Modrinth.
- **Платные моды.** В Usage Guidelines нет явного положения о платных модах (Patreon с ранним доступом); толкование остаётся за читателем.
- **Примеры NeoForge primers.** Расположение `neoforged/.github/primers/26.x` определено через API GitHub, содержимое не читалось.
- **EULA в gameTestServer.** В CI запуск `runGameTestServer` на NeoForge прошёл без правки `eula.txt`; для других версий и серверных запусков проверьте отдельно.
