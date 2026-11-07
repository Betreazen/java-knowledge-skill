# Сервер Minecraft: плагины Paper, Folia, Velocity, датапаки

Справочник по серверной стороне Minecraft Java Edition: всё, что работает на сервере или перед ним, а не внутри загрузчика клиентских модов. Тут выбор между датапаком, плагином, серверным и клиентским модом; Paper после жёсткого форка и его современный API (`paper-plugin.yml`, bootstrapper и loader, команды Brigadier, Lifecycle, изменение реестров, компоненты данных, Adventure и MiniMessage, PDC, планировщики); многопоточность Folia; остальное серверное ПО (Spigot, Purpur, Pufferfish, Leaf, Sponge, Minestom, серверы на Fabric и NeoForge, Polymer); прокси (Velocity, BungeeCord, Gate); Bedrock и совместимость версий (Geyser, Floodgate, ViaVersion); датапаки эпохи 26.x; инженерия плагина (зависимости, конфигурация, БД, тесты, производительность, безопасность, несколько версий Minecraft); проверенные в CI шаблоны и чеклист ревью.

Актуально на ноябрь 2026. Minecraft Java 26.3 (выпуск 2026-09-15), Paper 26.3 build 244, Velocity 4.3.1, Folia 26.3 build 6 (ALPHA), JDK 25 (LTS, минимум для Minecraft 26.1+), JDK 27 (последний выпуск). Для последней линии 1.21.x (1.21.11) нужен Java 21; она разобрана там, где старые руководства всё ещё на неё нацелены. Примеры охватывают разные жанры: мини-игры и лобби, выживание и экономика SMP, RPG и кастомные предметы, творческие инструменты, модерация и античит, управление мирами, сети серверов.

Пометки: `[не проверено]` — факт из одного вторичного источника или страница недоступна для чтения; `[устарело]` — рядом названа замена; `[CI]` — проверено в CI на GitHub Actions. Строки без пометки прочитаны в первоисточнике или в метаданных. Соседние файлы: моды и загрузчики — [minecraft-modding.md](minecraft-modding.md); API игры для модов — [minecraft-mod-api.md](minecraft-mod-api.md); измерение и профилирование — [minecraft-performance-testing.md](minecraft-performance-testing.md); документация, книги и сообщества — [minecraft-resources.md](minecraft-resources.md); общий инструментарий Java — [toolchain.md](toolchain.md); тесты — [testing.md](testing.md); потоки и FFM — [concurrency-native.md](concurrency-native.md); серверная Java вообще — [domains-server.md](domains-server.md).

