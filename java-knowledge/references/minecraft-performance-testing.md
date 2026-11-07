# Моды и плагины Minecraft: замеры, профилирование, тесты и отладка

Справочник о том, как измерять, профилировать, тестировать и отлаживать моды и плагины Minecraft: Java Edition. Он питает конвейер оптимизации из [pipeline.md](pipeline.md): бенчмарк, базовый замер, ревью, решение из библиотеки, оптимизация, повторный замер. Покрыто: что мерить на сервере и клиенте; воспроизводимые замеры ванильными средствами (`/tick`, `/perf`, `/jfr`, F3-графики, `--tracy`); бенчмарк мира командой `/tick sprint` и скрипт `tick_sprint_bench.sh`; JMH для чистой логики мода; профилировщики (spark, JFR и JMC, async-profiler, VisualVM, Tracy, RenderDoc, Nsight); уроки модов оптимизации; таблица «симптом → техника → источник»; тесты (ванильный GameTest, серверные и клиентские gametest Fabric, gametest и JUnit в NeoForge, MockBukkit); отладка (краш-репорты, ошибки Mixin, несовпадение сторон, горячая перезагрузка); чеклист ревью. Примеры разнесены по видам модов: контентные, технические и автоматизация, магия, генерация мира, интерфейс и QoL, клиентский рендеринг, серверные и библиотеки.

Актуально на ноябрь 2026: Minecraft 26.3 (2026-09-15) и последняя линия 1.21.x (1.21.11), Java 25 для 26.x, JDK 25 LTS и JDK 27 как последний выпуск. Из инструментов темы: spark 1.10.187, JMH 1.37, async-profiler 4.6, Fabric Loader 0.20.1, Fabric API 0.165.2+26.3, NeoForge 26.3.0.101, ModDevGradle 2.0.148. Пометки: `[не проверено]` — факт из одного вторичного источника, вывод из кода или метаданных, либо команда или шаблон, которые не запускались; `[устарело]` — рядом названа замена. Данные проверены 2026-11-07. Шаблоны с пометкой «проверено в CI на GitHub Actions» собираются и запускаются в CI репозитория скилла. minecraft.wiki отвечает 403 на curl (защита от ботов), но открывается в браузере.

Общая теория JVM-профилирования и JMH — [performance.md](performance.md); общие тесты — [testing.md](testing.md); сборка проекта, Loom и ModDevGradle — [minecraft-modding.md](minecraft-modding.md); API игры, миксины, сеть — [minecraft-mod-api.md](minecraft-mod-api.md); серверные плагины и Paper — [minecraft-server-plugins.md](minecraft-server-plugins.md); документация и эталонные кодовые базы — [minecraft-resources.md](minecraft-resources.md). Скрипты: [`../scripts/tick_sprint_bench.sh`](../scripts/tick_sprint_bench.sh), [`../scripts/BenchCompare.java`](../scripts/BenchCompare.java), [`../scripts/EmptyStructure.java`](../scripts/EmptyStructure.java).

