# Моды Minecraft: архитектура, API игры и паттерны

Справочник по устройству мода Minecraft: Java Edition и по API игры и загрузчиков: **Fabric** (Fabric Loader и Fabric API) и **NeoForge** рядом, на Minecraft 26.1–26.3 (текущая стабильная линия), с пометками о том, где отличается последняя линия 1.21.x (1.21.11 и долгоживущая 1.21.1 — основа больших сборок). Покрыты: стороны и потоки, реестры, компоненты данных, блоки, блок-сущности и сущности, меню, рецепты, лут, достижения и зачарования как данные, datagen, Codec и StreamCodec, сеть и её безопасность, события, Mixin и MixinExtras, access wideners, class tweakers и access transformers, возможности, вложения и передача предметов, жидкостей и энергии, рендеринг (Blaze3D, Renderpearl, Vulkan), конфигурация, команды, клавиши, генерация мира, сохранённые данные, перенос между версиями и чеклист ревью. Примеры намеренно разнесены по видам модов: контентные, технические, магические, генерация мира, интерфейс и QoL, клиентские, серверные, оптимизация, библиотеки.

Актуально на ноябрь 2026: Minecraft 26.3 «Wilderness Bound» (2026-09-15), Java 25 для 26.1+ (Java 21 для 1.20.5–1.21.11), NeoForge 26.3.0.101, Fabric Loader 0.20.1, Fabric Loom 1.19.4, Fabric API 0.165.2+26.3, ModDevGradle 2.0.148, MixinExtras 0.5.5. Пометки: `[не проверено]` — факт из одного вторичного источника, не открытый целиком, либо шаблон кода, который не собирался в CI; `[устарело]` — рядом названа замена. Все ссылки проверены на доступность 2026-11-07; minecraft.wiki отвечает 403 на curl (защита от ботов), но открывается в браузере. Сокращения: «primer 26.x» — порт-примеры NeoForged авторства ChampionAsh5357 (`github.com/neoforged/.github/primers/<версия>/index.md`; описывают изменения самой игры, не зависят от загрузчика). «Пост Fabric 26.x» — релизная запись в блоге Fabric.

Сборка проекта, Loom, ModDevGradle, мультилоадер и публикация — [minecraft-modding.md](minecraft-modding.md). Замеры, профилирование, GameTest и отладка — [minecraft-performance-testing.md](minecraft-performance-testing.md). Серверные плагины и датапаки — [minecraft-server-plugins.md](minecraft-server-plugins.md). Документация, книги и эталонные кодовые базы — [minecraft-resources.md](minecraft-resources.md). Общие принципы архитектуры — [architecture.md](architecture.md), JMH и профилирование — [performance.md](performance.md), тесты — [testing.md](testing.md).

