---
name: java-knowledge
description: Knowledge base and workflow for Java and the JVM in any domain — libraries, backends (Spring Boot, Quarkus, Micronaut), JDBC/JPA, Kafka, CLI, JavaFX, Spark, ML/LLM apps, games (libGDX, LWJGL), Minecraft mods/plugins (Fabric, NeoForge, Paper, mixins), embedded. Use for any question about Java code, even one that looks quick to answer from memory: writing or reviewing Java; javac errors (incompatible bounds, cannot find symbol) and exceptions (ConcurrentModificationException); records, sealed types, pattern matching, streams; JDK versions and JEPs; Maven or Gradle builds, Lombok/MapStruct that stopped running; Error Prone, NullAway, SpotBugs, Spotless; JUnit, Mockito, Testcontainers, JaCoCo; slow code, hot profiler frames, allocations, GC pauses, slow startup; JMH, JFR, async-profiler; hangs and deadlocks, virtual threads, CompletableFuture, commonPool, Java Memory Model, FFM API; multi-module structure, JPMS; Java books. Includes a curated library, CI-verified templates and a measured optimization pipeline.
---

# Java: библиотека знаний и пайплайн

Скилл помогает писать, проверять и ускорять код на Java в любой предметной области. Внутри три вещи:

1. **Библиотека знаний.** Книги, статьи, доклады, документация, JEP, кодовые базы и паттерны по категориям. Каждый источник дан со ссылкой, автором, годом и пояснением, зачем его читать.
2. **Тулчейн.** Какие JDK, инструменты сборки и анализаторы ставить, какие команды запускать и в каком порядке. Шаблоны `pom.xml`, Gradle и CI собраны и проверены.
3. **Пайплайн.** Порядок работы: тесты до кода, проверки качества, бенчмарки JMH, ревью, поиск решений в библиотеке и повторный замер.

Состояние знаний — ноябрь 2026: JDK 27 (сентябрь 2026) — последний релиз, JDK 25 — текущий LTS, JDK 21 и 17 — предыдущие LTS, JDK 28 выйдет в марте 2027. Maven 3.9.16 и 3.10.0 (Maven 4 всё ещё RC), Gradle 9.9, JUnit 6.1, Spring Boot 4.1, Quarkus 3.40, JMH 1.37. Minecraft: 26.3 — последний релиз (сентябрь 2026), 26.4 в снапшотах; с 26.1 игра без обфускации и требует Java 25.

## Куда смотреть

