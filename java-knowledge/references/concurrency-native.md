# Конкурентность, виртуальные потоки и нативный код в Java

Справочник для выбора модели конкурентности (платформенные потоки, виртуальные потоки, реактивные библиотеки, ForkJoin), для работы с Java Memory Model, для безопасного обращения с `java.util.concurrent`, для вызова нативного кода (FFM API, JNI, JNA) и для миграции с `sun.misc.Unsafe`. Актуально на ноябрь 2026: последняя LTS — JDK 25, последний релиз — JDK 27 (GA 2026-09-15), JDK 28 ожидается 2027-03-09 (RDP1 2026-12-03). Главные новости: `synchronized` больше не закрепляет (pin) виртуальные потоки с JDK 24, scoped values стали финальными в JDK 25, структурная конкурентность станет финальной в JDK 28, FFM API финален с JDK 22.

Пометки в таблицах ресурсов: без пометки — страница прочитана или ссылка проверена; `[не проверено]` — единственный вторичный источник или страница недоступна автоматической проверке; `[устарело]` — заменено, замена названа рядом; «проверено локально» — воспроизведено на JDK 25 (Temurin 25.0.x) и JDK 21 (Temurin 21.0.12) под Windows 11, результаты даны отношениями, а не абсолютными числами. Смежные темы: JMH, GC, JIT и профилирование — [performance.md](performance.md); флаги `--enable-preview`, `--add-modules`, `--enable-native-access` в Maven/Gradle — [toolchain.md](toolchain.md); jcstress и многопоточные тесты — [testing.md](testing.md); потоковые модели Spring, Quarkus, Helidon — [domains-server.md](domains-server.md); книги по конкурентности — [books.md](books.md).

