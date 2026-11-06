# Серверная Java

Справочник по серверной Java: выбор фреймворка (Spring Boot 4 и Spring Framework 7, Quarkus, Micronaut, Helidon, рантаймы Jakarta EE, лёгкие библиотеки), HTTP API и клиенты, устойчивость к сбоям, доступ к данным (JDBC, JPA и Hibernate, jOOQ, миграции, транзакции, кэш, NoSQL), сообщения и стриминг (Kafka, RabbitMQ, outbox, CDC), наблюдаемость (логи, Micrometer, OpenTelemetry, JFR), безопасность (Spring Security 7, OAuth2 и JWT, OWASP), развёртывание (контейнеры, JVM в Kubernetes, быстрый старт: CDS и AOT cache, CRaC, native image, Lambda SnapStart), боты, batch, планировщики и workflow. В конце — таблица «симптом → проверка», чеклист ревью, антипаттерны и спорные места. Актуально на ноябрь 2026: JDK 25 LTS, JDK 27 — последний выпуск; Spring Boot 4.1.2 (4.2 ожидается в ноябре 2026), Spring Framework 7.0.10, Spring Security 7.1.2, Quarkus 3.40 LTS (4.0 — в бете), Micronaut 5.2, Helidon 4.5 LTS и 27, Hibernate ORM 7.4, Jackson 3.2, Kafka 4.4, Micrometer 1.17. Версии библиотек взяты из `maven-metadata.xml` на Maven Central 6 ноября 2026. Пометки: `[устарело]` — рядом названа замена; `[не проверено]` — факт из одного вторичного источника или не перечитан в этом проходе; «проверено локально» — запуск на JDK 25, Windows 11. Соседние темы: сборка, зависимости и сканирование уязвимостей — [toolchain.md](toolchain.md); Testcontainers и тестовые срезы — [testing.md](testing.md); JMH, GC, JIT, профилировщики — [performance.md](performance.md); виртуальные потоки и реактивность изнутри — [concurrency-native.md](concurrency-native.md); гексагональная архитектура и модульные монолиты — [architecture.md](architecture.md); клиентские и десктопные приложения — [domains-client.md](domains-client.md).

