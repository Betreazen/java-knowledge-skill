# Идиомы, паттерны и антипаттерны Java

Справочник по идиоматичной современной Java (17 → 27, с взглядом на 28): возможности языка с номерами JEP и статусом, базовые идиомы (неизменяемость, null, равенство, исключения, потоки, generics, enum, java.time, деньги, логирование, ресурсы), data-oriented programming, GoF-паттерны в современной Java, идиомы безопасности, таблица антипаттернов с правилами статических анализаторов, спорные вопросы и чеклист code review. Актуально на ноябрь 2026: JDK 25 — последняя LTS (GA 2025-09-16), JDK 27 — последний GA-релиз (2026-09-15), JDK 28 в разработке (GA запланирован на 2027-03-09, Rampdown Phase One 2026-12-03). Основные инструменты темы: Error Prone, NullAway, SpotBugs с Find Security Bugs, PMD 7, Checkstyle, google-java-format, SLF4J 2. Пометки: `[не проверено]` — факт из одного вторичного источника или страница не загрузилась; `[устарело]` — рядом названа замена; «проверено локально» означает Temurin JDK 25 на Windows 11 (для контраста JDK 21); результаты приводятся как наблюдаемое поведение, не как время. Все JEP-ссылки ниже открывались; без пометки значит, что страница прочитана или отвечает 200. Соседние темы: настройка сборки, анализаторов и форматтеров — [toolchain.md](toolchain.md); тесты — [testing.md](testing.md); производительность и настройка JVM — [performance.md](performance.md); конкурентность, виртуальные потоки и нативный код — [concurrency-native.md](concurrency-native.md); структура проекта и DI-фреймворки — [architecture.md](architecture.md); серверные фреймворки — [domains-server.md](domains-server.md); список книг — [books.md](books.md); процесс разработки с ревью — [pipeline.md](pipeline.md).

