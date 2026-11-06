# Производительность Java: замеры, профилирование и оптимизация

Справочник для ускорения Java-кода и снижения его памяти в любой области: библиотеки, сервисы, пакетные задачи, CLI, клиентские приложения. Здесь есть таблица «симптом → техника → источник» для быстрого поиска, порядок работы и правила честного бенчмаркинга, JMH в деталях (настройка Maven и Gradle, аннотации, профайлеры, схема JSON), макробенчмарки и бенчмарки в CI, профилирование (JFR, JMC, async-profiler, IDE), внутренности JIT, память и GC, старт и прогрев (CDS, AOT-кэш Leyden, CRaC, native image), техники в коде с доказательствами, анти-паттерны и разборы реальных случаев. Актуально на ноябрь 2026: JDK 25 — LTS (Temurin 25.0.5+8, CPU от 20 октября 2026), JDK 27 — последняя версия (GA 15 сентября 2026), JDK 28 в разработке; JMH 1.37, плагин Gradle `me.champeau.jmh` 0.7.3, async-profiler 4.6, JDK Mission Control 9.1.2, VisualVM 2.2.2, JITWatch 1.7.0, GraalVM Community 25 Innovation 5. Метки: без метки — источник или версия проверены; `[не проверено]` — единственный вторичный источник или страница не открывалась; `[устарело]` — заменено тем, что названо рядом; «проверено локально (JDK 25, Windows 11)» — воспроизведено на Temurin 25.0.5+8, AMD Ryzen 9 5950X (16 ядер, 32 потока), 32 ГБ, от сети, Maven 3.9.16, Gradle 9.8.2, JMH 1.37. Машина общая, поэтому локальные результаты даны как отношения, а не как абсолютные числа. Виртуальные потоки, блокировки и модель памяти — в [concurrency-native.md](concurrency-native.md), JPA и JDBC — в [domains-server.md](domains-server.md), сборка и инструменты — в [toolchain.md](toolchain.md), тесты — в [testing.md](testing.md), идиомы — в [idioms-patterns.md](idioms-patterns.md), книги — в [books.md](books.md). Как этот файл используется на этапе оптимизации, описано в [pipeline.md](pipeline.md). Прогоны JMH «до» и «после» сравнивает скрипт [BenchCompare.java](../scripts/BenchCompare.java).

