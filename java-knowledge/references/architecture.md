# Архитектура Java-проектов и кода

Как раскладывать сборку (Maven, Gradle) и код (пакеты, модули JPMS), какой архитектурный стиль подходит к какой задаче, как проверять границы автоматически (ArchUnit, Spring Modulith, JPMS, анализ зависимостей), как устроить внедрение зависимостей, эволюцию API и слои хранения. В конце: кодовые базы для изучения и чеклист ревью. Актуально на ноябрь 2026: JDK 25 — последняя LTS, JDK 27 — последний feature-релиз (сентябрь 2026), Gradle 9.8.2, Maven 3.9.16 (Maven 4.0.0-rc-7 пока release candidate), ArchUnit 1.5.1, Spring Modulith 2.1.2, Spring Boot 4.1.2. Пометки: без пометки — страница прочитана или URL проверен; `[не проверено]` — единственный вторичный источник или сводка без проверки; `[устарело]` — заменено, замена указана рядом; «проверено локально (JDK 25, Windows 11)» — результат воспроизведён на Maven 3.9.16 + Temurin 25.0.4 и Gradle 9.8.2 + Temurin 25.0.5. Серверные фреймворки и транзакции — в [domains-server.md](domains-server.md), тесты — в [testing.md](testing.md), сборка и инструменты — в [toolchain.md](toolchain.md), идиомы языка (records, sealed, исключения) — в [idioms-patterns.md](idioms-patterns.md), конкурентность — в [concurrency-native.md](concurrency-native.md), книги — в [books.md](books.md).

