# Тестирование Java-кода

Как писать, организовывать и ревьюить тесты для современной Java (JDK 17–27, LTS 25) в сборках Maven и Gradle: JUnit 6 и Jupiter, проверки (AssertJ), тестовые двойники (Mockito 5), интеграционные тесты (Testcontainers, тестовая поддержка Spring Boot и Quarkus), property-based, снапшот- и контрактные тесты, покрытие (JaCoCo), мутационное тестирование (PIT), асинхронные и нестабильные тесты, раскладка тестов в проекте, правила для ИИ-агента и чеклист ревью. Актуально на ноябрь 2026: JDK 25 LTS, JDK 27 — последняя версия; JUnit 6.1.3, AssertJ 3.27.7, Mockito 5.24.0, Testcontainers 2.0.5, JaCoCo 0.8.16, PIT 1.30.2, Surefire 3.6.1. Метки: без метки — источник или версия проверены; `[не проверено]` — единственный вторичный источник или страница не открывалась; `[устарело]` — заменено тем, что названо рядом; «проверено локально (JDK 25, Windows 11)» — воспроизведено на Temurin 25.0.5+8, Maven 3.9.16, Gradle 9.8.2. Глубина по сборке и статическому анализу — в [toolchain.md](toolchain.md), JMH и бенчмарки — в [performance.md](performance.md), jcstress и виртуальные потоки — в [concurrency-native.md](concurrency-native.md), ArchUnit — в [architecture.md](architecture.md), внутренности Spring и Quarkus — в [domains-server.md](domains-server.md), порядок работы агента — в [pipeline.md](pipeline.md), книги — в [books.md](books.md).