- [Версии на ноябрь 2026](#версии-на-ноябрь-2026)
- [Симптом → техника → источник](#симптом--техника--источник)
- [Как бенчмаркать честно](#как-бенчмаркать-честно)
- [Бенчмарки: JMH](#бенчмарки-jmh)
- [Макробенчмарки, нагрузочные тесты и CI](#макробенчмарки-нагрузочные-тесты-и-ci)
- [Профилирование](#профилирование)
- [JIT и внутренности JVM](#jit-и-внутренности-jvm)
- [Память и GC](#память-и-gc)
- [Старт и прогрев](#старт-и-прогрев)
- [Техники в коде](#техники-в-коде)
- [Анти-паттерны производительности](#анти-паттерны-производительности)
- [Разборы и кейсы](#разборы-и-кейсы)

---

## Версии на ноябрь 2026

Версии проверены 6 ноября 2026 по страницам OpenJDK, релизам на GitHub и `maven-metadata.xml` на Maven Central.

| Компонент | Версия, дата | Примечание |
|---|---|---|
| JDK, последняя версия | JDK 27 (GA 15 сентября 2026) | [JDK 27](https://openjdk.org/projects/jdk/27/) |
| JDK, LTS | JDK 25 (GA 16 сентября 2025); Temurin 25.0.5+8 (22 октября 2026, октябрьский CPU от 20 октября) | [JDK 25](https://openjdk.org/projects/jdk/25/); на нём сделаны локальные замеры |
| JDK 28 (в разработке) | страница обновлена 15 октября 2026 | [JDK 28](https://openjdk.org/projects/jdk/28/): JEP 401 Value Objects (Preview), 535 Shenandoah generational по умолчанию, 544 AOT-компиляция кода, 545 и 546 (старт и адаптивный размер кучи ZGC), 540 Simple JSON API (Incubator) |
| JMH | 1.37 (3 августа 2023), последняя | [jmh-core на Maven Central](https://repo.maven.apache.org/maven2/org/openjdk/jmh/jmh-core/maven-metadata.xml). Работает на JDK 25 (проверено локально) |
| `jmh-java-benchmark-archetype` | 1.37 | Целится в Java 1.8 и maven-compiler-plugin 3.8.0, на JDK 23+ собирает неработающий jar (см. [Бенчмарки: JMH](#бенчмарки-jmh)) |
| maven-compiler-plugin / maven-shade-plugin | 3.16.0 / 3.6.2 | 4.0.0-beta-5 компилятора — для Maven 4 |
| Плагин Gradle `me.champeau.jmh` | 0.7.3 (апрель 2025) | Работает на Gradle 9 с предупреждением об устаревшем `Configuration.setVisible` ([issue #296](https://github.com/melix/jmh-gradle-plugin/issues/296)); нужен Gradle 8.0+ |
| async-profiler | 4.6 (19 октября 2026) | Linux x64/arm64 и macOS. **Сборки для Windows нет**. На Maven Central — `tools.profiler:async-profiler:4.6` |
| ap-loader | 4.6-14 (26 октября 2026) | async-profiler, упакованный в jar ([релизы](https://github.com/jvm-profiling-tools/ap-loader/releases)) |
| JDK Mission Control | 9.1.2-ga | [теги openjdk/jmc](https://github.com/openjdk/jmc/tags) |
| VisualVM | 2.2.2 (8 сентября 2026) | [релизы](https://github.com/oracle/visualvm/releases) |
| JITWatch | 1.7.0 (22 сентября 2026) | [релизы](https://github.com/AdoptOpenJDK/jitwatch/releases) |
| JOL (`org.openjdk.jol:jol-core`) | 0.17 (февраль 2023) | |
| GraalVM Community | 25 Innovation 5 (graal 25.5.5.0.3, JDK 25.0.5), 3 ноября 2026 | [релизы](https://github.com/graalvm/graalvm-ce-builds/releases). С 25.1 — ежемесячные innovation-релизы ([GraalVM 25.1](https://www.graalvm.org/release-notes/25.1/)); календарь Oracle ещё описывает старую модель `[не проверено]` |
| fastutil | 8.5.19 (22 июля 2026) | |
| Eclipse Collections | 13.0.0 (июнь 2025) | |
| HPPC | 0.13.0 (4 ноября 2026) | |
| Koloboke | 1.0.0 (2016) | [устарело], заброшен; замена — fastutil, Eclipse Collections, HPPC |
| Caffeine | 3.3.1 (13 октября 2026) | |
| Jackson 2.x / 3.x | 2.22.4 (`com.fasterxml.jackson.core`) / 3.2.4 (`tools.jackson.core`) | Blackbird есть для обеих веток; Afterburner — 2.22.4 |
| DSL-JSON | 2.0.2 (август 2023) | |
| jsoniter (Java) | 0.9.23 (2018) | [устарело], заброшен; замена — DSL-JSON или Jackson + Blackbird |
| JCTools / Agrona / HdrHistogram / Disruptor | 4.0.7 / 2.7.1 / 2.2.2 / 4.0.0 | |
| log4j-core / logback-classic | 2.26.1 (3.0.0-beta3 — бета) / 1.6.5 | |
| CRaC API `org.crac:crac` | 1.5.0 (июнь 2024) | |
| Gatling | 3.16.0 (29 сентября 2026), maven-плагин 4.21.12 | |
| k6 | v2.3.0; v1.8.2 — сопровождение | [релизы](https://github.com/grafana/k6/releases) |
| wrk / wrk2 | 4.2.0 / форк Гила Тене | Оба не развиваются |
| hyperfine | 2.0.0 (7 октября 2026) | [релизы](https://github.com/sharkdp/hyperfine/releases) |
| codspeed-jvm | v0.2.1 (23 сентября 2026) | Форк JMH для CodSpeed |
| Bencher | v0.6.16 (31 октября 2026) | Есть адаптер `java_jmh` ([релизы](https://github.com/bencherdev/bencher/releases)) |
| Caliper | 1.0-beta-3 (2021) | [устарело], замена — JMH |
| jcstress | 0.16 (2023) | Корректность конкурентного кода, не скорость; см. [concurrency-native.md](concurrency-native.md) |

Изменения JDK, которые влияют на скорость:

- **JDK 24**: JEP 483 (AOT-загрузка и линковка классов); JEP 450 (компактные заголовки объектов, экспериментально); JEP 490 удалил негенерационный ZGC. Генерационный режим ZGC стал умолчанием ещё в JDK 23 (JEP 474).
- **JDK 25**: JEP 514 (`-XX:AOTCacheOutput`, AOT-кэш в один шаг), JEP 515 (профили методов в AOT-кэше), JEP 519 (компактные заголовки — продуктовая опция `-XX:+UseCompactObjectHeaders`, по умолчанию выключена), JEP 521 (генерационный Shenandoah — продуктовый), JEP 509 (CPU-time профилирование в JFR, экспериментально, только Linux), JEP 518 (кооперативный сэмплинг JFR), JEP 520 (замер и трассировка методов в JFR), JEP 508 (Vector API, 10-й инкубатор), JEP 506 (Scoped Values, финал).
- **JDK 26** (GA 17 марта 2026): JEP 516 (AOT-кэш объектов с любым GC, в том числе ZGC), JEP 522 (G1 быстрее за счёт меньшей синхронизации), JEP 517 (HTTP/3 в HttpClient), JEP 500 (подготовка к «final значит final»), JEP 529 (Vector API, 11-й инкубатор).
- **JDK 27** (GA 15 сентября 2026): JEP 523 — **G1 по умолчанию везде**, автоматического выбора Serial на 1 CPU или при памяти < 1792 МБ больше нет; JEP 534 — **компактные заголовки по умолчанию** (выключить: `-XX:-UseCompactObjectHeaders`); JEP 536 — редактирование данных JFR в процессе; JEP 537 — Vector API, 12-й инкубатор (по-прежнему `--add-modules jdk.incubator.vector`). `-XX:AOTMode=on` переименован в `required`.
- **JDK 28** (запланировано): JEP 544 (скомпилированный C2-код в AOT-кэше), JEP 545 и 546 (старт, прогрев и адаптивный размер кучи для ZGC), JEP 401 (value objects, preview).

---

## Симптом → техника → источник

Основная таблица для поиска. Начинайте с того, что показал профиль, бенчмарк или ревью. Сначала названа вероятная причина, затем что попробовать. Каждую технику подтверждайте JMH с `-prof gc` и макрозамером.

| Что видно (профиль, бенчмарк, ревью) | Техника | Источник |
|---|---|---|
| Высокий `gc.alloc.rate.norm` (байт на операцию) в горячем бенчмарке | Временные объекты, боксинг, защитные копии. Найти места: `-prof gc`, затем `jfr view allocation-by-site` или async-profiler `-e alloc`. Переиспользовать буферы, перейти на примитивы, убрать копии | [GCProfiler.java](https://github.com/openjdk/jmh/blob/master/jmh-core/src/main/java/org/openjdk/jmh/profile/GCProfiler.java) / [async-profiler: ProfilingModes.md](https://github.com/async-profiler/async-profiler/blob/master/docs/ProfilingModes.md) |
| В JMH `gc.alloc.rate.norm` около нуля, в продакшене аллокаций много | Escape analysis сработал в микробенчмарке, где всё заинлайнено, но не в реальном графе вызовов. Профилировать продакшен (`jdk.ObjectAllocationSample`), проверить инлайнинг в JITWatch | [JVM Anatomy Quark #18: Scalar Replacement](https://shipilev.net/jvm/anatomy-quarks/18-scalar-replacement/) |
| Результат бенчмарка почти равен пустому методу (< 1–2 нс) | Dead-code elimination или свёртка констант. Возвращать результат или отдавать его в `Blackhole`, входы брать из не-final полей `@State`. Проверено локально (JDK 25, Windows 11): выброшенный результат дал оценку в 14 раз ниже реальной цены | [JMHSample_08_DeadCode](https://github.com/openjdk/jmh/blob/master/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_08_DeadCode.java) / [JMHSample_10_ConstantFold](https://github.com/openjdk/jmh/blob/master/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_10_ConstantFold.java) / [Как бенчмаркать честно](#как-бенчмаркать-честно) |
| Большая погрешность, результаты расходятся между запусками | Шум: turbo, чужие процессы, GC внутри итераций, мало форков. Больше форков и итераций, тихая машина, `-prof gc` и `-prof pauses`, чтобы увидеть GC; сравнивать доверительные интервалы | [JMHSample_13_RunToRun](https://github.com/openjdk/jmh/blob/master/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_13_RunToRun.java) / [Как бенчмаркать честно](#как-бенчмаркать-честно) |
| Прогревочные итерации всё ещё снижаются | JIT не закончил, работает OSR-код. Удлинить прогрев, посмотреть `-prof comp`, убрать цикл из тела бенчмарка | [JMHSample_11_Loops](https://github.com/openjdk/jmh/blob/master/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_11_Loops.java) / [JMHSample_34_SafeLooping](https://github.com/openjdk/jmh/blob/master/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_34_SafeLooping.java) |
| В профиле кадры `itable stub` или `vtable stub`, мегаморфный вызов | В горячей точке 3 и больше типов получателя. Сократить число типов в этой точке, разделить цикл по типам, sealed-иерархия + `switch`, вынести диспетчеризацию из цикла | [JVM Anatomy Quark #16: Megamorphic Virtual Calls](https://shipilev.net/jvm/anatomy-quarks/16-megamorphic-virtual-calls/) / [The Black Magic of (Java) Method Dispatch](https://shipilev.net/blog/2015/black-magic-method-dispatch/) |
| `hot method too big` или `too big` в выводе `-XX:+PrintInlining` | Метод больше `FreqInlineSize` (325 байт байткода) или `MaxInlineSize` (35). Разделить метод: быстрый путь короткий, редкий путь в отдельный метод | [Server Compiler Inlining Messages](https://wiki.openjdk.org/display/HotSpot/Server+Compiler+Inlining+Messages) `[не проверено]` |
| Много времени в `HashMap.resize` | Карта растёт с ёмкости по умолчанию. `HashMap.newHashMap(n)`; вызывающих найти через JFR `method-trace=java.util.HashMap::resize`. Проверено локально (JDK 25, Windows 11): заранее заданный размер быстрее в 1,6–2,6 раза | [HashMap.newHashMap](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/HashMap.html#newHashMap(int)) / [JEP 520: JFR Method Timing & Tracing](https://openjdk.org/jeps/520) |
| Много времени в `HashMap.getNode`, `TreeNode.find`, `equals` | Плохой `hashCode`, коллизии, дорогой `equals`. Лучший хеш (record или `Objects.hash`, либо свой), кэшировать хеш, ключи меньше | [HashMap (Java SE 25)](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/HashMap.html#newHashMap(int)) |
| `Integer.valueOf`, `Long.valueOf`, `intValue` горячие | Боксинг в коллекциях и стримах. Примитивные массивы, `IntStream`, fastutil, Eclipse Collections, HPPC. Проверено локально (JDK 25, Windows 11): `List<Integer>` в 5 раз медленнее `int[]` | [fastutil](https://fastutil.di.unimi.it/) / [Локальные замеры](#локальные-замеры-jmh) |
| Горячие кадры `java.util.Formatter` или `String.format` | Строка формата разбирается при каждом вызове. Конкатенация (indy), `StringBuilder`, заранее собранные шаблоны. Проверено локально (JDK 25, Windows 11): `String.format` в 9–10,5 раза медленнее конкатенации | [JEP 280: Indify String Concatenation](https://openjdk.org/jeps/280) / [Локальные замеры](#локальные-замеры-jmh) |
| `Pattern.compile` горячий | `split`, `replaceAll`, `matches` в цикле. `private static final Pattern`; разбор через `indexOf` | [Pattern (Java SE 25)](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/regex/Pattern.html) |
| `Throwable.fillInStackTrace` горячий | Исключения как управление потоком. Возвращать тип-результат, исключения без стека, проверять до разбора | [The Exceptional Performance of Lil' Exception](https://shipilev.net/blog/2014/exceptional-performance/) |
| В логе NPE или AIOOBE без стека | `OmitStackTraceInFastThrow` после многих бросков. Для отладки `-XX:-OmitStackTraceInFastThrow`; сам поток исключений — проблема, его надо убрать | [The java Command](https://docs.oracle.com/en/java/javase/25/docs/specs/man/java.html) |
| Горячие кадры логирования (`AbstractStringBuilder`, аппендеры, `StackWalker`) | Сборка строк, location info, синхронные аппендеры. Параметризованные сообщения, guard-проверки, асинхронные аппендеры, убрать `%L` | [Performance :: Apache Log4j](https://logging.apache.org/log4j/2.x/manual/performance.html) |
| `Method.invoke` и рефлексия горячие | Рефлексия на горячем пути. Кэшировать поиск, `static final MethodHandle`, `LambdaMetafactory`, генерация кода | [JEP 416: Reimplement Core Reflection with Method Handles](https://openjdk.org/jeps/416) |
| Длинные паузы G1 на большой куче | Большой live set, humongous-объекты, сбои эвакуации. Уменьшить live set и humongous-аллокации, настроить `MaxGCPauseMillis`, попробовать генерационный ZGC | [HotSpot GC Tuning Guide, Release 25](https://docs.oracle.com/en/java/javase/25/gctuning/) / [Netflix: Bending Pause Times to Your Will with Generational ZGC](https://inside.java/2024/03/06/generational-zgc-netflix) |
| `Allocation Stall` в логах ZGC | Куча мала для такой скорости аллокаций. Больше кучи или `-XX:SoftMaxHeapSize`, меньше аллокаций | [The Z Garbage Collector (Java SE 25)](https://docs.oracle.com/en/java/javase/25/gctuning/z-garbage-collector1.html) |
| Частые young GC, высокая доля CPU на GC | Высокая скорость аллокаций. Сначала снизить байты на операцию (первая строка таблицы), потом увеличить young gen | [HotSpot GC Tuning Guide, Release 25](https://docs.oracle.com/en/java/javase/25/gctuning/) / [Память и GC](#память-и-gc) |
| Full GC, old gen растёт после каждой сборки | Утечка или кэш без границ. Heap dump в MAT, JFR `memory-leaks-by-site`, границы в Caffeine | [Memory Analyzer (MAT)](https://www.eclipse.org/mat/) |
| `OutOfMemoryError: Java heap space` в контейнере, хотя память у контейнера есть | Максимальная куча по умолчанию — 25% лимита контейнера. `-XX:MaxRAMPercentage=50..75`, проверить через `-XX:+PrintFlagsFinal` | [Containerize your Java Applications](https://learn.microsoft.com/en-us/azure/developer/java/containers/overview) |
| Контейнер убит по OOM, а куча в порядке | Нативная память: metaspace, стеки потоков, direct-буферы, code cache. NMT и `jcmd VM.native_memory`, лимит `MaxDirectMemorySize`, меньше потоков | [JVM Anatomy Quark #12: Native Memory Tracking](https://shipilev.net/jvm/anatomy-quarks/12-native-memory-tracking/) |
| Всплески задержки с периодом 100 мс в Kubernetes | Троттлинг CPU по квоте CFS. `jfr view container-cpu-throttling`, поднять лимит CPU, `-XX:ActiveProcessorCount` | [The jfr Command](https://docs.oracle.com/en/java/javase/25/docs/specs/man/jfr.html) / [View Command (Erik Gahlin)](https://egahlin.github.io/2023/05/30/views.html) |
| Сервис стартует секунды | Загрузка и линковка классов, прогрев JIT, инициализация фреймворка. AOT-кэш (`-XX:AOTCacheOutput` в JDK 25), ленивая инициализация, CRaC, native image | [JEP 483: Ahead-of-Time Class Loading & Linking](https://openjdk.org/jeps/483) / [JEP 514: Ahead-of-Time Command-Line Ergonomics](https://openjdk.org/jeps/514) |
| Медленная статическая инициализация (`<clinit>`) | Тяжёлый static-блок. `-XX:StartFlightRecording:method-timing=::<clinit>`, затем `jfr view method-timing`. Проверено локально (JDK 25, Windows 11): медленный инициализатор найден сразу | [JEP 520: JFR Method Timing & Tracing](https://openjdk.org/jeps/520) |
| Первые запросы после деплоя медленные | Код в интерпретаторе и C1, холодные кэши. AOT-кэш с профилями (JEP 515), прогревочный трафик, в JDK 28 — AOT-код | [JEP 515: Ahead-of-Time Method Profiling](https://openjdk.org/jeps/515) / [JEP 544: Ahead-of-Time Code Compilation](https://openjdk.org/jeps/544) |
| Конкуренция за блокировки (`jdk.JavaMonitorEnter`, `jdk.ThreadPark`) в JFR | Горячая общая блокировка. Короче критическая секция, шардирование, `LongAdder`, lock-free структуры, `ReentrantLock`. Подробно — [concurrency-native.md](concurrency-native.md) | [The jfr Command](https://docs.oracle.com/en/java/javase/25/docs/specs/man/jfr.html) (`contention-by-site`) |
| Виртуальные потоки закреплены (`jdk.VirtualThreadPinned`) | `synchronized` с блокировкой внутри (JDK 21–23) или нативные кадры. Перейти на JDK 24+ (JEP 491) или на `ReentrantLock` | [JEP 491: Synchronize Virtual Threads without Pinning](https://openjdk.org/jeps/491) |
| Пропускная способность не растёт с ядрами, perfc2c показывает HITM | False sharing. Паддинг или `@Contended`, состояние на поток | [JMHSample_22_FalseSharing](https://github.com/openjdk/jmh/blob/master/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_22_FalseSharing.java) / [JEP 142: Reduce Cache Contention on Specified Fields](https://openjdk.org/jeps/142) |
| `Class.isInstance` или `checkcast` всё дороже с ростом числа потоков | Конкуренция за secondary super cache («type pollution»). Обновить JDK (JDK-8180450 исправлен в новых JDK, версия `[не проверено]`) или сократить проверки на интерфейсные типы | [Journey to a N-fold improvement due to a stealthy scalability issue](https://redhatperf.github.io/post/type-check-scalability-issue/) |
| Много деоптимизаций (`jfr view deoptimizations-by-reason`) | Меняются профили типов и ветвлений, иерархия классов. Стабилизировать типы в горячих точках, прогревать на представительных данных | [JVM Anatomy Quark #29: Uncommon Traps](https://shipilev.net/jvm/anatomy-quarks/29-uncommon-traps/) |
| `CodeCache is full. Compiler has been disabled` | Code cache исчерпан. Поднять `-XX:ReservedCodeCacheSize`, искать бесконечно генерируемые классы | [The java Command](https://docs.oracle.com/en/java/javase/25/docs/specs/man/java.html) |
| Горячие `LinkedList.get` или `LinkedList.node` | Доступ по индексу к связному списку. `ArrayList` | [Java Objects Inside Out](https://shipilev.net/jvm/objects-inside-out/) / [Техники в коде](#техники-в-коде) |
| Кадры стрима доминируют на горячем пути с маленькой коллекцией | Стоимость сборки конвейера, лямбды не заинлайнены. Обычный цикл или примитивный стрим. Проверено локально (JDK 25, Windows 11) | [Локальные замеры](#локальные-замеры-jmh) / [Техники в коде](#техники-в-коде) |
| Горячая арифметика `BigDecimal` | Произвольная точность на каждой операции. Фиксированная точка в `long`, меньше конвертаций `[не проверено]` | [Техники в коде](#числа-off-heap-и-simd) |
| `SimpleDateFormat` или `DateTimeFormatter.ofPattern` на каждый вызов | Создание форматтера. `static final DateTimeFormatter` | [DateTimeFormatter (Java SE 25)](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/time/format/DateTimeFormatter.html) |
| Горячий конструктор `ObjectMapper` или `TypeFactory` | Новый маппер на каждый вызов. Один общий маппер, `ObjectReader`/`ObjectWriter`, Blackbird | [Presentation: Jackson Performance](https://github.com/FasterXML/jackson-docs/wiki/Presentation:-Jackson-Performance) |
| Сериализация JSON занимает основной CPU | Биндинг через рефлексию, декодирование строк. Blackbird, потоковый API, JSON на этапе компиляции (DSL-JSON), бинарные форматы | [java-json-benchmark](https://github.com/fabienrenaud/java-json-benchmark) |
| `FileInputStream.read` или `readBytes` с крошечными чтениями, много системных вызовов | Небуферизованный I/O. Буферизованные потоки, буферы больше, `readAllBytes` | [Техники в коде: I/O](#io-сериализация-http-кэш-и-бд) |
| Многогигабайтный файл разбирается медленно | Декодирование в `String` построчно, один поток. `FileChannel.map` → `MemorySegment`, параллельный разбор кусков по байтам | [The Billion Row Challenge (1BRC): 71s → 1.7s](https://questdb.com/blog/billion-row-challenge-step-by-step/) |
| Файлы и сокеты копируются через user space | Циклы read/write. `FileChannel.transferTo` (zero-copy) | [Java ZeroCopy I/O optimization for high throughput networking](https://developer.ibm.com/articles/j-zerocopy/) |
| Много коротких HTTP-соединений и TLS-рукопожатий | Новый клиент на каждый запрос. Один общий `HttpClient`, keep-alive, HTTP/2 | [HttpClient (Java SE 25)](https://docs.oracle.com/en/java/javase/25/docs/api/java.net.http/java/net/http/HttpClient.html) |
| Время БД съедают однотипные мелкие запросы | N+1. Join fetch, entity graph, batch fetching | [N+1 query problem with JPA and Hibernate](https://vladmihalcea.com/n-plus-1-query-problem/) |
| Вставки медленные | Отдельный запрос на каждую строку. JDBC batching, `hibernate.jdbc.batch_size` | [Hibernate performance tuning tips](https://vladmihalcea.com/hibernate-performance-tuning-tips/) |
| Потоки ждут в `HikariPool.getConnection` | Пул исчерпан или запросы медленные. Сначала ускорить запросы, затем размер пула по формуле, таймауты | [About Pool Sizing](https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing) |
| Низкий hit rate кэша или растущая память | Не та политика вытеснения или кэш без границ. Caffeine с `maximumSize`, `recordStats()` | [Caffeine wiki: Efficiency](https://github.com/ben-manes/caffeine/wiki/Efficiency) |
| Горячий цикл не векторизован (perfasm показывает скалярный код) | Сложное тело цикла, индекс `long`, данные не в массиве. Упростить цикл, индекс `int`, массивы, Vector API (инкубатор) | [JEP 537: Vector API (Twelfth Incubator)](https://openjdk.org/jeps/537) |
| Много промахов предсказания ветвлений (perfnorm) | Ветвления зависят от данных. Код без ветвлений, предварительная сортировка, код под cmov | [JMHSample_36_BranchPrediction](https://github.com/openjdk/jmh/blob/master/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_36_BranchPrediction.java) / [JVM Anatomy Quark #30: Conditional Moves](https://shipilev.net/jvm/anatomy-quarks/30-conditional-moves/) |
| Много промахов кэша (perfnorm, L1 и LLC) | Переходы по указателям, боксинг, большой шаг доступа. Массивы примитивов, struct-of-arrays, объекты меньше (компактные заголовки) | [JMHSample_37_CacheAccess](https://github.com/openjdk/jmh/blob/master/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_37_CacheAccess.java) / [JMHSample_39_MemoryAccess](https://github.com/openjdk/jmh/blob/master/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_39_MemoryAccess.java) / [JEP 534: Compact Object Headers by Default](https://openjdk.org/jeps/534) |
| Кучу занимают миллионы мелких объектов (заголовки) | Накладные расходы на объект. Компактные заголовки (флаг в JDK 25, умолчание в JDK 27), примитивные коллекции | [JEP 519: Compact Object Headers](https://openjdk.org/jeps/519) |
| Куча чуть больше 32 ГБ работает хуже, чем 31 ГБ | Сжатые указатели выключились. Держать кучу < 32 ГБ или принять 8-байтовые ссылки | [JVM Anatomy Quark #23: Compressed References](https://shipilev.net/jvm/anatomy-quarks/23-compressed-references/) |
| Время в профиле приходится на safepoint poll или на код после цикла | Safepoint bias у профайлера. async-profiler или JFR, флаг `-XX:+DebugNonSafepoints` | [Why (Most) Sampling Java Profilers Are Fucking Terrible](http://psy-lob-saw.blogspot.com/2016/02/why-most-sampling-java-profilers-are.html) |
| Долгий time-to-safepoint (`jfr view safepoints`) | Длинные counted-циклы, огромные копирования массивов. Обновить JDK (loop strip mining), дробить работу | [JVM Anatomy Quark #22: Safepoint Polls](https://shipilev.net/jvm/anatomy-quarks/22-safepoint-polls/) |
| Много одинаковых `String` в heap dump | Одни и те же разобранные значения. Словарь для дедупликации, `-XX:+UseStringDeduplication` | [The java Command](https://docs.oracle.com/en/java/javase/25/docs/specs/man/java.html) |
| Среднее в нагрузочном тесте хорошее, а пользователи жалуются | Среднее прячет хвосты; coordinated omission. Перцентили через HdrHistogram, открытая модель нагрузки (wrk2, k6 arrival rate) | [How NOT to Measure Latency](https://www.youtube.com/watch?v=lJ8ydIuPFeU) / [wrk2](https://github.com/giltene/wrk2) |
| «Регрессии» бенчмарков в CI, которые исчезают в следующем прогоне | Шум раннера выше порога. Измерить шум, поднять порог, выделенные раннеры, CodSpeed или Bencher | [Benchmark Harness Adapters (Bencher)](https://bencher.dev/docs/explanation/adapters/) / [CodSpeed Docs](https://codspeed.io/docs) |
| Native image на пике медленнее JVM | Нет профиля JIT, Serial GC. PGO (Oracle GraalVM), G1 в native image или остаться на JIT + AOT-кэш | [Native Image: Optimizations and Performance](https://www.graalvm.org/latest/reference-manual/native-image/optimizations-and-performance/) |
| Предупреждения про `Unsafe` при старте (JDK 24+) | Библиотека использует доступ к памяти через `sun.misc.Unsafe`. Обновить библиотеку; в своём коде — FFM и `VarHandle` | [JEP 498: Warn upon Use of Memory-Access Methods in sun.misc.Unsafe](https://openjdk.org/jeps/498) |
| `MaxRAMPercentage` не действует, JVM видит все CPU хоста | Старый JDK без поддержки cgroup v2. JDK 17+, 21+ или 25; `-XX:ActiveProcessorCount` | [Containerize your Java Applications](https://learn.microsoft.com/en-us/azure/developer/java/containers/overview) |
| После перехода Jackson 2.x → 3.x разбор замедлился | Новый пул буферов `RecyclerPool` по умолчанию, проверка хвостовых токенов. Сравнить с `JsonRecyclerPools.threadLocalPool()`, для доверенного ввода выключить `FAIL_ON_TRAILING_TOKENS` | [Migrating to Jackson 3](https://github.com/FasterXML/jackson/blob/main/jackson3/MIGRATING_TO_JACKSON_3.md) |
| `Formatter.parse` наверху `jfr view allocation-by-site` | `String.format` на горячем пути. Конкатенация или `StringBuilder`. Проверено локально (JDK 25, Windows 11): на `Formatter.parse` пришлось 72% аллокаций | [View Command (Erik Gahlin)](https://egahlin.github.io/2023/05/30/views.html) / [Локальные замеры](#локальные-замеры-jmh) |
| Один и тот же код даёт в 1,3–1,5 раза разные числа в разных запусках JVM | Размещение объектов в куче и недетерминизм JIT. Больше форков, чередование A/B, смотреть `rawData` по форкам. Проверено локально (JDK 25, Windows 11) | [JMHSample_13_RunToRun](https://github.com/openjdk/jmh/blob/master/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_13_RunToRun.java) / [Как бенчмаркать честно](#как-бенчмаркать-честно) |

---

## Как бенчмаркать честно

**Ключевые выводы**

- Порядок атаки: (1) убрать работу — алгоритм, структура данных, сетевые и дисковые обращения, N+1, лишняя сериализация; (2) убрать аллокации и косвенность на горячем пути; (3) только потом флаги JVM. Флаги редко дают больше, чем исправление кода в 2 раза. Флаги GC лечат проблемы GC, а не медленный код.
- Начинайте с вопроса и цели: «p99 < 50 мс при 2000 запросов в секунду», «разобрать 1 ГБ быстрее 3 с». Нужны воспроизводимая нагрузка и базовая линия в машиночитаемом виде: JMH `-rf json`, JSON Gatling или k6, файл JFR. После каждого изменения — новый замер. Одно изменение на замер.
- Микро и макро. JMH отвечает на вопрос «какая из двух реализаций маленького участка быстрее под JIT». Он не отвечает «станет ли сервис быстрее». Подтверждайте выигрыш макротестом (нагрузка, время всей задачи) и профилем, где этот код действительно горячий.
- Закон Амдала: ускорение в 10 раз кода, занимающего 5% времени, даёт не больше ~5% в целом: 1 / (0,95 + 0,05/10) = 1,047.
- Сначала профиль, потом бенчмарк. Профиль (JFR, async-profiler) находит, куда уходят время и аллокации в реальном приложении. JMH затем изолирует и сравнивает варианты исправления. Бенчмарк без профиля ведёт к оптимизации холодного кода.
- До кода посмотрите на ресурсы и сервис: метод USE (utilization, saturation, errors) для ресурсов и RED (rate, errors, duration) для сервисов (Брендан Грегг).
- Задержку меряйте перцентилями через HdrHistogram, не средним. Опасайтесь coordinated omission: генератор нагрузки с закрытой моделью ждёт медленных ответов и прячет очереди. Берите открытую модель: Gatling `constantUsersPerSec`, k6 `constant-arrival-rate`, wrk2 `-R`.

### Источники шума, специфичные для JVM

- Прогрев JIT и многоуровневая компиляция: числа меняются первые секунды или минуты. OSR (on-stack replacement): долгий цикл компилируется иначе, чем обычный вызов метода. Деоптимизация, когда появляется новый тип или ветка.
- Загрязнение профиля: в одной JVM меряют несколько реализаций, и точки вызова становятся мегаморфными. Форки JMH это устраняют. GC может попасть в любую итерацию; размер кучи по умолчанию влияет на частоту сборок.
- Частота CPU, turbo/boost, тепловой троттлинг, ноутбук от батареи (план питания режет частоту), SMT-соседи, фоновые процессы.
- Windows: `System.nanoTime` на современной Windows точен, но у `Thread.sleep` и `LockSupport.parkNanos` гранулярность ~0,5–15,6 мс без высокоточных таймеров.

### Дисциплина замеров

- Одна машина, питание от сети, план питания «высокая производительность», ничего другого не запущено.
- Закрепить версию JDK и флаги. Несколько форков. Показывать погрешность. Сравнивать распределения, а не одиночные числа.
- Если доверительные интервалы двух результатов пересекаются, разница не доказана.
- **Никогда не запускайте два JMH-прогона одновременно.** JMH берёт общий для машины файл-замок `jmh.lock` во временном каталоге. Второй параллельный запуск падает с `Unable to acquire the JMH lock`. Флаг `-Djmh.ignoreLock=true` обходит замок, но два прогона на одних ядрах портят друг другу замеры. Прогоны идут строго по очереди.

### Шум между запусками: локальные числа

Проверено локально (JDK 25, Windows 11): один и тот же jar, три отдельных запуска JVM, `-f 1 -wi 3 -i 5`, две сессии (20 октября и 6 ноября 2026).

| Бенчмарк | Разброс max/min, октябрь | Разброс max/min, ноябрь |
|---|---|---|
| `StringBench.concat` | 1,16 | 1,12 |
| `MapBench.presized` | 1,21 | 1,20 |
| `BoxingBench.loopBoxed` | 1,22 | 1,15 |

- Разброс между запусками — **12–22%**. Это больше, чем «±» внутри одного прогона, который печатает JMH.
- Прогон с 2 форками и `-prof gc` дал `loopBoxed` одинаково в обоих форках, а три однофорковых запуска — в 1,3–1,5 раза медленнее при том же коде. Вероятные причины (гипотеза): размещение 10 000 объектов `Integer` в куче (GC их перемещает, ср. Quark #11 «Moving GC and Locality») и разные решения JIT в разных JVM. Поэтому в JMH есть форки, и одного форка мало.
- Правило: на неизолированной машине разницу меньше ~25% считайте шумом, пока она не подтверждена ≥ 3 форками в чередующемся A/B-прогоне в одной сессии.
- Отношения внутри одного прогона стабильны: `String.format` против конкатенации — 9–10,5 раза в обеих сессиях, хотя абсолютные числа сдвинулись.
- `BenchCompare.java` поэтому сравнивает несколько прогонов на сторону в порядке ABBA: `java ../scripts/BenchCompare.java bench/before bench/after --threshold 5`.

### Демонстрация dead-code elimination

C2 удаляет вычисление, результат которого никуда не идёт, и сворачивает константы. Проверено локально (JDK 25, Windows 11). Схема бенчмарка упрощена; `compute` — чистая арифметика:

```java
@State(Scope.Thread)
public class DceArith {
    double x = Math.PI;                      // не final: иначе свёртка констант

    @Benchmark public void baseline() { }                          // пустой метод
    @Benchmark public void wrongDiscard() { compute(x); }          // результат выброшен
    @Benchmark public double wrongConstant() { return compute(Math.PI); } // вход-константа
    @Benchmark public double rightReturn() { return compute(x); }  // верно
    @Benchmark public void rightBlackhole(Blackhole bh) { bh.consume(compute(x)); } // верно
}
```

| Вариант | Время относительно `baseline` | Вывод |
|---|---|---|
| `baseline` | 1 | пустой метод |
| `wrongDiscard` | ×2,4 | в 14 раз «быстрее» реальной цены: работа удалена |
| `wrongConstant` | ≈ ×1 | свёрнуто в ничто |
| `rightReturn` | ×34 | реальная цена |
| `rightBlackhole` | ×30 | реальная цена (compiler blackhole) |

- DCE реален, но непредсказуем. В октябрьской сессии демо на `Math.log(x)` (как в JMHSample_08) на JDK 25 под Windows **не** было удалено, а чистая арифметика удалена. Не полагайтесь на то, что JIT удалит или не удалит код. Всегда возвращайте результат или отдавайте его в `Blackhole`.
- Числа около пустого метода (~0,3–1 нс) подозрительны всегда.

### Порядок работы на этапе оптимизации

1. **Базовая линия**: собрать uber-jar JMH, `java -jar target/benchmarks.jar -f 3 -wi 5 -i 10 -prof gc -rf json -rff baseline.json`. Записать JDK (`jdkVersion`, `vmVersion`), `jvmArgs`, железо, питание. Для сервиса — отчёт нагрузочного теста, JFR (`settings=profile`) и GC-лог.
2. **Измерить шум**: тот же коммит 3 раза в отдельных запусках, разброс max/min на каждый бенчмарк. Это минимальный эффект, который можно заявить.
3. **Профиль** реальной нагрузки: `jfr view hot-methods`, `allocation-by-site`, `contention-by-site`, `gc-pauses`, `deoptimizations-by-reason` (все ОС); flame graph async-profiler по cpu, alloc, wall, lock (Linux и macOS).
4. **Ревью** горячего кода по разделам [Техники в коде](#техники-в-коде) и [Анти-паттерны](#анти-паттерны-производительности); каждый симптом — в [таблицу](#симптом--техника--источник).
5. **Одно изменение**, тот же JMH с теми же флагами в `candidate.json`. Сравнение по ключу «бенчмарк + параметры + режим»: `primaryMetric.score` с пересечением `scoreConfidence` и точный `gc.alloc.rate.norm`.
6. **Макропроверка**: нагрузочный тест или вся задача целиком; снова GC-лог и JFR (нет новой конкуренции, нет волн деоптимизаций).
7. **Закрепить**: закоммитить бенчмарк вместе с исправлением, JSON — артефакт CI; при желании гейт в CI с порогом выше измеренного шума.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Nanotrusting the Nanotime](https://shipilev.net/blog/2014/nanotrusting-nanotime/) | Aleksey Shipilev, 2014 | Задержка и гранулярность таймера; почему крошечные вещи нельзя мерить через `nanoTime` `[не проверено]` |
| [(The Art of) (Java) Benchmarking](https://shipilev.net/talks/devoxx-Nov2013-benchmarking.pdf) | Aleksey Shipilev, 2013 | Канонический доклад о ловушках бенчмаркинга `[не проверено]` |
| [The USE Method](https://www.brendangregg.com/usemethod.html) | Brendan Gregg | Метод USE для анализа ресурсов |
| [Performance Analysis Methodology](https://www.brendangregg.com/methodology.html) | Brendan Gregg | Каталог методологий и анти-методологий |
| [How NOT to Measure Latency](https://www.youtube.com/watch?v=lJ8ydIuPFeU) | Gil Tene, 2015 | Coordinated omission, перцентили `[не проверено]` |
| [HdrHistogram](http://hdrhistogram.org/) | Gil Tene | Правильная запись распределений задержки |
| [Amdahl's law](https://en.wikipedia.org/wiki/Amdahl%27s_law) | Wikipedia | Предел ускорения от оптимизации части программы |
| [Avoiding Benchmarking Pitfalls on the JVM](https://www.oracle.com/technical-resources/articles/java/architect-benchmarking.html) | Julien Ponge (Oracle), 2014 | Введение в JMH на примерах ловушек `[не проверено]` |

---

## Бенчмарки: JMH

**Ключевые выводы**

- JMH — единственный поддерживаемый харнес микробенчмарков для Java. Caliper мёртв. Версия 1.37 (август 2023) по-прежнему последняя и работает на JDK 25 (проверено локально).
- Раскладка в Maven: отдельный модуль `benchmarks`, который зависит от тестируемого кода. Собрать uber-jar и запускать через `java -jar`.
- Не запускайте бенчмарки из отладчика IDE и под тест-раннером (JUnit). Только uber-jar, форки, ничего лишнего не запущено. Плагин JMH для IntelliJ годится только для пробных запусков.
- Результат верен только для той JVM, флагов, железа и данных, на которых он снят. Публикуйте их вместе с числами.

### Настройка в Maven и ловушка JDK 23+

Официальный архетип:

```bash
mvn archetype:generate -DarchetypeGroupId=org.openjdk.jmh \
  -DarchetypeArtifactId=jmh-java-benchmark-archetype -DarchetypeVersion=1.37 \
  -DgroupId=org.sample -DartifactId=bench -Dversion=1.0
```

Архетип 1.37 целится в Java 1.8 и использует maven-compiler-plugin 3.8.0. Начиная с JDK 23 javac по умолчанию не запускает процессоры аннотаций, найденные на classpath (JDK-8321314 `[не проверено]`: страница бага отдаёт 403). Сборка проходит, но `java -jar target/benchmarks.jar` падает с `Unable to find the resource: /META-INF/BenchmarkList`. Исправление: maven-compiler-plugin 3.16.0 с `<annotationProcessorPaths>` на `jmh-generator-annprocess` (или флаг `-proc:full`), `maven.compiler.release` = 25, maven-shade-plugin 3.6.2. Рабочий фрагмент pom, проверено локально (JDK 25, Windows 11):

```xml
<properties>
  <maven.compiler.release>25</maven.compiler.release>
  <jmh.version>1.37</jmh.version>
</properties>

<dependencies>
  <dependency>
    <groupId>org.openjdk.jmh</groupId>
    <artifactId>jmh-core</artifactId>
    <version>${jmh.version}</version>
  </dependency>
</dependencies>

<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>3.16.0</version>
      <configuration>
        <release>${maven.compiler.release}</release>
        <annotationProcessorPaths>
          <path>
            <groupId>org.openjdk.jmh</groupId>
            <artifactId>jmh-generator-annprocess</artifactId>
            <version>${jmh.version}</version>
          </path>
        </annotationProcessorPaths>
      </configuration>
    </plugin>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-shade-plugin</artifactId>
      <version>3.6.2</version>
      <executions>
        <execution>
          <phase>package</phase>
          <goals><goal>shade</goal></goals>
          <configuration>
            <finalName>benchmarks</finalName>
            <transformers>
              <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                <mainClass>org.openjdk.jmh.Main</mainClass>
              </transformer>
              <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
            </transformers>
            <filters>
              <filter>
                <artifact>*:*</artifact>
                <excludes><exclude>META-INF/*.SF</exclude><exclude>META-INF/*.DSA</exclude><exclude>META-INF/*.RSA</exclude></excludes>
              </filter>
            </filters>
          </configuration>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

### Настройка в Gradle

Плагин `me.champeau.jmh` 0.7.3. Исходники — в `src/jmh/java`, запуск `./gradlew jmh`, результат по умолчанию в `build/results/jmh/results.json`.

```kotlin
plugins {
    java
    id("me.champeau.jmh") version "0.7.3"
}

jmh {
    jmhVersion = "1.37"
    warmupIterations = 5
    iterations = 10
    fork = 3
    profilers = listOf("gc")
    resultFormat = "JSON"
}
```

- Проверено локально (JDK 25, Windows 11): Gradle 9.8.2 + плагин 0.7.3 + toolchain JDK 25 — `./gradlew jmh` работает и пишет `results.json`. С `--warning-mode all` видно, что `Configuration.setVisible(boolean)` будет удалён в Gradle 11. Сейчас работает; на Gradle 11 сломается, если плагин не обновят.
- Обход отставания плагина: обычный source set с `jmh-core` и `annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")`, запуск `org.openjdk.jmh.Main` задачей `JavaExec`.

### Запуск

```bash
java -jar target/benchmarks.jar "StringBench" -f 3 -wi 5 -w 1s -i 10 -r 1s \
  -prof gc -rf json -rff result.json
```

- `-l` — список бенчмарков, `-lp` — с параметрами, `-lprof` — доступные профайлеры, `-h` — все опции.
- `-p size=10,1000` переопределяет `@Param`; `-jvmArgsAppend "-XX:+UseZGC"` меняет флаги в каждом форке; `-jvm <путь>` меряет другой JDK.
- `-t 4` — потоки, `-bm thrpt,avgt` — режимы, `-tu ns` — единица времени.
- Прогоны только последовательно: см. `jmh.lock` в [Как бенчмаркать честно](#как-бенчмаркать-честно).

### Аннотации и понятия

- `@Benchmark` должен **вернуть** результат или отдать его в `Blackhole`, иначе C2 удалит работу. Входы — из не-final полей `@State`, иначе C2 свернёт их как константы.
- `@State(Scope.Thread | Scope.Benchmark | Scope.Group)`. `Scope.Benchmark` — общие данные только для чтения. `Scope.Thread` — изменяемые данные на поток. Общее изменяемое состояние в `Scope.Benchmark` при нескольких потоках меряет конкуренцию, а не код.
- `@Setup`/`@TearDown(Level.Trial | Level.Iteration | Level.Invocation)`. Trial — раз на форк (по умолчанию), Iteration — перед каждой итерацией, Invocation — перед каждым вызовом. **Избегайте `Level.Invocation`** для работы короче ~1 мс: накладные расходы на метки времени и координацию доминируют (JMHSample_38 и предупреждение в javadoc).
- `@Fork(n)` — n свежих JVM. Профили JIT живут внутри одной JVM, а разброс между запусками виден только между форками. `@Fork(0)` — только для отладки. Флаги добавляют так: `@Fork(value = 3, jvmArgsAppend = {...})`.
- `@Warmup` и `@Measurement(iterations, time)` дают C2 скомпилировать код, а GC — устояться. Смотрите на вывод по итерациям: если прогрев ещё снижается, удлините его.
- `@BenchmarkMode`: `Throughput` (операций за время), `AverageTime` (время на операцию), `SampleTime` (время отдельных вызовов → перцентили), `SingleShotTime` (холодный старт, один вызов на итерацию; с многими форками и `batchSize`), `All`.
- `@OutputTimeUnit`; `@Param({"10","1000","100000"})` — декартово произведение всех параметров. Размер важен из-за кэшей L1/L2/L3/RAM.
- `@OperationsPerInvocation(n)` — если один вызов делает n операций. `@CompilerControl(DONT_INLINE | INLINE | EXCLUDE)` — для контролируемых экспериментов. `@Threads`, `@Group`/`@GroupThreads` — асимметричные бенчмарки производитель/потребитель. `@AuxCounters` — свои счётчики.
- Цикл внутри бенчмарка — ловушка (JMHSample_11 и 34): C2 разворачивает, конвейеризует и выносит код, и вы меряете другое. Пусть цикл делает JMH или отдавайте каждый элемент в `Blackhole`.

### Blackhole

- `Blackhole.consume(x)` не даёт удалить вычисление.
- С JDK 17 JMH умеет compiler blackholes (`-XX:CompileCommand=blackhole,...`), они дешевле. JMH сам определяет поддержку: `-Djmh.blackhole.autoDetect=true` по умолчанию; принудительно — `-Djmh.blackhole.mode=COMPILER`, отладка — `-Djmh.blackhole.debug=true`.
- Строка лога `# Blackhole mode: compiler (auto-detected...)` подтверждает, что на JDK 25 используются compiler blackholes (проверено локально).
- `Blackhole.consumeCPU(tokens)` сжигает фиксированное время — для моделирования backoff.

### Встроенные профайлеры (`-prof <имя>`)

| Профайлер | Что даёт | Платформа | Заметки |
|---|---|---|---|
| `gc` | `gc.alloc.rate` (МБ/с), **`gc.alloc.rate.norm` (байт на операцию)**, `gc.count`, `gc.time`, `gc.churn.*` | все | Для сравнения аллокаций — только `.norm`. МБ/с растут, когда код ускоряется (проверено локально) |
| `stack` | Гистограмма состояний потоков и верхних кадров | все | Профайлер «для бедных», подвержен safepoint bias; только для первого взгляда |
| `jfr` | Запись JFR на каждый форк (`-prof jfr:dir=out`) | все | Открыть в JMC или `jfr view` (проверено локально) |
| `async` | Flame graph async-profiler (`-prof "async:libPath=/path/libasyncProfiler.so;output=flamegraph;dir=prof"`) | Linux, macOS | **На Windows недоступен**: `Unable to load async-profiler` (проверено локально) |
| `perfasm` | Самые горячие участки сгенерированного ассемблера со счётчиками perf (нужен hsdis) | Linux | Инструмент для вопроса «почему этот цикл медленный». `xperfasm` на Windows требует xperf и прав администратора |
| `perf` / `perfnorm` / `perfc2c` | Аппаратные счётчики (такты, инструкции, промахи кэша и предсказаний) на операцию; поиск false sharing | Linux | На Windows недоступны (проверено локально) |
| `dtraceasm` | perfasm для macOS через dtrace | macOS | Нужен sudo |
| `comp` / `cl` | Время JIT-компиляции, загрузка классов | все | Хватает ли прогрева |
| `safepoints` / `pauses` | Статистика safepoint и пауз | все | Шум от GC и safepoint внутри итераций |
| `mempool` | Размер пулов памяти | все | |

Проверено локально (JDK 25, Windows 11): `-lprof` показывает как поддерживаемые `cl`, `comp`, `gc`, `jfr`, `mempool`, `pauses`, `safepoints`, `stack`; как неподдерживаемые — `async`, `dtraceasm`, `perf`, `perfasm`, `perfc2c`, `perfnorm`, `xperfasm`. Проба async-профайлера печатает предупреждение JDK 24+ про restricted method `System::loadLibrary`. Оно безвредно; заглушить — `--enable-native-access=ALL-UNNAMED`.

Ещё одно предупреждение: JMH 1.37 на JDK 24+ в каждом форке печатает `WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils` (JEP 498). Сейчас это безвредно. Будущий JDK может потребовать более новый JMH.

### Формат JSON (`-rf json -rff file.json`)

Схема элемента массива, проверено локально (JDK 25, Windows 11) на JMH 1.37. Один элемент — одна комбинация «бенчмарк × параметры».

- `jmhVersion`, `benchmark` (полное имя `pkg.Class.method`), `mode` (`thrpt`, `avgt`, `sample`, `ss`), `threads`, `forks`, `jvm` (путь), `jvmArgs` (массив), `jdkVersion`, `vmName`, `vmVersion`, `warmupIterations`, `warmupTime` (`"1 s"`), `warmupBatchSize`, `measurementIterations`, `measurementTime`, `measurementBatchSize`.
- `params` — объект «имя `@Param` → значение». **Значения — строки**, даже для чисел (`"1000"`). Поля нет, если у бенчмарка нет параметров. Параметры входят в ключ сравнения.
- `primaryMetric`: `score` (среднее), `scoreError` (полуширина 99,9% доверительного интервала; `NaN` при одном замере), `scoreConfidence` `[low, high]`, `scorePercentiles` (ключи `"0.0"`, `"50.0"`, `"90.0"`, `"95.0"`, `"99.0"`, `"99.9"`, `"99.99"`, `"99.999"`, `"99.9999"`, `"100.0"`), `scoreUnit` (`"us/op"`, `"ops/s"`), `rawData` — **массив по форкам из массивов по итерациям** (`[[it1, it2, ...], [форк 2 ...]]`). В режиме `sample` есть ещё `rawDataHistogram`.
- `secondaryMetrics` — словарь «имя → та же структура». С `-prof gc`: `gc.alloc.rate` (MB/sec), `gc.alloc.rate.norm` (B/op), `gc.count`, `gc.time` (мс); старые версии и коллекторы дают ещё `gc.churn.*`.

Сокращённый пример структуры (значения условные):

```json
{ "benchmark": "bench.MapBench.presized", "mode": "avgt", "params": { "size": "100000" },
  "primaryMetric": { "score": 1994.1, "scoreError": 35.2, "scoreConfidence": [1958.9, 2029.3],
                     "scoreUnit": "us/op", "rawData": [[1990.2, 2001.5], [1987.0, 1998.4]] },
  "secondaryMetrics": { "gc.alloc.rate.norm": { "score": 7444557.0, "scoreUnit": "B/op" } } }
```

Правила для скрипта сравнения:

- Ключ — `benchmark` + отсортированные `params` + `mode`.
- В `thrpt` больше — лучше; в `avgt`, `sample`, `ss` меньше — лучше.
- Изменение есть, только если доверительные интервалы не пересекаются **и** относительный сдвиг больше измеренного шума между запусками.
- `gc.alloc.rate.norm` сравнивать точно: он детерминирован до нескольких байт (8 493 302 и 8 493 309 B/op в двух сессиях). `gc.alloc.rate` не сравнивать: `MapBench.presized` показал **больше** МБ/с (в 1,4 раза), чем вариант с ёмкостью по умолчанию, хотя аллоцирует **меньше** байт на операцию. Он просто быстрее.
- Другие форматы: `-rf csv` (колонки `Benchmark, Mode, Threads, Samples, Score, Score Error (99.9%), Unit, Param: <name>...`), `scsv`, `text`, `latex`. JSON читают jmh.morethan.io, Bencher (адаптер `java_jmh`) и github-action-benchmark (`tool: jmh`).

### Примеры JMH, которые стоит прочитать по порядку

Все лежат в [jmh-samples](https://github.com/openjdk/jmh/tree/master/jmh-samples/src/main/java/org/openjdk/jmh/samples): 01 HelloWorld, 02 BenchmarkModes, 03 States, 05 StateFixtures, 06 FixtureLevel, 07 FixtureLevelInvocation (почему `Level.Invocation` вредит), **08 DeadCode**, **09 Blackholes**, **10 ConstantFold**, **11 Loops**, **12 Forking** (загрязнение профиля), **13 RunToRun** (разброс между форками), 15 Asymmetric, 16 CompilerControl, 17 SyncIterations, 21 ConsumeCPU, **22 FalseSharing**, 23 AuxCounters, 26 BatchSize, 27 Params, 28 BlackholeHelpers, 31 InfraParams, 32 BulkWarmup, **34 SafeLooping**, **35 Profilers** (как пользоваться gc, stack, perfasm), 36 BranchPrediction, 37 CacheAccess, 38 PerInvokeSetup, 39 MemoryAccess.

### Частые ошибки в JMH (чеклист ревью)

- Результат не возвращается и не потребляется → DCE.
- Входы — константы времени компиляции или `static final` → свёртка констант.
- Цикл в теле бенчмарка; сумма копится в локальную переменную, которая никуда не идёт.
- `@Fork(0)` или один форк; мало прогрева; одно число без погрешности.
- `Level.Invocation` для работы наносекундного масштаба.
- Включены assertions, отладчик или агенты (в том числе из конфигурации запуска IDE).
- Сравнение результатов с разных машин, JDK или флагов; ноутбук от батареи; turbo включён в одном прогоне и выключен в другом.
- Данные слишком малы (влезают в L1) или слишком регулярны (предсказатель ветвлений их выучил, JMHSample_36) по сравнению с продакшеном.
- Сравнение `gc.alloc.rate` (МБ/с) вместо `gc.alloc.rate.norm` (байт на операцию).
- Доверие микрорезультату без проверки через `-prof perfasm` или JFR, что горячий код тот, о котором вы думаете.
- Два прогона JMH одновременно.

### Локальные замеры JMH

Проверено локально (JDK 25, Windows 11): G1 по умолчанию, максимум кучи 8 ГБ, `@Fork(2) @Warmup(3×1s) @Measurement(5×1s)`, `-prof gc`, сессия 6 ноября 2026 (Temurin 25.0.5); в скобках — сессия 20 октября (Temurin 25.0.4.1). Байты на операцию детерминированы, поэтому даны как есть.

| Бенчмарк | Время относительно базы | B/op | Вывод |
|---|---|---|---|
| `StringBench.concat` (`"user " + name + " id=" + id`) | 1 (база) | 56 | |
| `StringBench.builder` (явный `StringBuilder(32)`) | ×1,1 | 56 | indy-конкатенация не хуже ручного `StringBuilder` |
| `StringBench.format` (`String.format`) | **×10,5** (×9,1) | 432 | в 7,7 раза больше байт |
| `StringBench.formatted` | ×10,6 | 432 | то же, что `String.format` |
| `BoxingBench.loopPrimitive` (сумма `int[10000]`) | 1 (база) | ~0 | |
| `BoxingBench.loopBoxed` (сумма `List<Integer>`) | **×5,0** (×5,0) | ~0 | аллокаций нет; цена — переходы по указателям |
| `BoxingBench.intStreamSum` | ×1,1 | 256 | почти как примитивный цикл |
| `BoxingBench.streamBoxedReduce` (`reduce(0, Integer::sum)`) | **×41 к IntStream** (×47) | 160 151 | боксится каждая частичная сумма |
| `BoxingBench.boxingCollect` (`range().boxed().toList()`) | — | 198 176 | ~20 байт на элемент |
| `MapBench.defaultCapacity` (100 000 вставок) | 1 (база) | 8 493 302 | |
| `MapBench.presized` (`HashMap.newHashMap(n)`) | **в 1,6 раза быстрее** (в 2,6) | 7 444 557 | на 12% меньше байт |

Наблюдения:

- `-prof stack` на `StringBench.format` отнёс ~38% RUNNABLE-сэмплов к `Formatter$FormatSpecifier.print` и `Formatter.parse`.
- `-prof jfr` + `jfr view allocation-by-site` отнёс **72% аллокаций к `java.util.Formatter.parse(String)`**: строка формата разбирается при каждом вызове.
- `jfr view hot-methods` работает на записях, которые сделал JMH, без JMC.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [openjdk/jmh](https://github.com/openjdk/jmh) | OpenJDK | Исходники и README |
| [JMH samples](https://github.com/openjdk/jmh/tree/master/jmh-samples/src/main/java/org/openjdk/jmh/samples) | Aleksey Shipilev и др. | Настоящее руководство по JMH: 38 примеров с комментариями |
| [JMHSample_35_Profilers](https://github.com/openjdk/jmh/blob/master/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_35_Profilers.java) / [JMHSample_07_FixtureLevelInvocation](https://github.com/openjdk/jmh/blob/master/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_07_FixtureLevelInvocation.java) / [JMHSample_38_PerInvokeSetup](https://github.com/openjdk/jmh/blob/master/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_38_PerInvokeSetup.java) | OpenJDK | Профайлеры; почему `Level.Invocation` вредит |
| [JMHSample_09_Blackholes](https://github.com/openjdk/jmh/blob/master/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_09_Blackholes.java) / [JMHSample_12_Forking](https://github.com/openjdk/jmh/blob/master/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_12_Forking.java) | OpenJDK | Blackhole; загрязнение профиля без форков |
| [GCProfiler.java](https://github.com/openjdk/jmh/blob/master/jmh-core/src/main/java/org/openjdk/jmh/profile/GCProfiler.java) | OpenJDK | Как считается `gc.alloc.rate.norm` (выделенные байты из ThreadMXBean) |
| [AsyncProfiler.java](https://github.com/openjdk/jmh/blob/master/jmh-core/src/main/java/org/openjdk/jmh/profile/AsyncProfiler.java) | OpenJDK | Опции интеграции с async-profiler |
| [jmh-gradle-plugin](https://github.com/melix/jmh-gradle-plugin) | Cédric Champeau | Документация и настройка плагина Gradle |
| [The Configuration.setVisible(boolean) method has been deprecated (issue #296)](https://github.com/melix/jmh-gradle-plugin/issues/296) | Ben Manes, 2026 | Статус предупреждения на Gradle 9 |
| [Gradle Plugin Portal: me.champeau.jmh](https://plugins.gradle.org/plugin/me.champeau.jmh) | Gradle | Страница плагина |
| [JVM Anatomy Quark #27: Compiler Blackholes](https://shipilev.net/jvm/anatomy-quarks/27-compiler-blackholes/) | Aleksey Shipilev | Compiler blackholes, которые использует JMH |
| [Microbenchmarking with Java](https://www.baeldung.com/java-microbenchmark-harness) | Baeldung | Быстрый старт |
| [JMH Visualizer](https://jmh.morethan.io) / [jmh-visualizer](https://github.com/jzillmann/jmh-visualizer) | Johannes Zillmann | Визуализация JSON JMH, сравнение двух прогонов |

---

## Макробенчмарки, нагрузочные тесты и CI

**Ключевые выводы**

- Нагрузочные инструменты: **Gatling** 3.16 (DSL на Java, Kotlin, Scala; открытая и закрытая модели; HTML-отчёт с перцентилями; плагин `io.gatling:gatling-maven-plugin` 4.21.12); **k6** 2.3 (сценарии на JavaScript, исполнитель `constant-arrival-rate` — открытая модель); **wrk** быстрый, но с закрытой моделью и coordinated omission; **wrk2** держит постоянную частоту (`-R`) и корректирует задержки через HdrHistogram.
- Для SLO по задержке — открытая модель с фиксированной частотой запросов. Для максимальной пропускной способности — закрытая.
- Прогрейте сервис до замера: прогрев JIT в большом приложении может занимать минуты. Гоняйте достаточно долго, чтобы попало несколько сборок old gen. Во время нагрузочного теста пишите GC-лог и JFR, чтобы макрорезультат шёл вместе с профилем.
- Старт CLI и сервиса: **hyperfine** 2.0 (`hyperfine --warmup 3 "java -jar app.jar"`, `--export-json`); JMH `SingleShotTime` с многими форками для холодного пути; `-Xlog:startuptime`, `-Xlog:class+load`, сравнение с AOT-кэшем (см. [Старт и прогрев](#старт-и-прогрев)). Для Spring и Quarkus меряйте «время до первого ответа», а не «JVM поднялась».
- Бенчмарки в CI. Общие раннеры шумят (10–30% — обычное дело). Варианты: выделенные или bare-metal раннеры; сравнение с базовым прогоном в той же задаче; инструменты на подсчёте инструкций или симуляции. **CodSpeed** — codspeed-jvm (форк JMH + раннер CodSpeed, режим walltime, JDK 21+, Linux). **github-action-benchmark** (`tool: jmh`) — история в gh-pages, порог тревоги в процентах. Всегда сохраняйте сырой JSON JMH как артефакт CI.
- **Bencher**: статистические пороги, комментарии в PR.
  ```bash
  bencher run --adapter java_jmh --file results.json "java -jar benchmarks.jar -rf json -rff results.json"
  ```
- Порог гейта регрессий должен быть больше измеренного шума на этом раннере. Сначала прогоните один и тот же коммит 3–5 раз.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Gatling documentation](https://docs.gatling.io/) | Gatling | Профили нагрузки, Java DSL |
| [Grafana k6 documentation](https://grafana.com/docs/k6/latest/) | Grafana | Документация k6 |
| [k6: Constant arrival rate](https://grafana.com/docs/k6/latest/using-k6/scenarios/executors/constant-arrival-rate/) | Grafana | Исполнитель с открытой моделью, без coordinated omission |
| [wrk](https://github.com/wg/wrk) | Will Glozer | HTTP-бенчмарк |
| [wrk2](https://github.com/giltene/wrk2) | Gil Tene | Постоянная частота, корректная запись задержек |
| [hyperfine](https://github.com/sharkdp/hyperfine) | David Peter | Замер команд (время старта), экспорт в JSON |
| [codspeed-jvm](https://github.com/CodSpeedHQ/codspeed-jvm) | CodSpeed, 2026 | Форк JMH для CI CodSpeed |
| [CodSpeed Docs](https://codspeed.io/docs) | CodSpeed | Интеграция с CI |
| [Benchmark Harness Adapters](https://bencher.dev/docs/explanation/adapters/) | Bencher | Адаптер `java_jmh` для JSON JMH |
| [github-action-benchmark](https://github.com/benchmark-action/github-action-benchmark) | сообщество | GitHub Action с поддержкой JMH и порогами тревоги |
| [Measuring performance](https://quarkus.io/guides/performance-measure) | Quarkus | Как правильно мерить старт, RSS и первый запрос |

---

## Профилирование

**Ключевые выводы**

- JFR — выбор по умолчанию: встроен в JDK, работает на всех ОС, включая Windows.
- async-profiler — лучший сэмплер для Linux и macOS. **Для Windows сборки нет**. На Windows: JFR + JMC, IntelliJ Profiler или VisualVM; либо профилируйте внутри WSL2 или Linux-контейнера.
- Для агента без GUI: `jfr view` и `jfr print --json` дают текст, который удобно читать.
- Добавляйте `-XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints`, чтобы заинлайненные кадры приписывались правильно (помогает и JFR, и async-profiler).
- Проверено на Linux (JDK 25.0.5, async-profiler 4.6, JMH 1.37; ноутбук на Xubuntu 26.04 с ядром 7.0 и раннер GitHub Actions на Ubuntu 24.04; в обоих случаях стандартный `kernel.perf_event_paranoid=4`, без root). Повторяется в CI репозитория скилла, workflow `profilers.yml`:
  - async-profiler `event=cpu` и `event=alloc` работают и строят flame graph: без доступа к perf_events профайлер сам переходит на таймерный режим;
  - async-profiler с аппаратным событием (`event=cycles`) без прав не работает, но JMH завершается с кодом 0. В логе только `[WARN] perf_event_open ... failed` и совет `sysctl kernel.perf_event_paranoid=1`, а профиль пуст. Проверяйте лог;
  - JFR CPU-time (JEP 509) пишет сэмплы без root: около 600 событий `jdk.CPUTimeSample` за короткий прогон, `jfr view cpu-time-hot-methods` работает;
  - `-prof perfnorm` и `-prof perfasm` падают с «Profilers failed to initialize». Им нужны `perf`, `perf_event_paranoid` ≤ 1 (`sudo sysctl kernel.perf_event_paranoid=1`) и аппаратные счётчики. На раннерах GitHub счётчиков нет даже после `sysctl` (`The cycles event is not supported`), поэтому perfnorm и perfasm там не работают вообще. Для них нужна физическая машина или VM с проброшенным PMU; для perfasm ещё и библиотека hsdis.

### Инструменты

| Инструмент | Команда | Что даёт | Windows? |
|---|---|---|---|
| JFR при старте | `java -XX:StartFlightRecording:filename=rec.jfr,settings=profile,duration=120s -jar app.jar` | CPU-сэмплы, аллокации, блокировки, GC, I/O, деоптимизации | да (проверено локально) |
| JFR на живой JVM | `jcmd <pid> JFR.start name=r settings=profile` → `jcmd <pid> JFR.dump name=r filename=rec.jfr` → `jcmd <pid> JFR.stop name=r` | То же без перезапуска | да |
| `jfr view` / `jcmd JFR.view` | `jfr view hot-methods rec.jfr`; `jcmd <pid> JFR.view hot-methods` (JDK 21+, без файла) | ~70 готовых текстовых отчётов | да (проверено локально) |
| JFR method timing (JEP 520) | `-XX:StartFlightRecording:method-timing=::<clinit>,filename=clinit.jfr` → `jfr view method-timing clinit.jfr` | Время и число вызовов выбранных методов без агента | да (проверено локально) |
| JFR CPU-time (JEP 509) | `-XX:StartFlightRecording=jdk.CPUTimeSample#enabled=true,filename=p.jfr` → `jfr view cpu-time-hot-methods p.jfr` | Настоящее CPU-время по таймеру ядра | **нет**: на Windows принимается молча и пишет 0 сэмплов (проверено локально); на Linux работает без root (проверено) |
| JDK Mission Control 9.1.2 | GUI, открыть `.jfr` | Flame graph, автоматический анализ, страницы Threads, Locks, Allocations, GC | да |
| async-profiler 4.6 | `asprof -d 30 -e cpu -f cpu.html <pid>` | Java + нативные + кадры ядра, без safepoint bias; cpu, wall, alloc, lock, nativemem | **нет**: только Linux и macOS (проверено локально) |
| `jfrconv` | `jfrconv rec.jfr flame.html` | Flame graph из файла JFR | обычная Java-программа, на Windows должна работать `[не проверено]` |
| IntelliJ Profiler (Ultimate) | Run with Profiler | JFR и async-profiler вместе; flame graph, дерево вызовов, timeline | часть JFR — да; работает ли часть async-profiler — `[не проверено]` |
| VisualVM 2.2.2 | GUI | CPU- и memory-сэмплер, heap и thread dump, просмотр JFR | да |
| YourKit, JProfiler | GUI | Запись аллокаций, пробы БД и HTTP | да (коммерческие) |
| `perf` + FlameGraph | `perf record` с `-XX:+PreserveFramePointer` и perf-map-agent | Смешанные Java и нативные стеки | нет, Linux |
| JMH `-prof` | `-prof gc`, `jfr`, `stack`, `comp`, `safepoints` | Профиль внутри бенчмарка | `gc`, `jfr`, `stack`, `comp`, `cl`, `safepoints`, `pauses`, `mempool` — да; `async`, `perf*` — нет |
| Heap dump и гистограмма | `jcmd <pid> GC.heap_dump file.hprof`; `jcmd <pid> GC.class_histogram` | Дамп для MAT, VisualVM, IntelliJ; дешёвая живая гистограмма | да |
| NMT | `-XX:NativeMemoryTracking=summary` + `jcmd <pid> VM.native_memory summary` | Нативная память JVM по категориям | да |

### JFR и JMC

- Настройки: `settings=default` — цель ~1% накладных расходов; `profile` сэмплирует чаще, ~2% `[не проверено]`. Для глубоких стеков фреймворков — `-XX:FlightRecorderOptions:stackdepth=256` (по умолчанию 64).
- Командная строка (JDK 21+): `jfr summary rec.jfr` (число событий), `jfr view <view> rec.jfr`, `jfr print --events jdk.ExecutionSample --stack-depth 20 rec.jfr`, `jfr print --json`, `jfr scrub` (убрать чувствительные события), `jfr configure` (собрать свой `.jfc`), `jfr view --verbose` (показывает запрос за отчётом).
- Проверено локально (JDK 25, Windows 11): `jfr help view` перечисляет ~70 отчётов. JVM: `gc`, `gc-pauses`, `gc-cpu-time`, `gc-configuration`, `heap-configuration`, `tlabs`, `safepoints`, `vm-operations`, `compiler-statistics`, `longest-compilations`, `deoptimizations-by-reason`, `deoptimizations-by-site`, `native-memory-committed`.
  - Окружение: `cpu-load`, `container-cpu-throttling`, `container-memory-usage`, `jvm-flags`, `system-properties`.
  - Приложение: `hot-methods`, `cpu-time-hot-methods`, `allocation-by-class`, `allocation-by-site`, `allocation-by-thread`, `contention-by-site`, `contention-by-class`, `exception-by-site`, `exception-count`, `file-reads-by-path`, `socket-reads-by-host`, `latencies-by-type`, `memory-leaks-by-site`, `method-timing`, `method-calls`, `pinned-threads`, `monitor-inflation`, `object-statistics`, `thread-allocation`.
- Полезные события: `jdk.ExecutionSample` (CPU-сэмплы Java), `jdk.NativeMethodSample`, `jdk.ObjectAllocationSample` (ограниченный по частоте сэмплинг аллокаций, JDK 16+), `jdk.JavaMonitorEnter` и `jdk.ThreadPark` (конкуренция), `jdk.GarbageCollection` и `jdk.GCPhasePause`, `jdk.SafepointBegin`, `jdk.Deoptimization`, `jdk.Compilation`, `jdk.FileRead` и `jdk.SocketRead`, `jdk.VirtualThreadPinned` (JDK 21+), `jdk.OldObjectSample` (кандидаты в утечки).
- Новое в JDK 25:
  - **JEP 509** `jdk.CPUTimeSample` — экспериментально, **только Linux**: сэмплинг по CPU-таймеру ядра. Частота: `throttle=10ms` или `500/s`.
  - **JEP 518** кооперативный сэмплинг: стек обходится в ближайшем safepoint по сохранённым PC и SP. Стабильнее, в основном без safepoint bias; остаточная неточность, например, внутри интринсиков.
  - **JEP 520** замер и трассировка методов: инструментирование байткода по фильтру, без агента. `method-trace=java.util.HashMap::resize`; на живой JVM — `jcmd <pid> JFR.start method-timing=@jakarta.ws.rs.GET`. Проверено локально (JDK 25, Windows 11): `method-timing=::<clinit>` сразу нашёл намеренно медленный статический инициализатор; `method-trace=java.util.HashMap::resize` записал 20 событий `jdk.MethodTrace`.
  - JDK 27: JEP 536 — редактирование данных в процессе.
- JMC 9.x: flame graph, страница автоматического анализа, страницы Threads, Locks, Allocations, GC. Открывает и записи JMH `-prof jfr`. JMC поставляется отдельно от JDK (сборки Oracle, Adoptium, Azul, BellSoft).
- `ExecutionSample` покрывает потоки, выполняющие Java-код, плюс отдельное событие для нативных методов. Полные смешанные стеки (ядро, нативный код, потоки GC) даёт async-profiler на Linux.

### async-profiler 4.x (Linux, macOS)

- Сэмплер с низкими накладными расходами без safepoint bias (perf_events или itimer + AsyncGetCallTrace и обход стека через vmstructs). Видит кадры Java, нативные и ядра, включая потоки GC и JIT.
- События: `cpu`, `wall` (все потоки, включая спящие и заблокированные, — для задержки), `alloc` (профилирование аллокаций по TLAB), `lock` (конкурентные мониторы и блокировки j.u.c), `nativemem` (нативные утечки), `itimer`/`ctimer` (когда perf_events запрещён, например в контейнерах), аппаратные счётчики (`cache-misses` и др.).
- Агентом: `-agentpath:/opt/ap/lib/libasyncProfiler.so=start,event=cpu,file=out.jfr`. Вывод: HTML flame graph, дерево, JFR, collapsed stacks, OTLP (4.6).
- Для perf_events нужен `perf_event_paranoid` ≤ 1 (в контейнере — ещё `--cap-add SYS_ADMIN` или `PERFMON`), иначе `cpu` откатывается на `ctimer`/`itimer`. В Ubuntu по умолчанию стоит 4, и откат происходит и на обычной машине; для `cpu`, `wall` и `alloc` этого достаточно, а аппаратные события (`cycles`, `cache-misses`) дают пустой профиль с предупреждением в логе (проверено на Linux).
- В виде Maven-артефакта: ap-loader и `tools.profiler:async-profiler`.
- С JMH: `-prof "async:libPath=/path/libasyncProfiler.so;output=flamegraph;event=cpu"` (или `event=alloc`) — самый короткий путь от «бенчмарк медленный» к «вот горячий кадр».

### Safepoint bias

Классические сэмплеры на JVMTI `GetAllStackTraces` (старый сэмплер VisualVM, многие APM-агенты, JMH `-prof stack`) видят потоки только в safepoint. Горячие counted-циклы без safepoint poll исчезают, а их время приписывается следующему safepoint. Берите async-profiler, JFR (кооперативный сэмплинг в 25+) или perf. Honest-profiler заброшен: [устарело], замена — async-profiler.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [The jfr Command](https://docs.oracle.com/en/java/javase/25/docs/specs/man/jfr.html) | Oracle | Справка по `jfr`: print, summary, view, scrub, configure |
| [The jcmd Command](https://docs.oracle.com/en/java/javase/25/docs/specs/man/jcmd.html) | Oracle | Команды `JFR.*`, `GC.*`, `VM.*` |
| [Flight Recorder API Programmer's Guide, Release 25](https://docs.oracle.com/en/java/javase/25/jfapi/) | Oracle | Свои события, потоковое чтение |
| [View Command](https://egahlin.github.io/2023/05/30/views.html) | Erik Gahlin (лид JFR), 2023 | `jfr view` от автора |
| [JEP 509: JFR CPU-Time Profiling (Experimental)](https://openjdk.org/jeps/509) | Oracle, 2025 | CPU-time профилирование (Linux, экспериментально) |
| [JEP 518: JFR Cooperative Sampling](https://openjdk.org/jeps/518) | Oracle, 2025 | Кооперативный сэмплинг |
| [JEP 520: JFR Method Timing & Tracing](https://openjdk.org/jeps/520) | Oracle, 2025 | Замер и трассировка методов |
| [JEP 536: JFR In-Process Data Redaction](https://openjdk.org/jeps/536) | Oracle, 2026 | Редактирование данных JFR (JDK 27) |
| [Unbiased Java CPU profiling with JFR in JDK 25](https://www.datadoghq.com/blog/engineering/jfr-cpu-time-profiling/) | J. Bachorik, S. Gerring (Datadog), 2026 | Чем JEP 509 лучше `ExecutionSample`; компромиссы AsyncGetCallTrace |
| [openjdk/jmc](https://github.com/openjdk/jmc) | OpenJDK | Исходники JDK Mission Control |
| [JDK Mission Control](https://jdk.java.net/jmc/) | Oracle | Загрузка JMC |
| [Marcus Hirt – Life, Java and Everything](https://hirt.se/blog/) | Marcus Hirt | Блог создателя JMC и JFR |
| [JDK Flight Recorder (Dev.java)](https://dev.java/learn/jvm/monitoring-troubleshooting/jfr/) | dev.java | Учебник по JFR (старый адрес `dev.java/learn/jvm/jfr/` перенаправляет сюда) |
| [async-profiler](https://github.com/async-profiler/async-profiler) | Andrei Pangin и др. | README: платформы, быстрый старт |
| [async-profiler v4.6](https://github.com/async-profiler/async-profiler/releases/tag/v4.6) | async-profiler, 2026 | Вывод OTLP, фильтр wall по имени потока, убран встроенный HTTP-сервер |
| [ProfilingModes.md](https://github.com/async-profiler/async-profiler/blob/master/docs/ProfilingModes.md) | async-profiler | Режимы cpu, wall, alloc, lock, nativemem |
| [ProfilingInContainer.md](https://github.com/async-profiler/async-profiler/blob/master/docs/ProfilingInContainer.md) | async-profiler | perf_events в Docker и Kubernetes |
| [JfrVisualization.md](https://github.com/async-profiler/async-profiler/blob/master/docs/JfrVisualization.md) | async-profiler | JFR в flame graph и heatmap |
| [Async-profiler with Andrei Pangin (видеокурс)](https://www.youtube.com/playlist?list=PLNCLTEx3B8h4Yo_WvKWdLvI9mj1XpTKBr) | Andrei Pangin | Трёхчасовой курс по async-profiler |
| [Async-profiler - manual by use cases](https://krzysztofslusarski.github.io/2022/12/12/async-manual.html) | Krzysztof Ślusarski, 2022 | Большое практическое руководство с кейсами |
| [ap-loader](https://github.com/jvm-profiling-tools/ap-loader) | Johannes Bechberger | async-profiler в виде jar |
| [Why (Most) Sampling Java Profilers Are Fucking Terrible](http://psy-lob-saw.blogspot.com/2016/02/why-most-sampling-java-profilers-are.html) | Nitsan Wakart, 2016 | Safepoint bias на пальцах |
| [Jean-Philippe Bempel's Blog](https://jpbempel.github.io/) | Jean-Philippe Bempel | Внутренности профайлеров, `DebugNonSafepoints` |
| [Flame Graphs](https://www.brendangregg.com/flamegraphs.html) | Brendan Gregg | Как читать flame graph |
| [Java Flame Graphs](https://www.brendangregg.com/blog/2014-06-12/java-flame-graphs.html) | Brendan Gregg, 2014 | Смешанные flame graph для Java (perf + `PreserveFramePointer`) |
| [Introduction to CPU and allocation profiling](https://www.jetbrains.com/help/idea/cpu-and-allocation-profiling-basic-concepts.html) | JetBrains | IntelliJ Profiler = JFR + async-profiler |
| [VisualVM](https://visualvm.github.io/) | VisualVM | Домашняя страница |
| [honest-profiler](https://github.com/jvm-profiling-tools/honest-profiler) | R. Warburton, N. Wakart | [устарело], заброшен; замена — async-profiler |
| [JVM Anatomy Quark #12: Native Memory Tracking](https://shipilev.net/jvm/anatomy-quarks/12-native-memory-tracking/) | Aleksey Shipilev | NMT для нативной памяти |
| [Getting to know the JVM](https://dev.java/learn/jvm/) | dev.java | Обзор инструментов JVM |

---

## JIT и внутренности JVM

**Ключевые выводы**

- Многоуровневая компиляция: уровень 0 — интерпретатор, 3 — C1 с полным профилированием, 4 — C2 (оптимизирующий, использует профиль). Уровень 1 — C1 без профилирования (тривиальные методы).
- Умолчания JDK 25, проверено локально (`-XX:+PrintFlagsFinal`): `Tier3InvocationThreshold=200`, `Tier4InvocationThreshold=5000`, `Tier4CompileThreshold=15000`, `ReservedCodeCacheSize=240 МБ`, сегментированный code cache включён. При переполнении — `CodeCache is full. Compiler has been disabled` и резкое замедление; поднимите `-XX:ReservedCodeCacheSize`.
- **Инлайнинг — мать всех оптимизаций**: он открывает escape analysis, распространение констант, девиртуализацию. Умолчания JDK 25 (проверено локально): `MaxInlineSize=35` байт байткода для холодных точек вызова, `FreqInlineSize=325` для горячих, `MaxInlineLevel=15`, `InlineSmallCode=2500` (предел нативного размера уже скомпилированного вызываемого метода). Держите горячие методы маленькими. Огромные методы (> 8000 байт байткода, `HugeMethodLimit`) не компилируются вообще (`DontCompileHugeMethods`).
- Как увидеть решения JIT: `-XX:+PrintCompilation -XX:+PrintInlining` после `-XX:+UnlockDiagnosticVMOptions` (сообщения `too big`, `hot method too big`, `callee is too large`, `already compiled into a big method`); `-Xlog:jit+compilation=debug`; события JFR `jdk.Compilation` и `jdk.CompilerInlining`; JITWatch 1.7.0 (лог `-XX:+LogCompilation` → дерево инлайнинга, escape analysis, ассемблер с hsdis); JMH `-prof perfasm` (Linux) — горячий сгенерированный код.
- Полиморфизм точки вызова. Мономорфные (1 тип) и биморфные (2 типа) точки инлайнятся с проверкой типа. Мегаморфные (3+ типов, если ни один не набирает `TypeProfileMajorReceiverPercent=90`) становятся вызовами через vtable или itable. Это блокирует инлайнинг и всё, что за ним. Что делать: меньше типов в горячей точке, разделить циклы по типам, `switch` по sealed-иерархии или enum (pattern matching компилируется в проверки типов), вынести вызов из горячего цикла.
- Escape analysis и scalar replacement. Объект, который не утекает из скомпилированного метода (после инлайнинга), может исчезнуть: его поля остаются в регистрах. Работает для маленьких объектов фиксированной формы в одной единице компиляции. Ломается, если объект уходит в поле, массив или не заинлайненный вызов; при слиянии разных аллокаций в ветвлениях; для массивов непостоянной длины; при больших методах, которые не заинлайнились. Проверяйте через `-prof gc` (`gc.alloc.rate.norm` ≈ 0 — объект устранён), не предполагайте.
- Интринсики. Многие методы JDK заменяются ручным ассемблером или IR: `System.arraycopy`, `Arrays.equals`/`mismatch`, `String.indexOf`/`equals`, `Math.*`, `Integer.bitCount`, CRC32/Adler32, AES, пути `StringBuilder`, `Unsafe`, векторизованный `ArraysSupport`. Предпочитайте API JDK самописным циклам. Список — в `vmIntrinsics.hpp`.
- Деоптимизация (uncommon traps). C2 компилирует оптимистично: недостигнутые ветки, увиденные типы, «null не встречался». Когда предположение ломается, код откатывается в интерпретатор и перекомпилируется. Симптомы: периодические замедления, строки `made not entrant` в PrintCompilation, отчёт JFR `deoptimizations-by-reason`. После повторных ловушек по одной причине (`PerMethodTrapLimit=100`) C2 отказывается от этой оптимизации.
- `final`-поля. Экземплярные `final`-поля C2 в общем случае не считает константами (рефлексия может их изменить). Исключения — records, hidden classes и некоторые внутренние классы JDK. `static final` — настоящие константы. JEP 500 (JDK 26) начинает предупреждать о глубокой рефлексивной записи в `final`, готовя доверие к ним.
- Counted-циклы. C2 режет длинные counted-циклы на полосы (`LoopStripMiningIter=1000`), чтобы safepoint poll был дешёвым. Индуктивная переменная `int` оптимизируется лучше `long`: удаление проверок границ, разворачивание, векторизация SuperWord.
- Автовекторизация (SuperWord) работает на простых counted-циклах по примитивным массивам. Для явного SIMD — Vector API: в JDK 27 всё ещё инкубатор (JEP 537), нужен `--add-modules jdk.incubator.vector`. Зависимость от инкубаторного модуля допустима для внутренних инструментов и рискованна для библиотек.
- Graal JIT есть в дистрибутивах GraalVM (`-XX:+UseJVMCICompiler`). Может обгонять C2 на коде со стримами и лямбдами, а также на Scala и Kotlin благодаря partial escape analysis. Экспериментальный Graal JIT внутри OpenJDK удалён в JDK 17 (JEP 410).

```bash
# Почему метод не заинлайнился
java -XX:+UnlockDiagnosticVMOptions -XX:+PrintCompilation -XX:+PrintInlining -jar app.jar \
  | grep -E "too big|callee is too large|already compiled"
# Лог для JITWatch
java -XX:+UnlockDiagnosticVMOptions -XX:+LogCompilation -XX:LogFile=jit.log -jar app.jar
```

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [JVM Anatomy Quarks](https://shipilev.net/jvm/anatomy-quarks/) | Aleksey Shipilev, 2016–2021 | 30 коротких разборов, по одной теме каждый |
| [JVM Anatomy Quark #16: Megamorphic Virtual Calls](https://shipilev.net/jvm/anatomy-quarks/16-megamorphic-virtual-calls/) | Aleksey Shipilev | Цена мегаморфного вызова |
| [JVM Anatomy Quark #18: Scalar Replacement](https://shipilev.net/jvm/anatomy-quarks/18-scalar-replacement/) | Aleksey Shipilev | Когда аллокации исчезают |
| [JVM Anatomy Quark #17: Trust Nonstatic Final Fields](https://shipilev.net/jvm/anatomy-quarks/17-trust-nonstatic-final-fields/) | Aleksey Shipilev | Почему экземплярные `final` не константы |
| [JVM Anatomy Quark #15: Just-In-Time Constants](https://shipilev.net/jvm/anatomy-quarks/15-just-in-time-constants/) | Aleksey Shipilev | `static final` как константы JIT |
| [JVM Anatomy Quark #29: Uncommon Traps](https://shipilev.net/jvm/anatomy-quarks/29-uncommon-traps/) | Aleksey Shipilev | Механика деоптимизации |
| [JVM Anatomy Quark #30: Conditional Moves](https://shipilev.net/jvm/anatomy-quarks/30-conditional-moves/) | Aleksey Shipilev | Ветвления против cmov, скорость зависит от данных |
| [JVM Anatomy Quark #22: Safepoint Polls](https://shipilev.net/jvm/anatomy-quarks/22-safepoint-polls/) | Aleksey Shipilev | Safepoint poll в циклах |
| [The Black Magic of (Java) Method Dispatch](https://shipilev.net/blog/2015/black-magic-method-dispatch/) | Aleksey Shipilev, 2015 | Цена виртуальных и интерфейсных вызовов, mono/bi/megamorphic |
| [Arrays of Wisdom of the Ancients](https://shipilev.net/blog/2016/arrays-wisdom-ancients/) | Aleksey Shipilev, 2016 | `toArray(new T[0])` быстрее заранее заданного размера |
| [JITWatch](https://github.com/AdoptOpenJDK/jitwatch) | Chris Newland | Визуализация LogCompilation, инлайнинга, ассемблера |
| [JITWatch: Instructions](https://github.com/AdoptOpenJDK/jitwatch/wiki/Instructions) | Chris Newland | Как получить логи (hsdis, флаги) |
| [VM Options Explorer](https://chriswhocodes.com/vm-options-explorer.html) | Chris Newland | Все флаги HotSpot по версиям JDK с умолчаниями |
| [vmIntrinsics.hpp](https://github.com/openjdk/jdk/blob/master/src/hotspot/share/classfile/vmIntrinsics.hpp) | OpenJDK | Авторитетный список интринсиков |
| [compiler_globals.hpp](https://github.com/openjdk/jdk/blob/master/src/hotspot/share/compiler/compiler_globals.hpp) | OpenJDK | Определения и умолчания флагов компилятора |
| [HotSpot Glossary of Terms](https://openjdk.org/groups/hotspot/docs/HotSpotGlossary.html) | OpenJDK | OSR, safepoint, uncommon trap и др. |
| [JEP 537: Vector API (Twelfth Incubator)](https://openjdk.org/jeps/537) | Oracle, 2026 | Vector API в JDK 27 |
| [JEP 500: Prepare to Make Final Mean Final](https://openjdk.org/jeps/500) | Oracle, 2025 | Предупреждения о записи в `final` через рефлексию `[не проверено]` |
| [HotSpot PerformanceTechniques](https://wiki.openjdk.org/display/HotSpot/PerformanceTechniques) | OpenJDK wiki | Обзор техник JIT HotSpot `[не проверено]`: вики отдаёт 403 скриптам |
| [Server Compiler Inlining Messages](https://wiki.openjdk.org/display/HotSpot/Server+Compiler+Inlining+Messages) | OpenJDK wiki | Значение сообщений PrintInlining `[не проверено]` |

---

## Память и GC

**Ключевые выводы**

- Главный рычаг — скорость аллокаций. Цена любого GC растёт со скоростью аллокаций (частота young GC) и с live set (маркировка и копирование). Сначала снизьте байты на операцию на горячих путях (`-prof gc`, `jfr view allocation-by-site`, async-profiler `-e alloc`), потом выбирайте и настраивайте коллектор.
- Сама аллокация дешёвая (bump pointer в TLAB). Цена — в работе GC и промахах кэша. Короткоживущие мелкие объекты, которые убрал escape analysis, ничего не стоят. Большие массивы дороги (в G1 humongous — от половины региона).
- Выбор коллектора (JDK 25 и 27):
  - **G1** — по умолчанию; с JDK 27 и на машинах с 1 CPU или < 1792 МБ (JEP 523). Сбалансированный, цель паузы `-XX:MaxGCPauseMillis=200`. JEP 522 в JDK 26 снизил цену write barrier (5–15% пропускной способности для приложений с большим числом ссылок).
  - **Parallel** (`-XX:+UseParallelGC`) — максимальная пропускная способность для пакетных задач, где паузы не важны.
  - **ZGC** (`-XX:+UseZGC`) — только генерационный (негенерационный удалён в JDK 24, JEP 490). Паузы меньше миллисекунды независимо от размера кучи. Платит частью пропускной способности, CPU и запасом памяти. Следите за `Allocation Stall` (куча мала). Netflix перешёл на генерационный ZGC по умолчанию на JDK 21+. JDK 28 нацелен на быстрый старт и прогрев и адаптивный размер кучи (JEP 545 и 546).
  - **Shenandoah** (`-XX:+UseShenandoahGC`, генерационный режим `-XX:ShenandoahGCMode=generational` — продуктовый в JDK 25, по умолчанию в JDK 28 по JEP 535). Низкие паузы. Нет в сборках Oracle JDK, есть в сборках OpenJDK (Temurin, Red Hat, Corretto) `[не проверено]` для матрицы вендоров.
  - **Serial** — крошечные кучи, одно ядро, минимальный след. С JDK 27 выбирается только явно: `-XX:+UseSerialGC`.
  - **Epsilon** (`-XX:+UnlockExperimentalVMOptions -XX:+UseEpsilonGC`) — без сборки мусора. Для бенчмарков, изолирующих цену GC, и очень коротких задач.
- Размер кучи. Для сервисов с предсказуемой нагрузкой `-Xms` = `-Xmx` (нет изменения размера); с `-XX:+AlwaysPreTouch` страницы заранее подгружаются ценой старта. Умолчания JDK 25 (проверено локально): `MaxRAMPercentage=25`, `InitialRAMPercentage=1.5625`, размер региона G1 эргономический (4 МБ при максимуме кучи 8 ГБ).
- Контейнеры. JVM читает лимиты cgroup v1 и v2 (поддержка контейнеров включена по умолчанию). Максимальная куча по умолчанию — 25% лимита контейнера; для выделенного контейнера это обычно мало. Ставьте `-XX:MaxRAMPercentage=50..75` и оставляйте место для metaspace, code cache, стеков потоков, direct-буферов и структур GC (размер подбирайте через NMT).
- Лимит CPU задаёт `availableProcessors()` и число потоков GC и JIT; при дробных квотах подумайте об `-XX:ActiveProcessorCount=n`. Троттлинг CPU в Kubernetes (квота CFS) выглядит как всплески задержки: `jfr view container-cpu-throttling`.
- **Компактные заголовки объектов**: JEP 519 (JDK 25, включаются `-XX:+UseCompactObjectHeaders`), по умолчанию в JDK 27 (JEP 534). Заголовок 12 → 8 байт. По данным JEP: SPECjbb2015 −22% кучи, −8% CPU, −15% сборок; JSON-парсер −10% времени. В JDK 25 по умолчанию `false` (проверено локально). Больше всего выигрывают нагрузки с массой мелких объектов; меряйте своё приложение.
- Сжатые указатели включены до ~32 ГБ кучи. За 32 ГБ каждая ссылка занимает 8 байт: куча 31 ГБ часто вмещает больше, чем 35 ГБ. Выравнивание объектов — 8 байт (`ObjectAlignmentInBytes`).
- GC-лог в продакшене держите включённым всегда, он почти бесплатен. Анализ: GCViewer, GCEasy, Microsoft gctoolkit или JFR (`jfr view gc`, `gc-pauses`, `gc-cpu-time`).
- Large pages и THP: `-XX:+UseTransparentHugePages` (Linux, режим `madvise`) может снизить промахи TLB на больших кучах. Меряйте.
- Дедупликация строк (`-XX:+UseStringDeduplication`, все коллекторы с JDK 18) меняет CPU на память, когда живёт много одинаковых строк.
- Утечка или просто большой след. Old gen растёт после full GC — утечка: heap dump, JFR `OldObjectSample` и `memory-leaks-by-site`. RSS сильно выше кучи — нативная память: NMT, direct-буферы, стеки потоков, арены glibc (`MALLOC_ARENA_MAX`).

```bash
# GC-лог с ротацией
java -Xlog:gc*,safepoint:file=gc.log:time,uptime,level,tags:filecount=10,filesize=50m -jar app.jar
# Куча в контейнере и проверка, что JVM её увидела
java -XX:MaxRAMPercentage=70 -XX:+PrintFlagsFinal -version | grep -E "MaxHeapSize|UseG1GC"
# Нативная память
java -XX:NativeMemoryTracking=summary -jar app.jar &
jcmd <pid> VM.native_memory summary
```

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [HotSpot Virtual Machine Garbage Collection Tuning Guide, Release 25](https://docs.oracle.com/en/java/javase/25/gctuning/) | Oracle | Руководство по настройке GC для JDK 25 |
| [Garbage-First (G1) Garbage Collector](https://docs.oracle.com/en/java/javase/25/gctuning/garbage-first-g1-garbage-collector1.html) | Oracle | Устройство и настройка G1 |
| [The Z Garbage Collector](https://docs.oracle.com/en/java/javase/25/gctuning/z-garbage-collector1.html) | Oracle | Настройка ZGC (адрес из исходной заметки `z-garbage-collector.html` теперь уводит на главную JDK 26) |
| [The java Command](https://docs.oracle.com/en/java/javase/25/docs/specs/man/java.html) | Oracle | Все опции `java` и `-XX` для JDK 25 |
| [JEP 523: Make G1 the Default Garbage Collector in All Environments](https://openjdk.org/jeps/523) | Oracle, 2026 | G1 по умолчанию везде (JDK 27) |
| [JEP 522: G1 GC: Improve Throughput by Reducing Synchronization](https://openjdk.org/jeps/522) | Oracle, 2025 | Двойные таблицы карт в G1 (JDK 26) |
| [JEP 474: ZGC: Generational Mode by Default](https://openjdk.org/jeps/474) | Oracle, 2024 | Генерационный ZGC по умолчанию (JDK 23) |
| [JEP 490: ZGC: Remove the Non-Generational Mode](https://openjdk.org/jeps/490) | Oracle, 2024 | Негенерационный ZGC удалён (JDK 24) |
| [JEP 521: Generational Shenandoah](https://openjdk.org/jeps/521) | Oracle/Amazon, 2025 | Генерационный Shenandoah — продуктовый (JDK 25) |
| [JEP 535: Shenandoah GC: Generational Mode by Default](https://openjdk.org/jeps/535) | 2026 | Генерационный Shenandoah по умолчанию (JDK 28) |
| [JEP 545: Faster Startup and Warmup with ZGC](https://openjdk.org/jeps/545) | Oracle, 2026 | Старт и прогрев с ZGC (JDK 28) |
| [JEP 546: Adaptive Heap Sizing for ZGC](https://openjdk.org/jeps/546) | Oracle, 2026 | Адаптивный размер кучи ZGC (JDK 28) |
| [JEP 519: Compact Object Headers](https://openjdk.org/jeps/519) | Oracle/Amazon, 2025 | Компактные заголовки (JDK 25) |
| [JEP 534: Compact Object Headers by Default](https://openjdk.org/jeps/534) | 2026 | По умолчанию в JDK 27, цифры |
| [OpenJDK: Lilliput](https://openjdk.org/projects/lilliput/) | OpenJDK | Проект уменьшения заголовков |
| [Netflix - Bending Pause Times to Your Will with Generational ZGC](https://inside.java/2024/03/06/generational-zgc-netflix) | Netflix / inside.java, 2024 | Переход Netflix на генерационный ZGC |
| [Lower Java Tail Latencies With ZGC](https://www.morling.dev/blog/lower-java-tail-latencies-with-zgc/) | Gunnar Morling | Хвостовые задержки с ZGC |
| [Tricks of the Trade: Tuning JVM Memory for Large-scale Services](https://www.uber.com/blog/jvm-tuning-garbage-collection/) | Uber, 2020 | Настройка памяти JVM для крупных сервисов (HDFS NameNode) `[не проверено]`: страница отдаёт 406 скриптам |
| [Garbage Collection Optimization for High-Throughput and Low-Latency Java Applications](https://engineering.linkedin.com/garbage-collection/garbage-collection-optimization-high-throughput-and-low-latency-java-applications) | LinkedIn, 2014 | Систематическая настройка GC для сервиса с низкой задержкой |
| [JVM Anatomy Quark #3: GC Design and Pauses](https://shipilev.net/jvm/anatomy-quarks/3-gc-design-and-pauses/) | Aleksey Shipilev | Компромиссы в устройстве GC |
| [JVM Anatomy Quark #4: TLAB allocation](https://shipilev.net/jvm/anatomy-quarks/4-tlab-allocation/) | Aleksey Shipilev | Аллокация в TLAB |
| [JVM Anatomy Quark #23: Compressed References](https://shipilev.net/jvm/anatomy-quarks/23-compressed-references/) | Aleksey Shipilev | Сжатые указатели и обрыв на 32 ГБ |
| [JVM Anatomy Quark #2: Transparent Huge Pages](https://shipilev.net/jvm/anatomy-quarks/2-transparent-huge-pages/) | Aleksey Shipilev | Влияние THP |
| [GCViewer](https://github.com/chewiebug/GCViewer) | сообщество | Просмотр GC-логов |
| [GCEasy](https://gceasy.io/) | Tier1app | Онлайн-анализ GC-логов |
| [gctoolkit](https://github.com/microsoft/gctoolkit) | Microsoft | Библиотека разбора GC-логов |
| [Containerize your Java Applications](https://learn.microsoft.com/en-us/azure/developer/java/containers/overview) | Microsoft | Java в контейнерах: проценты кучи, CPU, выбор GC |
| [Red Hat Developer: memory management in Java containers](https://developers.redhat.com/articles/2023/03/07/overview-memory-management-java-containers) | Red Hat, 2023 | Память Java в контейнерах `[не проверено]`: страница отдаёт 403 скриптам |

---

## Старт и прогрев

**Ключевые выводы**

- CDS-архив классов JDK включён по умолчанию с JDK 12 (JEP 341). AppCDS для классов приложения на JDK < 25: `-XX:ArchiveClassesAtExit=app.jsa`, затем `-XX:SharedArchiveFile=app.jsa` (JEP 350). С JDK 19 есть автоматический динамический CDS: `-XX:+AutoCreateSharedArchive -XX:SharedArchiveFile=...`.
- **AOT-кэш (Project Leyden, JDK 24+) заменяет AppCDS**. JDK 24, JEP 483: классы загружены и слинкованы заранее. JDK 25, JEP 514: создание в один шаг `-XX:AOTCacheOutput=app.aot`; JEP 515: профили методов, C2 стартует раньше. JDK 26, JEP 516: работает с любым GC, включая ZGC; потоковый формат объектов. JDK 28, JEP 544 (запланировано): кэш скомпилированного кода.
- Порядок работы с AOT-кэшем: тренировочный прогон `java -XX:AOTCacheOutput=app.aot -jar app.jar` (прогнать представительные пути и завершить программу), затем в продакшене `java -XX:AOTCache=app.aot -jar app.jar`. Двухшаговый вариант для раздельных окружений: `-XX:AOTMode=record -XX:AOTConfiguration=app.aotconf`, затем `-XX:AOTMode=create`.
- `JDK_AOT_VM_OPTIONS` передаёт опции только шагу создания кэша. Шаг создания использует кучу размером с тренировочную (нужно ~2× памяти).
- Ограничения AOT-кэша: та же сборка JDK, ОС и архитектура CPU; тот же classpath (дописывать в конец можно), без каталогов в classpath; те же опции модулей; без JVMTI-агентов, переписывающих классы. При нарушении кэш молча игнорируется с предупреждением. С `-XX:AOTMode=required` (до JDK 27 — `on`) это ошибка.
- Проверено локально (JDK 25, Windows 11): AOT-кэш работает на Windows. Для небольшой программы со стримами, regex и `java.time` кэш занял 11,8 МБ. Лучшее из 15 запусков (от старта процесса до выхода): AOT-кэш **~на 25% быстрее** CDS по умолчанию; без CDS — в ~1,33 раза медленнее CDS по умолчанию. Медианы шумели (машина общая). Запуск с `-cp classes` вместо тренировочного `-jar app.jar` дал `shared class paths mismatch`, и кэш был проигнорирован; с `-XX:AOTMode=on` это ошибка.
- Цифры из JEP: Spring PetClinic 4,49 → 2,60 с (−42%) и небольшая программа на стримах −42% с JEP 483; +19% к прогреву от профилей JEP 515; ранние сборки JEP 544 — сокращение старта на 65–80% с кэшированным кодом.
- Spring Boot: для Java 25+ документация рекомендует AOT-кэш, для старых JDK — CDS. Обоим нужен распакованный jar: `java -Djarmode=tools -jar app.jar extract --destination application`, тренировка с `-Dspring.context.exit=onRefresh`.
- **CRaC** (Coordinated Restore at Checkpoint): снимок прогретой JVM, восстановление за десятки миллисекунд. Только Linux, нужна сборка JDK с CRaC (Azul Zulu, BellSoft Liberica). Ресурсы (сокеты, файлы) закрываются и открываются заново через API `org.crac` (1.5.0); поддержка в Spring Boot, Quarkus, Micronaut. Команды: `-XX:CRaCCheckpointTo=dir`, `jcmd <pid> JDK.checkpoint`, `-XX:CRaCRestoreFrom=dir`. Безопасность: снимок содержит память, включая секреты и seed генераторов случайных чисел.
- **GraalVM native image**: AOT в закрытом мире, нативный бинарник. Старт за миллисекунды, меньше RSS. Пиковая пропускная способность обычно ниже C2, если нет PGO (Oracle GraalVM). Сборка дольше; рефлексии и ресурсам нужны метаданные. GC по умолчанию — Serial (G1 доступен на Linux в Oracle GraalVM). В 1BRC native image использовали в основном, чтобы убрать прогрев JIT на масштабе ~1,5 с.
- Как выбрать: долгоживущие сервисы — JIT + AOT-кэш (пиковая скорость и динамические возможности сохраняются); serverless, CLI, scale-to-zero — native image или CRaC; пакетные задачи на секунды — AOT-кэш, а `-XX:TieredStopAtLevel=1` только если замер показал пользу (он меняет пиковую скорость на старт).
- Дешёвые выигрыши на старте: меньше бинов и классов и ленивее их создание; без сканирования classpath; `-Xshare` по умолчанию включён; без огромных статических инициализаторов (медленные находит JEP 520 `method-timing=::<clinit>`).

```bash
# AOT-кэш, JDK 25+: тренировка, затем продакшен
java -XX:AOTCacheOutput=app.aot -jar app.jar   # прогнать типичную нагрузку и завершить
java -XX:AOTCache=app.aot -XX:AOTMode=required -jar app.jar   # JDK 27+; в 25–26 — AOTMode=on
# Сравнение старта
hyperfine --warmup 3 "java -jar app.jar" "java -XX:AOTCache=app.aot -jar app.jar"
```

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Project Leyden](https://openjdk.org/projects/leyden/) | OpenJDK | Обзор, документы, доклады |
| [JEP 483: Ahead-of-Time Class Loading & Linking](https://openjdk.org/jeps/483) | Oracle, 2024 | AOT-загрузка классов, цифры, ограничения |
| [JEP 514: Ahead-of-Time Command-Line Ergonomics](https://openjdk.org/jeps/514) | Oracle, 2025 | AOT-кэш в один шаг |
| [JEP 515: Ahead-of-Time Method Profiling](https://openjdk.org/jeps/515) | Oracle, 2025 | Профили методов в кэше |
| [JEP 516: Ahead-of-Time Object Caching with Any GC](https://openjdk.org/jeps/516) | Oracle, 2025 | AOT-кэш с любым GC (JDK 26) |
| [JEP 544: Ahead-of-Time Code Compilation](https://openjdk.org/jeps/544) | Oracle, 2026 | Кэш скомпилированного кода (JDK 28, запланировано) |
| [JEP 341: Default CDS Archives](https://openjdk.org/jeps/341) | Oracle | CDS по умолчанию |
| [JEP 350: Dynamic CDS Archives](https://openjdk.org/jeps/350) | Oracle | Динамический CDS |
| [AOT Cache :: Spring Boot](https://docs.spring.io/spring-boot/reference/packaging/aot-cache.html) | Spring | AOT-кэш и CDS в Spring Boot |
| [Checkpoint and Restore With the JVM :: Spring Boot](https://docs.spring.io/spring-boot/reference/packaging/checkpoint-restore.html) | Spring | Spring Boot с CRaC |
| [OpenJDK: CRaC](https://openjdk.org/projects/crac/) | OpenJDK | Проект CRaC |
| [Coordinated Restore at Checkpoint (CRaC) Documentation](https://docs.azul.com/crac/) | Azul | Руководство по CRaC |
| [Native Image](https://www.graalvm.org/latest/reference-manual/native-image/) | Oracle | Справочник Native Image |
| [Native Image: Optimizations and Performance](https://www.graalvm.org/latest/reference-manual/native-image/optimizations-and-performance/) | Oracle | GC, PGO, `-O3`, `-march=native` |
| [Profile-Guided Optimization](https://www.graalvm.org/latest/reference-manual/native-image/optimizations-and-performance/PGO/) | Oracle | PGO для native image |
| [GraalVM 25.1](https://www.graalvm.org/release-notes/25.1/) | Oracle | Модель ежемесячных innovation-релизов |
| [JVM Anatomy Quark #7: Initialization Costs](https://shipilev.net/jvm/anatomy-quarks/7-initialization-costs/) | Aleksey Shipilev | Цена инициализации объектов |

---

## Техники в коде

**Ключевые выводы**

- Это кандидаты в исправления. Пробуйте их **после** того, как профиль показал, что код горячий. Подтверждайте каждый JMH с `-prof gc`, затем макробенчмарком.
- Самые частые выигрыши в обычном коде: заранее заданный размер коллекций, примитивы вместо боксинга, конкатенация вместо `String.format`, `static final` для `Pattern`, форматтеров и маппера, один общий HTTP-клиент, батчинг в БД.

### Коллекции и раскладка данных

- `ArrayList` вместо `LinkedList` почти всегда: у связного списка переходы по указателям, 24+ байта на узел, промахи кэша. `ArrayDeque` для стеков и очередей; `EnumMap`/`EnumSet` для ключей-enum; `BitSet` или RoaringBitmap для плотных множеств int.
- Задавайте размер `HashMap`/`HashSet`, если он известен. JDK 19+: `HashMap.newHashMap(n)` и `HashSet.newHashSet(n)` учитывают коэффициент загрузки 0,75. `new HashMap<>(n)` — ошибка: такая карта перестроится на 0,75n. Проверено локально (JDK 25, Windows 11): 100 000 вставок `Integer` в 1,6–2,6 раза быстрее и на 12% меньше байт на операцию.
- Плохой `hashCode` (много коллизий) превращает корзины `HashMap` в деревья (для Comparable-ключей) или списки. Время в `HashMap.getNode` и `TreeNode` в профиле — повод проверить хеширование ключей. Изменяемые ключи ломают карты.
- Неизменяемые `List.of`, `Map.of`, `Set.of` компактны и быстры для небольших данных, которые в основном читают. Порядок обхода `Map.of` случаен для каждого запуска JVM.
- Боксинг: `List<Integer>`, `Map<Long, Double>` стоят 16+ байт на элемент плюс указатель и промах кэша. `Integer.valueOf` кэширует только −128..127 (`AutoBoxCacheMax=128`, проверено локально). Проверено локально (JDK 25, Windows 11): сумма 10 000 int по `int[]` примерно в 5 раз быстрее, чем по `List<Integer>`; `boxed().toList()` и `stream().reduce(0, Integer::sum)` аллоцируют 16–20 байт на элемент.
- Для карт и списков с примитивными ключами: **fastutil** 8.5.19, **Eclipse Collections** 13.0.0 (ещё и неизменяемые коллекции, богатый API), **HPPC** 0.13.0, коллекции Agrona (без аллокаций, для низкой задержки). Koloboke и Trove заброшены.
- Массив объектов против struct-of-arrays: для горячих числовых данных параллельные примитивные массивы (`double[] x, y`) быстрее `Point[]`, где каждый элемент — отдельный объект в куче. Это изменится с value-классами Valhalla (JEP 401, preview, запланирован на JDK 28).
- Раскладку объектов смотрите в **JOL**: `java -jar jol-cli.jar internals java.lang.String` или `ClassLayout.parseClass(Foo.class).toPrintable()`. Компактные заголовки (JDK 25 по флагу, JDK 27 по умолчанию) экономят 4 байта на объект.
- `toArray(new T[0])` не медленнее, а часто быстрее `toArray(new T[size])`.

```java
// Плохо: перестроится на 0,75 * expected
Map<String, Order> byId = new HashMap<>(expected);
// Хорошо (JDK 19+)
Map<String, Order> byId = HashMap.newHashMap(expected);
// Примитивы вместо боксинга
int total = IntStream.of(values).sum();        // а не list.stream().reduce(0, Integer::sum)
```

### Строки, форматирование, regex

- `+` компилируется в `invokedynamic` через `StringConcatFactory` с JDK 9 (JEP 280). Для одного выражения это так же быстро, как ручной `StringBuilder` (проверено локально: 56 байт на операцию у обоих). Явный `StringBuilder` нужен только в циклах: конкатенация в цикле — O(n²).
- `String.format` и `"...".formatted(...)` разбирают строку формата при каждом вызове. Проверено локально (JDK 25, Windows 11): в 9–10,5 раза медленнее и в 7,7 раза больше аллокаций (432 против 56 байт), чем конкатенация, для сообщения с двумя аргументами. Держите их вне горячих путей и вызовов логера.
- Regex: `String.split`, `replaceAll`, `matches` компилируют `Pattern` при каждом вызове (у `split` есть быстрый путь для одного не-regex символа). Выносите `private static final Pattern P = Pattern.compile(...)`. Избегайте катастрофического backtracking (вложенные квантификаторы). `Pattern.compile` дорогой.
- Compact Strings (JDK 9, Latin-1 = 1 байт на символ) включены по умолчанию. `String.intern` медленный, у него таблица фиксированного размера. Для дедупликации — свой `HashMap` или `-XX:+UseStringDeduplication`.
- Разбор чисел: `Integer.parseInt(s.substring(...))` аллоцирует подстроку. Разбирайте прямо из диапазона: `Integer.parseInt(cs, begin, end, radix)` (JDK 9+) или из байтов.
- Дата и время: `DateTimeFormatter` неизменяемый и потокобезопасный — храните в `static final`. Не создавайте форматтеры на каждый вызов. `SimpleDateFormat` медленный и не потокобезопасный.

```java
private static final Pattern COMMA = Pattern.compile("\\s*,\\s*");
private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

String msg = "user " + name + " id=" + id;      // а не String.format("user %s id=%d", name, id)
```

### Исключения, логирование, рефлексия, лямбды

- Бросок и перехват исключения дёшевы, пока не заполняется стек. Цена — в `fillInStackTrace` (пропорциональна глубине стека) и в деоптимизации, если исключение летит по пути, который C2 считал холодным. Не используйте исключения для управления потоком на горячих путях; для ожидаемых ошибок возвращайте тип-результат. Если иначе нельзя — заранее созданное исключение или конструктор `Throwable(String, Throwable, boolean enableSuppression, boolean writableStackTrace)` с `writableStackTrace=false`.
- `OmitStackTraceInFastThrow` (по умолчанию `true`, проверено локально) после многих бросков заменяет неявные исключения (NPE, AIOOBE, ClassCastException) заранее созданным экземпляром без стека. В логах тогда видно `NullPointerException` без стека. Для отладки — `-XX:-OmitStackTraceInFastThrow`. Поток таких исключений сам по себе — запах производительности.
- Логирование: параметризованные сообщения (`log.debug("x={}", x)`) не собирают строку, если уровень выключен. Дорогие аргументы — под `if (log.isDebugEnabled())`, через fluent API SLF4J 2 или лямбды Log4j2 (`log.debug("{}", () -> expensive())`). Без `%L` и location info (поиск вызывающего через обход стека), без синхронных консольных аппендеров, без логирования в тесных циклах.
- Для низкой задержки — асинхронные логеры Log4j2 (LMAX Disruptor) и garbage-free режим. Для пропускной способности — `AsyncAppender` в Logback с `neverBlock`.
- Рефлексия: с JDK 18 (JEP 416) реализована через method handles. Вызов через `static final MethodHandle` (или `VarHandle`) сворачивается и инлайнится как прямой вызов. `MethodHandle` в не-final поле намного медленнее. Кэшируйте поиск `Method`/`Field`; не вызывайте `getDeclaredMethods()` на горячих путях. `LambdaMetafactory` даёт почти прямую скорость сгенерированных аксессоров (так работает Jackson Blackbird).
- Лямбды: незахватывающие — кэшированные синглтоны; захватывающие аллоцируют (часто это убирает escape analysis после инлайнинга). Первое использование каждой лямбды и indy-точки стоит времени на bootstrap — это цена старта, не пропускной способности.

```java
// Исключение без стека для ожидаемой ошибки на горячем пути
final class ParseFailure extends RuntimeException {
    ParseFailure(String msg) { super(msg, null, false, false); }
}
```

### Стримы, циклы, диспетчеризация

- У стрима есть цена сборки конвейера, и его скорость зависит от инлайнинга всей цепочки. Для маленьких коллекций (< ~100 элементов) и очень горячих путей обычный цикл обычно быстрее и аллоцирует меньше.
- Используйте примитивные стримы (`IntStream`, `mapToInt`, `sum()`) вместо `Stream<Integer>` + `reduce(Integer::sum)`. Проверено локально (JDK 25, Windows 11): `IntStream.of(int[]).sum()` почти равен примитивному циклу, а `boxedList.stream().reduce(0, Integer::sum)` в 41–47 раз медленнее и аллоцирует ~160 КБ на вызов.
- `parallelStream()` использует общий ForkJoinPool всей JVM. Он оправдан только для больших, CPU-bound, хорошо делимых источников (массивы, `ArrayList`, диапазоны). Не для I/O, `LinkedList` и итераторов. Меряйте.
- Stream gatherers (JDK 22+, JEP 485) добавляют промежуточные операции с состоянием; без специализации они тоже боксят.
- Sealed-интерфейсы + `switch` с pattern matching и records дают исчерпывающую диспетчеризацию, которую C2 компилирует в проверки типов. Это альтернатива мегаморфным интерфейсным вызовам в горячих циклах. `switch` по enum компилируется в `tableswitch`.
- Код с ветвлениями на непредсказуемых данных: попробуйте арифметику без ветвлений (cmov, JMHSample_36). Сортировка данных заранее делает ветвления предсказуемыми.
- `Arrays.sort` для примитивов — dual-pivot quicksort (с ускорением AVX-512 для int, long, float, double на подходящих x86 с JDK 22 `[не проверено]`: JDK-8309130), для объектов — TimSort. Для очень больших массивов — `Arrays.parallelSort`.

### Конкурентность: что стоит денег

- False sharing: горячие поля, которые пишут разные потоки, в одной 64-байтовой линии кэша сериализуются на трафике когерентности. Паддинг или `@jdk.internal.vm.annotation.Contended` (вне JDK нужен `-XX:-RestrictContended`; `ContendedPaddingWidth=128`, проверено локально). Для счётчика под конкуренцией — `LongAdder` вместо `AtomicLong`. Обнаружение: JMH `-prof perfc2c` (Linux) или JMHSample_22.
- Блокировки, `synchronized` против `ReentrantLock`, виртуальные потоки (JEP 444; закрепление на `synchronized` исправлено в JDK 24, JEP 491), structured concurrency, очереди JCTools, Disruptor — в [concurrency-native.md](concurrency-native.md). Конкуренция за блокировки видна в `jfr view contention-by-site` и в async-profiler `-e lock`.

### Числа, off-heap и SIMD

- `BigDecimal` в 10–100 раз медленнее примитивной арифметики и аллоцирует на каждой операции `[не проверено]`: оценка по опыту, не замер. Для денег с фиксированной точностью — `long` в минимальных единицах, или хотя бы переиспользуйте `MathContext` и избегайте круговых конвертаций `BigDecimal.valueOf(double)` в горячих циклах.
- `Math.fma`, `Math.multiplyHigh`, `Integer.bitCount`, `numberOfTrailingZeros` — интринсики. Используйте их в битовых трюках (SWAR-приёмы в 1BRC держатся на них).
- Off-heap: FFM API финален в JDK 22 (JEP 454): `Arena`, `MemorySegment`, `ValueLayout`. `FileChannel.map(mode, offset, size, arena)` отображает файлы > 2 ГБ как `MemorySegment`. Доступ к памяти через `sun.misc.Unsafe` объявлен к удалению (JEP 471, JDK 23) и с JDK 24 предупреждает в рантайме (JEP 498): переходите на `MemorySegment` и `VarHandle`.
- Vector API: явный SIMD (`IntVector.SPECIES_PREFERRED`), большие ускорения числовых ядер, но в JDK 27 всё ещё инкубатор (JEP 537). C2 сам векторизует только простые циклы.

### I/O, сериализация, HTTP, кэш и БД

- Буферизуйте весь потоковый I/O (`BufferedInputStream`, `BufferedReader`, `Files.newBufferedReader`, буферы 8–64 КБ). Небуферизованный `FileInputStream.read()` по байту — системный вызов на каждый вызов. Маленькие файлы читайте целиком: `Files.readAllBytes`, `Files.readString`.
- Большие файлы: `FileChannel` + `ByteBuffer.allocateDirect` или отображение в память (`FileChannel.map` → `MemorySegment` в JDK 22+, без предела 2 ГБ), разбиение на куски для параллельного разбора (1BRC). `Files.lines` ленивый, но декодирует каждую строку в `String`: годится для умеренных данных, медленно для гигабайтов.
- Zero-copy: `FileChannel.transferTo`/`transferFrom` используют `sendfile`/`copy_file_range`, где они есть. `InputStream.transferTo` (JDK 9) — удобное копирование, не zero-copy.
- Кодировки: декодирование байтов в `String` — крупная статья расходов в парсерах. Работайте с байтами, где возможно (быстрые пути для ASCII).
- JSON: один `ObjectMapper`/`JsonMapper` на приложение (потокобезопасный, дорогой в создании) и заранее собранные `ObjectReader`/`ObjectWriter` для горячих типов; потоковый `JsonParser` для огромных документов; ненужные фичи выключены.
  - Jackson 3.x (`tools.jackson`, базовый JDK 17, 3.2.4) по умолчанию включает `FAIL_ON_TRAILING_TOKENS` (небольшие накладные расходы) и перешёл на пул буферов `RecyclerPool` на основе deque. Если после перехода 2.x → 3.x есть регрессия, сравните с `JsonRecyclerPools.threadLocalPool()`;
  - Blackbird (на `LambdaMetafactory`, JDK 11+) — рекомендованный преемник Afterburner для сгенерированных аксессоров; оба есть для 2.22.4, Blackbird — и для 3.2.4;
  - JSON на этапе компиляции (DSL-JSON, Avaje Jsonb) обходится без рефлексии и быстрее всех в сторонних бенчмарках. jsoniter заброшен с 2018.
- Бинарные форматы (Protobuf, Avro, CBOR и Smile через Jackson, Kryo, Fury) выигрывают у JSON по размеру и CPU, когда вы контролируете обе стороны.
- HTTP-клиенты: **один** `java.net.http.HttpClient` (или OkHttp, Apache client) на приложение — пул соединений, мультиплексирование HTTP/2; HTTP/3 в JDK 26 (JEP 517). Клиент на каждый запрос утекает потоками и убивает keep-alive. Задавайте таймауты соединения и чтения.
- Пулы соединений с БД: правило HikariCP `connections = (core_count * 2) + effective_spindle_count`. Меньший пул обычно даёт большую пропускную способность.
- База данных: батчинг вставок и обновлений (`addBatch`, `hibernate.jdbc.batch_size`), fetch size для больших чтений, устранение N+1 через join, entity graph и batch fetching, только нужные колонки, индексы. Подробно — в [domains-server.md](domains-server.md).
- Кэш: **Caffeine** 3.3.1 (допуск W-TinyLFU, почти оптимальный hit rate, высокая конкурентность) вместо Guava Cache и самодельных кэшей на `ConcurrentHashMap` (без границ — утечка). Загрузка в стиле `computeIfAbsent` защищает от cache stampede. Меряйте hit rate (`recordStats()`).
- Для сервисов, упирающихся в блокирующий I/O, виртуальные потоки (JDK 21+) повышают конкурентность без настройки пулов — см. [concurrency-native.md](concurrency-native.md). CPU-bound код они не ускоряют.

```java
// Один клиент и один маппер на приложение
private static final HttpClient HTTP = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5)).build();
private static final JsonMapper JSON = JsonMapper.builder().build();
private static final ObjectReader ORDER_READER = JSON.readerFor(Order.class);

// Ограниченный кэш вместо ConcurrentHashMap
LoadingCache<Key, Value> cache = Caffeine.newBuilder()
        .maximumSize(10_000).expireAfterWrite(Duration.ofMinutes(5))
        .recordStats().build(this::load);
```

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [JEP 280: Indify String Concatenation](https://openjdk.org/jeps/280) | Aleksey Shipilev, 2016 | Конкатенация через indy |
| [The Exceptional Performance of Lil' Exception](https://shipilev.net/blog/2014/exceptional-performance/) | Aleksey Shipilev, 2014 | Модель цены исключений с JMH |
| [Java Objects Inside Out](https://shipilev.net/jvm/objects-inside-out/) | Aleksey Shipilev | Заголовки, выравнивание, раскладка объектов |
| [JVM Anatomy Quark #10: String.intern()](https://shipilev.net/jvm/anatomy-quarks/10-string-intern/) | Aleksey Shipilev | Почему не `String.intern` |
| [JVM Anatomy Quark #26: Identity Hash Code](https://shipilev.net/jvm/anatomy-quarks/26-identity-hash-code/) | Aleksey Shipilev | Цена identity hash code |
| [JOL](https://github.com/openjdk/jol) / [JOL samples](https://github.com/openjdk/jol/tree/master/jol-samples/src/main/java/org/openjdk/jol/samples) | OpenJDK | Измерение раскладки и размера объектов; примеры про выравнивание и false sharing |
| [JEP 416: Reimplement Core Reflection with Method Handles](https://openjdk.org/jeps/416) | Oracle, 2021 | Рефлексия через method handles |
| [JEP 454: Foreign Function & Memory API](https://openjdk.org/jeps/454) / [Foreign Function and Memory API](https://docs.oracle.com/en/java/javase/25/core/foreign-function-and-memory-api.html) | Oracle, 2024 | FFM API финален (JDK 22); руководство программиста |
| [JEP 471: Deprecate the Memory-Access Methods in sun.misc.Unsafe for Removal](https://openjdk.org/jeps/471) / [JEP 498: Warn upon Use of Memory-Access Methods in sun.misc.Unsafe](https://openjdk.org/jeps/498) | Oracle, 2024 | Уход от `Unsafe` |
| [JEP 485: Stream Gatherers](https://openjdk.org/jeps/485) | Oracle, 2024 | Gatherers |
| [JEP 142: Reduce Cache Contention on Specified Fields](https://openjdk.org/jeps/142) | Oracle | `@Contended` |
| [JEP 401: Value Objects (Preview)](https://openjdk.org/jeps/401) | Oracle | Value-классы (preview, запланировано на JDK 28) |
| [HashMap.newHashMap](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/HashMap.html#newHashMap(int)) / [Pattern](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/regex/Pattern.html) / [DateTimeFormatter](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/time/format/DateTimeFormatter.html) / [MethodHandle](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/invoke/MethodHandle.html) | Oracle | Размер карты; компилировать Pattern один раз; неизменяемые форматтеры; семантика MethodHandle |
| [fastutil](https://fastutil.di.unimi.it/) | Sebastiano Vigna | Примитивные коллекции, большие массивы |
| [Eclipse Collections](https://eclipse.dev/collections/) | Eclipse Foundation | Примитивные и неизменяемые коллекции |
| [HPPC](https://github.com/carrotsearch/hppc) | Carrot Search | Примитивные коллекции |
| [Agrona](https://github.com/real-logic/agrona) | Real Logic | Коллекции и буферы для низкой задержки |
| [RoaringBitmap](https://github.com/RoaringBitmap/RoaringBitmap) | D. Lemire и др. | Сжатые битовые множества |
| [java-performance.info](https://java-performance.info/) | Mikhail Vorontsov | Старые, но добротные статьи про коллекции, строки, I/O. [устарело]: на 6 ноября 2026 домен отдаёт посторонний сайт; замена — разборы Шипилёва выше и книги из [books.md](books.md) |
| [Garbage-free logging](https://logging.apache.org/log4j/2.x/manual/garbagefree.html) / [Asynchronous loggers](https://logging.apache.org/log4j/2.x/manual/async.html) / [Performance](https://logging.apache.org/log4j/2.x/manual/performance.html) | Apache Log4j | Логирование без мусора, асинхронные логеры, советы по скорости |
| [SLF4J FAQ: logging performance](https://www.slf4j.org/faq.html#logging_performance) | QOS.ch | Параметризованные сообщения против guard-проверок |
| [Logback: AsyncAppender](https://logback.qos.ch/manual/appenders.html#AsyncAppender) | QOS.ch | Асинхронный аппендер Logback |
| [False Sharing](https://mechanical-sympathy.blogspot.com/2011/07/false-sharing.html) | Martin Thompson, 2011 | False sharing на пальцах `[не проверено]`: таймаут при проверке |
| [A Guide to False Sharing and @Contended](https://www.baeldung.com/java-false-sharing-contended) | Baeldung | `@Contended` и false sharing с JMH |
| [Journey to a N-fold improvement due to a stealthy scalability issue](https://redhatperf.github.io/post/type-check-scalability-issue/) | Red Hat perf team, 2023 | Конкуренция за secondary super cache («type pollution») в Hibernate и Quarkus |
| [FileChannel](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/channels/FileChannel.html) | Oracle | Семантика `map` и `transferTo` |
| [Java ZeroCopy I/O optimization for high throughput networking](https://developer.ibm.com/articles/j-zerocopy/) | S. K. Palaniappan, P. Nagaraja (IBM), 2008 | Классика про zero-copy (`transferTo`, sendfile) |
| [Migrating to Jackson 3](https://github.com/FasterXML/jackson/blob/main/jackson3/MIGRATING_TO_JACKSON_3.md) / [Jackson Release 3.0](https://github.com/FasterXML/jackson/wiki/Jackson-Release-3.0) | FasterXML | Миграция на Jackson 3 с умолчаниями, влияющими на скорость; заметки к 3.0 |
| [Presentation: Jackson Performance](https://github.com/FasterXML/jackson-docs/wiki/Presentation:-Jackson-Performance) | Tatu Saloranta | Переиспользовать мапперы, reader и writer |
| [Blackbird](https://github.com/FasterXML/jackson-modules-base/tree/2.x/blackbird) / [Afterburner](https://github.com/FasterXML/jackson-modules-base/tree/2.x/afterburner) | FasterXML | Модуль Blackbird; Afterburner — прежний подход |
| [DSL-JSON](https://github.com/ngs-doo/dsl-json) | Rikard Pavelic | JSON на этапе компиляции |
| [java-json-benchmark](https://github.com/fabienrenaud/java-json-benchmark) | Fabien Renaud | Сравнение JSON-библиотек на JMH |
| [jvm-serializers](https://github.com/eishay/jvm-serializers/wiki) | сообщество | Сравнение сериализаторов (старое) |
| [HttpClient](https://docs.oracle.com/en/java/javase/25/docs/api/java.net.http/java/net/http/HttpClient.html) / [JEP 517: HTTP/3 for the HTTP Client API](https://openjdk.org/jeps/517) | Oracle | Переиспользовать экземпляр; HTTP/3 в JDK 26 |
| [About Pool Sizing](https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing) | Brett Wooldridge | Формула размера пула и её обоснование |
| [N+1 query problem with JPA and Hibernate](https://vladmihalcea.com/n-plus-1-query-problem/) / [Hibernate performance tuning tips](https://vladmihalcea.com/hibernate-performance-tuning-tips/) | Vlad Mihalcea | Поиск и устранение N+1; чеклист настройки Hibernate |
| [Caffeine](https://github.com/ben-manes/caffeine) / [Benchmarks](https://github.com/ben-manes/caffeine/wiki/Benchmarks) / [Efficiency](https://github.com/ben-manes/caffeine/wiki/Efficiency) / [Design](https://github.com/ben-manes/caffeine/wiki/Design) | Ben Manes | Кэш; пропускная способность на JMH; hit rate разных политик; устройство (W-TinyLFU, буферы) |
| [Design of a Modern Cache](https://highscalability.com/design-of-a-modern-cache/) | Ben Manes, 2016 | Как устроен современный кэш |
| [JEP 444: Virtual Threads](https://openjdk.org/jeps/444) / [JEP 491: Synchronize Virtual Threads without Pinning](https://openjdk.org/jeps/491) | Oracle, 2023 / 2024 | Виртуальные потоки; без закрепления на `synchronized` (JDK 24) |

---

## Анти-паттерны производительности

| Анти-паттерн | Почему вредит | Что делать | Источник |
|---|---|---|---|
| Замер через `System.currentTimeMillis()` вокруг цикла в `main` | Нет прогрева, OSR, DCE, грубый таймер | JMH с форками и прогревом | [JMH samples](https://github.com/openjdk/jmh/tree/master/jmh-samples/src/main/java/org/openjdk/jmh/samples) / [Nanotrusting the Nanotime](https://shipilev.net/blog/2014/nanotrusting-nanotime/) |
| Бенчмарк возвращает `void` и выбрасывает результат | DCE: меряется ничто | Вернуть значение или `Blackhole.consume` | [JMHSample_08_DeadCode](https://github.com/openjdk/jmh/blob/master/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_08_DeadCode.java); проверено локально |
| Входы бенчмарка — константы | Свёртка констант | Не-final поля `@State` | [JMHSample_10_ConstantFold](https://github.com/openjdk/jmh/blob/master/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_10_ConstantFold.java); проверено локально |
| Два прогона JMH одновременно | Второй падает на `jmh.lock`; с `-Djmh.ignoreLock=true` прогоны портят друг другу замеры | Строго по очереди | [Как бенчмаркать честно](#как-бенчмаркать-честно) |
| Сравнение `gc.alloc.rate` (МБ/с) между версиями | Быстрый код показывает больше МБ/с | Сравнивать `gc.alloc.rate.norm` (байт на операцию) | [GCProfiler.java](https://github.com/openjdk/jmh/blob/master/jmh-core/src/main/java/org/openjdk/jmh/profile/GCProfiler.java); проверено локально |
| Настройка флагов GC до взгляда на аллокации | Лечит симптом | Снизить байты на операцию, потом размер кучи, потом выбор GC | [HotSpot GC Tuning Guide](https://docs.oracle.com/en/java/javase/25/gctuning/) |
| `-Xmx` = лимит памяти контейнера | Нет места для metaspace, потоков, direct-памяти → OOM kill | `MaxRAMPercentage` 50–75, NMT | [Containerize your Java Applications](https://learn.microsoft.com/en-us/azure/developer/java/containers/overview) |
| Огромный young gen «чтобы реже был GC» без замера хвостов | У LinkedIn p99.9 стал хуже | Менять одну ручку, мерить перцентили | [LinkedIn: GC Optimization](https://engineering.linkedin.com/garbage-collection/garbage-collection-optimization-high-throughput-and-low-latency-java-applications) |
| `new HashMap<>(n)` для n элементов | Всё равно перестроится на 0,75n | `HashMap.newHashMap(n)` (JDK 19+) | [HashMap.newHashMap](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/HashMap.html#newHashMap(int)); проверено локально |
| `LinkedList` как список общего назначения | Промахи кэша, узлы по 24 байта | `ArrayList` / `ArrayDeque` | [Java Objects Inside Out](https://shipilev.net/jvm/objects-inside-out/) |
| `Stream<Integer>` / `List<Integer>` на числовых горячих путях | Боксинг, аллокации, переходы по указателям | `int[]`, `IntStream`, fastutil | [fastutil](https://fastutil.di.unimi.it/); проверено локально |
| `String.format` на горячих путях и в логах | ~в 10 раз медленнее конкатенации | Конкатенация, параметризованное логирование | [JEP 280](https://openjdk.org/jeps/280); проверено локально |
| `str.split(regex)` / `replaceAll` в цикле | Pattern компилируется заново | `static final Pattern` | [Pattern](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/regex/Pattern.html) |
| Исключения для управления потоком | Заполнение стека, деоптимизация | Тип-результат; исключения без стека | [The Exceptional Performance of Lil' Exception](https://shipilev.net/blog/2014/exceptional-performance/) |
| `log.debug("x=" + x)` | Строка собирается, даже когда уровень выключен | `log.debug("x={}", x)` | [SLF4J FAQ](https://www.slf4j.org/faq.html#logging_performance) |
| `ObjectMapper`, `HttpClient`, `DateTimeFormatter`, `Pattern` на каждый вызов | Дорогое создание, потерянные кэши и пулы | Создать один раз и делить (все потокобезопасны) | [Jackson Performance](https://github.com/FasterXML/jackson-docs/wiki/Presentation:-Jackson-Performance) / [HttpClient](https://docs.oracle.com/en/java/javase/25/docs/api/java.net.http/java/net/http/HttpClient.html) |
| «Кэш» на `ConcurrentHashMap` без границ | Утечка памяти, нет вытеснения | Caffeine с ограничением по размеру или времени | [Caffeine wiki: Efficiency](https://github.com/ben-manes/caffeine/wiki/Efficiency) |
| Огромный пул соединений | Конкуренция внутри БД, пропускная способность хуже | Маленький пул по формуле HikariCP | [About Pool Sizing](https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing) |
| `parallelStream()` везде | Общий пул, накладные расходы, I/O блокирует пул | Мерить; только для больших CPU-bound данных | [Техники в коде](#стримы-циклы-диспетчеризация) |
| Поиск через рефлексию на каждый вызов | `getMethod` медленный | Кэш, `static final MethodHandle` | [JEP 416](https://openjdk.org/jeps/416) |
| `synchronized` на горячем общем счётчике | Конкуренция, пинг-понг линии кэша | `LongAdder`, шардирование | [concurrency-native.md](concurrency-native.md) |
| Микрооптимизация кода, который занимает 1% профиля | Закон Амдала: выигрыша не видно | Сначала профиль, оптимизировать верхние кадры | [Amdahl's law](https://en.wikipedia.org/wiki/Amdahl%27s_law) |
| Профилирование сэмплером с safepoint bias | Не те горячие точки | async-profiler или JFR | [Why (Most) Sampling Java Profilers Are Fucking Terrible](http://psy-lob-saw.blogspot.com/2016/02/why-most-sampling-java-profilers-are.html) |
| Бенчмарк на ноутбуке от батареи или при других задачах | 10–30% шума, троттлинг | Тихая машина, план питания, несколько форков | [Как бенчмаркать честно](#как-бенчмаркать-честно); проверено локально |
| Доступ к памяти через `Unsafe` в новом коде | Объявлен к удалению, предупреждения с JDK 24 | FFM `MemorySegment` / `VarHandle` | [JEP 471](https://openjdk.org/jeps/471) / [JEP 498](https://openjdk.org/jeps/498) |
| Разные JDK или флаги у базы и кандидата | Сравнение несравнимого | Записывать `jdkVersion` и `jvmArgs` из JSON JMH | [Бенчмарки: JMH](#формат-json--rf-json--rff-filejson) |

---

## Разборы и кейсы

**Ключевые выводы**

- **One Billion Row Challenge (1BRC, январь 2024).** Задача: посчитать min/mean/max по станциям в текстовом файле на 13 ГБ и миллиард строк. Победитель — 1,535 с (Thomas Würthinger, Quan Anh Mai, Alfonso Peterssen) на 8 ядрах AMD EPYC 7502P; базовое решение — ~2–4 минуты. Все из первой десятки использовали `sun.misc.Unsafe`. 8 из 10 — нативные образы GraalVM (JDK 21.0.2-graal), в основном чтобы убрать прогрев JIT и старт JVM на масштабе ~1,5 с. Организатор отмечает разброс ±3% между запусками, поэтому соседние места — ничья.
- Пошаговый разбор Марко Топольника (71 с → 1,7 с на Hetzner CCX33): (1) идиоматичные параллельные стримы — 71 с; (2) Graal JIT — 66 с; (3) параллельный разбор кусков файла, отображённого через `MemorySegment`, ручной разбор вместо `BufferedReader` — 17 с; (4) температура разбирается как int, без `String` и `parseDouble` — 11 с; (5) своя хеш-таблица с открытой адресацией по смещениям имён, ноль аллокаций в горячем цикле — 6,6 с; (6) `Unsafe`, SWAR-поиск точки с запятой по 8 байт за раз, разбор числа без ветвлений — 2,4 с; (7) разворачивание с учётом предсказания ветвлений для имён ≤ 16 байт — 1,8 с; (8) native image + подпроцесс, чтобы родитель вышел до очистки `munmap`; (9) куски меньше и work stealing на атомике — 1,7 с.
- Самые большие выигрыши дали удаление аллокаций и декодирования (шаги 3–5: 66 с → 6,6 с). Это доступно обычному коду без `Unsafe`. SWAR, код без ветвлений и `Unsafe` дали следующие 2,7 раза. Трюки со стартом важны, только когда вся работа длится ~1–2 с.
- Решения, заточенные под набор из 413 станций, опустились в рейтинге на наборе из 10 000 ключей. Не подгоняйте код под тестовые данные бенчмарка.
- Другие кейсы: Netflix перешёл на генерационный ZGC по умолчанию на JDK 21+, allocation stall стали редкостью; Red Hat нашёл скрытую проблему масштабируемости (конкуренция за secondary super cache) через perf и JMH; LinkedIn методично настраивал GC и увидел, что большой young gen ухудшил p99.9.

### 1BRC

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [gunnarmorling/1brc](https://github.com/gunnarmorling/1brc) | Gunnar Morling, 2024 | Правила, таблица результатов, все решения, список «1BRC on the Web» |
| [The One Billion Row Challenge](https://www.morling.dev/blog/one-billion-row-challenge/) | Gunnar Morling, 2024 | Анонс задачи |
| [1BRC—The Results Are In!](https://www.morling.dev/blog/1brc-results-are-in/) | Gunnar Morling, 2024 | Итоги и наблюдения: `Unsafe`, GraalVM, подгонка под набор ключей |
| [The Billion Row Challenge (1BRC): 71s → 1.7s](https://questdb.com/blog/billion-row-challenge-step-by-step/) | Marko Topolnik, 2024 | Лучший пошаговый разбор |
| [CalculateAverage_thomaswue.java](https://github.com/gunnarmorling/1brc/blob/main/src/main/java/dev/morling/onebrc/CalculateAverage_thomaswue.java) | Würthinger и др. | Решение-победитель |
| [CalculateAverage_artsiomkorzun.java](https://github.com/gunnarmorling/1brc/blob/main/src/main/java/dev/morling/onebrc/CalculateAverage_artsiomkorzun.java) | Artsiom Korzun | 2-е место |
| [CalculateAverage_jerrinot.java](https://github.com/gunnarmorling/1brc/blob/main/src/main/java/dev/morling/onebrc/CalculateAverage_jerrinot.java) | Jaromir Hamala | 3-е место |
| [CalculateAverage_royvanrijn.java](https://github.com/gunnarmorling/1brc/blob/main/src/main/java/dev/morling/onebrc/CalculateAverage_royvanrijn.java) | Roy van Rijn | Решение с упором на SWAR |
| [CalculateAverage_mtopolnik.java](https://github.com/gunnarmorling/1brc/blob/main/src/main/java/dev/morling/onebrc/CalculateAverage_mtopolnik.java) | Marko Topolnik | Решение из разбора |
| [The One Billion Row Challenge Shows That Java Can Process a One Billion Rows File in Two Seconds](https://www.infoq.com/news/2024/01/1brc-fast-java-processing/) | Olimpiu Pop (InfoQ), 2024 | Интервью с Морлингом |
| [InfoQ Dev Summit Munich: How to Optimize Java for the 1BRC](https://www.infoq.com/news/2024/10/optimize-java-1brc/) | InfoQ, 2024 | Конспект доклада (Würthinger, van Rijn) |
| [Coffee Compiler Club, 2024_01_12](https://www.youtube.com/watch?v=NJNIbgV6j-Y) | Cliff Click, 2024 | Автор C2 разбирает своё решение |
| [One Billion Row Challenge](https://curiouscoding.nl/posts/1brc/) | Ragnar Groot Koerkamp, 2024 | Низкоуровневый взгляд (на Rust), идеи переносятся |
| [Reading a file insanely fast in Java](https://rmannibucau.metawerx.net/reading-a-file-insanely-fast-in-java.html) | Romain Manni-Bucau, 2024 | Быстрое чтение файлов в Java `[не проверено]`: страница не отвечала |
| [#1BRC Timeline](https://tivrfoa.github.io/java/benchmark/performance/2024/02/05/1BRC-Timeline.html) | tivrfoa, 2024 | Какая идея какой выигрыш дала `[не проверено]` |

### Посты компаний и блоги

Посты Netflix, Uber, LinkedIn, Datadog, Red Hat и Брендана Грегга о Java-флеймграфах уже есть в разделах [Память и GC](#память-и-gc), [Профилирование](#профилирование) и [Техники в коде](#техники-в-коде). Netflix в оригинале на netflixtechblog.com отдаёт 403 скриптам, поэтому ссылка ведёт на копию в inside.java.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [shipilev.net](https://shipilev.net/) | Aleksey Shipilev | JVM Anatomy Quarks, JMH, посты про GC |
| [Richard Startin's Blog](https://richardstartin.github.io/) | Richard Startin | Глубокие посты про JIT, SIMD, битовые трюки с доказательствами на JMH |
| [5 Mundane Java Performance Tips](https://richardstartin.github.io/posts/5-java-mundane-performance-tricks) | Richard Startin | Пять приземлённых, но работающих приёмов |
| [Gunnar Morling: blog](https://www.morling.dev/blog/) | Gunnar Morling | 1BRC, JFR, ZGC |
| [tschatzl @ github](https://tschatzl.github.io/) | Thomas Schatzl (лид G1) | Изменения G1 по релизам |
| [Stefan Johansson's Blog](https://kstefanj.github.io/) | Stefan Johansson (G1) | Улучшения GC по версиям JDK |
| [Performance - Inside.java](https://inside.java/tag/performance) | Oracle | Официальные посты о производительности |
| [JavaSpecialists Newsletter](https://www.javaspecialists.eu/archive/) | Heinz Kabutz | Рассылка, много выпусков про скорость |
| [Vanilla Java](https://blog.vanillajava.blog/) | Peter Lawrey | Java с низкой задержкой |
| [foojay.io](https://foojay.io/) | сообщество | Статьи сообщества, в том числе о производительности |
| [Systems Performance 2nd Edition Book](https://www.brendangregg.com/systems-performance-2nd-edition-book.html) | Brendan Gregg, 2020 | Книга о производительности на уровне ОС |
| [Java Performance, 2nd Edition](https://www.oreilly.com/library/view/java-performance-2nd/9781492056102/) | Scott Oaks, 2020 | Книга, подробнее в [books.md](books.md) `[не проверено]` |
| [Optimizing Cloud Native Java](https://www.oreilly.com/library/view/optimizing-cloud-native/9781098149332/) | Ben Evans, James Gough, 2024 | Книга, подробнее в [books.md](books.md) `[не проверено]` |
