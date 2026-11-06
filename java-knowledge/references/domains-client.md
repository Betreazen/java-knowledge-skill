# Java вне сервера: библиотеки, CLI, десктоп, данные, ML, игры, устройства, браузер

Справочник по областям Java за пределами серверных веб-приложений: библиотеки для Maven Central (публикация, совместимость API и бинарников, JPMS, Javadoc); CLI и TUI; старт и упаковка (CDS, AOT-кеш, GraalVM Native Image, jlink, jpackage, JReleaser); десктоп (JavaFX, Swing, SWT, Compose Multiplatform); Android (только ориентиры); данные и big data; ноутбуки и DataFrame; численные расчёты и GPU; ML, ИИ и LLM; игры всех жанров и типов движков; embedded и IoT; WebAssembly и браузер; interop с другими языками; скрипты и автоматизация. Актуально на ноябрь 2026: JDK 25 — текущая LTS (сентябрь 2025), JDK 27 — последний релиз (GA 2026-09-15, не LTS), JDK 26 вышел 2026-03-17. Для библиотек база 17 — консервативный минимум, 21 — частый выбор, 25 — для новых приложений. Ключевые версии темы: central-publishing-maven-plugin 0.12.0, japicmp 0.27.0, JReleaser 1.27.0, picocli 4.7.7, JLine 4.5.0, GraalVM 25.5.5.0.3, JavaFX 27.0.1, FlatLaf 3.8, Spark 4.3.0, Flink 2.3.0, DuckDB JDBC 2.0.0.1, LangChain4j 1.23.0, Spring AI 2.0.2, MCP Java SDK 2.1.0, libGDX 1.14.2, Pi4J 4.0.2, TeaVM 0.16.0. Версии взяты из `maven-metadata.xml` Maven Central и GitHub API на 6 ноября 2026. Пометки: `[не проверено]` — факт из одного вторичного источника или страница не читалась; `[устарело]` — рядом названа замена. Без пометки — источник открыт и прочитан либо проверен по метаданным. Сервер, фреймворки, базы данных и контейнеры — в [domains-server.md](domains-server.md); сборка, JDK и менеджеры версий — в [toolchain.md](toolchain.md); тесты — в [testing.md](testing.md); JMH, JFR и GC — в [performance.md](performance.md); FFM, JNI и виртуальные потоки — в [concurrency-native.md](concurrency-native.md); records, sealed и паттерны — в [idioms-patterns.md](idioms-patterns.md); модули и границы — в [architecture.md](architecture.md); процесс разработки — в [pipeline.md](pipeline.md); книги — в [books.md](books.md).