- [Версии и статус (ноябрь 2026)](#версии-и-статус-ноябрь-2026)
- [Выбор инструмента по нагрузке](#выбор-инструмента-по-нагрузке)
- [Модель памяти Java](#модель-памяти-java)
- [Исполнители, ForkJoinPool и CompletableFuture](#исполнители-forkjoinpool-и-completablefuture)
- [Блокировки, атомики, коллекции и ThreadLocal](#блокировки-атомики-коллекции-и-threadlocal)
- [Виртуальные потоки](#виртуальные-потоки)
- [Структурная конкурентность и scoped values](#структурная-конкурентность-и-scoped-values)
- [Реактивные библиотеки и виртуальные потоки](#реактивные-библиотеки-и-виртуальные-потоки)
- [Классические проблемы и их диагностика](#классические-проблемы-и-их-диагностика)
- [Нативный код: FFM API, JNI и Unsafe](#нативный-код-ffm-api-jni-и-unsafe)
- [Vector API, GraalVM Native Image и Polyglot](#vector-api-graalvm-native-image-и-polyglot)
- [Антипаттерны](#антипаттерны)
- [Чеклист ревью](#чеклист-ревью)
- [Локальные замеры](#локальные-замеры)
- [Дополнительные материалы и открытые вопросы](#дополнительные-материалы-и-открытые-вопросы)

## Версии и статус (ноябрь 2026)

| Тема | Статус и версия | Источник |
|---|---|---|
| Последний JDK | 27 (GA 2026-09-15); последняя LTS — 25; JDK 28 GA запланирован на 2027-03-09 (RDP1 2026-12-03) | openjdk.org/projects/jdk/27, /28 |
| Виртуальные потоки | Финальны в JDK 21 (JEP 444); превью — JEP 425 (19), JEP 436 (20) | JEP 444 |
| `synchronized` не закрепляет поток | С JDK 24 (JEP 491). Закрепление остаётся: нативные кадры (JNI или upcall FFM, который блокируется), загрузка классов, блокировка в `<clinit>`, ожидание инициализации класса другим потоком | JEP 491; проверено локально |
| `-Djdk.tracePinnedThreads` | Удалён в JEP 491 (JDK 24): флаг в командной строке ничего не делает. Использовать JFR `jdk.VirtualThreadPinned` | JEP 491; проверено локально |
| Структурная конкурентность | Инкубатор JEP 428 (19), 437 (20); превью JEP 453 (21), 462 (22), 480 (23), 499 (24), **505 (25, 5-е превью, новый `StructuredTaskScope.open()` и `Joiner`)**, 525 (26, 6-е), 533 (27, 7-е); **JEP 543 — финальный в JDK 28 («Integrated», без изменений относительно 533)** | JEP 505/525/533/543 |
| Scoped values | Финальны в JDK 25 (JEP 506); превью — JEP 429 (инкубатор, 20), 446, 464, 481, 487 | JEP 506; проверено локально |
| Stable values и lazy constants | JEP 502 `StableValue` превью (25), JEP 526 `LazyConstant` 2-е превью (26), JEP 531 3-е превью (27). В списке целей JDK 28 на 2026-11-06 не значатся | JEP 502/526/531, страница jdk/28 |
| FFM API | Финален в JDK 22 (JEP 454); превью — JEP 424 (19), 434 (20), 442 (21) | JEP 454 |
| Ограничения JNI | JDK 24 (JEP 472): предупреждения для `System.loadLibrary`, привязки native-методов и ограниченных методов FFM; флаги `--enable-native-access`, `--illegal-native-access=allow\|warn\|deny` (по умолчанию `warn`) | JEP 472; проверено локально |
| Память через `sun.misc.Unsafe` | Deprecated for removal в JDK 23 (JEP 471); предупреждение по умолчанию в JDK 24 (JEP 498, `--sun-misc-unsafe-memory-access=warn`). Фаза 3 (`deny` по умолчанию) запланирована на «JDK 26 или позже»: ни в одном списке JEP для 26, 27, 28 её нет, фактическое значение по умолчанию в 26 и 27 не подтверждено | JEP 471/498; для 26/27 `[не проверено]` |
| Мутация final-полей | JDK 26 (JEP 500): запись в `final`-поля через глубокую рефлексию предупреждает; флаги `--enable-final-field-mutation`, `--illegal-final-field-mutation=allow\|warn\|debug\|deny` | JEP 500 |
| Vector API | По-прежнему инкубатор: JEP 508 (25, 10-й), 529 (26, 11-й), **537 (27, 12-й, без изменений API)**; ждёт value classes из Valhalla. В списке JDK 28 пока нет. JEP 401 Value Objects (Preview) нацелен на 28 | JEP 537, jdk/28 |
| `ForkJoinPool` | В JDK 25 реализует `ScheduledExecutorService` (в 21 нет) | проверено локально |
| `ThreadPoolExecutor.finalize()` | Удалён в JDK 27 | заметки о релизе JDK 27 |
| JSON-дамп потоков | JDK 27: числовые id и счётчики, `formatVersion: 2` | заметки о релизе JDK 27 |

Версии библиотек на 2026-11-06: jcstress 0.16 (Maven Central, 2023-02, новее релиза нет), jextract EA-сборка 25-jextract+2-4 (2025-11-25, репозиторий активен, коммит 2026-10-14), JNA 5.19.1, jnr-ffi 2.3.7 (2026-11-02), Project Reactor reactor-core 3.8.7 (2026-08-24) стабильный и 3.9.0-RC1 (BOM 2025.0.8 стабильный, 2026.0.0-RC1), RxJava 3.1.12 стабильный (2025-09) и 4.0.0-alpha-21 (2026-08), SmallRye Mutiny 3.3.1 (2026-10-19), BlockHound 1.0.18.RELEASE, kotlinx.coroutines 1.11.0 (2026-05), Caffeine 3.3.1 (2026-10-13), JCTools 4.0.7 (2026-08), LMAX Disruptor 4.0.0 (2023-09), Resilience4j 2.4.0, Netflix concurrency-limits 0.5.4.

## Выбор инструмента по нагрузке

| Нагрузка | Первый выбор (JDK 25/27) | Также подходит | Избегать |
|---|---|---|---|
| Много одновременных I/O-запросов (HTTP, БД, RPC), поток на запрос | Виртуальные потоки (`newVirtualThreadPerTaskExecutor`, Spring `spring.threads.virtual.enabled`, Helidon 4, Quarkus `@RunOnVirtualThread`) и лимиты через `Semaphore` или пул соединений | Реактивный стек, если он уже есть | Огромные пулы платформенных потоков; `newCachedThreadPool` |
| CPU-нагрузка с параллелизмом по данным | `ForkJoinPool`/`RecursiveTask`, параллельные стримы на больших массивах, фиксированный пул платформенных потоков размером около числа ядер | Vector API (инкубатор) для SIMD-ядер | Виртуальные потоки; блокировки в common pool |
| Fan-out к N сервисам с дедлайном, fail-fast или «первый успешный» | `StructuredTaskScope` (финальный в 28, превью в 25–27) с `Joiner` и таймаутом | `CompletableFuture.allOf/anyOf` и `orTimeout` на executor виртуальных потоков | Неуправляемый `submit()` без отмены |
| Потоки данных с backpressure, окнами, повторами, слиянием источников | Reactor, Mutiny, RxJava 3, Kafka Streams | `Flow` и `SubmissionPublisher` для простых случаев внутри JVM; стадии на ограниченных `BlockingQueue` и виртуальных потоках | Неограниченные очереди; блокировки внутри операторов |
| Передача данных между стадиями (producer/consumer) | Ограниченные `ArrayBlockingQueue`/`LinkedBlockingQueue(capacity)` | JCTools MPSC/SPSC, LMAX Disruptor для сверхнизкой задержки | `LinkedBlockingQueue()` без ёмкости |
| Общие счётчики и метрики под конкуренцией | `LongAdder`/`LongAccumulator` | Счётчики Micrometer (внутри адаптеры) | `synchronized`-счётчики; `volatile long++` |
| Уникальные ID и последовательности | `AtomicLong.incrementAndGet()` | Последовательности БД, UUIDv7 | `LongAdder` (сумма не атомарна) |
| Конфигурация и снимки состояния, чтение преобладает | Неизменяемый объект за `volatile`/`AtomicReference` (copy-on-write) | `CopyOnWriteArrayList` для списков слушателей | `ReentrantReadWriteLock` для крошечных секций |
| Кэш в процессе | Caffeine (границы, срок жизни, асинхронная загрузка) | `ConcurrentHashMap.computeIfAbsent` для небольшой неограниченной мемоизации | Самописный LRU поверх `synchronized LinkedHashMap` в большом масштабе |
| Контекст запроса (пользователь, trace id, tenant) | `ScopedValue` (финальный в 25) и явные параметры | Micrometer context-propagation для реактивного кода и MDC | `InheritableThreadLocal`; `ThreadLocal` без `remove()` |
| Ограничение скорости, bulkhead | `Semaphore`, Resilience4j Bulkhead/RateLimiter | Netflix concurrency-limits (адаптивный) | Размер пула потоков как ограничитель скорости (при виртуальных потоках) |
| Периодические задачи | `ScheduledExecutorService` (один платформенный поток), Spring `@Scheduled` | Планирование в `ForkJoinPool` (25+) | `Timer` (одна упавшая задача убивает его) |
| Ленивые синглтоны и дорогие константы | Holder idiom (static), `volatile` DCL (экземпляр) | `LazyConstant` (превью 26–27) | DCL без `volatile` |
| Вызов C-библиотек | FFM API (и jextract) на 22+ | JNA для редких вызовов; JNI для унаследованного кода | Off-heap-хаки через `sun.misc.Unsafe` |
| Память вне кучи, отображение файлов | `MemorySegment` (`FileChannel.map(…, arena)`), `Arena.ofShared` для нескольких потоков | `ByteBuffer` (лимит 2 ГБ) на старых JDK | `Unsafe.allocateMemory` |
| SIMD-ядра для чисел | Vector API (инкубатор, `--add-modules jdk.incubator.vector`) после проверки автовекторизации C2 | Нативные библиотеки через FFM (BLAS, SIMD на C) | Ожидание ускорения без JMH |
| Быстрый старт, малый RSS (CLI, serverless) | GraalVM Native Image | Кэш AOT проекта Leyden (JDK 24+), CRaC (сборки вендоров) | Расчёт на то, что native image обгонит JIT по пиковой пропускной способности |

Правило выбора: сначала определить, чем ограничена нагрузка (CPU, I/O, внешний ресурс), затем брать инструмент из таблицы. Профилирование и бенчмарки — в [performance.md](performance.md).

## Модель памяти Java

**Ключевые выводы**

- **Happens-before (HB) — единственный контракт.** Рёбра по JLS §17.4.5: порядок программы внутри потока; разблокировка монитора → последующая блокировка того же монитора; запись volatile → последующее чтение того же поля; `Thread.start()` → первое действие запущенного потока; последнее действие потока → возврат из `join()`; статический инициализатор → любое использование класса; всё из `java.util.concurrent` (`BlockingQueue.put` → `take`, завершение `Future` → `get`, `CountDownLatch.countDown` → возврат из `await`). **Гонка данных** — два конфликтующих обращения (минимум одна запись), не упорядоченных через HB. Корректно синхронизированные программы без гонок получают последовательную согласованность (DRF-SC).
- **Программа с гонкой не «чуть неверна», а не определена на практике.** JIT может поднять, объединить, удалить или переставить обычные обращения: `while (!stop) {}` с не-volatile `stop` может скомпилироваться в бесконечный цикл; обычная запись `long`/`double` может разорваться (JLS 17.7). Doug Lea: «Experience has shown that guesses about guaranteed orderings in Plain mode are often wrong».
- **`volatile`** даёт видимость и порядок (последовательная согласованность для всех volatile-обращений), но не атомарность составных действий: `count++` на volatile остаётся гонкой с потерей обновлений. Нужны `AtomicLong`/`LongAdder` или блокировка.
- **`final`-поля (JLS 17.5).** Если `this` не утёк при конструировании, любой поток, увидевший ссылку, видит корректно инициализированные final-поля (и объекты, достижимые через них на конец конструктора), даже если ссылка опубликована с гонкой. Для не-final полей такой гарантии нет. Shipilev: на HotSpot барьер после записи *любого* final-поля сейчас покрывает все поля, но полагаться на это нельзя: рефакторинг может молча убрать барьер.
- **JDK 26, «final means final» (JEP 500).** Изменение `final`-полей через `Field.setAccessible(true).set(...)` теперь предупреждает, в будущем по умолчанию будет `deny`. Библиотекам сериализации нужен `sun.reflect.ReflectionFactory`. Выигрыш: JIT может сворачивать final-поля не-`Serializable` объектов в константы.
- **Идиомы безопасной публикации** (Shipilev, «Safe Publication and Safe Initialization»): сохранить ссылку (а) в `volatile`-поле или `Atomic*`, (б) в `final`-поле объекта, который сам опубликован, (в) под той же блокировкой, которую берут читатели, (г) в статическом инициализаторе; либо передать через конкурентную коллекцию, очередь или отправку в executor. «Добавить `synchronized` только в писателе не помогает. Читатели тоже должны синхронизироваться».
- **Double-checked locking (DCL)** сломан без `volatile` и корректен с ним:

  ```java
  private volatile Helper helper;
  Helper get() {
      Helper h = helper;                 // одно volatile-чтение на быстром пути
      if (h == null) {
          synchronized (this) {
              h = helper;
              if (h == null) helper = h = new Helper();
          }
      }
      return h;
  }
  ```

  Для статических синглтонов лучше **holder idiom**: инициализация класса ленива и защищена JVM, в бенчмарках Shipilev на ARM у него самый быстрый установившийся режим. Для полей экземпляра на 25+ можно взять **`LazyConstant`** (превью, см. [Структурная конкурентность и scoped values](#структурная-конкурентность-и-scoped-values)) или просто инициализировать сразу.
- **Режимы доступа `VarHandle`** (JEP 193, JDK 9; Doug Lea, «Using JDK 9 Memory Order Modes»). Лестница силы: **Plain < Opaque < Release/Acquire < Volatile**. Plain — только последовательная семантика. Opaque — согласованность по одной переменной и «в конце концов» видимость, без порядка между переменными (счётчики прогресса, мониторинг). Release/Acquire — причинность для односторонней передачи («ownership, in which only the owner may write, but others may read»), дешевле volatile на ARM/POWER. Volatile — полный порядок всех volatile-обращений (рукопожатия в стиле Деккера, консенсус). Барьеры: `VarHandle.acquireFence/releaseFence/fullFence/loadLoadFence/storeStoreFence`. Правило: **по умолчанию volatile и блокировки; слабые режимы — только с тестом jcstress и бенчмарком, доказывающим выигрыш**.

  ```java
  private static final VarHandle STATE;
  static { try { STATE = MethodHandles.lookup().findVarHandle(Node.class, "state", int.class); }
           catch (ReflectiveOperationException e) { throw new ExceptionInInitializerError(e); } }
  boolean tryClaim() { return STATE.compareAndSet(this, 0, 1); }
  void publish(int v) { STATE.setRelease(this, v); }
  int  observe()      { return (int) STATE.getAcquire(this); }
  ```

- **x86 скрывает ошибки.** x86-TSO переставляет только store→load, поэтому многие программы с гонками «работают» на x86-серверах и ломаются на ARM (Graviton, Apple Silicon, Ampere). Проверять на ARM или через jcstress, который заодно проверяет перестановки компилятора.
- **jcstress** (OpenJDK Code Tools) — инструмент для эмпирической проверки утверждений о JMM. Запуск: `mvn archetype:generate -DinteractiveMode=false -DarchetypeGroupId=org.openjdk.jcstress -DarchetypeArtifactId=jcstress-java-test-archetype -DgroupId=org.sample -DartifactId=test -Dversion=1.0`, затем `mvn clean verify && java -jar target/jcstress.jar` (`-m quick` для CI, `-h` для опций). Аннотации: `@JCStressTest`, `@State`, `@Actor`, `@Arbiter`, `@Outcome(id=..., expect=ACCEPTABLE|ACCEPTABLE_INTERESTING|FORBIDDEN)`; классы результатов `II_Result`, `JJ_Result` и другие. Примеры разбиты на API, JMM и Concurrency. Последний релиз 0.16 (2023): проект живёт в исходниках, релизы редки.

  ```java
  @JCStressTest
  @Outcome(id = "1, 0", expect = Expect.ACCEPTABLE_INTERESTING, desc = "reordering visible")
  @Outcome(expect = Expect.ACCEPTABLE, desc = "sequential outcomes")
  @State
  public class Reorder {
      int x, y;
      @Actor public void writer() { x = 1; y = 1; }
      @Actor public void reader(II_Result r) { r.r1 = y; r.r2 = x; }
  }
  ```

- **Неизменяемость и confinement лучше хитрого порядка памяти.** Records с final-полями неизменяемых типов, `List.copyOf`, привязка к одному потоку (один владелец, actor) и передача сообщений убирают большую часть рассуждений о JMM.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [JLS §17 Threads and Locks (SE 25)](https://docs.oracle.com/javase/specs/jls/se25/html/jls-17.html) | Oracle | Нормативные правила HB, final-полей и разрыва слов |
| [Java Memory Model Pragmatics](https://shipilev.net/blog/2014/jmm-pragmatics/) | A. Shipilev, 2014 | Лучшее длинное объяснение JMM для практиков |
| [Close Encounters of the JMM Kind](https://shipilev.net/blog/2016/close-encounters-of-jmm-kind/) | A. Shipilev, 2016 | Каталог мифов о JMM с доказательствами в jcstress |
| [Safe Publication and Safe Initialization in Java](https://shipilev.net/blog/2014/safe-public-construction/) | A. Shipilev, 2014 | Варианты DCL, final-поля, holder idiom, бенчмарки на x86 и ARM |
| [JVM Anatomy Quarks](https://shipilev.net/jvm/anatomy-quarks/) | A. Shipilev, 2017–2023 | Короткие заметки о внутренностях JVM (укрупнение блокировок, барьеры, TLAB) |
| [Using JDK 9 Memory Order Modes](https://gee.cs.oswego.edu/dl/html/j9mm.html) | D. Lea, 2018 | Авторитетное описание Plain/Opaque/RA/Volatile |
| [The JSR-133 Cookbook for Compiler Writers](https://gee.cs.oswego.edu/dl/jmm/cookbook.html) | D. Lea | Барьеры по процессорам; почему volatile стоит столько, сколько стоит |
| [JSR-133 (Java Memory Model) FAQ](https://www.cs.umd.edu/~pugh/java/memoryModel/jsr-133-faq.html) | J. Manson, B. Goetz, 2004 | Классический FAQ простым языком, остаётся точным |
| [The "Double-Checked Locking is Broken" Declaration](https://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html) | D. Bacon, W. Pugh и др. | Историческая причина, по которой DCL требует volatile |
| [The Java Memory Model (страница Pugh)](https://www.cs.umd.edu/~pugh/java/memoryModel/) | W. Pugh | Указатель статей по JMM и архив рассылки |
| [VarHandle Javadoc (25)](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/invoke/VarHandle.html) | Oracle | Определения режимов доступа, барьеры |
| [JEP 193: Variable Handles](https://openjdk.org/jeps/193) | D. Lea, P. Sandoz, 2014–2017 | Обоснование, замена CAS из Unsafe |
| [JEP 500: Prepare to Make Final Mean Final](https://openjdk.org/jeps/500) | R. Pressler, A. Bateman, 2026 | Предупреждения о мутации final-полей и флаги |
| [jcstress (GitHub)](https://github.com/openjdk/jcstress) | OpenJDK | Харнесс, архетип, запуск |
| [jcstress samples](https://github.com/openjdk/jcstress/tree/master/jcstress-samples/src/main/java/org/openjdk/jcstress/samples) | A. Shipilev | Размеченные уроки: основы JMM, использование API, поведение библиотек |
| [jcstress project page](https://openjdk.org/projects/code-tools/jcstress/) | OpenJDK | Обзор проекта |
| [Acquire and Release Semantics](https://preshing.com/20120913/acquire-and-release-semantics/) | J. Preshing, 2012 | Интуиция для порядка RA вне привязки к языку |

## Исполнители, ForkJoinPool и CompletableFuture

**Ключевые выводы**

- **Не создавать неограниченные ресурсы случайно.** `Executors.newFixedThreadPool(n)` и `newSingleThreadExecutor()` используют *неограниченный* `LinkedBlockingQueue` (при перегрузке память растёт до OOM). `newCachedThreadPool()` создаёт неограниченное число *потоков* (`maximumPoolSize = Integer.MAX_VALUE`, `SynchronousQueue`): всплеск трафика порождает тысячи платформенных потоков. В сервисах собирать `ThreadPoolExecutor` явно: **ограниченная очередь, именованные потоки, осознанная политика отказа**.

  ```java
  var pool = new ThreadPoolExecutor(
      8, 32, 60, TimeUnit.SECONDS,
      new ArrayBlockingQueue<>(1_000),                       // ограниченная
      Thread.ofPlatform().name("orders-", 0).factory(),      // именованная фабрика, JDK 21+
      new ThreadPoolExecutor.CallerRunsPolicy());            // backpressure вместо потери задач
  ```

- **Правило роста `ThreadPoolExecutor` удивляет всех.** Потоки сверх `corePoolSize` создаются только когда очередь *заполнена*. При неограниченной очереди `maximumPoolSize` не используется никогда. Обработчик по умолчанию — `AbortPolicy` (`RejectedExecutionException`); `CallerRunsPolicy` притормаживает отправителя; `Discard*` молча теряет работу, при их использовании нужны лог или метрика.
- **Размер пула.** Для CPU-нагрузки — около числа ядер (`availableProcessors()` учитывает контейнер). Для блокирующего I/O — по закону Литтла (`потоки ≈ пропускная способность × задержка`). На 21+ вместо этого брать виртуальные потоки и ограничивать *ресурс* (пул соединений БД, семафор), а не число потоков. Для пулов соединений эталон — «About Pool Sizing» из HikariCP (малые пулы лучше больших).
- **Исключения пропадают в `submit()`.** Исключение сохраняется в `Future`; если никто не вызвал `get()`, его никто не увидит. `execute()` отправляет исключение в `UncaughtExceptionHandler`. Нужно либо всегда потреблять future, либо оборачивать задачи, либо переопределять `afterExecute`.
- **Жизненный цикл.** `ExecutorService` — `AutoCloseable` с JDK 19: `try (var ex = Executors.newVirtualThreadPerTaskExecutor()) { ... }` на `close()` ждёт все задачи. Для долгоживущих пулов: `shutdown()` → `awaitTermination(timeout)` → `shutdownNow()`. Рабочие потоки делать не-daemon, только если JVM действительно должна их ждать. JDK 27 удалил `ThreadPoolExecutor.finalize()` (код, который его переопределяет или вызывает, может перестать компилироваться).
- **ForkJoinPool** — для CPU-задач по схеме «разделяй и властвуй» (кража работы, `RecursiveTask`). У **common pool** `parallelism = availableProcessors() - 1` (проверено локально: 31 при 32 CPU); его делят параллельные стримы, `CompletableFuture.*Async` без executor и многие библиотеки. **Блокировки в common pool голодят всех** (`ManagedBlocker` существует, но редко является правильным решением). `-Djava.util.concurrent.ForkJoinPool.common.parallelism=N` — крайняя мера. В JDK 25 `ForkJoinPool` дополнительно реализует `ScheduledExecutorService` (в 21 нет, проверено локально).
- **Параллельные стримы окупаются только** для больших (от 10⁴–10⁵ элементов), делимых источников (массивы, `ArrayList`, `IntStream.range`) и CPU-тяжёлых, stateless, неблокирующих операций, не зависящих от порядка. Избегать для I/O, `LinkedList`/`Stream.iterate`, малых коллекций, `forEachOrdered`/`limit` на упорядоченных стримах и любых побочных эффектов в общее не потокобезопасное состояние. Измерять через JMH ([performance.md](performance.md)).
- **Идиомы и ловушки `CompletableFuture`**
  - Executor по умолчанию для `*Async` без executor — `ForkJoinPool.commonPool()`. **На JDK 21 при parallelism common pool, равном 1 (например, контейнер на 2 CPU), `defaultExecutor()` — это `CompletableFuture$ThreadPerTaskExecutor`: новый платформенный поток на каждую задачу; на JDK 25 common pool используется и в этом случае** (проверено локально). Для I/O всегда передавать явный executor.
  - `join()` бросает непроверяемое `CompletionException`, `get()` — `ExecutionException`; `exceptionally`/`handle` ниже по цепочке обычно получают **обёрнутое** `CompletionException`, поэтому нужен `getCause()` (проверено локально).
  - `cancel(true)` **не прерывает** выполняющееся вычисление («cancellation is treated as just another form of exceptional completion»). `orTimeout(...)` (JDK 9) завершает future с `TimeoutException`, но задача продолжает выполняться; `completeOnTimeout(value, ...)` подставляет запасное значение.
  - Неасинхронные стадии (`thenApply`) выполняются в потоке, завершившем предыдущую стадию (или в вызывающем, если она уже завершена): дорогие колбэки могут попасть в поток event loop. Использовать `thenApplyAsync(fn, executor)` осознанно.
  - `allOf` возвращает `CompletableFuture<Void>`; результаты собирать так: `futures.stream().map(CompletableFuture::join).toList()` после завершения. Для fan-out с отменой лучше структурная конкурентность.
  - JDK 19+: `Future.state()`, `resultNow()`, `exceptionNow()`; JDK 12: `exceptionallyCompose`; подкласс с переопределением `defaultExecutor()` и `newIncompleteFuture()` меняет executor по умолчанию для всей цепочки.

  ```java
  var io = Executors.newVirtualThreadPerTaskExecutor();
  CompletableFuture<Quote> q = CompletableFuture
      .supplyAsync(() -> pricing.fetch(id), io)
      .orTimeout(2, TimeUnit.SECONDS)
      .exceptionally(t -> Quote.fallback(unwrap(t)));   // unwrap снимает CompletionException
  ```

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [java.util.concurrent package summary (25)](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/package-summary.html) | Oracle / D. Lea | Гарантии согласованности памяти каждого класса j.u.c |
| [ThreadPoolExecutor Javadoc](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/ThreadPoolExecutor.html) | Oracle | Правила core/max/queue, политики отказа, хуки |
| [Executors Javadoc](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/Executors.html) | Oracle | Что на самом деле строит каждая фабрика |
| [ForkJoinPool Javadoc](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/ForkJoinPool.html) | Oracle | Свойства common pool, ManagedBlocker |
| [CompletableFuture Javadoc](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/CompletableFuture.html) | Oracle | Executor по умолчанию, семантика cancel, таймауты |
| [CompletableFuture.java (исходники)](https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/java/util/concurrent/CompletableFuture.java) | D. Lea | Заметки о реализации (резервный executor, раскрутка стека) |
| [A Java Fork/Join Framework (статья)](https://gee.cs.oswego.edu/dl/papers/fj.pdf) | D. Lea, 2000 | Дизайн кражи работы в основе FJP |
| [Concurrency JSR-166 Interest Site](https://gee.cs.oswego.edu/dl/concurrency-interest/) | D. Lea | Исходники j.u.c, история, рассылка |
| [ExecutorService — 10 tips and tricks](https://www.nurkiewicz.com/2014/11/executorservice-10-tips-and-tricks.html) | T. Nurkiewicz, 2014 | Имена потоков, ограниченные очереди, проглоченные исключения |
| [CompletableFuture can't be interrupted](https://www.nurkiewicz.com/2015/03/completablefuture-cant-be-interrupted.html) | T. Nurkiewicz, 2015 | Почему cancel(true) не затрагивает задачу |
| [Guide to CompletableFuture](https://www.baeldung.com/java-completablefuture) | Baeldung | Обзор API с примерами |
| [About Pool Sizing](https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing) | B. Wooldridge | Почему малые пулы соединений выигрывают; формула |
| [Java Concurrency in Practice](https://jcip.net/) | B. Goetz и др., 2006 | Концептуальная основа, написана до Loom |

## Блокировки, атомики, коллекции и ThreadLocal

**Ключевые выводы**

- **Блокировки: брать самое простое, что работает.** JEP 491 повторяет JCiP §13.4: «Use `synchronized` where practical, since it is more convenient and less error prone». `java.util.concurrent.locks` нужны для `tryLock` с таймаутом или прерыванием, честности, нескольких `Condition` и разделения чтения и записи. На JDK 24+ `synchronized` не закрепляет виртуальные потоки, поэтому переписывать его ради Loom больше не нужно. Всегда `lock(); try { … } finally { unlock(); }`.
- **`ReentrantReadWriteLock`** помогает, только если секции чтения длинные и чтений намного больше записей; повышения read→write нет (дедлок), при нечестном режиме писатели могут голодать. **`StampedLock`**: оптимистичные чтения (`tryOptimisticRead` и `validate`) очень дёшевы для крошечного состояния с преобладанием чтений, но он **не реентерабелен**, без `Condition`, а оптимистичная секция должна переносить несогласованные чтения (скопировать поля в локальные, проверить `validate`, затем использовать).

  ```java
  long s = lock.tryOptimisticRead();
  double cx = x, cy = y;                     // значения могут быть разорваны
  if (!lock.validate(s)) { s = lock.readLock(); try { cx = x; cy = y; } finally { lock.unlockRead(s); } }
  return Math.hypot(cx, cy);
  ```

- **Атомики.** `AtomicLong`/`AtomicReference` и `compareAndSet`/`updateAndGet` (функция должна быть без побочных эффектов: она может выполняться повторно). При высокой конкуренции на запись — **`LongAdder`/`LongAccumulator`** (раздельные ячейки; `sum()` не атомарный снимок: годится для метрик, не годится для ID). `AtomicReferenceFieldUpdater` или `VarHandle` экономят объект на поле в горячих структурах.
- **ConcurrentHashMap**
  - Проверка-затем-действие (`if (!map.containsKey(k)) map.put(k, v)`) — гонка. Заменять на `putIfAbsent`, `computeIfAbsent`, `compute`, `merge`.
  - `computeIfAbsent`/`compute` блокируют корзину: «the computation should be short and simple» и «must not modify this map during computation». **Рекурсивный `computeIfAbsent` (мемоизация с рекурсией) бросает `IllegalStateException: Recursive update`** на современных JDK (проверено локально на 25); на JDK 8 мог зациклиться (JDK-8062841 `[не проверено]`). Для однопоточной мемоизации брать обычный `HashMap` или вычислять вне лямбды.
  - Нет `null`-ключей и значений; `size()` — оценка типа `int`, нужен `mappingCount()`; для конкурентного множества `ConcurrentHashMap.newKeySet()`.
  - Нужны вытеснение, срок жизни, границы размера — **Caffeine** (3.3.1), а не самописный кэш на CHM.
- **Другие коллекции.** `CopyOnWriteArrayList` — для списков слушателей с преобладанием чтения; `ConcurrentLinkedQueue`/`Deque` — lock-free неограниченные; `ConcurrentSkipListMap` — отсортированные конкурентные карты; `Collections.synchronizedX` требует ручного `synchronized (list)` вокруг итерации. JCTools (4.0.7) даёт очереди SPSC/MPSC для специализированных конвейеров с низкой задержкой; LMAX Disruptor (4.0.0) — кольцевой буфер для обработки событий.
- **BlockingQueue.** `ArrayBlockingQueue` (ограниченная, одна блокировка), `LinkedBlockingQueue` (можно ограничить: *всегда задавать ёмкость*), `SynchronousQueue` (передача из рук в руки), `LinkedTransferQueue`, `PriorityBlockingQueue` и `DelayQueue` (неограниченные). В сервисах использовать `offer(e, timeout)`/`poll(timeout)`, чтобы остановка и перегрузка были наблюдаемы; останавливать потребителей poison pill или прерыванием.
- **Синхронизаторы.** `CountDownLatch` (одноразовое «ждать N событий»), `CyclicBarrier` (повторно используемая точка встречи фиксированного числа участников), `Phaser` (динамические участники, фазы), `Semaphore` (ограничение одновременного доступа; идиоматичный лимитер для виртуальных потоков), `Exchanger` (обмен парой). У каждого `acquire()` должен быть `release()` в `finally`.
- **Ловушки `ThreadLocal`.** (1) В пулах значения переживают задачи: утечки и перетекание данных между запросами; всегда `remove()` в `finally`. (2) Утечки загрузчиков классов в серверах приложений через значения классов веб-приложения. (3) `InheritableThreadLocal` копируется при каждом создании потока. (4) При миллионах виртуальных потоков кэши на поток (форматтеры, пулы буферов в `ThreadLocal`) создаются заново на каждую задачу и умножают память (JEP 444: «use thread locals only after careful consideration»; Quarkus отдельно называет пулы в стиле Jackson). Для передачи контекста брать `ScopedValue` (финальный в 25), для кэшей — общие потокобезопасные объекты (`DateTimeFormatter` неизменяем).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [StampedLock Javadoc](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/locks/StampedLock.html) | Oracle | Канонический пример оптимистичного чтения и оговорки |
| [ReentrantReadWriteLock Javadoc](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/locks/ReentrantReadWriteLock.html) | Oracle | Честность, запрет повышения, пример CachedData |
| [LongAdder Javadoc](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/atomic/LongAdder.html) | Oracle | Когда предпочесть AtomicLong или LongAdder |
| [ConcurrentHashMap Javadoc](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/ConcurrentHashMap.html) | Oracle | Контракт compute*, ISE при рекурсивном обновлении, mappingCount |
| [Phaser Javadoc](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/Phaser.html) | Oracle | Шаблоны барьера с динамическим числом участников |
| [Semaphore Javadoc](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/Semaphore.html) | Oracle | Разрешения, честность |
| [ThreadLocal Javadoc](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/ThreadLocal.html) | Oracle | `withInitial`, `remove` |
| [JDK-8062841: ConcurrentHashMap.computeIfAbsent stuck in an endless loop](https://bugs.openjdk.org/browse/JDK-8062841) | OpenJDK bug | История ловушки с рекурсией `[не проверено]` (сайт отдаёт 403 ботам) |
| [Caffeine](https://github.com/ben-manes/caffeine) | B. Manes | Промышленный кэш (W-TinyLFU), асинхронная загрузка |
| [Caffeine benchmarks](https://github.com/ben-manes/caffeine/wiki/Benchmarks) | B. Manes | Цифры по конкуренции в сравнении с CHM и Guava |
| [JCTools](https://github.com/JCTools/JCTools) | N. Wakart и др. | Очереди SPSC/MPSC/MPMC, которыми пользуются Netty и другие |
| [LMAX Disruptor](https://github.com/LMAX-Exchange/disruptor) · [docs](https://lmax-exchange.github.io/disruptor/) | LMAX | Конвейеры на кольцевом буфере, mechanical sympathy |
| [False Sharing](https://mechanical-sympathy.blogspot.com/2011/07/false-sharing.html) | M. Thompson, 2011 | Конкуренция за строку кэша `[не проверено]` (недоступно при проверке) |
| [Single Writer Principle](https://mechanical-sympathy.blogspot.com/2011/09/single-writer-principle.html) | M. Thompson, 2011 | Правило дизайна, убирающее большую часть конкуренции `[не проверено]` |
| [Guide to false sharing and @Contended](https://www.baeldung.com/java-false-sharing-contended) | Baeldung | `-XX:-RestrictContended`, ячейки LongAdder |
| [ThreadLocal (and virtual threads)](https://www.happycoders.eu/java/scoped-values/) | S. Woltmann (HappyCoders) | ThreadLocal против ScopedValue пошагово |

## Виртуальные потоки

**Ключевые выводы**

- **Что это.** Экземпляры `java.lang.Thread`, которые JDK планирует на небольшой пул платформенных *carrier*-потоков (`ForkJoinPool` в FIFO-режиме, `parallelism` равен числу CPU). Блокирующий I/O JDK, `Thread.sleep`, `BlockingQueue`, `j.u.c.locks` и (с 24) мониторы **снимают** виртуальный поток с carrier, а не блокируют его. Создание стоит примерно как аллокация объекта; стеки лежат в куче (JEP 444).
- **Помогают коду «поток на запрос» с преобладанием I/O** (HTTP-клиенты, JDBC, RPC fan-out): выше пропускная способность при том же простом блокирующем коде. **CPU-код не ускоряют** (Quarkus: «useless and counterproductive» для CPU-нагрузки); вытесняющего разделения времени нет, поэтому долгие вычисления монополизируют carrier.
- **Не пулировать** («virtual threads are not expensive so there is never a need to pool them»). Использовать `Executors.newVirtualThreadPerTaskExecutor()` или `Thread.ofVirtual().name("worker-", 0).start(r)`. **Параллелизм к дефицитному ресурсу ограничивать `Semaphore`**, а не маленьким пулом (JEP 444):

  ```java
  private final Semaphore dbPermits = new Semaphore(50);
  Result query(String sql) throws InterruptedException {
      dbPermits.acquire();
      try { return jdbc.run(sql); } finally { dbPermits.release(); }
  }
  ```

  Узким местом становятся внешние ресурсы (пулы соединений, лимиты скорости, файловые дескрипторы): 10 000 виртуальных потоков, ждущих пул из 20 соединений, получат таймауты вместо того, чтобы стоять в очереди пула потоков.
- **Закрепление (pinning).** JDK 21–23: блокировка внутри `synchronized` или `Object.wait()` закрепляет carrier; при малом числе carrier всё приложение встаёт (продакшен-инцидент Netflix «Dude, Where's My Lock?», 2024: Spring Boot 3 + Tomcat + блокировка на `synchronized` в библиотеке дала зависание, похожее на дедлок). **JDK 24+ (JEP 491) убрал закрепление мониторами.** Закрепление остаётся при блокировке с нативным кадром в стеке (JNI или upcall FFM), при загрузке классов, внутри статического инициализатора и при ожидании завершения инициализации класса другим потоком. «Pinning does not make an application incorrect, but it might hinder its scalability».
  - Проверено локально: 16 виртуальных потоков, каждый делает `sleep(200)` внутри своего `synchronized`, parallelism планировщика 2. **На JDK 25 примерно в 7,6 раза быстрее, чем на 21** (на 21 закрепление, около 8 волн по 200 мс; на 25 одна волна).
  - Проверено локально: JFR `jdk.VirtualThreadPinned` на JDK 21 даёт события для `synchronized`+`sleep` и для `<clinit>`+`sleep`; на JDK 25 — **только** для случая с инициализатором класса, с новыми полями `pinnedReason = "VM call to PinJfr$Slow.<clinit> on stack"` и `blockingOperation = "LockSupport.park"`. `Object.wait()` внутри `synchronized` на 25 события не дал.
  - `-Djdk.tracePinnedThreads=short|full` печатал стеки на 21 и **молча игнорируется на 25** (проверено локально): флаг удалён JEP 491.
- **Диагностика**
  - JFR: `jdk.VirtualThreadPinned` (включено по умолчанию, порог 20 мс), `jdk.VirtualThreadSubmitFailed` (включено), `jdk.VirtualThreadStart/End` (по умолчанию выключены). Запуск: `java -XX:StartFlightRecording=filename=app.jfr ...`, затем `jfr print --events jdk.VirtualThreadPinned app.jfr`.
  - Дампы потоков: `jcmd <pid> Thread.dump_to_file -format=json threads.json` включает виртуальные потоки (классические `jstack`/`Thread.print` показывают только платформенные); в JDK 27 JSON использует числовые id и `formatVersion: 2`. В JDK 25 есть также `jcmd <pid> Thread.vthread_scheduler` и `Thread.vthread_pollers`. Проверено локально: 1000 спящих виртуальных потоков дали 1043 записи потоков в JSON.
  - Тесты: библиотека `junit-virtual-threads` (`@ShouldNotPin`) валит тест при закреплении (из руководства Quarkus).
- **Настройка планировщика (нужна редко).** `-Djdk.virtualThreadScheduler.parallelism=N` (число carrier, по умолчанию число ядер), `-Djdk.virtualThreadScheduler.maxPoolSize=M` (временная компенсация при захваченных carrier: например `Object.wait` на 21, файловый I/O на некоторых платформах). Не настраивать до измерений.
- **Виртуальные потоки всегда daemon** (проверено локально). В Spring Boot полностью виртуальное приложение может завершиться по окончании `main`: задать `spring.main.keep-alive=true`.
- **ThreadLocal работает, но стоит памяти:** у каждого виртуального потока своя копия; не использовать как пул объектов или кэш. Для контекста — `ScopedValue`.
- **Переключатели во фреймворках**
  - Spring Boot 3.2+: `spring.threads.virtual.enabled=true` (обработка запросов Tomcat/Jetty, `@Async` → `SimpleAsyncTaskExecutor`, планировщик → `SimpleAsyncTaskScheduler`; свойства пулов игнорируются). В документации: «Java 24 or later is strongly recommended». В Spring Boot 4 то же свойство переключает больше компонентов, включая слушатели Kafka и Rabbit и HTTP-клиенты, — см. [domains-server.md → «Фреймворки и рантаймы»](domains-server.md#1-фреймворки-и-рантаймы).
  - Quarkus: явное включение на эндпоинт через `@RunOnVirtualThread` (намеренно не глобально).
  - Helidon 4 SE WebServer («Níma») целиком построен на виртуальных потоках, API в блокирующем стиле; нужна Java 21+, для 4.3+ рекомендована 25 `[не проверено]`.
  - Tomcat 10.1/11: `<Connector ... useVirtualThreads="true">`; Jetty 12: executor на виртуальных потоках в `QueuedThreadPool`/`VirtualThreadPool`. Точные имена параметров перечитать в документации своей версии сервера.
  - Vert.x 4.5+/5: verticle на виртуальных потоках (`ThreadingModel.VIRTUAL_THREAD`) с `await`.
- **Старые драйверы закрепляют потоки сильно** на 21–23 (например, PostgreSQL JDBC < 42.6.0 по руководству Quarkus): обновить драйвер или перейти на JDK 24+.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [JEP 444: Virtual Threads](https://openjdk.org/jeps/444) | R. Pressler, A. Bateman, 2023 | Дизайн, «делай/не делай», события JFR, дампы jcmd |
| [JEP 491: Synchronize Virtual Threads without Pinning](https://openjdk.org/jeps/491) | P. Chilano Mateo, A. Bateman, 2024 | Что всё ещё закрепляет; удаление tracePinnedThreads |
| [JEP 425](https://openjdk.org/jeps/425) / [JEP 436](https://openjdk.org/jeps/436) | OpenJDK | История превью `[устарело]`, замена — JEP 444 |
| [Virtual Threads — Java 25 Core Libraries guide](https://docs.oracle.com/en/java/javase/25/core/virtual-threads.html) | Oracle | Официальное руководство, включая поиск закрепления |
| [Virtual Threads — Java 21 guide](https://docs.oracle.com/en/java/javase/21/core/virtual-threads.html) | Oracle | Рекомендации эпохи 21 (закрепление в synchronized) `[устарело]` для 24+, замена — руководство для JDK 25 |
| [Thread.Builder Javadoc](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Thread.Builder.html) | Oracle | `ofVirtual()/ofPlatform()`, имена, фабрики |
| [dev.java: Virtual Threads](https://dev.java/learn/new-features/virtual-threads/) | Oracle DevRel | Учебник |
| [State of Loom, part 1](https://cr.openjdk.org/~rpressler/loom/loom/sol1_part1.html) | R. Pressler, 2020 | Мотивация; почему не async/await |
| [Project Loom page](https://openjdk.org/projects/loom/) · [loom repo](https://github.com/openjdk/loom) · [loom-dev list](https://mail.openjdk.org/pipermail/loom-dev/) | OpenJDK | Текущая работа (собственные планировщики и др.) |
| [Java 21 Virtual Threads — Dude, Where's My Lock?](https://netflixtechblog.com/java-21-virtual-threads-dude-wheres-my-lock-3052540e231d) | Netflix JVM team, 2024 | Реальный сбой из-за закрепления и его диагностика `[не проверено]` (403 ботам; подтверждено по InfoQ) |
| [Netflix performance case study (InfoQ)](https://www.infoq.com/news/2024/08/netflix-performance-case-study) | InfoQ, 2024 | Краткий пересказ инцидента Netflix `[не проверено]` |
| [Spring Boot: SpringApplication → Virtual threads](https://docs.spring.io/spring-boot/reference/features/spring-application.html) | Spring | `spring.threads.virtual.enabled`, keep-alive |
| [Spring Boot: Task execution and scheduling](https://docs.spring.io/spring-boot/reference/features/task-execution-and-scheduling.html) | Spring | Какие executor переключаются на виртуальные потоки |
| [Embracing Virtual Threads](https://spring.io/blog/2022/10/11/embracing-virtual-threads) | Spring team, 2022 | План внедрения в Spring |
| [Web Applications and Project Loom](https://spring.io/blog/2023/02/27/web-applications-and-project-loom) | Spring team, 2023 | Замеры Tomcat + Loom |
| [Quarkus: Virtual thread support reference](https://quarkus.io/guides/virtual-threads) | Quarkus | Модель явного включения, монополизация, тесты на закрепление |
| [Helidon SE introduction (v4)](https://helidon.io/docs/v4/se/introduction) | Oracle Helidon | Веб-сервер на виртуальных потоках `[не проверено]` |
| [Jetty 12 threading architecture](https://jetty.org/docs/jetty/12/programming-guide/arch/threads.html) | Eclipse Jetty | Виртуальные потоки в пуле Jetty |
| [Tomcat 11 Executor config](https://tomcat.apache.org/tomcat-11.0-doc/config/executor.html) | Apache | Параметры executor на виртуальных потоках |
| [Vert.x core docs](https://vertx.io/docs/vertx-core/java/) | Eclipse Vert.x | Verticle на виртуальных потоках |
| [Virtual threads (HappyCoders)](https://www.happycoders.eu/java/virtual-threads/) | S. Woltmann | Подробный учебник с демонстрацией закрепления |
| [Virtual Thread vs Thread](https://www.baeldung.com/java-virtual-thread-vs-thread) | Baeldung | Быстрое сравнение |
| [Virtual thread pinning (Todd Ginsberg)](https://todd.ginsberg.com/post/java/virtual-thread-pinning/) | T. Ginsberg | Поиск закрепления через JFR |
| [Virtual Threads (InfoQ article)](https://www.infoq.com/articles/java-virtual-threads/) | InfoQ | Обзор и внедрение |
| [jcmd manual (25)](https://docs.oracle.com/en/java/javase/25/docs/specs/man/jcmd.html) | Oracle | `Thread.dump_to_file`, `Thread.print` |
| [jfr manual (25)](https://docs.oracle.com/en/java/javase/25/docs/specs/man/jfr.html) | Oracle | `jfr print/summary` |
| [JFR event reference (SapMachine)](https://sap.github.io/SapMachine/jfrevents/) | SAP | Список событий JFR по версиям, включая VirtualThread* |

## Структурная конкурентность и scoped values

**Ключевые выводы**

- **Статус.** `StructuredTaskScope` был превью в 21–27. API **переработано в JDK 25 (JEP 505)**: подклассов `new ShutdownOnFailure()` больше нет, scope открывается статическими фабриками с политикой `Joiner`. JDK 26 (JEP 525): `anySuccessfulResultOrThrow` → `anySuccessfulOrThrow`, `allSuccessfulOrThrow` возвращает `List` результатов, добавлен `Joiner.onTimeout()`. JDK 27 (JEP 533): третий параметр типа `R_X` для исключения из `join()`, joiner бросают `ExecutionException` (перегрузки принимают `Function` для отображения исключений), `awaitAll()` удалён, `onTimeout()` → `timeout()`, таймауты приходят как `ExecutionException` с причиной `CancelledByTimeoutException`. **JDK 28 (JEP 543, «Integrated») делает API JDK 27 финальным без изменений.**
- **Ловушка рассинхрона версий для кода от ИИ и из блогов.** Примеры 2023–24 (`ShutdownOnFailure`, `throwIfFailed()`) **не компилируются на 25+**; код для 25 (`FailedException`, `TimeoutException`, `anySuccessfulResultOrThrow`) не компилируется на 27/28. Для нового кода целиться в финальный API JDK 28; на 25 LTS считать его превью (`--enable-preview --release 25`).

| JDK | JEP | Статус | Форма API и отличия |
|---|---|---|---|
| 21 | 453 | превью | Старый API: подклассы `ShutdownOnFailure`/`ShutdownOnSuccess`, `throwIfFailed()` |
| 22 | 462 | 2-е превью | Тот же старый API |
| 23 | 480 | 3-е превью | Тот же старый API |
| 24 | 499 | 4-е превью | Тот же старый API |
| 25 LTS | 505 | 5-е превью | Переработка: `StructuredTaskScope.open()`, `open(Joiner)`, `open(Joiner, Function<Configuration,Configuration>)`; фабрики `Joiner`: `allSuccessfulOrThrow`, `anySuccessfulResultOrThrow`, `awaitAll`, `awaitAllSuccessfulOrThrow`, `allUntil`; сбой → `FailedException`, таймаут → `TimeoutException` |
| 26 | 525 | 6-е превью | `anySuccessfulResultOrThrow` → `anySuccessfulOrThrow`; `allSuccessfulOrThrow` возвращает `List`; `Joiner.onTimeout()` |
| 27 | 533 | 7-е превью | Параметр типа `R_X`; `ExecutionException` из `join()`; `awaitAll()` удалён; `onTimeout()` → `timeout()`; таймаут = `ExecutionException` с причиной `CancelledByTimeoutException` |
| 28 | 543 | финальный | API 27 без изменений |

Scoped values по релизам: JDK 20 — инкубатор (JEP 429), 21 — превью (JEP 446), 22 — превью (JEP 464), 23 — превью (JEP 481), 24 — превью (JEP 487), 25 — финальные (JEP 506), в 25 `orElse(null)` запрещён.

- **Форма JDK 25** (проверено локально): `StructuredTaskScope` — интерфейс. Без `--enable-preview` компиляция падает с «StructuredTaskScope is a preview API and is disabled by default.» Умолчание `open()` при сбое даёт `StructuredTaskScope$FailedException` (причина — исключение подзадачи); настроенный таймаут даёт `StructuredTaskScope$TimeoutException`.
- **Финальная идиома (JDK 28)**, JEP 543/533:

  ```java
  // fail-fast fan-out: первый сбой отменяет остальных; join() бросает ExecutionException
  try (var scope = StructuredTaskScope.open()) {
      Subtask<User>  user  = scope.fork(() -> users.find(id));
      Subtask<Order> order = scope.fork(() -> orders.latest(id));
      scope.join();
      return new Page(user.get(), order.get());
  }
  // побеждает первый успешный, с дедлайном
  try (var scope = StructuredTaskScope.open(Joiner.<String>anySuccessfulOrThrow(),
                                            cf -> cf.withTimeout(Duration.ofSeconds(2)))) {
      mirrors.forEach(m -> scope.fork(() -> m.fetch(key)));
      return scope.join();
  }
  ```

  Гарантии: подзадачи работают в (виртуальных) потоках, которыми владеет scope; `close()` отменяет и ждёт, поэтому потоков-сирот нет; дампы потоков показывают дерево задач; scoped values наследуются форками. Неправильное использование (fork из потока не-владельца, неверная вложенность) → `StructureViolationException`.
- **Scoped values (финальные в JDK 25, JEP 506).** Неизменяемый контекст ограниченной длительности — замена `ThreadLocal`, используемого как неявные параметры (id запроса, принципал, транзакция). `set()` нет; привязка заканчивается при возврате из `run`/`call`; форки `StructuredTaskScope` наследуют её без копирования; `ForkJoinPool` и обычные executor не наследуют.

  ```java
  static final ScopedValue<User> CURRENT = ScopedValue.newInstance();
  ScopedValue.where(CURRENT, user).run(() -> handle(request));   // внутри доступен CURRENT.get()
  ```

  Проверено локально: `ScopedValue.where(...).run(...)` на JDK 21 без preview не компилируется, на 25 работает; вложенная перепривязка работает; вне привязки `isBound()` равно false.
- **Lazy constants (превью).** JDK 25 `StableValue` (JEP 502) → JDK 26/27 `LazyConstant` (JEP 526/531; `LazyConstant.of(supplier)`, в 27 `List.ofLazy`, `Map.ofLazy`, `Set.ofLazy`). Вычисление ровно один раз, потокобезопасно, сворачивается в константу в `final`-поле: современная альтернатива DCL и holder idiom. API ещё меняется: не использовать в библиотеках до финализации.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [JEP 543: Structured Concurrency (final, JDK 28)](https://openjdk.org/jeps/543) | A. Bateman, V. Klang, R. Pressler, 2026 | Финальный API |
| [JEP 533 (7th preview, JDK 27)](https://openjdk.org/jeps/533) | те же | Параметр `R_X`, `timeout()`, ExecutionException |
| [JEP 525 (6th preview, JDK 26)](https://openjdk.org/jeps/525) | те же | Переименования относительно 25 `[устарело]`, замена — JEP 543 |
| [JEP 505 (5th preview, JDK 25)](https://openjdk.org/jeps/505) | те же | Переработка `open()` и `Joiner`; что поставляется в 25 LTS `[устарело]`, замена — JEP 543 |
| [JEP 453](https://openjdk.org/jeps/453) / [462](https://openjdk.org/jeps/462) / [480](https://openjdk.org/jeps/480) / [499](https://openjdk.org/jeps/499) | OpenJDK | Старый API `ShutdownOnFailure` (21–24) `[устарело]`, замена — JEP 543 |
| [StructuredTaskScope Javadoc (25)](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/StructuredTaskScope.html) | Oracle | Справка по превью API для 25 |
| [Structured Concurrency guide (25)](https://docs.oracle.com/en/java/javase/25/core/structured-concurrency.html) | Oracle | Руководство для 25 |
| [JEP 506: Scoped Values (final)](https://openjdk.org/jeps/506) | A. Haley и др., 2025 | Семантика в сравнении с ThreadLocal |
| [JEP 429: Scoped Values (incubator)](https://openjdk.org/jeps/429) | OpenJDK | История `[устарело]`, замена — JEP 506 |
| [ScopedValue Javadoc (25)](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/ScopedValue.html) | Oracle | API |
| [Scoped Values guide (25)](https://docs.oracle.com/en/java/javase/25/core/scoped-values.html) | Oracle | Руководство |
| [JEP 502: Stable Values (Preview)](https://openjdk.org/jeps/502) | P. Minborg, M. Cimadamore, 2025 | Происхождение lazy constants `[устарело]`, замена — JEP 531 |
| [JEP 526](https://openjdk.org/jeps/526) / [JEP 531: Lazy Constants](https://openjdk.org/jeps/531) | те же | Текущий превью API |
| [Structured Concurrency (HappyCoders)](https://www.happycoders.eu/java/structured-concurrency-structuredtaskscope/) | S. Woltmann | Изменения API по версиям |
| [Scoped Values (HappyCoders)](https://www.happycoders.eu/java/scoped-values/) | S. Woltmann | Учебник, включая перепривязку и наследование |
| [Structured Concurrency in Java (Rock the JVM)](https://rockthejvm.com/articles/structured-concurrency-in-java) | R. Cardin | Длинный учебник (проверять версию API) |
| [Structured Concurrency (Baeldung)](https://www.baeldung.com/java-structured-concurrency) | Baeldung | Короткое введение (старый API, проверять версию) |
| [Horstmann unblog: virtual threads & structured concurrency](https://horstmann.com/unblog/2023-06-27/index.html) | C. Horstmann, 2023 | Критический взгляд, подводные камни (API частично `[устарело]`) |

## Реактивные библиотеки и виртуальные потоки

**Ключевые выводы**

- **Виртуальные потоки убрали главную *причину*, по которой команды шли в реактивность** (не занимать поток на каждый ожидающий запрос). Для сервисов запрос-ответ с вызовами БД и HTTP блокирующий код на виртуальных потоках (Spring MVC и `spring.threads.virtual.enabled`, Helidon 4, Quarkus `@RunOnVirtualThread`) проще писать, отлаживать (настоящие стеки) и профилировать.
- **Реактивность по-прежнему оправдана для:** настоящих **потоков данных** с **backpressure** (протокол Reactive Streams `request(n)`; `java.util.concurrent.Flow` — копия интерфейсов в JDK), долгоживущих соединений с push (SSE, WebSocket, конвейеры Kafka/RSocket), операторов по времени (window, buffer, debounce, sample, retry с backoff), слияния множества источников событий и существующих кодовых баз на WebFlux/Vert.x/Mutiny. Не переписывать работающую реактивную систему только потому, что есть Loom; не начинать новый CRUD-сервис реактивным ради масштабируемости.
- **Никогда не блокировать в реактивном конвейере** (на потоках event loop вроде Netty или планировщика `parallel()` Reactor): это останавливает все запросы на этом цикле. Неизбежные блокирующие вызовы оборачивать: `Mono.fromCallable(blockingCall).subscribeOn(Schedulers.boundedElastic())`. С Reactor 3.6 флаг `-Dreactor.schedulers.defaultBoundedElasticOnVirtualThreads=true` на Java 21+ заставляет `boundedElastic()` запускать каждую задачу в новом виртуальном потоке `[не проверено]` (по поиску в документации Reactor; актуальна reactor-core 3.8.7).
- **BlockHound** (`io.projectreactor.tools:blockhound` 1.0.18) — Java-агент, который бросает исключение при блокирующих вызовах (`Thread.sleep`, сокетный и файловый I/O, `Object.wait` и т. п.) на потоках, помеченных неблокирующими (Reactor `parallel`/`single`, event loop Netty, computation в RxJava). `BlockHound.install()` вызывать в тестовом `static {}`/`@BeforeAll`; на JDK 13+ добавить в JVM тестов `-XX:+AllowRedefinitionToAddDeleteMethods` (Surefire `argLine` / Gradle `jvmArgs`); для JPMS — режим `-javaagent:`. Исключения: `allowBlockingCallsInside(Class, method)`. Запускать в тестах CI, не в продакшене.
- **Выбор библиотеки.** Project Reactor (Spring WebFlux, R2DBC; Context для передачи контекста, `Hooks.enableAutomaticContextPropagation()` с Micrometer); RxJava 3 (наследие Android; RxJava 4 в конце 2026 всё ещё alpha: в продакшен не брать); SmallRye Mutiny 3.x (Quarkus; `Uni`/`Multi` с событийными именами, `.emitOn()`/`.runSubscriptionOn()`). Все совместимы через Reactive Streams.
- **Чеклист ловушек реактивного кода.** Без подписки ничего не происходит («assembly vs subscription»); `.block()` внутри конвейера; побочные эффекты в `map` вместо `doOnNext`/`flatMap`; неограниченная конкурентность `flatMap` (передавать аргумент `concurrency`); проглоченные ошибки (без `onError` вызывается `onErrorDropped`); MDC и безопасность на `ThreadLocal` теряются при смене потока (использовать Reactor `Context` и context-propagation); горячие и холодные источники повторяют побочные эффекты на каждого подписчика.
- **Корутины Kotlin** (kotlinx.coroutines 1.11.0): структурная конкурентность через `coroutineScope`, отмена по кооперации, `Dispatchers.IO` для блокирующих вызовов, `Flow` для холодных потоков с backpressure через приостановку. На JVM диспетчер корутин можно построить на executor виртуальных потоков (`Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()`). Остальное по Kotlin — в соответствующем справочнике по Kotlin.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Reactor reference guide](https://projectreactor.io/docs/core/release/reference/) | Reactor team | Операторы, планировщики, Context, отладка |
| [Reactor: Threading and Schedulers](https://projectreactor.io/docs/core/release/reference/coreFeatures/schedulers.html) | Reactor team | boundedElastic, режим виртуальных потоков `[не проверено]` |
| [What's new in reactor-core 3.6.0](https://spring.io/blog/2023/10/31/what-new-is-coming-in-reactor-core-3-6-0) | Spring/Reactor team, 2023 | Флаг boundedElastic на виртуальных потоках `[не проверено]` |
| [reactor-core (GitHub)](https://github.com/reactor/reactor-core) | Reactor | Релизы (3.8.7 / 3.9.0-RC1) |
| [BlockHound](https://github.com/reactor/BlockHound) · [Quick start](https://github.com/reactor/BlockHound/blob/master/docs/quick_start.md) | Reactor | Поиск блокирующих вызовов в неблокирующих потоках |
| [RxJava](https://github.com/ReactiveX/RxJava) | ReactiveX | Документация 3.x, статус 4.0 alpha |
| [SmallRye Mutiny docs](https://smallrye.io/smallrye-mutiny/latest/) · [GitHub](https://github.com/smallrye/smallrye-mutiny) | Red Hat | API Uni/Multi, интеграция с Quarkus |
| [Reactive Streams specification](https://www.reactive-streams.org/) | RS group | Протокол из четырёх интерфейсов и правила |
| [java.util.concurrent.Flow Javadoc](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/Flow.html) | Oracle | Интерфейсы Reactive Streams в JDK |
| [Kotlin coroutines overview](https://kotlinlang.org/docs/coroutines-overview.html) · [kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines) | JetBrains | Структурная конкурентность в Kotlin |
| [Netflix concurrency-limits](https://github.com/Netflix/concurrency-limits) | Netflix | Адаптивное ограничение параллелизма (в стиле TCP-congestion) |
| [Resilience4j Bulkhead](https://resilience4j.readme.io/docs/bulkhead) | Resilience4j | Bulkhead на семафоре и пуле потоков, работает с виртуальными потоками |

## Классические проблемы и их диагностика

**Ключевые выводы**

- **Deadlock** (циклическое ожидание блокировок). Профилактика: глобальный порядок блокировок, `tryLock(timeout)`, секции без колбэков и чужого кода, не блокироваться на future под блокировкой, не вкладывать `synchronized`. Обнаружение: `jcmd <pid> Thread.print` / `jstack` печатают «Found one Java-level deadlock» с обоими стеками (проверено локально); `ThreadMXBean.findDeadlockedThreads()` для health-проверок; JFR `jdk.JavaMonitorEnter`/`jdk.ThreadPark` для долгих ожиданий. Классические дампы не показывают виртуальные потоки, поэтому нужен `Thread.dump_to_file -format=json`. Дампы JDK 21–23 не содержали и информации о блокировках для виртуальных потоков, из-за чего инцидент Netflix было трудно диагностировать `[не проверено]`.
- **Дедлок и голодание, вызванные пулом.** Задача в ограниченном пуле отправляет подзадачу в *тот же* пул и ждёт её; либо все воркеры common pool блокируются на I/O. Решения: отдельные пулы по уровням зависимости, структурная конкурентность, виртуальные потоки.
- **Livelock** (потоки без конца повторяют попытки и уступают друг другу, например при симметричном backoff): добавить случайный backoff с jitter. **Голодание**: нечестные блокировки под нагрузкой, голодание писателей в RW-блокировках, задачи с низким приоритетом, которым не достаётся времени; виртуальные потоки, монополизирующие carrier CPU-работой.
- **Конкуренция (contention).** Профилировать JFR (`jdk.JavaMonitorEnter`, `jdk.ThreadPark`), async-profiler `-e lock`; исправлять сокращением критических секций, шардированием (striping), `LongAdder`, неизменяемыми снимками с обменом через `volatile` (copy-on-write), схемой с одним писателем.
- **False sharing.** Независимые горячие поля в одной 64-байтной строке кэша гоняются между ядрами. В JDK используется `@jdk.internal.vm.annotation.Contended` (ячейки `LongAdder`, поля `Thread`); в прикладном коде для него нужны `-XX:-RestrictContended` и `--add-exports java.base/jdk.internal.vm.annotation=ALL-UNNAMED`, иначе — ручное выравнивание или накопление по потокам. Сначала измерить (JMH с `@Threads`, perf c2c на Linux).
- **Ленивая инициализация.** Holder idiom для статики; `volatile` DCL для экземпляров; в перспективе `LazyConstant` (превью); или просто немедленная инициализация. Сломанные шаблоны: DCL без volatile, `if (x == null) x = new X()` в общем коде, публикация `this` из конструктора (регистрация слушателей, запуск потоков в конструкторах).
- **Неизменяемость и confinement.** Records с final-компонентами неизменяемых типов, защитные копии (`List.copyOf`), привязка к одному потоку (один владелец, actor/mailbox) и передача сообщений через `BlockingQueue` убирают целые классы ошибок. Общее изменяемое состояние должно быть документированным исключением (`@GuardedBy` из аннотаций JCiP или Error Prone).
- **Статический анализ.** Error Prone (`GuardedBy`, `DoubleCheckedLocking`, `SynchronizeOnNonFinalField`, `ThreadPriorityCheck`, `FutureReturnValueIgnored`), детекторы SpotBugs MT_*, IS2_INCONSISTENT_SYNC, DC_DOUBLECHECK, JLM, инспекции IntelliJ «Threading issues». jcstress — для доказательства утверждений о порядке; `Thread.onSpinWait()` в циклах активного ожидания.
- **Прерывания.** Никогда не проглатывать `InterruptedException`: либо пробросить, либо восстановить флаг через `Thread.currentThread().interrupt()`. На этом держатся отмена в executor, `Future.cancel(true)`, структурная конкурентность и виртуальные потоки.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Troubleshoot locking and synchronization issues (25)](https://docs.oracle.com/en/java/javase/25/troubleshoot/troubleshoot-locking-and-synchronization-issues.html) | Oracle | Официальное руководство по диагностике дедлоков и конкуренции |
| [Diagnostic tools (25)](https://docs.oracle.com/en/java/javase/25/troubleshoot/diagnostic-tools.html) | Oracle | Обзор jcmd, JFR, jstack, NMT |
| [Error Prone bug patterns](https://errorprone.info/bugpatterns) | Google | Проверки конкурентности (GuardedBy, DCL и др.) |
| [SpotBugs bug descriptions](https://spotbugs.readthedocs.io/en/latest/bugDescriptions.html) | SpotBugs | Детекторы MT_CORRECTNESS |
| [Java Concurrency in Practice](https://jcip.net/) | B. Goetz и др., 2006 | Опасности живости, @GuardedBy, confinement |
| [The Java Specialists' Newsletter archive](https://www.javaspecialists.eu/archive/) | H. Kabutz | Десятилетия ловушек конкуренции (CHM, блокировки, виртуальные потоки) |
| [Mechanical Sympathy: False Sharing](https://mechanical-sympathy.blogspot.com/2011/07/false-sharing.html) | M. Thompson, 2011 | Классическая демонстрация false sharing `[не проверено]` |
| [JDK-8161372 (CHM computeIfAbsent locks on hit)](https://bugs.openjdk.org/browse/JDK-8161372) | OpenJDK | Историческая конкуренция в computeIfAbsent (исправлено в 9) `[не проверено]` (403 ботам) |

## Нативный код: FFM API, JNI и Unsafe

**Ключевые выводы**

- **FFM API — выбор по умолчанию для нового нативного взаимодействия** (финален в JDK 22, JEP 454; пакет `java.lang.foreign`). Составные части: `Arena` (время жизни), `MemorySegment` (память с проверкой границ и времени жизни, в куче или вне её), `MemoryLayout`/`ValueLayout`/`StructLayout` (и `VarHandle` для полей), `Linker` (downcall через `MethodHandle`, upcall через `upcallStub`), `SymbolLookup` (`defaultLookup()`, `libraryLookup(name, arena)`, `loaderLookup()`).

  ```java
  Linker linker = Linker.nativeLinker();
  MethodHandle strlen = linker.downcallHandle(                       // создавать ОДИН раз, хранить в static final
      linker.defaultLookup().find("strlen").orElseThrow(),
      FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
  try (Arena arena = Arena.ofConfined()) {
      long n = (long) strlen.invokeExact(arena.allocateFrom("Hello FFM"));   // 9
  }
  ```

  Проверено локально (Windows, JDK 25): `strlen` из `defaultLookup` (C runtime) даёт 9; `GetTickCount64` через `SymbolLookup.libraryLookup("kernel32", arena)` работает; `qsort` с Java-компаратором через `upcallStub` сортирует `[5,3,9,1,7]` в `[1,3,5,7,9]`.
- **Виды Arena.** `ofConfined()` (один поток-владелец, детерминированный `close()`), `ofShared()` (доступ из нескольких потоков; закрытие дороже: нужно рукопожатие со всеми потоками), `ofAuto()` (управляется GC, без `close`), `global()` (не освобождается никогда). Доступ после закрытия → `IllegalStateException: Already closed` (проверено локально). Выход за границы → `IndexOutOfBoundsException` вместо падения JVM.
- **Правила производительности.** Кэшировать `MethodHandle` и `VarHandle` в `static final` (создание стоит сотни наносекунд и больше; `invokeExact` с точными типами позволяет JIT встроить вызов). `Linker.Option.critical(allowHeapAccess)` — только для функций с «extremely short running time … [that do] not call back into Java»: при неверном использовании JVM может упасть. `Linker.Option.captureCallState("errno")` (или `"GetLastError"` на Windows) — безопасное чтение errno. `firstVariadicArg(i)` обязателен для вариативных функций вроде `printf`. Блоги ставят downcall FFM вровень с JNI или чуть быстрее и намного быстрее JNA; проверять собственным прогоном JMH `[не проверено]`.
- **jextract** генерирует Java-привязки из C-заголовков: `jextract --include-dir include --output src --target-package org.example.mylib --library mylib include/mylib.h`; фильтрация через `--dump-includes includes.txt` и затем `@includes_filtered.txt` или `--include-function name`. Распространяется отдельными EA-сборками (последняя `25-jextract+2-4`, 2025-11-25) на jdk.java.net/jextract, в JDK не входит.
- **Целостность по умолчанию**
  - JDK 24 (JEP 472): `System.load/loadLibrary`, первая привязка `native`-методов и *ограниченные методы* FFM (`downcallHandle`, `libraryLookup`, `MemorySegment.reinterpret` и др.) печатают предупреждение, если нативный доступ не разрешён. Разрешение: `--enable-native-access=ALL-UNNAMED` (classpath) или `=module.a,module.b`; в манифесте исполняемого JAR `Enable-Native-Access: ALL-UNNAMED` (единственное поддерживаемое значение). `--illegal-native-access=allow|warn|deny`, по умолчанию `warn`, в будущем `deny`. `jnativescan` (JDK 22+) находит использование JNI/FFM в JAR; JFR `jdk.NativeLibraryLoad/Unload`.
  - Проверено локально, JDK 25: FFM без флага печатает `WARNING: A restricted method in java.lang.foreign.Linker has been called … Use --enable-native-access=ALL-UNNAMED to avoid a warning for callers in this module … Restricted methods will be blocked in a future release unless native access is enabled`. `System.loadLibrary("kernel32")` печатает такое же предупреждение для `java.lang.System::loadLibrary`. JDK 21 ничего не печатает.
- **JNI** остаётся для унаследованного кода и отдельных граничных случаев, но требует C-обвязки, сборки под каждую платформу, ручного управления ссылками (`NewGlobalRef`/`DeleteLocalRef`) и роняет JVM при ошибках. Блокировка внутри JNI-кода, вызывающего Java, закрепляет виртуальные потоки (JEP 491).
- **JNA (5.19.1) и jnr-ffi (2.3.7).** Удобные динамические привязки через интерфейсы, компилятор C не нужен; самые медленные на вызов (рефлексия, libffi): подходят для редких вызовов ОС, не для горячих циклов. Многие библиотеки (например, OSHI и платформенные инструменты на JNA) всё ещё зависят от JNA; горячие пути переносить на FFM при JDK 22+.
- **Доступ к памяти через `sun.misc.Unsafe` уходит.** Deprecated for removal в JDK 23 (JEP 471); JDK 24+ печатает однократное предупреждение при первом использовании (JEP 498, `--sun-misc-unsafe-memory-access=allow|warn|debug|deny`, по умолчанию `warn`); план: `deny` по умолчанию «in JDK 26 or later», затем удаление. **Подтвердить по спискам JEP и заметкам о релизах, переключили ли 26 или 27 значение по умолчанию на `deny`, не удалось**: проверять `java -XX:+PrintFlagsFinal` и заметки о релизе своего JDK.
  - Проверено локально, JDK 25: `Unsafe.allocateMemory` → `WARNING: A terminally deprecated method in sun.misc.Unsafe has been called … sun.misc.Unsafe::allocateMemory will be removed in a future release`; javac выдаёт предупреждения `[removal]`. JDK 21 молчит.
  - Карта миграции: CAS и упорядоченный доступ к полям и массивам → `VarHandle` (`compareAndSet`, `getAcquire`/`setRelease`, `getAndAdd`); память вне кучи (`allocateMemory`/`putLong(address, …)`) → `Arena` и `MemorySegment` (и layouts/`VarHandle`); трюки с `arrayBaseOffset` → `MemorySegment.ofArray`; хаки с `objectFieldOffset` → `MethodHandles.Lookup.findVarHandle`. Netty, Kryo и библиотеки в стиле Hadoop мигрируют: обновлять их, а не подавлять предупреждение.
  - JEP 500 в JDK 26 аналогично предупреждает о рефлексивной мутации `final`-полей (см. [Модель памяти Java](#модель-памяти-java)).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [JEP 454: Foreign Function & Memory API](https://openjdk.org/jeps/454) | M. Cimadamore, 2023 | Обоснование финального API и примеры |
| [JEP 442 (3rd preview, JDK 21)](https://openjdk.org/jeps/442) | M. Cimadamore | Превью API эпохи 21 (отличается!) `[устарело]`, замена — JEP 454 |
| [java.lang.foreign package (25)](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/foreign/package-summary.html) | Oracle | Обзор API, включая ограниченные методы |
| [Arena Javadoc](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/foreign/Arena.html) · [Linker Javadoc](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/foreign/Linker.html) | Oracle | Времена жизни; отображение типов C по платформам |
| [Linker.Option Javadoc](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/foreign/Linker.Option.html) | Oracle | critical, captureCallState, firstVariadicArg |
| [Foreign Function and Memory API guide (25)](https://docs.oracle.com/en/java/javase/25/core/foreign-function-and-memory-api.html) | Oracle | Пошаговое руководство |
| [dev.java: FFM](https://dev.java/learn/ffm/) | Oracle DevRel | Серия учебных материалов |
| [Project Panama](https://openjdk.org/projects/panama/) · [Panama FFI doc](https://github.com/openjdk/panama-foreign/blob/foreign-memaccess%2Babi/doc/panama_ffi.md) | OpenJDK | Документы по дизайну |
| [jextract (GitHub)](https://github.com/openjdk/jextract) · [Guide](https://github.com/openjdk/jextract/blob/master/doc/GUIDE.md) · [EA builds](https://jdk.java.net/jextract/) | OpenJDK | Генератор привязок, использование, загрузка |
| [JEP 472: Prepare to Restrict the Use of JNI](https://openjdk.org/jeps/472) | R. Pressler, 2024 | Предупреждения, флаги, атрибут манифеста |
| [jnativescan manual](https://docs.oracle.com/en/java/javase/25/docs/specs/man/jnativescan.html) | Oracle | Поиск нативного доступа в зависимостях |
| [JEP 471: Deprecate Unsafe memory-access methods](https://openjdk.org/jeps/471) | R. Pressler, A. Buckley, 2024 | Поэтапный план, таблица миграции |
| [JEP 498: Warn upon use of Unsafe memory-access methods](https://openjdk.org/jeps/498) | R. Pressler, A. Buckley, 2024 | Режимы `--sun-misc-unsafe-memory-access` |
| [Integrity by Default (Inside.java)](https://inside.java/2025/01/03/evolving-default-integrity/) | Inside.java, 2025 | Зачем нужны ограничения JNI, Unsafe и final |
| [Java integrity by default (JVM Advent)](https://www.javaadvent.com/2024/12/java-integrity-by-default.html) | JVM Advent, 2024 | Та же тема глазами практика |
| [Unsafe memory-access migration guide (noregressions.dev)](https://noregressions.dev/guides/java/cards/unsafe-memory-access) | noregressions.dev, 2026 | Карточка миграции JDK 8→26; утверждает, что в 25/26 по умолчанию остаётся `warn` (для 26 `[не проверено]`) |
| [OpenTelemetry Java issue #7219 (Unsafe)](https://github.com/open-telemetry/opentelemetry-java/issues/7219) | OTel | Реальное обсуждение миграции библиотеки |
| [JNA](https://github.com/java-native-access/jna) | JNA project | Привязки через интерфейсы |
| [jnr-ffi](https://github.com/jnr/jnr-ffi) | JNR project | Альтернативный FFI (наследие JRuby) |
| [Foreign Function & Memory API (HappyCoders)](https://www.happycoders.eu/java/foreign-function-memory-api/) | S. Woltmann | Учебник со структурами и upcall |
| [Project Panama (Baeldung)](https://www.baeldung.com/java-project-panama) | Baeldung | Короткое введение |

## Vector API, GraalVM Native Image и Polyglot

**Ключевые выводы**

- **Vector API в JDK 27 остаётся инкубатором** (JEP 537, 12-й инкубатор, без изменений API относительно 26; перейдёт в превью только когда value classes из Valhalla станут доступны в превью; JEP 401 Value Objects (Preview) нацелен на JDK 28). Нужен `--add-modules jdk.incubator.vector` при компиляции и запуске, печатается `WARNING: Using incubator modules: jdk.incubator.vector` (проверено локально). Без флага: «package jdk.incubator.vector is not visible» (проверено локально).

  ```java
  static final VectorSpecies<Float> S = FloatVector.SPECIES_PREFERRED;   // 256 бит (8 float) на тестовой машине с AVX2
  static float dot(float[] a, float[] b) {
      var acc = FloatVector.zero(S); int i = 0;
      for (int ub = S.loopBound(a.length); i < ub; i += S.length())
          acc = FloatVector.fromArray(S, a, i).fma(FloatVector.fromArray(S, b, i), acc);
      float r = acc.reduceLanes(VectorOperators.ADD);
      for (; i < a.length; i++) r += a[i] * b[i];          // скалярный хвост
      return r;
  }
  ```

  **Когда окупается:** горячие циклы по примитивным массивам и `MemorySegment`, где автовекторизация C2 не срабатывает (редукции с FMA, маскированные хвосты, перестановки, сравнение и выбор по полосам, побайтовый разбор как в 1BRC), на x64 (AVX2/AVX-512) или AArch64 (NEON/SVE). **Когда нет:** код, который C2 уже векторизует (простые циклы `a[i] = b[i] + c[i]`), малые массивы, негорячий код, библиотеки, которым нельзя зависеть от инкубаторных модулей. Бенчмарк через JMH и просмотр кода через `-XX:+PrintAssembly`/perfasm ([performance.md](performance.md)).
- **GraalVM Native Image** компилирует замкнутый мир в нативный исполняемый файл: быстрый старт и малый RSS (CLI, serverless, scale-to-zero). Цена: рефлексия, JNI и ресурсы требуют метаданных достижимости (`reachability-metadata.json`, агент трассировки), ловушки инициализации классов на этапе сборки, пиковая пропускная способность обычно ниже, чем у C2 без PGO. Виртуальные потоки и downcall FFM в свежих релизах поддерживаются; сверяться с документацией своей версии. Собственные AOT-работы OpenJDK (Leyden: кэши AOT в 24–26, JEP 544 AOT code compilation нацелен на 28) — малорисковая альтернатива для ускорения старта (см. [performance.md](performance.md) и [toolchain.md](toolchain.md)).
- **GraalVM Polyglot (Truffle):** встраивание JavaScript, Python (GraalPy), Ruby, WebAssembly в JVM-приложение через `org.graalvm.polyglot.Context`; доступны песочница и лимиты ресурсов. JDK 27 удалил модули JVMCI/Graal из сборок OpenJDK (`jdk.internal.vm.ci`, `jdk.graal.compiler`): использовать дистрибутивы GraalVM (заметки о релизе JDK 27).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [JEP 537: Vector API (12th Incubator)](https://openjdk.org/jeps/537) | X. Shen (владелец), 2026 | Статус в JDK 27, зависимость от Valhalla |
| [JEP 529 (11th, JDK 26)](https://openjdk.org/jeps/529) · [JEP 508 (10th, JDK 25)](https://openjdk.org/jeps/508) · [JEP 338 (1st, JDK 16)](https://openjdk.org/jeps/338) | OpenJDK | История инкубации `[устарело]`, замена — JEP 537 |
| [jdk.incubator.vector Javadoc (25)](https://docs.oracle.com/en/java/javase/25/docs/api/jdk.incubator.vector/jdk/incubator/vector/package-summary.html) | Oracle | Справочник API с идиомами циклов |
| [panama-vector repo](https://github.com/openjdk/panama-vector) | OpenJDK | Ветка разработки, бенчмарки |
| [JEP 401: Value Objects (Preview)](https://openjdk.org/jeps/401) | D. Smith (владелец) | Часть Valhalla, которую ждёт Vector API |
| [One Billion Row Challenge](https://github.com/gunnarmorling/1brc) | G. Morling, 2024 | Реальные приёмы FFM/Unsafe/SWAR/векторов и их эффект |
| [GraalVM Native Image reference](https://www.graalvm.org/latest/reference-manual/native-image/) | Oracle GraalVM | Сборка, метаданные, ограничения |
| [Native Image: Foreign Function & Memory API](https://www.graalvm.org/latest/reference-manual/native-image/native-code-interoperability/foreign-interface/) | Oracle GraalVM | Поддержка FFM в native image |
| [GraalVM Polyglot programming](https://www.graalvm.org/latest/reference-manual/polyglot-programming/) | Oracle GraalVM | Встраивание других языков |

## Антипаттерны

| Симптом или запах кода | Вероятная причина | Исправление |
|---|---|---|
| Приложение зависает под нагрузкой на JDK 21–23 с виртуальными потоками | Закрепление: блокировка внутри `synchronized`/`Object.wait` | Перейти на JDK 24+ (JEP 491) или заменить на `ReentrantLock`; искать через JFR `jdk.VirtualThreadPinned` |
| На JDK 25 события закрепления всё ещё есть | Блокировка в `<clinit>`, при загрузке классов или в нативном кадре (JNI/upcall FFM) | Вынести блокирующую работу из статических инициализаторов; не блокироваться в upcall |
| `IllegalStateException: Recursive update` | Рекурсивный или изменяющий карту `ConcurrentHashMap.computeIfAbsent` | Вычислять вне лямбды или использовать неконкурентную карту для рекурсии |
| Всплески задержки при работе параллельных стримов и CF | Common pool забит блокирующими задачами | Отдельный executor для I/O; виртуальные потоки |
| В контейнере на 2 CPU JDK 21 порождает много потоков `Thread-N` | Резервный `ThreadPerTaskExecutor` в `CompletableFuture` (parallelism common pool равен 1) | Передавать явный executor; JDK 25 использует common pool (проверено локально) |
| OOM при множестве задач в очереди | У `newFixedThreadPool` неограниченная очередь | Ограниченная очередь и политика отказа или backpressure |
| Память растёт с виртуальными потоками | Кэши в `ThreadLocal` на каждый виртуальный поток; неограниченное создание задач | `ScopedValue`, общие потокобезопасные объекты, `Semaphore` |
| Потеряны исключения в фоновых задачах | Future от `submit()` никто не читает | `execute()` и UncaughtExceptionHandler, либо логирование в `whenComplete` |
| Дамп потоков не показывает запросы на виртуальных потоках | `jstack` перечисляет только платформенные потоки | `jcmd <pid> Thread.dump_to_file -format=json f.json` |
| `WARNING: A restricted method … has been called` | JNI/FFM без разрешённого нативного доступа | `--enable-native-access=ALL-UNNAMED`, список модулей или манифест |
| `WARNING: A terminally deprecated method in sun.misc.Unsafe` | Зависимость использует доступ к памяти через Unsafe | Обновить библиотеку; мигрировать на `VarHandle`/FFM; временно `--sun-misc-unsafe-memory-access=allow` |
| Код `StructuredTaskScope` из блога не компилируется | API менялось в 25, 26, 27 | Сверить с JEP для своей версии JDK (505/525/533/543) |
| Реактивное приложение медленное при простаивающем CPU | Блокирующий вызов на event loop | BlockHound в тестах; `subscribeOn(Schedulers.boundedElastic())` |

## Чеклист ревью

**Общее состояние и JMM**

- [ ] 1. Каждое изменяемое поле, к которому обращаются несколько потоков, имеет документированную защиту (`@GuardedBy("lock")`, `volatile`, атомик или confinement); нет «наверное, нормально» обычных полей.
- [ ] 2. Составные действия (`check-then-act`, `read-modify-write`, `count++`) атомарны: блокировка, `Atomic*`/`LongAdder` или CHM `compute`/`merge`/`putIfAbsent`; никогда `containsKey` и `put`.
- [ ] 3. Объекты публикуются безопасно (final-поля, `volatile`, конкурентная коллекция, передача через executor); `this` не утекает из конструктора (нет регистрации слушателей и запуска потоков в конструкторах).
- [ ] 4. Типы значений и DTO неизменяемы (records, final-поля, `List.copyOf`); геттеры не отдают внутренние изменяемые коллекции.
- [ ] 5. DCL использует `volatile`-поле и локальную переменную; статика — holder idiom (или немедленная инициализация).
- [ ] 6. Слабые режимы `VarHandle` (`Opaque`, `Acquire/Release`, plain) встречаются только с комментарием, объясняющим аргумент о порядке, и тестом jcstress.
- [ ] 7. Нет рефлексивной мутации `final`-полей (предупреждения JEP 500 на 26+); нет нового использования `sun.misc.Unsafe` (предупреждает с 24, запланированы `deny` и удаление).

**Блокировки и блокирующие вызовы**

- [ ] 8. После `Lock.lock()` сразу идёт `try { … } finally { unlock(); }`; так же для `Semaphore.acquire()` с `release()`.
- [ ] 9. Под блокировкой нет блокирующего I/O, удалённых вызовов, `Future.get()` и колбэков в неизвестный код.
- [ ] 10. Несколько блокировок всегда берутся в едином глобальном порядке или через `tryLock(timeout)` с backoff.
- [ ] 11. `synchronized` берётся на приватном final-объекте-замке, никогда на `this` публичного класса, интернированных `String`, упакованных примитивах или value-based классах (`Integer`, `Optional`, `LocalDate`; будущие value classes Valhalla).
- [ ] 12. `wait()`/`await()` стоят в цикле `while (!condition)`; используется `notifyAll()`/`signalAll()`, если не доказан единственный тип ожидающих.
- [ ] 13. `InterruptedException` не проглатывается: пробрасывается или восстанавливается через `Thread.currentThread().interrupt()`.
- [ ] 14. Оптимистичные чтения `StampedLock` копируют поля в локальные и вызывают `validate()` перед использованием; реентерабельно не применяется.

**Исполнители и асинхронность**

- [ ] 15. В серверных путях нет `Executors.newCachedThreadPool()`/`newFixedThreadPool()` без анализа ограниченности очереди; собственный `ThreadPoolExecutor` имеет ограниченную очередь, именованные потоки и явную политику отказа.
- [ ] 16. Executor останавливаются (`try`-with-resources на 19+ или `shutdown` и `awaitTermination` при остановке приложения).
- [ ] 17. Future от `submit()` потребляются, иначе сбои логируются другим способом (`afterExecute`, `whenComplete`).
- [ ] 18. Вызовы `CompletableFuture.*Async` с I/O передают явный executor; ничего блокирующего не выполняется в common pool и потоках event loop.
- [ ] 19. Обработчики ошибок снимают обёртки `CompletionException`/`ExecutionException`; на каждом удалённом ожидании есть таймаут (`orTimeout`, `get(timeout)`); учтено, что отмена не прерывает задачи CF.
- [ ] 20. Параллельные стримы — только для больших CPU-задач без побочных эффектов и с обоснованием бенчмарком.

**Виртуальные потоки и структурная конкурентность**

- [ ] 21. Виртуальные потоки не пулируются; лимиты параллелизма задаются `Semaphore`/bulkhead по размеру нижестоящего ресурса.
- [ ] 22. На JDK 21–23 нет долгих блокировок внутри `synchronized` на горячих путях (или переход на 24+); нативные вызовы, которые блокируются, и блокировки в статических инициализаторах проверены на закрепление на любом JDK.
- [ ] 23. В коде на виртуальных потоках нет кэшей и пулов в `ThreadLocal`; контекст передаётся через `ScopedValue` (25+) или явные параметры; у каждого `ThreadLocal.set` в пулах потоков есть `remove()` в `finally`.
- [ ] 24. CPU-тяжёлая работа не запускается массово на виртуальных потоках (ограниченный пул платформенных потоков или FJP).
- [ ] 25. Fan-out с дедлайнами использует `StructuredTaskScope` (финальный API в 28, превью на 25–27: сверить точную версию API) или CF с явной отменой, чтобы после сбоя или таймаута не оставалось подзадач-сирот.
- [ ] 26. Проверена daemon-природа: приложения только с виртуальными потоками держат JVM живой (`spring.main.keep-alive=true` или не-daemon главный цикл).

**Коллекции и кэши**

- [ ] 27. Лямбды `ConcurrentHashMap.compute*` короткие, без побочных эффектов и не трогают ту же карту (рекурсивное обновление → `IllegalStateException`).
- [ ] 28. Итерация по `Collections.synchronizedX` обёрнута в `synchronized(list)`; `CopyOnWriteArrayList` только для данных с преобладанием чтения.
- [ ] 29. У кэшей есть границы и срок жизни (Caffeine), а не вечно растущий CHM.
- [ ] 30. Очереди между стадиями ограничены; производители обрабатывают отказ `offer` (backpressure или сброс нагрузки).

**Нативный и низкоуровневый код**

- [ ] 31. `MethodHandle` и `VarHandle` FFM лежат в `static final`; arena закрываются (try-with-resources), сегменты не выходят за пределы своей arena; `ofShared` только при настоящей необходимости.
- [ ] 32. `Linker.Option.critical` только для тривиально коротких неблокирующих функций без upcall; `captureCallState` для errno/GetLastError.
- [ ] 33. Нативный доступ разрешён явно (`--enable-native-access=...` или манифест `Enable-Native-Access: ALL-UNNAMED`) и задокументирован; `jnativescan` запущен на зависимостях перед обновлением JDK.

**Проверка**

- [ ] 34. У классов, чувствительных к конкурентности, есть стресс-тесты (jcstress для порядка, многопоточные JUnit-тесты с latch и barrier, повторные прогоны), а не тесты на `Thread.sleep`.
- [ ] 35. Утверждения о производительности (lock-free против блокировок, LongAdder против AtomicLong, Vector API) подтверждены JMH с `@Threads` > 1 на архитектуре CPU, где будет работать код (x86 скрывает ошибки порядка; при развёртывании на ARM тестировать на ARM).

## Локальные замеры

Среда: Windows 11, x64, 32 логических CPU; Temurin 25.0.4.1 (часть перепроверена на 25.0.5+8) и Temurin 21.0.12; запуск одним файлом `java X.java`. Абсолютное время дано отношениями; пометка «проверено локально (JDK 25, Windows 11)».

| Эксперимент | JDK 21.0.12 | JDK 25.0.x |
|---|---|---|
| 16 виртуальных потоков, каждый `synchronized(ownLock){ sleep(200) }`, `-Djdk.virtualThreadScheduler.parallelism=2 -Djdk.virtualThreadScheduler.maxPoolSize=2` | Закрепление; время примерно в 7,6 раза больше, чем на 25 | Не закрепляется; базовое время |
| `-Djdk.tracePinnedThreads=short` | Печатает стек `reason:MONITOR` | Вывода нет (опция удалена) |
| JFR `jdk.VirtualThreadPinned` для: (1) sleep в `synchronized`, (2) sleep в `<clinit>`, (3) `wait()` в `synchronized` | События для (1) и (2); (2) заметно длиннее (около 10 раз длиннее (1)) | Только (2): `pinnedReason = "VM call to PinJfr$Slow.<clinit> on stack"`, `blockingOperation = "LockSupport.park"` |
| `StructuredTaskScope` без `--enable-preview` | неприменимо | Ошибка компиляции «preview API and is disabled by default» |
| `StructuredTaskScope` с `--enable-preview --source 25` | неприменимо | Интерфейс; три перегрузки `open()`; Joiner: `allSuccessfulOrThrow, allUntil, anySuccessfulResultOrThrow, awaitAll, awaitAllSuccessfulOrThrow`; сбой → `FailedException`, таймаут → `TimeoutException` |
| `ScopedValue.where(...).run(...)` без preview | Ошибка компиляции (в 21 превью) | Работает; вложенная перепривязка; `isBound()=false` снаружи |
| FFM `strlen` (default lookup), `GetTickCount64` (kernel32), закрытие arena | неприменимо (в 21 превью API) | strlen=9; доступ после закрытия → `IllegalStateException: Already closed`; WARNING об ограниченном методе без `--enable-native-access` |
| Upcall FFM: `qsort` из C с Java-компаратором | неприменимо | `[5,3,9,1,7]` → `[1,3,5,7,9]` |
| `System.loadLibrary("kernel32")` | Тихо | WARNING JEP 472 для `System::loadLibrary` |
| `Unsafe.allocateMemory` | Тихо | Предупреждения javac `[removal]` и WARNING времени выполнения «terminally deprecated method in sun.misc.Unsafe» |
| Рекурсивный `ConcurrentHashMap.computeIfAbsent` (мемоизированный fib) | — | `IllegalStateException: Recursive update` |
| `ForkJoinPool.getCommonPoolParallelism()` | — | 31 (CPU − 1) |
| `ScheduledExecutorService.class.isAssignableFrom(ForkJoinPool.class)` | false | true |
| `CompletableFuture.defaultExecutor()` с `-Djava.util.concurrent.ForkJoinPool.common.parallelism=1` | `CompletableFuture$ThreadPerTaskExecutor` (задача выполнилась в `Thread-0`) | `ForkJoinPool` (воркер common pool) |
| Обёртка исключений CF | — | `join()` → `CompletionException`, `get()` → `ExecutionException`, `exceptionally`/`handle` видят `CompletionException` |
| `jcmd <pid> Thread.dump_to_file -format=json` при 1000 спящих виртуальных потоков и 2 потоках в дедлоке | — | JSON размером около 730 КБ, 1043 записи потоков; `Thread.print` сообщает «Found one Java-level deadlock» |
| Скалярное произведение через Vector API | — | Нужен `--add-modules jdk.incubator.vector` (WARNING: Using incubator modules); `SPECIES_PREFERRED` = 256 бит, float×8 на этом процессоре с AVX2 |

## Дополнительные материалы и открытые вопросы

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Java Memory Model Unlearning Experience (slides)](https://shipilev.net/talks/geecon-May2018-jmm.pdf) | A. Shipilev, 2018 | Заблуждения о том, чего JMM не обещает |
| [Aleksey Shipilёv one-stop page](https://shipilev.net/) | A. Shipilev | Доклады, статьи о JMM и бенчмаркинге |
| [Java 21 new feature: Virtual Threads #RoadTo21 (video)](https://www.youtube.com/watch?v=5E0LU85EnTI) | Java channel, 2023 | Наглядное введение от команды Java |
| [The Challenges of Introducing Virtual Threads to the Java Platform (JVMLS video)](https://www.youtube.com/watch?v=WsCJYQDPrrE) | Java channel (JVMLS) | Компромиссы реализации (закрепление, планировщик) |
| [Inside Java Newscast #80: Java 24 stops pinning virtual threads (almost)](https://nipafx.dev/inside-java-newscast-80/) | N. Parlog, 2024 | Объяснение JEP 491 |
| [Inside Java Newscast #91: Structured Concurrency revamp in Java 25](https://nipafx.dev/inside-java-newscast-91/) | N. Parlog, 2025 | Переработка `open()` и `Joiner` |
| [InfoQ: JEP 505 structured concurrency 5th preview](https://www.infoq.com/news/2025/05/jep-505-concurrency-preview-5) | InfoQ, 2025 | Совет «создавать новый Joiner на каждый scope» `[не проверено]` |
| [Java 25: virtual threads with structured task scopes and scoped values](https://javapro.io/2025/12/23/java-25-getting-the-most-out-of-virtual-threads-with-structured-task-scopes-and-scoped-values/) | JAVAPRO, 2025 | Сводное руководство для 25 LTS |
| [Inside.java — Loom tag](https://inside.java/tag/loom) · [Panama tag](https://inside.java/tag/panama) · [Podcast](https://inside.java/podcast/) | Oracle Java team | Новости из первых рук |
| [Project Loom comparison (benchmarks)](https://github.com/ebarlas/project-loom-comparison) | E. Barlas | Платформенные потоки, виртуальные потоки и async HTTP-серверы |
| [java-native-benchmark](https://github.com/zakgof/java-native-benchmark) | zakgof | Сравнения JNI, JNA, JNR и FFM на JMH |
| [Welcome to Panama](https://softwaremill.com/welcome-to-panama/) | SoftwareMill | FFM против JNI с цифрами |
| [One Billion Row Challenge write-up](https://www.morling.dev/blog/one-billion-row-challenge/) | G. Morling, 2024 | Итоги победивших низкоуровневых приёмов |
| [async-profiler](https://github.com/async-profiler/async-profiler) | A. Pangin и др. | Профилирование блокировок (`-e lock`) и wall-clock для конкуренции |
| [ThreadMXBean Javadoc](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/management/ThreadMXBean.html) | Oracle | `findDeadlockedThreads()` для health-проверок |
| [ReentrantLock Javadoc](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/locks/ReentrantLock.html) · [atomic package](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/atomic/package-summary.html) · [BlockingQueue](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/BlockingQueue.html) · [CountDownLatch](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/CountDownLatch.html) | Oracle | Справка по базовым блокам |
| [jcstress sample: BasicJMM_06_Causality](https://github.com/openjdk/jcstress/blob/master/jcstress-samples/src/main/java/org/openjdk/jcstress/samples/jmm/basic/BasicJMM_06_Causality.java) · [BasicJMM_08_Finals](https://github.com/openjdk/jcstress/blob/master/jcstress-samples/src/main/java/org/openjdk/jcstress/samples/jmm/basic/BasicJMM_08_Finals.java) | A. Shipilev | Исполняемые уроки о причинности и final-полях |
| [ForkJoinPool.java (исходники)](https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/java/util/concurrent/ForkJoinPool.java) | D. Lea | Очень подробный обзорный комментарий о реализации |
| [Micrometer context-propagation](https://github.com/micrometer-metrics/context-propagation) · [docs](https://docs.micrometer.io/context-propagation/reference/) | Micrometer | Мост между ThreadLocal и Reactor Context |
| [Spring Framework: Task execution and scheduling](https://docs.spring.io/spring-framework/reference/integration/scheduling.html) | Spring | Абстракции `TaskExecutor`, `@Async` |
| [JavaSpecialists concurrency course](https://www.javaspecialists.eu/courses/concurrency/) | H. Kabutz | Платный курс; глубокое покрытие j.u.c |
| [JEP 483: AOT Class Loading & Linking](https://openjdk.org/jeps/483) · [JEP 544: AOT Code Compilation (JDK 28)](https://openjdk.org/jeps/544) | OpenJDK | Альтернативы native image для старта |
| [JDK 28 EA builds](https://jdk.java.net/28/) · [JDK 28 project page](https://openjdk.org/projects/jdk/28/) | OpenJDK | Ранняя проверка финальной структурной конкурентности |

Книги по конкурентности (JCiP, «Modern Concurrency in Java» A N M Bazlur Rahman, O'Reilly 2025 `[не проверено]`, книги Goetz и Lea) — в [books.md](books.md).

**Открытые вопросы и непроверенное**

- Переключили ли JDK 26 или 27 значение `--sun-misc-unsafe-memory-access` по умолчанию на `deny` (план: «JDK 26 or later»; в списках JEP 26/27/28 такого JEP нет; одно руководство 2026 года утверждает, что в 26 всё ещё `warn`). Перед советом проверить на реальном JDK.
- Статус Vector API и lazy constants в JDK 28: в списке JEP JDK 28 на 2026-11-06 их нет (RDP1 — 2026-12-03, так что их могут добавить).
- Точные имена параметров виртуальных потоков для Tomcat и Jetty не перечитывались (страницы отвечали 200); сверяться с документацией своей версии сервера.
- Пост Netflix о закреплении и некоторые страницы багов OpenJDK недоступны автоматической проверке (HTTP 403); детали взяты из вторичных источников.
