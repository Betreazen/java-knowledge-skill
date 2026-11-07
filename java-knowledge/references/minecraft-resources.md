# Моды Minecraft: документация, курсы, книги и эталонные кодовые базы

Каталог источников для тех, кто пишет, ревьюит и портирует моды и плагины Minecraft Java Edition: официальная документация и её свежесть, туториалы и курсы, шаблоны и тестовые моды, кодовые базы для чтения (по жанрам модов), сообщества, платформы публикации, статистика экосистемы, книги, русскоязычные ресурсы и маршрут обучения. Файл отвечает на вопросы «где это написано», «насколько свежий источник» и «чей код стоит читать». Сами API игры, сборка и публикация разобраны в соседних файлах.

Актуально на ноябрь 2026. Minecraft Java 26.3 (выпуск 2026-09-15), последний снапшот 26.4-snapshot-7. Версии 26.x работают без обфускации на Java 25. Последний выпуск 1.x — 1.21.11. Версия 1.21.1 остаётся «долгой» целью NeoForge для крупных контентных модов. Fabric Loader 0.20.1, Fabric API 0.165.2+26.3, Fabric Loom 1.19.4, NeoForge 26.3.0.101, ModDevGradle 2.0.148, Paper 26.3 build 244. Статистика и числа в таблицах получены 2026-11-07 из API Modrinth и GitHub, из метаданных Maven и из манифеста Mojang. JDK 25 — текущая LTS, JDK 27 — последний выпуск.

Метки: `[не проверено]` — факт из одного вторичного источника или страница недоступна для чтения; `[устарело]` — источник или API устарел, рядом названа замена; `[CI]` — проверено в CI на GitHub Actions. Строки без метки: страница прочитана или ответила HTTP 200, метаданные получены по API. Соседние файлы: платформы, загрузчики, сборка и публикация — [minecraft-modding.md](minecraft-modding.md); архитектура и API игры — [minecraft-mod-api.md](minecraft-mod-api.md); измерение и профилирование — [minecraft-performance-testing.md](minecraft-performance-testing.md); Paper, Folia, Velocity, датапаки — [minecraft-server-plugins.md](minecraft-server-plugins.md); общие книги по Java — [books.md](books.md); инструменты Java — [toolchain.md](toolchain.md).