- [Стек по умолчанию по областям](#стек-по-умолчанию-по-областям)
- [Версии на ноябрь 2026](#версии-на-ноябрь-2026)
- [1. Библиотеки для других: публикация, дизайн API, совместимость](#1-библиотеки-для-других-публикация-дизайн-api-совместимость)
- [2. CLI: разбор аргументов, терминал, старт, упаковка](#2-cli-разбор-аргументов-терминал-старт-упаковка)
- [3. Терминальные интерфейсы (TUI)](#3-терминальные-интерфейсы-tui)
- [4. Десктоп и GUI](#4-десктоп-и-gui)
- [5. Android (только ориентиры)](#5-android-только-ориентиры)
- [6. Данные и большие данные](#6-данные-и-большие-данные)
- [7. Ноутбуки, DataFrame и разведочный анализ](#7-ноутбуки-dataframe-и-разведочный-анализ)
- [8. Научные и численные расчёты, GPU](#8-научные-и-численные-расчёты-gpu)
- [9. ML, ИИ и LLM-приложения](#9-ml-ии-и-llm-приложения)
- [10. Игры: жанры и типы движков](#10-игры-жанры-и-типы-движков)
- [11. Embedded и IoT](#11-embedded-и-iot)
- [12. WebAssembly и браузер](#12-webassembly-и-браузер)
- [13. Interop: другие языки JVM, polyglot, Python и нативный код](#13-interop-другие-языки-jvm-polyglot-python-и-нативный-код)
- [14. Скрипты и автоматизация](#14-скрипты-и-автоматизация)
- [Заброшенные и затихшие проекты](#заброшенные-и-затихшие-проекты)
- [Чеклист ревью публикуемой библиотеки](#чеклист-ревью-публикуемой-библиотеки)
- [Непроверенные места и список наблюдения](#непроверенные-места-и-список-наблюдения)

---

## Стек по умолчанию по областям

Выбор на конец 2026 года. Колонка «не брать в новый код» — устаревшее или заброшенное; подробности и замены — в разделе [Заброшенные и затихшие проекты](#заброшенные-и-затихшие-проекты).

| Область | По умолчанию | Альтернатива | Не брать в новый код |
|---|---|---|---|
| Публикация библиотеки (Maven) | central-publishing-maven-plugin 0.12.0 + maven-gpg-plugin + sources и javadoc JAR | JReleaser (`deploy`) | nexus-staging-maven-plugin, адреса OSSRH |
| Публикация библиотеки (Gradle) | vanniktech maven-publish 0.37.0 | nmcp (агрегация модулей), JReleaser | gradle-nexus publish-plugin против OSSRH |
| Контроль совместимости API | japicmp 0.27.0 в фазе `verify` | Revapi | только ручное ревью |
| Nullness в публичном API | JSpecify 1.0 (`@NullMarked`) | Checker Framework | JSR-305 `javax.annotation` (мёртвый JSR, split-пакеты) |
| Разбор аргументов CLI | picocli 4.7.7 + picocli-codegen | Æsh, Airline | JCommander, args4j (давно без релизов) `[не проверено]` |
| Интерактивный терминал | JLine 4.5.0 | `java.io.Console` | JLine 2, схемы только на Jansi |
| TUI | TamboUI 0.6.0 (экспериментальный) | Lanterna 3.1.5, TUI4J | обёртки над curses через JNI |
| Быстрый старт CLI | AOT-кеш (JDK 25+, `-XX:AOTCacheOutput`) | GraalVM Native Image 25.x | толстые JAR с DI на рефлексии |
| Поставка десктоп- или CLI-приложения | jlink + jpackage (badass-jlink) + JReleaser | Conveyor, install4j | просьба к пользователю поставить JDK |
| Десктоп UI (новый) | JavaFX 27 на JDK 25 (или JavaFX 25 LTS) + AtlantaFX | Swing + FlatLaf 3.8; Compose Multiplatform (Kotlin) | апплеты, Web Start, UI только на AWT |
| Десктоп UI (экосистема Eclipse) | SWT / Eclipse RCP | — | — |
| Android | Kotlin + Compose; уровень языка Java 17 с desugaring | — | новые UI-проекты только на Java |
| Пакетная обработка больших данных | Spark 4.3 (DataFrame и SQL) | Beam 2.77 на Dataflow или Flink | код на RDD, Spark 2.x и Scala 2.12 |
| Потоковая обработка | Flink 2.3 или Kafka Streams 4.4 | Beam | Flink DataSet API, SourceFunction |
| Встроенная аналитика | DuckDB JDBC 2.0 | Arrow Java + DataFusion | Spark в local-режиме для малых данных |
| DataFrame и ноутбуки | DFLib + JJava; Kotlin Notebook | Tablesaw, Smile | IJava, BeakerX |
| Линейная алгебра | EJML (малые и средние матрицы), ojAlgo (оптимизация) | Hipparchus | Commons Math 3 и 4-beta, ND4J |
| GPU | TornadoVM 8.1 | JavaCPP-пресеты CUDA; Babylon HAT (только исследование) | Aparapi |
| LLM-приложение (Spring) | Spring AI 2.0.x | стартер LangChain4j для Spring Boot | Spring AI 1.x в новом коде |
| LLM-приложение (Quarkus или чистая Java) | LangChain4j 1.23 / Quarkus LangChain4j | SDK вендора напрямую | — |
| Один LLM-вендор | официальный SDK (anthropic-java 3.6, openai-java 4.101, google-genai 2.0) | — | сторонние обёртки над официальными API |
| MCP-сервер и клиент | MCP Java SDK 2.1 (или Spring AI, Quarkus MCP) | — | артефакты `mcp-spring-*` версии 1.x |
| Инференс в процессе | ONNX Runtime Java 1.31 | DJL 0.40 | Deeplearning4j, Jlama |
| Классический ML | Tribuo (Apache-2.0) | Smile (GPLv3 или коммерческая лицензия, см. §9), XGBoost4J | Weka в продакшене |
| Векторный поиск | клиент PGVector, Qdrant или Milvus; Lucene и JVector внутри процесса | OpenSearch k-NN | — |
| 2D и 3D игры, кроссплатформенные | libGDX 1.14 | jMonkeyEngine 3.9 (3D), FXGL (2D, десктоп), LITIENGINE (2D с редактором карт) | mini2Dx, Slick2D `[не проверено]` |
| Свой движок | LWJGL 3.4 + JOML + imgui-java | — | JOGL `[не проверено]` |
| Моддинг Minecraft | Fabric или NeoForge на Java 25 | плагины Paper (сервер) | туториалы эпохи Forge 1.12 |
| GPIO на Raspberry Pi | Pi4J 4 (провайдер FFM, JDK 25) | diozero | Pi4J v1 (WiringPi), sysfs GPIO, запуск от root |
| MQTT | HiveMQ MQTT Client | Paho (давно без релизов, но работает) | — |
| Смарт-карты | Java Card 3.2 + JCDK 26.0 + ant-javacard + jCardSim | — | — |
| Java в браузере (новый код) | TeaVM (Wasm GC или JS) | J2CL (команды на Bazel) | GWT для новых приложений |
| Старый Swing или апплет в браузере | CheerpJ 5 | — | браузерный плагин Java (удалён) |
| Wasm внутри JVM | Chicory | GraalWasm | wasmtime-java |
| Python внутри Java | GraalPy (чистый Python) / Jep (CPython + NumPy) | JPype (Java из Python) | Jython (только Python 2) |
| JS внутри Java | GraalJS | Javet (V8), Rhino | Nashorn из JDK (удалён в 15) |
| Скрипты | JBang; `java Script.java` без зависимостей | JShell, Groovy 6 | bash-обёртки вокруг `javac` |

## Версии на ноябрь 2026

Проверено 6 ноября 2026 по `maven-metadata.xml` (поле `<release>` и список стабильных версий) и GitHub REST API (`releases/latest`, `pushed_at`, `archived`). Факты о JDK — со страниц openjdk.org.

| Что | Версия или состояние | Дата | Примечание |
|---|---|---|---|
| JDK | **27** (не LTS); LTS **25** | 2026-09-15 / 2025-09-16 | В JDK 27: G1 по умолчанию везде (JEP 523), compact object headers по умолчанию (JEP 534), Vector API — 12-й инкубатор (JEP 537), lazy constants — 3-й preview (JEP 531) |
| JDK 26 | GA | 2026-03-17 | JEP 516: AOT-кеш объектов с любым GC; JEP 504: удалён Applet API; JEP 500: предупреждения «final означает final» |
| central-publishing-maven-plugin | 0.12.0 | 2026-10-13 | официальный плагин Sonatype для Central Portal |
| vanniktech gradle-maven-publish-plugin | 0.37.0 | 2026-06-21 | самый распространённый путь в Central из Gradle |
| GradleUp nmcp | 1.6.2 | 2026-08-31 | агрегирующий плагин Gradle для Central Portal |
| JReleaser | 1.27.0 | 2026-10-31 | релиз, деплой в Central, Homebrew, Scoop, Winget и другие |
| japicmp | 0.27.0 | 2026-11-04 | проверка бинарной и исходной совместимости |
| Revapi (Maven-плагин) | 0.15.1 | 2025-04-15 | репозиторий обновлялся в 2025-11; темп упал |
| maven-shade-plugin / Shadow (GradleUp) | 3.6.2 / 9.6.1 | 2026-03 / 2026-07 | id плагина `com.gradleup.shadow`; старый `com.github.johnrengelman.shadow` `[устарело]` |
| maven-javadoc-plugin | 3.12.0 | 2025-09 | |
| JSpecify | 1.0.1 | 2026-07 | стандартные аннотации nullness для библиотек |
| picocli | 4.7.7 | **2025-04-19** | 18 месяцев без релиза; репозиторий обновлялся в 2026-08; всё ещё выбор по умолчанию |
| JLine | **4.5.0** | 2026-11-04 | JLine 4 требует Java 11+; бандл `org.jline:jline` |
| Æsh / Airline / JCommander | 3.18.6 / 3.3.0 / 1.82 | 2026-10 / 2026-07 / **2022-01** | JCommander давно без релизов |
| TamboUI | 0.6.0 | 2026-10-12 | новый (2025–26) TUI в стиле Ratatui; до 1.0 |
| TUI4J | 0.3.5 | 2026-10-23 | порт Bubble Tea; до 1.0 `[не проверено]` |
| Lanterna | 3.1.5 стабильная; 3.2.0-alpha1 | 2026-03 | медленно, но жива |
| JBang | **0.143.0** (GitHub) | 2026-10-30 | артефакт `dev.jbang:jbang-cli` в Maven отстаёт (0.132.1) |
| GraalVM (CE и Oracle) | **25.5.5.0.3**, feature-релиз | 2026-11-03 | ежемесячные релизы «25.x» на базе JDK 25; GraalVM на базе JDK 27 не запланирован |
| native-maven-plugin (graalvm buildtools) | 1.1.18 | 2026-10-29 | |
| OpenJFX | **27.0.1** GA; 28-ea | 2026-09 | **JavaFX 27 требует JDK 25+** (`--release 25`); на macOS по умолчанию Metal; Gluon 25.x LTS (25.0.4) для старых стеков |
| Gluon: плагин GluonFX / Attach / Scene Builder | 1.0.29 / 4.0.26 / 26.0.0 | 2026-06 / 2026-10 / 2026-04 | |
| AtlantaFX / ControlsFX / Ikonli | 3.0.0 / 11.2.6 / 12.4.0 | 2026-09 / 2026-10 / 2025-04 | |
| FXGL | 25.0.1 | 2026-02-06 | игровая библиотека на JavaFX |
| FlatLaf | **3.8** | 2026-11-04 | современный look and feel для Swing |
| SWT | 3.135.0 (Eclipse 4.37) | 2026-09 | |
| Compose Multiplatform | 1.12.2; 1.13.0-beta02 | 2026-10-15 | только Kotlin |
| badass-jlink / badass-runtime | 4.2.0 / 2.0.1 | 2026-10-30 / 2025-11-06 | |
| Apache Spark | **4.3.0** | 2026-10-30 | только Scala 2.13; Java 17, 21, 25 (25 — с 25.0.3); Python 3.11+ |
| Apache Flink | 2.3.0 | 2026-06-19 | Flink 2.0 убрал DataSet API, Scala API, SourceFunction и SinkFunction |
| Kafka (клиенты и Streams) | 4.4.0 | 2026-10-20 | |
| Apache Beam (Java SDK) | 2.77.0 | 2026-10-07 | |
| Apache Arrow Java | 19.0.0 | 2026-03-16 | Arrow Java выходит отдельно от C++ (репозиторий arrow-java) `[не проверено]` |
| Parquet Java | 1.18.1 | 2026-09-04 | |
| Apache Iceberg / Delta Kernel | 1.12.0 / 4.4.1 | 2026-09-30 / 2026-10-05 | |
| DuckDB JDBC | **2.0.0.1** | 2026-10-31 | DuckDB 2.0 |
| Trino JDBC | 484 | 2026-10-16 | |
| Tablesaw | 0.44.4 | 2026-01 на GitHub / 2025-06 в Central | медленно |
| DFLib | 2.0.0-M7 (стабильная 1.3.0) | 2026-08-23 | |
| JJava (Jupyter-ядро, форк IJava от DFLib) | 1.0-a8 | 2026-08-19 | IJava `[устарело]`: последний релиз 2019 |
| Kotlin Jupyter kernel | 0.19.0-958 | 2026-07 | |
| Smile | 7.1.0 | 2026-10-19 | ML + DataFrame + графики; **GPLv3 или коммерческая лицензия** |
| EJML / ojAlgo / Hipparchus | 0.46.1 / 57.6.2 / 4.0.3 | 2026-07 / 2026-10 / 2026-02 | |
| Commons Math | 3.6.1 (2016); 4.0-beta1 (2022) | — | давно без релизов; разделён на Commons Numbers, RNG, Statistics, Geometry (1.3 / 1.7 / 1.3, 2026) |
| JavaCPP / JavaCV | 1.5.14 | 2026-08-10 | |
| ND4J / Deeplearning4j | 1.0.0-M2.1 | **2022-08** | четыре года без релиза в Central; репозиторий ещё обновлялся в 2026-10 |
| TornadoVM | **8.1.0** (сборки `-jdk21` и `-jdk22plus`) | 2026-11-06 | `io.github.beehive-lab:tornado-api` |
| Project Babylon / HAT | проект OpenJDK, JEP пока нет | — | статьи про GPU от 2026-01 и 2026-05 |
| DJL | **0.40.0** | 2026-10-30 | |
| ONNX Runtime Java | 1.31.1 | 2026-10-26 | |
| Tribuo | 4.3.2 | 2025-04-04 | репозиторий живой, релизы редкие |
| LangChain4j | **1.23.0** | 2026-10-28 | |
| Spring AI | **2.0.2** GA; 2.1.0-RC2 | 2026-10-23 | 2.x работает на Spring Boot 4 |
| Quarkus LangChain4j | 1.14.x; 2.0.0.Beta2 | 2026-11-01 | |
| MCP Java SDK | **2.1.0**; 2.2.0-M2 | 2026-10-16 | `io.modelcontextprotocol.sdk:mcp` |
| Anthropic Java SDK | **3.6.0** | 2026-11-05 | `com.anthropic:anthropic-java` |
| OpenAI Java SDK | 4.101.0 | 2026-11-05 | `com.openai:openai-java` |
| Google Gen AI Java SDK / ADK Java | 2.0.0 / 1.14.0 | 2026-11 | |
| Embabel agent framework | 1.5.4 | 2026-10-28 | JVM-фреймворк агентов от Рода Джонсона |
| Jlama | 0.8.4 | **2025-01-01** | давно без релизов; репозиторий обновлялся в 2025-10 |
| Клиенты векторных БД: Qdrant / Milvus / Weaviate v6 / JVector | 1.20.0 / 3.0.12 / 6.3.2 / 4.0.2 | 2026-10 | Lucene core 10.6.0 |
| libGDX | 1.14.2 | 2026-06-05 | |
| jMonkeyEngine | 3.9.0-stable; 3.10.0-beta3 | 2026-01-20 / 2026-10-31 | |
| LWJGL | 3.4.3 | 2026-08-23 | |
| JOML / imgui-java | 1.10.9 / 1.92.7.1 | 2026-06 / 2026-07 | |
| LITIENGINE | 0.12.0 | 2026-06 | 2D-движок |
| Fabric Loader | 0.20.1 | 2026-10-19 | моддинг Minecraft |
| Pi4J | 4.0.2 | 2026-06-08 | построен на Java 25; провайдер FFM заменяет JNI |
| diozero | 1.4.1 | 2024-10 | темп упал |
| jSerialComm / HiveMQ MQTT client / Eclipse Milo | 2.11.4 / 1.4.2 / 1.1.8 (1.2.0-RC2) | 2025-11 / 2026-11 / 2026 | клиент Paho MQTTv5 Java 1.2.5 — от **2020** года |
| TeaVM | 0.16.0 | 2026-10-02 | |
| J2CL | v20261026 (теги по датам) | 2026-10-26 | |
| GraalPy / GraalJS / GraalWasm (polyglot-артефакты) | 25.5.5.0.3 | 2026-11-03 | `org.graalvm.polyglot:{python,js,wasm}` |
| Jep (CPython внутри JVM) | 4.3.2 | 2026-08-15 | |
| Kotlin / Scala 3 / Groovy | 2.4.21 (2.5.0-Beta2) / 3.10.0 / **6.0.0** | 2026 | Groovy 6.0.0 GA — 2026-09 |
| JNA | 5.19.1 | 2026-06 | |

---

## 1. Библиотеки для других: публикация, дизайн API, совместимость

**Ключевые выводы**

- **OSSRH закрыт.** Sonatype OSSRH (`oss.sonatype.org`, `s01.oss.sonatype.org`, Nexus 2 staging) прекратил работу **2025-06-30**; все пространства имён переехали в **Central Publisher Portal** (`central.sonatype.com`). Любой гайд с `nexus-staging-maven-plugin`, адресами `oss.sonatype.org` или `io.github.gradle-nexus.publish-plugin` против OSSRH `[устарело]`. Для старых инструментов есть совместимый слой **OSSRH Staging API** в Portal, но он покрывает лишь часть вызовов Nexus 2.
- **Путь через Maven:** `org.sonatype.central:central-publishing-maven-plugin` **0.12.0** с `<extensions>true</extensions>`. `publishingServerId` по умолчанию `central`; `autoPublish` по умолчанию **false** (деплой ждёт ручного клика в Portal), для CI ставьте `true`; `waitUntil` — `validated` (по умолчанию), `uploaded` или `published`. Учётные данные — **user token** Portal в `settings.xml` (`<server id="central">`), а не пароль от входа. Публикация SNAPSHOT поддерживается с 0.7.0.
- **Плагин не собирает бандл за вас.** Нужны `-sources.jar`, `-javadoc.jar` (maven-source-plugin и maven-javadoc-plugin) и подписи `.asc` (maven-gpg-plugin 3.2.8 или JReleaser). Контрольные суммы плагин считает сам.
- **В POM обязательны:** name, description, url, минимум одна лицензия, минимум один developer, scm (connection, developerConnection, url). Релизная версия не может быть `-SNAPSHOT`. groupId должен быть подтверждённым пространством имён: обратный домен через DNS TXT-запись или `io.github.<user>` через GitHub.
- **Путь через Gradle:** официального плагина Sonatype для Gradle **по-прежнему нет**. Реальные варианты: **vanniktech `com.vanniktech.maven.publish` 0.37.0** (`mavenPublishing { publishToMavenCentral(automaticRelease = true); signAllPublications() }`, переменные CI `ORG_GRADLE_PROJECT_mavenCentralUsername`, `ORG_GRADLE_PROJECT_mavenCentralPassword`, `ORG_GRADLE_PROJECT_signingInMemoryKey`); **GradleUp nmcp** для сборки многих модулей в один деплой; JReleaser.
- **JReleaser** (1.27.0) не привязан к системе сборки: сборка кладёт артефакты в `target/staging-deploy` (или `build/staging-deploy`), затем `jreleaser:deploy` проверяет правила POM, наличие sources и javadoc JAR, считает суммы, подписывает и загружает через Publisher API. Тот же инструмент публикует релизы GitHub, Homebrew, Scoop, Winget, Chocolatey, SDKMAN!, Docker и другое (см. §2).
- **Sigstore:** Portal с 2025-01 проверяет необязательные бандлы `*.sigstore.json`, но PGP-подпись по-прежнему **обязательна**. Закладывайте, что Sigstore станет обязательным позже.
- **Старый релиз как цель, новый JDK как компилятор:** используйте `--release N` (`maven.compiler.release` или toolchains Gradle плюс `options.release`), не `-source`/`-target` по отдельности. `--release` ещё и проверяет использование API JDK, поэтому animal-sniffer (1.29) нужен только для сигнатур вне JDK, например Android. Нижняя граница для библиотек в конце 2026: **Java 17** для широкого охвата, **21**, если в API нужны виртуальные потоки или pattern matching.
- **Бинарная совместимость не равна совместимости исходников.** Смена типа результата, расширение параметра `int` до `long`, новый абстрактный метод в интерфейсе (вместо `default`), `final` на классе, смена `static final` константы (javac встраивает её в клиентов) и смена generic-сигнатуры ломают уже скомпилированных клиентов. Авторитет — глава 13 JLS. Закрывайте релиз проверкой **japicmp** (0.27.0; цель `cmp`, `oldVersion` берётся из репозитория; `--semantic-versioning` подсказывает, какую часть semver повышать) или **Revapi** (гибче в настройке, но релизы замедлились: последний в 2025-04).
- **Правила дизайна API в Java:** минимальная публичная поверхность (package-private по умолчанию, `final` классы, sealed-иерархии для явных точек расширения); возвращайте интерфейсы (`List`, не `ArrayList`); не выставляйте типы сторонних библиотек (Guava, Jackson) в сигнатурах, если это не цель библиотеки; статические фабрики и билдеры вместо телескопических конструкторов; `record` для неизменяемых значений, но records трудно развивать (новый компонент меняет канонический конструктор и набор аксессоров); nullness документируйте через **JSpecify** 1.0 (`@NullMarked` на пакете или модуле, `@Nullable` где нужно), чтобы Kotlin, NullAway и Error Prone проверяли использование.
- **JPMS для библиотек:** минимум — `Automatic-Module-Name` в манифесте, чтобы у пользователей модулей было стабильное имя. Лучше — настоящий `module-info.java`: `exports` только для пакетов API, `requires transitive` для типов, видимых в API, `requires static` для необязательных зависимостей. Имя модуля совпадает с корневым пакетом. Если цель всё ещё Java 8, кладите `module-info.class` в `META-INF/versions/9` (multi-release JAR) или используйте **Moditect** (1.3.0.Final). Split-пакеты в разных JAR на module path запрещены.
- **Multi-release JAR (JEP 238):** `Multi-Release: true` и `META-INF/versions/N/...` позволяют одному JAR нести, например, реализацию на FFM или виртуальных потоках для 21+ рядом с запасной. Подводные камни: публичный API обязан совпадать во всех версиях; shading и часть инструментов (старый Shadow, конфиги ProGuard, classpath в IDE) склеивают или игнорируют версионные записи; каждый вариант тестируйте на своём JDK.
- **Зависимости:** каждая транзитивная зависимость — будущий конфликт у ваших пользователей. Предпочитайте JDK (`java.net.http`, `java.util.concurrent`, records, `HexFormat`, `System.Logger`) вместо Guava и Commons; логируйте **только через SLF4J API** (binding никогда не поставляйте); необязательные интеграции помечайте `<optional>true</optional>` или feature variants Gradle; для многомодульных библиотек публикуйте BOM.
- **Подводные камни shading:** shading (maven-shade-plugin 3.6.2; Shadow 9.6.1 с id `com.gradleup.shadow`, старый `com.github.johnrengelman.shadow` `[устарело]`) без **relocation** даёт дубли классов на classpath пользователей. Перенесённые копии ломают файлы `ServiceLoader`, если не включить `ServicesResourceTransformer` или `mergeServiceFiles()`; ломают JPMS (`module-info` нельзя переместить); раздувают JAR; прячут CVE от сканеров; оставляют shaded-зависимости в POM, если не включён `createDependencyReducedPom`. Shade нужен только для реальных конфликтов и fat JAR у CLI, не по умолчанию в библиотеке.
- **Gradle Module Metadata** (файл `.module` рядом с POM) несёт variants, capabilities, feature variants и rich versions; потребители на Maven видят урезанный POM (capabilities не публикуются, feature variants становятся необязательными зависимостями). Оставляйте включённой, пока репозиторий не отвергает. Для контроля, что попадает на compile classpath потребителя, используйте `api` и `implementation` (плагин java-library).
- **Javadoc в Markdown (JEP 467, JDK 23+):** комментарии `///` в CommonMark, ссылки `[java.util.List]` и `[text][String#chars()]`, таблицы GFM, все теги `@` работают. Подводные камни: пустая строка или строка без `///` разрывает комментарий, и остаётся только последний фрагмент; незакрытые обратные кавычки ломают разбор молча; для рендера нужен `javadoc` из JDK 23+, но исходники по-прежнему компилируются с `--release 17`.
- **Семантическое версионирование:** major — бинарно несовместимое изменение, minor — расширение API, patch — исправления; до 1.0 «обещаний нет». Помечайте `@Deprecated(since = "x", forRemoval = true)` за один major вперёд; ведите `CHANGELOG.md`; для нестабильных API рассмотрите `@API(status = EXPERIMENTAL)` из apiguardian.
- **Воспроизводимые сборки:** `project.build.outputTimestamp` (Maven) или `isPreserveFileTimestamps = false` и `isReproducibleFileOrder = true` (Gradle); проверка — `mvn artifact:compare` или Reproducible Central.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [OSSRH sunset announcement](https://central.sonatype.org/news/20250326_ossrh_sunset/) | Sonatype, 2025 | EOL 2025-06-30, слой Staging API |
| [OSSRH EOL page](https://central.sonatype.org/pages/ossrh-eol/) | Sonatype, 2025 | подтверждение закрытия и миграции пространств имён |
| [Publishing with Maven (central-publishing-maven-plugin)](https://central.sonatype.org/publish/publish-portal-maven/) | Sonatype, 2026 | настройка плагина, autoPublish, waitUntil, snapshots |
| [Publishing with Gradle](https://central.sonatype.org/publish/publish-portal-gradle/) | Sonatype, 2026 | официального плагина нет; список плагинов сообщества |
| [Central requirements](https://central.sonatype.org/publish/requirements/) | Sonatype | метаданные POM, sources и javadoc, подписи |
| [GPG signing guide](https://central.sonatype.org/publish/requirements/gpg/) | Sonatype | создание и распространение ключа |
| [Namespace registration](https://central.sonatype.org/register/namespace/) | Sonatype | подтверждение через DNS и `io.github` |
| [Portal OSSRH Staging API](https://central.sonatype.org/publish/publish-portal-ossrh-staging-api/) | Sonatype | как оживить старые инструменты на Nexus 2 |
| [Sigstore validation on the Portal](https://central.sonatype.org/news/20250128_sigstore_signature_validation_via_portal/) | Sonatype, 2025 | необязательные бандлы Sigstore, PGP остаётся обязательным |
| [vanniktech plugin: Central Portal](https://vanniktech.github.io/gradle-maven-publish-plugin/central/) | Niklas Baudy, 2026 | DSL Gradle, переменные CI, подпись ключом из памяти |
| [vanniktech/gradle-maven-publish-plugin](https://github.com/vanniktech/gradle-maven-publish-plugin) | Niklas Baudy, 2026 | исходники, issues |
| [GradleUp/nmcp](https://github.com/GradleUp/nmcp) | GradleUp, 2026 | агрегированная загрузка многопроектных сборок |
| [Gradle forum: publishing to Maven Central in 2025 (OSSRH EOL)](https://discuss.gradle.org/t/publishing-to-maven-central-in-2025-ossrh-eol/50983) | сообщество Gradle, 2025 | обсуждение вариантов миграции `[не проверено]` |
| [JReleaser: Maven Central example](https://jreleaser.org/guide/latest/examples/maven/maven-central.html) | Andres Almiray, 2026 | поток через staging-каталог, подпись, проверка правил |
| [Migrating Gradle publishing to the Central Portal](https://www.endoflineblog.com/migrate-maven-central-publishing-to-central-portal-for-a-gradle-project) | End of Line blog, 2025 | разобранная миграция `[не проверено]` |
| [japicmp](https://siom79.github.io/japicmp/) | Martin Mois, 2026 | проверка бинарной и исходной совместимости, цель Maven |
| [Revapi](https://revapi.org/) | Lukas Krejci | настраиваемая проверка API |
| [JLS ch. 13: Binary Compatibility](https://docs.oracle.com/javase/specs/jls/se25/html/jls-13.html) | Oracle, 2025 | авторитетные правила |
| [JEP 467: Markdown documentation comments](https://openjdk.org/jeps/467) | Jonathan Gibbons, 2024 | синтаксис `///` и подводные камни |
| [JEP 238: Multi-Release JAR files](https://openjdk.org/jeps/238) | Paul Sandoz, 2015 | правила раскладки MRJAR |
| [JEP 261: Module System](https://openjdk.org/jeps/261) | Mark Reinhold | module path, автоматические модули |
| [Gradle Module Metadata](https://docs.gradle.org/current/userguide/publishing_gradle_module_metadata.html) | документация Gradle 9.8.2 | variants, деградация POM, отключение |
| [Gradle: Java Library plugin](https://docs.gradle.org/current/userguide/java_library_plugin.html) | Gradle | `api` против `implementation` |
| [Gradle: feature variants](https://docs.gradle.org/current/userguide/feature_variants.html) | Gradle | необязательные зависимости без костылей |
| [Maven: reproducible builds guide](https://maven.apache.org/guides/mini/guide-reproducible-builds.html) | Apache Maven | outputTimestamp |
| [maven-shade-plugin](https://maven.apache.org/plugins/maven-shade-plugin/) | Apache | relocation, трансформеры |
| [Shadow (GradleUp)](https://gradleup.com/shadow/) | GradleUp | fat JAR для Gradle, relocation |
| [Moditect](https://github.com/moditect/moditect) | Gunnar Morling | module-info для целей на Java 8 |
| [JSpecify](https://jspecify.dev/) | группа JSpecify, 1.0 в 2024 | стандартные аннотации nullness |
| [Semantic Versioning 2.0.0](https://semver.org/) | Tom Preston-Werner | контракт версионирования |
| [apiguardian](https://github.com/apiguardian-team/apiguardian) | команда JUnit | маркеры `@API(status = ...)` |
| [How to Design a Good API and Why it Matters (talk)](https://www.youtube.com/watch?v=aAb7hSCtvGw) | Joshua Bloch, 2007 | вечные правила дизайна API `[не проверено]` |
| [Reproducible Central](https://github.com/jvm-repo-rebuild/reproducible-central) | Hervé Boutemy | пересобирается ли релиз бит в бит |

## 2. CLI: разбор аргументов, терминал, старт, упаковка

**Ключевые выводы**

- **Стек по умолчанию:** **picocli** (4.7.7) для аргументов + процессор аннотаций `picocli-codegen` + при необходимости JLine для интерактивных оболочек. picocli-codegen проверяет аннотации при компиляции и пишет конфигурацию рефлексии, ресурсов и прокси для GraalVM в `META-INF/native-image/picocli-generated/`, так что нативной сборке обычно не нужна ручная конфигурация. Есть подкоманды, отрицаемые опции, `@`-файлы, ввод паролей, справка с ANSI и автодополнение для bash и zsh. Подвох: у picocli **нет релиза с 2025-04** (коммиты идут). Библиотека стабильна и повсеместна (Quarkus, Micronaut, JReleaser, JBang), но следите за преемственностью мейнтейнера. Альтернативы: **Æsh** 3.18.6 (Red Hat, ещё и фреймворк оболочек), **Airline** 3.3.0, **JCommander** 1.82 (давно без релизов, 2022), Apache Commons CLI (крошечный, императивный), jopt-simple (давно без релизов, 2018). Встроенные во фреймворки: расширение Quarkus Picocli, Micronaut Picocli, Spring Shell.
- **Ввод-вывод терминала:** **JLine 4** (4.5.0, 2026-11-04) требует Java 11+, поставляет настоящий `module-info` для основных модулей и имеет **провайдер терминала на FFM** (JDK 22+, нужен `--enable-native-access`); классификатор `jdk11` откатывается на провайдеры JNI и exec. Подходит для редактирования строки, истории, автодополнения, подсветки. Для простых вопросов хватает `java.io.Console` (в JDK 22+ доступен всегда, есть `isTerminal()`). Jansi (2.4.3) нужен только для ANSI в очень старых консолях Windows; Windows Terminal понимает ANSI сам.
- **Однофайловые и скриптовые CLI:** `java Main.java` запускает исходник напрямую (JEP 330); **многофайловые программы** (JEP 458, JDK 22) компилируют соседние файлы по требованию; **compact source files и instance main** (JEP 512, финальные в JDK 25) убирают церемонию класса и `static`, автоматически импортируют `java.base` и дают `IO.println`. Для зависимостей берите **JBang** (§14).
- **Старт — налог CLI.** Варианты от наименее до наиболее инвазивного: (1) **AppCDS**: `-XX:ArchiveClassesAtExit=app.jsa`, затем `-XX:SharedArchiveFile=app.jsa`; (2) **AOT-кеш (Project Leyden)**: JDK 24 JEP 483 (загрузка и линковка классов), JDK 25 JEP 514 (одношаговый тренировочный прогон `-XX:AOTCacheOutput=app.aot`) и JEP 515 (профили методов), JDK 26 JEP 516 (работает с любым GC); запуск с `-XX:AOTCache=app.aot`. Подвохи: одношаговому обучению нужна примерно вдвое большая куча, чем самому тренировочному прогону; кеш годится только для той же сборки JDK и совместимого classpath (только JAR, не каталоги); (3) **GraalVM Native Image**: старт за миллисекунды и малый RSS, но анализ закрытого мира требует метаданных достижимости для рефлексии, JNI, прокси и ресурсов; собирается под каждую ОС и архитектуру (кросс-компиляции нет); нужны gcc, glibc-devel, zlib или MSVC. Берите `native-maven-plugin` 1.1.18 или `org.graalvm.buildtools.native`, запускайте tracing agent (`-agentlib:native-image-agent`) на тестах и используйте репозиторий GraalVM Reachability Metadata.
- **Смена статуса GraalVM (важно для планов):** Oracle заявила, что GraalVM for JDK 24 — последняя версия, входящая в продукты Oracle Java SE; экспериментальный Graal JIT из Oracle JDK прекращён; GraalVM перешёл на именование «GraalVM 25.x» с **ежемесячными feature-релизами на базе JDK 25** (последний **25.5.5.0.3**, 2026-11-03) и сместил фокус на языки Graal (GraalPy, GraalJS). Календарь проверен; точная формулировка анонса Oracle — из вторичных источников `[не проверено]`. **GraalVM на базе JDK 27 не запланирован** до 2027-02. Native Image остаётся доступным (Community Edition, лицензия GFTC), но для старта в режиме JVM надёжнее долгосрочная ставка на AOT Leyden в OpenJDK.
- **Самодостаточная поставка без native image:** `jlink` собирает урезанный рантайм (`--add-modules`, `--strip-debug`, `--no-header-files`, `--no-man-pages`, `--compress zip-6`); с JDK 24 (JEP 493) jlink умеет работать без JMOD-файлов, если JDK так собран (часть вендоров так поставляет). `jdeps --print-module-deps` находит список модулей. **jpackage** упаковывает приложение и рантайм в `msi`/`exe` (нужен WiX; WiX 4 и 5 поддерживаются с JDK 24, WiX 3 работает; у WiX 5/6 были ошибки, см. JDK-8356592), `dmg`/`pkg` (флаги подписи и нотаризации), `deb`/`rpm` или каталог app-image. Подвохи: кросс-упаковки нет (собирайте под каждую ОС на этой ОС, например матрицей CI); немодульные приложения работают через `--main-jar`; в Gradle обычно берут **badass-jlink** 4.2.0 (модульные) или **badass-runtime** 2.0.1 (немодульные, тихий с 2025-11).
- **Распространение:** **JReleaser** (1.27.0) автоматизирует релизы GitHub и GitLab, changelog, суммы, подпись (PGP, cosign и Sigstore), SBOM и packagers: Homebrew, Scoop, Winget, Chocolatey, SDKMAN!, Snap, Flatpak, AppImage, Docker, каталоги jbang, а также Java-специфичные ассемблеры jlink и native-image. **Каталоги приложений JBang** позволяют запустить `jbang yourtool@yourorg` без установки. **Coursier** ставит JVM-приложения по координатам Maven. Fat JAR с лаунчером остаётся нормальным для внутренних инструментов.
- **Подвохи UX:** коды выхода (`CommandLine.execute` возвращает код, передайте его в `System.exit`), разделение stderr и stdout для конвейеров, переменная `NO_COLOR`, кодовая страница Windows (`-Dstdout.encoding=UTF-8`; JDK 18+ по умолчанию читает файлы в UTF-8 по JEP 400, но кодировка консоли следует за ОС), сигналы (`Runtime.addShutdownHook`), отказ от фреймворка логирования на старте (`System.Logger` или ничего).
- **Проекты-образцы:** JReleaser, JBang, Quarkus CLI, Spring Boot CLI, `jfr` и `jcmd` из JDK, Maven Daemon (`mvnd`, нативный клиент на GraalVM), Kestra и Camel JBang (picocli + JBang).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [picocli user manual](https://picocli.info/) | Remko Popma, 2025 | справочник по picocli |
| [picocli on GraalVM native image](https://picocli.info/picocli-on-graalvm.html) | Remko Popma | конфигурация codegen, нативная сборка |
| [remkop/picocli](https://github.com/remkop/picocli) | Remko Popma, 2026 | issues, релизы |
| [Æsh](https://github.com/aeshell/aesh) | Red Hat | фреймворк команд и оболочки |
| [Airline](https://github.com/rvesse/airline) | Rob Vesse | парсер CLI на аннотациях, релизы идут |
| [JLine 3/4 repo](https://github.com/jline/jline3) | Guillaume Nodet и др., 2026 | требования JLine 4, провайдер FFM |
| [JLine documentation](https://jline.org/) | JLine | чтение строки, completers, prompts |
| [JEP 458: Launch multi-file source-code programs](https://openjdk.org/jeps/458) | Ron Pressler, 2024 | `java Main.java` с несколькими файлами |
| [JEP 512: Compact source files and instance main methods](https://openjdk.org/jeps/512) | Ron Pressler / Gavin Bierman, 2025 | финальные в JDK 25 |
| [JEP 483: Ahead-of-Time class loading and linking](https://openjdk.org/jeps/483) | John Rose / Ioi Lam, 2024 | первый AOT-кеш Leyden |
| [JEP 514: AOT command-line ergonomics](https://openjdk.org/jeps/514) | OpenJDK, 2025 | одношаговый `-XX:AOTCacheOutput` |
| [JEP 515: AOT method profiling](https://openjdk.org/jeps/515) | OpenJDK, 2025 | прогрев по профилям из кеша |
| [JEP 516: AOT object caching with any GC](https://openjdk.org/jeps/516) | OpenJDK, 2025 | JDK 26 |
| [Project Leyden](https://openjdk.org/projects/leyden/) | OpenJDK | дорожная карта старта и прогрева |
| [JEP 493: Linking run-time images without JMODs](https://openjdk.org/jeps/493) | OpenJDK, 2024 | меньшие JDK с jlink |
| [jlink tool reference (JDK 25)](https://docs.oracle.com/en/java/javase/25/docs/specs/man/jlink.html) | Oracle | опции |
| [jpackage tool reference (JDK 25)](https://docs.oracle.com/en/java/javase/25/docs/specs/man/jpackage.html) | Oracle | все опции установщиков |
| [Packaging Tool User's Guide](https://docs.oracle.com/en/java/javase/25/jpackage/packaging-overview.html) | Oracle | подпись, нотаризация, лаунчеры |
| [JDK-8319457: jpackage WiX v4/v5](https://bugs.openjdk.org/browse/JDK-8319457) | OpenJDK | история поддержки WiX (JBS отдаёт ботам 403) `[не проверено]` |
| [JDK-8356592: jpackage with WiX 5/6](https://bugs.openjdk.org/browse/JDK-8356592) | OpenJDK | известная проблема `[не проверено]` |
| [badass-jlink plugin](https://github.com/beryx/badass-jlink-plugin) | Serban Iordache | jlink и jpackage для Gradle |
| [GraalVM Native Image docs](https://www.graalvm.org/latest/reference-manual/native-image/) | Oracle, 2026 | закрытый мир, метаданные, инструменты сборки |
| [GraalVM release calendar](https://www.graalvm.org/release-calendar/) | Oracle, 2026 | каденс 25.x, нет базы JDK 27 |
| [Native Build Tools (Maven/Gradle)](https://graalvm.github.io/native-build-tools/latest/index.html) | GraalVM | документация плагинов |
| [GraalVM reachability metadata repository](https://github.com/oracle/graalvm-reachability-metadata) | Oracle | общие метаданные для популярных библиотек |
| [Oracle shifts GraalVM focus away from Java](https://adtmag.com/articles/2025/09/30/oracle-shifts-graalvm-focus-away-from-java.aspx) | ADTmag, 2025 | контекст изменений 2025 года (ботам отдаёт 403) `[не проверено]` |
| [JReleaser guide](https://jreleaser.org/guide/latest/index.html) | Andres Almiray | релизы, packagers, ассемблеры |
| [JReleaser packagers reference](https://jreleaser.org/guide/latest/reference/packagers/index.html) | Andres Almiray | Homebrew, Scoop, Winget и другие |
| [Coursier install](https://get-coursier.io/docs/cli-install) | Coursier | установка JVM-приложений по координатам |
| [JEP 400: UTF-8 by default](https://openjdk.org/jeps/400) | Naoto Sato, 2022 | подвохи кодировок |
| [Building fast CLI apps with Java (Quarkus guide)](https://quarkus.io/guides/picocli) | Quarkus | picocli и native во фреймворке |

## 3. Терминальные интерфейсы (TUI)

**Ключевые выводы**

- **В 2025–26 появился реальный выбор.** **TamboUI** (0.6.0, MIT) начался как порт Rust-библиотеки Ratatui с идеями Bubble Tea. Модули: `tamboui-core`, `-widgets`, `-tui` (TuiRunner), `-toolkit` (декларативный DSL), `-picocli`; бэкенды JLine 3, Panama (FFM) и Æsh. Ядро работает на Java 8+, рекомендуется 17+; дружит с native-image; среди авторов Cédric Champeau, Max Rydahl Andersen и Claus Ibsen. Проект **явно экспериментальный, API могут меняться**.
- **TUI4J** (0.3.5) — порт в стиле Bubble Tea (архитектура Elm); до 1.0 `[не проверено]`.
- **Lanterna** (3.1.5; 3.2.0-alpha1 в 2026-03) — давний чисто Java-вариант со слоем виджетов в стиле Swing и терминалом, эмулируемым в Swing для запуска из IDE; развивается медленно, но жив.
- **Одного JLine 4** достаточно для prompts, меню и чекбоксов (старый модуль `console-ui` объявлен устаревшим в пользу нового API `prompt`).
- Подвохи: консоли Windows для raw-режима нужен бэкенд JLine или FFM; консоли запуска в IDE не настоящие терминалы (проверяйте в реальном терминале или на эмуляторе Lanterna); ширина широких символов и эмодзи; восстанавливайте состояние терминала в shutdown hook.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [TamboUI](https://github.com/tamboui/tamboui) | авторы TamboUI, 2026 | TUI для Java в стиле Ratatui |
| [TamboUI site](https://tamboui.dev/) | TamboUI, 2026 | документация и демо `[не проверено]` |
| [TUI4J](https://github.com/WilliamAGH/tui4j) | William Callahan, 2026 | TUI в стиле Bubble Tea `[не проверено]` |
| [Lanterna](https://github.com/mabe02/lanterna) | Martin Berglund | классический TUI на Java |
| [Ratatui](https://ratatui.rs/) | команда Ratatui | дизайн, которому следует TamboUI |

## 4. Десктоп и GUI

**Ключевые выводы**

- **JavaFX/OpenJFX — выбор по умолчанию для новых насыщенных десктоп-приложений.** Текущая GA **27** (27.0.1, октябрь 2026) **требует JDK 25+** (компиляция с `--release 25`; на старых JDK ошибка «module javafx.base cannot be read»). В JavaFX 27 **Metal стал конвейером по умолчанию на macOS** (`-Dprism.order=es2` возвращает прежний), добавлены атрибуты RichTextArea (инкубатор), условный импорт стилей и media queries, элементы управления в заголовке окна, поддержка reduced-motion. JavaFX 28 GA запланирован на 2027-03.
- **Линию JavaFX выбирают по JDK:** Gluon публикует бесплатные сборки и ведёт LTS-линии **25** (минимум JDK 23), **21** (минимум JDK 17) и 17 (минимум JDK 11, поддержка кончается 2026-10); патчи 25.0.5 и 21.0.13 вышли в октябре 2026; долгосрочные бэкпорты безопасности сверх бесплатных обновлений — платный контракт Gluon. В Maven Central попадают не все патчи LTS (например, 25.0.4), так что проверьте перед фиксацией версии.
- **JavaFX не входит в JDK** (убран из Oracle JDK в 11). Берите как артефакты Maven (`org.openjfx:javafx-controls` с классификаторами платформ) или JDK со встроенным JavaFX (Azul Zulu FX, BellSoft Liberica Full). Из-за классификаторов **JAR, собранный на Windows, не запустится на macOS**: собирайте под каждую платформу или включайте все классификаторы. Немодульный запуск fat JAR требует класса-лаунчера, не наследующего `Application`.
- **Плагины сборки:** `org.openjfx:javafx-maven-plugin` 0.0.8 (**последний релиз 2021**, но работает) и Gradle-плагин `org.openjfx.javafxplugin`; для установщиков — jlink и jpackage (§2) через badass-jlink. **GluonFX** (плагин 1.0.29) компилирует JavaFX в нативный код для десктопа, iOS и Android на базе Substrate от Gluon (GraalVM); **Gluon Attach** (4.0.26) даёт сервисы устройства. С учётом сдвига GraalVM (§2) мобильный GluonFX считайте нишевым.
- **Экосистема JavaFX:** Scene Builder 26.0.0 (дизайнер FXML, Gluon), **AtlantaFX** 3.0.0 (современные темы Primer, Nord, Cupertino), ControlsFX 11.2.6, Ikonli 12.4.0 (шрифты иконок), RichTextFX, TilesFX, MaterialFX (проверяйте активность), FXGL для игр (§10). Архитектура: MVVM со свойствами и биндингами; к scene graph не обращаться вне потока JavaFX Application Thread (`Platform.runLater`, `javafx.concurrent.Task`); виртуальные потоки годятся для фоновой работы.
- **Swing жив и подходит** для инструментов, IDE-подобных приложений и legacy. **FlatLaf 3.8** (2026-11-04) даёт современные светлые и тёмные темы, темы IntelliJ, HiDPI-масштабирование, собственные декорации окон в Windows и Linux, нативный `SystemFileChooser` (3.7), API масштабирования (3.7), цветовые функции Oklab и `flatlaf-bom` (3.8). JetBrains Runtime с **Project Wakefield** (нативный тулкит Wayland, `-Dawt.toolkit.name=WLToolkit`) — путь к нативному Wayland для Swing; FlatLaf отключает там свои декорации окон. Полезные библиотеки Swing: MigLayout, RSyntaxTextArea 4.0.1, JFreeChart 1.5.6, XChart 4.0.4, SwingX (давно без релизов).
- **SWT** (3.135.0, Eclipse 4.37) оборачивает нативные виджеты; лучший выбор при работе на платформе Eclipse RCP; нативные JAR для каждой ОС; GTK3 на Linux.
- **Compose Multiplatform (Kotlin)** 1.12.2 стабилен на десктопе (JVM/Skia), Android и iOS; в 1.12 добавлены экспериментальный API окон v2 и MCP-сервер для Compose Hot Reload `[не проверено: блог JetBrains]`. Десктоп-упаковка использует jpackage (`packageDistributionForCurrentOS`). Выбирайте, если команда пишет на Kotlin и нужен общий код с Android и iOS; из Java писать UI на Compose нельзя.
- **Другие варианты:** веб-UI в десктопной оболочке (JCEF или JxBrowser с Chromium, либо локальный веб-сервер и браузер), Vaadin и Hilla для «десктопоподобных» браузерных приложений, **CheerpJ** для запуска старого Swing в браузере (§12). **Applet API удалён в JDK 26** (JEP 504); Java Web Start исчез с 11 (OpenWebStart — сторонняя замена).
- **Установщики и обновления:** jpackage по ОС (MSI через WiX, DMG и PKG с подписью и нотаризацией, DEB и RPM), Conveyor (Hydraulic, коммерческий, бесплатный для OSS) собирает самообновляемые пакеты для всех ОС с одной машины, install4j (коммерческий). Подписывайте бинарники Windows и macOS, иначе пользователи увидят предупреждения SmartScreen и Gatekeeper.
- **HiDPI и шрифты:** JavaFX и Swing учитывают масштаб ОС на JDK 9+; в Linux задайте `GDK_SCALE` или `-Dsun.java2d.uiScale`; если нужна точная отрисовка, поставляйте шрифты.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [openjfx.io](https://openjfx.io/) | Gluon / OpenJFX | начало работы для Maven, Gradle, IDE |
| [JavaFX 27 highlights](https://openjfx.io/highlights/27/) | OpenJFX, 2026 | минимум JDK 25, Metal по умолчанию |
| [Gluon JavaFX downloads and LTS table](https://gluonhq.com/products/javafx/) | Gluon, 2026 | какая версия для какого JDK |
| [openjdk/jfx](https://github.com/openjdk/jfx) | OpenJDK | исходники, release notes |
| [JavaFX documentation (openjfx.io/openjfx-docs)](https://openjfx.io/openjfx-docs/) | Gluon | модульная и немодульная настройка, jlink |
| [javafx-maven-plugin](https://github.com/openjfx/javafx-maven-plugin) | OpenJFX | запуск и jlink из Maven |
| [GluonFX Maven plugin docs](https://docs.gluonhq.com/) | Gluon | нативные десктоп и мобильные сборки |
| [Scene Builder](https://github.com/gluonhq/scenebuilder) | Gluon | дизайнер FXML |
| [AtlantaFX](https://github.com/mkpaz/atlantafx) | mkpaz | современные темы JavaFX |
| [ControlsFX](https://github.com/controlsfx/controlsfx) | команда ControlsFX | дополнительные контролы |
| [RichTextFX](https://github.com/FXMisc/RichTextFX) | FXMisc | редакторы кода в JavaFX |
| [JavaFX concurrency (Task, Service)](https://openjfx.io/javadoc/25/javafx.graphics/javafx/concurrent/package-summary.html) | OpenJFX | правила работы с потоками `[не проверено]` |
| [FlatLaf](https://www.formdev.com/flatlaf/) | FormDev | современный look and feel для Swing |
| [FlatLaf releases](https://github.com/JFormDesigner/FlatLaf/releases) | FormDev, 2026 | изменения 3.7 и 3.8, заметки про Wakefield |
| [Project Wakefield (JBR Wayland)](https://github.com/JetBrains/JetBrainsRuntime/wiki/Wayland) | JetBrains | нативный Wayland для AWT и Swing `[не проверено]` |
| [MigLayout](https://github.com/mikaelgrev/miglayout) | Mikael Grev | менеджер раскладки, который стоит брать |
| [RSyntaxTextArea](https://github.com/bobbylight/RSyntaxTextArea) | Robert Futrell | редактор с подсветкой синтаксиса |
| [SWT](https://www.eclipse.org/swt/) | Eclipse | тулкит нативных виджетов |
| [Compose Multiplatform 1.12.0 release](https://blog.jetbrains.com/kotlin/2026/08/compose-multiplatform-1-12-0/) | JetBrains, 2026 | API окон v2, MCP для hot reload `[не проверено]` |
| [Compose Multiplatform repo](https://github.com/JetBrains/compose-multiplatform) | JetBrains | релизы, документация по упаковке для десктопа |
| [Conveyor](https://hydraulic.dev/) | Hydraulic | подписанные самообновляемые пакеты для всех ОС |
| [JEP 504: Remove the Applet API](https://openjdk.org/jeps/504) | OpenJDK, 2025 | апплеты удалены в JDK 26 |
| [OpenWebStart](https://openwebstart.com/) | Karakun | замена JNLP для старых приложений |
| [JCEF](https://github.com/chromiumembedded/java-cef) | проект CEF | Chromium внутри Java UI |

## 5. Android (только ориентиры)

**Ключевые выводы**

- Android — **Kotlin-first** (Jetpack Compose, корутины, KMP); Java остаётся полностью поддерживаемой для кода приложений и библиотек, и многие SDK по-прежнему написаны на Java.
- Android не запускает JVM: вывод `javac` и `kotlinc` конвертируют **D8/R8** в DEX для ART. Уровень языка и уровень API — разные вещи: `sourceCompatibility` и `targetCompatibility` (обычно 17) задают синтаксис и байткод, `compileSdk` и `minSdk` — какие API `java.*` доступны. Android 14 (API 34) соответствует базовым библиотекам Java 17.
- **Desugaring:** D8 переписывает возможности языка (лямбды, default-методы, try-with-resources на всех уровнях API); **core library desugaring** (`isCoreLibraryDesugaringEnabled = true` + `com.android.tools:desugar_jdk_libs` 2.x) переносит `java.time`, streams, `Optional`, часть `java.nio.file` на старые устройства. `MethodHandle.invoke` требует minSdk 26.
- AGP 8.x требует JDK 17 для запуска Gradle; новые линии AGP могут требовать более нового JDK (смотрите таблицу совместимости AGP). Используйте toolchains Gradle.
- Подвохи для авторов библиотек под Android: избегайте трюков `java.lang.invoke`, `java.awt`, `javax.management`, возможностей только JPMS, FFM, виртуальных потоков (Loom на ART нет), API новее уровня устройства; проверяйте **animal-sniffer** с сигнатурой Android или lint.
- Пересечения десктопа и мобильных платформ: Gluon (JavaFX на Android и iOS), libGDX и jMonkeyEngine (игры, §10), Compose Multiplatform (Kotlin).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Java versions in Android builds](https://developer.android.com/build/jdks) | Google | какой JDK что делает в сборке |
| [Java 8+ language features and API desugaring](https://developer.android.com/studio/write/java8-support) | Google | desugaring D8/R8, core library desugaring |
| [Java 11+ APIs available through desugaring](https://developer.android.com/studio/write/java11-default-support-table) | Google | таблица API |
| [desugar_jdk_libs](https://github.com/google/desugar_jdk_libs) | Google | журнал изменений перенесённых API |
| [AGP release notes and compatibility](https://developer.android.com/build/releases/gradle-plugin) | Google | матрица AGP, Gradle и JDK |
| [Android Kotlin-first](https://developer.android.com/kotlin/first) | Google | официальная позиция |

## 6. Данные и большие данные

**Ключевые выводы**

- **JVM по-прежнему владеет движками больших данных** (Spark, Flink, Kafka, раннеры Beam, Trino, Iceberg, Hadoop), даже если пользователи управляют ими из Python или SQL. Java — естественный язык для коннекторов, UDF, своих источников и приёмников и встроенных конвейеров.
- **Apache Spark 4.3.0** (2026-10-30): только Scala **2.13**, Java **17, 21, 25** (25 — с 25.0.3), Python 3.11+. API Java: `SparkSession`, `Dataset<Row>`, `Encoders.bean(...)`; предпочитайте DataFrame и SQL вместо RDD (Catalyst не оптимизирует лямбды). **Spark Connect** (`spark-connect-client-jvm_2.13`) даёт тонкий клиент к удалённому Spark по gRPC и снимает конфликт зависимостей встроенного Spark. Подвохи: Spark тянет огромный classpath (версии Jackson, Guava, Netty); на JDK 17+ нужны флаги `--add-opens` (скрипты запуска Spark их добавляют, ваш `main` в тестах должен добавлять сам); лямбды, передаваемые в Spark, должны быть сериализуемыми; в именах артефактов есть суффикс Scala (`_2.13`).
- **Apache Flink 2.3.0**: версия 2.0 убрала DataSet API, Scala API, `SourceFunction` и `SinkFunction`, старые табличные источники; Java 17 — по умолчанию, 21 поддерживается; в 2.x добавлены разделённое состояние (ForSt) и materialized tables. Коннекторы на старых API Source/Sink V1 на 2.x не работают. Flink прежде всего Java: DataStream API, Table API и SQL, состояние и чекпоинты; тестируйте через `MiniClusterWithClientResource`.
- **Kafka 4.4.0** (клиенты и Streams): Kafka 4.x работает **только на KRaft** (ZooKeeper убран в 4.0). **Kafka Streams** — библиотека, а не кластер: встраивайте в любое Java-приложение для потоковой обработки с состоянием (хранилища RocksDB, exactly-once v2). Серверное использование Kafka — в [domains-server.md](domains-server.md).
- **Apache Beam 2.77.0**, Java SDK: одна модель конвейера, раннеры для Dataflow, Flink, Spark и Direct; Java остаётся самым полным SDK Beam. Релизы примерно раз в 6 недель; фиксируйте BOM.
- **Колоночные форматы и lakehouse:** **Arrow Java 19.0.0** (теперь в отдельном репозитории `apache/arrow-java`) требует JDK 11+ и **`--add-opens=java.base/java.nio=org.apache.arrow.memory.core,ALL-UNNAMED`** в рантайме и в `argLine` Surefire; для Flight нужен `--add-reads`; берите `arrow-bom`. **Parquet Java 1.18.1** всё ещё тянет классы Hadoop (`hadoop-client-api`), если не использовать новые абстракции `InputFile` и `OutputFile`. **Iceberg 1.12.0** и **Delta Kernel 4.4.1** — библиотеки табличных форматов (Java API Iceberg — эталонная реализация).
- **DuckDB из Java:** `org.duckdb:duckdb_jdbc` **2.0.0.1** (DuckDB 2.0.0 LTS «Cinnamon Teal» от 2026-10-21, поддержка минимум до 2027-10; новый формат файла по умолчанию и новый клиент-серверный протокол «Quack»). URL `jdbc:duckdb:` (в памяти) или `jdbc:duckdb:/path/db`; Appender API для массовой загрузки; обмен через Arrow; поддержка GraalVM native-image описана в документации. Подвохи: JAR содержит нативные библиотеки для каждой платформы (большой, около 70 МБ и более `[не проверено]`); файл DuckDB допускает один процесс на запись; соединение дублируйте на поток, не делитесь одним.
- **Trino JDBC 484** — запросы к кластеру Trino; сам Trino — серверное приложение на Java 25 (см. [domains-server.md](domains-server.md)).
- **Малые и средние данные без кластера:** DuckDB JDBC или Apache DataFusion через Arrow; `java.util.stream` и records для преобразований в памяти; **jOOQ** или чистый JDBC для SQL; Apache Commons CSV, FastCSV или Jackson CSV для файлов. Spark в local-режиме для этого тяжёл; для скриптов избегайте.
- **Эксплуатационные подвохи:** стеки больших данных конфликтуют по версиям Guava, Jackson, Netty, Protobuf и Hadoop; изолируйте через Spark Connect, отдельные процессы или shading (§1). Перед обновлением рантайма проверяйте поддерживаемый JDK движка: движки отстают от новых JDK на 6–12 месяцев.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Spark 4.3.0 docs overview](https://spark.apache.org/docs/latest/) | Apache Spark, 2026 | поддерживаемые Java, Scala, Python |
| [Spark news and releases](https://spark.apache.org/news/index.html) | Apache Spark, 2026 | даты релизов |
| [Spark SQL guide (Java examples)](https://spark.apache.org/docs/latest/sql-programming-guide.html) | Apache Spark | API Dataset и DataFrame |
| [Spark Connect overview](https://spark.apache.org/docs/latest/spark-connect-overview.html) | Apache Spark | тонкий клиент JVM |
| [Flink 2.0 announcement](https://flink.apache.org/2025/03/24/apache-flink-2.0.0-a-new-era-of-real-time-data-processing/) | Apache Flink, 2025 | что убрано и новая модель состояния |
| [Flink DataStream API docs](https://nightlies.apache.org/flink/flink-docs-stable/docs/dev/datastream/overview/) | Apache Flink | потоковая обработка на Java |
| [Kafka Streams documentation](https://kafka.apache.org/documentation/streams/) | Apache Kafka | встроенная потоковая обработка |
| [Beam Java SDK quickstart](https://beam.apache.org/get-started/quickstart-java/) | Apache Beam | первый конвейер, раннеры |
| [Beam programming guide](https://beam.apache.org/documentation/programming-guide/) | Apache Beam | PCollections, окна |
| [Arrow Java install notes](https://arrow.apache.org/java/current/install.html) | Apache Arrow, 2026 | `--add-opens`, флаги Flight, BOM |
| [apache/arrow-java](https://github.com/apache/arrow-java) | Apache Arrow | отдельный репозиторий Java |
| [Parquet Java](https://github.com/apache/parquet-java) | Apache Parquet | библиотека чтения и записи |
| [Iceberg Java API](https://iceberg.apache.org/docs/latest/java-api-quickstart/) | Apache Iceberg | API табличного формата |
| [DuckDB Java client](https://duckdb.org/docs/2.0/clients/java/overview.html) | DuckDB, 2026 | URL JDBC, appender, Arrow, native image |
| [Announcing DuckDB 2.0.0 LTS](https://duckdb.org/2026/10/21/announcing-duckdb-200) | DuckDB Foundation, 2026 | даты LTS, протокол Quack, новый формат файла `[не проверено]` |
| [duckdb/duckdb-java](https://github.com/duckdb/duckdb-java) | DuckDB | исходники драйвера и issues |
| [Trino JDBC driver](https://trino.io/docs/current/client/jdbc.html) | Trino | подключение из Java |
| [Apache DataFusion Java bindings (datafusion-java)](https://github.com/datafusion-contrib/datafusion-java) | сообщество | движок запросов на Rust из Java `[не проверено]` |
| [FastCSV](https://github.com/osiegmar/FastCSV) | Oliver Siegmar | быстрый CSV по RFC 4180 |
| [jOOQ](https://www.jooq.org/) | Data Geekery | типобезопасный SQL (см. серверные заметки) |

## 7. Ноутбуки, DataFrame и разведочный анализ

**Ключевые выводы**

- **DataFrame на Java есть, но это ниша.** **DFLib** (стабильная 1.3.0; 2.0.0-M7 в работе) — самая активная чисто Java-реализация с текучим API выражений над колонками, коннекторами CSV, JDBC, Parquet, Avro и отрисовкой в Jupyter. **Tablesaw** (0.44.4) старше, похож на Pandas, с графиками Plot.ly; развивается медленно. **Smile 7.1** включает DataFrame, ML и графики. **Kotlin DataFrame** (dev-сборки 1.0) силён, если ноутбук на Kotlin. Для больших объёмов перекладывайте работу на DuckDB или Spark SQL.
- **Ядра Jupyter:** **JJava** (форк IJava от сообщества DFLib, Java 11+, на основе JShell, магии в стиле IPython, установка через `jupyter kernelspec install`). **IJava** `[устарело]`: релизов нет с 2019, «авторами больше не поддерживается» → JJava. **Kotlin Jupyter kernel** (0.19.x) — самый отполированный опыт ноутбуков на JVM; у JBang есть интеграция с Jupyter (`jbang-jupyter`, небольшой проект). **BeakerX** давно без обновлений (последний push 2023-12); **Ganymede** затих (последний push 2025-10). Альтернативы в IDE: Kotlin Notebook от JetBrains и «Java scratch + консоль JShell» в IntelliJ.
- **Графики:** XChart 4.0.4 (простые графики Swing), JFreeChart 1.5.6, вывод Plot.ly в Tablesaw, графики Smile, Kandy и Lets-Plot (Kotlin), ECharts-Java для HTML.
- **JShell** (JDK 9+) — REPL; библиотеки подключайте через `/env --class-path` или `jshell --class-path`; JBang открывает JShell с зависимостями (`jbang --interactive`).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [DFLib](https://dflib.org/) | ObjectStyle / DFLib, 2026 | документация по Java DataFrame |
| [dflib/dflib](https://github.com/dflib/dflib) | DFLib, 2026 | релизы, вехи 2.0 |
| [JJava kernel](https://github.com/dflib/jjava) | сообщество DFLib, 2026 | поддерживаемое ядро Jupyter для Java |
| [IJava](https://github.com/SpencerPark/IJava) | Spencer Park, 2019 | `[устарело]`: используйте JJava |
| [Kotlin Jupyter kernel](https://github.com/Kotlin/kotlin-jupyter) | JetBrains | ноутбуки на JVM с Kotlin |
| [Tablesaw](https://github.com/jtablesaw/tablesaw) | команда Tablesaw | Java DataFrame в стиле Pandas |
| [Smile](https://haifengl.github.io/) | Haifeng Li | ML, DataFrame, графики на Java; **проверьте лицензию (GPLv3 или коммерческая)** |
| [Kotlin DataFrame](https://github.com/Kotlin/dataframe) | JetBrains | типизированные DataFrame |
| [XChart](https://knowm.org/open-source/xchart/) | Knowm | быстрые графики |
| [JShell user guide](https://docs.oracle.com/en/java/javase/25/jshell/introduction-jshell.html) | Oracle | основы REPL |

## 8. Научные и численные расчёты, GPU

**Ключевые выводы**

- **Линейная алгебра:** **EJML** 0.46.1 (плотные и разреженные матрицы, малые и средние размеры, чистая Java, очень быстрая в своей нише); **ojAlgo** 57.6.2 (линейная алгебра и решатели оптимизации LP/QP, чистая Java, релизы идут); **Hipparchus** 4.0.3 (поддерживаемый форк Commons Math 3: линейная алгебра, ОДУ, статистика, оптимизация, геометрия; используется в Orekit); **dev.ludovic.netlib** 3.2.0 (BLAS/LAPACK с Vector API и нативными бэкендами; используется в Spark MLlib). **Apache Commons Math давно без релизов:** 3.6.1 — 2016 год, 4.0-beta1 — 2022; его части выделены в **Commons Numbers 1.3, RNG 1.7, Statistics 1.3, Geometry 1.0** (релизы 2026 года, кроме Geometry) — берите их или Hipparchus.
- **N-мерные массивы:** **ND4J и Deeplearning4j давно без релизов:** последний релиз в Central 1.0.0-M2.1 в 2022-08, хотя в репозитории коммиты ещё идут (2026-10). Новые проекты на них не начинайте. Альтернативы: DJL NDArray (на движках PyTorch и ONNX Runtime), пресеты JavaCPP для PyTorch 2.13, OpenBLAS и MKL, матричный пакет Smile (учитывайте лицензию) или Kotlin Multik 0.3.1.
- **Нативные библиотеки из Java:** **JavaCPP и JavaCV 1.5.14** (2026-08) поставляют готовые пресеты для OpenCV 4.14, FFmpeg 8.1.2, PyTorch 2.13, CUDA, TensorRT и других; артефакты `-platform` добавляйте аккуратно (они тянут все ОС, ограничивайте через `javacpp.platform`). Для новых привязок вместо JNI берите **FFM и jextract** (финальные в JDK 22+) — см. [concurrency-native.md](concurrency-native.md).
- **SIMD:** **Vector API** всё ещё в инкубаторе (**12-й инкубатор в JDK 27**, API не менялся, ждёт value-классов Valhalla); включается через `--add-modules jdk.incubator.vector`; есть тип `Float16`. Многие библиотеки (Lucene, netlib, JVector, ojAlgo `[не проверено]`) используют его оппортунистически за флагом. Типы инкубатора в публичный API не выставляйте.
- **GPU из Java:** **TornadoVM 8.1.0** (2026-11; JDK 21–27; бэкенды OpenCL, CUDA/PTX и Apple Metal; `TaskGraph` и `TornadoExecutionPlan`; цикловой API `@Parallel`, работающий и на обычной JVM; API `KernelContext` для ядер в стиле CUDA; новый `TileContext` только для CUDA; установка через SDKMAN `sdk install tornadovm`; артефакты `io.github.beehive-lab:tornado-api` с суффиксами `-jdk21` и `-jdk22plus`). Команда TornadoVM также делает инференс LLM на GPU на чистой Java (GPULlama3, «jitllm») с интеграцией в LangChain4j и Quarkus. **Project Babylon** (OpenJDK) добавляет **code reflection** (`@Reflect`, прежде `@CodeReflection`) и **HAT** (Heterogeneous Accelerator Toolkit): ядра — обычные методы Java с `KernelContext`, буферы вроде `F32Array`, бэкенды CUDA, OpenCL и Java-потоки; статья 2026-01 показала для HAT около 10,2 TFLOP/s FP32 против около 12,7 у cuBLAS на A10. **У Babylon пока нет JEP, нужна сборка JDK Babylon: только исследование.** Aparapi 3.0.2 (2024) давно без релизов.
- **Предметные библиотеки:** **Orekit** 13.1.9 (динамика космических аппаратов), **GeoTools** и **JTS** 1.20 (геометрия ГИС), **Apache SIS** 1.7 (геопространственные метаданные и CRS), **BioJava** 7.3.0, **ImageJ/Fiji + ImgLib2** 8.1.0 (научная визуализация, очень активны), **CDK** (хемоинформатика), **Apache Commons Statistics**. Наука на Java часто выглядит как «движок на Java, интерфейс на Python» (pyimagej, JPype; см. §13).
- **Подвохи:** boxing убивает скорость расчётов (используйте `double[]`, `DoubleStream`, примитивные коллекции вроде fastutil или Eclipse Collections); двумерные массивы — массивы массивов (применяйте плоские массивы с арифметикой индексов); различия `Math.fma` и `StrictMath`; бенчмарки — через JMH (см. [performance.md](performance.md)); автовекторизация C2 работает только для простых счётных циклов.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [EJML](https://ejml.org/) | Peter Abeles | быстрая линейная алгебра на чистой Java |
| [ojAlgo](https://www.ojalgo.org/) | Optimatika | линейная алгебра и оптимизация |
| [Hipparchus](https://hipparchus.org/) | команда Hipparchus | поддерживаемый наследник Commons Math |
| [Commons Math (status)](https://commons.apache.org/proper/commons-math/) | Apache Commons | почему разбит на компоненты |
| [Commons Numbers](https://commons.apache.org/proper/commons-numbers/) | Apache Commons | комплексные числа, дроби, гамма-функция и другое |
| [Commons Statistics](https://commons.apache.org/proper/commons-statistics/) | Apache Commons | распределения, описательная статистика |
| [Commons RNG](https://commons.apache.org/proper/commons-rng/) | Apache Commons | качественные генераторы случайных чисел |
| [netlib-java successor (dev.ludovic.netlib)](https://github.com/luhenry/netlib) | Ludovic Henry | BLAS/LAPACK для JVM |
| [JavaCPP presets](https://github.com/bytedeco/javacpp-presets) | Samuel Audet | привязки OpenCV, FFmpeg, PyTorch, CUDA |
| [JavaCV](https://github.com/bytedeco/javacv) | Samuel Audet | компьютерное зрение и медиа |
| [JEP 537: Vector API (12th incubator)](https://openjdk.org/jeps/537) | OpenJDK, 2026 | текущее состояние SIMD |
| [TornadoVM](https://github.com/beehive-lab/TornadoVM) | Beehive Lab (Univ. Manchester), 2026 | ускорение на GPU для Java |
| [TornadoVM docs](https://tornadovm.readthedocs.io/en/latest/) | Beehive Lab | API, установка, бэкенды |
| [jitllm (formerly GPULlama3.java)](https://github.com/beehive-lab/jitllm) | Beehive Lab, 2026 | инференс LLM на GPU на чистой Java; репозиторий переименован |
| [Project Babylon](https://openjdk.org/projects/babylon/) | OpenJDK | code reflection, HAT |
| [Optimizing GPU programs from Java using Babylon and HAT](https://openjdk.org/projects/babylon/articles/hat-matmul/hat-matmul) | Juan Fumero, 2026 | ядра HAT, цифры |
| [Code Models (Babylon)](https://openjdk.org/projects/babylon/articles/code-models) | Paul Sandoz, 2024/2025 | как работает code reflection |
| [openjdk/babylon](https://github.com/openjdk/babylon) | OpenJDK | исходники, примеры HAT |
| [Orekit](https://www.orekit.org/) | CS GROUP | динамика полёта |
| [GeoTools](https://geotools.org/) | OSGeo | инструментарий ГИС |
| [JTS Topology Suite](https://github.com/locationtech/jts) | LocationTech | движок геометрии |
| [Apache SIS](https://sis.apache.org/) | Apache | геопространственные системы отсчёта |
| [BioJava](https://biojava.org/) | BioJava | биоинформатика |
| [ImgLib2](https://imagej.net/libs/imglib2/) | сообщество ImageJ | n-мерная обработка изображений |
| [Fiji](https://fiji.sc/) | сообщество Fiji | платформа научного анализа изображений |
| [fastutil](https://fastutil.di.unimi.it/) | Sebastiano Vigna | примитивные коллекции |

## 9. ML, ИИ и LLM-приложения

**Ключевые выводы**

- **Разделение труда в 2026:** модели обучают в Python, Java их **обслуживает, встраивает и оркестрирует**. Сильные стороны Java: (1) LLM-приложения и агенты внутри существующих JVM-систем, (2) инференс в процессе через ONNX Runtime или DJL, (3) классический ML на табличных данных (Smile, Tribuo), (4) поиск и векторное извлечение (Lucene, JVector).
- **Фреймворки LLM-приложений:**
  - **LangChain4j 1.23.0** (2026-10-28): нативный Java (не порт), единый API над 20+ провайдерами моделей и 30+ хранилищами эмбеддингов, декларативные **AI Services** (аннотированные интерфейсы), tools и function calling, память чата, конвейер RAG (загрузка, нарезка, эмбеддинг, извлечение, re-rank), модуль агентов и «Skills», MCP-клиент, интеграции со Spring Boot, Quarkus, Helidon и Micronaut. Пользователям Quarkus — **Quarkus LangChain4j** (1.14.x стабильная; 2.0.0.Beta2).
  - **Spring AI 2.0.2** GA (2026-10-23; 2.1.0-RC2 в работе): fluent API `ChatClient`, Advisors, tool calling, структурированный вывод в POJO, память чата, RAG и ETL, evaluators, наблюдаемость; провайдеры Anthropic, OpenAI, Azure/Microsoft, Amazon Bedrock, Google, Ollama; переносимый API векторных хранилищ поверх PGVector, Qdrant, Milvus, Weaviate, Redis, Neo4j, Oracle, MongoDB Atlas, Cassandra, Chroma, Pinecone. Spring AI **2.0.0 GA вышел 2026-06-12** для **Spring Boot 4.0 и 4.1 и Spring Framework 7.0**, с Jackson 3 и аннотациями JSpecify; на Boot 3.x не работает `[не проверено: блог Spring через поиск]`. Линии 1.x патчатся для пользователей Boot 3, но гайды 1.x для новых проектов `[устарело]`.
  - **Фреймворки агентов:** Google **ADK for Java** 1.14.0, **Embabel** 1.5.4 (JVM-фреймворк агентов в стиле GOAP от Рода Джонсона), Microsoft Semantic Kernel Java 1.5.0 (темп ниже), AgentScope Java; JetBrains **Koog** (Kotlin).
- **SDK вендоров (официальные, генерируются из спецификаций OpenAPI, совместимы с Java 8+):** **Anthropic `com.anthropic:anthropic-java` 3.6.0** (2026-11-05; `AnthropicOkHttpClient.fromEnv()` читает `ANTHROPIC_API_KEY`; модули для Bedrock, Vertex, Foundry, AWS, Google Cloud и вспомогательный модуль MCP; `MIGRATION.md` описывает переход 2.x на 3.x); **OpenAI `com.openai:openai-java` 4.101.0**; **Google Gen AI `com.google.genai:google-genai` 2.0.0** (2026-11). Подвох: эти SDK выходят еженедельно; фиксируйте версии и читайте changelog перед мажорными обновлениями (Anthropic 3.0 и Google 2.0 вышли в 2026). Для одного провайдера берите SDK напрямую; LangChain4j или Spring AI — когда нужны переносимость между провайдерами, готовый RAG или интеграция с фреймворком.
- **MCP (Model Context Protocol):** официальный **MCP Java SDK** `io.modelcontextprotocol.sdk:mcp` **2.1.0** (2.2.0-M2 в работе), Java 17+, клиент и сервер, транспорты STDIO и streamable HTTP/SSE. Ядро использует `HttpClient` из JDK и серверный транспорт на Jakarta Servlet; публичный API — Reactive Streams (внутри Reactor) с синхронным фасадом; **Jackson 3 — привязка JSON по умолчанию** (`mcp-json-jackson2` для Jackson 2). **В 2.0 транспорты Spring (WebClient, WebFlux, WebMVC) переехали в Spring AI 2.0+**, поэтому старые артефакты `mcp-spring-*` `[устарело]`. У Quarkus своё расширение `quarkus-mcp-server`.
- **Локальный инференс на Java:** **ONNX Runtime Java 1.31.1** (CPU; GPU — отдельный артефакт `onnxruntime_gpu`) — самый надёжный способ запускать экспортированные модели в процессе. **DJL 0.40.0** (Amazon) оборачивает PyTorch, ONNX Runtime и другие под одним API, имеет zoo моделей, токенизаторы HuggingFace (`ai.djl.huggingface:tokenizers`) и DJL Serving. **Jlama** (инференс LLM на чистой Java с Vector API) давно без релизов: последний 0.8.4 от 2025-01-01. LLM на GPU на чистой Java: GPULlama3 из TornadoVM (§8). **Ollama** через LangChain4j, Spring AI или `ollama4j` 1.1.7 для локальных моделей вне процесса; привязки Java к **llama.cpp** (`de.kherud:llama`; репозиторий тих с 2025-06).
- **Классический ML:** **Smile 7.1.0** (очень активен: классификация, регрессия, кластеризация, NLP, DataFrame, графики). **Лицензия ядра Smile — двойная: GPLv3 или коммерческая** (файл LICENSE прочитан 2026-11-06); для многих закрытых продуктов это блокер, проверяйте до выбора. **Tribuo 4.3.2** (Oracle, Apache-2.0, отслеживание происхождения моделей; релизы редкие с 2025-04); **Weka** (академическая, GPL); XGBoost4J и LightGBM JNI для градиентного бустинга; Apache OpenNLP 2.5.x и вехи 3.0 для классического NLP; Stanford CoreNLP (GPL).
- **Deeplearning4j давно без релизов** для новых работ (см. §8).
- **Эмбеддинги и векторный поиск из Java:** клиентские SDK Qdrant 1.20.0, Milvus 3.0.12, Weaviate v6 client 6.3.2; **PGVector** через обычный JDBC (`com.pgvector:pgvector` 0.1.6 крошечный и старый, но это только типы); внутри процесса — векторы **Lucene 10.6** (HNSW), **JVector 4.0.2** (DataStax, стиль DiskANN, используется в Cassandra); клиенты Java для OpenSearch и Elasticsearch. Модели эмбеддингов в процессе: ONNX-модули эмбеддингов LangChain4j (например, all-MiniLM, BGE) или DJL.
- **Подвохи:** не зашивайте ключи API в код (переменные окружения или хранилище секретов); задавайте таймауты и ретраи (SDK по умолчанию ретраят с backoff); стримьте длинные ответы; при виртуальных потоках блокирующие вызовы SDK допустимы; расход токенов нужно наблюдать (Spring AI и LangChain4j отдают метрики Micrometer и OpenTelemetry); поддержка JSON-схемы и структурированного вывода различается по провайдерам; LangChain4j и Spring AI оборачивают возможности вендоров с задержкой, поэтому самые новые функции API появляются сначала в SDK вендора.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [LangChain4j docs](https://docs.langchain4j.dev/intro) | команда LangChain4j, 2026 | AI Services, RAG, tools, интеграции |
| [langchain4j/langchain4j](https://github.com/langchain4j/langchain4j) | LangChain4j, 2026 | релизы, примеры |
| [LangChain4j examples](https://github.com/langchain4j/langchain4j-examples) | LangChain4j, 2026 | запускаемые примеры по провайдерам |
| [Quarkus LangChain4j](https://docs.quarkiverse.io/quarkus-langchain4j/dev/index.html) | Quarkiverse | LLM-приложения на Quarkus |
| [Spring AI project page](https://spring.io/projects/spring-ai) | VMware/Broadcom, 2026 | возможности, провайдеры, векторные хранилища |
| [Spring AI reference docs](https://docs.spring.io/spring-ai/reference/) | Spring | ChatClient, advisors, MCP |
| [spring-projects/spring-ai](https://github.com/spring-projects/spring-ai) | Spring, 2026 | релизы |
| [Spring AI 2.0 news roundup (InfoQ)](https://infoq.com/news/2026/06/spring-news-roundup-jun08-2026) | InfoQ, 2026 | контекст выхода 2.0 GA `[не проверено]` |
| [MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk) | команды MCP и Spring AI, 2026 | модули, транспорты, миграция на 2.0 |
| [MCP Java SDK docs](https://java.sdk.modelcontextprotocol.io/latest/) | команды MCP и Spring AI | руководства по клиенту и серверу |
| [Model Context Protocol spec](https://modelcontextprotocol.io/) | MCP | справочник по протоколу |
| [Anthropic Java SDK](https://github.com/anthropics/anthropic-sdk-java) | Anthropic, 2026 | координаты, модули, миграция |
| [Claude API: Java SDK docs](https://platform.claude.com/docs/en/api/sdks/java) | Anthropic | стриминг, tools, структурированный вывод |
| [OpenAI Java SDK](https://github.com/openai/openai-java) | OpenAI, 2026 | официальный клиент |
| [Google Gen AI Java SDK](https://github.com/googleapis/java-genai) | Google, 2026 | клиент Gemini |
| [Google ADK for Java](https://github.com/google/adk-java) | Google, 2026 | комплект разработки агентов |
| [Embabel agent framework](https://github.com/embabel/embabel-agent) | Rod Johnson / Embabel, 2026 | целеориентированные агенты на JVM |
| [Koog (Kotlin agents)](https://github.com/JetBrains/koog) | JetBrains | фреймворк агентов на Kotlin |
| [Semantic Kernel for Java](https://github.com/microsoft/semantic-kernel-java) | Microsoft | SK на JVM |
| [ONNX Runtime Java API](https://onnxruntime.ai/docs/get-started/with-java.html) | Microsoft | инференс в процессе |
| [DJL](https://djl.ai/) | Amazon | глубокое обучение для Java, не привязанное к движку |
| [deepjavalibrary/djl](https://github.com/deepjavalibrary/djl) | Amazon, 2026 | релизы, движки |
| [Tribuo](https://tribuo.org/) | Oracle Labs | ML с отслеживанием происхождения |
| [Smile](https://github.com/haifengl/smile) | Haifeng Li, 2026 | библиотека ML; **проверьте лицензию (GPLv3 или коммерческая)** |
| [Jlama](https://github.com/tjake/Jlama) | Jake Luciani | инференс LLM на чистой Java; давно без релизов |
| [ollama4j](https://github.com/ollama4j/ollama4j) | сообщество | клиент Ollama |
| [java-llama.cpp](https://github.com/kherud/java-llama.cpp) | Konstantin Herud | привязки llama.cpp; тихо с 2025-06 |
| [JVector](https://github.com/datastax/jvector) | DataStax | встроенный векторный поиск |
| [Lucene KNN vectors](https://lucene.apache.org/core/10_0_0/core/org/apache/lucene/document/KnnFloatVectorField.html) | Apache Lucene | HNSW в Lucene `[не проверено]` |
| [Qdrant Java client](https://github.com/qdrant/java-client) | Qdrant | клиент векторной БД |
| [Milvus Java SDK](https://github.com/milvus-io/milvus-sdk-java) | Zilliz | клиент векторной БД |
| [pgvector-java](https://github.com/pgvector/pgvector-java) | Andrew Kane | типы PGVector для JDBC |
| [Apache OpenNLP](https://opennlp.apache.org/) | Apache | классический инструментарий NLP |

## 10. Игры: жанры и типы движков

**Ключевые выводы**

- **Выбирайте слой, а не только движок.** В Java-играх три слоя: (1) **фреймворки** с игровым циклом, рендером, вводом, звуком и пайплайном ассетов (libGDX, FXGL, LITIENGINE); (2) **полный 3D-движок** со scene graph и инструментами уровня редактора (jMonkeyEngine); (3) **сырые привязки** (LWJGL 3: OpenGL, Vulkan, OpenAL, GLFW, bgfx, Assimp, stb, Nuklear) для тех, кто пишет свой движок. Визуального редактора класса Unity или Godot у Java-движков нет: разработка игр здесь code-first.
- **libGDX 1.14.2** (2026-06) — самый распространённый: 2D и 3D, десктоп (LWJGL3), Android, iOS (через **MobiVM/RoboVM**, всё ещё поддерживается), веб (GWT или **gdx-teavm** 1.4.0 на TeaVM); генератор проектов **gdx-liftoff**; официальные расширения Box2D, Bullet, Ashley ECS, gdx-ai, FreeType, Controllers. Подвох: liftoff по-прежнему рекомендует байткод Java 8 для совместимости с Android, iOS и GWT, тогда как десктоп может использовать 22+; GWT поддерживает лишь подмножество JDK (без рефлексии и потоков), а Ashley (2023) и gdx-ai (2024) тихи. Коммерческие и инди-игры на libGDX охватывают разные жанры: Slay the Spire (карточный деки-билдер), Mindustry (фабрика и tower defense на форке Arc от Anuke), Shattered Pixel Dungeon (roguelike, Android-first), Wildermyth (тактическая RPG) `[не проверено: движки отдельных игр]`.
- **jMonkeyEngine 3.9.0-stable** (2026-01; 3.10.0-beta3 в 2026-10): 3D scene graph, PBR, физика через **Minie** (Bullet), сеть (SpiderMonkey), поддержка Android, jME SDK (на базе NetBeans). Подходит для 3D-экшена, симуляций, воксельных и VR-экспериментов.
- **LWJGL 3.4.3** (2026-08): основа под libGDX, jME, Minecraft и многими самописными движками; для математики берите **JOML** 1.10.9, для отладочных и редакторских UI — **imgui-java** 1.92.7.1. Свой движок на LWJGL разумен для воксельных игр, симуляторов и инструментов; загрузку ассетов, управление сценой и звук придётся писать самим.
- **FXGL 25.0.1** (на JavaFX, 2D): быстрый старт для головоломок, казуальных, обучающих и аркадных игр и прототипов; наследует требования JavaFX к JDK (§4). **LITIENGINE 0.12.0**: 2D-движок на тайлах с редактором карт (utiLITI), подходит для top-down RPG и приключений. **mini2Dx** давно без релизов (2023).
- **По жанрам (code-first паттерны, переносимые между движками):**
  - *Экшен, платформеры, шутеры:* цикл с фиксированным шагом, Box2D (`gdx-box2d` в libGDX) или jBox2D (давно без релизов, 2024) для 2D-физики, Bullet через Minie или libGDX для 3D.
  - *Стратегии, тактика, симуляции, карточные и настольные игры:* держите правила в **чистом Java-ядре** без зависимостей от рендера (records и sealed-интерфейсы для ходов и событий, неизменяемое состояние, детерминированный ГСЧ с seed), чтобы покрывать его unit-тестами, запускать ИИ без графики (minimax, MCTS) и переиспользовать на сервере. Forge (движок правил Magic: The Gathering с ИИ, очень активен) — крупный открытый пример движка правил карточной игры на Java.
  - *Roguelike и ASCII/тайловые игры:* **SquidSquad** (процедурная генерация, поле зрения, поиск пути; преемник SquidLib) и AsciiPanel (простой, тих с 2024).
  - *RPG и приключения:* UI scene2d из libGDX или LITIENGINE, карты Tiled (`TmxMapLoader`), диалоги как данные.
  - *Мобильные, казуальные, головоломки:* бэкенды libGDX для Android и iOS; FXGL — только десктоп; следите за размером APK и паузами GC (пулы объектов, без аллокаций в `render`).
  - *Воксельные и песочницы:* LWJGL или jME; Terasology — открытый воксельный движок на Java (активен).
  - *Многопользовательские:* Netty либо чистый NIO или виртуальные потоки для серверов; KryoNet (давно без релизов, последний push 2021; форк crykn/kryonet тих с 2023); headless-сборки сервера на том же ядре; сеть — см. [domains-server.md](domains-server.md).
- **ECS:** Ashley (libGDX, тих), Artemis-odb (давно без релизов: релиз 2019, push 2023), Dominion (давно без релизов, 2024). Для большинства инди-игр хватает обычных объектов с компонентами или небольшого самописного ECS на массивах; отсутствие value-типов в Java (до Valhalla) ограничивает кеш-дружественные схемы ECS.
- **Моддинг Minecraft — отдельная крупная Java-экосистема.** Minecraft Java Edition **26.1** (2026, новое годовое версионирование) — **первый релиз без обфускации** и **требует Java 25** (было 21); для многих модов официальные имена Mojang заменяют маппинги Yarn и Parchment `[не проверено: блог NeoForge и сообщество]`. Загрузчики: **Fabric** (Loader 0.20.1, Gradle-плагин Fabric Loom; лёгкий, быстро обновляется), **NeoForge** (версии теперь повторяют версии Minecraft, например 26.1.0.x; ModDevGradle; нужен Gradle 9.1+ `[не проверено]`), старый MinecraftForge (активен, доля меньше). Серверные плагины: **Paper** (наследник API Bukkit и Spigot, очень активен), прокси Velocity и BungeeCord. Mixins (SpongePowered Mixin) — основной приём патчинга байткода; клиентские инструменты вроде RuneLite (клиент Old School RuneScape на Java) показывают те же приёмы плагинов.
- **Производительность в играх:** ZGC или G1 с фиксированной кучей для малых пауз (G1 теперь по умолчанию везде в JDK 27, JEP 523); без аллокаций в горячих циклах (пулы объектов, примитивные массивы); `-XX:+UseCompactObjectHeaders` включён по умолчанию в JDK 27 (JEP 534); упаковка через jlink и jpackage (§2), чтобы игрокам не требовался JDK; Steamworks через steamworks4j `[не проверено]`.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [libGDX](https://libgdx.com/) | команда libGDX | документация, вики, витрина |
| [libGDX wiki: project generation](https://libgdx.com/wiki/start/project-generation) | libGDX | gdx-liftoff, платформы, уровни Java |
| [gdx-liftoff](https://github.com/libgdx/gdx-liftoff) | libGDX, 2026 | генератор проектов |
| [gdx-teavm](https://github.com/xpenatan/gdx-teavm) | xpenatan, 2026 | веб-бэкенд libGDX на TeaVM |
| [MobiVM (RoboVM fork)](https://github.com/MobiVM/robovm) | MobiVM, 2026 | AOT для iOS в libGDX |
| [Awesome libGDX](https://github.com/rafaskb/awesome-libgdx) | сообщество | подборка расширений |
| [jMonkeyEngine](https://jmonkeyengine.org/) | команда jME | документация 3D-движка |
| [jMonkeyEngine wiki](https://wiki.jmonkeyengine.org/docs/3.8/documentation.html) | команда jME | туториалы `[не проверено]` |
| [Minie (Bullet physics for jME)](https://github.com/stephengold/Minie) | Stephen Gold, 2026 | 3D-физика |
| [LWJGL 3](https://www.lwjgl.org/) | команда LWJGL | привязки OpenGL, Vulkan, OpenAL |
| [LWJGL wiki](https://github.com/LWJGL/lwjgl3-wiki/wiki) | LWJGL | настройка, управление памятью |
| [LearnOpenGL (concepts map 1:1 to LWJGL)](https://learnopengl.com/) | Joey de Vries | основы графики (понятия переносятся на LWJGL один к одному) |
| [3D Game Development with LWJGL 3 (online book)](https://ahbejarano.gitbook.io/lwjglgamedev/) | Antonio Hernández Bejarano | как построить движок на LWJGL `[не проверено]` |
| [JOML](https://github.com/JOML-CI/JOML) | команда JOML | математика для игр |
| [imgui-java](https://github.com/SpaiR/imgui-java) | SpaiR, 2026 | привязки Dear ImGui |
| [FXGL](https://github.com/AlmasB/FXGL) | Almas Baimagambetov | игровая библиотека на JavaFX |
| [LITIENGINE](https://litiengine.com/) | Gurkenlabs | 2D-движок на тайлах и редактор |
| [SquidSquad](https://github.com/yellowstonegames/SquidSquad) | Tommy Ettinger, 2026 | набор для roguelike |
| [Forge (MTG rules engine)](https://github.com/Card-Forge/forge) | команда Forge, 2026 | правила карточной игры и ИИ на Java |
| [Mindustry source](https://github.com/Anuken/Mindustry) | Anuke, 2026 | крупная открытая игра-стратегия на Java |
| [Shattered Pixel Dungeon source](https://github.com/00-Evan/shattered-pixel-dungeon) | Evan Debenham | roguelike на libGDX, Android-first |
| [Terasology](https://github.com/MovingBlocks/Terasology) | MovingBlocks | воксельный движок на Java |
| [NeoForge for Minecraft 26.1](https://neoforged.net/news/26.1release/) | NeoForged, 2026 | Java 25, версионирование, инструменты `[не проверено]` |
| [NeoForge docs](https://docs.neoforged.net/) | NeoForged | руководство по моддингу |
| [Fabric documentation](https://docs.fabricmc.net/) | FabricMC | руководство по моддингу Fabric |
| [Fabric Loom](https://github.com/FabricMC/fabric-loom) | FabricMC, 2026 | Gradle-плагин для модов |
| [PaperMC docs](https://docs.papermc.io/) | PaperMC | API серверных плагинов |
| [SpongePowered Mixin](https://github.com/SpongePowered/Mixin) | SpongePowered | патчинг байткода в модах |
| [RuneLite](https://github.com/runelite/runelite) | RuneLite, 2026 | крупный игровой клиент на Java с плагинами |
| [steamworks4j](https://github.com/code-disaster/steamworks4j) | code-disaster | привязки Steam API `[не проверено]` |
| [Game Programming Patterns](https://gameprogrammingpatterns.com/) | Robert Nystrom, 2014 | игровой цикл, компоненты, пулы (не зависит от языка) |

## 11. Embedded и IoT

**Ключевые выводы**

- **«Embedded Java» в 2026 — это устройства класса Linux** (Raspberry Pi, промышленные шлюзы, ARM-платы) с обычным JDK, а также **Java Card** для смарт-карт и защищённых элементов. Java для микроконтроллеров (Java ME Embedded, Java SE Embedded) фактически ушла; **Compact Profiles** (Java 8) вытеснены рантаймами **jlink** (JDK 9+). Для MCU выбирайте C, Rust или MicroPython; Java-альтернативы (MicroEJ, коммерческая VM; проекты вроде Espruino) нишевые `[не проверено]`.
- **Raspberry Pi:** ставьте 64-битный JDK (Azul Zulu, BellSoft Liberica, включая сборку «Full» со встроенным JavaFX, Eclipse Temurin или через SDKMAN). **Pi4J 4.0.2** построен на Java 25; его **провайдер FFM требует JDK 25+**, говорит с ядром напрямую (`/dev/gpiochip*`, I2C через `libi2c-dev`) без сторонних нативных библиотек; провайдеры FFM, GpioD, LinuxFS, PiGpio. Запускайте с `--enable-native-access=com.pi4j.plugin.ffm` (или `Enable-Native-Access: ALL-UNNAMED` в манифесте), чтобы убрать предупреждение JDK 22+, и **не запускайте от root**: используйте `setup-permissions.sh`, группу `gpio` и правила udev. В V4.0.0 поддерживается только аппаратный PWM. Примеры на JBang упрощают быстрые эксперименты с датчиками.
- **diozero** 1.4.1 (2024) — альтернативная библиотека ввода-вывода устройств (GPIO, I2C, SPI, много плат и датчиков), тише Pi4J; темп упал. Сырой sysfs GPIO в Linux объявлен устаревшим; используйте путь через символьное устройство GPIO (libgpiod), который оборачивают Pi4J и diozero.
- **Последовательные порты и USB:** **jSerialComm** 2.11.4 (встроенные нативные библиотеки, кроссплатформенный; фактический стандарт); RXTX `[устарело]` → jSerialComm. Через FFM можно напрямую вызывать `termios` и libusb.
- **Протоколы:** MQTT — **HiveMQ MQTT Client** 1.4.2 (MQTT 3.1.1 и 5, реактивный, асинхронный и блокирующий API); клиент Eclipse Paho Java MQTTv5 1.2.5 — от **2020** года, давно без релизов. OPC UA — **Eclipse Milo** 1.1.8 (1.2.0 RC); Modbus — j2mod или digitalpetri modbus `[не проверено]`; CoAP — Eclipse Californium; BLE через D-Bus BlueZ (bluez-dbus, `[не проверено]`). Шлюзы: Eclipse Kura (на OSGi), Apache Camel (компоненты IoT), Apache PLC4X (протоколы ПЛК).
- **Размер и старт:** собирайте через jlink рантайм только с нужными модулями (часто меньше 50 МБ `[не проверено]`); используйте Serial GC или небольшую кучу G1 (`-Xmx64m`); JDK 27 по умолчанию включает compact object headers (JEP 534), что экономит память; AOT-кеш (§2) сокращает старт на медленных SD-картах; GraalVM Native Image собирает бинарники ARM64 (собирайте на ARM64 или в раннере CI ARM64; кросс-компиляции нет) с крошечным RSS. JavaFX работает на Pi (headless через DRM/KMS «monocle» или полный десктоп) для киоск-интерфейсов.
- **Java Card:** отдельная платформа, классические апплеты на защищённых элементах (SIM, eSIM, банковские карты, ключи FIDO). Последняя **спецификация 3.2**; **Java Card Development Kit 26.0** (инструменты, симулятор и плагин Eclipse, 2026-05) поддерживает продукты на основе 3.0.4, 3.0.5, 3.1, 3.2 `[не проверено: страница загрузок Oracle через поиск]`. Подмножество языка: нет `String`, нет `long` и `float` (`int` опционально), нет потоков, нет гарантий GC; применяйте транзакции и `JCSystem.makeTransientByteArray`. Инструменты сообщества: **ant-javacard**, GlobalPlatformPro, jCardSim (симулятор для unit-тестов).
- **Подвохи:** износ SD-карты (избегайте тяжёлого логирования, монтируйте `/tmp` как tmpfs); синхронизация времени до TLS; ARMv6 (Pi Zero/1) большинство современных сборок JDK больше не поддерживает (берите 32-битные сборки вендоров или более новые платы) `[не проверено]`; права на нативный доступ и членство в группах устройств — самая частая причина «работает только от root».

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Pi4J](https://www.pi4j.com/) | команда Pi4J, 2026 | ввод-вывод Raspberry Pi на Java |
| [Pi4J FFM provider](https://www.pi4j.com/documentation/providers/ffm/) | Pi4J, 2026 | JDK 25, права, без root |
| [Pi4J getting started](https://www.pi4j.com/getting-started/) | Pi4J | путь настройки, примеры, JBang |
| [Pi4J/pi4j](https://github.com/Pi4J/pi4j) | Pi4J, 2026 | исходники, релизы |
| [diozero](https://www.diozero.com/) | Matthew Lewis | библиотека ввода-вывода устройств `[не проверено]` |
| [jSerialComm](https://fazecast.github.io/jSerialComm/) | Fazecast | последовательные порты |
| [HiveMQ MQTT Client](https://github.com/hivemq/hivemq-mqtt-client) | HiveMQ, 2026 | клиент MQTT 5 |
| [Eclipse Paho Java](https://github.com/eclipse-paho/paho.mqtt.java) | Eclipse | клиент MQTT, давно без релизов, но широко используется `[не проверено]` |
| [Eclipse Milo](https://github.com/eclipse-milo/milo) | Eclipse | клиент и сервер OPC UA |
| [Eclipse Californium](https://github.com/eclipse-californium/californium) | Eclipse | CoAP |
| [Apache PLC4X](https://plc4x.apache.org/) | Apache | протоколы ПЛК (S7, Modbus и другие) |
| [Eclipse Kura](https://eclipse.dev/kura/) | Eclipse | фреймворк шлюзов IoT |
| [Java Card downloads](https://www.oracle.com/java/technologies/javacard-downloads.html) | Oracle, 2026 | SDK 26.0, спецификация 3.2 `[не проверено]` |
| [Java Card documentation](https://docs.oracle.com/en/java/javacard/) | Oracle | спецификации и руководства платформы |
| [ant-javacard](https://github.com/martinpaljak/ant-javacard) | Martin Paljak | сборка CAP-файлов |
| [GlobalPlatformPro](https://github.com/martinpaljak/GlobalPlatformPro) | Martin Paljak | установка апплетов на карты |
| [jCardSim](https://github.com/licel/jcardsim) | Licel | симулятор Java Card для тестов |

## 12. WebAssembly и браузер

**Ключевые выводы**

- **Два направления:** (a) **Java в браузер** (компиляция Java в JS или Wasm либо запуск JVM в браузере); (b) **Wasm в JVM** (запуск модулей Wasm внутри Java-приложения, например для плагинов или изолированного кода). Выбирайте по потребности, а не по моде: для обычных веб-интерфейсов основным ответом остаются Java-бэкенд и фронтенд на JS или TS (либо Vaadin и Hilla с серверной частью).
- **TeaVM 0.16.0** (2026-10): AOT-компилятор из **байткода** (то есть Java, Kotlin и Scala) в JavaScript, **WebAssembly GC** и C; есть архетип Maven и плагин Gradle, npm и webpack не нужны. Ограничения: рефлексия, ресурсы, загрузчики классов и JNI ограничены (код часто приходится адаптировать); потоки — «зелёные», через CPS-преобразование, Web Workers не делят состояние. Интероп с JS — через API **JSO**. Используется в gdx-teavm (веб-бэкенд libGDX, §10).
- **J2CL** (Google, релизы с тегами по датам, последний v20261026): исходники Java в JS в стиле Closure, оптимизация Closure Compiler; на нём работают Gmail, Docs, Slides и Calendar; **J2Wasm** (Java в WasmGC) «полностью работает, но в работе». Сборка через **Bazel**, Windows не поддерживается (используйте WSL); README для открытого использования всё ещё говорит «alpha». У Vertispan есть `j2cl-maven-plugin` для Maven `[не проверено]`.
- **GWT 2.13.1** (2026-06, groupId `org.gwtproject`; старые координаты `com.google.gwt` заканчиваются на 2.10.0) поддерживается для существующих приложений; GWT теперь ведёт сообщество, фокус на библиотеках и корпоративном секторе. Для новых проектов не рекомендуется.
- **CheerpJ 5.0** (Leaning Technologies): полный OpenJDK, скомпилированный в WebAssembly, запускает **немодифицированные JAR** в браузере, Java **8, 11, 17, 21** (25 в preview); Swing и AWT рисуются через HTML и canvas; расширение браузера запускает старые **апплеты и приложения JNLP**; клиенты Oracle Forms и EBS. Лицензирование коммерческое (смотрите страницу цен); слабые места — производительность и часть случаев OpenGL и звука. Лучший инструмент для спасения старых приложений на Swing и апплетах после удаления Applet API в JEP 504.
- **GraalVM Web Image** (экспериментальный бэкенд Native Image) выдаёт модуль Wasm с JS-обёрткой (GraalVM 25.1+) `[не проверено]`. Следите, но не опирайтесь.
- **Bytecoder** (репозиторий активен 2026-10, последний релиз в Central 2024-05) и **JWebAssembly** (push 2026-10, активность низкая) нишевые; **Bck2Brwsr** и **DukeScript** давно без релизов `[не проверено]`.
- **Wasm внутри JVM:** **Chicory 1.7.5** (Dylibso, рантайм Wasm на чистой Java без нативных зависимостей; интерпретатор плюс компилятор AOT в байткод; поддержка WASI) — ведущий вариант для систем плагинов и изоляции (например, запуск политик, скомпилированных из Rust или Go, или QuickJS через `quickjs4j` 0.1.1); **GraalWasm** (`org.graalvm.polyglot:wasm` 25.5.5.0.3) через API polyglot; привязки Wasmtime и Wasmer для Java через JNI старше (wasmtime-java 0.19.0, 2023, давно без релизов); Extism Java SDK 1.1.0 (2024) для плагинов Extism.
- **JavaScript из Java (сервер или десктоп):** GraalJS (polyglot, ECMAScript 2024+), **Javet** 6.0.3 (V8 и Node.js, встроенные через JNI; самый быстрый для тяжёлого JS), Rhino 1.9.2 (чистая Java, поддерживается), отдельный **Nashorn** 15.7 (удалён из JDK в 15, теперь побочный проект OpenJDK, последний релиз 2025-08).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [TeaVM](https://teavm.org/) | Alexey Andreev | обзор, начало работы |
| [TeaVM docs overview](https://teavm.org/docs/intro/overview.html) | Alexey Andreev | цели, ограничения, потоки |
| [konsoletyper/teavm](https://github.com/konsoletyper/teavm) | Alexey Andreev, 2026 | релизы, бэкенд Wasm GC |
| [J2CL](https://github.com/google/j2cl) | Google, 2026 | Java в Closure JS, J2Wasm |
| [J2CL getting started](https://github.com/google/j2cl/blob/master/docs/getting-started.md) | Google | настройка Bazel |
| [Vertispan j2cl-maven-plugin](https://github.com/Vertispan/j2clmavenplugin) | Vertispan | J2CL с Maven `[не проверено]` |
| [GWT Project](https://www.gwtproject.org/) | сообщество GWT | старый Java-в-JS |
| [CheerpJ](https://cheerpj.com/) | Leaning Technologies, 2026 | JVM в браузере, апплеты, Swing |
| [CheerpJ docs](https://cheerpj.com/docs/) | Leaning Technologies | начало работы, совместимость |
| [Chicory](https://chicory.dev/) | Dylibso | рантайм Wasm на чистой Java |
| [dylibso/chicory](https://github.com/dylibso/chicory) | Dylibso, 2026 | исходники, компилятор AOT, WASI |
| [quickjs4j](https://github.com/roastedroot/quickjs4j) | Roastedroot, 2026 | QuickJS на Chicory `[не проверено]` |
| [GraalWasm](https://www.graalvm.org/webassembly/) | Oracle | Wasm через API polyglot |
| [GraalJS](https://www.graalvm.org/javascript/) | Oracle | JavaScript на JVM |
| [Javet](https://github.com/caoccao/Javet) | Sam Cao, 2026 | V8 и Node.js в Java |
| [Rhino](https://github.com/mozilla/rhino) | Mozilla, 2026 | движок JS на чистой Java |
| [Nashorn (standalone)](https://github.com/openjdk/nashorn) | OpenJDK, 2025 | движок JS, удалённый из JDK 15 |
| [Bytecoder](https://github.com/mirkosertic/Bytecoder) | Mirko Sertic | нишевый JVM-в-Wasm/JS |

## 13. Interop: другие языки JVM, polyglot, Python и нативный код

**Ключевые выводы**

- **Kotlin (2.4.21; 2.5 в beta):** interop отличный, но с шероховатостями. Размечайте Java API nullness через **JSpecify**, чтобы Kotlin видел не platform-типы. Если Java вызывает Kotlin, Kotlin-стороне нужны `@JvmStatic`, `@JvmOverloads`, `@JvmField`, `@Throws`. Функции Kotlin `suspend` из Java выглядят как методы с `Continuation` (отдавайте обёртки на `CompletableFuture`). Sealed- и value-классы отображаются неидеально. Смешанные модули Java и Kotlin требуют плагина Kotlin для Gradle, чтобы компилировать оба языка.
- **Scala 3 (3.10.0)** свободно вызывает Java; Java, вызывающая Scala, упирается в companion-объекты (`Foo$.MODULE$`), implicits и givens, коллекции Scala (конвертируйте через `scala.jdk.CollectionConverters`). Пользователи Spark и Flink встречают эту границу ежедневно.
- **Groovy 6.0.0** (GA 2026-09): скрипты, старый DSL Gradle, тесты Spock, пайплайны Jenkins; новые координаты `org.apache.groovy` (группа `org.codehaus.groovy` закончилась на 3.0.x). **Clojure 1.12.6** (альфы 1.13) имеет первоклассный interop с Java (method values, `^[]` param tags). **JRuby 10.1.3.0** (совместим с Ruby 3.4, Java 21+ `[не проверено]`).
- **Движки скриптов внутри Java (JSR 223 `javax.script`):** Groovy, GraalJS, Rhino, отдельный Nashorn, JRuby, Jython (`[устарело]`: только Python 2.7, 2.7.5b1 в 2026 → GraalPy), LuaJ (давно без релизов, 2015) или LuaJava 4.1.0 (нативный Lua).
- **GraalVM polyglot (Truffle) на обычном JDK:** добавьте `org.graalvm.polyglot:polyglot` и POM языка (`python`, `js`, `wasm`, `ruby`, `llvm-community`, `espresso`), все версии **25.5.5.0.3**. **GraalPy** встраивает Python **3.13** в Java; используйте `org.graalvm.python:python-embedding` и **`graalpy-maven-plugin`** (цель `process-graalpy-resources`, список `<packages>`) либо плагин Gradle `org.graalvm.python`, затем `GraalPyResources.contextBuilder()`. Это также путь обновления с Jython. Подвохи: лучше всего работают чистые Python-пакеты; нативные расширения (NumPy и подобные) описаны в отдельном руководстве и имеют ограничения по платформам; на обычном OpenJDK код гостевых языков может работать без компиляции Truffle (печатается предупреждение), для пиковой производительности нужен GraalVM или описанная настройка JVMCI `[не проверено]`; контексты polyglot по умолчанию не потокобезопасны; песочница настраивается (`allowAllAccess`, `SandboxPolicy`).
- **Interop с CPython (когда нужны настоящие NumPy или PyTorch):** **Jep** 4.3.2 встраивает CPython в JVM через JNI (общие интерпретаторы, массивы NumPy и массивы Java; учитывайте GIL и ограничения sub-интерпретаторов); **JPype** 1.7.1 запускает JVM внутри Python (Python вызывает Java; используется в pyimagej и многих научных инструментах); **Py4J** 0.10.9.9 (сокетный мост, используется в PySpark). Вне процесса (gRPC, HTTP, JSON-RPC по stdio, Arrow Flight) часто проще и надёжнее, чем мосты в процессе.
- **Нативный код:** для библиотек C используйте **FFM API** (финальный в JDK 22) с **jextract** вместо JNI; JNA 5.19.1 и jnr-ffi 2.3.7 остаются для старых баз. Подробности — в [concurrency-native.md](concurrency-native.md).
- **Другие экосистемы:** .NET через jni4net (`[устарело]`) или вне процесса; Rust через FFM и `cbindgen` либо крейты JNI (`jni`, `jni-rs`); Go через C ABI и FFM.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Calling Java from Kotlin](https://kotlinlang.org/docs/java-interop.html) | JetBrains | platform-типы, nullability |
| [Calling Kotlin from Java](https://kotlinlang.org/docs/java-to-kotlin-interop.html) | JetBrains | аннотации `@Jvm*` |
| [Kotlin and JSpecify](https://kotlinlang.org/docs/java-interop.html#jspecify-support) | JetBrains | interop nullness `[не проверено]` |
| [Scala 3: Java interoperability](https://docs.scala-lang.org/scala3/book/interacting-with-java.html) | Scala Center | в обе стороны |
| [Groovy documentation](https://groovy-lang.org/documentation.html) | Apache Groovy | Groovy 6 |
| [Clojure Java interop](https://clojure.org/reference/java_interop) | Clojure | справочник по interop |
| [JRuby](https://www.jruby.org/) | команда JRuby | Ruby на JVM |
| [GraalPy for JVM developers](https://graalpy.org/jvm-developers/) | Oracle, 2026 | артефакты, плагины Maven и Gradle |
| [GraalPy](https://graalpy.org/) | Oracle, 2026 | обзор, Python 3.13, миграция с Jython |
| [graal-languages-demos](https://github.com/graalvm/graal-languages-demos) | Oracle, 2026 | руководства по GraalPy, GraalJS, GraalWasm |
| [GraalVM polyglot embedding reference](https://www.graalvm.org/latest/reference-manual/embed-languages/) | Oracle | Context API, песочница |
| [Jep](https://github.com/ninia/jep) | Ninia, 2026 | CPython в JVM |
| [JPype](https://github.com/jpype-project/jpype) | проект JPype, 2026 | JVM в Python |
| [Py4J](https://www.py4j.org/) | Barthélémy Dagenais | сокетный мост, используется в PySpark |
| [JEP 454: Foreign Function & Memory API](https://openjdk.org/jeps/454) | Maurizio Cimadamore, 2024 | современный нативный interop |
| [jextract](https://github.com/openjdk/jextract) | OpenJDK | генерация привязок FFM по заголовкам |
| [JNA](https://github.com/java-native-access/jna) | команда JNA | нативный доступ для старых баз |

## 14. Скрипты и автоматизация

**Ключевые выводы**

- **JBang** (0.143.0, 2026-10-30) — стандарт для скриптов на Java: `//DEPS group:artifact:version` (и BOM), `//REPOS`, `//JAVA 25+`, `//PREVIEW`, `//SOURCES`, `//FILES`, `//COMPILE_OPTIONS`, `//RUNTIME_OPTIONS`, `//NATIVE_OPTIONS`, `//CDS` (экспериментально), `//JAVAAGENT`, `//KOTLIN` и `//GROOVY`; shebang `///usr/bin/env jbang "$0" "$@" ; exit $?`; `jbang init -t cli` создаёт скрипт на picocli; `jbang edit` открывает проект, готовый для IDE; `jbang app install` кладёт скрипт на PATH; **каталоги** делятся скриптами как `jbang name@org`; `jbang export portable|fatjar|native|maven|gradle` превращает скрипт в настоящий проект или бинарник (native — через GraalVM); `jbang wrapper` добавляет в репозиторий `./jbang` без установки; JDK скачивается по требованию. Карта документации прочитана; детали флагов `[не проверено]`. Подвох: артефакт Maven `dev.jbang:jbang-cli` отстаёт от релиза на GitHub и SDKMAN (0.132.1 против 0.143.0), ставьте JBang через SDKMAN, Homebrew, Scoop или curl-скрипт.
- **Чистый JDK без инструментов:** `java Script.java` (JEP 330), поддержка **нескольких файлов** (JEP 458) и **compact source files** с `void main()` и неявными импортами `java.base` (JEP 512, JDK 25) делают одноразовые скрипты почти такими же короткими, как на Python; shebang в Unix работает для файлов без расширения `.java` (`#!/usr/bin/java --source 25`). Ограничения: нет зависимостей (используйте JBang), компиляция при каждом запуске (кешируйте через JBang или AOT-кеш), `--source` должен совпадать с JDK.
- **JShell** для исследования: `jshell --class-path libs/*`, `/open script.jsh`, `/save`; `--interactive` в JBang даёт JShell с разрешёнными зависимостями; скрипты JShell (`.jsh`) — ещё один лёгкий вариант.
- **Полезные библиотеки для скриптов автоматизации:** `java.net.http.HttpClient` (без зависимостей), `java.nio.file.Files` (`Files.walk`, `Files.readString`), `ProcessBuilder` и `Process.inputReader()` (JDK 17+) для запуска команд, Jackson или `jakarta.json` для JSON, picocli для аргументов, zt-exec или NuProcess для более богатого управления процессами `[не проверено]`, Apache Commons IO и CSV.
- **Groovy 6** остаётся классическим скриптовым языком JVM (старый DSL Gradle, Jenkins); для новых скриптов большинство команд теперь предпочитает Java на JBang или скрипты Kotlin (`.main.kts`). **Скрипты Kotlin** работают, но инструменты неровные `[не проверено]`.
- **Автоматизация в системах сборки:** Maven `exec-maven-plugin`, задачи Gradle `JavaExec`; у JBang есть плагины Maven и Gradle для запуска скриптов во время сборки.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [JBang documentation](https://www.jbang.dev/documentation/jbang/latest/index.html) | Max Rydahl Andersen и др., 2026 | полное руководство |
| [JBang: script directives](https://www.jbang.dev/documentation/jbang/latest/script-directives.html) | JBang, 2026 | все директивы `//`, shebang |
| [JBang: dependencies](https://www.jbang.dev/documentation/jbang/latest/dependencies.html) | JBang | `//DEPS`, BOM, репозитории |
| [JBang: running and execution](https://www.jbang.dev/documentation/jbang/latest/running.html) | JBang | shebang, `//JAVA`, опции |
| [JBang: exporting](https://www.jbang.dev/documentation/jbang/latest/exporting.html) | JBang | portable, fatjar, native, maven |
| [JBang: installing scripts](https://www.jbang.dev/documentation/jbang/latest/install.html) | JBang | `jbang app install` |
| [JBang: aliases and catalogs](https://www.jbang.dev/documentation/jbang/latest/alias_catalogs.html) | JBang | обмен инструментами |
| [jbangdev/jbang](https://github.com/jbangdev/jbang) | команда JBang, 2026 | релизы |
| [JEP 330: Launch single-file source-code programs](https://openjdk.org/jeps/330) | Jonathan Gibbons, 2018 | `java Script.java`, shebang |
| [JShell scripts (Oracle guide)](https://docs.oracle.com/en/java/javase/25/jshell/scripts.html) | Oracle | скрипты `.jsh` |
| [dev.java: running single-file programs](https://dev.java/learn/single-file-program/) | Oracle Java DevRel | учебник по запуску исходников |
| [dev.java: JShell tool](https://dev.java/learn/jshell-tool/) | Oracle Java DevRel | учебник по JShell |
| [zt-exec](https://github.com/zeroturnaround/zt-exec) | ZeroTurnaround | помощник для запуска процессов |

## Заброшенные и затихшие проекты

Состояние на 2026-11-06. «Давно без релизов» — больше 18 месяцев без релиза; проект ещё можно использовать с осторожностью.

| Проект | Последний релиз | Чем заменить |
|---|---|---|
| Sonatype OSSRH (oss.sonatype.org) | закрыт 2025-06-30 `[устарело]` | Central Publisher Portal |
| `com.github.johnrengelman.shadow` | плагин переехал `[устарело]` | `com.gradleup.shadow` 9.x |
| picocli | 4.7.7, 2025-04 (коммиты идут) | остаётся выбором по умолчанию; следите за мейнтейнером |
| JCommander | 1.82, 2022-01 | picocli, Airline |
| jopt-simple | 5.0.4 / 6.0-alpha-3, 2018 | picocli |
| Revapi | 0.15.1, 2025-04 (темп упал) | japicmp |
| IJava | 1.3.0, 2019 `[устарело]` | JJava |
| BeakerX | push 2023-12 | JJava, Kotlin Notebook |
| Tablesaw | 0.44.4, медленно | DFLib |
| Apache Commons Math | 3.6.1 (2016), 4.0-beta1 (2022) | Hipparchus; Commons Numbers, Statistics, RNG |
| ND4J / Deeplearning4j | 1.0.0-M2.1, 2022-08 | DJL, ONNX Runtime, пресеты JavaCPP для PyTorch |
| Aparapi | 3.0.2, 2024-03 | TornadoVM |
| Jlama | 0.8.4, 2025-01 | ONNX Runtime, jitllm/GPULlama3, Ollama |
| java-llama.cpp | push 2025-06 | Ollama по HTTP |
| Tribuo | 4.3.2, 2025-04 (релизы редкие) | остаётся пригодным; Smile, если подходит лицензия (GPLv3 или коммерческая) |
| Eclipse Paho Java MQTT | клиент MQTTv5 1.2.5 (2020); push 2024-08 | HiveMQ MQTT Client |
| diozero | 1.4.1, 2024-10 | Pi4J 4 |
| RXTX | давно мёртв `[устарело]` | jSerialComm |
| Artemis-odb / Ashley / Dominion (ECS) | 2019–2024 | простые компоненты, свой ECS |
| KryoNet | push 2021 (форк 2023) | Netty, сокеты на виртуальных потоках |
| mini2Dx / jBox2D / AsciiPanel / SquidLib | 2020–2024 | libGDX, Box2D из libGDX, SquidSquad |
| javafx-maven-plugin | 0.0.8, 2021 (работает) | badass-jlink / jpackage |
| Координаты GWT `com.google.gwt` | 2.10.0, 2022 `[устарело]` | `org.gwtproject` 2.13.x (только старые приложения) |
| wasmtime-java | 0.19.0, 2023 | Chicory, GraalWasm |
| Jython | 2.7.x (Python 2) `[устарело]` | GraalPy |
| Nashorn в составе JDK | удалён в JDK 15 `[устарело]` | GraalJS, отдельный Nashorn 15.7 |
| Applet API / Java Web Start | удалены в JDK 26 / JDK 11 `[устарело]` | приложения jpackage, CheerpJ, OpenWebStart |
| Graal JIT в Oracle JDK | прекращён в 2025 `[устарело]` | C2 |
| Java SE Embedded / Compact Profiles | прекращены `[устарело]` | рантаймы jlink |

## Чеклист ревью публикуемой библиотеки

Проходите список перед каждым релизом и при ревью нового модуля. Пункты, помеченные разделом, подробно разобраны там.

**Публикация и подпись**

- [ ] Публикация идёт через Central Portal (central-publishing-maven-plugin, vanniktech, nmcp или JReleaser); в сборке нет `oss.sonatype.org`, `nexus-staging-maven-plugin` и gradle-nexus publish-plugin (§1).
- [ ] groupId — подтверждённое пространство имён (DNS TXT или `io.github.<user>`).
- [ ] В POM есть name, description, url, лицензия, developer и scm (connection, developerConnection, url).
- [ ] Релизная версия не `-SNAPSHOT`; публикация SNAPSHOT идёт в отдельный репозиторий.
- [ ] Вместе с основным JAR публикуются `-sources.jar` и `-javadoc.jar`.
- [ ] Все артефакты подписаны PGP (`.asc`); ключ не хранится в репозитории, на CI он передаётся секретом (например, `signingInMemoryKey`).
- [ ] Учётные данные Portal — user token, а не пароль; токен лежит в секретах CI.
- [ ] `autoPublish` включён осознанно: для CI `true`, для ручного релиза `false` с проверкой в Portal.
- [ ] Сборка воспроизводима: задан `project.build.outputTimestamp` (Maven) или отключены метки времени файлов (Gradle); `mvn artifact:compare` проходит.

**Совместимость и API**

- [ ] Целевой уровень задан через `--release N` (17 для широкого охвата), а не только `-source`/`-target`.
- [ ] В фазе `verify` работает japicmp (или Revapi); нарушение совместимости без повышения major блокирует релиз.
- [ ] Версия соответствует semver: бинарно несовместимое изменение повышает major.
- [ ] Публичная поверхность минимальна: остальное package-private, классы `final`, точки расширения явно закрыты через sealed.
- [ ] Сигнатуры возвращают интерфейсы и не содержат типов сторонних библиотек.
- [ ] В интерфейсы не добавлены абстрактные методы (только `default`); `static final` константы не меняются между релизами.
- [ ] Nullness описана через JSpecify (`@NullMarked`, `@Nullable`).
- [ ] Устаревшее помечено `@Deprecated(since = "...", forRemoval = true)` минимум за один major до удаления; нестабильные API помечены `@API(status = EXPERIMENTAL)`.
- [ ] В публичном API нет типов инкубаторных модулей (например, Vector API).
- [ ] Ведётся `CHANGELOG.md`, где отмечены ломающие изменения.

**Модули и упаковка**

- [ ] Есть `module-info.java` или хотя бы `Automatic-Module-Name` в манифесте; имя совпадает с корневым пакетом.
- [ ] Нет split-пакетов между JAR; `requires transitive` стоит только для типов, видимых в API; необязательные зависимости — `requires static`.
- [ ] Multi-release JAR (если используется) проверен: публичный API одинаков во всех версиях, каждый вариант протестирован на своём JDK.
- [ ] Shading не применяется по умолчанию; если применяется, включены relocation, `ServicesResourceTransformer` или `mergeServiceFiles()` и `createDependencyReducedPom`.
- [ ] Gradle Module Metadata включена, если репозиторий её не отвергает; `api` и `implementation` разведены осознанно.

**Зависимости и качество**

- [ ] Минимум зависимостей: вместо Guava и Commons использованы возможности JDK; необязательные интеграции помечены `<optional>true</optional>` или вынесены в feature variants.
- [ ] Логирование только через SLF4J API, без binding; для многомодульных библиотек опубликован BOM.
- [ ] Javadoc собирается без ошибок; комментарии Markdown (`///`) проверены рендером на JDK 23+ (не разорваны пустыми строками).
- [ ] Тесты запускаются на всех поддерживаемых JDK (минимум нижняя граница и последний LTS); для библиотек с GraalVM есть метаданные достижимости или проверка Native Image.
- [ ] Зависимости проверены на уязвимости и лицензионную совместимость (GPL-зависимости вроде Smile не попали транзитивно в библиотеку под другой лицензией).

## Непроверенные места и список наблюдения

**Данные, которые стоит перепроверить до использования**

- Точная формулировка анонса Oracle о GraalVM от сентября 2025 года: прочитано только вторичное покрытие (ADTmag отдаёт ботам 403).
- Статус ошибок jpackage с WiX 5/6: JBS отдаёт ботам 403.
- Minecraft 26.1: детали про Java 25 и отказ от обфускации взяты из блога NeoForge и страниц сообщества, не из заметок Mojang.
- Java Card Development Kit 26.0: страница загрузок Oracle видна только через результаты поиска.
- Compose Multiplatform 1.12: блог JetBrains через поиск, страница вернула HTTP 202.
- Дата Spring AI 2.0 GA: блог Spring через поиск.
- Размер JAR у DuckDB JDBC; выход Arrow Java из отдельного репозитория (репозиторий существует, каденс не проверен).
- Заявления о дизайне TUI4J; заявления о движках у отдельных игр (общеизвестны, но не перепроверялись).

**Что отслеживать при следующем обновлении**

- Сопровождение picocli.
- Планы GraalVM по JDK 27 (по плану нет до 2027-02).
- JEP Leyden в JDK 28 (компиляция AOT-кода).
- Выход Vector API из инкубатора после preview Valhalla.
- Первый JEP Babylon.
- TamboUI 1.0.
- Sigstore как обязательное требование Maven Central.
- Официальный плагин Sonatype для Gradle.
- JavaFX 28 (2027-03).
- Каденс LTS DuckDB 2.x.
- Spring AI 2.1 и LangChain4j 2.0 (если будут объявлены).
