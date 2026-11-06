# Тулчейн Java: JDK, сборка, анализаторы, CI

Справочник по всему, что нужно, чтобы настроить, собрать, проверить, защитить и выпустить современный Java-проект: выбор и установка JDK, Maven (3.9, 3.10, 4.0-rc) и Gradle 9.x, другие сборщики (Bazel, Mill, JBang, запуск исходника без сборки), форматтеры и анализаторы (javac lint, Error Prone, NullAway и JSpecify, SpotBugs, PMD, Checkstyle, Spotless, OpenRewrite, Sonar), безопасность зависимостей и публикация в Maven Central, консольные инструменты JDK и CI на GitHub Actions. В конце лежат готовые шаблоны, которые собраны локально. Актуально на ноябрь 2026: JDK 25 LTS, JDK 27 (последний feature-релиз, не LTS), следующий LTS — JDK 29 (сентябрь 2027); Maven 3.9.16 и 3.10.0 (Maven 4 всё ещё RC), Gradle 9.8.2, Error Prone 2.52.0, NullAway 0.14.3, google-java-format 2.0.0, Spotless 3.11.0 (Maven) и 8.11.0 (Gradle). Пометки: «проверено локально (JDK 25, Windows 11)» значит, что команда выполнена в Git Bash на Temurin 25.0.5+8 с Maven 3.9.16 / 3.10.0 и Gradle 9.8.2; `[не проверено]` значит, что источник один вторичный или страница не открывалась; `[устарело]` значит, что инструмент заменён, замена названа рядом. Тесты, JUnit и Mockito — в [testing.md](testing.md); JMH, профилирование, GC и AOT — в [performance.md](performance.md); идиомы языка — в [idioms-patterns.md](idioms-patterns.md); модули и слои — в [architecture.md](architecture.md); виртуальные потоки и FFM — в [concurrency-native.md](concurrency-native.md); серверные фреймворки — в [domains-server.md](domains-server.md); десктоп, CLI и мобильные платформы — в [domains-client.md](domains-client.md); порядок работы агента — в [pipeline.md](pipeline.md); книги — в [books.md](books.md).