- [Версии на ноябрь 2026](#версии-на-ноябрь-2026)
- [Что выбрать: датапак, плагин или мод](#что-выбрать-датапак-плагин-или-мод)
- [1. Paper в 2026: жёсткий форк, версии, runtime без обфускации](#1-paper-в-2026-жёсткий-форк-версии-runtime-без-обфускации)
- [2. Структура проекта Paper: descriptor, bootstrapper, loader, библиотеки](#2-структура-проекта-paper-descriptor-bootstrapper-loader-библиотеки)
- [3. paperweight-userdev, доступ к внутренностям, run-paper](#3-paperweight-userdev-доступ-к-внутренностям-run-paper)
- [4. Современный API Paper (26.x)](#4-современный-api-paper-26x)
- [5. Потоки: планировщики Paper и Folia](#5-потоки-планировщики-paper-и-folia)
- [6. Остальное серверное ПО: Spigot, Purpur, Leaf, Sponge, Minestom](#6-остальное-серверное-по-spigot-purpur-leaf-sponge-minestom)
- [7. Серверы на загрузчиках модов: Fabric, NeoForge, Polymer](#7-серверы-на-загрузчиках-модов-fabric-neoforge-polymer)
- [8. Прокси: Velocity, BungeeCord, Gate](#8-прокси-velocity-bungeecord-gate)
- [9. Bedrock и совместимость версий: Geyser, Floodgate, ViaVersion](#9-bedrock-и-совместимость-версий-geyser-floodgate-viaversion)
- [10. Датапаки в эпоху 26.x](#10-датапаки-в-эпоху-26x)
- [11. Инженерия плагина](#11-инженерия-плагина)
- [12. Публикация и распространение](#12-публикация-и-распространение)
- [Шаблоны](#шаблоны)
- [Чеклист ревью плагина](#чеклист-ревью-плагина)
- [Открытые вопросы](#открытые-вопросы)

---

## Версии на ноябрь 2026

Версии проверены 2026-11-07 по манифесту Mojang, API Fill v3, maven-metadata и Gradle Plugin Portal.

| Что | Версия и дата | Примечание |
|---|---|---|
| Minecraft Java, последний выпуск | **26.3** (2026-09-15) | Манифест Mojang: `latest.release = 26.3`. В снапшотах 26.4 (`26.4-snapshot-7`, 2026-11-03). Схема года и дропа `год.дроп[.хотфикс]` действует с 26.1 |
| Minecraft 26.1 | 2026-03-24 | Первый выпуск на Java 25 и первый полностью без обфускации (обфусцированного jar нет) |
| Требование Java | **Java 25** для 26.1+; Java 21 для 1.20.5–1.21.11 | В Fill API `java.version.minimum` = 25 для Paper 26.x и 21 для 1.21.11 |
| Paper 26.3 | build **244** STABLE, 2026-11-06 | `support.status = SUPPORTED`, JAR `paper-26.3-244.jar` |
| Paper 26.2, 26.1.2, 1.21.11 | UNSUPPORTED с 2026-11-03, 2026-07-26, 2026-06-15 | Paper поддерживает только последнюю версию, исправления эксплойтов не переносятся назад |
| `io.papermc.paper:paper-api` | `26.3.build.244-stable` (2026-11-06) | Схема с 26.1: `<mc>.build.<n>-<alpha\|beta\|stable>`. До 1.21.11 было `<mc>-R0.1-SNAPSHOT`. В Gradle: `26.3.build.+` |
| Странный артефакт в metadata | `26.3-pre-2.build.0-alpha` указан как `<latest>` | Не разрешайте через `latest.release` или `+`, закрепляйте префикс `26.3.build.` |
| paperweight-userdev | **2.0.0-beta.24** (2026-09-25) | Нужен **Gradle 9.7.1+**, работает на Java 21+; реобфускация в маппинги Spigot для 26.1+ мертва |
| Gradle | 9.9.0 (2026-11-06) | Пример paperweight-test-plugin застрял на wrapper 9.4.1 и userdev beta.21 |
| run-paper, run-velocity, run-waterfall | **3.1.0** (2026-08-08) | Плагин `xyz.jpenilla.run-paper` |
| resource-factory | 1.4.0 | Генератор `plugin.yml` и `paper-plugin.yml`: `xyz.jpenilla.resource-factory-paper-convention` или `-bukkit-convention` |
| plugin-yml (Eldoria) | 0.9.0 | Альтернатива: `de.eldoria.plugin-yml.paper` |
| Shadow | `com.gradleup.shadow` **9.6.1** | Старый id `com.github.johnrengelman.shadow` `[устарело]`, замена — `com.gradleup.shadow` |
| Folia 26.3 | build **6**, канал **ALPHA** (2026-10-29) | `dev.folia:folia-api:26.3.build.6-alpha`; 26.2 дошла до beta |
| Velocity | **4.3.1** STABLE (2026-10-27, исправление безопасности для legacy plugin message) | Минимум **Java 25** с 4.0.0 (2026-07-14, Adventure 5) |
| Velocity 3.5.1 | DEPRECATED, конец 2026-09-30 | Последняя линия на Java 21 |
| `com.velocitypowered:velocity-api` | 4.3.1 (4.3.2-SNAPSHOT в работе) | Процессор аннотаций генерирует `velocity-plugin.json` |
| Waterfall | EOL объявлен 2024-03-26, последняя линия «1.21», в Fill UNSUPPORTED | Иногда появляются коммиты (2026-09), поддержки нет |
| BungeeCord | активен, сборка Jenkins b2112 (2026-11-06) | Апстрим md_5, поддерживает клиентов 26.x |
| Adventure | **5.2.0** (Maven Central) | Paper 26.2+ и Velocity 4.0+ поставляют Adventure 5 (Java 21+, JSpecify, sealed-типы) |
| Spigot | 26.3 через BuildTools (`hub.spigotmc.org/versions/26.3.json`, build 4664) | `spigot-api:26.3-R0.1-SNAPSHOT`, схема `-R0.1-SNAPSHOT` сохранена |
| Purpur | 26.3, build 2657 | `api.purpurmc.org/v2/purpur`: `current = 26.3` |
| Leaf | релиз `ver-26.3` (2026-11-05) | Форк на базе Gale и Paper, нацеленный на производительность |
| Pufferfish | последний push 2025-12-21 (`ver/1.21`), ветки 26.x нет | Для 26.x фактически остановлен |
| Sponge | API **17.0.0** stable (2026-01-11, MC 1.21.10); API 18–21 SNAPSHOT для 1.21.11, 26.1.2, 26.2, 26.3 | Ветки `api-18`…`api-21` |
| Minestom | `net.minestom:minestom:2026.10.07-26.2` (Java 25); ветка `26_3` в работе | Версии по датам `YYYY.MM.DD-<mc>` |
| Polymer (Fabric) | 0.18.7+26.3 (2026-11-03) | Серверное кастомное содержимое для ванильных клиентов |
| Geyser | 2.12.1 build 1295 (2026-11-07) | Java 26.3; Bedrock 26.40–26.62 |
| Floodgate | 2.2.5 build 141 (2026-09-17) | «Update to 26.3» |
| ViaVersion, ViaBackwards | 5.12.2 (2026-10-27) | Покрывает 26.3 и 26.2 |
| Gate (прокси на Go) | v0.76.2 (2026-11-04) | |
| MockBukkit | `org.mockbukkit.mockbukkit:mockbukkit-v26.3:4.119.0` (2026-10-28) | Отдельный artifact id на версию MC (`-v1.21`, `-v26.1.2`, `-v26.2`, `-v26.3`) |
| Формат датапака | 26.3 = **121.0**; 26.2 = 107.1; 26.1 = 101.1; 1.21.11 = 94.1; 1.21.9–10 = 88.0 | `min_format` и `max_format` (major.minor) с 25w31a (1.21.9) |
| Формат ресурспака | 26.3 = **97.1**; 26.2 = 88.0; 26.1 = 84.0; 1.21.11 = 75.0 | |

---

## Что выбрать: датапак, плагин или мод

**Ключевые выводы**

Таблица решений идёт первой. Читать её по строке «что нужно».

| Нужно | Датапак | Плагин Paper | Серверный мод (Fabric/NeoForge) | Клиентский мод | Пример |
|---|---|---|---|---|---|
| Свой рецепт, лут, достижение | Лучший вариант | Возможно (Recipe API) | Возможно | Нет | ребаланс крафта для SMP |
| Новая генерация мира (биом, структура) | Лучший вариант | Встроенный датапак в JAR | Возможно | Нет | измерения для приключенческой карты |
| Свои чары, видимые в ванильном UI | Лучший вариант (данные с 1.21) | Registry API в bootstrap (плагин Paper) | Возможно | Нет | RPG-чары «вампиризм» |
| Предмет с собственной моделью | Датапак и ресурспак (`item_model`) | Плагин и ресурспак (API компонентов данных) | Polymer и ресурспак | Полностью | оружие и косметика для RPG |
| Новый блок с логикой блок-сущности | Только эмуляция | Эмуляция (display-сущности, состояния note block) | Polymer (настоящий реестр, отображается как ванильный блок) | Полностью | машины и автоматизация |
| Экономика, SQL, веб-API | Нет | Лучший вариант | Возможно | Нет | магазин и аукцион, мост в Discord |
| Отмена и изменение событий по игроку | Ограниченно (предикаты) | Лучший вариант | Миксины и события | Нет | приваты, защита от грифинга |
| Чат и очередь между серверами | Нет | Частично (plugin messaging) | Нет | Нет | плагин Velocity |
| Новый HUD, шейдер, привязка клавиш | Нет (кроме core-шейдеров в ресурспаке и `/posteffect` в 26.3 для постэффектов из ресурспака) | Нет | Нет | Лучший вариант | миникарты, шейдерпаки |
| Изменение производительности тиков | Нет | Ограниченно | Лучший вариант (в духе Lithium) или форк сервера | Моды производительности клиента | Lithium, Leaf |
| Полностью своя игра на сервере | Нет | Возможно, но идёт против ванили | Нет | Нет | лобби или мини-игра на Minestom |

- Главный вопрос: **нужны ли клиенту новые ассеты или новый код?** Ванильный клиент рисует только ванильные реестры плюс реестры на данных, которые присылает сервер (чары, биомы, типы урона, отделка, варианты картин, волков и кошек, диалоги), и ресурспаки. Новая клиентская отрисовка, привязки клавиш, виджеты GUI сверх ванильных контейнеров и диалогов или клиентская физика возможны только в **клиентском моде** (Fabric или NeoForge).
- Начиная с 1.20.5 (компоненты предметов) и 1.21.x (чары на данных, диалоги в 1.21.6, компонент `item_model`) «подделка» своего содержимого для ванильных клиентов стала намного мощнее. Свой предмет — это ванильный базовый предмет с `item_model` и компонентами. Свои блоки на серверах семейства Bukkit по-прежнему эмулируются (note block, блоки грибов, display-сущности), а **Polymer** (Fabric) даёт настоящие серверные блоки, отображаемые ванильными состояниями.
- **Датапак** выбирайте, когда логика помещается в команды, функции, предикаты, таблицы лута и JSON генерации мира; когда он должен работать на любом сервере (ваниль, Paper, Fabric, NeoForge); когда между версиями меняются только номера формата. Генерация мира (биомы, структуры, шум), рецепты, лут, достижения, чары, типы урона, отделка и (в 26.3) компоненты предметов `block_transformer`, `cooking_fuel`, `brewing_fuel`, `compostable` — территория датапаков.
- **Плагин Paper** выбирайте, когда нужны постоянное состояние (БД), сеть (HTTP, обмен с прокси), сложная логика по игроку, интеграция прав, экономика или перехват событий (отмена и изменение) на многопользовательском сервере с ванильными клиентами. Плагин может поставлять датапаки через `LifecycleEvents.DATAPACK_DISCOVERY` и регистрировать записи реестров (чары и другое) в bootstrap.
- **Серверный мод (Fabric, NeoForge, `environment: server`, Polymer)** выбирайте, когда нужно глубоко менять внутренности ванили (миксины), работать вместе со сборкой модов или добавлять настоящие записи реестров на модовом сервере. У серверов Fabric нет API Bukkit, поэтому экосистемы прав и экономики другие (fabric-permissions-api, LuckPerms для Fabric).
- **Плагин прокси (Velocity)** выбирайте, когда логика охватывает несколько серверов: общий чат, группы и очереди, режим обслуживания, выбор сервера, наказания на всю сеть.
- **Minestom** выбирайте для собственного сервера мини-игр или лобби с нуля, когда ванильный геймплей не нужен.
- Антипаттерн: писать плагин Bukkit, который эмулирует то, что теперь делает датапак (свое топливо и компост через компоненты 26.3, свои чары через реестр). Сначала проверьте последние возможности на данных.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [How do plugins work?](https://docs.papermc.io/paper/dev/how-do-plugins-work/) | PaperMC docs, 2026 | модель жизненного цикла плагина (загрузка, включение, выключение) |
| [Data pack](https://minecraft.wiki/w/Data_pack) | Minecraft Wiki, 2026 | возможности и структура датапака |
| [Polymer documentation](https://polymer.pb4.eu/) | Patbox, 2026 | настоящее серверное содержимое для ванильных клиентов |
| [Minestom wiki](https://minestom.net/docs/introduction) | Minestom, 2026 | модель сервера-библиотеки с нуля |
| [Why Velocity?](https://docs.papermc.io/velocity/why-velocity/) | PaperMC docs | зачем нужен прокси |

---

## 1. Paper в 2026: жёсткий форк, версии, runtime без обфускации

**Ключевые выводы**

- **Жёсткий форк объявлен 2024-12-13**, действует с 1.21.4: Paper перестал автоматически забирать патчи и API Spigot. Существующий API Bukkit и Spigot (включая deprecated) пока работает, но **новое API только Spigot после форка на Paper не гарантировано**. Доля Paper на свежих версиях около 85–90%, плагины только для Paper считаются нормой.
- Внутри: исходники ванили хранятся как патчи по файлам, API и реализация лежат обычными `.java`, используются маппинги Mojang плюс имена параметров Parchment, декомпилятор Vineflower. Старые ветки `ver/1.8.8`…`ver/1.21.3` перенесены в архив.
- **1.20.5**: runtime Paper стал **на маппингах Mojang**, CraftBukkit перестал переименовываться в `org.bukkit.craftbukkit.v1_XX_RY`. Плагины на маппингах Spigot ремапились при первой загрузке, плагины Paper (`paper-plugin.yml`) считаются написанными под Mojang.
- **26.1**: Mojang выпускает jar **без обфускации**, Paper полностью убрал внутренний ремаппер. Следствия: плагины с именами NMS по маппингам Spigot (`EntityHuman`, `PacketPlayIn…`) **не работают на 26.1+ ни на Paper, ни на Spigot**; `reobfJar` исчез из dev-бандлов 26.1+; рефлексия должна использовать имена Mojang. Путь проверки на 1.21.11 был `-Dpaper.disablePluginRemapping=true`.
- **Версионирование с 26.1**: артефакты `26.3.build.244-stable`. В Gradle: `compileOnly("io.papermc.paper:paper-api:26.3.build.+")`. Оставляйте `.build.` в префиксе (`26.1.+` может совпасть с `26.1.1…`). В Maven диапазон `[26.3.build,)`. Каналы выпуска: `alpha` (раньше experimental), `beta`, `stable` (раньше default).
- `api-version` в plugin.yml: с 1.20.5 допустимы минорные версии (`'1.21.11'`, `'26.3'`). Сервер старее заявленной версии откажется грузить плагин. Без `api-version` плагин считается legacy (шим Material, предупреждение).
- **Заметные изменения API по выпускам** (по новостям PaperMC):
  - 1.21.5–1.21.7: `/reload` объявлен deprecated на удаление (указывает на `/minecraft:reload`). `PlayerLoginEvent` deprecated, замена — `PlayerConnectionValidateLoginEvent` (срабатывает дважды, даёт `PlayerProfile`, а не `Player`) и `PlayerServerFullCheckEvent`. API фазы конфигурации (`AsyncPlayerConnectionConfigureEvent`, `PlayerConnectionReconfigureEvent`, `PlayerGameConnection#reenterConfiguration`). **Dialog API** (`ClickEvent.openDialog`, `DialogAction.customClick`, `PlayerCustomClickEvent`). ItemStack в конфиги пишутся как SNBT (с data fixer). Хранение «сырого» `ItemMeta` в конфигах не поддерживается, нужен `UnsafeValues#serializeStack`.
  - 1.21.11: игровые правила стали реестром (snake_case), используйте `GameRules`, а не имена `GameRule`. Методы WorldBorder в миллисекундах deprecated в пользу тиковых.
  - 26.1: мир хранится в `world/dimensions/minecraft/<dim>/…`. `WorldInfo#getName` устарел, используйте ключи. Появились `ClockTimeSkipEvent` и аргумент запуска `add-plugin-dir`.
  - 26.2: **Adventure 5** (Java 21+, sealed-типы, JSpecify; `BookMeta` больше не наследует `Book`; удалены deprecated-вызовы `ClickEvent` и `HoverEvent`). У кроватей пропали блок-сущности и PDC (миграция через `AsyncServerDataFixerRemoveBlockEntityEvent`). `MagmaCube` больше не наследует `Slime` (`AbstractCubeMob`). Добавлены `RegistryEvents.TRIM_MATERIAL` и `RegistryEvents.TRIM_PATTERN`.
  - 26.3: большая часть API компонентов данных больше не `@Experimental`. `PotionBrewer` deprecated (варка стала данными, замена — `BrewingRecipe`). `PlayerAnimationEvent` заменён на `PlayerArmSwingEvent`. Добавлены `Entity#getRemovalReason`, `EntityCollideWithEntityEvent`, `Player#postEffects`. Слияние конфигов (bukkit, spigot, paper YAML в один) запланировано на **26.4** (PaperMC/Paper, issue #14304).
- **Политика deprecation**: `@Deprecated(forRemoval = true)` плюс `@ApiStatus.ScheduledForRemoval(inVersion=…)`; удаления только в мажорных версиях MC. По дорожной карте `ItemStack` станет интерфейсом: используйте `ItemStack.of(Material)`, не наследуйте `ItemStack`, не кэшируйте NMS `ServerPlayer` через смену мира.
- Что неверно в старых руководствах: координаты `-R0.1-SNAPSHOT` для 26.x; версионированные пакеты CraftBukkit; `reobfJar`; `ChatColor` и строки с § вместо Adventure; `CommandExecutor` и команды в `plugin.yml` вместо Brigadier; `BukkitRunnable` повсюду (несовместим с Folia); `/reload` в цикле разработки; Timings (deprecated, spark встроен).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [The future of Paper - Hard fork](https://papermc.io/news/the-future-of-paper-hard-fork) | PaperMC, 2024-12-13 | официальные причины форка и влияние на разработчиков плагинов |
| [Paper 1.21.7 (covers 1.21.5–1.21.7)](https://papermc.io/news/1-21-7) | PaperMC, 2025-06-30 | deprecated `/reload`, события входа и фазы конфигурации, диалоги |
| [Paper 1.21.11](https://papermc.io/news/1-21-11) | PaperMC, 2025-12-21 | анонс отказа от обфускации, реестр игровых правил |
| [Paper 26.1](https://papermc.io/news/26-1) | PaperMC, 2026-04-26 | новое версионирование артефактов, удаление ремаппера, структура мира |
| [Paper 26.2](https://papermc.io/news/26-2) | PaperMC, 2026-06-12 | Adventure 5, миграция PDC кроватей |
| [Paper 26.3](https://papermc.io/news/26-3) | PaperMC, 2026-09-26 | последние добавления и deprecated в API, слияние конфигов в 26.4 |
| [Important dev PSA: Future removal of CB package relocation](https://papermc.io/news/important-dev-psa-future-removal-of-cb-package-relocation) | PaperMC, 2024-03-22 | почему версионированные пакеты `craftbukkit` исчезли |
| [Roadmap](https://docs.papermc.io/paper/dev/roadmap/) | PaperMC docs | ItemStack как интерфейс, политика deprecation |
| [Minecraft internals](https://docs.papermc.io/paper/dev/internals/) | PaperMC docs | рекомендации по NMS и рефлексии, runtime на маппингах Mojang |
| [PaperMC/Paper repository](https://github.com/PaperMC/Paper) | PaperMC | исходники, issues, история PR |
| [Paper downloads service (Fill v3)](https://docs.papermc.io/misc/downloads-service/) | PaperMC docs | скриптовая загрузка, статус поддержки, минимальная Java по версиям |
| [Paper javadocs](https://jd.papermc.io/paper/) | PaperMC | справочник по API |

---

## 2. Структура проекта Paper: descriptor, bootstrapper, loader, библиотеки

**Ключевые выводы**

- **С чего начать.** Новому плагину только для Paper берите `paper-plugin.yml` с bootstrapper, как в шаблоне ниже: команды Brigadier и реестры доступны только так, и шаблон проверен в CI. Пометка Experimental означает, что формат ещё может меняться между версиями, а не что он не работает. `plugin.yml` выбирайте, если плагин должен работать и на Spigot или если команды объявляются через секцию `commands:` и `CommandExecutor`.

- **Плагин Bukkit** — это `plugin.yml` и главный класс `JavaPlugin`. **Плагин Paper** — это `paper-plugin.yml`. В документации (2026) он всё ещё помечен **Experimental**, но нужен для возможностей bootstrapper (изменение реестров, обнаружение датапаков, ранняя регистрация команд). Оба файла могут лежать в одном JAR.
- Отличия `paper-plugin.yml`: нет секции `commands:` (команды Brigadier регистрируются через Lifecycle). Зависимости разделены на `dependencies.bootstrap` и `dependencies.server` с параметрами на каждую: `load: BEFORE|AFTER|OMIT`, `required` (по умолчанию true), `join-classpath` (по умолчанию true). **Изоляция загрузчиков классов**: плагин Paper не видит классы других плагинов, пока не зависит от них. Собственные `ConfigurationSerializable` не регистрируются автоматически (вызывайте `ConfigurationSerialization.registerClass`). Paper не разрешает циклические порядки загрузки и падает с `[LoadOrderTree] Circular plugin loading detected`.
- **Bootstrapper**: `implements PluginBootstrap { bootstrap(BootstrapContext); createPlugin(PluginProviderContext) }`. Он выполняется до появления сервера и реестров. Используйте только lifecycle manager, не API Bukkit. Исключения в обработчиках реестров **останавливают сервер** (так задумано, чтобы не потерять данные).
- **Loader**: `implements PluginLoader { classloader(PluginClasspathBuilder) }` с `JarLibrary` и `MavenLibraryResolver`. Берите `MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR` (зеркало Google). Прямое обращение к Maven Central пишет предупреждение и нарушает условия Central для CDN-использования. Зеркало переопределяется переменной окружения `PAPER_DEFAULT_CENTRAL_REPOSITORY` или `org.bukkit.plugin.java.LibraryLoader.centralURL`.
- Список `libraries:` в `plugin.yml` разрешается в runtime через зеркало Central (shading не нужен), но только для артефактов Maven Central. Для частных репозиториев нужен loader или shading. Поля `paper-plugin-loader:` и `paper-skip-libraries:` позволяют плагину Bukkit передать работу loader Paper.
- `folia-supported: true` обязателен, чтобы Folia вообще загрузил плагин (необходимо, но недостаточно).
- Генерируйте дескрипторы из Gradle, а не пишите руками: `xyz.jpenilla.resource-factory-paper-convention` (1.4.0) или `de.eldoria.plugin-yml.paper` (0.9.0). Так версии в `build.gradle.kts` и YAML не расходятся.
- Рекомендуемая раскладка (один модуль): `src/main/java/<pkg>/{ExamplePlugin, ExampleBootstrap, ExampleLoader, command/, listener/, service/, storage/, config/}`, `src/main/resources/{paper-plugin.yml, config.yml|*.conf, lang/*.properties, <датапак>/}`. Для проектов под несколько платформ (Paper, Velocity, Fabric): модули `common/` (чистая Java без API платформы), `paper/`, `velocity/`, `fabric/`. Общий код не должен импортировать `org.bukkit`.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Paper plugins](https://docs.papermc.io/paper/dev/getting-started/paper-plugins/) | PaperMC docs, 2026 | `paper-plugin.yml`, bootstrapper, loader, изоляция |
| [plugin.yml](https://docs.papermc.io/paper/dev/plugin-yml/) | PaperMC docs, 2026 | все поля, включая `libraries` и `paper-plugin-loader` |
| [Project setup](https://docs.papermc.io/paper/dev/project-setup/) | PaperMC docs, 2026 | Gradle Kotlin DSL, `26.x.build.+`, toolchain Java 25 |
| [System properties](https://docs.papermc.io/paper/reference/system-properties/) | PaperMC docs | `LibraryLoader.centralURL`, `com.mojang.eula.agree`, `paper.preferSparkPlugin` |
| [jpenilla/resource-factory](https://github.com/jpenilla/resource-factory) | J. Penilla | генерация plugin.yml, paper-plugin.yml, velocity-plugin.json, fabric.mod.json |
| [eldoriarpg/plugin-yml](https://github.com/eldoriarpg/plugin-yml) | Eldoria (форк Minecrell) | другой генератор дескрипторов, включая генерацию loader Paper |
| [Minecraft Development IntelliJ plugin](https://plugins.jetbrains.com/plugin/8327-minecraft-development) | MinecraftDev | мастер проектов Paper, Velocity, Fabric, NeoForge |

---

## 3. paperweight-userdev, доступ к внутренностям, run-paper

**Ключевые выводы**

- `paperweight-userdev` — «единственный поддерживаемый способ» компилироваться против внутренностей сервера. Он декомпилирует и патчит сервер из **dev-бандла**: `paperweight.paperDevBundle("26.3.build.+")` (отдельную зависимость `paper-api` уберите, бандл её включает). Для Folia: `paperweight.foliaDevBundle(...)`, для форков: `paperweight.devBundle("group", "ver")`.
- **2.0.0-beta.24 требует Gradle 9.7.1+** и Java 21+ для демона Gradle. Поддерживается только последний userdev. Снапшоты берутся только из `repo.papermc.io` (добавьте в `pluginManagement.repositories`). Если `paperweightUserdevSetup` падает на применении патчей для старых бандлов, задайте `paperweight { javaLauncher = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(17) } }`.
- **Реобфускация для 26.1+ ушла в историю**: `reobfJar`, `ReobfArtifactConfiguration.REOBF_PRODUCTION`, различие `-dev.jar` и reobf JAR относятся только к ≤1.21.11. Для плагинов только под Paper на 1.20.5–1.21.11 настройка `paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION` пропускала runtime-ремаппинг.
- Внутренности ломаются в каждом выпуске MC. Прячьте NMS за интерфейсом в маленьком модуле (`nms/`) и держитесь API-first. Предпочитайте код без рефлексии; если без неё нельзя, кэшируйте `MethodHandle`. Для NMS на нескольких версиях ≤1.21.11 инструментом был reflection-remapper; на 26.x имена везде Mojang.
- **run-paper 3.1.0**: `tasks.runServer { minecraftVersion("26.3") }` скачивает Paper через Fill и запускает его с вашим JAR (или shadowJar). Для плагинов-зависимостей есть `downloadPlugins { modrinth("id","ver"); hangar("name","ver"); url("…") }` (LuckPerms, ViaVersion на тестовом сервере); синтаксис `downloadPlugins` `[не проверено]`. `run-velocity`: `runVelocity { velocityVersion("4.3.1") }`. В CI передавайте `-Dcom.mojang.eula.agree=true` (системное свойство Paper). Остановку сервера в CI см. в разделе «Шаблоны»: проверенный путь — следить за строкой `Done (` в логе и отправить `stop`.
- Горячая перезагрузка при разработке: run-paper поддерживает hotswap на JetBrains Runtime (`javaLauncher` с JBR и `-XX:+AllowEnhancedClassRedefinition`). Предпочитайте её `/reload`. `[не проверено]`

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [paperweight-userdev](https://docs.papermc.io/paper/dev/userdev/) | PaperMC docs, 2026 | настройка, dev-бандлы, история реобфускации |
| [PaperMC/paperweight](https://github.com/PaperMC/paperweight) | PaperMC | релизы (требование Gradle 9.7.1 у beta.24) |
| [PaperMC/paperweight-test-plugin](https://github.com/PaperMC/paperweight-test-plugin) | PaperMC | эталонная сборка userdev (версии немного устарели) |
| [jpenilla/run-task](https://github.com/jpenilla/run-task) | J. Penilla | run-paper, run-velocity, run-waterfall |
| [run-task wiki](https://github.com/jpenilla/run-task/wiki) | J. Penilla | downloadPlugins, hotswap, несколько серверов |
| [jpenilla/reflection-remapper](https://github.com/jpenilla/reflection-remapper) | J. Penilla | рефлексия по именам Mojang на обфусцированных серверах (≤1.20.4); `[устарело]` для 26.x, имена уже Mojang |
| [Gradle wrapper docs](https://docs.gradle.org/current/userguide/gradle_wrapper.html) | Gradle | обновление до 9.7.1+ для userdev |

---

## 4. Современный API Paper (26.x)

### 4.1 Adventure и MiniMessage

**Ключевые выводы**

- Paper реализует Adventure нативно: `Player`, `CommandSender`, `World`, `Server` являются `Audience`. Отправляйте `Component`, никогда строки со старыми `§` и `ChatColor` (`ChatColor` в Paper deprecated). Для пользовательского форматирования используйте **MiniMessage**: `MiniMessage.miniMessage().deserialize("<gold>Hi <player>", Placeholder.component("player", p.name()))`.
- **Безопасность**: никогда не передавайте недоверенный ввод игрока в `deserialize` как шаблон. Вставляйте его через `Placeholder.unparsed(...)` (экранируется) или `Component.text(input)`. Иначе игроки внедряют теги `<click:run_command:…>` и `<hover>`. Через `TagResolver` ограничивайте набор тегов в сообщениях, которые редактируют пользователи (например, права на форматирование чата). `[не проверено]` (по документации MiniMessage)
- Adventure **5.x** (Paper 26.2+, Velocity 4.0+): Java 21+, sealed-иерархия `Component` (свои компоненты только через `VirtualComponent`), JSpecify, типизированный `ClickEvent<T>` (`ClickEvent#value` теперь `payload`), удалены `MessageType` и перегрузки `sendMessage` с `Identity`, `TranslationRegistry` заменён на `TranslationStore`, `PlainComponentSerializer` на `PlainTextComponentSerializer`.
- Локализация: `GlobalTranslator` и `TranslationStore.messageFormat(key)`. Рендерите через `Component.translatable("myplugin.shop.bought")`, чтобы использовалась локаль каждого клиента.
- Сокращения `Server#sendRichMessage(String)` и `sendPlainMessage` (1.21.7+).

### 4.2 API команд Brigadier

- Регистрация через Lifecycle: `getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, e -> e.registrar().register(node, "description", List.of("alias")))` из `onEnable` или, лучше, из bootstrapper (тогда команды доступны функциям датапаков раньше). Команды перерегистрируются автоматически при `/minecraft:reload`.
- Деревья строятся через `Commands.literal(...)` и `Commands.argument("target", ArgumentTypes.player())`. Селекторы разрешаются так: `ctx.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst()`. Возвращайте `Command.SINGLE_SUCCESS`. Нативные типы аргументов дают проверку и подсказки на клиенте (позиции блоков, стеки предметов, компоненты, ключи реестров `ArgumentTypes.resource(RegistryKey.ENCHANTMENT)`).
- Права: `.requires(src -> src.getSender().hasPermission("shop.admin"))`. С 1.21.6 оборачивайте в `Commands.restricted(...)`, чтобы получить ванильное поведение «подтвердите опасную команду» для команд уровня оператора.
- `BasicCommand` подходит для простых команд со `String[] args` (помощь при миграции). В `paper-plugin.yml` секции `commands:` нет; команды в `plugin.yml` по-прежнему работают у плагинов Bukkit.
- Подвох: дерево команд отправляется клиенту; предикаты `requires` вычисляются для каждого игрока в момент отправки. После смены прав вызывайте `player.updateCommands()`. `[не проверено]`

### 4.3 Lifecycle, реестры, обнаружение датапаков

- `LifecycleEventManager` бывает двух видов: `BootstrapContext` (до сервера, реестры не заморожены) и `Plugin` (onEnable). У обработчиков есть `priority(int)` или `monitor()` (эксклюзивный). Регистрация некоторых обработчиков отключает `/bukkit:reload` из соображений безопасности.
- **Изменение реестров** (только плагины Paper, в bootstrap): `RegistryEvents.ENCHANTMENT.compose()` добавляет записи (`event.registry().register(EnchantmentKeys.create(Key.key("myplugin:lifesteal")), b -> b.description(...).supportedItems(event.getOrCreateTag(ItemTypeTagKeys.SWORDS)).maxLevel(3)...)`), а `entryAdd().newHandler(e -> e.builder().maxLevel(20)).filter(EnchantmentKeys.SHARPNESS)` меняет ванильные записи. Записываемые реестры: чары, игровые события, варианты картин, кошек, волков, свиней, коров, куриц и лягушек, типы урона, материалы и узоры отделки (26.2), диалоги. API реестров всё ещё помечен **Experimental**. Используйте свой namespace, никогда `minecraft:` и `papermc:`.
- **Обнаружение датапаков** (1.21.4+, плагины Paper): положите *исходные папки* датапака в JAR и вызовите `event.registrar().discoverPack(getClass().getResource("/my_pack").toURI(), "provided")` в обработчике `LifecycleEvents.DATAPACK_DISCOVERY`. Позже ищите его как `<pluginName>/provided` через `DatapackManager`. Есть и `removeDiscoveredPack("trade_rebalance")`.

### 4.4 Компоненты данных, ItemStack, PDC, диалоги, меню

- API компонентов данных (`DataComponentTypes.*`, `item.getData/setData/unsetData/resetData`, `Material.X.getDefaultData(...)` для прототипа) повторяет ванильную модель «прототип плюс патч». Оно **привязано к версии и не обещает обратной совместимости**, но в 26.3 в основном уже не `@Experimental`. `ItemMeta` оставьте для простых правок предметов, переносимых между версиями; для всего современного (item_model, food, tool, equippable, consumable, списки custom_model_data) берите компоненты данных.
- PDC: `PersistentDataContainer` есть у сущностей, tile state, чанков, миров и `ItemStack` (через `item.editPersistentDataContainer(pdc -> ...)`, 1.21.4+). Только читающее `PersistentDataContainerView` возвращает `item.getPersistentDataContainer()`. Типы: примитивы, `BOOLEAN`, `LIST.strings()`, вложенные контейнеры, собственные `PersistentDataType<P,C>`. Ключи: `new NamespacedKey(plugin, "owner")`, кэшируйте как константы. PDC блока только у `TileState` (в 26.2 у кроватей нет блок-сущностей).
- Подвох: `getItemMeta()` возвращает **копию-снимок**. Изменения теряются без `setItemMeta`. Предпочитайте `editMeta(m -> ...)` и `editPersistentDataContainer`.
- **Dialog API** (1.21.6+): `Dialog.create(b -> b.empty().base(DialogBase.builder(text("Rules")).build()).type(DialogType.notice()))`, `player.showDialog(dialog)`. Реагируйте через `DialogAction.customClick((response, audience) -> ...)`. Подходит для принятия правил, подтверждения покупки и форм ввода без хаков с GUI-сундуками. Через Audience Velocity не поддерживается.
- `MenuType` API даёт типизированные представления инвентаря. Для опознания GUI плагина используйте свой `InventoryHolder`, никогда не сравнивайте по названию.

### 4.5 События и их производительность

- `@EventHandler(priority = …, ignoreCancelled = true)`. Порядок приоритетов LOWEST, …, MONITOR (более высокий выполняется позже; MONITOR ничего не меняет). Отписка: `HandlerList.unregisterAll(listener)`. Для собственных событий нужны статические `getHandlerList()` и `getHandlers()`; передавайте `true` в конструктор `Event(boolean isAsync)`, если событие запускается не из главного потока.
- Горячие события: `PlayerMoveEvent` (каждый пакет движения), `EntityMoveEvent` (Paper, срабатывает только при наличии слушателя), `BlockPhysicsEvent`, `ChunkLoadEvent`, `InventoryClickEvent`, `ServerTickStartEvent`. Правила: ранний выход по дешёвым проверкам (`event.hasChangedBlock()` для движения), никаких аллокаций, ввода-вывода и БД в обработчиках, никаких обходов `Bukkit.getOnlinePlayers()` на каждое событие, поиск в `HashMap<UUID, …>` с очисткой при выходе. `[не проверено]`
- Чат: `AsyncChatEvent` и `ChatRenderer` (отрисовка с учётом зрителя) заменяют `AsyncPlayerChatEvent` (старые строки). Событие асинхронное: внутри нельзя трогать мир, возвращайтесь на нужный поток через планировщик.
- Вход: `AsyncPlayerPreLoginEvent` — место для блокирующих запросов к БД (он уже вне главного потока). `PlayerConnectionValidateLoginEvent` разрешает или запрещает вход (1.21.7+). `PlayerJoinEvent` — настройка в главном потоке.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Adventure documentation](https://docs.papermc.io/adventure/) | PaperMC/Kyori, 2026 | модель Component, audiences, сериализаторы |
| [MiniMessage format](https://docs.papermc.io/adventure/minimessage/format/) | Kyori | синтаксис тегов |
| [MiniMessage dynamic replacements](https://docs.papermc.io/adventure/minimessage/dynamic-replacements/) | Kyori | `Placeholder.unparsed` и `parsed`, безопасный пользовательский ввод |
| [Migrating from Adventure 4.x](https://docs.papermc.io/adventure/migration/adventure-4.x/) | Kyori, 2026 | ломающие изменения Adventure 5 |
| [Adventure localization](https://docs.papermc.io/adventure/localization/) | Kyori | TranslationStore, GlobalTranslator |
| [MiniMessage web editor](https://docs.papermc.io/misc/tools/minimessage-web-editor/) | PaperMC | предпросмотр строк MiniMessage |
| [PaperMC/adventure](https://github.com/PaperMC/adventure) | PaperMC (раньше KyoriPowered) | исходники, релизы (репозиторий переехал под PaperMC) |
| [Velocity: Libraries used](https://docs.papermc.io/velocity/credits/) | PaperMC docs | библиотеки внутри Velocity (не шейдить) |
| [Command API introduction](https://docs.papermc.io/paper/dev/command-api/basics/introduction/) | PaperMC docs | Brigadier на Paper |
| [Command registration](https://docs.papermc.io/paper/dev/command-api/basics/registration/) | PaperMC docs | Lifecycle COMMANDS, bootstrap и onEnable |
| [Requirements](https://docs.papermc.io/paper/dev/command-api/basics/requirements/) | PaperMC docs | `requires`, `Commands.restricted` |
| [Comparison: Bukkit vs Brigadier](https://docs.papermc.io/paper/dev/command-api/misc/comparison-bukkit-brigadier/) | PaperMC docs | миграция с CommandExecutor |
| [Mojang/brigadier](https://github.com/Mojang/brigadier) | Mojang | библиотека под капотом |
| [Lifecycle API](https://docs.papermc.io/paper/dev/lifecycle/) | PaperMC docs | настройка обработчиков, зачем вторая система событий |
| [Registries](https://docs.papermc.io/paper/dev/registries/) | PaperMC docs | compose, entryAdd, TypedKey, RegistrySet |
| [Datapack discovery](https://docs.papermc.io/paper/dev/lifecycle/datapacks/) | PaperMC docs | доставка датапаков внутри плагинов |
| [Data components](https://docs.papermc.io/paper/dev/data-component-api/) | PaperMC docs | прототип и патч, отличие от ItemMeta |
| [Persistent data container](https://docs.papermc.io/paper/dev/pdc/) | PaperMC docs | типы, представления, владельцы |
| [Dialog API](https://docs.papermc.io/paper/dev/dialogs/) | PaperMC docs | формы, управляемые сервером |
| [Menu Type API](https://docs.papermc.io/paper/dev/menu-type-api/) | PaperMC docs | типизированные представления инвентаря |
| [Custom InventoryHolder](https://docs.papermc.io/paper/dev/custom-inventory-holder/) | PaperMC docs | безопасное опознание GUI плагина |
| [Event listeners](https://docs.papermc.io/paper/dev/event-listeners/) | PaperMC docs | приоритеты, ignoreCancelled |
| [Custom events](https://docs.papermc.io/paper/dev/custom-events/) | PaperMC docs | шаблонный код HandlerList |
| [Handler lists](https://docs.papermc.io/paper/dev/handler-lists/) | PaperMC docs | динамическая (де)регистрация |
| [Chat events](https://docs.papermc.io/paper/dev/chat-events/) | PaperMC docs | AsyncChatEvent и ChatRenderer |
| [Display entities](https://docs.papermc.io/paper/dev/display-entities/) | PaperMC docs | голограммы, поддельные блоки, визуальные эффекты |
| [Mob goals](https://docs.papermc.io/paper/dev/mob-goals/) | PaperMC docs | своё поведение мобов для RPG и мини-игр |
| [Entity teleport](https://docs.papermc.io/paper/dev/entity-teleport/) | PaperMC docs | `teleportAsync`, пассажиры |
| [Recipes](https://docs.papermc.io/paper/dev/recipes/) | PaperMC docs | рецепты shaped, shapeless, smithing |
| [Particles](https://docs.papermc.io/paper/dev/particles/) | PaperMC docs | ParticleBuilder |

---

## 5. Потоки: планировщики Paper и Folia

### 5.1 Paper (один главный поток)

**Ключевые выводы**

- Один главный («серверный») поток выполняет тик на 20 TPS (бюджет 50 мс). Почти всё API Bukkit, читающее или меняющее состояние мира, сущностей и инвентарей, **доступно только из главного потока**. Асинхронный доступ может бросить исключение (`AsyncCatcher`) или молча испортить состояние.
- `BukkitScheduler`: `runTask`, `runTaskLater(plugin, r, ticks)`, `runTaskTimer`, `runTaskAsynchronously`. Асинхронные задачи всё равно *запускаются* циклом тиков (лаги сдвигают время). Для задач по времени стены используйте свой `ScheduledExecutorService`. `Tick.tick().fromDuration(Duration.ofMinutes(5))` даёт 6000 тиков.
- Шаблон для ввода-вывода: `CompletableFuture.supplyAsync(dbCall, ioExecutor).thenAcceptAsync(result -> applyToWorld(result), mainThreadExecutor)`, где `mainThreadExecutor = r -> Bukkit.getScheduler().runTask(plugin, r)`. В коде, готовом к Folia, вместо этого пишите `player.getScheduler().run(plugin, t -> ..., null)`. Никогда не вызывайте `.join()` и `.get()` в главном потоке. `[не проверено]`
- Асинхронные помощники Paper: `World#getChunkAtAsync`, `Entity#teleportAsync` (грузит чанки вне потока, завершается в главном), `AsyncChatEvent`, `AsyncPlayerPreLoginEvent`.
- Предпочитайте планировщики в стиле Folia даже на Paper (`Bukkit.getGlobalRegionScheduler()`, `getRegionScheduler()`, `getAsyncScheduler()`, `entity.getScheduler()`): на Paper они отображаются на главный поток, и тот же код работает на Folia.

### 5.2 Folia (регионализированная многопоточность)

- Folia (форк PaperMC) делит загруженные чанки на **независимые регионы**. Каждый регион тикает параллельно в пуле потоков со своим дедлайном 20 TPS (планирование типа EDF). **Главного потока нет.** Соседние регионы сливаются и делятся, когда возможно. Каждый регион владеет данными сущностей, чанков и POI.
- Плагины грузятся только с `folia-supported: true`. README говорит: «have your expectations for compatibility at 0», то есть каждому плагину нужны правки.
- Планировщики: **GlobalRegionScheduler** (задачи вне мира: игровые правила, глобальные таймеры, команды консоли), **RegionScheduler** (`execute(plugin, location, r)`, изменения блоков и мира в точке), **EntityScheduler** (`entity.getScheduler().run/runDelayed/runAtFixedRate(plugin, task, retiredCallback, …)`, следует за сущностью между регионами; колбэк `retired`, если сущность удалена), **AsyncScheduler** (`runNow`, `runDelayed(…, TimeUnit)`, ввод-вывод вне тика).
- Контексты потоков: команды игрока выполняются в регионе игрока; команды консоли в глобальном регионе; события одной сущности в её регионе; события урона в регионе цели. `Bukkit.isOwnedByCurrentRegion(location|entity)` проверяет владение. Флаг async у событий deprecated: все события из регионов считаются синхронными.
- Сломано или не поддерживается на Folia: API scoreboard (глобальное состояние), загрузка и выгрузка миров, многие API порталов, возрождения и входа. `Entity#teleport` **никогда не вернётся**, используйте `teleportAsync`. Данные плагина — ваша ответственность: «a concurrent collection used carelessly will only hide threading issues».
- Определение во время работы: `ServerBuildInfo.buildInfo().isBrandCompatible(Key.key("papermc", "folia"))`.
- Статус (2026-11): сборки Folia 26.3 в канале **ALPHA** (build 6, 2026-10-29); 26.2 дошла до BETA. Подходит для больших SMP и skyblock с разбросанными игроками и ≥16 физическими ядрами. Миры лучше заранее сгенерировать; потоков тиков не больше 80% ядер после потоков netty, чанков и GC. Это не замена Paper «один в один».
- Типичные правки при миграции: на каждого игрока `Bukkit.getScheduler().runTaskTimer(...)` заменяется на `player.getScheduler().runAtFixedRate(...)`; асинхронный `block.setType` — на `Bukkit.getRegionScheduler().execute(plugin, loc, ...)`; статические кэши `HashMap` — на `ConcurrentHashMap` с неизменяемыми значениями; взаимодействия между регионами (телепорт к игроку) — на `teleportAsync` с колбэком в планировщике сущности; scoreboard — на боковые панели через пакеты на игрока или пропуск на Folia.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Scheduling](https://docs.papermc.io/paper/dev/scheduler/) | PaperMC docs | BukkitScheduler, правила sync и async |
| [Supporting Paper and Folia](https://docs.papermc.io/paper/dev/folia-support/) | PaperMC docs | четыре планировщика, определение Folia |
| [Folia overview](https://docs.papermc.io/folia/reference/overview/) | Spottedleaf / PaperMC | модель регионов, планирование тиков |
| [Folia region logic](https://docs.papermc.io/folia/reference/region-logic/) | Spottedleaf / PaperMC | инварианты слияния и разделения |
| [Folia FAQ](https://docs.papermc.io/folia/faq/) | PaperMC docs | железо, совместимость |
| [PaperMC/Folia](https://github.com/PaperMC/Folia) | PaperMC | README: правила, сломанное API, контексты потоков |
| [Folia downloads](https://papermc.io/downloads/folia) | PaperMC | сборки и каналы |
| [Java Concurrency in Practice](https://jcip.net/) | Goetz et al., 2006 | основы модели памяти для безопасного состояния плагина на Folia |

---

## 6. Остальное серверное ПО: Spigot, Purpur, Leaf, Sponge, Minestom

**Ключевые выводы**

- **Spigot, Bukkit, CraftBukkit (md_5)** живы и актуальны: BuildTools собирает 26.3 (`hub.spigotmc.org/versions/26.3.json`, build 4664), `org.spigotmc:spigot-api:26.3-R0.1-SNAPSHOT` (Spigot сохранил схему `-R0.1-SNAPSHOT`, от которой отказался Paper). Spigot тоже перешёл на выпуск без обфускации в 26.1. Распространяется только через BuildTools (готовых JAR нет). Компилируйтесь против `spigot-api` только если нужен запуск на Spigot: в нём нет Adventure, API Brigadier, Lifecycle, изменения реестров, API компонентов данных и планировщиков Folia. Spigot-специфичное API, добавленное после форка 2024 года (например, свои добавления компонентов и реестров), на Paper может отсутствовать.
- **Стратегия совместимости**: «Paper сначала, Spigot по желанию». Либо (а) только Paper (проще всего, ~85–90% серверов), либо (б) компиляция против `spigot-api` с тонким модулем на Paper, определяемым во время работы (`Class.forName("io.papermc.paper.ServerBuildInfo")`). Избегайте (в) модулей NMS на каждую версию, если не обязательно.
- **Purpur** (форк Paper с настраиваемым геймплеем: верховые мобы, дополнительные настройки): актуален на 26.3 (build 2657). API `org.purpurmc.purpur:purpur-api:26.3.build.2657-stable` из `repo.purpurmc.org/snapshots`; Purpur принял ту же схему `<mc>.build.<n>-stable`. Плагины для Paper работают на Purpur. Компилируйтесь против purpur-api только ради событий Purpur.
- **Pufferfish**: форк для производительности (DAB, асинхронный трекер сущностей, Sentry). Последний push публичного репозитория 2025-12-21 в `ver/1.21`, **ветки 26.x нет**; для 26.x считайте его остановившимся (компания также продаёт хостинг). Часть его патчей живёт в Purpur и Leaf.
- **Leaf** (Winds-Studio, форк линии Paper, Gale и Purpur для производительности): актуален на 26.3 (релиз 2026-11-05). Асинхронный поиск пути, асинхронный спаун мобов, асинхронный трекер сущностей, формат регионов Linear; по README «fully compatible with Spigot and Paper plugins». Подвох: асинхронные функции ломают плагины, которые предполагают состояние сущностей в главном потоке (вызовы pathfinder и goal API из событий). Тестируйте сначала на Paper, баги, свойственные форку, сообщайте форку.
- Другие форки для производительности (Gale, DivineMC, Canvas и др.) быстро меняются. Для продакшена берите Paper (или Folia, когда нужен параллелизм), если вы не измерили выигрыш конкретного форка в spark. `[не проверено]`
- **Sponge** (SpongeAPI; реализации SpongeVanilla и SpongeForge): стабильный **API 17.0.0** (2026-01-11, MC 1.21.10); API 18–21 только SNAPSHOT для 1.21.11, 26.1.2, 26.2, 26.3 (ветки `api-18`…`api-21`, `minecraftVersion` в gradle.properties). Экосистема плагинов небольшая; Configurate вышел отсюда. Используйте только для сетей Sponge или SpongeForge (Forge с плагинами).
- **Minestom**: серверная *библиотека* с нуля (без кода Mojang, без ванильного геймплея по умолчанию: нет ИИ мобов, редстоуна, генерации мира, пока вы их не напишете или не добавите). Версии по датам `YYYY.MM.DD-<mc>`: последняя `2026.10.07-26.2` (исправление безопасности GHSA-mmph-rj3w-m9rq); 26.3 в ветке `26_3`; toolchain Java 25; библиотека интеграционных тестов `net.minestom:testing`. Применения: лобби, мини-игры (типа BedWars, паркур), limbo и AFK-серверы, эксперименты с собственным протоколом; на экземпляр возможны тысячи игроков, потому что тикает только то, что вы добавили. С плагинами Bukkit несовместим.
- **Ванильный выделенный сервер** по-прежнему полезен для серверов только на датапаках: `server.properties`, `--nogui`, точка входа gametest (см. раздел 10).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [BuildTools](https://www.spigotmc.org/wiki/buildtools/) | SpigotMC wiki | как собираются JAR Spigot |
| [Spigot javadocs](https://hub.spigotmc.org/javadocs/spigot/) | SpigotMC | справочник по spigot-api |
| [SpigotMC](https://www.spigotmc.org/) | md_5 | форумы, ресурсы (устаревший канал распространения) |
| [PurpurMC/Purpur](https://github.com/PurpurMC/Purpur) | PurpurMC | исходники форка, координаты API |
| [Purpur docs](https://purpurmc.org/docs) | PurpurMC | параметры конфигурации, API |
| [pufferfish-gg/Pufferfish](https://github.com/pufferfish-gg/Pufferfish) | Pufferfish, 2025 | справка о статусе (остановлен на 1.21) |
| [Winds-Studio/Leaf](https://github.com/Winds-Studio/Leaf) | Winds-Studio, 2026 | возможности и оговорки форка для производительности |
| [Leaf docs](https://www.leafmc.one/docs/getting-started) | Winds-Studio | настройка асинхронных функций |
| [SpongePowered/Sponge](https://github.com/SpongePowered/Sponge) | SpongePowered | реализации SpongeVanilla и SpongeForge |
| [SpongePowered/SpongeAPI](https://github.com/SpongePowered/SpongeAPI) | SpongePowered | исходники API |
| [Sponge docs](https://docs.spongepowered.org/stable/en/) | SpongePowered | разработка плагинов |
| [Minestom/Minestom](https://github.com/Minestom/Minestom) | Minestom | README, релизы, security advisories |
| [Minestom docs](https://minestom.net/docs/introduction) | Minestom | понятия: instances, events, потоки |
| [Minestom javadocs](https://javadoc.minestom.net/) | Minestom | справочник по API |
| [Paper downloads](https://papermc.io/downloads/paper) | PaperMC | официальные сборки |

---

## 7. Серверы на загрузчиках модов: Fabric, NeoForge, Polymer

**Ключевые выводы**

- Fabric и NeoForge поставляют выделенные серверы для 26.x (установщик, затем серверный лаунчер). **API Bukkit там нет**: права через `fabric-permissions-api` (и LuckPerms для Fabric), события через колбэки Fabric API или `EventBus` NeoForge, команды через ванильный Brigadier (`CommandRegistrationCallback` на Fabric, `RegisterCommandsEvent` на NeoForge).
- **Моды только для сервера**: в Fabric `fabric.mod.json` с `"environment": "server"` (или `"*"`, если мод работает где угодно, но клиент ему не нужен); в NeoForge `@Mod(value = "id", dist = Dist.DEDICATED_SERVER)` для серверных входных классов и `[[dependencies.<id>]] side="SERVER"`. Мод совместим с ванильным клиентом, только если он **не добавляет синхронизируемых записей реестров и обязательных пользовательских payload**: иначе синхронизация реестров Fabric и согласование сети NeoForge отклонят ванильные клиенты. Безопасны для ванильного клиента, например: производительность (Lithium, C2ME, FerriteCore, Krypton), администрирование и модерация, форматирование чата, предгенерация мира, резервные копии, права, пересылка через прокси (FabricProxy-Lite). Правило про ванильный клиент `[не проверено]`.
- **Polymer** (Patbox, Fabric) позволяет моду регистрировать **настоящие** серверные блоки, предметы и сущности (`mymod:custom`, их видят датапаки и другие моды), отображаемые ванильными представлениями на клиенте (`PolymerItem#getPolymerItem`, `PolymerBlock` в ванильное состояние блока или display-сущность), с необязательными автогенерируемыми и автохостящимися ресурспаками (AutoHost). Актуален 0.18.7+26.3. Для сравнения: «кастомные предметы» Bukkit — это ванильные предметы с тегами и компонентами.
- PolyMc автоматически превращает обычные моды в серверные (с ограничениями); Plasmid (NucleoidMC) — фреймворк мини-игр на Fabric поверх серверного содержимого.
- Пересылка Velocity на модовых серверах: FabricProxy-Lite (Fabric), ProxyCompatibleForge (Forge). Для старой пересылки BungeeCord поддерживаемого мода Fabric нет.
- Когда сервер на модах лучше сервера на плагинах: нужны миксины в ваниль (например, свои функции генерации мира, поведение редстоуна), вы запускаете сборку модов или нужны настоящие реестры для содержимого. Когда лучше Paper: большая экосистема плагинов (экономика, защита, мини-игры), клиентские моды не нужны, стабильное API между обновлениями MC.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Fabric server install](https://fabricmc.net/use/server/) | FabricMC | лаунчер выделенного сервера |
| [Installing Fabric (players/servers)](https://docs.fabricmc.net/players/installing-fabric) | FabricMC docs | основы установки |
| [fabric.mod.json specification](https://wiki.fabricmc.net/documentation:fabric_mod_json_spec) | Fabric wiki | поле `environment` |
| [Fabric networking](https://docs.fabricmc.net/develop/networking) | FabricMC docs | payload; почему пользовательские payload ломают ванильных клиентов |
| [NeoForge: Sides](https://docs.neoforged.net/docs/concepts/sides) | NeoForged docs (26.1) | физическая и логическая стороны, `dist` |
| [NeoForge: Payloads](https://docs.neoforged.net/docs/networking/payload) | NeoForged docs | необязательные и обязательные payload |
| [Polymer docs](https://polymer.pb4.eu/latest/) | Patbox | серверные блоки, предметы, сущности, хостинг ресурспаков |
| [Patbox/polymer](https://github.com/Patbox/polymer) | Patbox | README, релизы (0.18.7+26.3) |
| [Polymer on Modrinth](https://modrinth.com/mod/polymer) | Patbox | загрузки и версии |
| [TheEpicBlock/PolyMc](https://github.com/TheEpicBlock/PolyMc) | TheEpicBlock | автоперенос обычных модов на серверную сторону |
| [NucleoidMC/plasmid](https://github.com/NucleoidMC/plasmid) | NucleoidMC | фреймворк мини-игр на Fabric |
| [lucko/fabric-permissions-api](https://github.com/lucko/fabric-permissions-api) | lucko | права на серверах Fabric |
| [CaffeineMC/lithium-fabric](https://github.com/CaffeineMC/lithium-fabric) | CaffeineMC | серверный мод производительности (mc26.3-0.26.3) |
| [C2ME](https://modrinth.com/mod/c2me-fabric) | ishland | параллельная генерация чанков и ввод-вывод |
| [FerriteCore](https://modrinth.com/mod/ferrite-core) | malte0811 | снижение расхода памяти |
| [Krypton](https://modrinth.com/mod/krypton) | astei | оптимизация сетевого стека |
| [FabricProxy-Lite](https://modrinth.com/mod/fabricproxy-lite) | OKTW | современная пересылка Velocity на Fabric |

---

## 8. Прокси: Velocity, BungeeCord, Gate

**Ключевые выводы**

- **Velocity** — прокси PaperMC и выбор по умолчанию. 4.0.0 (2026-07-14) поднял требования до **Java 25** и **Adventure 5** (и убрал `adventure-platform-facet`). 4.3.1 (2026-10-27) — рекомендуемое обновление безопасности (обработка буферов legacy plugin message и отклонённых пакетов). 3.5.x (Java 21) DEPRECATED (конец 2026-09-30). Недавнее усиление защиты: лимит распакованных байт в секунду, ограниченная очередь plugin message до входа, DNS бэкендов в ограниченном пуле, `SecureRandom` для verify token.
- **Модель плагина Velocity**: `@Plugin(id = "network-chat", name = …, version = …, dependencies = {@Dependency(id = "luckperms", optional = true)})` на главном классе; конструктор с `@Inject` (Guice) получает `ProxyServer`, SLF4J `Logger`, `@DataDirectory Path`. В конструкторе только сохраняйте поля; слушатели и команды регистрируйте в `@Subscribe ProxyInitializeEvent` (главный класс автоматически регистрируется как слушатель). `velocity-plugin.json` генерирует процессор аннотаций (`annotationProcessor("com.velocitypowered:velocity-api:4.3.1")`); на JDK 23+ с Maven процессор нужно объявлять явно.
- События Velocity умеют быть асинхронными: обработчик может вернуть `EventTask.async(...)` или `EventTask.resumeWhenComplete(future)`, чтобы приостановить событие, не блокируя поток (с 3.0). Команды: `BrigadierCommand`, `SimpleCommand`, `RawCommand`. Планировщик: `server.getScheduler().buildTask(plugin, runnable).delay(…).repeat(…).schedule()`.
- Пробелы Audience Velocity: книги и **диалоги** через прокси не поддерживаются, делайте их на бэкенде.
- **Пересылка информации об игроке**: `player-info-forwarding-mode = "modern"` (подпись MAC, 1.13+; у Paper `config/paper-global.yml` `proxies.velocity.enabled/secret/online-mode`, `online-mode=false` в server.properties, `settings.bungeecord: false` в spigot.yml). `legacy` (формат BungeeCord) «fundamentally insecure»: закройте бэкенды файрволом, чтобы к ним ходил только прокси; `bungeeguard` добавляет токен. Смешивать режимы нельзя.
- **Plugin messaging**: на Paper `getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord")` (Paper переписывает в `bungeecord:main`). Сообщения едут на подключённом игроке: если онлайн никого нет, отправить нельзя. На Velocity, слушая `PluginMessageEvent`, для своего канала задайте `ForwardResult.handled()`, иначе **игроки смогут подделывать сообщения прокси бэкендам**. Для всего сложнее простых команд берите настоящую шину сообщений (Redis pub/sub, RabbitMQ, NATS) между серверами.
- **BungeeCord** (md_5): поддерживается (b2112, 2026-11-06), API плагинов на Java с `plugin.yml` или `bungee.yml`; применяйте только для старых сетей. **Waterfall** (форк BungeeCord от PaperMC): EOL объявлен 2024-03-26, в Fill помечен UNSUPPORTED; переходите на Velocity (есть руководство по миграции и канал plugin messaging, совместимый с BungeeCord).
- **Gate** (Minekube, Go): быстрый прокси, современная пересылка совместима с Velocity, режим «Lite» для простой маршрутизации, встроенный Bedrock через управляемые Geyser и Floodgate (`bedrock: true`); расширения пишутся на Go, не на Java. v0.76.2 (2026-11-04).
- ViaVersion на прокси позволяет одному прокси принимать клиентов разных версий, пока бэкенды остаются на одной версии; точность состояния лучше на бэкендах Paper (README).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Velocity: creating your first plugin](https://docs.papermc.io/velocity/dev/creating-your-first-plugin/) | PaperMC docs, 2026 | настройка Gradle и Maven, Java 25, процессор аннотаций |
| [Velocity plugin basics](https://docs.papermc.io/velocity/dev/api-basics/) | PaperMC docs | `@Plugin`, внедрение Guice, событие инициализации |
| [Velocity common pitfalls](https://docs.papermc.io/velocity/dev/pitfalls/) | PaperMC docs | конструирование и инициализация, пробелы Audience |
| [velocity-plugin.json](https://docs.papermc.io/velocity/dev/velocity-plugin-json/) | PaperMC docs | дескриптор вручную |
| [Velocity event API](https://docs.papermc.io/velocity/dev/event-api/) | PaperMC docs | асинхронный EventTask |
| [Velocity command API](https://docs.papermc.io/velocity/dev/command-api/) | PaperMC docs | команды Brigadier, Simple, Raw; BrigadierCommand и CommandMeta |
| [Velocity scheduler API](https://docs.papermc.io/velocity/dev/scheduler-api/) | PaperMC docs | задачи прокси |
| [Velocity plugin messaging](https://docs.papermc.io/velocity/dev/plugin-messaging/) | PaperMC docs | замечание о безопасности `ForwardResult.handled()` |
| [Velocity dependency management](https://docs.papermc.io/velocity/dev/dependency-management/) | PaperMC docs | `@Dependency`, необязательные зависимости |
| [Porting from Velocity 1.x](https://docs.papermc.io/velocity/dev/porting-plugins-from-velocity-1/) | PaperMC docs | заметка о Java 25 для 4.x, модель событий |
| [Player information forwarding](https://docs.papermc.io/velocity/player-information-forwarding/) | PaperMC docs | modern, legacy и BungeeGuard |
| [Velocity security](https://docs.papermc.io/velocity/security/) | PaperMC docs | файрвол для бэкендов |
| [Velocity server compatibility](https://docs.papermc.io/velocity/server-compatibility/) | PaperMC docs | матрица бэкендов и загрузчиков модов |
| [Velocity configuration](https://docs.papermc.io/velocity/configuration/) | PaperMC docs | справочник по velocity.toml |
| [PaperMC/Velocity](https://github.com/PaperMC/Velocity) | PaperMC | релизы, коммиты (4.0 на Java 25) |
| [Velocity javadocs](https://jd.papermc.io/velocity/) | PaperMC | справочник по API |
| [Paper plugin messaging](https://docs.papermc.io/paper/dev/plugin-messaging/) | PaperMC docs | канал BungeeCord со стороны бэкенда |
| [Announcing the end of life of Waterfall](https://papermc.io/news/announcing-the-end-of-life-of-waterfall) | PaperMC, 2024-03-26 | почему нужно мигрировать |
| [SpigotMC/BungeeCord](https://github.com/SpigotMC/BungeeCord) | md_5 | старый прокси, поддерживается |
| [minekube/gate](https://github.com/minekube/gate) | Minekube | прокси на Go со встроенным Bedrock |
| [Gate documentation](https://gate.minekube.com) | Minekube | режим Lite, конфигурация, расширения |
| [velocity-api maven-metadata](https://repo.papermc.io/repository/maven-public/com/velocitypowered/velocity-api/maven-metadata.xml) | PaperMC repo | точные версии Velocity |

---

## 9. Bedrock и совместимость версий: Geyser, Floodgate, ViaVersion

**Ключевые выводы**

- **Geyser** переводит протокол Bedrock в протокол Java и обратно. Работает как плагин (Paper, Spigot, Velocity, BungeeCord), мод Fabric или NeoForge либо отдельное приложение. Актуален 2.12.1 (b1295, 2026-11-07), поддерживает **Java 26.3** и **Bedrock 26.40–26.62** (у Bedrock своя нумерация 26.x, не совпадающая с Java). Переводится не всё (см. Current Limitations): пользовательские GUI, часть компонентов; ресурспаки должны быть в формате Bedrock.
- **Floodgate** (2.2.5, 26.3) пускает игроков Bedrock **без аккаунта Java**: UUID Bedrock имеют вид `00000000-0000-0000-xxxx-xxxxxxxxxxxx` (производные от XUID), к именам добавляется префикс (по умолчанию `.`). Подвохи для плагинов: не считайте, что `Bukkit.getOfflinePlayer(name)` возвращает того же игрока; для ветвления используйте `FloodgateApi.getInstance().isFloodgatePlayer(uuid)` (например, отправляйте формы Bedrock вместо GUI-сундуков или диалогов); файл ключа должен совпадать на прокси и бэкендах. Формат UUID и префикс `[не проверено]`.
- Дизайн, дружелюбный к Bedrock: не опирайтесь на возможности клиента только для Java (диалоги, свой `item_model` без соответствия в Bedrock; display-сущности поддерживаются частично) и давайте командные альтернативы GUI.
- **ViaVersion** (плюс ViaBackwards для клиентов вплоть до 1.9 и ViaRewind для 1.7–1.8) позволяет заходить клиентам других версий; 5.12.2 (2026-10-27). Лучшая точность на Paper. Плагины не должны считать, что версия клиента равна версии сервера: перед использованием новых возможностей клиента спрашивайте `Via.getAPI().getPlayerVersion(uuid)`. Обновления ViaVersion выходят через несколько дней после выпуска Minecraft (ранний доступ для спонсоров). Существуют отдельный ViaProxy и клиентский ViaFabricPlus.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [GeyserMC/Geyser](https://github.com/GeyserMC/Geyser) | GeyserMC | таблица поддерживаемых версий |
| [Geyser wiki](https://geysermc.org/wiki/geyser/) | GeyserMC | настройка по платформам |
| [Geyser current limitations](https://geysermc.org/wiki/geyser/current-limitations/) | GeyserMC | что нельзя перевести |
| [Geyser supported versions](https://geysermc.org/wiki/geyser/supported-versions/) | GeyserMC | старые версии Java |
| [Floodgate wiki](https://geysermc.org/wiki/floodgate/) | GeyserMC | API Floodgate, общий ключ |
| [GeyserMC/Floodgate](https://github.com/GeyserMC/Floodgate) | GeyserMC | исходники |
| [Geyser downloads](https://geysermc.org/download/) | GeyserMC | сборки по платформам |
| [ViaVersion/ViaVersion](https://github.com/ViaVersion/ViaVersion) | kennytv et al. | матрица поддержки, API |
| [ViaVersion/ViaBackwards](https://github.com/ViaVersion/ViaBackwards) | ViaVersion | старые клиенты |
| [ViaVersion docs](https://docs.viaversion.com) | ViaVersion | документация для пользователей |
| [ViaVersion on Hangar](https://hangar.papermc.io/ViaVersion/ViaVersion) | ViaVersion | распространение плагина |
| [ViaVersion/ViaProxy](https://github.com/ViaVersion/ViaProxy) | ViaVersion | отдельный транслирующий прокси |

---

## 10. Датапаки в эпоху 26.x

**Ключевые выводы**

- **pack.mcmeta**: с 25w31a (1.21.9) паки объявляют `"min_format"` и `"max_format"` (major.minor; повышение minor означает отсутствие ломающих изменений) вместо одного `pack_format`. Текущие номера: **данные 121.0 / ресурсы 97.1 для 26.3**; 26.2 = 107.1 / 88.0; 26.1 = 101.1 / 84.0; 1.21.11 = 94.1 / 75.0. `overlays` (1.20.2+) кладут каталоги под разные версии в один пак; старый `pack_format` оставляйте, только если поддерживаете ещё и ≤1.21.8.

```json
{ "pack": { "description": "Example rebalance pack", "min_format": [121, 0], "max_format": [121, 0] } }
```

- `min_format` и `max_format` принимают целое (major, равно `[n, 0]`; в `max_format` означает любой minor) или массив `[major, minor]`. Они обязательны в `pack` и в каждом элементе `overlays.entries[]` (`directory`, `min_format`, `max_format`). `pack_format` и `supported_formats` разрешены и нужны, только если пак заявляет поддержку старых форматов (данные < 82, ресурсы < 65); иначе `supported_formats` «must be removed».
- **Имена папок**: с **1.21** (24w21a) папки реестров в единственном числе: `data/<ns>/function/`, `tags/function/`, `loot_table/`, `recipe/`, `advancement/`, `predicate/`, `item_modifier/`, `structure/` (папки содержимого переименованы в 24w21a; папки тегов `tags/items` в `tags/item`, `tags/blocks` в `tags/block`, `tags/entity_types` в `tags/entity_type` и т.д. в 24w19a). Старые папки во множественном числе молча игнорируются: это главная причина «мой пак ничего не делает» при следовании руководствам до 1.21.
- **Макросы** (1.20.2, 23w31a): строки, начинающиеся с `$`, подставляют `$(key)`. Вызов: `function ns:give_reward {item:"minecraft:diamond",count:3}` или `function ns:f with storage ns:state player` / `with entity @s` / `with block ~ ~ ~`. Если ключа нет, вся функция не выполняется. Строки макросов разбираются заново при каждом вызове (хорошо кэшируются, но медленнее статических строк); держите макро-строки минимальными.
- **Возвращаемые значения**: `/return <int>` (1.20, 23w16a), `return run <command>` (окончательно в 1.20.3, 23w41a; передаёт успех и результат команды), `return fail` (1.20.3, 23w44a). `execute if function ns:check run …` проверяет ненулевой результат (1.20.3). Применяйте для вспомогательных функций-предикатов.
- Лимиты: игровое правило `max_command_sequence_length` (по умолчанию 65 536 команд за тик; раньше `maxCommandChainLength`; игровые правила переименованы в snake_case в 1.21.11); `function-permission-level` в server.properties (по умолчанию 2).
- **Содержимое на данных продолжает расти** (для него предпочитайте датапаки): чары (1.21), варианты картин, волков, кошек, свиней, коров, куриц и лягушек, материалы и узоры отделки, типы урона, диалоги (1.21.6), тестовые экземпляры и окружения (1.21.5). **26.1** (выпущена 2026-03-24, первая на Java 25 и первая полностью без обфускации) добавила торговлю жителей на данных (`villager_trade`, `trade_set`), реестр `world_clock` (`/time` может указывать на часы; `default_clock` измерения), `has_ender_dragon_fight`, компонент `minecraft:dye` и разделение блочных тегов (`#dirt` сужен). В **26.3** появились компоненты предметов `minecraft:block_transformer` (превращения топором, мотыгой, лопатой теперь на данных), `cooking_fuel`, `brewing_fuel` (тег `#brewing_fuel` удалён), `compostable`, `villager_food`, `mob_visibility`, а также команды `/compute` (провайдеры чисел) и `/posteffect`. Варка стала данными (Paper пометил `PotionBrewer` deprecated в пользу `BrewingRecipe`). Источник один (сводка вики), точные имена полей сверяйте с официальным списком изменений `[не проверено]`.
- **Тестирование датапаков**:
  - В игре: `/test create|run|runfailed|verify` (доступны в обычной игре с **1.21.5**, 25w03a). Тесты — записи реестра `test_instance` (блочные с Test-блоками или на функциях), сгруппированные по `test_environment` (`minecraft:default`).
  - Headless в CI: `java -DbundlerMainClass="net.minecraft.gametest.Main" -jar server.jar --packs ./packs --tests "mypack:*" --report report.xml --universe ./gametest`. Код выхода равен числу провалов; отчёт в XML, похожем на JUnit. Команда не прогонялась локально, сверяйте с вики (`[не проверено]`).
  - Статические проверки: Spyglass (языковой сервер VS Code для mcfunction и JSON, учитывает версию), генераторы Misode для JSON, beet и mecha (сборка на Python и компилятор mcfunction) для больших паков. Выполните `/reload` и `/datapack list` и проверьте лог сервера на ошибки разбора. На Paper датапак можно встроить в плагин (раздел 4.3).
- Производительность датапака против плагина: функции тика (`#minecraft:tick`) выполняются каждый тик для каждого пака, защищайте их дешёвыми проверками `execute if score` или предикатом. Не используйте селекторы `@e` без ограничений `type=` и `distance=`. Для периодической работы берите `schedule function`.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Pack format](https://minecraft.wiki/w/Pack_format) | Minecraft Wiki, 2026 | номера форматов по версиям, min и max_format |
| [pack.mcmeta](https://minecraft.wiki/w/Pack.mcmeta) | Minecraft Wiki, 2026 | точный JSON для min и max_format и overlays |
| [Data pack](https://minecraft.wiki/w/Data_pack) | Minecraft Wiki | структура, имена папок в единственном числе, overlays |
| [Function (Java Edition)](https://minecraft.wiki/w/Function_(Java_Edition)) | Minecraft Wiki | макросы, return, лимиты |
| [Commands/return](https://minecraft.wiki/w/Commands/return) | Minecraft Wiki | return, run и fail с версиями |
| [Commands/test](https://minecraft.wiki/w/Commands/test) | Minecraft Wiki | синтаксис /test |
| [GameTest](https://minecraft.wiki/w/GameTest) | Minecraft Wiki | тестовые экземпляры, headless-запуск |
| [Java Edition 26.3](https://minecraft.wiki/w/Java_Edition_26.3) | Minecraft Wiki | новые компоненты и команды |
| [Java Edition 26.1](https://minecraft.wiki/w/Java_Edition_26.1) | Minecraft Wiki | 26.1 (2026-03-24): Java 25, без обфускации, торговля на данных |
| [Java Edition 1.21](https://minecraft.wiki/w/Java_Edition_1.21) | Minecraft Wiki | переименования папок, чары на данных |
| [Misode data pack generators](https://misode.github.io/) | Misode | генераторы и валидаторы JSON по версиям |
| [SpyglassMC/Spyglass](https://github.com/SpyglassMC/Spyglass) | SpyglassMC | языковой сервер для mcfunction и JSON |
| [beet](https://mcbeet.dev/) | Valentin Berlier | цепочка сборки паков на Python |
| [mcbeet/beet](https://github.com/mcbeet/beet) | mcbeet | исходники |
| [Minecraft Commands Wiki (r/MinecraftCommands)](https://minecraftcommands.github.io/wiki/) | сообщество | идиомы, производительность селекторов |

---

## 11. Инженерия плагина

### 11.1 Сборка и зависимости

- Используйте **Gradle Kotlin DSL** (инструменты Paper работают только с Gradle; Maven не рекомендуется). `compileOnly` для серверных API (paper-api, velocity-api, API LuckPerms, PlaceholderAPI, Vault и VaultUnlocked); никогда не шейдите их. Toolchain Java 25 (`options.release = 25`) для 26.x; для сборки, совместимой с 1.21.11, берите release 21 и компилируйтесь против API 1.21.11.
- Сторонние библиотеки, по убыванию предпочтения: (1) `libraries:` Paper (plugin.yml) или `PluginLoader` с `MavenLibraryResolver` (без shading, общий кэш загрузок, только зеркало Central); (2) **shading с relocation** через `com.gradleup.shadow` 9.6.1 (`relocate("com.zaxxer.hikari", "com.example.shop.libs.hikari")`): relocation обязательна для всего, что могут поставлять и другие плагины (Hikari, Caffeine, Configurate, bStats, stdlib Kotlin); (3) зависимость от плагина-библиотеки. Никогда не шейдите Adventure, Brigadier, Guava и Gson на Paper: их даёт сервер.
- `minimize()` в Shadow может вырезать классы, загружаемые рефлексией или через service loader (драйверы JDBC, провайдеры SLF4J): исключайте их. Объединяйте `META-INF/services` (`mergeServiceFiles()`).
- Плагины Velocity: то же правило. Velocity включает Guava, Gson, Adventure, Brigadier, Configurate (с SnakeYAML и HOCON), SLF4J и Log4j, fastutil, Netty и Guice (внедрение). Перед shading откройте страницу «Libraries used» Velocity; Caffeine в этом списке нет.

### 11.2 Конфигурация

- **Configurate 4.2** (`org.spongepowered:configurate-yaml|hocon|gson`) даёт типизированные `@ConfigSerializable` records и классы, комментарии, значения по умолчанию и **версионные миграции** (`ConfigurationTransformation.versionedBuilder()`). Для крошечных конфигов подходит и `FileConfiguration` Bukkit. Всегда записывайте значения по умолчанию при первой загрузке, проверяйте диапазоны при загрузке, падайте сразу с понятным сообщением.
- Храните предметы в конфиге как SNBT или сериализованные байты (`ItemStack#serializeAsBytes`, с data fixer), а не как «сырые» карты `ItemMeta` (не поддерживаются с 1.21.7). Сообщения в MiniMessage; файлы по локалям через Adventure `TranslationStore`.
- Перезагрузка: сделайте свою `/myplugin reload` (перечитать конфиг, атомарно подменить неизменяемый снимок через `AtomicReference`). Bukkit `/reload` не поддерживайте.

### 11.3 Базы данных и асинхронный ввод-вывод

- Пул **HikariCP 7.1** и драйвер JDBC (SQLite 3.54.1.0 для одного сервера, MariaDB, MySQL или PostgreSQL для сетей). Пул небольшой (≈ 2–10). Один выделенный `ExecutorService` (или виртуальные потоки на Java 25: `Executors.newVirtualThreadPerTaskExecutor()`; реальным ограничителем параллелизма остаётся пул Hikari). Никогда не трогайте JDBC из главного или региональных потоков.
- Данные игрока загружайте в `AsyncPlayerPreLoginEvent` (там блокировать можно, но есть тайм-аут) или асинхронно при входе, затем применяйте в планировщике сущности. Сохраняйте при выходе асинхронно, но **сбрасывайте синхронно в `onDisable`** (планировщик уже остановлен, асинхронные задачи, поставленные в `onDisable`, не выполнятся). Только prepared statements: SQL-инъекция через имена игроков и чат реальна. Миграции схемы через Flyway или Liquibase либо простую таблицу версий.
- Кэширование: Caffeine 3.3.1 для кэшей с чтением через кэш; в сетях инвалидируйте между серверами через Redis pub/sub.
- **PDC или база данных.** PDC (раздел 4.4) хранит данные прямо в сущности, предмете, чанке или мире и сохраняется вместе с ними. Он подходит, когда данные нужны только пока объект загружен: счётчики игрока, флаги, настройки предмета. Базу данных берите, если данные нужны, когда игрок не в сети (рейтинги, поиск по всем игрокам, веб-панель), если их делят несколько серверов за прокси или если их много и они растут без предела. Частый вариант — PDC для горячих данных игрока и база данных для общих.

### 11.4 Тестирование

- **Юнит-тесты**: обычный JUnit (JUnit Jupiter 6.1.3) для чистой логики в модулях `common`: самая большая польза. Проектируйте код так, чтобы логика без игры тестировалась без сервера.
- **MockBukkit** (`org.mockbukkit.mockbukkit:mockbukkit-v26.3:4.119.0`, пакет `org.mockbukkit.mockbukkit`): `ServerMock server = MockBukkit.mock(); MyPlugin plugin = MockBukkit.load(MyPlugin.class); PlayerMock p = server.addPlayer(); … MockBukkit.unmock();`. Нацелен на API Paper, один артефакт на версию MC; нереализованные методы бросают `UnimplementedOperationException` (тесты пропускаются или падают, см. troubleshooting в README). Lifecycle, реестры, bootstrap и планировщики Folia поддержаны частично или не поддержаны: тестируйте их интеграционно. **Находка CI**: `compileOnly` для paper-api не попадает в classpath тестов, а MockBukkit не приносит paper-api сам, поэтому нужна ещё и строка `testImplementation` с paper-api, иначе тест не компилируется («cannot access Server»). Проверено в CI на GitHub Actions; см. шаблон `build.gradle.kts` ниже.
- **Интеграция**: run-paper в CI со встроенным в плагин тестовым стендом (например, флаг `-Dmyplugin.selftest=true`, который прогоняет сценарии на `ServerLoadEvent`, пишет результаты в файл и вызывает `Bukkit.shutdown()`); проверка по коду выхода и логу. То же для Velocity с run-velocity и бэкендом. Testcontainers 2.0.5 для MariaDB и Redis в интеграционных тестах. У Minestom есть `net.minestom:testing` для собственных серверов. Проверенный в CI вариант запуска настоящего сервера — `paper_smoke.sh` из раздела «Шаблоны». Самотест-флаг `[не проверено]`.
- **Команды из bootstrapper.** Команды Brigadier, зарегистрированные через Lifecycle в bootstrapper, MockBukkit не видит. Проверяйте их на настоящем сервере: выполните команду с консоли и проверьте ответ в логе. `paper_smoke.sh` отправляет `visits reset Nobody`. Зарегистрированная команда отвечает ошибкой аргумента «No player was found», незарегистрированная — «Unknown or incomplete command». Проверено в CI на GitHub Actions.
- Датапаки: сервер gametest (раздел 10).

### 11.5 Производительность

- Профилируйте через **spark** (встроен в Paper с 1.21; `/spark profiler start --timeout 120`, `/spark tps`, `/spark health`, сводка кучи); `paper.preferSparkPlugin` включает отдельный плагин. Timings deprecated или удалён.
- Правила: никакого блокирующего ввода-вывода в главном или региональном потоке (файлы, HTTP, БД, DNS, запросы `UUID` через API Mojang); не грузите чанки неявно (`location.getBlock()` в незагруженном чанке грузит его синхронно: проверяйте `world.isChunkLoaded(x, z)` или используйте `getChunkAtAsync`); пакуйте изменения блоков; не делайте `getNearbyEntities` с большим радиусом каждый тик; используйте `PlayerMoveEvent#hasChangedBlock`; переиспользуйте `Component` для статичных сообщений; кэшируйте константы `NamespacedKey` и `TypedKey`; избегайте `Bukkit.getOfflinePlayer(String)` (может выполнять блокирующий веб-запрос). `[не проверено]` для части правил, кроме документации PaperMC.
- Память: очищайте карты по игрокам в `PlayerQuitEvent`; не держите ссылки на `Player`, `World`, `Chunk` в долгоживущих структурах (храните `UUID` или ключи); отменяйте задачи в `onDisable`.
- Измеряйте spark до и после; для чисто Java-горячих путей используйте JMH в модуле `common` (методика — в [minecraft-performance-testing.md](minecraft-performance-testing.md) и [performance.md](performance.md)).

### 11.6 Безопасность

- Считайте весь клиентский ввод враждебным: чат, текст табличек, страницы книг, названия на наковальне, аргументы команд, plugin messages, компоненты предметов из творческого режима. Ограничивайте длину, экранируйте для MiniMessage (`Placeholder.unparsed`), никогда не собирайте строки SQL или shell, никогда не десериализуйте Java-объекты из сети или plugin messages.
- Права: один узел на возможность (`shop.sell`, `shop.admin.reload`), для админских узлов по умолчанию `op` или `false`; проверяйте право при выполнении, а не только в `requires` (права меняются). Интегрируйте LuckPerms через стандартный `hasPermission`; API LuckPerms 5.5 нужен только для метаданных и групп.
- Прокси: современная пересылка с секретом; бэкенды за файрволом; обрабатывайте `PluginMessageEvent` с `handled()`; проверяйте, что «серверное» сообщение действительно пришло от соединения с бэкендом.
- От чего защищаться: дублирование предметов через события инвентаря и асинхронные сохранения (отменяйте и пересинхронизируйте, сохраняйте атомарно), NBT- и компонент-бомбы в творческом режиме (ограничивайте размеры), злоупотребление командными блоками и селекторами через `/execute` в командах плагина, DoS загрузкой чанков через команды телепорта и locate, payload для бана через книги. Держите сервер на последней сборке Paper (исправления эксплойтов не переносятся назад).
- Цепочка поставок: скачивайте плагины только с Hangar, Modrinth или релизов GitHub; анонс PaperMC 2022 года о вредоносном ПО показывает реальные заражения JAR; подписывайте и проверяйте собственные релизные артефакты (PaperMC планирует подпись JAR).
- Секреты (пароли БД, вебхуки) держите в конфигах с ограниченными правами доступа или в переменных окружения и никогда не логируйте.

### 11.7 Несколько версий Minecraft

- Paper поддерживает только последнюю версию MC; планируйте выпуск плагина в течение нескольких дней после каждого дропа (Mojang 26.1 — 2026-03-24, 26.3 — 2026-09-15; посты Paper вышли позже: 26.1 — 04-26, 26.2 — 06-12, 26.3 — 09-26; ранние сборки Paper в канале alpha).
- Стратегия окна поддержки: последняя 26.x (Java 25) плюс, при желании, 1.21.11 (Java 21), отдельными сборками, если используете компоненты данных и реестры. Один JAR, скомпилированный с `--release 21` против API 1.21.11, часто работает и на 26.x, если не использовать удалённые API (проверьте). `api-version: '1.21.11'` грузится на 26.x; `api-version: '26.3'` отказывается грузиться на старых серверах.
- Разбирайте версии надёжно: не предполагайте префикс `1.` (на 26.x он ломается); используйте `ServerBuildInfo.buildInfo().minecraftVersionId()` (также `brandId()`, `buildNumber()`, `isBrandCompatible(Key)`), а не разбор строк `Bukkit.getVersion()`.
- Следите за `@ApiStatus.Experimental` (реестры, обнаружение датапаков, плагины Paper) и `@Deprecated(forRemoval=true)`; запускайте `javac -Xlint:deprecation,removal` в CI и падайте на предупреждениях об удалении.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Using databases](https://docs.papermc.io/paper/dev/using-databases/) | PaperMC docs | HikariCP, асинхронность, SQLite и MySQL |
| [Plugin configurations](https://docs.papermc.io/paper/dev/plugin-configurations/) | PaperMC docs | конфиг Bukkit и Configurate |
| [Debugging your plugin](https://docs.papermc.io/paper/dev/debugging/) | PaperMC docs | удалённая отладка, hotswap |
| [Reading stacktraces](https://docs.papermc.io/paper/dev/reading-stacktraces/) | PaperMC docs | диагностика ошибок плагинов |
| [SpongePowered/Configurate](https://github.com/SpongePowered/Configurate) | SpongePowered | типизированный конфиг, трансформации |
| [Configurate wiki](https://github.com/SpongePowered/Configurate/wiki) | SpongePowered | маппинг объектов, миграции |
| [brettwooldridge/HikariCP](https://github.com/brettwooldridge/HikariCP) | B. Wooldridge | настройка пула, размер |
| [About Pool Sizing](https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing) | HikariCP wiki | почему пулы маленькие |
| [ben-manes/caffeine](https://github.com/ben-manes/caffeine) | B. Manes | кэширование |
| [GradleUp/shadow](https://github.com/GradleUp/shadow) | GradleUp | shading и relocation (`com.gradleup.shadow`) |
| [Shadow user guide](https://gradleup.com/shadow/) | GradleUp | relocate, minimize, service-файлы |
| [MockBukkit/MockBukkit](https://github.com/MockBukkit/MockBukkit) | MockBukkit | юнит-тесты плагинов Paper |
| [MockBukkit docs](https://docs.mockbukkit.org/) | MockBukkit | начало работы, моки |
| [Testcontainers for Java](https://java.testcontainers.org/) | Docker/AtomicJar | БД и Redis в интеграционных тестах |
| [lucko/spark](https://github.com/lucko/spark) | lucko | исходники профилировщика |
| [spark docs](https://spark.lucko.me/docs) | lucko | использование профилировщика, флаги |
| [Profiling (Paper)](https://docs.papermc.io/paper/profiling/) | PaperMC docs | spark на Paper |
| [Paper global configuration](https://docs.papermc.io/paper/reference/global-configuration/) | PaperMC docs | прокси, spark, ограничитель пакетов |
| [Anti-Xray](https://docs.papermc.io/paper/anti-xray/) | PaperMC docs | встроенная обфускация руд |
| [Aikar's flags](https://docs.papermc.io/paper/aikars-flags/) | PaperMC docs | флаги GC (есть и в Fill API) |
| [LuckPerms Developer API](https://luckperms.net/wiki/Developer-API) | lucko | интеграция прав и метаданных |
| [Incendo/cloud](https://github.com/Incendo/cloud) | Incendo | кроссплатформенный фреймворк команд (cloud-paper 2.0.1) |
| [Malware Announcement](https://papermc.io/news/malware-announcement) | PaperMC, 2022 | инцидент в цепочке поставок |
| [bStats docs](https://bstats.org/docs) | bStats | метрики (relocate класса) |

---

## 12. Публикация и распространение

**Ключевые выводы**

- **Hangar** (hangar.papermc.io, репозиторий PaperMC с 2023-04-20) для плагинов Paper, Velocity и Waterfall: проекты, организации, каналы (`Release`, свой `Snapshot`), диапазоны версий платформы (`1.21.11, 26.1-26.3`, шаблоны `26.x`). Автоматизация через `io.papermc.hangar-publish-plugin` 0.1.4: `./gradlew build publishPluginPublicationToHangar` с `HANGAR_API_TOKEN` (право `create_version`) как секретом GitHub; для каждой платформы регистрируйте `platforms { register(Platforms.PAPER) { jar.set(tasks.shadowJar.flatMap { it.archiveFile }) ... } }`.
- **Modrinth** тоже хостит плагины (загрузчики `paper`, `purpur`, `folia`, `velocity`, `bungeecord`, `spigot`) и является основным хостом модов Fabric и NeoForge и датапаков. Загрузка через Minotaur 2.10.0 (`com.modrinth.minotaur`); запросы через Modrinth API v2 (`/v2/project/{id}/version`). Список загрузчиков `[не проверено]`.
- Ресурсы SpigotMC остаются для плагинов, совместимых со Spigot (ручная загрузка). bStats даёт анонимные метрики использования (relocate). Публикуйте контрольные суммы SHA-256; Paper планирует подписанные JAR для себя, не для плагинов.
- Версионируйте плагин в стиле semver и явно указывайте поддерживаемые версии MC; диапазоны версий на Hangar и Modrinth должны совпадать с `api-version` и проверенными сборками.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Hangar](https://hangar.papermc.io/) | PaperMC | репозиторий плагинов |
| [Hangar - PaperMC's Plugin Repository](https://papermc.io/news/hangar-papermcs-plugin-repository) | PaperMC, 2023-04-20 | пост о запуске |
| [Hangar auto-publishing](https://docs.papermc.io/misc/hangar-publishing/) | PaperMC docs | Gradle и workflow Actions |
| [HangarMC/hangar-publish-plugin](https://github.com/HangarMC/hangar-publish-plugin) | HangarMC | исходники плагина Gradle |
| [PaperMC/Hangar](https://github.com/PaperMC/Hangar) | PaperMC | исходники Hangar, API |
| [Modrinth API docs](https://docs.modrinth.com/api/) | Modrinth | API загрузки и запросов |
| [modrinth/minotaur](https://github.com/modrinth/minotaur) | Modrinth | плагин Gradle для загрузки |
| [Modrinth plugins](https://modrinth.com/plugins) | Modrinth | каталог плагинов |

---

## Шаблоны

Версии на 2026-11-07 взяты из метаданных: paper-api `26.3.build.244-stable`, paperweight-userdev `2.0.0-beta.24` (нужен Gradle ≥ 9.7.1), run-paper и run-velocity `3.1.0`, velocity-api `4.3.1`, foojay-resolver-convention `1.0.0`, Gradle `9.9.0` (в `gradle-wrapper.properties`: `distributionUrl=https\://services.gradle.org/distributions/gradle-9.9.0-bin.zip`), Shadow `9.6.1`, MockBukkit `mockbukkit-v26.3:4.119.0`, JUnit `6.1.3`.

**Что проверил CI** (workflow `.github/workflows/minecraft.yml` в репозитории навыка, GitHub Actions, Ubuntu, Temurin 25):

- плагин Paper собирается на JDK 25 (`./gradlew build`);
- тест MockBukkit проходит;
- настоящий сервер Paper 26.3 build 244 запускается с собранным плагином и пишет в лог «Enabling ExamplePlugin» (плагин с bootstrapper загружен и включён; выполнение команды `/visits` в CI не проверялось);
- процессор аннотаций Velocity генерирует `velocity-plugin.json` при сборке плагина Velocity на JDK 25 (в CI он распаковывается из JAR). Запуск плагина Velocity на настоящем прокси в CI не проверялся.

**Находка CI**: `compileOnly` для paper-api не попадает в classpath тестов, а MockBukkit не приносит paper-api сам. Нужна ещё одна строка `testImplementation` с paper-api. Без неё компиляция тестов падает с ошибкой «cannot access Server». Исправление уже есть в `build.gradle.kts` ниже.

Рабочий workflow лежит в репозитории навыка (`.github/workflows/minecraft.yml`). Файлы ниже скопированы из репозитория без изменений. Проверено в CI на GitHub Actions.

### Плагин Paper

Минимальный плагин Paper: обычный paper-api, `paper-plugin.yml`, команда Brigadier из bootstrapper, PDC, MiniMessage с безопасной подстановкой имени игрока. Структура: `settings.gradle.kts`, `build.gradle.kts`, `src/main/resources/paper-plugin.yml`, три класса в `src/main/java/com/example/plugin/` и тест в `src/test/java/com/example/plugin/`. Обёртка Gradle (`gradlew`, `gradle/wrapper/`) берётся из любого проекта Gradle 9.9.0.

<!-- template: tests/minecraft/paper-plugin/settings.gradle.kts -->
```kotlin
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "example-paper-plugin"
```

<!-- template: tests/minecraft/paper-plugin/build.gradle.kts -->
```kotlin
plugins {
    java
    id("xyz.jpenilla.run-paper") version "3.1.0"
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") { name = "papermc" }
}

dependencies {
    // Pin for reproducible CI; use "26.3.build.+" to track latest (never "latest.release": metadata <latest> is 26.3-pre-2.build.0-alpha)
    compileOnly("io.papermc.paper:paper-api:26.3.build.244-stable")
    // compileOnly does not reach the test classpath, and MockBukkit does not bring paper-api itself
    testImplementation("io.papermc.paper:paper-api:26.3.build.244-stable")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v26.3:4.119.0")
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java { toolchain.languageVersion = JavaLanguageVersion.of(25) }

tasks {
    compileJava {
        options.release = 25
        options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:removal"))
    }
    processResources {
        val props = mapOf("version" to project.version)
        inputs.properties(props)
        filesMatching("paper-plugin.yml") { expand(props) }
    }
    test { useJUnitPlatform() }
    runServer {
        minecraftVersion("26.3")
        jvmArgs("-Dcom.mojang.eula.agree=true")
    }
}
```

<!-- template: tests/minecraft/paper-plugin/src/main/resources/paper-plugin.yml -->
```yaml
name: ExamplePlugin
version: '${version}'
main: com.example.plugin.ExamplePlugin
bootstrapper: com.example.plugin.ExampleBootstrap
api-version: '26.3'
description: Minimal Paper 26.3 plugin (Brigadier + PDC)
folia-supported: false
permissions:
  example.visits.reset:
    default: op
```

<!-- template: tests/minecraft/paper-plugin/src/main/java/com/example/plugin/ExampleBootstrap.java -->
```java
package com.example.plugin;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import java.util.List;

public final class ExampleBootstrap implements PluginBootstrap {
    @Override
    public void bootstrap(BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
            event.registrar().register(
                Commands.literal("visits")
                    .then(Commands.literal("reset")
                        .requires(src -> src.getSender().hasPermission("example.visits.reset"))
                        .then(Commands.argument("target", ArgumentTypes.player())
                            .executes(ctx -> {
                                PlayerSelectorArgumentResolver r =
                                    ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                                Player target = r.resolve(ctx.getSource()).getFirst();
                                VisitCounter.reset(target);
                                ctx.getSource().getSender().sendMessage(
                                    Component.text("Reset visits for " + target.getName()));
                                return Command.SINGLE_SUCCESS;
                            })))
                    .build(),
                "Show or reset visit counters",
                List.of()));
    }
}
```

<!-- template: tests/minecraft/paper-plugin/src/main/java/com/example/plugin/VisitCounter.java -->
```java
package com.example.plugin;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

final class VisitCounter {
    static final NamespacedKey KEY = new NamespacedKey("exampleplugin", "visits");
    private VisitCounter() {}

    static int increment(Player p) {
        int next = p.getPersistentDataContainer().getOrDefault(KEY, PersistentDataType.INTEGER, 0) + 1;
        p.getPersistentDataContainer().set(KEY, PersistentDataType.INTEGER, next);
        return next;
    }

    static void reset(Player p) { p.getPersistentDataContainer().remove(KEY); }
}
```

<!-- template: tests/minecraft/paper-plugin/src/main/java/com/example/plugin/ExamplePlugin.java -->
```java
package com.example.plugin;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class ExamplePlugin extends JavaPlugin implements Listener {
    private static final MiniMessage MM = MiniMessage.miniMessage();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        int visits = VisitCounter.increment(event.getPlayer());
        // player name inserted unparsed: no tag injection
        event.getPlayer().sendMessage(MM.deserialize("<green>Welcome <name>, visit #<count>",
            Placeholder.unparsed("name", event.getPlayer().getName()),
            Placeholder.unparsed("count", Integer.toString(visits))));
    }
}
```

Тест. Поддержка bootstrapper и Lifecycle в MockBukkit не гарантирована, поэтому тест проверяет только `VisitCounter` через `server.addPlayer()`. Если `MockBukkit.load` падает из-за bootstrapper, делайте так же.

<!-- template: tests/minecraft/paper-plugin/src/test/java/com/example/plugin/ExamplePluginTest.java -->
```java
package com.example.plugin;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ExamplePluginTest {
    private ServerMock server;

    @BeforeEach void setUp() { server = MockBukkit.mock(); }
    @AfterEach void tearDown() { MockBukkit.unmock(); }

    @Test void counterIncrementsPerCall() {
        PlayerMock p = server.addPlayer();
        assertEquals(1, VisitCounter.increment(p));
        assertEquals(2, VisitCounter.increment(p));
    }
}
```

### Запуск настоящего сервера Paper в CI

Скрипт запускается из корня репозитория после `./gradlew build` в `tests/minecraft/paper-plugin`. Он скачивает Paper через Fill v3 с проверкой SHA-256, кладёт JAR плагина в `run/plugins`, ждёт в логе строку `Done (`, отправляет `stop` в stdin сервера и проверяет, что в логе есть «Enabling ExamplePlugin» и нет «Error occurred while enabling» или «Could not load plugin». Номер сборки (`BUILD=244`) должен совпадать с версией paper-api в `build.gradle.kts`.

<!-- template: tests/minecraft/paper_smoke.sh -->
```bash
#!/usr/bin/env bash
# Запускает настоящий сервер Paper с собранным плагином и проверяет, что плагин включился.
# Запуск из корня репозитория после ./gradlew build в tests/minecraft/paper-plugin:
#   bash tests/minecraft/paper_smoke.sh
set -euo pipefail
MC=26.3
BUILD=244   # та же сборка, что paper-api 26.3.build.244-stable в build.gradle.kts
cd "$(dirname "$0")/paper-plugin"
rm -rf run && mkdir -p run/plugins
cp build/libs/*.jar run/plugins/
cd run

meta=$(curl -sf "https://fill.papermc.io/v3/projects/paper/versions/$MC/builds/$BUILD")
url=$(jq -r '.downloads."server:default".url' <<<"$meta")
sha=$(jq -r '.downloads."server:default".checksums.sha256' <<<"$meta")
curl -sfL -o paper.jar "$url"
echo "$sha  paper.jar" | sha256sum -c -
echo "eula=true" > eula.txt

# stdin сервера: ждём строку «Done (» в логе, выполняем команду плагина с консоли и отправляем stop.
# Команда зарегистрирована в bootstrapper, а MockBukkit bootstrapper не запускает, поэтому проверяем её здесь:
# зарегистрированная команда отвечает «No player was found», незарегистрированная — «Unknown or incomplete command».
touch server.log
timeout 600 java -Xms1G -Xmx2G -jar paper.jar --nogui > server.log 2>&1 < <(
    until grep -q 'Done (' server.log; do sleep 2; done
    echo "visits reset Nobody"
    sleep 3
    echo stop
) || { echo "server did not stop in time"; tail -50 server.log; exit 1; }

grep -E 'Enabling ExamplePlugin|Done \(|No player was found|Unknown or incomplete command' server.log
if grep -q 'Error occurred while enabling ExamplePlugin\|Could not load plugin\|Unknown or incomplete command' server.log; then
    tail -80 server.log
    exit 1
fi
grep -q 'Enabling ExamplePlugin' server.log
grep -q 'No player was found' server.log
echo "paper smoke test passed"
```

### Плагин Velocity

Скелет плагина Velocity: `@Plugin`, внедрение Guice, команда Brigadier, регистрация в `ProxyInitializeEvent`. Процессор аннотаций из `velocity-api` генерирует `velocity-plugin.json` при сборке. Структура: `settings.gradle.kts`, `build.gradle.kts` и `src/main/java/com/example/proxy/NetworkPlugin.java`.

<!-- template: tests/minecraft/velocity-plugin/settings.gradle.kts -->
```kotlin
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "example-velocity-plugin"
```

<!-- template: tests/minecraft/velocity-plugin/build.gradle.kts -->
```kotlin
plugins {
    java
    id("xyz.jpenilla.run-velocity") version "3.1.0"
}
repositories {
    maven("https://repo.papermc.io/repository/maven-public/") { name = "papermc" }
}
dependencies {
    compileOnly("com.velocitypowered:velocity-api:4.3.1")
    annotationProcessor("com.velocitypowered:velocity-api:4.3.1") // generates velocity-plugin.json
}
java { toolchain.languageVersion = JavaLanguageVersion.of(25) }
tasks.compileJava { options.release = 25 }
tasks.runVelocity { velocityVersion("4.3.1") }
```

<!-- template: tests/minecraft/velocity-plugin/src/main/java/com/example/proxy/NetworkPlugin.java -->
```java
package com.example.proxy;

import com.google.inject.Inject;
import com.mojang.brigadier.Command;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;
import java.nio.file.Path;

@Plugin(id = "example-network", name = "ExampleNetwork", version = "1.0.0",
        description = "Velocity skeleton", authors = {"Example"})
public final class NetworkPlugin {
    private final ProxyServer proxy;
    private final Logger logger;
    private final Path dataDir;

    @Inject
    public NetworkPlugin(ProxyServer proxy, Logger logger, @DataDirectory Path dataDir) {
        this.proxy = proxy;           // construction: store fields only
        this.logger = logger;
        this.dataDir = dataDir;
    }

    @Subscribe
    public void onInit(ProxyInitializeEvent event) {
        BrigadierCommand cmd = new BrigadierCommand(
            BrigadierCommand.literalArgumentBuilder("online")
                .executes(ctx -> {
                    ctx.getSource().sendMessage(
                        Component.text("Players online: " + proxy.getPlayerCount()));
                    return Command.SINGLE_SUCCESS;
                })
                .build());
        CommandMeta meta = proxy.getCommandManager().metaBuilder("online").plugin(this).build();
        proxy.getCommandManager().register(meta, cmd);
        logger.info("ExampleNetwork ready, data dir {}", dataDir);
    }
}
```

### Вариант paperweight-userdev

`[не проверено]` Доступ к внутренностям сервера. Не собирался ни локально, ни в CI. Первый `paperweightUserdevSetup` декомпилирует и патчит сервер (минуты, несколько ГБ ОЗУ), поэтому кэшируйте `~/.gradle/caches/paperweight-userdev` между запусками.

```kotlin
// settings.gradle.kts additionally (only needed for userdev SNAPSHOTs):
// pluginManagement { repositories { gradlePluginPortal(); maven("https://repo.papermc.io/repository/maven-public/") } }

plugins {
    java
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.24" // requires Gradle >= 9.7.1
    id("xyz.jpenilla.run-paper") version "3.1.0"
}
dependencies {
    paperweight.paperDevBundle("26.3.build.244-stable") // replaces paper-api; Folia: paperweight.foliaDevBundle(...)
}
java { toolchain.languageVersion = JavaLanguageVersion.of(25) }
// No reobfJar / reobfArtifactConfiguration for 26.x: the server is unobfuscated.
```

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [paper-api maven-metadata](https://repo.papermc.io/repository/maven-public/io/papermc/paper/paper-api/maven-metadata.xml) | PaperMC repo | точные версии сборок |
| [paperweight-userdev on Gradle Plugin Portal](https://plugins.gradle.org/plugin/io.papermc.paperweight.userdev) | Gradle | версии плагина |
| [run-paper on Gradle Plugin Portal](https://plugins.gradle.org/plugin/xyz.jpenilla.run-paper) | Gradle | версии плагина |
| [Fill v3 API: projects](https://fill.papermc.io/v3/projects) | PaperMC | машиночитаемые версии, минимумы Java, статус поддержки |
| [Paper Commands.java source](https://github.com/PaperMC/Paper/blob/main/paper-api/src/main/java/io/papermc/paper/command/brigadier/Commands.java) | PaperMC | перегрузки регистрации |
| [MockBukkit on Maven Central](https://central.sonatype.com/artifact/org.mockbukkit.mockbukkit/mockbukkit-v26.3) | Sonatype | версии |
| [gradle/actions](https://github.com/gradle/actions) | Gradle | кэширование setup-gradle |
| [actions/setup-java](https://github.com/actions/setup-java) | GitHub | установка JDK 25 |

---

## Чеклист ревью плагина

**Сборка и метаданные**
- [ ] 1. Серверные API (`paper-api`, `velocity-api`, API других плагинов) подключены как `compileOnly` и не шейдятся; шейдимые сторонние библиотеки перенесены (relocate) в пакет плагина или грузятся через `libraries:` или `PluginLoader`.
- [ ] 2. Зависимость Paper использует схему 26.x (`26.3.build.N-stable` или `26.3.build.+`), не `-R0.1-SNAPSHOT` и не `latest.release`.
- [ ] 3. Toolchain и `--release` соответствуют цели (25 для 26.x, 21 для ≤1.21.11); `api-version` задан и согласован с самой старой поддерживаемой версией MC.
- [ ] 4. Для 26.x нет `reobfJar` и NMS на маппингах Spigot; любой NMS спрятан за интерфейсом с аккуратным путём отказа.
- [ ] 5. `plugin.yml` или `paper-plugin.yml` сгенерирован из Gradle или синхронизирован с версией проекта; `folia-supported` ставится только если Folia действительно тестировали.
- [ ] 6. Нет использования `@Deprecated(forRemoval=true)` (компиляция с `-Xlint:removal`), нет `ChatColor` и строк с §, нет `PlayerLoginEvent` в новом коде, нет `PotionBrewer` (26.3).
- [ ] 7. Тестовый classpath содержит paper-api (`testImplementation`) рядом с MockBukkit; `compileOnly` один его туда не приносит.

**Потоки и производительность**
- [ ] 8. Нет блокирующего ввода-вывода (БД, файлы, HTTP, DNS, запросы Mojang, `getOfflinePlayer(String)`) в главном и региональных потоках.
- [ ] 9. Состояние мира, сущностей и инвентаря трогается только из потока-владельца (главный на Paper; планировщик региона или сущности на Folia); результаты асинхронных задач возвращаются через планировщик.
- [ ] 10. Нет `.join()` и `.get()` на futures из главного потока; у futures есть тайм-ауты и обработчики исключений (`exceptionally`, `whenComplete`), которые пишут в лог.
- [ ] 11. Горячие обработчики событий (`PlayerMoveEvent`, физика, клик по инвентарю) делают дешёвые проверки с ранним выходом, без аллокаций и ввода-вывода.
- [ ] 12. Нет неявной синхронной загрузки чанков (проверяется `isChunkLoaded`, используются `getChunkAtAsync` и `teleportAsync`).
- [ ] 13. Повторяющиеся задачи выполняют ограниченный объём работы за запуск, отменяются в `onDisable` и для работы по игроку используют планировщик сущности (готовность к Folia).
- [ ] 14. Кэши по игрокам ключуются по `UUID` и очищаются при выходе; нет долгоживущих ссылок на `Player`, `World`, `Chunk`.
- [ ] 15. Сохранение данных в `onDisable` синхронное (асинхронный планировщик к этому моменту остановлен) и атомарное (временный файл и перемещение или транзакция БД).

**Безопасность**
- [ ] 16. Каждая команда и подкоманда проверяет конкретное право при выполнении; админские узлы по умолчанию `op` или `false`.
- [ ] 17. Недоверенный текст вставляется в MiniMessage только через `Placeholder.unparsed` или `Component.text`; длины ограничены.
- [ ] 18. SQL использует только prepared statements, без конкатенации строк с данными игроков.
- [ ] 19. Обработчики plugin messaging проверяют источник (бэкенд или игрок) и на Velocity задают `ForwardResult.handled()` для своих каналов.
- [ ] 20. Нет десериализации Java-объектов из сети или конфигов; данные предметов сериализуются через `serializeAsBytes` или SNBT, а не «сырыми» картами `ItemMeta`.
- [ ] 21. Код инвентарей и GUI опознаёт меню через свой `InventoryHolder` или `MenuType`, отменяет клики перед действием и перепроверяет состояние предмета (защита от дюпа).
- [ ] 22. Секреты (пароли БД, токены, вебхуки) не логируются и не коммитятся; права доступа к конфигу описаны.
- [ ] 23. Развёртывания с прокси используют современную пересылку с секретом; в документации админам сказано закрыть бэкенды файрволом.

**Использование API и корректность**
- [ ] 24. Команды используют Brigadier через `LifecycleEvents.COMMANDS` (или поддерживаемый фреймворк), с нативными типами аргументов и подсказками.
- [ ] 25. Правки `ItemMeta` идут через `editMeta` и `editPersistentDataContainer`; нет предположения, что `getItemMeta()` живой.
- [ ] 26. Ключи PDC привязаны к namespace плагина, созданы один раз и согласованы по типам между версиями (есть путь миграции при смене типа).
- [ ] 27. Код bootstrap для реестров и датапаков использует собственный namespace и не бросает исключений в ожидаемых ситуациях (исключение прерывает запуск сервера).
- [ ] 28. Проверки версии идут через `ServerBuildInfo`, а не через разбор строк с предположением о префиксе `1.`.
- [ ] 29. Учтены игроки Bedrock (Floodgate) и ViaVersion: возможности, требующие новых функций клиента, деградируют аккуратно.

**Качество и эксплуатация**
- [ ] 30. Есть юнит-тесты для логики без игры, тесты MockBukkit для слушателей и команд там, где это поддержано, и интеграционный smoke-тест настоящего сервера в CI.
- [ ] 31. У конфига есть значения по умолчанию, проверка с понятными ошибками и версионная миграция; `/plugin reload` реализован самим плагином; на Bukkit `/reload` плагин не рассчитывает.
- [ ] 32. Логи идут через логгер плагина (SLF4J или ComponentLogger), без `printStackTrace` и спама в каждый тик; в ошибках есть контекст (игрок, мир, операция).
- [ ] 33. Для всего, что тикает, перед релизом снят профиль spark под реалистичной нагрузкой и результаты сравнены с базовой линией.
- [ ] 34. Поддерживаемые версии MC, Paper и Velocity в README, на Hangar и Modrinth совпадают с тем, что CI реально проверил.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Basic troubleshooting](https://docs.papermc.io/paper/basic-troubleshooting/) | PaperMC docs | сбои на стороне администратора, которые ревьюер должен предвидеть |
| [Paper bug fixes](https://docs.papermc.io/paper/misc/paper-bug-fixes/) | PaperMC docs | отличия от ванили, на которые опираются плагины |
| [Paper update checker](https://docs.papermc.io/paper/misc/update-checker/) | PaperMC docs | почему серверы должны быть актуальны |

---

## Открытые вопросы

- **Ломающие изменения API Velocity 4.0** сверх Java 25 и Adventure 5 (например, удалённое deprecated API, изменения обработчиков): страницы с release notes не нашлось, прочитан только список коммитов. Перед переносом больших плагинов прокси сравните `velocity-api` между 3.5.1 и 4.0.0.
- **Покрытие MockBukkit** для плагинов Paper с bootstrapper, Lifecycle, событиями реестров и планировщиками Folia не проверено; шаблон теста откатывается к проверке обычной логики. Пути классов `org.mockbukkit.mockbukkit.{MockBukkit, ServerMock, entity.PlayerMock}` подтверждены на ветке `minecraft/v26.3`, а сборка и тест проходят в CI.
- **Использование run-paper в CI**: `downloadPlugins { modrinth/hangar }` и неинтерактивный механизм остановки через run-paper не перечитаны в документации 3.1.0 `[не проверено]`. Проверенный в CI способ остановки — `paper_smoke.sh`, который ждёт `Done (` и отправляет `stop`.
- minecraft.wiki и spigotmc.org отвечают curl кодом HTTP 403 (защита от ботов); страницы вики прочитаны через WebFetch, страницы Spigot раньше отдавали 200 на простой curl. Ссылки на них рабочие в браузерах.
- **Дополнения 26.3 на данных** (block_transformer, cooking_fuel, brewing_fuel и др.) взяты из сводной выдержки вики; перед обучением точным именам полей сверьтесь с официальным списком изменений.
- **Ванильные клиенты на серверах NeoForge и Fabric**: точные условия (синхронизация реестров, необязательные payload, замена `displayTest` в NeoForge 26.x) не проверены; в документации NeoForge 26.1 `displayTest` не упоминается.
- **Pufferfish**: публичной активности для 26.x не замечено; существует ли закрытая или коммерческая сборка 26.x, неизвестно.
- **Сроки релиза Sponge API 18–21** (пока только SNAPSHOT) и нацелен ли SpongeForge на NeoForge для 26.x (есть ветка `update/1.21.5-neo`) не проверены.
- **Формат UUID Floodgate и префиксы имён по умолчанию** приведены по общим знаниям `[не проверено]`.
- **Слияние конфигов Paper в 26.4** — план (issue #14304), а не выпущенная функция; проверьте перед описанием путей конфигов для 26.4.
- **paper-api `26.3-pre-2.build.0-alpha`** значился как `<latest>` в maven-metadata на 2026-11-06; происхождение неясно (возможно, неверно помеченная предрелизная сборка 26.4). Практический совет (закрепляйте префикс `26.3.build.`) верен в любом случае.
- **«Fully compatible with Spigot and Paper plugins» у Leaf** — собственное заявление проекта; асинхронный трекер сущностей и поиск пути могут ломать плагины; независимого исследования совместимости не найдено.