| Задача | Файл |
| --- | --- |
| Новая фича, рефакторинг или оптимизация: порядок работы | [references/pipeline.md](references/pipeline.md) |
| Дистрибутивы и версии JDK, установка, Maven и Gradle, плагины, BOM, аннотационные процессоры, javac `-Xlint`, Error Prone, NullAway, SpotBugs, PMD, Spotless, OpenRewrite, безопасность зависимостей, инструменты JDK, CI, шаблоны | [references/toolchain.md](references/toolchain.md) |
| JUnit 6, AssertJ, Mockito, Testcontainers, срезы Spring Boot, property-based и контрактные тесты, JaCoCo, PIT, нестабильные тесты | [references/testing.md](references/testing.md) |
| Код медленный или ест память: техника по симптому, JMH, JFR, JIT, GC, старт и прогрев | [references/performance.md](references/performance.md) — начните с таблицы «Симптом → техника → источник» |
| Возможности языка Java 17–28 по JEP, идиомы, null и Optional, исключения, паттерны и антипаттерны, безопасность, чеклист ревью | [references/idioms-patterns.md](references/idioms-patterns.md) |
| Структура проекта, многомодульные сборки, пакеты и JPMS, слои и гексагональная архитектура, DDD, ArchUnit, DI, эволюция API, эталонные кодовые базы | [references/architecture.md](references/architecture.md) |
| Модель памяти, executors, CompletableFuture, блокировки, виртуальные потоки, структурная конкурентность, scoped values, реактивные библиотеки, FFM API, JNI, Unsafe, Vector API | [references/concurrency-native.md](references/concurrency-native.md) |
| Серверные области: Spring Boot, Quarkus, Micronaut, Helidon, REST и gRPC, JDBC, JPA и Hibernate, миграции, Kafka, наблюдаемость, безопасность, контейнеры и Kubernetes | [references/domains-server.md](references/domains-server.md) |
| Прочие области: публикация библиотек, CLI и TUI, десктоп, данные, ML и LLM-приложения, игры, embedded, WebAssembly, interop, скрипты | [references/domains-client.md](references/domains-client.md) |
| Что почитать, блоги, доклады, подкасты, курсы, русскоязычные ресурсы, маршрут обучения по уровням | [references/books.md](references/books.md). На общий вопрос о книгах начните с разделов [«Короткий список: 20 главных источников»](references/books.md#короткий-список-20-главных-источников) и [«Маршрут чтения по уровням»](references/books.md#маршрут-чтения-по-уровням), тематические разделы открывайте по оглавлению. Файл большой, целиком его не читайте |

**Ветвь Minecraft** (Java Edition: моды и серверные плагины, Minecraft 26.x и последняя 1.21.x):

| Задача | Файл |
| --- | --- |
| Начать мод: выбор загрузчика (Fabric, NeoForge, Forge, Quilt), версии игры и Java, Loom и ModDevGradle, мульти-загрузчик и мульти-версия, публикация на Modrinth и CurseForge, сборки модпаков, CI, проверенные шаблоны | [references/minecraft-modding.md](references/minecraft-modding.md) |
| Код мода: стороны и потоки, реестры, компоненты данных, блоки, сущности, датаген, кодеки, сеть, события, миксины, access wideners и transformers, рендер, конфиги, генерация мира, перенос на новую версию, чеклист ревью | [references/minecraft-mod-api.md](references/minecraft-mod-api.md) — что сломалось в 26.x, смотрите в разделе [«Что сломалось в 26.x»](references/minecraft-mod-api.md#что-сломалось-в-26x) |
| Лаги, TPS и MSPT, FPS, память, медленный старт; spark и JFR; бенчмарк мира `/tick sprint`; GameTest и юнит-тесты; разбор краш-репортов и конфликтов модов | [references/minecraft-performance-testing.md](references/minecraft-performance-testing.md) — начните с таблицы «Симптом → техника → источник» |
| Сервер: датапак, плагин или мод; Paper, Folia, Velocity, Spigot, Sponge, Minestom; Polymer; Geyser и ViaVersion; инженерия и шаблоны плагинов | [references/minecraft-server-plugins.md](references/minecraft-server-plugins.md) |
| Документация и её свежесть, как читать старые туториалы, курсы, эталонные моды по типам, сообщество, маршрут обучения | [references/minecraft-resources.md](references/minecraft-resources.md) |

Каждый справочник начинается с оглавления. Читайте только нужный раздел, а не файл целиком.

Частые вопросы и разделы с ответом:

| Вопрос | Раздел |
| --- | --- |
| После перехода на JDK 23+ пропали сгенерированные классы (Lombok, MapStruct, JMH), а сборка проходит; другие неочевидные сбои сборки | [toolchain.md → «Грабли: сводка»](references/toolchain.md#грабли-сводка) |
| Какой командой проверить проект перед слиянием | [toolchain.md → «Минимальные проверки качества»](references/toolchain.md#минимальные-проверки-качества) |
| Что видно в профиле или бенчмарке и что с этим делать | [performance.md → «Симптом → техника → источник»](references/performance.md#симптом--техника--источник) |
| Как написать бенчмарк, чтобы он не врал | [performance.md → «Бенчмарки: JMH»](references/performance.md#бенчмарки-jmh) |
| Чем профилировать на Windows, Linux и macOS | [performance.md → «Профилирование»](references/performance.md#профилирование) |
| Можно ли использовать возможность языка на нашей версии JDK | [idioms-patterns.md → «Версии языка: Java 17–28»](references/idioms-patterns.md#версии-языка-java-1728) |
| Переводить ли сервис на виртуальные потоки и что проверить | [concurrency-native.md → «Виртуальные потоки»](references/concurrency-native.md#виртуальные-потоки), для Spring Boot 4 — ещё [domains-server.md → «Фреймворки и рантаймы»](references/domains-server.md#1-фреймворки-и-рантаймы) |
| Какую модель конкурентности выбрать под нагрузку | [concurrency-native.md → «Выбор инструмента по нагрузке»](references/concurrency-native.md#выбор-инструмента-по-нагрузке) |
| Что изменилось в Spring Boot 4, Jackson 3, Hibernate 7 и что удалено | [domains-server.md → «Версии на ноябрь 2026»](references/domains-server.md#версии-на-ноябрь-2026) |
| Медленный эндпоинт, N+1, пул соединений | [domains-server.md → «Производительность сервера: симптомы»](references/domains-server.md#9-производительность-сервера-симптомы) |
| Как разложить проект на модули и закрепить границы | [architecture.md → «Короткие правила по умолчанию»](references/architecture.md#короткие-правила-по-умолчанию) |
| Существует ли книга или издание | [books.md → «Что вышло, что в раннем доступе и чего не существует»](references/books.md#что-вышло-что-в-раннем-доступе-и-чего-не-существует) |
| Minecraft: с чего начать мод, готовый проект с GameTest и CI | [minecraft-modding.md → «Шаблоны»](references/minecraft-modding.md#шаблоны), выбор загрузчика — [«Стек по умолчанию»](references/minecraft-modding.md#стек-по-умолчанию) |
| Minecraft: старый туториал или мод не собирается на 26.x (`modImplementation`, `remapJar`, `ResourceLocation`, Yarn) | [minecraft-modding.md → «Что в старых туториалах неверно»](references/minecraft-modding.md#что-в-старых-туториалах-неверно), [minecraft-mod-api.md → «Что сломалось в 26.x»](references/minecraft-mod-api.md#что-сломалось-в-26x) |
| Minecraft: сервер лагает (MSPT, TPS), как найти причину и доказать, что исправление помогло | [minecraft-performance-testing.md → «Симптом → техника → источник»](references/minecraft-performance-testing.md#симптом--техника--источник), порядок работы — [pipeline.md → «Моды и плагины Minecraft»](references/pipeline.md#моды-и-плагины-minecraft) |
| Minecraft: датапак, плагин или мод; плагин Paper с нуля | [minecraft-server-plugins.md → «Что выбрать: датапак, плагин или мод»](references/minecraft-server-plugins.md#что-выбрать-датапак-плагин-или-мод), [«Шаблоны»](references/minecraft-server-plugins.md#шаблоны) |

Скрипт [scripts/BenchCompare.java](scripts/BenchCompare.java) сравнивает прогоны JMH «до» и «после» по их JSON с учётом шума между прогонами и возвращает код 1 при регрессии. Запуск без сборки: `java BenchCompare.java <до> <после>` на JDK 17+. Он нужен на этапе 11 пайплайна.

Для Minecraft есть ещё два скрипта:

- [scripts/tick_sprint_bench.sh](scripts/tick_sprint_bench.sh) — бенчмарк сервера целиком командой `/tick sprint`. Результат пишется в формате JMH JSON, поэтому его сравнивает тот же `BenchCompare.java`;
- [scripts/EmptyStructure.java](scripts/EmptyStructure.java) — пустая структура `.nbt` для GameTest. Нужна на NeoForge, где своей пустой структуры нет.

Скрипты лежат в папке `scripts/` рядом с этим `SKILL.md`. При установке для всех проектов это `~/.claude/skills/java-knowledge/scripts/`, при установке в проект — `.claude/skills/java-knowledge/scripts/`. Подставляйте этот путь в команды из справочников.

## Как работать

**Для задачи больше одного метода** идите по [пайплайну](references/pipeline.md):

1. Опрос.
2. План и спецификация.
3. Подзадачи.
4. Тесты до кода.
5. Изоляция в worktree.
6. Бенчмарки.
7. Базовый замер и профиль.
8. Ревью.
9. Поиск решений в библиотеке.
10. Оптимизация.
11. Повторный замер.

Для мелкой правки хватает короткого режима из того же файла: тест, код, проверки, ревью.

**Перед тем как объявить работу готовой**, прогоните минимальные проверки и покажите их вывод:

```bash
./mvnw -B -ntp verify                 # компиляция с -Xlint -Werror, Error Prone и NullAway, тесты, Spotless, порог JaCoCo
./mvnw -B -ntp dependency:analyze
osv-scanner scan source -r .
```

На Gradle: `./gradlew build`, `./gradlew buildHealth`, `osv-scanner scan source -r .`. Анализаторы и пороги должны быть привязаны к сборке — шаблоны в [toolchain.md](references/toolchain.md#шаблоны). Если в проекте их нет, прогоните то, что есть, и скажите, каких проверок не хватает. Если инструмент не установлен, предложите команду установки из [toolchain.md](references/toolchain.md), а не пропускайте шаг молча.

**При оптимизации:**

- Сначала замер, потом правка. Без базовой линии нельзя сказать, что код стал быстрее.
- Мерьте через JMH, а не через `System.nanoTime()` в цикле: JIT выкидывает неиспользуемую работу. В локальном замере вычисление с выброшенным результатом выглядело в 14 раз дешевле настоящего.
- Снимайте 5 прогонов «до» и «после» отдельными запусками JVM, чередуя порядок (ABBA). Запуски одного и того же jar расходятся на 12–22%.
- Всегда добавляйте `-prof gc` и смотрите `gc.alloc.rate.norm` (байт на операцию): эта метрика почти не шумит.
- Запускайте только один JMH на машине за раз и не грузите машину другой работой во время замера.
- Ищите технику по симптому в [performance.md](references/performance.md) и указывайте источник рядом с правкой.
- Принимайте правку, только если бенчмарк показал значимый выигрыш, а тесты остались зелёными.

**При ревью** используйте чеклисты:

- [idioms-patterns.md](references/idioms-patterns.md) — для любого кода;
- [testing.md](references/testing.md) — для тестов;
- [concurrency-native.md](references/concurrency-native.md) — для многопоточного кода, виртуальных потоков и нативного кода;
- [domains-server.md](references/domains-server.md) — для серверного кода, базы данных и безопасности;
- [domains-client.md](references/domains-client.md) — для публикуемых библиотек;
- [architecture.md](references/architecture.md) — для структуры проекта и границ модулей.

## Правила использования библиотеки

- **Ссылайтесь на источник.** Рекомендуя паттерн или оптимизацию, давайте ссылку из справочника. Тогда пользователь может проверить совет, а решение не выглядит выдуманным.
- **Учитывайте пометки.** `[устарело]` значит, что у материала есть замена, и она указана рядом. `[не проверено]` значит, что утверждение взято из одного вторичного источника; его стоит перепроверить, прежде чем строить на нём решение.
- **Проверяйте версии.** Версии указаны на ноябрь 2026. В 2025–2026 годах вышло много мажорных версий с ломающими изменениями: Spring Boot 4 и Spring Framework 7 (переименованные стартеры, удалённые API), Jackson 3 (пакеты `tools.jackson`), Hibernate 7, JUnit 6, Testcontainers 2 (новые артефакты и пакеты), google-java-format 2. Публикация через OSSRH закрыта с 30 июня 2025 года. Перед важными решениями сверяйтесь с Maven Central, документацией и release notes.
- **Учитывайте целевую версию Java.** Уточните `maven.compiler.release` или toolchain в Gradle, прежде чем советовать новые возможности: scoped values, module imports и компактные исходные файлы есть только с JDK 25, структурная конкурентность станет стабильной только в JDK 28, а до этого её API меняется от релиза к релизу. Не предлагайте preview-возможности (`--enable-preview`) для продакшена без явного согласия. Таблица по релизам — в [idioms-patterns.md](references/idioms-patterns.md).
- **Говорите о пробелах прямо.** Если в библиотеке нет ответа, так и скажите, а затем ищите в официальной документации. Не выдавайте догадку за материал библиотеки и не выдумывайте книги и издания: список того, чего не существует, есть в [books.md](references/books.md).
- **Следуйте соглашениям проекта.** Если в проекте уже приняты стиль, инструмент сборки, фреймворк, библиотеки или архитектура, советы библиотеки применяются в их рамках, а не поверх.