- [Стек по умолчанию](#стек-по-умолчанию)
- [Версии на ноябрь 2026](#версии-на-ноябрь-2026)
- [1. Дистрибутивы JDK и сроки поддержки](#1-дистрибутивы-jdk-и-сроки-поддержки)
- [2. Установка и управление JDK](#2-установка-и-управление-jdk)
- [3. Maven: 3.9, 3.10 и 4.0-rc](#3-maven-39-310-и-40-rc)
- [4. Gradle 9.x](#4-gradle-9x)
- [5. Другие сборщики и запуск без сборки](#5-другие-сборщики-и-запуск-без-сборки)
- [6. Форматирование и статический анализ](#6-форматирование-и-статический-анализ)
- [7. Безопасность цепочки поставок и публикация в Maven Central](#7-безопасность-цепочки-поставок-и-публикация-в-maven-central)
- [8. Консольные инструменты JDK](#8-консольные-инструменты-jdk)
- [9. CI на GitHub Actions](#9-ci-на-github-actions)
- [Грабли: сводка](#грабли-сводка)
- [Соседние справочники](#соседние-справочники)
- [Минимальные проверки качества](#минимальные-проверки-качества)
- [Шаблоны](#шаблоны)

## Стек по умолчанию

| Решение | По умолчанию | Альтернатива | Не брать |
|---|---|---|---|
| JDK | Eclipse Temurin 25 (LTS, поддержка до сентября 2031) | Corretto (AWS), Zulu (самые длинные сроки), Liberica (Alpine, JavaFX), Microsoft Build of OpenJDK (Azure), SapMachine, Red Hat build | Oracle JDK 17 и 21 для продакшена (лицензия OTN); JDK 26 (EOL); non-LTS в продакшене без плана обновления раз в полгода |
| Целевая версия | Приложения: 25. Библиотеки: baseline 17 или 21 через `--release`, тесты на 17, 21, 25 и 27 | JDK 27 как ранний сигнал перед LTS 29 | Сборка библиотеки без `--release` |
| Сборщик | Maven 3.9.16 или Gradle 9.8.2 (Kotlin DSL), оба с wrapper | Maven 3.10.x (после 3.10.1), Bazel (большие монорепозитории), Mill | Maven 4 RC в критичном CI; Gradle 9.8.0 (уязвимости); Ant + Ivy в новых проектах |
| Управление JDK | Gradle toolchains + foojay-resolver 1.0.0; SDKMAN! или mise на машине разработчика | Maven toolchains, `winget`, Scoop | Сборка, которая зависит от JDK разработчика по умолчанию |
| Компиляция | `--release N` + `-Xlint:all -Werror` | — | `source`/`target` без `release` |
| Статический анализ | Error Prone 2.52.0 + NullAway 0.14.3 (JSpecify mode) | SpotBugs + FindSecBugs, PMD + CPD, Checker Framework, Sonar | Checkstyle и форматтер с конфликтующими правилами |
| Форматирование | Spotless + google-java-format 2.0.0 | palantir-java-format 2.103.0 | Смесь двух форматтеров |
| Покрытие | JaCoCo 0.8.16, порог 80% по строкам | — | `${argLine}` вместо `@{argLine}` (агент теряется) |
| Уязвимости зависимостей | OSV-Scanner v2.7.0 | OWASP Dependency-Check 13.0.0 (нужен NVD API key) | Dependency-Check без NVD API key |
| Обновления | Dependabot | Renovate | Ручные обновления без бота |
| Миграции версий | OpenRewrite с закреплённой версией | — | Запуск OpenRewrite на каждой сборке |
| Fat jar | `maven-shade-plugin` 3.6.2; Gradle `com.gradleup.shadow` 9.6.1 | `maven-assembly-plugin` 3.8.0 для архивов | `com.github.johnrengelman.shadow` `[устарело]` |
| Публикация в Central | Central Portal: `central-publishing-maven-plugin` 0.12.0 или `com.vanniktech.maven.publish` 0.37.0 | `com.gradleup.nmcp` 1.6.2, JReleaser | OSSRH и `nexus-staging-maven-plugin` `[устарело]` (OSSRH закрыт 2025-06-30) |
| CI | GitHub Actions: `setup-java@v6` с `distribution: temurin`, `verify` / `build` | — | `setup-java` v1–v4 (deprecated) |
| Скрипты | `java Foo.java` (JEP 330/458/512) | JBang | Сборка проекта ради одного скрипта |

## Версии на ноябрь 2026

Версии проверены 2026-11-06. Строки «metadata» прочитаны из `maven-metadata.xml` на repo.maven.apache.org, plugins.gradle.org/m2, services.gradle.org/versions или через GitHub releases API.

| Компонент | Версия, дата | Заметка |
|---|---|---|
| JDK, последний feature-релиз | 27 (GA 2026-09-15) | Не LTS. Upstream-поддержка заканчивается с GA JDK 28 (март 2027). Adoptium API «latest» для Windows x64 всё ещё отдаёт GA-сборку 27+35. GitHub-тег temurin27-binaries читается как jdk-27.0.1.0.2+1, поэтому октябрьское обновление может зависеть от платформы `[не проверено]`. |
| JDK, последний LTS | 25 (GA 2025-09-16) | Temurin 25.0.5+8 вышел 2026-10-22 (октябрьский CPU). Поддержка Temurin — минимум до сентября 2031. |
| JDK в разработке | 28 (GA ожидается в марте 2027) | Следующий LTS — **29 (сентябрь 2027)** по дорожной карте Oracle. |
| JDK 26 | GA 2026-03-17, **EOL** | Заменён на 27. Не оставайтесь на мёртвом non-LTS. |
| JDK 21 LTS | Temurin 21.0.13 | Поддержка Temurin минимум до декабря 2029. **Обновления Oracle JDK 21 с 2026-10-20 идут по лицензии OTN (не бесплатно для продакшена).** |
| JDK 17 LTS | Temurin 17.0.21 | Поддержка Temurin минимум до октября 2027. Oracle JDK 17 под OTN с октября 2024. |
| Maven 3.9.x | **3.9.16** (2026-05-17) | Консервативная база. Работает на Java 8+. |
| **Maven 3.10.0** | **2026-10-01** | Новая линия. Resolver 2.x (как в Maven 4). Воспроизводимые сборки и UTF-8 по умолчанию. Валидация POM строже: дубликаты зависимостей и плагинов роняют сборку. Classpath упорядочен вширь (breadth-first). |
| Maven 4 | **4.0.0-rc-7** (2026-09-24). **Не GA.** | Модель POM 4.1.0, разделение build/consumer POM, `mvnup`. На 2026-11-06 это всё ещё release candidate. |
| Maven Wrapper | 3.3.4 | `mvn wrapper:wrapper -Dtype=only-script` (тип по умолчанию). |
| Gradle | **9.8.2** (2026-10-19). Есть 9.9.0-rc-1. | Kotlin DSL — по умолчанию в `gradle init`. Configuration cache — предпочтительный режим. |
| foojay-resolver-convention | 1.0.0 | Settings-плагин, который сам скачивает toolchains. |
| maven-compiler-plugin | **3.16.0** (линия 3.x). 4.0.0-beta-5 — для Maven 4. | `<release>`. На JDK 23+ используйте `annotationProcessorPaths`. |
| surefire / failsafe | 3.6.1 | |
| maven-enforcer-plugin | 3.7.0 | |
| versions-maven-plugin | 2.24.0 | |
| dependency / shade / assembly | 3.11.0 / 3.6.2 / 3.8.0 | |
| jar / source / javadoc / install / deploy / resources / clean | 3.5.1 / 3.4.0 / 3.12.0 / 3.2.0 / 3.2.0 / 3.5.0 / 3.5.0 | Последние 3.x. Их линии `4.0.0-beta-*` — только для Maven 4. |
| maven-site-plugin | 3.22.0 (3.x). Есть 4.0.0-M16. | |
| central-publishing-maven-plugin | 0.12.0 | Публикация через Sonatype Central Portal. |
| maven-gpg-plugin | 3.2.8 | |
| maven-artifact-plugin | 3.7.0 | `artifact:compare` проверяет воспроизводимость. |
| maven-build-cache-extension | 1.3.0 | |
| JaCoCo | **0.8.16** | |
| Error Prone | **2.52.0** (2026-11-02) | **Запускается только на JDK 21+** (с 2.43). На JDK 21 нужен `-XDaddTypeAnnotationsToSymbol=true`. |
| NullAway | **0.14.3** | Нужны Error Prone 2.36+ и JDK 17+. JSpecify mode лучше всего работает на новейшем JDK. |
| JSpecify | 1.0.1 | `@NullMarked`, `@Nullable`. |
| Плагины Gradle для Error Prone / NullAway | net.ltgt.errorprone 5.2.0 / net.ltgt.nullaway 3.3.0 | |
| SpotBugs | 4.10.5. Maven-плагин 4.10.5.0. Gradle-плагин `com.github.spotbugs` 6.5.13. | FindSecBugs 1.14.0. |
| PMD | 7.29.0. maven-pmd-plugin 3.28.0. | |
| Checkstyle | 14.4.0. maven-checkstyle-plugin 3.6.0. | |
| Spotless | Maven 3.11.0. Gradle `com.diffplug.spotless` 8.11.0. | |
| google-java-format | **2.0.0** (2026-10-30) | Новый major. Переформатирует больше кода и включает проход «cleanup» по умолчанию. При обновлении переформатируйте весь репозиторий одним коммитом. |
| palantir-java-format | 2.103.0 | |
| OpenRewrite | rewrite-maven-plugin 6.46.1. Gradle `org.openrewrite.rewrite` 7.41.0. rewrite-migrate-java 3.42.1. | Новые релизы вне Maven Central, см. [раздел 6](#6-форматирование-и-статический-анализ). |
| OWASP Dependency-Check | 13.0.0 (Maven и Gradle) | Для приемлемой скорости нужен NVD API key. |
| CycloneDX | Maven-плагин 2.9.3. Gradle `org.cyclonedx.bom` 3.5.1. | |
| OSV-Scanner | v2.7.0 (2026-10-12) | |
| Публикация в Central из Gradle | `com.vanniktech.maven.publish` 0.37.0 (лежит на Maven Central, **не** на plugin portal, где 0.13.0 — устаревшая копия 2020 года). `com.gradleup.nmcp` 1.6.2. | |
| Shadow (fat jar для Gradle) | `com.gradleup.shadow` 9.6.1 | Заменяет `com.github.johnrengelman.shadow` `[устарело]`. |
| JUnit | 6.1.3 (BOM `org.junit:junit-bom`) | См. [testing.md](testing.md). |
| GitHub Actions | actions/checkout v7.0.1, actions/setup-java **v6.2.0**, gradle/actions **v6.4.1** | |
| JBang / Mill / Bazel | 0.143.0 / 1.3.1 / 9.3.2 | |
| SDKMAN! CLI / mise | 5.24.0 / v2026.10.15 | |
| GraalVM CE | graal-25.4.4.1.1 (сентябрь 2026) | |

## 1. Дистрибутивы JDK и сроки поддержки

**Ключевые выводы**
- **Все основные дистрибутивы собраны из одного исходного кода OpenJDK.** Они различаются сроком поддержки, лицензией, набором платформ, дополнениями (JavaFX в Liberica Full и Zulu FX, сборки с CRaC в Zulu и Liberica) и платной поддержкой. Поведение и скорость практически одинаковы. Два исключения: **Semeru использует VM OpenJ9** вместо HotSpot, а **GraalVM** добавляет Graal JIT и `native-image`.
- **Выбор по умолчанию: Eclipse Temurin (Adoptium).**
  - Нейтрален к вендорам и сертифицирован TCK.
  - Лучшая поддержка в CI: `setup-java` с `distribution: temurin` и официальные Docker-образы `eclipse-temurin`.
  - LTS 25 поддерживается минимум до сентября 2031.

  Хорошие альтернативы:
  - **Amazon Corretto**: для AWS; 25 поддерживается до октября 2032.
  - **Azul Zulu**: самые длинные сроки (25 до сентября 2033) и самая широкая матрица ОС и архитектур.
  - **BellSoft Liberica**: сборки для Alpine (musl) и пакет с JavaFX.
  - **Microsoft Build of OpenJDK**: для Azure; 25 до сентября 2030.
  - **SapMachine**: сборка SAP.
  - **Red Hat build of OpenJDK**: для RHEL и OpenShift.
- **Лицензионная ловушка Oracle JDK.** Бесплатная лицензия Oracle NFTC покрывает LTS только до года после выхода следующего LTS. Слова Oracle: *"Further update releases of Oracle JDK 17, released as of October 15, 2024, and Oracle JDK 21 releases as of October 20, 2026, are available under the Java SE OTN license"*. Лицензия OTN не бесплатна для продакшена. Oracle JDK 25 пока под NFTC. Для бесплатных обновлений 17 и 21 берите Temurin, Corretto или Zulu. GPL-сборки «Oracle OpenJDK» на `jdk.java.net` обновляются только шесть месяцев на релиз.
- **Non-LTS (26, 27, ...) получают upstream-обновления шесть месяцев.** Все вендоры JDK 26 (Temurin, Corretto, Zulu, Liberica, Oracle) закончили его поддержку в сентябре 2026. Запускайте non-LTS в продакшене, только если готовы обновляться каждые полгода. Библиотеки должны **компилироваться с `--release <старший поддерживаемый LTS>`** и тестироваться на новейшем JDK в матрице CI.
- **Какую версию выбрать (ноябрь 2026):**
  - Новые приложения: **25**.
  - Библиотеки: baseline **17** или **21**, в CI тесты на 17, 21, 25 и 27. Spring Boot 4 и Jakarta EE 11 требуют 17 или новее `[не проверено]` (см. [domains-server.md](domains-server.md)).
  - JDK 27 даёт ранние предупреждения перед следующим LTS, это 29 (сентябрь 2027).
- **GraalVM:** Oracle GraalVM (лицензия GFTC) и GraalVM Community (`graalvm-ce-builds`, последний graal-25.4.4.1.1) дают `native-image`. Для нативной сборки Spring, Quarkus или Micronaut берите дистрибутив GraalVM, названный в документации фреймворка. Mandrel — дистрибутив Red Hat только с native-image, его использует Quarkus. См. [domains-server.md](domains-server.md).
- **OpenJ9 / Semeru:** меньше памяти и быстрее старт с общими кэшами классов. Диагностика и флаги `-XX` отличаются от HotSpot (поддержка JFR не на уровне HotSpot), поэтому советы по тюнингу HotSpot не переносятся.
- **Версии программно:** Adoptium API (`api.adoptium.net`), foojay Disco API (на нём работают Gradle toolchain resolver и `setup-java`) и endoflife.date (`/api/v1/products/<name>`).

Конец поддержки по дистрибутивам (endoflife.date, 2026-11-06):

| Дистрибутив | 17 | 21 | 25 | 27 (non-LTS) |
|---|---|---|---|---|
| Eclipse Temurin | 2027-10 | 2029-12 | 2031-09 | 2027-03 |
| Amazon Corretto | 2029-10 | 2030-10 | 2032-10 | 2027-04 |
| Azul Zulu (community) | 2029-09 | 2031-09 | 2033-09 | 2027-03 |
| BellSoft Liberica | 2027-10 | нет в ленте | 2031-09 | 2027-03 |
| Microsoft OpenJDK | 2027-09 | 2028-09 | 2030-09 | не собирается |
| SapMachine | EOL 2026-09 | 2028-09 | 2030-09 | 2027-03 |
| IBM Semeru (OpenJ9) | 2027-10 | 2029-12 | 2031-09 | 2027-03 |
| Oracle JDK (Premier) | закончилась 2026-09 | 2028-09 (теперь лицензия OTN) | 2030-09 | 2027-03 |
| Red Hat build of OpenJDK | 2027-12 | 2029-12 | 2030-12 | не собирается |

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Eclipse Temurin Support](https://adoptium.net/support/) | Eclipse Adoptium, 2026 | Таблица сроков поддержки Temurin |
| [Eclipse Temurin Releases](https://adoptium.net/temurin/releases/) | Eclipse Adoptium | Загрузки (zip, msi, tar.gz) для всех ОС и архитектур |
| [Adoptium API (Swagger UI)](https://api.adoptium.net/q/swagger-ui/) | Eclipse Adoptium | REST API для скриптовой загрузки JDK и проверки версий |
| [Oracle Java SE Support Roadmap](https://www.oracle.com/java/technologies/java-se-support-roadmap.html) | Oracle, 2026 | Ритм LTS (следующий — 29) и переход 17 и 21 с NFTC на OTN |
| [Eclipse Temurin: end of life](https://endoflife.date/eclipse-temurin) | endoflife.date | Таблицы EOL по вендорам и JSON API (также /amazon-corretto, /azul-zulu и другие) |
| [Which JDK?](https://whichjdk.com/) | Jochen Christ | Пристрастный гид по выбору JDK, охватывает всех вендоров |
| [jdk.java.net](https://jdk.java.net/) | Oracle / OpenJDK | GA- и early-access-сборки OpenJDK (27 GA, 28 EA) |
| [JDK 27](https://openjdk.org/projects/jdk/27/) | OpenJDK, 2026 | JEP в JDK 27 и расписание |
| [JDK 28](https://openjdk.org/projects/jdk/28/) | OpenJDK, 2026 | Расписание JDK 28 и JEP (GA в марте 2027) |
| [JDK 25](https://openjdk.org/projects/jdk/25/) | OpenJDK, 2025 | Список JEP текущего LTS |
| [Amazon Corretto](https://aws.amazon.com/corretto/) | Amazon | Загрузки и FAQ Corretto |
| [Amazon Corretto documentation](https://docs.aws.amazon.com/corretto/) | Amazon | Руководства и календарь поддержки Corretto |
| [Azul Downloads](https://www.azul.com/downloads/) | Azul | Сборки Zulu, включая варианты с JavaFX и CRaC |
| [Liberica JDK](https://bell-sw.com/libericajdk/) | BellSoft | Liberica, включая Full-сборки с JavaFX и сборки musl/Alpine |
| [Microsoft Build of OpenJDK](https://learn.microsoft.com/java/openjdk/) | Microsoft | Microsoft Build of OpenJDK и рекомендации для Azure |
| [SapMachine](https://sap.github.io/SapMachine/) | SAP | Загрузки SapMachine и политика поддержки |
| [IBM Semeru Runtimes](https://developer.ibm.com/languages/java/semeru-runtimes/) | IBM | Рантаймы Semeru (OpenJ9) |
| [GraalVM](https://www.graalvm.org/) | Oracle GraalVM team | Документация GraalVM и Native Image |
| [Java Version Almanac](https://javaalmanac.io/) | Marc R. Hoffmann | Различия API и список возможностей по версиям; отвечает на вопрос «с какого JDK?» |
| [A guide to Java versions and features](https://www.marcobehler.com/guides/a-guide-to-java-versions-and-features) | Marco Behler | Обзор дистрибутивов и версий простым языком |
| [foojay.io](https://foojay.io/) | Foojay community | Disco API для Gradle toolchains и новости JDK |

## 2. Установка и управление JDK

**Ключевые выводы**
- **На машине разработчика:**
  - **SDKMAN!** (macOS, Linux, WSL; CLI 5.24.0): `sdk install java 25.0.5-tem` и `sdk env` с файлом `.sdkmanrc` на проект.
  - **mise** (кроссплатформенный, включая Windows): `mise use java@temurin-25` и `mise.toml` на проект.
  - **jenv** только переключает уже установленные JDK, сам не скачивает.
  - **Windows:** `winget install EclipseAdoptium.Temurin.25.JDK` (MSI заодно задаёт `JAVA_HOME`), Scoop или портативный zip с явным `JAVA_HOME`. Проверено локально (JDK 25, Windows 11): шаблоны ниже собраны из портативного zip; Maven и Gradle не требуют ничего, кроме `JAVA_HOME`.
- **Сборка не должна зависеть от JDK разработчика по умолчанию.**
  - **Gradle toolchains:** `java { toolchain { languageVersion = JavaLanguageVersion.of(25) } }` плюс settings-плагин `org.gradle.toolchains.foojay-resolver-convention` 1.0.0, который скачивает недостающие JDK. Сам Gradle может работать на другом JDK; для Gradle 9 нужен 17 или новее.
  - **Maven toolchains:** `~/.m2/toolchains.xml` и `maven-toolchains-plugin` 3.3.0. Его цель `select-jdk-toolchain` (с 3.2 `[не проверено]`) выбирает JDK по версии *без* `toolchains.xml`, находя установленные JDK. Проще запустить Maven на целевом JDK и закрепить его правилом enforcer `requireJavaVersion`.
  - Изменение в Maven 3.10: если плагин запрашивает неверно настроенный toolchain, сборка теперь *падает*, а не молча берёт JDK, на котором запущен Maven.
- **Грабли (проверено локально, JDK 25, Windows 11): в Gradle 9.x форма `-Porg.gradle.java.installations.paths=...` объявлена устаревшей** («scheduled to be removed in Gradle 10»). Используйте `-Dorg.gradle.java.installations.paths=...` или `gradle.properties`. Чтобы CI не скачивал JDK, задайте `org.gradle.java.installations.auto-download=false` и укажите в `installations.paths` (или `fromEnv`) JDK, которые поставил `setup-java`.
- **CI:** `actions/setup-java@v6` с `distribution: temurin` и `java-version: 25`. Многострочный `java-version` ставит несколько JDK: последний становится `JAVA_HOME`, остальные экспортируются как `JAVA_HOME_<ver>_<arch>`. Вход `cache: maven|gradle|sbt` кэширует зависимости; для Gradle лучше `gradle/actions/setup-gradle`.
- **Docker:** `eclipse-temurin:25-jre` или `25-jdk`, а также варианты `-alpine` и `-noble`. Для маленьких образов соберите свой рантайм через `jlink` ([раздел 8](#8-консольные-инструменты-jdk)).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [SDKMAN!](https://sdkman.io/) | SDKMAN! | Ставит и переключает JDK, Maven, Gradle и JBang; `.sdkmanrc` на проект |
| [mise: Java](https://mise.jdx.dev/lang/java.html) | jdx (mise) | Кроссплатформенный менеджер версий с поддержкой Java и `mise.toml` |
| [jenv](https://www.jenv.be/) | jenv | Переключение между установленными JDK (macOS и Linux) |
| [winget](https://learn.microsoft.com/windows/package-manager/winget/) | Microsoft | Основы winget; пакеты Temurin, Corretto, Zulu и Microsoft JDK |
| [Gradle: Toolchains for JVM projects](https://docs.gradle.org/current/userguide/toolchains.html) | Gradle, 2026 | Выбор toolchain, авто-скачивание и свойства; справочник |
| [gradle/foojay-toolchains](https://github.com/gradle/foojay-toolchains) | Gradle | README и настройка settings-плагина foojay resolver |
| [Guide to Using Toolchains](https://maven.apache.org/guides/mini/guide-using-toolchains.html) | Apache Maven | `toolchains.xml` и как плагины выбирают JDK |
| [Apache Maven Toolchains Plugin](https://maven.apache.org/plugins/maven-toolchains-plugin/) | Apache Maven | Поиск JDK через `select-jdk-toolchain` без `toolchains.xml` |
| [actions/setup-java](https://github.com/actions/setup-java) | GitHub | Дистрибутивы, несколько JDK, вход `cache`, генерация настроек Maven |

## 3. Maven: 3.9, 3.10 и 4.0-rc

**Ключевые выводы**
- **Сейчас три линии (ноябрь 2026):**
  - **3.9.16** (май 2026) — консервативная база.
  - **3.10.0** (2026-10-01) — новая стабильная линия. Переходит на Resolver 2.x, тот же, что в Maven 4.
  - **4.0.0-rc-7** (2026-09-24) — всё ещё release candidate. **Maven 4 не GA** на 2026-11-06; страница «What's new in Maven 4» по-прежнему указывает на rc-7.

  Новые проекты можно начинать на 3.9.16 или 3.10.x. Maven 4 пробуйте в отдельной ветке.
- **Что меняет Maven 3.10.0 (release notes):**
  - **Воспроизводимые сборки включены по умолчанию**: Super POM теперь задаёт `project.build.outputTimestamp`.
  - `sourceEncoding` и `reportingOutputEncoding` по умолчанию **UTF-8**.
  - **Строгая валидация POM**: дубликаты зависимостей и плагинов роняют сборку. Выход: `-Dmaven.resolver.validation=off`.
  - **Classpath упорядочен вширь (level-order)** вместо вглубь. Старый порядок возвращает `-Daether.system.dependencyVisitor=preOrder`.
  - Из Super POM убраны plugin management и `release-profile`, поэтому **версии всех плагинов закрепляйте сами**.
  - Учётные данные серверов привязаны к origin репозитория.
  - Новые флаги политики обновления: `-UA` и `-UM`.
  - Расширения можно настраивать на пользователя (`~/.m2/extensions.xml`).
  - Maven по-прежнему работает на Java 8.
- **Грабли (проверено локально, JDK 25, Windows 11): Maven 3.10.0 не запускается, если путь установки содержит `--`.** Причина: 3.10 подставляет `${maven.conf}` в штатный `conf/settings.xml`, в том числе внутрь XML-комментария. Путь вроде `C:\...\c--Claude-...` вносит `--` в комментарий, и XML-парсер отвергает файл: «Non-parseable settings ... in comment after two dashes». 3.9.16 не затронут. Это apache/maven#13321, закрыто и исправлено после 3.10.0. **Обход:** установите Maven в путь без `--`. Другие ранние регрессии 3.10.0, все закрыты: #13385 (prefix filter блокирует артефакты), #13390 (активация профилей в POM зависимостей), #13084 (resteasy-client). **Для критичного CI дождитесь 3.10.1.** `[не проверено]` (названия issue из GitHub API)
- **Грабли (проверено локально, JDK 25, Windows 11): в XML-комментариях нельзя писать `--`.** Комментарий `<!-- use --release 25 -->` делает POM нечитаемым. Пишите `release 25`.
- **Главное в Maven 4:**
  - Для запуска нужна **Java 17**, но можно компилировать под старые цели.
  - Модель POM **4.1.0** (необязательна). **Build POM и consumer POM разделены**: в Central по-прежнему уходит consumer POM версии 4.0.0. `maven.consumer.pom.flatten=true` делает его плоским.
  - `<subprojects>` вместо `<modules>`; вывод родителя; подпроекты находятся автоматически.
  - Встроенный CI-friendly `${revision}`, поэтому `flatten-maven-plugin` не нужен.
  - Фазы `before:`/`after:` заменяют `pre-*`/`post-*`.
  - Активация профилей через `<condition>`, `--fail-on-severity WARN`, `-r` для продолжения.
  - `mvnup` (обновление), `mvnenc` (шифрование), `mvnsh` (shell).
  - **Плагины, зависящие от Plexus DI, ломаются**, а **Maven 4 предупреждает, если вы полагаетесь на версии плагинов по умолчанию**.
  - Линии плагинов 4.0.0-beta (compiler 4.0.0-beta-5, jar, install, deploy, clean, resources) — **только для Maven 4**. На Maven 3 оставайтесь на 3.x (compiler **3.16.0**, jar 3.5.1, install/deploy 3.2.0, resources/clean 3.5.0, source 3.4.0).
  - **Не запускайте `versions:use-latest-releases` вслепую**: он выберет плагины 4.0.0-beta.
- **Maven Wrapper 3.3.4:** `mvn wrapper:wrapper -Dmaven=3.9.16` создаёт `mvnw`, `mvnw.cmd` и `.mvn/wrapper/maven-wrapper.properties`. Тип по умолчанию `distributionType=only-script` скачивает дистрибутив через curl, wget или PowerShell на Windows, поэтому jar-файл wrapper не коммитится (проверено локально, JDK 25, Windows 11). Добавьте `distributionSha256Sum`, чтобы проверять контрольную сумму. Всегда коммитьте wrapper и запускайте в CI `./mvnw`.
- **Основные плагины (последние версии, совместимые с 3.x, ноябрь 2026):**
  - compiler **3.16.0**: используйте `<release>`, а не один `source`/`target`. `<release>` проверяет использование API против API целевого JDK, `source`/`target` — нет.
  - surefire / failsafe **3.6.1**: `*Test` идут в `test`, `*IT` — в `integration-test` и `verify`. Сборку роняет цель `verify` у failsafe.
  - enforcer **3.7.0**: `requireMavenVersion`, `requireJavaVersion`, `requirePluginVersions`, `dependencyConvergence`, `banDuplicatePomDependencyVersions`, `banDynamicVersions`, `requireUpperBoundDeps`.
  - versions **2.24.0**: `display-dependency-updates`, `display-plugin-updates`, `use-latest-releases`, `set`.
  - dependency **3.11.0**: `tree`, `analyze` (находит используемые, но не объявленные и объявленные, но неиспользуемые зависимости), `go-offline`.
  - shade **3.6.2** для uber-jar (релокация пакетов; используйте `ServicesResourceTransformer`) или assembly **3.8.0** для архивов поставки.
  - jar **3.5.1** (`Main-Class` и `Automatic-Module-Name` в манифесте), source **3.4.0** и javadoc **3.12.0** (оба обязательны для Central).
  - artifact **3.7.0** (`artifact:compare` проверяет воспроизводимость), gpg **3.2.8**, `central-publishing-maven-plugin` **0.12.0**.
- **BOM:** импортируйте в `<dependencyManagement>` через `<type>pom</type><scope>import</scope>` (например `org.junit:junit-bom`, `com.fasterxml.jackson:jackson-bom`, `org.springframework.boot:spring-boot-dependencies`).
  - Объявленные версии побеждают импортированные.
  - Если два BOM управляют одним артефактом, **побеждает BOM, импортированный первым**.
  - Для транзитивных зависимостей Maven использует **nearest-wins** (ближайшая, а не наивысшая версия). Поэтому важны `dependencyConvergence` и `requireUpperBoundDeps` из enforcer. Gradle по умолчанию берёт наивысшую.
- **Воспроизводимые сборки:**
  - Задайте `project.build.outputTimestamp` (3.10 задаёт значение по умолчанию).
  - Закрепите все плагины.
  - Проверка: `mvn clean install`, затем `mvn clean verify artifact:compare`.
  - Reproducible Central отслеживает, какие проекты пересобираются воспроизводимо.
- **Скорость:**
  - `mvnd` (Maven Daemon) держит прогретые JVM.
  - `-T 1C` собирает модули параллельно.
  - `maven-build-cache-extension` 1.3.0 кэширует результат инкрементальной сборки локально и удалённо.
  - `.mvn/maven.config` и `.mvn/jvm.config` задают флаги CLI и JVM на проект.
- **Аннотационные процессоры на JDK 23+ (JDK-8321314), проверено локально (JDK 25, Windows 11):** javac больше не запускает процессоры, найденные на обычном classpath, пока не заданы `-processor`, `--processor-path` или `--processor-module-path` либо `-proc:full`/`-proc:only`. Другими словами, *"absent other command line options, -proc:none is the default on JDK 23"* (inside.java). Проекты, рассчитывавшие на поиск процессоров в classpath, **компилируются без ошибок, но молча пропускают генерацию кода**: архетип JMH (нет `BenchmarkList`), а также Lombok, MapStruct, Immutables, Dagger или Micronaut, объявленные только как зависимости. **Исправление:** перечислите каждый процессор в `<annotationProcessorPaths>` у maven-compiler-plugin (3.16.0) или в качестве быстрого обхода задайте `<proc>full</proc>` (или `-Dmaven.compiler.proc=full`). В Gradle процессоры лежат в конфигурации `annotationProcessor`, которая всегда была явной, поэтому сборки Gradle не затронуты. JDK 21 и 22 печатают примечание, когда видят неявную обработку, поэтому превращайте предупреждения в ошибки, чтобы заметить проблему рано.
- **Грабли (проверено локально, JDK 25, Windows 11): `-q` скрывает детали lint.** При `-Xlint:all` и `failOnWarning` команда `mvn -q compile` показывает только «warnings found and -Werror specified» без строки `[rawtypes]`. Перезапустите без `-q`, чтобы увидеть причину.
- **Порог JaCoCo работает как ворота (проверено локально, JDK 25, Windows 11):** hello-проект с непокрытым `main()` упал с «lines covered ratio is 0.50, but expected minimum is 0.80».
- `@{argLine}` (позднее вычисление свойства) в `argLine` у surefire/failsafe нужен, чтобы **дописать** `-javaagent` JaCoCo, а не заменить его.
- Добавьте `-XX:+EnableDynamicAgentLoading`, когда Mockito или Byte Buddy подключаются сами (предупреждение JEP 451). Альтернатива через `-javaagent` для Mockito — в [testing.md](testing.md).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Apache Maven 3.10.0 Release Notes](https://maven.apache.org/docs/3.10.0/release-notes.html) | Apache Maven, 2026 | Все изменения поведения 3.10 и способы их отключить |
| [What's New in Maven 4](https://maven.apache.org/whatsnewinmaven4.html) | Apache Maven, 2025–2026 | Авторитетный список изменений Maven 4 |
| [Maven 4 migration guide](https://maven.apache.org/guides/mini/guide-migration-to-mvn4.html) | Apache Maven | Пошаговая миграция на Maven 4 |
| [Maven Upgrade Tool (mvnup)](https://maven.apache.org/tools/mvnup.html) | Apache Maven | Инструмент обновления `mvnup` |
| [Maven release history](https://maven.apache.org/docs/history.html) | Apache Maven | История релизов, чтобы найти последние 3.x и 4.x |
| [apache/maven issue 13321](https://github.com/apache/maven/issues/13321) | apache/maven, 2026 | Ошибка интерполяции settings в 3.10.0 при пути с `--` |
| [Maven Wrapper](https://maven.apache.org/tools/wrapper/) | Apache Maven | Типы wrapper (only-script, script, bin, source) и параметры контрольных сумм |
| [Maven Compiler Plugin: set the --release flag](https://maven.apache.org/plugins/maven-compiler-plugin/examples/set-compiler-release.html) | Apache Maven | Почему `--release`, а не source/target |
| [compiler:compile](https://maven.apache.org/plugins/maven-compiler-plugin/compile-mojo.html) | Apache Maven | Все параметры компилятора (`proc`, `annotationProcessorPaths`, `failOnWarning`) |
| [Quality Heads-up: JDK 23 annotation processing](https://inside.java/2024/06/18/quality-heads-up/) | Oracle Quality Outreach, 2024 | Смена политики аннотационной обработки в JDK 23 и советы по миграции |
| [JDK 23 Release Notes: issues](https://www.oracle.com/java/technologies/javase/23-relnote-issues.html) | Oracle, 2024 | Release note по JDK-8321314 (`-proc:full`, `maven.compiler.proc`) |
| [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/) | Apache Maven | Запуск юнит-тестов: форки, argLine, провайдер JUnit Platform |
| [Maven Failsafe Plugin](https://maven.apache.org/surefire/maven-failsafe-plugin/) | Apache Maven | Интеграционные тесты и зачем привязывать `verify` |
| [Maven Enforcer: built-in rules](https://maven.apache.org/enforcer/enforcer-rules/index.html) | Apache Maven | Каталог встроенных правил enforcer |
| [Versions Maven Plugin](https://www.mojohaus.org/versions/versions-maven-plugin/) | MojoHaus | Отчёты об обновлениях зависимостей и плагинов, `versions:set` |
| [Maven Dependency Plugin](https://maven.apache.org/plugins/maven-dependency-plugin/) | Apache Maven | `tree`, `analyze`, `go-offline` |
| [Maven Shade Plugin](https://maven.apache.org/plugins/maven-shade-plugin/) | Apache Maven | Uber-jar, релокация, трансформеры ресурсов |
| [Maven Assembly Plugin](https://maven.apache.org/plugins/maven-assembly-plugin/) | Apache Maven | Архивы поставки (zip/tar) по дескрипторам |
| [Maven JAR Plugin](https://maven.apache.org/plugins/maven-jar-plugin/) | Apache Maven | Записи манифеста, например Main-Class и Automatic-Module-Name |
| [Maven Source Plugin](https://maven.apache.org/plugins/maven-source-plugin/) | Apache Maven | `-sources.jar` (обязателен для Central) |
| [Maven Javadoc Plugin](https://maven.apache.org/plugins/maven-javadoc-plugin/) | Apache Maven | `-javadoc.jar` и параметры doclint |
| [Introduction to the Dependency Mechanism](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html) | Apache Maven | Области видимости, импорт BOM, nearest-wins |
| [Introduction to the Build Lifecycle](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html) | Apache Maven | Фазы и привязки по умолчанию; почему в CI `verify`, а не `install` |
| [Super POM](https://maven.apache.org/ref/current/maven-model-builder/super-pom.html) | Apache Maven | Что Super POM задаёт по умолчанию |
| [Configuring for Reproducible Builds](https://maven.apache.org/guides/mini/guide-reproducible-builds.html) | Apache Maven | `outputTimestamp` и проверка воспроизводимости |
| [Apache Maven Artifact Plugin](https://maven.apache.org/plugins/maven-artifact-plugin/) | Apache Maven | `artifact:compare` и `check-buildplan` |
| [Reproducible Builds: JVM](https://reproducible-builds.org/docs/jvm/) | Reproducible Builds | Воспроизводимость в сборщиках JVM |
| [reproducible-central](https://github.com/jvm-repo-rebuild/reproducible-central) | Hervé Boutemy et al. | Какие релизы Central пересобираются бит-в-бит |
| [Maven CI Friendly Versions](https://maven.apache.org/guides/mini/guide-maven-ci-friendly.html) | Apache Maven | Версии `${revision}`, `${sha1}`, `${changelist}` |
| [Flatten Maven Plugin](https://www.mojohaus.org/flatten-maven-plugin/) | MojoHaus | Плоские consumer POM на Maven 3 (в Maven 4 встроено) |
| [Configuring Apache Maven](https://maven.apache.org/configure.html) | Apache Maven | `.mvn/maven.config`, `.mvn/jvm.config`, `MAVEN_OPTS` |
| [Maven Build Cache Extension](https://maven.apache.org/extensions/maven-build-cache-extension/) | Apache Maven | Локальный и удалённый кэш сборки для Maven |
| [apache/maven-mvnd](https://github.com/apache/maven-mvnd) | Apache Maven | Maven Daemon для быстрых локальных сборок |
| [Maven Resolver](https://maven.apache.org/resolver/) | Apache Maven | Устройство Resolver 2 (фильтры, валидация), используется в 3.10 и 4 |
| [mvn clean install: a short guide](https://www.marcobehler.com/guides/mvn-clean-install-a-short-guide-to-maven) | Marco Behler | Мягкое введение в Maven для новичков |

## 4. Gradle 9.x

**Ключевые выводы**
- **Gradle 9.8.2** (2026-10-19) — текущая версия; есть 9.9.0-rc-1.
  - 9.8 добавляет **поддержку Java 27** для демона и toolchains.
  - Опциональное повторное использование настроек зеркал Maven: `org.gradle.mirror.maven.settings=true`.
  - Кликабельные места проблем.
  - **9.8.1 закрыл три уязвимости высокой степени** (десериализация между демоном и worker или клиентом, артефакты, открытые после ошибок SSL) (release notes). **Не оставайтесь на 9.8.0.**
  - В заметках 9.8 предупреждение: *"some third-party tools (for example, PMD) do not yet support Java 27"*. Держите анализаторы на toolchain LTS.
- **База Gradle 9 `[не проверено]` (по памяти о релизе 9.0; сверьте с upgrading guide):**
  - Демону нужна **JVM 17 или новее**.
  - Kotlin DSL — по умолчанию в `gradle init`, на Kotlin 2.x.
  - **Configuration cache — «предпочтительный» режим**. Принудительно он не включён: включайте `org.gradle.configuration-cache=true`.
  - Удалено много устаревших API.

  Другие JDK подключаются через toolchains.
- **Рекомендуемая форма проекта:**
  - `settings.gradle.kts` с foojay resolver и `dependencyResolutionManagement { repositoriesMode = FAIL_ON_PROJECT_REPOS }`.
  - Каталог версий в `gradle/libs.versions.toml`.
  - **Convention-плагины** в подключаемой сборке `build-logic/` (`includeBuild("build-logic")` внутри `pluginManagement`). Не используйте `allprojects {}`/`subprojects {}` и `buildSrc` для больших сборок.
  - `gradle.properties` с `org.gradle.configuration-cache=true`, `org.gradle.caching=true` и `org.gradle.parallel=true`.

  Проверено локально (JDK 25, Windows 11): шаблон ниже использует всё перечисленное, кроме `build-logic`. После `clean` строка «5 from cache» показала, что локальный кэш сборки работает.
- **Каталоги версий:** объявляйте `[versions]`, `[libraries]`, `[bundles]` и `[plugins]`, затем используйте `alias(libs.plugins.x)` и `libs.x.y`. Дефис в alias становится точкой в accessor (`google-java-format` превращается в `libs.versions.google.java.format`). Каталоги **только объявляют** версии и не ограничивают транзитивные зависимости. Для этого нужны `platform(...)` (BOM) или `constraints {}`.
- **Разрешение конфликтов:** Gradle берёт **наивысшую** версию (Maven — ближайшую). Для разбора конфликтов используйте `failOnVersionConflict()` или `dependencyInsight --dependency x`.
- **Блокировка зависимостей:** `dependencyLocking { lockAllConfigurations() }`, затем `./gradlew dependencies --write-locks`. Важно при динамических версиях и диапазонах.
- **Проверка зависимостей (dependency verification):**
  - `./gradlew --write-verification-metadata sha256 <tasks>` пишет `gradle/verification-metadata.xml`. Вариант `pgp,sha256` проверяет ещё и подписи.
  - **Грабли (проверено локально, JDK 25, Windows 11):** генерация через `help` записала только 100 компонентов. Следующий `build` **упал с 10 непроверенными артефактами** в `detachedConfiguration`: это загрузки инструментов плагинами, например jar google-java-format у Spotless и агент JaCoCo. **Генерируйте файл, выполняя реальные задачи** (`--write-verification-metadata sha256 clean build` дал 106 компонентов) и перегенерируйте после каждого обновления плагина.
  - Изменение файла также сбрасывает configuration cache.
- **Wrapper:**
  - `gradle wrapper --gradle-version 9.8.2` (проверено локально, JDK 25, Windows 11). Коммитьте `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar` и `gradle-wrapper.properties`.
  - Добавьте `--gradle-distribution-sha256-sum <sha>`, чтобы загрузка проверялась.
  - **Проверяйте jar wrapper в CI.** `gradle/actions/setup-gradle@v6` делает это **по умолчанию** (`validate-wrappers: true`). Отдельное действие — `gradle/actions/wrapper-validation@v6`. `gradle/wrapper-validation-action` `[устарело]`; замена — `gradle/actions/wrapper-validation`.
- **Error Prone / NullAway в Gradle:**
  - Плагины: `net.ltgt.errorprone` **5.2.0** и `net.ltgt.nullaway` **3.3.0** (`nullaway { onlyNullMarked = true; jspecifyMode = true }` и `options.errorprone.nullaway { error() }`).
  - В отличие от Maven, **настройка `--add-exports` не нужна**: плагин сам передаёт флаги и при необходимости запускает javac в отдельном процессе.
  - Проверено локально (JDK 25, Windows 11): намеренное разыменование null уронило `compileJava` с сообщением `[NullAway] dereferenced expression 'name' is @Nullable`.
- **Публикация в Maven Central из Gradle:** используйте `com.vanniktech.maven.publish` **0.37.0**. **Грабли:** плагин резолвится с Maven Central; на plugin portal лежит только устаревшая 0.13.0 от 2020 года, поэтому добавьте `mavenCentral()` в `pluginManagement.repositories`. Альтернатива — `com.gradleup.nmcp` 1.6.2, который загружает результат `maven-publish` в Central Portal. Встроенные `maven-publish` и `signing` по-прежнему создают артефакты.
- **Полезные плагины:**
  - `com.gradleup.shadow` 9.6.1: fat jar. Заменяет `com.github.johnrengelman.shadow` `[устарело]`.
  - `com.github.ben-manes.versions` 0.67.0: `dependencyUpdates`.
  - `nl.littlerobots.version-catalog-update` 1.1.1: обновляет версии в каталоге.
  - `com.autonomousapps.dependency-analysis` 3.19.2: ищет неиспользуемые и неверно объявленные зависимости (аналог `dependency:analyze`).
- **Build Scans / Develocity** (`--scan`) бесплатны для публичных сборок на scans.gradle.com. Дают тайминги и сведения о зависимостях и тестах. Перед использованием на закрытом коде учтите политику приватности данных.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Gradle 9.8.2 Release Notes](https://docs.gradle.org/9.8.2/release-notes.html) | Gradle, 2026 | Возможности 9.8 и исправления безопасности 9.8.1 |
| [Upgrading to Gradle 9.0](https://docs.gradle.org/current/userguide/upgrading_major_version_9.html) | Gradle | Ломающие изменения с 8.x на 9.0 |
| [Upgrading within Gradle 9.x](https://docs.gradle.org/9.8.2/userguide/upgrading_version_9.html) | Gradle, 2026 | Устаревания внутри 9.x, например свойства проекта для toolchain |
| [Compatibility Matrix](https://docs.gradle.org/current/userguide/compatibility.html) | Gradle | Какие версии Java могут запускать Gradle и быть целями toolchain |
| [Best Practices for Gradle](https://docs.gradle.org/current/userguide/best_practices.html) | Gradle | Официальный каталог практик (структура, скорость, зависимости) |
| [Gradle Kotlin DSL Primer](https://docs.gradle.org/current/userguide/kotlin_dsl.html) | Gradle | Введение в Kotlin DSL: accessor'ы, ленивые свойства |
| [Sharing dependency versions between projects (version catalogs)](https://docs.gradle.org/current/userguide/version_catalogs.html) | Gradle | Синтаксис `libs.versions.toml` и правила alias |
| [Dependency constraints and platforms](https://docs.gradle.org/current/userguide/platforms.html) | Gradle | Platforms и BOM в сравнении с каталогами |
| [Sharing build logic between subprojects](https://docs.gradle.org/current/userguide/sharing_build_logic_between_subprojects.html) | Gradle | Convention-плагины в `build-logic` |
| [Configuration Cache](https://docs.gradle.org/current/userguide/configuration_cache.html) | Gradle | Как сделать сборки и плагины совместимыми с configuration cache |
| [Build Cache](https://docs.gradle.org/current/userguide/build_cache.html) | Gradle | Локальный и удалённый кэш сборки, кэшируемые задачи |
| [Dependency Locking](https://docs.gradle.org/current/userguide/dependency_locking.html) | Gradle | Lock-файлы для воспроизводимого разрешения |
| [Verifying dependencies](https://docs.gradle.org/current/userguide/dependency_verification.html) | Gradle | Проверка контрольных сумм и PGP, разбор проблем |
| [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) | Gradle | Создание wrapper, контрольные суммы, обновления |
| [The Java Plugin](https://docs.gradle.org/current/userguide/java_plugin.html) | Gradle | Задачи Java-плагина и source sets |
| [The JaCoCo Plugin](https://docs.gradle.org/current/userguide/jacoco_plugin.html) | Gradle | Отчёт JaCoCo и правила проверки |
| [gradle-errorprone-plugin](https://github.com/tbroyer/gradle-errorprone-plugin) | Thomas Broyer | Интеграция Error Prone с Gradle (DSL опций) |
| [gradle-nullaway-plugin](https://github.com/tbroyer/gradle-nullaway-plugin) | Thomas Broyer | Типизированные опции NullAway для Gradle |
| [idiomatic-gradle](https://github.com/jjohannes/idiomatic-gradle) | Jendrik Johannes | Эталонная многопроектная сборка в современном Gradle |
| [gradle-project-setup-howto](https://github.com/jjohannes/gradle-project-setup-howto) | Jendrik Johannes | Полная, с пояснениями настройка большого Java-проекта |
| [Jendrik Johannes (YouTube)](https://www.youtube.com/@jjohannes) | Jendrik Johannes | Серия видео «Understanding Gradle», коротко и точно |
| [gradle/actions](https://github.com/gradle/actions) | Gradle | `setup-gradle`, `dependency-submission`, `wrapper-validation` |
| [setup-gradle: documentation](https://github.com/gradle/actions/blob/main/docs/setup-gradle.md) | Gradle | Провайдеры кэша, проверка wrapper, захват build scan |
| [GradleUp/shadow](https://github.com/GradleUp/shadow) | GradleUp | Fat/uber jar (поддерживаемый Shadow) |
| [gradle-versions-plugin](https://github.com/ben-manes/gradle-versions-plugin) | Ben Manes | Отчёт `dependencyUpdates` |
| [dependency-analysis-gradle-plugin](https://github.com/autonomousapps/dependency-analysis-gradle-plugin) | Tony Robalik | Ищет неиспользуемые, необъявленные и неверно настроенные зависимости |
| [gradle-maven-publish-plugin](https://github.com/vanniktech/gradle-maven-publish-plugin) | Niklas Baudy | Публикация в Central Portal из Gradle |
| [GradleUp/nmcp](https://github.com/GradleUp/nmcp) | GradleUp | Лёгкий загрузчик в Central Portal для Gradle |

## 5. Другие сборщики и запуск без сборки

**Ключевые выводы**
- **Bazel 9.3.2.** Для огромных полиглотных монорепозиториев, где нужны герметичные сборки с удалённым кэшем. Java-зависимости подключаются через `rules_jvm_external` с `maven_install` и lock-файлом. Цена — высокие затраты на настройку и сопровождение. Error Prone встроен в Java-правила Bazel.
- **Mill 1.3.1.** Сборщик на Scala с хорошей поддержкой Java: быстрый и с меньшей церемонией, чем Maven и Gradle. Mill 1.x стабилен, 2.0.0-M2 — milestone. Выбор нишевый: перед использованием проверьте поддержку IDE и CI.
- **JBang 0.143.0.** Однофайловые Java-скрипты с комментариями `//DEPS group:artifact:version`, закреплением `//JAVA 25` и командой `jbang app install`. Лучший вариант для скриптов и утилит с зависимостями; если JDK нет, он скачивает его сам.
- **Запуск исходника: JEP 330 (JDK 11) и JEP 458 (JDK 22).** `java Foo.java` компилирует в памяти и запускает файл. С 22 версии он по требованию компилирует **другие исходные файлы из того же дерева каталогов**, а `--class-path` добавляет jar.
- **JEP 512 (final в JDK 25).** Компактные исходные файлы и instance main: `void main() { IO.println("hi"); }` без объявления класса. `java.base` импортируется автоматически, есть консольный класс `java.lang.IO`.
- Проверено локально (JDK 25, Windows 11): на Temurin 25.0.5 `java Hi.java` запустил компактный исходный файл, чей `void main()` вызывал вспомогательный класс `Util` из отдельного `Util.java`. Вывод: `Hi from a compact source file, JEP 512 + JEP 458`.
- **jshell** подходит для быстрых экспериментов с API. Проверено локально (JDK 25, Windows 11): `echo 'IO.println(List.of(1,2,3).reversed())' | jshell -q -` напечатал `[3, 2, 1]`. В JShell `java.util` импортирован по умолчанию.
- **Ant + Ivy и sbt.** Ant с Ivy остался только в унаследованных сборках; в новых проектах не используйте. sbt нужен только для сборок вокруг Scala.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [JEP 330: Launch Single-File Source-Code Programs](https://openjdk.org/jeps/330) | Jim Laskey, 2018 | Запуск однофайловых программ из исходника |
| [JEP 458: Launch Multi-File Source-Code Programs](https://openjdk.org/jeps/458) | Ron Pressler, 2023 | Запуск многофайловых программ из исходника (JDK 22) |
| [JEP 512: Compact Source Files and Instance Main Methods](https://openjdk.org/jeps/512) | Ron Pressler, Gavin Bierman, 2025 | Компактные исходные файлы и instance main (final в 25) |
| [JEP 495: Simple Source Files and Instance Main Methods (Fourth Preview)](https://openjdk.org/jeps/495) | Ron Pressler, Gavin Bierman, 2024 | Предыдущий раунд preview (JDK 24), для истории и граничных случаев |
| [JBang](https://www.jbang.dev/) | Max Rydahl Andersen et al. | Java-скрипты с зависимостями и управлением JDK |
| [Bazel](https://bazel.build/) | Google / Bazel | Герметичные масштабируемые сборки |
| [rules_jvm_external](https://github.com/bazelbuild/rules_jvm_external) | Bazel | Разрешение Maven-зависимостей и lock-файлы для Bazel |
| [Mill](https://mill-build.org/) | Li Haoyi et al. | Документация Mill, включая быстрый старт для Java |

## 6. Форматирование и статический анализ

**Ключевые выводы**: что ловит каждый инструмент и как они пересекаются.

| Инструмент (версия, ноябрь 2026) | Работает на | Что ловит | Скорость / шум | Вердикт |
|---|---|---|---|---|
| `javac -Xlint:all -Werror` | исходник, внутри javac | rawtypes, unchecked, deprecation/removal, fallthrough, serial, `this-escape` (JDK 21+), missing-explicit-ctor, `try`, cast | Бесплатно; ложных срабатываний нет | **Включён всегда.** `-Xlint:all,-serial,-processing` — только при необходимости. |
| **Error Prone 2.52.0** | плагин javac | около 500 шаблонов ошибок: неверный `equals`, проигнорированные возвращаемые значения (`CheckReturnValue`), строки формата, `CollectionIncompatibleType`, `MissingOverride`, неверное использование `Optional` и `Stream` | Около 10–20% к времени компиляции; очень точный | **Обязателен по умолчанию.** Умеет автоматически править код (`-XepPatchChecks`). |
| **NullAway 0.14.3** + **JSpecify 1.0.1** | плагин Error Prone | Разыменования, опасные для NPE; возврат или передача null туда, где ожидается non-null | Менее 10% накладных расходов (по данным проекта) | **Обязателен для нового кода.** `@NullMarked` на пакет или модуль, `OnlyNullMarked=true` и JSpecify mode. |
| Checker Framework | плагин javac | Строгая nullness, плюс units, locks, resource leaks, regex | Медленнее; нужно больше аннотаций | Только для кода с повышенными требованиями к надёжности. |
| **SpotBugs 4.10.5** (+ FindSecBugs 1.14.0) | байткод | Конкурентность (несогласованная синхронизация), утечки ресурсов, неверные приведения, `equals`/`hashCode`, раскрытие внутреннего представления (EI_EXPOSE_REP, очень шумный); FindSecBugs добавляет проверки инъекций, криптографии и XXE | Минуты на больших проектах; нужен фильтр исключений | Опционально: для безопасности (FindSecBugs) или унаследованного кода. |
| **PMD 7.29.0** | AST исходника | Запахи дизайна, неиспользуемый код, сложность, практики; CPD ищет копипаст | Быстрый; без выверенного набора правил шумный | Опционально. Проверки дублирования CPD полезны. |
| **Checkstyle 14.4.0** | исходник | Стиль и соглашения: имена, наличие Javadoc, импорты, магические числа | Быстрый; при работающем форматтере в основном избыточен | Только когда команда требует стиль сверх форматирования. |
| **Spotless** (Maven 3.11.0 / Gradle 8.11.0) + google-java-format 2.0.0 или palantir-java-format 2.103.0 | исходник | Форматирование (check/apply), порядок импортов, заголовки лицензий | Быстрый | **Обязателен.** Закрывает споры о форматировании в ревью. |
| **OpenRewrite** (Maven-плагин 6.46.1 / Gradle 7.41.0 на Central) | рефакторинг по LST | Не линтер: автоматические миграции (Java 8→17→21→25, JUnit 4→5, Spring Boot 3→4, Jakarta), плюс рецепты исправления best practices | По запросу | **Запускать перед обновлениями**, а не на каждой сборке. |
| Sonar: SonarQube Server/Cloud и SonarQube for IDE (прежний SonarLint) | сервер или IDE | Ошибки, запахи, security hotspots, покрытие, дашборды дублирования, quality gate на новом коде | Отдельный сервис | Опционально, на уровне организации. Пересекается со всем перечисленным. |

- **Рекомендуемый минимум:** `--release` + `-Xlint:all -Werror` + **Error Prone + NullAway (JSpecify)** + **Spotless** + порог JaCoCo + сканирование зависимостей на уязвимости ([раздел 7](#7-безопасность-цепочки-поставок-и-публикация-в-maven-central)). SpotBugs+FindSecBugs или Sonar добавляйте только под конкретную нужду: аудит безопасности, унаследованная кодовая база, дашборд на всю организацию. Не запускайте Checkstyle *и* форматтер, которые спорят друг с другом.
- **Грабли настройки Error Prone (документация по установке):**
  - Запускается на **JDK 21+** (с 2.43); для 11 и 17 есть старые версии Error Prone.
  - Нужны `-XDcompilePolicy=simple` и `--should-stop=ifError=FLOW`.
  - Именно на JDK 21 нужен ещё `-XDaddTypeAnnotationsToSymbol=true` (2.46+).
  - В Maven список `--add-exports`/`--add-opens` кладите в `.mvn/jvm.config`, потому что javac работает внутри процесса Maven. Если javac запускается отдельно (`<fork>true</fork>` или toolchains), передавайте их как аргументы компилятора `-J--add-exports=...`.
  - **Всю строку `-Xplugin:ErrorProne ...` со всеми флагами `-Xep` кладите в ОДИН `<arg>`.**
  - Сгенерированные исходники исключайте так: `-XepExcludedPaths:.*/target/generated-sources/.*`.
  - 2.51 добавил проверку `MissingJavadoc` и сделал `CompareToZero` ошибкой. После обновлений ждите новых падений: закрепляйте версию и обновляйтесь осознанно.
- **Настройка NullAway:** нужен ровно один из параметров `AnnotatedPackages=com.acme` или `OnlyNullMarked=true`. С JSpecify аннотируйте `package-info.java` через `@NullMarked` (или `module-info.java`). `JSpecifyMode=true` включает обобщённые типы и массивы; лучше всего работает на новейшем JDK. В тестах, если шумит, отключайте. Lombok поддерживается частично (нужен `lombok.addLombokGeneratedAnnotation`).
- Проверено локально (JDK 25, Windows 11), запуск Maven: Error Prone 2.52.0 + NullAway 0.14.3 на Temurin 25.0.5. Намеренный вызов `name.length()` для параметра с `@Nullable` уронил компиляцию с `[NullAway] dereferenced expression 'name' is @Nullable`. Добавленный возвращаемый тип `java.util.List` без параметра дал «warnings found and -Werror specified».
- **google-java-format 2.0.0 (2026-10-30):**
  - Он «reformats more existing code than a typical release». Переформатируйте весь репозиторий отдельным коммитом и добавьте этот коммит в `.git-blame-ignore-revs`.
  - Новый проход **cleanup включён по умолчанию**: убирает лишние точки с запятой, пустые скобки у аннотаций и скобки вокруг единственного нетипизированного параметра лямбды.
  - Единственный аргумент-лямбда теперь остаётся на строке вызова.
  - Spotless 3.11.0 (Maven) с ним справился (проверено локально, JDK 25, Windows 11).
  - **palantir-java-format** — альтернатива с 120 колонками, популярна у пользователей Gradle и Palantir. Выберите один и не смешивайте.
- **Грабли Spotless на Windows (проверено локально, JDK 25, Windows 11):** значение по умолчанию `lineEndings=GIT_ATTRIBUTES` в каталоге без `.gitattributes` откатилось к умолчанию платформы (CRLF). Тогда `spotless:check` пометил нарушением *каждый* файл с LF. Исправление: `<lineEndings>UNIX</lineEndings>` (Gradle: `lineEndings = LineEnding.UNIX`) или коммит `.gitattributes` со строкой `* text=auto eol=lf`.
- **Ранний запуск (2026-10-20), проверено локально (JDK 25, Windows 11):** google-java-format 1.37.0 через Spotless 3.10.4 на JDK 25 упал с голым `InvocationTargetException`, причину в стандартном логе видно не было. С gjf 2.0.0 и Spotless 3.11.0 тот же проект прошёл. При такой ошибке обновите gjf или запустите с `-e`/`-X`. Как общая ошибка не подтверждено.
- **OpenRewrite, смена дистрибуции в 2026 (docs.openrewrite.org):**
  - *"Recipes are moving from Maven Central to the Code Genome Project."* Новые релизы OpenRewrite, *"including the Maven and Gradle plugins themselves, are published to the Code Genome Project repository (https://artifacts.codegenomeproject.org/maven), which requires authentication"*: нужно войти и использовать токен загрузки.
  - Прежние релизы остаются на Central. Поэтому последний `rewrite-maven-plugin` на Central — 6.46.1 (август 2026), а в документации указан OpenRewrite 8.98.0.
  - Рецепты под лицензией Apache бесплатны для любого аутентифицированного пользователя. Рецепты source-available и проприетарные требуют подписки Moderne.
  - Страницы рецептов на docs.openrewrite.org теперь перенаправляют на codegenomeproject.org.
  - **Для CI без учётных данных закрепите последние версии с Central** (rewrite-maven-plugin 6.46.1, rewrite-migrate-java 3.42.1).
- **Использование OpenRewrite:** `mvn org.openrewrite.maven:rewrite-maven-plugin:6.46.1:dryRun -Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-migrate-java:3.42.1 -Drewrite.activeRecipes=org.openrewrite.java.migrate.UpgradeToJava25`, затем `:run`. После dry run просмотрите `target/rewrite/rewrite.patch`. Проверено локально (JDK 25, Windows 11): на hello-шаблоне первый запуск был долгим, в основном из-за загрузок; dry run записал патч из двух изменений, оба стоит просмотреть. Он заменил `<release>${maven.compiler.release}</release>` литералом `25`, и свойство потерялось. Он также переписал `System.out.println(...)` в `IO.println(...)` (`java.lang.IO` из JEP 512). На JDK 25 запуск ещё напечатал предупреждения об устаревании `sun.misc.Unsafe` от Caffeine 2.9.3, который встроен в плагин. Они безвредны.
  - Основные рецепты: `UpgradeToJava17`, `UpgradeToJava21`, `UpgradeToJava25`, `org.openrewrite.java.testing.junit5.JUnit4to5Migration`, `org.openrewrite.java.spring.boot4.UpgradeSpringBoot_4_0` (rewrite-spring) и `org.openrewrite.java.migrate.jakarta.JavaxMigrationToJakarta`.
- **Совместимость API для библиотек:** japicmp (`japicmp-maven-plugin` 0.27.0) или Revapi ловят бинарные и исходные несовместимости между релизами. Modernizer (3.5.0) помечает устаревшие API, например из Guava то, что теперь есть в JDK. Мутационное тестирование (PIT 1.30.2) — в [testing.md](testing.md).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [javac (JDK 25 man page)](https://docs.oracle.com/en/java/javase/25/docs/specs/man/javac.html) | Oracle, 2025 | Все ключи `-Xlint`, `--release`, `-proc`, `-Werror` |
| [Error Prone: Installation](https://errorprone.info/docs/installation) | Google | Точная настройка Maven, Gradle, Bazel и CLI, таблица поддержки JDK |
| [Error Prone: Bug Patterns](https://errorprone.info/bugpatterns) | Google | Каталог всех проверок с уровнями серьёзности |
| [Error Prone: Flags](https://errorprone.info/docs/flags) | Google | `-Xep:Check:LEVEL`, `-XepOpt`, `-XepExcludedPaths`, `-XepDisableWarningsInGeneratedCode` |
| [Error Prone: Patching](https://errorprone.info/docs/patching) | Google | Автоматическое исправление кода через `-XepPatchChecks` |
| [google/error-prone releases](https://github.com/google/error-prone/releases) | Google | Заметки к релизам; читайте перед каждым обновлением, чтобы увидеть новые проверки уровня ERROR |
| [Error Prone Support](https://github.com/PicnicSupermarket/error-prone-support) | Picnic | Дополнительные проверки Error Prone и правила Refaster от продакшен-команды |
| [Error Prone Support: documentation](https://error-prone.picnic.tech/) | Picnic | Документация по шаблонам ошибок Error Prone Support |
| [uber/NullAway](https://github.com/uber/NullAway) | Uber | README NullAway: установка, Lombok, сгенерированный код |
| [NullAway: Configuration](https://github.com/uber/NullAway/wiki/Configuration) | Uber | Все опции NullAway (`OnlyNullMarked`, `AnnotatedPackages`, ...) |
| [NullAway: JSpecify Support](https://github.com/uber/NullAway/wiki/JSpecify-Support) | Uber | Состояние JSpecify mode и ограничения |
| [JSpecify: Start here](https://jspecify.dev/docs/start-here/) | JSpecify group | Что значат `@NullMarked` и `@Nullable` |
| [JSpecify: User Guide](https://jspecify.dev/docs/user-guide/) | JSpecify group | Подробная семантика: generics, массивы, размещение type-use |
| [JSpecify: Whether to adopt](https://jspecify.dev/docs/whether/) | JSpecify group | Стоит ли принимать JSpecify сейчас; состояние поддержки в инструментах |
| [Checker Framework](https://checkerframework.org/) | Checker Framework team | Строгие подключаемые чекеры типов, если NullAway недостаточно |
| [SpotBugs documentation](https://spotbugs.readthedocs.io/en/latest/) | SpotBugs | Детекторы, файлы фильтров, интеграция с Maven и Gradle |
| [spotbugs-maven-plugin](https://github.com/spotbugs/spotbugs-maven-plugin) | SpotBugs | Maven-плагин (`spotbugs:check`, `excludeFilterFile`) |
| [spotbugs-gradle-plugin](https://github.com/spotbugs/spotbugs-gradle-plugin) | SpotBugs | Gradle-плагин `com.github.spotbugs` |
| [Find Security Bugs](https://find-sec-bugs.github.io/) | Philippe Arteau et al. | Детекторы безопасности для SpotBugs (категории OWASP) |
| [PMD documentation](https://docs.pmd-code.org/latest/) | PMD | Документация PMD 7, rule designer, CPD |
| [PMD: Java rules](https://docs.pmd-code.org/latest/pmd_rules_java.html) | PMD | Индекс правил Java для составления выверенного набора |
| [Apache Maven PMD Plugin](https://maven.apache.org/plugins/maven-pmd-plugin/) | Apache Maven | Цели `pmd:check` и `pmd:cpd-check` |
| [Checkstyle: checks](https://checkstyle.org/checks.html) | Checkstyle | Все проверки |
| [Checkstyle: Google style](https://checkstyle.org/google_style.html) | Checkstyle | Что из стиля Google покрывает `google_checks.xml` |
| [Apache Maven Checkstyle Plugin](https://maven.apache.org/plugins/maven-checkstyle-plugin/) | Apache Maven | `checkstyle:check` и переопределение версии Checkstyle |
| [diffplug/spotless](https://github.com/diffplug/spotless) | DiffPlug | Обзор Spotless (Maven, Gradle, много форматтеров) |
| [Spotless: plugin-maven](https://github.com/diffplug/spotless/tree/main/plugin-maven) | DiffPlug | Справочник по настройке Maven (`lineEndings`, `ratchetFrom`, форматтеры) |
| [Spotless: plugin-gradle](https://github.com/diffplug/spotless/tree/main/plugin-gradle) | DiffPlug | Справочник по настройке Gradle |
| [google-java-format](https://github.com/google/google-java-format) | Google | CLI gjf, плагины IDE, изменения 2.0 |
| [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) | Google | Стиль, который реализует gjf |
| [palantir-java-format](https://github.com/palantir/palantir-java-format) | Palantir | Форматтер-альтернатива со 120 колонками |
| [OpenRewrite: Getting started](https://docs.openrewrite.org/running-recipes/getting-started) | Moderne / OpenRewrite, 2026 | Настройка плагина, **включая репозиторий Code Genome Project и аутентификацию** |
| [UpgradeToJava25 recipe](https://codegenomeproject.org/recipes/org.openrewrite.java.migrate.UpgradeToJava25) | Moderne, 2026 | Страница рецепта `UpgradeToJava25` (сюда теперь перенаправляют страницы рецептов docs.openrewrite.org) |
| [OpenRewrite: Migrate to Java 17](https://docs.openrewrite.org/running-recipes/popular-recipe-guides/migrate-to-java-17) | OpenRewrite | Разбор миграции версии Java по шагам |
| [rewrite-migrate-java](https://github.com/openrewrite/rewrite-migrate-java) | OpenRewrite | Исходники рецептов миграции Java |
| [openrewrite/rewrite](https://github.com/openrewrite/rewrite) | OpenRewrite | Ядро (Apache 2.0) |
| [SonarQube](https://www.sonarsource.com/products/sonarqube/) | SonarSource | SonarQube Server и Cloud; quality gate на новом коде |
| [SonarQube for IDE (SonarLint)](https://www.sonarsource.com/products/sonarlint/) | SonarSource | SonarQube for IDE (прежнее имя SonarLint `[устарело]`) |
| [SonarScanner for Maven](https://docs.sonarsource.com/sonarqube-server/latest/analyzing-source-code/scanners/sonarscanner-for-maven/) | SonarSource | Использование `sonar-maven-plugin` (5.8.0.7211) |
| [japicmp](https://github.com/siom79/japicmp) | Martin Mois | Отчёты о бинарной и исходной совместимости API для библиотек |
| [Revapi](https://revapi.org/) | Revapi | Проверка эволюции API (альтернатива japicmp) |
| [modernizer-maven-plugin](https://github.com/gaul/modernizer-maven-plugin) | Andrew Gaul | Находит устаревшие API, которые заменили новые JDK |

## 7. Безопасность цепочки поставок и публикация в Maven Central

**Ключевые выводы**
- **Сканирование уязвимостей: выберите один основной сканер и не замедляйте его.**
  - **OSV-Scanner v2.7.0** (Google). Читает `pom.xml` и lock-файлы Gradle или `gradle/verification-metadata.xml` и опрашивает OSV.dev, который агрегирует GHSA и другие источники. API-ключ не нужен, работает быстро, поэтому хороший выбор для CI по умолчанию. В v2.7 добавлена приблизительная подстановка свойств Maven и серьёзность в выводе CycloneDX.
  - **OWASP Dependency-Check 13.0.0** (август 2026). Использует NVD и сопоставление CPE, поэтому находит больше, но даёт **больше ложных срабатываний** и требует файлов подавления. Обновление до 12.1+ было обязательным из-за смены API NVD. **Получите NVD API key**: без него первая загрузка базы «extremely slow». В CI кэшируйте каталог данных и не запускайте много сборок на одном ключе, иначе упрётесь в лимиты 403. В 13.0 удалены устаревшие опции и поддержка Nexus v2. (README, release notes)
  - **GitHub Dependabot alerts** требуют граф зависимостей. Для Gradle отправляйте его через `gradle/actions/dependency-submission@v6`. Для Maven — `advanced-security/maven-dependency-submission-action`.
- **Боты обновлений:**
  - **Dependabot** (`.github/dependabot.yml`, `package-ecosystem: maven` или `gradle`) понимает каталоги версий и `gradle-wrapper`, умеет группировать обновления.
  - **Renovate** (44.x) богаче по группировке и расписаниям, работает с Maven, Gradle, каталогами и wrapper, даёт issue-дашборд.
  - Подойдёт любой. Закрепляйте GitHub Actions по SHA коммита, если этого требует модель угроз.
- **SBOM:** CycloneDX `cyclonedx-maven-plugin` 2.9.3 (`makeAggregateBom`) или `org.cyclonedx.bom` 3.5.1 для Gradle. Прикладывайте `bom.json` к релизам. SPDX — другой стандарт. Артефактные аттестации GitHub (`actions/attest-build-provenance`) добавляют происхождение SLSA.
- **Проверка зависимостей Gradle** ([раздел 4](#4-gradle-9x)) и **контрольные суммы Maven Wrapper/Gradle Wrapper** защищают от подмены загрузок. У Maven нет встроенного аналога verification-metadata из Gradle. Полагайтесь на политики контрольных сумм Resolver и закреплённые версии.
- **Публикация в Maven Central в 2026:**
  - **OSSRH закрыт 2025-06-30** (объявление Sonatype). Всё идёт через **Central Publisher Portal** (central.sonatype.com) с **токеном пользователя портала**, а не паролем OSSRH.
  - Maven: `org.sonatype.central:central-publishing-maven-plugin:0.12.0` с `<extensions>true</extensions>` и `<publishingServerId>central</publishingServerId>`. Для полностью автоматических релизов добавьте `<autoPublish>true</autoPublish>` и `<waitUntil>published</waitUntil>`. Учётные данные лежат в `settings.xml` под `<server><id>central</id>`.
  - Gradle: vanniktech 0.37.0 или nmcp 1.6.2.
  - **Требования:** `-sources.jar` и `-javadoc.jar` (допустимы заглушки), контрольные суммы, **подписи GPG/PGP (`maven-gpg-plugin` 3.2.8)** и метаданные POM (name, description, url, licenses, developers, scm).
  - SNAPSHOT поддерживаются через snapshot-репозиторий портала, когда он включён для namespace.
  - **Sigstore:** портал проверяет бандлы `.sigstore.json`, если они есть, но **PGP по-прежнему обязателен**. Sigstore необязателен, хотя неверный бандл может заблокировать публикацию `[не проверено]` (блог Sonatype 2023 и отчёт Socket 2025; изменений в 2026 не найдено).
  - **JReleaser** автоматизирует загрузку в портал, подпись (включая Sigstore), changelog и GitHub-релизы для Maven и Gradle.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [OSV-Scanner documentation](https://google.github.io/osv-scanner/) | Google | Использование OSV-Scanner: lock-файлы, SBOM, управляемое исправление |
| [google/osv-scanner](https://github.com/google/osv-scanner) | Google | Релизы (v2.7.0) и GitHub Action |
| [OSV: Open Source Vulnerabilities](https://osv.dev/) | OpenSSF / Google | Открытая база уязвимостей за OSV-Scanner |
| [OWASP Dependency-Check](https://owasp.org/www-project-dependency-check/) | OWASP | Домашняя страница проекта Dependency-Check |
| [dependency-check-maven](https://jeremylong.github.io/DependencyCheck/dependency-check-maven/index.html) | Jeremy Long et al. | Цели и настройка Maven-плагина (`nvdApiKey`, подавления) |
| [dependency-check/DependencyCheck](https://github.com/dependency-check/DependencyCheck) | DependencyCheck | README: NVD API key, лимиты, обязательные обновления |
| [DependencyCheck v13.0.0](https://github.com/dependency-check/DependencyCheck/releases/tag/v13.0.0) | DependencyCheck, 2026 | Ломающие изменения 13.0 |
| [NVD: Start here](https://nvd.nist.gov/developers/start-here) | NIST | API NVD и запрос ключа |
| [Dependabot options reference](https://docs.github.com/en/code-security/dependabot/dependabot-version-updates/configuration-options-for-the-dependabot.yml-file) | GitHub | Справочник по `dependabot.yml` (группы, реестры, расписания) |
| [Dependabot: supported ecosystems](https://docs.github.com/en/code-security/dependabot/ecosystems-supported-by-dependabot/supported-ecosystems-and-repositories) | GitHub | Детали поддержки Maven и Gradle |
| [Renovate: Maven manager](https://docs.renovatebot.com/modules/manager/maven/) | Mend / Renovate | Менеджер Maven в Renovate |
| [Renovate: Gradle manager](https://docs.renovatebot.com/modules/manager/gradle/) | Mend / Renovate | Менеджер Gradle в Renovate (каталоги, wrapper) |
| [gradle/actions: dependency-submission](https://github.com/gradle/actions/blob/main/docs/dependency-submission.md) | Gradle | Отправка графа зависимостей Gradle для оповещений Dependabot |
| [maven-dependency-submission-action](https://github.com/advanced-security/maven-dependency-submission-action) | GitHub | То же для Maven |
| [Using the dependency submission API](https://docs.github.com/en/code-security/supply-chain-security/understanding-your-software-supply-chain/using-the-dependency-submission-api) | GitHub | Как работает dependency submission |
| [CycloneDX](https://cyclonedx.org/) | OWASP CycloneDX | Стандарт SBOM |
| [cyclonedx-maven-plugin](https://github.com/CycloneDX/cyclonedx-maven-plugin) | CycloneDX | Генерация SBOM для Maven |
| [cyclonedx-gradle-plugin](https://github.com/CycloneDX/cyclonedx-gradle-plugin) | CycloneDX | Генерация SBOM для Gradle |
| [SPDX](https://spdx.dev/) | Linux Foundation | Второй стандарт SBOM |
| [OSSRH sunset](https://central.sonatype.org/news/20250326_ossrh_sunset/) | Sonatype, 2025 | Конец жизни OSSRH 2025-06-30 |
| [Publishing by using the Maven plugin](https://central.sonatype.org/publish/publish-portal-maven/) | Sonatype, 2026 | Опции `central-publishing-maven-plugin` 0.12.0 (`autoPublish`, `waitUntil`, ...) |
| [Publishing by using Gradle](https://central.sonatype.org/publish/publish-portal-gradle/) | Sonatype | Варианты Gradle для портала (плагины сообщества) |
| [Publishing SNAPSHOTs via the Portal](https://central.sonatype.org/publish/publish-portal-snapshots/) | Sonatype | Публикация SNAPSHOT через портал |
| [Central requirements](https://central.sonatype.org/publish/requirements/) | Sonatype | Обязательные метаданные, sources и javadoc, контрольные суммы |
| [Central: GPG](https://central.sonatype.org/publish/requirements/gpg/) | Sonatype | Создание и распространение ключей GPG |
| [Apache Maven GPG Plugin](https://maven.apache.org/plugins/maven-gpg-plugin/) | Apache Maven | Подпись (`gpg:sign`, `signer=bc` для ключей из переменных окружения) |
| [Maven Central and Sigstore](https://www.sonatype.com/blog/maven-central-and-sigstore) | Sonatype, 2023 | Бандлы Sigstore на Central |
| [Maven Central adds Sigstore signature validation](https://socket.dev/blog/maven-central-adds-sigstore-signature-validation) | Socket, 2025 | Портал теперь проверяет подписи Sigstore `[не проверено]` |
| [sigstore-java](https://github.com/sigstore/sigstore-java) | Sigstore | Клиент Sigstore для Java и плагины подписи Maven/Gradle (v2.3.0) |
| [JReleaser](https://jreleaser.org/) | Andres Almiray et al. | Сквозная автоматизация релизов, включая Central и Sigstore |
| [Using artifact attestations](https://docs.github.com/en/actions/security-for-github-actions/using-artifact-attestations) | GitHub | Аттестации происхождения сборки для jar |
| [SLSA](https://slsa.dev/) | OpenSSF | Фреймворк уровней безопасности цепочки поставок |
| [OpenSSF Scorecard](https://scorecard.dev/) | OpenSSF | Проверка безопасности репозитория (и `ossf/scorecard-action`) |
| [deps.dev](https://deps.dev/) | Google | Граф зависимостей, лицензии и уязвимости пакетов Maven |

## 8. Консольные инструменты JDK

**Ключевые выводы**: шпаргалка в таблице ниже. Профилирование и настройка GC подробно разобраны в [performance.md](performance.md).

| Инструмент | Для чего | Готовая команда |
|---|---|---|
| `java Foo.java` | Запуск одного файла или небольшого дерева исходников (JEP 330/458/512) | `java --enable-preview Main.java` (preview только при необходимости) |
| `jshell` | REPL и эксперименты с API | `jshell --enable-preview`; `/open file.jsh`; фрагменты по конвейеру: `jshell -q -` |
| `javap` | Просмотр байткода, версии класса и сигнатур | `javap -v -p -cp app.jar com.x.Foo` (**major version 69 = Java 25** в файле класса) (проверено локально, JDK 25, Windows 11) |
| `jdeps` | Поиск использования внутренних API JDK и список модулей для jlink | `jdeps --print-module-deps --ignore-missing-deps app.jar` вывел `java.base` для hello-jar (проверено локально, JDK 25, Windows 11); `jdeps --jdk-internals app.jar` |
| `jlink` | Сборка собственного образа рантайма | `jlink --add-modules $(jdeps ...) --strip-debug --no-header-files --no-man-pages --compress zip-6 --output rt` дал образ около 32 МБ для `java.base`, и приложение из шаблона на нём запустилось (проверено локально, JDK 25, Windows 11) |
| `jpackage` | Нативные установщики (msi/exe, dmg/pkg, deb/rpm) или app-image | `jpackage --type app-image --name hello --input target --main-jar hello.jar`. Для msi/exe на Windows нужен WiX. |
| `jcmd` | Универсальный инструмент диагностики работающей JVM | `jcmd <pid> help`, `Thread.print`, `GC.heap_info`, `GC.class_histogram`, `VM.flags`, `VM.system_properties`, `JFR.start/dump`, `Thread.dump_to_file -format=json` (виртуальные потоки) |
| `jfr` | Просмотр файлов Flight Recorder | `java -XX:StartFlightRecording=filename=rec.jfr,settings=profile ...`, затем `jfr summary rec.jfr` и `jfr view hot-methods rec.jfr` (проверено локально, JDK 25, Windows 11) |
| `jstack` / `jmap` / `jstat` | Устаревшие дампы потоков, дампы кучи и статистика GC | Лучше эквиваленты `jcmd`, например `jcmd <pid> GC.heap_dump /tmp/h.hprof` |
| `jwebserver` | Статический файловый сервер для тестов и документации (JEP 408) | `jwebserver -p 8000 -d /abs/path` (по умолчанию только loopback) |
| `jar` | Создание, просмотр и обновление jar; информация о модуле | `jar --describe-module --file app.jar` ответил «No module descriptor found. Derived automatic module.» (проверено локально, JDK 25, Windows 11); `jar --create --file a.jar --main-class X -C classes .` |
| `keytool` | Keystore и truststore (по умолчанию PKCS12) | `keytool -importcert -keystore truststore.p12 -alias corp -file corp.crt` |
| `jconsole` / JDK Mission Control | GUI-мониторинг и анализ JFR | JMC скачивается отдельно (adoptium.net/jmc или Oracle). |

- **Temurin 25 поставляется без каталога `jmods/`** (проверено локально, JDK 25, Windows 11), но `jlink` всё равно работал. Это JEP 493, «Linking Run-Time Images without JMODs», JDK 24. JDK, собранный так, не может скомпоновать рантайм, который *содержит* `jdk.jlink`, а для кросс-платформенной сборки нужны jmods JDK целевой платформы.
- **Старт и AOT** (подробности в [performance.md](performance.md)): CDS/AppCDS, JEP 483 (AOT-загрузка и связывание классов, JDK 24) и JEP 514/515 (эргономика командной строки AOT и профилирование методов, JDK 25). `-XX:AOTCacheOutput=app.aot` создаёт кэш тренировочного запуска за один шаг. JEP 519 (compact object headers) стал продуктовой возможностью в 25; по умолчанию выключен, включается `-XX:+UseCompactObjectHeaders` (проверено локально, JDK 25, Windows 11: `UseCompactObjectHeaders = false {default}` на 25.0.5).
- Man-страницы всех инструментов JDK лежат в `docs.oracle.com/en/java/javase/25/docs/specs/man/`.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [java (JDK 25 man page)](https://docs.oracle.com/en/java/javase/25/docs/specs/man/java.html) | Oracle, 2025 | Все опции лаунчера `java`, включая режим исходного файла и основы `-XX` |
| [jshell (man page)](https://docs.oracle.com/en/java/javase/25/docs/specs/man/jshell.html) | Oracle | Команды и опции JShell |
| [javap (man page)](https://docs.oracle.com/en/java/javase/25/docs/specs/man/javap.html) | Oracle | Опции дизассемблера |
| [jdeps (man page)](https://docs.oracle.com/en/java/javase/25/docs/specs/man/jdeps.html) | Oracle | Анализ зависимостей и список модулей для jlink |
| [jlink (man page)](https://docs.oracle.com/en/java/javase/25/docs/specs/man/jlink.html) | Oracle | Образы рантайма и плагины (compress, strip) |
| [JEP 493: Linking Run-Time Images without JMODs](https://openjdk.org/jeps/493) | Oracle (JEP), 2024 | Линковка без JMOD: почему в Temurin 25 нет `jmods/` и какие из этого ограничения |
| [JEP 282: jlink, the Java Linker](https://openjdk.org/jeps/282) | OpenJDK, 2016 | Исходный дизайн jlink |
| [jpackage (man page)](https://docs.oracle.com/en/java/javase/25/docs/specs/man/jpackage.html) | Oracle | Опции jpackage для каждой ОС |
| [Packaging Tool User's Guide](https://docs.oracle.com/en/java/javase/25/jpackage/) | Oracle | Руководство по jpackage (подпись, установщики) |
| [JEP 392: Packaging Tool](https://openjdk.org/jeps/392) | OpenJDK, 2020 | JEP jpackage: охват и ограничения |
| [jcmd (man page)](https://docs.oracle.com/en/java/javase/25/docs/specs/man/jcmd.html) | Oracle | Все диагностические команды |
| [jfr (man page)](https://docs.oracle.com/en/java/javase/25/docs/specs/man/jfr.html) | Oracle | `jfr print`, `summary`, `view`, `scrub` |
| [jstack (man page)](https://docs.oracle.com/en/java/javase/25/docs/specs/man/jstack.html) | Oracle | Дампы потоков (устаревший способ) |
| [jmap (man page)](https://docs.oracle.com/en/java/javase/25/docs/specs/man/jmap.html) | Oracle | Дампы кучи и гистограммы (устаревший способ) |
| [jwebserver (man page)](https://docs.oracle.com/en/java/javase/25/docs/specs/man/jwebserver.html) | Oracle | Опции простого веб-сервера |
| [JEP 408: Simple Web Server](https://openjdk.org/jeps/408) | OpenJDK, 2021 | JEP простого веб-сервера |
| [jar (man page)](https://docs.oracle.com/en/java/javase/25/docs/specs/man/jar.html) | Oracle | Режимы инструмента jar, включая работу с модулями |
| [keytool (man page)](https://docs.oracle.com/en/java/javase/25/docs/specs/man/keytool.html) | Oracle | Управление keystore |
| [Troubleshooting Guide (JDK 25)](https://docs.oracle.com/en/java/javase/25/troubleshoot/) | Oracle, 2025 | Руководство по диагностике (инструменты, анализ падений и зависаний) |
| [dev.java: core JDK tools](https://dev.java/learn/jvm/tools/core/) | Oracle dev.java | Обзорный учебник по основным инструментам JDK |
| [dev.java: jlink](https://dev.java/learn/jlink/) | Oracle dev.java | Учебник по jlink |
| [JEP 483: Ahead-of-Time Class Loading & Linking](https://openjdk.org/jeps/483) | OpenJDK, 2024 | AOT-загрузка и связывание классов (старт) |
| [JEP 514: Ahead-of-Time Command-Line Ergonomics](https://openjdk.org/jeps/514) | OpenJDK, 2025 | Эргономика командной строки AOT (JDK 25) |
| [JEP 515: Ahead-of-Time Method Profiling](https://openjdk.org/jeps/515) | OpenJDK, 2025 | AOT-профилирование методов (JDK 25) |
| [JEP 519: Compact Object Headers](https://openjdk.org/jeps/519) | OpenJDK, 2025 | Compact object headers (продуктовая возможность в 25) |

## 9. CI на GitHub Actions

**Ключевые выводы**
- **Актуальные версии действий (GitHub API, 2026-11-06):**
  - `actions/checkout@v7` (v7.0.1).
  - `actions/setup-java@v6` (v6.2.0, 2026-11-04). v5 перешёл на рантайм Node 24. v6 добавил дистрибутивы `oracle-openjdk`, `redhat` и `liberica-nik`. **v1–v4 deprecated.**
  - `gradle/actions/setup-gradle@v6` (v6.4.1).
  - `actions/upload-artifact@v7`.
  - Реюзабл-workflow `google/osv-scanner-action` `@v2.7.0`.
- **Возможности `setup-java`:**
  - `cache: maven` или `cache: gradle` кэширует зависимости. С v5 отдельные кэши wrapper.
  - `java-version-file: .java-version` (или `.sdkmanrc`).
  - `java-version: latest`.
  - Многострочный `java-version` ставит сразу несколько JDK.
  - `set-default: false` ставит JDK, не меняя `JAVA_HOME`.
  - Может создавать `settings.xml` с `server-id`/`server-username`/`server-password`/`gpg-private-key` для деплоя.
- **`gradle/actions/setup-gradle` (документация):**
  - По умолчанию проверяет wrapper.
  - Кэш только для чтения на не-основных ветках: `cache-read-only: ${{ github.ref != 'refs/heads/main' }}`.
  - Ссылки на Build Scan сохраняются.
  - **Лицензионная ловушка:** провайдер кэша по умолчанию `cache-provider: enhanced` использует **проприетарную** библиотеку кэширования. Она бесплатна для публичных репозиториев и в «free preview» для закрытых; для больших коммерческих организаций планируется оплата по использованию. Если нужна полностью открытая конфигурация, задайте **`cache-provider: basic`** (MIT, на основе `actions/cache`).
- **Матрица:**
  - Собирайте на LTS, который выпускаете (25), и на новейшем GA (27).
  - Библиотеки добавляют ещё минимальный baseline (17 или 21) как цель *запуска тестов*. Компилируйте один раз с `--release 17`, затем запускайте тесты на каждом JDK или используйте toolchains.
  - Добавьте одну Windows-задачу: она ловит ошибки путей и окончаний строк.
  - Проверено локально (JDK 25, Windows 11): шаблон Maven прошёл `verify` без изменений на **Temurin 27+35** (Error Prone 2.52.0, NullAway 0.14.3 и JaCoCo 0.8.16 работают на 27).
  - В заметках Gradle 9.8 сказано, что некоторые инструменты (PMD) пока не поддерживают 27, поэтому, если анализаторы ломаются, держите их в задаче на LTS.
- **Порядок шагов контроля качества (быстрее падать, дешёвое вперёд):**
  1. Проверка wrapper. Gradle делает это автоматически в `setup-gradle`; для Maven проверьте `distributionSha256Sum` в `maven-wrapper.properties`.
  2. Enforcer (Maven) или фаза конфигурации (Gradle): версии JDK/Maven, закреплённые плагины, сходимость зависимостей.
  3. Проверка форматирования: `spotless:check`, `spotlessCheck`.
  4. Компиляция с `-Xlint:all -Werror`, Error Prone и NullAway.
  5. Юнит-тесты.
  6. Интеграционные тесты (Failsafe или source set `integrationTest` в Gradle; см. [testing.md](testing.md)).
  7. Порог покрытия (JaCoCo `check`, `jacocoTestCoverageVerification`).
  8. Опционально тяжёлые анализаторы: SpotBugs, PMD/CPD, Sonar.
  9. Сканирование зависимостей на уязвимости (OSV-Scanner на PR и плановые полные сканирования) и SBOM.
  10. Упаковка и публикация только из тегов или main: подпись, затем публикация в Central Portal.
- **Укрепление:** по умолчанию `permissions: contents: read`; `concurrency` с `cancel-in-progress`. Сторонние действия закрепляйте по SHA, если этого требует политика (Renovate и Dependabot умеют обновлять закрепления). Не печатайте секреты. Вместо долгоживущих облачных ключей используйте OIDC.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [actions/setup-java](https://github.com/actions/setup-java) | GitHub, 2026 | Входы v6, дистрибутивы, кэширование, настройки Maven и GPG для деплоя |
| [setup-gradle: documentation](https://github.com/gradle/actions/blob/main/docs/setup-gradle.md) | Gradle, 2026 | Провайдеры кэша (enhanced и basic), проверка wrapper, кэш только для чтения |
| [gradle/actions: wrapper-validation](https://github.com/gradle/actions/tree/main/wrapper-validation) | Gradle | Отдельное действие проверки wrapper |
| [OSV-Scanner: GitHub Action](https://google.github.io/osv-scanner/github-action/) | Google | Реюзабл-workflow OSV-Scanner для PR и плановых сканирований; загрузка SARIF |
| [Using artifact attestations](https://docs.github.com/en/actions/security-for-github-actions/using-artifact-attestations) | GitHub | Подтверждение происхождения релизных артефактов |
| [ossf/scorecard-action](https://github.com/ossf/scorecard-action) | OpenSSF | Оценка безопасности репозитория в CI |

## Грабли: сводка

Самые ценные в начале.

1. **JDK 23+ молча пропускает аннотационные процессоры, найденные только на classpath** (JDK-8321314). Используйте `annotationProcessorPaths` или `-proc:full`. Симптомы: нет аксессоров Lombok, нет мапперов MapStruct или нет `BenchmarkList` у JMH. Заметить можно по примечанию javac на JDK 21/22 или по падению сборки после перехода на 25. Проверено локально (JDK 25, Windows 11).
2. **Обновления Oracle JDK 21 с 2026-10-20 идут по лицензии OTN.** Для бесплатных обновлений 21 берите сборку другого вендора.
3. **Maven 4 всё ещё RC** (4.0.0-rc-7). Версии плагинов `4.0.0-beta-*` — только для Maven 4. Боты вроде `versions:use-latest-releases` всё равно их предложат, поэтому отфильтруйте их (`allowedVersions` в Renovate или `ruleSet` в плагине versions).
4. **Maven 3.10.0 ломается, если путь установки содержит `--`** (issue #13321, исправлено после 3.10.0). Он также ужесточает валидацию POM и меняет порядок classpath. Сначала пилот, потом раскатка. Проверено локально (JDK 25, Windows 11).
5. **`--` внутри XML-комментария делает POM нечитаемым.** Проверено локально (JDK 25, Windows 11).
6. **Error Prone в Maven требует экспортов в `.mvn/jvm.config`.** Без них будет только «An unknown compilation problem occurred». Вся строка `-Xplugin:ErrorProne ...` — один `<arg>`. Для работы Error Prone нужен JDK 21+. Проверено локально (JDK 25, Windows 11).
7. **Spotless и окончания строк на Windows:** зафиксируйте `lineEndings` как UNIX или закоммитьте `.gitattributes`. Проверено локально (JDK 25, Windows 11).
8. **google-java-format 2.0.0 переформатирует больше кода.** Сделайте один отдельный коммит переформатирования и внесите его в `.git-blame-ignore-revs`.
9. **Метаданные проверки зависимостей Gradle нужно генерировать, выполняя реальные задачи**, а не `help`. Загрузки инструментов плагинами идут в detached-конфигурациях. Проверено локально (JDK 25, Windows 11).
10. **Форма `-P` у `org.gradle.java.installations.*` объявлена устаревшей в Gradle 9**; используйте `-D` или `gradle.properties`. Проверено локально (JDK 25, Windows 11).
11. **У Gradle 9.8.0 были три уязвимости высокой степени.** Берите 9.8.1 или новее (сейчас 9.8.2).
12. **`gradle/actions/setup-gradle` по умолчанию использует проприетарный провайдер кэша.** Для полностью открытой конфигурации задайте `cache-provider: basic`.
13. **Новые релизы OpenRewrite ушли с Maven Central** в репозиторий Code Genome Project с аутентификацией. Последние версии на Central: rewrite-maven-plugin 6.46.1 и rewrite-migrate-java 3.42.1.
14. **OSSRH больше нет (2025-06-30).** Старые схемы `nexus-staging-maven-plugin`/OSSRH `[устарело]`; используйте Central Portal.
15. **`mvn -q` скрывает настоящее предупреждение lint** за «warnings found and -Werror specified». Проверено локально (JDK 25, Windows 11).
16. **JaCoCo с surefire:** используйте `@{argLine}`, а не `${argLine}`, иначе агент теряется и покрытие показывает 0%. Конфигурация проверена локально (JDK 25, Windows 11); само поведение взято из документации JaCoCo `[не проверено]`.
17. **В Temurin 25 нет `jmods/`.** jlink всё равно работает благодаря JEP 493, но для кросс-платформенной сборки нужен JDK целевой платформы. Проверено локально (JDK 25, Windows 11).

## Соседние справочники

- **Тесты** (JUnit 6, AssertJ, настройка агента Mockito, Testcontainers, PIT, ArchUnit): [testing.md](testing.md).
- **Производительность** (настройка JMH 1.37, включая исправление аннотационных процессоров выше; JFR/JMC; async-profiler; выбор GC; AOT/Leyden): [performance.md](performance.md).
- **Возможности языка и идиомы** (records, sealed types, pattern matching, JEP 512 на практике): [idioms-patterns.md](idioms-patterns.md).
- **Конкурентность и нативное взаимодействие** (виртуальные потоки, structured concurrency, scoped values, `jcmd Thread.dump_to_file`, FFM): [concurrency-native.md](concurrency-native.md).
- **Архитектура** (модули и JPMS, слои, правила ArchUnit): [architecture.md](architecture.md).
- **Серверные фреймворки** (Spring Boot 4, Quarkus, Micronaut, Helidon; их плагины сборки и нативные образы): [domains-server.md](domains-server.md).
- **Клиент и другие области** (десктоп/JavaFX и jpackage, CLI с picocli и GraalVM, Android/Gradle AGP, данные/ML, игры, WASM): [domains-client.md](domains-client.md).
- **Книги** (Effective Java, Java Performance и другие): [books.md](books.md).
- **Конвейер разработки** (как контроль качества встраивается в цикл написания, ревью и оптимизации): [pipeline.md](pipeline.md).

## Минимальные проверки качества

Команды выполняются в этом порядке: дешёвое вперёд, тяжёлое и разовое в конце.

**Maven** (проверено локально на Maven 3.9.16 и 3.10.0 с Temurin 25.0.5 и на 3.9.16 с Temurin 27, Windows 11):

```bash
export JAVA_HOME=/path/to/jdk-25          # or rely on setup-java / sdkman / mise
./mvnw -B -ntp spotless:apply             # local only: fix formatting first
./mvnw -B -ntp verify                     # enforcer -> compile (lint+EP+NullAway) -> unit tests -> IT -> spotless:check -> jacoco:check
./mvnw -B -ntp versions:display-dependency-updates versions:display-plugin-updates   # weekly / before release
./mvnw -B -ntp dependency:analyze         # unused/undeclared deps
osv-scanner scan source -r .              # or the GitHub reusable workflow
./mvnw -B -ntp clean install && ./mvnw -B -ntp clean verify artifact:compare   # reproducibility check before release
```

Что делает каждая команда, по порядку:
1. `export JAVA_HOME=...` — выбирает JDK; в CI это делает `setup-java`.
2. `spotless:apply` — только локально, исправляет форматирование до проверки.
3. `verify` — главный шаг: enforcer, компиляция с lint, Error Prone и NullAway, юнит-тесты, интеграционные тесты, `spotless:check`, `jacoco:check`.
4. `versions:display-dependency-updates versions:display-plugin-updates` — отчёт об обновлениях, раз в неделю или перед релизом.
5. `dependency:analyze` — неиспользуемые и необъявленные зависимости.
6. `osv-scanner scan source -r .` — уязвимости в зависимостях (или реюзабл-workflow GitHub).
7. `clean install` и `clean verify artifact:compare` — проверка воспроизводимости перед релизом.

**Gradle** (проверено локально на Gradle 9.8.2 с Temurin 25.0.5, Windows 11):

```bash
./gradlew spotlessApply                   # local only
./gradlew build                           # spotlessCheck -> compileJava (lint+EP+NullAway) -> test -> jacocoTestCoverageVerification -> assemble
./gradlew dependencyUpdates               # needs com.github.ben-manes.versions; or rely on Renovate/Dependabot
./gradlew buildHealth                     # needs com.autonomousapps.dependency-analysis
./gradlew --write-verification-metadata sha256 clean build   # after any dependency/plugin bump, if verification is enabled
osv-scanner scan source -r .
```

Что делает каждая команда, по порядку:
1. `spotlessApply` — только локально, исправляет форматирование.
2. `build` — главный шаг: `spotlessCheck`, компиляция с lint, Error Prone и NullAway, тесты, проверка покрытия JaCoCo, сборка.
3. `dependencyUpdates` — отчёт об обновлениях; нужен плагин `com.github.ben-manes.versions`, иначе полагайтесь на Renovate или Dependabot.
4. `buildHealth` — состояние зависимостей; нужен плагин `com.autonomousapps.dependency-analysis`.
5. `--write-verification-metadata sha256 clean build` — перегенерация метаданных проверки после любого обновления зависимости или плагина, если проверка включена.
6. `osv-scanner scan source -r .` — уязвимости в зависимостях.

**Проверенные оговорки**
- В CI целью Maven служит `verify`, а не `install`: она выполняет всё до проверок failsafe и JaCoCo и не трогает `~/.m2`.
- `-ntp` (`--no-transfer-progress`) делает логи CI читаемыми.
- Проверено локально (JDK 25, Windows 11): первый запуск Maven дольше всего из-за загрузок; тёплый запуск занимает примерно десятую часть первого. Maven 3.10.0 на тёплом запуске примерно на 15% медленнее 3.9.16.
- Проверено локально (JDK 25, Windows 11): холодный `build` Gradle занял примерно в 2,5 раза больше, чем `clean build` с кэшем сборки («5 from cache»).
- Чтобы CI или офлайн-запуск Gradle не скачивал JDK, используйте `-Dorg.gradle.java.installations.auto-download=false` вместе с `-Dorg.gradle.java.installations.paths=<jdk>`. Используйте `-D`, а не устаревшее `-P` (проверено локально, JDK 25, Windows 11).
- Maven 3.10.0 не запускается из каталога с `--` в пути; `.mvn/jvm.config` с `--add-exports` обязателен для Error Prone; в `pom.xml` нельзя писать `--` в комментариях; Spotless на Windows требует `lineEndings=UNIX`; метаданные проверки зависимостей Gradle генерируются на реальных задачах. Подробности — в разделе [Грабли: сводка](#грабли-сводка).
- Не используйте `mvn -q` при разборе ошибок lint: он скрывает конкретное предупреждение.

**По ситуации добавляются** (только когда потребность реальна):
- **Библиотеки для Central:** sources, javadoc и подпись GPG, `central-publishing-maven-plugin` 0.12.0 или vanniktech 0.37.0 / nmcp 1.6.2 в Gradle; japicmp 0.27.0 или Revapi для совместимости API; `Automatic-Module-Name` или настоящий `module-info.java`; SBOM CycloneDX; JReleaser для автоматизации релизов.
- **Код, чувствительный к безопасности:** SpotBugs 4.10.5 с FindSecBugs 1.14.0; OWASP Dependency-Check 13.0.0 с NVD API key и кэшированной базой; quality gate Sonar; OpenSSF Scorecard; аттестации артефактов.
- **Большие или унаследованные кодовые базы:** PMD 7.29.0 с выверенным набором правил и CPD; Error Prone сначала в режиме `WARNING`, затем перевод в `ERROR`; Spotless с `ratchetFrom 'origin/main'`, чтобы форматировались только изменённые файлы; `@NullMarked` по пакетам.
- **Обновление версий:** OpenRewrite `UpgradeToJava21` / `UpgradeToJava25`, `JUnit4to5Migration`, `UpgradeSpringBoot_4_0`. Закрепите последние версии с Central или настройте учётные данные Code Genome Project. Затем полный прогон проверок.
- **Многомодульные сборки:** convention-плагины Gradle в `build-logic`. В Maven — родительский POM с `pluginManagement` и модуль BOM, `mvnd` для локальной скорости, `-T 1C` и `maven-build-cache-extension`.
- **Более строгая null-безопасность:** nullness checker из Checker Framework вместо NullAway.
- **Мутационное тестирование:** PIT 1.30.2 (`pitest-maven`); см. [testing.md](testing.md).
- **Нативные и чувствительные к старту приложения:** GraalVM `native-image`, Leyden/AOT-кэш (JEP 483/514/515), jlink и jpackage для десктопной поставки. См. [performance.md](performance.md) и [domains-client.md](domains-client.md).
- **Монорепозиторий в большом масштабе:** Bazel 9.3.2 с `rules_jvm_external`. **Быстрые скрипты:** JBang 0.143.0 или просто `java Script.java`.
- **Гигиена зависимостей:** `dependency:analyze` (Maven), `com.autonomousapps.dependency-analysis` (Gradle), правило enforcer `banDuplicateClasses` (extra-enforcer-rules) и `duplicate-finder-maven-plugin`.

## Шаблоны

Все шаблоны собраны локально (проверено локально, JDK 25, Windows 11). Тестовый проект — пакет `com.example.hello`. В нём `package-info.java` с `@NullMarked`, класс `Greeter` с параметром `@Nullable`, тесты JUnit 6.1.3 (`GreeterTest`) и `GreeterIT` для failsafe в Maven. Содержимое XML, Kotlin, TOML и YAML скопировано без изменений, комментарии оставлены на английском.

### pom.xml

Минимальный, но строгий: release 25, `-Xlint:all -Werror`, Error Prone + NullAway, surefire/failsafe, порог JaCoCo 80% по строкам, Spotless, enforcer.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.example</groupId>
  <artifactId>hello</artifactId>
  <version>0.1.0-SNAPSHOT</version>
  <packaging>jar</packaging>

  <properties>
    <maven.compiler.release>25</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <!-- Reproducible builds: fixed timestamp (the release plugin / versions:set can update it) -->
    <project.build.outputTimestamp>2026-11-06T00:00:00Z</project.build.outputTimestamp>

    <junit.version>6.1.3</junit.version>
    <jspecify.version>1.0.1</jspecify.version>
    <errorprone.version>2.52.0</errorprone.version>
    <nullaway.version>0.14.3</nullaway.version>
    <google-java-format.version>2.0.0</google-java-format.version>
    <jacoco.minLineCoverage>0.80</jacoco.minLineCoverage>
  </properties>

  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.junit</groupId>
        <artifactId>junit-bom</artifactId>
        <version>${junit.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <dependencies>
    <dependency>
      <groupId>org.jspecify</groupId>
      <artifactId>jspecify</artifactId>
      <version>${jspecify.version}</version>
    </dependency>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <pluginManagement>
      <!-- Pin EVERY plugin, including default-lifecycle ones (enforcer requirePluginVersions checks this). -->
      <plugins>
        <plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-clean-plugin</artifactId><version>3.5.0</version></plugin>
        <plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-resources-plugin</artifactId><version>3.5.0</version></plugin>
        <plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-compiler-plugin</artifactId><version>3.16.0</version></plugin>
        <plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-surefire-plugin</artifactId><version>3.6.1</version></plugin>
        <plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-failsafe-plugin</artifactId><version>3.6.1</version></plugin>
        <plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-jar-plugin</artifactId><version>3.5.1</version></plugin>
        <plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-install-plugin</artifactId><version>3.2.0</version></plugin>
        <plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-deploy-plugin</artifactId><version>3.2.0</version></plugin>
        <plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-site-plugin</artifactId><version>3.22.0</version></plugin>
        <plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-enforcer-plugin</artifactId><version>3.7.0</version></plugin>
        <plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-dependency-plugin</artifactId><version>3.11.0</version></plugin>
        <plugin><groupId>org.codehaus.mojo</groupId><artifactId>versions-maven-plugin</artifactId><version>2.24.0</version></plugin>
        <plugin><groupId>org.jacoco</groupId><artifactId>jacoco-maven-plugin</artifactId><version>0.8.16</version></plugin>
        <plugin><groupId>com.diffplug.spotless</groupId><artifactId>spotless-maven-plugin</artifactId><version>3.11.0</version></plugin>
      </plugins>
    </pluginManagement>

    <plugins>
      <!-- 1. Fail fast: wrong Maven/JDK, unpinned plugins, dependency conflicts -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-enforcer-plugin</artifactId>
        <executions>
          <execution>
            <id>enforce</id>
            <goals><goal>enforce</goal></goals>
            <configuration>
              <rules>
                <requireMavenVersion><version>[3.9.6,)</version></requireMavenVersion>
                <requireJavaVersion><version>[25,)</version></requireJavaVersion>
                <requirePluginVersions/>
                <dependencyConvergence/>
                <banDuplicatePomDependencyVersions/>
              </rules>
            </configuration>
          </execution>
        </executions>
      </plugin>

      <!-- 2. Formatting gate (fix with: mvn spotless:apply) -->
      <plugin>
        <groupId>com.diffplug.spotless</groupId>
        <artifactId>spotless-maven-plugin</artifactId>
        <configuration>
          <lineEndings>UNIX</lineEndings> <!-- default GIT_ATTRIBUTES falls back to CRLF on Windows -->
          <java>
            <googleJavaFormat><version>${google-java-format.version}</version></googleJavaFormat>
            <removeUnusedImports/>
          </java>
        </configuration>
        <executions>
          <execution>
            <goals><goal>check</goal></goals>
            <phase>verify</phase>
          </execution>
        </executions>
      </plugin>

      <!-- 3. Compiler: release 25, all lint warnings fail the build, Error Prone + NullAway.
           annotationProcessorPaths is REQUIRED on JDK 23+: javac no longer runs processors
           found on the plain classpath (JDK-8321314). -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <configuration>
          <release>${maven.compiler.release}</release>
          <failOnWarning>true</failOnWarning>
          <showWarnings>true</showWarnings>
          <compilerArgs>
            <arg>-Xlint:all</arg>
            <arg>-XDcompilePolicy=simple</arg>
            <arg>--should-stop=ifError=FLOW</arg>
            <arg>-Xplugin:ErrorProne -Xep:NullAway:ERROR -XepOpt:NullAway:OnlyNullMarked=true -XepOpt:NullAway:JSpecifyMode=true -XepExcludedPaths:.*/target/generated-sources/.*</arg>
          </compilerArgs>
          <annotationProcessorPaths>
            <path>
              <groupId>com.google.errorprone</groupId>
              <artifactId>error_prone_core</artifactId>
              <version>${errorprone.version}</version>
            </path>
            <path>
              <groupId>com.uber.nullaway</groupId>
              <artifactId>nullaway</artifactId>
              <version>${nullaway.version}</version>
            </path>
            <!-- add Lombok / MapStruct / JMH generator / Immutables processors here too -->
          </annotationProcessorPaths>
        </configuration>
      </plugin>

      <!-- 4. Unit tests (*Test). @{argLine} keeps the JaCoCo agent. -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <configuration>
          <argLine>@{argLine} -XX:+EnableDynamicAgentLoading</argLine>
        </configuration>
      </plugin>

      <!-- 5. Integration tests (*IT) in the integration-test + verify phases -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-failsafe-plugin</artifactId>
        <configuration>
          <argLine>@{argLine} -XX:+EnableDynamicAgentLoading</argLine>
        </configuration>
        <executions>
          <execution>
            <goals>
              <goal>integration-test</goal>
              <goal>verify</goal>
            </goals>
          </execution>
        </executions>
      </plugin>

      <!-- 6. Coverage with a hard threshold (unit-test data; add merge for IT data if needed) -->
      <plugin>
        <groupId>org.jacoco</groupId>
        <artifactId>jacoco-maven-plugin</artifactId>
        <executions>
          <execution>
            <id>prepare-agent</id>
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
                      <minimum>${jacoco.minLineCoverage}</minimum>
                    </limit>
                  </limits>
                </rule>
              </rules>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

`.mvn/jvm.config` нужен, потому что Error Prone работает внутри процессного javac. Проверено локально (JDK 25, Windows 11): без него Maven 3.9.16 на JDK 25 падает только с «COMPILATION ERROR: An unknown compilation problem occurred», и причина в сообщении не указана.

```text
--add-exports jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED
--add-exports jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED
--add-exports jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED
--add-exports jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED
--add-exports jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED
--add-exports jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED
--add-exports jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED
--add-exports jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED
--add-opens jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED
--add-opens jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED
```

Заметки:
- Создайте wrapper командой `mvn wrapper:wrapper -Dmaven=3.9.16`, затем закоммитьте `mvnw`, `mvnw.cmd` и `.mvn/wrapper/`.
- Для Lombok, MapStruct или генератора JMH добавьте их в `annotationProcessorPaths`. Никогда не полагайтесь на поиск в classpath (JDK 23+).
- Для библиотеки добавьте в профиль `release` плагины `maven-source-plugin` 3.4.0, `maven-javadoc-plugin` 3.12.0, `maven-gpg-plugin` 3.2.8 и `central-publishing-maven-plugin` 0.12.0.
- `requirePluginVersions` заставил закрепить и `clean`, `resources`, `install`, `deploy`, `site`. На Maven 3.10 и 4 такое закрепление нужно в любом случае.

### Gradle (Kotlin DSL)

`settings.gradle.kts`:

```kotlin
plugins {
    // Auto-provisions missing JDKs for toolchains (foojay Disco API)
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "hello"

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories { mavenCentral() }
}
```

`gradle/libs.versions.toml`:

```toml
[versions]
junit = "6.1.3"
jspecify = "1.0.1"
errorprone = "2.52.0"
nullaway = "0.14.3"
google-java-format = "2.0.0"

[libraries]
junit-bom = { module = "org.junit:junit-bom", version.ref = "junit" }
junit-jupiter = { module = "org.junit.jupiter:junit-jupiter" }
junit-platform-launcher = { module = "org.junit.platform:junit-platform-launcher" }
jspecify = { module = "org.jspecify:jspecify", version.ref = "jspecify" }
errorprone-core = { module = "com.google.errorprone:error_prone_core", version.ref = "errorprone" }
nullaway = { module = "com.uber.nullaway:nullaway", version.ref = "nullaway" }

[plugins]
errorprone = { id = "net.ltgt.errorprone", version = "5.2.0" }
nullaway = { id = "net.ltgt.nullaway", version = "3.3.0" }
spotless = { id = "com.diffplug.spotless", version = "8.11.0" }
```

`build.gradle.kts`:

```kotlin
import net.ltgt.gradle.errorprone.errorprone
import net.ltgt.gradle.nullaway.nullaway

plugins {
    application
    jacoco
    alias(libs.plugins.errorprone)
    alias(libs.plugins.nullaway)
    alias(libs.plugins.spotless)
}

group = "com.example"
version = "0.1.0-SNAPSHOT"

java {
    toolchain { languageVersion = JavaLanguageVersion.of(25) }
}

application { mainClass = "com.example.hello.Greeter" }

dependencies {
    implementation(libs.jspecify)
    errorprone(libs.errorprone.core)
    errorprone(libs.nullaway)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

nullaway {
    onlyNullMarked = true
    jspecifyMode = true
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 25
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror"))
    options.errorprone {
        disableWarningsInGeneratedCode = true
        nullaway { error() }
    }
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading")
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport { dependsOn(tasks.test) }

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule { limit { counter = "LINE"; minimum = "0.80".toBigDecimal() } }
    }
}

tasks.check { dependsOn(tasks.jacocoTestCoverageVerification) }

spotless {
    lineEndings = com.diffplug.spotless.LineEnding.UNIX
    java {
        googleJavaFormat(libs.versions.google.java.format.get())
        removeUnusedImports()
    }
}
```

`gradle.properties`:

```properties
org.gradle.configuration-cache=true
org.gradle.caching=true
org.gradle.parallel=true
```

Заметки:
- Создайте wrapper командой `gradle wrapper --gradle-version 9.8.2`.
- В многомодульной сборке вынесите блоки `tasks.withType<JavaCompile>`, `spotless`, `jacoco` и `nullaway` в convention-плагин `build-logic/src/main/kotlin/java-conventions.gradle.kts` и подключайте его через `id("java-conventions")`.
- **В Gradle настройка `--add-exports` не нужна**: плагин errorprone делает это сам (проверено локально, JDK 25, Windows 11).
- Чтобы падать и на предупреждениях проверок Error Prone, оставьте `-Werror`. Предупреждения Error Prone — это предупреждения javac, поэтому они тоже роняют сборку.

### GitHub Actions

`.github/workflows/ci.yml`:

```yaml
name: ci
on:
  push:
    branches: [main]
  pull_request:
permissions:
  contents: read
concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true

jobs:
  maven:
    runs-on: ${{ matrix.os }}
    strategy:
      fail-fast: false
      matrix:
        os: [ubuntu-latest]
        java: ['25', '27']        # LTS you ship on + newest GA as an early warning
        include:
          - os: windows-latest   # catch path/line-ending issues once
            java: '25'
    steps:
      - uses: actions/checkout@v7
      - uses: actions/setup-java@v6
        with:
          distribution: temurin
          java-version: ${{ matrix.java }}
          cache: maven
      - name: Build, test, lint, coverage gate
        run: ./mvnw -B -ntp verify
        shell: bash
      - name: Upload test reports on failure
        if: failure()
        uses: actions/upload-artifact@v7
        with:
          name: reports-${{ matrix.os }}-${{ matrix.java }}
          path: |
            target/surefire-reports
            target/failsafe-reports
            target/site/jacoco

  # Gradle variant of the same gate (keep only the job that matches your build)
  gradle:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v7
      - uses: actions/setup-java@v6
        with:
          distribution: temurin
          java-version: '25'
      - uses: gradle/actions/setup-gradle@v6   # validates the wrapper jar by default
        with:
          cache-read-only: ${{ github.ref != 'refs/heads/main' }}
      - run: ./gradlew build

  osv-scan:
    permissions:
      actions: read
      contents: read
      security-events: write
    uses: google/osv-scanner-action/.github/workflows/osv-scanner-reusable.yml@v2.7.0
    with:
      scan-args: |-
        --recursive
        ./
```

Workflow проверен на GitHub Actions (ноябрь 2026): все задания зелёные — Maven на Ubuntu с JDK 25 и 27 и на Windows с JDK 25, Gradle, OSV-Scanner (он разобрал `pom.xml` и уязвимостей не нашёл). Он работает в CI репозитория скилла дословно, к нему добавлен только рабочий каталог, и вместе с ним каждый раз собираются все шаблоны этого раздела. Версии действий на ноябрь 2026 последние: checkout v7.0.1, setup-java v6.2.0, upload-artifact v7.0.2, gradle/actions v6.4.1, osv-scanner-action v2.7.0. Метка `ubuntu-latest` переезжает на Ubuntu 26 с 19 октября 2026; в ноябре раннеры ещё были на 24.04. Для полностью открытой конфигурации Gradle добавьте в `setup-gradle` вход `cache-provider: basic` ([раздел 9](#9-ci-на-github-actions)).