- [Версии языка: Java 17–28](#версии-языка-java-1728)
- [Каноничные источники и стайлгайды](#каноничные-источники-и-стайлгайды)
- [Неизменяемость, значения, равенство](#неизменяемость-значения-равенство)
- [Null и Optional](#null-и-optional)
- [Исключения и ресурсы](#исключения-и-ресурсы)
- [Повседневные идиомы библиотеки](#повседневные-идиомы-библиотеки)
- [Data-oriented programming и ADT](#data-oriented-programming-и-adt)
- [Мини-каталог паттернов](#мини-каталог-паттернов)
- [Идиомы безопасности](#идиомы-безопасности)
- [Антипаттерны](#антипаттерны)
- [Спорные вопросы](#спорные-вопросы)
- [Чеклист code review: идиоматичная Java](#чеклист-code-review-идиоматичная-java)
- [Что перепроверить](#что-перепроверить)

---

## Версии языка: Java 17–28

**Ключевые выводы**

- Для продакшена берите LTS (21 или 25). Без `--enable-preview` доступны только final-возможности. Preview-возможности (Lazy Constants, примитивные паттерны, value classes) годятся для экспериментов и не годятся для библиотек. Код, скомпилированный с `--enable-preview`, запускается только на том же feature-релизе.
- Источник таблицы: страницы релизов `https://openjdk.org/projects/jdk/<N>/` для 17, 21–28. Статусы: final (стандарт), preview, incubator.

| JDK | GA | JEP, важные для идиом (статус) | Изменения платформы, влияющие на код |
|---|---|---|---|
| 17 (LTS) | 2021-09-14 | 409 Sealed Classes (final); 406 Pattern Matching for switch (preview); 356 Enhanced PRNGs (final); 306 всегда строгий FP (`strictfp` избыточен) | 403 сильная инкапсуляция внутренностей JDK; 411 Security Manager deprecated for removal; 415 контекстные фильтры десериализации; 398 Applet API deprecated |
| 18 | 2022-03 | 413 сниппеты в Javadoc `{@snippet}` (final); 400 UTF-8 по умолчанию (final) | 421 финализация deprecated for removal |
| 21 (LTS) | 2023-09-19 | 431 Sequenced Collections; 440 Record Patterns; 441 Pattern Matching for switch; 444 Virtual Threads (все final); 430 String Templates, 443 Unnamed Patterns & Variables, 445 Unnamed Classes & Instance Main, 446 Scoped Values, 453 Structured Concurrency (все preview) | 451 подготовка к запрету динамической загрузки агентов; 449 deprecate Win32 x86 |
| 22 | 2024-03-19 | 456 Unnamed Variables & Patterns `_` (final); 454 FFM API (final); 458 запуск нескольких исходников (final); 447 операторы перед `super(...)`, 461 Stream Gatherers, 459 String Templates (2-й), 463 implicit classes (2-й), 464 Scoped Values (2-й), 462 SC (2-й), 457 Class-File API (все preview) | — |
| 23 | 2024-09-17 | 467 Markdown Documentation Comments `///` (final); preview: 455 примитивы в паттернах, 476 Module Import Declarations, 473 Gatherers (2-й), 477 implicit classes (3-й), 482 Flexible Constructor Bodies (2-й), 481 Scoped Values (3-й), 480 SC (3-й). **String Templates удалены** (JEP 465 withdrawn) | 471 методы `sun.misc.Unsafe` для доступа к памяти deprecated for removal; 474 generational ZGC по умолчанию |
| 24 | 2025-03-18 | 485 Stream Gatherers (final); 484 Class-File API (final); preview: 488 примитивные паттерны (2-й), 492 flexible ctor (3-й), 494 module imports (2-й), 495 Simple Source Files (4-й), 487 Scoped Values (4-й), 499 SC (4-й), 478 KDF API | **486 Security Manager отключён навсегда**; 472 подготовка к ограничению JNI (предупреждения); 498 предупреждение при доступе к памяти через `sun.misc.Unsafe`; 491 виртуальные потоки не закрепляются на `synchronized`; 479 удалён Win32 x86; 501 deprecate 32-bit x86 |
| 25 (LTS) | 2025-09-16 | **final: 511 Module Import Declarations; 512 Compact Source Files & Instance Main; 513 Flexible Constructor Bodies; 506 Scoped Values; 510 KDF API**; preview: 502 Stable Values, 507 примитивные паттерны (3-й), 505 SC (5-й), 470 PEM | 503 удалён 32-bit x86; 519 compact object headers (product); 514/515 AOT-эргономика |
| 26 | 2026-03-17 | preview: 526 Lazy Constants (2-й, переименован из StableValue), 530 примитивные паттерны (4-й), 525 SC (6-й), 524 PEM (2-й); final: 517 HTTP/3 для HttpClient | **500 Prepare to Make Final Mean Final** (предупреждение при мутации final-полей через reflection); 504 удалён Applet API |
| 27 | 2026-09-15 | preview: 531 Lazy Constants (3-й; добавлены `Set.ofLazy`, убраны `isInitialized`/`orElse`), 532 примитивные паттерны (5-й, без изменений), 533 SC (7-й), 538 PEM (3-й) | 523 G1 по умолчанию везде; 534 compact object headers по умолчанию; 527 гибридный post-quantum обмен ключами TLS; 536 JFR in-process redaction |
| 28 (разработка) | 2027-03-09 (план) | **401 Value Objects (preview)** — первый превью Valhalla; **543 Structured Concurrency (final)**; **542 PEM Encodings (final)**; 540 Simple JSON API (incubator); 539 Strict Field Initialization in the JVM (preview) | 541 deprecate macOS/x64; 544 AOT-компиляция кода; 535 generational Shenandoah по умолчанию |

Что ещё не вышло (проверено 2026-11-06):

- **JEP 468 Derived Record Creation** (`p with { x = 0; }`) — статус Candidate, обновлялся в апреле 2024, ни в один релиз не назначен.
- **Null-Restricted and Nullable Types** (`Foo!` / `Foo?`) — JEP draft 8303099, обновлялся 2025-07-24, не назначен.
- **String Templates** — JEP 430 (21, preview) → 459 (22, 2-й preview) → 465 withdrawn до 23. Замены нет. Страница JDK 23 прочитана; причины отзыва (JDK-8329949, heise, Inside Java Newscast #71) `[не проверено]`.
- **Lazy Constants** — в 27 всё ещё preview (третий). В списке JDK 28 на 2026-11-02 их нет. Они остаются preview как минимум до выхода 28, если до Rampdown (2026-12-03) не будет назначен JEP с четвёртым превью или финализацией.
- **Примитивные паттерны** — пять превью (455, 488, 507, 530, 532), в списке JDK 28 пока нет.

### Records (JEP 395, final в 16)

Прозрачные, поверхностно неизменяемые носители данных. Канонический конструктор, аксессоры, `equals`/`hashCode`/`toString` генерируются.

```java
public record Range(int lo, int hi) {
    public Range {                                   // компактный канонический конструктор: проверка и нормализация
        if (lo > hi) throw new IllegalArgumentException("lo > hi: " + lo + " > " + hi);
    }
    public static Range of(int lo, int hi) { return new Range(lo, hi); }   // статическая фабрика допустима
    public Range withHi(int newHi) { return new Range(lo, newHi); }        // «wither» вручную
}
```

Ловушки:

- **Компоненты-массивы сравниваются по ссылке.** `record Blob(String n, byte[] data)`: два blob с равными байтами не `equals`, а `toString` печатает `[B@44d52de2` (проверено локально). Решение: `List<Byte>`, `ByteBuffer`, обёртка-значение или ручные `equals`/`hashCode`/`toString` через `Arrays.equals`/`Arrays.hashCode`. Правило Error Prone `ArrayRecordComponent`.
- Поверхностная неизменяемость: `record Team(List<String> members)` отдаёт изменяемый список. Копируйте в компактном конструкторе: `members = List.copyOf(members);` (заодно отвергает null).
- Record не наследует классы, не объявляет поля экземпляра сверх компонентов, неявно `final`.
- Record не JavaBean: аксессор `name()`, не `getName()`. Jackson (с 2.12), Hibernate (embeddables, проекции), Spring binding поддерживают records. JPA-сущностью record быть не может (нужны конструктор без аргументов, изменяемость, прокси).
- Сериализация record идёт через канонический конструктор, это безопаснее классической. Для сериализуемых данных берите records.
- С включённым превью JEP 401 (JDK 28) identity records подчиняются правилам ранней конструкции: часть существующих конструкторов, которые используют `this` до присваивания полей, перестаёт компилироваться (по тексту JEP 401).

### Sealed + pattern matching for switch + record patterns (JEP 409 / 441 / 440)

Sealed (final в 17), pattern matching for switch (final в 21) и record patterns (final в 21) вместе дают алгебраические типы данных.

```java
sealed interface Shape permits Circle, Square, Rect {}
record Circle(double r) implements Shape {}
record Square(double side) implements Shape {}
record Rect(double w, double h) implements Shape {}

static double area(Shape s) {
    return switch (s) {                       // без default: компилятор проверяет полноту
        case Circle c -> Math.PI * c.r() * c.r();
        case Square(var side) -> side * side;
        case Rect(double w, double h) when w == h -> w * w;   // guard
        case Rect(double w, double h) -> w * h;
    };
}
```

Ловушки:

- Пропуск `Rect` даёт `error: the switch expression does not cover all possible input values` (проверено локально). В этом смысл: **не добавляйте `default` в switch по sealed-типу**, иначе теряется предупреждение компилятора о новом подтипе. При раздельной компиляции новый разрешённый подтип во время выполнения даст `MatchException`.
- Pattern switch бросает `NullPointerException` на null-селекторе, если нет явного `case null` (допустимо `case null, default ->`) (проверено локально).
- Порядок важен: доминирующий паттерн (`case Shape s`) перед более специфичными — ошибка компиляции.
- Разрешённые подклассы лежат в том же модуле (или в том же пакете безымянного модуля); каждый обязан быть `final`, `sealed` или `non-sealed`.
- Guard (`when`) не учитывается при проверке полноты.

### Pattern matching для instanceof (JEP 394, final в 16)

`if (o instanceof String s && !s.isBlank())` заменяет приведение после проверки. Error Prone `PatternMatchingInstanceof` (experimental) подсказывает замену; аналогичное правило Sonar `[не проверено]`. Идиома для `equals`: `return o instanceof Point p && x == p.x && y == p.y;`.

### Switch expressions (JEP 361, final в 14)

Стрелочная форма, без fall-through, `yield` для блоков. Switch-выражение по enum полно без `default`: пропущенная константа — ошибка компиляции. Google Java Style теперь **требует** стрелочный синтаксис для новых switch и полноту.

### Text blocks (JEP 378, final в 15)

После открывающего `"""` идёт перевод строки. Случайный отступ срезается относительно закрывающего разделителя. `\` в конце строки склеивает строки, `\s` сохраняет пробел. Подстановки делайте через `.formatted(...)`, но не склеивайте SQL из пользовательских данных (см. «Идиомы безопасности»). Ловушки: хвостовые пробелы срезаются, переводы строк нормализуются к `\n` на любой платформе.

### `var` (JEP 286, Java 10; параметры лямбд JEP 323, Java 11)

Только локальные переменные. Правила из LVTI style guide Стюарта Маркса: применять, когда инициализатор делает тип очевидным (`var users = new ArrayList<User>()`), когда тип громоздкий, в for-each и try-with-resources. Не применять, когда справа вызов метода с неочевидным типом. Давайте переменным хорошие имена.

- `var list = new ArrayList<>();` выводит **`ArrayList<Object>`**: diamond вместе с `var` теряет тип элемента (проверено локально).
- С числовыми литералами нет целевой типизации: `var x = 0;` — `int`, `var l = 0L` — `long`.
- Google Java Style про `var` молчит.

### Sequenced collections (JEP 431, final в 21)

`SequencedCollection`/`SequencedSet`/`SequencedMap` дают `getFirst()`, `getLast()`, `addFirst()`, `removeLast()`, `reversed()` (это *представление*), `firstEntry()`, `pollLastEntry()`. Замените `list.get(list.size() - 1)` на `list.getLast()`, а обходы `LinkedHashMap` и `deque.peekFirst()` на `firstEntry()` (проверено локально).

- `reversed()` для неизменяемого списка тоже неизменяем.
- `getFirst()` на пустом списке бросает `NoSuchElementException`, не `IndexOutOfBoundsException`.
- `List.getFirst/getLast` из JDK 21 могут конфликтовать с одноимёнными extension-функциями Kotlin (проблема Kotlin/Android) `[не проверено]`.

### Unnamed variables и patterns `_` (JEP 456, final в 22)

`catch (NumberFormatException _)`, `for (var _ : items)`, `case Rect(var w, _) ->`, `map.forEach((_, v) -> ...)`, `try (var _ = lock.acquire())` (проверено локально). Имя `_` показывает намерение и заменяет `ignored`/`unused`. Требует `--release 22+`.

### Flexible constructor bodies (JEP 513, final в 25)

Операторы перед `super(...)`/`this(...)`, которые не используют `this`: проверка аргументов, вычисления и **инициализация собственных полей подкласса** до вызова конструктора суперкласса.

```java
class Pos extends Base {
    private final int v;
    Pos(int v) {
        if (v <= 0) throw new IllegalArgumentException("non-positive");
        this.v = v;          // разрешено: присваивание собственному полю в прологе
        super(v);
    }
}
```

Побочная выгода (проверено локально): если конструктор `Base` вызывает переопределяемый метод, переопределение теперь видит инициализированное `v` (раньше в старой Java печатался `0`). Идиома заменяет приём со статическим вспомогательным методом в аргументе `super(...)`.

### Module import declarations (JEP 511, final в 25)

`import module java.base;` импортирует все публичные типы верхнего уровня из всех экспортируемых пакетов модуля. Удобно для скриптов, прототипов и обучения.

- Неоднозначность: после `import module java.base; import module java.sql;` имя `Date` даёт `error: reference to Date is ambiguous` (проверено локально). Лечится импортом одного типа (`import java.util.Date;` перекрывает).
- **Google Java Style запрещает module imports** в продакшен-коде, как и wildcard-импорты.

### Compact source files и instance main (JEP 512, final в 25)

Файл из `void main() { IO.println("hi"); }` запускается `java Hello.java`; неявно импортируется `module java.base`; `java.lang.IO` даёт `print/println/readln` (проверено локально на 25; на 21 ответ «unnamed classes are a preview feature»).

- В финальной версии методы `IO` не импортированы статически: пишите `IO.println`.
- Неявный класс `final`, в безымянном пакете, по имени на него сослаться нельзя.
- Протокол запуска: `main(String[])` предпочтительнее `main()`; нестатический `main` требует неприватного конструктора без аргументов.
- В паре с запуском нескольких исходников (JEP 458, JDK 22) подходит для небольших утилит.

### Markdown documentation comments (JEP 467, final в 23)

Комментарии `///` используют CommonMark и теги Javadoc; ссылки в виде `[Math#addExact(int, int)]`. Проверено локально: `javadoc` 25 превращает `**sum**` в `<strong>`, списки в `<li>`. Google Java Style допускает и Markdown, и классический Javadoc. В паре с `{@snippet}` (JEP 413) даёт компилируемые примеры.

### Stream gatherers (JEP 485, final в 24)

`stream.gather(Gatherer)` — пользовательские промежуточные операции. Встроенные в `java.util.stream.Gatherers`: `windowFixed`, `windowSliding`, `fold`, `scan`, `mapConcurrent`.

- `Stream.of(1,2,3,4,5).gather(Gatherers.windowFixed(2)).toList()` даёт `[[1, 2], [3, 4], [5]]`; `scan(() -> 0, Integer::sum)` над 1..4 даёт `[1, 3, 6, 10]` (проверено локально).
- `mapConcurrent` использует виртуальные потоки и сохраняет порядок встречи.
- Не пишите больше stateful-лямбды в `map` вручную: пишите `Gatherer`.

### Scoped values и structured concurrency

Scoped values (JEP 506, final в 25) — неизменяемая замена `ThreadLocal` с ограниченным временем жизни, рассчитанная на виртуальные потоки. Structured concurrency остаётся preview до JDK 27 включительно (JEP 533), **final в 28 (JEP 543)**. Подробности — в [concurrency-native.md](concurrency-native.md).

### Stable values → Lazy constants (JEP 502 → 526 → 531, preview)

Отложенная инициализация не более одного раза, которую JIT рассматривает как `final`-константу.

```java
// API JDK 27, preview
private static final LazyConstant<Logger> LOG = LazyConstant.of(() -> Logger.create(Orders.class));
static final List<Connection> POOL = List.ofLazy(8, i -> open(i));
```

История API: JDK 25 — `StableValue.of()` и `orElseSet(...)` (проверено локально с `--enable-preview`); JDK 26 — переименование в `LazyConstant`, убраны `orElseSet/setOrThrow/trySet`, null-результаты запрещены; JDK 27 — убраны `isInitialized`/`orElse`, добавлен `Set.ofLazy`. **Не поставляйте код на этом API.** Он заменит идиомы holder-класса и double-checked locking только после финализации. Храните в `static final` полях ради constant folding.

### Примитивные типы в паттернах (JEP 455 → 488 → 507 → 530 → 532, всё ещё preview в 27)

`instanceof` и `switch` по всем примитивам с проверкой *точности*: `1000 instanceof byte b` — false; `16_777_217 instanceof float f` — false; `switch` по `long` (проверено локально, JDK 25 preview). Ловушки: семантика присваивания и приведения не меняется (всё ещё молча теряет данные); более строгое доминирование в JEP 530 делает ошибками часть прежде допустимых switch (несовместимость на уровне исходников).

### Value classes (JEP 401, preview в 28)

`value class` / `value record`: нет идентичности, `==` сравнивает значения полей, поля неявно final. С включённым превью `Integer`, `Long`, `Double`, `LocalDate`, `Optional` и другие становятся value classes (`String` нет).

- Ломается код, который синхронизируется на boxed-значениях или полагается на идентичность боксов (`Integer x = 1996, y = 1996; x == y` становится true).
- `synchronized` на value-объекте — ошибка компиляции или `IdentityException`; `WeakHashMap` и `java.lang.ref` неприменимы. Появились `Objects.hasIdentity` и `requireIdentity`.
- Идиома на сегодня: пишите «value-based» классы (final, неизменяемые, `equals` по состоянию, без идентичности, статические фабрики вместо публичных конструкторов). Тогда миграция сводится к перекомпиляции. `-Xlint:synchronization` в javac и Error Prone предупреждают о синхронизации на value-based классах.

### Derived record creation (JEP 468, candidate)

`Point p2 = p with { x = 0; };` недоступно. До тех пор: ручные withers или RecordBuilder.

### Устаревания и удаления, меняющие идиомы

| Изменение | JEP / релиз | Что делать | Проверка |
|---|---|---|---|
| Security Manager отключён навсегда; `System.setSecurityManager` бросает `UnsupportedOperationException` | 411 (deprecated в 17) → 486 (24) | Удалить код SM; изоляция через контейнеры и процессы | проверено локально |
| Финализация deprecated for removal; `finalize()` даёт предупреждение `[removal]` | 421 (18) | try-with-resources; `java.lang.ref.Cleaner` только как страховка | проверено локально (предупреждение на 25) |
| `Thread.stop()` бросает `UnsupportedOperationException` | JDK 20 (деградация), deprecated for removal | Кооперативная отмена через interrupt | проверено локально; версия 20 `[не проверено]` |
| Методы `sun.misc.Unsafe` для доступа к памяти: deprecated for removal (23), предупреждение при первом использовании (24) | 471, 498 | `VarHandle` (JEP 193), FFM `MemorySegment` (JEP 454) | проверено локально: на 25 печатается `WARNING: A terminally deprecated method in sun.misc.Unsafe has been called` |
| JNI предупреждает без `--enable-native-access` | 472 (24) | Предпочесть FFM; объявлять native access явно | — |
| Мутация `final`-полей через reflection предупреждает (26), позже начнёт бросать | 500 (26) | Не менять final через reflection; сериализаторы используют `ReflectionFactory`; тест с `--illegal-final-field-mutation=deny` | — |
| Удалены порты 32-bit x86 | 479 (24, Windows), 503 (25) | — | — |
| Удалён Applet API | 504 (26) | — | — |
| UTF-8 как кодировка по умолчанию | 400 (18) | Всё равно передавайте `StandardCharsets.UTF_8` явно; Error Prone `DefaultCharset` | `[не проверено]` |
| String Templates удалены | 465 withdrawn (23) | `String.formatted`, `MessageFormat`, `StringBuilder`; параметризованный SQL | — |
| Динамическая загрузка агентов предупреждает | 451 (21) | `-XX:+EnableDynamicAgentLoading` для инструментов вроде inline-моков Mockito; лучше `-javaagent` | `[не проверено]` |
| javac перестал по умолчанию запускать annotation processors из classpath | JDK 23 (release note `[не проверено]`) | Задавать процессоры явно: `<annotationProcessorPaths>` в maven-compiler-plugin или `-proc:full` | проверено локально (JDK 25, Windows 11): JMH-проект собрался без ошибок, но без сгенерированного `BenchmarkList`; исправлено `<annotationProcessorPaths>` и `-proc:full` |

Последняя строка касается любых процессоров: JMH, Lombok, MapStruct, Immutables, Dagger, RecordBuilder. Сборка при этом проходит молча, а ошибка проявляется позже, в рантайме.

### Ресурсы: возможности языка

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [JDK 17](https://openjdk.org/projects/jdk/17/) | OpenJDK | Список JEP JDK 17 |
| [JDK 21](https://openjdk.org/projects/jdk/21/) | OpenJDK | Список JEP JDK 21 |
| [JDK 22](https://openjdk.org/projects/jdk/22/) | OpenJDK | Список JEP JDK 22 |
| [JDK 23](https://openjdk.org/projects/jdk/23/) | OpenJDK | Список JEP JDK 23 (String Templates уже нет) |
| [JDK 24](https://openjdk.org/projects/jdk/24/) | OpenJDK | Список JEP JDK 24 |
| [JDK 25](https://openjdk.org/projects/jdk/25/) | OpenJDK | Список JEP JDK 25 (LTS) |
| [JDK 26](https://openjdk.org/projects/jdk/26/) | OpenJDK | Список JEP JDK 26 |
| [JDK 27](https://openjdk.org/projects/jdk/27/) | OpenJDK | Список JEP JDK 27 |
| [JDK 28](https://openjdk.org/projects/jdk/28/) | OpenJDK | График JDK 28 и назначенные JEP |
| [JEP 395: Records](https://openjdk.org/jeps/395) | Bierman, 2020 | Records: спецификация и обоснование («номинальные кортежи») |
| [JEP 409: Sealed Classes](https://openjdk.org/jeps/409) | Bierman, 2021 | Sealed classes |
| [JEP 441: Pattern Matching for switch](https://openjdk.org/jeps/441) | Bierman, 2023 | Pattern matching for switch: null, доминирование, полнота |
| [JEP 440: Record Patterns](https://openjdk.org/jeps/440) | Bierman, 2023 | Record patterns |
| [JEP 456: Unnamed Variables & Patterns](https://openjdk.org/jeps/456) | Bierman, 2024 | Unnamed variables `_` |
| [JEP 378: Text Blocks](https://openjdk.org/jeps/378) | Laskey/Marks, 2020 | Text blocks |
| [JEP 361: Switch Expressions](https://openjdk.org/jeps/361) | Bierman, 2020 | Switch expressions |
| [JEP 286: Local-Variable Type Inference](https://openjdk.org/jeps/286) | Goetz, 2018 | `var` |
| [JEP 431: Sequenced Collections](https://openjdk.org/jeps/431) | Marks, 2023 | Sequenced collections |
| [JEP 465: String Templates (Third Preview)](https://openjdk.org/jeps/465) | Bierman, 2024 | String Templates, 3-й preview, отозван |
| [JEP 513: Flexible Constructor Bodies](https://openjdk.org/jeps/513) | Bierman/Goetz, 2025 | Flexible constructor bodies |
| [JEP 511: Module Import Declarations](https://openjdk.org/jeps/511) | Bierman, 2025 | Module import declarations |
| [JEP 512: Compact Source Files and Instance Main Methods](https://openjdk.org/jeps/512) | Bierman/Reinhold, 2025 | Compact source files и instance main |
| [JEP 467: Markdown Documentation Comments](https://openjdk.org/jeps/467) | Gibbons, 2024 | Markdown doc comments |
| [JEP 485: Stream Gatherers](https://openjdk.org/jeps/485) | Lundén, 2024 | Stream gatherers |
| [JEP 502: Stable Values (Preview)](https://openjdk.org/jeps/502) | Minborg/Cimadamore, 2025 | Stable Values (1-й preview) |
| [JEP 526: Lazy Constants (Second Preview)](https://openjdk.org/jeps/526) | Minborg, 2026 | Lazy Constants (2-й preview, переименование) |
| [JEP 531: Lazy Constants (Third Preview)](https://openjdk.org/jeps/531) | Minborg, 2026 | Lazy Constants (3-й preview) |
| [JEP 532: Primitive Types in Patterns, instanceof, and switch (Fifth Preview)](https://openjdk.org/jeps/532) | Bimpoudis, 2026 | Примитивные паттерны (5-й preview), список ловушек |
| [JEP 506: Scoped Values](https://openjdk.org/jeps/506) | Rose/Bateman, 2025 | Scoped values (final) |
| [JEP 543: Structured Concurrency](https://openjdk.org/jeps/543) | Bateman, 2026 | Structured concurrency (final в 28) |
| [JEP 401: Value Objects (Preview)](https://openjdk.org/jeps/401) | Smith/Bateman, 2026 | Value objects (preview, JDK 28) |
| [JEP 468: Derived Record Creation (Preview)](https://openjdk.org/jeps/468) | Bierman/Goetz | Derived record creation (candidate) |
| [JEP draft: Null-Restricted and Nullable Types (Preview)](https://openjdk.org/jeps/8303099) | Smith | Null-restricted и nullable типы (draft) |
| [JEP 500: Prepare to Make Final Mean Final](https://openjdk.org/jeps/500) | Bateman, 2025 | Final means final |
| [JEP 486: Permanently Disable the Security Manager](https://openjdk.org/jeps/486) | Bateman/Reinhold, 2024 | Security Manager отключён |
| [JEP 421: Deprecate Finalization for Removal](https://openjdk.org/jeps/421) | Ludwig, 2021 | Deprecate finalization |
| [JEP 471: Deprecate the Memory-Access Methods in sun.misc.Unsafe for Removal](https://openjdk.org/jeps/471) | Bateman/Cimadamore, 2024 | Deprecate доступа к памяти через Unsafe |
| [JEP 498: Warn upon Use of Memory-Access Methods in sun.misc.Unsafe](https://openjdk.org/jeps/498) | Bateman, 2024 | Предупреждение при доступе к памяти через Unsafe |
| [JEP 472: Prepare to Restrict the Use of JNI](https://openjdk.org/jeps/472) | Pressler/Cimadamore, 2024 | Подготовка к ограничению JNI |
| [Project Amber](https://openjdk.org/projects/amber/) | OpenJDK | Центр Project Amber (records, patterns, `var`) |
| [Project Valhalla](https://openjdk.org/projects/valhalla/) | OpenJDK | Центр Valhalla |
| [Programmer's Guide to Text Blocks](https://openjdk.org/projects/amber/guides/text-blocks-guide) | Laskey/Marks | Руководство по text blocks |
| [Java Language Changes for Java SE 25](https://docs.oracle.com/en/java/javase/25/language/java-language-changes-release.html) | Oracle | Изменения языка по релизам с примерами |
| [The Java Language Specification, Java SE 25 Edition](https://docs.oracle.com/javase/specs/jls/se25/html/index.html) | Oracle | JLS 25, авторитетная семантика |
| [Java Almanac](https://javaalmanac.io/) | Marc Hoffmann | Различия возможностей и API по релизам, песочница |
| [Learn Java (dev.java)](https://dev.java/learn/) | Oracle DevRel | Официальные туториалы (records, patterns, collections) |
| [Inside Java Newscast #71](https://nipafx.dev/inside-java-newscast-71/) | Parlog, 2024 | Почему убрали String Templates |
| [String Templates in Java (HappyCoders)](https://www.happycoders.eu/java/string-templates) | Woitzik | История String Templates |

---

## Каноничные источники и стайлгайды

**Ключевые выводы**

- **Effective Java, 3rd ed. (Bloch, 2017/2018)** остаётся самым полезным справочником идиом: 90 пунктов, база Java 9. Четвёртого издания на 2026-11 не найдено `[не проверено]`; перепроверьте страницы издателя. Соответствие пунктов современной Java:

| Пункты | Совет | Статус в Java 25 |
|---|---|---|
| 1 статические фабрики, 2 builders | `of/from/valueOf`; builder при многих необязательных параметрах | Верно. Для агрегатов данных берите **record** и статические фабрики; builder только при многих необязательных параметрах (или RecordBuilder). |
| 3 singleton через enum, 4 класс-утилита без экземпляров | | Верно; в приложениях лучше singleton под управлением DI. |
| 5 DI вместо жёсткой связки | | Верно; конструкторная инъекция, фреймворк не нужен (см. каталог паттернов). |
| 7 убирать устаревшие ссылки, 8 избегать финализаторов и cleaners | | **Усилено**: финализация deprecated for removal (JEP 421). |
| 9 try-with-resources | | Верно; в спецификации ресурса допустимы `var` и `_`. |
| 10–12 контракты equals/hashCode/toString | | Верно; records генерируют их, вручную пишите только для классов. Массивы в компонентах ломают равенство record. |
| 13 разумное использование clone | | Верно: копирующие конструкторы и фабрики, `List.copyOf`. |
| 14 Comparable | | Верно; через `Comparator.comparing(...).thenComparing(...)`. |
| 15–17 минимизировать доступность и изменяемость | | Верно; records и модули (сильная инкапсуляция с JEP 403). |
| 18–19 композиция вместо наследования; проектировать для наследования или запрещать | | Верно; **sealed** — новая середина («закрытое наследование»). |
| 20–22 интерфейсы или абстрактные классы, default-методы | | Верно; приватные методы интерфейсов (Java 9) помогают. |
| 23 иерархии классов вместо tagged classes | | Теперь: **sealed interface + records**. |
| 26–33 generics | | Верно (PECS, без raw types, `@SafeVarargs`). |
| 34–38 enum, EnumSet/EnumMap | | Верно. |
| 42–48 лямбды и потоки | | Верно; добавились `Stream.toList()` (16) и gatherers (24). |
| 49–56 методы: проверять параметры, защитные копии, пустые коллекции вместо null, Optional с умом | | Верно; `Objects.requireNonNull`, `List.copyOf`, JSpecify. |
| 57–68 общее программирование | | Верно; пункт 63 (конкатенация строк) важен только в циклах после JEP 280 (indy-конкатенация). |
| 69–77 исключения | | Верно (checked для восстановимых, не игнорировать). |
| 78–84 конкурентность | | Частично устарело: пункт 80 «executors вместо потоков» — теперь виртуальные потоки (`Executors.newVirtualThreadPerTaskExecutor()`); пункт 83 (ленивая инициализация) — сегодня holder-идиома, позже Lazy Constants. |
| 85–90 сериализация («предпочитайте альтернативы») | | Усилено; records сериализуются через канонический конструктор; фильтры JEP 290/415. |

- **Google Java Style Guide**: отступ 2 пробела, лимит 100 колонок, без wildcard- и module-импортов, один класс верхнего уровня на файл, перегрузки рядом, `@Override` всегда, пойманные исключения не игнорируются молча, `UPPER_SNAKE_CASE` только для глубоко неизменяемых `static final`, Markdown-Javadoc допустим, новые switch только со `->` и полные, правила оформления text blocks. Проверяется google-java-format и Checkstyle `google_checks.xml`.
- **Oracle «Code Conventions for the Java Programming Language»** (1997, последняя правка 1999) `[устарело]`: заморожены, предшествуют generics и лямбдам. Замена: Google Java Style или Palantir style плюс форматтер.
- **Java Design Patterns (iluwatar)**: около 200 реализаций паттернов, качество разное, многое в стиле до Java 17. Как каталог годится, как справочник идиом нет. **Refactoring Guru**: ясные объяснения GoF с примерами на Java в классическом ООП-стиле.
- **«Java by Comparison» (Harrer, Lenhard, Dietz, 2018)**: 70 примеров «до/после» для Java 8/9, полезно для джуниоров (имена, отрицания, `Optional`, исключения).
- **Nicolai Parlog (nipafx), inside.java, Java Almanac, dev.java**: лучшие актуальные источники идиом по релизам.
- **SEI CERT Oracle Coding Standard for Java**: каталог правил по безопасности и корректности, по странице на правило (ERR, OBJ, SER, IDS). Составлен до Java 11, но большинство правил верно.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Effective Java, 3rd Edition](https://www.informit.com/store/effective-java-9780134685991) | Bloch, 2017 | Effective Java 3e, страница издателя |
| [effective-java-3e-source-code](https://github.com/jbloch/effective-java-3e-source-code) | Bloch | Исходный код книги |
| [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) | Google | Google Java Style |
| [Checkstyle: Google's Style](https://checkstyle.sourceforge.io/google_style.html) | Checkstyle | Что из Google style проверяет Checkstyle |
| [Code Conventions for the Java Programming Language](https://www.oracle.com/java/technologies/javase/codeconventions-contents.html) | Sun/Oracle, 1999 | Исторические конвенции `[устарело]`, замена: Google Java Style |
| [iluwatar/java-design-patterns](https://github.com/iluwatar/java-design-patterns) | Seppälä et al. | Каталог паттернов |
| [Java Design Patterns](https://java-design-patterns.com/) | iluwatar | То же, сайт |
| [Design Patterns in Java (Refactoring Guru)](https://refactoring.guru/design-patterns/java) | Shvets | GoF на Java |
| [Code Smells (Refactoring Guru)](https://refactoring.guru/refactoring/smells) | Shvets | Каталог code smells |
| [Java by Comparison](https://pragprog.com/titles/javacomp/java-by-comparison/) | Harrer et al., 2018 | Идиомы «до/после» |
| [nipafx](https://nipafx.dev/) | Parlog | Статьи и видео по современной Java |
| [Inside.java](https://inside.java/) | Команда Oracle Java | Официальный блог и newscasts |
| [SEI CERT Oracle Coding Standard for Java](https://wiki.sei.cmu.edu/confluence/display/java/SEI+CERT+Oracle+Coding+Standard+for+Java) | SEI CERT | Правила безопасного кодирования |
| [Secure Coding Guidelines for Java SE](https://www.oracle.com/java/technologies/javase/seccodeguide.html) | Oracle | Secure Coding Guidelines for Java SE |

---

## Неизменяемость, значения, равенство

**Ключевые выводы**

- **Records для значений; `final`-классы с `private final` полями для типов-значений с поведением.** Неизменяемые объекты потокобезопасны, кэшируемы и безопасны как ключи map. Изменённый ключ пропадает: положите `List` ключом в `HashMap`, измените его, и `get` вернёт `null` при `size()` равном 1 (проверено локально).
- **Unmodifiable не значит immutable.** `Collections.unmodifiableList(src)` — *представление*: последующий `src.add(3)` виден через него. `List.copyOf(src)` — настоящий снимок (и возвращает тот же экземпляр, если список уже неизменяемый) (проверено локально).
- **`List.of/Set.of/Map.of/copyOf` (JEP 269) отвергают null везде**, в том числе в запросах: `List.of("a").contains(null)` бросает NPE; `Map.of` с дубликатами ключей бросает `IllegalArgumentException`; порядок итерации `Set.of`/`Map.of` намеренно случаен при каждом запуске JVM (NPE и IAE проверены локально).
- **`Stream.toList()` (16) неизменяем, но допускает null.** `Collectors.toList()` возвращает `ArrayList` (по спецификации не определено, сейчас изменяемый). `Collectors.toUnmodifiableList()` отвергает null. Выбирайте осознанно (проверено локально).
- **`Arrays.asList` имеет фиксированный размер и пишет сквозь**: `set` работает, `add` бросает UOE (проверено локально).
- Защитные копии на входе (конструктор) и на выходе (аксессор) для изменяемых типов; для массивов возвращайте `array.clone()` или лучше `List`. SpotBugs `EI_EXPOSE_REP`/`EI_EXPOSE_REP2`, PMD `MethodReturnsInternalArray`/`ArrayIsStoredDirectly`, Error Prone `MutablePublicArray`.
- **Контракт equals/hashCode**: переопределяйте оба или ни одного. Класс с `equals` без `hashCode` «теряется» в `HashSet` (`contains` даёт false) (проверено локально). Error Prone `EqualsHashCode` (ERROR), SpotBugs `HE_EQUALS_NO_HASHCODE`, PMD `OverrideBothEqualsAndHashcode`. Используйте `instanceof`-паттерн (или `getClass()` для неfinal-классов, где подклассы добавляют состояние; Error Prone `EqualsGetClass` помечает `getClass`, спор в Bloch, пункт 10). `Objects.hash(...)` подходит везде, кроме горячих путей (varargs-массив).
- **`compareTo` согласован с equals** или это документировано: `BigDecimal("2.0").equals(BigDecimal("2.00"))` — false, а `compareTo` даёт 0, поэтому `HashSet` держит 2 элемента, `TreeSet` — 1 (проверено локально). Error Prone `BigDecimalEquals`.
- **Равенство по ссылке**: `==` на `String` и боксах — ошибка (работает случайно для интернированных литералов и кэша `Integer` от −128 до 127: `127 == 127` true, `128 == 128` false; проверено локально). С Valhalla (превью JEP 401) боксы становятся value-объектами, и `==` сравнивает значения. Не полагайтесь ни на одно из поведений.
- Комбинаторы `Comparator` вместо ручного compare:
  ```java
  static final Comparator<Person> BY_NAME_THEN_AGE =
      Comparator.comparing(Person::lastName, String.CASE_INSENSITIVE_ORDER)
                .thenComparingInt(Person::age)
                .thenComparing(Person::id, Comparator.nullsLast(Comparator.naturalOrder()));
  ```
  Никогда не пишите `return a - b` (переполнение); используйте `Integer.compare`. Error Prone `ComparisonOutOfRange`.
- **Builders и records**: до примерно 5 обязательных компонентов хватает record с компактным конструктором и именованными статическими фабриками. Для многих необязательных параметров: вложенный `Builder` (Bloch, пункт 2), `@RecordBuilder` (Randgalt/record-builder генерирует builders и withers для records), Immutables или Lombok `@Builder`. Выражения `with` из JEP 468 недоступны (candidate).
- **Дисциплина value-based классов (до Valhalla)**: final, неизменяемость, equals по состоянию, нет публичных конструкторов (статические фабрики), никогда не синхронизируйтесь на экземплярах и не применяйте к ним `==`/`identityHashCode`. Тогда переход на `value class` тривиален.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [JEP 269: Convenience Factory Methods for Collections](https://openjdk.org/jeps/269) | Marks, 2016 | Фабричные методы коллекций (политика null, случайный порядок) |
| [List (Java SE 25 API)](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/List.html) | Oracle | Раздел «Unmodifiable Lists» |
| [Value-based Classes](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/doc-files/ValueBased.html) | Oracle | Определение value-based классов |
| [Comparator (Java SE 25 API)](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/Comparator.html) | Oracle | Комбинаторы Comparator |
| [Object (Java SE 25 API)](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Object.html) | Oracle | Контракты equals/hashCode |
| [Record (Java SE 25 API)](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Record.html) | Oracle | Семантика равенства records |
| [ImmutableCollectionsExplained (Guava)](https://github.com/google/guava/wiki/ImmutableCollectionsExplained) | Guava | Immutable и unmodifiable |
| [RecordBuilder](https://github.com/Randgalt/record-builder) | Randgalt | Builders и withers для records |
| [Immutables](https://immutables.github.io/) | Immutables | Неизменяемые типы через annotation processor |
| [EqualsHashCode (Error Prone)](https://errorprone.info/bugpattern/EqualsHashCode) | Error Prone | Описание правила |
| [ArrayRecordComponent (Error Prone)](https://errorprone.info/bugpattern/ArrayRecordComponent) | Error Prone | Массивы в records |
| [ReferenceEquality (Error Prone)](https://errorprone.info/bugpattern/ReferenceEquality) | Error Prone | `==` на типах с equals |
| [BigDecimalEquals (Error Prone)](https://errorprone.info/bugpattern/BigDecimalEquals) | Error Prone | Ловушка equals у BigDecimal |

---

## Null и Optional

**Ключевые выводы**

- **Падайте рано на границах**: `this.name = Objects.requireNonNull(name, "name");` (с Java 7); `Objects.requireNonNullElse(x, dflt)` и `requireNonNullElseGet` (9). Информативные NPE (JEP 358, включены по умолчанию с 15) называют выражение, давшее null.
- **Optional — для возвращаемых типов, где результата может не быть.** Goetz: «ограниченный механизм для возвращаемых значений библиотечных методов, где нужен ясный способ выразить "нет результата"». Чего не делать:
  - Optional в полях, параметрах, элементах коллекций, `Optional<List<T>>` (возвращайте пустой список). Правила Sonar против Optional-параметров и null-Optional `[не проверено]`; Error Prone `NullOptional`.
  - Не возвращайте `null` из метода, возвращающего Optional (SpotBugs `NP_OPTIONAL_RETURN_NULL`).
  - `opt.get()` без проверки заменяйте на `orElseThrow()` (10), он честнее читается; `isPresent()+get()` заменяйте на `map/ifPresentOrElse`.
  - `orElse(expensive())` вычисляет аргумент всегда; `orElseGet(() -> expensive())` лениво (проверено локально).
  - `Optional.of(null)` бросает NPE; используйте `ofNullable` (проверено локально).
  - Специализации `OptionalInt` и другие избегают боксинга. `Optional` — value-based класс: операции идентичности неверны, под превью JEP 401 это value class.
- **Аннотации nullness: JSpecify 1.0 (2024)** — сходящийся стандарт (`@NullMarked` на пакете, модуле или классе делает неаннотированные типы non-null; `@Nullable` — type-use аннотация; `@NullUnmarked` — выход). Spring Framework 7 и Boot 4 (ноябрь 2025) перешли на JSpecify; Guava и Truth переходят. Проверяйте **NullAway** (плагин Error Prone, режим JSpecify) или Checker Framework; IntelliJ понимает JSpecify. `@NullMarked` кладите в `package-info.java` или `module-info.java`. Даты внедрения `[не проверено]`; документация отвечает 200.
  ```java
  @NullMarked package com.acme.orders;   // package-info.java
  // дальше: String find(String id)  -> non-null; @Nullable String find(...) -> может быть null
  ```
- Возвращайте пустые коллекции и массивы, не null (Bloch, пункт 54; PMD `ReturnEmptyCollectionRatherThanNull`; SpotBugs `PZLA_PREFER_ZERO_LENGTH_ARRAYS`).
- `Boolean`, возвращающий `null`, — трёхзначная логика и источник ошибок (SpotBugs `NP_BOOLEAN_RETURN_NULL`); моделируйте enum.
- Pattern `switch` бросает NPE на null без `case null`: обрабатывайте null в switch явно (проверено локально).
- Null-restricted типы (`String!`) на уровне языка — пока только draft JEP (8303099). Не ждите их раньше финализации value classes Valhalla.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Should Java 8 getters return Optional type? (Stack Overflow)](https://stackoverflow.com/questions/26327957/should-java-8-getters-return-optional-type/26328555#26328555) | Goetz, 2014 | Задуманное назначение Optional `[не проверено]` (403 для ботов) |
| [Optional (Java SE 25 API)](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/Optional.html) | Oracle | Заметки API («primarily for return types») |
| [JSpecify](https://jspecify.dev/) | Группа JSpecify | Стандартные аннотации nullness |
| [JSpecify User Guide](https://jspecify.dev/docs/user-guide/) | JSpecify | Семантика `@NullMarked` и `@Nullable` |
| [NullAway](https://github.com/uber/NullAway) | Uber | Быстрый проверяющий nullness (плагин Error Prone) |
| [The Checker Framework](https://checkerframework.org/) | UW PLSE | Подключаемые проверки типов, включая nullness |
| [Null-safety (Spring Framework)](https://docs.spring.io/spring-framework/reference/core/null-safety.html) | Spring | Переход Spring 7 на JSpecify |
| [JSpecify 1.0 (InfoQ)](https://www.infoq.com/news/2024/08/jspecify-java-nullability/) | InfoQ, 2024 | Новость о JSpecify 1.0 |
| [UsingAndAvoidingNullExplained (Guava)](https://github.com/google/guava/wiki/UsingAndAvoidingNullExplained) | Guava | Философия null |
| [JEP 358: Helpful NullPointerExceptions](https://openjdk.org/jeps/358) | Goldstein, 2019 | Информативные NullPointerException |

---

## Исключения и ресурсы

**Ключевые выводы**

- **Checked — для восстановимых ситуаций, на которые вызывающий может отреагировать; unchecked — для ошибок программиста** (Bloch, пункт 70). Современные библиотеки и фреймворки склоняются к unchecked (`DataAccessException` в Spring, `UncheckedIOException` в операциях потоков `Files.lines`). Лямбды и checked-исключения плохо сочетаются: оборачивайте на границе (`catch (IOException e) { throw new UncheckedIOException(e); }`), не используйте sneaky throw.
- **Оборачивайте с причиной на нужном уровне абстракции** (Bloch, пункт 73): `throw new OrderStoreException("load " + id, e);`. Не теряйте причину (PMD `PreserveStackTrace`). Никакого `e.printStackTrace()` (Error Prone `CatchAndPrintStackTrace`, PMD `AvoidPrintStackTrace`). Не логируйте и не перебрасывайте одновременно: получится двойная запись.
- **Не ловите широко `Exception`/`Throwable`/`Error`**, кроме верхнеуровневых границ (обработчик запроса, корень потока), где вы логируете и переводите ошибку. Широкий catch скрывает ошибки. PMD `AvoidCatchingGenericException`; Error Prone `CatchingUnchecked` (experimental); SpotBugs `REC_CATCH_EXCEPTION`; Sonar «Throwable and Error should not be caught» `[не проверено]`.
- **Пустые catch-блоки** требуют комментария с причиной (Google style) или имени `_` для обозначения намерения. Error Prone `EmptyCatch`, PMD `EmptyCatchBlock`, SpotBugs `DE_MIGHT_IGNORE`.
- **InterruptedException**: пробросьте или восстановите флаг. Перехват **сбрасывает** флаг прерывания (`isInterrupted()` даёт false), поэтому установите его снова (проверено локально):
  ```java
  try { queue.take(); }
  catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new CancellationException("interrupted"); }
  ```
  Error Prone `InterruptedExceptionSwallowed` (experimental); Sonar «InterruptedException should not be ignored» `[не проверено]`.
- **try-with-resources** для каждого `AutoCloseable`: потоки из `Files.lines/list/walk`, JDBC, `HttpClient` (с 21 AutoCloseable), `ExecutorService` (с 19 AutoCloseable). Ресурсы закрываются в обратном порядке, исключения при закрытии добавляются как **suppressed**: тело «body» и закрытия дают `suppressed=[close r2, close r1]` (проверено локально). Error Prone `StreamResourceLeak`/`MustBeClosedChecker`, PMD `CloseResource`, SpotBugs `OS_OPEN_STREAM`.
- Без финализаторов; `Cleaner` только как страховка (Bloch, пункт 8; JEP 421). Предупреждение `[removal]` проверено локально.
- Исключения — для исключительных путей: без управления потоком через исключения (PMD `ExceptionAsFlowControl`). Для ожидаемых отказов в доменном коде рассмотрите sealed-тип `Result` (см. «Мини-каталог паттернов»); это спорно (см. «Спорные вопросы»).
- Используйте стандартные исключения (`IllegalArgumentException`, `IllegalStateException`, `UnsupportedOperationException`, `NoSuchElementException`); не бросайте голые `RuntimeException`/`Exception` (PMD `AvoidThrowingRawExceptionTypes`).
- Сообщения об ошибке содержат данные для разбора (Bloch, пункт 75), но никогда секреты и персональные данные (см. «Идиомы безопасности»).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [The try-with-resources Statement](https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html) | Oracle | try-with-resources, suppressed-исключения |
| [Unchecked Exceptions — The Controversy](https://docs.oracle.com/javase/tutorial/essential/exceptions/runtime.html) | Oracle | «Unchecked exceptions — the controversy» |
| [The Trouble with Checked Exceptions](https://www.artima.com/articles/the-trouble-with-checked-exceptions) | Venners/Hejlsberg, 2003 | Классическая критика checked-исключений |
| [InterruptedExceptionSwallowed (Error Prone)](https://errorprone.info/bugpattern/InterruptedExceptionSwallowed) | Error Prone | Обработка прерываний |
| [CatchAndPrintStackTrace (Error Prone)](https://errorprone.info/bugpattern/CatchAndPrintStackTrace) | Error Prone | Логгер вместо printStackTrace |
| [Cleaner (Java SE 25 API)](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/ref/Cleaner.html) | Oracle | Cleaner как замена финализатору |
| [ERR00-J. Do not suppress or ignore checked exceptions](https://wiki.sei.cmu.edu/confluence/display/java/ERR00-J.+Do+not+suppress+or+ignore+checked+exceptions) | SEI CERT | Правило ERR00-J |

---

## Повседневные идиомы библиотеки

**Ключевые выводы**

### Коллекции

- Объявляйте по интерфейсу (`List`, `Map`; Bloch, пункт 64; PMD `LooseCoupling`). По умолчанию `ArrayList`, `ArrayDeque`, `HashMap`; `LinkedList` почти никогда.
- `Vector`/`Hashtable`/`Stack`/`StringBuffer` — устаревшие синхронизированные типы (PMD `ReplaceVectorWithList`, `ReplaceHashtableWithMap`, `AvoidStringBufferField`; Error Prone `JdkObsolete`).
- Изменение при итерации даёт `ConcurrentModificationException`; используйте `removeIf` или `Iterator.remove` (проверено локально).
- `list.remove(1)` удаляет **по индексу** (`[1,2,3]` → `[1,3]`), `remove(Integer.valueOf(1))` — по значению (→ `[2,3]`) (проверено локально).
- Идиомы `Map`: `computeIfAbsent(k, _ -> new ArrayList<>()).add(v)`, `merge(k, 1, Integer::sum)`, `getOrDefault`, `putIfAbsent`. Sequenced-представления — см. раздел о версиях языка.

### Потоки: когда и как

- Подходят для декларативных конвейеров: filter/map/group/collect. Набор коллекторов: `groupingBy(f, counting())`, `groupingBy(f, TreeMap::new, toList())`, `partitioningBy`, `toMap(k, v, merge)`, `teeing` (12), `flatMapping`, `filtering` (9), `mapMulti` (16), gatherers (24).
- `Collectors.toMap` бросает `IllegalStateException` на дубликатах ключей и **NPE на null-значениях**: передавайте функцию слияния; для null нужен другой коллектор (проверено локально).
- Держите лямбды без побочных эффектов (Bloch, пункт 46); без `forEach`, меняющего внешнее состояние; без `peek` для логики.
- Предпочитайте циклы при checked-исключениях, раннем `break`/`return` во вложенной логике, изменении нескольких локальных переменных, арифметике индексов и когда for-each короче. Возвращайте из API `Collection`/`List`, не `Stream` (Bloch, пункт 47), если только поток не ленивый или огромный.
- `parallel()` только после измерения (Bloch, пункт 48): большие делимые источники, нет общего состояния, CPU-нагрузка. В серверном пути запроса по умолчанию нельзя (общий ForkJoinPool).
- Потоки ввода-вывода (`Files.lines`, `Files.walk`, `Files.list`) закрывайте через try-with-resources.
- Различия изменяемости `Stream.toList()` и `collect(toList())` — см. раздел о неизменяемости.

### Generics

- Без raw types (Bloch, пункт 26; Error Prone и javac `-Xlint:rawtypes`; правила PMD и Sonar про raw types `[не проверено]`).
- **PECS**: Producer `extends`, Consumer `super`: `static <T> void copy(List<? super T> dst, List<? extends T> src)`; для ключей сортировки `<T extends Comparable<? super T>>`. Не возвращайте wildcard-типы.
- Ловушки стирания: нет `new T[]`, нет `instanceof List<String>`, перегрузки `f(List<String>)`/`f(List<Integer>)` конфликтуют («same erasure»), heap pollution при generic varargs (`@SafeVarargs` на final/static/private методах). Типобезопасный гетерогенный контейнер с ключами `Class<T>` (Bloch, пункт 33). Предпочитайте `List<E>` массиву `E[]` (Bloch, пункт 28).

### Enum

- Заменяйте `int`-константы и булевы флаги; `EnumSet` вместо битовых полей, `EnumMap` вместо индексации через `ordinal()` (Bloch, пункты 34–37).
- **Strategy enum**: тела констант (`ADD { int apply(...) }`) или лучше поле с лямбдой: `ADD("+", Integer::sum)`.
- Ловушка: константы с телами — анонимные подклассы, `Op.ADD.getClass() != Op.class`; используйте `getDeclaringClass()` (проверено локально).
- Enum-singleton для служб без состояния (Bloch, пункт 3). Error Prone `ImmutableEnumChecker` помечает изменяемые поля enum.
- Switch по enum пишите полным switch-выражением (без default).

### Строки и текст

- `equals`, не `==` (SpotBugs `ES_COMPARING_STRINGS_WITH_EQ`, PMD `UseEqualsToCompareStrings`); литерал первым или `Objects.equals` для безопасности к null (PMD `LiteralsFirstInComparisons`).
- `isBlank()` и `strip()` (учитывает Unicode, 11) вместо `trim()`; `repeat`, `lines()`, `indent`, `formatted` (15), `String.join`, `StringJoiner`, `chars()`.
- Преобразование регистра для машинных строк **с `Locale.ROOT`**: `"TITLE".toLowerCase(tr)` даёт точечную `ı` без точки (проверено локально). Error Prone `StringCaseLocaleUsage`, PMD `UseLocaleWithCaseConversions`.
- `String.split` отбрасывает хвостовые пустые строки (`"a,b,,".split(",")` длины 2; проверено локально): используйте `split(regex, -1)` или Guava `Splitter` (Error Prone `StringSplitter`).
- Всегда задавайте кодировку (`StandardCharsets.UTF_8`; по умолчанию UTF-8 с JEP 400, но будьте явными; Error Prone `DefaultCharset`).
- `StringBuilder` в циклах; одиночное выражение с `+` допустимо (indy-конкатенация, JEP 280).

### Дата и время: только java.time (JSR 310)

- `Instant` для машинных меток времени, `LocalDate` для дат без зоны, `ZonedDateTime` для человеческого календарного времени с правилами, `OffsetDateTime` для сетевых форматов и БД, `Duration`/`Period`.
- Внедряйте `Clock` для тестируемости (`Instant.now(clock)`). `DateTimeFormatter` неизменяем и потокобезопасен (в отличие от `SimpleDateFormat`).
- Не используйте `java.util.Date`/`Calendar`/`SimpleDateFormat` (Error Prone `JavaUtilDate`, PMD `ReplaceJavaUtilDate`); на границах с legacy конвертируйте через `date.toInstant()`.
- Избегайте неявной зоны по умолчанию (`LocalDateTime.now()`; Error Prone `JavaTimeDefaultTimeZone`). Joda-Time в режиме сопровождения и сам рекомендует java.time.

### Деньги

- `BigDecimal` (или `long` в минимальных единицах), никогда `double` (`0.1 + 0.2 = 0.30000000000000004`).
- Создавайте из `String` или `BigDecimal.valueOf(double)`, никогда `new BigDecimal(0.1)` (получится `0.1000000000000000055511…`; проверено локально). Error Prone `BigDecimalLiteralDouble`, PMD `AvoidDecimalLiteralsInBigDecimalConstructor`, SpotBugs `DMI_BIGDECIMAL_CONSTRUCTED_FROM_DOUBLE`.
- Всегда передавайте `RoundingMode` в `divide`/`setScale` (иначе `ArithmeticException` на непериодических делениях). Сравнивайте через `compareTo`, не `equals`.
- Храните сумму вместе с валютой: `Money(BigDecimal amount, Currency currency)` или JSR 354 (Moneta).
- Переполнение целых: `Math.addExact`; `Math.abs(Integer.MIN_VALUE)` отрицателен (проверено локально).

### Логирование (фасад SLF4J 2.x + бэкенд Logback или Log4j 2)

```java
private static final Logger log = LoggerFactory.getLogger(OrderService.class);
log.info("Order {} placed by {}", orderId, customerId);           // параметры: без конкатенации и isDebugEnabled
log.atWarn().setMessage("Retrying {}").addArgument(attempt)
   .addKeyValue("orderId", orderId).setCause(e).log();             // fluent API (2.0+), структурированные пары ключ-значение
try (var _ = MDC.putCloseable("requestId", reqId)) { ... }          // MDC в рамках блока
```

- Исключение передавайте **последним аргументом** (логируется со стектрейсом).
- Не логируйте секреты и персональные данные; очищайте пользовательский ввод в логах (инъекция CR/LF).
- Не используйте `System.out` в библиотеках (Error Prone `SystemOut`, PMD `SystemPrintln`).
- Fluent-цепочка ничего не пишет без финального `.log()`.
- MDC построен на `ThreadLocal`: передавайте его явно через executors и виртуальные потоки (или библиотеки контекста с поддержкой ScopedValue). Наблюдаемость вынесена в отдельные заметки.

### Сериализация

- Не используйте Java-сериализацию в новом коде (Bloch, пункт 85: «нет причин использовать Java-сериализацию в новой системе»). Берите JSON/Protobuf/Avro с явными DTO (records).
- Если неизбежна: records (сериализуются через канонический конструктор), serialization proxy (Bloch, пункт 90) и **фильтры десериализации**: JEP 290 (`jdk.serialFilter`, `ObjectInputFilter`) и фабрики контекстных фильтров JEP 415 (17).
- Никогда не десериализуйте недоверенные данные (RCE через gadget chain). SpotBugs `SE_BAD_FIELD`.
- Полиморфная типизация Jackson (`enableDefaultTyping`) несёт тот же риск.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [java.util.stream (Java SE 25 API)](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/stream/package-summary.html) | Oracle | Семантика потоков: невмешательство, отсутствие состояния |
| [Collectors (Java SE 25 API)](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/stream/Collectors.html) | Oracle | Каталог коллекторов |
| [Gatherers (Java SE 25 API)](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/stream/Gatherers.html) | Oracle | Встроенные gatherers |
| [The Stream API (dev.java)](https://dev.java/learn/api/streams/) | Oracle DevRel | Туториал по потокам |
| [The Collections Framework (dev.java)](https://dev.java/learn/api/collections-framework/) | Oracle DevRel | Туториал по коллекциям |
| [Java Generics FAQ](http://www.angelikalanger.com/GenericsFAQ/JavaGenericsFAQ.html) | Langer | FAQ по generics (стирание, wildcards) |
| [What is PECS (Producer Extends Consumer Super)? (Stack Overflow)](https://stackoverflow.com/questions/2723397/what-is-pecs-producer-extends-consumer-super) | SO | PECS `[не проверено]` (403 для ботов) |
| [Lesson: Generics (The Java Tutorials)](https://docs.oracle.com/javase/tutorial/java/generics/index.html) | Oracle | Туториал по generics |
| [java.time (Java SE 25 API)](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/time/package-summary.html) | Oracle | Замысел java.time |
| [Joda-Time](https://www.joda.org/joda-time/) | Colebourne | «Users are now asked to migrate to java.time» |
| [BigDecimal (Java SE 25 API)](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html) | Oracle | equals и compareTo, округление |
| [JavaMoney/jsr354-ri (Moneta)](https://github.com/JavaMoney/jsr354-ri) | JavaMoney | Moneta, референсная реализация JSR 354 |
| [SLF4J User Manual](https://www.slf4j.org/manual.html) | Gülcü | Руководство SLF4J 2, включая fluent API |
| [SLF4J FAQ](https://www.slf4j.org/faq.html) | Gülcü | FAQ по производительности параметризованного логирования |
| [Logback: Mapped Diagnostic Context](https://logback.qos.ch/manual/mdc.html) | QOS.ch | MDC |
| [JEP 290: Filter Incoming Serialization Data](https://openjdk.org/jeps/290) | Seligman, 2016 | Фильтрация сериализации |
| [JEP 415: Context-Specific Deserialization Filters](https://openjdk.org/jeps/415) | Seligman, 2021 | Контекстные фильтры десериализации |
| [Serialization Filtering (Java SE 25)](https://docs.oracle.com/en/java/javase/25/core/serialization-filtering1.html) | Oracle | Руководство по настройке фильтров |
| [Towards Better Serialization](https://openjdk.org/projects/amber/design-notes/towards-better-serialization) | Goetz, 2019 | Почему records сериализуются лучше |
| [JEP 400: UTF-8 by Default](https://openjdk.org/jeps/400) | Reinhold, 2021 | UTF-8 по умолчанию |

---

## Data-oriented programming и ADT

**Ключевые выводы**

**DOP (Goetz, InfoQ 2022; серия Parlog «DOP v1.1», inside.java 2024).** Принципы v1.1:

1. Моделируйте данные неизменяемо и прозрачно (records).
2. Моделируйте данные, все данные и только данные (один record на понятие; sealed-типы для альтернатив).
3. Делайте недопустимые состояния непредставимыми (sealed + records + проверки в компактных конструкторах; без enum «тип» и nullable-полей).
4. Отделяйте операции от данных (функции с pattern matching над иерархией; валидация на границе).

DOP дополняет ООП: ООП — для границ и компонентов с состоянием и инвариантами; DOP — для простых данных, текущих через службы, парсеры, сообщения, результаты.

```java
sealed interface PaymentResult permits Approved, Declined, Failed {}
record Approved(String authCode, Instant at) implements PaymentResult {}
record Declined(String reason) implements PaymentResult {}
record Failed(Exception cause, boolean retryable) implements PaymentResult {}

String describe(PaymentResult r) {
    return switch (r) {
        case Approved(var code, _)        -> "ok " + code;
        case Declined(var why)            -> "declined: " + why;
        case Failed(_, var retry) when retry -> "retry later";
        case Failed f                      -> "failed: " + f.cause().getMessage();
    };
}
```

**Sealed switch и Visitor.** Visitor (двойная диспетчеризация) был обходом отсутствия sum-типов в Java. Полный `switch` над sealed-иерархией даёт то же свойство «компилятор покажет каждое место обновления» с гораздо меньшим кодом, поддерживает вложенные паттерны и не требует заранее предвидеть операции. Оставляйте Visitor только когда иерархия открыта для третьих сторон или очень велика и диспетчеризация по узлам должна быть виртуальной (например, старые библиотеки AST). **Expression problem** никуда не делась: sealed switch — легко добавлять операции, трудно типы; полиморфные методы — наоборот. Выбирайте по оси изменений.

**DI без фреймворков.** Конструкторная инъекция и рукописный composition root (`main` или record `AppContext`, связывающий всё) хватает небольшим и средним приложениям и CLI. Классы остаются свободными от фреймворка и тестируемыми (Bloch, пункт 5). Если нужна сгенерированная связка на этапе компиляции: Dagger 2, Avaje Inject (без reflection, подходит GraalVM). Избегайте инъекции в поля, service locator и статического `getInstance()` в доменном коде. Spring/Quarkus/Micronaut — в [architecture.md](architecture.md) и [domains-server.md](domains-server.md).

**Наследование и композиция.** Предпочитайте композицию и интерфейсы (Bloch, пункт 18). Делайте классы `final`, если они не проектировались для расширения; `sealed` для закрытых иерархий; избегайте глубоких иерархий и «базовых» классов ради повторного использования кода (PMD `GodClass` / `CouplingBetweenObjects` помогают найти последствия). Default-методы нужны для эволюции интерфейса, не для mixin-состояния (интерфейсы не хранят состояние). Приватные методы интерфейсов (9) разделяют код между default-методами.

**Анемичная доменная модель (спорно).** Фаулер считает её антипаттерном для богатых доменов; в стиле DOP «данные плюс отдельные функции» намеренны. Правило: инварианты живут вместе с данными (компактные конструкторы, фабрики); рабочие процессы живут в службах; избегайте сеттеров, позволяющих любому коду ломать инварианты.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Data Oriented Programming in Java](https://www.infoq.com/articles/data-oriented-programming-java/) | Goetz, 2022 | Манифест DOP: records, sealed, patterns |
| [Data Oriented Programming in Java, v1.1: Introduction](https://inside.java/2024/05/23/dop-v1-1-introduction/) | Parlog, 2024 | DOP v1.1, часть 1 |
| [DOP v1.1: Immutable, Transparent Data](https://inside.java/2024/05/27/dop-v1-1-immutable-transparent-data/) | Parlog, 2024 | Неизменяемые прозрачные данные |
| [DOP v1.1: Model the Data, the Whole Data](https://inside.java/2024/05/29/dop-v1-1-model-data/) | Parlog, 2024 | Моделируйте все данные |
| [DOP v1.1: Make Illegal States Unrepresentable](https://inside.java/2024/06/03/dop-v1-1-illegal-states/) | Parlog, 2024 | Недопустимые состояния непредставимы |
| [DOP v1.1: Separate Operations from Data](https://inside.java/2024/06/05/dop-v1-1-separate-operations/) | Parlog, 2024 | Отделение операций (против visitor) |
| [DOP v1.1: Wrap-up](https://inside.java/2024/06/10/dop-v1-1-wrap-up/) | Parlog, 2024 | Итоги |
| [DOP v1.1: Why the Update](https://inside.java/2024/06/26/dop-v1-1-why-update/) | Parlog, 2024 | Зачем понадобилась версия 1.1 |
| [Pattern Matching for Java](https://openjdk.org/projects/amber/design-notes/patterns/pattern-matching-for-java) | Goetz, 2017 | Обоснование pattern matching |
| [Pattern Matching (Java SE 25 Language Updates)](https://docs.oracle.com/en/java/javase/25/language/pattern-matching.html) | Oracle | Руководство по pattern matching (JDK 25) |
| [Sealed Classes (Java SE 25 Language Updates)](https://docs.oracle.com/en/java/javase/25/language/sealed-classes-and-interfaces.html) | Oracle | Руководство по sealed-классам |
| [Using Record to Model Immutable Data](https://dev.java/learn/records/) | Oracle DevRel | Туториал по records |
| [Strategy in Java (Refactoring Guru)](https://refactoring.guru/design-patterns/strategy/java/example) | Shvets | Классический strategy для сравнения |
| [Visitor in Java (Refactoring Guru)](https://refactoring.guru/design-patterns/visitor/java/example) | Shvets | Классический visitor для сравнения |
| [AnemicDomainModel](https://martinfowler.com/bliki/AnemicDomainModel.html) | Fowler, 2003 | Критика анемичной модели |
| [Primitive Obsession](https://refactoring.guru/smells/primitive-obsession) | Shvets | Primitive obsession → records-значения |
| [The "Double-Checked Locking is Broken" Declaration](https://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html) | Pugh et al. | Декларация «DCL сломан» |
| [Initialization-on-demand holder idiom](https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom) | Wikipedia | Идиома ленивого holder-класса |
| [Dagger](https://dagger.dev/) | Google | DI на этапе компиляции |
| [Avaje Inject](https://avaje.io/inject/) | Avaje | DI без reflection |
| [Expression problem](https://en.wikipedia.org/wiki/Expression_problem) | Wikipedia | Фон по expression problem |

---

## Мини-каталог паттернов

**Ключевые выводы**

- Большинство поведенческих паттернов GoF сворачиваются в функции. Колонка «Источник» указывает, где подробнее: первая ссылка — классический вариант, после неё — раздел этого файла или внешняя статья.
- Таблица «задача → решение в современной Java → источник»:

| Задача | Решение в современной Java | Источник |
|---|---|---|
| Взаимозаменяемые алгоритмы (Strategy) | Функциональный интерфейс или лямбда: `Comparator`, `Function<Order, Money> pricing`; либо strategy enum; внедрять через конструктор | [Refactoring Guru: Strategy](https://refactoring.guru/design-patterns/strategy/java/example); Bloch, пункты 34, 42 |
| Операция как объект (Command) | `Runnable`/`Supplier`/собственный `@FunctionalInterface`; records для сериализуемых команд; sealed `Command` и switch для интерпретаторов | Goetz, DOP (раздел «Data-oriented programming и ADT») |
| Скелет алгоритма (Template method) | Функция высшего порядка: меняющийся шаг передаётся лямбдой; template method оставлять для фреймворков | Bloch, пункт 18 |
| Операции над иерархией (Visitor) | Sealed interface + records + полный switch | [Parlog: separate operations](https://inside.java/2024/06/05/dop-v1-1-separate-operations/); [Refactoring Guru: Visitor](https://refactoring.guru/design-patterns/visitor/java/example) для сравнения |
| Подписка на события (Observer) | Слушатели `Consumer<Event>`; `Flow` и реактивные библиотеки; event bus во фреймворках; следить за утечками слушателей | [concurrency-native.md](concurrency-native.md) |
| Создание объектов (Factory method / Abstract factory) | Статические фабрики (`of`, `from`, `parse`), `Supplier<T>`, ссылки на конструктор `Foo::new`; `ServiceLoader` для плагинов | Bloch, пункт 1 |
| Сложное построение (Builder) | Record + статические фабрики; builder при многих необязательных параметрах; RecordBuilder; в будущем `with` (JEP 468) | [Randgalt/record-builder](https://github.com/Randgalt/record-builder); Bloch, пункт 2 |
| Единственный экземпляр (Singleton) | Enum-singleton или область видимости DI; ленивый holder-класс; никогда DCL без `volatile` (SpotBugs `DC_DOUBLECHECK`, PMD `DoubleCheckedLocking`, Error Prone `DoubleCheckedLocking`); Lazy Constants после финализации | [Pugh: DCL](https://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html); [holder idiom](https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom) |
| Обёртка с поведением (Decorator) | Классы остаются для интерфейсов (`InputStream`); либо композиция функций `f.andThen(g)`, цепочки `UnaryOperator` | JDK API |
| Приведение интерфейса (Adapter) | Лямбда или ссылка на метод, адаптирующая к функциональному интерфейсу | — |
| Конечный автомат (State) | Sealed interface из records-состояний + функция перехода на `switch`, возвращающая следующее состояние (неизменяемо); либо enum с таблицей переходов (`EnumMap`) | Bloch, пункт 37 |
| Цепочка обработчиков (Chain of responsibility) | `List<Function<Req, Optional<Resp>>>` или композиция `Predicate` | — |
| Пустой объект (Null object) | `Optional`, пустые коллекции или sealed-вариант `None` | раздел «Null и Optional» |
| Ожидаемые отказы домена (Result/Either, не GoF) | Sealed `Result<T>` с records `Ok`/`Err`; не оборачивать всё подряд | раздел «Исключения и ресурсы»; спор в «Спорные вопросы» |
| Выбор реализации без фреймворка (DI) | Конструкторная инъекция + рукописный composition root; Dagger 2 или Avaje Inject при желании сгенерированной связки | [Dagger](https://dagger.dev/); [Avaje Inject](https://avaje.io/inject/) |
| Группа связанных констант с поведением | Enum с полями-лямбдами, `EnumMap` вместо индексации по `ordinal()` | Bloch, пункты 34–37 |
| Значение предметной области вместо примитива (Primitive obsession) | Маленький record `Email`, `OrderId`, `Money` с проверкой в компактном конструкторе | [Refactoring Guru: primitive obsession](https://refactoring.guru/smells/primitive-obsession) |
| Лениво вычисляемая константа | Holder-класс сегодня; `LazyConstant` после финализации (сейчас preview) | раздел «Версии языка: Java 17–28» |

- Каталоги для сопоставления с классикой: [iluwatar/java-design-patterns](https://github.com/iluwatar/java-design-patterns), [Refactoring Guru: GoF на Java](https://refactoring.guru/design-patterns/java). Это каталоги, не справочники идиом.

---

## Идиомы безопасности

**Ключевые выводы**

- **Проверяйте на границе, внутри доверяйте типизированным данным.** Парсите в records и типы-значения, чьи компактные конструкторы обеспечивают инварианты («parse, don't validate»); allow-list вместо deny-list; ограничение длины до regex. В фреймворках — Bean Validation (Jakarta Validation 3.x).
- **SQL: всегда `PreparedStatement` с плейсхолдерами `?`.** Никогда не склеивайте и не форматируйте через `formatted()` пользовательские данные в SQL, в том числе внутри text blocks. Идентификаторы (таблица, столбец, направление сортировки) параметризовать нельзя: сопоставляйте с enum из allow-list. JPA/JPQL: именованные параметры, не строки. Find Security Bugs `SQL_INJECTION_JDBC`; SpotBugs `SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE`, `SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING`.
  ```java
  try (var ps = conn.prepareStatement("SELECT id, name FROM users WHERE email = ?")) {
      ps.setString(1, email);
      try (var rs = ps.executeQuery()) { ... }
  }
  ```
- **Десериализация**: никогда `ObjectInputStream.readObject` на недоверенных байтах; при необходимости allow-list фильтр JEP 290/415 (`ObjectInputFilter.Config.createFilter("com.acme.dto.*;java.base/*;!*")`). Jackson: никакой default typing и `@JsonTypeInfo(use = CLASS)` на недоверенном вводе (FSB `OBJECT_DESERIALIZATION`, `JACKSON_UNSAFE_DESERIALIZATION`).
- **XML (XXE)**: парсеры JDK по умолчанию разрешают внешние сущности ради совместимости. Настройте каждую фабрику:
  ```java
  var dbf = DocumentBuilderFactory.newInstance();
  dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);   // лучше всего: вообще без DTD
  dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
  dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
  dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
  dbf.setXIncludeAware(false); dbf.setExpandEntityReferences(false);
  ```
  То же для `SAXParserFactory`, `XMLInputFactory` (`IS_SUPPORTING_EXTERNAL_ENTITIES=false`, `SUPPORT_DTD=false`), `TransformerFactory`, `SchemaFactory`. JDK 23 поставляет `$JAVA_HOME/conf/jaxp-strict.properties.template` (включается `-Djava.xml.config.file=...`) для проверки более строгих умолчаний; JDK 24 отключил расширяющие функции XSLT/XPath по умолчанию `[не проверено: JDK-8331016, JDK-8343002]`. FSB `XXE_DOCUMENT`/`XXE_SAXPARSER`/`XXE_XMLREADER`; правило Sonar по XXE `[не проверено]`.
- **Обход пути**: сначала resolve, потом проверка вхождения:
  ```java
  Path base = uploadsDir.toRealPath();
  Path target = base.resolve(userName).normalize();
  if (!target.startsWith(base)) throw new SecurityException("path escapes base");
  ```
  Учитывайте симлинки (`toRealPath` после создания) и zip-slip при распаковке архивов. FSB `PATH_TRAVERSAL_IN`.
- **Случайность**: `SecureRandom` (или `SecureRandom.getInstanceStrong()` для долгоживущих ключей) для токенов, идентификаторов, nonce, паролей; `java.util.Random`/`ThreadLocalRandom`/`Math.random` только для небезопасных применений (FSB `PREDICTABLE_RANDOM`). `UUID.randomUUID()` использует SecureRandom.
- **Секреты**: никогда в исходниках, логах, сообщениях исключений и `toString`. Records печатают все компоненты: переопределяйте `toString` для records с секретами или заверните значение в тип `Secret` с маскированием. Загружайте из окружения или хранилища секретов. `char[]` для паролей — слабая мера (строки неизменяемы, частично интернируются), актуально главным образом для `Console.readPassword`. FSB `HARD_CODE_PASSWORD`/`HARD_CODE_KEY`, PMD `HardCodedCryptoKey`, SpotBugs `DMI_CONSTANT_DB_PASSWORD`.
- **Криптография**: высокоуровневые API и библиотеки (Tink); AES-GCM со случайным 12-байтовым IV; без ECB (FSB `ECB_MODE`), без статического IV (`STATIC_IV`), без MD5/SHA-1 для безопасности (`WEAK_MESSAGE_DIGEST_MD5`); пароли через Argon2, bcrypt или PBKDF2. Новые API JDK: KDF (JEP 510, final в 25), PEM encodings (final в 28, JEP 542), ML-KEM/ML-DSA (JEP 496/497, JDK 24).
- **ReDoS**: избегайте вложенных квантификаторов (`(a+)+`, `(.*a){n}`) на недоверенном вводе; ограничивайте длину ввода; притяжательные квантификаторы и атомарные группы (`a++`, `(?>...)`); `Pattern` компилируйте один раз как `static final`. FSB `REDOS`.
- **Инъекция в логи и Log4Shell (CVE-2021-44228)**: Log4j 2 до 2.14.1 включительно выполнял JNDI-подстановки для `${jndi:...}` в логируемых *данных*. Выводы: держите библиотеки логирования обновлёнными (Log4j не ниже 2.17.1 в линии 2.x), никогда не позволяйте логгеру интерпретировать пользовательские данные (message lookups убраны), логируйте ввод как параметры, нейтрализуйте CR/LF (FSB `CRLF_INJECTION_LOGS`), ведите SBOM и сканирование зависимостей (см. [toolchain.md](toolchain.md)).
- **Инъекция команд**: `ProcessBuilder(List.of("convert", in, out))` со списком аргументов; никогда `Runtime.exec(String)` из пользовательского ввода и `sh -c` с пользовательскими данными (FSB `COMMAND_INJECTION`).
- **Security Manager ушёл (JEP 486)**: изоляция недоверенного кода внутри процесса в Java больше невозможна; используйте изоляцию ОС и контейнеров.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [XML External Entity Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html) | OWASP | Настройки XXE для каждого парсера Java |
| [Deserialization Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Deserialization_Cheat_Sheet.html) | OWASP | Защита от десериализации в Java |
| [SQL Injection Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/SQL_Injection_Prevention_Cheat_Sheet.html) | OWASP | Параметризованные запросы |
| [Query Parameterization Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Query_Parameterization_Cheat_Sheet.html) | OWASP | Примеры для JDBC/JPA/Hibernate |
| [Input Validation Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Input_Validation_Cheat_Sheet.html) | OWASP | Валидация по allow-list |
| [Injection Prevention in Java Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Injection_Prevention_in_Java_Cheat_Sheet.html) | OWASP | Предотвращение инъекций в Java |
| [Logging Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html) | OWASP | Что логировать и что нет |
| [Secrets Management Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html) | OWASP | Работа с секретами |
| [Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html) | OWASP | Хеширование паролей |
| [Regular expression Denial of Service - ReDoS](https://owasp.org/www-community/attacks/Regular_expression_Denial_of_Service_-_ReDoS) | OWASP | ReDoS |
| [Path Traversal](https://owasp.org/www-community/attacks/Path_Traversal) | OWASP | Обход пути |
| [Apache Log4j Security Vulnerabilities](https://logging.apache.org/log4j/2.x/security.html) | Apache | Бюллетени Log4Shell и исправленные версии |
| [CVE-2021-44228](https://nvd.nist.gov/vuln/detail/CVE-2021-44228) | NIST | CVE Log4Shell |
| [Find Security Bugs: Bug Patterns](https://find-sec-bugs.github.io/bugs.htm) | FSB | Шаблоны уязвимостей с исправлениями |
| [Java API for XML Processing (JAXP) Security Guide](https://docs.oracle.com/en/java/javase/25/security/java-api-xml-processing-jaxp-security-guide.html) | Oracle | Руководство по безопасности JAXP |
| [JDK-8331016](https://bugs.openjdk.org/browse/JDK-8331016) | OpenJDK | Строгий шаблон JAXP `[не проверено]` |
| [Tink](https://developers.google.com/tink) | Google | Криптография, устойчивая к неверному использованию |
| [SecureRandom (Java SE 25 API)](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/security/SecureRandom.html) | Oracle | Использование SecureRandom |

---

## Антипаттерны

**Ключевые выводы**

- Названия правил: Error Prone (EP) — с errorprone.info/bugpatterns, «exp» означает experimental и выключено по умолчанию; PMD 7; SpotBugs/FSB (Find Security Bugs). **ID правил Sonar взяты по памяти и вторичным источникам и помечены `[не проверено]`**: сверяйте на rules.sonarsource.com перед публикацией (сайт не загрузился при проверке, это JS-приложение, curl его не читает).
- Столбец «правило анализатора» собирает EP / SpotBugs-FSB / PMD / Sonar в одну ячейку; «—» значит, что такого правила среди проверенных нет.

| Антипаттерн | Чем плох | Правило анализатора | Что вместо |
|---|---|---|---|
| Широкий `catch (Exception/Throwable)` | Скрывает ошибки, глотает прерывания | EP CatchingUnchecked (exp); SpotBugs REC_CATCH_EXCEPTION; PMD AvoidCatchingGenericException; Sonar S1181, S2221 [не проверено] | Ловить конкретные типы; широко только на верхней границе |
| Пустой или проглоченный catch | Тихий отказ | EP EmptyCatch, CatchFail; SpotBugs DE_MIGHT_IGNORE; PMD EmptyCatchBlock; Sonar S108 [не проверено] | Обработать, перебросить с причиной либо комментарий и `_` |
| `e.printStackTrace()` и потерянная причина | Нет логов, потерян контекст | EP CatchAndPrintStackTrace; PMD AvoidPrintStackTrace, PreserveStackTrace; Sonar S1148, S1166 [не проверено] | Логгер с исключением в аргументе; оборачивать с причиной |
| Проглоченный `InterruptedException` | Ломает отмену | EP InterruptedExceptionSwallowed (exp); Sonar S2142 [не проверено] | Пробросить или `Thread.currentThread().interrupt()` |
| Возврат `null` вместо пустой коллекции или массива | NPE ниже по стеку | SpotBugs PZLA_PREFER_ZERO_LENGTH_ARRAYS; PMD ReturnEmptyCollectionRatherThanNull; Sonar S1168 [не проверено] | Пустая коллекция |
| `Optional` как поле или параметр, null-Optional | Лишает смысла | EP NullOptional, OptionalNotPresent; SpotBugs NP_OPTIONAL_RETURN_NULL; Sonar S3553, S2789 [не проверено] | Перегрузки или nullable-параметр; никогда null-Optional |
| `Boolean`, возвращающий null | NPE при распаковке | SpotBugs NP_BOOLEAN_RETURN_NULL; Sonar S2447 [не проверено] | Enum или Optional |
| Изменяемое статическое состояние | Скрытая связность, гонки | SpotBugs MS_SHOULD_BE_FINAL, MS_MUTABLE_COLLECTION, ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD; PMD MutableStaticState; Sonar S1444, S2696, S2386 [не проверено] | Неизменяемые константы; состояние инжектировать |
| Раскрытие внутреннего изменяемого состояния | Ломает инварианты | EP MutablePublicArray; SpotBugs EI_EXPOSE_REP, EI_EXPOSE_REP2; PMD MethodReturnsInternalArray, ArrayIsStoredDirectly; Sonar S2384 [не проверено] | Защитные копии, `List.copyOf` |
| DCL без `volatile` | Частично сконструированные объекты | EP DoubleCheckedLocking; SpotBugs DC_DOUBLECHECK, LI_LAZY_INIT_STATIC; PMD DoubleCheckedLocking, NonThreadSafeSingleton; Sonar S2168 [не проверено] | Holder-идиома, enum, `volatile`, Lazy Constants |
| `==` на строках и боксах | Идентичность, зависит от кэша | EP ReferenceEquality, BoxedPrimitiveEquality; SpotBugs ES_COMPARING_STRINGS_WITH_EQ, RC_REF_COMPARISON; PMD UseEqualsToCompareStrings, CompareObjectsWithEquals; Sonar S4973, S1698 [не проверено] | `equals` / `Objects.equals` |
| `equals` без `hashCode` | Ломает хеш-коллекции | EP EqualsHashCode; SpotBugs HE_EQUALS_NO_HASHCODE, HE_EQUALS_USE_HASHCODE; PMD OverrideBothEqualsAndHashcode; Sonar S1206 [не проверено] | Оба метода или record |
| `equals`, бросающий на чужих типах | Нарушение контракта | EP EqualsIncompatibleType, EqualsGetClass; SpotBugs BC_EQUALS_METHOD_SHOULD_WORK_FOR_ALL_OBJECTS; Sonar S2097 [не проверено] | Паттерн `instanceof` |
| Массивы в records и `equals` на массивах | Сравнение по ссылке | EP ArrayRecordComponent, ArrayEquals, ArrayHashCode, ArrayToString; Sonar S2116 [не проверено] | `Arrays.equals`, компоненты-списки |
| Raw types | Потеря типобезопасности | javac `-Xlint:rawtypes`; Sonar S3740 [не проверено] | Generics |
| Коллекция с несовместимым типом ключа | Поиск всегда false | EP CollectionIncompatibleType; SpotBugs EC_UNRELATED_TYPES; Sonar S2175 [не проверено] | Исправить типы |
| `java.util.Date`, `Calendar`, `SimpleDateFormat` | Изменяемы, не потокобезопасны | EP JavaUtilDate, JavaTimeDefaultTimeZone; PMD ReplaceJavaUtilDate; Sonar S2143 [не проверено] | java.time |
| `Vector`, `Hashtable`, `Stack`, `StringBuffer` | Устаревшая синхронизация, медленно | EP JdkObsolete; PMD ReplaceVectorWithList, ReplaceHashtableWithMap, AvoidStringBufferField; Sonar S1149 [не проверено] | ArrayList / HashMap / ArrayDeque / StringBuilder |
| `new BigDecimal(double)`, `BigDecimal.equals` | Неверные значения, зависимость от scale | EP BigDecimalLiteralDouble, BigDecimalEquals; SpotBugs DMI_BIGDECIMAL_CONSTRUCTED_FROM_DOUBLE; PMD AvoidDecimalLiteralsInBigDecimalConstructor; Sonar S2111 [не проверено] | `valueOf` или String; `compareTo` |
| Кодировка и локаль по умолчанию | Зависит от платформы | EP DefaultCharset, StringCaseLocaleUsage; SpotBugs DM_DEFAULT_ENCODING; PMD UseLocaleWithCaseConversions; Sonar S1449 [не проверено] | `UTF_8`, `Locale.ROOT` |
| Незакрытые ресурсы | Утечки | EP StreamResourceLeak, MustBeClosedChecker; SpotBugs OS_OPEN_STREAM, OBL_UNSATISFIED_OBLIGATION; PMD CloseResource; Sonar S2095 [не проверено] | try-with-resources |
| Игнорируемое возвращаемое значение (`trim()`, `Future`) | Потерян результат | EP ReturnValueIgnored, CheckReturnValue, FutureReturnValueIgnored; SpotBugs RV_RETURN_VALUE_IGNORED; Sonar S2201 [не проверено] | Использовать или убрать |
| Переопределение `finalize` | Deprecated for removal | javac `[removal]`; SpotBugs CT_CONSTRUCTOR_THROW (смежное); Sonar S1113 [не проверено] | try-with-resources / Cleaner |
| God class, длинные методы, много параметров | Не поддерживается | PMD GodClass, TooManyMethods, ExcessiveParameterList, CognitiveComplexity, NPathComplexity; Sonar S1448, S107, S3776 [не проверено] | Делить по ответственности, объекты-параметры (records) |
| Глубокое наследование ради повторного использования | Хрупкий базовый класс | PMD CouplingBetweenObjects; Sonar S110 [не проверено] | Композиция, `final` / `sealed` |
| Primitive obsession (`String email`, `long userId`) | Перепутанные аргументы, нет валидации | — | Маленькие records: `record Email(String value) {...}` |
| Анемичная модель (спорно) | Инварианты размазаны | PMD DataClass | Инварианты в конструкторе и фабриках |
| Switch по enum с `default` или без части case | Тихие пропуски | EP MissingCasesInEnumSwitch, StatementSwitchToExpressionSwitch; PMD NonExhaustiveSwitch, ImplicitSwitchFallThrough; Sonar S131 [не проверено] | Полное switch-выражение |
| `System.out` в библиотечном коде | Вывод не настраивается | EP SystemOut (exp); PMD SystemPrintln; Sonar S106 [не проверено] | Логгер |
| Склейка SQL строками | Инъекция | SpotBugs SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE, FSB SQL_INJECTION_JDBC; Sonar S2077, S3649 [не проверено] | PreparedStatement |
| Умолчания XML-парсера | XXE | FSB XXE_DOCUMENT, XXE_SAXPARSER; Sonar S2755 [не проверено] | Настроить фабрики |
| `java.util.Random` для секретов | Предсказуемо | FSB PREDICTABLE_RANDOM, SpotBugs DMI_RANDOM_USED_ONLY_ONCE; Sonar S2245 [не проверено] | SecureRandom |
| Зашитые учётные данные | Утечка | FSB HARD_CODE_PASSWORD, SpotBugs DMI_CONSTANT_DB_PASSWORD; PMD HardCodedCryptoKey; Sonar S2068, S6437 [не проверено] | Хранилище секретов или окружение |
| Непроверенные пользовательские данные в логах | Подделка логов | FSB CRLF_INJECTION_LOGS; Sonar S5145 [не проверено] | Параметризованное логирование, нейтрализация CR/LF |
| Изменяемые поля у enum | Глобальное изменяемое состояние | EP ImmutableEnumChecker | Неизменяемые поля enum |
| `synchronized` на value-based и j.u.c объектах | Сломается под Valhalla / бессмысленно | javac `-Xlint:synchronization`; SpotBugs JLM_JSR166_UTILCONCURRENT_MONITORENTER; Sonar S3436 [не проверено] | Отдельный объект-замок |

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Error Prone Bug Patterns](https://errorprone.info/bugpatterns) | Google | Полный каталог Error Prone |
| [SpotBugs Bug Descriptions](https://spotbugs.readthedocs.io/en/latest/bugDescriptions.html) | SpotBugs | Описания багов SpotBugs |
| [PMD Java Rules](https://pmd.github.io/pmd/pmd_rules_java.html) | PMD | Индекс правил PMD 7 для Java |
| [Java static code analysis (Sonar rules)](https://rules.sonarsource.com/java/) | SonarSource | Правила Sonar для Java `[не проверено]`: ID выше не подтверждены, сайт — JS-приложение, curl его не читает |
| [NullAway](https://github.com/uber/NullAway) | Uber | Контроль null-безопасности |
| [Code Smells (Refactoring Guru)](https://refactoring.guru/refactoring/smells) | Shvets | Соответствие smell → рефакторинг |

---

## Спорные вопросы

Позиции и условия, при которых выигрывает каждая сторона.

- **Checked-исключения.** *За*: компилятор принуждает обработать восстановимые отказы (Bloch, пункты 70–71; Goetz отмечает, что полнота sealed-switch даёт похожую страховку). *Против*: трение с лямбдами и потоками, утечка типов реализации через сигнатуры, хрупкость версий (Хейлсберг). **Практика**: checked только когда вызывающий реально может восстановиться и должен решить (ввод-вывод на границах); в остальном unchecked доменные исключения; sealed-типы `Result` для ожидаемых исходов в DOP-коде. «Switch по исключениям» ещё нет (исследовался в Amber, но не выпущен) `[не проверено]`.
- **Lombok.** *За*: убирает шаблонный код для JPA-сущностей, builders, `@Slf4j`, DI через `@RequiredArgsConstructor`. *Против*: цепляется за внутренности javac (нужен новый релиз под каждый JDK: 1.18.40 для JDK 25, 1.18.46 для 26, 1.18.48 для 27, по changelog), скрывает код от читателей и инструментов, `@Data` на сущностях ломает equals/hashCode с ленивыми связями, `@SneakyThrows` скрывает checked-исключения. **Практика**: records заменяют DTO на `@Value`/`@Data`; статические фабрики и RecordBuilder заменяют большинство `@Builder`; при использовании Lombok ограничьте его allow-list'ом (`lombok.config`: `lombok.data.flagUsage = error`), обновляйте до смены JDK. Annotation processing: javac с JDK 23 не запускает процессоры из classpath неявно, и это проверено локально на JDK 25 (Windows 11): процессор, найденный только в classpath, молча не работает. Настройте `annotationProcessorPaths` (maven-compiler-plugin) или `-proc:full` (см. строку в таблице устареваний и [toolchain.md](toolchain.md)).
- **Optional.** Только тип возврата (Goetz) или «использовать везде» (FP-стиль). Консенсус: тип возврата; не поля, не параметры, не коллекции. Цепочки `Optional` в порядке; `get()` нет.
- **`var`.** Применять, когда тип очевиден из правой части или имени; избегать для результатов методов с неочевидным типом и для числовых литералов; никогда `var` вместе с diamond. Команды расходятся; Google style молчит; эталон — LVTI guide OpenJDK.
- **`final` везде.** Поля: да (неизменяемость, безопасная публикация). Локальные переменные и параметры: шум для многих команд, раз есть effectively final; часть кодовых баз навязывает Checkstyle `FinalParameters`/`FinalLocalVariable`. Классы: `final` (или `sealed`), если не проектировались для расширения. Фреймворки с прокси-классами (Spring CGLIB, ленивые прокси Hibernate, Mockito без inline) требуют не-final.
- **Геттеры/сеттеры и records.** Records для данных; JavaBeans только там, где их требует фреймворк (JPA-сущности, привязка в некоторых UI). Не конструируйте значения через сеттеры. «Tell, don't ask» остаётся верным для доменных объектов с состоянием.
- **Потоки и циклы.** Потоки для преобразований и агрегаций; циклы для кода с тяжёлым control flow, исключениями или индексами и когда профилирование показало накладные расходы в горячих путях (см. [performance.md](performance.md)). Сначала читаемость.
- **Интерфейсы для всего (`FooService` + `FooServiceImpl`).** Не нужны при Mockito, records и sealed; вводите интерфейс при двух и более реализациях или на границе модуля.
- **Анемичная и богатая доменная модель.** DOP легитимирует «данные плюс функции»; DDD предпочитает поведение на агрегатах. Обе стороны согласны: обеспечивайте инварианты при конструировании.
- **`null`, JSpecify и Optional.** Сходятся на JSpecify `@NullMarked` плюс NullAway в библиотеках; Optional для возвращаемых типов; `!`/`?` на уровне языка — через годы (draft).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Style Guidelines for Local Variable Type Inference in Java](https://openjdk.org/projects/amber/LVTIstyle.html) | Marks, 2018 | Рекомендации по стилю `var` |
| [LVTI FAQ](https://openjdk.org/projects/amber/guides/lvti-faq) | Goetz/Marks, 2019 | FAQ по `var` |
| [Lombok Changelog](https://projectlombok.org/changelog) | Lombok | Поддержка JDK по релизам |
| [Lombok Configuration System](https://projectlombok.org/features/configuration) | Lombok | Ограничения через `lombok.config` (flagUsage) |
| [Java Record vs. Lombok](https://www.baeldung.com/java-record-vs-lombok) | Baeldung | Сравнение records и Lombok |
| [Record Classes (Java SE 25 Language Updates)](https://docs.oracle.com/en/java/javase/25/language/records.html) | Oracle | Руководство по records |

---

## Чеклист code review: идиоматичная Java

Целевые версии JDK 21 и 25. Отмечайте пункт, если проверка пройдена или неприменима.

**Уровень языка и возможности**

- [ ] 1. Цель — LTS (`--release 21` или `25`); в поставляемом коде нет `--enable-preview`.
- [ ] 2. Носители данных — records; массивы и изменяемые коллекции в компонентах record копируются (`List.copyOf`) или не используются.
- [ ] 3. Закрытые альтернативы — `sealed` интерфейсы + records; в switch над ними **нет `default`**.
- [ ] 4. `instanceof` использует паттерны; нет приведения после проверки.
- [ ] 5. Новые switch в стрелочной форме `->` и полные; `case null` обработан явно там, где возможен null.
- [ ] 6. `var` только когда тип очевиден; нигде `var x = new ArrayList<>()`.
- [ ] 7. Неиспользуемые параметры catch, лямбд и компоненты паттернов названы `_` (22+).
- [ ] 8. Text blocks для многострочных литералов; не для сборки SQL/HTML из пользовательских данных.
- [ ] 9. `list.getFirst()/getLast()/reversed()` вместо арифметики индексов (21+).
- [ ] 10. Нет `finalize`, `Thread.stop`, Security Manager, доступа к памяти через `sun.misc.Unsafe`, записи в `final`-поля через reflection.
- [ ] 11. Нет синхронизации на value-based объектах и сравнения их через `==` (боксы, `Optional`, `LocalDate`).

**Проектирование**

- [ ] 12. Классы `final` или `sealed`, если не проектировались для расширения; композиция вместо наследования.
- [ ] 13. Конструкторная инъекция; нет инъекции в поля и статических service locator в доменном коде.
- [ ] 14. Статические фабрики со смысловыми именами там, где это проясняет (`of`, `from`, `parse`); builder только при многих необязательных параметрах.
- [ ] 15. Поля `private final`; нет публичной изменяемой статики; константы глубоко неизменяемы.
- [ ] 16. Предметные примитивы обёрнуты в маленькие records (`Email`, `OrderId`, `Money`) с проверкой в компактном конструкторе.
- [ ] 17. Вариации strategy/command/template выражены лямбдами или enum, а не россыпью классов.

**Null и контракты**

- [ ] 18. Nullness публичного API объявлена (пакет `@NullMarked` + `@Nullable` от JSpecify) и проверяется NullAway или IDE.
- [ ] 19. `Objects.requireNonNull` на входах конструкторов и API; пустые коллекции, не `null`.
- [ ] 20. `Optional` только как тип возврата; нет `get()`, вместо него `orElseThrow`, `map`, `orElseGet`.
- [ ] 21. `equals`/`hashCode` переопределены вместе (или record); `compareTo` согласован с equals либо это задокументировано.
- [ ] 22. Компараторы строятся через `Comparator.comparing(...).thenComparing(...)`; нет сравнения вычитанием.

**Коллекции и потоки**

- [ ] 23. Осознанный выбор между `toList()` (неизменяем, null допустим), `Collectors.toList()` и `toUnmodifiableList()`.
- [ ] 24. У `toMap` есть функция слияния, если ключи могут совпасть; значения не null.
- [ ] 25. Нет изменения внешнего состояния в лямбдах потока; нет `peek` для логики; `parallel()` только с бенчмарками.
- [ ] 26. Потоки ввода-вывода (`Files.lines/walk/list`) закрыты через try-with-resources.
- [ ] 27. В сигнатурах типы-интерфейсы (`List`, `Map`); по умолчанию нет `Vector`/`Hashtable`/`StringBuffer`/`LinkedList`.

**Ошибки и ресурсы**

- [ ] 28. Нет широкого `catch (Exception|Throwable)` кроме границ; нет пустого catch без комментария.
- [ ] 29. Исключения обёрнуты с причиной на нужном уровне абстракции; нет «лог и rethrow»; нет `printStackTrace`.
- [ ] 30. `InterruptedException` проброшен или флаг восстановлен.
- [ ] 31. Каждый `AutoCloseable` (включая `ExecutorService`, `HttpClient`) в try-with-resources либо с ясным владельцем.

**Текст, время, деньги**

- [ ] 32. Явные `StandardCharsets.UTF_8` и `Locale.ROOT` для машинных строк.
- [ ] 33. Только java.time; `Clock` внедрён; нет `Date`/`Calendar`/`SimpleDateFormat`; нет неявной зоны по умолчанию.
- [ ] 34. Деньги в `BigDecimal` (из String или `valueOf`) с явным `RoundingMode`, сравнение через `compareTo`; арифметика, чувствительная к переполнению, через `Math.*Exact`.

**Логирование и безопасность**

- [ ] 35. Параметризованные сообщения SLF4J (`{}`) или fluent API; исключение последним аргументом; нет секретов и персональных данных; MDC очищается.
- [ ] 36. SQL через `PreparedStatement` или именованные параметры; идентификаторы из allow-list.
- [ ] 37. XML-фабрики защищены от XXE; нет Java-десериализации недоверенных данных (если неизбежна, то с фильтрами).
- [ ] 38. Пути от пользователя нормализованы и проверены через `startsWith(base)`; записи zip тоже.
- [ ] 39. `SecureRandom` для токенов и ключей; нет зашитых секретов; `toString` у record не печатает секреты.
- [ ] 40. Регулярные выражения на недоверенном вводе проверены на катастрофический откат; длина ввода ограничена.

**Гейты инструментов** (настройка — в [toolchain.md](toolchain.md)): компиляция с `-Xlint:all -Werror` (или выбранным набором), Error Prone + NullAway, SpotBugs + Find Security Bugs, PMD/Checkstyle (google_checks или правила команды), форматтер (google-java-format, Palantir или Spotless) принудительно в CI. Сквозной процесс ревью описан в [pipeline.md](pipeline.md).

---

## Что перепроверить

- ID правил Sonar в таблице антипаттернов: rules.sonarsource.com не загрузился, ID взяты по памяти. Проверьте каждый перед публикацией.
- Получат ли Lazy Constants или примитивные паттерны JEP в JDK 28 до Rampdown Phase One (2026-12-03).
- Точное содержимое `jaxp-strict.properties.template` и изменение умолчания JDK 24 для расширяющих функций XSLT/XPath.
- Номер релиза, в котором изменено умолчание annotation processing: поведение на JDK 25 проверено локально (см. таблицу устареваний), ссылка на release note JDK 23 остаётся `[не проверено]`.
- Четвёртое издание Effective Java не найдено; перепроверьте страницы издателя.
- То, что `Thread.stop` бросает UOE начиная с JDK 20, взято по памяти; проверено только, что он бросает на 25.