- [Версии на ноябрь 2026](#версии-на-ноябрь-2026)
- [Короткие правила по умолчанию](#короткие-правила-по-умолчанию)
- [1. Раскладка сборки: Maven и Gradle](#1-раскладка-сборки-maven-и-gradle)
- [2. Упаковка кода: пакеты, видимость, модули JPMS](#2-упаковка-кода-пакеты-видимость-модули-jpms)
- [3. Архитектурные стили, DDD, модульный монолит и микросервисы](#3-архитектурные-стили-ddd-модульный-монолит-и-микросервисы)
- [4. Автоматическая проверка границ](#4-автоматическая-проверка-границ)
- [5. Внедрение зависимостей](#5-внедрение-зависимостей)
- [6. Конфигурация, ошибки на границах, эволюция API](#6-конфигурация-ошибки-на-границах-эволюция-api)
- [7. Слои хранения, DTO и транзакции](#7-слои-хранения-dto-и-транзакции)
- [8. Кодовые базы для изучения](#8-кодовые-базы-для-изучения)
- [Чеклист ревью архитектуры](#чеклист-ревью-архитектуры)
- [Открытые вопросы](#открытые-вопросы)

## Версии на ноябрь 2026

Источник: `maven-metadata.xml` на Maven Central (`https://repo.maven.apache.org/maven2/<путь группы>/<artifact>/maven-metadata.xml`) и метаданные Gradle Plugin Portal. «Стабильная» — самая новая версия без milestone, RC и beta.

| Артефакт | Координаты | Стабильная | Pre-release в работе |
|---|---|---|---|
| ArchUnit | `com.tngtech.archunit:archunit-junit5` | 1.5.1 (2026-09-25) | нет |
| Spring Modulith | `org.springframework.modulith:spring-modulith-bom` | 2.1.2 | 2.2.0-RC1 |
| Spring Boot (контекст) | `org.springframework.boot:spring-boot` | 4.1.2 | 4.2.0-RC2 |
| Spring Framework (контекст) | `org.springframework:spring-core` | 7.0.10 | 7.1.0-RC2 |
| jMolecules BOM | `org.jmolecules:jmolecules-bom` | 2025.0.2 | нет |
| jMolecules DDD | `org.jmolecules:jmolecules-ddd` | 2.0.1 | нет |
| jMolecules ArchUnit rules | `org.jmolecules.integrations:jmolecules-archunit` | 1.6.0 | нет |
| MapStruct | `org.mapstruct:mapstruct` | 1.6.3 | 1.7.0.Beta2 |
| Dagger | `com.google.dagger:dagger` | 2.60.1 | нет |
| Avaje Inject | `io.avaje:avaje-inject` | 12.7 (и `12.7-javax` для javax.inject) | нет |
| Avaje Config | `io.avaje:avaje-config` | 5.2 | нет |
| Guice | `com.google.inject:guice` | 7.0.0 (2023, jakarta.inject) | нет |
| Micronaut platform BOM | `io.micronaut.platform:micronaut-platform` | 5.2.5 | нет |
| Quarkus BOM | `io.quarkus:quarkus-bom` | 3.40.1.1 | 4.0.0.Beta3 |
| Axon Framework | `org.axonframework:axon-messaging` | 5.4.1 (линия Axon 4: `axon-bom` 4.13.3) | нет |
| japicmp Maven plugin | `com.github.siom79.japicmp:japicmp-maven-plugin` | 0.27.0 | нет |
| japicmp Gradle plugin | `me.champeau.gradle.japicmp` (Plugin Portal) | 0.4.6 (2025-02) | нет |
| Revapi Maven plugin | `org.revapi:revapi-maven-plugin` | 0.15.1 (2025-04, редкие релизы) | нет |
| @API Guardian | `org.apiguardian:apiguardian-api` | 1.1.2 (2021, закончен) | нет |
| Dependency Analysis Gradle Plugin | `com.autonomousapps.build-health` / `dependency-analysis` | 3.20.1 | нет |
| maven-dependency-plugin | `org.apache.maven.plugins:maven-dependency-plugin` | 3.11.0 | нет |
| maven-compiler-plugin | `org.apache.maven.plugins:maven-compiler-plugin` | 3.16.0 | 4.0.0-beta-5 (для Maven 4) |
| maven-surefire-plugin | `org.apache.maven.plugins:maven-surefire-plugin` | 3.6.1 | нет |
| jQAssistant CLI | `com.buschmais.jqassistant.cli:jqassistant-commandline-neo4jv5` | 2.9.1 | 2.10.0-M4 |
| Checkstyle | `com.puppycrawl.tools:checkstyle` | 14.4.0 | нет |
| Error Prone | `com.google.errorprone:error_prone_core` | 2.52.0 | нет |
| NullAway | `com.uber.nullaway:nullaway` | 0.14.3 | нет |
| JSpecify | `org.jspecify:jspecify` | 1.0.1 | нет |
| Immutables | `org.immutables:value` | 2.12.3 | нет |
| RecordBuilder | `io.soabase.record-builder:record-builder-core` | 53 | нет |
| ModiTect Maven plugin | `org.moditect:moditect-maven-plugin` | 1.3.0.Final | нет |
| JUnit (контекст) | `org.junit:junit-bom` | 6.1.3 | нет |

Подводные камни, найденные при сверке версий:

- jMolecules перешёл на календарные BOM (`2025.0.x`), отдельные артефакты остались на semver (`jmolecules-ddd` 2.0.1, `jmolecules-archunit` 1.6.0). Импортируйте BOM и не закрепляйте отдельные модули.
- Avaje Inject публикует два варианта каждого релиза: обычный (jakarta.inject) и `-javax`. Метаданные «latest» Maven указывают на `12.7-javax`. Для Jakarta-проектов это ловушка: версию задавайте явно.
- **Annotation processors на JDK 23+.** javac больше не запускает процессоры, найденные на обычном classpath. MapStruct, Dagger, Avaje Inject, Immutables, RecordBuilder, Micronaut и байткод/процессорные интеграции jMolecules нужно объявить в `annotationProcessorPaths` (maven-compiler-plugin 3.16.0) или в конфигурации Gradle `annotationProcessor`. Второй путь — флаг `-proc:full`. Симптом: сборка проходит, но сгенерированных классов (`*MapperImpl`, `Dagger*Component`, `*$DI`) нет во время выполнения.

```xml
<!-- Maven: явный путь процессоров (нужен на JDK 23+) -->
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <version>3.16.0</version>
  <configuration>
    <release>25</release>
    <annotationProcessorPaths>
      <path><groupId>org.mapstruct</groupId><artifactId>mapstruct-processor</artifactId><version>1.6.3</version></path>
      <path><groupId>com.google.dagger</groupId><artifactId>dagger-compiler</artifactId><version>2.60.1</version></path>
    </annotationProcessorPaths>
  </configuration>
</plugin>
```

## Короткие правила по умолчанию

Применяйте их к любому новому Java-проекту, пока нет причины поступить иначе.

- Начинайте с одного развёртываемого артефакта и одной базы. Модульный монолит — вариант по умолчанию для бизнес-систем. Микросервисы нужны при названной причине: независимое развёртывание, масштабирование, границы команд.
- Группируйте код по возможностям (`order`, `billing`), а не по техническим ролям (`controller`, `service`, `repository`). Удаление возможности должно означать удаление одного пакета.
- Делайте всё package-private по умолчанию. `public` — только фасад возможности: сервис, главная сущность, события. Остальное (репозитории, мапперы, JPA-сущности) закрывайте пакетом.
- Внедряйте зависимости через конструктор: `final`-поля, один конструктор, без field injection. Контейнер знают только адаптеры и composition root.
- Держите ядро (домен и сценарии) без импортов Spring, JPA и веба. Ядро создаётся через `new` в обычном тесте.
- Общую логику сборки кладите в convention-плагины (`build-logic` в Gradle) или родительский POM. Не используйте `allprojects {}` и `subprojects {}`. Исходники в корневом проекте не держите.
- Все версии берите из одного места: version catalog (`gradle/libs.versions.toml`), `dependencyManagement` родителя, импортированные BOM. Литералов версий в модулях быть не должно.
- Делите на модули Maven/Gradle только там, где нужна проверка на этапе компиляции, отдельный выпуск или отдельное развёртывание. Внутри одного артефакта хватает пакетов плюс ArchUnit или Spring Modulith.
- Запускайте архитектурные правила как обычные тесты в CI: ArchUnit (`slices().beFreeOfCycles()`, правило про `internal`-пакеты) или `ApplicationModules.verify()`. Старые нарушения замораживайте через `freeze(...)`, а не отключайте.
- Объявляйте annotation processors явно (`annotationProcessorPaths` или `annotationProcessor`). На JDK 23+ иначе генерация молча не работает.
- Фиксируйте версию Java через toolchain (Gradle) или `<release>` (Maven), а не через JDK, на котором запущена сборка.
- Значения и идентификаторы оформляйте как records с валидацией в compact-конструкторе. Закрытые варианты результата — sealed-интерфейсы. Сырые `Long`/`UUID` через границы не передавайте.
- Привязывайте конфигурацию один раз при старте к неизменяемому проверяемому объекту (`@ConfigurationProperties` на record). Ошибки доменного уровня переводите в HTTP-ответ или код выхода в одном месте на границе.
- Одна транзакция — один сценарий — один агрегат. Транзакцию ставьте на сервис сценария, не на контроллер и не на репозиторий. Согласованность между модулями обеспечивайте событиями после коммита.
- Для библиотек: публичный API отдельно от `internal`, статус типов через `@API`, проверка бинарной совместимости (japicmp) в `verify`, `module-info.java` или хотя бы `Automatic-Module-Name`.
- Записывайте выбранный стиль в один абзац (ADR) и сверяйте его с кодом. Архитектурные диаграммы генерируйте из кода.

## 1. Раскладка сборки: Maven и Gradle

**Ключевые выводы**

Maven

- Используйте стандартную раскладку каталогов (`src/main/java`, `src/main/resources`, `src/test/java`, `src/test/resources`). Её предполагают все плагины и IDE, отклонение стоит дороже, чем даёт.
- Не смешивайте три роли «родительского» POM: **агрегатор** (`<modules>`/`<subprojects>`, определяет, что собирается вместе), **parent** (наследуемая конфигурация плагинов и `pluginManagement`) и **BOM** (только `dependencyManagement`, подключается потребителями через `<scope>import</scope>`). Маленькой сборке хватает одного корневого POM в роли агрегатора и parent. BOM публикуйте отдельным модулем, чтобы потребители не наследовали вашу конфигурацию сборки.
- Версии держите в `dependencyManagement` parent (или в импортированных BOM), зависимости модулей объявляйте без версий. Версии плагинов — в `pluginManagement`.
- Для обычной сборки запускайте `mvn verify`, а не `mvn clean install`. Резолвер реактора находит результат соседних модулей без установки в `~/.m2`, документация Maven 4 говорит об этом прямо.
- `-pl <module> -am` (also-make) собирает один модуль и всё, от чего он зависит. `-rf :module` продолжает упавшую сборку. Maven 4 чинит баг `--also-make` (MNG-6863) и добавляет `-r`/`--resume`.
- CI-friendly версии: на Maven 3 `${revision}` требует flatten-maven-plugin, иначе опубликованные POM непригодны. Maven 4 поддерживает любые переменные сам.
- **Что меняет Maven 4** (4.0.0-rc-7 на момент проверки, не GA):
  - Build POM и **consumer POM**: опубликованный POM теряет данные, нужные только сборке. Полное «разглаживание» (parent и импорт BOM подставляются внутрь) включается опцией `maven.consumer.pom.flatten=true`.
  - Модель `4.1.0` применяется только к build POM. Потребители получают `4.0.0`.
  - `<modules>` переименован в `<subprojects>` (старый элемент устарел, но работает). Подпроекты находятся автоматически, если у parent упаковка `pom` и список не задан.
  - **Вывод parent**: `<parent/>` (по умолчанию `relativePath` `..`) обходится без GAV. Внутри реактора версии соседних зависимостей можно не указывать.
  - Новая упаковка `bom` отделяет BOM от parent. Maven 4 предупреждает, если сборка импортирует BOM из того же реактора, в планах сделать это ошибкой. BOM оставляйте для внешних потребителей.
  - `<build><sources>` заменяет build-helper-maven-plugin для дополнительных каталогов исходников и даёт module-source-hierarchy (несколько модулей JPMS в одном Maven-проекте).
  - `deployAtEnd` теперь по умолчанию true.
- Проверяйте структуру через maven-enforcer-plugin (3.6.3): правила `dependencyConvergence`, `requireUpperBoundDeps`, `banDuplicatePomDependencyVersions`, а также `banCircularDependencies` из MojoHaus extra-enforcer-rules (1.12.1).

```text
shop/                       агрегатор + parent (packaging=pom)
  shop-bom/                 packaging=pom, только dependencyManagement; публикуется для потребителей
  shop-domain/              чистая Java, без фреймворков
  shop-application/         сценарии, порты
  shop-adapter-persistence/ реализация портов на JPA/JDBC
  shop-adapter-web/         REST-контроллеры
  shop-app/                 main Spring Boot + сборка графа; единственный артефакт развёртывания
```

Делите на модули Maven/Gradle только там, где нужна проверка **на этапе компиляции** или отдельный цикл выпуска. Внутри одного артефакта пакеты плюс ArchUnit или Spring Modulith обычно достаточны (раздел 4).

Gradle

- Страницы best practices Gradle 9.x говорят: модуляризуйте сборку, **не держите исходники в корневом проекте**, предпочитайте **включённую сборку `build-logic`** вместо `buildSrc` (меньше инвалидаций, собирается и тестируется отдельно, нет сюрпризов с загрузчиками классов), общую логику выносите в **convention-плагины**, не создавайте пустых промежуточных проектов случайно (`include(":subs:web:x")` создаёт также `:subs` и `:subs:web`).
- Не используйте `allprojects {}` и `subprojects {}`. Они скрывают межпроектную конфигурацию, связывают проекты на этапе конфигурации и не работают с configuration-on-demand и Isolated Projects (incubating). Заменяйте их convention-плагинами, которые каждый проект применяет явно.
- Версии держите в **version catalog** (`gradle/libs.versions.toml`). Для BOM используйте `platform(...)`, для публикации собственного BOM — плагин `java-platform`. Выбирайте `api` или `implementation` (плагин `java-library`) осознанно: `api` попадает в compile classpath потребителей.
- Gradle не генерирует типобезопасный аксессор `libs` внутри precompiled script plugin. Используйте `extensions.getByType<VersionCatalogsExtension>().named("libs").findLibrary("x").get()` или известный обходной путь с jar аксессора в `build-logic`. Проверено локально (JDK 25, Windows 11).
- Объявляйте репозитории один раз в settings: `dependencyResolutionManagement { repositoriesMode = FAIL_ON_PROJECT_REPOS }`. Проверено локально (JDK 25, Windows 11).
- Фиксируйте Java через toolchains (`java { toolchain { languageVersion = JavaLanguageVersion.of(25) } }`), а не через JDK, на котором запущен Gradle. Проверено локально (JDK 25, Windows 11).
- Для общих тестовых билдеров применяйте `java-test-fixtures`, а не модуль `*-test-utils`, который зависит от всего.

```kotlin
// settings.gradle.kts
pluginManagement { includeBuild("build-logic") }
dependencyResolutionManagement {
    repositories { mavenCentral() }
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
}
include("core", "app")

// build-logic/settings.gradle.kts -- общий каталог корня для включённой сборки
dependencyResolutionManagement {
    repositories { gradlePluginPortal() }
    versionCatalogs { create("libs") { from(files("../gradle/libs.versions.toml")) } }
}

// build-logic/build.gradle.kts
plugins { `kotlin-dsl` }

// build-logic/src/main/kotlin/demo.java-conventions.gradle.kts
plugins { `java-library` }
val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
java { toolchain { languageVersion = JavaLanguageVersion.of(25) } }
dependencies {
    testImplementation(platform(libs.findLibrary("junit-bom").get()))
    testImplementation(libs.findLibrary("junit-jupiter").get())
    testRuntimeOnly(libs.findLibrary("junit-launcher").get())
    testImplementation(libs.findLibrary("archunit-junit5").get())
}
tasks.withType<JavaCompile>().configureEach { options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror")) }
tasks.named<Test>("test") { useJUnitPlatform() }

// core/build.gradle.kts
plugins { id("demo.java-conventions") }
```

Проверено локально (JDK 25, Windows 11): на Gradle 9.8.2 с JDK 25.0.5 `gradle build run` на такой раскладке прошёл. В `core` есть тест ArchUnit, `app` использует плагин `application`. Выполнено 19 задач, первый запуск занял около 1 мин 41 с вместе с загрузками.

Как это делают большие проекты (по сырым файлам settings/build, 2026-11-06)

- **Gradle** использует включённые сборки `build-logic-settings`, `build-logic-commons` и `build-logic`. Сотни подпроектов сгруппированы в *платформы* (`core`, `software`, `jvm`, `extensibility`, ...), объявленные в `settings.gradle.kts`.
- **Spring Framework** собирается Gradle из плоского списка `include "spring-*"` плюс `framework-bom`, `framework-platform`, `framework-api`. Конвенции лежат в `buildSrc` (`ConventionsPlugin`, `JavaConventions`, `CheckstyleConventions`). Пакет `architecture` запускает **ArchUnit как задачу сборки** (`ArchitectureCheck`/`ArchitectureRules`): нет циклов между пакетами, `toLowerCase()`/`toUpperCase()` обязаны получать Locale, запрещённые импорты отсекаются. В Spring Boot тот же `ArchitecturePlugin` в `buildSrc`.
- **JUnit** держит конвенции во включённых сборках `gradle/base` и `gradle/plugins`, каталог — в `gradle/libs.versions.toml`.
- **Elasticsearch** использует включённые сборки `build-conventions`, `build-tools`, `build-tools-internal`. Переиспользуемый код лежит в `libs/`, функциональность — в `modules/` и `x-pack/plugin/`.
- **Micronaut**: каждый репозиторий применяет общий settings-плагин `io.micronaut.build.shared.settings` из отдельного проекта `micronaut-build`, то есть конвенции поставляются как опубликованный плагин.
- **Apache Kafka — контрпример.** Один корневой `build.gradle` на несколько тысяч строк с `allprojects {}`, `subprojects {}` и множеством блоков `project(':x') {}`. Сборка работает, но Gradle теперь рекомендует против такого стиля.
- **Keycloak**, **Quarkus** и сам **Maven** — большие реакторы Maven. Quarkus держит отдельный BOM (`bom/application`) и даёт каждому расширению свой каталог (`extensions/<name>/{runtime,deployment}`).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Standard Directory Layout](https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html) | Apache Maven | Каноническая раскладка |
| [Guide to Working with Multiple Modules](https://maven.apache.org/guides/mini/guide-multiple-modules.html) | Apache Maven | Реактор, порядок, `-pl/-am` |
| [Multiple subprojects in Maven 4](https://maven.apache.org/guides/mini/guide-multiple-modules-4.html) | Apache Maven | Подпроекты Maven 4, вывод parent |
| [What's new in Maven 4](https://maven.apache.org/whatsnewinmaven4.html) | Apache Maven, 2025–26 | Consumer POM, упаковка `bom`, `<sources>` |
| [Introduction to the POM](https://maven.apache.org/guides/introduction/introduction-to-the-pom.html) | Apache Maven | Наследование против агрегации |
| [Introduction to the Dependency Mechanism](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html) | Apache Maven | Scopes, `dependencyManagement`, импорт BOM |
| [Maven CI Friendly Versions](https://maven.apache.org/maven-ci-friendly.html) / [Flatten Maven Plugin](https://www.mojohaus.org/flatten-maven-plugin/) | Apache Maven; MojoHaus | `${revision}` (на Maven 3 нужен flatten) |
| [Enforcer built-in rules](https://maven.apache.org/enforcer/enforcer-rules/index.html) | Apache Maven | Конвергенция, upper bounds, запреты |
| [banCircularDependencies](https://www.mojohaus.org/extra-enforcer-rules/banCircularDependencies.html) | MojoHaus | Проверка циклов на уровне артефактов |
| [Structuring builds best practices](https://docs.gradle.org/current/userguide/best_practices_structuring_builds.html) | Gradle 9.8 | build-logic, convention-плагины, нет исходников в корне |
| Gradle best practices: [index](https://docs.gradle.org/current/userguide/best_practices_index.html) / [dependencies](https://docs.gradle.org/current/userguide/best_practices_dependencies.html) / [general](https://docs.gradle.org/current/userguide/best_practices_general.html) | Gradle | Каталоги, платформы, `api` против `implementation`, Kotlin DSL |
| [Sharing build logic between subprojects](https://docs.gradle.org/current/userguide/sharing_build_logic_between_subprojects.html) | Gradle | Почему не `allprojects`/`subprojects` |
| Gradle user guide: [Multi-project builds](https://docs.gradle.org/current/userguide/multi_project_builds.html) / [Composite builds](https://docs.gradle.org/current/userguide/composite_builds.html) / [Version catalogs](https://docs.gradle.org/current/userguide/version_catalogs.html) / [Java Library plugin](https://docs.gradle.org/current/userguide/java_library_plugin.html) / [Java Platform plugin](https://docs.gradle.org/current/userguide/java_platform_plugin.html) / [Precompiled script plugins](https://docs.gradle.org/current/userguide/implementing_gradle_plugins_precompiled.html) / [Isolated Projects](https://docs.gradle.org/current/userguide/isolated_projects.html) / [Test fixtures](https://docs.gradle.org/current/userguide/java_testing.html#sec:java_test_fixtures) | Gradle 9.8 | Пути проектов, `includeBuild`, TOML-каталоги, `api` против `implementation`, публикация BOM, convention-плагины, отказ от межпроектной конфигурации, общие тестовые помощники |
| [idiomatic-gradle](https://github.com/jjohannes/idiomatic-gradle) | Jendrik Johannes | Эталонная многопроектная настройка |
| [gradle-project-setup-howto](https://github.com/jjohannes/gradle-project-setup-howto) | Jendrik Johannes | Полный пример и видеосерия |
| [java-module-dependencies](https://github.com/gradlex-org/java-module-dependencies) | GradleX | Зависимости Gradle из `module-info.java` |
| [jvm-dependency-conflict-resolution](https://github.com/gradlex-org/jvm-dependency-conflict-resolution) | GradleX | Конфликты capability (например, два логгера) |
| [Gradle build-logic](https://github.com/gradle/gradle/tree/master/build-logic) | Gradle team | Convention-плагины в большом масштабе |
| [Gradle platforms dir](https://github.com/gradle/gradle/tree/master/platforms) | Gradle team | Группировка сотен подпроектов |
| [Spring Framework buildSrc](https://github.com/spring-projects/spring-framework/tree/main/buildSrc) | Spring team | Конвенции и задача ArchUnit |
| [Spring Framework settings.gradle](https://github.com/spring-projects/spring-framework/blob/main/settings.gradle) | Spring team | Плоский список модулей, BOM, платформа |
| [Spring Boot buildSrc](https://github.com/spring-projects/spring-boot/tree/main/buildSrc) | Spring team | Архитектурные проверки, конвенции |
| [spring-boot-dependencies](https://github.com/spring-projects/spring-boot/tree/main/platform/spring-boot-dependencies) | Spring team | Самый часто импортируемый BOM |
| JUnit [gradle/plugins](https://github.com/junit-team/junit-framework/tree/main/gradle/plugins) / [libs.versions.toml](https://github.com/junit-team/junit-framework/blob/main/gradle/libs.versions.toml) | JUnit team | Конвенции во включённых сборках; реальный каталог |
| [Elasticsearch build-tools-internal](https://github.com/elastic/elasticsearch/tree/main/build-tools-internal) | Elastic | Очень большая настройка включённых сборок |
| [Elasticsearch libs/](https://github.com/elastic/elasticsearch/tree/main/libs) | Elastic | Небольшие внутренние библиотеки с явными зависимостями |
| [micronaut-build](https://github.com/micronaut-projects/micronaut-build) | Micronaut team | Конвенции как опубликованный плагин |
| [Kafka build.gradle](https://github.com/apache/kafka/blob/trunk/build.gradle) | Apache Kafka | Контрпример: монолитный корневой скрипт |
| [Keycloak root pom](https://github.com/keycloak/keycloak/blob/main/pom.xml) | Keycloak | Большой реактор Maven |
| Quarkus [extensions](https://github.com/quarkusio/quarkus/tree/main/extensions) / [application BOM](https://github.com/quarkusio/quarkus/blob/main/bom/application/pom.xml) | Quarkus | Разделение runtime/deployment по расширениям; BOM отдельным модулем |

### Что выбрать для сборки и на что смотреть

Выводы ниже следуют из материала раздела.

| Ситуация | Что делать |
|---|---|
| Небольшой проект, мало модулей | Один корневой POM как агрегатор и parent; версии в `dependencyManagement` |
| Нужны потребители вашего BOM | Отдельный модуль BOM (`packaging=pom`, только `dependencyManagement`); в Maven 4 — упаковка `bom` |
| Много модулей на Maven | Реактор, `mvn verify`, `-pl <module> -am`, правила enforcer (`dependencyConvergence`, `requireUpperBoundDeps`) |
| Много модулей на Gradle | `build-logic` как включённая сборка, convention-плагины, version catalog, без `allprojects`/`subprojects` |
| Общие тестовые помощники | `java-test-fixtures` (Gradle), а не модуль `*-test-utils` |
| Несколько модулей JPMS в одном проекте Maven | Maven 4 `<build><sources>` (module-source-hierarchy); в Maven 3 — отдельные модули реактора |
| Версия Java должна быть зафиксирована | Toolchain (Gradle) или `<release>` (Maven) |

Типичные ошибки сборки:

- Корневой проект с исходниками и логикой плагинов: любое изменение инвалидирует всё.
- Версия зависимости записана литералом в модуле: версии расходятся между модулями.
- `api` вместо `implementation` без причины: внутренние типы попадают в compile classpath потребителей.
- Пустые промежуточные проекты Gradle из-за вложенных путей в `include(...)`.
- Репозитории объявлены в каждом проекте: `FAIL_ON_PROJECT_REPOS` в settings это запрещает.
- `${revision}` на Maven 3 без flatten-maven-plugin: опубликованные POM непригодны.

## 2. Упаковка кода: пакеты, видимость, модули JPMS

**Ключевые выводы**

- **Пакет по возможности (или по компоненту), а не по слою.** Группируйте классы по тому, что они делают для бизнеса (`order`, `billing`, `catalog`), а не по технической роли (`controller`, `service`, `repository`). Правило Филиппа Хауэра: удаление возможности должно означать удаление одного пакета. Пакеты по слоям размазывают каждое изменение по всем пакетам и толкают общие DAO к обобщённому коду с кучей параметров.
- **Package-private — главный инструмент инкапсуляции в Java, и он работает только если пакет — это возможность.** Фасад возможности (`OrderService`, `Order`, события) делайте `public`, всё остальное (репозитории, мапперы, JPA-сущности, помощники) — package-private. На этом держатся «package by component» Саймона Брауна и «simple module» в Spring Modulith.
- В Java нет видимости «friend» или «internal» между пакетами. Когда возможности нужны подпакеты, общие типы приходится делать `public`, и компилятор перестаёт их защищать. Складывайте их в подпакет `internal` по соглашению и проверяйте ArchUnit, Spring Modulith или JPMS. Документация Spring Modulith называет именно это ограничение.
- Типичная форма внутри одного артефакта:

```text
com.acme.shop                 Application (main), без логики
com.acme.shop.order           публичный API: OrderManagement, Order, OrderCompleted (событие)
com.acme.shop.order.internal  JPA-сущности, репозитории, мапперы (public только ради связывания)
com.acme.shop.billing         ...
com.acme.shop.shared          крошечные, стабильные типы значений без фреймворков (Money, id); держите малым
```

- **Библиотека: API отдельно от реализации.** Публичный API — в `com.acme.lib`, реализация — в `com.acme.lib.internal` (или `.impl`), SPI для расширителей — в `com.acme.lib.spi`. Guava, JUnit, Netty, Micrometer и Jackson используют варианты этой схемы. Статус публичных типов помечайте `@API(status = STABLE | MAINTAINED | EXPERIMENTAL | INTERNAL | DEPRECATED)` из @API Guardian, как делает JUnit.
- **JPMS (`module-info.java`)** даёт сильную инкапсуляцию на этапе компиляции и выполнения: неэкспортируемые пакеты нельзя прочитать (`package ... is not visible`), рефлексия в пакеты без `opens` падает. Проверено локально (JDK 25, Windows 11).

```java
module com.acme.shop.domain {
    exports com.acme.shop.order;              // API
    exports com.acme.shop.order.spi to com.acme.shop.persistence; // квалифицированный экспорт
    opens com.acme.shop.order.internal to com.fasterxml.jackson.databind; // только рефлексия
    requires static org.jspecify;             // только на этапе компиляции (аннотации)
    uses com.acme.shop.order.spi.PaymentGateway;   // потребитель ServiceLoader
}
```

- Проверено локально (JDK 25, Windows 11): двухмодульная сборка Maven 3.9.16. При `module com.example.shop.domain { exports com.example.shop.order; }` модуль `app`, обратившийся к `com.example.shop.order.internal.OrderStore`, не скомпилировался: `package com.example.shop.order.internal is not visible (package ... is declared in module com.example.shop.domain, which does not export it)`. После удаления лишнего класса сборка и тесты ArchUnit прошли на module path, а `java -p domain.jar;app.jar -m com.example.shop.app/...` запустился. `jlink --add-modules com.example.shop.app --strip-debug --no-header-files --no-man-pages` дал runtime размером 45 МБ: только `java.base` и два модуля приложения.
- **Когда JPMS стоит затрат**: библиотеки, которые хотят закрыть пользователям доступ к внутренностям; приложения, поставляющие `jlink`-runtime (десктоп, CLI, контейнеры с малым образом); платформы со слоями плагинов (`ModuleLayer`). **Часто не стоит** для серверных приложений Spring/Jakarta EE, которые запускаются из fat jar на classpath: фреймворкам нужна глубокая рефлексия (`opens` везде), а одна немодульная зависимость тянет automatic modules. Пакетные инструменты (ArchUnit, Spring Modulith) дают там около 80% пользы. [не проверено] (общий консенсус, см. Colebourne, nipafx)
- **Авторам библиотек**: как минимум добавьте `Automatic-Module-Name` в манифест, чтобы имя модуля было стабильным (обратный DNS, совпадает с корневым пакетом). Colebourne (2018) предупреждает, что даже это меняет то, как Maven размещает jar, поэтому проверяйте на module path. Полный `module-info` делайте, когда базовая версия — Java 11+ (в 2026 практически всегда).
- **Split packages** (один пакет в двух jar) недопустимы на module path. Ищите их через `jdeps --check` или `jdeps -s` и не создавайте в новом коде даже на classpath. [не проверено]
- **Automatic modules**: обычный jar на module path становится модулем с именем из `Automatic-Module-Name` либо, если его нет, из имени файла (нестабильно: не публикуйте модуль, который делает `requires` на имя из файла).
- JDK 25 окончательно принял **module import declarations** (JEP 511): `import module java.base;` импортирует все экспортируемые пакеты модуля. Удобно в скриптах и обучении. В production-коде предпочитайте явные импорты: они сохраняют точность представлений зависимостей в ArchUnit и IDE.
- `--add-opens`/`--add-exports` в скриптах запуска — запах: какая-то библиотека зависит от внутренностей JDK, сильно инкапсулированных JEP 403 (JDK 17). Собирайте их в одном месте и убирайте при обновлении.
- Инструменты: `jdeps` (анализ зависимостей, `--generate-module-info`), `jlink` (свой runtime), `jpackage` (установщики), ModiTect (добавляет `module-info` в немодульные jar), GradleX `java-module-dependencies` (выводит зависимости Gradle из `requires`). С JDK 23 javac не запускает процессоры с class/module path по умолчанию, поэтому задавайте путь процессоров явно (раздел «Версии на ноябрь 2026»).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Package by Feature](https://phauer.com/2020/package-by-feature/) | Philipp Hauer, 2020 (обновлено 2022) | Ясный довод против пакетов по слоям |
| [Simon Brown (сайт, доклады о «Modular Monoliths» / package by component)](https://www.simonbrown.je/) | Simon Brown | Package by component, «architecturally evident coding style» |
| [Clean Architecture Boundaries with Spring Boot and ArchUnit](https://reflectoring.io/java-components-clean-boundaries/) | Tom Hombergs | Подпакеты `api`/`internal`, проверяемые ArchUnit |
| [Spring Modulith fundamentals](https://docs.spring.io/spring-modulith/reference/fundamentals.html) | Spring team | Простые и продвинутые модули, пакеты `internal`, named interfaces |
| [JEP 261 Module System](https://openjdk.org/jeps/261) / [JEP 403 Strong encapsulation](https://openjdk.org/jeps/403) / [JEP 511 Module imports](https://openjdk.org/jeps/511) / [JEP 282 jlink](https://openjdk.org/jeps/282) | OpenJDK | Нормативное описание JPMS; почему ломаются хаки `--add-opens` (17); `import module` (final в 25); runtime-образы |
| dev.java modules series: [overview](https://dev.java/learn/modules/) / [intro](https://dev.java/learn/modules/intro/) / [strong encapsulation](https://dev.java/learn/modules/strong-encapsulation/) / [automatic modules](https://dev.java/learn/modules/automatic-module/) / [services](https://dev.java/learn/modules/services/) / [add-exports/opens](https://dev.java/learn/modules/add-exports-opens/) / [jlink](https://dev.java/learn/jlink/) | Oracle (Nicolai Parlog и др.) | Официальный учебник JPMS: основы, exports против opens, миграция, `uses`/`provides`, запасные выходы, runtime-образы |
| [Code-First Java Module System Tutorial](https://nipafx.dev/java-module-system-tutorial/) | Nicolai Parlog | JPMS на практике |
| [The Java Module System (book)](https://www.manning.com/books/the-java-module-system) | Nicolai Parlog, Manning 2019 | Самое глубокое изложение JPMS |
| [Project Jigsaw quick start](https://openjdk.org/projects/jigsaw/quick-start) | OpenJDK | Минимальные примеры командной строки |
| Colebourne: [JPMS for library developers: negative benefits](https://blog.joda.org/2018/03/jpms-negative-benefits.html) / [JPMS module naming](https://blog.joda.org/2017/04/java-se-9-jpms-module-naming.html) | Stephen Colebourne, 2018/2017 | Честная цена модуляризации библиотек; правило «обратный DNS = корневой пакет» |
| JDK 25 man pages: [jlink](https://docs.oracle.com/en/java/javase/25/docs/specs/man/jlink.html) / [jdeps](https://docs.oracle.com/en/java/javase/25/docs/specs/man/jdeps.html) | Oracle | Справочник по опциям; `--check`, `--generate-module-info` |
| [maven-compiler-plugin: module-info](https://maven.apache.org/plugins/maven-compiler-plugin/examples/module-info.html) | Apache Maven | Компиляция модулей в Maven 3 |
| [Gradle: building modules](https://docs.gradle.org/current/userguide/java_library_plugin.html#sec:java_library_modular) | Gradle | Работа с module path |
| [ModiTect](https://github.com/moditect/moditect) | ModiTect | Дескрипторы модулей для старых jar |

## 3. Архитектурные стили, DDD, модульный монолит и микросервисы

**Ключевые выводы**

- **Начинайте скучно.** Для нового сервиса: один артефакт, пакеты по возможностям, простое наслоение *внутри* каждой возможности (веб, сценарии, хранение), внедрение через конструктор, одна база. Порты и адаптеры добавляйте там, где действительно есть вторая реализация или шов для теста. Стандартные ссылки: «MonolithFirst» и «Microservice Premium» Фаулера; «Choose Boring Technology» Маккинли объясняет бюджет «токенов инноваций».
- **Слоистая** архитектура (представление, сценарии, домен, хранение) подходит, когда домен тонкий (CRUD). Правило: зависимости идут только вниз, типы хранения не протекают в веб-слой. Проверяйте через `layeredArchitecture()` в ArchUnit.
- **Hexagonal / ports & adapters** (Cockburn 2005), **onion** (Palermo 2008) и **clean architecture** (Martin 2012) разделяют одну идею: домен и ядро сценариев не зависят от фреймворков и I/O, адаптеры зависят внутрь. В Java это значит: в модуле ядра нет импортов Spring/JPA; порты — интерфейсы, принадлежащие ядру (`LoadAccountPort`, `SendMoneyUseCase`); адаптеры их реализуют. Цена: больше типов, преобразование между доменной моделью и моделью хранения, лишняя косвенность для CRUD. Платите там, где логика домена богата или инфраструктура будет меняться.
- **Sliced onion** (Drotbohm 2023): у каждого модуля возможности своя небольшая «луковица», адаптеры и тесты могут обращаться к кольцу сценариев напрямую, модули связываются событиями. Главная единица архитектуры — модуль, а не кольцо. Это концептуальная основа Spring Modulith.
- **Vertical slice** (Bogard 2018): организация по запросам/сценариям, «минимизируйте связность между срезами, максимизируйте внутри среза». Начинайте с transaction script и переходите к доменной модели, когда появляются запахи. Нужна команда, которая хорошо умеет рефакторить.
- **Тактические паттерны DDD в Java** (Drotbohm 2020/2021, README jMolecules):
  - Объекты-значения — **records** с валидацией в compact-конструкторе, без сеттеров.
  - Идентификаторы — отдельные типы (`record OrderId(UUID value) implements Identifier`), не сырые `Long`/`UUID`.
  - Корень агрегата: одна публичная точка входа, которая обеспечивает инварианты. Другие агрегаты ссылаются **по id** (или через `Association<Customer, CustomerId>`), не по ссылке на объект. Один репозиторий на корень агрегата, одна транзакция меняет один агрегат.
  - Доменные события — records с именем в прошедшем времени (`OrderCompleted`). Публикуются после коммита (`@TransactionalEventListener` в Spring, `@ApplicationModuleListener` в Modulith или transactional outbox).
  - jMolecules делает роли явными (`@AggregateRoot`/`AggregateRoot<T,ID>`, `@ValueObject`, `@DomainEvent`, `@Repository`, аннотации слоёв/колец/портов/адаптеров). Его правила ArchUnit (`JMoleculesDddRules.all()`, `JMoleculesArchitectureRules.ensureOnionSimple()`, `ensureHexagonal()`) проверяют их. Плагин ByteBuddy умеет генерировать шаблонный код JPA/Spring из этих аннотаций.

```java
@AggregateRoot
public class Order {                                   // корень агрегата: поведение, без сеттеров
    private final OrderId id;
    private final CustomerId customer;                 // ссылка по id, не Customer
    private final List<LineItem> items = new ArrayList<>();
    private Status status = Status.OPEN;

    public OrderCompleted complete(Clock clock) {      // поведение обеспечивает инварианты
        if (items.isEmpty()) throw new IllegalStateException("empty order");
        status = Status.COMPLETED;
        return new OrderCompleted(id, Instant.now(clock));
    }
}
public record OrderId(UUID value) implements Identifier {}
@DomainEvent public record OrderCompleted(OrderId orderId, Instant at) {}
```

- **Модульный монолит** — рекомендация по умолчанию 2026 года для большинства бизнес-систем: модули с явными API, внутренними пакетами, вызовами между модулями через события или API, один артефакт и обычно одна база с владением схемами/таблицами по модулям. Об этом пути пишут Саймон Браун (GOTO 2018), Камиль Гржибек, Thoughtworks и Shopify. Spring Modulith 2.1 — инструмент Spring для этого (раздел 4).
- **Микросервисы** покупают независимое развёртывание и масштабирование ценой распределённых транзакций, сетевых отказов, версионируемых контрактов, наблюдаемости и стоимости платформы. Режьте по ограниченным контекстам, которые уже работают как модули. Для миграций используйте strangler fig, для надёжных событий — transactional outbox.
- **CQRS / event sourcing (Axon)**: полезно, когда журнал событий и *есть* домен (бухгалтерия, процессы, регуляторная история) или нагрузки чтения и записи сильно различаются. По умолчанию избегайте: это конечная согласованность, версионирование событий/upcasting, перестройка проекций, более сложная ментальная модель и обычно хранилище событий (Axon Server). Axon Framework 5 (5.4.1) — текущая линия, Axon 4 (4.13.x) — поддержка. Дешёвая форма CQRS уместна везде: отдельные проекции запросов в DTO (JDBC/Spring Data projections) отдельно от агрегатов на стороне команд. Версии проверены, совет — [не проверено] (консенсус по bliki Фаулера о CQRS и практикам).
- **Functional core, imperative shell** (Gary Bernhardt): чистая логика решений (records на входе, records/события на выходе) внутри, I/O по краям. Хорошо ложится на современную Java (records, sealed-интерфейсы, pattern matching) и делает большую часть логики тестируемой без моков.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/) | Alistair Cockburn, 2005 | Оригинал ports & adapters |
| [The Onion Architecture, part 1](https://jeffreypalermo.com/2008/07/the-onion-architecture-part-1/) | Jeffrey Palermo, 2008 | Оригинал onion |
| [The Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html) | Robert C. Martin, 2012 | Правило зависимостей |
| [Sliced Onion Architecture](https://odrotbohm.de/2023/07/sliced-onion-architecture/) | Oliver Drotbohm, 2023 | Модули как единица, луковица внутри |
| [Vertical Slice Architecture](https://www.jimmybogard.com/vertical-slice-architecture/) | Jimmy Bogard, 2018 | Альтернатива слоям |
| [Explicit Architecture](https://herbertograca.com/2017/11/16/explicit-architecture-01-ddd-hexagonal-onion-clean-cqrs-how-i-put-it-all-together/) | Herberto Graça, 2017 | Сводит DDD, hexagonal, onion, clean, CQRS |
| [A Color Coded Guide to Ports and Adapters](https://8thlight.com/insights/a-color-coded-guide-to-ports-and-adapters) | 8th Light | Наглядное объяснение |
| [Hexagonal Architecture with Java and Spring](https://reflectoring.io/spring-hexagonal/) | Tom Hombergs | Раскладка пакетов Java для hexagonal |
| [Get Your Hands Dirty on Clean Architecture, 2nd ed.](https://reflectoring.io/book/) | Tom Hombergs, 2023 | Книга о buckpal; стратегии маппинга |
| [Presentation Domain Data Layering](https://martinfowler.com/bliki/PresentationDomainDataLayering.html) | Martin Fowler | Слои внутри модулей |
| [Monolith First](https://martinfowler.com/bliki/MonolithFirst.html) | Martin Fowler, 2015 | Почему не начинать с микросервисов |
| [Microservice Premium](https://martinfowler.com/bliki/MicroservicePremium.html) / [Microservices](https://martinfowler.com/articles/microservices.html) | Martin Fowler; Lewis & Fowler 2014 | Порог сложности; определяющая статья |
| [How to break a Monolith into Microservices](https://martinfowler.com/articles/break-monolith-into-microservices.html) | Zhamak Dehghani | Порядок выделения |
| [Strangler Fig Application](https://martinfowler.com/bliki/StranglerFigApplication.html) | Martin Fowler | Постепенная миграция |
| [Modular Monoliths (GOTO 2018)](https://www.youtube.com/watch?v=5OjqD-ow8GE) | Simon Brown | «If you can't build a monolith...» |
| [Modular Monolith: A Primer](https://www.kamilgrzybek.com/blog/posts/modular-monolith-primer) | Kamil Grzybek | Определения, движущие силы |
| [modular-monolith-with-ddd](https://github.com/kgrzybek/modular-monolith-with-ddd) | Kamil Grzybek | Полный пример (C#, идеи переносимы на Java) |
| [When (modular) monolith is the better way](https://www.thoughtworks.com/insights/blog/microservices/modular-monolith-better-way-build-software) | Thoughtworks | Взгляд индустрии |
| Shopify: [Deconstructing the Monolith](https://shopify.engineering/deconstructing-monolith-designing-software-maximizes-developer-productivity) / [Under Deconstruction](https://shopify.engineering/shopify-monolith) | Shopify Engineering | Компонентизация огромного монолита и уроки |
| [Choose Boring Technology](https://mcfunley.com/choose-boring-technology) / [boringtechnology.club](https://boringtechnology.club/) | Dan McKinley, 2015 | Токены инноваций |
| [CUPID: for joyful coding](https://dannorth.net/cupid-for-joyful-coding/) | Dan North | Свойства вместо принципов |
| [Functional Core, Imperative Shell](https://www.destroyallsoftware.com/screencasts/catalog/functional-core-imperative-shell) | Gary Bernhardt | Чистое ядро, I/O снаружи |
| [Implementing DDD Building Blocks in Java](https://odrotbohm.de/2020/03/Implementing-DDD-Building-Blocks-in-Java/) | Oliver Drotbohm, 2020 | Id, ассоциации, репозитории на корень |
| [Architecturally evident code with jMolecules](https://odrotbohm.de/2021/02/Architecturally-evident-code-with-jMolecules/) | Oliver Drotbohm, 2021 | Статья-преемник; обоснование jMolecules |
| [jMolecules](https://github.com/xmolecules/jmolecules) | xMolecules | Аннотации DDD и архитектуры |
| [jmolecules-integrations](https://github.com/xmolecules/jmolecules-integrations) | xMolecules | ArchUnit, ByteBuddy, Spring, Jackson, JPA |
| [Effective Aggregate Design](https://www.dddcommunity.org/library/vernon_2011/) | Vaughn Vernon, 2011 | Малые агрегаты, ссылки по id |
| [DDD Reference](https://www.domainlanguage.com/ddd/reference/) | Eric Evans | Краткие описания паттернов |
| [DDD_Aggregate](https://martinfowler.com/bliki/DDD_Aggregate.html) / [ValueObject](https://martinfowler.com/bliki/ValueObject.html) / [BoundedContext](https://martinfowler.com/bliki/BoundedContext.html) | Martin Fowler | Короткие определения |
| [Anemic Domain Model](https://martinfowler.com/bliki/AnemicDomainModel.html) | Martin Fowler | Анти-паттерн и когда он допустим |
| [Implementing DDD with Spring (Spring I/O 2024)](https://www.youtube.com/watch?v=VGhg6Tfxb60) | Maciej Walkowiak | Практический DDD на Spring |
| [Talk code repo](https://github.com/maciejwalkowiak/implementing-ddd-with-spring-talk) | Maciej Walkowiak | Код к докладу |
| [Domain-Driven Design in Practice](https://www.infoq.com/articles/ddd-in-practice/) | InfoQ | Статья о DDD с уклоном в Java |
| Fowler [CQRS](https://martinfowler.com/bliki/CQRS.html) / [Event Sourcing](https://martinfowler.com/eaaDev/EventSourcing.html); Axon [reference 5.0](https://docs.axoniq.io/axon-framework-reference/5.0/) / [repo](https://github.com/AxonIQ/AxonFramework) | Fowler; AxonIQ | «Большинству систем это не нужно»; главный Java-фреймворк CQRS/ES |
| microservices.io: [Transactional outbox](https://microservices.io/patterns/data/transactional-outbox.html) / [Monolithic architecture](https://microservices.io/patterns/monolithic.html) / [Microservice architecture](https://microservices.io/patterns/microservices.html) | Chris Richardson | Надёжные события; силы и компромиссы обоих стилей |
| [domain-driven-hexagon](https://github.com/sairyss/domain-driven-hexagon) | Sairyss | Большое руководство по DDD/hexagonal (TypeScript; концепции переносимы) |

### Выбор стиля по задаче

Сводка из ключевых выводов выше.

| Задача | Подходящий стиль | Цена |
|---|---|---|
| Тонкий домен, в основном CRUD | Слоистая архитектура внутри возможностей, проверка `layeredArchitecture()` | Низкая; следите, чтобы типы хранения не протекали в веб |
| Богатая доменная логика, инфраструктура будет меняться | Hexagonal / onion / clean | Больше типов, маппинг модели домена и модели хранения |
| Несколько возможностей в одном артефакте | Модульный монолит, sliced onion, Spring Modulith | Нужна дисциплина границ, проверка `verify()` |
| Запросы и сценарии растут независимо | Vertical slice | Нужна команда, которая хорошо рефакторит |
| Журнал событий и есть домен, сильно разные нагрузки чтения и записи | CQRS / event sourcing (Axon) | Конечная согласованность, версионирование событий, перестройка проекций |
| Независимое развёртывание и масштабирование нужны по названной причине | Микросервисы по границам уже работающих модулей | Распределённые транзакции, сеть, контракты, наблюдаемость, платформа |
| Много решающей логики, мало I/O | Functional core, imperative shell | Минимальная; тестируется без моков |

Типичные ошибки дизайна:

- «Hexagonal» с портами, у которых всегда один тривиальный адаптер.
- Агрегат ссылается на другой агрегат объектом, а не по id: транзакция растёт на несколько агрегатов.
- Сырые `Long`/`UUID` как идентификаторы: параметры легко перепутать.
- Синхронные вызовы во внутренности соседнего модуля вместо событий после коммита.
- Микросервисы с первого дня без стабильных границ модулей.

## 4. Автоматическая проверка границ

Относитесь к архитектурным правилам как к тестам: они идут в обычной сборке, валят её и замораживают старые нарушения, чтобы падали только *новые*. Выбирайте самый дешёвый уровень, который держит: модули сборки (компиляция) > JPMS (компиляция и выполнение) > ArchUnit / Spring Modulith (время тестов) > Checkstyle / Error Prone (линт) > ревью.

**Ключевые выводы**

- **ArchUnit 1.5.1** (движки JUnit 5 и 6) импортирует байткод и проверяет правила в обычных unit-тестах. Агент и дополнительный плагин сборки не нужны, в Maven и Gradle работает одинаково.

```java
@AnalyzeClasses(packages = "com.acme.shop", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {
    @ArchTest static final ArchRule internals_stay_internal = freeze(   // FreezingArchRule
        noClasses().that().resideOutsideOfPackage("com.acme.shop.order..")
            .should().dependOnClassesThat().resideInAPackage("com.acme.shop.order.internal..")
            .because("order.internal is an implementation detail"));

    @ArchTest static final ArchRule no_cycles =
        slices().matching("com.acme.shop.(*)..").should().beFreeOfCycles();

    @ArchTest static final ArchRule layers = layeredArchitecture().consideringAllDependencies()
        .layer("Web").definedBy("..web..")
        .layer("Application").definedBy("..application..")
        .layer("Persistence").definedBy("..persistence..")
        .whereLayer("Web").mayNotBeAccessedByAnyLayer()
        .whereLayer("Application").mayOnlyBeAccessedByLayers("Web")
        .whereLayer("Persistence").mayOnlyBeAccessedByLayers("Application");

    // withOptionalLayers(true): без него правило падает, если какого-то кольца или адаптера ещё нет
    @ArchTest static final ArchRule onion = onionArchitecture().withOptionalLayers(true)
        .domainModels("..domain.model..").domainServices("..domain.service..")
        .applicationServices("..application..")
        .adapter("rest", "..adapter.rest..").adapter("persistence", "..adapter.persistence..");

    @ArchTest static final ArchRule no_field_injection = GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;
}
```

- Проверено локально (JDK 25, Windows 11): Maven 3.9.16 + ArchUnit 1.5.1, многомодульный проект.
  1. Правило про внутренний пакет поймало `billing.Invoicer -> order.internal.OrderStore` и показало и тип поля, и вызов конструктора с номерами строк.
  2. После обёртки правила в `freeze(...)` и `archunit.properties` (`freeze.store.default.allowStoreCreation=true`, `freeze.store.default.path=src/test/resources/archunit_store`) сборка прошла и записала текстовое хранилище с ключом по тексту правила. *Новое* нарушение (`billing.Refunder`) затем валило сборку и показывало ровно одно нарушение.
  3. Зависимость `billing <-> order` ломала `beFreeOfCycles()` сообщением `Cycle detected: Slice billing -> Slice order -> Slice billing` со списком рёбер.
  4. (Gradle 9.8.2, JDK 25.0.5) Правило `layeredArchitecture()` выше поймало `web.AccountController`, обходящий `application` и идущий в `persistence` (3 нарушения: поле, конструктор, вызов метода). **`onionArchitecture()` упал на кодовой базе без всех колец** («Layer 'domain model' is empty», 5 нарушений). Добавляйте `.withOptionalLayers(true)`, когда кольца или адаптера ещё нет; с этой опцией правило прошло. `NO_CLASSES_SHOULD_USE_FIELD_INJECTION` прошло.
- Подводные камни ArchUnit: хранилище замороженных нарушений нужно коммитить, а создание хранилища по умолчанию выключено (один раз задайте `allowStoreCreation=true`, затем в CI обычно возвращайте `false`). Смена текста правила или `because(...)` создаёт новую запись в хранилище. `@Disabled` на полях `@ArchTest` не работает, используйте `@ArchIgnore`. В больших кодовых базах настраивайте `cycles.maxNumberToDetect`. Чтобы импортировать только production-код, передавайте `ImportOption.DoNotIncludeTests` (и `DoNotIncludeJars`).
- ArchUnit умеет проверять **компонентную диаграмму PlantUML** (`classes().should(adhereToPlantUmlDiagram(url, consideringAllDependencies()))` со стереотипами пакетов вида `<<..order..>>`): диаграмма и есть правило.
- Большие фреймворки запускают ArchUnit как задачу сборки: в `buildSrc` Spring Framework и Spring Boot есть `ArchitecturePlugin`/`ArchitectureCheck`/`ArchitectureRules` (циклы пакетов, `toLowerCase` без Locale, запрещённые импорты).
- **Spring Modulith 2.1.x** (Spring Boot 4):
  - `ApplicationModules.of(Application.class).verify()` обеспечивает три правила: нет циклов между модулями, нет доступа к внутренним (не-API) пакетам чужого модуля, допустимы только объявленные `allowedDependencies`. `detectViolations()` позволяет отфильтровать результат до исключения.
  - Модули по умолчанию — прямые подпакеты главного пакета. `@ApplicationModule(type = Type.OPEN)` облегчает миграцию старого кода. `@NamedInterface("spi")` в `package-info.java` открывает дополнительные пакеты, ссылка вида `"order :: spi"`.
  - Если jMolecules есть на classpath, его правила DDD/архитектуры тоже запускаются. Дополнительные правила: `VerificationOptions.defaults().withAdditionalVerifications(JMoleculesArchitectureRules.ensureHexagonal(VerificationDepth.STRICT))`.
  - `@ApplicationModuleTest` (режимы запуска `STANDALONE`, `DIRECT_DEPENDENCIES`, `ALL_DEPENDENCIES`), API `Scenario` для потоков на событиях и выбор тестов с учётом изменений (`spring-modulith-junit`, `spring.modulith.test.reference-commit`).
  - `Documenter` пишет диаграммы C4 (по умолчанию) или UML PlantUML и canvas модулей в `<build>/spring-modulith-docs`.
  - События: `@ApplicationModuleListener` = `@Async` + `@Transactional(REQUIRES_NEW)` + `@TransactionalEventListener`. **Event Publication Registry** (стартеры JPA/JDBC/MongoDB/Neo4j) сохраняет публикации в бизнес-транзакции и поддерживает `republish-outstanding-events-on-restart`, режимы завершения `UPDATE`/`DELETE`/`ARCHIVE`, а с 2.0 — состояния `PUBLISHED/PROCESSING/COMPLETED/FAILED/RESUBMITTED`. `@Externalized` отправляет события в Kafka, AMQP, JMS или Spring Messaging. В 2.1 добавлен режим outbox (Namastack Outbox или JobRunr).

```java
class ModularityTests {
    static final ApplicationModules modules = ApplicationModules.of(ShopApplication.class);
    @Test void verifiesModularStructure() { modules.verify(); }
    @Test void writesDocs() { new Documenter(modules).writeModulesAsPlantUml().writeIndividualModulesAsPlantUml(); }
}
```

- **jQAssistant** (CLI 2.9.1) сканирует код в Neo4j и проверяет правила на Cypher/XML. Есть плагин jMolecules и генерация PlantUML. Тяжелее ArchUnit; берите, если нужно ещё и запрашивать зависимости, историю git или метаданные Maven ad hoc.
- **Checkstyle `ImportControl`** даёт дешёвые правила разрешения/запрета импортов по пакетам. Это линт: полные имена классов и рефлексия его обходят. Error Prone (2.52.0) плюс NullAway (0.14.3) с JSpecify (1.0.1) проверяют контракты nullability на границах API. forbidden-apis запрещает небезопасные вызовы JDK (кодировка и локаль по умолчанию).

```xml
<import-control pkg="com.acme.shop">
  <allow pkg="java"/>
  <subpackage name="order"><allow pkg="com.acme.shop.order"/></subpackage>
  <subpackage name="billing"><disallow pkg="com.acme.shop.order.internal"/><allow pkg="com.acme.shop"/></subpackage>
</import-control>
```

  Синтаксис выше взят из документации Checkstyle и локально не запускался. [не проверено]
- **Гигиена зависимостей между модулями и артефактами**:
  - Gradle: **Dependency Analysis Gradle Plugin** (`com.autonomousapps.build-health` 3.20.1 в `settings.gradle.kts`), задачи `buildHealth`, `projectHealth`, `reason`, `fixDependencies`. Сообщает о неиспользуемых зависимостях, использованных, но не объявленных транзитивных зависимостях и ошибках `api`/`implementation`. Проверено локально (JDK 25, Windows 11): на демо-сборке плагин пометил неиспользуемую `implementation 'org.apache.commons:commons-lang3'` как «should be removed». Он также предложил заменить агрегирующие артефакты, добавленные convention-плагином (`junit-jupiter`, `archunit-junit5`), на то, против чего код реально компилируется (`junit-jupiter-api`, `archunit`). Такой шум ожидаем: следуйте совету или настройте исключения в `dependencyAnalysis { issues { ... } }`.
  - Maven: `maven-dependency-plugin:3.11.0:analyze-only` с `<failOnWarning>true</failOnWarning>` в фазе `verify`. Анализ байткода не видит рефлексию, инлайновые константы и использование только в аннотациях; такие случаи вносите в `ignoredUnusedDeclaredDependencies` / `usedDependencies`.
  - Циклы между артефактами внутри реактора Maven невозможны (Maven их отвергает), но могут проникнуть через опубликованные версии. Их ловит MojoHaus `banCircularDependencies`.
- **IntelliJ DSM / Analyze Dependencies** и диаграммы Structurizr/C4 помогают в *поиске*, но ничего не обеспечивают. Используйте их, чтобы найти правила, затем закодируйте правила в ArchUnit или Modulith.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [ArchUnit User Guide](https://www.archunit.org/userguide/html/000_Index.html) | TNG, v1.5.1 | Полный API, архитектуры, заморозка, PlantUML |
| ArchUnit: [Getting started](https://www.archunit.org/getting-started) / [Use cases](https://www.archunit.org/use-cases) / [Architectures](https://www.archunit.org/userguide/html/000_Index.html#_architectures) / [Freezing](https://www.archunit.org/userguide/html/000_Index.html#_freezing_arch_rules) / [PlantUML rules](https://www.archunit.org/userguide/html/000_Index.html#_plantuml_component_diagrams_as_rules) | TNG | Установка, каталог правил, DSL layered/onion, внедрение на старом коде, диаграмма как правило |
| [ArchUnit-Examples](https://github.com/TNG/ArchUnit-Examples) | TNG | Запускаемые примеры правил |
| [ArchUnit releases](https://github.com/TNG/ArchUnit/releases) | TNG | Список изменений |
| [Spring Modulith reference](https://docs.spring.io/spring-modulith/reference/) | Spring team, 2.1.x | Точка входа |
| Spring Modulith pages: [Verification](https://docs.spring.io/spring-modulith/reference/verification.html) / [Events](https://docs.spring.io/spring-modulith/reference/events.html) / [Testing](https://docs.spring.io/spring-modulith/reference/testing.html) / [Documentation](https://docs.spring.io/spring-modulith/reference/documentation.html) / [Appendix/compatibility](https://docs.spring.io/spring-modulith/reference/appendix.html) | Spring team | `verify()`, реестр событий и outbox, `@ApplicationModuleTest`/Scenario, Documenter, матрица версий Boot |
| [spring-modulith-examples](https://github.com/spring-projects/spring-modulith/tree/main/spring-modulith-examples) | Spring team | Официальные примеры |
| [Introducing Spring Modulith](https://spring.io/blog/2022/10/21/introducing-spring-modulith) | Oliver Drotbohm, 2022 | Происхождение и цели |
| [Moduliths (predecessor)](https://github.com/odrotbohm/moduliths) | Oliver Drotbohm | [устарело]: заменён Spring Modulith |
| [jmolecules-archunit](https://github.com/xmolecules/jmolecules-integrations/tree/main/jmolecules-archunit) | xMolecules | `JMoleculesDddRules`, `JMoleculesArchitectureRules` |
| jQAssistant [manual](https://jqassistant.github.io/jqassistant/current/) / [repo](https://github.com/jQAssistant/jqassistant) / [jMolecules plugin](https://github.com/jqassistant-plugin/jqassistant-jmolecules-plugin) | jQAssistant | Правила на графе (Neo4j/Cypher), понятия DDD |
| [Checkstyle ImportControl](https://checkstyle.org/checks/imports/importcontrol.html) | Checkstyle | Разрешения и запреты по пакетам |
| [Error Prone bug patterns](https://errorprone.info/bugpatterns) | Google | Проверки, встроенные в компилятор |
| [NullAway](https://github.com/uber/NullAway) / [JSpecify user guide](https://jspecify.dev/docs/user-guide/) | Uber; JSpecify | Null-безопасность на границах API |
| [forbidden-apis](https://github.com/policeman-tools/forbidden-apis) | Uwe Schindler | Запрет API с кодировкой/локалью по умолчанию |
| [Dependency Analysis Gradle Plugin](https://github.com/autonomousapps/dependency-analysis-gradle-plugin) / [wiki](https://github.com/autonomousapps/dependency-analysis-gradle-plugin/wiki) | Tony Robalik | Неиспользуемые и неверно объявленные зависимости; конфигурация и FAQ |
| Maven [dependency:analyze](https://maven.apache.org/plugins/maven-dependency-plugin/analyze-mojo.html) / [analyze-only](https://maven.apache.org/plugins/maven-dependency-plugin/analyze-only-mojo.html) | Apache Maven | Эквивалент для Maven; вариант, привязанный к жизненному циклу |
| [IntelliJ DSM analysis](https://www.jetbrains.com/help/idea/dsm-analysis.html) | JetBrains | Наглядная матрица зависимостей |
| [C4 model](https://c4model.com/) / [Structurizr docs](https://docs.structurizr.com/) | Simon Brown | Словарь диаграмм; диаграммы как код |
| [Gradle's own architecture README](https://github.com/gradle/gradle/blob/master/architecture/README.md) | Gradle team | Как большой OSS-проект документирует архитектуру |

### Какой инструмент проверки выбрать

| Что нужно | Инструмент | Заметка |
|---|---|---|
| Закрыть доступ на этапе компиляции | Отдельные модули сборки, JPMS | Самый надёжный уровень, если граница действительно нужна |
| Правила пакетов, циклы, слои в любом проекте | ArchUnit | Обычный тест; `freeze(...)` для старых нарушений |
| Модули Spring Boot, события, документация | Spring Modulith | `verify()`, `Documenter`, `@ApplicationModuleTest` |
| Автоматическая проверка ролей DDD | jMolecules + `jmolecules-archunit` | `JMoleculesDddRules.all()`, `ensureHexagonal()` |
| Дешёвые разрешения и запреты импортов по пакетам | Checkstyle `ImportControl` | Линт: полные имена и рефлексия его обходят |
| Контракты nullability на границах API | Error Prone + NullAway + JSpecify | Проверка во время компиляции |
| Неиспользуемые и необъявленные зависимости | Dependency Analysis Gradle Plugin; `dependency:analyze-only` | Ожидайте шум от агрегирующих артефактов |
| Запросы по графу зависимостей, git, Maven | jQAssistant | Тяжелее ArchUnit |
| Найти правила, а не обеспечить их | IntelliJ DSM, Structurizr/C4 | Ничего не обеспечивают; найденное закодируйте в ArchUnit или Modulith |

Типичные ошибки проверки границ:

- Правило пишется после того, как нарушений накопилось много, и без `freeze(...)`: его приходится отключать.
- Хранилище замороженных нарушений не закоммичено, а `allowStoreCreation=true` остался в CI.
- Для `onionArchitecture()` не включено `.withOptionalLayers(true)` при неполных кольцах.
- Правило импортирует тесты (не указан `DoNotIncludeTests`) и ругается на тестовый код.
- Изменили текст `because(...)`: в хранилище появилась новая запись, старая осталась.

## 5. Внедрение зависимостей

**Ключевые выводы**

- **Всегда внедрение через конструктор.** `final`-поля и один конструктор (Spring не требует `@Autowired` для единственного конструктора). Избегайте field injection: его ловит `NO_CLASSES_SHOULD_USE_FIELD_INJECTION` из ArchUnit. Конструктор с более чем примерно 5 параметрами означает, что класс делает слишком много.
- **Ядро без фреймворков**: классы домена и сценариев — обычные объекты Java, создаваемые через `new` в тестах. Контейнер знают только адаптеры и composition root (`@Configuration`, `@Module` в Dagger, `main`). Марк Зееман называет это **Composition Root**. Обращение к контейнеру из бизнес-кода (`ApplicationContext.getBean`, статический `ServiceLocator.get`) — **анти-паттерн Service Locator**: он скрывает зависимости и ломается во время выполнения, а не компиляции.
- **Pure DI** (ручная сборка в `main`) подходит для CLI, библиотек и малых сервисов и ничего не стоит на старте. Контейнер берите, когда важны жизненный цикл, области видимости, привязка конфигурации или AOP (транзакции, безопасность). [не проверено]
- **Выбор контейнера** (версии из раздела «Версии на ноябрь 2026»):

| Контейнер | Модель | Когда |
|---|---|---|
| Spring Framework 7 / Boot 4 | Рефлексия во время выполнения + опция AOT (GraalVM native) | По умолчанию для корпоративных сервисов; самая большая экосистема |
| Quarkus ArC (подмножество CDI Lite) | CDI на этапе сборки, без рефлексии во время выполнения | Приложения Quarkus; быстрый старт/native |
| Micronaut 5 | DI на этапе компиляции через annotation processing | Быстрый старт, native, serverless |
| Dagger 2.60 | Этап компиляции, сгенерированный код, без рефлексии | Android, библиотеки, CLI; строгие графы |
| Avaje Inject 12.7 | Этап компиляции, `jakarta.inject`, сгенерированные классы `$DI`, жизненный цикл | Лёгкие сервисы/CLI; похоже на Spring, но без Spring |
| Guice 7.0 | Рефлексия во время выполнения, модули в коде | Старые кодовые базы на Guice; релизы замедлились (7.0.0 в 2023) |

  Всем контейнерам этапа компиляции на **JDK 23+ нужно явно настроить annotation processor** (`annotationProcessorPaths` или `-proc:full`). Документация Avaje говорит об этом прямо и советует `maven.compiler.proc=full`.
- **Плагины без контейнера: `java.util.ServiceLoader`.** Объявите SPI-интерфейс в API-модуле, зарегистрируйте реализации в `META-INF/services/<fqcn>` (classpath) или через `provides X with Y;` в `module-info` (module path) и загружайте `ServiceLoader.load(X.class).stream()`. Так работают драйверы JDBC, провайдеры SLF4J 2, модули Jackson (`findAndRegisterModules`), движки и расширения JUnit.

```java
// api-модуль
public interface PaymentGateway { String id(); Receipt charge(Money m); }
// потребитель
Map<String, PaymentGateway> gateways = ServiceLoader.load(PaymentGateway.class).stream()
    .map(ServiceLoader.Provider::get)
    .collect(toMap(PaymentGateway::id, g -> g));
```

- Что стоит обеспечивать в Spring: избегайте `@Lazy` и циклических ссылок (Boot по умолчанию запрещает циклы), держите классы `@Configuration` внутри модуля, который они связывают, привязывайте настройки через records с `@ConfigurationProperties`, а не через разбросанные `@Value`. Подробности — в [domains-server.md](domains-server.md).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Inversion of Control Containers and the DI pattern](https://martinfowler.com/articles/injection.html) | Martin Fowler, 2004 | Основа; DI против service locator |
| [Service Locator is an Anti-Pattern](https://blog.ploeh.dk/2010/02/03/ServiceLocatorisanAnti-Pattern/) | Mark Seemann, 2010 | Почему `getBean()` в бизнес-коде вредит |
| [Composition Root](https://blog.ploeh.dk/2011/07/28/CompositionRoot/) | Mark Seemann, 2011 | Собирайте граф в одном месте |
| [Dependency Injection Principles, Practices, and Patterns](https://www.manning.com/books/dependency-injection-principles-practices-patterns) | van Deursen & Seemann, 2019 | Книга целиком (C#, идеи универсальны) |
| [Spring: Dependency Injection](https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html) | Spring team | Внедрение через конструктор и сеттер |
| [Dagger](https://dagger.dev/) / [dev guide](https://dagger.dev/dev-guide/) | Google | DI на этапе компиляции |
| [Guice wiki](https://github.com/google/guice/wiki) / [Motivation](https://github.com/google/guice/wiki/Motivation) | Google | DI во время выполнения, модули |
| [Avaje Inject](https://avaje.io/inject/) | Rob Bygrave | DI на этапе компиляции, замечание о процессорах JDK 23+ |
| [Micronaut IoC](https://docs.micronaut.io/latest/guide/#ioc) | Micronaut | DI на этапе компиляции |
| [Quarkus CDI reference (ArC)](https://quarkus.io/guides/cdi-reference) | Quarkus | Ограничения CDI на этапе сборки |
| [Quarkus intro to CDI](https://quarkus.io/guides/cdi) | Quarkus | Основы |
| [Jakarta Dependency Injection 2.0](https://jakarta.ee/specifications/dependency-injection/2.0/) | Eclipse Foundation | Аннотации `jakarta.inject` |
| [Jakarta CDI 4.1](https://jakarta.ee/specifications/cdi/4.1/) | Eclipse Foundation | Спецификация CDI |
| [ServiceLoader (JDK 25 API)](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/ServiceLoader.html) | Oracle | Механизм плагинов |

## 6. Конфигурация, ошибки на границах, эволюция API

**Ключевые выводы**

- **Конфигурация**: привязывайте один раз при старте к неизменяемым проверяемым объектам (Spring Boot `@ConfigurationProperties` на record с `@Validated`, `@ConfigMapping` SmallRye/MicroProfile Config в Quarkus, avaje-config, Typesafe Config). Падайте быстро при отсутствующих значениях. Секреты держите в переменных окружения или хранилищах секретов, не в репозитории (12-factor). Домен получает значения, а не фреймворк конфигурации.
- **Ошибки**: внутри ядра выражайте ожидаемые бизнес-исходы типами (sealed-иерархии результатов, `Optional`, доменные исключения со смыслом). Переводите на **границе**: одно место отображает их в HTTP (RFC 9457 `ProblemDetail` в Spring 6+), коды выхода CLI или сообщения. Не пропускайте исключения хранения (`DataIntegrityViolationException`, `SQLException`) в веб-слой и никогда не проглатывайте исключения. Непроверяемые исключения — норма для инфраструктурных сбоев. Подробнее — в [idioms-patterns.md](idioms-patterns.md).

```java
sealed interface PlaceOrderResult permits Placed, OutOfStock, Rejected {}
record Placed(OrderId id) implements PlaceOrderResult {}
record OutOfStock(List<Sku> missing) implements PlaceOrderResult {}
record Rejected(String reason) implements PlaceOrderResult {}
// веб-адаптер: switch (result) { case Placed p -> 201; case OutOfStock o -> 409; case Rejected r -> 422; }
```

- **Неизменяемые объекты-значения**: records (JEP 395) с валидацией в compact-конструкторе и защитными копиями (`List.copyOf`). Для закрытых альтернатив — sealed-интерфейсы (JEP 409). Для многих необязательных полей — билдер: RecordBuilder (53) или Immutables (2.12.3); оба — annotation processors (замечание о JDK 23+ действует).
- **Эволюция API библиотек**:
  - Следуйте **semantic versioning** и явно определяйте, что есть API: экспортируемые пакеты (JPMS) или типы с `@API(status = STABLE)`. Статусы `@API Guardian`: `INTERNAL`, `DEPRECATED`, `EXPERIMENTAL`, `MAINTAINED`, `STABLE`, атрибуты `since` и `consumers`. JUnit использует их повсюду.
  - Различайте **бинарную и исходную совместимость** (глава 13 JLS, Eclipse «Evolving Java-based APIs»). Добавление метода в интерфейс ломает реализаторов, если метод не `default`. Смена типа возвращаемого значения ломает бинарную совместимость, даже если исходник компилируется. Добавление проверяемого исключения ломает исходную совместимость. Позднее превращение публичного класса в `final`, `sealed` или record — тоже ломающее изменение.
  - Ставьте на выпуск шлюз **japicmp** (`japicmp-maven-plugin` 0.27.0, цель `cmp` в `verify`). По умолчанию сравнивает с последним релизом. Полезные флаги: `breakBuildOnBinaryIncompatibleModifications`, `breakBuildBasedOnSemanticVersioning`, `excludes` для внутренних пакетов. Gradle: `me.champeau.gradle.japicmp` 0.4.6. Revapi (0.15.1) — альтернатива с более богатой конфигурацией, но редкими релизами.
  - Правила дизайна: минимизируйте публичную поверхность (package-private по умолчанию, `final`-классы, если не проектируются для расширения), возвращайте интерфейсы и неизменяемые коллекции, принимайте самый общий тип, не показывайте в API типы сторонних библиотек (они фиксируют версии у пользователей), объявляйте устаревание через `@Deprecated(since, forRemoval)` за одну мажорную версию до удаления. Страницы Guava «Compatibility» и «Philosophy» показывают зрелую политику.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| Spring Boot [externalized config](https://docs.spring.io/spring-boot/reference/features/external-config.html) / [type-safe properties](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties) | Spring team | Records с `@ConfigurationProperties` и валидацией |
| [SmallRye Config](https://smallrye.io/smallrye-config/Main/) / [MicroProfile Config](https://microprofile.io/specifications/config/) / [avaje-config](https://avaje.io/config/) / [Lightbend Config](https://github.com/lightbend/config) | разные | `@ConfigMapping` (Quarkus), спецификация, малые автономные библиотеки, HOCON |
| [The Twelve-Factor App: Config](https://12factor.net/config) | Heroku | Конфигурация в окружении |
| [Spring MVC error responses (ProblemDetail)](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html) | Spring team | Отображение ошибок на границе |
| [RFC 9457 Problem Details](https://www.rfc-editor.org/rfc/rfc9457) | IETF, 2023 | Стандарт тела ошибки HTTP |
| [JEP 395: Records](https://openjdk.org/jeps/395) / [JEP 409: Sealed Classes](https://openjdk.org/jeps/409) / [Immutables](https://immutables.github.io/) / [RecordBuilder](https://github.com/Randgalt/record-builder) | OpenJDK; Immutables; J. Zimmerman | Объекты-значения, закрытые типы результатов, генерируемые билдеры и withers |
| [Semantic Versioning 2.0.0](https://semver.org/) | Tom Preston-Werner | Семантика версий |
| [JLS 25 ch. 13: Binary Compatibility](https://docs.oracle.com/javase/specs/jls/se25/html/jls-13.html) | Oracle | Нормативные правила |
| [Evolving Java-based APIs](https://wiki.eclipse.org/Evolving_Java-based_APIs) / [part 2](https://wiki.eclipse.org/Evolving_Java-based_APIs_2) | Jim des Rivières (Eclipse) | Классический каталог совместимости |
| [japicmp](https://github.com/siom79/japicmp) / [Maven plugin docs](https://siom79.github.io/japicmp/MavenPlugin.html) / [Gradle plugin](https://github.com/melix/japicmp-gradle-plugin) | Martin Mois; Cédric Champeau | Шлюз бинарной совместимости для Maven и Gradle |
| [Revapi](https://revapi.org/) / [repo](https://github.com/revapi/revapi) | Lukas Krejci | Альтернативный проверяльщик API |
| [@API Guardian](https://github.com/apiguardian-team/apiguardian) / [javadoc](https://apiguardian-team.github.io/apiguardian/docs/current/api/) | API Guardian team | Аннотация статуса API |
| Guava [Compatibility](https://github.com/google/guava/wiki/CompatibilityExplained) / [Philosophy](https://github.com/google/guava/wiki/PhilosophyExplained) | Google | Политика совместимости в реальном проекте; ценности дизайна API |
| [jdeprscan (JDK 25)](https://docs.oracle.com/en/java/javase/25/docs/specs/man/jdeprscan.html) | Oracle | Поиск использования устаревших API JDK |

### Совместимость API: что ломает

Сводка из правил совместимости выше.

| Изменение | Результат |
|---|---|
| Добавить абстрактный метод в интерфейс | Ломает реализаторов |
| Добавить метод `default` | Реализаторов не ломает |
| Сменить тип возвращаемого значения | Ломает бинарную совместимость, исходник может продолжать компилироваться |
| Добавить проверяемое исключение в `throws` | Ломает исходную совместимость |
| Позже сделать публичный класс `final`, `sealed` или record | Ломающее изменение |

Порядок работы с API библиотеки: определить, что является API (экспортируемые пакеты или `@API(status = STABLE)`), сравнивать с прошлым релизом в `verify` (japicmp), объявлять устаревание за одну мажорную версию до удаления (`@Deprecated(since, forRemoval)`), только затем удалять.

## 7. Слои хранения, DTO и транзакции

Это указатель на серверные заметки; глубина — в [domains-server.md](domains-server.md).

**Ключевые выводы**

- **Репозиторий на корень агрегата**, интерфейс принадлежит стороне домена/сценариев (hexagonal) или лежит рядом с возможностью (прагматичный вариант). Репозитории Spring Data в большинстве приложений *и есть* адаптер, поэтому не оборачивайте их во второй, написанный вручную репозиторий, если только домен не должен остаться свободным от JPA.
- **Сущность JPA = доменная модель?** Две защитимые позиции [не проверено]:
  1. *Прагматичная* (команда Spring, Drotbohm, Walkowiak): аннотировать доменные агрегаты напрямую. jMolecules с плагином ByteBuddy умеет генерировать «проводку» JPA из `@AggregateRoot`/`@Entity`/`Identifier`, сохраняя исходник чистым. Дешевле и годится, когда модель хранения подходит домену.
  2. *Строгий hexagonal* (Hombergs/buckpal): отдельные `AccountJpaEntity` и доменный `Account`, маппер в адаптере хранения. Требует слоя преобразования, но домен остаётся свободным от прокси Hibernate, ленивой загрузки и конструкторов без аргументов. Выбирайте, когда инварианты домена богаты или хранилище может смениться.

  Выберите один вариант на модуль и проверяйте ArchUnit (`domain` не должен зависеть от `jakarta.persistence..`).
- **Чтение ≠ запись**: для команд загружайте агрегаты, для запросов проецируйте прямо в DTO-records (проекции Spring Data на интерфейсы/records, конструкторные выражения JPQL, jOOQ, чистый JDBC). Это «дешёвый CQRS», он избегает проблем N+1 и избыточной выборки.
- **Маппинг DTO**: для малых и средних приложений — написанные вручную статические фабричные методы или конструкторы records (`OrderDto.from(order)`): явно, безопасно при рефакторинге, без магии. **MapStruct 1.6.3** (1.7 в beta) — для множества крупных, в основном симметричных преобразований. Он генерирует обычный код на этапе компиляции без рефлексии, а `unmappedTargetPolicy = ReportingPolicy.ERROR` ловит дрейф. На JDK 23+ процессор должен быть в `annotationProcessorPaths` (вместе с `lombok-mapstruct-binding`, если используете Lombok). В новом коде избегайте рефлексивных мапперов (ModelMapper, Dozer): ошибки всплывают во время выполнения. Версия проверена, совет — [не проверено].
- **Границы транзакций** ставятся на сервисы сценариев, не на репозитории и не на контроллеры: один сценарий — одна транзакция — один агрегат. Согласованность между агрегатами или модулями идёт через доменные события после коммита (Event Publication Registry в Modulith или transactional outbox). Не вызывайте удалённые сервисы внутри транзакции БД. Подробнее — в серверных заметках (прокси `@Transactional` в Spring, `REQUIRES_NEW`, выключенный open-session-in-view). [не проверено]

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Spring Data JPA projections](https://docs.spring.io/spring-data/jpa/reference/repositories/projections.html) | Spring team | Проекции на интерфейсы и DTO |
| [Spring Data JDBC](https://docs.spring.io/spring-data/relational/reference/jdbc.html) | Spring team | Ориентация на агрегаты, нет ленивой загрузки |
| [jOOQ](https://www.jooq.org/) | Data Geekery | Типобезопасный SQL для моделей чтения |
| [MapStruct](https://mapstruct.org/) / [reference guide](https://mapstruct.org/documentation/stable/reference/html/) | MapStruct team | Маппинг на этапе компиляции |
| [DTO projections](https://thorben-janssen.com/dto-projections/) | Thorben Janssen | Когда и как проецировать |
| [Best way to map a projection query to a DTO](https://vladmihalcea.com/the-best-way-to-map-a-projection-query-to-a-dto-with-jpa-and-hibernate/) | Vlad Mihalcea | Способы маппинга DTO в JPA |

## 8. Кодовые базы для изучения

Данные GitHub API на 2026-10-20: все репозитории существуют, ни один не архивирован, кроме отмеченного. Читайте их ради *структуры*, не как шаблоны для копирования.

Как выбрать, что читать:

- Нужен baseline обычного приложения: spring-petclinic. Сравните его с версией на микросервисах и оцените цену разреза.
- Нужны hexagonal и DDD: buckpal, ddd-by-examples/library, dddsample-core.
- Нужен модульный монолит: spring-restbucks (Modulith + jMolecules).
- Нужен дизайн библиотеки с маленьким API: caffeine, guava, junit-framework, micrometer.
- Нужны большие сборки: gradle, spring-framework, elasticsearch (включённые сборки и convention-плагины) и kafka как контрпример.
- Нужен паттерн SPI: keycloak (`Provider`/`ProviderFactory`), jackson-databind (`Module`), junit-framework (движки через ServiceLoader).

| Репозиторий | Показываемый стиль | На что смотреть |
|---|---|---|
| [spring-projects/spring-petclinic](https://github.com/spring-projects/spring-petclinic) | Пакеты по возможностям, тонкие слои | Базовое «скучное» приложение Spring Boot (около 9,6 тыс. звёзд) |
| [spring-petclinic/spring-petclinic-rest](https://github.com/spring-petclinic/spring-petclinic-rest) | Слоистый REST, MapStruct | Маппинг DTO, OpenAPI-first |
| [spring-petclinic/spring-petclinic-microservices](https://github.com/spring-petclinic/spring-petclinic-microservices) | Микросервисы (Spring Cloud) | Тот же домен, разрезанный на части: сравните цену |
| [spring-petclinic org (all variants)](https://github.com/spring-petclinic) | Разные | Kotlin, Angular, реактивные и др. |
| [thombergs/buckpal](https://github.com/thombergs/buckpal) | Hexagonal/clean | Порты (`in`/`out`), адаптеры, отдельные сущности JPA, тесты ArchUnit; последний push 2024 |
| [ddd-by-examples/library](https://github.com/ddd-by-examples/library) | DDD + hexagonal, документы event storming | Агрегаты, политики, события, ArchUnit; около 5,9 тыс. звёзд |
| [ddd-by-examples/factory](https://github.com/ddd-by-examples/factory) | DDD, близко к CQRS | Домен планирования производства |
| [odrotbohm/spring-restbucks](https://github.com/odrotbohm/spring-restbucks) | Modulith + jMolecules + hypermedia | Живая витрина Drotbohm |
| [odrotbohm/arch-evident-spring](https://github.com/odrotbohm/arch-evident-spring) | Architecturally evident code | Пример к докладу; **архивирован в 2026** |
| [xmolecules/jmolecules-examples](https://github.com/xmolecules/jmolecules-examples) | jMolecules | Модели на аннотациях и на типах, ByteBuddy |
| [citerus/dddsample-core](https://github.com/citerus/dddsample-core) | Классический DDD (пример Evans с грузами) | Агрегаты, репозитории, доменные события |
| [VaughnVernon/IDDD_Samples](https://github.com/VaughnVernon/IDDD_Samples) | DDD (книга «Implementing DDD») | Ограниченные контексты; старый стиль Java |
| [maciejwalkowiak/implementing-ddd-with-spring-talk](https://github.com/maciejwalkowiak/implementing-ddd-with-spring-talk) | Прагматичный DDD на Spring | Код доклада Spring I/O 2024 |
| [apache/kafka](https://github.com/apache/kafka) | Большой многопроектный Gradle | Разделение `clients` и `server`, публичные и внутренние пакеты; контрпример по Gradle (раздел 1) |
| [netty/netty](https://github.com/netty/netty) | Библиотека, много модулей Maven | Слои `buffer`/`common`/`transport`/`codec-*`, транспорты в духе SPI |
| [ben-manes/caffeine](https://github.com/ben-manes/caffeine) | Малая библиотека, Gradle | Крошечный публичный API, огромная внутренняя реализация, генерируемый код, плотное тестирование |
| [google/guava](https://github.com/google/guava) | Библиотека | `@Beta`, политика совместимости, дизайн на package-private |
| [junit-team/junit-framework](https://github.com/junit-team/junit-framework) | Платформа-библиотека | Разделение API/engine/launcher, статусы `@API`, движки через ServiceLoader, JPMS |
| [FasterXML/jackson-databind](https://github.com/FasterXML/jackson-databind) | Библиотека | SPI модулей (`Module`), версионируемая совместимость |
| [micrometer-metrics/micrometer](https://github.com/micrometer-metrics/micrometer) | Библиотека-фасад | Фасад и реестры бэкендов в отдельных модулях |
| [spring-projects/spring-framework](https://github.com/spring-projects/spring-framework) | Фреймворк | Конвенции `buildSrc` и задача ArchUnit, границы модулей |
| [gradle/gradle](https://github.com/gradle/gradle) | Очень большая сборка | build-logic, платформы, документы в `architecture/` |
| [elastic/elasticsearch](https://github.com/elastic/elasticsearch) | Очень большая сборка Gradle | `libs/` против `modules/` против `x-pack/plugin`, API плагинов |
| [keycloak/keycloak](https://github.com/keycloak/keycloak) | Большой реактор Maven, много SPI | Паттерн SPI `Provider`/`ProviderFactory` |

## Чеклист ревью архитектуры

Используйте при ревью кодовой базы Java или PR, меняющего структуру. Каждый пункт должен проверяться (инструментом, где он назван).

Сборка и модули

- [ ] 1. В корневом проекте/POM нет production-кода; общая логика сборки лежит в convention-плагинах (`build-logic`) или родительском POM, а не в `allprojects`/`subprojects`.
- [ ] 2. Все версии идут из одного места (version catalog, `dependencyManagement` родителя, импортированные BOM); литералов версий в сборках модулей нет.
- [ ] 3. У каждого модуля Maven/Gradle есть причина существовать (отдельный артефакт развёртывания, публикуемый артефакт или нужная кому-то граница компиляции). Нет модуля «utils»/«common», от которого зависит всё.
- [ ] 4. Нет неиспользуемых и необъявленных, но используемых зависимостей (`buildHealth` / `dependency:analyze-only -DfailOnWarning`).
- [ ] 5. `api` и `implementation` (Gradle) или scopes (Maven) выбраны осознанно. Типы сторонних библиотек не протекают через публичные API случайно.
- [ ] 6. Annotation processors объявлены явно (`annotationProcessorPaths`, `annotationProcessor`), поэтому сборки на JDK 23+ не пропускают генерацию кода молча.
- [ ] 7. Сборка использует toolchains или `--release` для фиксированной версии Java; правила enforcer/convergence проходят.

Пакеты и границы

- [ ] 8. Пакеты верхнего уровня — это возможности/ограниченные контексты, а не технические слои.
- [ ] 9. Каждая возможность открывает небольшой публичный API; остальное package-private или в пакетах `internal`, недоступных другим возможностям (правило ArchUnit или `verify()` в Modulith).
- [ ] 10. Нет циклов между возможностями/модулями (`slices().beFreeOfCycles()`, Modulith, реактор Maven).
- [ ] 11. Ядро домена/сценариев не импортирует веб, хранение или пакеты фреймворков (ArchUnit `noClasses().that().resideInAPackage("..domain..").should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta.persistence..")`).
- [ ] 12. Архитектурные правила запускаются в CI как тесты. Старые нарушения заморожены (`FreezingArchRule`), а не проигнорированы, и хранилище замороженных нарушений сокращается.
- [ ] 13. Если используется JPMS: экспортируются только пакеты API, `opens` квалифицирован (`to <framework>`), нет split packages, имя модуля совпадает с корневым пакетом.
- [ ] 14. Библиотеки: публичные типы имеют статус `@API` или лежат только в экспортируемых пакетах; japicmp/Revapi блокирует выпуски, нарушающие semver.

Дизайн

- [ ] 15. Выбранный стиль (слоистый, hexagonal, sliced onion, vertical slice) записан одним абзацем (ADR) и совпадает с кодом. Нет «hexagonal» с портами, у которых всегда один тривиальный адаптер.
- [ ] 16. Агрегаты малы, ссылаются друг на друга по id, и одна транзакция меняет один агрегат.
- [ ] 17. Объекты-значения и идентификаторы — records с валидацией; сырые `String`/`Long` id через границы не передаются.
- [ ] 18. Побочные эффекты между модулями идут через события, публикуемые после коммита (реестр Modulith или outbox), а не через синхронные вызовы во внутренности чужого модуля.
- [ ] 19. CQRS/event sourcing, микросервисы и реактивные стеки обоснованы названным требованием, а не вкусом.
- [ ] 20. Чистая логика решений отделена от I/O (functional core, imperative shell) и покрыта unit-тестами без моков.

DI, конфигурация, ошибки

- [ ] 21. Только внедрение через конструктор; нет field injection, нет `getBean`/service locator в бизнес-коде; один composition root на артефакт развёртывания.
- [ ] 22. Конфигурация привязана один раз к проверяемым неизменяемым объектам; домен не читает переменные окружения и файлы свойств.
- [ ] 23. Ошибки переводятся на границах (HTTP ProblemDetail, код выхода CLI). Исключения хранения не доходят до контроллеров, ни одно исключение не проглатывается.
- [ ] 24. Контракт nullability явен на публичных API (JSpecify + NullAway или документированные возвращаемые `Optional`).

Хранение и API

- [ ] 25. Транзакции ограничены сервисами сценариев, никогда не контроллерами. Нет удалённых вызовов внутри транзакций БД.
- [ ] 26. Пути чтения используют проекции/DTO; сущности не сериализуются в JSON напрямую.
- [ ] 27. Маппинг явный (конструкторы/фабрики или MapStruct с `unmappedTargetPolicy = ERROR`), рефлексивных мапперов нет.
- [ ] 28. Публичные контракты REST/сообщений версионированы и генерируются/проверяются по схеме (OpenAPI/AsyncAPI, consumer-driven contracts). См. [domains-server.md](domains-server.md) и [testing.md](testing.md).

Эксплуатация и документы

- [ ] 29. Диаграммы модулей генерируются из кода (Modulith `Documenter`, PlantUML из ArchUnit, Structurizr), а не рисуются вручную и не забываются.
- [ ] 30. Новый разработчик находит, где живёт возможность, по дереву пакетов меньше чем за минуту.

## Открытые вопросы

- Синтаксис фрагмента Checkstyle `ImportControl` взят из структуры страницы документации и локально не запускался. [не проверено]
- Утверждения о споре «сущность JPA или доменная модель» и совет по маппингу DTO — консенсус практиков, а не один авторитетный источник. [не проверено]
- Maven 4 пока release candidate (4.0.0-rc-7). Перепроверьте детали consumer POM и упаковки `bom` после GA.
- Spring Modulith 2.2 (RC1) и Spring Boot 4.2 (RC2) близки к GA. Перепроверьте таблицу версий после выхода.