- [Версии на ноябрь 2026](#версии-на-ноябрь-2026)
- [Симптом → техника → источник](#симптом--техника--источник)
- [Что и как мерить](#что-и-как-мерить)
- [Встроенные инструменты ванильной игры](#встроенные-инструменты-ванильной-игры)
- [Флаги запуска и системные свойства](#флаги-запуска-и-системные-свойства)
- [Бенчмарк мира: /tick sprint](#бенчмарк-мира-tick-sprint)
- [JMH для логики мода](#jmh-для-логики-мода)
- [Профилирование: spark, JFR и другие](#профилирование-spark-jfr-и-другие)
- [Уроки модов оптимизации](#уроки-модов-оптимизации)
- [Типичные проблемы производительности в модах](#типичные-проблемы-производительности-в-модах)
- [Тесты: GameTest и юнит-тесты](#тесты-gametest-и-юнит-тесты)
- [Отладка](#отладка)
- [Как встроить Minecraft в конвейер оптимизации](#как-встроить-minecraft-в-конвейер-оптимизации)
- [Чеклист ревью производительности и стабильности](#чеклист-ревью-производительности-и-стабильности)
- [Открытые вопросы](#открытые-вопросы)

---

## Версии на ноябрь 2026

Источники: `version_manifest_v2.json` Mojang и JSON версии 26.3 (piston-meta), API Modrinth, метаданные Maven (maven.fabricmc.net, maven.neoforged.net, Maven Central, Gradle Plugin Portal), API GitHub, misode/mcmeta (ванильный `en_us.json` снапшота 26.4-snapshot-7).

| Что | Версия, дата | Примечание |
|---|---|---|
| Minecraft, последний релиз | 26.3 (2026-09-15) | 26.2 — 2026-06-16, 26.1.2 — 2026-04-09, 26.1 — 2026-03-24; 26.4-snapshot-7 — 2026-11-03 |
| Последняя линия 1.21.x | 1.21.11 (2025-12-09) | Последний обфусцированный релиз; 26.1 — первый без обфускации |
| Java для 26.x | Java 25 (`javaVersion.majorVersion: 25`, компонент `java-runtime-epsilon`) | Из JSON версии 26.3 |
| Ванильная привязка Tracy | `com.mojang:jtracy:1.14.38` (натив для linux, macos, windows) | Аргумент клиента `--tracy` с 24w37a (1.21.2); тайминги GPU с 26.2 |
| LWJGL в 26.3 | 3.4.3, включая `lwjgl-vulkan`, `lwjgl-sdl`, `lwjgl-shaderc`, `lwjgl-vma` | В 26.2 появился экспериментальный бэкенд Vulkan; в 26.3 GLFW заменён на SDL |
| Fabric Loader | 0.20.1 (`latest` в Maven) | В блоге на запуске 26.3 рекомендован 0.19.5 |
| Fabric Loom | 1.19.4 (`release` в Maven) | Блог: Loom 1.17 и Gradle 9.6.0 для 26.3; id плагина `net.fabricmc.fabric-loom` (без ремаппинга) с 26.1 |
| fabric-loader-junit | 0.20.1 | Та же линия версий, что у загрузчика |
| Fabric API | 0.165.2+26.3; 0.165.4+26.4 (снапшот) | Модули gametest: `fabric-gametest-api-v1`, `fabric-client-gametest-api-v1` |
| Sponge Mixin (форк Fabric) | 0.17.5+mixin.0.8.7 | `net.fabricmc:sponge-mixin` |
| NeoForge | 26.3.0.101 (также 26.1.2.126, 21.11.48) | Четырёхчастные версии с 26.1 |
| ModDevGradle | 2.0.148 | Типы запусков `client`, `data`, `gameTestServer`, `server`; `unitTest { enable() }` |
| NeoForge testframework | 26.1.2.126 — последняя опубликованная | Даёт JUnit-расширение `EphemeralTestServerProvider` |
| spark | 1.10.187 (2026-09-19) для Fabric, NeoForge, Forge; MC 26.1–26.3 | Встроен в Paper с 1.21 |
| JMH | 1.37 | Плагин Gradle `me.champeau.jmh` 0.7.4 (2026-10-29) |
| async-profiler | 4.6 (2026-10-19) | Использует spark на Linux и macOS (x86_64, aarch64) |
| VisualVM | 2.2.2 (2026-09-08) | |
| Tracy | v0.15.0 (2026-11-05) | GUI профилировщика и `tracy-capture`; версия протокола должна совпадать с клиентом jtracy в игре. Совместимость `[не проверено]` |
| RenderDoc | v1.47 (2026-10-31) | |
| JetBrains Runtime | jbr-release-25.0.4.1b329.142 (2026-10-28) | Расширенная перезагрузка классов для hot reload |
| MockBukkit | `org.mockbukkit.mockbukkit:mockbukkit-v26.3` 4.119.0 (2026-10-28) | Есть артефакты для v26.2, v26.1.2, v1.21 |
| Paper Timings | Пустышка с Paper 1.21.3; spark встроен с 1.21 | Классы Timings API оставлены для совместимости |
| Возможности JDK 25 для Minecraft | JEP 519 Compact Object Headers (продуктовый флаг, по умолчанию выключен), JEP 518 JFR Cooperative Sampling, JEP 520 JFR Method Timing & Tracing, JEP 509 JFR CPU-Time Profiling (экспериментальный, только Linux) | Все «Closed / Delivered», Release 25 |
| Sodium | 0.9.3-beta.3 для 26.3 (2026-11-07); 0.8.15-beta.1 для 1.21.11 | 0.9.0 перевёл весь рендеринг на Blaze3D, добавил экспериментальный Vulkan и асинхронное occlusion culling |
| Lithium | 0.26.3 для 26.3 (2026-10-28) | Fabric и NeoForge |
| FerriteCore | 9.0.0 для 26.1–26.3 (2026-03-24) | Fabric и NeoForge |
| ModernFix | 5.27.27+mc26.1.2 (на 26.x только NeoForge); сборки Fabric остановились на 1.21.1 | Сборок 26.2 и 26.3 на Modrinth нет; есть форк сообщества «ModernFix-mVUS» |
| C2ME | 0.4.2-alpha.0.116+26.3 (только Fabric) | Есть и сборки для снапшотов 26.4 |
| ScalableLux | 0.3.0-alpha для 26.3 (Fabric, NeoForge) | Форк Starlight с необязательными параллельными обновлениями света |
| Starlight | Заархивирован (GitHub archived; последняя загрузка для 1.20.4) | Развивается внутри Moonrise и Paper. `[устарело]` — замена: Moonrise или ScalableLux |
| Moonrise | 1.2.2 для 26.3, 1.0.3 для 26.1.x LTS, 1.3.0-alpha для снапшотов 26.4 | Система чанков Paper и Starlight для Fabric и NeoForge |
| Krypton | 0.3.2 для 26.3 (только Fabric) | |
| EntityCulling | 1.11.3 (Fabric, NeoForge; 26.1.x–26.3) | |
| ImmediatelyFast | 1.17.1+26.3 (Fabric, NeoForge); 1.6.15 для 1.21.1 | |
| Noisium | GitHub заархивирован 2025-07; статус на Modrinth «unlisted»; последняя сборка 2.7.0 для 1.21.6 | Сборок 26.x нет. `[устарело]` — замена для пропускной способности генерации мира: C2ME |
| Observable | Последняя сборка на Modrinth: 1.21.4 и 1.21.1 (2025) | В репозитории ещё есть коммиты в ветке 1.21 |
| Not Enough Crashes | Версии на Modrinth для 26.1.2–26.3 (Fabric, NeoForge) | |
| Crash Assistant | 1.11.14 для 26.2–26.3 (Fabric, Forge, NeoForge) | |
| Neruina (исправление тикающих сущностей) | Сборки для 26.1.2–26.3 | |

---

## Симптом → техника → источник

Таблица для шага «ревью» и «решения из библиотеки» в [pipeline.md](pipeline.md). Сначала найдите симптом в профиле (spark, JFR, F3, Tracy), затем применяйте технику и перемеряйте по тому же протоколу. Строки, помеченные `[не проверено]`, опираются на один вторичный источник или вывод из кода.

| Что видно | Техника | Источник |
|---|---|---|
| MSPT растёт линейно с числом поставленных машин; в spark на виде Sources виден `tick` блок-сущности | Тикать только при необходимости: засыпание и пробуждение по событиям, расписание «раз в N тиков», `null` вместо тикера на стороне, которой тикер не нужен | [NeoForge BE docs](https://docs.neoforged.net/docs/blockentities/), [Lithium config](https://github.com/CaffeineMC/lithium/blob/develop/lithium-fabric-mixin-config.md) |
| Блок-сущность нужна только для 2–3 состояний | Заменить свойствами состояния блока | [NeoForge BE docs](https://docs.neoforged.net/docs/blockentities/) |
| Высокая скорость аллокаций (F3 `memory` МБ/с, spark `--alloc`), в основном `BlockPos` | `MutableBlockPos`, ключи `asLong()`, не звать `immutable()` в циклах | [Lithium `mixin.alloc.chunk_random`](https://github.com/CaffeineMC/lithium/blob/develop/lithium-fabric-mixin-config.md) |
| `Enum.values()` и `Direction.values()` в профиле аллокаций | Кэшировать массивы в `static final` | [Lithium `mixin.alloc.enum_values`](https://github.com/CaffeineMC/lithium/blob/develop/lithium-fabric-mixin-config.md) |
| Кадры stream и лямбд в горячем пути тика | Обычные циклы, вынести лямбды из цикла | [Lithium `mixin.alloc.deep_passengers`](https://github.com/CaffeineMC/lithium/blob/develop/lithium-fabric-mixin-config.md) |
| Горячие `HashMap.getNode`, `Integer.valueOf` | Примитивные map и set из fastutil | [Lithium `mixin.collections.*`](https://github.com/CaffeineMC/lithium/blob/develop/lithium-fabric-mixin-config.md) |
| Поиск возможностей и инвентаря соседей каждый тик (трубы, конвейеры, воронки) | `BlockCapabilityCache` и `invalidateCapabilities`; кэш инвентаря в стиле Lithium | [NeoForge capabilities](https://docs.neoforged.net/docs/inventories/capabilities), [Lithium `mixin.block.hopper`](https://github.com/CaffeineMC/lithium/blob/develop/lithium-fabric-mixin-config.md) |
| Доминирует `getEntitiesOfClass`; магниты, ауры, турели | Меньший AABB, реже вызовы, поиск по группам типов, отслеживание по событиям `[не проверено]` | [Lithium entity collisions](https://github.com/CaffeineMC/lithium/blob/develop/lithium-fabric-mixin-config.md) |
| Сканы блоков по радиусу каждый тик (карьер, проверка ритуала, валидация мультиблока) | Курсор с амортизацией; проверка при смене соседей; кэш структуры `[не проверено]` | [NeoForge BE docs](https://docs.neoforged.net/docs/blockentities/) |
| «Найти ближайший X» для ИИ медленно | Типы POI вместо сканирования | [Lithium `mixin.ai.poi`](https://github.com/CaffeineMC/lithium/blob/develop/lithium-fabric-mixin-config.md) |
| Мобы с поиском пути доминируют в MSPT | Ограничить пересчёт путей; дешевле оценивать узлы | [Lithium `mixin.ai.pathing`](https://github.com/CaffeineMC/lithium/blob/develop/lithium-fabric-mixin-config.md) |
| Чанки грузятся или генерируются рядом с машинами, куда никто не заходит | Проверять `hasChunkAt` или `isLoaded` перед доступом | [NeoForge BE docs](https://docs.neoforged.net/docs/blockentities/) |
| Блоки-чанклоадеры держат целые области в тиках | `TicketType` с использованием `LOADING`, срок жизни, минимальный радиус | [NeoForge 1.21.5 primer: Tickets](https://github.com/neoforged/.github/blob/main/primers/1.21.5/index.md) |
| Всплески на графике трафика (F3+3); поток `minecraft.PacketSent` в JFR | Синхронизация при изменении с ограничением частоты; дельты; только ближайшие игроки | [NeoForge payload docs](https://docs.neoforged.net/docs/networking/payload), [Fabric networking](https://docs.fabricmc.net/develop/networking) |
| Полный NBT блок-сущности отправляется постоянно | Малый пользовательский пакет или слоты данных меню; `getUpdateTag` только при загрузке чанка | [NeoForge BE docs](https://docs.neoforged.net/docs/blockentities/) |
| Синхронизация вложений пересылает большие данные | Писать только изменённые поля при `initialSync == false`; фильтр `sendToPlayer` | [NeoForge attachments](https://docs.neoforged.net/docs/datastorage/attachments) |
| Остановки главного потока при приходе больших пакетов | `HandlerThread.NETWORK` и `enqueueWork` | [NeoForge payload docs](https://docs.neoforged.net/docs/networking/payload) |
| Отключения из-за слишком больших пакетов | Соблюдать лимиты 1 МиБ и 32 КиБ; делить или сжимать | [NeoForge payload docs](https://docs.neoforged.net/docs/networking/payload) |
| Тихие сбои в обработке пакетов | `.exceptionally(...)` на futures от `enqueueWork` | [NeoForge payload docs](https://docs.neoforged.net/docs/networking/payload) |
| Всплески времени кадра при множестве рендереров блок-сущностей и сущностей на экране | Считать заранее в тике; снимки состояния для рендера; батчинг | [ImmediatelyFast](https://github.com/RaphiMC/ImmediatelyFast), [Fabric BER docs](https://docs.fabricmc.net/develop/blocks/block-entity-renderer) |
| Скрытые сущности и блок-сущности всё равно стоят GPU и CPU | Правильные ограничивающие объёмы рендера; occlusion culling | [EntityCulling](https://github.com/tr7zw/EntityCulling), [MoreCulling](https://github.com/fxmorin/moreculling) |
| Статичный декор рисуется рендерером блок-сущности каждый кадр | Запечь в модель блока и меш чанка | [Sodium 0.5.0 notes](https://modrinth.com/mod/sodium/version/mc1.20.1-0.5.0) `[не проверено]` |
| Мод ломается или тормозит на бэкенде Vulkan 26.2+ | Заменить сырой GL на Blaze3D | [Fabric 26.2 post](https://fabricmc.net/2026/06/15/262.html) |
| Долгий старт, в профиле много «model baking» | Меньше свойств у состояний блока; ленивая запечка | [FerriteCore summary](https://github.com/malte0811/FerriteCore/blob/26.1/summary.md), [ModernFix dynamic resources](https://github.com/embeddedt/ModernFix/wiki/Dynamic-Resources-FAQ) |
| В куче доминируют дубликаты квадов, строк и map | Дедупликация и общие неизменяемые экземпляры | [FerriteCore summary](https://github.com/malte0811/FerriteCore/blob/26.1/summary.md) |
| `Method.invoke` и `Field.get` в горячих кадрах | Accessor и Invoker миксины, access wideners и transformers, `MethodHandle` | [Access Transformers](https://docs.neoforged.net/docs/advanced/accesstransformers), [Access Wideners](https://wiki.fabricmc.net/tutorial:accesswideners) |
| Кадры обработчиков Mixin в очень горячих ванильных методах | Узкие точки внедрения; обёртки MixinExtras; без `@Overwrite` | [MixinExtras](https://github.com/LlamaLad7/MixinExtras/wiki) `[не проверено]` |
| Падения и странности генерации мира только с C2ME или Moonrise | Оставаться внутри `WorldGenRegion`; использовать выданный `RandomSource` | [C2ME README](https://github.com/RelativityMC/C2ME-fabric) |
| Лаги света, когда блоки часто меняют излучение | Не переключать уровень света каждый тик; группировать смены состояний | [Starlight gist](https://gist.github.com/Spottedleaf/6cc1acdd03a9b7ac34699bf5e8f1b85c) `[не проверено]` |
| Штормы обновлений редстоуна и соседей от блоков мода | Верные флаги обновления; без каскадных `updateNeighbors` | [Alternate Current](https://github.com/SpaceWalkerRS/alternate-current) `[не проверено]` |
| Тысячи сущностей предметов и опыта | Объединять или ограничивать сущности | [Clumps](https://github.com/jaredlll08/Clumps) |
| Лаг сервера растёт с числом игроков | Карты областей для запросов «ближайшие игроки»; кэш трекеров | [VMP-fabric](https://github.com/RelativityMC/VMP-fabric) |
| Всплески автосохранения (JFR `ChunkRegionWrite`, всплески spark каждые 5 минут) | Держать NBT блок-сущностей и вложений малым; `setChanged` только при реальном изменении | [NeoForge BE docs](https://docs.neoforged.net/docs/blockentities/), [NeoForge attachments](https://docs.neoforged.net/docs/datastorage/attachments) |
| Куча растёт после выхода из миров | Чистить статические кэши при выгрузке; слабые ссылки; сравнение `heapsummary` до и после | [spark heapsummary](https://spark.lucko.me/docs/Command-Usage), [Memory Leak Fix](https://github.com/fxmorin/memoryLeakFix) `[не проверено]` |
| Клиентский тик тяжёл от частиц (магические эффекты, машины) | Меньше частиц, проверки расстояния; ваниль 26.2 случайно отбрасывает частицы сверх лимита | [Java Edition 26.2](https://minecraft.wiki/w/Java_Edition_26.2) |
| Всплески «догоняния» сервера после лага | Уменьшить догоняние TPS | [Moonrise](https://github.com/Tuinity/Moonrise) |
| Впустую тратится CPU при неактивном окне | Снижать частоту рендера в простое | [Dynamic FPS](https://codeberg.org/LostLuma/Dynamic-FPS) |
| Моды с тяжёлой генерацией мира замедляют pregeneration | Параллельно-безопасные фичи; профиль `ChunkGeneration` по статусам | [C2ME](https://github.com/RelativityMC/C2ME-fabric), [Commands/jfr](https://minecraft.wiki/w/Commands/jfr) |

---

## Что и как мерить

**Ключевые выводы**

- **Бюджет тика сервера — 50 мс** (20 TPS). Сообщайте MSPT (миллисекунды на тик: среднее плюс p95 и p99), а не только TPS. TPS остаётся 20, пока MSPT не превысит 50 мс, и поэтому скрывает потерю запаса. По руководству spark доля сна у «Server thread» ниже 20 % означает «работает на пределе», ниже 5 % — «отстаёт». Средние значения скрывают всплески.
- **Клиент**: FPS — плохая метрика. Записывайте время кадра в мс с перцентилями (1 % и 0,1 % low) и числом статтеров. График FPS в F3 показывает последние 240 времён кадра с min, avg и max. Tracy с `--tracy` даёт зоны по кадрам, а с 26.2 и тайминги GPU.
- **Генерация и загрузка чанков**: чанков в секунду при pregeneration (Chunky), события JFR `minecraft.ChunkGeneration` (поля `status`, `level`, позиция чанка) и `StructureGeneration`, события ввода-вывода региональных файлов. Генерация мира многопоточная, поэтому важны и время по часам, и CPU по потокам.
- **Память**: куча после полного GC (живой набор), скорость аллокаций (запись `memory` в F3 показывает МБ/с; JFR `ThreadAllocationStatistics`; режим spark `--alloc`) и паузы GC. Скорость аллокаций — главный сигнал для проблем «мусор в каждом тике».
- **Старт**: время до заголовочного экрана и до «Done» на сервере. Кейсы ModernFix и FerriteCore показывают, что в больших сборках доминируют запуск и запечка моделей. Мерьте по меткам времени в `latest.log` или через JFR (`--jfrProfile` с запуска). Часть данных `[не проверено]`.
- **Сохранение мира и автосохранение**: события JFR `minecraft.ChunkRegionWrite`, всплески spark на интервалах автосохранения, время `/save-all flush` на тестовом сервере. Часть данных `[не проверено]`.
- **Сеть**: события JFR `minecraft.PacketSent`, `PacketReceived`, `NetworkSummary`; график трафика F3+3; `spark health --network`.
- **Бюджеты, которые мод заявляет явно**: MSPT на каждые 100 машин, на активный ритуал, на загруженную структуру мода; байты аллокаций за тик; пакетов в секунду на игрока; секунды добавленного старта; мегабайты добавленной кучи с контентом мода.

**Протокол воспроизводимости** (шаг «базовый замер» конвейера; сведён из источников ниже `[не проверено]`):

1. Фиксированный seed и сохранённый тестовый мир как артефакт (или детерминированная перегенерация) с известной позицией игрока. Заморозьте смену дня и погоду и спавн мобов, если сценарий позволяет. Идентификаторы игровых правил в 1.21.11 (25w44a) перешли из camelCase (`doDaylightCycle`) в snake_case: смотрите актуальный id в автодополнении `/gamerule`. Сама генерация мира не детерминирована от запуска к запуску (README C2ME ссылается на MC-55596), поэтому мерьте пропускную способность генерации, а не побайтовый результат.
2. Предгенерируйте область через Chunky, чтобы бенчмарк тиков не включал генерацию.
3. Одна JVM, одинаковые `-Xms` и `-Xmx` (равные значения), один GC, один набор модов. Версии записывайте в файл результата.
4. Прогрев JIT: гоняйте сценарий 1–2 минуты до замера, первый прогон отбрасывайте.
5. Мерьте инструментом с перцентилями: `/tick sprint`, `/tick query` (P50, P95, P99), spark (окна `mspt`, tickmonitor), JFR (`ServerTickTime`). Минимум 3–5 повторов, сообщайте медиану и разброс.
6. Меняйте одно за раз, перемеряйте по тому же протоколу, храните сырые файлы (JFR, ссылки spark, JSON JMH).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [The tick loop](https://spark.lucko.me/docs/guides/The-tick-loop) | lucko (документация spark) | Бюджет 50 мс, эвристики доли сна, почему средние обманывают |
| [TPS and MSPT](https://spark.lucko.me/docs/guides/TPS-and-MSPT) | lucko (документация spark) | Определения TPS и MSPT в отчётах spark |
| [Finding lag spikes](https://spark.lucko.me/docs/guides/Finding-lag-spikes) | lucko (документация spark) | Процедура `tickmonitor` и `--only-ticks-over` |
| [Tick](https://minecraft.wiki/w/Tick) | Minecraft Wiki | Игровой тик, тик чанка, случайный и запланированный тик |
| [Debug screen](https://minecraft.wiki/w/Debug_screen) | Minecraft Wiki, 2026 | Записи F3 (fps, memory со скоростью аллокаций, статистика чанков) и графики |
| [Chunky (GitHub)](https://github.com/pop4959/Chunky) | pop4959 | Pregeneration для воспроизводимых миров; Fabric, NeoForge, Paper, Folia, 26.1–26.3 |
| [Chunky (Modrinth)](https://modrinth.com/mod/chunky) | pop4959 | Страница загрузки и матрица версий |
| [C2ME README (vanilla parity, MC-55596)](https://github.com/RelativityMC/C2ME-fabric) | RelativityMC | Генерация мира недетерминирована между запусками даже при фиксированном seed |

---

## Встроенные инструменты ванильной игры

**Ключевые выводы**

- **`/tick`** (с 1.20.3 / 23w43a, уровень прав 3) — лучший ванильный драйвер бенчмарка.
  - `/tick query` печатает (точные строки перевода из `en_us.json` снапшота 26.4-snapshot-7): `Target tick rate: %s per second.\nAverage time per tick: %sms (Target: %sms)` и `Percentiles: P50: %sms P95: %sms P99: %sms. Sample: %s`. Статусные строки включают `The game is running, but can't keep up with the target tick rate`. Итого: среднее и P50, P95, P99 MSPT.
  - `/tick sprint <time>` гонит тики максимально быстро `<time>` тиков (+1), игнорируя целевую частоту, и печатает `Sprint completed with %s ticks per second, or %s ms per tick`. `<time>` принимает `t` (по умолчанию), `s` (20 тиков), `d` (24000 тиков). Подробности и проверенный в CI скрипт — в разделе [Бенчмарк мира: /tick sprint](#бенчмарк-мира-tick-sprint).
  - `/tick freeze` и `/tick step [<time>]` замораживают мир (игроки продолжают тикать) для детерминированной отладки машин, ИИ и редстоуна; `/tick rate <1.0..10000.0>` меняет целевую частоту.
  - Ловушка (26.3-snapshot-1 и новее): `/tick step`, `/tick step stop`, `/tick sprint stop` теперь *завершаются ошибкой*, а не возвращают 0, если ничего не запущено. Обвязка на командных блоках и функциях, которая рассчитывала на результат 0, должна измениться.
  - Ловушка: sprint измеряет только работу сервера; на встроенном сервере цикл рендера клиента всё равно конкурирует за CPU. Для чистых чисел бенчмаркайте выделенный сервер (или `net.minecraft.gametest.Main`) `[не проверено]`.
- **`/debug start` и `/debug stop`** (уровень 3): в текущих версиях выводят только `Stopped tick profiling after %s second(s) and %s tick(s) (%s tick(s) per second)` — грубую пробу TPS. Старые `profile-results-*.txt` и `/debug report` удалены (в 1.17 заменены F3+L и `/perf`). `/debug function <id>` пишет `debug/debug-trace-<timestamp>.txt` с трассой каждой команды (полезно для модов с большим числом датапак-функций).
- **`/perf start|stop`** (только выделенный сервер, уровень 4): 10-секундный прогон профилирования, ZIP в `debug/profiling` с `profiling.txt`, `threads.txt`, `system.txt`, `classpath.txt`, `gamerules.txt`, `modules.txt`, `server.properties.txt`, логами отклонений тиков, CSV в `metrics/` (CPU, JVM, event loops, mailboxes, ticking) и данными по измерениям в `levels/` (блок-сущности, чанки, сущности, чанки сущностей). В одиночной игре используйте **F3+L** (тоже 10 секунд, пишет папки `client/` и `server/`).
- **Как читать `profiling.txt`** (документация NeoForge): строки вида `[03] tick(201/1) - 99.98%/39.57%` — глубина, имя секции, всего вызовов и вызовов за тик, доля родителя и доля всего тика; строки счётчиков вида `[07] #tickNonPassenger 4585/22`.
- **Свои секции в ванильном профилировщике** (видны на круговой диаграмме F3+1 и экспортируются как зоны Tracy):

  ```java
  // 1.21.2+ (и 26.x): профилировщик глобальный, не level.getProfiler()
  ProfilerFiller p = Profiler.get();
  p.push("examplemod:pipe_network");
  try { network.tick(); } finally { p.pop(); }
  p.incrementCounter("examplemod:items_moved");
  ```

  `level.getProfiler()` и `Minecraft#getProfiler` — `[устарело]`, замена `Profiler.get()` с 1.21.2 (primer NeoForge 1.21.2).
- **`/jfr start|stop`** (с 1.18 / 21w37a, уровень 4) пишет файл `.jfr` и JSON-сводку в `debug/` и логирует сводку. Пользовательские события JFR (имена из `jfr metadata` по записи; классы есть в 1.21.11 в `net.minecraft.util.profiling.jfr.event`): `minecraft.ChunkGeneration` (worldPosX/Z, chunkPosX/Z, status, level), `minecraft.LoadWorld`, `minecraft.NetworkSummary` (remoteAddress, байты и пакеты отправлено и получено), `minecraft.PacketReceived`, `minecraft.PacketSent` (protocolId, packetId, bytes), `minecraft.ServerTickTime` (averageTickDuration). В 1.21.11 есть ещё `ChunkRegionReadEvent`, `ChunkRegionWriteEvent`, `StructureGenerationEvent` и клиентский `ClientFpsEvent` (поле `fps`). JSON-сводка строится из классов `jfr.stats`: `TickTimeStat`, `ChunkGenStat`, `StructureGenStat`, `FileIOStat`, `GcHeapStat`, `ThreadAllocationStat`, `CpuLoadStat`, `FpsStat`, `IoSummary`. Точные имена `minecraft.*` у новых классов `[не проверено]`: сверьтесь командой `jfr metadata file.jfr | grep -A20 minecraft`.
- **Аргументы запуска клиента** (Minecraft Wiki): `--jfrProfile` (JFR с запуска: измеряет старт и загрузку ресурсов), `--tracy` (Tracy с запуска, с 24w37a / 1.21.2), `--tracyNoImages` (без кадров-изображений на таймлайне Tracy), `--renderDebugLabels` (метки отладки GPU, с 25w09a / 1.21.5), `--vulkanValidation` (слои валидации Vulkan и метки, с 26.2-snapshot-1), `--quickPlaySingleplayer [world]` (загрузить мир сразу: подходит для скриптовых клиентских бенчмарков; имя мира необязательно с 25w19a). MC-307177 «Включение профилирования JFR роняет клиент при запуске» исправлено в 26.2.
- **Экран отладки F3** (переработка 1.21.9, 26.x): `F3+F6` открывает «Debug Options» (в ранних сборках 1.21.9 это был F3+F5) с режимами Off, In Overlay, Always по записям и двумя встроенными профилями, «Default» и «Performance». Графики: `F3+1` круговая диаграмма профилировщика, `F3+2` FPS (слева) и TPS (справа; одиночная игра или оператор), `F3+3` трафик и пинг, `F3+4` lightmap (26.1+). Столбцы TPS складывают простой, прочее, запланированные задачи и основную серверную логику. Руководства с «Alt+F3 / Shift+F3» — `[устарело]` с 23w33a (1.20.2). Моды добавляют записи через `DebugScreenEntry` (primer 1.21.9).
- **Ванильные отладочные рендереры**: с 1.21.9 ваниль открывает свои отладочные рендереры через свойства JVM (`-DMC_DEBUG_ENABLED` и отдельные флаги) и API подписок, который синхронизирует состояние сервера с клиентом. Используйте его для визуализации поиска пути, границ структур или сетей труб вместо самодельных частиц. Точный список флагов `[не проверено]`.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Commands/tick](https://minecraft.wiki/w/Commands/tick) | Minecraft Wiki, 2026 | Синтаксис, семантика freeze, step, sprint, смена поведения ошибок в 26.3 |
| [Commands/debug](https://minecraft.wiki/w/Commands/debug) | Minecraft Wiki, 2026 | `/debug start/stop/function`, удалённые выводы |
| [Commands/perf](https://minecraft.wiki/w/Commands/perf) | Minecraft Wiki, 2026 | Структура ZIP отчётов `/perf` |
| [Commands/jfr](https://minecraft.wiki/w/Commands/jfr) | Minecraft Wiki, 2026 | Место вывода, группы событий |
| [Java Edition client command line arguments](https://minecraft.wiki/w/Java_Edition_client_command_line_arguments) | Minecraft Wiki, 2026 | `--jfrProfile`, `--tracy`, `--renderDebugLabels`, `--vulkanValidation`, быстрая игра |
| [Debug screen](https://minecraft.wiki/w/Debug_screen) | Minecraft Wiki, 2026 | Параметры F3+F6, горячие клавиши графиков, смысл записей, история версий |
| [Java Edition 26.2](https://minecraft.wiki/w/Java_Edition_26.2) | Minecraft Wiki, 2026 | Тайминги GPU Tracy, бэкенд Vulkan, исправления производительности (MC-307330, MC-295893) |
| [misode/mcmeta](https://github.com/misode/mcmeta) | misode | Ванильные ассеты и данные по версиям; ищите в `en_us.json` точные строки вывода команд |
| [Debug Profiler (NeoForge docs)](https://docs.neoforged.net/docs/misc/debugprofiler) | NeoForged, документация 26.1 | Чтение `profiling.txt`, `Profiler.get().push/pop`, счётчики |
| [NeoForge 1.21.2 primer: Profilers and the Tracy Client](https://github.com/neoforged/.github/blob/main/primers/1.21.2/index.md) | ChampionAsh5357, 2024 | `Profiler#get`, `Profiler#use`, зоны Tracy |
| [NeoForge 1.21.9 primer: The Debugging Overhaul](https://github.com/neoforged/.github/blob/main/primers/1.21.9/index.md) | ChampionAsh5357, 2025 | `-DMC_DEBUG_ENABLED`, подписки отладки, `DebugScreenEntry`, профили |
| [Custom Events in the Blocky World: Using JFR in Minecraft](https://foojay.io/today/custom-events-in-the-blocky-world-using-jfr-in-minecraft/) | Johannes Bechberger, 2023 | Разбор `/jfr`, приём с `jfr metadata` |
| [Minecraft JFR event metadata (gist)](https://gist.github.com/parttimenerd/a3b0c74eea0c1da89fec533ebd468479) | Johannes Bechberger, 2023 | Точные имена и поля событий |
| [jfr.event package, 1.21.11 (mappings.dev)](https://mappings.dev/1.21.11/net/minecraft/util/profiling/jfr/event/index.html) | mappings.dev | Классы событий в последней обфусцированной версии |
| [jfr.stats package, 1.21.11 (mappings.dev)](https://mappings.dev/1.21.11/net/minecraft/util/profiling/jfr/stats/index.html) | mappings.dev | Что агрегирует JSON-сводка |
| [MC-236873: JFR event improvements](https://bugs.mojang.com/browse/MC-236873) | Erik Gahlin, 2021 | Единицы `ServerTickTime` (проблема аннотации timespan в мс) |
| [Commands/test](https://minecraft.wiki/w/Commands/test) | Minecraft Wiki | Подкоманды `/test` для запуска gametest в игре |

---

## Флаги запуска и системные свойства

**Ключевые выводы**

- **Fabric Loader** (`SystemProperties.java`): `-Dfabric.development=true`, `-Dfabric.log.level=debug`, `-Dfabric.debug.disableModIds=a,b` (отключить моды без удаления jar: быстрая бисекция), `-Dfabric.debug.throwDirectly` (без агрегации подавленных исключений), `-Dfabric.debug.logClassLoad`, `-Dfabric.debug.logClassLoadErrors`, `-Dfabric.debug.logTransformErrors` (показывает проглоченные ошибки трансформеров), `-Dfabric.debug.disableModShuffle` (загрузчик перемешивает порядок модов, пока это не выключено, поэтому ошибки, зависящие от порядка, проявляются на разных машинах), `-Dfabric.debug.disableClassPathIsolation`, `-Dfabric.addMods=<paths>`, `-Dfabric.unitTest`, `-Dfabric.noGui`. Детали перемешивания `[не проверено]`.
- **Fabric API gametest**: `-Dfabric-api.gametest` (запустить сервер в режиме gametest), `-Dfabric-api.gametest.report-file=build/junit.xml`, `-Dfabric-api.gametest.filter=<pattern>`, `-Dfabric-api.gametest.verify`. Клиентские gametest: `-Dfabric.client.gametest`, `-Dfabric.client.gametest.disableNetworkSynchronizer=true` (обход для GitHub Actions).
- **NeoForge и ModDevGradle**: `neoforge.enableGameTest=true` (включён по умолчанию только в запусках `client` и `gameTestServer`); уровень консольного лога через `systemProperty 'forge.logging.console.level', 'debug'` или `logLevel = org.slf4j.event.Level.DEBUG` в запуске MDG; `jvmArgument("-XX:+AllowEnhancedClassRedefinition")` для hot reload на JBR.
- **Mixin** (вики SpongePowered): `-Dmixin.debug.export=true` (классы после миксинов в `run/.mixin.out`, с декомпиляцией при наличии Fernflower), `-Dmixin.debug.export.filter=<pattern>` (экспорт всего медленный), `-Dmixin.dumpTargetOnFailure=true` (дамп немиксиненной цели при сбое), `-Dmixin.debug.verbose=true`, `-Dmixin.debug.countInjections=true` (внедрения, ни с чем не совпавшие, становятся ошибками: ловит тихие поломки после обновления игры), `-Dmixin.debug.strict=true`, `-Dmixin.checks.interfaces=true` (находит нереализованные интерфейсы, которые дали бы `AbstractMethodError`), `-Dmixin.hotSwap=true` (вместе с `-javaagent`), `-Dmixin.debug=true` (всё сразу). `@Debug(export = true)` на одном классе миксина экспортирует только его цели.
- **Сторона JVM** (глубже — [performance.md](performance.md)): равные `-Xms` и `-Xmx`; JFR с запуска `-XX:StartFlightRecording=filename=rec.jfr,settings=profile,dumponexit=true`; логи GC `-Xlog:gc*:file=gc.log:time,uptime`; эффективные флаги — `jcmd <pid> VM.flags`. `-XX:+UseCompactObjectHeaders` (JEP 519, продуктовый флаг в JDK 25, по умолчанию выключен) уменьшает заголовки объектов. Стоит провести A/B-тест: Minecraft аллоцирует огромное число мелких объектов (позиции, состояния блоков, стеки предметов). Опубликованного бенчмарка для Minecraft не найдено `[не проверено]`. Флаги G1 от Aikar по-прежнему описаны в документации Paper, но старше поколенческого ZGC: перемеряйте на JDK 25, а не копируйте `[не проверено]`.
- **Сторожевой таймер сервера**: `max-tick-time` в `server.properties` (по умолчанию 60000 мс) останавливает сервер с дампом потоков в краш-репорте, если один тик завис. Читайте дамп «Server Watchdog», чтобы найти бесконечные циклы и взаимоблокировки. Ставьте `-1` только на время отладки с точками останова `[не проверено]`.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Fabric Loader SystemProperties.java](https://github.com/FabricMC/fabric-loader/blob/master/src/main/java/net/fabricmc/loader/impl/util/SystemProperties.java) | FabricMC | Авторитетный список отладочных свойств `fabric.*` |
| [fabric-gametest-api-v1](https://github.com/FabricMC/fabric-api/tree/26.3/fabric-gametest-api-v1) | FabricMC | `-Dfabric-api.gametest`, файл отчёта, структуры |
| [Mixin Java System Properties](https://github.com/SpongePowered/Mixin/wiki/Mixin-Java-System-Properties) | SpongePowered | Все переключатели `mixin.debug.*` и `mixin.checks.*` |
| [Exporting mixin classes (Fabric wiki)](https://wiki.fabricmc.net/tutorial:mixin_export) | Fabric Wiki | `.mixin.out`, `dumpTargetOnFailure`, `@Debug(export=true)` |
| [ModDevGradle README](https://github.com/neoforged/ModDevGradle) | NeoForged | Типы запусков, аргументы JVM, системные свойства, `unitTest` |
| [Aikar's flags (Paper docs)](https://docs.papermc.io/paper/aikars-flags) | PaperMC | Широко копируемая настройка G1; перепроверьте на JDK 25 |
| [Minecraft-Performance-Flags-Benchmarks](https://github.com/brucethemoose/Minecraft-Performance-Flags-Benchmarks) | brucethemoose | Бенчмарки флагов JVM от сообщества; пример методики (старые JDK) |
| [JEP 519: Compact Object Headers](https://openjdk.org/jeps/519) | OpenJDK, 2025 | Продуктовый флаг в JDK 25; кандидат на выигрыш памяти |
| [Server.properties](https://minecraft.wiki/w/Server.properties) | Minecraft Wiki | `max-tick-time`, параметры дальности прорисовки и симуляции |

---

## Бенчмарк мира: /tick sprint

**Ключевые выводы**

Команда `/tick sprint <ticks>` — готовый макробенчмарк пропускной способности серверной логики: она гонит тики без ожидания и печатает среднее время тика. Факты ниже проверены в CI на GitHub Actions: Paper 26.3 (сборка 244), JDK 25, пустой мир, принудительная загрузка чанков радиусом 64 блока, 400 тиков на один спринт.

- Сервер печатает строку `Sprint completed with X ticks per second, or Y ms per tick`. Метрика — Y, миллисекунды на тик (меньше — лучше).
- Спринты в одной JVM дали 0,47, 0,20, 0,18 мс/тик. Первые два спринта — прогрев JIT. Отбрасывайте 2 прогревочных спринта и оценивайте по остальным.
- **Не меряйте пустой мир ради чисел.** Пустой мир почти ничего не измеряет: в нём нет сущностей, блок-сущностей и контента мода, а сам тик занимает доли миллисекунды. Нужен фиксированный seed или сохранённый тестовый мир с реалистичной нагрузкой: сущности, блок-сущности, фермы, собственный контент мода (машины, ритуалы, структуры). Пустой мир годится как проверка работоспособности скрипта и как нулевая линия.
- Спринт измеряет только работу сервера (без клиентского рендера и сети). Это макробенчмарк: он ловит эффект оптимизации на реальных механиках и дополняет JMH на чистой логике ([JMH для логики мода](#jmh-для-логики-мода)).

**Скрипт `tick_sprint_bench.sh`** лежит в [`../scripts/tick_sprint_bench.sh`](../scripts/tick_sprint_bench.sh) (проверен в CI на GitHub Actions). Он запускает сервер, включает принудительную загрузку чанков вокруг 0,0, выполняет `tick sprint` нужное число раз и пишет результат в формате JMH JSON, который читает [`../scripts/BenchCompare.java`](../scripts/BenchCompare.java). Запуск из папки сервера, где лежат `eula.txt`, мир с фиксированным seed и ваши моды или плагины:

```bash
bash tick_sprint_bench.sh <server.jar | "команда запуска"> <out.json> [runs=5] [ticks=1200] [radius_blocks=64] [name=world] [warmup=2]
```

- Первый аргумент — jar сервера (ванильный, Paper) или полная команда запуска, если сервер стартует не из одного jar. Все три варианта проверены в CI на GitHub Actions (Minecraft 26.3, JDK 25) с модом-шаблоном из [minecraft-modding.md](minecraft-modding.md#шаблоны):

  | Сервер | Первый аргумент | Как подготовить папку | Спринты, мс/тик |
  | --- | --- | --- | --- |
  | Paper | `paper.jar` | jar с Fill API, плагины в `plugins/` | 0,15 0,13 0,12 |
  | Fabric | `"java -Xmx2G -jar fabric-server-launch.jar nogui"` | лаунчер с `https://meta.fabricmc.net/v2/versions/loader/<игра>/<loader>/<installer>/server/jar`, моды и Fabric API в `mods/`; ванильный сервер лаунчер скачает сам | 0,25 0,22 0,19 |
  | NeoForge | `"bash run.sh nogui"` | `java -jar neoforge-<версия>-installer.jar --installServer`, моды в `mods/`, память — в `user_jvm_args.txt` | 0,22 0,19 0,24 |

  Числа — пустой мир, они показывают только, что замер работает.
- `runs` — число замеряемых спринтов после прогрева; `ticks` — длина одного спринта; `radius_blocks` — радиус `forceload` вокруг 0,0, не больше 128: ванильный `/forceload` принимает за раз не больше 256 чанков, и при большем радиусе скрипт сразу завершается с кодом 2. Всё, что нужно замерить, должно стоять внутри этого квадрата; `name` — имя бенчмарка в JSON (`tick_sprint.<name>`); `warmup` — сколько первых спринтов отбросить.
- Один запуск скрипта — один запуск JVM (для jar — `-Xms2G -Xmx2G` и `nogui`, таймаут 1800 с). Оценка в JSON — медиана замеряемых спринтов; `scoreConfidence` содержит их минимум и максимум, `scoreError` равен `NaN`, единица `ms/tick`, режим `avgt`.
- Скрипт разбирает строку результата на языке `en_us`. Если сервер упал, вышел по таймауту или выполнил меньше спринтов, чем `warmup + runs`, скрипт печатает код выхода сервера, число выполненных спринтов и конец лога и завершается с кодом 1. JSON в этом случае не пишется. Процесс, который подаёт команды в консоль сервера, при этом тоже завершается и не остаётся висеть. Если убит сам скрипт, даже через `kill -9`, этот процесс отправляет серверу `stop`, и сервер останавливается штатно. Оба случая проверены на имитаторе сервера, а не на настоящем сервере.
- Шапка скрипта с описанием и примером вывода — первые 19 строк файла.

**Сравнение «до» и «после»: 5 запусков на сторону, чередование ABBA.** Один запуск сервера даёт одно число, а шум между запусками JVM (JIT, GC, соседи по CPU) сравним с эффектом небольших оптимизаций. Поэтому для каждой стороны делайте 5 запусков сервера, а порядок чередуйте (A B, B A, A B, …), чтобы дрейф машины не лёг на одну сторону. Цикл ниже не запускался в CI `[не проверено]`; сам скрипт проверен.

```bash
S=~/.claude/skills/java-knowledge/scripts      # папка scripts/ скилла; при установке в проект — .claude/skills/java-knowledge/scripts
LAUNCH=server.jar                               # или "bash run.sh nogui" (NeoForge), "java -Xmx2G -jar fabric-server-launch.jar nogui" (Fabric)
mkdir -p bench/before bench/after
# каждый запуск — с чистой копии мира: сервер сохраняет мир при stop, и без сброса следующий замер шёл бы на другом мире
run() {
  rm -rf "srv-$1/world" && cp -r world-template "srv-$1/world"
  ( cd "srv-$1" && bash "$S/tick_sprint_bench.sh" "$LAUNCH" "$OLDPWD/bench/$1/run$2.json" )
}
for i in 1 2 3 4 5; do
  if (( i % 2 )); then run before $i; run after $i; else run after $i; run before $i; fi
done
java "$S/BenchCompare.java" bench/before bench/after
```

Здесь `srv-before` и `srv-after` — две копии папки сервера с разными версиями мода, а `world-template` — эталонная копия мира (`level-name` в `server.properties`, по умолчанию `world`). Сервер сохраняет мир при остановке, а спринт сдвигает время, мобов и фермы, поэтому без сброса каждый следующий запуск измерял бы уже другой мир, и стороны разошлись бы по состоянию. Правила интерпретации те же, что в [performance.md](performance.md): сравнивайте медианы и разброс, принимайте изменение только если оно превышает шум между запусками.

**Тестовый мир.** Фиксированный seed плюс сохранённый мир как артефакт; внутри — реалистичная нагрузка и собственный контент мода (машины, структуры, мобы). Заморозьте то, что мешает повторяемости (смена дня, погода, спавн), если сценарий позволяет, и предгенерируйте область (Chunky), чтобы в спринт не попала генерация. Для проверки без мира-заготовки скрипту достаточно seed в `server.properties`, но тогда он измерит почти пустой мир.

**Пустая структура для GameTest.** Скрипт [`../scripts/EmptyStructure.java`](../scripts/EmptyStructure.java) создаёт пустую структуру `.nbt` (блоки воздуха заданного размера) для gametest, когда тесту не важна исходная постройка. Она нужна в NeoForge: у загрузчика нет своей пустой структуры. В Fabric API она есть — `fabric-gametest-api-v1:empty`. Запуск (JDK 17+, без сборки и зависимостей):

```bash
java EmptyStructure.java <out.nbt> [dataVersion=5023] [size=3]
```

Положите файл в `src/main/resources/data/<modid>/structure/<name>.nbt` и укажите `"structure": "<modid>:<name>"` в `test_instance/*.json`. `dataVersion` — `world_version` из `version.json` серверного jar нужной версии игры (26.3 — 5023). Подробнее о тестах — [Тесты: GameTest и юнит-тесты](#тесты-gametest-и-юнит-тесты).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Commands/tick](https://minecraft.wiki/w/Commands/tick) | Minecraft Wiki, 2026 | Драйвер макробенчмарка: sprint, freeze, step |
| [Chunky (GitHub)](https://github.com/pop4959/Chunky) | pop4959 | Pregeneration области перед спринтом |
| [Finding lag spikes](https://spark.lucko.me/docs/guides/Finding-lag-spikes) | lucko (документация spark) | Если спринт показал регрессию, найти кадр в профиле |

---

## JMH для логики мода

**Ключевые выводы**

- **Когда подходит JMH**: чистые алгоритмы, которыми владеет мод: поиск пути по сетке, решатели энергетических и жидкостных сетей, сопоставление рецептов, функции шума и плотности, кодеки и сериализация данных мода, пространственные индексы, поиск по инвентарю, раскладка текста. Измеряйте в JMH, затем подтверждайте эффект в игре через `/tick sprint` или spark (шаг «повторный замер» конвейера). Теория JMH, подводные камни и `BenchCompare.java` — в [performance.md](performance.md) `[не проверено]`.
- **Настройка Gradle**, которую используют настоящие моды: плагин `id "me.champeau.jmh" version "0.7.4"` рядом с Loom (`net.fabricmc.fabric-loom`) или ModDevGradle (в C2ME, Voxy, MicroRedstone встречались 0.7.2 и 0.7.3); исходники в `src/jmh/java`; `jmh { zip64 = true }`, потому что uber-jar JMH включает Minecraft; `includeTests = true` повторно использует фикстуры тестов; `includes = ['MyBench']` запускает один класс; `resultFormat = 'JSON'` для конвейера. Конфигурации прочитаны в файлах сборки C2ME, Voxy и MicroRedstone; точное поведение 0.7.4 `[не проверено]`.
- **Начальная загрузка реестров** нужна, как только код трогает ванильные реестры, состояния блоков, предметы или `ItemStack` (26.1: создание стеков «теперь требует загруженных реестров» — примечания к выпуску NeoForge 26.1). Шаблон из настоящего бенчмарка (WorldBytes `DensityFunctionBenchmarks`):

  ```java
  @State(Scope.Benchmark)
  public class NetworkSolverBench {
      static {
          synchronized (Bootstrap.class) {          // JMH может инициализировать состояние из нескольких потоков
              SharedConstants.tryDetectVersion();   // читает version.json из jar Minecraft; обязательно первым
              Bootstrap.bootStrap();                // строит и замораживает встроенные реестры (медленно, один раз на форк)
              Bootstrap.validate();
          }
      }
      @Benchmark public int solve(Blackhole bh) { ... }
  }
  ```

  В документации Fabric те же два вызова (`SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();`) стоят в `@BeforeAll` JUnit.
- **Подводные камни** (`[не проверено]`, если не указано иное):
  - Форки JMH работают на обычном classpath: **нет загрузчика классов Fabric Knot или FML, поэтому не применяются миксины, access wideners и transformers, не работают реестры модов**. Бенчмаркайте код, который не зависит от ванильных классов, изменённых миксинами; иначе мерьте в игре (зоны Tracy через `Profiler.get().push`, spark) или внутри gametest. `fabric-loader-junit` существует именно ради Knot в JUnit; эквивалентного запускателя JMH не найдено.
  - `Bootstrap.bootStrap()` регистрирует только встроенные реестры. Контент на данных (биомы, типы измерений, рецепты, зачарования, экземпляры тестов) требует `RegistryAccess` из загруженных датапаков: соберите минимальный объект-фикстуру вместо загрузки миров в бенчмарке.
  - Инициализация классов `Blocks` и `Items` неявно запускает bootstrap; без `tryDetectVersion()` в таком случае будет ошибка о ненастроенной версии игры.
  - Используйте `@Param` для размеров (сеть из 10, 1 000, 100 000 узлов): горячие циклы в модах обычно чувствительны к размеру.
  - В Minecraft аллокации важны не меньше времени: добавьте `-prof gc` и сообщайте `gc.alloc.rate.norm` (байт на операцию); уменьшение байт на операцию на пути «каждый тик» часто и есть настоящий выигрыш (см. `mixin.alloc.*` в Lithium). Флаг JMH проверен; совет `[не проверено]`.
  - Сборка C2ME показывает `profilers = ['perf', 'perfasm']` для глубокого разбора на Linux, `timeUnit = 'ns'`, `fork = 1`.
- **Где JMH не подходит**: всё, что зависит от кэшей, ввода-вывода и взаимодействия потоков с живым сервером (загрузка чанков, отслеживание сущностей, сброс пакетов). Там используйте макробенчмарки на основе gametest или `/tick sprint` `[не проверено]`.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [JMH](https://github.com/openjdk/jmh) | OpenJDK | Харнесс, примеры, профилировщики (`-prof gc`) |
| [jmh-gradle-plugin](https://github.com/melix/jmh-gradle-plugin) | Cédric Champeau | Опции `me.champeau.jmh` (`includes`, `zip64`, `resultFormat`) |
| [WorldBytes DensityFunctionBenchmarks](https://github.com/eigenraven/WorldBytes/blob/dev/src/jmh/java/me/eigenraven/mc/worldbytes/jmh/DensityFunctionBenchmarks.java) | eigenraven | Настоящий шаблон bootstrap для JMH в Minecraft (функции плотности генерации мира) |
| [C2ME build.gradle (jmh block)](https://github.com/RelativityMC/C2ME-fabric/blob/dev/26.3.0/build.gradle) | RelativityMC | Конфигурация JMH рядом с Loom; профилировщики perf и perfasm |
| [MicroRedstone build.gradle](https://github.com/XFactHD/MicroRedstone) | XFactHD | JMH с ModDevGradle на 26.3 (`includeTests`, `zip64`) |
| [Voxy](https://github.com/MCRcortex/voxy) | MCRcortex | JMH рядом с Loom для бенчмарков сохранения и загрузки LOD |
| [Automated Testing (Fabric docs)](https://docs.fabricmc.net/develop/automatic-testing) | Fabric docs, 26.2 | `SharedConstants.tryDetectVersion()` и `Bootstrap.bootStrap()` |
| [NeoForge 26.1 release notes](https://neoforged.net/news/26.1release/) | NeoForged, 2026 | Создание ItemStack требует загруженных реестров; `ItemStackTemplate` |

---

## Профилирование: spark, JFR и другие

### spark: профилировщик в игре по умолчанию

**Ключевые выводы**

- **Поставка**: jar spark 1.10.187 (2026-09-19) для Fabric, NeoForge и Forge покрывают MC 26.1–26.3. Paper встраивает spark с 1.21 (более новый отдельный плагин используется при `paper.preferSparkPlugin=true`). На клиенте команда — `/sparkc` (прокси: `/sparkb` BungeeCord, `/sparkv` Velocity). Отдельный Java-агент профилирует любую JVM (полезно для сервера gametest или форка JMH).
- **Движки**: async-profiler используется автоматически на Linux и macOS (x86_64, aarch64); в остальных случаях (Windows) spark переходит на свой Java-сэмплер (`--force-java-sampler` включает его принудительно). Режим async видит нативные кадры и потоки GC и JIT; Java-сэмплер берёт дампы потоков Java, которые смещены к safepoint `[не проверено]`. На Windows подтверждайте горячие точки через JFR. Профиль аллокаций требует отладочных символов HotSpot (поставляются с Java 11); в контейнерах нужен `--cap-add SYS_ADMIN`, иначе async-profiler откатывается на `itimer`.
- **Фоновый профилировщик**: включён по умолчанию (`backgroundProfiler=true`, движок `async`, интервал 10 мс) с запуска, поэтому `/spark profiler open` может выгрузить данные о лаге, который уже случился. В бенчмарках отключайте его (конфиг `backgroundProfiler=false`), чтобы не измерять сам профилировщик. Частично `[не проверено]`.
- **Основные команды** (точные флаги): `/spark profiler start [--timeout <s>] [--thread *|<name> [--regex]] [--interval <ms>] [--only-ticks-over <ms>] [--alloc [--alloc-live-only] [--interval <bytes>]] [--combine-all] [--not-combined] [--ignore-sleeping] [--force-java-sampler]`, `/spark profiler stop [--comment <c>] [--save-to-file]`, `/spark tps`, `/spark health [--memory|--network]`, `/spark tickmonitor [--threshold <%>|--threshold-tick <ms>] [--without-gc]`, `/spark gc`, `/spark gcmonitor`, `/spark heapsummary`, `/spark heapdump [--compress gzip|xz|lzma]`, `/spark ping [--player <name>]`.
- **Рецепты**:
  - Постоянный лаг: `/spark profiler start --timeout 300` при работающем сценарии, затем смотрите «Server thread»: доля `waitForNextTick` (сон) и крупнейшие дочерние узлы под тиком.
  - Всплески: `/spark tickmonitor --threshold-tick 50`, затем `/spark profiler start --only-ticks-over <значение ниже длительности всплеска>`; сопоставьте всплески с действиями игрока (открытие GUI, пересечение границы чанка, запуск ритуала).
  - Мусор: `/spark profiler start --alloc --timeout 60` даёт flame graph аллокаций по местам вызова; затем `/spark heapsummary` покажет число живых объектов по классам (утечки: растущие счётчики классов мода, `ChunkAccess`, `ServerPlayer`, `Level` после выгрузки мира). Частично `[не проверено]`.
  - `--save-to-file` оставляет локальный `.sparkprofile`, когда выгрузка не нужна (артефакты CI, закрытые серверы).
- **Просмотрщик**: дерево «All» (сверху вниз), «Flat» (250 самых медленных вызовов, режим снизу вверх), вид **«Sources»** делит профиль по плагинам и модам: самый быстрый способ отнести время к моду. Селектор мэппингов деобфусцирует стеки 1.21.11 и старше; стеки 26.x уже читаемы.
- **API разработчика**: `compileOnly 'me.lucko:spark-api:0.1-SNAPSHOT'` (репозиторий `https://repo.lucko.me/`), `SparkProvider.get()` затем `spark.mspt().poll(StatisticWindow.MillisPerTick.MINUTES_1).percentile95th()`, `spark.tps()`, `spark.gc()`. Это позволяет моду подстраивать работу (например, дросселировать фоновый скан) под нагрузку сервера, а тестовой обвязке — читать MSPT.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [spark (GitHub)](https://github.com/lucko/spark) | lucko | Исходники, платформы, релизы |
| [spark (Modrinth)](https://modrinth.com/mod/spark) | lucko | Матрица версий по загрузчикам |
| [Command Usage](https://spark.lucko.me/docs/Command-Usage) | lucko | Все команды и флаги |
| [Configuration](https://spark.lucko.me/docs/Configuration) | lucko | Фоновый профилировщик, движок, URL выгрузки |
| [Using the viewer](https://spark.lucko.me/docs/Using-the-viewer) | lucko | Виды All, Flat, Sources, мэппинги |
| [Using async-profiler](https://spark.lucko.me/docs/misc/Using-async-profiler) | lucko | Поддержка платформ, требования режима alloc, контейнеры |
| [Standalone Agent](https://spark.lucko.me/docs/Standalone-Agent) | lucko | Профилирование любой JVM (сервер gametest, утилиты) |
| [spark vs others](https://spark.lucko.me/docs/misc/spark-vs-others) | lucko | Сравнение с Timings и другими профилировщиками |
| [Developer API](https://spark.lucko.me/docs/Developer-API) | lucko | Чтение TPS, MSPT, GC из кода |
| [spark-mappings](https://github.com/lucko/spark-mappings) | lucko | Файлы мэппингов просмотрщика для обфусцированных версий |
| [Paper: Profiling](https://docs.papermc.io/paper/profiling/) | PaperMC | spark как предпочтительный профилировщик, `--timeout 600`, `preferSparkPlugin` |

### JFR и JMC, async-profiler, VisualVM, Observable, Paper Timings

**Ключевые выводы**

- **JFR на сервере Minecraft**: либо `/jfr start|stop` (ваниль, включает пользовательские события MC), либо на уровне JVM `jcmd <pid> JFR.start name=mc settings=profile duration=120s filename=mc.jfr` или `-XX:StartFlightRecording`. Откройте в JDK Mission Control (JMC): Method Profiling (горячие методы), Memory (аллокации TLAB и вне TLAB по классам и стекам), Threads (конкуренция за блокировки между серверным потоком и рабочими потоками генерации и ввода-вывода), Event Browser для событий `minecraft.*` (фильтруйте `ChunkGeneration` по `status`, чтобы увидеть доминирующую стадию генерации). Быстрая оценка из командной строки: `jfr print --events minecraft.ServerTickTime mc.jfr`, `jfr summary mc.jfr`, `jfr view hot-methods mc.jfr` (`jfr view` с JDK 21). Частично `[не проверено]`.
- **Улучшения JFR в JDK 25** напрямую применимы к серверам MC: JEP 518 (кооперативная выборка: безопаснее, ниже накладные расходы), JEP 520 (замер времени и трассировка методов: `jdk.MethodTiming` и `jdk.MethodTrace` с фильтром методов — точно замерить один метод мода без агента-профилировщика), JEP 509 (профилирование по времени CPU; экспериментально, только Linux). Точный синтаксис фильтра — в JEP 520.
- **async-profiler 4.6** напрямую (Linux, macOS): `asprof -e cpu -d 60 -f cpu.html <pid>`, `-e alloc`, `-e lock`, `-e wall` для заблокированных потоков; вывод JFR (`-f out.jfr`) открывается в JMC и IntelliJ. spark оборачивает тот же движок.
- **VisualVM 2.2.2**: живой сэмплер и просмотр дампов кучи; подходит для быстрой охоты за утечками на dev-клиенте (удерживаемый размер `ClientLevel` после выхода из мира). Файлы `.jfr` и `.hprof` от `/jfr` и spark тоже открываются в нём.
- **Профилировщик IntelliJ IDEA** (Ultimate): async-profiler или JFR в один клик на конфигурации запуска Loom или MDG — самый простой вариант при разработке.
- **Observable** (преемник LagGoggles): время тика по сущностям и блок-сущностям поверх мира; последние сборки на Modrinth для 1.21.1 и 1.21.4 (2025). Для 26.x используйте вид Sources в spark и CSV `levels/` из ванильного `/perf` (число блок-сущностей и сущностей по чанкам).
- **Paper Timings**: выключены по умолчанию в 1.21 и сделаны пустышкой в Paper 1.21.3 («больше нельзя включить или использовать»); классы API остаются ради двоичной совместимости. Плагины должны убрать свои вызовы Timings и использовать spark. Руководства с `/timings paste` — `[устарело]`, замена `/spark profiler`.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [JDK Mission Control](https://www.oracle.com/java/technologies/jdk-mission-control.html) | Oracle | Загрузка и документация JMC |
| [JEP 518: JFR Cooperative Sampling](https://openjdk.org/jeps/518) | OpenJDK, 2025 | Выборка с меньшими накладными расходами в JDK 25 |
| [JEP 520: JFR Method Timing & Tracing](https://openjdk.org/jeps/520) | OpenJDK, 2025 | Точный замер выбранных методов (метод тика мода) |
| [JEP 509: JFR CPU-Time Profiling (Experimental)](https://openjdk.org/jeps/509) | OpenJDK, 2025 | Сэмплер по времени CPU на Linux |
| [async-profiler](https://github.com/async-profiler/async-profiler) | async-profiler team | Профилирование CPU, аллокаций, блокировок, wall, flame graph |
| [VisualVM](https://visualvm.github.io/) | VisualVM team | Сэмплер, дампы кучи |
| [IntelliJ IDEA profiler](https://www.jetbrains.com/help/idea/profiler-intro.html) | JetBrains | Запуски с профилированием из IDE |
| [Observable (GitHub)](https://github.com/tasgon/observable) | tasgon | Наложение времени тика по сущностям; статус проекта |
| [Observable (Modrinth)](https://modrinth.com/mod/observable) | tasgon | Поддерживаемые версии (до 1.21.4) |
| [Paper 1.21 announcement](https://papermc.io/news/1-21) | PaperMC, 2024 | spark встроен, Timings выключены по умолчанию |
| [Paper 1.21.3 announcement](https://papermc.io/news/1-21-3) | PaperMC, 2024 | Timings стали пустышкой, уберите свои вызовы Timings |
| [Paper 26.1 announcement](https://papermc.io/news/26-1) | PaperMC, 2026 | Односторонний апгрейд мира, устаревания API, за которыми стоит следить |

### Профилирование клиентского рендеринга

**Ключевые выводы**

- **Начинайте со времени кадра, а не FPS**: график времени кадра F3+2; Tracy с `--tracy` показывает зоны CPU по кадрам (ваниль экспортирует секции профилировщика как зоны Tracy с 1.21.2), а с 26.2 и тайминги GPU через `TracyGpuProfiler`. Захватывайте через GUI Tracy или `tracy-capture`; у `ClientProductionRunTask` из Fabric Loom есть блок `tracy { tracyCapture = file(...); output = file("profile.tracy"); maxShutdownWaitSeconds = 10 }` для автоматического захвата в CI.
- **Сначала решите, что упирается: CPU или GPU.** README ImmediatelyFast отмечает, что выигрыш виден только при упоре в CPU. Если снижение разрешения или масштаба рендера не меняет время кадра, узкое место — CPU (работа потока рендера, число draw call, загрузка буферов, логика рендереров сущностей и блок-сущностей). Частично `[не проверено]`.
- **Графические отладчики**: RenderDoc (захват кадра, draw call, буферы) и NVIDIA Nsight Graphics. Подключайтесь через лаунчер с «Capture Child Processes» (руководство ShaderLabs) или модом `renderdoc-support` и «Attach to Running Instance». Запускайте с `--renderDebugLabels`, чтобы проходы и буферы имели имена; на бэкенде Vulkan 26.2+ добавьте `--vulkanValidation`, чтобы видеть ошибки API.
- **Изменения рендеринга в 26.x влияют на профилирование**: 26.2 добавил экспериментальный бэкенд Vulkan (опция «Graphics API»; OpenGL планируют убрать, когда Vulkan стабилизируется) и обратный буфер глубины; 26.3 заменил GLFW на SDL. Сырые вызовы OpenGL в модах нужно перевести на Blaze3D (посты Fabric 26.1 и 26.2); Sodium 0.9 перевёл весь рендеринг на Blaze3D. Процесс профилирования только на GL (например, GL debug output) больше не покрывает всех пользователей.
- **Гигиена потока рендера при профилировании**: никакого сканирования мира и блок-сущностей в путях `render` и `submit`; применяйте шаблон извлечения состояния рендера (render states у сущностей и блок-сущностей, `extractRenderState` у GUI в 26.1), чтобы тяжёлые вычисления шли один раз за кадр или за тик, а не на каждый draw. Переименование `Screen#extractRenderState` в 26.1 проверено; сам совет `[не проверено]`.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Tracy](https://github.com/wolfpld/tracy) | Bartosz Taudul | Кадровый профилировщик, который использует ванильный `--tracy` |
| [Production Run Tasks (Loom)](https://docs.fabricmc.net/develop/loom/production-run-tasks) | Fabric docs | `ClientProductionRunTask`, `useXVFB`, захват через `tracy {}` |
| [RenderDoc](https://renderdoc.org/) | Baldur Karlsson | Отладчик кадров (OpenGL, Vulkan) |
| [NVIDIA Nsight Graphics](https://developer.nvidia.com/nsight-graphics) | NVIDIA | Профилирование кадров GPU, GPU trace |
| [Attaching Graphics Debuggers to Minecraft](https://shaderlabs.org/wiki/Attaching_Graphics_Debuggers_to_Minecraft) | ShaderLabs wiki | Подключение через лаунчер и дочерние процессы, чтение проходов Iris |
| [renderdoc-support](https://github.com/latvian-dev/renderdoc-support) | latvian-dev | Мод для подключения RenderDoc во время работы |
| [Fabric for Minecraft 26.2](https://fabricmc.net/2026/06/15/262.html) | FabricMC, 2026 | Бэкенд Vulkan, требование миграции на Blaze3D |
| [Fabric for Minecraft 26.3](https://fabricmc.net/2026/09/15/263.html) | FabricMC, 2026 | SDL вместо GLFW, изменения фокуса ввода текста |
| [ImmediatelyFast README](https://github.com/RaphiMC/ImmediatelyFast) | RaphiMC | Оговорка про упор в CPU, метод измерения (MSI Afterburner) |
| [NeoForge 26.2 primer](https://github.com/neoforged/.github/blob/main/primers/26.2/index.md) | ChampionAsh5357, 2026 | `TracyGpuProfiler`, изменения API статистики JFR, билдеры gametest |

---

## Уроки модов оптимизации

**Ключевые выводы**

Статус проверен через API Modrinth и GitHub 2026-11-07. Колонка «Техника» — переносимый урок для автора мода.

| Мод | Область | Статус в ноябре 2026 | Какую технику показывает |
|---|---|---|---|
| Sodium | Клиентский рендеринг | Активен; 0.9.3-beta для 26.3 (Fabric и NeoForge), 0.8.15 для 1.21.11 | Построение мешей чанков вне главного потока, объединение проходов рендера, общие индексные буферы (до 25 % меньше пропускной способности VRAM), компактный формат вершин, culling по графу видимости (асинхронный с 0.9.0), multidraw; 0.9 перестроен на Blaze3D с экспериментальным Vulkan |
| Lithium | Сервер, игровая логика | Активен; 0.26.3 для 26.3 (Fabric и NeoForge) | Устранение аллокаций (`mixin.alloc.*`: без копий `Enum#values()`, меньше `BlockPos` в случайных тиках), коллекции fastutil, **засыпание** блок-сущностей (не тикают, пока не разбудит изменение инвентаря или состояния), кэшированный доступ к блокам для столкновений, упакованные запланированные тики, специализированные VoxelShape, ленивые диспетчеры игровых событий |
| FerriteCore | Память | Активен; 9.0.0 для 26.1–26.3 | Раскладка данных и дедупликация: `FastMap` для таблиц соседей состояний блоков (около 600 МБ экономии в большой сборке), общее хранилище свойств, кэш предикатов multipart, дедупликация квадов и строк; каждое изменение измерено (МБ и влияние на CPU) |
| ModernFix | Старт, память | На 26.x только NeoForge (26.1.2); Fabric остановился на 1.21.1; сборок 26.2 и 26.3 нет; форк ModernFix-mVUS | Ленивая и динамическая загрузка моделей («dynamic resources»), исправления запуска, исправления падений из-за конкурентности, диагностика зависаний |
| C2ME | Генерация чанков, ввод-вывод | Активен; только Fabric; 26.3 и снапшоты 26.4 | Параллельная генерация мира и ввод-вывод чанков; санитайзеры неопределённого поведения (`CheckedThreadLocalRandom`), которые ловят моды, использующие случайность мира не из того потока; несовместим с Moonrise |
| Moonrise | Сервер (система чанков, сущности, свет) | Активен; 26.3 текущая, 26.1.2 LTS, 26.4 alpha (Fabric и NeoForge) | Полная замена системы чанков из Paper, освещение Starlight, быстрее получение сущностей и блоков для ИИ и поиска пути, меньше рабочих потоков на слабых CPU, меньше догоняния TPS |
| Starlight | Освещение | Заархивирован (последний 1.20.4); ваниль 1.20 приняла его основные идеи. `[устарело]` — замена: Moonrise или ScalableLux | Распространение *уровней* света очередями увеличения и уменьшения, отдельная логика небесного света, движок без состояния, допускающий параллелизм |
| ScalableLux | Освещение | Активен; 0.3.0-alpha для 26.3 (Fabric и NeoForge) | Форк Starlight с необязательными параллельными обновлениями света для быстрой генерации мира |
| Krypton | Сеть | Активен; 0.3.2 для 26.3 (только Fabric) | Работа с конвейером Netty по образцу Velocity и Paper: обработчики сжатия и шифрования, микрооптимизации varint и UTF-8, декодер кадров, доработки трекера сущностей; сохраняет совместимость протокола (список по файлам миксинов) |
| VMP (Very Many Players) | Сервер при большом числе игроков | Активен; Fabric 26.3 | Карты областей для поиска ближайших игроков и рассылки пакетов, кэши трекеров сущностей и поиска жидкостей |
| EntityCulling | Клиент | Активен; 1.11.3 (Fabric и NeoForge, 26.1.x–26.3) | Асинхронная трассировка лучей и путей на свободных ядрах, чтобы не рисовать скрытые сущности и блок-сущности; данные собираются в главном потоке раз в несколько тиков |
| ImmediatelyFast | Клиентский immediate-mode рендеринг | Активен; 1.17.1 для 26.3 | Батчинг draw call через собственный буфер; точечные переписывания для карт, HUD и текста; выигрыш только при упоре в CPU |
| MoreCulling | Клиент | Активен (Fabric и NeoForge 26.x) | Culling граней с учётом состояния блока (листва, рамки и т. д.) |
| Noisium | Генерация мира | Заархивирован 2025-07, unlisted; последний 1.21.6. `[устарело]` — замена: C2ME | Обход покадровых абстракций: запись состояний блоков прямо в хранилище палитры при заполнении шумом; паритет 1:1 с ванилью; профили spark как доказательства |
| Alternate Current | Редстоун | Активен (Fabric, NeoForge, Forge) | Замена алгоритма: детерминированные, не локационные обновления проводов; до 30 раз ниже MSPT редстоун-пыли |
| ServerCore | Сервер | Активен; 1.5.20 для 26.3 | Динамические дальность прорисовки и симуляции и лимиты мобов по MSPT; радиус активации; «лоботомия жителей» (компромиссы геймплея, настраивается) |
| Clumps | Сервер | Активен; 26.1–26.3 | Уменьшение числа сущностей слиянием сфер опыта |
| Dynamic FPS | Клиент | Активен | Снижение частоты рендера при потере фокуса, в простое, на батарее |
| Nvidium | Клиент | Активен; 26.1–26.3 (Fabric) | Бэкенд для Sodium, привязанный к вендору (mesh shaders NVIDIA) |
| BadOptimizations | Клиент | Активен | Микрооптимизации: пропуск обновления lightmap без изменений, прочее кэширование клиентского тика |
| LazyDFU | Старт | Не поддерживается; устарел с 1.19.4. `[устарело]` — замена: ваниль | Ленивая инициализация правил DataFixerUpper — Mojang приняла ленивый DFU (сервер с 1.19, гораздо быстрее инициализация в 1.19.4) |
| Memory Leak Fix | Память | Последнее обновление 2024-01. `[устарело]` для 26.x | Исправление утечек удержания (старые уровни и сущности в статических кэшах) |

**Выводы для «библиотеки решений»** (сведены из таблицы выше `[не проверено]`):

1. Сначала уберите аллокации в горячих путях, потом настраивайте арифметику (аллокационные миксины Lithium; у Sodium на 80 % меньше аллокаций при построении мешей).
2. Не тикайте то, у чего нет работы (засыпание блок-сущностей Lithium, радиус активации ServerCore, Clumps).
3. Кэшируйте поиски и инвалидируйте по событиям (кэш инвентаря воронок Lithium, `BlockCapabilityCache` в NeoForge).
4. Выбирайте раскладку данных, которая уменьшает живой набор (FerriteCore, прямая запись в палитру в Noisium).
5. Выносите чистую работу из главного потока и потока рендера только со строгими правилами владения (санитайзеры C2ME, снимки данных EntityCulling, построение мешей в Sodium).
6. Заменяйте алгоритм, если ванильный патологичен (Alternate Current, Starlight).
7. Доказывайте каждое изменение профилем или бенчмарком и заявляйте паритет с ванилью (профили spark у Noisium, таблица МБ у FerriteCore, таблицы FPS у ImmediatelyFast).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Sodium](https://github.com/CaffeineMC/sodium) | CaffeineMC | Движок рендеринга; требования к оборудованию (официально OpenGL 4.5) |
| [Sodium 0.5.0 changelog](https://modrinth.com/mod/sodium/version/mc1.20.1-0.5.0) | CaffeineMC, 2023 | Количественные приёмы рендеринга (объединение проходов, индексные буферы, аллокации при построении мешей) |
| [Sodium (Modrinth)](https://modrinth.com/mod/sodium) | CaffeineMC | Списки изменений 0.9.x: Blaze3D, Vulkan, асинхронный culling |
| [Lithium](https://github.com/CaffeineMC/lithium) | CaffeineMC / 2No2Name | Обзор проекта, философия конфигурации |
| [Lithium Fabric mixin config](https://github.com/CaffeineMC/lithium/blob/develop/lithium-fabric-mixin-config.md) | CaffeineMC | Одна строка на каждую оптимизацию: каталог приёмов |
| [FerriteCore](https://github.com/malte0811/FerriteCore) | malte0811 | Мод оптимизации памяти |
| [FerriteCore summary.md](https://github.com/malte0811/FerriteCore/blob/26.1/summary.md) | malte0811 | Каждая оптимизация с МБ экономии и влиянием на CPU |
| [ModernFix](https://github.com/embeddedt/ModernFix) | embeddedt | Мод старта и памяти, профилирован YourKit |
| [ModernFix wiki](https://github.com/embeddedt/ModernFix/wiki) | embeddedt | Сводки патчей, FAQ по динамическим ресурсам |
| [Dynamic Resources FAQ](https://github.com/embeddedt/ModernFix/wiki/Dynamic-Resources-FAQ) | embeddedt | Компромиссы ленивой запечки моделей |
| [ModernFix (Modrinth)](https://modrinth.com/mod/modernfix) | embeddedt | Матрица версий (на 26.x только NeoForge) |
| [C2ME](https://github.com/RelativityMC/C2ME-fabric) | RelativityMC / ishland | Параллельный движок чанков, санитайзеры, политика поддержки |
| [Moonrise](https://github.com/Tuinity/Moonrise) | Tuinity (Spottedleaf) | Система чанков Paper для модов; таблица совместимости |
| [Starlight](https://github.com/PaperMC/Starlight) | Spottedleaf | Заархивированная переписанная система освещения |
| [Starlight 1.20 and the future (gist)](https://gist.github.com/Spottedleaf/6cc1acdd03a9b7ac34699bf5e8f1b85c) | Spottedleaf, 2023 | Что приняла ваниль; почему это важно для серверов |
| [ScalableLux](https://github.com/RelativityMC/ScalableLux) | RelativityMC | Форк с параллельными обновлениями света |
| [Krypton](https://github.com/astei/krypton) | Andrew Steinborn | Оптимизация сетевого стека (наследие Velocity) |
| [Velocity](https://github.com/PaperMC/Velocity) | PaperMC | Источник идей нативного конвейера сжатия и шифрования |
| [VMP-fabric](https://github.com/RelativityMC/VMP-fabric) | RelativityMC | Приёмы масштабирования по числу игроков |
| [EntityCulling](https://github.com/tr7zw/EntityCulling) | tr7zw | Асинхронный culling видимости |
| [ImmediatelyFast](https://github.com/RaphiMC/ImmediatelyFast) | RaphiMC | Батчинг draw call, измеренные таблицы |
| [MoreCulling](https://github.com/fxmorin/moreculling) | FxMorin | Приёмы culling |
| [Noisium](https://github.com/Steveplays28/noisium) | Steveplays28 | Прямая запись в палитру при генерации мира (архив) |
| [Alternate Current](https://github.com/SpaceWalkerRS/alternate-current) | Space Walker | Алгоритмическая переписка редстоуна, подробный README |
| [ServerCore](https://github.com/Wesley1808/ServerCore) | Wesley1808 | Динамические дальности, радиус активации |
| [Clumps](https://github.com/jaredlll08/Clumps) | Jaredlll08 | Уменьшение числа сущностей |
| [Dynamic FPS](https://codeberg.org/LostLuma/Dynamic-FPS) | LostLuma | Снижение частоты в простое |
| [Nvidium](https://github.com/MCRcortex/nvidium) | MCRcortex | Бэкенд на mesh shaders |
| [BadOptimizations](https://github.com/ItsThosea/BadOpitmizations) | ItsThosea | Микрооптимизации клиента |
| [LazyDFU](https://github.com/astei/lazydfu) | Andrew Steinborn | Ленивый DFU; пометка об устаревании для 1.19.4+ |
| [Memory Leak Fix](https://github.com/fxmorin/memoryLeakFix) | FxMorin | Шаблоны утечек (старые версии) |

---

## Типичные проблемы производительности в модах

**Ключевые выводы**

Таблица «симптом → техника → источник» в начале файла собирает эти проблемы по признакам в профиле. Ниже — описание причин и ловушек.

- **Аллокации в каждом тике**: `new BlockPos` на каждого соседа, копии `Direction.values()` и `Enum#values()`, stream и захватывающие лямбды в коде тика, боксинг в `Map<BlockPos, Integer>`. Исправление: `BlockPos.MutableBlockPos`, кэшированные массивы `static final`, обычные циклы, fastutil (`Long2ObjectOpenHashMap` с ключом `BlockPos.asLong()`). Lithium поставляет каждый из этих приёмов отдельным переключателем `mixin.alloc.*` или `mixin.collections.*`, и это свидетельство того, что в масштабе они важны. Ловушка: `BlockPos.betweenClosed(...)` отдаёт *переиспользуемый изменяемый* экземпляр: перед сохранением копируйте через `.immutable()`.
- **Блок-сущности, тикающие всегда**: `getTicker` вызывается каждый тик для каждого загруженного экземпляра. Документация NeoForge советует возвращать разные тикеры по сторонам (или `null`, где ничего не нужно), работать раз в N тиков или кэшировать, а при малом конечном числе состояний использовать свойства состояния блока вместо блок-сущности. Засыпание блок-сущностей в Lithium показывает более сильный шаблон: не тикать до события (изменение инвентаря, обновление соседа), которое будит блок-сущность.
- **Сканирование мира или сущностей каждый тик**: циклы по радиусу с `getBlockState`, `level.getEntitiesOfClass(type, hugeAabb)` каждый тик, поиск «ближайшего X». Исправления: амортизация (обрабатывать K позиций за тик от курсора), реакция на события (изменения соседей, `onPlace` и `onRemove`), ванильные POI для «найти ближайший блок типа», собственный реестр машин и узлов мода, меньшие AABB. Для сравнения: Lithium `mixin.ai.poi`, `entity.collisions.*`, `chunk.entity_class_groups`.
- **Случайная загрузка чанков**: `level.getBlockState(pos)` и `getBlockEntity(pos)` для незагруженной позиции могут загрузить чанк (а для некоторых API — сгенерировать). Охраняйте проверкой `level.hasChunkAt(pos)` или `isLoaded(pos)`; документация NeoForge требует эту проверку в пользовательских обработчиках пакетов, которые трогают блок-сущности. Детали «загрузка или генерация» `[не проверено]`.
- **Загрузка чанков модами**: принудительные тикеты держат чанки *симулируемыми* (сущности, случайные тики), если тип тикета не говорит иного. С 1.21.5 тикеты используют зарегистрированный `TicketType(timeoutTicks, persist, TicketUse.LOADING | LOADING_AND_SIMULATION)`: берите `LOADING`, когда нужен только доступ к блокам, задавайте сроку жизни тикета и держите радиус минимальным. Совет `[не проверено]`; primer проверен.
- **Спам пакетами синхронизации**: отправка полного NBT блок-сущности через `sendBlockUpdated` или пакет каждый тик ради индикаторов прогресса и значений энергии. Исправление: отправка при изменении (флаг «грязно» и ограничение частоты), дельты, синхронизация меню через слоты данных, пока меню открыто, адресация только игрокам, отслеживающим чанк (`PacketDistributor.sendToPlayersTrackingChunk`). Вложения данных NeoForge пересылают всё вложение при каждом `syncData` с перегрузками stream codec; пишите только изменённые поля при `initialSync == false`. Лимиты пакетов: к клиенту до 1 МиБ, к серверу менее 32 КиБ.
- **Тяжёлая работа в обработчиках пакетов**: обработчики по умолчанию выполняются в главном потоке. Вынесите разбор и распаковку в `HandlerThread.NETWORK` и возвращайтесь в главный поток через `context.enqueueWork(...)`; добавляйте `.exceptionally`, иначе исключения глотаются молча.
- **Тяжёлая работа в потоке рендера**: рендереры блок-сущностей и сущностей, которые опрашивают мир, обходят инвентари или строят меши на каждом кадре. Считайте в тике (или при изменении) и передавайте снимок через шаг извлечения состояния рендера; запекайте статичную геометрию в модель блока, чтобы построение чанков Sodium или ванили сделало её один раз; задавайте точные ограничивающие объёмы рендера, чтобы моды culling могли пропускать скрытое. `[не проверено]`.
- **Запечка клиентских моделей и старт**: комбинаторные свойства состояний блока умножают число запечённых моделей и память (главные выигрыши FerriteCore — таблицы соседей состояний и multipart-модели); «dynamic resources» ModernFix показывают компромиссы ленивой запечки. Держите число свойств малым; редко меняющиеся визуальные детали переносите в компоненты данных или рендеринг блок-сущности. Частично `[не проверено]`.
- **Рефлексия в горячих путях**: `Field#get` и `Method#invoke` каждый тик. Используйте Mixin `@Accessor` и `@Invoker`, access wideners (Fabric) и access transformers (NeoForge) или `static final MethodHandle` и `VarHandle`. `[не проверено]`.
- **Накладные расходы и конфликты Mixin**: внедрение в очень горячие методы (тик сущности, `getBlockState`) добавляет вызов и, возможно, аллокацию `CallbackInfo` на каждый вызов; `@Redirect` и `@Overwrite` конфликтуют с другими модами. Предпочитайте MixinExtras (`@WrapOperation`, `@ModifyExpressionValue`), держите обработчики крошечными, смотрите объединённый байткод через `-Dmixin.debug.export`, включайте `mixin.debug.countInjections` в CI, чтобы ловить внедрения без совпадений. Частично `[не проверено]`.
- **Потокобезопасность в генерации мира и асинхронной работе**: фичи и структуры, которые читают вне своего `WorldGenRegion` или используют `level.random` из другого потока, ломают параллельные движки (C2ME поставляет `CheckedThreadLocalRandom` для обнаружения). Используйте выданный `RandomSource`, оставайтесь внутри региона, никогда не трогайте `ServerLevel` из рабочих потоков.
- **Утечки**: статические кэши с `Level`, `Entity`, `ServerPlayer` или клиентским `ClientLevel` после выгрузки. Очищайте по событиям остановки сервера и выгрузки уровня или используйте слабые ссылки; проверяйте `/spark heapsummary` после выхода из мира. `[не проверено]`.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Block Entities (NeoForge)](https://docs.neoforged.net/docs/blockentities/) | NeoForged, документация 26.1 | Тикеры, `setChanged`, методы синхронизации, `hasChunkAt` |
| [Capabilities (NeoForge)](https://docs.neoforged.net/docs/inventories/capabilities) | NeoForged, документация 26.1 | `BlockCapabilityCache`, правила инвалидации |
| [Registering Payloads (NeoForge)](https://docs.neoforged.net/docs/networking/payload) | NeoForged, документация 26.1 | Потоки, `enqueueWork`, лимиты размера |
| [Networking (NeoForge)](https://docs.neoforged.net/docs/networking/) | NeoForged | Обзор вариантов синхронизации |
| [Data Attachments (NeoForge)](https://docs.neoforged.net/docs/datastorage/attachments) | NeoForged, документация 26.1 | Обработчик синхронизации, отметка «грязно» |
| [Block Entity Renderer (NeoForge)](https://docs.neoforged.net/docs/blockentities/ber) | NeoForged | Шаблоны стороны рендера |
| [Entity Renderers (NeoForge)](https://docs.neoforged.net/docs/entities/renderer) | NeoForged | Состояния рендера |
| [Models (NeoForge)](https://docs.neoforged.net/docs/resources/client/models/) | NeoForged | Запечка, загрузчики моделей |
| [Block States (NeoForge)](https://docs.neoforged.net/docs/blocks/states) | NeoForged | Когда свойства, а когда блок-сущность |
| [Block Entities (Fabric docs)](https://docs.fabricmc.net/develop/blocks/block-entities) | Fabric docs | Тикеры на стороне Fabric |
| [Block Entity Renderers (Fabric docs)](https://docs.fabricmc.net/develop/blocks/block-entity-renderer) | Fabric docs | Извлечение состояния рендера в Fabric |
| [Networking (Fabric docs)](https://docs.fabricmc.net/develop/networking) | Fabric docs | Пакеты и отправка |
| [Rendering basic concepts (Fabric docs)](https://docs.fabricmc.net/develop/rendering/basic-concepts) | Fabric docs | Словарь конвейера рендеринга |
| [Access Transformers (NeoForge)](https://docs.neoforged.net/docs/advanced/accesstransformers) | NeoForged | Замена рефлексии |
| [Access Wideners (Fabric wiki)](https://wiki.fabricmc.net/tutorial:accesswideners) | Fabric Wiki | Замена рефлексии |
| [MixinExtras wiki](https://github.com/LlamaLad7/MixinExtras/wiki) | LlamaLad7 | Совместимые инжекторы с малым числом конфликтов |
| [NeoForge 1.21.5 primer: Tickets / Game Test Overhaul](https://github.com/neoforged/.github/blob/main/primers/1.21.5/index.md) | ChampionAsh5357, 2025 | Типы тикетов; gametest на реестрах |

---

## Тесты: GameTest и юнит-тесты

**Ключевые выводы: ванильный GameTest (1.21.5+ и 26.x)**

- **1.21.5 (25w03a) переделал GameTest** на реестры: прежняя система на аннотациях (`@GameTest`, `@BeforeBatch`, `timeoutTicks`, `rotationSteps`) из ванили ушла. Теперь есть **функции** тестов (`Consumer<GameTestHelper>` в `BuiltInRegistries.TEST_FUNCTION`), **окружения** тестов (датапак-реестр `test_environment`, типы `all_of` и `function` с `setup` и `teardown`, `game_rules`, `weather`, `clock_time` (до 26.1 назывался `time_of_day`), `timeline_attributes` (26.1), `difficulty` (26.2)) и **экземпляры** тестов (датапак-реестр `test_instance`, типы `minecraft:block_based` и `minecraft:function`). Руководства с `@GameTest(template = ...)` для ванили и Forge 1.20.x — `[устарело]` для 1.21.5+.
- **JSON экземпляра теста** (`data/<ns>/test_instance/<name>.json`), ванильный пример `minecraft:always_pass`:

  ```json
  { "type": "minecraft:function", "function": "examplemod:pipe_moves_items",
    "environment": "minecraft:default", "structure": "examplemod:pipe_3x1",
    "max_ticks": 100, "setup_ticks": 1, "required": true,
    "rotation": "none", "manual_only": false, "sky_access": false,
    "max_attempts": 1, "required_successes": 1 }
  ```

  Значения по умолчанию по вики: `setup_ticks` 0, `required` true, `rotation` none, `manual_only` false, `sky_access` false, `max_attempts` 1, `required_successes` 1 (попытки и успехи нужны для тестов с плавающим временем). Заметки по версиям: 25w04a переименовал `batch` в `environment`; 1.21.11 заменил `bool_rules` и `int_rules` в окружениях `game_rules` одним ключом `rules`; 26.1-snapshot-3 переименовал `time_of_day` в `clock_time` (и добавил обязательный `clock`). Страница вики об окружениях всё ещё показывает `bool_rules`: доверяйте более новому журналу изменений.
- **Написание тестов**: `GameTestHelper` даёт `absolutePos` и `relativePos`, `succeed`, `succeedIf`, `succeedWhen` (опрашивается каждый тик до таймаута), `succeedOnTickWhen`, `runAtTickTime`, `runAfterDelay`, `onEachTick`, проверки, бросающие `GameTestAssertException`. В 26.2 добавлены билдеры `spawnEntity(...)` и `spawnMob(...)` (`.withNoFreeWill()`, `.spawnReason(...)`) и `makeMockServerPlayer`. 26.1 позволяет окружению вернуть исходное состояние из `setup` и восстановить его в `teardown`.
- **В игре**: `/test run <id>`, `runmultiple`, `runclosest`, `runthat`, `runthese`, `resetclosest`, `locate`, `clear`; блок экземпляра теста правит структуру и ID; итоговые строки «Game Test complete! %s test(s) were run», «All required tests passed :)».
- **Безголовый ванильный запуск**: `java -DbundlerMainClass="net.minecraft.gametest.Main" -jar server.jar --tests 'examplemod:*' --report build/gametest.xml --universe gametestserver --packs <dir> [--verify true]`. Код выхода: 0 — всё прошло, N — число провалов, -1 — сервер остановлен до тестов, 1 — падение. `--report` пишет XML в стиле JUnit; `--verify` запускает каждый тест 100 раз на каждый шаг поворота для поиска нестабильности. Подходит для модов только из датапаков и служит образцом для запусков загрузчиков. `[не проверено]`.

**Ключевые выводы: Fabric**

- **DSL Loom** (документация Fabric, 26.2): `fabricApi { configureTests { createSourceSet = true; modId = "examplemod-test"; enableGameTests = true; enableClientGameTests = true; eula = true; clearRunDirectory = true; username = "Player0" } }`. С `createSourceSet` тесты лежат в `src/gametest/` со своим `fabric.mod.json`, который объявляет точки входа `fabric-gametest` и `fabric-client-gametest`. Задачи в пользовательских проектах: **`runGameTest`** (сервер) и **`runClientGameTest`** (интеграционные тесты Loom используют эти имена; в собственном репозитории Fabric API — `runGametest`). По документации `build` запускает серверные gametest автоматически. Рабочий `build.gradle` проверен в CI: [`tests/minecraft/fabric-mod/build.gradle`](https://github.com/Betreazen/java-knowledge-skill/blob/main/tests/minecraft/fabric-mod/build.gradle) (в репозитории скилла лежит по пути `tests/minecraft/fabric-mod/build.gradle`).
- **Серверные тесты**: методы с аннотацией `@GameTest` из `net.fabricmc.fabric.api.gametest.v1` — методы экземпляра с одним `GameTestHelper`, завершаются `helper.succeed()` (или `succeedWhen`); структуры — через `@GameTest(structure = ...)` или файлы SNBT в `<modid>/gametest/structure/`; класс может реализовать `CustomTestMethodInvoker`, чтобы обернуть вызов. Флаги сервера gametest: `-Dfabric-api.gametest`, `-Dfabric-api.gametest.report-file=...`, `.filter`, `.verify`. Javadoc пакета модуля в примере всё ещё показывает старое имя `@FabricGameTest`; CI подтвердил, что с Fabric API 0.165.2+26.3 собирается аннотация `@GameTest` из `net.fabricmc.fabric.api.gametest.v1`. Минимальный тест (проверено в CI на GitHub Actions):

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

  Метаданные тестового мода с точкой входа `fabric-gametest` (проверено в CI на GitHub Actions):

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

- **Клиентские gametest** (модуль «Client Game Test API», новый в посте о выпуске 1.21.5): класс реализует `FabricClientGameTest#runTest(ClientGameTestContext)`; `context.worldBuilder().create()` даёт `TestSingleplayerContext`; `getClientLevel().waitForChunksRender()`; `context.takeScreenshot("name")` (сравнение скриншотов для модов интерфейса, HUD и рендеринга).
- **CI** (`build.yaml` из документации Fabric; повторить `[не проверено]`): зарегистрируйте `tasks.register("runProductionClientGameTest", net.fabricmc.loom.task.prod.ClientProductionRunTask) { jvmArgs.add("-Dfabric.client.gametest"); useXVFB = true }`, запускайте на `ubuntu-24.04` с Temurin 25, выгружайте `build/run/clientGameTest/screenshots`. `useXVFB` по умолчанию true на Linux при заданном `CI`; установите `xvfb`. Если GitHub Actions падает с ошибкой network synchronizer, добавьте `-Dfabric.client.gametest.disableNetworkSynchronizer=true`. Производственные задачи запуска требуют модов, переремапленных в intermediary, для обфусцированных версий (1.21.11 и старше).
- **Юнит-тесты**: `testImplementation "net.fabricmc:fabric-loader-junit:${loader_version}"` и `test { useJUnitPlatform() }`. По документации обычный JUnit «не сработает», потому что моды полагаются на изменение байткода во время работы (Mixin); `fabric-loader-junit` запускает тесты под загрузчиком. Классам, зависящим от реестров, нужны `SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();` в `@BeforeAll`, иначе `IllegalArgumentException: Not bootstrapped (called from registry ...)`.

**Ключевые выводы: NeoForge** (документация 26.1, ModDevGradle 2.0.148)

- Регистрируйте функции через `DeferredRegister.create(BuiltInRegistries.TEST_FUNCTION, MODID)`; собственные типы экземпляров — через `BuiltInRegistries.TEST_INSTANCE_TYPE` (наследуйте `GameTestInstance`, дайте `MapCodec`); окружения — в `data/<ns>/test_environment/` или datagen `bootstrap.register(Registries.TEST_ENVIRONMENT, ...)`; `RegisterGameTestsEvent` (`registerEnvironment`, `registerTest`) обходится без датапака.
- Запуск: запуск MDG с `type = "gameTestServer"`, команда `./gradlew runGameTestServer`; код выхода равен числу проваленных обязательных тестов (все провалы логируются). `neoforge.enableGameTest` включён по умолчанию только в запусках `client` и `gameTestServer`. Сборка и запуск проверены в CI на GitHub Actions. Минимальный мод с функцией теста (проверено в CI на GitHub Actions):

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

  Экземпляр теста для этой функции (проверено в CI на GitHub Actions). Структура `examplemod:empty` в NeoForge не поставляется: создайте её скриптом [`../scripts/EmptyStructure.java`](../scripts/EmptyStructure.java) и положите в `src/main/resources/data/examplemod/structure/empty.nbt` (в CI команда `java EmptyStructure.java src/main/resources/data/examplemod/structure/empty.nbt 5023 3`):

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

- JUnit: `neoForge { unitTest { enable(); testedMod = mods.examplemod } }` плюс `junit-jupiter` и `junit-platform-launcher`; тесты в `src/test` могут ссылаться на классы Minecraft. С `testImplementation "net.neoforged:testframework:<версия neoforge>"` аннотируйте `@ExtendWith(EphemeralTestServerProvider.class)` и получайте параметр `MinecraftServer` в методах `@Test`: интеграционные тесты без клиента. Последний опубликованный testframework — 26.1.2.126; наличие версии для 26.3 `[не проверено]`.
- Forge (MinecraftForge) ещё существует; имена его системных свойств gametest исторически имели префикс `forge.` (`forge.enableGameTest`, `forge.enabledGameTestNamespaces`): проверяйте по версии. `[не проверено]`.

**Ключевые выводы: подмена зависимостей и Paper**

- **Моки реестров**: предпочитайте настоящий bootstrap (`Bootstrap.bootStrap()`) моками. Реестрам на данных нужен `RegistryAccess` (соберите минимальный или используйте gametest). Мок `Level` и `ServerLevel` через Mockito обычно ломается на глубоких цепочках вызовов: вынесите зависящую от мира логику за небольшой интерфейс, которым владеет мод, юнит-тестируйте логику, а интеграцию покройте gametest. `[не проверено]`.
- **Плагины Paper**: MockBukkit `org.mockbukkit.mockbukkit:mockbukkit-v26.3` 4.119.0 (артефакты по версиям MC: `-v26.3`, `-v26.2`, `-v26.1.2`, `-v1.21`). Типичный поток: `ServerMock server = MockBukkit.mock(); MyPlugin p = MockBukkit.load(MyPlugin.class); PlayerMock pl = server.addPlayer(); server.getScheduler().performTicks(20); MockBukkit.unmock();`. Артефакт проверен; имена методов API `[не проверено]`: сверьтесь с docs.mockbukkit.org.

Предлагаемое задание GitHub Actions для проектов на загрузчиках `[не проверено]`:

```yaml
jobs:
  test:
    runs-on: ubuntu-24.04
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: '25' }
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew test                       # JUnit (fabric-loader-junit / MDG unitTest)
      - run: ./gradlew runGameTest                # серверные gametest Fabric (NeoForge: runGameTestServer)
      - run: sudo apt-get install -y xvfb && ./gradlew runProductionClientGameTest   # клиентские gametest Fabric
      - uses: actions/upload-artifact@v4
        if: always()
        with: { name: gametest-output, path: build/run/clientGameTest/screenshots }
```

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [GameTest](https://minecraft.wiki/w/GameTest) | Minecraft Wiki, 2026 | Концепции, безголовый `net.minecraft.gametest.Main`, коды выхода, история |
| [Test instance definition](https://minecraft.wiki/w/Test_instance_definition) | Minecraft Wiki | Поля JSON и значения по умолчанию |
| [Test environment definition](https://minecraft.wiki/w/Test_environment_definition) | Minecraft Wiki | Типы окружений |
| [Automated Testing (Fabric docs)](https://docs.fabricmc.net/develop/automatic-testing) | Fabric docs, 26.2 | fabric-loader-junit, серверные и клиентские gametest, рабочий процесс CI |
| [Fabric API DSL (Loom)](https://docs.fabricmc.net/develop/loom/fabric-api) | Fabric docs | Опции `configureTests` и значения по умолчанию |
| [Loom Tasks](https://docs.fabricmc.net/develop/loom/tasks) | Fabric docs | Справочник задач |
| [Loom Options](https://docs.fabricmc.net/develop/loom/options) | Fabric docs | Опции расширения |
| [fabric-loom (GitHub)](https://github.com/FabricMC/fabric-loom) | FabricMC | Интеграционные тесты подтверждают `runGameTest` и `runClientGameTest` |
| [fabric-client-gametest-api-v1](https://github.com/FabricMC/fabric-api/tree/26.3/fabric-client-gametest-api-v1) | FabricMC | Исходники API клиентских gametest |
| [Fabric for Minecraft 1.21.5](https://fabricmc.net/2025/03/24/1215.html) | FabricMC, 2025 | Анонс модуля Client Game Test API (Earthcomputer); изменения gametest после ванильной переделки |
| [Fabric for Minecraft 1.21.4](https://fabricmc.net/2024/12/02/1214.html) | FabricMC, 2024 | Заметки для разработчиков предыдущего цикла (контекст изменений 1.21.5) |
| [Game Tests (NeoForge docs)](https://docs.neoforged.net/docs/misc/gametest) | NeoForged, документация 26.1 | Функции, экземпляры, окружения, `runGameTestServer` |
| [ModDevGradle: Unit testing with JUnit](https://github.com/neoforged/ModDevGradle) | NeoForged | `unitTest { enable() }`, `EphemeralTestServerProvider` |
| [NeoForge 26.1 primer](https://github.com/neoforged/.github/blob/main/primers/26.1/index.md) | ChampionAsh5357, 2026 | Отслеживание состояния окружения тестов |
| [NeoForge 26.2 primer](https://github.com/neoforged/.github/blob/main/primers/26.2/index.md) | ChampionAsh5357, 2026 | Билдеры сущностей для gametest |
| [NeoForge 26.3 primer](https://github.com/neoforged/.github/blob/main/primers/26.3/index.md) | ChampionAsh5357, 2026 | Последние перемещения API (пакеты рендеринга) |
| [MockBukkit](https://github.com/MockBukkit/MockBukkit) | MockBukkit team | Юнит-тесты плагинов Paper |
| [MockBukkit docs](https://docs.mockbukkit.org/) | MockBukkit team | Руководство по API |

---

## Отладка

**Ключевые выводы**

- **Где что лежит**: `logs/latest.log` (и история в gzip), `crash-reports/crash-<date>-<side>.txt`, фатальные ошибки JVM `hs_err_pid*.log` в папке игры (нативные падения: драйвер GPU, LWJGL, нехватка нативной памяти), `debug/` для вывода `/perf`, `/jfr` и F3+L; при разработке: `run/logs`, `run/crash-reports`, `run/.mixin.out`.
- **Чтение краш-репорта** (руководство игрока Fabric): голова («Description», исключение, стек) — главное; «Last Reload» важен только при падениях при перезагрузке ресурсов (обычно ресурспаки); «System Details» перечисляет моды (Fabric добавляет «Fabric Mods:»). Начинайте с первого мода в стеке, но падения совместимости часто винят второй мод в цепочке. Кадры обработчиков Mixin выглядят как `<modid>$handlerName` в именах методов: префикс указывает на мод.
- **Сбои Mixin**: ищите в логе `Caused by: org.spongepowered.asm.mixin`, `Mixin apply for mod <id> failed` и `<id>.mixins.json`, чтобы найти владельца (руководство поддержки Modrinth, октябрь 2026). Частые причины: цель переименована после обновления игры (в CI используйте `-Dmixin.debug.countInjections=true`, чтобы упасть сразу), два мода делают `@Overwrite` или `@Redirect` одного вызова, несовпадение сигнатуры `@Shadow`, миксин в клиентский класс, загруженный на сервере. Отладка: `-Dmixin.debug.export=true` (плюс `.filter`) и `-Dmixin.dumpTargetOnFailure=true`, затем читайте `.mixin.out`.
- **NoSuchMethodError, NoSuchFieldError, AbstractMethodError**: компиляция против другой версии игры, загрузчика или библиотеки (не та сборка мода для версии MC, изменение API зависимости или дважды упакованный jar). В Fabric 1.21.11 и старше — ещё несовпадение пространства имён мэппингов (dev jar не переремаплен). `-Dmixin.checks.interfaces=true` находит миксины, объявляющие интерфейсы, которые они не реализуют. Частично `[не проверено]`.
- **ClassNotFoundException и NoClassDefFoundError из-за несовпадения сторон**: ссылки на клиентские классы (`Minecraft`, `Screen`, рендереры) из общего кода, который выполняется на выделенном сервере. Документация NeoForge называет это самыми частыми ошибками плохого разделения клиента и сервера. Изолируйте клиентский код в классе `@Mod(value = MODID, dist = Dist.CLIENT)` (NeoForge) или в точке входа и наборе исходников `client` (раздельное окружение Fabric); логическую сторону проверяйте `level.isClientSide()`, физическую — `FMLEnvironment.getDist()`, и **всегда проверяйте на выделенном сервере** (`runServer` или `runGameTestServer` в CI ловят большинство таких ошибок).
- **Конфликты модов**: бинарный поиск по списку модов (отключить половину, повторить; руководство Modrinth); `-Dfabric.debug.disableModIds=a,b` в Fabric не требует перемещать jar; ошибки, зависящие от порядка, сверяйте с `-Dfabric.debug.disableModShuffle`.
- **Стеки и имена**: 26.1+ поставляется без обфускации, поэтому стеки в продакшене, профили spark и дампы кучи показывают настоящие имена Mojang, без шага ремаппинга. Для 1.21.11 и старше производственные стеки Fabric используют intermediary (`class_1234`, `method_5678`) и требуют ремаппинга (селектор мэппингов в просмотрщике spark; Not Enough Crashes деобфусцирует стеки). Отсутствие обфускации проверено; охват инструментов `[не проверено]`.
- **Помощники при падениях** (игроки и авторы сборок, полезны и разработчику для первичной сортировки): Not Enough Crashes (возврат на заголовочный экран вместо выхода, список замешанных модов со ссылками на трекеры, файл и строка Mixin в стеках, ZIP для локального воспроизведения; 26.1.2–26.3), Crash Assistant (GUI после падения с логами, файлами hs_err, анализом известных причин, предупреждением об интегрированной графике; 26.2–26.3), Neruina (не даёт падениям тикающих сущностей и блок-сущностей «убить» миры; 26.1.2–26.3), MixinTrace и MixinTrace Reborn (раздел «Mixins in Stacktrace»; Reborn покрывает 26.1.x на Fabric, Forge, NeoForge).
- **Зависания и взаимоблокировки**: `jcmd <pid> Thread.print` (или `jstack`) дважды с интервалом 5 с; смотрите «Server thread» и «Render thread». Сторожевой таймер сервера (`max-tick-time`) пишет полный дамп потоков в краш-репорт при зависшем тике. Классические причины: `CompletableFuture.join()` в главном потоке в ожидании работы, которой нужен главный поток (`enqueueWork` и `execute`), `synchronized`, общий с рабочими потоками, доступ к чанкам из потоков генерации мира. `[не проверено]`.
- **Горячая перезагрузка**: HotSwap в режиме отладки перезагружает только тела методов; JetBrains Runtime с `-XX:+AllowEnhancedClassRedefinition` позволяет ещё добавлять и удалять методы, поля и классы. Горячая перезагрузка Mixin требует `-javaagent:"<путь к jar sponge-mixin>"` в конфигурации запуска (документация Fabric); MDG задаёт тот же флаг JBR через `jvmArgument(...)`. Ассеты: F3+T; данные: `/reload`. Содержимое реестров и *цели* миксинов всё равно требуют перезапуска. HotswapAgent добавляет плагины перезагрузки под отдельные фреймворки (универсальный инструмент JVM).
- **Логирование**: логгер SLF4J на мод (`LoggerFactory.getLogger(MOD_ID)`); для отладочного вывода при разработке — `log4j-dev.xml` и `loom.log4jConfigs.from "log4j-dev.xml"` (Fabric) или `logLevel` в MDG. Никогда не логируйте каждый тик на уровне info: сборка строк лога видна в профилях аллокаций. Частично `[не проверено]`.
- **Dev login**: DevLogin (covers1624) позволяет dev-запуску использовать настоящий аккаунт (нужно для части отладки мультиплеера); описан в README MDG.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Reading crash reports (Fabric)](https://docs.fabricmc.net/players/troubleshooting/crash-reports) | Fabric docs | Разделы, поиск виновника, именование кадров Mixin |
| [Uploading logs (Fabric)](https://docs.fabricmc.net/players/troubleshooting/uploading-logs) | Fabric docs | Безопасная передача логов |
| [Debugging Mods (Fabric docs)](https://docs.fabricmc.net/develop/debugging) | Fabric docs, 26.2 | Настройка логирования, точки останова, hotswap, расположение логов и краш-репортов |
| [Launching the game: IntelliJ IDEA (Fabric docs)](https://docs.fabricmc.net/develop/getting-started/intellij-idea/launching-the-game) | Fabric docs | Расширенная перезагрузка JBR, hotswap Mixin через `-javaagent` |
| [Launching the game: VS Code (Fabric docs)](https://docs.fabricmc.net/develop/getting-started/vscode/launching-the-game) | Fabric docs | То же для VS Code |
| [Debugging Minecraft crashes (Modrinth support)](https://support.modrinth.com/aen/articles/8792916) | Fen (Modrinth), 2026 | Маркеры Mixin, бисекция модов |
| [Sides (NeoForge docs)](https://docs.neoforged.net/docs/concepts/sides) | NeoForged, документация 26.1 | Физическая и логическая сторона, моды `Dist.CLIENT`, причина NoClassDefFoundError |
| [Structuring your mod (NeoForge)](https://docs.neoforged.net/docs/gettingstarted/structuring) | NeoForged | Разделение клиентского и общего кода |
| [Mixin wiki](https://github.com/SpongePowered/Mixin/wiki) | SpongePowered | Семантика инжекторов, отладка |
| [Not Enough Crashes](https://github.com/natanfudge/Not-Enough-Crashes) | natanfudge | Восстановление после падений, стеки с учётом Mixin |
| [Not Enough Crashes (Modrinth)](https://modrinth.com/mod/notenoughcrashes) | natanfudge | Версии 26.1.2–26.3 |
| [Crash Assistant](https://github.com/KostromDan/Crash-Assistant) | KostromDan | GUI и анализ после падения |
| [Neruina](https://github.com/Bawnorton/Neruina) | Bawnorton | Защита от падений в тике |
| [MixinTrace](https://github.com/comp500/mixintrace) | comp500 | «Mixins in Stacktrace» (Fabric, старые версии) |
| [MixinTrace Reborn (Modrinth)](https://modrinth.com/mod/mixintrace-reborn) | IAFEnvoy | То же для 26.1.x на Fabric, Forge, NeoForge |
| [JetBrains Runtime](https://github.com/JetBrains/JetBrainsRuntime) | JetBrains | Расширенная перезагрузка классов |
| [HotswapAgent](https://github.com/HotswapProjects/HotswapAgent) | HotswapProjects | Дополнительные плагины перезагрузки |
| [DevLogin](https://github.com/covers1624/DevLogin) | covers1624 | Dev-запуски с настоящим аккаунтом |
| [mclo.gs](https://mclo.gs/) | Aternos | Вставка логов с подсветкой для MC |
| [Fabric: Minecraft obfuscation removal](https://fabricmc.net/2025/10/31/obfuscation.html) | FabricMC, 2025 | Почему 26.1 меняет стеки и мэппинги |

---

## Как встроить Minecraft в конвейер оптимизации

**Ключевые выводы** (синтез разделов выше `[не проверено]`)

- **Бенчмарки**: два слоя. Микро: JMH для чистой логики мода ([JMH для логики мода](#jmh-для-логики-мода)) с `-prof gc` и JSON, который сравнивает [`../scripts/BenchCompare.java`](../scripts/BenchCompare.java). Макро: сценарный мир и `/tick sprint` (мс на тик) через [`../scripts/tick_sprint_bench.sh`](../scripts/tick_sprint_bench.sh) ([Бенчмарк мира: /tick sprint](#бенчмарк-мира-tick-sprint)) и `/tick query` (перцентили) на выделенном сервере, либо gametest, который строит сценарий и проверяет бюджет времени. Клиент: `--quickPlaySingleplayer <world>` и захват Tracy через блок `tracy {}` в Loom или график времени кадра F3+2.
- **Базовый замер**: тот же мир, seed, набор модов и флаги JVM; 3–5 запусков после прогрева (для спринтов — 5 запусков сервера на сторону, чередование ABBA); храните сырые файлы spark, JFR и Tracy и версии (MC, загрузчик, мод, JDK) рядом с числами.
- **Ревью**: вид Sources в spark относит время к моду; аллокации JFR по стеку; CSV `levels/` из `/perf` показывают число сущностей и блок-сущностей по чанкам; сверка с таблицей симптомов в начале файла и [чеклистом](#чеклист-ревью-производительности-и-стабильности).
- **Решения из библиотеки**: выберите шаблон из разделов [Уроки модов оптимизации](#уроки-модов-оптимизации) и [Типичные проблемы производительности в модах](#типичные-проблемы-производительности-в-модах) (засыпание и пробуждение, кэш и инвалидация, амортизация, удаление аллокаций, раскладка данных, чистая работа вне главного потока, замена алгоритма) и сошлитесь на мод, который это доказал.
- **Оптимизация и повторный замер**: одно изменение за итерацию, тот же протокол; оставляйте только изменения, превышающие шум между запусками; перезапускайте gametest (паритет поведения) и проверку на выделенном сервере (безопасность сторон).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Finding lag spikes](https://spark.lucko.me/docs/guides/Finding-lag-spikes) | lucko | Процедура профилирования всплесков |
| [Commands/tick](https://minecraft.wiki/w/Commands/tick) | Minecraft Wiki | Драйвер макробенчмарка |
| [Production Run Tasks (Loom)](https://docs.fabricmc.net/develop/loom/production-run-tasks) | Fabric docs | Скриптовые клиентские запуски с захватом Tracy |
| [Noisium README (benchmark evidence)](https://github.com/Steveplays28/noisium) | Steveplays28 | Пример публикации профилей spark до и после |
| [FerriteCore summary.md](https://github.com/malte0811/FerriteCore/blob/26.1/summary.md) | malte0811 | Пример учёта памяти и CPU по каждому изменению |

---

## Чеклист ревью производительности и стабильности

Производительность: сервер и логика

- [ ] 1. Каждый `BlockEntityTicker` обоснован; клиентский тикер `null`, если не нужен; простаивающие машины перестают тикать (засыпание и пробуждение) или тикают раз в N тиков.
- [ ] 2. Нет сканов мира, запросов сущностей по AABB и поиска рецептов каждый тик без амортизации или кэша.
- [ ] 3. Поиск возможностей и инвентарей соседей кэшируется (`BlockCapabilityCache` или аналог) и корректно инвалидируется.
- [ ] 4. Нет случайных загрузок чанков: `hasChunkAt` или `isLoaded` перед доступом между чанками; нет доступа из генерации мира за пределами региона.
- [ ] 5. Тикеты чанков: верное использование `TicketType` (загрузка или симуляция), срок жизни, минимальный радиус; никаких постоянных принудительных загрузок по умолчанию.
- [ ] 6. Горячие пути без аллокаций: `MutableBlockPos`, без `Enum#values()`, без stream и боксинга; проверено spark `--alloc` или JMH `-prof gc`.
- [ ] 7. Коллекции подобраны по размеру и типу (примитивные fastutil, ключи `asLong()`); нет `List.contains` по большим спискам каждый тик.
- [ ] 8. Нет рефлексии в горячих путях (Accessor и Invoker миксины, access wideners и transformers, `MethodHandle`).
- [ ] 9. Миксины целятся в узкие точки; нет `@Overwrite`; обработчики крошечные; `countInjections` включён в CI.
- [ ] 10. Потокобезопасность: нет доступа к `ServerLevel` из рабочих и сетевых потоков; `enqueueWork` с `.exceptionally`; в генерации мира выданный `RandomSource`.
- [ ] 11. NBT и вложения малы; `setChanged` только при реальном изменении; большие данные в `SavedData` с флагом «грязно».
- [ ] 12. Рост числа сущностей ограничен (слияние, правила исчезновения, лимиты порождаемых помощников и снарядов).

Производительность: сеть и клиент

- [ ] 13. Синхронизация при изменении с ограничением частоты; дельты для крупного состояния; только отслеживающие игроки; размеры пакетов в пределах лимитов.
- [ ] 14. Обработчики пакетов выполняют тяжёлый разбор вне главного потока.
- [ ] 15. Рендереры не опрашивают мир и не обходят инвентари на каждом кадре; состояние рендера готовится за тик или при изменении.
- [ ] 16. Статичный визуал запечён в модели; ограничивающие объёмы рендера точные; частицы ограничены и проверяются по расстоянию.
- [ ] 17. Нет сырого OpenGL (только Blaze3D) на 26.x; проверено и на OpenGL, и на экспериментальном бэкенде Vulkan (26.2+).
- [ ] 18. Число свойств состояния блока минимально; нет комбинаторного взрыва моделей; изменение старта и кучи измерено.

Стабильность и корректность

- [ ] 19. Клиентские классы изолированы (класс мода `Dist.CLIENT` или клиентская точка входа); проверка на выделенном сервере в CI.
- [ ] 20. Gametest покрывают основную механику (по одному на машину, ритуал, поведение моба, фичу генерации мира), запускаются без головы в CI с отчётом JUnit; нестабильные используют `max_attempts` и `required_successes` и исправляются, а не игнорируются.
- [ ] 21. Юнит-тесты для чистой логики (fabric-loader-junit или `unitTest` в MDG); реестры инициализируются один раз.
- [ ] 22. Клиентские gametest со скриншотами для модов интерфейса, HUD и рендеринга.
- [ ] 23. Нет статических ссылок на `Level`, `Entity`, `Player`, переживающих выгрузку мира; сводка кучи проверена после циклов входа и выхода.
- [ ] 24. Исключения в коде тика обрабатываются по экземплярам (одна сломанная машина не роняет сервер); нет проглоченных исключений в futures.
- [ ] 25. Логирование: нет логов уровня info в каждом тике; отладочные логи защищены условием; в логах ошибок есть контекст (позиция, измерение, id).
- [ ] 26. Конфигурационные значения, определяющие стоимость (дальности, интервалы, лимиты), имеют безопасные значения по умолчанию и документированы.
- [ ] 27. Хуки профилирования: `Profiler.get().push("<modid>:...")` вокруг крупных подсистем, чтобы spark, Tracy и F3+L относили время.
- [ ] 28. К каждому утверждению о производительности приложены измерения (ссылка spark, JFR, Tracy, JSON JMH) с версиями.
- [ ] 29. Совместимость проверена с распространённым набором модов производительности (Sodium, Lithium, FerriteCore, Moonrise или C2ME, ImmediatelyFast, EntityCulling) на целевых загрузчике и версии.
- [ ] 30. Факты о версиях записаны (различия MC 26.x и 1.21.11: имена без обфускации, Java 25, формат реестров GameTest).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Lithium Fabric mixin config](https://github.com/CaffeineMC/lithium/blob/develop/lithium-fabric-mixin-config.md) | CaffeineMC | Каталог серверных оптимизаций размером с чеклист |
| [Block Entities (NeoForge)](https://docs.neoforged.net/docs/blockentities/) | NeoForged | Правила тиков и синхронизации за пунктами 1, 11, 13 |
| [Sides (NeoForge)](https://docs.neoforged.net/docs/concepts/sides) | NeoForged | Пункт 19 |
| [Moonrise compatibility table](https://github.com/Tuinity/Moonrise) | Tuinity | Пункт 29 (что с чем конфликтует) |
| [Sodium hardware/compat notes](https://github.com/CaffeineMC/sodium) | CaffeineMC | Пункт 29 (ожидания рендерера) |

---

## Открытые вопросы

- Точные имена событий JFR `minecraft.*` у новых классов (`ChunkRegionRead/Write`, `StructureGeneration`, `ClientFps`) и добавила или переименовала ли 26.x события: сверить через `jfr metadata` на записи 26.3 `[не проверено]`.
- Работает ли `/jfr` на встроенном сервере в 26.x так же (вики молчит); MC-307177 исправил падение клиента при запуске JFR в 26.2.
- Полный список флагов `-DMC_DEBUG_*` для отладочных рендереров в 26.x (из primer 1.21.9 подтверждён только `MC_DEBUG_ENABLED`).
- Совместимость протокола GUI Tracy v0.15.0 и встроенного `jtracy` 1.14.38: версия инструмента захвата должна совпадать с клиентской библиотекой; не проверялось.
- Подключён ли `runGameTest` Loom к `check` или `build` по умолчанию в текущем Loom (документация говорит, что `build` запускает серверные gametest; проверить в CI) и работает ли `runClientGameTest` без головы без `ClientProductionRunTask`.
- Имя аннотации gametest в Fabric: документация использует `@GameTest` (`net.fabricmc.fabric.api.gametest.v1`), а пример в javadoc пакета всё ещё говорит `@FabricGameTest`. Сборка в CI с Fabric API 0.165.2+26.3 принимает `@GameTest`.
- Формат окружения `game_rules` в 26.x: страница вики об окружениях показывает `bool_rules` и `int_rules`, страница GameTest говорит, что 1.21.11 заменила их одним ключом `rules`: сверить по ванильным данным или валидатору датапаков.
- Артефакт NeoForge `testframework`: последняя опубликованная версия 26.1.2.126; наличие для 26.3 неизвестно.
- Будущее ModernFix на 26.2+ (есть только сборки NeoForge 26.1.2; Fabric остановился на 1.21.1): официального заявления не найдено.
- Причина архивации Noisium (заархивирован 2025-07 без пометки в README); C2ME предположительно заменяет его для генерации мира.
- Эффект `-XX:+UseCompactObjectHeaders` на кучу и пропускную способность Minecraft: опубликованного бенчмарка не найдено; кандидат на A/B-тест.
- Имена методов MockBukkit API (`performTicks`, `addPlayer`) взяты по памяти о прежних версиях; сверьтесь с docs.mockbukkit.org для v26.3.
- Специфика gametest и JUnit у Forge (MinecraftForge) 26.x не исследовалась глубоко.
- Полный список оптимизаций Krypton выведен по именам файлов его миксинов (в вики списка нет).
