# Книги, статьи, доклады и маршрут чтения по Java

Каталог источников для тех, кто пишет, ревьюит и оптимизирует Java-код в любой области: книги по категориям, официальные сайты, блоги, рассылки, отчёты об экосистеме, доклады и каналы, подкасты, подборки, курсы, русскоязычные ресурсы и маршрут чтения по уровням. Актуально на ноябрь 2026: последний JDK — 27 (GA в сентябре 2026), последняя LTS — 25 (сентябрь 2025). Книга «про Java 21» отстаёт на одну LTS, книга про Java 17 — на две. Из языковых возможностей после Java 21 книги почти не описывают: flexible constructor bodies (JEP 513), module import declarations, compact source files и instance `main`, scoped values (финальные в 25), stable/lazy constants (preview), structured concurrency (всё ещё preview), preview-версии value-классов из Valhalla. Тематические ссылки (инструменты, тесты, производительность, идиомы, архитектура, параллелизм, серверы, клиенты) лежат в соседних файлах: [toolchain.md](toolchain.md), [testing.md](testing.md), [performance.md](performance.md), [idioms-patterns.md](idioms-patterns.md), [architecture.md](architecture.md), [concurrency-native.md](concurrency-native.md), [domains-server.md](domains-server.md), [domains-client.md](domains-client.md), [pipeline.md](pipeline.md). Этот файл — общий список чтения.

Метки: `[устарело]` — издание или содержание устарело, рядом названа замена; `[не проверено]` — факт подтверждён только поисковой выдачей или листингом продавца, либо издатель блокирует скрипты. Ссылки на O'Reilly и Packt возвращают HTTP 403 скриптам, поэтому все они помечены `[не проверено]`. Их адреса взяты из поисковой выдачи, не угаданы. Строки без метки проверены (страница открыта или ответила HTTP 200 при проверке 2026-11-06).