- [Стек по умолчанию](#стек-по-умолчанию)
- [Версии на ноябрь 2026](#версии-на-ноябрь-2026)
- [1. Фреймворки и рантаймы](#1-фреймворки-и-рантаймы)
- [2. Веб-слой и API](#2-веб-слой-и-api)
- [3. Доступ к данным](#3-доступ-к-данным)
- [4. Сообщения и стриминг](#4-сообщения-и-стриминг)
- [5. Наблюдаемость](#5-наблюдаемость)
- [6. Безопасность](#6-безопасность)
- [7. Развёртывание: контейнеры, JVM в Kubernetes, старт](#7-развёртывание-контейнеры-jvm-в-kubernetes-старт)
- [8. Боты, интеграции, batch, планировщики, workflow](#8-боты-интеграции-batch-планировщики-workflow)
- [9. Производительность сервера: симптомы](#9-производительность-сервера-симптомы)
- [10. Эталонные кодовые базы](#10-эталонные-кодовые-базы)
- [Чеклист ревью серверного кода](#чеклист-ревью-серверного-кода)
- [Антипаттерны](#антипаттерны)
- [Спорные места](#спорные-места)

---

## Стек по умолчанию

Одна строка — одно решение. Для нового сервиса начинайте с колонки «По умолчанию» и отходите от неё только с причиной. Ловушки, из-за которых этот выбор ломается молча, собраны в разделе «Версии на ноябрь 2026».

| Решение | По умолчанию | Альтернатива | Не брать в новый код |
|---|---|---|---|
| Фреймворк | Spring Boot 4.1 (4.2 — на 6 ноября 2026 в RC2, GA ожидается в конце ноября) на Spring Framework 7 | Quarkus 3.40 LTS (быстрый старт, native), Micronaut 5, Helidon 4.5 LTS (блокирующий код на виртуальных потоках), Jakarta EE 11 на WildFly, Open Liberty или Payara (стандарты, готовый app-сервер) | Spring Boot 3.5 (открытая поддержка закончилась 2026-06-30); Spark Java `[устарело]` → Javalin |
| DI | Spring DI; на Quarkus — CDI (ArC); на Micronaut — DI на этапе компиляции | — | `javax.annotation` и `javax.inject` (поддержка удалена в Spring 7) → `jakarta.*` |
| HTTP-клиент | `RestClient` (блокирующий) или интерфейсы `@HttpExchange` с `@ImportHttpServices`; `WebClient` в реактивном стеке | JDK `java.net.http.HttpClient`, OkHttp 5, Apache HttpClient 5.6, Retrofit | `RestTemplate` `[устарело]` → `RestClient` (формально `@Deprecated` в 7.1, удаление в 8.0) |
| Доступ к БД | Spring Data JPA и Hibernate 7 с `LAZY` везде и DTO-проекциями на чтение; `JdbcClient` для простого SQL | jOOQ 3.21 для сложного SQL, Spring Data JDBC, MyBatis 3.5, Jdbi 3 | `spring.jpa.open-in-view=true` (OSIV); R2DBC без полностью реактивного стека; `hibernate-jpamodelgen` `[устарело]` → `hibernate-processor` |
| Пул соединений | HikariCP 7.1, размер считается от возможностей БД | PgBouncer (transaction mode) или RDS Proxy при многих репликах | пул по умолчанию «на глаз» и `connectionTimeout` в 30 с |
| Миграции | Flyway 13 (в Boot 4 — через `spring-boot-starter-flyway`) | Liquibase 5 (лицензия FSL, проверьте пригодность) | «голая» библиотека Flyway или Liquibase без стартера в Boot 4; правка применённых миграций |
| JSON | Jackson 3 (`tools.jackson.*`), бин `JsonMapper` | Jackson 2.22 через мост `spring-boot-jackson2` как временная мера | `ObjectMapper` как бин-замена в Boot 4; `Jackson2ObjectMapperBuilderCustomizer` `[устарело]` → `JsonMapperBuilderCustomizer`; `@JsonComponent` `[устарело]` → `@JacksonComponent` |
| Сообщения | Kafka 4.x (только KRaft) и Spring Kafka; outbox + идемпотентные потребители | RabbitMQ с quorum queues, SmallRye Reactive Messaging, Helidon Messaging, JMS (`JmsClient` в Spring 7) | Kafka с ZooKeeper (удалён в 4.0); классические зеркалируемые очереди RabbitMQ; двойная запись «БД + брокер» |
| Кэш | Caffeine 3.3 локально, Redis (Lettuce 7) для общего | Redisson 4.8 для распределённых объектов | кэш без границы размера и TTL; Redis-замок как гарантия корректности |
| Логи | SLF4J 2 + Logback (по умолчанию в Boot), структурный JSON | Log4j 2.26 с `JsonTemplateLayout` | конкатенация строк в логах; секреты и персональные данные в логах |
| Метрики и трейсы | Micrometer 1.17 + Micrometer Tracing; в Boot 4 `spring-boot-starter-opentelemetry` (OTLP) | агент OpenTelemetry Java 2.33 (без кода) | агент и SDK OpenTelemetry одновременно (дубли спанов); `management.tracing.enabled` `[устарело]` → `management.tracing.export.enabled` |
| Безопасность | Spring Security 7.1: лямбда-DSL, `authorizeHttpRequests`, OAuth2 resource server с JWT, Keycloak 26 или другой IdP | непрозрачные токены с интроспекцией, если нужен мгновенный отзыв | `and()`, `authorizeRequests`, `AntPathRequestMatcher`, `MvcRequestMatcher`, OAuth2 password grant (удалены); адаптеры Keycloak `[устарело]` |
| Устойчивость | таймауты везде; `@Retryable` и `@ConcurrencyLimit` из Spring 7 для простых случаев | Resilience4j 2.4 (`resilience4j-spring-boot4`), Bucket4j 8.22 для лимитов | Spring Retry как зависимость Boot (больше не под управлением BOM); повторы без backoff и jitter |
| Контейнерный образ | Jib 3.5 или Buildpacks (`spring-boot:build-image`), JRE-образ distroless, не root | многоступенчатый Dockerfile с `-Djarmode=tools extract --layers --launcher` | режим `layertools` (удалён в Boot 4.1); образ с полным JDK; запуск от root |
| JDK | JDK 25 LTS | JDK 21 (минимум для Quarkus 4.0 и Helidon 4.5), JDK 27 при обновлении каждые 6 месяцев | JDK ниже 17 (минимум Boot 4 — Java 17) |
| Процессоры аннотаций | явно в `annotationProcessorPaths` (Maven) или `annotationProcessor` (Gradle) | флаг `-proc:full` | неявный поиск процессоров на class path (с JDK 23 не работает) |

## Версии на ноябрь 2026

Источник версий библиотек: `https://repo.maven.apache.org/maven2/<путь группы>/<артефакт>/maven-metadata.xml` (элементы `<release>` и `<version>`), 6 ноября 2026. Окна поддержки: [endoflife.date](https://endoflife.date/api/spring-boot.json) и страницы вендоров. Все строки проверены, если не сказано иное.

| Компонент | Последняя стабильная | Следующая / примечание |
|---|---|---|
| Spring Boot | 4.1.2 (2026-10-22); 4.0.9; 3.5.16 (последняя открытая 3.5, 2026-06-25) | 4.2.0-RC2, GA ожидается в ноябре 2026. Конец открытой поддержки: 4.0 — 2026-12-31, 4.1 — 2027-07-31; 3.5 — 2026-06-30 (коммерческая до 2032) |
| Spring Framework | 7.0.10 | 7.1.0-RC2; GA 7.1 планируется на ноябрь 2026 (`RestTemplate` формально `@Deprecated`, минимум Jackson 3.1, база Hibernate 7.3, поддержка JPA 4.0 и Hibernate 8) |
| Spring Security | 7.1.2 | 7.2.0-RC1 |
| Spring Data JPA / Spring Kafka / Spring Batch | 4.1.2 / 4.1.2 / 6.0.6 | RC 4.2 / 4.2 / 6.1 (поезд Boot 4.2) |
| Spring GraphQL / Spring gRPC / Spring Modulith | 2.0.6 / 1.1.2 / 2.1.2 | Boot 4.1 добавляет автоконфигурацию Spring gRPC |
| Quarkus (BOM платформы) | 3.40.1.1 (LTS, 2026-09-30, поддержка сообщества до 2027-09-30); 3.33.4.1 (LTS до 2027-03-25) | 4.0.0.Beta3; 4.0 Final планируется в конце ноября 2026 (база Java 21, Vert.x 5, Netty 4.2, Hibernate ORM 8, Jackson 3, Jakarta REST 4) |
| Micronaut (платформа) | 5.2.5 (core 5.2.16) | Micronaut 5.0 GA 2026-05-20: JSpecify, предвычисленные индексы бинов, HTTP/3 на Netty, GraalVM 25 |
| Helidon | 27.0.1 («Tip», следует за JDK 27); 4.5.6 («Tail», LTS, база JDK 21) | Номера теперь следуют за JDK (Helidon 28 с JDK 28, март 2027). Helidon SE переименован в «Helidon Core», MP — «Helidon MicroProfile» |
| Платформа Jakarta EE | 11.0.0 (2025-06) | Jakarta EE 12 сдвинулась с июля 2026 на «конец 2026» `[не проверено]`; Jakarta Persistence 4.0 — 4.0.0-M8 |
| WildFly / Open Liberty / Payara / GlassFish | 42.0.0.Final / 26.0.0.10 / 7.2026.10 / 8.0.5 | 9.0.0-M3 GlassFish — работа над EE 12 |
| Hibernate ORM | 7.4.15.Final | 8.0.0.Beta4 (Jakarta Persistence 4.0, Jakarta Data 1.1). Boot 4.1 поставляет Hibernate 7.4, Boot 4.0 — 7.2 |
| Hibernate Validator | 9.1.6.Final | 9.2.0.Beta1 |
| Jackson 3 (`tools.jackson.core:jackson-databind`) | 3.2.4 | Ветка Jackson 2 поддерживается: 2.22.4. Аннотации остались в `com.fasterxml.jackson.annotation` |
| HikariCP | 7.1.0 (2026-06) | |
| Flyway / Liquibase | 13.12.1 / 5.0.4 | Liquibase Community с 5.0 — лицензия FSL (исходники открыты, через 2 года превращается в Apache-2.0) |
| jOOQ / MyBatis / Jdbi | 3.21.9 / 3.5.19 / 3.55.0 | jOOQ 3.20+ требует Java 21 (по заметкам Boot 4.1) |
| PostgreSQL JDBC / R2DBC Postgres | 42.7.14 / 1.1.3 | SPI R2DBC всё ещё 1.0.0 (2022) |
| Kafka clients / Streams | 4.4.0 | 4.0 убрал ZooKeeper; протокол потребителя KIP-848 GA в 4.0; share groups («очереди») готовы к продакшену в 4.2 |
| RabbitMQ Java client / Spring AMQP | 5.37.0 / 4.1.x | Spring AMQP 4.2.0-RC1 |
| Debezium | 3.7.1.Final | |
| Micrometer / Micrometer Tracing | 1.17.2 / 1.7.2 | RC 1.18 (Boot 4.2) |
| OpenTelemetry Java agent / API | 2.33.1 / 1.67.0 | В Spring Boot 4 есть собственный `spring-boot-starter-opentelemetry` |
| Logback / Log4j 2 / SLF4J | 1.6.5 (поддерживается 1.5.38) / 2.26.1 (3.0 бета) / 2.0.20 | logstash-logback-encoder 9.0 |
| springdoc-openapi (линия Boot 4) | 3.2.0 | springdoc 2.x — для Boot 3 |
| Resilience4j | 2.4.0 (есть `resilience4j-spring-boot4`) | Spring Framework 7 даёт `@Retryable` и `@ConcurrencyLimit` в ядре |
| Bucket4j (`com.bucket4j:bucket4j_jdk17-core`) | 8.22.0 | |
| gRPC Java | 1.85.1 | |
| Javalin / Vert.x / Jooby | 7.2.4 / 5.2.3 / 4.5.4 | Spark Java: последний релиз 2.9.4 (2022), репозиторий неактивен с 2023 — считайте мёртвым |
| Caffeine / Lettuce / Redisson | 3.3.1 / 7.9.0 (8.0.0.BETA1) / 4.8.0 | |
| MongoDB sync driver / Cassandra driver | 5.14.0 / 4.19.4 (Apache) | |
| Tomcat / Jetty / Netty | 11.0.27 / 12.1.14 / 4.2.20 | Undertow убран из Spring Boot 4 (нет Servlet 6.1) |
| Keycloak | 26.8.2 | |
| Jib Maven plugin / GraalVM native build tools | 3.5.2 / 1.1.18 | |
| Spring Cloud | 2025.1.x GA; 2026.0.0-RC2 | |
| Temporal Java SDK / ShedLock / Quartz | 1.40.0 / 7.10.1 / 2.5.2 | |
| TelegramBots (rubenlagus) / JDA | 10.3.0 / 6.7.0 | |
| AWS Lambda | управляемый рантайм Java 25 GA 2025-11-14, SnapStart поддерживается | |

### Главные ловушки 2026

Шесть мест, где сервис собирается и стартует, но ведёт себя неправильно. Проверьте их первыми при создании проекта, обновлении или ревью.

1. **JDK 23+ не запускает процессоры аннотаций с class path.** Проверено локально (JDK 25, Windows 11). `javac` по умолчанию больше не ищет процессоры на class path. Затронуты Lombok, MapStruct, `hibernate-processor` (статическая метамодель JPA, раньше `hibernate-jpamodelgen`), процессоры Micronaut и смежные. Объявите их в Maven `maven-compiler-plugin` → `<annotationProcessorPaths>` или в Gradle `annotationProcessor`; запасной путь — компиляция с `-proc:full`. Симптом: `cannot find symbol Book_` или пропавшие геттеры Lombok, при этом ошибки процессора нет.
2. **Spring Boot 4: переименованные стартеры и мосты миграции.** Автоконфигурация разбита на модули, у каждой технологии свой стартер: `spring-boot-starter-web` → `spring-boot-starter-webmvc`, `-aop` → `-aspectj`, `-web-services` → `-webservices`, OAuth2-стартеры → `spring-boot-starter-security-oauth2-*`, тестовые стартеры по технологиям (`spring-boot-starter-webmvc-test`, `spring-boot-starter-security-test`). Flyway и Liquibase требуют `spring-boot-starter-flyway` и `spring-boot-starter-liquibase`: голая библиотека больше не включает автоконфигурацию, миграции молча не запускаются. Временные мосты: `spring-boot-starter-classic` и `spring-boot-starter-test-classic` возвращают старый общий class path; уберите их после миграции. Подробности — раздел 1.1.
3. **Jackson 3 переехал в другие пакеты.** Код и сериализаторы — в `tools.jackson.*`, аннотации остались в `com.fasterxml.jackson.annotation`. Замените бин на `JsonMapper`. `JacksonException` стал unchecked: старые `catch (JsonProcessingException e)` и `catch (IOException e)` перестают срабатывать, поведение меняется без ошибки компиляции. Даты по умолчанию сериализуются в ISO-8601, `FAIL_ON_NULL_FOR_PRIMITIVES` включён.
4. **Удалено и объявлено устаревшим в Boot 4, Framework 7 и Security 7.** Сводная таблица ниже. Главное: Undertow, `@MockBean` и `@SpyBean`, `and()` в Security DSL, `authorizeRequests`, `RestTemplate` (устаревание), `layertools`, поддержка `javax.*`.
5. **Open Session in View (OSIV) включён по умолчанию.** `spring.jpa.open-in-view=true` держит соединение с БД на всё время запроса и прячет N+1 в сериализаторах и представлениях. Выставьте `spring.jpa.open-in-view=false` (раздел 3.2).
6. **Пул соединений и виртуальные потоки.** `spring.threads.virtual.enabled=true` снимает потолок по потокам, но потолком становится `maximumPoolSize` у HikariCP: тысячи виртуальных потоков встанут в очередь на `getConnection()`. Задайте размер пула, короткий `connectionTimeout` и bulkhead на входе (разделы 1.1 и 3.1). Закрепление на `synchronized` исправлено в JDK 24+ (JEP 491).

### Удалено и устаревшее: сводка

| Что | Статус | Замена | Где |
|---|---|---|---|
| Undertow | удалён в Boot 4 (нет Servlet 6.1) | Tomcat 11 или Jetty 12 | Boot 4 |
| Скрипты запуска «fully executable» jar | удалены | systemd-юнит или `java -jar` | Boot 4 |
| Интеграция Spock | удалена | JUnit 6 | Boot 4 |
| `@MockBean`, `@SpyBean` | удалены | `@MockitoBean`, `@MockitoSpyBean` | Boot 4 |
| `javax.annotation`, `javax.inject` | поддержка удалена | `jakarta.annotation`, `jakarta.inject` | Framework 7 |
| `ListenableFuture`, suffix pattern matching, Spring JCL | удалены | `CompletableFuture`, явные пути, SLF4J | Framework 7 |
| Бин `HttpMessageConverters` | устарел | `ClientHttpMessageConvertersCustomizer`, `ServerHttpMessageConvertersCustomizer` | Boot 4 |
| `Jackson2ObjectMapperBuilderCustomizer` | заменён | `JsonMapperBuilderCustomizer` | Boot 4 |
| `@JsonComponent` | заменён | `@JacksonComponent` | Boot 4 |
| `spring.jackson.read.*`, `spring.jackson.write.*` | переименованы | `spring.jackson.json.read.*`, `spring.jackson.json.write.*` | Boot 4 |
| `spring-boot-jackson2` | мост, устарел | Jackson 3; поддержка Jackson 2 планируется к удалению в Framework 7.2 | Boot 4 |
| Аннотации JSR 305 | устарели | JSpecify `@Nullable`, `@NullMarked` | Framework 7 |
| `RestTemplate` | в документации устарел в 7.0, формально `@Deprecated` в 7.1, удаление в 8.0 | `RestClient`, `WebClient` | Framework 7 |
| Spring Retry | больше не под управлением BOM Boot | `@Retryable` из ядра Spring 7 или Resilience4j | Boot 4 |
| Режим `layertools` | удалён в Boot 4.1 | `java -Djarmode=tools -jar app.jar extract --layers --launcher` | Boot 4.1 |
| Derby, LiveReload в devtools | устарели | другая встроенная БД, перезагрузка браузера | Boot 4.1 |
| `management.tracing.enabled` | переименован | `management.tracing.export.enabled` | Boot 4 |
| `HandlerMappingIntrospector` | удалён в 7.1 | может изменить способ поиска CORS-источника | Framework 7.1 |
| `.and()` в Security DSL | удалён | лямбда-DSL | Security 7 |
| `authorizeRequests` | удалён | `authorizeHttpRequests` | Security 7 |
| `AntPathRequestMatcher`, `MvcRequestMatcher` | удалены | `PathPatternRequestMatcher` | Security 7 |
| OAuth2 password grant | удалён | authorization code + PKCE, client credentials | Security 7 |
| `hibernate-jpamodelgen` | переименован | `hibernate-processor` (путь процессора на JDK 23+) | Hibernate 7 |
| springdoc-openapi 2.x | линия Boot 3 | springdoc 3.x | Boot 4 |
| Адаптеры Keycloak для Spring (`keycloak-spring-boot-adapter`) | `[устарело]` | стандартная поддержка Spring OAuth2 client и resource server | Keycloak 26 |
| Spark Java | `[устарело]` (2.9.4, 2022) | Javalin 7 | библиотеки |

## 1. Фреймворки и рантаймы

### 1.1 Spring Boot 4 и Spring Framework 7: что изменилось по сравнению с Boot 3

**Ключевые выводы**

- Базовые требования: минимум Java 17 (рекомендуется JDK 25), Jakarta EE 11 (Servlet 6.1, JPA 3.2, Bean Validation 3.1), Kotlin 2.2, GraalVM 25 для native, Netty 4.2, JUnit 6.
- Путь обновления: сначала последний 3.5.x, уберите устаревшие API, затем 4.0. Один раз подключите `spring-boot-properties-migrator` (scope runtime): он пишет в лог переименованные свойства. После правок удалите его. Для автоматической миграции есть рецепты OpenRewrite.
- Модульная автоконфигурация: список переименований стартеров и ловушка с Flyway и Liquibase — в разделе «Главные ловушки 2026», пункт 2.
- Jackson 3 по умолчанию. Определяйте бин `JsonMapper`, а не `ObjectMapper`. Свойство `spring.jackson.use-jackson2-defaults=true` облегчает миграцию. Остальные переименования — в сводной таблице выше.
- Null-safety по JSpecify: API Spring 7 и Boot 4 размечены `org.jspecify.annotations.@Nullable` и `@NullMarked`. Сборки на Kotlin и NullAway могут упасть из-за изменившейся nullness. В своём коде включайте `@NullMarked` в `package-info.java` вместе с NullAway (подробнее — [toolchain.md](toolchain.md)).
- Версионирование API встроено в MVC и WebFlux: `spring.mvc.apiversion.*` (заголовок, параметр запроса, сегмент пути, параметр media type), `@GetMapping(path = "/orders", version = "1.1")`, подсказки об устаревании через `ApiVersionDeprecationHandler` (заголовки Deprecation и Sunset), клиентская поддержка в `RestClient` и HTTP-интерфейсах.
- HTTP-клиенты-интерфейсы: `@HttpExchange` и `@ImportHttpServices(group = "...", types = ...)` создают прокси-бины по группам; настройки — `spring.http.serviceclient.<group>.*` (базовый URL, таймауты) `[не проверено]` в части точного префикса. Это заменяет самописные обёртки над `RestTemplate` и большую часть OpenFeign.
- `RestTemplate`: устарел в документации 7.0, формально `@Deprecated` в 7.1, удаление запланировано в 8.0. Используйте `RestClient` (блокирующий) или `WebClient` (реактивный). `RestTestClient` — новый нереактивный тестовый клиент.
- Устойчивость в ядре: `@EnableResilientMethods`, `@Retryable` (backoff, jitter) и `@ConcurrencyLimit` в `org.springframework.core.retry` и `resilience`.
- `@SpringBootTest` больше не подключает MockMvc и TestRestTemplate сам: добавьте `@AutoConfigureMockMvc`, `@AutoConfigureRestTestClient` или `@AutoConfigureTestRestTemplate`.
- Пробы liveness и readiness включены по умолчанию. Стартер Spring Batch по умолчанию работает в памяти: для JDBC-репозитория заданий подключите `spring-boot-starter-batch-jdbc`.
- Boot 4.1 (июнь 2026): автоконфигурация Spring gRPC (сервер на Netty или gRPC поверх Servlet HTTP/2); `InetAddressFilter` против SSRF в HTTP-клиентах; свойства sampler и лимитов OTel и стандартные переменные `OTEL_*`; ленивое получение JDBC-соединения; `@RedisListener`; репозиторий заданий Batch на MongoDB. Maven `-DskipTests` больше не пропускает AOT-обработку тестов: используйте `-Dmaven.test.skip`.
- Виртуальные потоки: `spring.threads.virtual.enabled=true` переключает на них обработку запросов Tomcat и Jetty, `@Async`, планировщик, слушатели Kafka и Rabbit и (в Boot 4) клиенты на базе JDK `HttpClient`. Размер пула соединений становится реальным потолком конкурентности (раздел 3.1; детали виртуальных потоков — [concurrency-native.md](concurrency-native.md)).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Spring Boot 4.0 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Release-Notes) | Spring team, 2025 | Авторитетный список возможностей 4.0 |
| [Spring Boot 4.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide) | Spring team, 2025-26 | Переименования стартеров, Jackson 3, изменения тестов, удаления |
| [Spring Boot 4.1 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.1-Release-Notes) | Spring team, 2026 | gRPC, фильтр SSRF, OTel, удалённый layertools |
| [Spring Framework 7.0 Release Notes](https://github.com/spring-projects/spring-framework/wiki/Spring-Framework-7.0-Release-Notes) | Spring team, 2025 | Базы, JSpecify, версионирование, устойчивость, удаления |
| [Spring Framework 7.1 Release Notes](https://github.com/spring-projects/spring-framework/wiki/Spring-Framework-7.1-Release-Notes) | Spring team, 2026 | Устаревание RestTemplate, HTTP QUERY, ProblemDetail для необработанных ошибок |
| [Анонс Spring Boot 4.1.0](https://spring.io/blog/2026/06/10/spring-boot-4) | Spring team, 2026 | Анонс 4.1.0 (URL как вернул поиск) |
| [Spring Boot reference](https://docs.spring.io/spring-boot/reference/) | Spring team | Справочник |
| [API versioning в MVC](https://docs.spring.io/spring-framework/reference/web/webmvc-versioning.html) | Spring team | Справка по версионированию API |
| [REST clients](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html) | Spring team | RestClient, WebClient, HTTP-интерфейсы, группы |
| [Resilience в Spring Framework](https://docs.spring.io/spring-framework/reference/core/resilience.html) | Spring team | `@Retryable`, `@ConcurrencyLimit` |
| [Null-safety в Spring](https://docs.spring.io/spring-framework/reference/core/null-safety.html) | Spring team | JSpecify в Spring |
| [JSpecify user guide](https://jspecify.dev/docs/user-guide/) | JSpecify group | Семантика `@NullMarked` |
| [Jackson 3 in Spring Boot 4](https://www.danvega.dev/blog/2025/11/10/jackson-3-spring-boot-4) | Dan Vega, 2025 | Практика Jackson 3 в Boot 4 |
| [Migrating to Jackson 3](https://github.com/FasterXML/jackson/blob/main/jackson3/MIGRATING_TO_JACKSON_3.md) | FasterXML | Официальная миграция Jackson 2 → 3 |
| [Jackson 3.0.0 GA released](https://cowtowncoder.medium.com/jackson-3-0-0-ga-released-1f669cda529a) | Tatu Saloranta, 2025 | Причины и умолчания Jackson 3 GA `[не проверено]` (curl получает 403, найдено поиском) |
| [Jackson Release 3.1](https://github.com/FasterXML/jackson/wiki/Jackson-Release-3.1) | FasterXML, 2026 | Исправления умолчаний 3.1 (база Framework 7.1) |
| [OpenRewrite: рецепты Boot 4](https://docs.openrewrite.org/recipes/java/spring/boot4) | Moderne/OpenRewrite | Автоматическая миграция Boot 3 → 4 |

### 1.2 Quarkus

**Ключевые выводы**

- Сборка с аугментацией: расширения выполняют сканирование class path, конфигурацию и генерацию прокси на этапе сборки. Старт JVM и RSS низкие, native image работает «из коробки». Библиотекам на рефлексии нужно расширение или `@RegisterForReflection`.
- Dev mode (`quarkus dev`): живая перезагрузка, Dev UI, непрерывные тесты. Dev Services сами поднимают PostgreSQL, Kafka, Keycloak, Redis на Testcontainers, если URL не задан. Ловушка: нужны Docker или Podman; в продакшене задавайте реальные URL через профиль `%prod.`.
- Модель программирования: Quarkus REST (раньше RESTEasy Reactive) обслуживает блокирующие и неблокирующие эндпоинты; поток выбирают тип результата или `@Blocking`, `@NonBlocking`, `@RunOnVirtualThread`. Не блокируйте event loop (Vert.x пишет предупреждения «Thread blocked»).
- Персистентность: Hibernate ORM с Panache (active record или репозиторий), Hibernate Reactive, репозитории Jakarta Data.
- Цикл LTS: LTS каждые 6 месяцев, 12 месяцев поддержки сообществом: 3.33 (до 2027-03-25), 3.40 (до 2027-09-30). Минорные версии не-LTS заканчиваются со следующим минором. `quarkus update` (OpenRewrite) мигрирует версии.
- Quarkus 4.0: Beta1 2026-10-01 (к 2026-11-04 вышла Beta3), Final планируется на конец ноября 2026. База Java 21, Vert.x 5 и Netty 4.2, HTTP/3 по включению, Hibernate ORM 8 (Jakarta Persistence 4.0, Jakarta Data 1.1), Jackson 3, Jakarta REST 4, CDI 5.0, Observation API (мост Micrometer и OpenTelemetry), расширение JMS, Kafka `@ExactlyOnce`.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Quarkus releases](https://quarkus.io/releases) | Quarkus team | Таблица поддерживаемых и LTS-версий |
| [Quarkus 4.0.0.Beta1 released](https://quarkus.io/blog/quarkus-4-0-0-beta1-released/) | Quarkus team, 2026 | Изменения 4.0 и миграция |
| [Quarkus 3.33 released](https://quarkus.io/blog/quarkus-3-33-released/) | Quarkus team, 2026 | LTS 3.33 |
| [Quarkus guides](https://quarkus.io/guides/) | Quarkus team | Все руководства |
| [Dev Services](https://quarkus.io/guides/dev-services) | Quarkus team | Справка по Dev Services |
| [Quarkus REST](https://quarkus.io/guides/rest) | Quarkus team | Quarkus REST, модель потоков |
| [Virtual threads](https://quarkus.io/guides/virtual-threads) | Quarkus team | `@RunOnVirtualThread`: советы и ловушки |
| [Hibernate ORM with Panache](https://quarkus.io/guides/hibernate-orm-panache) | Quarkus team | Panache |
| [Building a native image](https://quarkus.io/guides/building-native-image) | Quarkus team | Native-сборка, Mandrel |
| [Writing extensions](https://quarkus.io/guides/writing-extensions) | Quarkus team | Как работает аугментация на этапе сборки |

### 1.3 Micronaut

**Ключевые выводы**

- DI и AOP на этапе компиляции через процессоры аннотаций (без рефлексии и прокси в рантайме). Следствие: правило JDK 23+ про процессоры действует (плагины Micronaut для Maven и Gradle настраивают путь сами).
- Micronaut 5 (GA 2026-05-20): BOM платформы на 70+ модулей, предвычисленные индексы бинов и `@Replaces` на этапе компиляции, HTTP/3 на Netty, JSpecify, GraalVM 25.0.3, Groovy 5 и Kotlin 2.3.
- Micronaut Data: запросы репозиториев компилируются при сборке (JDBC, R2DBC, JPA, MongoDB). Неверное имя производного запроса ломает сборку, а не старт.
- Подходит для сервисов малого размера, serverless-функций и CLI; экосистема меньше, чем у Spring.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Micronaut Framework 5.0.0 released](https://micronaut.io/2026/05/20/micronaut-framework-5-0-0-released/) | Micronaut Foundation, 2026 | Изменения 5.0 |
| [Micronaut guide](https://docs.micronaut.io/latest/guide/) | Micronaut Foundation | Основное руководство |
| [Micronaut Data guide](https://micronaut-projects.github.io/micronaut-data/latest/guide/) | Micronaut Foundation | Micronaut Data |
| [Micronaut Guides](https://guides.micronaut.io/latest/index.html) | Micronaut Foundation | Пошаговые руководства |

### 1.4 Helidon

**Ключевые выводы**

- Helidon 4 принёс веб-сервер Níma, написанный под виртуальные потоки: блокирующий код, один виртуальный поток на запрос, без реактивного API. Остаётся ядром Helidon 27.
- Helidon 27 (сентябрь-октябрь 2026): номер следует за JDK (модель Oracle «Tip & Tail»): 27 — Tip на JDK 27; Helidon 4.5.x — Tail/LTS (база JDK 21, JDK 25 поддерживается). «Helidon Core» заменяет SE: императивная модель (без DI) и декларативная (DI, генерируемый при сборке). Helidon MicroProfile заменяет MP. Новые Helidon Messaging (блокирующий API над Kafka, JMS, Pulsar) и Helidon Data JDBC (репозитории, генерируемые при компиляции).
- Для долгоживущего продакшена берите Helidon 4.5 LTS; 27 и 28 — только если вы обновляете JDK каждые 6 месяцев.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Helidon 27 released](https://www.infoq.com/news/2026/10/helidon-27-released/) | InfoQ, 2026 | Обзор Helidon 27, Tip & Tail |
| [Helidon docs](https://helidon.io/docs/latest) | Oracle | Документация |
| [JEP 14: The Tip & Tail Model of Library Development](https://openjdk.org/jeps/14) | OpenJDK | Модель релизов Tip & Tail для библиотек |

### 1.5 Рантаймы Jakarta EE и MicroProfile

**Ключевые выводы**

- Актуальна Jakarta EE 11 (июнь 2025): Jakarta Data 1.0, Concurrency 3.1 (виртуальные потоки), Persistence 3.2, больше поддержки records, SecurityManager удалён. Цель Jakarta EE 12 сдвинута на конец 2026 `[не проверено]`.
- Рантаймы: WildFly 42 (Red Hat; upstream JBoss EAP), Open Liberty (IBM; ежемесячные `26.0.0.x`, InstantOn — checkpoint/restore на CRIU), Payara 7 (на базе GlassFish), GlassFish 8 (EE 11) и 9 (вехи EE 12), TomEE. Выбирайте их ради переносимости по стандартам или при готовом парке app-серверов.
- MicroProfile (Config, Health, Fault Tolerance, Rest Client, OpenAPI, Telemetry) — облачный слой поверх; реализуют Quarkus, Helidon MP, Open Liberty, Payara, WildFly.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Jakarta EE 11](https://jakarta.ee/release/11/) | Eclipse Foundation | Состав EE 11 |
| [Jakarta Data 1.0](https://jakarta.ee/specifications/data/1.0/) | Eclipse Foundation | Спецификация Jakarta Data |
| [Jakarta EE 12 consistency](https://www.infoq.com/news/2025/08/jakarta-ee-12-consistency/) | InfoQ, 2025 | Темы EE 12 |
| [WildFly news](https://www.wildfly.org/news/) | WildFly team | Посты о релизах |
| [Open Liberty InstantOn](https://openliberty.io/docs/latest/instanton.html) | IBM | Быстрый старт InstantOn |
| [MicroProfile](https://microprofile.io/) | Eclipse Foundation | Спецификации MicroProfile |

### 1.6 Лёгкие библиотеки

**Ключевые выводы**

- Javalin 7 (Jetty 12): небольшая библиотека REST и WebSocket с вариантом на виртуальных потоках; подходит для малых сервисов, внутренних инструментов и бэкендов ботов. Vert.x 5 (event loop Netty, API только на futures, колбэки убраны): реактивный набор высокой пропускной способности, на котором построен Quarkus. Jooby 4: модульный микрофреймворк.
- Spark Java `[устарело]`: последний релиз 2.9.4 (2022), репозиторий неактивен с 2023. Замена — Javalin.
- Без фреймворка: `com.sun.net.httpserver.HttpServer` и `Executors.newVirtualThreadPerTaskExecutor()` хватает для проб, крошечных админ-эндпоинтов и заглушек в тестах.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Javalin documentation](https://javalin.io/documentation) | Javalin team | Документация Javalin 7 |
| [Vert.x docs](https://vertx.io/docs/) | Eclipse Vert.x | Документация Vert.x 5 |
| [Vert.x 5 migration guide](https://vertx.io/docs/guides/vertx-5-migration-guide/) | Eclipse Vert.x | Миграция 4 → 5 |
| [Jooby](https://jooby.io/) | Jooby | Документация |
| [perwendel/spark](https://github.com/perwendel/spark) | Per Wendel | Проверка статуса: последний push 2023-10 |

### 1.7 Таблица выбора

| Задача | Выбор по умолчанию | Альтернативы | Почему |
|---|---|---|---|
| Обычный бизнес-сервис, большая команда, найм | Spring Boot 4.1 (4.2 — на 6 ноября 2026 в RC2, GA ожидается в конце ноября) | Quarkus | Самая большая экосистема, документация, интеграции |
| Быстрый старт и малый RSS в Kubernetes, native в приоритете | Quarkus 3.40 LTS | Micronaut 5, Spring Boot с AOT cache или native | Аугментация при сборке, Dev Services |
| Serverless-функции | Quarkus или Micronaut native; Spring Boot с SnapStart | простой handler с SnapStart | Доминирует холодный старт |
| Стандарты или существующий app-сервер | Jakarta EE 11 на WildFly, Open Liberty, Payara | Quarkus (MicroProfile) | Переносимость, поддержка вендора |
| Простой блокирующий код на виртуальных потоках, стек Oracle | Helidon 4.5 LTS | Spring Boot с виртуальными потоками | Сервер Níma |
| Малый HTTP-сервис, бэкенд бота, внутренний инструмент | Javalin 7 | JDK HttpServer, Jooby | Минимум зависимостей |
| Предельная пропускная способность, событийная модель | Vert.x 5 | Spring WebFlux, Netty | Event loop, back-pressure |
| Долгие workflow и саги | Фреймворк по выбору + Temporal | Camunda, Spring State Machine | Durable execution |

## 2. Веб-слой и API

### 2.1 REST: проектирование, валидация, ошибки, OpenAPI

**Ключевые выводы**

- Контроллеры тонкие: переводят HTTP в команды приложения, валидируют на границе, возвращают DTO (records), а не сущности JPA (иначе исключения ленивой загрузки, случайно открытые поля, mass assignment). Используйте `record`-DTO с аннотациями Jakarta Bean Validation и `@Valid` на `@RequestBody`; `@Validated` на классе — для ограничений `@PathVariable` и `@RequestParam` (Spring 6.1+ валидирует параметры методов нативно и бросает `HandlerMethodValidationException`).
- Ошибки: возвращайте RFC 9457 Problem Details (`application/problem+json`). В Spring: `spring.mvc.problemdetails.enabled=true` или наследование `ResponseEntityExceptionHandler` в `@RestControllerAdvice`; бросайте наследников `ErrorResponseException`. Framework 7.1 добавляет включаемый рендеринг ProblemDetail для необработанных исключений и больше не выставляет `instance` сам. Не отдавайте в `detail` стек-трейсы и SQL; идентификатор корреляции или трассировки кладите в extension-поле.

  ```java
  @RestControllerAdvice
  class ApiErrors extends ResponseEntityExceptionHandler {
    @ExceptionHandler(OrderNotFound.class)
    ProblemDetail notFound(OrderNotFound ex) {
      var pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Order " + ex.id() + " not found");
      pd.setType(URI.create("https://api.example.com/problems/order-not-found"));
      return pd;
    }
  }
  ```

- Идемпотентность: PUT и DELETE идемпотентны по определению; для POST, создающего движение денег, принимайте заголовок `Idempotency-Key` и храните (ключ, хеш запроса, ответ) с уникальным ограничением. Для оптимистичной конкурентности используйте `ETag` и `If-Match` (соответствует JPA `@Version`).
- Пагинация: курсорная (keyset) `WHERE id > :last ORDER BY id LIMIT n` для больших таблиц; offset деградирует линейно. Keyset-прокрутку реализуют Spring Data `Window`, `ScrollPosition` и `KeysetScrollPosition`.
- OpenAPI: springdoc-openapi 3.x для Boot 4 (2.x — для Boot 3) генерирует `/v3/api-docs` и Swagger UI; в продакшене отключите UI или защитите. Альтернатива «контракт первым»: плагин `openapi-generator` для Maven и Gradle генерирует интерфейсы.
- Версионирование API: предпочитайте нативное версионирование Spring 7 (заголовок или media type) копиям контроллеров `/v2`.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [RFC 9457: Problem Details](https://www.rfc-editor.org/rfc/rfc9457) | IETF, 2023 | Спецификация Problem Details (заменяет RFC 7807) |
| [ProblemDetail в MVC](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html) | Spring team | ProblemDetail и ErrorResponse в MVC |
| [Bean Validation в Spring](https://docs.spring.io/spring-framework/reference/core/validation/beanvalidation.html) | Spring team | Интеграция Bean Validation |
| [Jakarta Validation 3.1](https://jakarta.ee/specifications/bean-validation/3.1/) | Eclipse Foundation | Спецификация |
| [Hibernate Validator 9.1 reference](https://docs.hibernate.org/validator/9.1/reference/en-US/html_single/) | Hibernate team | Свои ограничения, группы |
| [springdoc](https://springdoc.org/) | springdoc | Документация и матрица версий |
| [OpenAPI Generator: spring](https://openapi-generator.tech/docs/generators/spring/) | OpenAPI Generator | Опции генератора для подхода «контракт первым» |
| [Idempotency-Key header (draft)](https://datatracker.ietf.org/doc/draft-ietf-httpapi-idempotency-key-header/) | IETF draft | Семантика `Idempotency-Key` |
| [Scrolling в Spring Data](https://docs.spring.io/spring-data/commons/reference/repositories/scrolling.html) | Spring team | Keyset-прокрутка |
| [No Offset](https://use-the-index-luke.com/no-offset) | Markus Winand | Почему offset-пагинация медленна |
| [API design best practices](https://learn.microsoft.com/en-us/azure/architecture/best-practices/api-design) | Microsoft | Руководство по REST, не привязанное к языку |
| [API Improvement Proposals](https://google.aip.dev/) | Google | Имена ресурсов, пагинация, ошибки |

### 2.2 GraphQL, gRPC, WebSocket

**Ключевые выводы**

- Spring for GraphQL 2.0 (Boot 4): `@SchemaMapping` и `@QueryMapping` со схемой первой; для устранения N+1 по полям используйте `@BatchMapping` или DataLoader; задайте лимиты глубины и сложности запроса (instrumentation), чтобы остановить злоупотребления. Netflix DGS теперь работает поверх Spring for GraphQL.
- gRPC: grpc-java 1.85; Spring Boot 4.1 автоконфигурирует Spring gRPC 1.1 (сервер на Netty или servlet-транспорт). Всегда задавайте дедлайны клиента (`withDeadlineAfter`), переиспользуйте `ManagedChannel`, включайте keepalive за L4-балансировщиками и применяйте клиентскую балансировку или service mesh (HTTP/2-соединение привязывается к одному бэкенду).
- WebSocket: Spring `@MessageMapping` и STOMP с внешним брокером (RabbitMQ или ActiveMQ) при масштабировании; обычные сессии локальны для узла. Для односторонней отправки рассмотрите SSE (`SseEmitter`, `Flux<ServerSentEvent>`).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Spring for GraphQL](https://docs.spring.io/spring-graphql/reference/) | Spring team | Справочник Spring for GraphQL |
| [DGS framework](https://netflix.github.io/dgs/) | Netflix | DGS (поверх Spring for GraphQL) |
| [gRPC Java](https://grpc.io/docs/languages/java/) | gRPC authors | Основы grpc-java |
| [gRPC deadlines](https://grpc.io/docs/guides/deadlines/) | gRPC authors | Дедлайны: задавайте всегда |
| [Spring gRPC](https://docs.spring.io/spring-grpc/reference/) | Spring team | Spring gRPC |
| [WebSocket в Spring](https://docs.spring.io/spring-framework/reference/web/websocket.html) | Spring team | WebSocket, STOMP, broker relay |

### 2.3 HTTP-клиенты

**Ключевые выводы**

- Выбор: в Spring — `RestClient` (блокирующий, fluent) или интерфейсы `@HttpExchange` над ним; в реактивных приложениях — `WebClient`. Вне Spring — JDK `java.net.http.HttpClient` (HTTP/2, async, без зависимостей), OkHttp 5 (Kotlin-first, сильные interceptor'ы), Apache HttpClient 5.6 (тонкая настройка пула соединений). Retrofit — типизированные интерфейсы поверх OkHttp.
- Всегда задавайте таймаут соединения И таймаут чтения/ответа. У JDK `HttpClient` `connectTimeout` задаётся в билдере, а таймаут ответа — на запрос (`HttpRequest.newBuilder().timeout(...)`); без него потоки зависают навсегда.
- Держите один экземпляр клиента (пул соединений); не создавайте клиент на каждый запрос. Закрывайте тела ответов (OkHttp).
- Spring Boot 4 автоконфигурирует фабрику клиентов (`spring.http.clients.*`; по умолчанию JDK-клиент, если нет другого) `[не проверено]` в части точных умолчаний. `InetAddressFilter` из Boot 4.1 блокирует запросы в частные диапазоны (SSRF).
- Заголовки трассировки пробрасываются автоматически, если строить клиенты из внедрённых `RestClient.Builder` и `WebClient.Builder` (observation зарегистрирован); `RestClient.create()` обходит инструментирование.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [java.net.http.HttpClient (JDK 25)](https://docs.oracle.com/en/java/javase/25/docs/api/java.net.http/java/net/http/HttpClient.html) | Oracle | API JDK HttpClient |
| [JDK HttpClient intro](https://openjdk.org/groups/net/httpclient/intro.html) | OpenJDK | Введение в JDK HttpClient |
| [REST client в Spring Boot](https://docs.spring.io/spring-boot/reference/io/rest-client.html) | Spring team | Автоконфигурация клиентов (RestClient, WebClient, HTTP-сервисы) |
| [square/okhttp](https://github.com/square/okhttp) | Square | OkHttp 5 (в README: выбирайте `okhttp-jvm` или `okhttp-android` в Maven) |
| [Apache HttpClient 5.6](https://hc.apache.org/httpcomponents-client-5.6.x/) | Apache | Документация HttpClient 5 |
| [square/retrofit](https://github.com/square/retrofit) | Square | Retrofit |

### 2.4 Устойчивость и ограничение частоты

**Ключевые выводы**

- Порядок защит: таймаут (всегда) → повтор (только идемпотентные операции, экспоненциальный backoff с jitter, малое число попыток) → circuit breaker (перестать долбить упавшую зависимость) → bulkhead (ограничить конкурентность на зависимость) → fallback. Повторы умножают нагрузку: 3 слоя по 3 повтора дают 27 вызовов.
- Resilience4j 2.4 имеет модули `resilience4j-spring-boot3` и `-spring-boot4`; аннотации `@CircuitBreaker`, `@Retry`, `@Bulkhead`, `@RateLimiter`, `@TimeLimiter`. Та же ловушка самовызова через прокси, что и у `@Transactional`. Для простых случаев `@Retryable` и `@ConcurrencyLimit` из Spring 7 избавляют от зависимости.
- Ограничение частоты: Bucket4j 8.22, token bucket локально (Caffeine) или распределённо (Redis через Lettuce или Redisson, JCache, Hazelcast, PostgreSQL). Лимиты на уровне шлюза (Spring Cloud Gateway `RequestRateLimiter`, Envoy, NGINX) дешевле кода в каждом сервисе, если доступны.
- MicroProfile Fault Tolerance (`@Timeout`, `@Retry`, `@CircuitBreaker`) — аналог для Quarkus, Helidon, Liberty.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Resilience4j getting started](https://resilience4j.readme.io/docs/getting-started) | Resilience4j | Документация |
| [resilience4j/resilience4j](https://github.com/resilience4j/resilience4j) | Resilience4j | Релизы, модуль для Boot 4 |
| [Bucket4j](https://bucket4j.com/) | Bucket4j | Справочник |
| [bucket4j/bucket4j](https://github.com/bucket4j/bucket4j) | Bucket4j | Релизы |
| [Timeouts, retries, and backoff with jitter](https://aws.amazon.com/builders-library/timeouts-retries-and-backoff-with-jitter/) | Marc Brooker (AWS), 2019 | Каноническая статья о повторах и jitter |
| [Using load shedding to avoid overload](https://aws.amazon.com/builders-library/using-load-shedding-to-avoid-overload/) | David Yanacek (AWS) | Сброс нагрузки |
| [Circuit Breaker](https://martinfowler.com/bliki/CircuitBreaker.html) | Martin Fowler, 2014 | Идея circuit breaker |
| [SmallRye Fault Tolerance](https://quarkus.io/guides/smallrye-fault-tolerance) | Quarkus team | MicroProfile Fault Tolerance |
| [Spring Cloud Gateway](https://docs.spring.io/spring-cloud-gateway/reference/) | Spring team | Лимиты и маршрутизация на шлюзе |

## 3. Доступ к данным

### 3.1 JDBC и пулы соединений (HikariCP)

**Ключевые выводы**

- HikariCP 7.1 — пул по умолчанию в Spring Boot и большинстве стеков. Размер пула: начните с `connections = (physical_cores * 2) + effective_spindle_count` для сервера БД, затем нагрузочно проверьте. Держите пулы малыми (не больше пары десятков) и позвольте потокам стоять в очереди. Минимум против взаимной блокировки, когда поток держит несколько соединений: `Tn x (Cm - 1) + 1`. Сумма «поды x размер пула» должна оставаться ниже `max_connections` БД (минус запас для администраторов). При многих репликах ставьте PgBouncer (transaction mode) или RDS Proxy.
- Важные настройки: `maximumPoolSize` (для фиксированного пула не задавайте `minimumIdle`), `connectionTimeout` (падать быстро, например 2-5 с вместо 30), `maxLifetime` на несколько секунд или минут меньше таймаута простоя на стороне БД или файрвола, `keepaliveTime`, `leakDetectionThreshold` на staging. Включите `register-mbeans` или метрики Micrometer: устойчиво ненулевой `hikaricp.connections.pending` означает исчерпание пула.
- С виртуальными потоками пул — реальный ограничитель конкурентности: тысячи виртуальных потоков встанут в очередь на `getConnection()`. Добавьте `connectionTimeout`, bulkhead или семафор, либо `@ConcurrencyLimit` на входе.
- Не держите соединение во время удалённых вызовов: HTTP внутри `@Transactional` под нагрузкой с задержкой приводит к голоданию пула.
- Boot 4.1 добавляет ленивое получение JDBC-соединения: соединение берётся при первом SQL-операторе, а не при начале транзакции.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [About Pool Sizing](https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing) | Brett Wooldridge | Формула размера пула и обоснование |
| [HikariCP](https://github.com/brettwooldridge/HikariCP) | Brett Wooldridge | Параметры и умолчания |
| [MySQL Configuration](https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration) | Brett Wooldridge | Настройка драйвера (кэш prepared statements) |
| [Number Of Database Connections](https://wiki.postgresql.org/wiki/Number_Of_Database_Connections) | PostgreSQL wiki | Почему меньше соединений быстрее |
| [PgBouncer features](https://www.pgbouncer.org/features.html) | PgBouncer | Оговорки transaction pooling (состояние сессии, prepared statements) |
| [pgJDBC: use](https://jdbc.postgresql.org/documentation/use/) | pgJDBC | `reWriteBatchedInserts`, `prepareThreshold` |
| [datasource-proxy](https://github.com/jdbc-observations/datasource-proxy) | datasource-proxy | Подсчёт и лог SQL на запрос в тестах |
| [P6Spy](https://github.com/p6spy/p6spy) | P6Spy | Прокси для логирования SQL |

### 3.2 JPA и Hibernate 7: производительность

**Ключевые выводы**

- N+1: связи `@ManyToOne` и `@OneToOne` по умолчанию EAGER в JPA. Ставьте `fetch = LAZY` везде, затем выбирайте данные под сценарий через `JOIN FETCH`, `@EntityGraph` или DTO-проекции. Находите проблему по статистике Hibernate, проверкам datasource-proxy (`assertSelectCount(1)`) или Hypersistence Optimizer. `hibernate.default_batch_fetch_size` (16-50) превращает ленивое N+1 в N/batch запросов — это страховка, а не исправление.
- Пагинация с fetch join коллекции: до 7.4 Hibernate применял limit в памяти (предупреждение HHH90003004, загружает всё). Hibernate 7.4 переносит limit в SQL на поддерживаемых БД; ради переносимости всё же лучше два запроса (страница id, затем выборка по id).
- Пакетная запись: `hibernate.jdbc.batch_size=20-50`, `order_inserts=true`, `order_updates=true`. `GenerationType.IDENTITY` отключает пакетные вставки; используйте `SEQUENCE` с pooled-оптимизатором (`allocationSize` 50). Для PostgreSQL нужен ещё `reWriteBatchedInserts=true`.
- Пути чтения: `@Transactional(readOnly = true)` позволяет Hibernate не хранить снимки для dirty checking (и Spring маршрутизирует на реплики, если настроено). Для отчётов только на чтение используйте DTO-проекции (конструкторные выражения `record`, проекции Spring Data) вместо сущностей; для массовой обработки — `StatelessSession`.
- Стоимость dirty checking растёт с числом управляемых сущностей: длинные сессии с тысячами сущностей замедляют каждый flush. В пакетных циклах периодически вызывайте `clear()` или применяйте `StatelessSession`.
- **OSIV.** Open Session in View в Spring Boot включён по умолчанию (`spring.jpa.open-in-view`), на старте пишется предупреждение. Выставьте `spring.jpa.open-in-view=false`: OSIV держит соединение весь запрос и прячет N+1 в представлениях и сериализаторах. После отключения ленивые связи надо загружать внутри транзакции сервиса.
- `equals` и `hashCode`: опирайтесь на естественный (бизнес-)ключ или на id с постоянным `hashCode()`; не ставьте Lombok `@Data` на сущности (ленивая загрузка в `toString`, сломанные хеш-множества).
- `@OneToMany`: двунаправленная связь с `mappedBy` и вспомогательными методами; избегайте однонаправленной `@OneToMany` без `@JoinColumn` (лишняя join-таблица); для many-to-many предпочитайте `Set`; не используйте `CascadeType.REMOVE` на `@ManyToMany`.
- Кэш второго уровня (JCache + Caffeine, Ehcache, Infinispan): только для редко меняющихся справочников; кэш запросов редко окупается; кластеру нужен распределённый или инвалидирующий провайдер.
- Hibernate 7: Jakarta Persistence 3.2, Hibernate Validator 9; `hibernate-jpamodelgen` → `hibernate-processor` (на JDK 23+ нужен путь процессора, см. «Главные ловушки 2026»); репозитории Jakarta Data генерирует `hibernate-processor`. 7.4 добавляет инкубационные `@Temporal` и `@Audited` (совместимо с Envers), обратная разработка схемы перенесена в ORM. Hibernate 8 (бета) нацелен на Jakarta Persistence 4.0.
- Hypersistence Optimizer (коммерческий, Vlad Mihalcea) на тестах сканирует маппинг и конфигурацию и находит антипаттерны; `hypersistence-utils` (OSS) добавляет типы JSON и массивов.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Hibernate ORM 7.4: what's new](https://docs.hibernate.org/orm/7.4/whats-new/) | Hibernate team, 2026 | Изменения 7.4 (temporal/audit, limit с fetch join) |
| [Hibernate ORM 7.4 introduction](https://docs.hibernate.org/orm/7.4/introduction/html_single/) | Gavin King et al. | Современное введение, глава о производительности |
| [Hibernate ORM 7.4 user guide](https://docs.hibernate.org/orm/7.4/userguide/html_single/) | Hibernate team | Полное руководство (загрузка, пакетная запись, кэш) |
| [Hibernate ORM 7.0 migration guide](https://docs.hibernate.org/orm/7.0/migration-guide/) | Hibernate team | Миграция 6 → 7 |
| [Hibernate ORM 7.4 announcement](https://in.relation.to/2026/05/26/orm-74/) | Hibernate team, 2026 | Анонс 7.4 |
| [N+1 query problem](https://vladmihalcea.com/n-plus-1-query-problem/) | Vlad Mihalcea | N+1: суть и обнаружение |
| [Open Session in View anti-pattern](https://vladmihalcea.com/the-open-session-in-view-anti-pattern/) | Vlad Mihalcea | Почему отключать OSIV |
| [Eager fetching is a code smell](https://vladmihalcea.com/eager-fetching-is-a-code-smell/) | Vlad Mihalcea | LAZY по умолчанию |
| [Batch insert and update with Hibernate](https://vladmihalcea.com/how-to-batch-insert-and-update-statements-with-hibernate/) | Vlad Mihalcea | Настройка JDBC-батчинга |
| [Best way to map @OneToMany](https://vladmihalcea.com/the-best-way-to-map-a-onetomany-association-with-jpa-and-hibernate/) | Vlad Mihalcea | Маппинг коллекций |
| [equals and hashCode with entity identifier](https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/) | Vlad Mihalcea | Равенство сущностей |
| [Spring read-only transaction optimization](https://vladmihalcea.com/spring-read-only-transaction-hibernate-optimization/) | Vlad Mihalcea | Выгоды readOnly |
| [Spring transaction best practices](https://vladmihalcea.com/spring-transaction-best-practices/) | Vlad Mihalcea | Где ставить транзакции, readOnly по умолчанию |
| [Best Spring Data JpaRepository](https://vladmihalcea.com/best-spring-data-jparepository/) | Vlad Mihalcea | Не вызывать `save` для управляемых сущностей, ловушки `findAll` |
| [Hypersistence Optimizer](https://vladmihalcea.com/hypersistence-optimizer/) | Vlad Mihalcea | Автоматический поиск антипаттернов JPA |
| [High-Performance Java Persistence](https://vladmihalcea.com/books/high-performance-java-persistence/) | Vlad Mihalcea, 2016-ongoing | Эталонная книга о производительности JDBC и JPA |
| [hypersistence-utils](https://github.com/vladmihalcea/hypersistence-utils) | Vlad Mihalcea | Типы JSON и массивов, утилиты (модуль `-hibernate-71`) |
| [5 ways to initialize lazy relations](https://thorben-janssen.com/5-ways-to-initialize-lazy-relations-and-when-to-use-them/) | Thorben Janssen | Способы инициализации ленивых связей |
| [Spring Data JPA](https://docs.spring.io/spring-data/jpa/reference/) | Spring team | Проекции, entity graph, прокрутка |
| [Hibernate repositories (Jakarta Data)](https://docs.hibernate.org/orm/7.4/repositories/html_single/) | Hibernate team | Репозитории Jakarta Data в Hibernate |

### 3.3 Транзакции: ловушки @Transactional

**Ключевые выводы**

- Прокси: самовызов (`this.save()` из другого метода того же бина) обходит прокси, транзакции нет. То же для `@Async`, `@Cacheable`, `@Retryable`, аннотаций Resilience4j. Решение: вынести в другой бин или использовать `TransactionTemplate`.
- Надёжно проксируются только публичные методы (Spring 6 поддерживает protected и package-private в class-based прокси, но оставляйте public). Аннотация на `private`-методе молча игнорируется.
- Правила отката: по умолчанию откатываются только unchecked-исключения и `Error`; checked-исключения фиксируют транзакцию. Используйте `rollbackFor` или бросайте unchecked. Если поймать исключение во внутреннем вызове REQUIRED, транзакция помечается rollback-only, и на коммите будет `UnexpectedRollbackException`.
- `REQUIRES_NEW` берёт второе соединение, пока первое занято: при пуле из N соединений и N параллельных вызовов пул встаёт в взаимную блокировку. Применяйте редко (журналы аудита) и учитывайте при расчёте пула.
- Границы транзакций — на уровне приложения и сервисов, короткие, без удалённого ввода-вывода. Побочные эффекты — через `TransactionalEventListener(phase = AFTER_COMMIT)` или паттерн outbox.
- `readOnly = true` как умолчание класса для сервисов запросов; методы записи переопределяют.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [@Transactional](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html) | Spring team | Семантика `@Transactional`, режим прокси |
| [Transaction propagation](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/tx-propagation.html) | Spring team | Распространение (REQUIRED, REQUIRES_NEW, NESTED) |
| [Transaction-bound events](https://docs.spring.io/spring-framework/reference/data-access/transaction/event.html) | Spring team | События, привязанные к транзакции |

### 3.4 Альтернативы JPA: Spring Data JDBC, jOOQ, MyBatis, Jdbi, JdbcClient

**Ключевые выводы**

- `JdbcClient` (Spring 6.1+) — современная fluent-обёртка над `JdbcTemplate`; хороший выбор для простого SQL с маппингом в records. Spring Data JDBC: репозитории по агрегатам без ленивой загрузки и dirty checking (проще ментальная модель, чем у JPA).
- jOOQ 3.21: типобезопасный SQL DSL, генерируемый из схемы (запускайте кодогенерацию на БД из Testcontainers, собранной Flyway). Открытая редакция поддерживает только открытые БД, для Oracle, SQL Server и других нужна коммерческая. Лучше всего при сложном SQL (оконные функции, CTE, upsert, MULTISET для вложенных коллекций без N+1).
- MyBatis 3.5: SQL в XML или аннотациях, зрелый; Jdbi 3: лёгкий fluent API и SQL-объекты. Оба подходят командам, думающим в SQL.
- R2DBC: только если весь стек реактивный; с виртуальными потоками блокирующий JDBC обычно проще и масштабируется так же.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [JDBC core: JdbcClient, JdbcTemplate](https://docs.spring.io/spring-framework/reference/data-access/jdbc/core.html) | Spring team | `JdbcClient` и `JdbcTemplate` |
| [Spring Data JDBC](https://docs.spring.io/spring-data/relational/reference/jdbc.html) | Spring team | Spring Data JDBC |
| [jOOQ manual](https://www.jooq.org/doc/latest/manual/) | Data Geekery | Руководство jOOQ |
| [jOOQ 3.15 MULTISET](https://blog.jooq.org/jooq-3-15s-new-multiset-operator-will-change-how-you-think-about-sql/) | Lukas Eder, 2021 | Вложенные коллекции через MULTISET |
| [MyBatis 3](https://mybatis.org/mybatis-3/) | MyBatis | Документация MyBatis |
| [Jdbi](https://jdbi.org/) | Jdbi | Документация Jdbi 3 |
| [R2DBC](https://r2dbc.io/) | R2DBC | Реактивный SPI для SQL |

### 3.5 Миграции схемы (Flyway, Liquibase)

**Ключевые выводы**

- Один инструмент миграций на базу данных, запуск один раз на развёртывание (init-задание или приложение на старте с блокировкой). В Boot 4 подключайте `spring-boot-starter-flyway` или `spring-boot-starter-liquibase`: голой библиотеки недостаточно.
- Flyway 13 (Redgate): версионные `V1__init.sql` и повторяемые `R__views.sql`; для многих БД нужны модули под конкретную базу (`flyway-database-postgresql`, `flyway-mysql`). Не правьте применённые миграции (ошибка контрольной суммы); исправляйте вперёд.
- Liquibase 5: changelog в SQL, YAML или XML, поддержка отката; лицензия сменилась на FSL с 5.0, а OSS-дистрибутив поставляется без расширений и драйверов (ставятся через `liquibase lpm`). Проверьте лицензию до внедрения.
- Без простоя: expand/contract (добавить nullable-колонку → заполнить → код читает новую → включить ограничение → удалить старую в следующем релизе); индексы `CONCURRENTLY` (PostgreSQL; Flyway нужна нетранзакционная миграция); задавайте `lock_timeout` и `statement_timeout` в DDL-миграциях.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Flyway docs](https://documentation.red-gate.com/flyway) | Redgate | Документация Flyway |
| [Liquibase docs](https://docs.liquibase.com/) | Liquibase | Документация Liquibase |
| [Liquibase Community and FSL](https://www.liquibase.com/blog/liquibase-community-for-the-future-fsl) | Liquibase, 2025 | Смена лицензии на FSL |
| [Data initialization](https://docs.spring.io/spring-boot/how-to/data-initialization.html) | Spring team | Boot с Flyway и Liquibase |
| [Evolutionary Database Design](https://martinfowler.com/articles/evodb.html) | Sadalage & Fowler | Эволюционное проектирование БД |
| [CREATE INDEX](https://www.postgresql.org/docs/current/sql-createindex.html) | PostgreSQL | `CREATE INDEX CONCURRENTLY` |

### 3.6 Кэш и клиенты NoSQL

**Ключевые выводы**

- Локальный кэш: Caffeine 3.3 (W-TinyLFU, границы по размеру и времени, `refreshAfterWrite`). Абстракция Spring Cache: `@Cacheable` с `spring.cache.type=caffeine`; всегда ограничивайте размер и TTL; помните про самовызов через прокси и про кэширование `null` и изменяемых объектов. Распределённый: Redis через Spring Data Redis (по умолчанию Lettuce). Двухуровневым кэшам нужна инвалидация (pub/sub) или короткие TTL.
- Cache stampede: локально используйте `refreshAfterWrite` и `AsyncLoadingCache` (объединяет загрузки); удалённо — замки Redisson или вероятностное раннее истечение.
- Клиенты Redis: Lettuce 7.x (потокобезопасный, одно общее соединение, async и reactive), Jedis (пул, простой), Redisson 4.x (распределённые объекты, замки, ограничители частоты; замки на Redis не дают fencing-безопасности).
- MongoDB Java driver 5.14 (sync и reactive streams); Spring Boot 4 перенёс свойства подключения в `spring.mongodb.*` и требует явного представления UUID и BigDecimal. Cassandra: Apache Cassandra Java driver 4.19 (теперь под Apache, `org.apache.cassandra:java-driver-core`).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Caffeine wiki](https://github.com/ben-manes/caffeine/wiki) | Ben Manes | Руководство по Caffeine (вытеснение, refresh, статистика) |
| [Cache abstraction](https://docs.spring.io/spring-framework/reference/integration/cache.html) | Spring team | Абстракция кэша |
| [Caching в Spring Boot](https://docs.spring.io/spring-boot/reference/io/caching.html) | Spring team | Провайдеры кэша в Boot |
| [Lettuce](https://redis.github.io/lettuce/) | Redis | Документация Lettuce |
| [Redisson docs](https://redisson.org/docs/) | Redisson | Документация Redisson |
| [How to do distributed locking](https://martin.kleppmann.com/2016/02/08/how-to-do-distributed-locking.html) | Martin Kleppmann, 2016 | Почему Redlock небезопасен для корректности |
| [MongoDB Java sync driver](https://www.mongodb.com/docs/drivers/java/sync/current/) | MongoDB | Синхронный драйвер для Java |
| [Spring Data MongoDB](https://docs.spring.io/spring-data/mongodb/reference/) | Spring team | Spring Data MongoDB |
| [Cassandra Java driver](https://apache.github.io/cassandra-java-driver/) | Apache | Драйвер Cassandra для Java |

## 4. Сообщения и стриминг

**Ключевые выводы**

- Kafka 4.x: только KRaft (ZooKeeper удалён в 4.0); новый протокол ребалансировки потребителей (KIP-848, включается `group.protocol=consumer`) убирает ребалансировки с остановкой всего; share groups, «очереди для Kafka» (KIP-932), готовы к продакшену в 4.2 (подтверждение по записи, потребителей больше, чем партиций); в Kafka Streams серверный протокол ребалансировки GA (ограниченно), а в 4.2 появилась поддержка DLQ в обработчиках исключений.
- Умолчания продюсера безопасны с 3.0 (`acks=all`, `enable.idempotence=true`); не меняйте их. Транзакции (`transactional.id`) дают exactly-once только внутри Kafka (consume-transform-produce). Если участвует БД, используйте outbox и идемпотентных потребителей.
- Ловушки потребителя: обработка дольше `max.poll.interval.ms` вызывает шторм ребалансировок; коммитьте после обработки (at-least-once) и делайте обработчики идемпотентными (таблица дедупликации по id сообщения или естественный upsert). Spring Kafka: `DefaultErrorHandler` с `DeadLetterPublishingRecoverer` и ограниченным backoff; неблокирующие повторы через `@RetryableTopic` (retry-топики). Не блокируйтесь бесконечно на «ядовитых» сообщениях.
- Outbox: пишите бизнес-строку и строку `outbox` в одной транзакции БД; доставляйте через Debezium CDC (логическая репликация Postgres → Kafka; Debezium 3.7) или опрашивающий publisher (реестр публикации событий Spring Modulith, Namastack outbox и др.). Потребители всё равно должны дедуплицировать (доставка at-least-once).
- RabbitMQ: publisher confirms и флаг mandatory; ручные подтверждения с prefetch (`spring.rabbitmq.listener.simple.prefetch`), соразмерным времени обработки; quorum queues (не классические зеркалируемые) для HA; streams для повторного чтения. Spring AMQP 4.x.
- JMS: `JmsClient` в Spring Framework 7; расширение JMS появляется в Quarkus 4.
- Quarkus, Helidon, Micronaut: SmallRye Reactive Messaging (`@Incoming`, `@Outgoing`), Helidon Messaging (блокирующий API).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Kafka documentation](https://kafka.apache.org/documentation/) | Apache Kafka | Параметры, семантика |
| [Apache Kafka 4.2.0 release announcement](https://kafka.apache.org/blog/2026/02/17/apache-kafka-4.2.0-release-announcement/) | Apache Kafka, 2026 | Share groups в продакшене, изменения Streams `[не проверено]` |
| [Introducing Apache Kafka 4.0](https://www.confluent.io/blog/introducing-apache-kafka-4-0/) | Confluent, 2025 | Только KRaft, KIP-848 |
| [KIP-932: Queues for Kafka](https://cwiki.apache.org/confluence/display/KAFKA/KIP-932%3A+Queues+for+Kafka) | Apache Kafka | Дизайн share groups |
| [KIP-848](https://cwiki.apache.org/confluence/display/KAFKA/KIP-848%3A+The+Next+Generation+of+the+Consumer+Rebalance+Protocol) | Apache Kafka | Новый протокол потребителя |
| [Spring for Apache Kafka](https://docs.spring.io/spring-kafka/reference/) | Spring team | Spring Kafka (обработчики ошибок, retry-топики, транзакции) |
| [Kafka Streams](https://kafka.apache.org/documentation/streams/) | Apache Kafka | Kafka Streams |
| [Kafka Streams course](https://developer.confluent.io/courses/kafka-streams/get-started/) | Confluent | Курс по Kafka Streams |
| [Transactional outbox](https://microservices.io/patterns/data/transactional-outbox.html) | Chris Richardson | Паттерн outbox |
| [Outbox Event Router](https://debezium.io/documentation/reference/stable/transformations/outbox-event-router.html) | Debezium | SMT для outbox |
| [Debezium PostgreSQL connector](https://debezium.io/documentation/reference/stable/connectors/postgresql.html) | Debezium | Коннектор CDC для Postgres |
| [Spring Modulith events](https://docs.spring.io/spring-modulith/reference/events.html) | Spring team | Реестр публикации событий (похож на outbox) |
| [RabbitMQ reliability](https://www.rabbitmq.com/docs/reliability) | RabbitMQ | Confirms, acks, долговечность |
| [Quorum queues](https://www.rabbitmq.com/docs/quorum-queues) | RabbitMQ | Quorum queues |
| [Spring AMQP](https://docs.spring.io/spring-amqp/reference/) | Spring team | Spring AMQP |
| [SmallRye Reactive Messaging](https://smallrye.io/smallrye-reactive-messaging/latest/) | SmallRye | Reactive Messaging (Quarkus) |
| [Enterprise Integration Patterns](https://www.enterpriseintegrationpatterns.com/) | Hohpe & Woolf | Каталог EIP (idempotent receiver, DLQ) |

## 5. Наблюдаемость

**Ключевые выводы**

- Логирование: API SLF4J 2 и Logback 1.5/1.6 (умолчание Boot) или Log4j 2.26. Параметризованные сообщения (`log.info("order {} paid", id)`), никогда не конкатенация; не логируйте секреты и персональные данные; одна строка лога на событие.
- Структурные JSON-логи: Spring Boot 3.4+ `logging.structured.format.console=ecs|logstash|gelf`; записи MDC и fluent-метод SLF4J `addKeyValue` становятся полями JSON; `logging.structured.json.add/rename/exclude`; свой `StructuredLogFormatter`. Вне Boot: logstash-logback-encoder 9.0, Log4j `JsonTemplateLayout`.
- Метрики: Micrometer 1.17 (`Timer`, `Counter`, `DistributionSummary`, `@Timed`, `@Counted`, `@MeterTag`), реестр Prometheus или OTLP. Главная ловушка — кардинальность: не ставьте в теги id пользователя, сырой URL, текст исключения. Гистограммы перцентилей включайте только на ключевых таймерах (`management.metrics.distribution.percentiles-histogram`).
- Трассировка: Micrometer Observation API и Micrometer Tracing (мост OTel) в Spring; `spring-boot-starter-opentelemetry` в Boot 4 экспортирует трассы и метрики по OTLP; Boot 4.1 добавляет конфигурацию sampler, лимиты спанов и логов, OTLP exemplars и читает стандартные переменные `OTEL_*`. `management.tracing.enabled` переименовано в `management.tracing.export.enabled`.
- Агент OpenTelemetry Java 2.33 (без кода, инструментирует байткод 100+ библиотек) или стартер OTel для Spring Boot (без агента, декларативный YAML с 2.26, экспериментально). Не сочетайте агент с настройкой SDK OTel в приложении (дубли спанов). В Quarkus есть `quarkus-opentelemetry`; 4.0 объединяет через Observation API.
- Распространение контекста: MDC и контекст трассировки привязаны к потоку; с `@Async`, executor'ами и реактивным кодом используйте `ContextPropagatingTaskDecorator` или Micrometer Context Propagation; виртуальные потоки ничего не наследуют от родительского потока автоматически, кроме как через обёртку executor'а.
- JFR в продакшене: накладные расходы около 1%, `-XX:StartFlightRecording=disk=true,maxage=6h,settings=default`; JFR Event Streaming (`RecordingStream`) для оповещений внутри процесса; `jcmd <pid> JFR.dump`. Подробнее — [performance.md](performance.md).
- Health: Actuator `/actuator/health/liveness` и `/readiness` (в Boot 4 включены по умолчанию); readiness может проверять зависимости, liveness — нет (сбой БД не должен перезапускать все поды).
- Безопасность Actuator: открывайте только `health,info,prometheus` на отдельном порту управления (`management.server.port`); никогда публично `env`, `heapdump`, `threaddump`, запись в `loggers`; heap dump содержит секреты.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Logging в Spring Boot](https://docs.spring.io/spring-boot/reference/features/logging.html) | Spring team | Структурное логирование (4.1.2) |
| [SLF4J manual](https://www.slf4j.org/manual.html) | QOS.ch | Fluent API SLF4J 2 |
| [Logback manual](https://logback.qos.ch/manual/) | QOS.ch | Руководство Logback |
| [JSON Template Layout](https://logging.apache.org/log4j/2.x/manual/json-template-layout.html) | Apache | Шаблонный JSON-layout Log4j |
| [logstash-logback-encoder](https://github.com/logfellow/logstash-logback-encoder) | logfellow | JSON-энкодер для Logback |
| [Micrometer reference](https://docs.micrometer.io/micrometer/reference/) | Micrometer team | Понятия метрик, именование, кардинальность |
| [Micrometer Tracing](https://docs.micrometer.io/tracing/reference/) | Micrometer team | Мосты трассировки |
| [Context Propagation](https://docs.micrometer.io/context-propagation/reference/) | Micrometer team | Распространение контекста между потоками |
| [Observability в Spring Boot](https://docs.spring.io/spring-boot/reference/actuator/observability.html) | Spring team | Observation и OTel в Boot |
| [Actuator endpoints](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html) | Spring team | Открытие эндпоинтов и их защита |
| [OpenTelemetry Java agent](https://opentelemetry.io/docs/zero-code/java/agent/) | OpenTelemetry | Java-агент |
| [OpenTelemetry Spring Boot starter](https://opentelemetry.io/docs/zero-code/java/spring-boot-starter/) | OpenTelemetry | Стартер OTel для Spring Boot |
| [Spring Boot declarative config](https://opentelemetry.io/blog/2026/spring-boot-declarative-config/) | OpenTelemetry, 2026 | Декларативная конфигурация стартера |
| [opentelemetry-java-instrumentation](https://github.com/open-telemetry/opentelemetry-java-instrumentation) | OpenTelemetry | Список поддерживаемых библиотек, релизы |
| [OpenTelemetry with Spring Boot](https://spring.io/blog/2025/11/18/opentelemetry-with-spring-boot) | Spring team, 2025 | Стартер OTel в Boot 4 |
| [Quarkus OpenTelemetry](https://quarkus.io/guides/opentelemetry) | Quarkus team | OTel в Quarkus |
| [JFR API](https://docs.oracle.com/en/java/javase/25/jfapi/) | Oracle | API JFR и потоковая передача событий |
| [Monitoring Distributed Systems](https://sre.google/sre-book/monitoring-distributed-systems/) | Google SRE | Четыре золотых сигнала |

## 6. Безопасность

**Ключевые выводы**

- Spring Security 7: удалён `and()` — только лямбда-DSL; `authorizeRequests` заменён `authorizeHttpRequests`; `AntPathRequestMatcher` и `MvcRequestMatcher` заменены `PathPatternRequestMatcher`; Spring Authorization Server и Kerberos влиты в Spring Security. Новое: MFA (`@EnableMultiFactorAuthentication`), passkeys и WebAuthn, `csrf.spa()` для SPA, кодировщики на Password4j (Argon2, BCrypt, scrypt, PBKDF2, Balloon), OAuth2 для HTTP-клиентов, PKCE по умолчанию в authorization server; OAuth2 password grant удалён. 7.1 добавляет `InetAddressMatcher`, условный MFA, `RestClientOpaqueTokenIntrospector`.

  ```java
  @Bean SecurityFilterChain api(HttpSecurity http) throws Exception {
    return http
      .authorizeHttpRequests(a -> a
        .requestMatchers("/actuator/health/**").permitAll()
        .requestMatchers(HttpMethod.POST, "/orders/**").hasAuthority("SCOPE_orders:write")
        .anyRequest().authenticated())
      .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()))
      .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .csrf(c -> c.disable()) // допустимо только для stateless API с bearer-токеном, не для cookie-сессий
      .build();
  }
  ```

- Resource server: `spring.security.oauth2.resourceserver.jwt.issuer-uri` проверяет `iss`, подпись по JWKS и `exp`; проверку аудитории (`aud`) добавьте сами (`JwtValidators`); scopes и роли сопоставляйте через `JwtAuthenticationConverter`. Если нужен мгновенный отзыв, предпочитайте непрозрачные токены с интроспекцией.
- Keycloak 26 как IdP (OIDC, realms, token exchange); используйте стандартную поддержку Spring OAuth2 client и resource server, а не удалённые адаптеры Keycloak `[устарело]: keycloak-spring-boot-adapter`.
- Пароли: `PasswordEncoderFactories.createDelegatingPasswordEncoder()` (по умолчанию bcrypt, префикс `{id}` позволяет обновлять алгоритм) или Argon2id с параметрами OWASP; никогда SHA-256 или MD5.
- CORS: настраивайте через бин `CorsConfigurationSource`, который использует Spring Security; никогда `allowedOrigins("*")` вместе с credentials. Framework 7.1 удалил `HandlerMappingIntrospector`, что может изменить способ поиска источника CORS.
- OWASP Top 10 для Java-сервисов: нарушенный контроль доступа (проверяйте владение ресурсом по id, а не только роль: IDOR), инъекции (конкатенация JPQL и SQL, SpEL из пользовательского ввода, LDAP, инъекция в логи), SSRF (проверяйте исходящие URL; `InetAddressFilter` в Boot 4.1), небезопасная десериализация (не десериализуйте недоверенные данные средствами Java; default typing Jackson выключен), XXE (защищённый `DocumentBuilderFactory`), ошибки конфигурации (открытый Actuator, консоль H2, Swagger UI), уязвимые зависимости (OWASP Dependency-Check, OSV-Scanner, Dependabot и Renovate — см. [toolchain.md](toolchain.md)).
- Секреты: не в `application.yml` и не в образах; переменные окружения или Secrets Kubernetes, смонтированные файлами, с импортом Spring `configtree:`; Vault (Spring Cloud Vault); AWS Secrets Manager и Parameter Store (Spring Cloud AWS); External Secrets Operator. Ротируйте учётные данные БД (динамические секреты Vault и `maxLifetime` у Hikari).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Spring Security 7.0: what's new](https://docs.spring.io/spring-security/reference/7.0/whats-new.html) | Spring team, 2025 | Удаления и возможности 7.0 |
| [Spring Security: what's new](https://docs.spring.io/spring-security/reference/whats-new.html) | Spring team, 2026 | Изменения 7.1 |
| [Migrating to Spring Security 7](https://docs.spring.io/spring-security/reference/migration/index.html) | Spring team | Миграция на 7 |
| [JWT resource server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html) | Spring team | JWT resource server |
| [Passkeys](https://docs.spring.io/spring-security/reference/servlet/authentication/passkeys.html) | Spring team | Passkeys |
| [CSRF](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html) | Spring team | CSRF, включая SPA |
| [Password storage](https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html) | Spring team | Кодировщики паролей |
| [Keycloak documentation](https://www.keycloak.org/documentation) | Keycloak | Документация Keycloak |
| [OWASP Top 10](https://owasp.org/Top10/) | OWASP | Top 10 |
| [Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html) | OWASP | Параметры Argon2id и bcrypt |
| [SSRF Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Server_Side_Request_Forgery_Prevention_Cheat_Sheet.html) | OWASP | Защита от SSRF |
| [Deserialization Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Deserialization_Cheat_Sheet.html) | OWASP | Десериализация в Java |
| [XXE Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html) | OWASP | XXE в Java-парсерах |
| [JSON Web Token Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_Cheat_Sheet.html) | OWASP | Ловушки JWT (старая страница «for Java» переименована и отдаёт 404) |
| [REST Security Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html) | OWASP | Безопасность REST |
| [OWASP API Security](https://owasp.org/www-project-api-security/) | OWASP | API Security Top 10 (BOLA, IDOR) |
| [Externalized configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html) | Spring team | `configtree:` и импорт конфигурации |
| [Spring Cloud Vault](https://docs.spring.io/spring-cloud-vault/reference/) | Spring team | Интеграция с Vault |
| [Spring Security guide](https://www.marcobehler.com/guides/spring-security) | Marco Behler | Архитектура Spring Security простыми словами |

## 7. Развёртывание: контейнеры, JVM в Kubernetes, старт

**Ключевые выводы**

- Образы: Jib 3.5 (без Dockerfile и демона, воспроизводимые слои: зависимости, ресурсы, классы); Cloud Native Buildpacks через `mvn spring-boot:build-image` или `gradle bootBuildImage` (Paketo Java, включает калькулятор памяти и опциональное обучение CDS и AOT cache); либо многоступенчатый Dockerfile с `java -Djarmode=tools -jar app.jar extract --layers --launcher` (Boot 3.3+; старый режим `layertools` удалён в Boot 4.1). Основа: образ с JRE (не JDK), distroless или Chiseled Ubuntu; запуск не от root; закрепляйте digest.
- jlink: `jdeps --print-module-deps --ignore-missing-deps`, затем `jlink --add-modules ... --strip-debug --no-header-files --no-man-pages` даёт рантайм 50-80 МБ для небольших сервисов (Spring требует больше модулей: проверьте).
- JVM в контейнерах: поддержка контейнеров включена по умолчанию (cgroup v2 поддерживается с JDK 15 и 11.0.16). Размер кучи задавайте долей от лимита: `-XX:MaxRAMPercentage=70..75` (оставьте место для metaspace, потоков, direct buffers, code cache) или явный `-Xmx` по калькулятору памяти. При менее чем 2 CPU или менее чем 1792 МБ JVM выбирает SerialGC: явно задайте `-XX:+UseG1GC` (или ZGC) и дайте не меньше 2 CPU. Лимиты CPU вызывают троттлинг CFS и долгие паузы GC; многие команды задают requests, но не задают лимит CPU. `-XX:ActiveProcessorCount=N` переопределяет определение числа процессоров. Добавьте `-XX:+ExitOnOutOfMemoryError`, чтобы Kubernetes перезапустил под, а не оставил JVM-«зомби».
- Пробы: liveness — `/actuator/health/liveness` (без проверок зависимостей), readiness — `/actuator/health/readiness`, `startupProbe` для медленного старта вместо большого `initialDelaySeconds`. Quarkus: `/q/health/live|ready|started`.
- Graceful shutdown: Boot включает `server.shutdown=graceful` по умолчанию с 3.4 `[не проверено]`; задайте `spring.lifecycle.timeout-per-shutdown-phase` меньше `terminationGracePeriodSeconds` и добавьте `preStop` со сном 5-10 с, чтобы эндпоинт ушёл из Service и Ingress до того, как сервер перестанет принимать запросы. Потребители Kafka должны на остановке закоммитить офсеты и выйти из группы.
- Варианты ускорения старта (от самого быстрого во внедрении):
  1. Распакованный jar и AOT cache на JDK 25+ (JEP 483, 514, 515): обучение `java -XX:AOTCacheOutput=app.aot -Dspring.context.exit=onRefresh -jar app.jar`, затем `java -XX:AOTCache=app.aot -jar app.jar`. Нужны тот же JDK и тот же class path; работает только с распакованной раскладкой. На JDK ниже 25 используйте CDS (`-XX:ArchiveClassesAtExit`, `-XX:SharedArchiveFile`). Типичный выигрыш 30-50%.
  2. Обработка Spring AOT (`process-aot`) и JVM: убирает часть рефлексии; обязательна для native.
  3. CRaC (сборки Azul Zulu или Liberica с CRaC; только Linux): контрольная точка прогретой JVM и восстановление за десятки мс; Spring `-Dspring.context.checkpoint=onRefresh -XX:CRaCCheckpointTo=dir`; ресурсы (сокеты, пулы) нужно закрывать и открывать через `org.crac.Resource`; секреты, попавшие в образ, — риск.
  4. GraalVM native image (Spring Boot native, Quarkus и Micronaut native): старт около 50-100 мс, низкий RSS, но долгая сборка, конфигурация рефлексии «закрытого мира», ниже пиковая пропускная способность без PGO, сложнее профилирование.
  5. AWS Lambda SnapStart (управляемые рантаймы Java 21 и 25): снимок Firecracker после init; хуки CRaC обновляют случайность и соединения; priming (вызов путей кода до снимка) улучшает результат.
- Serverless: рассмотрите также Spring Cloud Function, Quarkus Amazon Lambda, обработчики функций Micronaut; держите класс-обработчик маленьким, клиентов создавайте вне обработчика.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [AOT cache в Spring Boot](https://docs.spring.io/spring-boot/reference/packaging/aot-cache.html) | Spring team | Команды AOT cache и CDS |
| [Efficient deployments](https://docs.spring.io/spring-boot/reference/packaging/efficient.html) | Spring team | Распаковка jar |
| [Container images](https://docs.spring.io/spring-boot/reference/packaging/container-images/index.html) | Spring team | Dockerfile, buildpacks, слои |
| [Native image в Spring Boot](https://docs.spring.io/spring-boot/reference/packaging/native-image/index.html) | Spring team | Native image с Spring AOT |
| [Checkpoint/restore](https://docs.spring.io/spring-framework/reference/integration/checkpoint-restore.html) | Spring team | Поддержка CRaC |
| [JEP 483](https://openjdk.org/jeps/483) | OpenJDK | AOT-загрузка и связывание классов |
| [JEP 514](https://openjdk.org/jeps/514) | OpenJDK | Эргономика командной строки AOT |
| [JEP 515](https://openjdk.org/jeps/515) | OpenJDK | AOT-профилирование методов |
| [Run AOT cache](https://inside.java/2026/01/09/run-aot-cache/) | Inside Java, 2026 | Практический разбор AOT cache |
| [Project Leyden: JEP 514 and 515](https://softwaremill.com/whats-new-in-project-leyden-jep-514-and-jep-515-explained/) | SoftwareMill | Объяснение JEP Leyden |
| [Jib](https://github.com/GoogleContainerTools/jib) | Google | Jib |
| [Paketo Java how-to](https://paketo.io/docs/howto/java/) | Paketo | Опции Java-buildpack (калькулятор памяти, версия JVM) |
| [jlink](https://docs.oracle.com/en/java/javase/25/docs/specs/man/jlink.html) | Oracle | jlink |
| [Distroless](https://github.com/GoogleContainerTools/distroless) | Google | Distroless-образы Java |
| [Java 17: container awareness](https://developers.redhat.com/articles/2022/04/19/java-17-whats-new-openjdks-container-awareness) | Red Hat, 2022 | Поддержка контейнеров, эргономика `[не проверено]` (curl получает 403) |
| [Java containers overview](https://learn.microsoft.com/en-us/azure/developer/java/containers/overview) | Microsoft | Размеры JVM в контейнерах (выбор GC, CPU, память) |
| [Probes в Kubernetes](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/) | Kubernetes | Пробы |
| [Graceful shutdown](https://docs.spring.io/spring-boot/reference/web/graceful-shutdown.html) | Spring team | Плавная остановка |
| [Kubernetes probes в Actuator](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html#actuator.endpoints.kubernetes-probes) | Spring team | Группы проб |
| [CRaC](https://crac.org/) | CRaC project | Обзор CRaC |
| [CRaC/docs](https://github.com/CRaC/docs) | CRaC project | Документация и FAQ CRaC |
| [GraalVM Native Image](https://www.graalvm.org/latest/reference-manual/native-image/) | Oracle | Справочник Native Image |
| [Lambda SnapStart](https://docs.aws.amazon.com/lambda/latest/dg/snapstart.html) | AWS | SnapStart |
| [AWS Lambda now supports Java 25](https://aws.amazon.com/blogs/compute/aws-lambda-now-supports-java-25) | AWS, 2025 | Рантайм Java 25 и SnapStart |
| [Spring Cloud Function](https://docs.spring.io/spring-cloud-function/reference/) | Spring team | Функции на Lambda, Azure, GCP |

## 8. Боты, интеграции, batch, планировщики, workflow

**Ключевые выводы**

- Telegram: `org.telegram:telegrambots-*` 10.x (rubenlagus; разделён на `telegrambots-client` (OkHttp) и `telegrambots-longpolling` / `-webhook`, есть стартер Spring Boot) или `com.github.pengrad:java-telegram-bot-api` (тонкая, генерируется из Bot API). В продакшене используйте webhook за HTTPS, long polling — для разработки; обрабатывайте обновления идемпотентно (`update_id`); токен храните в секретах, не в коде.
- Discord: JDA 6.x (gateway и REST; `GatewayIntent` надо включить в портале разработчика, MESSAGE_CONTENT — привилегированный); Discord4J для реактивного стиля. Slack: Bolt for Java.
- Spring Batch 6 (Boot 4): шаги с chunk'ами, перезапуск через репозиторий заданий; стартер Boot 4 по умолчанию в памяти, для перезапускаемых продакшен-заданий возьмите `spring-boot-starter-batch-jdbc`; Boot 4.1 добавляет репозиторий заданий на MongoDB. Для масштаба — partitioning или remote chunking; держите транзакции chunk'ов короткими.
- Планировщики: `@Scheduled` срабатывает на каждой реплике. Используйте ShedLock 7 (`@SchedulerLock`, провайдеры блокировок JDBC, Redis, Mongo; `lockAtMostFor` больше максимального времени работы), либо кластерный JobStore Quartz 2.5 (постоянные задания, обработка misfire), либо Kubernetes CronJob. Quarkus: `quarkus-scheduler` (плюс расширение Quartz для кластеризации).
- Workflow и durable execution: Temporal Java SDK 1.40 (workflow — детерминированный код: внутри нельзя `Thread.sleep`, `Random`, `System.currentTimeMillis()`; используйте `Workflow.sleep`, activities для ввода-вывода, API версионирования при изменениях). Альтернативы: Camunda 8 (BPMN, Zeebe; лицензирование self-managed в продакшене менялось в последних версиях `[не проверено]`), Conductor.
- Интеграции: Apache Camel 4 (300+ компонентов, рантаймы Quarkus и Spring Boot) и Spring Integration 7.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [TelegramBots](https://github.com/rubenlagus/TelegramBots) | Ruben Bermudez | Библиотека TelegramBots |
| [TelegramBots documentation](https://rubenlagus.github.io/TelegramBotsDocumentation/) | Ruben Bermudez | Документация TelegramBots (API v7+) |
| [java-telegram-bot-api](https://github.com/pengrad/java-telegram-bot-api) | Stas Parshin | Альтернативный клиент Telegram |
| [Telegram Bot API](https://core.telegram.org/bots/api) | Telegram | Справочник Bot API |
| [Telegram webhooks](https://core.telegram.org/bots/webhooks) | Telegram | Требования к webhook |
| [JDA](https://github.com/discord-jda/JDA) | JDA team | JDA |
| [JDA wiki](https://jda.wiki/) | JDA team | Wiki и руководства JDA |
| [Bolt for Java](https://tools.slack.dev/java-slack-sdk/guides/bolt-basics/) | Slack | Bolt for Java |
| [Spring Batch](https://docs.spring.io/spring-batch/reference/) | Spring team | Spring Batch 6 |
| [ShedLock](https://github.com/lukas-krecan/ShedLock) | Lukas Krecan | ShedLock |
| [Quartz documentation](https://www.quartz-scheduler.org/documentation/) | Quartz | Документация Quartz (кластеризация, misfire) |
| [Quartz в Spring Boot](https://docs.spring.io/spring-boot/reference/io/quartz.html) | Spring team | Boot и Quartz |
| [Temporal Java developer guide](https://docs.temporal.io/develop/java) | Temporal | Руководство разработчика Java SDK |
| [Temporal deterministic constraints](https://docs.temporal.io/workflows#deterministic-constraints) | Temporal | Правила детерминизма |
| [Apache Camel manual](https://camel.apache.org/manual/) | Apache Camel | Руководство Camel |
| [Spring Integration](https://docs.spring.io/spring-integration/reference/) | Spring team | Spring Integration |

## 9. Производительность сервера: симптомы

Таблица связывает симптом с первыми проверками. Профилировщики и бенчмарки (async-profiler, JFR, JMC, JMH) — в [performance.md](performance.md).

| Симптом | Первые проверки | Вероятные причины | Направление исправления | Раздел |
|---|---|---|---|---|
| Один эндпоинт медленный, остальные нет | Водопад трассы (OTel), число SQL на запрос, JFR | N+1, нет индекса, удалённый вызов в цикле, огромный JSON | План загрузки или DTO-проекция, `EXPLAIN ANALYZE`, пакетные удалённые вызовы | 3.2, performance.md |
| p99 скачет каждые несколько минут | Логи GC (`-Xlog:gc*`), метрики троттлинга CPU контейнера | Паузы GC при малой куче или SerialGC, троттлинг CFS | Явно G1 или ZGC, >= 2 CPU, убрать лимит CPU, подобрать кучу | 7, performance.md |
| Таймауты под нагрузкой, CPU низкий | `hikaricp.connections.pending`, `active == max`, thread dump | Исчерпание пула: долгие транзакции, удалённый ввод-вывод в транзакции, OSIV, `REQUIRES_NEW` | Сократить транзакции, отключить OSIV, короткий `connectionTimeout`, bulkhead | 3.1, 3.3 |
| Пропускная способность не растёт, много потоков WAITING/BLOCKED | `jcmd <pid> Thread.dump_to_file -format=json`, события блокировок JFR | Голодание пула потоков (Tomcat `max-threads` 200), горячая `synchronized`-блокировка, блокировка event loop | Виртуальные потоки, асинхронные границы, убрать конкуренцию, не блокировать цикл Netty или Vert.x | [concurrency-native.md](concurrency-native.md) |
| Память растёт до OOMKilled (не Java OOM) | RSS против кучи, NMT (`-XX:NativeMemoryTracking=summary`) | Слишком большая доля кучи, direct buffers, стеки потоков, арены glibc | Снизить `MaxRAMPercentage`, ограничить direct memory, `MALLOC_ARENA_MAX=2` | 7 |
| OOM кучи Java | Дамп кучи при OOM (`-XX:+HeapDumpOnOutOfMemoryError`), MAT или JFR OldObjectSample | Кэши без границ, загрузка целых таблиц, утечки сессий | Ограничить кэши, потоковая выборка и пагинация, `StatelessSession` | 3.6 |
| Медленный старт и медленное масштабирование | Actuator startup (`/actuator/startup`), `-Xlog:class+load` | Сканирование class path, жадная инициализация, холодный JIT | AOT cache или CDS, lazy init в dev, native image, CRaC | 7 |
| Растёт отставание потребителя Kafka | Метрики lag, время обработки против `max.poll.interval.ms` | Медленный обработчик, шторм ребалансировок, горячий ключ в одной партиции | Parallel consumer или share groups, больше партиций, протокол KIP-848 | 4 |
| Медленные первые запросы после деплоя | Прогрев JIT | Холодные пути кода | Readiness после прогрева, AOT cache с профилями (JEP 515), плавный набор трафика | 7 |
| CPU БД высокий, приложение в порядке | `pg_stat_statements`, лог медленных запросов | Нет индексов, `SELECT *` от ORM, болтливые обновления | Индексы, проекции, пакетная запись, кэш справочников | 3 |

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Gatling docs](https://gatling.io/docs/) | Gatling | Нагрузочное тестирование (Java DSL) |
| [k6 docs](https://grafana.com/docs/k6/latest/) | Grafana | Нагрузочное тестирование k6 |
| [async-profiler](https://github.com/async-profiler/async-profiler) | Andrei Pangin | Профилировщик с малыми накладными расходами (режим wall-clock для ожиданий ввода-вывода) |
| [pg_stat_statements](https://www.postgresql.org/docs/current/pgstatstatements.html) | PostgreSQL | Поиск дорогих запросов |
| [Actuator endpoints](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html) | Spring team | Эндпоинты `startup`, `metrics`, `threaddump` |

## 10. Эталонные кодовые базы

Книги (список — в [books.md](books.md)): «High-Performance Java Persistence» (Mihalcea), «Cloud Native Spring in Action» (Vitale, Manning 2022), «Spring Boot: Up and Running» (Heckler), «Microservices Patterns» (Richardson), «Designing Data-Intensive Applications» (Kleppmann; 2-е изд. 2025-26), «Release It!» (Nygard, 2-е изд.).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [spring-petclinic](https://github.com/spring-projects/spring-petclinic) | Spring team | Каноническое приложение Spring Boot, держится на последней версии Boot |
| [spring-petclinic-microservices](https://github.com/spring-petclinic/spring-petclinic-microservices) | Community | Вариант на микросервисах (Spring Cloud, наблюдаемость) |
| [spring-petclinic organization](https://github.com/spring-petclinic) | Community | Другие варианты PetClinic (REST, Angular, Kotlin, GraphQL) |
| [realworld](https://github.com/realworld-apps/realworld) | RealWorld community | Спецификация «клон Medium» со множеством бэкендов |
| [spring-boot-realworld-example-app](https://github.com/gothinkster/spring-boot-realworld-example-app) | gothinkster | Бэкенд RealWorld на Spring Boot (MyBatis, старый Boot) |
| [quarkus-quickstarts](https://github.com/quarkusio/quarkus-quickstarts) | Quarkus team | По быстрому старту на каждое расширение |
| [quarkus-super-heroes](https://github.com/quarkusio/quarkus-super-heroes) | Quarkus team | Многосервисная демонстрация Quarkus (REST, gRPC, Kafka, реактивность) |
| [micronaut-guides](https://github.com/micronaut-projects/micronaut-guides) | Micronaut Foundation | Исходники всех руководств Micronaut |
| [helidon-examples](https://github.com/helidon-io/helidon-examples) | Oracle | Примеры Helidon |
| [generator-jhipster](https://github.com/jhipster/generator-jhipster) | JHipster | Сгенерированные full-stack приложения; полезны умолчания конфигурации и безопасности |
| [spring-modulith](https://github.com/spring-projects/spring-modulith) | Spring team | Modulith и примеры реестра публикации событий |
| [cloud-native-spring-in-action](https://github.com/ThomasVitale/cloud-native-spring-in-action) | Thomas Vitale | Код книги: Polar Bookshop (K8s, наблюдаемость, безопасность) |
| [ftgo-application](https://github.com/microservices-patterns/ftgo-application) | Chris Richardson | Пример саг, outbox, CQRS |
| [samples-java](https://github.com/temporalio/samples-java) | Temporal | Паттерны workflow на Java |
| [Boot smoke-test](https://github.com/spring-projects/spring-boot/tree/main/smoke-test) | Spring team | Минимальные приложения Boot по возможностям (smoke-тесты) |
| [spring-guides](https://github.com/spring-guides) | Spring team | Руководства «для начала» |
| [high-performance-java-persistence](https://github.com/vladmihalcea/high-performance-java-persistence) | Vlad Mihalcea | Тесты для каждого утверждения о производительности JPA и JDBC |
| [eugenp/tutorials](https://github.com/eugenp/tutorials) | Baeldung | Огромный корпус примеров (проверяйте версии перед копированием) |

## Чеклист ревью серверного кода

Платформа и сборка

- [ ] Фреймворк на поддерживаемой линии (Boot 4.1 или 4.2, Quarkus 3.33 или 3.40 LTS, Micronaut 5, Helidon 4.5 LTS); JDK 25 LTS.
- [ ] Версии берутся из BOM фреймворка; вручную закреплённых транзитивных версий нет, а исключения снабжены комментарием.
- [ ] Процессоры аннотаций (Lombok, MapStruct, `hibernate-processor`) объявлены на пути процессоров (правило JDK 23+); сборка не падает с `cannot find symbol Book_`.
- [ ] Boot 4: используются технологические стартеры (`-webmvc`, `-flyway` и т. д.); после миграции нет `starter-classic` и `test-classic`.
- [ ] Нет импортов Jackson 2 (`com.fasterxml.jackson.databind`) в коде Boot 4, кроме случаев намеренного моста jackson2; блоки `catch` обновлены под unchecked `JacksonException`.
- [ ] Нет удалённых API Boot 4, Framework 7 и Security 7 (`@MockBean`, `javax.*`, `RestTemplate` в новом коде, `.and()` и `authorizeRequests`).

Слой API

- [ ] Контроллеры возвращают DTO или records, не сущности; тела запросов валидируются `@Valid`; параметры пути и запроса ограничены.
- [ ] Ошибки в формате RFC 9457 ProblemDetail; в ответах нет стек-трейсов, SQL и внутренних имён классов.
- [ ] Авторизация на уровне объекта для каждого id ресурса (нет IDOR); method security или явная проверка владения.
- [ ] Пагинация ограничена (максимальный размер страницы); keyset для больших таблиц; нет неограниченного `findAll()`.
- [ ] Идемпотентность для повторяемых POST (Idempotency-Key или естественные ключи); оптимистичная блокировка через `@Version` или ETag.
- [ ] OpenAPI генерируется или «контракт первым»; Swagger UI в продакшене отключён или защищён.

Исходящие вызовы и устойчивость

- [ ] У каждого вызова HTTP, gRPC, БД, Redis есть таймауты соединения и чтения или дедлайны.
- [ ] Повторы только для идемпотентных операций, с backoff и jitter и небольшим пределом; circuit breaker и bulkhead на критичных зависимостях.
- [ ] Клиенты строятся из внедрённого билдера (трассировка и метрики) и переиспользуются; нет клиента на каждый запрос.
- [ ] Исходящие URL из пользовательского ввода проверяются (SSRF; `InetAddressFilter` в Boot 4.1).

Данные

- [ ] Все `@ManyToOne` и `@OneToOne` помечены LAZY; планы загрузки под сценарии; N+1 проверяется в тестах (счёт SQL).
- [ ] `spring.jpa.open-in-view=false`.
- [ ] Пакетная запись настроена; ключи `SEQUENCE` (не IDENTITY), где важны массовые вставки.
- [ ] `@Transactional` на публичных методах сервисов, без самовызова, без удалённого ввода-вывода внутри, с верным `rollbackFor`.
- [ ] `readOnly = true` на путях чтения; DTO-проекции для моделей чтения.
- [ ] Пул Hikari рассчитан от ёмкости БД и числа реплик; `connectionTimeout` короткий; метрики пула экспортируются; при виртуальных потоках есть bulkhead на входе.
- [ ] Миграции версионированы (Flyway или Liquibase), обратно совместимы (expand/contract), применённые скрипты не правятся.
- [ ] Кэши ограничены (размер и TTL), стратегия инвалидации описана.

Сообщения

- [ ] Потребители идемпотентны; DLQ или retry-топик с ограниченным числом повторов; «ядовитое» сообщение не блокирует партицию навсегда.
- [ ] Побочные эффекты в БД и брокере идут через outbox или CDC, а не двойной записью.

Безопасность

- [ ] Конфигурация Security на лямбда-DSL; CSRF отключён только для stateless API с токеном; источники CORS заданы явно.
- [ ] Проверка JWT включает издателя, аудиторию и срок действия; scopes сопоставлены с authorities.
- [ ] Пароли хешируются делегирующим кодировщиком (bcrypt или Argon2id).
- [ ] Нет секретов в исходниках, образах и логах; секреты из окружения, файлов или Vault; `.env` и ключи в `.gitignore`.
- [ ] Actuator: открыты только `health`, `info`, `prometheus`; порт управления отдельный; `heapdump` и `env` не публичны.
- [ ] Сканирование зависимостей в CI (см. [toolchain.md](toolchain.md)); нет Java-десериализации недоверенного ввода; XML-парсеры защищены от XXE.

Наблюдаемость и эксплуатация

- [ ] Структурные JSON-логи с идентификатором трассы; без персональных данных; уровни логов меняются в рантайме.
- [ ] RED-метрики (rate, errors, duration) на эндпоинт и на зависимость; нет тегов с высокой кардинальностью.
- [ ] Трассы проходят через HTTP и сообщения (W3C `traceparent`); контекст передаётся через executor'ы.
- [ ] Liveness не проверяет зависимости; readiness проверяет; `startupProbe` для медленного старта.
- [ ] Плавная остановка и задержка `preStop`; потребители корректно останавливаются и коммитят.
- [ ] Контейнер: не root, основа JRE, куча через `MaxRAMPercentage`, явный GC, >= 2 CPU, `ExitOnOutOfMemoryError`.
- [ ] Оптимизация старта выбрана осознанно (AOT cache или CDS, native, CRaC или SnapStart) и измерена.
- [ ] Плановые задания безопасны при нескольких репликах (ShedLock, кластер Quartz или CronJob).
- [ ] Интеграционные тесты идут против настоящих зависимостей (Testcontainers; см. [testing.md](testing.md)).

## Антипаттерны

| Антипаттерн | Чем плох | Что вместо |
|---|---|---|
| Процессоры аннотаций только на class path при JDK 23+ | `javac` их не запускает: нет геттеров Lombok, нет `Book_`, нет ошибки процессора | `annotationProcessorPaths` (Maven), `annotationProcessor` (Gradle) или `-proc:full` |
| Flyway или Liquibase без стартера в Boot 4 | автоконфигурация не срабатывает, миграции молча не запускаются | `spring-boot-starter-flyway`, `spring-boot-starter-liquibase` |
| `spring-boot-starter-classic` оставлен после миграции | возвращает старый общий class path, скрывает зависимости | технологические стартеры (`-webmvc` и др.) |
| `ObjectMapper` из Jackson 2 и `catch (IOException e)` вокруг Jackson 3 | исключения `JacksonException` unchecked: блок не срабатывает | бин `JsonMapper`, пакеты `tools.jackson.*`, перехват `JacksonException` |
| `spring.jpa.open-in-view=true` (умолчание) | соединение держится весь запрос, N+1 прячется в сериализаторе | `spring.jpa.open-in-view=false`, загрузка внутри транзакции сервиса |
| Виртуальные потоки без ограничения на входе при малом пуле Hikari | тысячи потоков встают на `getConnection()`, таймауты | размер пула от БД, короткий `connectionTimeout`, bulkhead или `@ConcurrencyLimit` |
| Сущности JPA в ответе контроллера | ленивые исключения, утечка полей, mass assignment | DTO-records и проекции |
| `@ManyToOne` и `@OneToOne` с EAGER по умолчанию | скрытое N+1 | `fetch = LAZY` и `JOIN FETCH`, `@EntityGraph`, DTO |
| Lombok `@Data` на сущностях | ленивая загрузка в `toString`, сломанные хеш-множества | `equals` и `hashCode` по бизнес-ключу или id |
| `GenerationType.IDENTITY` при массовой вставке | отключает пакетные вставки | `SEQUENCE` с pooled-оптимизатором |
| HTTP-вызов внутри `@Transactional` | соединение занято на время удалённого вызова, голодание пула | вызов вне транзакции; `AFTER_COMMIT` или outbox для побочных эффектов |
| Самовызов `@Transactional`, `@Cacheable`, `@Retryable` | прокси обойдён, аннотация не работает | вынести в другой бин или `TransactionTemplate` |
| `REQUIRES_NEW` в горячем пути | второе соединение, взаимная блокировка пула | редко, с учётом размера пула |
| Двойная запись «БД + брокер» | рассинхронизация при сбое между записями | outbox с Debezium CDC или опрашивающим publisher |
| Потребитель без идемпотентности и DLQ | дубли при повторной доставке, «ядовитое» сообщение блокирует партицию | дедупликация, `DefaultErrorHandler`, `DeadLetterPublishingRecoverer`, `@RetryableTopic` |
| Вызов без таймаута | потоки зависают навсегда | таймауты соединения и ответа, дедлайны gRPC |
| Повторы на нескольких слоях без backoff | 3 слоя по 3 повтора — 27 вызовов, усиление сбоя | один слой повторов, backoff с jitter, circuit breaker |
| Новый HTTP-клиент на каждый запрос или `RestClient.create()` | нет пула, нет трассировки | клиент из внедрённого билдера, один экземпляр |
| `RestTemplate` в новом коде | `[устарело]`, удаление в 8.0 | `RestClient` или `@HttpExchange` |
| `.and()`, `authorizeRequests`, `AntPathRequestMatcher` | удалены в Security 7 | лямбда-DSL, `authorizeHttpRequests`, `PathPatternRequestMatcher` |
| CSRF отключён для cookie-сессий | уязвимость CSRF | отключать только для stateless API с bearer-токеном; `csrf.spa()` для SPA |
| Проверка роли без проверки владельца ресурса | IDOR | проверка принадлежности ресурса по id |
| Адаптеры Keycloak `[устарело]` | не поддерживаются | стандартный Spring OAuth2 client и resource server |
| SHA-256 или MD5 для паролей | быстрые хеши легко подбирать | делегирующий кодировщик (bcrypt) или Argon2id |
| Redis-замок как гарантия корректности | не fencing-безопасен | транзакции БД, fencing-токены |
| Кэш без границы размера и TTL | утечка памяти, OOM | Caffeine с `maximumSize` и TTL |
| Теги метрик с id пользователя, URL, текстом ошибки | взрыв кардинальности | ограниченные наборы значений тегов |
| Агент OTel и SDK в приложении одновременно | дубли спанов | либо агент, либо стартер OTel |
| Liveness-проба с проверкой БД | сбой БД перезапускает все поды | liveness без зависимостей, readiness с ними |
| Actuator наружу с `env`, `heapdump` | утечка секретов | `health,info,prometheus` на отдельном порту |
| Куча по `-Xmx` без учёта лимита контейнера, JDK-образ, root | OOMKilled, лишний размер, риск | `MaxRAMPercentage` 70-75, JRE distroless, не root, `ExitOnOutOfMemoryError` |
| `@Scheduled` без блокировки на нескольких репликах | задание выполняется N раз | ShedLock, кластер Quartz или CronJob |
| Режим `layertools` в Dockerfile | удалён в Boot 4.1 | `-Djarmode=tools extract --layers --launcher` |
| Spark Java в новом коде | `[устарело]`, проект неактивен | Javalin 7 |
| Недетерминизм внутри workflow Temporal | ломает воспроизведение | `Workflow.sleep`, activities для ввода-вывода, API версионирования |

## Спорные места

- **Версии, дата которых ещё не наступила.** Дата GA Spring Boot 4.2 (RC2 вышел 2026-11-05, GA ожидается в ноябре 2026) и точный список возможностей 4.2 не перечитывались. Дата GA Jakarta EE 12: последние надёжные сведения — «конец 2026» из источника начала 2026 года `[не проверено]`.
- **Префиксы свойств HTTP-клиентов.** `spring.http.serviceclient.<group>.*` и `spring.http.clients.*` записаны по памяти документации Boot 4.0 и не перечитывались `[не проверено]`.
- **Graceful shutdown по умолчанию с Boot 3.4.** Записано по памяти, не перечитывалось `[не проверено]`.
- **Лицензия Camunda 8 для self-managed в продакшене.** Изменения лицензирования записаны по памяти `[не проверено]`.
- **Kafka 4.3 и 4.4.** Главные возможности не читались; изучены только сводки 4.0 и 4.2.
- **Флаги Paketo для обучения AOT cache.** Не проверялись.
- **Hibernate 7.4: limit с fetch join.** Перенос limit в SQL работает на поддерживаемых БД; ради переносимости многие по-прежнему советуют два запроса (страница id, затем выборка), и это остаётся безопасным выбором.
- **Spring Boot или Quarkus для нового сервиса.** Spring выигрывает размером экосистемы и наймом; Quarkus выигрывает стартом, RSS и Dev Services. Решение зависит от среды выполнения (Kubernetes с частым масштабированием или serverless против долгоживущих сервисов) и от команды.
- **JPA или SQL-first (jOOQ, JdbcClient, MyBatis).** JPA ускоряет CRUD, но требует дисциплины (LAZY, проекции, отключённый OSIV, счёт SQL в тестах). SQL-first даёт прозрачность за счёт ручного маппинга. Один подход на сервис и обязательное обнаружение N+1.
- **R2DBC и WebFlux против блокирующего JDBC на виртуальных потоках.** Для нового кода блокирующий JDBC на виртуальных потоках обычно проще и масштабируется так же; реактивный стек оправдан, когда он уже есть сквозным.
- **Liquibase после смены лицензии на FSL.** Версия 5 доступна по лицензии с исходными кодами, которая через 2 года становится Apache-2.0, а OSS-дистрибутив идёт без расширений и драйверов. Для проектов с жёсткими требованиями к лицензиям сначала проверьте пригодность, либо выберите Flyway.
- **Версии LTS Quarkus и Helidon.** Номера Helidon следуют за JDK («Tip & Tail»), а Quarkus выпускает LTS каждые 6 месяцев с 12 месяцами поддержки сообщества. Для долгоживущего продакшена берите LTS-линии; Helidon 27 и 28 имеют смысл, только если JDK обновляется каждые 6 месяцев.