- [Короткий список: с чего начать](#короткий-список-с-чего-начать)
- [Версии на ноябрь 2026](#версии-на-ноябрь-2026)
- [Документация и её свежесть](#документация-и-её-свежесть)
- [Как читать старые туториалы](#как-читать-старые-туториалы)
- [1. Официальная документация и порталы](#1-официальная-документация-и-порталы)
- [2. Туториалы и курсы](#2-туториалы-и-курсы)
- [3. Шаблоны, тестовые моды и инструменты сборки](#3-шаблоны-тестовые-моды-и-инструменты-сборки)
- [4. Сообщества, платформы и публикация](#4-сообщества-платформы-и-публикация)
- [5. Статистика экосистемы](#5-статистика-экосистемы)
- [Эталонные кодовые базы](#эталонные-кодовые-базы)
- [Книги](#книги)
- [Русскоязычные ресурсы](#русскоязычные-ресурсы)
- [Маршрут обучения](#маршрут-обучения)
- [Что не проверено](#что-не-проверено)

## Короткий список: с чего начать

**Ключевые выводы**

- Источники расположены по порядку полезности для автора мода на 26.x. Начинайте сверху и спускайтесь вниз, пока не найдёте ответ.
- На вопрос «что изменилось в ванильном коде между версиями» отвечают праймеры NeoForge. Они подходят для любого загрузчика.
- Официальные тестовые моды (`src/testmod` в Fabric API, `tests/` в NeoForge) показывают API на примерах и заменяют недостающую документацию.
- Ванильный код без рабочего проекта читайте на mcsrc.dev.

| № | Ресурс | Автор, год | Зачем читать |
|---|---|---|---|
| 1 | [Fabric Documentation — Developer Guides](https://docs.fabricmc.net/develop/) | FabricMC, 2026 (по умолчанию 26.2) | Канонический гайд по Fabric с выбором версии; почти бесплатный текстовый курс |
| 2 | [NeoForge Documentation](https://docs.neoforged.net/docs/gettingstarted/) | NeoForged, 2026 (по умолчанию 26.1) | Канонический справочник NeoForge, версионный |
| 3 | [NeoForge porting primers index](https://docs.neoforged.net/primer/docs/) | ChampionAsh5357 и др., 2019–2026 | Что менялось в ваниле на каждом шаге, от 1.12 до 26.3 |
| 4 | [Fabric blog — post list](https://fabricmc.net/blog/) | FabricMC | Записи «Fabric for Minecraft X» служат журналом изменений и руководством по портированию |
| 5 | [Fabric-Tutorial-26.X](https://github.com/Tutorials-By-Kaupenjoe/Fabric-Tutorial-26.X) | Kaupenjoe, 2026 (MIT) | Самый полный видеокурс, код по ветке на эпизод, Fabric 26.3 |
| 6 | [NeoForge-Tutorial-26.X](https://github.com/Tutorials-By-Kaupenjoe/NeoForge-Tutorial-26.X) | Kaupenjoe, 2026 (MIT) | То же для NeoForge 26.3 |
| 7 | [mcsrc.dev — online Minecraft source viewer](https://mcsrc.dev/) | FabricMC, 2025–2026 (MIT) | Декомпилированный ванильный код в браузере |
| 8 | [Fabric API repository](https://github.com/FabricMC/fabric-api) | FabricMC, Apache-2.0, ветка 26.3 | Модули и `testmod` с примерами API |
| 9 | [NeoForge tests (26.3.x)](https://github.com/neoforged/NeoForge/tree/26.3.x/tests) | NeoForged | Сотни маленьких тестовых модов по возможностям |
| 10 | [NeoForge Mod Generator](https://neoforged.net/mod-generator/) / [Fabric template mod generator](https://fabricmc.net/develop/template/) | NeoForged; FabricMC, 2026 | Готовый проект под нужную версию вместо клонирования старых примеров |
| 11 | [Minecraft Wiki — Data component format](https://minecraft.wiki/w/Data_component_format) / [Minecraft Wiki — Java Edition protocol](https://minecraft.wiki/w/Java_Edition_protocol) | Сообщество, 2026 | Авторитет по форматам данных и пакетам |
| 12 | [Mixin wiki](https://github.com/SpongePowered/Mixin/wiki) / [MixinExtras wiki](https://github.com/LlamaLad7/MixinExtras/wiki) | SpongePowered; LlamaLad7 | Основы миксинов для всех загрузчиков |
| 13 | [PaperMC Documentation](https://docs.papermc.io/) | PaperMC | Paper, Velocity, Folia, Adventure; для плагинов |
| 14 | [Farmer's Delight](https://github.com/vectorwing/FarmersDelight) | vectorwing, MIT | Компактная контентная кодовая база для первого чтения |
| 15 | [Lithium](https://github.com/CaffeineMC/lithium) | CaffeineMC, LGPL-3.0, 26.3 | Образец организации миксинов и их конфигурации |

## Версии на ноябрь 2026

**Ключевые выводы**

- Всё, что написано до 2026-03 для Fabric, использует имена Yarn (`ItemGroup`, `World`, `PlayerEntity`) и `modImplementation`/`remapJar`. Всё, что написано до 2026-03 для NeoForge и Forge, использует Java 21, Parchment и `GuiGraphics`.
- Оба расхождения переводятся механически, но агент должен переводить имена сам (см. [Как читать старые туториалы](#как-читать-старые-туториалы)).
- Номера версий взяты из манифестов, метаданных Maven и API 2026-11-07.

| Что | Версия и дата | Примечание |
|---|---|---|
| Последний релиз Minecraft | **26.3** «Wilderness Bound», 2026-09-15 | Манифест Mojang `latest.release=26.3`; протокол 777, формат датапака 121.0, формат ресурспака 97.1 (minecraft.wiki) |
| Последний снапшот | 26.4-snapshot-7, 2026-11-03 | Названия снапшотов теперь `26.4-snapshot-N`, `-pre-N`, `-rc-N`. Старый стиль `26w14a` встретился один раз, 2026-04-01 |
| Линия выпусков 2026 | 26.1 (2026-03-24), 26.1.1 (04-01), 26.1.2 (04-09), 26.2 (06-16), 26.3 (09-15) | Схема «год.дроп.хотфикс» начата с 26.1 |
| Последний 1.x | 1.21.11 «Mounts of Mayhem», 2025-12-09 | Последняя обфусцированная версия. 1.21.1 (2024-08) остаётся «долгой» целью NeoForge |
| Обфускация | Снята с 26.1 | Имена Mojang во время выполнения, без intermediary; Yarn заморожен на 1.21.11 |
| Java | Java 25 обязательна с 26.1 (в комплекте Microsoft OpenJDK 25) | 1.20.5–1.21.11 = Java 21; 1.18–1.20.4 = Java 17 |
| Fabric Loader | 0.20.1 (мета `stable:true`) | В блоге о 26.3 указана 0.19.5 |
| Fabric Loom | 1.19.4 (Maven `release`), 1.19-SNAPSHOT | Для 26.1 нужен Loom 1.15 с id плагина `net.fabricmc.fabric-loom` (без remap); посты о 26.2 и 26.3 называют Loom 1.17 |
| Fabric API | 0.165.2+26.3 / 0.165.4+26.4 (снапшоты) | Id мода `fabric-api`; id `fabric` убран в 26.1 |
| NeoForge | 26.3.0.101, 26.2.0.93, 26.1.2.126, 21.1.265 (1.21.1) | Четырёхчастная схема с 26.1: `<mc major>.<minor>.<patch>.<build>[-beta]` |
| ModDevGradle | 2.0.148 | Генератор модов всё ещё предлагает NeoGradle 7.x |
| MinecraftForge | 26.3 → 66.1.5 latest / 66.1.0 recommended; 26.2 → 65.1.x; 26.1.2 → 64.1.x | Forge жив и выпускает сборки для 26.3 |
| Paper | 26.3 build 244 (STABLE, 2026-11-06) | Версия артефакта API теперь `26.3.build.244-stable`, а не `1.21.x-R0.1-SNAPSHOT` |
| Жёсткий форк Paper | Объявлен 2024-12-13 (действует с 1.21.4; papermc.io/news) | Компилируйте против `paper-api`, не против Spigot |
| Quilt | Загрузчик активен (push 2026-11-04); QSL, QKL и Quilted Fabric API закрыты для 26.1 (блог 2026-02-03) | Репозиторий Quilt Standard Libraries: последний push 2026-01, ветка по умолчанию 1.21.5 `[не проверено]` |
| Yarn | Заморожен; ветка `1.21.11`, последний push 2026-05-27 | Fabric рекомендует имена Mojang |
| Linkie | Работает; пространство `mojang_raw` покрывает 26.1.2/26.2/26.3; Yarn до 1.21.11 | Нужен для перевода имён 1.21.x и старше |
| Fabric Docs | По умолчанию 26.2; селектор 26.1.2, 1.21.11, 1.21.1, 1.20.4; есть русский `ru_ru` | Отстаёт от последнего релиза на один дроп |
| NeoForge Docs | По умолчанию 26.1; селектор назад до 1.20.3 | Праймеры покрывают все шаги до 26.2 → 26.3 |
| Forge Docs | Селектор 1.21.x, 1.20.x, 1.20.1, 1.19.x; документации 26.x нет | Ветка по умолчанию 1.21.x (push 2026-10-23) |

## Документация и её свежесть

**Ключевые выводы**

- Перед использованием страницы определите версию, на которую она нацелена: заголовок страницы, селектор версий или дата последней правки.
- Документация Fabric и NeoForge отстаёт от релиза на один-два дропа. Для последних изменений читайте блог Fabric и праймеры NeoForge.
- Для новых проектов устарели документация Forge, вики Forge Community, Yarn, Parchment (для 26.x) и QSL Quilt.

| Источник | До какой версии актуален | Что устарело |
|---|---|---|
| Fabric Docs | 26.2 по умолчанию; архивы 26.1.2, 1.21.11, 1.21.1, 1.20.4 | 26.3: удалённые реестры топлива и компоста, перестройка worldgen, ввод через SDL; примеры датагена для worldgen 26.3 не проверены |
| Fabric Wiki | В основном ≤1.21.11 (Yarn) | Сама вики пишет «Some pages may be out-of-date» и отсылает к Fabric Docs; годится для концепций (миксины) |
| Блог Fabric «Fabric for Minecraft X» | 26.3 (2026-09-15) | Не устарел; это журнал изменений по дропам |
| NeoForge Docs | 26.1 по умолчанию, архивы до 1.20.3 | Примеры 1.21.x используют `ResourceLocation`, Parchment и Java 21 |
| Праймеры NeoForge | До 26.2 → 26.3 | Не устарели |
| Forge Docs | 1.21.x (нет страниц для 26.x), хотя Forge выпускает 26.3 | Читать как справочник эпохи 1.20.1/1.21 |
| Forge Community Wiki (forge.gemwire.uk) | 1.19 / LTS 1.18.2, правка 2023-03-28 | `[устарело]`, замена: документация NeoForge |
| Paper Docs | 26.x | Старые примеры `1.21.x-R0.1-SNAPSHOT`, `ChatColor` и строковый чат |
| Вики и Javadocs Spigot | Базовые понятия (слушатели, `plugin.yml`, `BukkitRunnable`) | Новые API после форка (компоненты, компоненты данных, диалоги, Brigadier) есть только в Paper |
| Minecraft Wiki | 26.3 | Не устарела; техстраницы английской вики полнее русской |
| mcsrc.dev, Linkie | 26.x (mcsrc); Yarn до 1.21.11, `mojang_raw` для 26.x (Linkie) | Блок зависимостей Fabric в Linkie заканчивается на 1.21.11 |
| Parchment | До 1.21.x (ветка `versions/1.21.x`, push 2025-12-20) | Для 26.x не нужен: официальные имена параметров поставляются с игрой |
| Yarn | До 1.21.11 | Заморожен; замена: имена Mojang |
| Kaupenjoe | 26.x (репозитории и ветки обновляются) | Видео записаны на версии момента записи; ветка может отличаться от видео |
| moddingtutorials.org, Cy4, rentry MCModdingGuide | Forge 1.16.5–1.19.3; 2023 | `[устарело]`; сайт moddingtutorials.org недоступен с проверяющей машины |
| McJty | NeoForge 1.21.1, отдельный раздел 26.2 `[не проверено]` | Старые материалы: NeoForge 1.20.4 и Forge 1.12–1.20 |
| Статья Habr про миксины | Fabric 1.20.1, JDK 21, Yarn | Скрипт сборки устарел; понятия миксинов верны |
| Quilt | Загрузчик жив | QSL, QKL, Quilted Fabric API закрыты в 26.1; туториалы с API Quilt ведут в тупик |

## Как читать старые туториалы

**Ключевые выводы**

- Сначала классифицируйте туториал по четырём признакам. (1) Файл сборки: `modImplementation`, `remapJar`, `mappings "net.fabricmc:yarn:…"` — Fabric ≤1.21.11; `id 'net.neoforged.moddev'` с `parchment` — NeoForge 1.21.x; `net.minecraftforge.gradle` с `mappings channel: 'official'` — Forge. (2) Версия Java: 17/21 или 25. (3) Имена классов: Yarn или Mojang. (4) NBT предметов (`getOrCreateNbt`, `stack.getTag()`) или компоненты данных. NBT предметов означает версию до 1.20.5 и требует переделки, а не переименования.
- На Fabric 26.x плагин `net.fabricmc.fabric-loom` не делает remap. Замените `modImplementation` и `modCompileOnly` на `implementation` и `compileOnly`, `remapJar` на `jar`. Строки `mappings` уберите.
- Id мода Fabric API теперь `fabric-api`, не `fabric`.
- Классы с именами Mojang на 26.x не переименовывались, кроме отмеченных в таблице.
- `ResourceLocation` существует до 1.21.10. С 1.21.11 класс называется `Identifier` и лежит в `net.minecraft.resources`.
- Для миграции имён есть задача Loom `migrateMappings` (только Java, Kotlin не поддерживается) и плагин IntelliJ **Ravel** (поддерживает Kotlin; им пользовался сам Fabric API). Обоим нужны ручные правки. Строки целей миксинов (`method = "…"`) инструменты не переименовывают.

| Yarn (старые туториалы Fabric) | Mojang / Mojmap (NeoForge, Forge, Fabric 26.x) |
|---|---|
| `PlayerEntity`, `ServerPlayerEntity` | `Player`, `ServerPlayer` |
| `MinecraftClient` | `Minecraft` |
| `World`, `ServerWorld` | `Level`, `ServerLevel` |
| `ItemGroup` (Fabric API `ItemGroupEvents`) | `CreativeModeTab` (Fabric API `CreativeModeTabEvents`, с 26.1) |
| `Registries.ITEM` | `BuiltInRegistries.ITEM` |
| `Identifier` (`net.minecraft.util`) | `ResourceLocation` до 1.21.10, затем `Identifier` (`net.minecraft.resources`) с 1.21.11 |
| `Text`, `Formatting` | `Component`, `ChatFormatting` |
| `DrawContext` | `GuiGraphics` (≤1.21.11), затем `GuiGraphicsExtractor` (26.1, по посту NeoForge) |
| `MatrixStack` | `PoseStack` (`com.mojang.blaze3d.vertex`) |
| `NbtCompound` | `CompoundTag` |
| `ScreenHandler` | `AbstractContainerMenu` |
| `ActionResult`, `Hand` | `InteractionResult`, `InteractionHand` |
| `Vec3d`, `Box` | `Vec3`, `AABB` |
| `StatusEffect` | `MobEffect` |
| `ItemUsageContext` | `UseOnContext` |
| `BlockRenderLayer` (1.21.6+) | `ChunkSectionLayer` |

- **Старые туториалы NeoForge и Forge (≤1.21.11)** уже в именах Mojang. Портируйте их по праймерам: для туториала 1.21.1 прочитайте праймеры 1.21.2 … 26.3 по порядку или найдите ту же функцию в репозитории 26.X автора. Имена только для Forge (`ForgeRegistries`, `@Mod.EventBusSubscriber`, `IForgeItem`) соответствуют NeoForge (`DeferredRegister`, `@EventBusSubscriber`, `IItemExtension`); проверяйте каждое имя в документации NeoForge `[не проверено]`. Полезный приём: держите рядом рабочее пространство 1.21.11 и 26.1 и сравнивайте использование ванильных классов.
- **Туториалы Bukkit и Spigot для Paper 26.x.** Оставьте слушатели и основы `plugin.yml`. Замените `ChatColor` и строки на Adventure `Component`. Замените `CommandExecutor` и команды из `plugin.yml` на Command API на базе Brigadier, зарегистрированный через событие lifecycle. Зависьте от `paper-api`. Формат версии `paper-api` изменился: `26.3.build.244-stable` вместо `1.21.x-R0.1-SNAPSHOT`. Точная строка Gradle проверяется в CI.
- **Туториалы по рендерингу стареют быстрее всего.** 1.21.6+ перенёс рисование GUI на render state. 1.21.11 перенёс типы из `RenderType` в `RenderTypes`. 26.1 переименовал `Screen#render` в `extractRenderState`. 26.2 добавил экспериментальный бэкенд Vulkan, на котором ломаются сырые вызовы GL. Не копируйте код рендеринга через границу больше одного дропа без праймера.
- **Туториалы по worldgen.** 26.3 объединил `Feature` с `FeatureConfiguration`, перенёс configured features в `worldgen/feature` и превратил surface rules в «material rules». Каждый JSON-туториал по worldgen до 26.3 устарел для 26.3.
- **Данные вместо реестров в 26.3.** Топливо, шанс компоста и поведение «стругать, вспахивать, выравнивать» теперь компоненты предмета (`minecraft:cooking_fuel`, `minecraft:compostable`, `minecraft:block_transformer`, `minecraft:brewing_fuel`). Туториалы с `FuelRegistry`, `CompostingChanceRegistry`, `StrippableBlockRegistry` (Fabric) устарели для 26.3.
- **Предупреждение про README.** Во многих README (например, mod-publish-plugin) в сборке остался `remapJar`. На Fabric 26.x с плагином без remap задача называется `jar` `[CI]`.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Primer 1.21.10 → 1.21.11 («The Rename Shuffle»)](https://docs.neoforged.net/primer/docs/1.21.11/) | ChampionAsh5357, 2025 | `ResourceLocation` → `Identifier`, перенос пакетов |
| [Primer 26.1 → 26.2](https://docs.neoforged.net/primer/docs/26.2/) | ChampionAsh5357, 2026 | Vulkan и Blaze3D, держатели id |
| [Fabric Docs 1.21.11 — Loom mapping migration](https://docs.fabricmc.net/1.21.11/develop/porting/mappings/loom) | FabricMC, 2025 | Использование `migrateMappings` |
| [Fabric Docs 1.21.11 — Ravel mapping migration](https://docs.fabricmc.net/1.21.11/develop/porting/mappings/ravel) | FabricMC, 2025 | Миграция из IDE, включая Kotlin |
| [Fabric Docs 26.1.2 — Fabric API porting](https://docs.fabricmc.net/26.1.2/develop/porting/fabric-api) | FabricMC, 2026 | Старые и новые имена Fabric API |
| [Linkie API (namespaces)](https://linkieapi.shedaniel.me/api/namespaces) | shedaniel | Программный перевод Yarn ↔ Mojang |
| [Paper API Maven metadata](https://repo.papermc.io/repository/maven-public/io/papermc/paper/paper-api/maven-metadata.xml) | PaperMC | Актуальные строки версий `paper-api` |

## 1. Официальная документация и порталы

**Ключевые выводы**

- **Fabric Docs (docs.fabricmc.net) — канонический справочник Fabric, Fabric Wiki — наследие.** Документация по умолчанию 26.2 (на один дроп позади 26.3) с селектором версий. Разделы: Getting Started, Items, Blocks, Fluids, Entities, Events, Sounds, Commands, Recipes, Rendering, Data Generation, Registries, Serialization (codecs, data attachments, saved data), Loom, Loader, Porting, Mixins, Class Tweakers (расширение доступа, interface injection, расширение enum), Misc (автоматическое тестирование, сеть, привязки клавиш, игровые правила).
- **Fabric Wiki** сама пишет «Some pages on the wiki may be out-of-date» и перенаправляет на версионную документацию. Большинство туториалов вики основаны на Yarn (≤1.21.11). Используйте вики для концепций (например, объяснения Mixin) и для старых версий.
- **Записи блога Fabric «Fabric for Minecraft X» — де-факто журнал изменений и руководство по портированию.** По записи на каждый дроп (даты записей, не релизов Mojang: 26.1 — 2026-03-24, 26.2 — 2026-06-16, 1.21.11 — 2025-12-09): 26.3 (2026-09-15), 26.2 (2026-06-15), 26.1 (2026-03-14), 1.21.11 (2025-12-05) … 1.21 (2024-05-31), плюс «Removing Obfuscation from Fabric» (2025-10-31). В каждой записи указаны версии Loom, Gradle и Loader и удалённый API.
  - 26.1: плагин `net.fabricmc.fabric-loom` (Loom 1.15) не делает remap. Замените `modImplementation` и `modCompileOnly` на `implementation` и `compileOnly`, `remapJar` на `jar`. Fabric API переименован в имена Mojang (`ItemGroupEvents` → `CreativeModeTabEvents`). `HudRenderCallback` удалён в пользу `HudElementRegistry`. Для миксинов нужна IntelliJ IDEA 2025.3+.
  - 26.2: необязательный экспериментальный бэкенд Vulkan, сырые вызовы OpenGL нужно пускать через Blaze3D. `Minecraft.getInstance().setScreen()` стал `Minecraft.getInstance().gui.setScreen()`. Появились расширения enum (Loader 0.19.0, Loom 1.17).
  - 26.3: `FuelRegistry`, `CompostingChanceRegistry`, `StrippableBlockRegistry` и другие удалены в пользу ванильных компонентов данных (`COOKING_FUEL`, `COMPOSTABLE`, `BLOCK_TRANSFORMER`). Рецепты и достижения стали перезагружаемыми динамическими реестрами. Ввод GLFW заменён на **SDL** (используйте `InputConstants`, вызывайте `TextInputManager` при смене фокуса).
- **NeoForge Docs (docs.neoforged.net)** по умолчанию 26.1, селектор назад до 1.20.3–1.20.4. Четыре набора: документация NeoForge, Toolchain Features (ModDevGradle и NeoGradle), **Primers**, User Guide, Modpack Development. Скачивания MDK на странице нет: используйте **Mod Generator** (neoforged.net/mod-generator) или организацию GitHub `neoforgemdks` (`MDK-26.3-ModDevGradle`, `MDK-1.21.1-ModDevGradle` и т. д., все пересозданы 2026-10/11). Для 26.x нужен JDK 25.
- **Праймеры портирования NeoForge (ChampionAsh5357) — лучший ресурс «что изменилось в ваниле» для всех загрузчиков.** 24 праймера, каждый шаг от 1.19.2→1.19.3 до **26.2→26.3**, с подразделами «Neo changes». Пост Fabric о 26.2 тоже ссылается на них.
- **Новости NeoForge** (neoforged.net/categories/news): посты по версиям (26.1 — 2026-03-24, 21.11, 21.9 с «The Transfer Rework», 21.6) и «Mitigating Network Vulnerabilities» (2026-05-11). Постов о 26.2 и 26.3 на первой странице нет, читайте праймеры.
- **Пост NeoForge о 26.1 — это чеклист портирования:** четырёхчастная схема версий, тулчейн Java 25, Gradle 9.1+, MDG 2.0.141, Parchment больше не нужен (официальные имена параметров поставляются с игрой), NeoForm вида `26.1-1`, `ItemStackTemplate`, `GuiGraphics` → `GuiGraphicsExtractor`, `Screen#render` → `Screen#extractRenderState`, `new ChunkPos(blockPos)` → `ChunkPos.containing(blockPos)`.
- **Forge Docs (docs.minecraftforge.net)** доходят до 1.21.x, хотя сам Forge выпускает 26.3. Читайте как справочник эпохи 1.20.1/1.21.
- **Forge Community Wiki (forge.gemwire.uk)** — «Latest 1.19, LTS 1.18.2», правка 2023-03-28: `[устарело]`, используйте документацию NeoForge.
- **Paper Docs (docs.papermc.io)** покрывают Paper, Velocity, Folia, Adventure и закрытый Waterfall. Разделы для разработки: Paper plugins (экспериментально), настройка проекта, plugin.yml, paperweight-userdev, Command API (Brigadier), Component API (Adventure), Lifecycle API, Data component API, PDC, планировщик, реестры, Dialog API (1.21.7+), поддержка Folia, дорожная карта. Javadoc: jd.papermc.io.
- **Вики и Javadoc Spigot** существуют, но после жёсткого форка новые API (компоненты, компоненты данных, диалоги, команды через lifecycle/Brigadier) есть только в Paper. Туториалы Spigot годятся для основ (слушатели, `plugin.yml`, `BukkitRunnable`), но используют устаревший строковый чат и `ChatColor`.
- **Технические страницы Minecraft Wiki — авторитет по форматам данных ванили:** формат компонентов данных, команды, датапаки, журналы изменений по версиям (`Java_Edition_26.3` перечисляет компоненты `minecraft:block_transformer`, `minecraft:cooking_fuel`, `minecraft:brewing_fuel`) и страницы **протокола Java Edition**, перенесённые из wiki.vg (сам wiki.vg больше не отвечает).
- **Статьи Mojang на minecraft.net** («Minecraft: Java Edition 26.x», посты о снапшотах) содержат разделы «Technical Changes» с номерами форматов паков и изменениями данных. «Removing obfuscation in Java Edition» (2025-10) — первоисточник решения о снятии обфускации. minecraft.net не отвечал с проверяющей машины `[не проверено]`.
- **Просмотр исходников.** **mcsrc.dev** (FabricMC/mcsrc, MIT, создан 2025-11) декомпилирует официальный jar в браузере через Vineflower и Unpick, это самый быстрый способ читать ванильный код 26.x без рабочего проекта. **Linkie** (linkie.shedaniel.dev; API linkieapi.shedaniel.me) показывает Yarn ≤1.21.11, Mojang, MCP (≤1.16.5), Quilt и `mojang_raw` для 26.x, а также поиск версий зависимостей («какая версия Fabric API, Architectury или Cloth Config нужна для X»). Блок зависимостей Fabric в Linkie заканчивается на 1.21.11, блоки NeoForge и Forge доходят до 26.3.
- **Parchment** (ParchmentMC) даёт имена параметров и javadoc для Mojmap ≤1.21.x. Для 26.x не нужен (последний push 2025-12-20, ветка `versions/1.21.x`).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Fabric Documentation — Developer Guides](https://docs.fabricmc.net/develop/) | FabricMC, 2026 (26.2 по умолчанию) | Канонический гайд Fabric, версионный |
| [Fabric Docs — Porting to 26.2](https://docs.fabricmc.net/develop/porting/) | FabricMC, 2026 | Скрипт сборки и изменения API по дропам |
| [Fabric Docs — Migrating mappings](https://docs.fabricmc.net/develop/porting/mappings/) | FabricMC, 2026 | Yarn → имена Mojang через Loom или Ravel |
| [Fabric Docs — Mixins](https://docs.fabricmc.net/develop/mixins/bytecode) | FabricMC, 2026 | Официальное введение в миксины (байткод, аксессоры) |
| [Fabric Docs — Automatic testing](https://docs.fabricmc.net/develop/automatic-testing) | FabricMC, 2026 | JUnit и игровые тесты в Fabric |
| [Fabric Docs 1.21.1 (archived version)](https://docs.fabricmc.net/1.21.1/develop/) | FabricMC, 2024–2025 | Для поддержки модов 1.21.1 (имена Yarn) |
| [Fabric Wiki](https://wiki.fabricmc.net/) | Сообщество, 2019–2026 | Наследие: туториалы и концепции, частично `[устарело]` → Fabric Docs |
| [Fabric blog — post list](https://fabricmc.net/blog/) | FabricMC | Записи «Fabric for Minecraft X» по версиям |
| [Fabric for Minecraft 26.3](https://fabricmc.net/2026/09/15/263.html) | FabricMC, 2026-09-15 | Loom 1.17, замены на компоненты данных, ввод SDL |
| [Fabric for Minecraft 26.2](https://fabricmc.net/2026/06/15/262.html) | FabricMC, 2026-06-15 | Бэкенд Vulkan, расширения enum, `gui.setScreen` |
| [Fabric for Minecraft 26.1](https://fabricmc.net/2026/03/14/261.html) | FabricMC, 2026-03-14 | Без обфускации, Loom без remap, переименования API |
| [Fabric for Minecraft 1.21.11](https://fabricmc.net/2025/12/05/12111.html) | FabricMC, 2025-12-05 | Заметки последнего выпуска эпохи Yarn |
| [Removing Obfuscation from Fabric](https://fabricmc.net/2025/10/31/obfuscation.html) | FabricMC, 2025-10-31 | Почему заканчиваются Yarn и intermediary; план миграции |
| [Fabric develop page (version picker)](https://fabricmc.net/develop/) | FabricMC | Актуальные версии Loader, API и Loom по версиям Minecraft |
| [Fabric template mod generator](https://fabricmc.net/develop/template/) | FabricMC, 2026 | Генерирует готовый проект (заменяет клонирование примера) |
| [NeoForge Documentation](https://docs.neoforged.net/docs/gettingstarted/) | NeoForged, 2026 (26.1 по умолчанию) | Канонический гайд NeoForge, версионный |
| [NeoForge porting primers index](https://docs.neoforged.net/primer/docs/) | ChampionAsh5357 и др., 2019–2026 | Разница ванили по версиям, 1.12 → 26.3 |
| [Primer 1.21.11 → 26.1](https://docs.neoforged.net/primer/docs/26.1/) | ChampionAsh5357, 2026 | Большой порт на снятие обфускации |
| [Primer 26.2 → 26.3](https://docs.neoforged.net/primer/docs/26.3/) | ChampionAsh5357, 2026 | Последние изменения ванили |
| [NeoForge Toolchain docs](https://docs.neoforged.net/toolchain/docs/) | NeoForged | Возможности MDG и NeoGradle |
| [NeoForge Mod Generator](https://neoforged.net/mod-generator/) | NeoForged | ZIP проекта для любой поддерживаемой версии MC |
| [neoforgemdks GitHub org](https://github.com/neoforgemdks) | NeoForged, пересоздано 2026-11 | Готовые MDK: MDK-26.3-ModDevGradle и др. |
| [NeoForge for Minecraft 26.1](https://neoforged.net/news/26.1release/) | NeoForged, 2026-03-24 | Схема версий, Java 25, переименования |
| [NeoForge 21.11 for 1.21.11](https://neoforged.net/news/21.11release/) | NeoForged, 2025-12-09 | Изменения последней линии 1.21 |
| [The Transfer Rework](https://neoforged.net/news/21.9-transfer-rework/) | NeoForged, 2025-09-30 | Новый API передачи предметов, жидкостей и энергии (для tech-модов) |
| [NeoForge news archive](https://neoforged.net/categories/news/) | NeoForged | Все анонсы |
| [Forge Documentation](https://docs.minecraftforge.net/en/latest/) | Команда Forge, ≤1.21.x | Справочник Forge, страниц 26.x нет |
| [Forge Community Wiki](https://forge.gemwire.uk/wiki/Main_Page) | Сообщество, последняя правка 2023 | `[устарело]` → документация NeoForge |
| [PaperMC Documentation](https://docs.papermc.io/) | PaperMC | Paper, Velocity, Folia, Adventure |
| [Paper development guide](https://docs.papermc.io/paper/dev/) | PaperMC, 2026 | Указатель API плагинов |
| [Paper plugins (experimental)](https://docs.papermc.io/paper/dev/getting-started/paper-plugins) | PaperMC | `paper-plugin.yml`, bootstrapper, loader |
| [Paper Javadocs](https://jd.papermc.io/paper/) | PaperMC | Справочник API |
| [Velocity developer docs](https://docs.papermc.io/velocity/dev/) | PaperMC | Плагины прокси |
| [Folia docs](https://docs.papermc.io/folia/) | PaperMC | Правила регионального многопоточия |
| [The future of Paper — Hard fork](https://papermc.io/news/the-future-of-paper-hard-fork/) | PaperMC, 2024-12-13 | Почему компилировать против Paper-API и переходить на paperweight-userdev; форк действует с 1.21.4. Тред на forums.papermc.io с тем же объявлением отвечает 522, ссылка дана на новость |
| [Spigot plugin development wiki](https://www.spigotmc.org/wiki/spigot-plugin-development/) | SpigotMC | Основы Bukkit (наследие) |
| [Spigot Javadocs](https://hub.spigotmc.org/javadocs/spigot/) | SpigotMC | API Bukkit/Spigot |
| [Minecraft Wiki — Data component format](https://minecraft.wiki/w/Data_component_format) | Сообщество, 2026 | Компоненты предметов (1.20.5+) |
| [Minecraft Wiki — Data pack](https://minecraft.wiki/w/Data_pack) | Сообщество, 2026 | Структура пака, номера форматов |
| [Minecraft Wiki — Commands](https://minecraft.wiki/w/Commands) | Сообщество, 2026 | Справочник синтаксиса Brigadier |
| [Minecraft Wiki — Java Edition protocol](https://minecraft.wiki/w/Java_Edition_protocol) | Сообщество (бывший wiki.vg), 2026 | Пакеты, типы данных, SLP, RCON |
| [Minecraft Wiki — Java Edition 26.3](https://minecraft.wiki/w/Java_Edition_26.3) | Сообщество, 2026 | Полный технический журнал изменений |
| [Minecraft Wiki — Java Edition 26.1](https://minecraft.wiki/w/Java_Edition_26.1) | Сообщество, 2026 | Без обфускации, Java 25, смена формата уровня |
| [Removing obfuscation in Java Edition](https://www.minecraft.net/en-us/article/removing-obfuscation-in-java-edition) | Mojang, 2025 | Первоисточник о снятии обфускации `[не проверено]` |
| [mcsrc.dev — online Minecraft source viewer](https://mcsrc.dev/) | FabricMC, 2025–2026 (MIT) | Просмотр декомпилированной ванили в браузере |
| [Linkie mappings & dependency search](https://linkie.shedaniel.dev/mappings) | shedaniel, 2026 | Поиск старых имён, версий зависимостей |
| [Parchment](https://github.com/ParchmentMC/Parchment) | ParchmentMC, ≤1.21.x | Имена параметров для Mojmap до 26.1 |

## 2. Туториалы и курсы

**Ключевые выводы**

- **Kaupenjoe (Modding by Kaupenjoe) — самый полный видеокурс, актуален для 26.x.** В организации GitHub `Tutorials-By-Kaupenjoe` есть `Fabric-Tutorial-26.X` и `NeoForge-Tutorial-26.X` (оба push 2026-11-03, MIT), по **одной ветке на эпизод** (`1-setup`, `3-items-and-datagen`, … `23-data-components`, `25-fov-mixin`, `30-update-to-26.2`, `31-crops`, `32-compostables`). В `gradle.properties` Fabric-репозитория стоят `minecraft_version=26.3`, `loom_version=1.18-SNAPSHOT`, `fabric_api_version=0.161.0+26.3`, `loader_version=0.19.5`; NeoForge-репозиторий нацелен на `neo_version=26.3.0.99`. Репозитории курсов `Fabric-Course-26.X` и `NeoForge-Course-26.X` существуют. Сайт kaupenjoe.net всё ещё рекламирует курсы для NeoForge и Fabric 1.21.1 и Forge и Fabric 1.20.X. Контент для Forge закончился на `Forge-Tutorial-1.21.X` (2025-05). Для основ Java есть `Java-Introduction-2026`.
- **Ловушка Kaupenjoe:** эпизоды записаны на версии момента записи, а затем обновляются в репозитории (`30-update-to-26.2`). Видео и ветка могут отличаться, всегда читайте дифф ветки.
- **TurtyWurty:** туториалы Forge 1.12–1.20, Fabric 1.21 (`1.21-Tutorial-Mod`, 2025-08) и **`26.1-TutorialMod`** (2026-05, CC0) — проект на MultiLoader-Template с подпроектами `common`/`fabric`/`neoforge` для 26.1 (`neo_form_version=26.1-1`, `fabric_version=0.144.0+26.1`, `fabric_loader_version=0.18.4`). Хорошее первое знакомство со структурой мультизагрузчика. Текстовые туториалы на turtywurty.dev. Он же делает **Railroad** — IDE для моддинга Minecraft (активна 2026-11).
- **McJty:** wiki.mcjty.eu/modding содержит вики с туториалами. По интервью CurseForge текущая видеосерия по NeoForge использует 1.21.1, а для 26.2 есть отдельный раздел вики `[не проверено]`. Старая документация mcjty.eu — NeoForge 1.20.4 и Forge 1.12–1.20. Хорош в блок-сущностях, GUI и основах рендеринга.
- **Официальные документы как туториалы:** цепочка Fabric Docs «Getting Started → Items → Blocks → Entities → Data generation» работает как бесплатный текстовый курс для 26.2. Документация NeoForge ближе к справочнику (сначала концепции).
- **MindScrath «NeoForge 26.1 Development in Action»** — бесплатное онлайн-руководство (Java 25, NeoForge 26.1.x; основы → блок-сущности, сущности, сеть → рендеринг, worldgen, миксины). Автор и лицензия не указаны, считайте вторичным источником.
- **Плагины Paper: Kody Simpson «Make a Minecraft Plugin in 2026»** (GitHub `KodySimpson/minecraft-plugin-tutorial`, push 2026-10-17) — современная серия по Paper, по замороженной ветке на часть: первый плагин, события, отмена событий, простые команды, **деревья команд Brigadier**, аргументы команд.
- **Туториалы на Habr (на русском)** описаны в разделе [Русскоязычные ресурсы](#русскоязычные-ресурсы). Статья про Fabric Mixin нацелена на 1.20.1 и Yarn (скрипт сборки устарел, понятия миксинов верны).
- **Серии, которые стоит узнавать и не брать для новой работы:** moddingtutorials.org (LukeGrahamLandry; Forge 1.16.5–1.19.3; сайт недоступен с проверяющей машины, репозиторий push 2026-09), Cy4 (Forge 1.16/1.18), Forge Community Wiki (≤1.19), rentry «MCModdingGuide» (последняя правка 2023-05; для игроков, эпоха Java 17).
- **MCreator** (генератор без кода; выпуск 2026.2.33518 от 2026-08-14) популярен у новичков. Сгенерированный код тяжело сопровождать, а на русском форуме mcmodding.ru есть целый раздел вопросов по MCreator. Упоминайте его как «с чем приходят новички».
- **Генераторы и помощники, которые предполагают туториалы:** генератор шаблона Fabric, NeoForge Mod Generator, misode.github.io (генераторы JSON для датапаков и worldgen), Blockbench (модели; GPL-3.0, push 2026-11), плагин IntelliJ Minecraft Development (minecraft-dev/MinecraftDev, LGPL-3.0, push 2026-11).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Fabric-Tutorial-26.X](https://github.com/Tutorials-By-Kaupenjoe/Fabric-Tutorial-26.X) | Kaupenjoe, 2026 (MIT) | Код Fabric 26.3 с веткой на эпизод |
| [NeoForge-Tutorial-26.X](https://github.com/Tutorials-By-Kaupenjoe/NeoForge-Tutorial-26.X) | Kaupenjoe, 2026 (MIT) | То же для NeoForge 26.3 |
| [Kaupenjoe Fabric 26.X YouTube playlist](https://www.youtube.com/playlist?list=PLKGarocXCE1H4l8g3c_KoRMsFzBsVjjAx) | Kaupenjoe, 2026 | Видеоприложение к коду |
| [NeoForge-Tutorial-1.21.X](https://github.com/Tutorials-By-Kaupenjoe/NeoForge-Tutorial-1.21.X) | Kaupenjoe, 2024–2025 | «Долгая» линия 1.21.1 |
| [Fabric-Tutorial-1.21.X](https://github.com/Tutorials-By-Kaupenjoe/Fabric-Tutorial-1.21.X) | Kaupenjoe, 2024–2025 | Fabric эпохи Yarn; `[устарело]` для 26.x → Fabric-Tutorial-26.X |
| [Forge-Tutorial-1.21.X](https://github.com/Tutorials-By-Kaupenjoe/Forge-Tutorial-1.21.X) | Kaupenjoe, 2024–2025 | Последняя серия по Forge |
| [Tutorials-By-Kaupenjoe org](https://github.com/Tutorials-By-Kaupenjoe) | Kaupenjoe | Все репозитории, включая Java-Introduction-2026 |
| [Kaupenjoe courses site](https://kaupenjoe.net/) | Kaupenjoe | Платные курсы (в списке 1.21.1 и 1.20.X) |
| [Modding by Kaupenjoe (YouTube)](https://www.youtube.com/@ModdingByKaupenjoe) | Kaupenjoe | Канал |
| [TurtyWurty 26.1-TutorialMod (multi-loader)](https://github.com/DaRealTurtyWurty/26.1-TutorialMod) | TurtyWurty, 2026 (CC0) | Структура common/Fabric/NeoForge на 26.1 |
| [TurtyWurty 1.21-Tutorial-Mod (Fabric)](https://github.com/DaRealTurtyWurty/1.21-Tutorial-Mod) | TurtyWurty, 2024–2025 | Код серии по Fabric 1.21 |
| [TurtyWurty Minecraft-Tutorials (text)](https://github.com/DaRealTurtyWurty/Minecraft-Tutorials) | TurtyWurty, ≤2024 | Текстовые туториалы за turtywurty.dev |
| [Railroad — IDE for Minecraft modding](https://github.com/DaRealTurtyWurty/Railroad) | TurtyWurty, 2026 | IDE для моддинга (экспериментальная) |
| [McJty modding wiki](https://wiki.mcjty.eu/modding/index.php/Main_Page) | McJty, 2024–2026 | Туториалы NeoForge 1.21.1 и раздел 26.2 |
| [10 Questions with McJty](https://blog.curseforge.com/10-questions-with-mcjty/) | Блог CurseForge | Подтверждает версии серий `[не проверено]` |
| [NeoForge 26.1 Development in Action](https://mindscrath.github.io/en/) | MindScrath, 2026 | Бесплатное онлайн-руководство по NeoForge 26.1 |
| [Make a Minecraft Plugin in 2026](https://github.com/KodySimpson/minecraft-plugin-tutorial) | Kody Simpson, 2026 | Современная серия по Paper, ветка на часть |
| [moddingtutorials.org source](https://github.com/LukeGrahamLandry/moddingtutorials.org) | LukeGrahamLandry, 2021–2026 | Текстовые туториалы Forge 1.16–1.19 `[не проверено]` `[устарело]` |
| [Architectury tutorial 1.21](https://github.com/Larsens-Mods/architectury-tutorial-1.21) | Larsens-Mods, 2025 | Пример мультизагрузчика на Architectury |
| [Dead Comedians resource list](https://epxzzy.github.io/Dead-Comedians-Resource-Expansion) | epxzzy, без даты | Подборка ссылок и мнения об авторах |
| [MCModdingGuide (rentry)](https://rentry.org/MCModdingGuide) | «Big Iron», 2022–2023 | Руководство по установке для игроков; `[устарело]` |
| [misode — data pack generators](https://misode.github.io/) | Misode | JSON таблиц добычи, worldgen, рецептов |
| [Minecraft Development plugin for IntelliJ](https://github.com/minecraft-dev/MinecraftDev) | minecraft-dev, 2026 | Мастера проектов, инспекции миксинов |
| [Blockbench](https://github.com/JannisX11/blockbench) | JannisX11, 2026 | Модели блоков, предметов и сущностей, экспорт GeckoLib |
| [MCreator](https://github.com/MCreator/MCreator) | Pylo, 2026.2 | Генератор без кода, который используют новички |
| [Mixin wiki](https://github.com/SpongePowered/Mixin/wiki) | SpongePowered | Основы Mixin (все загрузчики) |
| [MixinExtras wiki](https://github.com/LlamaLad7/MixinExtras/wiki) | LlamaLad7 | `@WrapOperation`, `@ModifyExpressionValue` и др. |
| [Mixin cheatsheet](https://github.com/2xsaiko/mixin-cheatsheet) | 2xsaiko | Примеры точек внедрения |

## 3. Шаблоны, тестовые моды и инструменты сборки

**Ключевые выводы**

- **Пример мода Fabric** (FabricMC/fabric-example-mod, CC0, 2395 звёзд): по одной ветке на версию Minecraft от 1.14.4 до **26.3** (ветка по умолчанию `26.3`, push 2026-10-24). Fabric теперь рекомендует **генератор шаблона** (fabricmc.net/develop/template), а не клонирование.
- **MDK NeoForge** создаются на каждую версию в организации `neoforgemdks`: `MDK-26.3-ModDevGradle`, `MDK-26.3-NeoGradle`, `MDK-26.2-…`, `MDK-26.1.2-…`, `MDK-1.21.1-…` и далее вниз до 1.20.x (пересозданы 2026-10/11). Issues и PR идут в `neoforged/MDK`. Предпочитайте вариант **ModDevGradle** (MDG 2.0.148).
- **Официальные тестовые моды — лучший источник «API на примерах».** Fabric API держит `src/testmod` и `src/testmodClient` в каждом модуле (например, `fabric-networking-api-v1/src/testmod`, `fabric-item-api-v1/src/testmod`). NeoForge держит `tests/` (ветка `26.3.x`) с сотнями мелких тестов функций. Paper держит модуль `test-plugin` в своём репозитории. Если для хука нет документации, сначала ищите в них.
- **Шаблоны мультизагрузчика:** MultiLoader-Template (jaredlll08, CC0, ветка 26.3) — подпроекты Gradle `common`/`fabric`/`neoforge` без зависимости времени выполнения. Шаблон Architectury описан в документации Architectury. `26.1-TutorialMod` TurtyWurty — разобранный пример на MultiLoader-Template. **Stonecutter** (препроцессинг для нескольких версий Minecraft) последний раз обновлялся 2025-12. **Unimined** (альтернативный тулчейн) активен 2026-11.
- **Шаблоны Paper:** `paperweight-test-plugin` (PaperMC, push 2026-04) показывает paperweight-userdev (доступ к NMS). Плагин IntelliJ Minecraft Development генерирует проекты Paper, Velocity, Fabric и NeoForge.
- Проверенные сборочные файлы (Fabric 26.3 с `net.fabricmc.fabric-loom`, NeoForge 26.3 с ModDevGradle 2.0.148) лежат в `tests/minecraft/` репозитория навыка и собираются в CI. Файлы лежат в `tests/minecraft/fabric-mod/build.gradle` и `tests/minecraft/neoforge-mod/build.gradle`, проверено в CI на GitHub Actions; их содержимое разобрано в [minecraft-modding.md](minecraft-modding.md). Версии в них подставляются из `gradle.properties` (Fabric: `minecraft_version=26.3`, `loader_version=0.20.1`, `loom_version=1.19.4`, `fabric_api_version=0.165.2+26.3`; NeoForge: `minecraft_version=26.3`, `neo_version=26.3.0.101`). Подробности настройки проекта — в [minecraft-modding.md](minecraft-modding.md). Генерируйте проект генератором и сверяйте его с этими файлами.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [fabric-example-mod](https://github.com/FabricMC/fabric-example-mod) | FabricMC, CC0, ветка 26.3 | Минимальный проект Fabric для каждой версии |
| [Fabric template generator](https://fabricmc.net/develop/template/) | FabricMC | Предпочтительный генератор проекта |
| [Fabric API repository](https://github.com/FabricMC/fabric-api) | FabricMC, Apache-2.0, ветка 26.3 | Модули и тестовые моды |
| [Fabric API item testmod (26.3)](https://github.com/FabricMC/fabric-api/tree/26.3/fabric-item-api-v1/src/testmod) | FabricMC | Пример тестового мода модуля |
| [Fabric Loader](https://github.com/FabricMC/fabric-loader) | FabricMC, Apache-2.0 | Внутренности загрузчика, `fabric.mod.json` |
| [Fabric Loom](https://github.com/FabricMC/fabric-loom) | FabricMC, MIT | Исходники плагина Gradle |
| [NeoForge MDK (upstream)](https://github.com/neoforged/MDK) | NeoForged | Источник генерируемых MDK |
| [MDK-26.3-ModDevGradle](https://github.com/neoforgemdks/MDK-26.3-ModDevGradle) | NeoForged, 2026-11 | Текущая стартовая точка NeoForge |
| [MDK-1.21.1-ModDevGradle](https://github.com/neoforgemdks/MDK-1.21.1-ModDevGradle) | NeoForged, 2026-10 | «Долгая» линия 1.21.1 |
| [NeoForge repository](https://github.com/neoforged/NeoForge) | NeoForged, LGPL-2.1, ветка 26.3.x | Патчи, события, расширения |
| [NeoForge tests (26.3.x)](https://github.com/neoforged/NeoForge/tree/26.3.x/tests) | NeoForged | Тестовые моды функций |
| [ModDevGradle](https://github.com/neoforged/ModDevGradle) | NeoForged, LGPL-2.1 | Рекомендуемый плагин Gradle |
| [NeoGradle](https://github.com/neoforged/NeoGradle) | NeoForged, LGPL-2.1 | Альтернативный плагин Gradle |
| [MinecraftForge repository](https://github.com/MinecraftForge/MinecraftForge) | Команда Forge, ветка 26.3 | Forge на 26.x |
| [Paper repository](https://github.com/PaperMC/Paper) | PaperMC, ветка main | `paper-api`, `paper-server`, `test-plugin` |
| [paperweight](https://github.com/PaperMC/paperweight) | PaperMC | Плагин Gradle userdev |
| [paperweight-test-plugin](https://github.com/PaperMC/paperweight-test-plugin) | PaperMC, 2026-04 | Шаблон плагина с доступом к NMS |
| [Paper userdev docs](https://docs.papermc.io/paper/dev/userdev) | PaperMC | Когда и как использовать внутренности |
| [Folia](https://github.com/PaperMC/Folia) | PaperMC, GPL-3.0, ver/26.3.x | Форк сервера с регионами |
| [Velocity](https://github.com/PaperMC/Velocity) | PaperMC, GPL-3.0, dev/4.0.0 | Прокси |
| [Adventure](https://github.com/PaperMC/adventure) | PaperMC (Kyori), MIT | Текстовые компоненты для плагинов |
| [Adventure docs](https://docs.advntr.dev/) | PaperMC | Компоненты, MiniMessage |
| [Stonecutter](https://github.com/stonecutter-versioning/stonecutter) | kikugie, LGPL-3.0 | Сборка под несколько версий |
| [Unimined](https://github.com/unimined/Unimined) | unimined, LGPL-2.1 | Альтернативный тулчейн для модов |
| [Architectury Loom](https://github.com/architectury/architectury-loom) | architectury, MIT | Форк Loom для мультизагрузчика |
| [Brigadier](https://github.com/Mojang/brigadier) | Mojang, MIT | Библиотека команд игры, Paper и Fabric |
| [DataFixerUpper](https://github.com/Mojang/DataFixerUpper) | Mojang, MIT | Исходники кодеков и DFU |
| [Vineflower](https://github.com/Vineflower/vineflower) | Vineflower, Apache-2.0 | Декомпилятор Loom и mcsrc |
| [MixinExtras](https://github.com/LlamaLad7/MixinExtras) | LlamaLad7, MIT | Поставляется вместе с Fabric и NeoForge |
| [Fabric Mixin fork](https://github.com/FabricMC/Mixin) | FabricMC, MIT | Mixin в версии Fabric |

## 4. Сообщества, платформы и публикация

**Ключевые выводы**

- **Официальные каналы помощи:** Discord Fabric (fabricmc.net/discuss предупреждает о серверах-самозванцах; есть также IRC #fabric на EsperNet), Discord NeoForged (discord.neoforged.net), Discord PaperMC (discord.gg/PaperMC; форумы Paper закрыты вместе с жёстким форком, для загрузок служит Hangar, для обсуждения Discord), форумы Forge (forums.minecraftforge.net, медленные). Для всех загрузчиков: Modded Minecraft Discord (discord.gg/moddedmc), Discord Kaupenjoe. В вопросе укажите загрузчик, точные версии (MC, загрузчик, API, Loom/MDG), полный лог (вставкой в стиле mclo.gs) и минимальный репозиторий.
- **Платформы:** Modrinth (открытый бэкенд `modrinth/code`; публичный API, чтение без ключа; правила на modrinth.com/legal/rules; проверка новых проектов персоналом) и CurseForge (REST API требует ключ; docs.curseforge.com; требование ключа `[не проверено]`). Плагины Paper: **Hangar** (hangar.papermc.io), Modrinth (тип проекта plugin), ресурсы SpigotMC.
- **Инструменты публикации:** `me.modmuss50.mod-publish-plugin` 2.2.1 (Modrinth, CurseForge и GitHub из Gradle), **Minotaur** (Gradle, Modrinth), GitHub Action **mc-publish** (Kira-NT, MIT). На Fabric 26.x указывайте им `jar`, а не `remapJar` `[CI]`.
- **Хостинг документации модов:** **Modded Minecraft Wiki** (moddedmc.wiki, Sinytra) — авторы модов публикуют версионную документацию из Git с интеграцией Gradle, в игре есть просмотрщик «Oracle Index», интерфейс на русском (`/ru`).
- **Базы и энциклопедии модов:** mcmod.cn (китайская, очень большая), страницы Modrinth и CurseForge.
- **Quilt:** Quilt Loader работает и загружает большинство модов Fabric, но QSL, QKL и Quilted Fabric API закрыты для 26.1. Туториалы с API Quilt ведут в тупик `[не проверено]`.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Fabric community page](https://fabricmc.net/discuss/) | FabricMC | Официальные ссылки на Discord и IRC |
| [Fabric Discord](https://discord.gg/v6v4pMv) | FabricMC | Официальная помощь |
| [NeoForged Discord](https://discord.neoforged.net/) | NeoForged | Официальная помощь |
| [PaperMC Discord](https://discord.gg/PaperMC) | PaperMC | Официальная помощь |
| [Modded Minecraft Discord](https://discord.gg/moddedmc) | Сообщество | Помощь для всех загрузчиков |
| [Minecraft Forge forums](https://forums.minecraftforge.net/) | Forge | Архив поддержки Forge |
| [SpigotMC](https://www.spigotmc.org/) | SpigotMC | Форумы и ресурсы Spigot |
| [Hangar](https://hangar.papermc.io/) | PaperMC | Репозиторий плагинов Paper и Velocity |
| [Modrinth API docs](https://docs.modrinth.com/api/) | Modrinth | Эндпоинты поиска, проектов, версий |
| [Modrinth content rules](https://modrinth.com/legal/rules) | Modrinth | Что отклоняется |
| [Modrinth support / help center](https://support.modrinth.com/) | Modrinth | Руководства для авторов |
| [Modrinth news](https://modrinth.com/news) | Modrinth | Изменения платформы |
| [modrinth/code](https://github.com/modrinth/code) | Modrinth | Открытый код платформы |
| [CurseForge API docs](https://docs.curseforge.com/rest-api/) | Overwolf/CurseForge | REST API (нужен ключ) |
| [mod-publish-plugin](https://github.com/modmuss50/mod-publish-plugin) | modmuss50, MIT, 2.2.1 | Публикация из Gradle на MR/CF/GH |
| [mod-publish-plugin docs](https://modmuss50.github.io/mod-publish-plugin/) | modmuss50 | Справочник настройки |
| [Minotaur](https://github.com/modrinth/minotaur) | Modrinth, LGPL-2.1 | Загрузчик Gradle для Modrinth |
| [mc-publish](https://github.com/Kira-NT/mc-publish) | Kira-NT, MIT | Публикация через GitHub Action |
| [Modded Minecraft Wiki](https://moddedmc.wiki/en) | Sinytra | Размещённая документация модов |
| [Modded Minecraft Wiki — author docs](https://docs.moddedmc.wiki/) | Sinytra | Как публиковать документацию |
| [Sinytra Wiki source](https://github.com/Sinytra/Wiki) | Sinytra | Код платформы вики |
| [MC百科 mcmod.cn](https://www.mcmod.cn/) | Сообщество (CN) | Энциклопедия модов |
| [Bringing Quilt into the non-obfuscated era](https://quiltmc.org/en/blog/2026-02-03-non-obfuscated-updates/) | QuiltMC, 2026-02-03 | Закрытие QSL |
| [Quilt Loader](https://github.com/QuiltMC/quilt-loader) | QuiltMC, Apache-2.0 | Всё ещё поддерживается |
| [Quilt Standard Libraries](https://github.com/QuiltMC/quilt-standard-libraries) | QuiltMC, последний push 2026-01 | Закрыта; `[устарело]` → Fabric API |
| [NeoForged project listing](https://projects.neoforged.net/neoforged/neoforge) | NeoForged | Все версии NeoForge |
| [2025: Big Changes are Coming](https://neoforged.net/news/2025-retrospection/) | NeoForged, 2026-01-01 | Дорожная карта и итоги года |

## 5. Статистика экосистемы

**Ключевые выводы**

- Все числа получены из API Modrinth 2026-11-07. Они завышают долю Fabric: у CurseForge нет статистики без ключа API, там исторически больше Forge и NeoForge. Данных CurseForge нет.
- **Итоги:** 167 161 проект, 1 547 646 версий, 72 114 авторов (`/v2/statistics`). Модов 78 230, плагинов 20 037, модпаков 19 208.
- **Доля загрузчиков среди модов Modrinth** (проект считается, если хотя бы одна версия указывает загрузчик; мультизагрузчики считаются в каждом): Fabric 47 751 (61% модов), Forge 35 648 (46%), NeoForge 30 622 (39%), Quilt 10 631 (14%). Плагины: Paper 18 978, Spigot 12 476, Purpur 11 015, Bukkit 9 480, Folia 3 911, Velocity 973, BungeeCord 545. Датапаки 15 591.
- **Модпаки по загрузчику:** Fabric 13 681, Forge 3 891, NeoForge 2 672, Quilt 687.
- **Принятие версий** (фасеты на уровне проекта, поэтому перекрёстные числа «загрузчик × версия» приблизительны):

| Версия MC | Все моды | Fabric | NeoForge | Forge | Paper (плагины) |
|---|---|---|---|---|---|
| 26.3 | 9 912 | 9 413 | 5 360 | 2 689* | 3 442 |
| 26.2 | 14 618 | 13 792 | 7 184 | 3 574* | 5 994 |
| 26.1.2 | 15 740 | 13 697 | 8 538 | 3 542* | 6 779 |
| 1.21.11 | 20 086 | 18 310 | 8 392 | 4 587 | 12 431 |
| 1.21.8 | 17 654 | 14 455 | 9 318 | 4 263 | 13 216 |
| 1.21.4 | 17 891 | 13 876 | 9 575 | 4 426 | 14 142 |
| **1.21.1** | **32 844** | 20 026 | **21 978** | 5 577 | 14 081 |
| **1.20.1** | **35 127** | 17 945 | 5 167 | **25 543** | 7 271 |
| 1.19.2 | 12 396 | 7 597 | 1 043 | 8 204 | 4 634 |
| 1.18.2 | 8 919 | 5 478 | 808 | 6 078 | 4 129 |
| 1.16.5 | 5 931 | 3 081 | 522 | 4 303 | 3 304 |
| 1.12.2 | 2 885 | 167 | 84 | 2 749 | 1 363 |

  \* Числа Forge на 26.x завышены мультизагрузчиками, которые указывают Forge для старых версий. Реальных Forge-модов на 26.x меньше.
- **Как читать числа.** Три «центра притяжения»: **1.20.1 (Forge)**, **1.21.1 (NeoForge)** и **последняя версия (Fabric)**. Модпаки: 1.20.1 = 7 421, 1.21.1 = 5 250, 1.21.11 = 2 988, 26.1.2 = 1 407, 26.3 = 838. Новый мод, которому нужен максимальный охват в конце 2026, нацеливается на 26.x (Fabric и NeoForge) и по желанию делает бэкпорт на 1.21.1 (NeoForge) и 1.20.1 (Forge). Авторы сборок остаются на 1.20.1 и 1.21.1 дольше авторов модов, поэтому библиотека, рано отказавшаяся от 1.21.1, теряет большую часть использования в сборках.
- **Самые скачиваемые по категориям (Modrinth, за всё время):**
  - Библиотеки: Fabric API 283,7M, Cloth Config 183,8M, YACL 134,0M, Fabric Language Kotlin 129,8M, Architectury API 107,6M, JEI 84,8M, GeckoLib 76,4M, Text Placeholder API 74,1M.
  - Оптимизация: Sodium 249,4M, Iris 192,9M, Entity Culling 186,0M, FerriteCore 165,4M, Lithium 141,5M, ImmediatelyFast 136,2M, Sodium Extra 105,6M, ModernFix 83,4M.
  - Утилиты и QoL: Mod Menu 159,2M, Xaero's Minimap 119,5M, ETF 110,3M, EMF 104,7M, AppleSkin 98,0M.
  - Технологии: VeinMiner 100,7M, Create 27,9M, Quark 26,3M, Immersive Aircraft 15,5M, Create: Steam 'n' Rails 13,5M.
  - Магия: Enchantment Descriptions 39,8M, Waystones 27,5M, Alex's Caves 11,5M, Aquamirae 11,2M, Simply Swords 10,3M.
  - Worldgen: TerraBlender 42,7M, Biomes O' Plenty 38,6M, YUNG's API 36,6M, Lithostitched 25,4M, Terralith 24,6M, Tectonic 17,5M.
  - Мобы: EMF, Cobblemon 36,8M, Dungeons and Taverns 22,1M, L_Ender's Cataclysm 19,3M, Deeper and Darker 16,3M, Alex's Mobs 15,7M.
  - Хранение: Mouse Tweaks 63,9M, Clumps 42,5M, Inventory Profiles Next 42,1M, Shulker Box Tooltip 41,0M, Supplementaries 29,1M.
  - Социальное и управление: Simple Voice Chat 75,8M, No Chat Reports 61,7M, Dynamic FPS 71,4M, Essential 48,2M.
  - Плагины (тип plugin): VeinMiner, Simple Voice Chat, LMD, Chunky 20,0M, JourneyMap, WorldEdit 11,8M. Почти все **кроссплатформенные** (сборки и мода, и плагина).
  - Модпаки: Fabulously Optimized 19,1M (Fabric, 26.3), Zombie Invade 100 Days 15,2M (Forge 1.20.1), Cobblemon Fabric 11,4M (1.21.1).
- **Вывод для учащегося:** клиентские и производительные моды (сначала Fabric) лидируют по скачиваниям. Контентные, технологические и магические моды имеют меньше, но более глубоких пользователей. Библиотеки — самый часто устанавливаемый код в экосистеме, поэтому дизайн API библиотек стоит изучать.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Modrinth statistics endpoint](https://api.modrinth.com/v2/statistics) | Modrinth | Общие итоги |
| [Modrinth loader tags](https://api.modrinth.com/v2/tag/loader) | Modrinth | Все id загрузчиков (fabric, neoforge, paper, folia, ornithe, babric…) |
| [Modrinth search API](https://api.modrinth.com/v2/search?index=downloads&facets=%5B%5B%22project_type:mod%22%5D%5D) | Modrinth | Запросы по фасетам для этих таблиц |
| [Mojang version manifest v2](https://piston-meta.mojang.com/mc/game/version_manifest_v2.json) | Mojang | Список релизов и снапшотов с датами |
| [Fabric meta — loader versions](https://meta.fabricmc.net/v2/versions/loader) | FabricMC | Актуальные сборки загрузчика |
| [NeoForge Maven metadata](https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml) | NeoForged | Версии NeoForge |
| [Forge promotions](https://files.minecraftforge.net/net/minecraftforge/forge/promotions_slim.json) | Forge | Latest и recommended по версиям MC |
| [Paper Fill API v3](https://fill.papermc.io/v3/projects/paper) | PaperMC | Версии и сборки Paper |

## Эталонные кодовые базы

Как читать раздел. «Загрузчик и версия» — старшая **релизная** версия Minecraft в `game_versions` проекта на Modrinth (2026-11-07), «push» — `pushed_at` на GitHub. Лицензия — SPDX-id с Modrinth (GitHub часто показывает `NOASSERTION` для собственных лицензий). Все строки проверены по API, кроме помеченных.

**Ключевые выводы (общие)**

- **Сосуществуют два поколения.** Крупные контентные, магические и технологические моды с наследием Forge в основном всё ещё выпускают **1.21.1 на NeoForge** (Create, Farmer's Delight, Supplementaries, Quark, Ars Nouveau, Iron's Spells, Immersive Engineering, Industrial Foregoing, Modern Industrialization, EMI, Cobblemon), потому что NeoForge 21.1 — «долгая» линия сообщества. Моды Fabric-first, библиотеки, клиентские и производительные моды уже на **26.3**. Определяйте, на какой линии вы читаете: код 1.21.1 использует `ResourceLocation`, имена Parchment и Java 21.
- **Читайте ветку по умолчанию, но проверяйте её.** Часть репозиториев по умолчанию стоит на старой ветке (Botania `1.20.x` с веткой `1.21.1-porting`; Continuity `1.20.1/dev`; Architectury API `1.19.2`; AppleSkin `1.16-forge`; ModernFix `1.20`). Выбирайте ветку с нужной версией (у AE2 есть ветки `1.21.1` … `1.21.11`, `26.1`, `main`; у Sodium `26.1.2/stable`, `26.2/stable`, `dev`).
- **Лицензии: читать можно, копировать часто нельзя.** Не OSI-лицензии или ограничивающие среди главных образцов: Create (собственная «Create Mod License»), Sodium (PolyForm Shield 1.0.0: исходники доступны, конкурирующее использование запрещено), Quark и Patchouli (CC BY-NC-SA 3.0), Jade (CC BY-NC-SA 4.0), Botania (своя), Supplementaries (своя), Terralith (лицензия Stardust Labs), EntityCulling, 3D Skin Layers и Not Enough Animations (tr7zw Protective), Iron's Spells, Ars Nouveau (ARR на Modrinth), Balm, Biomes O' Plenty, Waystones, Xaero's (ARR; Xaero's закрытый код). Разрешающие образцы, из которых можно брать приёмы: Farmer's Delight, Mekanism, Refined Storage 2, Occultism, JEI, EMI, REI, Mod Menu, FerriteCore, C2ME (MIT); Lithium, Iris, Polymer, Architectury, Cloth Config, TerraBlender (LGPL-3.0); Fabric API (Apache-2.0). Для многих репозиториев GitHub показывает `NOASSERTION` (собственные лицензии: Create, Botania, Quark, Supplementaries, Twilight Forest, Sodium). Точные условия не читались: перед повторным использованием кода читайте `LICENSE` `[не проверено]`.
- **Kotlin** часто встречается (Cobblemon, пользователи Fabric Language Kotlin; Kotlin for Forge 54M скачиваний). Тому, кто знает только Java, сначала подойдут кодовые базы на Java.

### Контентные моды (предметы, блоки, мобы, еда, декор)

- **Farmer's Delight** — чистый компактный контентный мод NeoForge: культуры, блок-сущность котла и меню, тип рецепта разделочной доски, датаген, модификаторы добычи. Порт на Fabric «Farmer's Delight Refabricated» (MIT, 16,9M) показывает тот же мод на Fabric.
- **Supplementaries** (MehVahdJukaar) — десятки мелких блоков и предметов; мультизагрузчик через **Moonlight Lib**. Хороший пример организации «много мелких функций», клиентского рендеринга блок-сущностей и настраиваемых функций.
- **Quark** (Vazkii) — система модулей (каждая функция отключаемый модуль), контент по конфигу.
- **Alex's Mobs** заморожен на 1.20.1 (последний выпуск 2024-09): изучайте только ИИ сущностей, не современные API.
- **Cobblemon** — огромный мод на Kotlin; изучайте data-driven виды и анимацию Molang, но не как первое чтение.

| Проект | Загрузчик и версия | Лицензия | Чему учит |
|---|---|---|---|
| [Farmer's Delight](https://github.com/vectorwing/FarmersDelight) | NeoForge 1.21.1 (ветка `1.21`; ветка `26.1` в работе); push 2026-09 | MIT | Компактный контентный мод; типы рецептов, блок-сущность |
| [Farmer's Delight Refabricated (Modrinth)](https://modrinth.com/mod/farmers-delight-refabricated) | Fabric | MIT | Тот же дизайн на Fabric |
| [Supplementaries](https://github.com/MehVahdJukaar/Supplementaries) | Мультизагрузчик, 1.21.1; push 2026-11 | Собственная | Много мелких функций, мультизагрузчик |
| [Moonlight Lib](https://github.com/MehVahdJukaar/Moonlight) | Мультизагрузчик, 26.1.2 | LGPL с оговоркой | Библиотека мультизагрузчика за Supplementaries |
| [Quark](https://github.com/VazkiiMods/Quark) | 1.21.1 (`1.21.1/dev`); push 2026-10 | CC BY-NC-SA 3.0 | Модульная, конфигурируемая архитектура |
| [The Twilight Forest](https://github.com/TeamTwilight/twilightforest) | Ветка `latest` нацелена на 26.1.2; push 2026-11 | Собственная | Измерение, боссы, структуры |
| [The Aether](https://github.com/The-Aether-Team/The-Aether) | 1.21.1 (`1.21.1-develop`, push 2026-06) | LGPL-3.0 | Мод-измерение на 1.21.1 |
| [Alex's Mobs](https://github.com/AlexModGuy/AlexsMobs) | 1.20.1; `[устарело]` API | GPL-3.0 | Идеи ИИ сущностей (старый API) |
| [Adorn](https://github.com/Juuxel/Adorn) | Мультизагрузчик, 26.2 | MIT | Небольшой читаемый мод декора |
| [Cobblemon](https://gitlab.com/cable-mc/cobblemon) | 1.21.1, Kotlin (GitLab) | MPL-2.0 | Большой data-driven мод существ |

### Технологии и автоматизация

- **Create** — кинетические сети, контрапции (движущиеся структуры из блоков), внутриигровые туториалы Ponder, инстансный рендеринг **Flywheel**. NeoForge 1.21.1 (`mc1.21.1/dev`, neo 21.1.219). Порт на Fabric (Fabricators-of-Create, MIT) остановился на 1.20.1 (push 2025-12): Create 6 на Fabric там не поддерживается.
- **Applied Energistics 2** — сети хранения, граф сеток и узлов, задания крафт-процессоров, большой набор игровых тестов. Ветки 1.21.1 … 26.1 и `main`; на Modrinth виден 26.3. Лицензия «Multiple» (код LGPL-3.0, ресурсы отдельно).
- **Mekanism** — машины, газы и химикаты, мультиблоки, энергия. Ветка по умолчанию на GitHub **26.3** (`minecraft_version=26.3`, `previous_minecraft_version=1.21.1`), а на Modrinth старший выпуск 1.21.1. Выпуски 26.x могут отставать или быть только на CurseForge: неясно `[не проверено]`.
- **Refined Storage 2** — переписан с отдельным чисто Java-ядром (`refinedstorage2`). Образец того, как держать игро-независимую логику вне классов Minecraft и покрывать её модульными тестами.
- **Transfer Rework (NeoForge 21.9)** изменил обработчики предметов, жидкостей и энергии. Tech-код для 1.21.1 использует старый API capability, код 26.x — новый API передачи. Прочитайте новостной пост перед сравнением tech-модов между линиями.

| Проект | Загрузчик и версия | Лицензия | Чему учит |
|---|---|---|---|
| [Create](https://github.com/Creators-of-Create/Create) | NeoForge 1.21.1 | Собственная | Кинетика, контрапции, Ponder |
| [Flywheel](https://github.com/Engine-Room/Flywheel) | Ветка 26.1.2/dev | MIT | Движок инстансного рендеринга, используется Create |
| [Create Fabric](https://github.com/Fabricators-of-Create/Create) | Fabric 1.20.1; остановлен `[устарело]` | MIT | Пример порта |
| [Applied Energistics 2](https://github.com/AppliedEnergistics/Applied-Energistics-2) | 26.3 | LGPL (код) | Сети-сетки, игровые тесты |
| [Mekanism](https://github.com/mekanism/Mekanism) | Ветка 26.3 | MIT | Большая модульная технологическая кодовая база |
| [Refined Storage 2](https://github.com/refinedmods/refinedstorage2) | 26.1.2 | MIT | Разделение ядра и платформы, модульные тесты |
| [Industrial Foregoing](https://github.com/InnovativeOnlineIndustries/Industrial-Foregoing) | 1.21 (на библиотеке Titanium) | MIT | Каркас машин |
| [Immersive Engineering](https://github.com/BluSunrize/ImmersiveEngineering) | 1.21.1; push 2025-08 | Собственная | Мультиблоки, провода |
| [Modern Industrialization](https://github.com/AztechMC/Modern-Industrialization) | NeoForge 1.21.1 (родом из Fabric) | MIT | Машины на данных, API передачи |
| [Tech Reborn](https://github.com/TechReborn/TechReborn) | Fabric 26.3 | MIT | Fabric-native технологии, API энергии Team Reborn Energy |
| [Oritech](https://github.com/Rearth/Oritech) | Fabric и NeoForge, 26.1.2 | CC0 на GitHub, CC-BY-4.0 на Modrinth | Современные мультизагрузчик-технологии, GeckoLib |
| [Galacticraft](https://github.com/TeamGalacticraft/Galacticraft) | Fabric 1.21.1; активен 2026-11 | MIT | Технологии и космос на Fabric, библиотека MachineLib |
| [Powah!](https://github.com/Technici4n/Powah) | 26.1.2 | LGPL-3.0 | Небольшой читаемый мод энергии |
| [Pipez](https://github.com/henkelmax/pipez) | 26.3 | ARR | Трубы и передача, небольшой |

### Магия

- **Botania** (Vazkii) — сеть маны, цветы, лексикон. Ветка по умолчанию `1.20.x`, есть ветка `1.21.1-porting`, на Modrinth максимум 1.20.1. Структура мультизагрузчика (Xplat/Fabric/Forge).
- **Ars Nouveau** (baileyholl) — система глифов заклинаний, ритуалы, сущности на GeckoLib. NeoForge 1.21.1 (`minecraft_version_range=[1.20.6,1.21.2)` на `main`).
- **Occultism** (klikli-dev) — мод ритуалов и духов; ветка по умолчанию `version/26.1.2`, на Modrinth 26.3. Автор также делает Modonomicon (библиотека книг). Лучший **разрешающе лицензированный** и актуальный образец магии.
- **Hex Casting** — язык заклинаний на рисуемых паттернах; на Modrinth максимум 1.20.1, но репозиторий активен 2026-11.
- **Spectrum** (DaFuqs) — магия и прогрессия на Fabric (ветка по умолчанию `1.21.1-neoforge`).

| Проект | Загрузчик и версия | Лицензия | Чему учит |
|---|---|---|---|
| [Botania](https://github.com/VazkiiMods/Botania) | Мультизагрузчик, 1.20.x / ветка 1.21.1-porting | Собственная | Классика магии на мультизагрузчике |
| [Ars Nouveau](https://github.com/baileyholl/Ars-Nouveau) | NeoForge 1.21.1 | ARR | Система заклинаний, GeckoLib |
| [Occultism](https://github.com/klikli-dev/occultism) | 26.x | MIT | Современный разрешающий мод магии |
| [Iron's Spells 'n Spellbooks](https://github.com/iron431/irons-spells-n-spellbooks) | 1.21.1 | ARR | Заклинания, атрибуты, анимации |
| [Hex Casting](https://github.com/FallingColors/HexMod) | Мультизагрузчик, 1.20.1 на Modrinth | MIT | Язык паттернов, мультизагрузчик |
| [Spectrum](https://github.com/DaFuqs/Spectrum) | Fabric, 1.21.1 | LGPL-3.0 | Прогрессия магии на Fabric |
| [Apotheosis](https://github.com/Shadows-of-Fire/Apotheosis) | Ветка 26.1, на Modrinth 26.1.2 | MIT | Системы добычи и аффиксов, чары |

### Генерация мира

- **Terralith** (Stardust Labs) — почти чистый **датапак** (поставляется как датапак и jar моды). Выпуск 26.3. Ветка по умолчанию на GitHub `1.20` (2025-02): выпуски идут в другом месте, публичный репозиторий может отставать `[не проверено]`. Изучайте JSON worldgen, не Java.
- **Tectonic** (Apollounknowndev) — форма рельефа через функции плотности (ветка `rewrite-squared`). **Lithostitched** (тот же автор) — библиотека worldgen с модификаторами структур и биомов в JSON.
- **Biomes O' Plenty** и **TerraBlender** (Glitchfiend) — мод биомов и библиотека внедрения регионов и биомов, которой он пользуется. Обе на 26.3, ветка по умолчанию `26.3`.
- **YUNG's Better Dungeons** (и YUNG's API) — структуры и обработка jigsaw.
- **Ловушка:** 26.3 переработал features (`worldgen/feature`, material rules). Код worldgen для ≤26.2 не компилируется и не загружается на 26.3.

| Проект | Загрузчик и версия | Лицензия | Чему учит |
|---|---|---|---|
| [Terralith](https://github.com/Stardust-Labs-MC/Terralith) | 26.3 (репозиторий отстаёт) | Собственная | Worldgen в датапаке |
| [Tectonic](https://github.com/Apollounknowndev/tectonic) | 26.3 | MIT | Рельеф на функциях плотности |
| [Lithostitched](https://github.com/Apollounknowndev/lithostitched) | 26.3 | MIT | Библиотека модификаторов worldgen |
| [Biomes O' Plenty](https://github.com/Glitchfiend/BiomesOPlenty) | 26.3 | ARR (исходники открыты) | Контент биомов |
| [TerraBlender](https://github.com/Glitchfiend/TerraBlender) | 26.3 | LGPL-3.0 | API внедрения биомов |
| [YUNG's Better Dungeons](https://github.com/YUNG-GANG/YUNGs-Better-Dungeons) | 26.1.2 | LGPL-3.0 | Структуры jigsaw |

### QoL и UI (просмотр рецептов, подсказки, меню)

- **JEI** (mezz) — эталонный API просмотра рецептов (шаблон интерфейса плагина, common/fabric/neoforge), **EMI** (emilyploszaj) — современный просмотрщик, но только до 1.21.1 (ветка `1.21`), **REI** (shedaniel) — на Architectury. Написание плагина JEI, EMI или REI — стандартное упражнение на интеграцию.
- **Jade** (Snownee) — HUD информации о блоке и сущности с API плагинов (ветки по загрузчикам, например `26.3-fabric`).
- **Mod Menu** (TerraformersMC) — список модов Fabric и точка входа API экранов настройки.
- **AppleSkin** — крошечный клиентский мод HUD и подсказок, идеальное первое чтение по рендерингу HUD и синхронизации данных о еде между клиентом и сервером. **Mouse Tweaks** — обработка ввода в инвентаре.
- Ловушка 26.3: `TooltipFlag#shouldDisplayAllInformation` добавили и Fabric, и NeoForge, чтобы просмотрщики рецептов могли индексировать подсказки за клавишей.

| Проект | Загрузчик и версия | Лицензия | Чему учит |
|---|---|---|---|
| [Just Enough Items](https://github.com/mezz/JustEnoughItems) | Мультизагрузчик, 26.3 | MIT | Дизайн API плагинов, мультизагрузчик |
| [EMI](https://github.com/emilyploszaj/emi) | 1.21.1 | MIT | Современный просмотрщик рецептов |
| [Roughly Enough Items](https://github.com/shedaniel/RoughlyEnoughItems) | 26.3, Architectury | MIT | Просмотрщик на Architectury |
| [Jade](https://github.com/Snownee/Jade) | 26.3 | CC BY-NC-SA 4.0 | HUD с API плагинов |
| [Mod Menu](https://github.com/TerraformersMC/ModMenu) | Fabric 26.3 | MIT | API конфигурации Fabric |
| [AppleSkin](https://github.com/squeek502/AppleSkin) | 26.3 | Unlicense | Крошечный HUD и пример синхронизации |
| [Mouse Tweaks](https://github.com/YaLTeR/MouseTweaks) | 26.3 | BSD-3 | Клиентский ввод в экранах контейнеров |
| [Clumps](https://github.com/jaredlll08/Clumps) | Мультизагрузчик, 26.3 | MIT | Крошечный мультизагрузчик-мод (автор MultiLoader-Template) |

### Клиент и рендеринг

- **Sodium** (CaffeineMC) — переписанный меширинг и рендеринг чанков; ветки по версиям (`26.1.2/stable`, `26.2/stable`, `dev`); на Modrinth 26.3-rc-2. **Iris** — загрузчик шейдерпаков поверх Sodium. Оба глубокие, читайте после знакомства с рендерингом ванили; Sodium только для чтения (лицензия).
- **Entity Model Features / Entity Texture Features** (Traben) — форматы OptiFine CEM и случайные текстуры на Fabric и NeoForge. **Continuity** — связные текстуры (ветка по умолчанию `1.20.1/dev`, но выпуски 26.3). **LambDynamicLights** — динамическое освещение.
- **Flywheel** и **Veil** (FoundryMC) — библиотеки рендеринга (инстансинг, постобработка и шейдеры).
- **Distant Horizons** — LOD-рендеринг (GitLab, 37,7M скачиваний). **Flashback** (Moulberry) — мод повторов, исходники на GitHub.
- Для любого кода рендеринга: 26.2 добавил экспериментальный бэкенд Vulkan, моды в обход Blaze3D с сырым GL ломаются при его выборе.

| Проект | Загрузчик и версия | Лицензия | Чему учит |
|---|---|---|---|
| [Sodium](https://github.com/CaffeineMC/sodium) | 26.3 | PolyForm Shield | Конвейер рендеринга чанков (только чтение) |
| [Iris](https://github.com/IrisShaders/Iris) | 26.3 | LGPL-3.0 | Конвейер шейдеров |
| [Entity Model Features](https://github.com/Traben-0/Entity_Model_Features) | 26.3 | LGPL-3.0 | Движок замены моделей |
| [Continuity](https://github.com/PepperCode1/Continuity) | Fabric, выпуски 26.3 | LGPL-3.0 | Связные текстуры через API моделей |
| [LambDynamicLights](https://github.com/LambdAurora/LambDynamicLights) | 26.3 | Собственная (Lambda) | Динамическое освещение |
| [Veil](https://github.com/FoundryMC/Veil) | 1.21.1 | LGPL-3.0 | Библиотека рендеринга |
| [Distant Horizons](https://gitlab.com/distant-horizons-team/distant-horizons) | 26.3 | LGPL-3.0 | LOD-рендеринг |
| [Flashback](https://github.com/Moulberry/Flashback) | Fabric 26.3 | ARR | Запись и рендеринг повторов; ссылка на исходники из Modrinth `[не проверено]` |

### Производительность

- **Lithium** (CaffeineMC) — оптимизации игровой логики как множество мелких, отдельно включаемых миксинов; образцовая организация конфигурации миксинов. **FerriteCore** (malte0811) — память: дедупликация карт состояний блоков и моделей; небольшой и хорошо документированный. **ModernFix** (embeddedt) — исправления запуска и памяти на обоих загрузчиках. **C2ME** (RelativityMC) — параллельная генерация и ввод-вывод чанков. **Krypton** (astei) — сетевой стек. **ScalableLux** — движок освещения. **ImmediatelyFast** (RaphiMC) — батчинг режима немедленного рендеринга. **Entity Culling** (tr7zw, защитная лицензия).
- Это лучшее место для изучения **продвинутых миксинов** (redirect, обход overwrite, защиты совместимости) и **правок по результатам профилирования**. Профилировщик, которым пользуются все, — spark (lucko).

| Проект | Загрузчик и версия | Лицензия | Чему учит |
|---|---|---|---|
| [Lithium](https://github.com/CaffeineMC/lithium) | 26.3 | LGPL-3.0 | Организация миксинов, оптимизация тиков |
| [FerriteCore](https://github.com/malte0811/FerriteCore) | 26.3 | MIT | Оптимизация памяти, небольшой |
| [ModernFix](https://github.com/embeddedt/ModernFix) | 26.1.2 | LGPL-3.0 | Исправления запуска и памяти |
| [C2ME](https://github.com/RelativityMC/C2ME-fabric) | 26.3 | MIT | Параллелизм в системах чанков |
| [Krypton](https://github.com/astei/krypton) | 26.3 | LGPL-3.0 | Оптимизация Netty и сети |
| [ScalableLux](https://github.com/RelativityMC/ScalableLux) | 26.3 | LGPL-3.0 | Движок освещения |
| [ImmediatelyFast](https://github.com/RaphiMC/ImmediatelyFast) | 26.3 | LGPL-3.0 | Батчинг рендеринга |
| [Entity Culling](https://github.com/tr7zw/EntityCulling) | Fabric и NeoForge | tr7zw Protective | Отсечение по окклюзии |
| [spark](https://github.com/lucko/spark) | 26.3 | GPL-3.0 | Профилировщик модов и плагинов |

### Библиотеки и API

- **Architectury API** (LGPL-3.0, 26.3; ветка по умолчанию `1.19.2`, выбирайте нужную) и **Architectury Loom** (MIT) — слой абстракции «пишем один раз» для Fabric и NeoForge. Альтернатива без зависимости времени выполнения: **MultiLoader-Template** (jaredlll08, CC0, ветка 26.3).
- **Библиотеки конфигурации:** Cloth Config (LGPL-3.0, 26.3), YACL (LGPL-3.0), owo-lib (MIT, 26.2; также UI-каркас, ветка «braid»), MidnightLib (MIT, 26.3), Fzzy Config (собственная).
- **Слоты экипировки:** Curios API (LGPL-3.0, 26.3; теперь и на Fabric), Accessories (wisp-forest, MIT, максимум 1.21.10), Trinkets (MIT, Fabric, остановлен на 1.21.1).
- **Анимация:** GeckoLib (MIT, 26.3; вики на GitHub) и AzureLib (MIT, 26.3) для сущностей и брони, созданных в Blockbench.
- **Книги-путеводители:** Patchouli (CC BY-NC-SA 3.0, 26.1.2; документация на vazkiimods.github.io/Patchouli), Modonomicon (klikli-dev).
- **Вложение данных на Fabric:** Cardinal Components API (Ladysnake, MIT, 26.3). У Fabric API теперь есть собственные data attachments (Fabric Docs).
- **Вспомогательные библиотеки мультизагрузчика:** Balm (ARR на Modrinth, Apache-2.0 на GitHub), Puzzles Lib (MPL-2.0), Resourceful Lib (MIT), Moonlight. **Sinytra Connector** и **Forgified Fabric API** (MIT/Apache-2.0, 26.2) запускают моды Fabric на NeoForge, их полезно читать, чтобы понять различия загрузчиков. **KubeJS** (LGPL-3.0, 26.1.2) — JavaScript-скрипты для сборок.
- **Текст и плейсхолдеры для серверов:** Text Placeholder API (Patbox, LGPL-3.0, 26.3).

| Проект | Загрузчик и версия | Лицензия | Чему учит |
|---|---|---|---|
| [Architectury API](https://github.com/architectury/architectury-api) | Fabric и NeoForge, 26.3 | LGPL-3.0 | Абстракция между загрузчиками |
| [Architectury docs](https://docs.architectury.dev/) | Документация | — | Настройка, форк Loom |
| [MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template) | Ветка 26.3 | CC0 | Шаблон мультизагрузчика без зависимости во время выполнения |
| [Cloth Config](https://github.com/shedaniel/cloth-config) | 26.3 | LGPL-3.0 | Экраны конфигурации |
| [YetAnotherConfigLib](https://github.com/isXander/YetAnotherConfigLib) | 26.3 | LGPL-3.0 | Современный UI конфигурации |
| [owo-lib](https://github.com/wisp-forest/owo-lib) | 26.2 | MIT | UI-каркас, конфигурация, помощники сети |
| [Curios API](https://github.com/TheIllusiveC4/Curios) | 26.3 | LGPL-3.0 | Слоты аксессуаров |
| [Accessories](https://github.com/wisp-forest/accessories) | Мультизагрузчик, 1.21.10 | MIT | Слоты аксессуаров для обоих загрузчиков |
| [Trinkets](https://github.com/emilyploszaj/trinkets) | Fabric, 1.21.1 | MIT | Старый API слотов Fabric; `[устарело]` → Accessories или Curios |
| [GeckoLib](https://github.com/bernie-g/geckolib) | 26.3 | MIT | Анимированные сущности и предметы |
| [GeckoLib wiki](https://github.com/bernie-g/geckolib/wiki) | Документация | MIT | Руководство по использованию |
| [Patchouli](https://github.com/VazkiiMods/Patchouli) | 26.1.2 | CC BY-NC-SA 3.0 | Книги-путеводители на данных |
| [Patchouli docs](https://vazkiimods.github.io/Patchouli/) | Документация | — | Формат JSON книги |
| [Cardinal Components API](https://github.com/Ladysnake/Cardinal-Components-API) | Fabric 26.3 | MIT | Привязка данных к сущностям и чанкам (Fabric) |
| [Sinytra Connector](https://github.com/Sinytra/Connector) | NeoForge 26.2 | MIT | Слой трансляции Fabric на NeoForge |
| [KubeJS](https://github.com/KubeJS-Mods/KubeJS) | 26.1.2 | LGPL-3.0 | Скрипты для сборок |
| [Balm](https://github.com/TwelveIterations/Balm) | 26.3 | ARR (Modrinth) | Абстракция мультизагрузчика (Waystones) |
| [Puzzles Lib](https://github.com/Fuzss/puzzles-lib) | 26.3 | MPL-2.0 | Помощники мультизагрузчика |
| [Fabric Language Kotlin](https://github.com/FabricMC/fabric-language-kotlin) | Fabric | Apache-2.0 | Адаптер Kotlin для Fabric |

### Серверные моды и плагины

- **Polymer** (Patbox) позволяет **серверному** моду Fabric добавлять блоки, предметы и сущности, которые видят ванильные клиенты (виртуальные блоки и генерация ресурспака). Документация на polymer.pb4.eu. Примеры на нём: **PolyFactory** (технологии), **PolyDecorations** (декор), **Get Off My Lawn ReServed** (приваты), **Styled Chat** и Text Placeholder API. **Plasmid** (каркас мини-игр) и **Fantasy** (динамические измерения) от Nucleoid — серверные библиотеки.
- **Fabric Carpet** (gnembon) — технические серверные инструменты, скрипты scarpet. **Ledger** (QuiltServerTools) — журнал блоков (Kotlin).
- **Кроссплатформенный серверный код:** LuckPerms (Bukkit, Paper, Fabric, NeoForge, Velocity), WorldEdit (GPL-3.0, 7.4.x), Chunky, spark, Simple Voice Chat (ARR), Geyser (мост для Bedrock), ViaVersion. Они показывают, как делить одно ядро между платформами: аналог мультизагрузчика в мире плагинов.
- **Плагины Paper:** EssentialsX (GPL-3.0, на Modrinth максимум 26.1.2), CoreProtect (Artistic-2.0, 26.2), PlaceholderAPI (GPL-3.0, 26.3). Модуль `test-plugin` в репозитории Paper показывает актуальное использование API. Поддержка Folia требует планировщиков с учётом регионов.

| Проект | Загрузчик и версия | Лицензия | Чему учит |
|---|---|---|---|
| [Polymer](https://github.com/Patbox/polymer) | Fabric 26.3 | LGPL-3.0 | Серверный контент для ванильных клиентов |
| [Polymer docs](https://polymer.pb4.eu/) | Документация | — | Как работают виртуальные блоки и предметы |
| [PolyFactory](https://github.com/Patbox/PolyFactory) | Fabric 26.3 | LGPL-3.0 | Серверный tech-мод |
| [PolyDecorations](https://github.com/Patbox/PolyDecorations) | Fabric 26.3 | LGPL-3.0 | Серверный декор |
| [Get Off My Lawn ReServed](https://github.com/Patbox/get-off-my-lawn-reserved) | Fabric 26.3 | MIT | Приваты, только сервер |
| [Styled Chat](https://github.com/Patbox/StyledChat) | Fabric 26.3 | LGPL-3.0 | Форматирование чата |
| [Text Placeholder API](https://github.com/Patbox/TextPlaceholderAPI) | Fabric 26.3 | LGPL-3.0 | Плейсхолдеры для серверов Fabric |
| [Plasmid](https://github.com/NucleoidMC/plasmid) | Fabric 26.3 | LGPL-3.0 | Каркас мини-игр |
| [Fantasy](https://github.com/NucleoidMC/fantasy) | Fabric | LGPL-3.0 | Динамические измерения |
| [Fabric Carpet](https://github.com/gnembon/fabric-carpet) | Fabric, снапшоты 26.3 | MIT | Технические серверные инструменты |
| [Ledger](https://github.com/QuiltServerTools/Ledger) | Fabric 26.3, Kotlin | LGPL-3.0 | Журнал блоков |
| [LuckPerms](https://github.com/LuckPerms/LuckPerms) | Bukkit, Paper, Fabric, NeoForge, Velocity | MIT | Одно ядро, много платформ |
| [WorldEdit](https://github.com/EngineHub/WorldEdit) | 7.4.x, много платформ | GPL-3.0 | Адаптеры платформ |
| [Chunky](https://github.com/pop4959/Chunky) | 26.3, много платформ | GPL-3.0 | Предгенерация, много платформ |
| [Geyser](https://github.com/GeyserMC/Geyser) | 26.3 | MIT | Трансляция протокола |
| [ViaVersion](https://github.com/ViaVersion/ViaVersion) | Много платформ | GPL-3.0 | Мост между версиями протокола |
| [EssentialsX](https://github.com/EssentialsX/Essentials) | Paper, до 26.1.2 | GPL-3.0 | Большой классический плагин |
| [CoreProtect](https://github.com/PlayPro/CoreProtect) | Paper, 26.2 | Artistic-2.0 | Плагин журнала и отката |
| [PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI) | Paper, 26.3 | GPL-3.0 | API расширений плагинов |
| [Simple Voice Chat](https://github.com/henkelmax/simple-voice-chat) | Мод и плагин, 26.3 | ARR | Гибрид мода и плагина, аудио по UDP |

### Малые примеры мод-проектов

Небольшие кодовые базы, которые целиком читаются за один присест. Ссылки на них даны выше и в [разделе 3](#3-шаблоны-тестовые-моды-и-инструменты-сборки).

| Проект | Загрузчик и версия | Лицензия | Чему учит |
|---|---|---|---|
| AppleSkin | 26.3 | Unlicense | HUD на клиенте и пакет синхронизации данных о еде |
| Clumps | Мультизагрузчик, 26.3 | MIT | Минимальный мультизагрузчик-проект |
| Adorn | Мультизагрузчик, 26.2 | MIT | Небольшой мод декора |
| Powah! | 26.1.2 | LGPL-3.0 | Энергия, блок-сущности, читаемый код |
| fabric-example-mod, MDK-26.3-ModDevGradle, TurtyWurty 26.1-TutorialMod | Fabric, NeoForge, мультизагрузчик | CC0 | Минимальные проекты на каждую версию |

### Инструменты модпаков, скрипты и внутриигровые путеводители

- Модпаки — отдельное ремесло: закрепление версий, переопределения конфигураций, квесты, скрипты рецептов, серверные паки. В документации NeoForge есть раздел **Modpack Development**.
- **KubeJS** (LGPL-3.0, Modrinth 26.1.2; репозиторий переехал в `kube-mods/kubejs`, ветка по умолчанию `2601`) — JavaScript для рецептов и событий. **CraftTweaker** (MIT, 1.21.1; документация docs.blamejared.com) — ZenScript. **FTB Quests** (FTBTeam, активен 2026-11, лицензия на GitHub не объявлена) — книги квестов.
- **packwiz** (MIT, push 2026-10) — сборка как код (индекс TOML, экспорт в Modrinth и CurseForge). **Prism Launcher** (GPL-3.0) — открытый лаунчер, которым разработчики пользуются для тестов в нескольких экземплярах.
- **Modonomicon** (klikli-dev, MIT и CC-BY-4.0, 26.3) и Patchouli — книги-путеводители на данных. **Waystones** (BlayTheNinth, ARR, 26.3) — небольшой популярный мод на Balm, читаемый пример законченного мультизагрузчик-мода.

| Проект | Загрузчик и версия | Лицензия | Чему учит |
|---|---|---|---|
| [NeoForge — Modpack Development docs](https://docs.neoforged.net/modpack/docs/) | Документация | — | Справочник сборщика |
| [KubeJS](https://github.com/KubeJS-Mods/KubeJS) | 26.1.2 (репозиторий перенаправляет на kube-mods/kubejs) | LGPL-3.0 | Слой скриптов |
| [KubeJS website](https://kubejs.com/) | Документация | — | Документация и вики |
| [CraftTweaker](https://github.com/CraftTweaker/CraftTweaker) | 1.21.1 | MIT | Правка рецептов через ZenScript |
| [CraftTweaker docs](https://docs.blamejared.com/) | Документация | — | Справочник API скриптов |
| [FTB Quests](https://github.com/FTBTeam/FTB-Quests) | Активен 2026-11 | Не объявлена | Система квестов |
| [packwiz](https://github.com/packwiz/packwiz) | Инструмент | MIT | Сборка как код |
| [packwiz docs](https://packwiz.infra.link/) | Документация | — | Использование |
| [Prism Launcher](https://github.com/PrismLauncher/PrismLauncher) | Лаунчер | GPL-3.0 | Тесты в нескольких экземплярах |
| [Modonomicon](https://github.com/klikli-dev/modonomicon) | 26.3 | MIT и CC-BY-4.0 | Библиотека книг-путеводителей |
| [Waystones](https://github.com/TwelveIterations/Waystones) | 26.3 | ARR | Мультизагрузчик-мод на Balm |

### Быстрый выбор: «хочу изучить X — читай Y»

| Тема | Пример для Fabric | Пример для NeoForge | Другое |
|---|---|---|---|
| Регистрация, предметы, блоки | Kaupenjoe Fabric-Tutorial-26.X, ветки 1–22 | Kaupenjoe NeoForge-Tutorial-26.X; Farmer's Delight | AppleSkin (маленький) |
| Блок-сущность, меню, экран | Tech Reborn, Adorn | Farmer's Delight (котёл), Powah! | — |
| Свой тип рецепта | Fabric Docs «Custom recipe types» | Farmer's Delight (разделочная доска) | генератор рецептов misode |
| Компоненты данных | Fabric Docs «Data components» | Документация NeoForge «Items» | minecraft.wiki Data component format |
| Сущности и анимация | Вики GeckoLib, Oritech | Ars Nouveau, Twilight Forest | Blockbench |
| Worldgen (JSON) | Tectonic, Lithostitched | TerraBlender и Biomes O' Plenty | Terralith (датапак) |
| Сеть и синхронизация | AppleSkin, owo-lib | AE2, Refined Storage 2 | Тестовые моды Fabric и NeoForge |
| Интеграция с просмотрщиком рецептов | API плагинов REI и EMI | API плагинов JEI | — |
| Экраны конфигурации | Mod Menu и YACL или Cloth Config | NeoForge `ModConfigSpec`, Cloth Config | конфигурация owo-lib |
| Миксины (продвинутые) | Lithium, FerriteCore, C2ME | ModernFix | вики MixinExtras |
| Рендеринг | Sodium (только чтение), Iris, EMF | Flywheel, Veil | Праймеры 1.21.6–26.2 |
| Мультизагрузчик | MultiLoader-Template, Architectury | Moonlight, Balm, Puzzles Lib | TurtyWurty 26.1-TutorialMod |
| Только сервер | Polymer, PolyFactory, Plasmid | — | Paper: серия Kody Simpson, CoreProtect |
| Кроссплатформенное ядро | LuckPerms, WorldEdit, spark | (то же) | Geyser, ViaVersion |
| Модпаки | packwiz, KubeJS | KubeJS, CraftTweaker, FTB Quests | Prism Launcher |

## Книги

**Ключевые выводы**

- **Все печатные книги по моддингу Minecraft вышли в 2013–2016 годах и устарели для кода.** Они нацелены на Forge 1.7/1.8, Bukkit, Spigot 1.8 или CanaryMod. Ни одна не описывает Fabric, NeoForge, компоненты данных, имена Mojang или API Paper после форка. Их единственная оставшаяся ценность: учить основам Java через игру. Источник: поиск по каталогам издателей и библиотек.
- Bukkit был закрыт по DMCA в сентябре 2014 года. Две книги переписывали по ходу работы: Энди Хант перешёл на CanaryMod, Гупты перешли с Bukkit на Forge. Сам CanaryMod давно мёртв.
- **Чего не существует (на 2026-11), не выдумывайте и не цитируйте:**
  - книги O'Reilly, Packt, Manning или No Starch по Fabric или NeoForge;
  - книги вида «Minecraft Modding in Action» или «Effective Modding»;
  - официального руководства или книги Mojang по моддингу;
  - печатной книги по плагинам Paper после жёсткого форка 2024 года;
  - русскоязычной печатной книги по современному моддингу Minecraft (не найдена).
  Отсутствие нельзя доказать полностью `[не проверено]`.
- Ближайшие заменители — бесплатные онлайн-руководства: Fabric Docs, документация NeoForge с праймерами и «NeoForge 26.1 Development in Action» (MindScrath).
- Книги по Java из общей библиотеки навыка (*Effective Java*, *Modern Java in Action*) полезнее любой книги по Minecraft. Минекрафтовые навыки приходят из документации и эталонных модов. См. [books.md](books.md).

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Minecraft Modding with Forge: A Family-Friendly Guide to Building Fun Mods in Java](https://www.oreilly.com/library/view/minecraft-modding-with/9781491918883/) | Arun Gupta, Aditya Gupta; O'Reilly, 2015-04 | Введение для детей и родителей, Forge и Eclipse; `[устарело]` → документация Fabric/NeoForge |
| [Sams Teach Yourself Mod Development for Minecraft in 24 Hours, 2nd ed.](https://www.oreilly.com/library/view/sams-teach-yourself/9780134389783/) | Jimmy Koene; Sams, 2016 (1-е изд. 2015) | Основы Forge 1.8 `[не проверено]` `[устарело]` |
| [Absolute Beginner's Guide to Minecraft Mods Programming, 2nd ed.](https://www.oreilly.com/library/view/absolute-beginners-guide/9780134277721/) | Rogers Cadenhead; Que, 2015–2016 | Серверные моды, NetBeans `[не проверено]` `[устарело]` |
| [Learn to Program with Minecraft Plugins, 2nd ed. (CanaryMod)](https://www.oreilly.com/library/view/-/9781680500523/) | Andy Hunt; Pragmatic Bookshelf, 2014 | Основы Java через плагины; CanaryMod мёртв; `[устарело]` → документация Paper |
| [Replacing Bukkit with CanaryMod (author note)](https://toolshed.com/2014/10/replacing-bukkit-with-canarymod-for-minecraft.html) | Andy Hunt, 2014 | История влияния DMCA на Bukkit `[не проверено]` |
| [Building Minecraft Server Modifications, 2nd ed.](https://www.packtpub.com/product/building-minecraft-server-modifications/9781785883026) | Cody M. Sommer; Packt, 2015-12 (1-е изд. 2013) | Плагины Spigot `[не проверено]` `[устарело]` → документация Paper |

## Русскоязычные ресурсы

**Ключевые выводы**

- **У Fabric Docs есть официальный русский перевод** (`/ru_ru/develop/`, заголовок «Руководства для разработчиков 26.2») с тем же селектором версий. Перевод **смешанный**: навигация в основном русская, но многие новые страницы остались английскими («Custom Fuel», «Custom Compostable», «Dynamic Registries», «Saved Data», «Fabric API DSL»). Старые русские страницы для 1.20.4 и 1.21.x описывают Yarn, проверяйте заголовок версии. Есть и украинский перевод (`uk_ua`).
- **Fabric Wiki на русском** (`wiki.fabricmc.net/ru:start`) — наследие, туториалы эпохи 1.14–1.15; годится только для концепций.
- **Документации NeoForge, Forge и Paper русского перевода не имеют** (`docs.neoforged.net/ru/…` отдаёт 404; документация Paper только на английском `[не проверено]`).
- **Русская Minecraft Wiki** (ru.minecraft.wiki) описывает механики ванили. Технические страницы (компоненты данных, протокол) заметно менее полны, чем в английской вики. Форматы читайте по-английски.
- **Форум MC Modding (forum.mcmodding.ru)** — главный русскоязычный форум по моддингу (XenForo; около 21,5 тыс. тем, около 175 тыс. сообщений, около 16,4 тыс. пользователей). Активен в 2026 (последние сообщения 24–31 октября 2026, например про перекомпиляцию декомпилированной 1.21.11). Разделы: вопросы по моддингу, **вопросы по MCreator**, туториалы, сниппеты, готовые моды, инструменты, коммерческий раздел (отзывы об исполнителях). Разделов по загрузчикам нет.
- **Habr:** статей немного, но полезные. «Создаём первый Minecraft мод и подробно разбираем Mixin. Просто и понятно» (Johnny_Depp, 2025-11-28; Fabric 1.20.1, JDK 21, Yarn; части про Mixin остаются верными). «Как я разрабатывал плагин для синхронизации погоды на Minecraft-сервере и в Санкт-Петербурге» (Владимир Туров, блог Selectel, 2025-11-10; Paper 1.21.5, Maven, Paper Command API, асинхронный HTTP).
- **Modded Minecraft Wiki** имеет интерфейс `/ru`. Статья Mojang об обфускации имеет локаль ru-RU `[не проверено]`.
- **Русскоязычные видеоуроки по Fabric, NeoForge и Paper проверить не удалось.** Поиск вернул только английские серии и сайты скачивания модов (minecraft-inside.ru, ru-minecraft.ru), которые рассчитаны на игроков, а не на разработчиков. Рекомендуется английское видео Kaupenjoe вместе с русской документацией Fabric `[не проверено]`.
- RuBukkit (исторический русский форум Bukkit): `rubukkit.org` отвечает 403, статус неизвестен `[не проверено]`.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Документация Fabric — Руководства для разработчиков](https://docs.fabricmc.net/ru_ru/develop/) | FabricMC и переводчики, 26.2 | Официальная русская документация |
| [Fabric Docs (ru) — porting](https://docs.fabricmc.net/ru_ru/develop/porting/) | FabricMC | Руководство по портированию в русском интерфейсе |
| [Fabric Docs (ru) 1.21.1 archive](https://docs.fabricmc.net/ru_ru/1.21.1/) | FabricMC | Для модов 1.21.1 (Yarn) |
| [Fabric Wiki — русская версия](https://wiki.fabricmc.net/ru:start) | Сообщество | Старые русские туториалы |
| [Fabric Wiki (ru) — введение](https://wiki.fabricmc.net/ru:tutorial:introduction) | Сообщество | Введение в концепции |
| [MC Modding Forum](https://forum.mcmodding.ru/) | Сообщество (RU), активен 2026-10 | Вопросы и ответы, MCreator, туториалы |
| [mcmodding.ru](https://mcmodding.ru/) | Сообщество (RU) | Портал форума |
| [Создаём первый Minecraft мод и подробно разбираем Mixin](https://habr.com/ru/articles/971448/) | Johnny_Depp, Habr, 2025-11-28 | Основы Mixin по-русски (Fabric 1.20.1) |
| [Плагин для синхронизации погоды (Paper)](https://habr.com/ru/post/958526) | Владимир Туров, Selectel/Habr, 2025-11-10 | Разбор плагина Paper по-русски |
| [Русская Minecraft Wiki](https://ru.minecraft.wiki/) | Сообщество | Механики ванили по-русски |
| [Modded Minecraft Wiki (ru)](https://moddedmc.wiki/ru) | Sinytra | Документация модов, русский интерфейс |
| [Удаление обфускации в Java Edition (ru-RU)](https://www.minecraft.net/ru-ru/article/removing-obfuscation-in-java-edition) | Mojang, 2025 | Официальный анонс по-русски `[не проверено]` |

## Маршрут обучения

Маршрут не привязан к загрузчику: на каждом уровне названы путь Fabric, путь NeoForge и по одному эталонному моду из **разных** жанров, чтобы ученик не знал только один вид модов. Целевая версия в конце 2026: **26.3** (Fabric или NeoForge). Версия для поддержки: **1.21.1** (NeoForge). У плагинов Paper параллельный трек (уровень 7).

### Уровень 0. Предпосылки

- JDK 25 (Microsoft OpenJDK или Temurin), IntelliJ IDEA 2025.3+ (для миксинов Fabric на 26.1+) с плагином Minecraft Development, Git. Уверенное владение generics, лямбдами, records, интерфейсами и основами Gradle. Для основ Java подойдёт `Java-Introduction-2026` Kaupenjoe или любой общий курс.
- Проект создавайте генератором Fabric или NeoForge. Проверенные в CI сборочные файлы (Fabric: `net.fabricmc.fabric-loom` без `mappings`, `modImplementation` и `remapJar`; NeoForge: ModDevGradle) приведены в [разделе 3](#3-шаблоны-тестовые-моды-и-инструменты-сборки). Fabric 26.3 просит Loom 1.17+, на maven.fabricmc.net новейший выпуск 1.19.4 (2026-11-07).

### Уровень 1. Первый мод (предметы, блоки, вкладка творчества, ресурсы)

- Учить: регистрация (Fabric `Registry.register` или NeoForge `DeferredRegister`), модели предметов и блоков, JSON состояний блоков, файлы lang, записи вкладки творчества, `runClient` и `runData`.
- Читать: Fabric Docs «Getting Started», «Creating your first item/block»; документацию NeoForge «Registries», «Items», «Blocks»; Kaupenjoe 26.X, ветки `1-setup` … `10-food`.
- Эталонный мод: **AppleSkin** (крошечный клиентский HUD) и **Clumps** (крошечный мультизагрузчик): читаются целиком за один присест.
- Проверка выхода: мод с одним блоком, одним предметом, одним рецептом, моделями и добычей из датагена, сборка проходит в CI.

### Уровень 2. Контентный мод (блок-сущности, меню, рецепты, сущности, worldgen)

- Учить: блок-сущности и тикеры, меню и экраны, свои типы рецептов, теги, модификаторы добычи, **компоненты данных** (не NBT), сущности и рендереры (GeckoLib по желанию), простой worldgen через датапаки.
- Читать: Fabric Docs «Block entities», «Container menus», «Custom recipe types», «Data components»; документацию NeoForge «Block Entities», «Inventories & Transfers», «Worldgen»; генераторы misode для JSON.
- Эталонные моды из разных жанров: **Farmer's Delight** (контент, типы рецептов), **Occultism** (магия, ритуалы), **Powah!** (технологии, энергия), **Tectonic и Lithostitched** (worldgen в JSON).
- Ловушка: на 26.3 используйте компоненты `minecraft:cooking_fuel`, `minecraft:compostable`, `minecraft:block_transformer` вместо удалённых реестров.

### Уровень 3. Сеть, данные и конфигурация

- Учить: свои payload (`CustomPacketPayload` и `StreamCodec`), разделение клиента и сервера (`@Environment` и точки входа по стороне), **кодеки** (DataFixerUpper), saved data, data attachments (Fabric Data Attachment API и attachments NeoForge), конфигурации (Cloth Config, YACL, NeoForge `ModConfigSpec`), игровые тесты.
- Читать: Fabric Docs «Networking», «Codecs», «Data attachments», «Automatic testing»; документацию NeoForge «Networking», «Data Storage»; пост NeoForge «Mitigating Network Vulnerabilities» (2026-05) о проверке ввода клиента.
- Эталон: **AppleSkin** (пакет синхронизации еды), **JEI** (API плагинов для разных загрузчиков), **AE2** (игровые тесты), **Refined Storage 2** (чисто Java-ядро с модульными тестами).
- Официальные тестовые моды (Fabric `src/testmod`, NeoForge `tests/`) служат справочником API последней инстанции.

### Уровень 4. Миксины и трансформация доступа

- Учить: когда миксин не нужен (сначала события и API), `@Inject`, `@ModifyArg`, MixinExtras `@WrapOperation` и `@ModifyExpressionValue`, аксессоры и инвокеры, access wideners и class tweakers (Fabric), access transformers (NeoForge), interface injection, расширение enum (Fabric 26.2+).
- Читать: Fabric Docs «Mixins» и «Class Tweakers», вики Mixin, вики MixinExtras, шпаргалку по миксинам; на русском: статья на Habr (1.20.1, только концепции).
- Эталон: **Lithium** (много мелких отключаемых миксинов с конфигурацией), **FerriteCore** (узкий, документированный), **Continuity** (хуки в конвейер моделей).
- Ловушка: строки целей миксинов инструменты миграции имён не переименовывают. После каждого дропа запускайте игру с экспортом отладки миксинов и чините упавшие внедрения.

### Уровень 5. Мультизагрузчик, несколько версий и публикация

- Учить: разделение `common`/`fabric`/`neoforge` (MultiLoader-Template) или Architectury; платформенные сервисы через `ServiceLoader`; необязательные бэкпорты на Forge и 1.21.1; лицензии (MIT/LGPL и собственные); списки изменений; публикация на Modrinth, CurseForge, Hangar; CI на GitHub Actions.
- Читать: README MultiLoader-Template, TurtyWurty `26.1-TutorialMod`, документацию Architectury, документацию mod-publish-plugin, правила контента Modrinth.
- Эталон: **JEI** (common и модули загрузчиков), **Supplementaries и Moonlight** (мультизагрузчик на библиотеке), **LuckPerms и WorldEdit** (одно ядро, много платформ), **Sinytra Connector** (чем различаются загрузчики).
- Ловушка: на Fabric 26.x публикуйте `jar`, а не `remapJar`. Id зависимости Fabric API — `fabric-api`, не `fabric`.

### Уровень 6. Производительность и долгая поддержка

- Учить: профилирование через spark и JFR, бюджеты аллокаций и времени тика, основы конвейеров чанков, освещения и рендеринга, чтение праймеров на каждом дропе (примерно раз в три месяца в 2026: 26.1 в марте, 26.2 в июне, 26.3 в сентябре), поддержка ветки 1.21.1, работа с устареванием API. Подробно о профилировании: [minecraft-performance-testing.md](minecraft-performance-testing.md).
- Читать: праймеры NeoForge по порядку; записи «Fabric for Minecraft X»; код Lithium, ModernFix, C2ME, ScalableLux; Sodium и Iris для рендеринга (Sodium только для чтения из-за лицензии).
- Эталон: **ModernFix** (запуск и память), **C2ME** (параллелизм), **ImmediatelyFast** (батчинг рендеринга), **Krypton** (сеть).

### Уровень 7. Параллельный трек: плагины Paper и серверные моды

- Paper: документация «Project setup» → Event API → Command API (Brigadier) → Component API (Adventure и MiniMessage) → PDC → планировщик (и поддержка Folia) → Data component API → реестры → Lifecycle. paperweight-userdev нужен только если в API чего-то не хватает. Видео: Kody Simpson «Make a Minecraft Plugin in 2026». Эталон: `test-plugin` Paper, CoreProtect, EssentialsX, PlaceholderAPI, LuckPerms. Детали Paper и Folia: [minecraft-server-plugins.md](minecraft-server-plugins.md).
- Серверный Fabric (ванильные клиенты): документация Polymer → PolyDecorations → PolyFactory; Text Placeholder API; Plasmid для мини-игр.

| Ресурс | Автор, год | Зачем читать |
|---|---|---|
| [Fabric Docs — Creating a project](https://docs.fabricmc.net/develop/getting-started/creating-a-project) | FabricMC, 2026 | Старт уровня 1 |
| [Fabric Docs — Networking](https://docs.fabricmc.net/develop/networking) | FabricMC, 2026 | Уровень 3 |
| [NeoForge Docs — Networking](https://docs.neoforged.net/docs/networking/) | NeoForged, 2026 | Уровень 3 |
| [Mitigating Network Vulnerabilities](https://neoforged.net/news/mitigating-vulnerabilities-network/) | NeoForged, 2026-05-11 | Проверка пакетов клиента |
| [Paper — Project setup](https://docs.papermc.io/paper/dev/project-setup) | PaperMC | Старт трека плагинов |
| [Paper — Command API](https://docs.papermc.io/paper/dev/api/command-api) | PaperMC | Команды Brigadier |
| [Paper — Supporting Paper and Folia](https://docs.papermc.io/paper/dev/folia-support) | PaperMC | Планирование с учётом регионов |
| [NeoForge primer source on GitHub](https://github.com/neoforged/.github/tree/main/primers) | NeoForged | Исходные праймеры (Markdown) |

## Что не проверено

- **Статистика CurseForge** (доля загрузчиков, принятие версий, топ модов) не собрана: API CurseForge требует ключ, сайт отвечал 403. Числа раздела 5 — только Modrinth, они завышают долю Fabric.
- **Русские видеоуроки** по Fabric, NeoForge, Paper (YouTube, VK, Rutube) не найдены и не проверены. Подтверждены только статьи Habr, русские Fabric Docs и вики, форум mcmodding.ru. Статус RuBukkit неизвестен (403).
- **Статьи minecraft.net** (анонс об обфускации, журналы изменений 26.x) не открывались с проверяющей машины. Содержание взято из сниппетов поиска и Minecraft Wiki. Точная дата публикации статьи об обфускации не прочитана.
- **Тред о жёстком форке Paper на forums.papermc.io** не открывался (ответ 522), поэтому в таблице ссылка на новость papermc.io/news/the-future-of-paper-hard-fork. Дата анонса, 2024-12-13, начало действия с 1.21.4 и совет собирать плагины против Paper-API прочитаны в ней.
- **Год 2-го издания Sams «Mod Development in 24 Hours»** (2016?) и **2-го издания Cadenhead** (год, Bukkit или Spigot) взяты из каталожных сниппетов, не со страниц издателей.
- **Раздел 26.2 вики McJty** и **видеопокрытие 26.x у TurtyWurty** известны из вторичных источников (интервью, описания репозиториев). Плейлисты не открывались.
- **Mekanism на 26.x:** ветка по умолчанию на GitHub нацелена на 26.3, но старший выпуск на Modrinth 1.21.1. Неясно, выпущены ли сборки 26.x (возможно, только CurseForge или не выпущены).
- **Исходники Terralith:** ветка по умолчанию `1.20` (2025-02), а выпуски 26.3. Публичный репозиторий может не отражать текущий код.
- **Отставание Fabric Docs:** документация по умолчанию 26.2, а текущая версия 26.3. Примеры датагена worldgen для 26.3 обещали «вскоре после релиза», не проверены.
- **Перекрёстные числа «загрузчик × версия» Modrinth** приблизительны (фасеты по проекту, не по файлу версии). Числа Forge на 26.x завышены.
- **Лицензии:** GitHub показывает `NOASSERTION` у нескольких репозиториев (собственные лицензии: Create, Botania, Quark, Supplementaries, Twilight Forest, Sodium). Точные условия не читались. Перед повторным использованием кода читайте `LICENSE`.