- [Короткий список: 20 главных источников](#короткий-список-20-главных-источников)
- [Устаревшие книги и чем их заменить](#устаревшие-книги-и-чем-их-заменить)
- [Что вышло, что в раннем доступе и чего не существует](#что-вышло-что-в-раннем-доступе-и-чего-не-существует)
- [Основы и современный Java](#основы-и-современный-java)
- [Идиоматичный и сопровождаемый код](#идиоматичный-и-сопровождаемый-код)
- [Производительность, настройка JVM и бенчмарки](#производительность-настройка-jvm-и-бенчмарки)
- [Параллелизм, виртуальные потоки и модель памяти](#параллелизм-виртуальные-потоки-и-модель-памяти)
- [Устройство JVM и спецификации](#устройство-jvm-и-спецификации)
- [Архитектура, дизайн и фреймворки](#архитектура-дизайн-и-фреймворки)
- [Тестирование](#тестирование)
- [Функциональный стиль и data-oriented programming](#функциональный-стиль-и-data-oriented-programming)
- [Безопасность и алгоритмы](#безопасность-и-алгоритмы)
- [Официальные источники и OpenJDK](#официальные-источники-и-openjdk)
- [Независимые блоги и серии статей](#независимые-блоги-и-серии-статей)
- [Отчёты об экосистеме и радары](#отчёты-об-экосистеме-и-радары)
- [Доклады и видеоканалы](#доклады-и-видеоканалы)
- [Подкасты и рассылки](#подкасты-и-рассылки)
- [Подборки и курсы](#подборки-и-курсы)
- [Русскоязычные ресурсы](#русскоязычные-ресурсы)
- [Маршрут чтения по уровням](#маршрут-чтения-по-уровням)
- [Пробелы и непроверенное](#пробелы-и-непроверенное)

## Короткий список: 20 главных источников

**Ключевые выводы**

- На вопрос «что читать, чтобы писать более быстрый, лучше устроенный Java-код» отвечайте этим списком в этом порядке. Подробности по каждому пункту — в разделе, указанном в последнем столбце.
- Для языка после Java 21 нет ни одной полной печатной книги. Закрывайте пробел по JEP 22–27 и inside.java.
- Книги из списка с датой до 2021 года читайте в паре с современным источником (см. [Устаревшие книги и чем их заменить](#устаревшие-книги-и-чем-их-заменить)).

| № | Книга или ресурс | Автор, год, издание | Зачем читать | Раздел |
|---|---|---|---|---|
| 1 | *Effective Java* | Bloch, 2018, 3-е изд. | Остаётся эталоном идиоматичных API и корректности | [Идиоматичный код](#идиоматичный-и-сопровождаемый-код) |
| 2 | *100 Java Mistakes and How to Avoid Them* | Valeev, 2024 | Современные ловушки со ссылками на статический анализ | [Идиоматичный код](#идиоматичный-и-сопровождаемый-код) |
| 3 | «Data-Oriented Programming in Java» и DOP v1.1 | Goetz, 2022; Parlog, 2024 | Современный стиль проектирования: records, sealed-типы, паттерны | [DOP](#функциональный-стиль-и-data-oriented-programming) |
| 4 | *A Philosophy of Software Design* | Ousterhout, 2021, 2-е изд. | Дизайн модулей и интерфейсов; противовес Clean Code | [Идиоматичный код](#идиоматичный-и-сопровождаемый-код) |
| 5 | *Java Concurrency in Practice* (2006) и *Modern Concurrency in Java* (2025) | Goetz и др.; Rahman | Основы потокобезопасности плюс виртуальные потоки и structured concurrency | [Параллелизм](#параллелизм-виртуальные-потоки-и-модель-памяти) |
| 6 | *Java Performance* | Oaks, 2020, 2-е изд. | Методология и набор инструментов производительности | [Производительность](#производительность-настройка-jvm-и-бенчмарки) |
| 7 | *JVM Performance Engineering* | Beckwith, 2024 | Актуальное устройство HotSpot и GC | [Производительность](#производительность-настройка-jvm-и-бенчмарки) |
| 8 | *Optimizing Cloud Native Java* | Evans, Gough, 2024 | Производительность в контейнерах и облаке | [Производительность](#производительность-настройка-jvm-и-бенчмарки) |
| 9 | Шипилёв: доклады о JMH, JVM Anatomy Quarks, статьи о JMM | Shipilev | Как измерять и почему микрооптимизации ведут себя именно так | [Производительность](#производительность-настройка-jvm-и-бенчмарки), [Доклады](#доклады-и-видеоканалы) |
| 10 | *High-Performance Java Persistence* и блог автора | Mihalcea | Доступ к данным — место, где живёт большая часть задержек | [Производительность](#производительность-настройка-jvm-и-бенчмарки), [Блоги](#независимые-блоги-и-серии-статей) |
| 11 | *Get Your Hands Dirty on Clean Architecture* | Hombergs, 2023, 2-е изд. | Гексагональная архитектура в идиоматичном Java | [Архитектура](#архитектура-дизайн-и-фреймворки) |
| 12 | *Learning Domain-Driven Design* | Khononov, 2021 | Практичный современный DDD | [Архитектура](#архитектура-дизайн-и-фреймворки) |
| 13 | *Fundamentals of Software Architecture* | Richards, Ford, 2025, 2-е изд. | Архитектурные стили и компромиссы | [Архитектура](#архитектура-дизайн-и-фреймворки) |
| 14 | *Designing Data-Intensive Applications* | Kleppmann, Riccomini, 2026, 2-е изд. | Архитектура систем данных | [Архитектура](#архитектура-дизайн-и-фреймворки) |
| 15 | *Unit Testing Principles, Practices, and Patterns* | Khorikov, 2020 | Что делает тесты ценными | [Тестирование](#тестирование) |
| 16 | *Effective Software Testing* | Aniche, 2022 | Систематический дизайн тестов на Java | [Тестирование](#тестирование) |
| 17 | *Refactoring* (Fowler, 2018, 2-е изд.) и *Working Effectively with Legacy Code* (Feathers, 2004) | Fowler; Feathers | Безопасное изменение существующего кода | [Идиоматичный код](#идиоматичный-и-сопровождаемый-код) |
| 18 | *Core Java* 13-е изд. или *Core Java for the Impatient* 4-е изд. | Horstmann, 2024–2025 | Актуальный (Java 21) справочник по языку и API | [Основы](#основы-и-современный-java) |
| 19 | JEP для JDK 22–27 и Java Almanac | OpenJDK; Hoffmann | Единственный полный источник по возможностям после Java 21 | [Официальные источники](#официальные-источники-и-openjdk) |
| 20 | *Secure by Design* (2019) и OWASP Cheat Sheets | Johnsson и др.; OWASP | Безопасность, заложенная в дизайн | [Безопасность](#безопасность-и-алгоритмы) |

## Устаревшие книги и чем их заменить

**Ключевые выводы**

- Устаревшая книга не обязательно плоха. *Java Concurrency in Practice* и *Effective Java* остаются обязательными, но их нужно читать вместе с современным дополнением.
- Если в таблице стоит «оставить», книгу не заменяют, а дополняют.

| Устаревшая книга | Почему устарела | Чем заменить или дополнить |
|---|---|---|
| *Java Performance: The Definitive Guide* (Oaks, 2014) | Java 7/8, GC эпохи CMS | *Java Performance* 2-е изд. (2020) плюс Beckwith (2024) |
| *Optimizing Java* (Evans, Gough, Newland, 2018) | До контейнеров, до Loom | *Optimizing Cloud Native Java* (2024) |
| *Java Performance Companion* (2016) | G1 времён JDK 8 | GC Tuning Guide SE 25 плюс *The Garbage Collection Handbook* 2-е изд. |
| *Clean Code* (2008) | Спорные советы; код эпохи Java 5 | *A Philosophy of Software Design* 2-е изд.; *Clean Code* 2-е изд. (2025) читать критически |
| *Spring in Action* 6-е изд. (2022) | Spring 5.3 / Boot 2.4 | *Spring Start Here* 2-е изд. (MEAP), *Pro Spring 6*, справочная документация Spring |
| *Core Java for the Impatient* 3-е изд. (2022) | Java 17 | 4-е изд. (2025, Java 21) |
| *Fundamentals of Software Architecture* 1-е изд. (2020) | Нет пяти новых глав | 2-е изд. (2025) |
| *Designing Data-Intensive Applications* 1-е изд. (2017) | Ландшафт данных до 2020 года | 2-е изд. (2026) |
| *Functional Programming in Java* (Saumont, 2017) | Java 8, строит собственную FP-библиотеку | Subramaniam 2-е изд. (2023), Weidig (2023) |
| *Inside the Java Virtual Machine* (Venners, 1999) | GC и JIT до эпохи HotSpot | Beckwith (2024), JVMS SE 25/27, JVM Anatomy Quarks |
| *Iron-Clad Java* (2014) | До Spring Security 5/6, до современных криптографических API | OWASP Cheat Sheets, *Spring Security in Action* 2-е изд., *Secure by Design* |
| *Java Concurrency in Practice* (2006) | Нет fork/join, `CompletableFuture`, виртуальных потоков | Оставить; добавить *Modern Concurrency in Java* (2025) и JEP 444/491/505/506 |
| *Modern Java in Action* (2018) | Заканчивается на Java 10 | Оставить для streams; добавить JEP и *Java Coding Problems* 2-е изд. (Java 21) |
| *Effective Java* 3-е изд. (2018) | До records и pattern matching | Оставить; добавить статью о DOP и *100 Java Mistakes* |
| Snyk JVM Ecosystem Report (2021) | Выпуск прекращён | JetBrains State of Java 2025, Azul 2026 |

## Что вышло, что в раннем доступе и чего не существует

**Ключевые выводы**

- **У трёх классиков нет новых изданий.** *Effective Java* — по-прежнему 3-е издание (2018), *Java Concurrency in Practice* — 2006, *Modern Java in Action* — 2018 (Java 9/10). Поиск не нашёл 4-го, 2-го и 3-го изданий `[не проверено]`. Российский издатель JCiP (Питер) написал в комментарии на Habr, что Goetz не планирует обновлять JCiP `[не проверено]` ([Habr, Piter](https://habr.com/en/companies/piter/articles/489038/comments)).
- **Книги по основам описывают Java 21.** Это *Core Java* 13-е изд. (Vol I 2024, Vol II 2024/2025), *Core Java for the Impatient* 4-е изд. (февраль 2025; ebook октябрь 2024), *Java: The Complete Reference* 13-е изд. (январь 2024) и *Learning Java* 6-е изд. (август 2023). Ни одна не покрывает Java 25. Книги про Java 25 — это небольшие или самоизданные работы: *Java 25 and Beyond* (Inden, Apress, 2026) `[не проверено]`, а также *From Java 21 to Java 25* и *Exploring Java 25* (Fu Cheng, Leanpub).
- **Заметные релизы 2024–2026:**
  - *100 Java Mistakes and How to Avoid Them* (Valeev, Manning, апрель 2024)
  - *JVM Performance Engineering* (Beckwith, Addison-Wesley, 2024)
  - *Optimizing Cloud Native Java* (Evans, Gough, O'Reilly, 2024) — 2-е издание *Optimizing Java*
  - *Modern Concurrency in Java* (A N M Bazlur Rahman, O'Reilly, сентябрь–октябрь 2025): виртуальные потоки, structured concurrency, scoped values
  - *Troubleshooting Java* 2-е изд. (Spilcă, Manning, октябрь/ноябрь 2025)
  - *Clean Code* 2-е изд. (Martin, Addison-Wesley, сентябрь/октябрь 2025)
  - *Fundamentals of Software Architecture* 2-е изд. (Richards, Ford, O'Reilly, март 2025)
  - *Designing Data-Intensive Applications* 2-е изд. (Kleppmann, Riccomini, O'Reilly, начало 2026)
  - *Quarkus in Action* (Štefanko, Martiška, Manning, февраль 2025)
  - *Pragmatic Unit Testing in Java with JUnit* 3-е изд. (Langr, Pragmatic, ноябрь/декабрь 2024, Java 21)
  - *Spring Security in Action* 2-е изд. (Spilcă, Manning, 2024)
  - *Java Coding Problems* 2-е изд. (Leonard, Packt, март 2024, Java 21)
  - *Mastering the Java Virtual Machine* (Santana, Packt, февраль 2024)
  - *OCP Java SE 21 Developer Study Guide* (Boyarsky, Selikoff, Sybex, ноябрь 2024)

**Книги в раннем доступе (MEAP): на 2026-11-06 не вышли.** Не называйте их изданными и не цитируйте номера страниц и содержание как окончательные.

| Книга | Автор, издатель | Статус на 2026-11-06 |
|---|---|---|
| *Data-Oriented Programming in Java* | Chris Kiehl, Manning | MEAP с сентября 2024, страница обновлена в сентябре 2026, ожидаемый выход — ноябрь 2026 (Manning). Розничные продавцы указывают 7 октября 2026 `[не проверено]`. Перед цитированием проверьте, вышла ли книга |
| *Spring Start Here* 2-е изд. | Laurențiu Spilcă, Manning | MEAP с июня 2026, Spring Framework 7 / Boot 4, выход ожидается в начале 2027 `[не проверено]` |
| *Testing Spring Boot Applications* | Daniel Garnier-Moiroux, Manning | MEAP с марта 2026, выход ожидается в начале 2027 `[не проверено]` |
| *Microservices Patterns* 2-е изд. | Chris Richardson, Manning | MEAP с июня 2025, выход ожидается весной 2027 `[не проверено]` |

**Чего не существует. Не выдумывайте и не цитируйте:**

- *Effective Java* 4-е издание.
- *Java Concurrency in Practice* 2-е издание.
- *Modern Java in Action* 3-е издание.
- *Spring in Action* 7-е издание (угаданный URL Manning отдаёт 404; существует только 6-е изд. 2022 года, оно устарело).
- 2-е издание *Cloud Native Spring in Action* (2022) и *Domain Modeling Made Functional* (2018).
- InfoQ Java Trends Report 2026 (последний — 2025).
- Отчёт New Relic State of the Java Ecosystem 2025 (последний — 2024).
- «OCP Java SE 25 Developer Study Guide» (Selikoff): появляется только в новинках Amazon, не проверено.
- Русский перевод *Spring Start Here* не найден.

**Отчёты об экосистеме и события**

- InfoQ **Java Trends Report 2025** — последний. Издания 2026 года к 2026-11-06 нет.
- JetBrains **State of Java 2025** (по опросу Developer Ecosystem 2025, 24 534 разработчика).
- Azul **2026 State of Java** (10 февраля 2026, более 2000 респондентов; 62% используют Java для AI-функций; 81% переносят часть Oracle Java на OpenJDK).
- New Relic **2024 State of the Java Ecosystem** (30 апреля 2024); издания 2025 года нет.
- Отчёт JVM от Snyk прекратился после 2021 года `[устарело]`.
- JavaOne 2026 прошёл 17–19 марта 2026 в Redwood Shores. Видео выкладывают на youtube.com/java. JavaOne 2027 анонсирован на 23–25 марта 2027 `[не проверено]`.
- JVMLS 2025 прошёл 4–6 августа 2025 `[не проверено]`.
- Joker 2026 прошёл 14–15 октября 2026 в Санкт-Петербурге.

## Основы и современный Java

**Ключевые выводы**

- Для **справочной полки** берите *Core Java* 13-е изд. (два тома, Java 21) или *Java: The Complete Reference* 13-е изд. (Java 21, 1280 стр.).
- Для **быстрого маршрута** опытному программисту подходит *Core Java for the Impatient* 4-е изд. (Java 21).
- Для **полных новичков** — *Head First Java* 3-е изд. (Java 17) или *Learning Java* 6-е изд. (Java 21, есть виртуальные потоки).
- «Вторая книга» по платформе (модули, байт-код, производительность, параллелизм, сборка, контейнеры, другие языки JVM) — *The Well-Grounded Java Developer* 2-е изд. (2022).
- После любой из них читайте официальные JEP 22–27 (см. [Официальные источники и OpenJDK](#официальные-источники-и-openjdk)). Ни одна печатная книга не описывает языковые возможности Java 25 хорошо.

| Книга | Автор, год, издание | Версия Java | Зачем читать |
|---|---|---|---|
| *Core Java, Volume I: Fundamentals* ([Pearson](https://pearson.com/en-au/subject-catalog/p/core-java-volume-i-fundamentals/P200000012511), [O'Reilly](https://www.oreilly.com/library/view/core-java-volume/9780135328385), [сайт автора](https://horstmann.com/corejava/index.html)) | Cay Horstmann, 2024, 13-е изд. (листинги: июль 2024 – март 2025) | Java 21 | Авторитетные основы «fully updated through Java 21»: ООП, интерфейсы, generics, коллекции, лямбды, параллелизм, модули. O'Reilly и Pearson `[не проверено]`, сайт автора проверен |
| *Core Java, Volume II: Advanced Features* ([InformIT](https://www.informit.com/store/core-java-volume-ii-advanced-features-9780135371749), [сайт автора](https://horstmann.com/corejava/index.html)) | Horstmann, 2024/2025, 13-е изд. | Java 21 | Streams, I/O, сеть, БД, date/time, безопасность, i18n, API компилятора и скриптов, native-методы `[не проверено]` |
| *Core Java for the Impatient* ([страница автора](https://horstmann.com/javaimpatient/index.html), [O'Reilly](https://www.oreilly.com/library/view/core-java-for/9780135404546/)) | Horstmann, февраль 2025 (ebook октябрь 2024), 4-е изд. | Java 21 | Плотный быстрый тур по современному Java для тех, кто уже программирует. Страница автора проверена, O'Reilly `[не проверено]` |
| *Core Java for the Impatient* | Horstmann, 2022, 3-е изд. | Java 17 | `[устарело]`: заменено 4-м изданием |
| *Java: The Complete Reference* ([McGraw Hill](https://www.mhprofessional.com/java-the-complete-reference-thirteenth-edition-9781265058432-usa), [O'Reilly](https://oreilly.com/library/view/java-the-complete/9781265062705)) | Herbert Schildt, Danny Coward, январь 2024, 13-е изд. | JDK 18–21 | Энциклопедический справочник: pattern matching для switch, record patterns, sequenced collections, виртуальные потоки. McGraw Hill проверен, O'Reilly `[не проверено]` |
| *Head First Java* ([O'Reilly](https://www.oreilly.com/library/view/head-first-java/9781492091646/)) | Kathy Sierra, Bert Bates, Trisha Gee, май 2022 (2-й тираж май 2025), 3-е изд. | Java 8–17 | Лучшая первая книга для новичков: лямбды, streams, параллелизм `[не проверено]` |
| *Learning Java* ([O'Reilly](https://www.oreilly.com/library/view/-/9781098145521)) | Marc Loy, Patrick Niemeyer, Daniel Leuck, август 2023, 6-е изд. | JDK 15–21 | От новичка до среднего уровня; расширенные лямбды и streams, раздел о виртуальных потоках `[не проверено]` |
| *Java in a Nutshell* ([O'Reilly](https://www.oreilly.com/library/view/-/9781098130992/)) | Ben Evans, Jason Clark, David Flanagan, февраль 2023, 8-е изд. | Java 17 | Компактный справочник по языку и ядру платформы; отмечает возможности после Java 8 `[не проверено]` |
| *The Well-Grounded Java Developer* ([Manning](https://www.manning.com/books/the-well-grounded-java-developer-second-edition)) | Ben Evans, Jason Clark, Martijn Verburg, октябрь 2022, 2-е изд. | Java 17 (эпоха) | Книга «о платформе» для среднего уровня: модули, байт-код, загрузка классов, производительность, параллелизм, Maven/Gradle, контейнеры, Kotlin/Clojure |
| *Modern Java in Action* ([Manning](https://www.manning.com/books/modern-java-in-action)) | Raoul-Gabriel Urma, Mario Fusco, Alan Mycroft, 2018 | Java 9/10 | Самая глубокая книга о лямбдах, streams, `Optional`, `CompletableFuture`, реактивном Flow. Языковые части устарели, читайте вместе с JEP |
| *Java Coding Problems* ([Packt](https://www.packtpub.com/en-us/product/java-coding-problems-second-edition-9781837633944), [O'Reilly](https://www.oreilly.com/library/view/-/9781837633944)) | Anghel Leonard, март 2024, 2-е изд. | Java 21 | Более 250 задач: records, record patterns, sealed-типы, виртуальные потоки, современные GC, компромиссы между решениями `[не проверено]` |
| *OCP Oracle Certified Professional Java SE 21 Developer Study Guide* ([O'Reilly](https://www.oreilly.com/library/view/ocp-oracle-certified/9781394286614/)) | Jeanne Boyarsky, Scott Selikoff, Sybex, ноябрь 2024 | Java 21 | Самое точное описание правил языка и крайних случаев (экзамен 1Z0-830); для «языкового юриста» `[не проверено]`. Руководство по SE 25 есть на Amazon, но не проверено |
| *Java 25 and Beyond: Modern Java Made Easy* ([Springer](https://link.springer.com/book/10.1007/979-8-8688-2385-5)) | Michael Inden, Apress, 2026 | Java 25 LTS | Одна из первых печатных книг про возможности Java 25 LTS `[не проверено]` (страница Springer перенаправляет скриптам на логин) |
| *From Java 21 to Java 25* ([Leanpub](https://leanpub.com/java21to25)) и *Exploring Java 25* ([Leanpub](https://leanpub.com/java25)) | Fu Cheng, 2025–2026 | 21 → 25 | Короткие самоизданные руководства по разнице между 21 и 25. Проверяйте качество |
| *Java SE Specifications* (JLS / JVMS), [индекс](https://docs.oracle.com/javase/specs/) ([JLS 27](https://docs.oracle.com/javase/specs/jls/se27/html/index.html), [JLS 25](https://docs.oracle.com/javase/specs/jls/se25/html/index.html)) | Oracle, каждый релиз | 25, 27 | Нормативное определение языка; последнее слово по семантике |
| *Java SE 25 documentation* ([docs](https://docs.oracle.com/en/java/javase/25/)) | Oracle, 2025 | 25 | Документация API, руководства по инструментам, GC и безопасности для текущей LTS |
| Сайт и блог Horstmann ([horstmann.com](https://horstmann.com/)) | Cay Horstmann | — | Код к книгам и список ошибок; частые посты о новых возможностях Java |

## Идиоматичный и сопровождаемый код

**Ключевые выводы**

- *Effective Java* 3-е изд. (2018) остаётся самой важной книгой о том, как писать хороший Java. Её 90 пунктов об создании объектов, `equals`/`hashCode`, неизменяемости, generics, enum, лямбдах и streams, исключениях, параллелизме и сериализации верны. Книга старше records, sealed-типов и pattern matching, поэтому читайте её вместе со статьёй Goetz о DOP (см. [DOP](#функциональный-стиль-и-data-oriented-programming)) и руководствами Amber (см. [Официальные источники](#официальные-источники-и-openjdk)).
- *100 Java Mistakes and How to Avoid Them* (Valeev, 2024) — лучшее современное дополнение. К каждой ошибке даны код, причина, способ избежать и сведения, ловит ли её статический анализ. Valeev написал много инспекций IntelliJ.
- О *Clean Code*: издание 2008 года широко критикуют (эссе qntm; книга Ousterhout и его письменный спор с Martin в 2024–2025). Издание 2025 года — полная переработка. Как противовес рекомендуйте *A Philosophy of Software Design* Ousterhout (глубокие модули, сокрытие информации) и *Tidy First?* для небольших безопасных шагов.
- Для legacy-кода и рефакторинга: *Refactoring* Fowler 2-е изд. (примеры на JavaScript, каталог не зависит от языка) и *Working Effectively with Legacy Code* Feathers (2004; seams и characterization tests не устарели). Для массовых миграций используйте рецепты OpenRewrite ([docs](https://docs.openrewrite.org/)).

| Книга | Автор, год, издание | Версия Java | Зачем читать |
|---|---|---|---|
| *Effective Java* ([InformIT](https://www.informit.com/store/effective-java-9780134685991)) | Joshua Bloch, декабрь 2017 / 2018, 3-е изд. | Java 7–9 | Канонические идиомы и правила проектирования API; по-прежнему стандарт. 4-го издания не найдено |
| *100 Java Mistakes and How to Avoid Them* ([Manning](https://www.manning.com/books/100-java-mistakes-and-how-to-avoid-them)) | Tagir Valeev, апрель 2024, предисловие Horstmann | Java 21 | Ловушки в выражениях, числах, строках, `equals`/`compareTo`, коллекциях, библиотечных методах, тестах; врезки о статическом анализе |
| *Java by Comparison* ([Pragmatic](https://pragprog.com/titles/javacomp/java-by-comparison/), [сайт](https://java.by-comparison.com/)) | Simon Harrer, Jörg Lenhard, Linus Dietz, 2018 | Java 8 | 70 примеров «до и после» для джунов: имена, условия, исключения, тесты, streams. Эпоха Java 8, но полезна |
| *Clean Code* ([Pearson](https://www.pearson.com/en-us/subject-catalog/p/clean-code-a-handbook-of-agile-software-craftsmanship-2nd-edition/P200000013239), [O'Reilly](https://www.oreilly.com/library/view/-/9780135398586/)) | Robert C. Martin, сентябрь/октябрь 2025, 2-е изд. | разные языки | Полная переработка в четыре части (практика кодирования, эвристики дизайна, архитектура, этика). Читать критически. O'Reilly `[не проверено]` |
| *Clean Code* ([InformIT](https://www.informit.com/store/clean-code-a-handbook-of-agile-software-craftsmanship-9780132350884)) | Martin, 2008, 1-е изд. | Java 5 | Историческая; советы о крошечных функциях и «без комментариев» спорны. `[устарело]`: заменено 2-м изданием и APoSD |
| «It's probably time to stop recommending Clean Code» ([qntm](https://qntm.org/clean)) | qntm, 2020 | — | Известная критика с конкретными примерами кода из книги |
| *A Philosophy of Software Design* ([страница Stanford](https://web.stanford.edu/~ouster/cgi-bin/book.php)) | John Ousterhout, 2021, 2-е изд. | — | Глубокие и мелкие модули, сокрытие информации, «define errors out of existence»; лучшая короткая книга о дизайне |
| Спор «APoSD vs Clean Code» ([GitHub](https://github.com/johnousterhout/aposd-vs-clean-code)) | Ousterhout и Martin, сентябрь 2024 – февраль 2025 | — | Письменный спор о длине методов, комментариях и TDD; сообщает о замедлении в 3–4 раза в переписанном Martin примере с простыми числами |
| *Refactoring* ([martinfowler.com](https://martinfowler.com/books/refactoring.html), [InformIT](https://www.informit.com/store/refactoring-improving-the-design-of-existing-code-9780134757599), [каталог](https://refactoring.com/)) | Martin Fowler, 2018, 2-е изд. | примеры на JS | Каталог рефакторингов и словарь запахов кода, которым пользуются IDE и ревьюеры |
| *Working Effectively with Legacy Code* ([InformIT](https://www.informit.com/store/working-effectively-with-legacy-code-9780131177055)) | Michael Feathers, 2004 | Java/C++ | Seams, characterization tests, разрыв зависимостей, чтобы покрыть тестами код без тестов. Старая, но не заменена |
| *Tidy First?* ([O'Reilly](https://www.oreilly.com/library/view/tidy-first/9781098151232/)) | Kent Beck, октябрь 2023 | — | Небольшие «уборки» (guard clauses, поясняющие переменные, вынос helper) и экономика решения, когда рефакторить `[не проверено]` |
| *Five Lines of Code* ([Manning](https://www.manning.com/books/five-lines-of-code)) | Christian Clausen, 2021 | TypeScript | Упражнения по рефакторингу по правилам, ООП-уклон |
| *Good Code, Bad Code* ([Manning](https://www.manning.com/books/good-code-bad-code)) | Tom Long, 2021 | Java-подобный псевдокод | «Столпы» качества кода из практики Google: читаемость, отсутствие сюрпризов, тестируемость |
| *The Pragmatic Programmer* ([Pragmatic](https://pragprog.com/titles/tpp20/the-pragmatic-programmer-20th-anniversary-edition/)) | Hunt, Thomas, 2019, юбилейное изд. | — | Профессиональные привычки: DRY, ортогональность, tracer bullets |
| *Modern Software Engineering* ([InformIT](https://www.informit.com/store/modern-software-engineering-doing-what-works-to-build-9780137314911)) | David Farley, 2021 | — | Инженерия как обучение и управление сложностью; модульность, связность, зацепление |
| *Design Patterns* (GoF) ([InformIT](https://www.informit.com/store/design-patterns-elements-of-reusable-object-oriented-9780201633610)) | Gamma, Helm, Johnson, Vlissides, 1994 | — | Словарь паттернов; многие (Strategy, Command, Visitor) сегодня сводятся к лямбдам или sealed-типам со switch. Читать глазами современного Java |
| *Head First Design Patterns* ([O'Reilly](https://www.oreilly.com/library/view/head-first-design/9781492077992/)) | Eric Freeman, Elisabeth Robson, 2020, 2-е изд. | Java 8 | Доступное введение в паттерны на Java `[не проверено]` |
| Google Java Style Guide ([guide](https://google.github.io/styleguide/javaguide.html)) | Google | — | Широко принятая база форматирования и имён (google-java-format) |
| SEI CERT Oracle Coding Standard for Java ([wiki](https://wiki.sei.cmu.edu/confluence/display/java/SEI+CERT+Oracle+Coding+Standard+for+Java)) | CMU SEI | — | Каталог правил корректности и безопасности (числа, исключения, параллелизм, сериализация) |
| *Software Engineering at Google* ([онлайн бесплатно](https://abseil.io/resources/swe-book)) | Winters, Manshreck, Wright, 2020 | — | Code review, тестирование в масштабе, депрекация, управление зависимостями |

## Производительность, настройка JVM и бенчмарки

**Ключевые выводы**

- Основная тройка:
  - Scott Oaks, *Java Performance* 2-е изд. (2020, Java 8 и 11). Лучшая единая книга по настройке: JIT, GC, куча и native memory, потоки, JDBC/JPA.
  - Evans и Gough, *Optimizing Cloud Native Java* (2024). 2-е издание *Optimizing Java*, переписанное под контейнеры, observability и современный параллелизм.
  - Monica Beckwith, *JVM Performance Engineering* (2024). Устройство HotSpot, GC, Valhalla, логирование, микробенчмарки.
- *The Garbage Collection Handbook* 2-е изд. (2023) — алгоритмический справочник за G1, ZGC и Shenandoah.
- Книги не успевают за изменениями GC в JDK 21–27 (generational ZGC как единственный режим ZGC, работа над пропускной способностью G1 в 26, compact object headers, AOT Leyden). Для них используйте JEP, inside.java и GC tuning guide.
- Основы бенчмаркинга закрывает бесплатный материал: доклады Шипилёва о JMH, JVM Anatomy Quarks, «How NOT to Measure Latency» Gil Tene и примеры JMH.

| Книга | Автор, год, издание | Версия Java | Зачем читать |
|---|---|---|---|
| *Java Performance* ([O'Reilly](https://oreilly.com/library/view/java-performance-2nd/9781492056102)) | Scott Oaks, февраль 2020, 2-е изд. | Java 8/11 | Методология, инструменты (JFR, jcmd), настройка JIT, G1/Parallel/CMS и предпросмотр ZGC, куча и native memory, потоки, коллекции, JDBC/JPA. Главы про GC частично устарели. `[не проверено]` |
| *Java Performance: The Definitive Guide* ([O'Reilly](https://www.oreilly.com/library/view/java-performance-the/9781449363512/)) | Oaks, 2014 | Java 7/8 | `[устарело]`: заменено 2-м изданием |
| *Optimizing Cloud Native Java* ([запись каталога TUM](https://search.ub.tum.de/vufind/Record/ZDB-30-ORH-100860508?lng=en)) | Ben Evans, James Gough, O'Reilly, 2024, 2-е изд. *Optimizing Java* | современный | Ловушки измерений, микробенчмарки, железо и ОС, GC, JIT, контейнеры и Kubernetes, observability, параллелизм `[не проверено]` |
| *Optimizing Java* ([O'Reilly](https://www.oreilly.com/library/view/optimizing-java/9781492039259/)) | Evans, Gough, Chris Newland, апрель 2018 | Java 8–10 | Главы о JIT и GC ещё полезны. `[устарело]`: заменено *Optimizing Cloud Native Java* |
| *JVM Performance Engineering: Inside OpenJDK and the HotSpot JVM* ([Pearson](https://www.pearson.com/en-us/subject-catalog/p/jvm-performance-engineering-inside-the-openjdk-hotspot-vm/P200000000120/9780134659978), [O'Reilly](https://www.oreilly.com/library/view/jvm-performance-engineering/9780134659954)) | Monica Beckwith, Addison-Wesley, 2024 | современный HotSpot | Устройство HotSpot, GC (TLAB, NUMA), система типов и Valhalla, модули, унифицированное логирование, JMH. Pearson проверен, O'Reilly `[не проверено]` |
| *The Garbage Collection Handbook* ([сайт](https://gchandbook.org/), [O'Reilly](https://www.oreilly.com/library/view/-/9781000883688/), [страница автора](https://www.cs.kent.ac.uk/people/staff/rej/)) | Richard Jones, Antony Hosking, Eliot Moss, CRC Press, июль 2023, 2-е изд. | не зависит от версии | Теория всех современных сборщиков: конкурентный, параллельный и real-time GC, барьеры, слабые ссылки, энергопотребление |
| *Java Performance Companion* ([O'Reilly](https://www.oreilly.com/library/view/java-r-performance-companion/9780133796896/)) | Charlie Hunt, Monica Beckwith, Poonam Parhar, Bengt Rutisson, апрель 2016 | JDK 8 | Внутренности и настройка G1, Serviceability Agent. G1 эпохи JDK 8, частично устарела `[не проверено]` |
| *Troubleshooting Java* ([Manning](https://www.manning.com/books/troubleshooting-java-second-edition)) | Laurențiu Spilcă, октябрь/ноябрь 2025, 2-е изд., предисловие Ben Evans | современный | Практическая диагностика: отладчик, профилировщики (VisualVM), дампы кучи и потоков, профилирование SQL, взаимные блокировки, микросервисы, диагностика с помощью AI |
| *Mastering the Java Virtual Machine* ([O'Reilly](https://www.oreilly.com/library/view/mastering-the-java/9781835467961)) | Otavio Santana, Packt, февраль 2024 | современный | Короткий обзор JVM: загрузка классов, память, GC, GraalVM, reflection. Вводный уровень `[не проверено]` |
| *High-Performance Java Persistence* ([Leanpub](https://leanpub.com/high-performance-java-persistence), [страница автора](https://vladmihalcea.com/the-high-performance-java-persistence-book/)) | Vlad Mihalcea, 2016, ebook продаётся до сих пор | Java 8 / Hibernate 5 | Batching в JDBC, кэш запросов, fetching, транзакции и блокировки, производительность JPA/Hibernate, jOOQ. Печатное издание 2016 года |
| JVM Anatomy Quarks ([индекс](https://shipilev.net/jvm/anatomy-quarks/)) | Aleksey Shipilev, 2017 – продолжается | современный | 30 коротких разборов (TLAB, scalar replacement, lock elision, compressed refs, safepoints, мегаморфные вызовы); сборники ePUB/MOBI/PDF |
| «Nanotrusting the Nanotime» ([блог](https://shipilev.net/blog/2014/nanotrusting-nanotime/)) | Shipilev, 2014 | — | Почему гранулярность таймера и задержки ломают наивные бенчмарки |
| *GC Tuning Guide*, Java SE 25 ([docs](https://docs.oracle.com/en/java/javase/25/gctuning/)) | Oracle | 25 | Официальная эргономика и флаги G1/ZGC/Parallel/Serial для текущей LTS |
| JMH ([проект](https://openjdk.org/projects/code-tools/jmh/)) | OpenJDK | — | Фреймворк микробенчмарков; его samples — лучший учебник по бенчмаркингу |
| HdrHistogram ([GitHub](https://github.com/HdrHistogram/HdrHistogram)) | Gil Tene | — | Корректная запись задержек (избегает coordinated omission) |
| Воркшоп jvmperf ([сайт](https://jvmperf.net/)) | сообщество | — | Бесплатный практический воркшоп по производительности JVM (JFR, JMC, анализ кучи) |
| Сайт Brendan Gregg ([brendangregg.com](https://www.brendangregg.com/)) | Brendan Gregg | — | Flame graphs, метод USE, Linux perf; применимо к профилированию JVM через async-profiler |

## Параллелизм, виртуальные потоки и модель памяти

**Ключевые выводы**

- *Java Concurrency in Practice* (2006) остаётся книгой о потокобезопасности, публикации, неизменяемости и дизайне `java.util.concurrent`. Главы о запуске задач старше fork/join, `CompletableFuture` и виртуальных потоков.
- Для Java эпохи Loom читайте *Modern Concurrency in Java* (Rahman, O'Reilly, 2025): внутреннее устройство виртуальных потоков, планировщик (ForkJoinPool), pinning, отладка и мониторинг, structured concurrency и scoped values. Затем читайте JEP: 444 (виртуальные потоки), 491 (нет pinning при `synchronized`, JDK 24), предпросмотры structured concurrency и 506 (scoped values, финальные в 25).
- Для модели памяти читайте бесплатные «JMM Pragmatics» и «Close Encounters of the JMM Kind» Шипилёва и руководство Doug Lea по режимам порядка памяти. Для тестов используйте jcstress.
- *Concurrent Programming in Java* Lea (1999) историческая. Читайте её ради логики, стоящей за j.u.c.

| Книга | Автор, год, издание | Версия Java | Зачем читать |
|---|---|---|---|
| *Java Concurrency in Practice* ([InformIT](https://www.informit.com/store/java-concurrency-in-practice-9780321349606), [jcip.net](https://jcip.net/)) | Brian Goetz, Tim Peierls, Joshua Bloch, Joseph Bowbeer, David Holmes, Doug Lea, 2006 | Java 5/6 | Потокобезопасность, видимость, безопасная публикация, составление потокобезопасных классов, executors, liveness, тестирование. 2-го издания нет; советы по executors и пулам старше виртуальных потоков |
| *Modern Concurrency in Java: Virtual Threads, Structured Concurrency, and Beyond* ([O'Reilly](https://oreilly.com/library/view/modern-concurrency-in/9781098165406), [код](https://github.com/Modern-Concurrency-in-Java/modern-concurrency-java-book)) | A N M Bazlur Rahman, сентябрь–октябрь 2025 | Java 21–25 | Устройство виртуальных потоков, carrier-планирование, pinning, отладка и мониторинг, structured concurrency, scoped values, rate limiting, сравнение с Reactor. Книга `[не проверено]`, репозиторий кода проверен |
| *Concurrent Programming in Java: Design Principles and Patterns* ([InformIT](https://www.informit.com/store/concurrent-programming-in-java-design-principles-and-9780201310092)) | Doug Lea, 1999, 2-е изд. | до Java 5 | Логика дизайна j.u.c. от её автора. Историческая, API до Java 5 |
| «Close Encounters of the JMM Kind» ([блог](https://shipilev.net/blog/2016/close-encounters-of-jmm-kind/)) | Shipilev, 2016 | — | Мифы и реальность JMM на примерах, проверенных jcstress |
| «Java Memory Model Pragmatics» ([стенограмма](https://shipilev.net/blog/2014/jmm-pragmatics/)) | Shipilev, 2014 | — | Happens-before, гонки, final-поля, объяснённые по спецификации |
| «Using JDK 9 Memory Order Modes» ([страница](https://gee.cs.oswego.edu/dl/html/j9mm.html)) | Doug Lea | JDK 9+ | Режимы plain/opaque/release-acquire/volatile для пользователей `VarHandle` `[не проверено]` |
| jcstress ([проект](https://openjdk.org/projects/code-tools/jcstress/)) | OpenJDK (Shipilev) | — | Фреймворк тестов корректности параллельного кода; его примеры учат JMM |
| «The Age of Virtual Threads» ([inside.java](https://inside.java/2022/10/13/the-age-of-virtual-threads)) | Ron Pressler, Alan Bateman, Devoxx 2022 | — | Обоснование дизайна виртуальных потоков и structured concurrency от руководителей Loom |
| «Virtual Threads for Lightweight Concurrency» ([доклад InfoQ](https://www.infoq.com/presentations/virtual-threads-lightweight-concurrency)) | Ron Pressler, QCon | — | 48-минутный доклад со стенограммой о дизайне Loom |
| Project Loom ([проект](https://openjdk.org/projects/loom/)) | OpenJDK | — | Ссылки на JEP, документы дизайна, рассылку loom-dev |
| Inside Java Podcast 26: Loom ([выпуск](https://inside.java/2022/09/20/podcast-026/)) | Pressler, 2022 | — | Аудиообзор целей Loom `[не проверено]` |
| Рассылка JavaSpecialists Heinz Kabutz ([сайт](https://www.javaspecialists.eu/), [архив](https://www.javaspecialists.eu/archive/)) | Kabutz, 2000 – продолжается | — | Более 300 выпусков о параллелизме, коллекциях и тонкостях производительности; его курс по JCiP рекомендуют как современное дополнение |

## Устройство JVM и спецификации

**Ключевые выводы**

- Спецификация JVM и JLS (см. [Основы](#основы-и-современный-java)) нормативны. *Inside the Java Virtual Machine* (Venners, 2-е изд., 1999; главы бесплатно) остаётся мягким введением в байт-код, но по GC и JIT `[устарело]`.
- Для сегодняшнего HotSpot читайте Beckwith (2024), *Optimizing Cloud Native Java* (2024), Quarks Шипилёва, JITWatch Chris Newland и страницы проектов OpenJDK (Valhalla, Leyden, Panama).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| *The Java Virtual Machine Specification*, SE 27 ([HTML](https://docs.oracle.com/javase/specs/jvms/se27/html/index.html)) / SE 25 ([HTML](https://docs.oracle.com/javase/specs/jvms/se25/html/index.html)) | Lindholm, Yellin, Bracha, Buckley, Smith и др.; Oracle | Формат class-файла, семантика байт-кода, верификация, связывание |
| *Inside the Java Virtual Machine*, 2-е изд. ([главы бесплатно](https://www.artima.com/insidejvm/ed2/)) | Bill Venners, 1999 | Читаемое введение в class-файлы, стековую машину, вызов методов. По GC и JIT `[устарело]`: используйте Beckwith 2024 |
| JITWatch ([GitHub](https://github.com/AdoptOpenJDK/jitwatch)) и сайт Chris Newland ([chrisnewland.com](https://www.chrisnewland.com/)) | Chris Newland | Визуализация логов JIT HotSpot: решения об inlining, ассемблер, escape analysis |
| Project Valhalla ([проект](https://openjdk.org/projects/valhalla/), [State of Valhalla](https://openjdk.org/projects/valhalla/design-notes/state-of-valhalla/01-background)) | Goetz и др. | Value-классы, null-restricted типы, flattening; заметки о дизайне объясняют, почему |
| Project Leyden ([проект](https://openjdk.org/projects/leyden/)) | OpenJDK | AOT-кэш, старт и прогрев |
| Project Panama ([проект](https://openjdk.org/projects/panama/)) | OpenJDK | FFM API (финальный в 22), Vector API (incubating) |
| Документы Brian Goetz ([cr.openjdk.org/~briangoetz](https://cr.openjdk.org/~briangoetz/)) | Goetz | Заметки о дизайне Amber и Valhalla (pattern matching, records, carrier-классы) |
| JVM Language Summit ([программа и архив](https://openjdk.org/projects/mlvm/jvmlangsummit/), [JVMLS 2025](https://inside.java/2025/07/01/jvmls2025/)) | OpenJDK, ежегодно | Самые глубокие доклады об устройстве JVM и будущем языка |

## Архитектура, дизайн и фреймворки

**Ключевые выводы**

- «Хребет» архитектурного чтения:
  - Ousterhout (дизайн модулей, см. [Идиоматичный код](#идиоматичный-и-сопровождаемый-код))
  - *Get Your Hands Dirty on Clean Architecture* 2-е изд. (Hombergs, 2023): самая практичная книга о гексагональной архитектуре (ports and adapters) на Java, с проверкой через ArchUnit и границы модулей
  - *Clean Architecture* (Martin, 2017) ради правила зависимостей
  - *Learning Domain-Driven Design* (Khononov, 2021) как современная точка входа в DDD; затем Evans (2003) и Vernon (2013)
  - *Fundamentals of Software Architecture* 2-е изд. (2025) и *Software Architecture: The Hard Parts* (2021) для мышления в компромиссах
  - *DDIA* 2-е изд. (2026) для систем данных
- О микросервисах сначала читайте *Building Microservices* 2-е изд. Newman (2021), затем *Microservices Patterns* Richardson. Первое издание — 2018 года; 2-е издание в MEAP до весны 2027. Бесплатный каталог паттернов — microservices.io.
- *Domain Modeling Made Functional* (Wlaschin, F#, 2018) — лучшее объяснение принципа «сделайте недопустимые состояния непредставимыми». С records, sealed-интерфейсами и pattern matching из Java 21+ переносится почти один к одному. Читайте вместе с *Data-Oriented Programming in Java* Kiehl (см. [DOP](#функциональный-стиль-и-data-oriented-programming)).
- Spring:
  - *Spring Start Here* (2021) для новичков. 2-е издание под Spring 7 / Boot 4 в MEAP.
  - *Spring Security in Action* 2-е изд. (2024)
  - *Cloud Native Spring in Action* (2022, Boot 2.7/3, K8s)
  - *Pro Spring 6* (Apress, 2023) как справочник
  - *Spring in Action* 6-е изд. (2022, Spring 5.3) устарела. 7-го издания не найдено.
- Quarkus: *Quarkus in Action* (февраль 2025).
- Персистентность: *High-Performance Java Persistence* Mihalcea (см. [Производительность](#производительность-настройка-jvm-и-бенчмарки)) и *Java Persistence with Spring Data and Hibernate* (Tudose, 2023).

| Книга | Автор, год, издание | Версия Java | Зачем читать |
|---|---|---|---|
| *Get Your Hands Dirty on Clean Architecture* ([Packt](https://www.packtpub.com/en-cz/product/get-your-hands-dirty-on-clean-architecture-9781805125914)) | Tom Hombergs, июль 2023, 2-е изд. | современный | Гексагональная архитектура на Java: use cases, ports, adapters, стратегии маппинга, контроль границ, осознанные уступки `[не проверено]` |
| *Clean Architecture* ([InformIT](https://www.informit.com/store/clean-architecture-a-craftsmans-guide-to-software-structure-9780134494166)) | Robert C. Martin, 2017 | — | Правило зависимостей, принципы связности и зацепления компонентов, SOLID на уровне архитектуры. Читать вместе с APoSD |
| *Domain-Driven Design* ([InformIT](https://www.informit.com/store/domain-driven-design-tackling-complexity-in-the-heart-9780321125217)) | Eric Evans, 2003 | старый Java | Исходный DDD: единый язык, bounded contexts, агрегаты, стратегический дизайн. Примеры на Java устарели |
| *Implementing Domain-Driven Design* ([InformIT](https://www.informit.com/store/implementing-domain-driven-design-9780321834577)) | Vaughn Vernon, 2013 | до records | Тактический DDD с уклоном в Java: агрегаты, доменные события, репозитории, context mapping |
| *Domain-Driven Design Distilled* ([InformIT](https://www.informit.com/store/domain-driven-design-distilled-9780134434421)) | Vernon, 2016 | — | Обзор DDD на 150 страниц |
| *Learning Domain-Driven Design* ([O'Reilly](https://www.oreilly.com/library/view/learning-domain-driven-design/9781098100124/)) | Vlad Khononov, октябрь 2021 (2-й тираж 2024) | — | Современный практичный DDD: поддомены, bounded contexts, паттерны бизнес-логики, event sourcing, эвристики выбора архитектуры `[не проверено]` |
| DDD Crew ([GitHub](https://github.com/ddd-crew)) | сообщество | — | Бесплатные шаблоны: процесс моделирования DDD, context mapping, EventStorming |
| *Domain Modeling Made Functional* ([Pragmatic](https://pragprog.com/titles/swdddf/domain-modeling-made-functional/)) | Scott Wlaschin, 2018 | F# | Алгебраические типы для моделирования домена, процессы как конвейеры, ошибки как значения; ложится на records и sealed-типы Java |
| *Fundamentals of Software Architecture* ([O'Reilly](https://oreilly.com/library/view/fundamentals-of-software/9781098175504)) | Mark Richards, Neal Ford, март 2025, 2-е изд. | — | Архитектурные характеристики, стили (layered, modular monolith, microservices, event-driven), диаграммы, governance, генеративный AI, team topologies. 1-е изд. (2020) `[устарело]`. O'Reilly `[не проверено]` |
| *Software Architecture: The Hard Parts* ([O'Reilly](https://www.oreilly.com/library/view/software-architecture-the/9781492086888/)) | Ford, Richards, Sadalage, Dehghani, октябрь 2021 | — | Компромиссы в распределённых системах: гранулярность, владение данными, распределённые транзакции, саги, контракты `[не проверено]` |
| *Building Microservices* ([O'Reilly](https://www.oreilly.com/library/view/building-microservices-2nd/9781492034018/)) | Sam Newman, август 2021, 2-е изд. | — | Когда (не) нужны микросервисы, моделирование, стили взаимодействия, развёртывание, тесты, observability, безопасность, устойчивость `[не проверено]` |
| *Microservices Patterns* ([Manning](https://www.manning.com/books/microservices-patterns)) / 2-е изд. MEAP ([Manning](https://www.manning.com/books/microservices-patterns-second-edition), [анонс](https://microservices.io/post/architecture/2025/06/26/announcing-meap-microservices-patterns-2nd-edition.html)) | Chris Richardson, 2018 / **MEAP с июня 2025, не вышла, ожидается весной 2027** | Java/Spring | Саги, CQRS, API composition, transactional outbox, тестирование микросервисов. 1-е изд. частично устарело |
| Язык паттернов microservices.io ([сайт](https://microservices.io/)) | Richardson | — | Бесплатный каталог паттернов микросервисов и управления данными |
| *Designing Data-Intensive Applications* ([O'Reilly](https://www.oreilly.com/library/view/designing-data-intensive/9781098119058/)) | Martin Kleppmann, Chris Riccomini, февраль/март 2026, 2-е изд. | — | Движки хранения, репликация, партиционирование, транзакции, потоковая обработка; справочник по архитектуре данных. 1-е изд. (2017) `[устарело]`. O'Reilly `[не проверено]` |
| *Patterns of Enterprise Application Architecture* ([martinfowler.com](https://martinfowler.com/books/eaa.html)) | Martin Fowler, 2002 | — | Источник repository, unit of work, data mapper, service layer (словарь JPA/Hibernate). Классика |
| *Enterprise Integration Patterns* ([сайт](https://www.enterpriseintegrationpatterns.com/)) | Gregor Hohpe, Bobby Woolf, 2003 | — | Паттерны обмена сообщениями за Spring Integration, Apache Camel и дизайном на Kafka. Классика |
| *Release It!* ([Pragmatic](https://pragprog.com/titles/mnee2/release-it-second-edition/)) | Michael Nygard, 2018, 2-е изд. | — | Паттерны устойчивости (timeouts, circuit breakers, bulkheads) и антипаттерны для продакшен-сервисов на JVM |
| *Architecture Modernization* ([Manning](https://www.manning.com/books/architecture-modernization)) | Nick Tune, Jean-Georges Perrin, 2024 | — | Модернизация legacy-систем через DDD, team topologies и wardley mapping |
| *The Java Module System* ([Manning](https://www.manning.com/books/the-java-module-system)) | Nicolai Parlog, 2019 | Java 9–11 | JPMS в глубину: сильная инкапсуляция, services, миграция. Эпоха Java 9–11, но JPMS изменился мало |
| *Spring Start Here* ([Manning](https://www.manning.com/books/spring-start-here)) / 2-е изд. MEAP ([Manning](https://www.manning.com/books/spring-start-here-second-edition)) | Laurențiu Spilcă, 2021 / **MEAP с июня 2026 (Spring 7, Boot 4), не вышла, ожидается в начале 2027** | Spring 5 / 7 | Лучшая первая книга о Spring: контекст, beans, AOP, web, данные |
| *Spring Security in Action* ([Manning](https://www.manning.com/books/spring-security-in-action-second-edition)) | Spilcă, апрель/июнь 2024, 2-е изд. | Spring Security 6 / Boot 3 | `SecurityFilterChain`, OAuth2/OIDC, authorization server, тестирование |
| *Spring in Action* ([Manning](https://www.manning.com/books/spring-in-action-sixth-edition)) | Craig Walls, 2022, 6-е изд. | Spring 5.3 / Boot 2.4 | `[устарело]` для Boot 3/4; берите официальные руководства Spring и книги выше. 7-го издания нет |
| *Spring Boot in Practice* ([Manning](https://www.manning.com/books/spring-boot-in-practice)) | Somnath Musib, 2022 | Boot 2.x | Рецепты работы с Boot |
| *Spring Boot: Up and Running* ([O'Reilly](https://www.oreilly.com/library/view/spring-boot-up/9781492076971/)) | Mark Heckler, 2021 | Boot 2.x | Практическое введение в Boot, включая реактивность и тесты `[не проверено]` |
| *Pro Spring 6* ([Springer](https://link.springer.com/book/10.1007/978-1-4842-8640-1)) | Iuliana Cosmina, Rob Harrop, Chris Schaefer, Clarence Ho, Apress, 2023 | Spring 6 / Boot 3 | Подробный справочник по Spring Framework 6 / Boot 3 |
| *Cloud Native Spring in Action* ([Manning](https://www.manning.com/books/cloud-native-spring-in-action)) | Thomas Vitale, ноябрь 2022 | Java 17 | Boot + Kubernetes: buildpacks, конфигурация, устойчивость, безопасность, observability, GitOps |
| *Testing Spring Boot Applications* ([страница автора Manning](https://www.manning.com/authors/daniel-garnier-moiroux), [подкаст Spring](https://spring.io/blog/2026/05/07/a-bootiful-podcast-daniel-garnier-moiroux/)) | Daniel Garnier-Moiroux, **MEAP с марта 2026, не вышла, ожидается в начале 2027** | Boot 3/4 | Срезы тестов, конфигурация, тесты безопасности, Testcontainers, AssertJ, Awaitility; автор из команды Spring. Страница книги отдавала 404 скриптам `[не проверено]`, подкаст проверен |
| *Quarkus in Action* ([Manning](https://www.manning.com/books/quarkus-in-action), [e-book Red Hat](https://developers.redhat.com/e-books/quarkus-action)) | Martin Štefanko, Jan Martiška, февраль 2025 | современный | Dev mode, Dev UI, continuous testing, messaging, gRPC, GraphQL, Kubernetes, observability. Страница e-book Red Hat `[не проверено]` |
| *Java Persistence with Spring Data and Hibernate* ([Manning](https://www.manning.com/books/java-persistence-with-spring-data-and-hibernate)) | Cătălin Tudose, 2023 | Hibernate 6 | Современный JPA/Hibernate 6 плюс Spring Data; преемник *Java Persistence with Hibernate* |

## Тестирование

**Ключевые выводы**

- *Unit Testing Principles, Practices, and Patterns* (Khorikov, 2020) даёт лучшую концептуальную модель хорошего теста: устойчивость к рефакторингу, классическая школа против лондонской, когда нужны моки (только для неуправляемых внепроцессных зависимостей). Примеры на C#, но идеи не привязаны к языку.
- *Effective Software Testing* (Aniche, 2022) — лучшая книга на Java о дизайне тестов: на основе спецификации, граничные значения, структурное и property-based тестирование, с JUnit 5 и jqwik.
- *GOOS* (2009) — классика TDD. Концептуально ценна, но стиль jMock устарел.
- Для практики JUnit 5 + Mockito: *Pragmatic Unit Testing in Java with JUnit* 3-е изд. (2024, Java 21) и *Mockito Made Clear* (2023). Книга Kaczanowski *Practical Unit Testing with JUnit and Mockito* — 2013/2019, старше.

| Книга | Автор, год, издание | Версия Java | Зачем читать |
|---|---|---|---|
| *Unit Testing Principles, Practices, and Patterns* ([Manning](https://www.manning.com/books/unit-testing), [блог автора](https://enterprisecraftsmanship.com/)) | Vladimir Khorikov, 2020 | примеры на C# | Четыре столпа хорошего теста, моки и стабы, тесты домена и контроллеров, интеграционные тесты |
| *Effective Software Testing: A Developer's Guide* ([Manning](https://www.manning.com/books/effective-software-testing), [сайт](https://effective-software-testing.com/)) | Maurício Aniche, 2022 | Java, JUnit 5 | Систематический дизайн тестов на Java: по спецификации, граничные значения, структурные, design-by-contract, property-based, test doubles |
| *Growing Object-Oriented Software, Guided by Tests* ([InformIT](https://www.informit.com/store/growing-object-oriented-software-guided-by-tests-9780321503626)) | Steve Freeman, Nat Pryce, 2009 | старый Java | TDD «снаружи внутрь» с полным примером на Java; «слушайте тесты». Инструменты устарели |
| *Pragmatic Unit Testing in Java with JUnit* ([Pragmatic](https://pragprog.com/titles/utj3/pragmatic-unit-testing-in-java-with-junit-third-edition/)) | Jeff Langr, ноябрь/декабрь 2024, 3-е изд. | Java 21 | JUnit 5 на Java 21: что тестировать, рефакторинг, TDD, AI-инструменты в тестировании |
| *Mockito Made Clear* ([Pragmatic](https://pragprog.com/titles/mockito/mockito-made-clear/)) | Ken Kousen, 2023 | Mockito 5 | Короткое сфокусированное руководство по Mockito 5 (stubs, spies, argument captors, BDD-стиль) |
| *JUnit in Action* ([Manning](https://www.manning.com/books/junit-in-action-third-edition)) | Cătălin Tudose, 2020, 3-е изд. | JUnit 5 | Миграция на JUnit 5, расширения, тесты Spring и REST. Инструменты 2020 года |
| *Practical Unit Testing* ([страница автора](https://kaczanowscy.pl/books/practical_unit_testing_junit_testng_mockito.html)) | Tomek Kaczanowski, 2013 / печать 2019 | JUnit 5 (по странице автора) | Читаемые тесты, моки, стабы и шпионы, параметризованные тесты, TDD, покрытие и мутационное тестирование. Страница автора описывает редакцию для JUnit 5, TestNG, Mockito и AssertJ; год этой редакции на ней не указан `[не проверено]` |
| *Testing Spring Boot Applications* | Garnier-Moiroux, MEAP, не вышла | — | См. [Архитектура, дизайн и фреймворки](#архитектура-дизайн-и-фреймворки) `[не проверено]` |

## Функциональный стиль и data-oriented programming

**Ключевые выводы**

- Главный сдвиг в дизайне современного Java — DOP. Records, sealed-интерфейсы, pattern matching для `switch` и record patterns позволяют моделировать данные алгебраическими типами, а поведение держать в функциях.
- Каноничный текст — «Data-Oriented Programming in Java» Goetz (InfoQ, июнь 2022). В 2024 году Inside.java опубликовал обновление «DOP v1.1».
- Первая полная книга на эту тему — *Data-Oriented Programming in Java* Kiehl (Manning, MEAP; завершение ожидается в конце 2026). **Книга ещё не вышла.**
- *Data-Oriented Programming* Sharvit (2022) — другая, близкая к Clojure идея «общих структур данных». Она полезна идеями, но не описывает подход Java с records. Не путайте их.
- Для повседневного FP в Java: Subramaniam 2-е изд. (2023) и Weidig (2023).

| Книга или статья | Автор, год, издание | Версия Java | Зачем читать |
|---|---|---|---|
| «Data-Oriented Programming in Java» ([InfoQ](https://www.infoq.com/articles/data-oriented-programming-java/), [зеркало inside.java](https://inside.java/2022/06/20/data-oriented-programming-in-java/)) | Brian Goetz, июнь 2022 | Java 21 | Принципы: моделируйте данные, все данные и только данные; неизменяемые данные; делайте недопустимые состояния непредставимыми; проверяйте на границе |
| «Data-Oriented Programming v1.1» ([inside.java](https://inside.java/2024/06/26/dop-v1-1-why-update/)) | Nicolai Parlog, июнь 2024 | Java 21+ | Бонусная статья, завершающая серию из шести статей inside.java о DOP; пересматривает четыре принципа Goetz для Java 21+ |
| *Data-Oriented Programming in Java* ([Manning](https://www.manning.com/books/data-oriented-programming-in-java), [блог автора](https://chriskiehl.com/)) | Chris Kiehl, **MEAP с сентября 2024, не вышла**; ожидается ноябрь 2026, около 480 стр. | Java 21+ | Records, sealed-классы, pattern matching для моделирования доменов; отказ от null; объекты для побочных эффектов |
| *Data-Oriented Programming* ([Manning](https://www.manning.com/books/data-oriented-programming)) | Yehonathan Sharvit, 2022 | не про Java | DOP на общих структурах данных (в духе Clojure), неизменяемость, валидация данных. Другое значение термина DOP |
| *Functional Programming in Java* ([Pragmatic](https://pragprog.com/titles/vsjava2e/functional-programming-in-java-second-edition/)) | Venkat Subramaniam, июль 2023, 2-е изд. | современный | Лямбды, streams, ленивость; новые главы об обработке ошибок, рефакторинге в функциональный стиль, преобразовании данных, идиомах |
| *A Functional Approach to Java* ([O'Reilly](https://www.oreilly.com/library/view/-/9781098109912/)) | Ben Weidig, май 2023 | современный | Добавление FP (неизменяемость, чистые функции, `Optional`, streams, records) в ОО-код; средний и продвинутый уровень `[не проверено]` |
| *Functional Programming in Java* ([Manning](https://www.manning.com/catalog/programming-languages-and-styles/java)) | Pierre-Yves Saumont, 2017 | Java 8 | Строит FP-типы с нуля на Java 8. `[устарело]`: заменено двумя книгами выше. Найдена в каталоге Manning |
| *Domain Modeling Made Functional* | Wlaschin, 2018 | F# | См. [Архитектура, дизайн и фреймворки](#архитектура-дизайн-и-фреймворки) |
| Руководство по `var` (LVTI) ([OpenJDK](https://openjdk.org/projects/amber/guides/lvti-style-guide)) и FAQ ([OpenJDK](https://openjdk.org/projects/amber/guides/lvti-faq)) | Stuart Marks, 2018 | Java 10+ | Когда `var` помогает, а когда вредит читаемости |

## Безопасность и алгоритмы

**Ключевые выводы**

- Актуальной полной книги по безопасности Java нет. *Iron-Clad Java* (2014) издатель до сих пор показывает, но она старше современного Java и Spring Security 6.
- Практичные источники: *Secure Coding Guidelines for Java SE* Oracle (обновляются с каждым релизом), OWASP Cheat Sheets и Top 10, SEI CERT (см. [Идиоматичный код](#идиоматичный-и-сопровождаемый-код)), *Spring Security in Action* 2-е изд. (см. [Архитектура](#архитектура-дизайн-и-фреймворки)) и *Secure by Design* (Manning, 2019, примеры на Java: domain primitives, неизменяемость, fail-fast контракты). *Secure by Design* хорошо сочетается с DOP.
- По алгоритмам: *Algorithms* 4-е изд. Sedgewick и Wayne написана на Java, с бесплатным кодом на сайте книги и курсами на Coursera.

| Ресурс | Автор, год, издание | Версия Java | Зачем читать |
|---|---|---|---|
| *Secure Coding Guidelines for Java SE* ([Oracle](https://www.oracle.com/java/technologies/javase/seccodeguide.html)) | Oracle | каждый релиз | Официальные рекомендации по инъекциям, десериализации, изменяемости, привилегиям, переполнению чисел |
| *Secure by Design* ([Manning](https://www.manning.com/books/secure-by-design)) | Dan Bergh Johnsson, Daniel Deogun, Daniel Sawano, 2019 | примеры на Java | Безопасность через дизайн: domain primitives, read-once объекты, порядок валидации, неизменяемость |
| *Iron-Clad Java: Building Secure Web Applications* ([McGraw Hill](https://www.mhprofessional.com/iron-clad-java-building-secure-web-applications-9780071835886-usa)) | Jim Manico, August Detlefsen, 2014 | Java 7/8 | Безопасность веб-приложений на Java (XSS, CSRF, управление сессиями, криптография). `[устарело]`: используйте OWASP и *Spring Security in Action* |
| OWASP Cheat Sheet Series ([индекс](https://cheatsheetseries.owasp.org/), [Java Security](https://cheatsheetseries.owasp.org/cheatsheets/Java_Security_Cheat_Sheet.html)) | OWASP | — | Поддерживаемые конкретные рекомендации (инъекции, десериализация, XXE, логирование) |
| OWASP Top 10 ([проект](https://owasp.org/www-project-top-ten/)) | OWASP | — | Таксономия рисков веб-приложений |
| *Fundamentals of Security using JDK Libraries* ([dev.java](https://dev.java/learn/security/)) | Oracle | современный | Официальный учебник по JDK security API (криптография, keystores, TLS). Указан на dev.java/learn |
| *Algorithms* ([сайт книги](https://algs4.cs.princeton.edu/home/), [InformIT](https://www.informit.com/store/algorithms-9780321573513)) | Robert Sedgewick, Kevin Wayne, 2011, 4-е изд. | Java | Структуры данных и алгоритмы с кодом на Java; бесплатный сайт со всем кодом |
| Algorithms Part I / II ([Coursera I](https://www.coursera.org/learn/algorithms-part1), [Coursera II](https://www.coursera.org/learn/algorithms-part2)) | Princeton (Sedgewick, Wayne) | Java | Бесплатный курс к книге, задания на Java |

## Официальные источники и OpenJDK

**Ключевые выводы**

- **Первоисточники идут первыми.** Используйте dev.java/learn (официальные учебники), inside.java (статьи, Newscast, подкаст команды Oracle Java), индекс JEP и страницы JDK по релизам, Java Almanac (разница API между любыми двумя версиями) и списки рассылки OpenJDK. Только они актуальны для Java 22–27.
- **Лучшие независимые технические блоги по теме «оптимальные решения и паттерны»:**
  - Vlad Mihalcea (производительность JPA/Hibernate/SQL)
  - Thorben Janssen (JPA/Hibernate)
  - Marco Behler (длинные практические руководства)
  - Nicolai Parlog / nipafx (возможности современного Java)
  - Aleksey Shipilev (производительность JVM, JMM)
  - Gunnar Morling (1BRC, CDC, производительность)
  - Heinz Kabutz (JavaSpecialists)
  - Stuart Marks (коллекции, дизайн API)
  - Baeldung для быстрых рецептов по API (широко, глубина неравномерна)
- **Отчёты об экосистеме («что используют на практике»):** InfoQ Java Trends 2025, JetBrains State of Java 2025, Azul State of Java 2026, New Relic 2024.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| dev.java Learn ([learn](https://dev.java/learn/)) | Oracle | Официальные учебники: первые шаги, язык, API, организация приложений, JVM и основные инструменты ([core tools](https://dev.java/learn/jvm/core-tools/)), безопасность, JavaFX |
| Java Playground ([dev.java](https://dev.java/playground/)) | Oracle | Запуск фрагментов Java в браузере |
| inside.java ([сайт](https://inside.java/)) | Oracle Java Platform Group | Статьи, разборы JEP, видео Newscast, предупреждения Quality Outreach о грядущих изменениях |
| Индекс JEP ([JEP 0](https://openjdk.org/jeps/0)) | OpenJDK | Все JEP со статусом; авторитетное описание каждой возможности |
| Страницы релизов JDK ([JDK 22](https://openjdk.org/projects/jdk/22/), [23](https://openjdk.org/projects/jdk/23/), [24](https://openjdk.org/projects/jdk/24/), [25](https://openjdk.org/projects/jdk/25/), [26](https://openjdk.org/projects/jdk/26/), [27](https://openjdk.org/projects/jdk/27/)) | OpenJDK | Списки возможностей и расписания по релизам; список чтения, заменяющий книги «нового издания» |
| Ключевые JEP после 21: [444 Virtual Threads](https://openjdk.org/jeps/444), [491 Synchronize VTs without pinning](https://openjdk.org/jeps/491), [505](https://openjdk.org/jeps/505) / [525 Structured Concurrency](https://openjdk.org/jeps/525), [506 Scoped Values](https://openjdk.org/jeps/506), [512 Compact Source Files](https://openjdk.org/jeps/512), [513 Flexible Constructor Bodies](https://openjdk.org/jeps/513), [519 Compact Object Headers](https://openjdk.org/jeps/519), [483](https://openjdk.org/jeps/483) / [514](https://openjdk.org/jeps/514) / [515 AOT method profiling](https://openjdk.org/jeps/515), [490 ZGC non-generational removed](https://openjdk.org/jeps/490), [500 Final means final](https://openjdk.org/jeps/500), [502 Stable Values](https://openjdk.org/jeps/502) / [526 Lazy Constants](https://openjdk.org/jeps/526), [530 Primitive patterns](https://openjdk.org/jeps/530), [401 Value Objects](https://openjdk.org/jeps/401) | OpenJDK | У каждого JEP есть мотивация, альтернативы и примеры. Разделы «Motivation» — лучшие эссе о дизайне современного Java. Заголовки проверены |
| Java Almanac ([javaalmanac.io](https://javaalmanac.io/)) | Marc Hoffmann и др. | Разница API между любыми двумя версиями Java, таблицы возможностей, запускаемые примеры |
| Списки рассылки OpenJDK ([индекс](https://mail.openjdk.org/mailman/listinfo)) | OpenJDK | amber-dev, valhalla-spec-experts, loom-dev: место, где спорят о решениях |
| Руководства Amber по `var` ([style guide](https://openjdk.org/projects/amber/guides/lvti-style-guide), [FAQ](https://openjdk.org/projects/amber/guides/lvti-faq)) | Stuart Marks | Официальные рекомендации по стилю |
| Brian Goetz на inside.java ([страница автора](https://inside.java/u/BrianGoetz)) и документы дизайна ([cr.openjdk.org](https://cr.openjdk.org/~briangoetz/)) | Goetz | DOP, pattern matching, Valhalla, carrier-классы. Страница автора `[не проверено]`, cr.openjdk.org проверен |
| Блог Mark Reinhold ([mreinhold.org](https://mreinhold.org/blog/)) | Reinhold | Эссе о модели релизов и стратегии платформы (в основном старые) |
| Java Magazine ([блоги Oracle](https://blogs.oracle.com/javamagazine/)) | Oracle | Статьи, викторины, рецензии на книги (например, рецензия на Core Java 12-е изд.) `[не проверено]` (403 скриптам) |
| Блог Spring ([spring.io/blog](https://spring.io/blog)) | Команда Spring | Заметки о релизах, «This Week in Spring», A Bootiful Podcast |
| Блог Quarkus ([quarkus.io/blog](https://quarkus.io/blog/)) | Команда Quarkus | Заметки о релизах и статьи о производительности и native-image |
| Документация OpenRewrite ([docs](https://docs.openrewrite.org/)) | Moderne | Рецепты автоматической миграции (Java 8→21/25, Spring Boot 2→3→4, JUnit 4→5) |

## Независимые блоги и серии статей

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| Vlad Mihalcea ([блог](https://vladmihalcea.com/), [руководства по Hibernate](https://vladmihalcea.com/tutorials/hibernate/)) | Vlad Mihalcea | Сотни измеренных статей о маппингах JPA/Hibernate, batching, N+1, блокировках, SQL |
| Thorben Janssen ([сайт](https://thorben-janssen.com/)) | Thorben Janssen | Советы по производительности JPA/Hibernate и Spring Data |
| Руководства Marco Behler ([guides](https://www.marcobehler.com/guides), [YouTube](https://www.youtube.com/@MarcoBehler)) | Marco Behler | Длинные практические руководства (Spring, транзакции, JDBC, сборка, версии Java) |
| nipafx ([nipafx.dev](https://nipafx.dev/), [YouTube](https://www.youtube.com/@nipafx)) | Nicolai Parlog | Возможности современного Java в глубину; ведущий Inside Java Newscast |
| Aleksey Shipilev ([shipilev.net](https://shipilev.net/)) | Shipilev | Производительность JVM, JMM, GC (Shenandoah), доклады о бенчмаркинге со слайдами и видео |
| Gunnar Morling ([morling.dev](https://www.morling.dev/), [1BRC](https://github.com/gunnarmorling/1brc)) | Gunnar Morling | One Billion Row Challenge: коллективный каталог экстремальных оптимизаций Java (SWAR, Unsafe/FFM, memory-mapped I/O, парсинг без ветвлений) |
| JavaSpecialists ([сайт](https://www.javaspecialists.eu/)) | Heinz Kabutz | Давняя рассылка для продвинутых |
| Stuart Marks ([блог](https://stuartmarks.wordpress.com/)) | Stuart Marks | Коллекции, дизайн API, депрекация |
| Baeldung ([сайт](https://www.baeldung.com/), [Java-учебники](https://www.baeldung.com/java-tutorial)) | Команда Baeldung | Быстрые рецепты по API, Spring и тестам; проверяйте версию Java в каждой статье |
| Foojay ([foojay.io](https://foojay.io/)) | Friends of OpenJDK | Статьи сообщества, сведения о дистрибутивах JDK, подкаст |
| Victor Rentea ([сайт](https://www.victorrentea.ro/)) | Victor Rentea | Чистый код, рефакторинг, прагматичная архитектура, обучение производительности Java |
| Venkat Subramaniam ([Agile Developer](https://agiledeveloper.com/)) | Venkat Subramaniam | Доклады и книги о FP и современном Java |
| Enterprise Craftsmanship ([блог](https://enterprisecraftsmanship.com/)) | Vladimir Khorikov | Модульные тесты, DDD, валидация, functional core (C#, но переносимо) |
| Mechanical Sympathy ([блог](https://mechanical-sympathy.blogspot.com/)) | Martin Thompson | Классика low-latency Java (false sharing, барьеры памяти, Disruptor). `[не проверено]` (blogspot недоступен из сети проверки); в основном 2011–2014 |
| Psychosomatic, Lobotomy, Saw ([блог](https://psy-lob-saw.blogspot.com/)) | Nitsan Wakart | Очереди, JMH, внутренности JCTools. `[не проверено]`; старое |
| Brendan Gregg ([сайт](https://www.brendangregg.com/)) | Brendan Gregg | Методология профилирования и flame graphs |
| Java Advent Calendar ([сайт](https://www.javaadvent.com/)) | сообщество, ежегодно | 24+ статьи каждый декабрь от Java Champions |
| «How NOT to Measure Latency» ([InfoQ](https://www.infoq.com/presentations/latency-response-time/)) | Gil Tene | Coordinated omission и перцентили; обязательно перед любым бенчмарком задержек |

## Отчёты об экосистеме и радары

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| InfoQ Java Trends Report 2025 ([InfoQ](https://www.infoq.com/articles/java-trends-report-2025/), [InfoQ Java](https://www.infoq.com/java/)) | Редакторы InfoQ, конец 2025 | Кривая внедрения: Java 25 как база, AI на JVM (Spring AI, LangChain4j, Embabel, Koog), OpenRewrite для модернизации, Jakarta EE 11. Издания 2026 к 2026-11-06 нет |
| JetBrains State of Java 2025 ([отчёт](https://lp.jetbrains.com/the-state-of-java-2025), [Dev Ecosystem 2025](https://www.jetbrains.com/lp/devecosystem-2025/), [блог](https://blog.jetbrains.com/research/2025/10/state-of-developer-ecosystem-2025/)) | JetBrains, октябрь 2025 | Версии, фреймворки (доминирует Spring; Quarkus, Ktor), инструменты. Выборка смещена к пользователям JetBrains |
| Azul 2026 State of Java Survey ([отчёт](https://www.azul.com/state-of-java-2026/), [пресс-релиз Azul](https://www.azul.com/newsroom/azul-2026-state-of-java-survey-report-62-of-enterprises-now-leverage-java-to-power-ai-functionality-41-rely-on-high-performance-java-platforms-to-reduce-cloud-compute-costs/)) | Azul, 10 февраля 2026 | Более 2000 респондентов: Java для AI 62%, миграция на OpenJDK, стоимость облака |
| New Relic 2024 State of the Java Ecosystem ([отчёт](https://newrelic.com/resources/report/2024-state-of-the-java-ecosystem), [пресс-релиз](https://newrelic.com/press-release/20240430)) | New Relic, апрель 2024 | Телеметрия продакшена: доли версий и вендоров, внедрение Java 21. Издания 2025 не найдено |
| Snyk JVM Ecosystem Report 2021 ([страница](https://snyk.io/jvm-ecosystem-report-2021/)) | Snyk | Последнее издание — 2021; URL теперь перенаправляет на общую страницу ресурсов. `[устарело]`: используйте JetBrains или Azul |
| Keyhole Software, «Java Trends of 2026» ([статья](https://keyholesoftware.com/java-trends-2026/)) | Keyhole, июнь 2026 | Вторичный синтез данных TIOBE, Stack Overflow и JetBrains |
| Thoughtworks Technology Radar ([языки и фреймворки](https://www.thoughtworks.com/radar/languages-and-frameworks)) | Thoughtworks | Сигналы adopt/trial/hold для фреймворков и инструментов JVM |

## Доклады и видеоканалы

**Ключевые выводы**

- **Каналы, на которые стоит подписаться:**
  - официальный канал **Java** (сессии JavaOne 2025/2026, трансляции релизов Java)
  - **Inside Java** (Newscast)
  - **Devoxx** (Devoxx Belgium/UK/France/Poland)
  - **Spring I/O**, SpringDeveloper, Quarkusio
  - **JavaZone** (Vimeo)
  - **jChampions Conference**
  - **InfoQ** (QCon)
  - **JUG Ru Group** (Joker/JPoint, на русском)
- **Вечнозелёные доклады, которые стоит посмотреть:**
  - измерение производительности: доклады Шипилёва о JMH, Gil Tene о задержках
  - дизайн Loom: Pressler и Bateman
  - направление языка: Goetz на JavaOne
  - доклады Шипилёва о JMM (русские версии самые подробные)
- Доклады JVMLS 2025 (heap flattening для Valhalla, «growing the language») — самое глубокое актуальное содержание. Ищите их через inside.java и канал Java.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| Канал Java на YouTube ([youtube.com/java](https://www.youtube.com/java), [@java](https://www.youtube.com/@java)) | Oracle | Сессии JavaOne 2025/2026, JVMLS, трансляции релизов |
| JavaOne 2026 ([страница dev.java](https://dev.java/community/javaone-2026/)) | Oracle, 17–19 марта 2026 | Каталог сессий; доклады постепенно выкладывают на канал Java |
| Inside Java на YouTube ([канал](https://www.youtube.com/@InsideJava)) | Oracle | Выпуски Newscast о новых JEP |
| Devoxx ([канал](https://www.youtube.com/@DevoxxForever), [devoxx.be](https://www.devoxx.be/)) | Devoxx | Крупнейший каталог докладов с Java-конференций |
| Spring I/O ([канал](https://www.youtube.com/@SpringIOConference), [сайт 2026](https://2026.springio.net/)) и SpringDeveloper ([канал](https://www.youtube.com/@SpringSourceDev)) | Сообщество Spring, Broadcom | Spring Boot 3/4, Spring AI, тестирование, observability |
| Quarkus ([канал](https://www.youtube.com/@QuarkusIO)) | Red Hat | Quarkus insights, native image |
| JavaZone ([Vimeo](https://vimeo.com/javazone), [сайт](https://www.javazone.no/)) | javaBin, Осло | Ежегодные доклады, записаны полностью |
| Jfokus ([сайт](https://www.jfokus.se/)) | Стокгольм | Доклады по ссылкам с сайта. Хэндл на YouTube не подтверждён |
| jChampions Conference ([сайт](https://jchampionsconf.com/), [канал](https://www.youtube.com/@JChampionsConf)) | Java Champions | Бесплатная онлайн-конференция, доклады Java Champions |
| InfoQ / QCon ([канал](https://www.youtube.com/@InfoQ)) | InfoQ | Доклады об архитектуре и JVM со стенограммами на infoq.com |
| «Benchmarking: The Lesser of Two Evils» (JMH) ([видео EN](https://www.youtube.com/watch?v=VaWgOCDBxYw), [слайды](https://shipilev.net/talks/devoxx-Nov2013-benchmarking.pdf), [видео RU](https://www.youtube.com/watch?v=Mw0Vimj39cI)) | Aleksey Shipilev, Devoxx 2013 | Почему наивные микробенчмарки Java лгут; дизайн JMH |
| «Benchmarking: Two Timestamps» ([видео RU, Joker 2014](https://www.youtube.com/watch?v=8pMfUopQ9Es), [слайды EN, JVMLS 2014](https://shipilev.net/talks/jvmls-July2014-benchmarking.pdf)) | Shipilev, 2014 | Таймеры, ловушки измерения задержек |
| «How NOT to Measure Latency» ([InfoQ](https://www.infoq.com/presentations/latency-response-time/), [Amara с субтитрами](https://amara.org/videos/0vqzbDPNIIbE/info/how-not-to-measure-latency-by-gil-tene/)) | Gil Tene, QCon SF 2013 | Coordinated omission; перцентили; HdrHistogram |
| «Java Memory Model: Close Encounters» ([видео RU](https://www.youtube.com/watch?v=C6b_dFtujKo), [слайды](https://shipilev.net/talks/joker-Oct2016-jmm-close-encounters.pdf)) | Shipilev, Joker 2016 | JMM на практике |
| Ключевой доклад «Performance» ([видео RU](https://www.youtube.com/watch?v=v6tJ3MTnLkc), [стенограмма на Habr](https://habr.com/ru/companies/jugru/articles/338732/)) | Shipilev, JPoint 2017 | Когда и как оптимизировать в больших проектах |
| «Performance Optimization 101» ([видео RU](https://www.youtube.com/watch?v=vF0lNnKCQM8)) | Shipilev, 2012 | Основы методологии производительности |
| «java.lang.String Catechism» ([видео RU](https://www.youtube.com/watch?v=SZFe3m1DV1A)) | Shipilev, JPoint 2015 | Устройство строк и их производительность |
| «Shenandoah GC» ([видео RU](https://www.youtube.com/watch?v=CnRtbtis79U)) | Shipilev, JPoint 2017 | Дизайн конкурентного уплотняющего GC |
| «The Age of Virtual Threads» ([inside.java](https://inside.java/2022/10/13/the-age-of-virtual-threads)) | Ron Pressler, Alan Bateman, Devoxx 2022 | Обоснование дизайна Loom |
| «Virtual Threads for Lightweight Concurrency» ([InfoQ](https://www.infoq.com/presentations/virtual-threads-lightweight-concurrency)) | Ron Pressler, QCon | Глубокий разбор Loom со стенограммой |
| «Where Is the Java Language Going?» ([inside.java](https://inside.java/2025/04/20/javaone-future-java/)) | Brian Goetz, JavaOne 2025 | Планы Amber и Valhalla от архитектора языка |
| Индекс докладов Valhalla ([тег inside.java](https://inside.java/tag/valhalla)) | Goetz и др. | «Valhalla – Java's Epic Refactor» (декабрь 2024) и более поздние обновления |
| JVMLS 2025 ([обзор](https://inside.java/2025/07/01/jvmls2025/), [программа](https://openjdk.org/projects/mlvm/jvmlangsummit/agenda.html)) | OpenJDK, август 2025 | Heap flattening для value-классов, развитие языка, marshalling. Программа `[не проверено]` |
| Доклады Victor Rentea ([сайт со ссылками на плейлисты](https://www.victorrentea.ro/)) | Victor Rentea | Чистый код, рефакторинг, прагматичная архитектура, производительность (любимые на Devoxx). Точные URL на YouTube не проверены |
| Доклады Venkat Subramaniam ([сайт](https://agiledeveloper.com/)) | Venkat Subramaniam | FP на Java, streams, дизайн |

## Подкасты и рассылки

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| Inside Java Podcast и Newscast ([inside.java/podcast](https://inside.java/podcast/)) | Oracle (Nicolai Parlog, Billy Korando и др.) | С февраля 2026: «Ask the Architects» в длинном формате плюс Newscast в аудио; выпуски о Valhalla, G1, изменении final-полей, JavaOne 2026 |
| airhacks.fm ([сайт](https://airhacks.fm/)) | Adam Bien | Еженедельные интервью о Java, Jakarta EE, MicroProfile, serverless, производительности |
| Foojay Podcast ([страница](https://foojay.io/today/category/podcast/)) | Foojay / Frank Delporte | Интервью с сообществом |
| Java Pub House ([сайт](https://javapubhouse.libsyn.com/)) | Freddy Guime, Bob Paulin | Давний подкаст о Java; проверьте актуальность |
| A Bootiful Podcast ([блог Spring](https://spring.io/blog)) | Josh Long | Интервью об экосистеме Spring (например, с автором «Testing Spring Boot Applications») |
| JVM Weekly ([Substack](https://www.jvm-weekly.com/)) | Artur Skowroński | Еженедельный дайджест экосистемы JVM |
| Рассылка JavaSpecialists ([архив](https://www.javaspecialists.eu/archive/)) | Heinz Kabutz | Советы для продвинутых |
| Inside Java Newsletter | Oracle | Ежемесячная сводка (выпуски публикуют на inside.java, например [август 2025](https://inside.java/2025/08/11/inside-java-newsletter)). `[не проверено]`: стабильного URL индекса нет, `inside.java/newsletter/` отдаёт 404 |

## Подборки и курсы

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| awesome-java ([GitHub](https://github.com/akullpp/awesome-java)) | akullpp и сообщество | Главный курируемый список библиотек, фреймворков и инструментов Java |
| useful-java-links ([GitHub](https://github.com/Vedenin/useful-java-links)) | Vedenin | Большой список библиотек и ресурсов по категориям (есть русская версия). Частично устарел |
| Java Programming MOOC ([java-programming.mooc.fi](https://java-programming.mooc.fi/)) | Университет Хельсинки | Бесплатный строгий курс для начинающих с множеством упражнений и автоматическими тестами |
| Hyperskill / JetBrains Academy ([hyperskill.org](https://hyperskill.org/courses), [JetBrains Academy](https://www.jetbrains.com/academy/)) | JetBrains | Проектные треки, встроенные в IntelliJ; платные тарифы |
| Algorithms I/II (Coursera, Princeton) | Sedgewick, Wayne | См. [Безопасность и алгоритмы](#безопасность-и-алгоритмы) |
| dev.java Learn | Oracle | См. [Официальные источники и OpenJDK](#официальные-источники-и-openjdk) |
| Воркшоп jvmperf | сообщество | См. [Производительность, настройка JVM и бенчмарки](#производительность-настройка-jvm-и-бенчмарки) |
| Java Almanac | сообщество | См. [Официальные источники и OpenJDK](#официальные-источники-и-openjdk) |

## Русскоязычные ресурсы

**Ключевые выводы**

- **Переводные книги** (подтверждены каталогами или листингами магазинов, `[не проверено]`). Переводы отстают на 1–3 года, а читатели на LiveLib критикуют качество старого перевода *Effective Java*. По возможности читайте английский оригинал, переводы используйте для знакомства. Доступны:
  - *Effective Java* 3-е изд. (Диалектика, 2019; допечатка 2022)
  - *Java Concurrency in Practice* (Питер, 2020)
  - *Java Performance* 2-е изд. Oaks (Питер, 2021, под названием «Эффективный Java. Тюнинг кода на Java 8, 11 и дальше»)
  - *Unit Testing* Khorikov (Питер, 2021)
  - *100 Java Mistakes* Valeev (Эксмо, 2025)
- **Доклады на русском.** Русские доклады Шипилёва (JMH, JMM, строки, Shenandoah, методология производительности) часто подробнее английских версий. Канал JUG Ru Group и архивы Joker/JPoint содержат более десяти лет докладов о Java. Статьи Tagir Valeev на Habr и его доклады-головоломки высокого качества.
- **Сообщество.** Сообщество Spring АйО (блог компании на Habr, Telegram, подкаст) — активное русскоязычное сообщество Spring/Java в 2025–2026 годах. «Разбор полётов» был влиятельным русским подкастом о Java в 2010-х. Актуальные выпуски подтвердить не удалось, поэтому как актуальный его не рекомендуют.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| «Java. Эффективное программирование», 3-е изд. ([страница издательства «Вильямс»](http://www.williamspublishing.com/Books/978-5-6041394-4-8.html), тираж закончился) | Блох, Диалектика, 2019 (ISBN 978-5-6041394-4-8; допечатка 2022) | Перевод *Effective Java* 3-е изд. `[не проверено]`; качество перевода критикуют (отзывы читателей на LiveLib; сайт закрыт от автоматических проверок `[не проверено]`) |
| «Java Concurrency на практике» ([страница издательства «Питер»](https://www.piter.com/product/java-concurrency-na-praktike)) | Гетц и др., Питер, 2020, 464 с., ISBN 978-5-4461-1314-9 | Перевод JCiP `[не проверено]` |
| «Эффективный Java. Тюнинг кода на Java 8, 11 и дальше» ([Яндекс Книги](https://books.yandex.ru/books/iocvAl7g/read-online)) | Скотт Оукс, Питер, 2021 | Перевод *Java Performance* 2-е изд. `[не проверено]` |
| «Принципы юнит-тестирования» ([Литрес](https://litres.com/book/vladimir-horikov/principy-unit-testirovaniya-pdf-epub-64083637/)) | Владимир Хориков, Питер, 2021 | Перевод *Unit Testing Principles, Practices, and Patterns* `[не проверено]` |
| «100 ошибок Java и как их избежать» ([листинг](https://sovabooks.pl/en/products/100-oshibok-java-i-kak-ih-izbezhat-valeev-t-sovabooks-ru-2025-eksmo)) | Тагир Валеев, Эксмо, 2025, 480 с. | Перевод *100 Java Mistakes* `[не проверено]` (единственный листинг магазина) |
| «Современный язык Java» | Урма, Фуско, Майкрофт | Перевод *Modern Java in Action* `[не проверено]`; издатель и год не подтверждены |
| Издательство «Питер» ([piter.com](https://www.piter.com/)) | Piter | Основной издатель переводных книг по Java и архитектуре (Clean Architecture, DDD, Spring) |
| JUG Ru Group на YouTube ([@JUGRuGroup](https://www.youtube.com/@JUGRuGroup), [c/JUGru](https://www.youtube.com/c/JUGru)) | JUG Ru Group | Доклады Joker, JPoint и Heisenbug |
| Конференция Joker ([сайт](https://jokerconf.com/), [архив](https://jokerconf.com/archive/)) | JUG Ru Group, Санкт-Петербург, с 2013; Joker 2026 прошёл 14–15 октября 2026 | Архив программ и записей |
| Конференция JPoint ([сайт](https://jpoint.ru/)) и JUG Ru Group ([jugru.org](https://jugru.org/)) | JUG Ru Group | Весенняя Java-конференция; календарь событий |
| Русские доклады Шипилёва | Алексей Шипилёв | JMH, JMM, строки, Shenandoah (ссылки в разделе [Доклады и видеоканалы](#доклады-и-видеоканалы)) |
| «Перформанс: что в имени тебе моём?» ([Habr](https://habr.com/ru/companies/jugru/articles/338732/)) | Шипилёв, стенограмма JPoint 2017 | Текст ключевого доклада о производительности |
| Тагир Валеев на Habr ([статьи](https://habr.com/ru/users/tagir_valeev/articles/)) | Tagir Valeev | Вывод типов, внутреннее устройство Stream API, головоломки, статический анализ (в списке 86 статей) |
| Блог JUG Ru Group на Habr ([статьи](https://habr.com/ru/companies/jugru/articles/)) | JUG Ru Group | Стенограммы докладов и интервью с экспертами по Java |
| Хаб Java на Habr ([статьи](https://habr.com/ru/hubs/java/articles/)) | сообщество | Новости и переводы; качество разное |
| Spring АйО ([Habr](https://habr.com/ru/companies/spring_aio/articles/), [Telegram](https://t.me/spring_aio), [выпуск подкаста 26](https://habr.com/ru/articles/927978/)) | Сообщество Spring АйО (с 2024) | Русскоязычные новости Spring Boot, Hibernate и Java, переводы, подкаст |
| Stepik «Java. Базовый курс» ([курс](https://stepik.org/course/187/promo)) | Stepik | Бесплатный русский курс для начинающих |
| JavaRush ([javarush.com](https://javarush.com/)) | JavaRush | Игровой русский курс для начинающих (платные тарифы); только начальный уровень |
| Подкаст «Разбор полётов» ([Habr, вып. 17, 2012](https://habr.com/en/articles/143726/)) | Barukh/Abashev/Tsyhankov и др. | Исторический русский подкаст о Java. `[устарело]`: актуальных выпусков не подтверждено |

## Маршрут чтения по уровням

**Принципы**

1. Одна книга по основам, затем *Effective Java*, затем JEP 22–27, затем специализация.
2. К каждой книге до 2021 года берите парный современный источник: JCiP с *Modern Concurrency in Java*, *Effective Java* со статьёй о DOP, *Java Performance* с Beckwith и руководством по GC для JDK 25.
3. Для работы над производительностью сначала учитесь измерять (JMH, задержки), потом оптимизировать.

### Начинающий (0–1 год в Java)

1. *Head First Java* 3-е изд. (или бесплатный MOOC.fi Java Programming). Русская альтернатива: Stepik «Java. Базовый курс».
2. dev.java/learn: от «Your First Steps» до «Mastering the API».
3. *Learning Java* 6-е изд. (Java 21, виртуальные потоки) или, для более глубокого чтения, *Core Java* Vol I 13-е изд.
4. *Java by Comparison* для формирования привычек.
5. *Pragmatic Unit Testing in Java with JUnit* 3-е изд.
6. Sedgewick *Algorithms* и параллельно курс Coursera Part I.
7. Просмотрите Google Java Style Guide; с первого дня используйте инспекции IntelliJ и Error Prone или SpotBugs.

### Средний (1–3 года)

1. *Effective Java* 3-е изд. от корки до корки.
2. *100 Java Mistakes and How to Avoid Them*.
3. *Core Java for the Impatient* 4-е изд. или *Core Java* Vol II для API, которые вы пропустили.
4. «Data-Oriented Programming in Java» Goetz плюс DOP v1.1. Затем *Data-Oriented Programming in Java* Kiehl, когда её закончат (сейчас MEAP).
5. *Modern Java in Action* (streams, `CompletableFuture`) или *Functional Programming in Java* 2-е изд. Subramaniam.
6. *The Well-Grounded Java Developer* 2-е изд. (модули, байт-код, сборка, контейнеры).
7. *Effective Software Testing* (Aniche), затем *Unit Testing Principles* Khorikov.
8. *A Philosophy of Software Design*, а также *Refactoring* 2-е изд. и *Tidy First?*.
9. Трек фреймворка:
   - Spring: *Spring Start Here* (2-е изд., когда выйдет), затем *Spring Security in Action* 2-е изд., затем блог Mihalcea, затем *Java Persistence with Spring Data and Hibernate*.
   - Quarkus: *Quarkus in Action*.

### Продвинутый (3+ года; параллелизм и JVM)

1. *Java Concurrency in Practice*, затем *Modern Concurrency in Java* (Rahman, 2025), затем JEP 444, 491, 505/525 и 506.
2. Шипилёв: «JMM Pragmatics», «Close Encounters of the JMM Kind» и примеры jcstress.
3. *JVM Performance Engineering* (Beckwith, 2024).
4. Уровень байт-кода: спецификация JVM (главы 2–4, 6) и эксперименты с JITWatch.
5. *Troubleshooting Java* 2-е изд. (дампы кучи и потоков, профилировщики).
6. *Working Effectively with Legacy Code* и OpenRewrite для реальных миграций (8→21→25).

### Специалист по производительности

1. Сначала измерение: доклады Шипилёва «Lesser of Two Evils» и «Two Timestamps», «Nanotrusting the Nanotime», примеры JMH и «How NOT to Measure Latency» Gil Tene с HdrHistogram.
2. *Java Performance* 2-е изд. (Oaks): инструменты и методология.
3. *Optimizing Cloud Native Java* (Evans и Gough, 2024): контейнеры и observability.
4. *The Garbage Collection Handbook* 2-е изд., затем Java SE 25 GC Tuning Guide, затем JEP о generational ZGC, compact object headers (519) и AOT-кэше (483/514/515).
5. JVM Anatomy Quarks (все 30), Brendan Gregg о flame graphs и воркшоп jvmperf.
6. *High-Performance Java Persistence* (основная часть реальных задержек сидит в доступе к данным).
7. Репозиторий 1BRC и разборы Morling: как выглядит экстремальная оптимизация и чем она оборачивается для читаемости.
8. Быть в курсе: inside.java, доклады JVMLS, JavaSpecialists, рассылки hotspot-dev и loom-dev.

### Архитектор

1. *A Philosophy of Software Design*, затем *Clean Architecture* (критически), затем *Get Your Hands Dirty on Clean Architecture* 2-е изд.
2. *Learning Domain-Driven Design* (Khononov), затем *Domain Modeling Made Functional* (переводите в records и sealed-типы), затем Evans, затем Vernon. На воркшопах используйте шаблоны DDD Crew.
3. *Fundamentals of Software Architecture* 2-е изд. (2025), затем *Software Architecture: The Hard Parts*.
4. *Designing Data-Intensive Applications* 2-е изд. (2026).
5. *Building Microservices* 2-е изд., затем *Microservices Patterns* (2-е изд., когда выйдет) и microservices.io. Затем *Release It!* 2-е изд. для паттернов устойчивости.
6. *Patterns of Enterprise Application Architecture* и *Enterprise Integration Patterns* ради словаря.
7. *Secure by Design* плюс OWASP Cheat Sheets и Oracle Secure Coding Guidelines.
8. *Architecture Modernization* (Tune) для legacy-ландшафтов.
9. Раз в год: InfoQ Java Trends, JetBrains State of Java, Azul State of Java, Thoughtworks Radar.

## Пробелы и непроверенное

- **Страницы O'Reilly и Packt** не открылись скриптами (HTTP 403). Их ссылки взяты из поисковой выдачи `[не проверено]`. Месяцы публикации нескольких книг расходятся на 1–3 месяца между издателем и продавцами (*Core Java* 13-е изд., *Clean Code* 2-е изд., *DDIA* 2-е изд., *Modern Concurrency in Java*). В тексте указан диапазон.
- ***Data-Oriented Programming in Java* (Kiehl):** Manning указывает MEAP с ожидаемым выходом в ноябре 2026; продавцы — 7 октября 2026. Перед тем как называть книгу изданной, проверьте.
- **Страница Manning *Testing Spring Boot Applications*** отдала 404 скриптам 2026-11-06, хотя поиск и подкаст Spring подтверждают MEAP. Возможно, изменился slug URL.
- **Не найдено, не цитировать:** *Spring in Action* 7-е изд., *Effective Java* 4-е изд., JCiP 2-е изд., *Modern Java in Action* 3-е изд., InfoQ Java Trends 2026, отчёт New Relic 2025.
- **Неподтверждённые URL YouTube:**
  - Хэндлы Jfokus и JavaZone на YouTube не подтверждены. Видео JavaZone подтверждены на Vimeo.
  - Отдельные ссылки на доклады Goetz (JavaOne 2025) и Pressler (Devoxx 2022) ведут на страницы inside.java, в которые встроено видео.
  - Точные URL на YouTube для докладов Victor Rentea и Kevlin Henney не проверены.
- **Непроверенные руководства по Java 25:** «OCP Java SE 25 Developer Study Guide» (Selikoff) и другие книги по Java 25 появляются только в списках новинок Amazon. Не проверено.
- **Русские переводы:** издатель и год подтверждены только отдельными записями каталогов. Издатель «Современный язык Java» не подтверждён. Русского перевода *Spring Start Here* не найдено.
- **Блоги на Blogspot** (Mechanical Sympathy, Psychosomatic Lobotomy Saw) недоступны из сети проверки. Их URL давние, но помечены `[не проверено]`.