- [Стек по умолчанию](#стек-по-умолчанию)
- [Версии на ноябрь 2026](#версии-на-ноябрь-2026)
- [1. JUnit 6 и Jupiter](#1-junit-6-и-jupiter)
- [2. Проверки: AssertJ](#2-проверки-assertj)
- [3. Тестовые двойники: Mockito 5](#3-тестовые-двойники-mockito-5)
- [4. Интеграционные тесты](#4-интеграционные-тесты)
- [5. Property-based, снапшоты и контракты](#5-property-based-снапшоты-и-контракты)
- [6. Покрытие и мутационное тестирование](#6-покрытие-и-мутационное-тестирование)
- [7. Асинхронные и нестабильные тесты](#7-асинхронные-и-нестабильные-тесты)
- [8. Организация тестов](#8-организация-тестов)
- [9. Книги и ссылки](#9-книги-и-ссылки)
- [10. Журнал локальной проверки](#10-журнал-локальной-проверки)
- [Правила для ИИ-агента, пишущего тесты](#правила-для-ии-агента-пишущего-тесты)
- [Чеклист ревью тестов](#чеклист-ревью-тестов)

## Стек по умолчанию

| Задача | По умолчанию | Альтернатива | Не брать |
|---|---|---|---|
| Фреймворк тестов | JUnit 6.1.x (Jupiter), один BOM `org.junit:junit-bom` | TestNG 7.12.0 для существующих наборов | JUnit 4 с Vintage-движком в новом коде; смешивание версий JUnit |
| Проверки | AssertJ 3.27.x | Google Truth 1.4.5; встроенные `assertAll`/`assertThrows` | Hamcrest в новом коде; AssertJ 4.0.0-M1 (milestone); три стиля проверок в одной кодовой базе |
| Тестовые двойники | реальные объекты и фейки (in-memory, `Clock.fixed`, лямбда) | Mockito 5.24.0 на границах, которыми вы владеете; MockK 1.14.11 для Kotlin | `mockito-inline` (артефакт [устарело], инлайн-режим встроен в `mockito-core` 5.x); мок класса под тестом; `RETURNS_DEEP_STUBS` |
| HTTP-заглушки | WireMock 3.13.2 (`org.wiremock:wiremock`) | MockServer 9.2.0; `MockRestServiceServer` в Spring | `com.github.tomakehurst:wiremock-jre8` [устарело], замена — `org.wiremock:wiremock`; WireMock 4.0.0-beta в продакшен-сборке |
| Интеграция с БД и брокерами | Testcontainers 2.0.5 с тем же движком и версией, что в продакшене | Dev Services в Quarkus; Docker Compose для локальной разработки | H2 для проверки SQL, специфичного для вендора; тег `latest` у образа |
| Spring-тесты | Узкий срез (`@WebMvcTest`, `@DataJpaTest`), `@MockitoBean` | `@SpringBootTest` с `RestTestClient` для сквозных сценариев | `@MockBean`/`@SpyBean` (в Spring Boot 4 удалены); `@DirtiesContext` без причины |
| Ожидание асинхронного результата | Awaitility 4.3.0, `succeedsWithin` для `CompletableFuture` | Заменяемый `Executor` (прямой исполнитель в юнит-тестах) | `Thread.sleep` |
| Время и случайность | Внедряемые `Clock`/`InstantSource` и `RandomGenerator` с seed | `MutableClock` из threeten-extra | `Instant.now()` и `new Random()` в доменном коде |
| Покрытие | JaCoCo 0.8.16, порог по строкам и по веткам | Отчёт-агрегатор для многомодульных проектов | Порог только по строкам; цель 100 % |
| Мутационные тесты | PIT 1.30.2 + `pitest-junit5-plugin` 1.2.3 на ключевой логике | Descartes 1.3.4 (экстремальные мутации) для больших кодовых баз | Полный прогон PIT на каждый PR |
| Property-based тесты | Решает пользователь (см. [раздел 5](#5-property-based-снапшоты-и-контракты)) | jetCheck 0.3.0; Jazzer 0.30.0 для фаззинга | jqwik, добавленный агентом по собственной инициативе |
| Снапшоты | ApprovalTests.Java 31.0.0 | Selfie 3.1.2 | Массовое одобрение снапшотов без чтения diff |
| Контрактные тесты | Pact JVM 4.7.7 (consumer-driven) | Spring Cloud Contract 5.0.3 (provider-driven) | Ручные «контракты» в виде общих JSON-файлов без проверки |
| Тестовые данные | Test Data Builder с валидными значениями по умолчанию | Instancio 6.2.1, Datafaker 2.7.0 с зафиксированным seed | Огромные общие JSON/SQL-фикстуры |
| Нестабильные тесты | Перезапуск, который отчитывается о флейках (Surefire `rerunFailingTestsCount`, Gradle `org.gradle.test-retry`) | JUnit Pioneer `@RetryingTest` как временная мера | Тихие повторы без отчёта; `@Disabled` без тикета |

## Версии на ноябрь 2026

Версии библиотек взяты из `maven-metadata.xml` на Maven Central (`<release>`) и с портала плагинов Gradle на 6 ноября 2026.

| Компонент | Версия, дата | Примечание |
|---|---|---|
| JUnit (Platform + Jupiter + Vintage, единый номер с 6.0) | 6.1.3 (7 августа 2026) | 6.0.0 GA — 30 сентября 2025; 6.1.0 — 19 мая 2026. Базовая Java 17, Kotlin 2.1 |
| JUnit 4 (`junit:junit`) | 4.13.2 (2021) | Только сопровождение; запускается через Vintage или мигрирует |
| AssertJ core | 3.27.7; 4.0.0-M1 (январь 2026) | В продакшене 3.27.x; 4.x — milestone `[не проверено]` в деталях |
| Hamcrest | 3.0 (август 2024) | Почти наследие |
| Google Truth | 1.4.5 (сентябрь 2025) | |
| Mockito (`mockito-core`, `mockito-junit-jupiter`) | 5.24.0 (23 сентября 2026) | Инлайн-режим по умолчанию с 5.0; тянет Byte Buddy 1.17.x |
| mockito-kotlin | 6.4.0 | |
| `mockito-inline` | 5.2.0 (2023), последняя | [устарело] — инлайн встроен в `mockito-core` 5.x, артефакт убрать |
| MockK | 1.14.11 (май 2026) | Kotlin |
| WireMock (`org.wiremock:wiremock`) | 3.13.2; 4.0.0-beta.41 (21 октября 2026) | 4.x ещё в бете |
| MockServer (`mockserver-netty`) | 9.2.0 (26 октября 2026) | Проект возобновлён в 2025–2026 |
| Testcontainers | 2.0.5 (20 апреля 2026) | 2.0.0 (14 октября 2025): убран JUnit 4, артефакты переименованы в `testcontainers-<module>` |
| Testcontainers 1.x | 1.21.4 (15 декабря 2025), последняя | Единственное исправление: работа с Docker Engine 29 |
| jqwik | 1.10.2 (10 октября 2026) | См. ограничение автора в [разделе 5](#5-property-based-снапшоты-и-контракты) |
| ApprovalTests.Java | 31.0.0 (июнь 2026) | |
| Selfie (`selfie-runner-junit5`) | 3.1.2 (2 ноября 2026) | |
| Pact JVM (`consumer:junit5`) | 4.7.7 (3 ноября 2026) | |
| Spring Cloud Contract verifier | 5.0.3 (июнь 2026) | |
| ArchUnit (`archunit-junit5`) | 1.5.1 (сентябрь 2026) | |
| Instancio (`instancio-junit`) | 6.2.1 (2 ноября 2026) | |
| Datafaker | 2.7.0 (июнь 2026) | |
| REST Assured | 6.3.0 (23 октября 2026) | |
| JsonUnit AssertJ | 6.4.0 (22 октября 2026) | |
| Awaitility | 4.3.0 (февраль 2025) | |
| jcstress | 0.16 (2023) | Только ссылка, детали в [concurrency-native.md](concurrency-native.md) |
| JaCoCo | 0.8.16 (26 октября 2026) | 0.8.14 — официально Java 25; 0.8.15 — Java 26 (27 экспериментально); 0.8.16 — Java 27 |
| PIT (`pitest-maven`) | 1.30.2 (5 ноября 2026) | Нумерация прыгнула 1.25.9 → 1.29.10 → 1.30.x |
| pitest-junit5-plugin | 1.2.3 (май 2025) | Нужен для тестов на JUnit 5/6 |
| gradle-pitest-plugin (`info.solidsoft.pitest`) | 1.19.0 (29 марта 2026) | Только портал плагинов Gradle; копия на Maven Central кончается на 1.15.0 |
| Descartes | 1.3.4 (сентябрь 2025) | Движок экстремальных мутаций для PIT |
| maven-surefire / failsafe | 3.6.1 (26 октября 2026) | |
| Gradle test-retry (`org.gradle.test-retry`) | 1.6.6 (сентябрь 2026) | |
| TestNG | 7.12.0 (январь 2026) | |
| Spring Boot (`spring-boot-starter-test`) | 4.1.2 GA; 4.2.0-RC2 (5 ноября 2026) | Boot 4 = Spring Framework 7; управление JUnit 6 в Boot — `[не проверено]` |
| Quarkus (`quarkus-junit`) | 3.40.1 GA; 4.0.0.Beta3 (4 ноября 2026) | `quarkus-junit5` переименован в `quarkus-junit`, публикуются оба |
| Cucumber JUnit Platform engine | 8.0.4 | |
| JSpecify | 1.0.1 | JUnit 6 аннотирован им |

## 1. JUnit 6 и Jupiter

**Ключевые выводы**
- Актуальная линия — JUnit 6 (6.0.0 GA 30 сентября 2025, 6.1.3 от 7 августа 2026). «Jupiter» остаётся моделью программирования (`org.junit.jupiter.api.*`), поэтому статьи про «JUnit 5» по API ещё верны.
- С 6.0 Platform, Jupiter и Vintage имеют один номер версии. Подключайте BOM `org.junit:junit-bom:6.1.3` и не смешивайте версии.
- Ломающие изменения 6.0:
  - Базовая Java 17 и Kotlin 2.1.
  - Аннотации nullability JSpecify на всём API. IDE и NullAway могут показать новые предупреждения.
  - `junit-platform-runner` (`@RunWith(JUnitPlatform.class)`) удалён без замены.
  - `junit-platform-jfr` влит в launcher, `junit-platform-suite-commons` — в `junit-platform-suite`.
  - Surefire и Failsafe версии ниже 3.0.0 больше не поддерживаются.
  - `MethodOrderer.Alphanumeric` удалён, используйте `MethodName`.
  - `@CsvSource` и `@CsvFileSource` переведены на FastCSV: атрибут `lineSeparator` удалён, символы после закрывающей кавычки отклоняются.
  - `@TestMethodOrder` и `@TestClassOrder` теперь наследуются в `@Nested`-классах.
  - Vintage-движок объявлен устаревшим.
  - Запуск ConsoleLauncher без подкоманды убран.
  - Неверные значения enum в ключах конфигурации роняют прогон, а не игнорируются.
  - Kotlin `suspend`-методы тестов и жизненного цикла поддерживаются.
- Путь обновления: сначала перейти на Java 17+, затем поднять BOM. Руководство называет обновление почти заменой «на месте» (удалены только API, помеченные устаревшими более двух лет). `@EnabledForJreRange(min = JAVA_17)` стал лишним, `JRE.JAVA_8..JAVA_16` устарели, `Store.getOrComputeIfAbsent` заменён на `computeIfAbsent`.
- Новое в 6.1 (19 мая 2026): встроенные `@DefaultLocale`, `@DefaultTimeZone` и расширения для установки/сброса системных свойств (раньше были только в JUnit Pioneer), настраиваемая стратегия удаления `@TempDir`, модуль `org.junit.start` для компактных исходных файлов, режим выполнения для динамических тестов, экспериментальная очистка памяти для огромных наборов, новый параллельный исполнитель `worker_thread_pool`, константы `JRE.JAVA_27`/`JAVA_28`.
- Возможности 5.13/5.14, доступные в 6.x:
  - `@ParameterizedClass` и `@ClassTemplate` параметризуют целый класс, включая деревья `@Nested`.
  - `@SentenceFragment` для имён `IndicativeSentences`.
  - Отчёт о проблемах обнаружения тестов: неверно настроенный тест (например, `private` метод с `@Test`) попадает в отчёт, а не пропускается молча.
  - `@FieldSource` и `@AutoClose` (5.11) [не проверено].
- Жизненный цикл: по умолчанию новый экземпляр класса на каждый метод (`PER_METHOD`). Это нужная изоляция. `@TestInstance(PER_CLASS)` разрешает нестатический `@BeforeAll`, но делит состояние между тестами и, по документации, отключает параллельное выполнение класса по умолчанию, пока не задан `@Execution(CONCURRENT)`.
- Параметризованные тесты:
  - `@CsvSource` с шаблоном `name = "{0} x {1} -> {2}"` для небольших таблиц.
  - `@MethodSource`/`@FieldSource` для объектов.
  - `@EnumSource`, чтобы проверка охватывала все значения.
  - Ожидаемое значение лежит в строке данных, а не вычисляется той же логикой, что и в продакшене.
  - Тест на `@CsvSource` проверен локально (JDK 25, Windows 11).
- `@Nested` и `@DisplayName` дают читаемое дерево Given/When/Then. Вложенность — не глубже 2–3 уровней.
- Расширения: `@ExtendWith`, `@RegisterExtension` для программной настройки. Типичные: `MockitoExtension`, `SpringExtension`, Testcontainers, `@TempDir`. Свои пишите только для сквозной подготовки; состояние хранится в `Store`.
- Теги: `@Tag("slow")`, `@Tag("integration")`. Выбор: Gradle `useJUnitPlatform { includeTags("fast") }`, Surefire `<groups>fast</groups>` и `<excludedGroups>`. Выражения тегов поддерживают `&`, `|`, `!` [не проверено].
- Динамические тесты (`@TestFactory`, возвращает `Stream<DynamicNode>`) нужны для данных, найденных во время выполнения (файлы в каталоге). `@BeforeEach`/`@AfterEach` выполняются один раз на фабрику, а не на каждый динамический тест [не проверено].
- Gradle: при ручном объявлении Jupiter нужен `testRuntimeOnly("org.junit.platform:junit-platform-launcher")`. Блок `testing.suites { ... useJUnitJupiter("6.1.3") }` добавляет его сам (проверено локально, JDK 25, Windows 11).
- JUnit 4 и Vintage: Vintage объявлен устаревшим в 6.0. Мигрируйте (рецепт OpenRewrite `org.openrewrite.java.testing.junit5.JUnit4to5Migration` [не проверено]), не держите два движка.
- TestNG 7.12.0 жив. Выбирайте его только для существующих наборов или ради data provider и зависимостей между тестами. Spring, Quarkus, Testcontainers и большинство инструментов ориентированы на JUnit Platform.
- Kotlin: JUnit 6 запускает `suspend fun`-тесты напрямую; Kotest — альтернативный фреймворк [не проверено].

Параллельный запуск (`src/test/resources/junit-platform.properties`), проверено локально (JDK 25, Windows 11):

```properties
junit.jupiter.execution.parallel.enabled=true
junit.jupiter.execution.parallel.mode.default=concurrent
junit.jupiter.execution.parallel.mode.classes.default=concurrent
junit.jupiter.execution.parallel.config.strategy=dynamic
junit.jupiter.execution.parallel.config.dynamic.factor=1
# экспериментальная замена fork_join_pool по умолчанию (6.1+):
# junit.jupiter.execution.parallel.config.executor-service=worker_thread_pool
```

- Оба ключа `mode` по умолчанию равны `same_thread`, поэтому одного `enabled=true` недостаточно.
- Локальный замер: 8 тестов по 1 с завершились примерно за 1,6 с на потоках ForkJoinPool (32 ядра).
- Общее состояние защищайте `@ResourceLock(Resources.SYSTEM_PROPERTIES | SYSTEM_OUT | SYSTEM_ERR | LOCALE | TIME_ZONE | "my-db")`. Классы с глобальным состоянием помечайте `@Isolated`. Отдельные методы выводите из параллели через `@Execution(SAME_THREAD)`.
- Параллелизм — не ограничитель числа потоков: при `fork_join_pool` блокирующие вызовы могут порождать лишние потоки. Задайте `max-pool-size` или используйте `worker_thread_pool`.
- `forkCount`/`reuseForks` в Surefire (параллель на уровне JVM) лучше изолируют статическое состояние.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [JUnit User Guide](https://docs.junit.org/current/user-guide/) | JUnit team, 2026 (6.1.3) | Основное руководство |
| [JUnit Release Notes (current)](https://docs.junit.org/current/release-notes/) | JUnit team, 2026 | Изменения 6.1.x |
| [JUnit 6.0.0 Release Notes](https://docs.junit.org/6.0.0/release-notes/) | JUnit team, 2025 | Полный список ломающих изменений 6.0 |
| [JUnit 5.14.0 Release Notes](https://docs.junit.org/5.14.0/release-notes/) | JUnit team, 2025 | Возможности 5.13/5.14 (`@ParameterizedClass`) |
| [Upgrading to JUnit 6.0](https://github.com/junit-team/junit-framework/wiki/Upgrading-to-JUnit-6.0) | JUnit team, 2025 | Официальный чеклист обновления |
| [Parallel Execution](https://docs.junit.org/current/writing-tests/parallel-execution.html) | JUnit team, 2026 | Все ключи параллели, блокировки, `@Isolated` |
| [junit-team/junit-framework](https://github.com/junit-team/junit-framework) | JUnit team | Репозиторий (бывший junit5) и задачи |
| [junit-team/junit-examples](https://github.com/junit-team/junit-examples) | JUnit team | Примеры сборок Maven и Gradle |
| [JUnit 6: Java 17 and Kotlin baseline (InfoQ)](https://www.infoq.com/news/2025/10/junit6-java17-kotlin) | InfoQ, 2025 | Новостная сводка по 6.0 [не проверено] |
| [JUnit Pioneer](https://junit-pioneer.org/) | JUnit Pioneer team | Дополнительные расширения (переменные окружения, декартовы тесты) |
| [Guide to JUnit 5 (Baeldung)](https://www.baeldung.com/junit-5) | Baeldung | Быстрый обзор API (5.x, актуален) [не проверено] (сайт блокирует скриптовые проверки, HTTP 403) |
| [Guide to JUnit 5 Parameterized Tests (Baeldung)](https://www.baeldung.com/parameterized-tests-junit-5) | Baeldung | Источники параметров [не проверено] (HTTP 403) |
| [TestNG](https://testng.org/) | C. Beust et al. | Документация TestNG |
| [JUnit 4 to 5 migration recipe (OpenRewrite)](https://docs.openrewrite.org/recipes/java/testing/junit5/junit4to5migration) | Moderne / OpenRewrite | Автоматическая миграция JUnit 4 → 5/6 |

## 2. Проверки: AssertJ

**Ключевые выводы**
- По умолчанию берите AssertJ 3.27.x (`assertThat(x).isEqualTo(y)`): беглый API, подсказки IDE, подробные сообщения об ошибках, поддержка коллекций, `Optional`, исключений и рекурсивного сравнения.
- 4.0.0-M1 — milestone. Базовая Java 17, бинарная несовместимость с 3.x, все устаревшие API помечены `forRemoval`. В продакшен не брать.
- Исключения: `assertThatThrownBy(() -> ...).isInstanceOf(X.class).hasMessageContaining("...")` или `assertThatIllegalArgumentException().isThrownBy(...)`. `assertThrows` из JUnit возвращает исключение для дальнейших проверок. Конструкцию `try { ...; fail(); } catch (X e) {}` не писать. Проверено локально (JDK 25, Windows 11).
- Рекурсивное сравнение заменяет ручные проверки полей DTO [не проверено]:

```java
assertThat(actual).usingRecursiveComparison().ignoringFields("id", "createdAt").isEqualTo(expected);
```

- Мягкие проверки (soft assertions) показывают все упавшие проверки за один прогон. Применяйте их для многих полей одного результата, не для склейки несвязанных поведений [не проверено]:

```java
@ExtendWith(SoftAssertionsExtension.class)
class OrderMappingTest {
    @InjectSoftAssertions SoftAssertions softly;

    @Test void mapsAllFields() {
        softly.assertThat(dto.id()).isEqualTo(7);
        softly.assertThat(dto.totalCents()).isEqualTo(990);
    }
}
```

  Альтернативы: `SoftAssertions.assertSoftly(s -> { ... })` и встроенный в JUnit `assertAll(...)`.
- Собственные проверки (`extends AbstractAssert<OrderAssert, Order>`) окупаются для центральных доменных типов. Генератор AssertJ создаёт заготовки [не проверено]. Держите их маленькими и в языке домена: `assertThat(order).isPaid()`.
- Hamcrest 3.0 (`assertThat(x, is(equalTo(y)))`) — почти наследие. Он ещё встречается в `jsonPath(..., Matchers...)` у MockMvc и в телах REST Assured. Не смешивайте три стиля проверок в одной кодовой базе.
- Google Truth 1.4.5: похожий беглый стиль, меньший API, хорошие сообщения; подходит для кода на Guava.
- JSON и XML: JsonUnit-AssertJ 6.4.0 (`assertThatJson(json).isEqualTo("{...}")` с плейсхолдерами `${json-unit.ignore}`), JSONAssert (последняя 2.0-rc1), XMLUnit 2.14.0. Сравнивайте JSON структурно, не строками.
- Подвох для агента: `assertThat(x);` без завершающей проверки ничего не проверяет и компилируется. Аннотации `CheckReturnValue` (Error Prone, AssertJ) ловят это при включённом чекере [не проверено].

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [AssertJ documentation](https://assertj.github.io/doc/) | AssertJ team | Полное руководство по core (soft, recursive, custom) |
| [AssertJ releases](https://github.com/assertj/assertj/releases) | AssertJ team | Заметки 3.27.x и milestone 4.0 |
| [AssertJ Assertions Generator](https://joel-costigliola.github.io/assertj/assertj-assertions-generator.html) | J. Costigliola | Генератор собственных проверок (старый сайт) |
| [Truth](https://truth.dev/) | Google | Документация Truth |
| [Truth: Comparison](https://truth.dev/comparison) | Google | Truth против AssertJ и Hamcrest |
| [Hamcrest](https://hamcrest.org/JavaHamcrest/) | Hamcrest team | Справочник матчеров |
| [JsonUnit](https://github.com/lukas-krecan/JsonUnit) | L. Krecan | Проверки JSON, включая интеграцию с AssertJ |
| [XMLUnit](https://www.xmlunit.org/) | XMLUnit team | Сравнение XML |
| [Soft Assertions with AssertJ](https://www.baeldung.com/java-assertj-soft-assertions) | Baeldung | Мягкие проверки: `assertSoftly` и `SoftAssertions` с `assertAll()` |

## 3. Тестовые двойники: Mockito 5

**Ключевые выводы**
- Mockito 5.x (5.24.0, 23 сентября 2026): инлайн-режим включён по умолчанию с 5.0. Финальные классы и методы, `mockStatic` и `mockConstruction` работают без артефакта `mockito-inline` (его не берите; он остановился на 5.2.0).
- Свежие добавления: мокирование синглтонов, включая Kotlin `object` (5.22), автоопределение класса в `mockStatic` (5.21), API SpyStatic и подавление статических инициализаторов через агент (5.24). О Mockito 6 не объявлено.
- Предупреждение JEP 451 (JDK 21+). Mockito присоединяет агент Byte Buddy к работающей JVM. На JDK 25 это печатает `Mockito is currently self-attaching to enable the inline-mock-maker. This will no longer work in future releases of the JDK...`, затем `WARNING: A Java agent has been loaded dynamically ...` и `Dynamic loading of agents will be disallowed by default in a future release`. Проверено локально (JDK 25.0.5, Mockito 5.24.0).
- Исправление — загрузить Mockito как статический `-javaagent` (раздел 0.3 в javadoc Mockito). Флаг `-XX:+EnableDynamicAgentLoading` только прячет предупреждение и может перестать работать.

Maven, предупреждения исчезают (проверено локально, JDK 25, Windows 11):

```xml
<properties>
  <argLine/>  <!-- пустое значение по умолчанию: @{argLine} всегда разрешается -->
</properties>

<plugin>
  <artifactId>maven-dependency-plugin</artifactId>
  <executions>
    <execution>
      <goals><goal>properties</goal></goals>
    </execution>
  </executions>
</plugin>
<plugin>
  <artifactId>maven-surefire-plugin</artifactId>
  <configuration>
    <argLine>@{argLine} -javaagent:${org.mockito:mockito-core:jar}</argLine>
  </configuration>
</plugin>
```

- `@{argLine}` (позднее разрешение) сохраняет `argLine`, который выставляет `prepare-agent` у JaCoCo. Запись `${argLine}` потеряла бы его или уронила бы форк, если свойство не определено.
- Тот же `argLine` скопируйте в `maven-failsafe-plugin`.
- Версию агента не задавайте вторым числом: `${org.mockito:mockito-core:jar}` берёт тот же JAR, что лежит в тестовом classpath. Spring Boot управляет версией Mockito, поэтому жёстко прописанная версия разойдётся [не проверено].

Gradle Kotlin DSL (проверено локально, JDK 25, Windows 11):

```kotlin
val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
    mockitoAgent("org.mockito:mockito-core:5.24.0") { isTransitive = false }
}

tasks.withType<Test>().configureEach {
    jvmArgumentProviders.add(CommandLineArgumentProvider { listOf("-javaagent:${mockitoAgent.asPath}") })
}
```

PIT: миньоны не наследуют `argLine` Surefire. Добавьте агент в `jvmArgs` плагина `pitest-maven` (проверено локально, JDK 25, Windows 11):

```xml
<plugin>
  <groupId>org.pitest</groupId>
  <artifactId>pitest-maven</artifactId>
  <configuration>
    <jvmArgs>
      <value>-javaagent:${org.mockito:mockito-core:jar}</value>
    </jvmArgs>
  </configuration>
</plugin>
```

- Подвох: прямой вызов цели плагина (`mvn org.pitest:pitest-maven:mutationCoverage` или `mvn surefire:test`) пропускает `dependency:properties`. Свойство `${org.mockito:mockito-core:jar}` остаётся буквальным, JVM падает (`Error opening zip file or JAR manifest missing : ${org.mockito:mockito-core:jar}`, у PIT — `MINION_DIED`). Запускайте через фазу: `mvn test-compile org.pitest:pitest-maven:mutationCoverage`. Проверено локально.
- Остаточная строка `OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended` остаётся и со статическим агентом. Это сообщение CDS, оно безвредно.
- Строгие заглушки: `MockitoExtension` включает `Strictness.STRICT_STUBS`, неиспользованная заглушка роняет тест с `UnnecessaryStubbingException`. Исправляйте тест, не добавляйте `lenient()`.
- Предпочитайте `@Mock`/`@InjectMocks` с внедрением через конструктор. `@InjectMocks` молча ничего не делает, если внедрение не удалось, поэтому явное создание тестируемого объекта яснее [не проверено].
- Фейки и моки (общее мнение Fowler, Google SWE book, вики Mockito):
  - Для значений и чистой логики берите реальные объекты.
  - Для объектов с состоянием берите фейки: in-memory репозиторий, `Clock.fixed`, лямбда вместо интерфейса с одним методом.
  - Моки нужны только на границах, которыми вы владеете (порты и адаптеры), когда само взаимодействие и есть поведение (письмо отправлено один раз).
  - «Не мокируйте типы, которыми не владеете» (HTTP-клиенты, JDBC, `List`): оберните их своим портом и проверьте адаптер интеграционным тестом (WireMock, Testcontainers).
  - Признаки избыточного мокирования: `verify` на каждый вызов, моки, возвращающие моки (`RETURNS_DEEP_STUBS`), тесты, повторяющие реализацию построчно, мокирование значений, `mockStatic` на собственный код (лучше внедрить зависимость).
  - В пробном проекте лямбда-фейк `new PriceCalculator(country -> 20)` заменил мок и читался лучше (проверено локально).
- Захват и сопоставление аргументов: `ArgumentCaptor` с AssertJ или `assertArg(arg -> assertThat(arg)...)` (с 5.3) внутри `verify`. Не смешивайте сырые значения и матчеры в одном вызове (`InvalidUseOfMatchersException`).
- Kotlin: идиоматичен MockK 1.14.11 (`every { } returns`, `coEvery` для корутин); mockito-kotlin 6.4.0 — если проект построен вокруг Mockito. В Mockito 5.23 для Android нужен API устройства или эмулятора 28+.
- HTTP-заглушки: WireMock, стабильная линия 3.13.2 (`org.wiremock:wiremock`, вариант на Jetty 12 — `wiremock-jetty12`), 4.0.0-beta.41 ещё бета. Интеграция с JUnit через `@WireMockTest` или `@RegisterExtension WireMockExtension`; модуль Testcontainers есть (1.0-alpha-15). Старый `com.github.tomakehurst:wiremock-jre8` [устарело], замена — `org.wiremock:wiremock`. MockServer 9.2.0 — альтернатива с проверкой ожиданий и проксированием. В Spring-приложениях для юнит-тестов `RestClient` есть `MockRestServiceServer`.
- Не подменяйте тестируемый объект (`spy` на нём, чтобы сыграть половину класса). Разделите класс.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Mockito javadoc (latest)](https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html) | Mockito team, 2026 | Основная документация; раздел 0.3 — настройка агента |
| [Mockito releases](https://github.com/mockito/mockito/releases) | Mockito team, 2026 | Изменения 5.20–5.24 |
| [Mockito issue 3037](https://github.com/mockito/mockito/issues/3037) | Mockito team, 2023 | Исходное обсуждение динамического агента на JDK 21 |
| [JEP 451: Prepare to Disallow the Dynamic Loading of Agents](https://openjdk.org/jeps/451) | R. Pressler, A. Bateman, 2023 | Почему динамические агенты запретят |
| [How to write good tests (Mockito wiki)](https://github.com/mockito/mockito/wiki/How-to-write-good-tests) | Mockito team | «Не мокируйте типы, которыми не владеете» и другое |
| [Mocks Aren't Stubs](https://martinfowler.com/articles/mocksArentStubs.html) | M. Fowler, 2007 | Классический и мокистский подходы |
| [TestDouble](https://martinfowler.com/bliki/TestDouble.html) | M. Fowler, 2006 | Словарь: dummy, stub, spy, mock, fake |
| [Software Engineering at Google, ch. 13: Test Doubles](https://abseil.io/resources/swe-book/html/ch13.html) | Google, 2020 | Предпочитайте реальные объекты и фейки |
| [Testing on the Toilet: Don't Overuse Mocks](https://testing.googleblog.com/2013/05/testing-on-toilet-dont-overuse-mocks.html) | Google, 2013 | Пример избыточного мокирования |
| [MockK](https://mockk.io/) | MockK team | Мокирование для Kotlin |
| [WireMock documentation](https://wiremock.org/docs/) | WireMock team | Документация (v3 и v4 beta) |
| [WireMock: JUnit Jupiter](https://wiremock.org/docs/junit-jupiter/) | WireMock team | `@WireMockTest`, `WireMockExtension` |
| [MockServer](https://www.mock-server.com/) | J. Bloom et al. | Документация MockServer |
| [Spring: Testing Client Applications (MockRestServiceServer)](https://docs.spring.io/spring-framework/reference/testing/spring-mvc-test-client.html) | Spring team | `MockRestServiceServer` |

## 4. Интеграционные тесты

**Ключевые выводы**
- Testcontainers 2.0 (2.0.0 — 14 октября 2025; 2.0.5 — 20 апреля 2026):
  - Поддержка JUnit 4 удалена (нет контейнеров через `@Rule`/`@ClassRule`), используйте `testcontainers-junit-jupiter`.
  - Артефакты модулей получили префикс: `org.testcontainers:postgresql` → `org.testcontainers:testcontainers-postgresql`. Старые координаты заморожены на 1.21.4.
  - Классы контейнеров переехали в пакеты `org.testcontainers.<module>` (`org.testcontainers.postgresql.PostgreSQLContainer`, в новом пакете уже не generic).
  - Конструкторы модулей без аргументов убраны: всегда указывайте тег образа.
  - Подключайте BOM `org.testcontainers:testcontainers-bom:2.0.5`.
- Docker Engine 29 поднял минимальную версию клиентского API (до 1.44, позже снижена до 1.40 в 29.3 [не проверено]). Старые Testcontainers и docker-java падали с `client version 1.32 is too old`. Исправлено в 2.0.2 (API по умолчанию 1.44) и 2.0.3 (запасной вариант для старых движков); 1.21.4 переносит исправление. На 1.21.3 и старше обходной путь — `DOCKER_API_VERSION` или `DOCKER_MIN_API_VERSION` на демоне [не проверено]; настоящее решение — обновление.
- Жизненный цикл контейнеров:
  - `@Testcontainers` и статическое поле `@Container` — один контейнер на класс. Поле экземпляра — один на метод (медленно, нужно редко).
  - Паттерн singleton для всего набора: запуск в `static {}` абстрактного базового класса, `stop()` не вызывается, Ryuk удалит контейнер при выходе JVM. Не сочетайте с `@Container` на том же поле.
  - Spring Boot: объявляйте контейнеры как `@Bean` в `@TestConfiguration` (с `@ServiceConnection`) или через `@ImportTestcontainers`. Тогда они живут столько же, сколько кэшированный контекст приложения. Контейнер с областью класса может быть остановлен, пока кэшированный контекст на него ссылается («последующие тесты могут упасть»).
  - Reuse (`withReuse(true)` и `testcontainers.reuse.enable=true` в `~/.testcontainers.properties` либо `TESTCONTAINERS_REUSE_ENABLE=true`): экспериментально, только для локальной разработки, «не подходит для CI». Конфигурация должна совпадать точно, контейнер не останавливается. Через свойства в classpath не включается.
- Ryuk (сборщик ресурсов) чистит контейнеры и сети после смерти JVM. Отключайте (`TESTCONTAINERS_RYUK_DISABLED=true`) только там, где CI чистит сам. В некоторых средах нужен `ryuk.container.privileged=true`.
- Windows: работает с Docker Desktop (бэкенд WSL2) через именованный канал. Windows-контейнеры не поддерживаются; на Windows Server Docker не поддерживается.
- Если Docker не запущен, каждый тест падает с `IllegalStateException: Could not find a valid Docker environment` в `DockerClientProviderStrategy`. `@Testcontainers(disabledWithoutDocker = true)` превращает падение в пропуск локально. В CI сборка обязана падать при отсутствии Docker: пропущенный набор — тихая дыра. Ошибка и пропуск воспроизведены локально (Docker Desktop остановлен, клиент Engine 29.8.2).
- Теги образов фиксируйте (`postgres:17-alpine`), не `latest`. В CI подменяйте имена образов на зеркало реестра, чтобы не упираться в лимиты Docker Hub [не проверено].
- Настоящая БД вместо H2. Режимы совместимости H2 не эмулируют Postgres JSONB, `ON CONFLICT`, семантику оконных функций и `FILTER`, последовательности, блокировки, изоляцию и коды ошибок. Миграции Flyway/Liquibase, написанные под Postgres, часто падают или ведут себя иначе. Для тестов репозиториев и миграций берите Testcontainers с тем же движком и версией, что в продакшене. H2 оставьте разве что для простых smoke-тестов JPA [не проверено, несколько практических источников].
- Изменения тестирования в Spring Boot 4:
  - Модульные тестовые стартеры: `spring-boot-starter-webmvc-test`, `spring-boot-starter-data-jpa-test` и так далее (каждый тянет `spring-boot-starter-test`). Срезы лежат в модулях `spring-boot-webmvc-test` (`@WebMvcTest`), `spring-boot-data-jpa-test` (`@DataJpaTest`), `spring-boot-jdbc-test` (`@JdbcTest`), `spring-boot-restclient-test` (`@RestClientTest`). Пакеты переехали, например `org.springframework.boot.jdbc.test.autoconfigure`.
  - `@MockBean`/`@SpyBean` удалены, замена — `@MockitoBean`/`@MockitoSpyBean` из Spring Framework (только поля тестового класса, не на `@Configuration`).
  - `@SpringBootTest` больше не настраивает автоматически MockMvc, `WebTestClient` и `TestRestTemplate`. Добавьте `@AutoConfigureMockMvc`, `@AutoConfigureRestTestClient` (новый `RestTestClient`, рекомендуется) или `@AutoConfigureTestRestTemplate` (плюс `spring-boot-resttestclient`).
  - `MockMvcTester` даёт проверки MockMvc в стиле AssertJ: `assertThat(mvc.get().uri("/")).hasStatusOk()`.
  - На тестовый класс — одна аннотация среза. `@DataJpaTest` и `@JdbcTest` транзакционные и по умолчанию откатываются.
  - `@AutoConfigureTestDatabase.Replace.NON_TEST` оставляет тестовую БД как есть (контейнер с `@ServiceConnection`, URL из `@DynamicPropertySource`, URL `jdbc:tc:`) и заменяет остальное; `NONE` отключает замену. `NON_TEST` как значение по умолчанию — `[не проверено]`.
  - При `webEnvironment = RANDOM_PORT` `@Transactional` на тесте не откатывает работу сервера (другой поток), очищайте данные явно.
- Кэширование контекста — главный рычаг скорости Spring-тестов. Ключ кэша: конфигурационные классы, профили, источники свойств, кастомизаторы контекста (каждый отдельный набор `@MockitoBean`, `@DynamicPropertySource`, `@TestPropertySource` создаёт новый контекст). Размер кэша 32 (`spring.test.context.cache.maxSize`). `@DirtiesContext` вытесняет контекст. Отдельная JVM на каждый тестовый класс убивает кэш. Собирайте `@MockitoBean` в несколько базовых классов или мета-аннотаций.
- Quarkus (3.40.x GA, 4.0 в бете):
  - `@QuarkusTest` запускает приложение один раз за прогон, но каждый отдельный `@TestProfile` вызывает перезапуск. Держите профилей мало.
  - `@QuarkusIntegrationTest` проверяет собранный jar, нативный образ или контейнер под Failsafe, не в той же фазе, что `@QuarkusTest`.
  - `@InjectMock` требует `quarkus-junit-mockito`.
  - Dev Services сами запускают базы и брокеры в тестах, если URL не задан.
  - Порт HTTP в тестах — 8081 (`quarkus.http.test-port`).
  - Артефакт называется `quarkus-junit`, старый `quarkus-junit5` ещё публикуется.
- HTTP-уровень: REST Assured 6.3.0 или Spring `RestTestClient`/`WebTestClient`. Проверяйте статус, заголовки и структуру JSON, не полные строки тела.
- Обработка аннотаций на JDK 23+: javac по умолчанию больше не запускает процессоры из classpath. Тесты, которым нужен сгенерированный код (MapStruct, Immutables, AutoValue, Lombok, Dagger), не компилируются, если процессоры не объявлены явно (`<annotationProcessorPaths>` в maven-compiler-plugin, Gradle `annotationProcessor` и `testAnnotationProcessor`) или не передан `-proc:full`. Тестовые наборы исходников требуют своей настройки процессоров [не проверено; подробности — [toolchain.md](toolchain.md)].

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Testcontainers for Java](https://java.testcontainers.org/) | Testcontainers / Docker | Главная страница документации |
| [Testcontainers for Java releases](https://github.com/testcontainers/testcontainers-java/releases) | Testcontainers, 2025–26 | Ломающие изменения 2.0, исправления под Docker 29 |
| [Manual container lifecycle control](https://java.testcontainers.org/test_framework_integration/manual_lifecycle_control/) | Testcontainers | Контейнеры-синглтоны |
| [Reusable containers](https://java.testcontainers.org/features/reuse/) | Testcontainers | Ограничения reuse |
| [Custom configuration](https://java.testcontainers.org/features/configuration/) | Testcontainers | Ryuk, хост Docker, ключи свойств |
| [Windows support](https://java.testcontainers.org/supported_docker_environment/windows/) | Testcontainers | Поддержка и ограничения Windows |
| [Testcontainers Guides](https://testcontainers.com/guides/) | Docker / AtomicJar | Руководства по задачам (Spring Boot, Quarkus, Kafka) |
| [Spring Boot: Testcontainers](https://docs.spring.io/spring-boot/reference/testing/testcontainers.html) | Spring team, 2026 | `@ServiceConnection`, `@ImportTestcontainers` |
| [Spring Boot: Testing Spring Boot Applications](https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html) | Spring team, 2026 | Срезы, MockMvcTester, RestTestClient |
| [Spring Boot 4.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide) | Spring team, 2025 | Тестовые стартеры, `@MockitoBean`, изменения MockMvc |
| [AutoConfigureTestDatabase.Replace](https://docs.spring.io/spring-boot/api/java/org/springframework/boot/jdbc/test/autoconfigure/AutoConfigureTestDatabase.Replace.html) | Spring team | Семантика `NON_TEST` |
| [Spring TestContext: Context Caching](https://docs.spring.io/spring-framework/reference/testing/testcontext-framework/ctx-management/caching.html) | Spring team, 2026 | Ключ и размер кэша контекста |
| [Spring Boot issue 48104](https://github.com/spring-projects/spring-boot/issues/48104) | Spring Boot issue, 2025 | Docker 29 и Testcontainers 1.x [не проверено] |
| [Quarkus: Testing Your Application](https://quarkus.io/guides/getting-started-testing) | Quarkus team, 2026 | `@QuarkusTest`, профили, моки |
| [Quarkus: Dev Services](https://quarkus.io/guides/dev-services) | Quarkus team | Каталог Dev Services |
| [Quarkus: Continuous Testing](https://quarkus.io/guides/continuous-testing) | Quarkus team | Непрерывное тестирование в dev-режиме |
| [Don't use in-memory databases (H2, Derby, HSQLDB) for integration tests](https://phauer.com/2017/dont-use-in-memory-databases-tests-h2/) | P. Hauer, 2017 | Почему не H2 в тестах |
| [REST Assured](https://rest-assured.io/) | J. Haleby | Документация REST Assured |

## 5. Property-based, снапшоты и контракты

**Ключевые выводы**
- Property-based тестирование находит крайние случаи, которые пропускают примерные тесты: переполнение, пустой ввод, Unicode. Типичные свойства:
  - обратимость: `decode(encode(x)) == x`;
  - инвариант: отсортированный результат — перестановка исходного и упорядочен;
  - сравнение с эталоном: быстрая реализация против простой референсной;
  - идемпотентность.
- Падающий seed всегда логируйте и фиксируйте.

**Ограничение jqwik для ИИ-агентов**
- jqwik 1.10.2 (10 октября 2026) — устоявшийся движок property-based тестов на JVM (`@Property` и `@ForAll`, 1000 попыток по умолчанию, ограниченное сжатие примера до 10 с, `afterFailure = SAMPLE_FIRST` по умолчанию, `@Property(seed = "...")` для воспроизведения).
- Руководство пользователя jqwik содержит с версии 1.10 «Anti-AI Usage Clause». Автор пишет, что jqwik «is not meant to be used by any AI coding agents», и при каждом запуске печатает в stdout уведомление (скрывается свойством `jqwik.hideAntiAiClause`).
- Агент не добавляет jqwik в проект по собственной инициативе. Если нужны property-based тесты, агент сообщает пользователю об этом ограничении, и решает пользователь.
- Если проект уже использует jqwik, это решение людей: продолжайте в том же стиле и упомяните оговорку пользователю, когда предлагаете расширить применение.
- При проверке для этого справочника jqwik намеренно не запускался.

**Совместимость jqwik с JUnit 6**
- jqwik построен на JUnit Platform 1.x (минимум 1.14.4). Примечания к релизу называют 1.10.1 «вероятно, последним» на Platform 1.x, а будущие релизы «если вообще состоятся» нацелены на Platform 6 и Java 21+.
- Совместимость с лаунчером JUnit 6.x в документации не описана. Её нужно проверять в своей сборке до внедрения. Здесь она не проверялась.

**Прочее**
- Альтернативы:
  - jetCheck 0.3.0 (JetBrains, июль 2026, небольшой API).
  - junit-quickcheck 1.0 (2020) и QuickTheories 0.26 (2019) заброшены.
  - Jazzer 0.30.0 (`@FuzzTest`, фаззинг по покрытию поверх JUnit) для парсеров и обработки небезопасного ввода.
  - Instancio 6.2.1 и Datafaker 2.7.0 генерируют случайные тестовые данные. Это не property-based тесты (нет сжатия примера), но с зафиксированным seed (`@Seed`, `Settings`) полезны.
- Снапшот-тесты и approval-тесты фиксируют сложный вывод (JSON, отчёты, сгенерированный код) и показывают diff при изменении:
  - ApprovalTests.Java 31.0.0: `Approvals.verify(result)` пишет `*.received.txt` и сравнивает с зафиксированным в репозитории `*.approved.txt`. Подходит для характеризации унаследованного кода («golden master»).
  - Selfie 3.1.2: встроенные литеральные снапшоты (`expectSelfie(x).toBe("...")`, `toBe_TODO()` переписывает исходник) или снапшоты на диске (`toMatchDisk()`, файлы `.ss`). Режим `readonly` включается по умолчанию при `CI=true` и падает, если остались маркеры `_TODO`. `-Dselfie=overwrite` переписывает всё. Документация перечисляет JUnit 5, Vintage и Kotest; JUnit 6 не упомянут.
  - Снапшоты детерминированы (сортируйте мапы, фиксируйте часы и идентификаторы), малы и ревьюятся как код. Агент не одобряет и не перезаписывает снапшоты пакетно ради зелёной сборки, не прочитав diff.
- Контрактные тесты для границ сервисов:
  - Pact JVM 4.7.7 (consumer-driven): тесты потребителя создают pact-файлы, провайдер их проверяет, Pact Broker/PactFlow распространяет и отвечает на вопрос «можно ли выкатывать».
  - Spring Cloud Contract 5.0.3 (provider-driven): контракты на Groovy, YAML или Kotlin DSL порождают тесты провайдера и заглушки WireMock для потребителей; ориентирован на Spring.
  - Альтернатива на основе OpenAPI: проверка ответов по схеме в интеграционных тестах (например, swagger-request-validator) [не проверено].
- Архитектурные тесты: ArchUnit 1.5.1 (`archunit-junit5`) кодирует слои, циклы и правила именования как юнит-тесты. Существующие нарушения замораживайте через `FreezingArchRule`. Подробности — в [architecture.md](architecture.md).
- BDD: Cucumber JVM 8.0.4 (`cucumber-junit-platform-engine`) нужен, когда спецификации читают не разработчики. В остальных случаях дешевле `@Nested` и `@DisplayName` в формате Given/When/Then на обычном JUnit.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [jqwik User Guide](https://jqwik.net/docs/current/user-guide.html) | J. Link, 2026 | Руководство jqwik, включая оговорку об ИИ |
| [jqwik Release Notes](https://jqwik.net/release-notes.html) | J. Link, 2026 | Заметки 1.10.x, планы по Platform 1.x и 6 |
| [How to Specify It! (jqwik version)](https://johanneslink.net/how-to-specify-it/) | J. Link (по J. Hughes), 2020 | Какие свойства писать (Java-версия статьи Hughes) |
| [Choosing properties for property-based testing](https://fsharpforfunandprofit.com/posts/property-based-testing-2/) | S. Wlaschin, 2014 | Выбор свойств (не привязано к языку) |
| [jetCheck](https://github.com/JetBrains/jetCheck) | JetBrains | Небольшая библиотека property-based тестов |
| [Jazzer](https://github.com/CodeIntelligenceTesting/jazzer) | Code Intelligence | Фаззинг по покрытию с JUnit |
| [Instancio](https://www.instancio.org/) | Instancio team | Случайные и структурированные тестовые данные |
| [Datafaker](https://www.datafaker.net/) | Datafaker team | Реалистичные фиктивные данные |
| [ApprovalTests.Java](https://github.com/approvals/ApprovalTests.Java) | L. Falco et al. | Approval-тесты для Java |
| [Selfie: Get started (JVM)](https://lib.selfie.dev/jvm/get-started) | DiffPlug, 2026 | Снапшот-тесты Selfie |
| [Pact: JVM implementation guide](https://docs.pact.io/implementation_guides/jvm) | Pact Foundation | Руководство Pact JVM |
| [Pact Docs](https://docs.pact.io/) | Pact Foundation | Понятия consumer-driven контрактов |
| [Spring Cloud Contract Reference](https://docs.spring.io/spring-cloud-contract/reference/) | Spring team | Справочник Spring Cloud Contract |
| [Consumer-Driven Contracts: A Service Evolution Pattern](https://martinfowler.com/articles/consumerDrivenContracts.html) | I. Robinson, 2006 | Происхождение consumer-driven контрактов |
| [ArchUnit User Guide](https://www.archunit.org/userguide/html/000_Index.html) | TNG | Руководство ArchUnit (глубина — в [architecture.md](architecture.md)) |
| [Cucumber: Installation for Java](https://cucumber.io/docs/installation/java/) | Cucumber team | Установка Cucumber JVM |

## 6. Покрытие и мутационное тестирование

**Ключевые выводы**
- Версия JaCoCo должна знать уровень вашего байткода: 0.8.12 — официально Java 22; 0.8.13 — Java 23–24; 0.8.14 (11 октября 2025) — Java 25; 0.8.15 (4 июня 2026) — Java 26 (27 экспериментально); 0.8.16 (24 октября 2026) — Java 27 (28 экспериментально). Старый агент на новых class-файлах падает с `Unsupported class file major version` или молча выдаёт пустые отчёты. Поднимайте JaCoCo при каждом обновлении JDK. Для классов загрузчика платформы в 0.8.16 нужна опция агента `inclbootstrapclasses=true`.
- Maven: цели `prepare-agent`, `report`, `check`. `prepare-agent` записывает свойство `argLine`, поэтому любой собственный `argLine` у Surefire должен использовать `@{argLine}`. Для Failsafe берите `prepare-agent-integration` и `report-integration`; для многомодульных проектов — отдельный модуль-агрегатор с `report-aggregate` (зависит от всех модулей). Проверено локально (JaCoCo 0.8.16, JDK 25).
- Gradle: плагин `jacoco`, задачи `jacocoTestReport` и `jacocoTestCoverageVerification { violationRules { ... } }`. Для многомодульных проектов плагин `jacoco-report-aggregation` создаёт `testCodeCoverageReport` по имени набора (работает с JVM Test Suites; запускайте с `--continue`, чтобы отчёты появлялись и при падении тестов).

Порог JaCoCo по строкам и по веткам, Maven (проваливает `verify`; числа подберите под проект):

```xml
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <version>0.8.16</version>
  <executions>
    <execution>
      <id>prepare</id>
      <goals><goal>prepare-agent</goal></goals>
    </execution>
    <execution>
      <id>report</id>
      <phase>verify</phase>
      <goals><goal>report</goal></goals>
    </execution>
    <execution>
      <id>check</id>
      <phase>verify</phase>
      <goals><goal>check</goal></goals>
      <configuration>
        <rules>
          <rule>
            <element>BUNDLE</element>
            <limits>
              <limit>
                <counter>LINE</counter>
                <value>COVEREDRATIO</value>
                <minimum>0.80</minimum>
              </limit>
              <limit>
                <counter>BRANCH</counter>
                <value>COVEREDRATIO</value>
                <minimum>0.75</minimum>
              </limit>
            </limits>
          </rule>
        </rules>
      </configuration>
    </execution>
  </executions>
</plugin>
```

Тот же порог в Gradle Kotlin DSL (проверено локально, Gradle 9.8.2, JDK 25: при пороге веток 0,90 и покрытии 3/4 `check` падает с «branches covered ratio is 0.75, but expected minimum is 0.90»):

```kotlin
plugins { jacoco }

jacoco { toolVersion = "0.8.16" }

tasks.jacocoTestReport { dependsOn(tasks.test) }

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)
    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.75".toBigDecimal()
            }
        }
    }
}

tasks.check { dependsOn(tasks.jacocoTestCoverageVerification) }
```

- Покрытие — нижняя планка, не цель. Локальная демонстрация: класс `PriceCalculator` с непротестированной веткой скидки показал 100 % покрытия строк (5/5) и прошёл правило JaCoCo LINE на 80 %, а покрытие веток JaCoCo составило 3/4. PIT нашёл 2 мутанта `NO_COVERAGE` и 1 выживший мутант границы (`>=` → `>` на пороге скидки): `Generated 11 mutations Killed 8 (73%) ... Test strength 89%`. Проверено локально (JDK 25, Windows 11). Поэтому порог ставьте и по строкам, и по веткам, а на ключевой логике подключайте мутационное тестирование. 100 % не добивайтесь.
- Исключения: JaCoCo автоматически пропускает классы и методы с аннотацией `@Generated` (в том числе Lombok при `lombok.addLombokGeneratedAnnotation = true` [не проверено]). Шаблонные DTO/records и «проводку» `main` исключайте настройкой, не фиктивными тестами.

PIT (pitest) 1.30.2 (5 ноября 2026):
- Тестам на JUnit 5/6 нужен `pitest-junit5-plugin` 1.2.3 как зависимость плагина (Maven) или `junit5PluginVersion` (Gradle). Без него PIT не находит тесты. С JUnit 6.1.3 работал локально.
- Нумерация прыгнула с 1.25.9 на 1.30.0 из-за ошибочной метки релиза 1.29.10 (версий 1.26–1.28 искать не нужно). 1.25.8 исправил мутаторы BigDecimal/BigInteger на Java 25; 1.30.1 перешёл на ASM 9.11; 1.30.2 добавил поддержку JSpecify.
- Maven: `mvn test-compile org.pitest:pitest-maven:mutationCoverage` (через фазу, см. подвох с агентом Mockito в [разделе 3](#3-тестовые-двойники-mockito-5)). Параметры: `targetClasses`, `targetTests`, `mutationThreshold`, `coverageThreshold`, `threads`, `outputFormats` (HTML, XML, CSV), `timestampedReports=false`, `jvmArgs` для агентов. Проверено локально.
- Мутаторы `DEFAULTS`: граница условий, отрицание условий, математика, инкременты, инверсия отрицательных, мутаторы возвращаемого значения, вызовы void-методов. `STRONGER` добавляет удаление условий. Список мутаторов выведен локально.
- Инкрементальный анализ (`withHistory=true` или `historyInputFile`/`historyOutputFile`) экспериментален и может давать неверные результаты, если меняются зависимости вне суперкласса или внешнего класса. Годится для локального цикла, полный анализ запускайте ночью в CI.
- Ограничивайте охват: в PR запускайте на изменённых классах (`-DtargetClasses=...` или git-интеграция Arcmutate — коммерческая [не проверено]), полные прогоны — ночью. Время PIT примерно равно числу тестов, умноженному на число мутантов.

Gradle (плагин с портала плагинов Gradle; копия на Maven Central кончается на 1.15.0):

```kotlin
plugins { id("info.solidsoft.pitest") version "1.19.0" }

pitest {
    junit5PluginVersion = "1.2.3"
    pitestVersion = "1.30.2"
    targetClasses = setOf("com.acme.*")
    threads = 4
    outputFormats = setOf("XML", "HTML")
    timestampedReports = false
    // агент Mockito для миньонов PIT
    jvmArgs = listOf("-javaagent:${mockitoAgent.asPath}")
}
```

- Многомодульная агрегация — плагин `info.solidsoft.pitest.aggregator`. Блок выше с `jvmArgs` и конфигурацией `mockitoAgent` из [раздела 3](#3-тестовые-двойники-mockito-5) проверен локально (Gradle 9.8.2, JDK 25, плагин 1.19.0, PIT 1.30.2): миньоны запустились с агентом, `Generated 7 mutations Killed 6 (86%)`.
- Descartes 1.3.4 (`eu.stamp-project:descartes`, `<mutationEngine>descartes</mutationEngine>`): экстремальные мутации (замена тела метода целиком). Находит «псевдо-протестированные методы» (покрыты, но ни один тест не замечает удаления тела) и заметно быстрее gregor на больших кодовых базах. Поведение — `[не проверено]`.
- Чтение результатов:
  - `SURVIVED` — тесты выполняют код, но проверяют слишком мало.
  - `NO_COVERAGE` — код не выполняет ни один тест.
  - `TIMED_OUT` считается убитым (бесконечные циклы).
  - Эквивалентные мутанты существуют. Не искажайте тесты ради их убийства.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [JaCoCo Change History](https://www.jacoco.org/jacoco/trunk/doc/changes.html) | JaCoCo team, 2026 | Поддерживаемые версии Java по релизам |
| [JaCoCo Maven Plug-in](https://www.jacoco.org/jacoco/trunk/doc/maven.html) | JaCoCo team | Цели Maven-плагина |
| [jacoco:check](https://www.jacoco.org/jacoco/trunk/doc/check-mojo.html) | JaCoCo team | Правила и пределы покрытия |
| [jacoco:report-aggregate](https://www.jacoco.org/jacoco/trunk/doc/report-aggregate-mojo.html) | JaCoCo team | Агрегат для многомодульных проектов |
| [JaCoCo Coverage Counters](https://www.jacoco.org/jacoco/trunk/doc/counters.html) | JaCoCo team | Что значат счётчики line, branch, instruction |
| [Gradle: The JaCoCo Plugin](https://docs.gradle.org/current/userguide/jacoco_plugin.html) | Gradle | Плагин JaCoCo, правила проверки |
| [Gradle: JaCoCo Report Aggregation Plugin](https://docs.gradle.org/current/userguide/jacoco_report_aggregation_plugin.html) | Gradle | Агрегированное покрытие |
| [PIT Quickstart](https://pitest.org/quickstart/) | H. Coles | Быстрый старт PIT |
| [PIT Mutators](https://pitest.org/quickstart/mutators/) | H. Coles | Каталог мутаторов |
| [PIT Incremental analysis](https://pitest.org/quickstart/incremental_analysis/) | H. Coles | Оговорки инкрементального анализа |
| [PIT Maven quickstart](https://pitest.org/quickstart/maven/) | H. Coles | Параметры Maven |
| [PIT releases](https://github.com/hcoles/pitest/releases) | H. Coles, 2026 | Заметки о релизах (скачок версии) |
| [pitest-junit5-plugin](https://github.com/pitest/pitest-junit5-plugin) | PIT team | Поддержка JUnit 5/6 |
| [gradle-pitest-plugin](https://gradle-pitest-plugin.solidsoft.info/) | M. Zajaczkowski, 2026 | Плагин PIT для Gradle |
| [pitest-descartes](https://github.com/STAMP-project/pitest-descartes) | STAMP project | Экстремальные мутации, псевдо-протестированные методы |
| [Arcmutate](https://www.arcmutate.com/) | H. Coles | Коммерческие расширения PIT (git diff, Kotlin, Spring) |
| [Practical Mutation Testing at Scale: A view from Google](https://arxiv.org/abs/2102.11378) | G. Petrovic, M. Ivankovic, G. Fraser, R. Just, 2021 | Мутации только в изменённом коде, фильтрация непродуктивных мутантов [не проверено] |
| [Code Coverage Best Practices](https://testing.googleblog.com/2020/08/code-coverage-best-practices.html) | Google, 2020 | Практики работы с покрытием |
| [TestCoverage](https://martinfowler.com/bliki/TestCoverage.html) | M. Fowler, 2012 | Покрытие как диагностика, не цель |

## 7. Асинхронные и нестабильные тесты

**Ключевые выводы**
- Не используйте `Thread.sleep` для ожидания асинхронного результата. Берите Awaitility 4.3.0 (версия проверена, API — [не проверено]):

```java
await().atMost(Duration.ofSeconds(5)).pollInterval(Duration.ofMillis(50))
       .untilAsserted(() -> assertThat(repo.findById(id))
           .hasValueSatisfying(o -> assertThat(o.status()).isEqualTo(PAID)));
```

- Для `CompletableFuture`: `future.get(5, SECONDS)` или `assertThat(future).succeedsWithin(Duration.ofSeconds(5))`.
- Лучше сделать асинхронную границу заменяемой (`Executor`, вместо которого в юнит-тестах ставится прямой исполнитель). Awaitility оставьте для интеграционных тестов.
- Таймауты: `@Timeout(5)` или `junit.jupiter.execution.timeout.default=30s` как страховка от зависания, чтобы CI падал быстро с дампом потоков, а не обрывался по лимиту. `assertTimeoutPreemptively` выполняет код в другом потоке и ломает контекст на `ThreadLocal` (транзакции Spring, MDC) [не проверено].
- Корректность конкурентного кода: юнит-тесты редко выявляют гонки. Для проверок на уровне модели памяти для маленьких примитивов берите jcstress 0.16 (подробности — [concurrency-native.md](concurrency-native.md)). Для кода выше уровнем — циклы с большим числом итераций и стартовым барьером `CountDownLatch`. Диагностика pinning у виртуальных потоков изменилась в JDK 24 (JEP 491), см. [concurrency-native.md](concurrency-native.md) [не проверено].
- Время: внедряйте `java.time.Clock` (или `InstantSource`, Java 17+). Тесты используют `Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC)` или изменяемые часы (`MutableClock` из threeten-extra [не проверено]). Не вызывайте `LocalDate.now()` и `Instant.now()` без часов в доменном коде. Если тестируется форматирование, фиксируйте локаль и часовой пояс через `@DefaultLocale` и `@DefaultTimeZone` из JUnit 6.1.
- Случайность: внедряйте `RandomGenerator`/`Random` с seed, при падении логируйте seed. Для случайных тестовых данных (Instancio, Datafaker) фиксируйте и печатайте seed. Не полагайтесь на порядок обхода `HashMap`/`HashSet` и на порядок методов (`@TestMethodOrder` — только для действительно упорядоченных сценариев) [не проверено].
- Изоляция:
  - Нет общего изменяемого статического состояния между тестами, синглтоны сбрасываются.
  - Каждый тест создаёт свои данные (уникальные идентификаторы) или откатывается.
  - Вместо фиксированных путей — `@TempDir`; порты — свободные случайные (`port 0`).
  - Контейнеры БД общие, поэтому тесты не должны считать таблицу пустой: проверяйте строки, которые создали сами.
- Обработка нестабильных тестов: обнаружить, поместить в карантин, исправить.
- Обнаружение. Перезапуск при падении сообщает о флейках, а не скрывает их.
  - Maven Surefire/Failsafe: `-Dsurefire.rerunFailingTestsCount=2`. Прошедшие при повторе тесты отображаются как `Flakes: 1` в итоге и как `<flakyFailure>` в XML; `failOnFlakeCount` роняет сборку при превышении N флейков.
  - Gradle `org.gradle.test-retry` 1.6.6: `retry { maxRetries = 2; maxFailures = 20; failOnPassedAfterRetry = false }`. Включайте только в CI (`System.getenv("CI")`). Параметризованные тесты JUnit повторяются целым методом.
  - Локальный запуск: тест, упавший с первой попытки, был перезапущен и прошёл (`flakyFirstRun() FAILED`, затем `PASSED`, сборка зелёная). Проверено локально.
  - Подвох: повторы перезапускают и сбои инфраструктуры. При остановленном Docker класс Testcontainers пытались выполнить 3 раза (`initializationError FAILED` ×3), прежде чем сборка упала. Задайте `maxFailures`, чтобы сломанная среда не умножала прогон. Проверено локально.
- Карантин: помечайте известные флейки (`@Tag("flaky")`) и гоняйте их отдельной неблокирующей задачей с владельцем и тикетом. `@RetryingTest(3)` из JUnit Pioneer — только временная задокументированная мера [не проверено].
- Исправление: большинство флейков происходит от времени, порядка, общего состояния, асинхронных ожиданий, сети и портов, утечек ресурсов. Воспроизводите прогоном в цикле (`@RepeatedTest(200)`) или с включённой параллелью.
- Отслеживание: Develocity (build scans для Gradle и Maven) и аналитика тестов в CI показывают долю флейков по сборкам (по README test-retry).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Awaitility Usage](https://github.com/awaitility/awaitility/wiki/Usage) | J. Haleby | Руководство по Awaitility |
| [jcstress](https://github.com/openjdk/jcstress) | OpenJDK (A. Shipilev) | Стресс-тесты конкурентности (ссылка) |
| [java.time.Clock (Java SE 25)](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/time/Clock.html) | Oracle | `Clock.fixed`, `Clock.offset` |
| [Surefire: Rerun Failing Tests](https://maven.apache.org/surefire/maven-surefire-plugin/examples/rerun-failing-tests.html) | Apache Maven | Повторы и отчёт о флейках |
| [test-retry-gradle-plugin](https://github.com/gradle/test-retry-gradle-plugin) | Gradle | Параметры плагина повторов |
| [JUnit Pioneer: Retrying test](https://junit-pioneer.org/docs/retrying-test/) | JUnit Pioneer | `@RetryingTest` |
| [Eradicating Non-Determinism in Tests](https://martinfowler.com/articles/nonDeterminism.html) | M. Fowler, 2011 | Борьба с недетерминизмом в тестах |
| [Flaky Tests at Google and How We Mitigate Them](https://testing.googleblog.com/2016/05/flaky-tests-at-google-and-how-we.html) | J. Micco, Google, 2016 | Нестабильность в масштабе |
| [Test Flakiness: One of the Main Challenges of Automated Testing](https://testing.googleblog.com/2020/12/test-flakiness-one-of-main-challenges.html) | Google, 2020 | Первопричины нестабильности |
| [ThreeTen-Extra](https://www.threeten.org/threeten-extra/) | S. Colebourne | `MutableClock` и дополнительные типы времени |

## 8. Организация тестов

**Ключевые выводы**
- Разделение в Maven:
  - Surefire запускает в фазе `test` классы `**/Test*.java`, `**/*Test.java`, `**/*Tests.java`, `**/*TestCase.java`.
  - Failsafe запускает `**/IT*.java`, `**/*IT.java`, `**/*ITCase.java` в `integration-test` и роняет сборку в `verify`, поэтому очистка в `post-integration-test` (остановка серверов и контейнеров) всё равно выполняется.
  - Всегда вызывайте `mvn verify`, не `mvn integration-test`. Пропуск интеграционных тестов — `-DskipITs`.
  - С Surefire 3.6.0 все фреймворки идут через единый провайдер JUnit Platform (JUnit 4 — через Vintage, TestNG — через свой движок). Шаблоны имён по умолчанию — [не проверено], заметка о 3.6.0 проверена.
- Плагин JVM Test Suite в Gradle (`testing { suites { ... } }`) — поддерживаемый способ добавить `integrationTest` со своим набором исходников, зависимостями и задачей. Подключите его к `check` и упорядочьте после `test`. Проверено локально (Gradle 9.8.2, JDK 25):

```kotlin
testing {
    suites {
        named<JvmTestSuite>("test") { useJUnitJupiter("6.1.3") }
        register<JvmTestSuite>("integrationTest") {
            useJUnitJupiter("6.1.3")
            dependencies {
                implementation(project())
                implementation("org.testcontainers:testcontainers-postgresql:2.0.5")
            }
            targets { all { testTask.configure { shouldRunAfter(tasks.named("test")) } } }
        }
    }
}
tasks.named("check") { dependsOn(testing.suites.named("integrationTest")) }
```

- Подвох: часто копируемый `val test by getting(JvmTestSuite::class) { ... }` теперь даёт предупреждения об устаревании в Kotlin DSL (`provideDelegate ... is deprecated ... See the Gradle 9.6 upgrading guide`). Пишите `named<JvmTestSuite>("test")`. Проверено локально.
- Тестовые фикстуры: плагин Gradle `java-test-fixtures` создаёт `src/testFixtures/java`, подключение — `testImplementation(testFixtures(project(":domain")))`. Так общие билдеры и фейки используются между модулями без публикации тестовых классов. Эквивалент в Maven — отдельный модуль `*-test-support` или цель `test-jar` (`test-jar` не несёт транзитивных тестовых зависимостей) [не проверено].
- Именование: класс `<Unit>Test` или `<Feature>IT`. Методы описывают поведение: `rejectsOrderWhenStockIsInsufficient()` или `@DisplayName("rejects order when stock is insufficient")`. Имена вроде `test1` и `testMethodName` не годятся. Один тест — одно поведение; несколько проверок этого поведения допустимы.
- Структура: Arrange-Act-Assert (или Given-When-Then) с пустыми строками между блоками. Один Act на тест. Циклов и `if` в тестах нет, для вариаций — параметризованные тесты.
- Тестовые данные: Test Data Builder (`anOrder().withStatus(PAID).build()`) с валидными значениями по умолчанию, чтобы тест называл только важное. Object Mother (`Orders.paidOrder()`) — для нескольких канонических примеров; билдеры масштабируются лучше. Records и статические фабричные методы дают лёгкие билдеры. Избегайте огромных общих JSON/SQL-фикстур, от которых зависят все тесты.
- Пирамида и «сот»: много быстрых юнит-тестов доменной логики, меньше интеграционных на адаптерах (БД, HTTP, брокеры с настоящими зависимостями через Testcontainers), несколько сквозных. Для тонких CRUD-сервисов баланс смещается к интеграционным тестам («testing honeycomb») [не проверено].
- Бюджет скорости: набор юнит-тестов выполняется за секунды. Держите мало контекстов Spring (кэширование), используйте срезы, переиспользуйте контейнеры на весь набор, включайте параллель JUnit для чистых юнит-тестов, применяйте кэш сборки Gradle, test distribution или Maven `-T` для модулей ([toolchain.md](toolchain.md)).
- Что не тестировать: геттеры и сеттеры, поведение фреймворка, приватные методы напрямую (проверяйте через публичный API), сгенерированный код.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/) | Apache Maven | Документация Surefire (includes, forks, argLine) |
| [Maven Failsafe Plugin](https://maven.apache.org/surefire/maven-failsafe-plugin/) | Apache Maven | Жизненный цикл Failsafe (`verify`) |
| [Gradle: JVM Test Suite Plugin](https://docs.gradle.org/current/userguide/jvm_test_suite_plugin.html) | Gradle | Плагин JVM Test Suite |
| [Gradle: Testing in Java & JVM projects](https://docs.gradle.org/current/userguide/java_testing.html) | Gradle | Тестирование в Java-проектах, фикстуры, фильтры |
| [The Practical Test Pyramid](https://martinfowler.com/articles/practical-test-pyramid.html) | H. Vocke, 2018 | Практическая пирамида тестов |
| [GivenWhenThen](https://martinfowler.com/bliki/GivenWhenThen.html) | M. Fowler, 2013 | Given-When-Then |
| [ObjectMother](https://martinfowler.com/bliki/ObjectMother.html) | M. Fowler, 2006 | Паттерн Object Mother |
| [Test Data Builders](http://www.natpryce.com/articles/000714.html) | N. Pryce, 2007 | Test Data Builders |
| [xUnit Patterns](http://xunitpatterns.com/) | G. Meszaros, 2007 | xUnit Test Patterns (запахи, фикстуры) |
| [Growing Object-Oriented Software, Guided by Tests](http://www.growing-object-oriented-software.com/) | S. Freeman, N. Pryce, 2009 | GOOS: TDD с моками, подход снаружи внутрь (Java) |
| [Unit Testing Principles, Practices, and Patterns](https://www.manning.com/books/unit-testing) | V. Khorikov, 2020 | Принципы и практики юнит-тестирования |
| [Software Engineering at Google, ch. 12: Unit Testing](https://abseil.io/resources/swe-book/html/ch12.html) | Google, 2020 | Глава про юнит-тесты |
| [Testing of Microservices (Spotify)](https://engineering.atspotify.com/2018/01/testing-of-microservices) | Spotify, 2018 | «Соты» тестирования |

## 9. Книги и ссылки

**Ключевые выводы**
- Книги с примерами на Java:
  - Aniche, «Effective Software Testing» (2022): JUnit 5, property-based тесты, моки, тестирование по спецификации и по структуре.
  - Khorikov, «Unit Testing Principles, Practices, and Patterns» (2020): примеры на C#, идеи переносятся один к одному — четыре столпа хорошего теста, моки только для неуправляемых зависимостей.
  - Freeman и Pryce, GOOS (2009): Java, мокистский подход снаружи внутрь.
  - Tudose, «JUnit in Action», 3-е издание (JUnit 5).
  - Глубже о книгах — в [books.md](books.md).
- «Test Desiderata» Кента Бека описывает 12 свойств хорошего теста: изолированность, компонуемость, детерминизм, скорость, простота написания, читаемость, поведенческий характер, нечувствительность к структуре, автоматизация, конкретность, предсказательность, вдохновляющий эффект. Это компактный словарь для ревью тестов.
- Перекрёстные ссылки: JMH и бенчмарк-тесты — [performance.md](performance.md); jcstress и виртуальные потоки — [concurrency-native.md](concurrency-native.md); ArchUnit — [architecture.md](architecture.md); Surefire, Gradle, Error Prone/NullAway, обработка аннотаций на JDK 23+ — [toolchain.md](toolchain.md); внутренности Spring и Quarkus — [domains-server.md](domains-server.md).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Effective Software Testing](https://www.manning.com/books/effective-software-testing) | M. Aniche, 2022 | Систематическое проектирование тестов на Java |
| [Effective Software Testing: companion site](https://effective-software-testing.com/) | M. Aniche | Сайт книги и код |
| [JUnit in Action, Third Edition](https://www.manning.com/books/junit-in-action-third-edition) | C. Tudose, 2020 | JUnit 5 в глубину |
| [Test Desiderata](https://kentbeck.github.io/TestDesiderata/) | K. Beck, 2019 | 12 свойств хороших тестов |
| [Just Say No to More End-to-End Tests](https://testing.googleblog.com/2015/04/just-say-no-to-more-end-to-end-tests.html) | M. Wacker, Google, 2015 | Почему пирамида отдаёт предпочтение мелким тестам |
| [Test Contra-Variance](https://blog.cleancoder.com/uncle-bob/2017/10/03/TestContravariance.html) | R. C. Martin, 2017 | Тесты, связанные со структурой и с поведением |
| [IntelliJ IDEA: Testing](https://www.jetbrains.com/help/idea/testing.html) | JetBrains | Запуск тестов, покрытие, перезапуск упавших в IntelliJ |

## 10. Журнал локальной проверки

Проверено 6 ноября 2026: Windows 11, Temurin 25.0.5+8 (ранние запуски на 25.0.4.1 дали те же результаты), Maven 3.9.16 с изолированным локальным репозиторием, Gradle 9.8.2 с изолированным `GRADLE_USER_HOME`, клиент Docker Desktop 29.8.2 при остановленном демоне.

| Проверка | Настройка | Результат |
|---|---|---|
| Предупреждение Mockito о самоприсоединении | JUnit 6.1.3, Mockito 5.24.0, Surefire 3.6.1, без агента | Предупреждения `Mockito is currently self-attaching...` и JEP 451 `A Java agent has been loaded dynamically` |
| Статический агент Mockito (Maven) | `dependency:properties` и `@{argLine} -javaagent:${org.mockito:mockito-core:jar}` | Предупреждений нет; `argLine` JaCoCo сохранён; 12 тестов проходят |
| Статический агент Mockito (Gradle) | конфигурация `mockitoAgent` и `jvmArgumentProviders` | Мок создаётся, предупреждения нет |
| PIT и агент | `jvmArgs` в pitest-maven, запуск через фазу `test-compile` | Работает; вызов цели напрямую даёт `MINION_DIED` (неразрешённое свойство) |
| Покрытие и мутации | Непротестированная ветка скидки | Строки 100 %, ветки 3/4, PIT убил 73 %, 2 без покрытия, 1 выживший мутант границы |
| Параллель JUnit | 8 тестов по 1 с, dynamic factor 1 | Около 1,6 с всего на потоках ForkJoinPool |
| Gradle test-retry 1.6.6 | Тест падает только с первой попытки | FAILED, затем PASSED, сборка зелёная |
| Повторы и сломанная инфраструктура | Testcontainers без Docker | Класс повторён 3 раза, все с `Could not find a valid Docker environment` |
| `disabledWithoutDocker = true` | То же, флаг в аннотации | Тест SKIPPED, сборка зелёная |
| DSL наборов в Gradle | `val test by getting(JvmTestSuite::class)` | Предупреждения об устаревании в Gradle 9.8.2; `named<JvmTestSuite>("test")` без предупреждений |
| Testcontainers на живом Docker Engine 29 | — | Не запускалось (демон остановлен), локально не проверено |

## Правила для ИИ-агента, пишущего тесты

1. Сначала красный тест. Для исправления бага напишите падающий тест, запустите его и убедитесь, что он падает по ожидаемой причине (сообщение проверки, а не ошибка компиляции или NPE в подготовке). Для новой функции хотя бы один тест должен падать до появления кода.
2. Не ослабляйте тест ради зелёной сборки. Не удаляйте проверки, не расширяйте допуски, не добавляйте `@Disabled`, не оборачивайте в `try/catch`, не переключайтесь на `lenient()`, не увеличивайте таймауты, не перезаписывайте снапшоты пакетно без объяснения, почему прежнее ожидание было неверным. Если ошибочен сам тест, скажите об этом явно в отчёте.
3. Запускайте настоящую команду проекта (`./mvnw verify`, `./gradlew check`), а не только один тест в IDE. Проверьте, что число тестов не упало: неверно названный класс или `private`-метод теста на старых настройках молча пропускается. JUnit 5.13+ сообщает о проблемах обнаружения, читайте их.
4. Предпочитайте фейки и реальные объекты. Мокируйте только границы, которыми владеете. Не мокируйте тестируемый класс, значения и коллекции JDK.
5. Проверяйте поведение, а не реализацию: выходы и наблюдаемое состояние. `verify` взаимодействий нужен, только когда взаимодействие и есть требование.
6. Делайте тесты детерминированными по построению: внедряйте `Clock`, seed и исполнители. Никакого `Thread.sleep`, опоры на порядок, локаль или часовой пояс по умолчанию, сеть и реальное время.
7. Тесты независимы: нет общих изменяемых статических полей, нет связи через порядок, данные в каждом тесте свои.
8. Ожидаемые значения — литералы либо берутся из независимого эталона. Никогда не пересчитывайте их логикой продакшена.
9. Следуйте стеку проекта: та же библиотека проверок (AssertJ или Hamcrest), тот же стиль мокирования, те же имена и раскладка. Не добавляйте новую тестовую библиотеку ради одного теста.
10. Не добавляйте jqwik в проект по собственной инициативе. Автор jqwik в руководстве пользователя (с версии 1.10) пишет, что библиотека «is not meant to be used by any AI coding agents». Если нужны property-based тесты, сообщите пользователю об этом ограничении и оставьте решение ему. Если jqwik уже используется, продолжайте в стиле проекта и упомяните оговорку, когда предлагаете расширить применение.
11. Интеграционные тесты используют настоящий движок (Testcontainers) для SQL и миграций. Не «чините» упавший запрос под Postgres переходом тестов на H2.
12. Следите за инфраструктурой сборки. На JDK 21+ подключайте агент Mockito. Поднимайте JaCoCo и PIT при обновлении JDK. На JDK 23+ явно объявляйте процессоры аннотаций и для компиляции тестов.
13. Не используйте PIT и JaCoCo как цель: порог по веткам и мутационный отчёт — средство найти слабые тесты, а не повод писать фиктивные.
14. Перед удалением или изменением чужого теста выясните, что он защищал (история git, тикет). Изменение или удаление объясняйте в отчёте.
15. Отчитывайтесь честно: какие тесты добавлены, что они доказывают, что не покрыто, какие тесты изменены или удалены и почему.

## Чеклист ревью тестов

- [ ] Каждое новое поведение и каждое исправление бага имеет тест, который падал до изменения (подтверждение в PR или в отчёте).
- [ ] Имена тестов называют поведение и ожидаемый исход.
- [ ] Структура AAA/GWT видна глазами; в телах тестов нет логики (циклов, `if`).
- [ ] Проверки конкретны: `isEqualTo`, тип исключения и сообщение.
- [ ] У каждого `assertThat` есть завершающая проверка.
- [ ] Нет `assertTrue(a.equals(b))`, пустых блоков `catch` и `System.out` вместо проверок.
- [ ] Крайние случаи покрыты: пусто, null (или явно запрещён), границы (`>=` и `>`), максимум и переполнение, Unicode, часовой пояс и переход на летнее время там, где это важно, пути ошибок.
- [ ] Моки стоят только на границах, которыми владеет проект.
- [ ] Нет моков, возвращающих моки, и `verify` на побочные вызовы.
- [ ] Нет `lenient()` и `@MockitoSettings(strictness = LENIENT)` без причины.
- [ ] Нет `Thread.sleep`; асинхронные ожидания идут через Awaitility или futures с таймаутом.
- [ ] Время берётся из внедрённого `Clock`; seed зафиксированы и логируются.
- [ ] Тесты независимы: проходят по одному, в любом порядке и параллельно.
- [ ] Нет общего изменяемого статического состояния; временные файлы создаются через `@TempDir`; порты динамические.
- [ ] Интеграционные тесты используют движок и версию БД из продакшена (Testcontainers, закреплённый тег образа).
- [ ] Контейнеры общие на набор; H2 не используется для SQL, специфичного для вендора.
- [ ] Spring-тесты используют самый узкий срез.
- [ ] Наборы `@MockitoBean` объединены, чтобы работало кэширование контекста; нет лишних `@DirtiesContext`.
- [ ] Spring Boot 4: нет `@MockBean`/`@SpyBean`; MockMvc, `RestTestClient` или `TestRestTemplate` подключены явными `@AutoConfigure...`.
- [ ] Нет `@Disabled` и `@Tag("flaky")` без ссылки на тикет и владельца.
- [ ] Повторы настроены так, чтобы сообщать о флейках, а не скрывать их; `maxFailures` ограничивает повторы при сломанной среде.
- [ ] Покрытие изменённого кода не упало ни по строкам, ни по веткам.
- [ ] Для ключевой логики PIT не показывает новых выживших мутантов; эквивалентные мутанты не «лечатся» искажением тестов.
- [ ] В сборке один BOM JUnit; для Gradle есть `junit-platform-launcher`.
- [ ] Агент Mockito подключён для Surefire, Failsafe, Gradle и PIT на JDK 21+ (`@{argLine}` в Maven, `jvmArgs` у PIT).
- [ ] JaCoCo поддерживает целевой JDK; PIT запускается через фазу, а не прямым вызовом цели.
- [ ] Интеграционные тесты выполняются в `verify` или `check`, а не остаются вне сборки.
- [ ] Тест-стек не получил новых зависимостей без причины; jqwik не добавлен агентом без решения пользователя.
- [ ] Файлы снапшотов и approval-тестов, изменённые в PR, прочитаны построчно и детерминированы.
- [ ] Тестовые данные собираются билдерами и фикстурами с валидными значениями по умолчанию.
- [ ] Нет гигантских общих фикстур, реальных персональных данных и секретов.
- [ ] Контрактные тесты (Pact, Spring Cloud Contract) обновлены вместе с изменением API и запускаются в CI.
- [ ] Тесты не вызывают сеть и не зависят от внешних сервисов.
- [ ] Число выполненных тестов в отчёте сборки не уменьшилось по сравнению с прошлым прогоном.
- [ ] Для обработки аннотаций на JDK 23+ тестовые наборы исходников имеют собственную настройку процессоров.