- [Версии на ноябрь 2026](#версии-на-ноябрь-2026)
- [Что сломалось в 26.x](#что-сломалось-в-26x)
- [Карта по типам модов](#карта-по-типам-модов)
- [1. Анатомия мода: точки входа и метаданные](#1-анатомия-мода-точки-входа-и-метаданные)
- [2. Стороны и потоки](#2-стороны-и-потоки)
- [3. Жизненный цикл, реестры, идентификаторы, теги](#3-жизненный-цикл-реестры-идентификаторы-теги)
- [4. Предметы и компоненты данных](#4-предметы-и-компоненты-данных)
- [5. Блоки, состояния блоков, блок-сущности](#5-блоки-состояния-блоков-блок-сущности)
- [6. Сущности, ИИ и мозги](#6-сущности-ии-и-мозги)
- [7. Меню и экраны](#7-меню-и-экраны)
- [8. Рецепты, лут, достижения, зачарования, торговля, звуки, частицы](#8-рецепты-лут-достижения-зачарования-торговля-звуки-частицы)
- [9. Данные вместо кода: датапаки, ресурспаки, datagen, Codec и StreamCodec](#9-данные-вместо-кода-датапаки-ресурспаки-datagen-codec-и-streamcodec)
- [10. Сеть: пакеты, синхронизация, безопасность](#10-сеть-пакеты-синхронизация-безопасность)
- [11. События и хуки: шины NeoForge, колбэки Fabric](#11-события-и-хуки-шины-neoforge-колбэки-fabric)
- [Миксины](#миксины)
- [12. Access wideners, class tweakers и access transformers](#12-access-wideners-class-tweakers-и-access-transformers)
- [13. Возможности, вложения и передача предметов, жидкостей, энергии](#13-возможности-вложения-и-передача-предметов-жидкостей-энергии)
- [14. Рендеринг и клиент](#14-рендеринг-и-клиент)
- [15. Конфигурация](#15-конфигурация)
- [16. Команды, клавиши, игровые правила](#16-команды-клавиши-игровые-правила)
- [17. Генерация мира](#17-генерация-мира)
- [18. Сохранённые данные и постоянное состояние](#18-сохранённые-данные-и-постоянное-состояние)
- [19. Перенос между версиями](#19-перенос-между-версиями)
- [20. Карта библиотек и экосистемы](#20-карта-библиотек-и-экосистемы)
- [Антипаттерны](#антипаттерны)
- [Чеклист ревью кода мода](#чеклист-ревью-кода-мода)
- [Спорные места и непроверенное](#спорные-места-и-непроверенное)

---

## Версии на ноябрь 2026

Данные проверены 2026-11-07 (Fabric meta, maven-метаданные, посты и порт-примеры).

| Что | Версия, дата | Примечание |
|---|---|---|
| Minecraft, последний стабильный | 26.3 «Wilderness Bound», 2026-09-15 | Список Fabric meta: 26.3, 26.2, 26.1.2, 26.1.1, 26.1, затем 1.21.11 и ниже |
| Снапшоты | 26.4-snapshot-1 … -7 | Следующий крупный выпуск в разработке |
| Нумерация по годам | с 26.1 (март 2026) | 1.21.11 («Mounts of Mayhem», декабрь 2025) — последний релиз 1.x |
| Игра без обфускации | с 26.1 | Во время работы имена Mojang. Intermediary нет, Yarn для 26.x не поддерживается, Parchment необязателен (только javadoc) |
| Java | 25 для 26.1+; 21 для 1.20.5–1.21.11 | Ванильный код использует JEP 447 (операторы до `super`) |
| NeoForge, схема версий | `<mc major>.<minor>.<patch>.<build>` с 26.1 | Например `26.1.2.126`, `26.3.0.101`; суффикс `-beta` разрешает ломающие изменения. В 1.21.x было `21.<mc minor>.<build>` (`21.1.265`, `21.11.48`) |
| NeoForge, последние по линиям | 26.3.0.101; 26.2.0.93; 26.1.2.126; 21.11.48; 21.1.265 | 21.1.x (MC 1.21.1) всё ещё поддерживается ради больших сборок |
| FancyModLoader | 12.0.13 | Загрузчик под NeoForge 26.x |
| ModDevGradle | 2.0.148 (2.0.141 на запуске 26.1) | NeoGradle 7.1.21 ещё существует |
| Документация NeoForge | последняя версия docs = 26.1 | Для 26.2 и 26.3 docs нет: читайте примеры порта |
| Fabric Loader | 0.20.1 (стабильный); 0.19.x на запуске 26.2 и 26.3; 0.18.4 на 26.1 | 0.19.0 добавил API расширения enum |
| Fabric Loom | 1.19.4, id плагина `net.fabricmc.fabric-loom` | 26.1: Loom 1.15 и Gradle 9.4.0; 26.2: 1.17 и 9.5.1; 26.3: 1.17 и 9.6.0 |
| Fabric API | 0.165.2+26.3; 0.165.4+26.4 (снапшот); 0.141.6+1.21.11 | id мода — `fabric-api` (id `fabric` исчез с 26.1) |
| Документация Fabric | последняя версия docs = 26.2 | Изменения 26.3 — только в посте блога |
| Sponge Mixin (форк Fabric) | 0.17.5+mixin.0.8.7 | Поставляется с Fabric Loader |
| MixinExtras | 0.5.5 (август 2026) | Встроен в Fabric Loader и NeoForge, JiJ не нужен ни там, ни там (факт встраивания `[не проверено]`) |
| `ResourceLocation` | переименован в `Identifier` в 1.21.11 | `Identifier.fromNamespaceAndPath(ns, path)`, `Identifier.withDefaultNamespace(path)` |
| Графический бэкенд | 26.1 — последний только на OpenGL; 26.2 — экспериментальный Vulkan; 26.3 — API рендера в `com.mojang.renderpearl`, LWJGL на SDL вместо GLFW | Прямые вызовы GL ломаются |
| Transfer API NeoForge | `ResourceHandler<ItemResource>` и аналоги с 21.9.1-beta | `IItemHandler`, `IFluidHandler`, `IEnergyStorage` объявлены к удалению и удалены в 26.3 |
| Исправление сетевого DoS | NeoForge 26.1.2.44-beta и 21.1.229 | Предвыделение коллекций ограничено 65 536 |

## Что сломалось в 26.x

Таблица для быстрой диагностики: в туториале написано одно, а на 26.x не собирается или падает. Подробности — в разделах ниже.

| Изменение | Версия | Что делать |
|---|---|---|
| `ResourceLocation` стал `Identifier` (заодно `critereon` → `criterion`, перестановка пакетов `util`) | 1.21.11 | Заменить класс и фабрики: `Identifier.fromNamespaceAndPath`, `withDefaultNamespace`, `parse`. Старые туториалы отстают на два переименования |
| Fabric API переименован под имена Mojang: `World*` → `Level*`, `ItemGroup*` → `CreativeModeTab*`, `KeyBindingHelper` → `KeyMappingHelper`; зависимость `fabric` → `fabric-api` | 26.1 | Выполнить миграцию имён до обновления (раздел 19), заменить зависимость в `fabric.mod.json` |
| Нет remap и конфигураций `mod*`: плагин `net.fabricmc.fabric-loom`, `implementation`/`compileOnly`/`include` вместо `modImplementation`/`modCompileOnly`, `jar` вместо `remapJar`; Yarn не поддерживается | 26.1 | Сменить плагин и конфигурации, перейти на Mojang names, `include(api(...))` вместо `include modApi(...)` |
| `ItemStack` нельзя создавать до загрузки мира: компоненты по умолчанию привязываются к `Holder` при загрузке датапака | 26.1 | Константы хранить как `ItemStackTemplate`, иконки творческих вкладок создавать лениво |
| `@OnlyIn` — пустая аннотация на NeoForge (не обрабатывается, `RuntimeDistCleaner` удалён), `DistExecutor` удалён | 1.21.6 | Разделять клиент и общий код классами и source set, использовать `dist` в `@Mod` и `@EventBusSubscriber` |
| Устаревшие `IItemHandler`, `IFluidHandler`, `IEnergyStorage`, `ItemStackHandler` удалены из NeoForge | 26.3 (устарели с 21.9) | Переписать на `ResourceHandler`, `Transaction`, `EnergyHandler` (раздел 13) |
| API рендера перенесён в `com.mojang.renderpearl`; GLSL компилируется Shaderc (`#moj_import` → `#include`, явные `layout(location=...)`); LWJGL перешёл с GLFW на SDL (коды клавиш, фокус ввода текста) | 26.3 | Убрать прямые вызовы GL и GLFW, править шейдеры, брать коды из `InputConstants`, вызывать `onTextInputFocusChange` в текстовых полях |
| Экспериментальный Vulkan-бэкенд, переписан `VertexFormat`, `MultiBufferSource` убран вне рендера чанков | 26.2 | Рисовать через `RenderPipeline` и `SubmitNodeCollector`, проверить мод с Vulkan |
| Генерация мира переписана: `Feature`, `ConfiguredFeature` и `FeatureConfiguration` слиты, `SurfaceRules` → `MaterialRules`, `DensityFunction` разделён на описание и `DensitySampler` | 26.3 | Переписать код и JSON фич, поверхностей и функций плотности (раздел 17) |
| Перезагружаемые реестры датапака: рецепты, достижения, таблицы лута, условия и функции лута, провайдеры чисел; провайдеры datagen для них перестали быть отдельными | 26.3 | Перейти на `Holder`/`HolderSet`, на `RegistriesDatapackGenerator#forReloadableLayer` в datagen |
| Права доступа: целочисленные уровни заменены на `Permission`, `PermissionSet`, `PermissionCheck` | 1.21.11 | `src.hasPermission(2)` заменить на проверку через `Commands.hasPermission(check)` и константы `Commands.LEVEL_*` |
| Лимиты размера в кодеках: предвыделение коллекций в `StreamCodec` ограничено 65 536 (исправление CWE-789) | 26.1.2.44-beta, 21.1.229 | Своим коллекциям и строкам задавать максимум явно, не полагаясь на защиту |
| `GuiGraphics` → `GuiGraphicsExtractor`, `render*` → `extract*` | 26.1 | Переименовать методы экранов и виджетов (раздел 7) |
| Блочные кодеки и реестр `block_type` удалены | 26.3 | Удалить `codec()` и поля `MapCodec` в классах блоков |
| Торговля жителей стала данными | 26.1 | `TradeOfferHelper` удалён: переписать сделки как JSON в `villager_trade` |
| `saveAdditional(CompoundTag)` заменён на `ValueOutput`/`ValueInput` | 1.21.6 | Использовать `output.store(key, CODEC, v)` и `input.read(key, CODEC)` |
| Поле `level.isClientSide` стало приватным | 1.21.9 | Вызывать метод `level.isClientSide()` |

## Карта по типам модов

Каждый вид мода упирается в свой набор разделов. Таблица помогает не читать всё подряд.

| Вид мода | Главные темы | Разделы |
|---|---|---|
| Контентные (предметы, блоки, мобы) | Реестры, компоненты данных, клиентские определения предметов, ИИ, звуки и частицы | 3, 4, 5, 6, 8 |
| Технические (машины, трубы, энергия) | Блок-сущности, возможности и transfer API, транзакции, меню, дешёвые тики | 5, 7, 10, 13 |
| Магические (заклинания, зачарования, артефакты) | Компоненты с зарядами, эффекты зачарований как данные, свой реестр датапака, вложения | 4, 8, 9, 13 |
| Генерация мира | Реестры датапака, biome modifiers, фичи, структуры, детерминизм | 9, 17 |
| Интерфейс и QoL | Экраны, HUD, клавиши, клиентские команды, конфигурация | 7, 14, 15, 16 |
| Клиентский рендеринг | Render state, submit-коллекторы, RenderPipeline, совместимость с Sodium и Iris | 14 |
| Серверные моды и админ-инструменты | Серверная авторитетность, ванильные клиенты, права, команды, сохранённые данные | 2, 10, 16, 18 |
| Оптимизация | Миксины с `@WrapOperation`, кэш возможностей, дешёвые тики, совместимость с Sodium | Миксины, 13, 14 |
| Библиотеки и API для других модов | События и собственный API, JiJ, мультилоадер, версионирование | 11, 19, 20 |

## 1. Анатомия мода: точки входа и метаданные

**Ключевые выводы**

- **Fabric (26.x).** `fabric.mod.json` (`schemaVersion: 1`) объявляет точки входа: `main` (`ModInitializer`), `client` (`ClientModInitializer`, запускается после `main`), `server` (`DedicatedServerModInitializer`), `fabric-datagen` (`DataGeneratorEntrypoint`), а также `fabric-gametest` и `fabric-client-gametest` из Fabric API. Поле `environment`: `*`, `client` или `server`. Конфиги Mixin можно привязать к стороне: `{ "config": "x.client.mixins.json", "environment": "client" }`.
- **С 26.1 зависеть нужно от `fabric-api`, а не от `fabric`.** Id `fabric` исчез. Плагин Loom называется `net.fabricmc.fabric-loom` (не `fabric-loom` и не `fabric-loom-remap`). Используйте `implementation`, `compileOnly`, `include` вместо `modImplementation`, `modCompileOnly` и `jar` вместо `remapJar`. В старых README (например Team Reborn Energy) ещё стоит `include modApi(...)`: на 26.x это `include(api(...))`.
- **Fabric: раздельные source set.** Шаблон Fabric делит код на `src/main/java` (общий) и `src/client/java` (только клиент). Если общий код ссылается на клиентские классы, Loom не скомпилирует проект. Это самая дешёвая защита от падений на выделенном сервере.
- **NeoForge (26.x).** Метаданные лежат в `META-INF/neoforge.mods.toml`. Главный класс помечен `@Mod("modid")`, конструктор принимает `IEventBus modBus` (и при необходимости `ModContainer`, `Dist`). Клиентская точка входа — второй класс `@Mod(value = "modid", dist = Dist.CLIENT)`.
- Версия NeoForge четырёхчастная (`26.1.2.126` = MC 26.1.2, сборка 126), у 1.21.x — трёхчастная (`21.1.265`). Диапазоны версий в `neoforge.mods.toml` и матрицы CI нужно переписать.
- Forge (MinecraftForge) и Quilt здесь не рассматриваются. Кросс-загрузчиковые проекты используют Architectury или шаблон MultiLoader (раздел 13).

Шаблон `fabric.mod.json` для 26.x. `[не проверено]`

```json
{ "schemaVersion": 1, "id": "examplemod", "version": "${version}",
  "environment": "*",
  "entrypoints": { "main": ["com.example.ExampleMod"], "client": ["com.example.client.ExampleModClient"],
                   "fabric-datagen": ["com.example.datagen.ExampleDataGen"] },
  "mixins": ["examplemod.mixins.json", {"config": "examplemod.client.mixins.json", "environment": "client"}],
  "accessWidener": "examplemod.classtweaker",
  "depends": { "fabricloader": ">=0.19.0", "minecraft": "~26.3", "java": ">=25", "fabric-api": "*" } }
```

Шаблон главного и клиентского класса NeoForge для 26.x. `[не проверено]`

```java
@Mod(ExampleMod.MODID)
public final class ExampleMod {
    public static final String MODID = "examplemod";
    public ExampleMod(IEventBus modBus, ModContainer container) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        modBus.addListener(ExampleMod::commonSetup);
        container.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
    }
}
@Mod(value = ExampleMod.MODID, dist = Dist.CLIENT)
public final class ExampleModClient {
    public ExampleModClient(IEventBus modBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
```

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [fabric.mod.json reference](https://docs.fabricmc.net/develop/loader/fabric-mod-json) | Fabric docs, 2026 (26.2) | Все поля, точки входа, диапазоны версий |
| [Project Structure](https://docs.fabricmc.net/develop/getting-started/project-structure) | Fabric docs, 2026 | Раздельные source set для клиента и общего кода |
| [Fabric for Minecraft 26.1](https://fabricmc.net/2026/03/14/261.html) | FabricMC, 2026 | Id плагина Loom, зависимость `fabric-api`, список удалённого |
| [Removing Obfuscation from Fabric](https://fabricmc.net/2025/10/31/obfuscation.html) | FabricMC, 2025 | Почему Yarn и Intermediary ушли |
| [Removing obfuscation in Java Edition](https://www.minecraft.net/en-us/article/removing-obfuscation-in-java-edition) | Mojang, 2025 | Официальное объявление |
| [Mod Files (neoforge.mods.toml)](https://docs.neoforged.net/docs/gettingstarted/modfiles) | NeoForge docs, 2026 (26.1) | Метаданные, зависимости, `@Mod` |
| [NeoForge for Minecraft 26.1](https://neoforged.net/news/26.1release/) | NeoForged, 2026 | Схема версий, Java 25, MDG 2.0.141 |
| [fabric-example-mod (ветка 26.2)](https://github.com/FabricMC/fabric-example-mod/tree/26.2) | FabricMC, 2026 | Эталонный шаблон |
| [NeoForge MDK templates](https://github.com/NeoForgeMDKs) | NeoForged, 2026 | MDK под каждую версию |
| [Fabric Develop (выбор версии)](https://fabricmc.net/develop/) | FabricMC | Актуальные версии Loader, Loom и API |

## 2. Стороны и потоки

**Ключевые выводы**

- **Физическая сторона** — какой jar запущен (клиент или выделенный сервер). **Логическая сторона** — логический сервер (игровая логика, работает и внутри одиночной игры) и логический клиент (рендеринг, ввод). Одиночная игра запускает обе логические стороны в одной JVM на разных потоках.
- Проверка логической стороны: `level.isClientSide()`. В **1.21.9 поле стало приватным**, старые туториалы читают поле `level.isClientSide`. Физическая сторона: NeoForge `FMLEnvironment.getDist()` (`Dist.CLIENT` или `Dist.DEDICATED_SERVER`), Fabric `FabricLoader.getInstance().getEnvironmentType()` (`EnvType.CLIENT` или `SERVER`).
- **Не ссылайтесь на `net.minecraft.client.*` из общего кода.** Классы загружаются лениво: сигнатура метода или лямбда с клиентским типом в общем классе роняет выделенный сервер с `NoClassDefFoundError`, но только когда этот путь выполнится. Клиентский код держите в отдельных классах, до которых дотягиваются только клиентские точки входа и подписчики `Dist.CLIENT`. Dev-среда NeoForge скрывает клиентские классы на dev-сервере (`NeoForgeDevDistCleaner`), поэтому ошибка воспроизводится в разработке.
- **`@OnlyIn` на NeoForge ничего не делает.** Класс аннотации ещё есть (`net.neoforged.api.distmarker`), но во время выполнения он **не обрабатывается**, `RuntimeDistCleaner` удалён в **1.21.6**. Туториалы эпохи Forge с `@OnlyIn(Dist.CLIENT)` больше ничего не защищают. Используйте `dist` в `@Mod` и `@EventBusSubscriber(value = Dist.CLIENT)` и разделение классов. Fabric `@Environment(EnvType.CLIENT)` тоже лишь маркер: на Fabric опирайтесь на раздельные source set. `DistExecutor` удалён при расколе Forge и NeoForge `[не проверено]`. В NeoForge 21.9 `FMLEnvironment.dist` и `production` стали методами `getDist()` и `isProduction()`.
- **Потоки:** серверный (тики уровней, сущностей, блок-сущностей), главный клиентский поток рендера (ввод, рендеринг, тики клиентского уровня), Netty (кодирование пакетов), пулы воркеров (генерация и ввод-вывод чанков, запекание моделей, перезагрузка ресурсов). Трогайте состояние мира только на потоке-владельце. Передача работы: `server.execute(...)`, `Minecraft.getInstance().execute(...)` или помощники загрузчика (`IPayloadContext.enqueueWork`).
- Обработчики пакетов: **NeoForge** по умолчанию выполняет их на главном потоке (для тяжёлой работы без доступа к миру можно включить `HandlerThread.NETWORK`). **Fabric** `ServerPlayNetworking.PlayPayloadHandler` по документации вызывается на серверном потоке и может безопасно менять мир. Необязательный `decodePredicate` Fabric работает **на потоке Netty**: не трогайте там мир и игрока.
- Статическое изменяемое состояние общее для обеих логических сторон в одиночной игре. Статический `Map<UUID, X>`, который пишет сервер и читает клиент, «работает» в одиночной игре и ломается на выделенном сервере. Ключуйте состояние по `Level` или `MinecraftServer` либо используйте вложения и сохранённые данные.
- Параллельные события жизненного цикла NeoForge (`FMLCommonSetupEvent`, `FMLClientSetupEvent` — это `ParallelDispatchEvent`) выполняются вне главного потока: небезопасную регистрацию оборачивайте в `event.enqueueWork(...)`.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Sides](https://docs.neoforged.net/docs/concepts/sides) | NeoForge docs, 2026 (26.1) | Физическая и логическая стороны, типичные ошибки |
| [Events (lifecycle, parallel dispatch)](https://docs.neoforged.net/docs/concepts/events) | NeoForge docs, 2026 | `enqueueWork`, шины |
| [Networking: Payloads](https://docs.neoforged.net/docs/networking/payload) | NeoForge docs, 2026 | Потоки обработчиков по умолчанию |
| [ServerPlayNetworking.java](https://github.com/FabricMC/fabric-api/blob/26.3/fabric-networking-api-v1/src/main/java/net/fabricmc/fabric/api/networking/v1/ServerPlayNetworking.java) | FabricMC, 2026 | Javadoc про потоки обработчиков |
| [MergeTool (distmarker: Dist/OnlyIn)](https://github.com/neoforged/MergeTool) | NeoForged | Где живут `Dist` и `@OnlyIn` |
| [FancyModLoader](https://github.com/neoforged/FancyModLoader) | NeoForged | Загрузчик, dev-очистка сторон |
| [Side (Fabric wiki)](https://wiki.fabricmc.net/tutorial:side) | Fabric wiki | Взгляд Fabric на стороны (старые имена до 26) |
| [Primer 1.21.9](https://github.com/neoforged/.github/blob/main/primers/1.21.9/index.md) | ChampionAsh5357, 2025 | Приватный `Level#isClientSide`, сведение ввода |

## 3. Жизненный цикл, реестры, идентификаторы, теги

**Ключевые выводы**

- **Identifier** (до 1.21.10 `ResourceLocation`, переименован в **1.21.11** вместе с `critereon` → `criterion` и перестановкой пакетов `util`): `Identifier.fromNamespaceAndPath("examplemod", "ruby")`, `Identifier.withDefaultNamespace("stone")`, `Identifier.parse("a:b")`. Пространства имён и пути — строчные `[a-z0-9_.-]` (в пути ещё `/`). Туториалы с `new ResourceLocation(...)` отстают на два переименования.
- `ResourceKey<T>` — ключ реестра плюс идентификатор. `Holder<T>` — (возможно ленивая) ссылка на запись реестра. `HolderSet<T>` — прямой список или тег. С 1.21.2 API ванили всё чаще принимает `Holder`/`HolderSet` вместо значений или `TagKey`. 26.1 убрал `EitherHolder`. 26.3 сделал таблицы лута, условия и функции лута, провайдеры чисел, источники слотов, достижения и рецепты **перезагружаемыми реестрами датапака**, заменив многие `ResourceKey` и прямые ссылки на `Holder`/`HolderSet`.
- **Id в Properties (с 1.21.2):** конструкторы `Block` и `Item` требуют `Properties#setId(ResourceKey)`, без id выбрасывается исключение. Помощники `DeferredRegister.Blocks/Items` NeoForge (`registerBlock`, `registerItem`, `registerSimpleItem`, `registerSimpleBlockItem`) ставят id сами.
- **26.2: id отделены от объектов.** Ванильные ключи переехали в классы `*Ids` (`BlockIds`, `ItemIds`, `BlockItemIds` с парами `BlockItemId`, а также id сущностей, блок-сущностей, жидкостей и зелий), типы сущностей и блок-сущностей — в `EntityTypes` и `BlockEntityTypes`. Провайдеры тегов в datagen принимают `ResourceKey` (добавление объектов в теги не принимается, `IntrinsicHolderTagsProvider` и родственные удалены). Документация Fabric отражает это через `ModItemIds` и `ModBlockItemIds`.
- **Регистрация в NeoForge:** `DeferredRegister.create(BuiltInRegistries.X, MODID)`, `createBlocks`, `createItems`, `createDataComponents`, `createEntities`, затем `.register(modBus)` в конструкторе. `DeferredHolder`, `DeferredBlock`, `DeferredItem` разрешаются после `RegisterEvent`. Свои реестры: `new RegistryBuilder<>(KEY).sync(true).defaultKey(...).maxId(...).create()` и `NewRegistryEvent`. Реестры датапака: `DataPackRegistryEvent.NewRegistry#dataPackRegistry(key, codec, networkCodecOrNull)` (null вместо сетевого кодека — не синхронизируется).
- **Регистрация в Fabric:** обычный ванильный `Registry.register(BuiltInRegistries.ITEM, key, item)` в `onInitialize`. Свои статические реестры — `FabricRegistryBuilder` `[не проверено]`. Динамические (датапак) реестры — `DynamicRegistries.register`/`registerSynced`, а с 26.3 ещё `DynamicRegistries.registerReloadable`.
- **Заморозка реестров:** регистрация после начальной загрузки выбрасывает исключение (на NeoForge регистрировать можно только внутри `RegisterEvent`). На NeoForge не делайте `new Item(...)` в статических инициализаторах, которые выполняются до регистрации: берите отложенные поставщики.
- **`ItemStack` не может существовать до привязки компонентов (26.1+).** Компоненты предметов по умолчанию привязываются к `Holder` при загрузке датапака (`DataComponentInitializers`), поэтому `new ItemStack(...)` в статических полях или в `onInitialize` падает. Используйте `ItemStackTemplate` для констант, рецептов и достижений, иконки творческих вкладок давайте лениво.
- **Теги:** `TagKey.create(Registries.BLOCK, Identifier...)`, проверка `state.is(tag)`, `stack.is(tag)`, `holder.is(tag)`. Для совместимости между модами используйте **конвенционные теги** в пространстве `c` (`#c:ingots/copper`, `#c:storage_blocks/...`): Fabric `fabric-convention-tags-v2` (`ConventionalItemTags`, `ConventionalBlockTags`) и NeoForge `Tags.Items/Blocks` делят пространство `c:`. Fabric 26.1 удалил `fabric-convention-tags-v1` и помеченные к удалению теги v2. Fabric API 0.150.1 (26.2) добавил ключ `"fabric:remove"` в JSON тегов, чтобы убирать записи без `replace: true`.
- Теги привязываются только после загрузки датапака: проверка тега во время регистрации или в статическом инициализаторе всегда возвращает false.

Регистрация предмета «сначала id» в Fabric 26.2+ (паттерн из документации Fabric). `[не проверено]`

```java
public final class ModItemIds {
    public static final ResourceKey<Item> RUBY = ResourceKey.create(Registries.ITEM, ExampleMod.id("ruby"));
}
public final class ModItems {
    public static final Item RUBY = register(ModItemIds.RUBY, Item::new, new Item.Properties());
    static <T extends Item> T register(ResourceKey<Item> key, Function<Item.Properties, T> f, Item.Properties p) {
        return Registry.register(BuiltInRegistries.ITEM, key, f.apply(p.setId(key)));
    }
    public static void init() {} // вызывается из onInitialize, чтобы загрузить класс
}
```

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Registries](https://docs.neoforged.net/docs/concepts/registries) | NeoForge docs, 2026 (26.1) | DeferredRegister, свои реестры и реестры датапака |
| [Dynamic registries (Fabric)](https://docs.fabricmc.net/develop/registries/dynamic-registry) | Fabric docs, 2026 | `DynamicRegistries` |
| [Creating Your First Item](https://docs.fabricmc.net/develop/items/first-item) | Fabric docs, 2026 (26.2) | Паттерн `ModItemIds` и `setId` |
| [Creating Your First Block](https://docs.fabricmc.net/develop/blocks/first-block) | Fabric docs, 2026 (26.2) | Помощник `BlockItemId` |
| [Items (NeoForge)](https://docs.neoforged.net/docs/items/) | NeoForge docs, 2026 | `DeferredRegister.Items`, момент создания ItemStack |
| [Tags (NeoForge)](https://docs.neoforged.net/docs/resources/server/tags) | NeoForge docs, 2026 | Файлы тегов, провайдеры, конвенции |
| [Tag generation (Fabric)](https://docs.fabricmc.net/develop/data-generation/tags) | Fabric docs, 2026 | `FabricTagsProvider` |
| [Primer 1.21.11](https://github.com/neoforged/.github/blob/main/primers/1.21.11/index.md) | ChampionAsh5357, 2025 | Переименование `ResourceLocation` → `Identifier` |
| [Primer 1.21.2](https://github.com/neoforged/.github/blob/main/primers/1.21.2/index.md) | ChampionAsh5357, 2024 | `setId`, переход на HolderSet, рецепты как реестр |
| [Primer 26.2](https://github.com/neoforged/.github/blob/main/primers/26.2/index.md) | ChampionAsh5357, 2026 | Классы `*Ids`, изменения провайдеров тегов |
| [Primer 26.3](https://github.com/neoforged/.github/blob/main/primers/26.3/index.md) | ChampionAsh5357, 2026 | Перезагружаемые реестры датапака |
| [Fabric for Minecraft 26.2](https://fabricmc.net/2026/06/15/262.html) | FabricMC, 2026 | `BlockIds`/`ItemIds`, `fabric:remove` |

## 4. Предметы и компоненты данных

**Ключевые выводы**

- **Компоненты данных заменили NBT стека в 1.20.5.** Методов `stack.getTag()` и `getOrCreateTag()` больше нет: любой туториал, читающий `CompoundTag` из `ItemStack`, старше 1.20.5. Состояние хранится в типизированных `DataComponentType<T>`: `stack.get(C)`, `getOrDefault(C, d)`, `set(C, v)` (возвращает старое), `has(C)`, `remove(C)`, у NeoForge ещё `update(C, default, op)`. Ванильный `DataComponents.CUSTOM_DATA` остаётся запасным выходом, но для данных мода это антипаттерн: нет типобезопасности, медленное сравнение, нет контроля синхронизации.
- **Значения компонентов неизменяемы и реализуют `equals`/`hashCode`** (записи `record` это делают). Стеки с равными компонентами складываются вместе. Изменяемый список внутри компонента, который вы меняете на месте, портит стакинг, рассинхронизирует клиентов и ломает кэш кодирования. Всегда вызывайте `set` с новым значением.
- Регистрация: Fabric `Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id, DataComponentType.<T>builder().persistent(CODEC).networkSynchronized(STREAM_CODEC).build())`. NeoForge `DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID)` и `registerComponentType(name, b -> b.persistent(...).networkSynchronized(...))`. Нужен хотя бы один из `persistent` и `networkSynchronized` (иначе NeoForge бросает NPE). Документация Fabric: не пропускайте `networkSynchronized` (ваниль вернётся к более медленному кодеку на основе Codec). `cacheEncoding()` — только для редко меняющихся значений.
- **Компоненты по умолчанию:** `Item.Properties#component(C, v)`. Для значений, ссылающихся на реестры датапака (зачарования, типы урона, музыкальные диски, материалы отделки), в **26.1** есть `Properties#delayedComponent(C, ctx -> ...)` и `delayedHolderComponent(C, key)`: они привязываются к `Holder` предмета при каждой перезагрузке датапака через `DataComponentInitializers`. Менять чужие и ванильные предметы: NeoForge `ModifyDefaultComponentsEvent` (шина мода; `modify`, `modifyMatching`, `null` убирает компонент), Fabric `DefaultItemComponentEvents.MODIFY`.
- **`ItemStackTemplate` (26.1):** неизменяемая запись из `Holder<Item>`, количества и `DataComponentPatch`. `create()` даёт `ItemStack`, `ItemStackTemplate.fromNonEmptyStack(stack)` — обратное. Есть `CODEC`, `MAP_CODEC`, `STREAM_CODEC`. Шаблон не может быть пустым (используйте `null`). `ItemInstance` — общий интерфейс только для чтения у стеков и шаблонов (`typeHolder()`, `count()`, `get(C)`, `is(item)`). Рецепты, достижения, частицы (`ItemParticleOption`), события наведения и контекст `TOOL` в луте теперь принимают шаблоны и инстансы. На NeoForge есть ещё `FluidStackTemplate`.
- **Поведение через компоненты вместо подклассов** растёт: еда и расходники (`Consumable`, 1.21.2), инструменты, оружие и броня (1.21.5 убрал большинство подклассов), компоненты `TooltipProvider` (`appendHoverText` устарел с 1.21.5), а в **26.3** — топливо (`DataComponents.COOKING_FUEL` вместо `FuelValues` и `FuelRegistry` Fabric), компостирование (`COMPOSTABLE` вместо `CompostingChanceRegistry`), топливо варки (`BREWING_FUEL`), еда жителей и `BLOCK_TRANSFORMER` (обтёсывание, вспашка, расплющивание; `AxeItem`, `ShovelItem`, `HoeItem` удалены).
- Подсказки: реализуйте `TooltipProvider` в записи компонента и зарегистрируйте порядок (Fabric `ItemComponentTooltipProviderRegistry.addAfter(DataComponents.DAMAGE, MY_COMPONENT)`). 26.3 добавил `TooltipFlag#shouldDisplayAllInformation` (на обоих загрузчиках), чтобы просмотрщики рецептов могли индексировать подсказки за клавишей.
- Клиентские определения предметов (1.21.4+): каждому предмету нужен `assets/<ns>/items/<id>.json` (`"model": {"type": "minecraft:model", "model": "<ns>:item/<id>"}`) в дополнение к модели в `models/item/`. Без файла — фиолетово-чёрная модель. Условные, select и range-dispatch модели предметов заменили предикаты `ItemProperties`.
- Творческие вкладки: Fabric `CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(out -> out.accept(ITEM))` (в 26.1 переименован из `ItemGroupEvents`), NeoForge `BuildCreativeModeTabContentsEvent` (шина мода) или `DeferredRegister<CreativeModeTab>`.

Запись-компонент, общая для магического жезла (заряды) и технического инструмента (режим). `[не проверено]`

```java
public record Charges(int current, int max) implements TooltipProvider {
    public static final Codec<Charges> CODEC = RecordCodecBuilder.create(i -> i.group(
        ExtraCodecs.NON_NEGATIVE_INT.fieldOf("current").forGetter(Charges::current),
        ExtraCodecs.POSITIVE_INT.fieldOf("max").forGetter(Charges::max)).apply(i, Charges::new));
    public static final StreamCodec<ByteBuf, Charges> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, Charges::current, ByteBufCodecs.VAR_INT, Charges::max, Charges::new);
    public Charges use() { return new Charges(Math.max(0, current - 1), max); } // новый экземпляр, не изменение
    @Override public void addToTooltip(Item.TooltipContext ctx, Consumer<Component> out, TooltipFlag flag, DataComponentGetter c) {
        out.accept(Component.translatable("tooltip.examplemod.charges", current, max));
    }
}
// использование: stack.set(ModComponents.CHARGES, stack.getOrDefault(ModComponents.CHARGES, DEFAULT).use());
```

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Data Components (NeoForge)](https://docs.neoforged.net/docs/items/datacomponents) | NeoForge docs, 2026 (26.1) | Регистрация, неизменяемость, значения по умолчанию, патчи |
| [Custom Data Components (Fabric)](https://docs.fabricmc.net/develop/items/custom-data-components) | Fabric docs, 2026 (26.2) | Регистрация, TooltipProvider |
| [Primer 26.1: Data Component Initializers / Item Instances](https://github.com/neoforged/.github/blob/main/primers/26.1/index.md) | ChampionAsh5357, 2026 | `delayedComponent`, `ItemStackTemplate`, `ItemInstance` |
| [Primer 1.20.5](https://github.com/neoforged/.github/blob/main/primers/1.20.5/index.md) | ChampionAsh5357, 2024 | Переход с NBT на компоненты |
| [Primer 1.21.4 (Client Items)](https://github.com/neoforged/.github/blob/main/primers/1.21.4/index.md) | ChampionAsh5357, 2024 | Определения предметов в `items/` |
| [Primer 1.21.5 (tools/armor redundancies)](https://github.com/neoforged/.github/blob/main/primers/1.21.5/index.md) | ChampionAsh5357, 2025 | Инструменты и оружие на компонентах |
| [Data component format (Minecraft Wiki)](https://minecraft.wiki/w/Data_component_format) | Minecraft Wiki | Все ванильные компоненты и их JSON |
| [Item models (Fabric)](https://docs.fabricmc.net/develop/items/item-models) | Fabric docs, 2026 | Клиентские предметы, типы моделей |
| [Custom Creative Tabs (Fabric)](https://docs.fabricmc.net/develop/items/custom-creative-tabs) | Fabric docs, 2026 | `CreativeModeTabEvents` |
| [Food (Fabric)](https://docs.fabricmc.net/develop/items/food) | Fabric docs, 2026 | `FoodProperties` и `Consumable` |
| [Custom Fuel (Fabric)](https://docs.fabricmc.net/develop/items/custom-fuel) | Fabric docs, 2026 | Топливо; в 26.3 стало `COOKING_FUEL` |
| [Consumables (NeoForge)](https://docs.neoforged.net/docs/items/consumables) | NeoForge docs, 2026 | Компонент `Consumable` |
| [Tools (NeoForge)](https://docs.neoforged.net/docs/items/tools) | NeoForge docs, 2026 | `ToolMaterial`, компоненты |

## 5. Блоки, состояния блоков, блок-сущности

**Ключевые выводы**

- Блоки — «приспособленцы» (flyweight): один экземпляр `Block` на тип. Данные конкретной позиции живут в `BlockState` (свойства объявляются в `createBlockStateDefinition`, значение по умолчанию — `registerDefaultState`) или в `BlockEntity`. Не храните состояние позиции в полях `Block`. Меняйте состояние так: `level.setBlock(pos, state.setValue(P, v), Block.UPDATE_ALL)`.
- **26.3 удалил кодеки блоков и реестр `block_type`**: уберите переопределения `codec()` и поля `MapCodec<...> CODEC`, которые требовались с 1.20.3 по 26.2.
- Взаимодействие (1.20.5+): `useWithoutItem(state, level, pos, player, hit)` и `useItemOn(stack, state, level, pos, player, hand, hit)` возвращают `InteractionResult` (1.21.2 слил `ItemInteractionResult` в `InteractionResult`). Fabric 26.1 добавил `BlockEvents.USE_ITEM_ON`/`USE_WITHOUT_ITEM` и `ItemEvents.USE_ON`/`USE`: при ненулевом результате они заменяют вызов.
- Удаление (1.21.5+): `onRemove` разделён. Содержимое выбрасывайте в `BlockEntity#preRemoveSideEffects(pos, state)`, обновление соседей — в `BlockBehaviour#affectNeighborsAfterRemoval` (`Containers.updateNeighboursAfterDestroy`). Блоки, удалённые с `UPDATE_SKIP_BLOCK_ENTITY_SIDEEFFECTS`, это пропускают.
- **Слои рендера:** с 26.1 слой чанка (`ChunkSectionLayer`: SOLID, CUTOUT, TRANSLUCENT) вычисляется по альфа-каналу текстуры для каждого квада. `ItemBlockRenderTypes` и `BlockRenderLayerMap` Fabric исчезли. Принудительная полупрозрачность — объектом материала текстуры в модели: `{ "sprite": "...", "force_translucent": true }`.
- Раскраска блоков: Fabric 26.1 `BlockColorRegistry.register(List.of(BlockTintSource...), block)` заменил `ColorProviderRegistry` (в ванили 26.1 — Block Tint Sources).
- **Блок-сущности:** `new BlockEntityType<>(MyBE::new, blocks...)` (билдер убран в 1.21.2, конструктор снова публичный); блок реализует `EntityBlock#newBlockEntity`. Тики: возвращайте тикер из `getTicker(level, state, type)` через `createTickerHelper` (`BaseEntityBlock`), обычно только на сервере (`level.isClientSide() ? null : ...`). Тик должен быть дешёвым: ранний выход, кэш соседей и возможностей, дросселирование через `level.getGameTime() % N`.
- Сохранение и загрузка (**1.21.6+**): `saveAdditional(ValueOutput)` и `loadAdditional(ValueInput)` заменяют методы на `CompoundTag`; используйте `output.store("key", CODEC, value)`, `input.read("key", CODEC)` и помощники `child`/`list`. Всегда вызывайте `super`. Зарезервированные ключи: `id`, `x`, `y`, `z`, `NeoForgeData`, `neoforge:attachments`. После изменения вызывайте `setChanged()`, иначе чанк не сохранится.
- Синхронизация с клиентом: `getUpdateTag(HolderLookup.Provider)` (при загрузке чанка) и `getUpdatePacket()`, возвращающий `ClientboundBlockEntityDataPacket.create(this)`; запуск — `level.sendBlockUpdated(pos, old, new, Block.UPDATE_CLIENTS)`. Это пересылает весь тег. Для часто меняющихся значений (прогресс, энергия) используйте слоты данных меню, пока открыт GUI, или небольшой собственный пакет через `PacketDistributor.sendToPlayersTrackingChunk` / `PlayerLookup.tracking(be)` Fabric.
- Компоненты блок-сущностей (1.20.5+): `applyImplicitComponents` и `collectImplicitComponents` копируют данные между блок-сущностью и выпавшим предметом (например именованный или заполненный контейнер) вместо рукописного NBT `BlockEntityTag`.
- Контейнерные блоки: NeoForge 21.9+ — `ItemStacksResourceHandler` и `ResourceHandlerSlot`; Fabric — ванильные `Container`/`WorldlyContainer` и `InventoryStorage`/`SidedStorageBlockEntity` из Transfer API (раздел 13).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Blocks (NeoForge)](https://docs.neoforged.net/docs/blocks/) | NeoForge docs, 2026 | Основы блоков, свойства |
| [Block states (NeoForge)](https://docs.neoforged.net/docs/blocks/states) | NeoForge docs, 2026 | Свойства, `setValue`, флаги |
| [Block Entities (NeoForge)](https://docs.neoforged.net/docs/blockentities/) | NeoForge docs, 2026 (26.1) | Тики, ValueIO, синхронизация, удаление |
| [Block States (Fabric)](https://docs.fabricmc.net/develop/blocks/blockstates) | Fabric docs, 2026 | Свойства, JSON blockstate |
| [Block Entities (Fabric)](https://docs.fabricmc.net/develop/blocks/block-entities) | Fabric docs, 2026 | Паттерн блок-сущности на Fabric |
| [Block Containers (Fabric)](https://docs.fabricmc.net/develop/blocks/block-containers) | Fabric docs, 2026 | Инвентари в блок-сущностях |
| [Block Tinting (Fabric)](https://docs.fabricmc.net/develop/blocks/block-tinting) | Fabric docs, 2026 | `BlockColorRegistry` |
| [Primer 1.21.5 (block entity removal)](https://github.com/neoforged/.github/blob/main/primers/1.21.5/index.md) | ChampionAsh5357, 2025 | `preRemoveSideEffects`, помощники по формам |
| [Primer 1.21.6 (ValueInput/ValueOutput)](https://github.com/neoforged/.github/blob/main/primers/1.21.6/index.md) | ChampionAsh5357, 2025 | Замена прямого доступа к NBT |
| [ValueIO (NeoForge)](https://docs.neoforged.net/docs/datastorage/valueio) | NeoForge docs, 2026 | API `ValueInput`/`ValueOutput` |

## 6. Сущности, ИИ и мозги

**Ключевые выводы**

- Регистрация: NeoForge `DeferredRegister.createEntities(MODID)` и `registerEntityType("name", MyEntity::new, MobCategory.MISC)`; ванильный `EntityType.Builder.of(factory, category).sized(w, h).build(ResourceKey)` (аргумент-ключ с 1.21.2). Атрибуты: NeoForge `EntityAttributeCreationEvent`, Fabric `FabricDefaultAttributeRegistry.register(type, MyMob.createAttributes())` `[не проверено]`. Спавн: `level.addFreshEntity(e)` только на сервере.
- Сохранение: `addAdditionalSaveData(ValueOutput)` и `readAdditionalSaveData(ValueInput)` (1.21.6+). Урон на сервере: `hurtServer(ServerLevel, DamageSource, float)` (1.21.2 разделил клиентский и серверный урон).
- Синхронизация: `SynchedEntityData` с `EntityDataAccessor`, объявленными в `defineSynchedData(SynchedEntityData.Builder)`; только для небольшого, часто читаемого клиентского состояния (флаги анимации, вариант). Большие или редкие данные — вложение с синхронизацией или собственный пакет в `sendToPlayersTrackingEntity(AndSelf)`.
- **26.1 удалил `Entity#interactAt`**: переопределяйте `interact(Player, InteractionHand, Vec3 location)`.
- **ИИ:** `Mob#registerGoals` с `GoalSelector`/`targetSelector` (простые мобы, большинство модовых) или **Brain** (память, сенсоры, активности; жители, пиглины, аксолотли, лягушки, верблюды). **Изменения мозгов в 26.1:** активности — записи `ActivityData`; `Brain.provider(sensors, entity -> List<ActivityData>)` хранится в статической константе; сборка через `makeBrain(Brain.Packed)`; `LivingEntity#brainProvider` удалён. Мозги сложнее писать, но дешевле расширять сенсорами. Для большинства контентных модов хватает целей.
- Рендеринг использует **render states** (1.21.2+): `EntityRenderer<T, S extends EntityRenderState>` с `createRenderState()`, `extractRenderState(entity, state, partialTick)` и (1.21.9+) `submit(state, poseStack, SubmitNodeCollector, CameraRenderState)` вместо `render(..., MultiBufferSource, ...)`. Не читайте сущность из рендерера после извлечения. Регистрация: NeoForge `EntityRenderersEvent.RegisterRenderers`, Fabric `EntityRendererRegistry.register`. 26.1 добавил раздельные модели взрослых и детёнышей и изменения текстур.
- Анимации: ванильные keyframe `AnimationDefinition` и `AnimationState`; GeckoLib (на Modrinth для 26.1–26.3, Fabric и NeoForge) для костных анимаций, созданных в Blockbench.
- Производительность: число сущностей определяет цену серверного тика. Не вызывайте `level.getEntitiesOfClass` по большим AABB каждый тик, кэшируйте цели, делайте дешёвый `canUse` у целей и `reducedTickDelay`.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Entities (NeoForge)](https://docs.neoforged.net/docs/entities/) | NeoForge docs, 2026 (26.1) | Регистрация, сохранение, спавн |
| [Entity data and networking (NeoForge)](https://docs.neoforged.net/docs/entities/data) | NeoForge docs, 2026 | `SynchedEntityData`, данные спавна |
| [Living entities, mobs and players (NeoForge)](https://docs.neoforged.net/docs/entities/livingentity) | NeoForge docs, 2026 | Иерархия мобов, ИИ |
| [Attributes (NeoForge)](https://docs.neoforged.net/docs/entities/attributes) | NeoForge docs, 2026 | Регистрация атрибутов |
| [Entity renderers (NeoForge)](https://docs.neoforged.net/docs/entities/renderer) | NeoForge docs, 2026 | Паттерн render state |
| [Creating your first entity (Fabric)](https://docs.fabricmc.net/develop/entities/first-entity) | Fabric docs, 2026 | Настройка сущности на Fabric |
| [Entity attributes (Fabric)](https://docs.fabricmc.net/develop/entities/attributes) | Fabric docs, 2026 | Реестр атрибутов Fabric |
| [Projectiles (Fabric)](https://docs.fabricmc.net/develop/entities/projectiles) | Fabric docs, 2026 | Снаряды |
| [Damage types (Fabric)](https://docs.fabricmc.net/develop/entities/damage-types) | Fabric docs, 2026 | Типы урона как данные |
| [Primer 1.21.2 (Entity Render States)](https://github.com/neoforged/.github/blob/main/primers/1.21.2/index.md) | ChampionAsh5357, 2024 | Почему рендереры изменились |
| [GeckoLib (Modrinth)](https://modrinth.com/mod/geckolib) | GeckoLib team, 2026 | Библиотека анимаций сущностей, предметов, брони |
| [GeckoLib wiki](https://github.com/bernie-g/geckolib/wiki) | bernie-g и соавторы | Документация API |

## 7. Меню и экраны

**Ключевые выводы**

- Разделение сервера и клиента: **меню** (`AbstractContainerMenu`) существует на обеих сторонах и владеет слотами, **экран** (`AbstractContainerScreen`) только клиентский и регистрируется отдельно. Сервер открывает через `player.openMenu(MenuProvider)`, клиент создаёт меню из фабрики `MenuType`.
- Дополнительные данные при открытии (например `BlockPos`): NeoForge `IMenuTypeExtension.create(MyMenu::new)` и `serverPlayer.openMenu(provider, buf -> buf.writeBlockPos(pos))`; Fabric `ExtendedMenuType<M, D>(factory, streamCodec)` и `ExtendedMenuProvider#getScreenOpeningData(ServerPlayer)` (`fabric-menu-api-v1`). Не доверяйте этим данным на сервере: выводите их из серверного состояния заново.
- `quickMoveStack` нужно реализовать (по умолчанию бросает исключение или зацикливается), а `stillValid` должен проверять расстояние и блок (`AbstractContainerMenu.stillValid(access, player, block)` или `Container.stillValidBlockEntity`). Без `stillValid` появляется эксплойт удалённого доступа к инвентарю.
- Синхронизация чисел при открытом меню: `DataSlot`/`ContainerData` (ваниль ограничивает значения типом short, NeoForge патчит до полного int). Большее состояние — пакетом.
- Регистрация экрана: Fabric `MenuScreens.register(type, Screen::new)` на клиенте; NeoForge `RegisterMenuScreensEvent` (шина мода, клиент) `[не проверено]` (имя события).
- **Нестабильность GUI API:** 1.21.2 — типы отрисовки GUI; 1.21.6 — переписывание GUI на render state; **26.1** — `GuiGraphics` → `GuiGraphicsExtractor`, `Screen#render` → `extractRenderState`, `renderBackground`/`renderBg` → `extractBackground`, `renderLabels` → `extractLabels`, `AbstractWidget#renderWidget` → `extractWidgetRenderState`, префиксы `draw*`/`render*` убраны (`renderOutline` → `outline`), `*String*` → `*Text*`; **26.2** — состояние экранов и HUD переехало из `Minecraft` в `Gui`/`Hud` (`Minecraft.getInstance().gui.setScreen(...)`, `gui.screen()`, `gui.hud.setOverlayMessage(...)`); **26.3** — текстовые поля обязаны вызывать `Minecraft.getInstance().onTextInputFocusChange(this, focused)` в `setFocused` (ввод текста через SDL).
- Правило: рисуйте в экране только из извлечённого состояния; тяжёлую раскладку считайте в `init()` или при изменении, а не каждый кадр.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Container Menus (Fabric)](https://docs.fabricmc.net/develop/blocks/container-menus) | Fabric docs, 2026 (26.2) | Меню и экран пошагово |
| [Menus (NeoForge)](https://docs.neoforged.net/docs/inventories/menus/) | NeoForge docs, 2026 (26.1) | Дополнительные данные, DataSlot, quickMoveStack |
| [Screens (NeoForge)](https://docs.neoforged.net/docs/rendering/screens) | NeoForge docs, 2026 | Жизненный цикл экрана, извлечение |
| [Custom Screens (Fabric)](https://docs.fabricmc.net/develop/rendering/gui/custom-screens) | Fabric docs, 2026 | Экраны |
| [Custom Widgets (Fabric)](https://docs.fabricmc.net/develop/rendering/gui/custom-widgets) | Fabric docs, 2026 | Виджеты, фокус |
| [GUI graphics (Fabric)](https://docs.fabricmc.net/develop/rendering/gui-graphics) | Fabric docs, 2026 | Рисование через `GuiGraphicsExtractor` |
| [Workstations (Fabric)](https://docs.fabricmc.net/develop/blocks/workstations) | Fabric docs, 2026 | Блоки-станции в стиле верстака |

## 8. Рецепты, лут, достижения, зачарования, торговля, звуки, частицы

**Ключевые выводы**

- **Рецепты** с 1.21.2 — реестр датапака (`ResourceKey<Recipe<?>>`, `RecipeManager` только на сервере; клиент получает `RecipeDisplay`, а не полные рецепты, поэтому клиентский поиск рецептов и интеграции JEI, EMI, REI изменились). **26.1:** `RecipeSerializer` — запись `new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC)` (классы сериализаторов убраны); общие настройки в `Recipe.CommonInfo`/`BookInfo`; базовые классы `NormalCraftingRecipe`, `CustomRecipe`, `SingleItemRecipe`, `AbstractCookingRecipe` сокращают шаблонный код; `Recipe#assemble` больше не принимает `HolderLookup.Provider`; результаты — `ItemStackTemplate`. **26.3:** рецепты и достижения — **перезагружаемые** динамические реестры; варка стала `BrewingRecipe` (`RecipeType.BREWING`, `BrewingRecipeBuilder`), поэтому `FabricPotionBrewingBuilder` Fabric удалён (в datagen используйте `FabricBrewingProvider`).
- **Таблицы лута:** `data/<ns>/loot_table/...` (имена папок в единственном числе с 1.21). Менять чужие таблицы: Fabric `LootTableEvents.MODIFY` (проверяйте `source.isBuiltin()`, чтобы не править таблицы, переопределённые датапаком), `REPLACE`, `ALL_LOADED`, `MODIFY_DROPS` (`fabric-loot-api-v3`; v2 удалён в 26.1); NeoForge **глобальные модификаторы лута** (`data/<ns>/neoforge/loot_modifiers/`, `IGlobalLootModifier` с зарегистрированным кодеком) и `LootTableLoadEvent`. **26.3** реорганизовал лут (новые типы данных, `ContextAwarePredicate` удалён, провайдеры чисел разделены на `context_float_provider`/`context_int_provider`) и сделал таблицы лута перезагружаемыми реестрами: ждите смены сигнатур API модификации лута. Форма API лута Fabric в 26.3 `[не проверено]`.
- **Достижения:** JSON в `data/<ns>/advancement/`; свои триггеры — `SimpleCriterionTrigger<T>` с кодеком экземпляра, регистрация в `BuiltInRegistries.TRIGGER_TYPES`. Datagen 26.3: `AdvancementSubProvider` — абстрактный класс, создаётся из `BootstrapContext<Advancement>`; родители задаются через `Identifier`.
- **Зачарования — данные с 1.21:** `data/<ns>/enchantment/<id>.json` с полями `description`, `supported_items`, `primary_items`, `weight`, `max_level`, `min_cost`, `max_cost`, `anvil_cost`, `slots`, `effects`, `exclusive_set`. Своё поведение — свой *компонент эффекта зачарования*, зарегистрированный в `BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE`; чтение: `EnchantmentHelper.runIterationOnItem(stack, (holder, level) -> holder.value().effects().get(MY_EFFECT))`. Не ссылайтесь на зачарования как на Java-константы: это `ResourceKey<Enchantment>`, разрешаемые через реестры текущего уровня.
- **Торговля жителей — данные с 26.1:** `data/<ns>/villager_trade/`, `data/<ns>/trade_set/`, теги профессий `data/minecraft/tags/villager_trade/<profession>`. `TradeOfferHelper` Fabric удалён; код в стиле `VillagerTradesEvent` NeoForge нужно перенести в данные.
- **Преобразователи блоков (26.3):** обтёсывание, вспашка и расплющивание — реестр `block_transformer`, на который ссылается `DataComponents.BLOCK_TRANSFORMER`; `StrippableBlockRegistry`, `TillableBlockRegistry`, `FlattenableBlockRegistry` Fabric удалены в пользу `BlockTransformerHelper` и `BlockTransformerEvents.MODIFY`.
- **Data maps NeoForge** (`data/<ns>/neoforge/data_maps/<registry>/<map>.json`, `DataMapType`, `DataMapProvider`) привязывают к записям реестра значения, которые датапак может переопределять (компостируемое, топливо печей, вощение, частоты вибрации, свои). Ванильный 26.3 теперь покрывает топливо и компост компонентами.
- **Звуки:** регистрируйте `SoundEvent.createVariableRangeEvent(id)` в `BuiltInRegistries.SOUND_EVENT`, объявляйте в `assets/<ns>/sounds.json`; с сервера играйте `level.playSound(null, pos, ...)` (null вместо игрока — слышат все, включая виновника), на клиенте — с аргументом игрока. Воспроизведение на обеих сторонах с ненулевым игроком звучит дважды. В 26.1 больше реестров вариантов звуков сущностей и изменений аудио `[не проверено]` (API звука).
- **Частицы:** регистрируйте `ParticleType` (`SimpleParticleType` или с кодеком опций), дайте `assets/<ns>/particles/<id>.json` со списком спрайтов, зарегистрируйте клиентский провайдер (NeoForge `RegisterParticleProvidersEvent`, Fabric `ParticleFactoryRegistry`/клиентский реестр частиц `[не проверено]`). С сервера спавните `serverLevel.sendParticles(...)`; `level.addParticle` на серверном уровне ничего не делает. С 1.21.4 частицы рисуются через render types; 26.1 — «слои квадов частиц».

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Recipes (NeoForge)](https://docs.neoforged.net/docs/resources/server/recipes/) | NeoForge docs, 2026 | Свои типы рецептов и сериализаторы |
| [Custom Recipe Types (Fabric)](https://docs.fabricmc.net/develop/recipes/custom-recipe-types) | Fabric docs, 2026 | Рецепт, сериализатор, тип |
| [Extending vanilla recipes (Fabric)](https://docs.fabricmc.net/develop/recipes/extending-vanilla-recipes) | Fabric docs, 2026 | Повторное использование ванильных классов рецептов |
| [Loot Tables (NeoForge)](https://docs.neoforged.net/docs/resources/server/loottables/) | NeoForge docs, 2026 | Структура лута, свои записи |
| [Global Loot Modifiers (NeoForge)](https://docs.neoforged.net/docs/resources/server/loottables/glm) | NeoForge docs, 2026 | Кодеки и JSON глобальных модификаторов |
| [Loot table events (Fabric)](https://docs.fabricmc.net/develop/events/loot-tables) | Fabric docs, 2026 (26.2) | `LootTableEvents` |
| [Advancements (NeoForge)](https://docs.neoforged.net/docs/resources/server/advancements) | NeoForge docs, 2026 | Критерии, триггеры |
| [Enchantments (NeoForge)](https://docs.neoforged.net/docs/resources/server/enchantments/) | NeoForge docs, 2026 (26.1) | Зачарования как данные, компоненты эффектов |
| [Custom Enchantment Effects (Fabric)](https://docs.fabricmc.net/develop/items/custom-enchantment-effects) | Fabric docs, 2026 | Компоненты эффектов на Fabric |
| [Data Maps (NeoForge)](https://docs.neoforged.net/docs/resources/server/datamaps/) | NeoForge docs, 2026 | Данные, привязанные к реестру |
| [Damage types (NeoForge)](https://docs.neoforged.net/docs/resources/server/damagetypes) | NeoForge docs, 2026 | Реестр типов урона |
| [Sounds (NeoForge)](https://docs.neoforged.net/docs/resources/client/sounds) | NeoForge docs, 2026 | `sounds.json`, воспроизведение |
| [Using sounds (Fabric)](https://docs.fabricmc.net/develop/sounds/using-sounds) | Fabric docs, 2026 | Воспроизведение на клиенте и сервере |
| [Custom sounds (Fabric)](https://docs.fabricmc.net/develop/sounds/custom) | Fabric docs, 2026 | Регистрация звуковых событий |
| [Dynamic sounds (Fabric)](https://docs.fabricmc.net/develop/sounds/dynamic-sounds) | Fabric docs, 2026 | Зацикленные и позиционные звуки |
| [Particles (NeoForge)](https://docs.neoforged.net/docs/resources/client/particles) | NeoForge docs, 2026 | Типы частиц, провайдеры |
| [Client particles (NeoForge)](https://docs.neoforged.net/docs/rendering/particles) | NeoForge docs, 2026 | Рендеринг частиц |
| [Creating particles (Fabric)](https://docs.fabricmc.net/develop/rendering/particles/creating-particles) | Fabric docs, 2026 | Регистрация частиц на Fabric |
| [Potions (Fabric)](https://docs.fabricmc.net/develop/items/potions) | Fabric docs, 2026 | Варка до 26.3; сверять с данными варки 26.3 |
| [Primer 26.1 (trades, recipes)](https://github.com/neoforged/.github/blob/main/primers/26.1/index.md) | ChampionAsh5357, 2026 | Перевод сделок в новый формат |
| [Fabric for Minecraft 26.3](https://fabricmc.net/2026/09/15/263.html) | FabricMC, 2026 | Варка, топливо, компост, список удалённых преобразователей |

## 9. Данные вместо кода: датапаки, ресурспаки, datagen, Codec и StreamCodec

**Ключевые выводы**

- **Данные и ассеты.** `data/<ns>/...` — серверное (рецепты, loot_table, advancement, теги, enchantment, damage_type, worldgen/*, villager_trade, trade_set, block_transformer, NeoForge `neoforge/data_maps`, `neoforge/loot_modifiers`, `neoforge/biome_modifier`). `assets/<ns>/...` — клиентское (blockstates, модели, items, текстуры, lang, sounds.json, particles, шейдеры, equipment, шрифты). Jar мода — одновременно датапак и ресурспак; игроки и сборки могут переопределить любой файл, поэтому **настраиваемые значения выражайте данными, а не константами Java**. Имена папок в единственном числе с 1.21 (`loot_table`, `recipe`, `advancement`, `tags/block`).
- **Форматы паков** теперь вида `major.minor`, а `pack.mcmeta` использует `min_format`/`max_format` (с 25w31a и 1.21.9). Формат датапака: 48 (1.21.1), 94.1 (1.21.11), 101.1 (26.1), 107.1 (26.2), 121.0 (26.3); формат ресурспака: 34, 75.0, 84.0, 88.0, 97.1. Встроенный пак мода генерирует загрузчик; `pack.mcmeta` пишете только для вложенных необязательных паков.
- **Codec (DFU)** — двунаправленный сериализатор для диска, JSON и NBT (`encodeStart(ops, v)`, `parse(ops, input)` → `DataResult`). Записи собирайте через `RecordCodecBuilder.create(i -> i.group(...).apply(i, Ctor::new))` (максимум 16 полей), `fieldOf`/`optionalFieldOf(name, default)`, `xmap`/`comapFlatMap`/`flatXMap`, `Codec.either`, `dispatch` (полиморфизм по ключу типа — так устроен каждый реестр «type»: фичи, функции лута, функции плотности), `Codec.recursive`, `listOf`, `unboundedMap`. Когда кодек ссылается на записи реестров (`Holder`, `HolderSet`), используйте `RegistryOps` (через `registryAccess.createSerializationContext(ops)`). Проверяйте рано: `ExtraCodecs.POSITIVE_INT`, `Codec.intRange`. Логируйте через `resultOrPartial(LOGGER::error)`, никогда `.result().get()`.
- **26.3, «упрощение типов до кодеков»:** многие зарегистрированные объекты *типов* (`MapCodec` для фич, функций плотности, материальных правил, провайдеров состояний блоков, размещения структур) теперь просто `MapCodec`, зарегистрированные в реестрах `*_TYPE`, а объект данных реализует интерфейс с `codec()`.
- **StreamCodec<B, V>** — сетевой сериализатор (`ByteBuf` < `FriendlyByteBuf` < `RegistryFriendlyByteBuf`; берите наименее специфичный). `StreamCodec.composite(codecA, getterA, ..., Ctor)` до 12 полей (по документации NeoForge); `ByteBufCodecs.VAR_INT`, `STRING_UTF8`/`stringUtf8(max)`, `collection(factory, elem, maxSize)`, `map(factory, k, v, maxSize)`, `optional`, `registry(key)`, `holderRegistry(key)`, `idMapper`, `fromCodec`/`fromCodecWithRegistries` (круг через NBT: удобно, но тяжело и без ограничения размера; избегайте для serverbound). `StreamCodec.unit(v)` для пустых пакетов, `dispatch` для полиморфизма, `recursive`.
- **Всегда задавайте максимальный размер сетевым коллекциям** (`ByteBufCodecs.collection(..., 256)`, `stringUtf8(256)`). NeoForge исправил DoS с предвыделением (CWE-789) в 26.1.2.44-beta и 21.1.229, ограничив начальную ёмкость значением `ByteBufCodecs.MAX_INITIAL_COLLECTION_SIZE` (65 536), но ваши `readCollection` и рукописные читатели не защищены.
- **Datagen (NeoForge 26.1):** подписывайтесь на `GatherDataEvent.Client` (можно добавлять все провайдеры) и `GatherDataEvent.Server` (только данные); помощники `event.createProvider(Factory)`, `createDatapackRegistryObjects(RegistrySetBuilder)`, `createBlockAndItemTags(...)`; запуск — конфигурации `clientData`/`serverData` MDG (Gradle `runClientData`). Провайдеры: `ModelProvider`, `LanguageProvider`, `EquipmentAssetProvider`, `ParticleDescriptionProvider`, `SoundDefinitionsProvider`, `SpriteSourceProvider`, `AdvancementProvider`, `LootTableProvider`, `RecipeProvider` (с вложенным `Runner`), `RecipePrioritiesProvider`, `DataMapProvider`, `GlobalLootModifierProvider`, `DatapackBuiltinEntriesProvider`, `JsonCodecProvider`, `PackMetadataGenerator`, провайдеры тегов. Вывод — `src/generated/resources`.
- **Datagen (Fabric):** в Gradle `fabricApi { configureDataGeneration() { client = true } }`, точка входа `fabric-datagen` с `DataGeneratorEntrypoint#onInitializeDataGenerator(FabricDataGenerator)`, `FabricDataGenerator.Pack pack = gen.createPack(); pack.addProvider(MyRecipeProvider::new)`, записи динамических реестров через `buildRegistry(RegistrySetBuilder)` и `FabricDynamicRegistryProvider`; запуск `./gradlew runDatagen`; вывод `src/main/generated`. Провайдеры: `FabricModelProvider`, `FabricLanguageProvider`, `FabricRecipeProvider`, `FabricBlockLootSubProvider`, `FabricAdvancementProvider`, `FabricTagsProvider`, `FabricCodecDataProvider`, `FabricBrewingProvider` (26.3), `FabricSoundsProvider`.
- **Изменение datagen в 26.3:** у перезагружаемых реестров (лут, достижения, рецепты) больше нет отдельных `DataProvider`: `LootTableProvider` и `AdvancementProvider` реализуют `SingleRegistryBootstrap` и добавляются в `RegistriesDatapackGenerator#forReloadableLayer`; `LootTableSubProvider#generate(consumer)` стал `run()` без аргументов с сохранённым `Context`. Обёртки загрузчиков, вероятно, последуют.
- Генерируемые файлы коммитьте (читаемые диффы) или генерируйте в CI, но никогда не правьте их руками; запускайте datagen в CI и проваливайте сборку при `git diff --exit-code`. `[не проверено]`
- Инструменты: генераторы misode и журнал изменений по версиям для форм JSON; `/reload` для итераций над данными без перезапуска (работает для перезагружаемых реестров 26.3; реестры генерации мира всё ещё требуют нового мира или перезапуска).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Resources (NeoForge)](https://docs.neoforged.net/docs/resources/) | NeoForge docs, 2026 (26.1) | Структура паков, настройка datagen, провайдеры |
| [Data Generation Setup (Fabric)](https://docs.fabricmc.net/develop/data-generation/setup) | Fabric docs, 2026 (26.2) | Точка входа, настройка Loom |
| [Recipe generation (Fabric)](https://docs.fabricmc.net/develop/data-generation/recipes) | Fabric docs, 2026 | `FabricRecipeProvider` |
| [Loot table generation (Fabric)](https://docs.fabricmc.net/develop/data-generation/loot-tables) | Fabric docs, 2026 | Провайдеры лута |
| [Advancement generation (Fabric)](https://docs.fabricmc.net/develop/data-generation/advancements) | Fabric docs, 2026 | Провайдеры достижений |
| [Block model generation (Fabric)](https://docs.fabricmc.net/develop/data-generation/block-models) | Fabric docs, 2026 | `FabricModelProvider` |
| [Item model generation (Fabric)](https://docs.fabricmc.net/develop/data-generation/item-models) | Fabric docs, 2026 | Клиентские предметы в datagen |
| [Translation generation (Fabric)](https://docs.fabricmc.net/develop/data-generation/translations) | Fabric docs, 2026 | Провайдер переводов |
| [Enchantment generation (Fabric)](https://docs.fabricmc.net/develop/data-generation/enchantments) | Fabric docs, 2026 | Записи динамических реестров |
| [Codecs (Fabric)](https://docs.fabricmc.net/develop/serialization/codecs) | Fabric docs, 2026 (26.2) | Сборник рецептов по Codec |
| [Codecs (NeoForge)](https://docs.neoforged.net/docs/datastorage/codecs) | NeoForge docs, 2026 | Справочник по Codec |
| [Stream Codecs (NeoForge)](https://docs.neoforged.net/docs/networking/streamcodecs) | NeoForge docs, 2026 | `ByteBufCodecs`, максимальные размеры |
| [Reload listeners (NeoForge)](https://docs.neoforged.net/docs/resources/reloadlisteners) | NeoForge docs, 2026 | Загрузка собственного JSON |
| [Resource conditions (Fabric)](https://docs.fabricmc.net/develop/resource-conditions) | Fabric docs, 2026 | Загрузка данных только при наличии мода |
| [DataFixerUpper](https://github.com/Mojang/DataFixerUpper) | Mojang | Исходники библиотеки кодеков |
| [Pack format](https://minecraft.wiki/w/Pack_format) | Minecraft Wiki | Номера форматов, `min_format`/`max_format` |
| [Data pack](https://minecraft.wiki/w/Data_pack) | Minecraft Wiki | Справочник по папкам |
| [misode.github.io generators](https://misode.github.io/) | Misode | Генераторы JSON для всех типов данных |
| [Misode version changelog](https://misode.github.io/versions/) | Misode | Изменения данных и ресурсов по версиям |

## 10. Сеть: пакеты, синхронизация, безопасность

**Ключевые выводы**

- **Оба загрузчика используют ванильный `CustomPacketPayload`** (1.20.5+): запись, реализующая `CustomPacketPayload`, со статическим `CustomPacketPayload.Type<T> TYPE = new CustomPacketPayload.Type<>(Identifier...)`, `StreamCodec` и `type()`, возвращающим `TYPE`. Старые пары записи и чтения `FriendlyByteBuf`, `SimpleChannel` и каналы `PacketByteBufs` Fabric — до 1.20.5.
- **NeoForge:** `RegisterPayloadHandlersEvent` (шина мода) → `PayloadRegistrar registrar = event.registrar("1")` (строка сетевой версии: клиенты с другой версией отклоняются), затем `playToServer`/`playToClient`/`playBidirectional` (есть и `configuration*`, `common*`). Вызовы настройки регистратора возвращают новый экземпляр (`registrar = registrar.executesOn(HandlerThread.NETWORK)`; `.optional()` для модов, терпящих отсутствие канала на другой стороне). Обработчики клиентских пакетов можно регистрировать отдельно в `RegisterClientPayloadHandlersEvent` (с 1.21.6), чтобы общий код не ссылался на клиентские классы. Отправка: `ClientPacketDistributor.sendToServer(p)`, `PacketDistributor.sendToPlayer(sp, p)`, `sendToPlayersTrackingChunk(level, chunkPos, p)`, `sendToPlayersTrackingEntity(AndSelf)`, `sendToAllPlayers(p)`. Сигнатура обработчика `(payload, IPayloadContext ctx)`; `ctx.player()`, `ctx.enqueueWork(...)` (исключения в возвращённом future проглатываются, пока вы их не обработаете), `ctx.disconnect(Component)`. Лимиты: clientbound 1 МиБ, serverbound меньше 32 КиБ.
- **Fabric Networking API v1 (имена 26.x):** регистрируйте типы на **обеих** сторонах в общей инициализации: `PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC)` / `serverboundPlay()` / `clientboundConfiguration()` / `serverboundConfiguration()` (имена для 1.20.5–1.21.11: `playS2C()`/`playC2S()`/`configurationS2C()`/`configurationC2S()`). Необязательный `decodePredicate` (работает на потоке Netty) и `registerLarge(type, codec, maxPacketSize)`, разбивающий крупные пакеты (замечание безопасности в javadoc: держите serverbound-максимум малым). Приёмники: `ServerPlayNetworking.registerGlobalReceiver(TYPE, (p, ctx) -> ...)` с `ctx.player()`, `ctx.server()`, `ctx.responseSender()`; `ClientPlayNetworking.registerGlobalReceiver(TYPE, (p, ctx) -> ctx.client()...)` в клиентском инициализаторе. Отправка: `ServerPlayNetworking.send(player, p)`, `ClientPlayNetworking.send(p)`, `PlayerLookup.tracking(entity|blockEntity|level,pos)`, `PlayerLookup.all(server)`. Перед отправкой возможно ванильным клиентам проверяйте `ServerPlayNetworking.canSend(player, TYPE)`.
- **Проверка на сервере обязательна.** Считайте каждый serverbound-пакет данными от злоумышленника. Проверяйте: игрок может действовать (`player.isAlive()`, `!player.isSpectator()`), расстояние (`player.canInteractWithBlock(pos, 1.0)` или в пределах досягаемости), чанк загружен (`level.hasChunkAt(pos)` / `isLoaded`), открыто ожидаемое меню (`player.containerMenu instanceof MyMenu m && m.containerId == p.containerId()`), границы индексов и enum, права, частоту (кулдауны на игрока). Отправляйте **намерение** (индекс слота, id кнопки), а не результат (никогда не принимайте от клиента `ItemStack`, который сервер должен вычислить). Ограничивайте каждую коллекцию и строку в кодеке. Ванильный образец: `ServerboundSetCreativeModeSlotPacket` проверяет творческий режим до декодирования стека.
- Обработчики clientbound-пакетов тоже должны быть защитными: сущности ещё может не быть (`level.getEntity(id)` равно null), чанк не загружен, запись реестра неизвестна.
- **Паттерны синхронизации:** блок-сущность → `getUpdatePacket`/`getUpdateTag` и `sendBlockUpdated`; сущность → `SynchedEntityData` или отслеживаемый пакет; данные игрока или сущности → вложение NeoForge `.sync(...)` или `.syncWith(codec, AttachmentSyncPredicate.targetOnly())` Fabric; данные реестров → синхронизируемые реестры датапака; конфигурация → автосинхронизация SERVER-конфига NeoForge, у Fabric — пакет фазы конфигурации (`ServerConfigurationConnectionEvents.CONFIGURE` и задача конфигурации) для данных, нужных клиенту до входа.
- Клиентские команды (Fabric 0.152.0, 26.2): добавляйте `.requires(FabricClientCommandSource::attended)`, чтобы кликабельный текст от сервера не мог тихо запускать клиентские команды.

Пакет «нажать кнопку станка» (технический мод), обработанный безопасно, имена Fabric 26.x. `[не проверено]`

```java
public record PressButton(int containerId, int buttonId) implements CustomPacketPayload {
    public static final Type<PressButton> TYPE = new Type<>(ExampleMod.id("press_button"));
    public static final StreamCodec<ByteBuf, PressButton> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, PressButton::containerId, ByteBufCodecs.VAR_INT, PressButton::buttonId, PressButton::new);
    @Override public Type<PressButton> type() { return TYPE; }
}
// общая инициализация
PayloadTypeRegistry.serverboundPlay().register(PressButton.TYPE, PressButton.CODEC);
ServerPlayNetworking.registerGlobalReceiver(PressButton.TYPE, (p, ctx) -> {
    ServerPlayer player = ctx.player();
    if (!(player.containerMenu instanceof MachineMenu menu) || menu.containerId != p.containerId()) return;
    if (!menu.stillValid(player) || p.buttonId() < 0 || p.buttonId() >= MachineMenu.BUTTON_COUNT) return;
    menu.pressButton(player, p.buttonId()); // эффект решает сервер
});
```

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Networking (NeoForge index)](https://docs.neoforged.net/docs/networking/) | NeoForge docs, 2026 | Обзор |
| [Registering Payloads (NeoForge)](https://docs.neoforged.net/docs/networking/payload) | NeoForge docs, 2026 (26.1) | Регистратор, потоки, рассылка, лимиты |
| [Configuration tasks (NeoForge)](https://docs.neoforged.net/docs/networking/configuration-tasks) | NeoForge docs, 2026 | Синхронизация до входа |
| [Mitigating Network Vulnerabilities](https://neoforged.net/news/mitigating-vulnerabilities-network/) | NeoForged, 2026 | DoS CWE-789, безопасные кодеки, проверки |
| [Networking (Fabric)](https://docs.fabricmc.net/develop/networking) | Fabric docs, 2026 (26.2) | Пакеты, пример проверки |
| [PayloadTypeRegistry.java](https://github.com/FabricMC/fabric-api/blob/26.3/fabric-networking-api-v1/src/main/java/net/fabricmc/fabric/api/networking/v1/PayloadTypeRegistry.java) | FabricMC, 2026 | `registerLarge`, `decodePredicate`, имена методов 26.x |
| [CWE-789](https://cwe.mitre.org/data/definitions/789.html) | MITRE | Класс уязвимости из бюллетеня 2026 |
| [CrashExploitFixer (Modrinth)](https://modrinth.com/mod/crashexploitfixer) | Drex, 2026 | Смягчение для непропатченных версий |
| [Fabric for Minecraft 26.2 (attended client commands)](https://fabricmc.net/2026/06/15/262.html) | FabricMC, 2026 | `FabricClientCommandSource::attended` |

## 11. События и хуки: шины NeoForge, колбэки Fabric

**Ключевые выводы**

- **У NeoForge две шины.** Шина мода (`IEventBus`, передаётся в конструктор `@Mod`): события регистрации и жизненного цикла, реализующие `IModBusEvent` (`RegisterEvent`, `FMLCommonSetupEvent`, `FMLClientSetupEvent`, `RegisterPayloadHandlersEvent`, `RegisterCapabilitiesEvent`, `EntityRenderersEvent.*`, `GatherDataEvent.*`, `ModConfigEvent.*`, `BuildCreativeModeTabContentsEvent`, `ModifyDefaultComponentsEvent`). Игровая шина `NeoForge.EVENT_BUS`: геймплей (`ServerTickEvent.Pre/Post`, `LevelTickEvent`, `PlayerTickEvent`, `LivingIncomingDamageEvent`, `BlockEvent.BreakEvent`, `PlayerInteractEvent.*`, `RegisterCommandsEvent`, `AddReloadListenerEvent`/`AddServerReloadListenersEvent`, `ServerStartingEvent`). В 1.21.x `@EventBusSubscriber(modid = ..., value = Dist.CLIENT)` выбирает шину сам; параметра `bus =` больше нет, старый код с `bus = Bus.MOD` не компилируется.
- Регистрация слушателей: `NeoForge.EVENT_BUS.addListener(MyMod::onX)` (тип берётся из параметра), `register(Object|Class)` с `@SubscribeEvent` или `@EventBusSubscriber` (статические методы). Приоритеты `HIGHEST..LOWEST` (для параллельных событий игнорируются); отмена — `ICancellableEvent#setCanceled(true)`, `receiveCanceled = true`, чтобы видеть отменённые. События тиков разделены на классы `Pre`/`Post` (поля `phase` нет с 1.20.5).
- **События Fabric** — объекты `Event<T>` на интерфейсах колбэков, создаются через `EventFactory.createArrayBacked(T.class, listeners -> invoker)`; регистрация `X.EVENT.register(lambda)`. Колбэки взаимодействия возвращают `InteractionResult`: `PASS` передаёт следующему слушателю и ваниле, любое другое значение обрывает цепочку (`FAIL` отменяет). Порядок: `Event.addPhaseOrdering(EARLY, Event.DEFAULT_PHASE)` и `register(phase, listener)` `[не проверено]`. Частые классы 26.x: `ServerLifecycleEvents`, `ServerTickEvents` (`START_SERVER_TICK`, `END_LEVEL_TICK`), `ServerLevelEvents`, `ServerChunkEvents`, `ServerEntityEvents`, `ServerPlayerEvents`, `ServerLivingEntityEvents`, `ServerEntityCombatEvents`, `ClientTickEvents`, `ClientLifecycleEvents`, `UseBlockCallback`, `UseItemCallback`, `UseEntityCallback`, `AttackBlockCallback`, `PlayerBlockBreakEvents`, `BlockEvents`/`ItemEvents` (26.1), `CommandRegistrationCallback`, `LootTableEvents`, `DefaultItemComponentEvents`, `CreativeModeTabEvents`, `EnchantmentEvents`, `LevelRenderEvents`/`LevelExtractionEvents` (клиент), `HudElementRegistry`, `ItemTooltipCallback`. С 26.1 имена следуют Mojang (`World*` → `Level*`, `ItemGroup*` → `CreativeModeTab*`, `KeyBindingHelper` → `KeyMappingHelper`).
- **Порядок выбора хука:** (1) данные (теги, data maps, компоненты, реестры датапака, модификаторы лута); (2) событие или реестр загрузчика; (3) точка расширения ваниля (переопределение в своём подклассе, например `Block#getProvidedEnchantmentPower`, добавленный в 26.3); (4) небольшой инжектор Mixin (`@Inject`, `@ModifyExpressionValue`, `@WrapOperation`) в ваниль; (5) никогда `@Overwrite`. Если миксин всё же пишете, подумайте о том, чтобы открыть его как собственное событие, чтобы другие моды тоже могли подцепиться (паттерн из документации Fabric: интерфейс колбэка и миксин, который его вызывает).
- **Серверные моды** (админ-инструменты, правки геймплея для ванильных клиентов): без записей в собственных реестрах, о которых должен знать клиент (блоки и предметы нужны клиенту), без собственных пакетов, кроме необязательных; используйте ванильные пакеты и подходы в духе Polymer; в Fabric ставьте `environment: "server"` или оставляйте `*` и защищайте клиентский код. NeoForge провела «Serverside Summer» (2025) вокруг этого сценария.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Events (NeoForge)](https://docs.neoforged.net/docs/concepts/events) | NeoForge docs, 2026 (26.1) | Шины, подписчики, приоритеты |
| [Events (Fabric)](https://docs.fabricmc.net/develop/events/) | Fabric docs, 2026 (26.2) | Колбэки, свои события через Mixin |
| [Fabric API repository (26.3 branch)](https://github.com/FabricMC/fabric-api/tree/26.3) | FabricMC, 2026 | Список модулей; читайте javadoc классов событий |
| [NeoForge repository](https://github.com/neoforged/NeoForge) | NeoForged | Классы событий, патчи |
| [2025: Big Changes are Coming](https://neoforged.net/news/2025-retrospection/) | NeoForged, 2026 | Удаление `bus` из `@EventBusSubscriber`, `@OnlyIn` не обрабатывается |
| [2025 Serverside Summer](https://neoforged.net/news/2025serversidesummer/) | NeoForged, 2025 | Проектирование серверных модов |
| [NeoForge 21.9 for Minecraft 1.21.9](https://neoforged.net/news/21.9release/) | NeoForged, 2025 | События рендерера, изменения API FML |
| [NeoForge 21.6 for Minecraft 1.21.6](https://neoforged.net/news/21.6release/) | NeoForged, 2025 | `ValueIOSerializable` |
| [NeoForge 21.11 for Minecraft 1.21.11](https://neoforged.net/news/21.11release/) | NeoForged, 2025 | Данные запечённых квадов, JSpecify |

## Миксины

Раздел про SpongePowered Mixin и MixinExtras. Правила одинаковы для модов любого вида: контентных, технических, оптимизационных, клиентских.

**Ключевые выводы**

- Mixin — внедрение байткода при загрузке класса, настраивается файлом `<modid>.mixins.json` (`package`, `compatibilityLevel` — на 26.x `JAVA_25` `[не проверено]`, массивы `mixins`/`client`/`server`, `injectors.defaultRequire: 1`, необязательный `plugin`). Fabric Loader поставляет форк Mixin от Fabric (`sponge-mixin 0.17.5+mixin.0.8.7`), а **MixinExtras встроен** в Fabric Loader 0.15.0+ и NeoForge 20.2.84+ (там не нужен JiJ). FML реализует собственный MixinService, поддерживает конфиги миксинов, зависящие от других модов, и «уровни совместимости» для обновлений Mixin.
- **С 26.1 нет refmap и шага ремаппинга**: игра работает с именами Mojang, поэтому цели миксинов — имена из декомпилированного кода. Шум с `remap = false` и баги refmap исчезли, но **бинарной совместимости между версиями MC нет** (перекомпилируйте под каждую версию). Цели ищите через `mcsrc.dev` или декомпилированные исходники в IDE.
- **Предпочтение инжекторов (от самых совместимых к самым конфликтным):** `@Inject` (с `CallbackInfo(Returnable)`, `cancellable = true` только при необходимости) и инжекторы MixinExtras `@ModifyExpressionValue`, `@ModifyReturnValue`, `@ModifyReceiver`, `@WrapWithCondition`, `@WrapOperation` (цепочка между модами: каждая обёртка вызывает `original.call(...)`), `@WrapMethod` (обёртка всего метода, тоже в цепочке); сахар `@Local` (захват локальных по типу или порядковому номеру вместо хрупкого `LocalCapture`), `@Share` (передача значений между инжекторами), `@Cancellable`. Избегайте `@Redirect` (вызов может перенаправить только один мод, конфликты роняют игру), `@ModifyConstant` по частым константам и **`@Overwrite`** (заменяет метод для всех). **Expressions** MixinExtras (`@Definition` и `@Expression("...")`, цель `MIXINEXTRAS:EXPRESSION`) находят код по Java-подобному шаблону вместо хрупких `ordinal` и `shift`.
- **Аксессоры и инвокеры:** `@Mixin(Target.class) interface TargetAccessor { @Accessor("field") T modid$getField(); @Invoker("method") R modid$callMethod(...); }`; `@Mutable` на сеттерах final-полей; статические аксессоры — `static`-методы интерфейса, которые бросают исключение (тело не выполняется); для final-классов приведение `((TargetAccessor) (Object) obj)`. Префиксуйте все добавляемые миксином члены `modid$`, чтобы моды не сталкивались.
- **Этикет совместимости:** каждый инжектор минимален и точечен; `require = 1` (или `defaultRequire`), чтобы пропущенная цель падала сразу в разработке; не инжектируйте в горячие пути с аллокациями (аллокация `CallbackInfo` в `@Inject` на потиковом методе сущности стоит денег; там лучше `@ModifyExpressionValue` и `@WrapOperation`); не считайте свой инжектор единственным; `priority` (по умолчанию 1000) меняйте только для реальных конфликтов порядка; миксинам в чужие моды нужны `@Pseudo` или плагин конфига или условный конфиг, чтобы отсутствие целевого мода не было крашем; не кладите игровую логику в классы миксинов, вызывайте обычный класс.
- **Внедрение интерфейсов:** добавьте интерфейс в ванильный класс, чтобы добавленные миксином методы были видны на этапе компиляции: Fabric class tweaker `inject-interface` (и `custom.loom:injected_interfaces` в `fabric.mod.json` на старом Loom), NeoForge и MDG `interfaceInjectionData` (MDG `[не проверено]`).
- **Отладка:** `-Dmixin.debug.export=true` пишет классы после применения миксинов в `.mixin.out/` (декомпилируются, если есть Fernflower или Vineflower) и показывает, что реально выполняла игра; `-Dmixin.debug.verbose=true`, `-Dmixin.debug.countInjections=true` (неудачные инжекции становятся ошибками), `-Dmixin.checks=true`, `-Dmixin.dumpTargetOnFailure=true` (дамп целевого класса без миксина при ошибке), `-Dmixin.debug=true` (всё вместе). Плагин MinecraftDev для IntelliJ проверяет цели и генерирует инжекторы.
- **Частые ошибки** (тексты `[не проверено]`): `InvalidInjectionException: ... Critical injection failure: @Inject annotation on X could not find any targets matching` (цель переименована в новой версии MC: сверьтесь с декомпилированным кодом); `InvalidMixinException ... is not a valid mixin target` (миксин в клиентский класс в общем конфиге на выделенном сервере: перенесите в массив `client`); `ClassMetadataNotFoundException`; `Mixin transformation of X failed` от двух модов, делающих `@Redirect` одного вызова; `MixinApplyError ... @Shadow field not located`; `IllegalClassLoadError: X is in a defined mixin package` (прямая ссылка на класс миксина из обычного кода: ссылайтесь только на интерфейсы аксессоров).

Обёртка вместо редиректа: мод оптимизации пропускает дорогой вызов, не ломая цепочку других модов. `[не проверено]`

```java
@Mixin(LevelChunk.class)
abstract class LevelChunkMixin {
    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/chunk/LevelChunk;expensiveUpdate()V"))  // цель условная
    private void examplemod$throttle(LevelChunk self, Operation<Void> original) {
        if (ThrottleState.shouldRun(self)) original.call(self);
    }
    @ModifyReturnValue(method = "isEmpty", at = @At("RETURN"))
    private boolean examplemod$alsoEmptyIfVirtual(boolean original) { return original || VirtualChunks.contains(this); }
}
```

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Mixin wiki](https://github.com/SpongePowered/Mixin/wiki) | SpongePowered | Официальные понятия |
| [Introduction to Mixins: Understanding Mixin Architecture](https://github.com/SpongePowered/Mixin/wiki/Introduction-to-Mixins---Understanding-Mixin-Architecture) | Mumfrey | Ментальная модель |
| [Introduction to Mixins: Overwriting Methods](https://github.com/SpongePowered/Mixin/wiki/Introduction-to-Mixins---Overwriting-Methods) | Mumfrey | Почему `@Overwrite` — крайняя мера |
| [Advanced Mixin Usage: Callback Injectors](https://github.com/SpongePowered/Mixin/wiki/Advanced-Mixin-Usage---Callback-Injectors) | Mumfrey | Подробности `@Inject` |
| [Injection Point Reference](https://github.com/SpongePowered/Mixin/wiki/Injection-Point-Reference) | SpongePowered | Значения `@At` |
| [Mixin Java System Properties](https://github.com/SpongePowered/Mixin/wiki/Mixin-Java-System-Properties) | SpongePowered | `mixin.debug.export` и другие |
| [MixinExtras README](https://github.com/LlamaLad7/MixinExtras) | LlamaLad7, 2026 | Установка на каждом загрузчике, 0.5.5 |
| [MixinExtras wiki](https://github.com/LlamaLad7/MixinExtras/wiki) | LlamaLad7 | Индекс инжекторов и сахара |
| [WrapOperation](https://github.com/LlamaLad7/MixinExtras/wiki/WrapOperation) | LlamaLad7 | Цепочечная альтернатива `@Redirect` |
| [ModifyExpressionValue](https://github.com/LlamaLad7/MixinExtras/wiki/ModifyExpressionValue) | LlamaLad7 | Правка значения на месте |
| [ModifyReturnValue](https://github.com/LlamaLad7/MixinExtras/wiki/ModifyReturnValue) | LlamaLad7 | Смена результата без отмены |
| [WrapWithCondition](https://github.com/LlamaLad7/MixinExtras/wiki/WrapWithCondition) | LlamaLad7 | Условный пропуск void-вызова |
| [WrapMethod](https://github.com/LlamaLad7/MixinExtras/wiki/WrapMethod) | LlamaLad7 | Обёртка метода целиком |
| [Local](https://github.com/LlamaLad7/MixinExtras/wiki/Local) | LlamaLad7 | Безопасный захват локальных переменных |
| [Share](https://github.com/LlamaLad7/MixinExtras/wiki/Share) | LlamaLad7 | Общее состояние между инжекторами |
| [Expressions](https://github.com/LlamaLad7/MixinExtras/wiki/Expressions) | LlamaLad7 | Адресация по шаблону |
| [Mixin Accessors (Fabric)](https://docs.fabricmc.net/develop/mixins/accessors) | Fabric docs, 2026 (26.2) | Аксессоры, инвокеры, именование |
| [Java Bytecode for Mixins (Fabric)](https://docs.fabricmc.net/develop/mixins/bytecode) | Fabric docs, 2026 | Дескрипторы, опкоды |
| [Mixin introduction (Fabric wiki)](https://wiki.fabricmc.net/tutorial:mixin_introduction) | Fabric wiki | Старая, но подробная серия уроков |
| [MinecraftDev IntelliJ plugin](https://github.com/minecraft-dev/MinecraftDev) | minecraft-dev | Проверка целей миксинов и генерация |
| [mcsrc.dev](https://mcsrc.dev/) | сообщество, 2026 | Декомпилятор в браузере для миксинов и class tweakers |

## 12. Access wideners, class tweakers и access transformers

**Ключевые выводы**

- **Class tweakers Fabric** (переименованные access wideners; Loader 0.18.0+, Loom 1.12+): файл `<modid>.classtweaker`, заголовок `classTweaker v1 official` (для расширения enum нужен `v2`), директивы `accessible`, `extendable`, `mutable` (и версии с префиксом `transitive-`, экспортируемые зависимым модам), `inject-interface`, `extend-enum`. Loom: `loom { accessWidenerPath = file("src/main/resources/<modid>.classtweaker") }`; ключ в `fabric.mod.json` по-прежнему `"accessWidener"`. Неверные записи при работе молча игнорируются: запускайте задачу `validateAccessWidener` в CI. Целями могут быть только ванильные классы.
- **Расширение enum (Fabric Loader 0.19.0+, Loom 1.16+ по документации и 1.17 по посту 26.2):** `extend-enum net/minecraft/world/inventory/RecipeBookType EXAMPLEMOD_TYPE`; константу получайте через `valueOf` и кэшируйте; `switch`-выражения без `default` в чужих модах могут бросить исключение; избегайте сериализуемых enum.
- **Access transformers NeoForge:** `META-INF/accesstransformer.cfg` (находится автоматически; дополнительные файлы — `[[accessTransformers]] file="..."` в `neoforge.mods.toml` и `neoForge { accessTransformers.from '...' }` в MDG). Синтаксис: `public net.minecraft.server.MinecraftServer random`, `protected-f ...` (снять final), методы с JVM-дескрипторами: `public net.minecraft.Util makeExecutor(Ljava/lang/String;)Lnet/minecraft/TracingExecutor;`. С 26.1 имена Mojang, SRG нет. AT меняет только указанный член, не его переопределения: расширение доступа метода, чьё переопределение остаётся более узким, приводит к краху.
- **Расширяемые enum NeoForge:** только enum с интерфейсом `IExtensibleEnum`; `enumExtensions="META-INF/enumextensions.json"` с записями `{enum, name (с префиксом modid), дескриптор конструктора, параметры}`; значения — через `valueOf` или `EnumProxy#getValue()`.
- Предпочтение по порядку: публичный API → миксин-аксессор (локальный, явный) → AW или AT (глобальные, влияют на вид компиляции каждого мода; записи `transitive` просачиваются в зависимые моды) → рефлексия (самая медленная, ломается молча).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Class Tweakers (Fabric)](https://docs.fabricmc.net/develop/class-tweakers/) | Fabric docs, 2026 (26.2) | Формат, настройка Loom, проверка |
| [Access Widening (Fabric)](https://docs.fabricmc.net/develop/class-tweakers/access-widening) | Fabric docs, 2026 | Справочник по директивам |
| [Interface Injection (Fabric)](https://docs.fabricmc.net/develop/class-tweakers/interface-injection) | Fabric docs, 2026 | `inject-interface` |
| [Enum Extension (Fabric)](https://docs.fabricmc.net/develop/class-tweakers/enum-extension) | Fabric docs, 2026 | `extend-enum`, ограничения |
| [Access Transformers (NeoForge)](https://docs.neoforged.net/docs/advanced/accesstransformers) | NeoForge docs, 2026 (26.1) | Синтаксис AT, настройка MDG |
| [Extensible Enums (NeoForge)](https://docs.neoforged.net/docs/advanced/extensibleenums/) | NeoForge docs, 2026 (26.1) | `enumextensions.json` |
| [ModDevGradle README](https://github.com/neoforged/ModDevGradle) | NeoForged | AT, данные внедрения интерфейсов, конфигурации запуска |

## 13. Возможности, вложения и передача предметов, жидкостей, энергии

**Ключевые выводы**

- **Возможности NeoForge (capabilities)** (переписаны в 20.3): типизированные поиски `BlockCapability<T, C>`, `EntityCapability<T, C>`, `ItemCapability<T, C>`, создаются через `create/createSided/createVoid` (то же имя даёт тот же объект). Провайдеры регистрируются в `RegisterCapabilitiesEvent` (шина мода): `registerBlockEntity(cap, beType, (be, side) -> ...)`, `registerBlock(cap, provider, blocks...)`, `registerEntity`, `registerItem`. Запрос: `level.getCapability(cap, pos, side)` (или с известным состоянием и блок-сущностью), `entity.getCapability(cap, ctx)`, `stack.getCapability(cap, ctx)`; `null` — нет возможности. Для повторных запросов к соседям (кабели, трубы, машины) держите `BlockCapabilityCache.create(cap, serverLevel, pos, side)`; вызывайте `level.invalidateCapabilities(pos)` всякий раз, когда возможность вашего блока появляется, исчезает или меняется вне создания и удаления блок-сущности и загрузки и выгрузки чанка.
- **Переработка transfer в NeoForge (21.9.1-beta+):** `ResourceHandler<ItemResource>`, `ResourceHandler<FluidResource>`, `EnergyHandler` за `Capabilities.Item.BLOCK/ENTITY/ENTITY_AUTOMATION/ITEM`, `Capabilities.Fluid.BLOCK/ENTITY/ITEM`, `Capabilities.Energy.BLOCK/ENTITY/ITEM`. Операции транзакционные: `try (Transaction tx = Transaction.openRoot()) { int moved = handler.insert(res, amount, tx); ...; tx.commit(); }` (без commit — откат). Свои обработчики пишите на `SnapshotJournal` (`createSnapshot`/`revertToSnapshot`, `onRootCommit`) и вызывайте `updateSnapshots(tx)` перед изменением; готовые `ItemStacksResourceHandler` (переопределите `onContentsChanged` → `setChanged()`), `SimpleEnergyHandler`, обработчики `ItemAccess*` для предметов внутри предметов. `ResourceHandlerUtil.moveStacking(...)` — для перемещения.
- **Старые `IItemHandler`, `IFluidHandler`, `IEnergyStorage`, `ItemStackHandler` объявлены устаревшими в 21.9 и удалены в NeoForge 26.3** (есть в ветке 26.2.x, нет в 26.3.x). Моды для 1.21.1 (самая большая база сборок) пока на старом API, поэтому порт с 1.21.1 прямо на 26.3 требует переписать весь код инвентарей, жидкостей и энергии. На время миграции на 21.9–26.2 используйте мосты вроде `IItemHandler.of(newHandler)` (обратной обёртки нет).
- **Transfer API Fabric** (`fabric-transfer-api-v1`, та же идея транзакций, появился раньше): `Storage<ItemVariant>` и `Storage<FluidVariant>`, `StorageView`, `SingleSlotStorage`, `Transaction.openOuter()` и `TransactionContext`, `SnapshotParticipant` для своих хранилищ, `SingleVariantStorage`, обёртки `InventoryStorage`/`ContainerStorage`, `ItemStorage.SIDED` и `FluidStorage.SIDED` (`BlockApiLookup` из `fabric-api-lookup-api-v1`), `StorageUtil.move(...)`, `ContainerItemContext` для предметов с жидкостью. Количество жидкости у Fabric в каплях (`FluidConstants.BUCKET = 81000`), у NeoForge — в милливедрах (1000): конвертируйте на границах абстракций (константа `[не проверено]`).
- **Энергия на Fabric:** Team Reborn Energy (`teamreborn:energy`, `EnergyStorage.SIDED`, `SimpleEnergyStorage`; v5.x для MC 26.1+, последний релиз 2026-01-01; соглашения: передача «толчком», 1 уголь = 4000). `EnergyHandler` NeoForge следует соглашению RF/FE.
- **Вложения данных NeoForge (attachments):** `DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID)`; `AttachmentType.builder(() -> default).serialize(MAP_CODEC_OR_SERIALIZER).copyOnDeath().sync(STREAM_CODEC | (holder, to) -> pred, STREAM_CODEC).build()`; владельцы: блок-сущности, чанки, сущности, уровни (**не стеки предметов: для них компоненты данных**). `getData` создаёт значение по умолчанию; `hasData`; `setData` помечает блок-сущность или чанк изменёнными, а изменение возвращённого объекта — нет (вызовите `setChanged()` или `setUnsaved(true)`). Синхронизация пересылает значение целиком при каждом `setData`.
- **Data Attachment API Fabric** (`fabric-data-attachment-api-v1`): `AttachmentRegistry.create(id, b -> b.initializer(...).persistent(CODEC).syncWith(STREAM_CODEC, AttachmentSyncPredicate.targetOnly()).copyOnDeath())`; цели — `Entity`, `BlockEntity`, `ServerLevel`, `ChunkAccess` и `GlobalAttachments` через `MinecraftServer#globalAttachments()`; методы `getAttached`, `getAttachedOrCreate`, `setAttached`, `modifyAttached`, `removeAttached`. Значения должны быть неизменяемыми. Класс с `AttachmentRegistry.create` должен загрузиться при инициализации, иначе сохранённые данные не десериализуются. Альтернатива на Fabric — Cardinal Components API (синхронизация и хуки тиков по компонентам).
- **Кросс-загрузчиковые абстракции:** (а) MultiLoader-Template (подпроекты common, fabric, neoforge; ветки до 26.3) со своим небольшим интерфейсом платформы на service loader; (б) Architectury API и плагин (реестры, события, сеть; Architectury API на Modrinth для 26.1–26.3); (в) библиотечные моды со своими кросс-загрузчиковыми слоями (Balm, Puzzles Lib, Resourceful Lib). Абстракции отстают от новых версий MC; чем тоньше слой, тем дешевле ежегодный порт на 26.x. Общего API передачи предметов, жидкостей и энергии между загрузчиками нет: оборачивайте по загрузчику за своим интерфейсом.
- Слоты аксессуаров: Curios API теперь выходит и для Fabric, и для NeoForge на 26.1–26.3; у Trinkets и Accessories на 2026-11-07 нет сборок под 26.x на Modrinth.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Capabilities (NeoForge)](https://docs.neoforged.net/docs/inventories/capabilities) | NeoForge docs, 2026 (26.1) | Регистрация, запрос, кэш, инвалидация |
| [Transactions (NeoForge)](https://docs.neoforged.net/docs/inventories/transactions/) | NeoForge docs, 2026 (26.1) | `Transaction`, `SnapshotJournal`, обработчики |
| [The Transfer Rework](https://neoforged.net/news/21.9-transfer-rework/) | NeoForged, 2025 | Соответствие старого и нового API, шаги миграции |
| [Data Attachments (NeoForge)](https://docs.neoforged.net/docs/datastorage/attachments) | NeoForge docs, 2026 | Билдер вложений, синхронизация, владельцы |
| [Data Attachments (Fabric)](https://docs.fabricmc.net/develop/serialization/data-attachments) | Fabric docs, 2026 (26.2) | `AttachmentRegistry`, предикаты синхронизации |
| [Transfer API (Fabric wiki)](https://wiki.fabricmc.net/tutorial:transfer-api) | Fabric wiki | Серия уроков по Storage и Transaction |
| [API Lookup (Fabric wiki)](https://wiki.fabricmc.net/tutorial:api_lookup) | Fabric wiki | `BlockApiLookup` |
| [fabric-transfer-api-v1 source (26.3)](https://github.com/FabricMC/fabric-api/tree/26.3/fabric-transfer-api-v1) | FabricMC, 2026 | Javadoc — настоящая документация |
| [TechReborn/Energy](https://github.com/TechReborn/Energy) | TechReborn, 2026 | API энергии для Fabric, соглашения |
| [Cardinal Components API (Modrinth)](https://modrinth.com/mod/cardinal-components-api) | Ladysnake, 2026 | Библиотека компонентов для Fabric |
| [Architectury API (Modrinth)](https://modrinth.com/mod/architectury-api) | architectury, 2026 | Кросс-загрузчиковый API |
| [Architectury documentation](https://docs.architectury.dev/) | architectury | Плагин и Loom |
| [MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template) | jaredlll08, 2026 | Раскладка common, fabric, neoforge |
| [Curios API (Modrinth)](https://modrinth.com/mod/curios) | TheIllusiveC4, 2026 | Слоты аксессуаров, теперь мультилоадер |

## 14. Рендеринг и клиент

**Ключевые выводы**

- **Направление развития (1.21.2 → 26.3): сначала извлечь, потом отправить.** Каждый рендерер копирует состояние игры в render state на главном потоке (`extractRenderState`) и отправляет запросы на отрисовку в коллектор; ваниль группирует и упорядочивает их. Код, читающий сущности или блок-сущности во время рисования либо делающий вызовы GL, ломается с каждым выпуском.
- **Blaze3D и RenderPipeline:** 1.21.5 заменил JSON шейдеров на `RenderPipeline`, описанные кодом; рисование идёт через `GpuDevice` (`RenderSystem.getDevice()`), `CommandEncoder.createRenderPass(...)`, `RenderPass#setPipeline/bindTexture/draw`. 1.21.11 выделил выборку в `GpuSampler` (`RenderSystem.getSamplerCache()`) и перенёс ванильные render types в `RenderTypes`. 26.1 разделил `GpuDevice`, `CommandEncoder`, `RenderPass` на проверяемые обёртки и `*Backend`. **26.2 добавил экспериментальный бэкенд Vulkan** (`com.mojang.blaze3d.vulkan` рядом с `opengl`), переписал `VertexFormat` (атрибуты строятся динамически, enum `PrimitiveTopology` и `IndexType`, до 16 вершинных буферов), раскладки bind group, коэффициенты смешивания, форматы GPU. **26.3 перенёс весь API рендера в `com.mojang.renderpearl`** (пакеты `api`, `backend`, `frontend`), компилирует GLSL через Shaderc для GL и Vulkan (`#moj_import` → `#include`, явные `layout(location=...)` у in/out, нужен `GL_ARB_separate_shader_objects`), добавил порядконезависимую прозрачность (определение `OIT_ALPHA_ONLY`) и убрал `RenderSystem#outputColorTextureOverride`. Любой прямой вызов `GL11.gl*` теперь ошибка. Свои конвейеры: NeoForge `RegisterRenderPipelinesEvent` или Fabric `FabricRenderPipeline`.
- **Рендеринг по «фичам» (1.21.9+, обобщён в 26.2):** сущности, блок-сущности, частицы и своя геометрия отправляют данные в `SubmitNodeCollector` (`submitModel`, `submitBlockModel`, `order(n)`), выполняет `FeatureRenderDispatcher`; **26.2 убрал `MultiBufferSource` вне рендера чанков**; свои фичи — запись `SubmitNode`, `FeatureRenderPhase` и `FeatureRenderer`. NeoForge добавляет `submitMultiLayerBlockModel`, `SubmitCustomGeometryEvent`, `ExtractLevelRenderStateEvent`, `RenderLevelStageEvent`; Fabric — `LevelExtractionEvents`/`LevelRenderEvents` (бывший `WorldRenderEvents`), `SubmitRenderPhases`, `FeatureRendererRegistry`.
- **BlockEntityRenderer (1.21.9+):** `BlockEntityRenderer<BE, S extends BlockEntityRenderState>` с `createRenderState()`, `extractRenderState(be, state, partialTick, cameraPos, crumblingOverlay)` (вызовите super или `extractBase`) и `submit(state, poseStack, SubmitNodeCollector, CameraRenderState)`. Регистрация: NeoForge `EntityRenderersEvent.RegisterRenderers#registerBlockEntityRenderer`, Fabric `BlockEntityRendererRegistry`. Предметы, чей вид зависит от данных блок-сущности, используют специальные рендереры моделей предметов (`SpecialModelRenderer`, NeoForge `RegisterSpecialModelRendererEvent`) вместо удалённого BEWLR.
- **Модели:** JSON-модели блоков, blockstates (variants и multipart) и клиентские определения предметов (`assets/<ns>/items/`). С 26.1 слой рендера выводится по прозрачности текстуры для каждого квада (флаг материала `force_translucent`), `BlockRenderDispatcher` и `ItemRenderer` удалены, добавлен `MutableQuad` (NeoForge 26.1), 26.3 добавил `ItemQuads.split(quads)` для твёрдых и полупрозрачных квадов предметов. Своя загрузка моделей: NeoForge `ModelEvent.*` и загрузчики своей геометрии; Fabric Model Loading API (`ModelLoadingPlugin`, `ModelModifier`, `UnbakedModelDeserializer`) и Renderer API/FRAPI (`QuadEmitter`, `MutableQuadView`, `Renderer`; Indigo — реализация по умолчанию, Sodium тоже её реализует).
- **Жидкости:** Fabric 26.1 `FluidRenderingRegistry.register(fluid, FluidModel.Unbaked)` заменяет большинство применений `FluidRenderHandler`; NeoForge `IClientFluidTypeExtensions` через `RegisterClientExtensionsEvent` `[не проверено]` (класс).
- **HUD:** Fabric `HudElementRegistry.attachElementBefore/After(VanillaHudElements.CHAT, id, (GuiGraphicsExtractor g, DeltaTracker dt) -> ...)` (`HudRenderCallback` удалён в 26.1); NeoForge `RegisterGuiLayersEvent` и идентификаторы `VanillaGuiLayers`. Для интерполяции — `deltaTracker.getGameTimeDeltaPartialTick(false)`.
- **Шейдеры, Iris и Sodium:** Sodium 0.9.3-beta (26.3, Fabric и NeoForge) заменяет рендеринг чанков, поэтому миксины в ванильную сборку мешей чанков с Sodium ничего не делают; для своей геометрии блоков используйте FRAPI и API моделей NeoForge. Iris 1.11.x (26.3) запускает шейдерпаки и заменяет ванильные core-шейдеры, поэтому собственные `RenderPipeline` под шейдерпаками могут рисоваться неверно или пропускаться (поведение Iris `[не проверено]`): рисуйте через ванильные render types и конвейеры, где возможно, и проверяйте с Iris и популярным шейдерпаком. Переопределения core-шейдеров из ресурспаков хрупки на всей линии 26.x (GLSL теперь компилируется Shaderc).
- **Гигиена клиентских модов:** Fabric `environment: "client"`; NeoForge `displayTest` и `@Mod(dist = Dist.CLIENT)` только для клиента; никогда не регистрируйте контент, который должен быть на сервере.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Features (NeoForge)](https://docs.neoforged.net/docs/rendering/feature) | NeoForge docs, 2026 (26.1) | Фазы отправки и рендеринга |
| [Block Entity Renderer (NeoForge)](https://docs.neoforged.net/docs/blockentities/ber) | NeoForge docs, 2026 (26.1) | Паттерн render state для BER |
| [Models (NeoForge)](https://docs.neoforged.net/docs/resources/client/models/) | NeoForge docs, 2026 | JSON моделей, загрузчики |
| [Client items (NeoForge)](https://docs.neoforged.net/docs/resources/client/models/items) | NeoForge docs, 2026 | Определения `items/`, специальные модели |
| [Textures (NeoForge)](https://docs.neoforged.net/docs/resources/client/textures) | NeoForge docs, 2026 | Атласы, источники спрайтов |
| [Basic rendering concepts (Fabric)](https://docs.fabricmc.net/develop/rendering/basic-concepts) | Fabric docs, 2026 | Конвейеры, форматы вершин |
| [World rendering (Fabric)](https://docs.fabricmc.net/develop/rendering/world) | Fabric docs, 2026 | События рендера уровня |
| [Rendering in the HUD (Fabric)](https://docs.fabricmc.net/develop/rendering/hud) | Fabric docs, 2026 (26.2) | `HudElementRegistry` |
| [Block Entity Renderer (Fabric)](https://docs.fabricmc.net/develop/blocks/block-entity-renderer) | Fabric docs, 2026 | BER на Fabric пошагово |
| [Block Models (Fabric)](https://docs.fabricmc.net/develop/blocks/block-models) | Fabric docs, 2026 | JSON моделей |
| [Item appearance (Fabric)](https://docs.fabricmc.net/develop/items/item-appearance) | Fabric docs, 2026 | Модели и оттенки предметов |
| [Primer 1.21.5 (Render Pipeline Rework)](https://github.com/neoforged/.github/blob/main/primers/1.21.5/index.md) | ChampionAsh5357, 2025 | `GpuDevice`, `RenderPass`, конвейеры |
| [Primer 26.2 (Vulkan, feature takeover)](https://github.com/neoforged/.github/blob/main/primers/26.2/index.md) | ChampionAsh5357, 2026 | Vulkan, форматы вершин, `SubmitNodeStorage` |
| [Primer 26.3 (Renderpearl, SDL, OIT)](https://github.com/neoforged/.github/blob/main/primers/26.3/index.md) | ChampionAsh5357, 2026 | Пакеты Renderpearl, синтаксис шейдеров |
| [Wavelet OIT paper](https://arxiv.org/pdf/2201.00094) | arXiv, 2022 | Основа OIT в 26.3 |
| [Sodium (GitHub)](https://github.com/CaffeineMC/sodium) | CaffeineMC, 2026 | Замена рендерера, поддержка FRAPI |
| [Sodium wiki](https://github.com/CaffeineMC/sodium/wiki) | CaffeineMC | Рекомендации по совместимости для авторов модов |
| [Iris Shaders (GitHub)](https://github.com/IrisShaders/Iris) | IrisShaders, 2026 | Загрузчик шейдерпаков |
| [Iris Shaders (Modrinth)](https://modrinth.com/mod/iris) | IrisShaders, 2026 | Поддерживаемые версии (26.1–26.3) |
| [Sodium (Modrinth)](https://modrinth.com/mod/sodium) | CaffeineMC, 2026 | Поддерживаемые версии, сборки NeoForge |

## 15. Конфигурация

**Ключевые выводы**

- **`ModConfigSpec` NeoForge:** строится через `ModConfigSpec.Builder` (`comment`, `translation`, `push`/`pop`, `define`, `defineInRange`, `defineEnum`, `defineList(AllowEmpty)` с проверкой элементов), обычно через `builder.configure(MyConfig::new)`, возвращающий `Pair<config, spec>`. Регистрация в конструкторе: `container.registerConfig(ModConfig.Type.X, SPEC)`. Типы: `STARTUP` (читается сразу; не используйте для включения контента, рассинхронизация), `CLIENT` (только физический клиент), `COMMON` (обе стороны, не синхронизируется, загружается до `FMLCommonSetupEvent`), `SERVER` (переопределение по миру в `serverconfig/`, **синхронизируется клиентам**, загружается до `ServerAboutToStartEvent`). Читайте `VALUE.get()` (кэшируется) только после загрузки; реагируйте в `ModConfigEvent.Loading/Reloading/Unloading` (шина мода). Встроенный GUI: `container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new)` на клиенте.
- Классическая ошибка: чтение значения SERVER-конфига в статическом инициализаторе, колбэке реестра или в главном меню клиента → `IllegalStateException: Cannot get config value before config is loaded`. Регистрация контента не должна зависеть от конфигов (реестры фиксированы; используйте данные и условия).
- **У Fabric встроенной конфигурации нет.** Популярные варианты со сборками под 26.3 (Modrinth, 2026-11-07): Cloth Config API (также NeoForge и Forge), YetAnotherConfigLib (YACL, Fabric и NeoForge), конфиг owo-lib (процессор аннотаций; сборки только 26.1–26.2, под 26.3 пока нет), Fzzy Config, MidnightLib, Resourceful Config; Forge Config API Port приносит конфиги в стиле `ModConfigSpec` на Fabric (частый выбор для мультилоадер-модов). Mod Menu (Fabric) даёт точку входа экрана конфигурации (`ModMenuApi#getModConfigScreenFactory`).
- Синхронизацию конфигурации делайте явной: серверно-авторитетные игровые значения читайте на сервере; если они нужны клиенту (предсказания, подсказки), отправляйте их пакетом фазы конфигурации или входа либо используйте синхронизацию SERVER-конфига NeoForge.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Configuration (NeoForge)](https://docs.neoforged.net/docs/misc/config) | NeoForge docs, 2026 (26.1) | `ModConfigSpec`, типы, экран |
| [Cloth Config (Modrinth)](https://modrinth.com/mod/cloth-config) | shedaniel, 2026 | Экраны конфигурации, 26.1–26.3 |
| [Cloth Config docs](https://shedaniel.gitbook.io/cloth-config) | shedaniel | Документация API |
| [YetAnotherConfigLib (Modrinth)](https://modrinth.com/mod/yacl) | isXander, 2026 | Современный GUI конфигурации, 26.1–26.3 |
| [YACL docs](https://docs.isxander.dev/yet-another-config-lib) | isXander | Документация API |
| [owo-lib (Modrinth)](https://modrinth.com/mod/owo-lib) | Wisp Forest, 2026 | Конфигурация на аннотациях и UI |
| [owo config docs](https://docs.wispforest.io/owo/config/getting-started) | Wisp Forest | Настройка конфигурации |
| [Forge Config API Port (Modrinth)](https://modrinth.com/mod/forge-config-api-port) | Fuzss, 2026 | Конфигурация в стиле NeoForge на Fabric |
| [Fzzy Config (Modrinth)](https://modrinth.com/mod/fzzy-config) | fzzyhmstrs, 2026 | Синхронизируемые конфигурации, мультилоадер |
| [Mod Menu (GitHub)](https://github.com/TerraformersMC/ModMenu) | TerraformersMC | Точка входа конфигурации на Fabric |

## 16. Команды, клавиши, игровые правила

**Ключевые выводы**

- **Команды** используют Brigadier от Mojang: `Commands.literal("x").requires(...).then(Commands.argument("target", EntityArgument.player()).executes(ctx -> ...))`. Регистрация: Fabric `CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> ...)`, клиентские команды — `ClientCommandRegistrationCallback` и `ClientCommands.literal`; NeoForge `RegisterCommandsEvent` (игровая шина) `event.getDispatcher()`, `event.getBuildContext()`. Возвращайте число затронутых объектов (`Command.SINGLE_SUCCESS` = 1); для пользовательских ошибок бросайте `CommandSyntaxException` (`SimpleCommandExceptionType`); ответ — `source.sendSuccess(() -> Component..., broadcastToOps)`. `[не проверено]`
- **Права изменились в 1.21.11:** целочисленные уровни стали `Permission`/`PermissionSet`/`PermissionCheck`; `Commands.LEVEL_*` — это `PermissionCheck`, а `Commands.hasPermission(check)` принимает проверку вместо int, поэтому `.requires(src -> src.hasPermission(2))` из старых туториалов устарело `[устарело]` (замена — `Commands.hasPermission(Commands.LEVEL_...)`). Свои типы прав регистрируют `MapCodec` в `BuiltInRegistries.PERMISSION_TYPE`. Fabric API добавляет `fabric-permission-api-v1` (`PermissionNode`, `PermissionPredicates`, `PermissionEvents`) для совместимости с плагинами прав. 26.1 добавил права на отправку и приём чата (только клиент). 26.3 добавил `CommandResponseTracker` для единообразных ответов при нескольких целях.
- Свои типы аргументов нужно регистрировать (`ArgumentTypeRegistry.registerArgumentType` на Fabric, реестр `ArgumentTypeInfos` на NeoForge), и они должны существовать на клиенте, иначе ванильных клиентов отключит; серверные моды должны собирать команды из ванильных типов аргументов.
- **Клавиши:** `new KeyMapping("key.examplemod.open_menu", InputConstants.KEY_M, category)`; с 1.21.9 категория — запись `KeyMapping.Category`, которую нужно зарегистрировать (NeoForge `RegisterKeyMappingsEvent#registerCategory` и `register(mapping)`; Fabric `KeyMappingHelper.registerKeyMapping`). Опрашивайте `while (mapping.consumeClick())` в событии клиентского тика (`ClientTickEvents.END_CLIENT_TICK`, NeoForge `ClientTickEvent.Post`) и отправляйте пакет для серверных эффектов. **26.3 перевёл LWJGL с GLFW на SDL:** коды клавиш — скан-коды SDL, номера кнопок мыши изменились; используйте `InputConstants` и константы `SDLScancode`, никогда сырые числа GLFW.
- **Игровые правила:** 1.21.11 «game rule shuffle» (правила на реестрах); Fabric `GameRuleBuilder`/`GameRuleEvents`, событие изменения игрового правила у NeoForge (21.11 beta).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Brigadier](https://github.com/Mojang/brigadier) | Mojang | Библиотека команд |
| [Commands basics (Fabric)](https://docs.fabricmc.net/develop/commands/basics) | Fabric docs, 2026 | Регистрация, requires, ответы |
| [Command arguments (Fabric)](https://docs.fabricmc.net/develop/commands/arguments) | Fabric docs, 2026 | Свои типы аргументов |
| [Command suggestions (Fabric)](https://docs.fabricmc.net/develop/commands/suggestions) | Fabric docs, 2026 | Провайдеры подсказок |
| [Commands (NeoForge)](https://docs.neoforged.net/docs/misc/commands) | NeoForge docs, 2026 | `RegisterCommandsEvent` |
| [Key Mappings (Fabric)](https://docs.fabricmc.net/develop/key-mappings) | Fabric docs, 2026 | `KeyMappingHelper` |
| [Key Mappings (NeoForge)](https://docs.neoforged.net/docs/misc/keymappings) | NeoForge docs, 2026 | Категории, контексты конфликтов |
| [Game Rules (Fabric)](https://docs.fabricmc.net/develop/game-rules) | Fabric docs, 2026 | `GameRuleBuilder` |
| [Primer 1.21.11 (Permission Overhaul)](https://github.com/neoforged/.github/blob/main/primers/1.21.11/index.md) | ChampionAsh5357, 2025 | API `PermissionCheck` |

## 17. Генерация мира

**Ключевые выводы**

- Генерация мира почти целиком состоит из реестров датапака: `worldgen/biome`, `worldgen/placed_feature`, `worldgen/feature` (26.3; раньше `worldgen/configured_feature`), `worldgen/structure`, `worldgen/structure_set`, `worldgen/template_pool`, `worldgen/processor_list`, `worldgen/density_function`, `worldgen/noise`, `worldgen/noise_settings`, `worldgen/configured_carver`, `dimension`, `dimension_type`; 26.3 добавляет `worldgen/material_rule`, `worldgen/material_condition`, `worldgen/block_state_provider`, `context_float_provider`/`context_int_provider`. Генерируйте их через datagen-bootstrap (`RegistrySetBuilder`), а не рукописным JSON.
- **Ломающие изменения 26.3:** `Feature`, `ConfiguredFeature` и `FeatureConfiguration` слиты в один интерфейс `Feature` (запись, реализующая `Feature`, с `MapCodec`, зарегистрированным в `BuiltInRegistries.FEATURE_TYPE`; `Registries.CONFIGURED_FEATURE` → `Registries.FEATURE`, папка `worldgen/feature`, объект `config` убран из JSON, модификаторы размещения переименованы); то же для карверов; `DensityFunction` разделён на регистрируемый `DensityFunction` (`codec`, `range`, `domainAxes`, `rewriteChildren`, `compileSampler`) и скомпилированный `DensitySampler`; правила поверхности переименованы в **material rules** (`SurfaceRules` → `MaterialRules`, `SurfaceSystem` → `MaterialSystem`, реестры `MATERIAL_RULE_TYPE`/`MATERIAL_CONDITION_TYPE`); `BlockStateProvider` и `StructurePlacement` стали интерфейсами. 26.1 убрал фичу random patch и добавил провайдеры состояний блоков на правилах. Любой мод генерации мира, написанный для 1.21.x, требует настоящей работы по портированию на 26.3.
- **Добавление в существующие биомы:** **biome modifiers** NeoForge (`data/<ns>/neoforge/biome_modifier/*.json`, встроенные типы `neoforge:add_features`, `remove_features`, `add_spawns`, `remove_spawns` и другие; свои — через кодек) и **structure modifiers**; Fabric `BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.UNDERGROUND_ORES, placedFeatureKey)` или `.create(id).add(ModificationPhase.ADDITIONS, selector, ctx -> ...)`. Для этого никогда не пишите миксины в загрузку JSON биомов.
- **Новые биомы в ванильных измерениях:** источник биомов Верхнего мира — фиксированный список параметров multi-noise; для вставки биомов нужны TerraBlender (API регионов), Biolith или Lithostitched (модификаторы генерации на уровне датапака); у всех есть сборки 26.1–26.3. Нижний мир и Энд: Fabric `NetherBiomes`/`TheEndBiomes`.
- Код фич выполняется на рабочих потоках генерации против `WorldGenLevel`: не вызывайте API, доступные только `ServerLevel`, не трогайте блок-сущности в других чанках, не загружайте чанки синхронно, держите детерминизм от переданного `RandomSource`.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Biome Modifiers (NeoForge)](https://docs.neoforged.net/docs/worldgen/biomemodifier) | NeoForge docs, 2026 | Встроенные и свои модификаторы |
| [Features datagen (Fabric)](https://docs.fabricmc.net/develop/data-generation/features) | Fabric docs, 2026 | Фичи и изменение биомов |
| [Primer 26.3 (features, density functions, material rules)](https://github.com/neoforged/.github/blob/main/primers/26.3/index.md) | ChampionAsh5357, 2026 | Переписывание генерации мира в 26.3 |
| [Custom world generation (Minecraft Wiki)](https://minecraft.wiki/w/Custom_world_generation) | Minecraft Wiki | Справочник по генерации мира на датапаках |
| [Density function (Minecraft Wiki)](https://minecraft.wiki/w/Density_function) | Minecraft Wiki | Типы функций плотности |
| [TerraBlender (Modrinth)](https://modrinth.com/mod/terrablender) | Glitchfiend, 2026 | Регионы биомов Верхнего и Нижнего мира |
| [Lithostitched (Modrinth)](https://modrinth.com/mod/lithostitched) | Apollo, 2026 | Библиотека модификаторов генерации |
| [Biolith (Modrinth)](https://modrinth.com/mod/biolith) | TerraformersMC, 2026 | Библиотека размещения биомов |
| [misode worldgen generators](https://misode.github.io/worldgen/) | Misode | Редакторы шума, фич, структур |

## 18. Сохранённые данные и постоянное состояние

**Ключевые выводы**

- **SavedData** (на уровень или, с 26.1, на сервер) хранит мировое состояние мода (заявки фракций, глобальный прогресс заданий, графы сетей). 1.21.5 ввёл `SavedDataType<T>(id, ctx -> new T(), ctx -> CODEC, DataFixTypes)`; **26.1** идентифицирует данные через `Identifier` (разрешается в папку данных, подкаталоги допустимы: `<world>/dimensions/<ns>/<path>/data/examplemod/example/data.dat`) и переименовал `DimensionDataStorage` → `SavedDataStorage`; глобальные данные кладутся на сервер: `server.getDataStorage().computeIfAbsent(TYPE)` вместо «хранить в Верхнем мире». После изменения вызывайте `setDirty()`.
- Выбирайте самого узкого владельца: предмет → компонент данных; блок → состояние блока или поле блок-сущности; сущность или игрок → вложение (`copyOnDeath` для прогресса игрока); чанк → вложение чанка; измерение → SavedData уровня или вложение уровня; весь мир → SavedData сервера или `GlobalAttachments` Fabric.
- Сохраняйте через кодеки (`ValueOutput.store`, кодек `SavedDataType`), чтобы фиксеры данных и смены версий были явными; если ждёте смены формата, добавляйте в свои записи поле версии данных: ванильные `DataFixTypes` нельзя расширить без патчей (зависит от загрузчика; NeoForge передаёт `null`).
- Не держите состояние мира в статических полях: одиночная игра переиспользует JVM между мирами; очищайте кэши в `ServerLifecycleEvents.SERVER_STOPPED` или `ServerStoppedEvent` NeoForge.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Saved Data (NeoForge)](https://docs.neoforged.net/docs/datastorage/saveddata) | NeoForge docs, 2026 | Использование `SavedDataType` |
| [Saved Data (Fabric)](https://docs.fabricmc.net/develop/serialization/saved-data) | Fabric docs, 2026 | Пример для Fabric |
| [NBT (NeoForge)](https://docs.neoforged.net/docs/datastorage/nbt) | NeoForge docs, 2026 | Где сырой NBT ещё применяется |
| [Primer 26.1 (Splitting Level Data into Saved Data)](https://github.com/neoforged/.github/blob/main/primers/26.1/index.md) | ChampionAsh5357, 2026 | `SavedDataStorage`, хранилище сервера |

## 19. Перенос между версиями

**Ключевые выводы**

- **Первоисточники в порядке чтения:** порт-пример ванили для каждого шага (`primers/<ver>/index.md`, также отрисован на docs.neoforged.net/primer), релизный пост Fabric (удаления и переименования Fabric API) и страница порта в документации Fabric, релизный пост NeoForge. Переносите по одной версии Minecraft; каждый примерa опирается на предыдущий. Пост NeoForge 26.1 советует держать открытым старый рабочий проект, находить в нём ванильное место вызова сломанного метода и сравнивать с тем же местом в новой версии.
- **1.21.11 → 26.1 (самый большой скачок):** Java 25; игра без обфускации (Fabric: сначала на 1.21.11 перейдите с Yarn на имена Mojang через Loom 1.13+: `./gradlew migrateMappings --mappings "net.minecraft:mappings:1.21.11" --overrideInputsIHaveABackup`, `migrateClientMappings`, `migrateClassTweakerMappings`, затем `mappings loom.officialMojangMappings()`; потом смените плагин на `net.fabricmc.fabric-loom`, уберите конфигурации `mod*` и `remapJar`); Fabric API переименован под официальные имена (карта миграции для IntelliJ в документации Fabric), зависимость `fabric` → `fabric-api`; `ItemStackTemplate` и отложенные компоненты (нет ItemStack до загрузки мира); запись `RecipeSerializer`; торговля как данные; `GuiGraphics` → `GuiGraphicsExtractor` и `render*` → `extract*`; вывод слоя рендера (удалите вызовы `BlockRenderLayerMap` и `ItemBlockRenderTypes`); запись `ChunkPos` (`ChunkPos.containing(pos)`, `pack`/`unpack`); удалён `interactAt`; переработка мозгов и активностей; сохранённые данные по `Identifier`, `SavedDataStorage`, хранилище уровня сервера; «разворачивание» типов лута и переработка проверки; четырёхчастные версии NeoForge, Parchment необязателен.
- **26.1 → 26.2:** Vulkan (экспериментальный) и обобщение Blaze3D; рендеринг по фичам заменяет `MultiBufferSource` вне чанков; реорганизация `Gui`/`Hud` (`minecraft.gui.setScreen`); классы `*Ids` и провайдеры тегов, принимающие ключи (`valueLookupBuilder` удалён); `ChatFormatting` заменён в компонентах; «разворачивание» процессоров структур; Fabric API: тег `fabric:remove`, API взаимодействия жидкостей, «присутствующие» клиентские команды.
- **26.2 → 26.3:** перенос в пакеты Renderpearl, правила GLSL Shaderc, OIT; LWJGL с GLFW на SDL (коды клавиш, фокус ввода текста); перезагружаемые реестры датапака (рецепты, достижения, лут), провайдеры datagen с bootstrap; разделение провайдеров чисел; преобразователи блоков; варка как рецепты; компоненты `COOKING_FUEL`/`COMPOSTABLE`/`BREWING_FUEL`; удалены кодеки блоков; переписаны фичи, карверы, функции плотности и материальные правила; `CommandResponseTracker`; NeoForge удалил старые `IItemHandler`/`IFluidHandler`/`IEnergyStorage`.
- **С 1.21.1 (самая популярная база NeoForge, больше 16 000 модов в 2025):** добавьте по порядку 1.21.2 (`setId`, наборы holder, реестр рецептов, render state сущностей, расходники, ассеты экипировки), 1.21.4 (клиентские предметы), 1.21.5 (конвейеры рендера, переработка моделей, типизированные сохранённые данные, компоненты инструментов и брони, хуки удаления блок-сущностей), 1.21.6 (переписывание GUI, `ValueInput/ValueOutput`), 1.21.9 (отправка фич, экраны отладки, `isClientSide()`, переработка transfer в NeoForge), 1.21.11 (`Identifier`, права, переписывание рендеринга: сэмплеры и `RenderTypes`), затем шаги 26.x выше. Закладывайте это как переписывание рендеринга, инвентарей и данных, а не как смену версии.
- **Чему сегодня учат неверно старые туториалы:** `new ResourceLocation`, `CompoundTag` на ItemStack, `@OnlyIn`, `DistExecutor`, `SimpleChannel`, возможности `IItemHandler`/`LazyOptional`, `RenderSystem.setShader` и сырой GL, `MultiBufferSource` в BER, `BlockRenderLayerMap`, `HudRenderCallback`, `ItemGroupEvents`, `modImplementation`/`remapJar`, имена Yarn (`World`, `PlayerEntity`, `Identifier` в смысле класса Yarn), `playS2C`/`playC2S`, `FuelRegistry`, `TradeOfferHelper`, `@EventBusSubscriber(bus = MOD)`, поле `level.isClientSide`, константы клавиш GLFW.
- Поддержка нескольких версий: Stonecutter (препроцессор для многих версий MC в одном дереве исходников) или ветка на каждую версию MC с cherry-pick (Stonecutter `[не проверено]`); логику, не зависящую от загрузчика, держите в обычных Java-классах без импортов Minecraft, где возможно, чтобы ежегодные порты были дешевле.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [NeoForged primers (GitHub index)](https://github.com/neoforged/.github/tree/main/primers) | ChampionAsh5357 и соавторы | По одному примеру на версию MC начиная с 1.14 |
| [Primer 26.1](https://github.com/neoforged/.github/blob/main/primers/26.1/index.md) | ChampionAsh5357, 2026 | 1.21.11 → 26.1 |
| [Primer 26.2 (docs site)](https://docs.neoforged.net/primer/docs/26.2/) | ChampionAsh5357, 2026 | Отрисованная версия, на неё ссылается документация Fabric |
| [Primer 1.21.9](https://github.com/neoforged/.github/blob/main/primers/1.21.9/index.md) | ChampionAsh5357, 2025 | Отправка фич, экраны отладки |
| [Primer 1.21.6](https://github.com/neoforged/.github/blob/main/primers/1.21.6/index.md) | ChampionAsh5357, 2025 | GUI, ValueIO |
| [Porting (Fabric docs)](https://docs.fabricmc.net/develop/porting/) | Fabric docs, 2026 | Шаги порта на текущую версию |
| [Migrating mappings with Loom](https://docs.fabricmc.net/1.21.11/develop/porting/mappings/loom) | Fabric docs, 2025 | `migrateMappings` на имена Mojang |
| [Migrating mappings overview](https://docs.fabricmc.net/develop/porting/mappings/) | Fabric docs | Пути через Loom и Ravel |
| [Fabric for Minecraft 1.21.11](https://fabricmc.net/2025/12/05/12111.html) | FabricMC, 2025 | Заметки о последней обфусцированной версии |
| [Fabric for Minecraft 1.21.9 & 1.21.10](https://fabricmc.net/2025/09/23/1219.html) | FabricMC, 2025 | Изменения Fabric API тогда |
| [Fabric for Minecraft 1.21.6, 1.21.7 & 1.21.8](https://fabricmc.net/2025/06/15/1216.html) | FabricMC, 2025 | Эпоха переписывания GUI |
| [Fabric for Minecraft 1.21.5](https://fabricmc.net/2025/03/24/1215.html) | FabricMC, 2025 | Эпоха конвейеров рендера |
| [NeoForge for 26.1 Snapshots](https://neoforged.net/news/26.1snapshots/) | NeoForged, 2025 | Инструменты для снапшотов без обфускации |
| [Java Edition 26.1 (Minecraft Wiki)](https://minecraft.wiki/w/Java_Edition_26.1) | Minecraft Wiki | Изменения для игроков и технические |
| [Java Edition 26.2 (Minecraft Wiki)](https://minecraft.wiki/w/Java_Edition_26.2) | Minecraft Wiki | Список изменений |
| [Java Edition 26.3 (Minecraft Wiki)](https://minecraft.wiki/w/Java_Edition_26.3) | Minecraft Wiki | Список изменений |
| [Minecraft Java Edition 26.2 (official)](https://www.minecraft.net/en-us/article/minecraft-java-edition-26-2) | Mojang, 2026 | Официальный журнал изменений |
| [Stonecutter](https://stonecutter.kikugie.dev/) | KikuGie | Препроцессор исходников для нескольких версий |

## 20. Карта библиотек и экосистемы

Наличие сборок под 26.x по Modrinth API на 2026-11-07. Перед рекомендацией перепроверяйте через `GET /v2/project/{slug}` и поле `game_versions` ([Modrinth API docs](https://docs.modrinth.com/api/)).

| Нужно | Fabric | NeoForge | Мультилоадер или примечание |
|---|---|---|---|
| Базовый API | Fabric API 0.165.x | встроен в NeoForge | — |
| Kotlin | [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin) | [Kotlin for Forge](https://modrinth.com/mod/kotlin-for-forge) | у обоих есть сборки 26.1–26.3 |
| Конфигурация | Cloth Config, YACL, Fzzy Config, MidnightLib, owo-lib (26.1–26.2) | `ModConfigSpec` встроен | [Forge Config API Port](https://modrinth.com/mod/forge-config-api-port) |
| Компоненты и вложения | вложения Fabric, [Cardinal Components API](https://modrinth.com/mod/cardinal-components-api) | вложения NeoForge | — |
| Энергия | [TechReborn Energy](https://github.com/TechReborn/Energy) 5.x | `EnergyHandler` | оборачивать по загрузчику |
| Слоты аксессуаров | [Curios API](https://modrinth.com/mod/curios) | Curios API | у Trinkets и Accessories нет сборок под 26.x |
| Анимация | [GeckoLib](https://modrinth.com/mod/geckolib) | GeckoLib | 26.1, 26.1.2, 26.2, 26.3 |
| Биомы | [TerraBlender](https://modrinth.com/mod/terrablender), [Biolith](https://modrinth.com/mod/biolith), [Lithostitched](https://modrinth.com/mod/lithostitched) | то же | все мультилоадер |
| Абстракция | [Architectury API](https://modrinth.com/mod/architectury-api), [Balm](https://modrinth.com/mod/balm), [Puzzles Lib](https://modrinth.com/mod/puzzles-lib), [Resourceful Lib](https://modrinth.com/mod/resourceful-lib) | то же | тонкие слои портируются быстрее |
| Книги-справочники | [Modonomicon](https://modrinth.com/mod/modonomicon) (26.1–26.3), [Patchouli](https://modrinth.com/mod/patchouli) (только 26.1.x) | то же | — |
| Производительность рендера | [Sodium](https://modrinth.com/mod/sodium) 0.9.3-beta, [Iris](https://modrinth.com/mod/iris) 1.11.x | оба | тестируйте свой рендеринг с ними |
| Хаб экрана конфигурации | [Mod Menu](https://modrinth.com/mod/modmenu) | встроенный экран модов | — |
| Просмотр рецептов | [JEI](https://modrinth.com/mod/jei), [REI](https://modrinth.com/mod/rei) (26.1–26.3) | то же | [EMI](https://modrinth.com/mod/emi): сборок 26.x на 2026-11-07 не найдено |

Инструменты для проверки качества мода (тесты, отладка, запуск собранного jar):

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Automatic Testing (Fabric)](https://docs.fabricmc.net/develop/automatic-testing) | Fabric docs, 2026 | Юнит-тесты, gametests, клиентские gametests |
| [Game Tests (NeoForge)](https://docs.neoforged.net/docs/misc/gametest) | NeoForge docs, 2026 | Фреймворк `@GameTest` |
| [Debugging (Fabric)](https://docs.fabricmc.net/develop/debugging) | Fabric docs, 2026 | Отладчик, hotswap, логи |
| [Debug profiler (NeoForge)](https://docs.neoforged.net/docs/misc/debugprofiler) | NeoForge docs, 2026 | Поиск медленных тиков |
| [Loom documentation](https://docs.fabricmc.net/develop/loom/) | Fabric docs, 2026 | Задачи, включая `validateAccessWidener`, конфигурации запуска |
| [Loom tasks](https://docs.fabricmc.net/develop/loom/tasks) | Fabric docs, 2026 | `runDatagen`, `runServer`, задачи production run |
| [Production run tasks (Loom)](https://docs.fabricmc.net/develop/loom/production-run-tasks) | Fabric docs, 2026 | Проверка собранного jar в CI |
| [ModDevGradle](https://github.com/neoforged/ModDevGradle) | NeoForged, 2026 | Конфигурации запуска, gametest, запуск данных |

## Антипаттерны

Сначала хорошие приёмы, затем то, чего делать не нужно. Примеры подходят для модов любого вида.

**Что делать**

- **Данные прежде кода:** теги, data maps, компоненты, реестры датапака и datagen вместо жёстко прошитых списков Java: сборки могут перенастраивать значения без перекомпиляции. Работает для любого вида мода: частота руд (генерация мира), рецепты станков (технический), определения заклинаний (магический, свой реестр датапака), лут (приключенческий).
- **Авторитет сервера:** игровая логика на логическом сервере; клиент предсказывает только косметику. Каждое действие клиента — запрос, который сервер проверяет.
- **Малые неизменяемые значения:** записи для компонентов, вложений, пакетов и render state; копирование при записи.
- **Регистрация через механизм загрузчика, ссылки через holder и ключи:** `DeferredHolder`/`ResourceKey`/`Holder` вместо раннего разрешения объектов; никогда не сравнивайте по отображаемому имени или числовому id.
- **Кэш поисков:** `BlockCapabilityCache`/`BlockApiCache`, ссылки на соседние блок-сущности с инвалидацией по `onNeighborChange`, заранее созданные `TagKey` и константы `Identifier`.
- **Дешёвые тики:** ранний возврат при простое, «сон» машин без входа (пробуждение по смене соседа), разнесение работы (`(gameTime + pos.hashCode()) % 20`), тяжёлые вычисления в фоновых потоках с возвратом результата через `server.execute`.
- **Разделение клиента:** клиентский код в клиентском source set или классах; клиентские регистрации в клиентской точке входа или подписчиках `Dist.CLIENT`.
- **Пространства имён везде:** id, ключи перевода (`item.<modid>.x`), члены миксинов (`modid$`), ключи NBT и кодеков, имена файлов конфигурации, имена расширений enum.
- **Открытый API:** события, колбэки или небольшой пакет API (и compile-only jar API без JiJ), чтобы аддоны и интеграции (просмотрщики рецептов JEI, REI, EMI, подсказки Jade, WTHIT) не требовали миксинов в ваш мод.

**Чего не делать**

- Статическое изменяемое игровое состояние, общее для логических сторон; статические ссылки на `Level`, `Player`, `Minecraft` (утечки памяти между мирами).
- Ссылки на `net.minecraft.client.*` из общих классов; проверки `instanceof LocalPlayer` в общем коде.
- Тяжёлая работа на серверном потоке каждый тик: `getEntitiesOfClass` с большими боксами, полные обходы инвентарей, поиск пути каждый тик, `BlockPos.betweenClosed` по большим объёмам, сборка строк и логирование в горячих путях, исключения как поток управления.
- Синхронная загрузка чанков из тикающего кода (`level.getBlockState` по незагруженным позициям загружает чанки): проверяйте `level.isLoaded(pos)` или `hasChunkAt`.
- Игнорирование компонентов данных: `CUSTOM_DATA` для типизированных данных, изменение значений компонентов на месте, создание ItemStack до привязки реестров (26.1+).
- Жёсткие числовые id, сырые индексы реестров, коды клавиш GLFW, цвета `0xRRGGBB` без альфа-канала (текст с 1.21.6 требует ARGB), номера форматов паков, пути к ванильным JSON, которые меняются между версиями.
- Непроверенные serverbound-пакеты; неограниченные коллекции и строки в `StreamCodec`; отправка `ItemStack` от клиента.
- `@Overwrite` и `@Redirect` там, где хватит `@Inject` и MixinExtras; игровая логика внутри миксинов; миксины в чужие моды без `@Pseudo` и условных конфигов.
- Регистрация контента по условию из конфигурации (рассинхронизация и порча миров при переключении); чтение конфигурации до загрузки.
- `Random` или `Math.random()` в генерации мира (недетерминированность) либо создание нового `RandomSource` на каждый вызов.
- `catch (Exception e) {}` вокруг логики мода: скрывает реальные сбои и порчу состояния; логируйте с контекстом и пробрасывайте либо отказывайте операции.
- Раздутые fat jar: делайте JiJ только того, что нужно; не вкладывайте MixinExtras на Fabric и NeoForge (уже есть) и модули Fabric API.

## Чеклист ревью кода мода

Fabric и NeoForge, 26.x. Пункты, помеченные `[CI]`, нужно закрепить автоматической проверкой (см. [minecraft-performance-testing.md](minecraft-performance-testing.md) про GameTest и [testing.md](testing.md)).

Стороны и потоки

- [ ] Нет ссылок на `net.minecraft.client.*` (или клиентский API мода), достижимых из общего кода; клиентский код живёт в клиентском source set или классах `Dist.CLIENT`; выделенный сервер запускается в CI. `[CI]`
- [ ] Игровая логика закрыта условием `!level.isClientSide()`; на клиенте нет изменения мира, кроме косметики.
- [ ] Нет статического изменяемого состояния, общего для логических сторон; кэши мира очищаются при остановке сервера.
- [ ] Доступ к миру только на главном потоке; обработчики пакетов, трогающие мир, выполняются на нём или ставят работу в очередь; код `decodePredicate` Fabric и `HandlerThread.NETWORK` NeoForge не касается состояния мира.
- [ ] Параллельная работа жизненного цикла (события setup NeoForge) обёрнута в `enqueueWork`.

Регистрация и данные

- [ ] Все регистрации идут через `DeferredRegister` или `Registry.register` в нужный момент; id строчные и с пространством имён; `setId` задан (или использован помощник).
- [ ] Нет создания `ItemStack` до привязки реестров и компонентов (26.1+); для констант используются шаблоны.
- [ ] Компоненты данных — неизменяемые записи с `equals`/`hashCode`, заданы и `persistent`, и `networkSynchronized`, нет `CUSTOM_DATA` для типизированных данных, значения заменяются, а не изменяются.
- [ ] Настраиваемые значения — данные (теги, data maps, реестры датапака, компоненты) с datagen; сгенерированные файлы синхронны (`runDatagen` или `runClientData` и `git diff --exit-code` в CI). `[CI]`
- [ ] Для материалов между модами используются конвенционные теги `c:`; нет жёстких id предметов других модов без обработки необязательной зависимости.
- [ ] Кодеки проверяют диапазоны (`intRange`, `POSITIVE_INT`), используют `optionalFieldOf` со значениями по умолчанию для прямой совместимости, ошибки логируются через `resultOrPartial`.

Сеть и безопасность

- [ ] У каждого пакета ограниченный `StreamCodec` (максимумы коллекций, `stringUtf8(max)`), нет `fromCodec` для serverbound-данных.
- [ ] Каждый serverbound-обработчик проверяет состояние игрока, расстояние, загруженность чанка, id открытого меню, границы индексов, права и частоту; отправляется намерение, а не результат.
- [ ] Clientbound-обработчики терпят отсутствие сущностей, чанков и записей реестра.
- [ ] Пакеты зарегистрированы на обеих сторонах (`PayloadTypeRegistry` Fabric в общей инициализации); задана сетевая версия (`registrar("x")` NeoForge); необязательные каналы помечены для совместимости с серверной или клиентской стороной.
- [ ] Частота синхронизации соразмерна: нет пересылки полного тега блок-сущности каждый тик; вложения и данные сущностей — только для небольшого состояния.

Геймплей и производительность

- [ ] Тикер блок-сущности есть только там, где нужен (только серверный, если нет клиентской анимации), ранний выход при простое, нет аллокаций в горячих путях каждый тик.
- [ ] Нет синхронной загрузки чанков из тиков; перед доступом к соседям через границу чанка проверяется `isLoaded`.
- [ ] Поиски возможностей и API кэшируются (`BlockCapabilityCache`, кэши API) и корректно инвалидируются (`invalidateCapabilities`).
- [ ] Инвентари используют транзакции правильно (commit только при успехе; журналы делают снимок до изменения; `setChanged` при коммите корня и смене содержимого).
- [ ] После каждого постоянного изменения вызывается `setChanged()`, `setDirty()` или `setUnsaved(true)`.
- [ ] Меню реализуют `stillValid` и `quickMoveStack`; нет эксплойтов дублирования при shift-клике и при разрушении блока, пока меню открыто.
- [ ] Цели и сенсоры ИИ дёшевы в `canUse`; нет неограниченных поисков сущностей каждый тик.
- [ ] Код генерации мира детерминирован и ограничен `WorldGenLevel`; нет доступа к блок-сущностям соседних чанков.

Миксины и преобразования

- [ ] Миксины минимальны: предпочтительны `@Inject` и MixinExtras; нет `@Overwrite`; `@Redirect` обоснован комментарием; `require` или `defaultRequire` заданы, чтобы пропущенные цели падали в разработке.
- [ ] Члены, добавленные миксином, имеют префикс `modid$`; клиентские миксины в клиентском массиве конфига; миксины в необязательные моды защищены (`@Pseudo` или условные конфиги).
- [ ] В классах миксинов нет логики, кроме делегирования; вместо рефлексии используются интерфейсы аксессоров.
- [ ] Class tweakers проверены (`validateAccessWidener` в CI); AT минимальны; нет записей `transitive-`, если API не требует. `[CI]`

Клиент и рендеринг

- [ ] Нет прямых вызовов OpenGL и GLFW; рендеринг идёт через API ваниля и загрузчика (`RenderPipeline`, коллекторы submit, render state); рендереры читают только render state.
- [ ] HUD и экраны используют текущий API (`HudElementRegistry` или `RegisterGuiLayersEvent`, `GuiGraphicsExtractor`, `extractRenderState`); цвета текста содержат альфа-канал; текстовые поля вызывают `onTextInputFocusChange` (26.3).
- [ ] Клавиши зарегистрированы с зарегистрированной `KeyMapping.Category`; коды клавиш берутся из `InputConstants`.
- [ ] Если мод рисует что-то своё, он проверен с Sodium и Iris (и популярным шейдерпаком). `[CI]` или вручную.

Конфигурация, совместимость и выпуск

- [ ] Регистрация контента не зависит от конфигурации; значения конфигурации читаются только после загрузки; серверно-авторитетным значениям из клиентской конфигурации не доверяют.
- [ ] Зависимости объявлены с верными диапазонами (`fabric-api`, `minecraft ~26.3`, четырёхчастные версии NeoForge); необязательные интеграции подключаются через проверки мягких зависимостей (`FabricLoader.isModLoaded`, `ModList.get().isLoaded`) в отдельных классах.
- [ ] Ключи перевода есть для каждого предмета, блока, сущности, записи конфигурации и клавиши; в UI нет жёстко прошитого английского.
- [ ] Нет проглатывания исключений через catch-all; ошибки логируются с id мода и контекстом; нет `printStackTrace`.
- [ ] Игровые тесты (gametest и клиентские gametest Fabric, `@GameTest` NeoForge) покрывают основные механики и работают в CI на выделенном сервере. `[CI]`
- [ ] Журнал изменений указывает версию MC и загрузчика и любые изменения формата данных мира; у сохранённых данных и кодеков есть путь миграции.

## Спорные места и непроверенное

- В документации NeoForge нет версий 26.2 и 26.3. Изменения API NeoForge в 26.2 и 26.3 (кроме исчезновения старых интерфейсов transfer из исходников 26.3.x) выведены из деревьев исходников и порт-примеров, а не из релизных постов: на первой странице новостей neoforged.net поста NeoForge для 26.2 и 26.3 не нашлось. Нужна компиляция шаблонов против 26.3.0.x в CI.
- Сигнатуры API событий лута Fabric после того, как 26.3 сделал таблицы лута перезагружаемыми реестрами, не прочитаны (страница документации относится к 26.2). То же для `FabricDynamicRegistryProvider` и провайдеров достижений после изменения datagen-bootstrap в 26.3.
- Точная требуемая версия Loom для class tweaker `v2` и расширения enum: документация Fabric говорит о Loom 1.16+, пост 26.2 — о Loom 1.17; текущий Loom 1.19.4, так что для новых проектов вопрос снят.
- Значение `compatibilityLevel` в `mixin.json` для Java 25 (`JAVA_25`) предположено по соглашениям Mixin; проверьте на sponge-mixin 0.17.5 в CI.
- Поведение Iris с собственными `RenderPipeline` и изменениями Renderpearl и Shaderc в 26.3 не описано в прочитанных первоисточниках; проверено только наличие релизов.
- Классы `FabricRegistryBuilder`, класс реестра частиц Fabric, класс `Tags` NeoForge, детали `RegisterMenuScreensEvent`: существование классов подтверждено по деревьям исходников, использование не читалось.
- Разница единиц жидкости (капли Fabric, 81000 на ведро, против 1000 мВ NeoForge) указана по давнему дизайну API, заново не перечитывалась.
- Имена параметров колбэка регистрации команд и сигнатура `source.sendSuccess` для 26.3 не перечитывались.
- Для EMI и owo-lib на 2026-11-07 нет сборки под 26.3 на Modrinth; для Trinkets и Accessories нет сборок под 26.x. Перепроверьте перед рекомендацией.
- Уберёт ли Minecraft 26.4 (снапшоты 1–7) OpenGL полностью, неизвестно: пост Fabric 26.2 говорит, что OpenGL будет убран «когда Vulkan станет стабильным», без даты.
