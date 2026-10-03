# HQFramework

[![License](https://img.shields.io/badge/license-GPL%20v3-blue.svg?style=flat)](LICENSE)
[![Kotlin](https://img.shields.io/badge/kotlin-2.3-blue.svg?logo=kotlin)](http://kotlinlang.org)

Bukkit(Paper)과 Proxy(BungeeCord, Velocity) 플러그인을 위한 프레임워크입니다. Spring에서 영감을 받은 어노테이션 기반 의존성 주입(Koin), 코루틴, 명령어, 인벤토리 UI, DB 리포지토리, NMS 추상화, 프록시↔백엔드 패킷 통신을 제공합니다.

## Supported by JetBrains
<a href="https://jb.gg/OpenSourceSupport"><img src="https://resources.jetbrains.com/storage/products/company/brand/logos/jetbrains.png" alt="JetBrains Logo (Main) logo." width="400"></a>

## 목차
- [요구사항과 설치](#요구사항과-설치)
- [빠른 시작](#빠른-시작)
- [컴포넌트와 의존성 주입](#컴포넌트와-의존성-주입)
- [핸들러 만들기](#핸들러-만들기)
- [Bukkit 기능](#bukkit-기능)
  - [이벤트 리스너](#이벤트-리스너)
  - [모듈](#모듈)
  - [코루틴](#코루틴)
  - [코루틴 예외 처리](#코루틴-예외-처리)
  - [스케줄러와 Folia](#스케줄러와-folia)
- [YAML 설정](#yaml-설정)
- [데이터베이스](#데이터베이스)
- [Redis 메시징과 임시 저장소](#redis-메시징과-임시-저장소)
- [명령어](#명령어)
- [인벤토리 UI](#인벤토리-ui)
- [리전](#리전)
- [NMS](#nms)
- [Packet I/O (프록시 통신)](#packet-io-프록시-통신)
- [Quartz 스케줄러](#quartz-스케줄러)
- [HQFramework config.yml](#hqframework-configyml)
- [변경 사항](#변경-사항)

---

## 요구사항과 설치

**서버 버전과 Java**

| 서버 | Java | 비고 |
|---|---|---|
| Paper 1.17 ~ 1.20.4 | 17+ | legacy NMS 구현 |
| Paper 1.20.6 ~ 1.21.11 | 21+ | 1.20.5, 1.21.2, 1.21.9/10처럼 명시되지 않은 버전은 가장 가까운 하위 구현으로 동작 |
| Paper 26.1, 26.2 | 25+ | |
| BungeeCord, Velocity 3.x | 17+ | |

공통 모듈은 Java 17 바이트코드로 배포되므로 한 jar로 모든 서버에서 동작합니다. Spigot(비 Paper)은 일부 기능이 제한됩니다.

**Gradle**

```kotlin
repositories {
    maven("https://maven.hqservice.kr/repository/maven-public/")
}

dependencies {
    compileOnly("kr.hqservice:hqframework-bukkit-core:2.3.0-SNAPSHOT")
    compileOnly("kr.hqservice:hqframework-bukkit-database:2.3.0-SNAPSHOT")
    compileOnly("kr.hqservice:hqframework-bukkit-command:2.3.0-SNAPSHOT")
    compileOnly("kr.hqservice:hqframework-bukkit-inventory:2.3.0-SNAPSHOT")
    compileOnly("kr.hqservice:hqframework-bukkit-region:2.3.0-SNAPSHOT")
    compileOnly("kr.hqservice:hqframework-bukkit-nms:2.3.0-SNAPSHOT")
}
```

프록시 플러그인은 `hqframework-proxy-bungee-core` 또는 `hqframework-proxy-velocity-core`를 사용합니다. RedisBungee 다중 프록시 환경은 `hqframework-proxy-multi-core`입니다. 런타임 클래스는 서버에 설치된 HQFramework 플러그인이 제공하므로 `compileOnly`로 충분합니다.

**plugin.yml**

```yaml
depend: [HQFramework]
```

---

## 빠른 시작

```kotlin
package kr.example.myplugin

import kr.hqservice.framework.bukkit.core.HQBukkitPlugin

class MyPlugin : HQBukkitPlugin() {
    override fun onPostEnable() {
        logger.info("ready")
    }
}
```

- 메인 클래스의 패키지(`kr.example.myplugin`)와 그 하위 패키지가 컴포넌트 스캔 범위입니다.
- `onLoad`, `onEnable`, `onDisable`은 final입니다. 대신 `onPreLoad`, `onPostLoad`, `onPreEnable`, `onPostEnable`, `onPreDisable`, `onPostDisable`을 오버라이드합니다.
- enable 순서: `onPreEnable` → jar의 `config.yml`을 dataFolder로 복사(이미 있고 `config-version`이 다르면 누락 키만 병합) → 컴포넌트 스캔과 생성 → `onPostEnable`.
- disable 순서: `onPreDisable` → 실행 중인 코루틴 정리(5초 유예 후 취소) → 컴포넌트 teardown → `onPostDisable`.
- `getHQConfig()`로 플러그인 `config.yml`을 `HQYamlConfiguration`으로 읽습니다.

---

## 컴포넌트와 의존성 주입

스캔 범위 안의 클래스에 어노테이션을 붙이면 생성자 주입으로 인스턴스가 만들어지고 Koin에 등록됩니다. 의존 관계 순서는 프레임워크가 정리합니다.

```kotlin
interface PointService {
    fun add(player: Player, amount: Long)
}

@Service
class PointServiceImpl(private val repository: PointRepository) : PointService {
    override fun add(player: Player, amount: Long) {
        repository.update(player.uniqueId) { it.point += amount }
    }
}

@Listener
class JoinListener(private val pointService: PointService, private val plugin: Plugin) {
    @Subscribe
    fun onJoin(event: PlayerJoinEvent) {
        pointService.add(event.player, 10)
        event.player.sendMessage("${plugin.name}에 오신 것을 환영합니다")
    }
}
```

### 어노테이션

패키지는 `kr.hqservice.framework.global.core.component`입니다.

| 어노테이션 | 대상 | 동작 |
|---|---|---|
| `@Component` | 클래스 | enable 시 즉시 생성. 자기 자신과 모든 상위 타입으로 싱글턴 등록. `HQComponent`를 구현하면 컴포넌트 핸들러가 setup/teardown을 호출 |
| `@Bean`, `@Service` | 클래스, 함수 | 지연 생성 싱글턴. 처음 주입될 때 만들어짐 (`@Service`는 `@Bean`의 별칭) |
| `@Singleton(binds = [...])` | 클래스, 함수 | 싱글턴. `binds`를 지정하면 그 타입으로만 등록 |
| `@Factory(binds = [...])` | 클래스, 함수 | 주입할 때마다 새 인스턴스 |
| `@Configuration` | 클래스 | 안에 선언된 `@Bean`/`@Singleton`/`@Factory` 함수를 빈 팩토리로 등록. 함수 파라미터도 주입됨 |
| `@Primary` | 클래스 | 같은 타입의 빈이 여럿일 때 우선 선택 |
| `@Qualifier("name")` | 클래스, 함수, 파라미터 | 같은 타입의 구현체를 이름으로 구분. 값이 `#`으로 시작하면 플러그인 config의 해당 키 값을 이름으로 사용 |
| `@MutableNamed(key)` + `@QualifierProvider(key)` | 파라미터 / 클래스 | 이름을 런타임 로직으로 결정 |
| `@PluginDepend(plugins = [...])` | 클래스 | 지정한 플러그인이 하나라도 없으면 스캔에서 제외 |
| `@Scannable` | 어노테이션 | 사용자 정의 어노테이션을 스캔 대상으로 만듦. [핸들러 만들기](#핸들러-만들기) 참고 |

`@Component`는 즉시 생성, `@Bean`/`@Service`는 지연 생성입니다. 리스너, 모듈, 리포지토리처럼 "존재 자체가 동작"인 것은 `@Component` 계열, 서비스처럼 "누군가 필요로 할 때 있으면 되는 것"은 `@Service`가 맞습니다.

### 자동으로 주입되는 인스턴스

생성자 파라미터 타입이 아래와 정확히 일치하면 플러그인 범위의 인스턴스가 들어갑니다.

| 타입 | 값 |
|---|---|
| `org.bukkit.plugin.Plugin`, `HQBukkitPlugin`, 플러그인 메인 클래스 | 플러그인 자신 |
| `java.util.logging.Logger` | `plugin.logger` |
| `HQYamlConfiguration` | `plugin.getHQConfig()` |
| `ConfigurationSection` | `plugin.config` (Bukkit) |
| `CoroutineScope` | 플러그인 코루틴 스코프 |

HQFramework가 전역으로 등록해 두는 빈도 주입할 수 있습니다. `Server`, `PluginManager`, `ServicesManager`, `Json`(kotlinx.serialization), `org.quartz.Scheduler`, `NettyServer`, `PacketSender`, `Navigator`, `RangeFactory`, NMS 서비스들이 여기에 해당합니다.

### Qualifier

```kotlin
interface StorageService { fun name(): String }

@Service
@Qualifier("mysql")
class MySQLStorage : StorageService { override fun name() = "mysql" }

@Service
@Qualifier("file")
class FileStorage : StorageService { override fun name() = "file" }

@Component
class StorageUser(@Qualifier("#storage.type") private val storage: StorageService) : HQSimpleComponent
```

`#storage.type`은 플러그인 `config.yml`의 `storage.type` 값을 이름으로 씁니다. 더 복잡한 로직이 필요하면 `@MutableNamed`를 씁니다.

```kotlin
@QualifierProvider(key = "myplugin.storage")
class StorageQualifierProvider(private val config: HQYamlConfiguration) : MutableNamedProvider {
    override fun provideQualifier(): String {
        return if (config.getBoolean("storage.remote", false)) "mysql" else "file"
    }
}

@Component
class StorageUser(@MutableNamed(key = "myplugin.storage") private val storage: StorageService) : HQSimpleComponent
```

### Configuration과 Primary

```kotlin
@Configuration
class MyConfig {
    @Bean
    fun provideGson(): Gson = GsonBuilder().setPrettyPrinting().create()

    @Singleton
    @Qualifier("cache")
    fun provideCacheExecutor(): ExecutorService = Executors.newFixedThreadPool(2)
}

@Component
@Primary
class DefaultStorage : StorageService { override fun name() = "default" }
```

### 알아둘 것

- 생성자는 하나여야 합니다. nullable이거나 기본값이 있는 파라미터는 해당 빈이 끝내 없을 때만 null/기본값이 됩니다.
- 의존성을 끝내 찾지 못하면 enable이 `NoBeanDefinitionsFoundException`으로 실패하고, 어느 클래스의 어느 파라미터가 문제인지 콘솔에 색으로 표시됩니다.
- `HQComponent`를 구현한 클래스에 `@Listener`, `@Module` 같은 스캔 어노테이션을 함께 붙이면 그 어노테이션은 처리되지 않습니다. 하나만 쓰세요.
- `ComponentRegistry`를 주입받으면 HQFramework 자신의 레지스트리가 들어옵니다. 자기 플러그인 것은 `plugin.getComponentRegistry()`로 얻습니다.
- 다른 플러그인이 정의한 핸들러는 공유되지 않습니다. HQFramework가 제공하는 핸들러만 모든 플러그인에 적용됩니다.

---

## 핸들러 만들기

### 컴포넌트 핸들러

`HQComponent`를 구현한 `@Component`들에 공통 setup/teardown 로직을 붙입니다.

```kotlin
interface Ticker : HQComponent {
    fun tick()
}

@Component
class ScoreboardTicker : Ticker {
    override fun tick() { }
}

@ComponentHandler
class TickerHandler(private val plugin: HQBukkitPlugin) : HQComponentHandler<Ticker> {
    private val tasks = mutableMapOf<Ticker, HQTask>()

    override fun setup(element: Ticker) {
        tasks[element] = plugin.getScheduler().runTaskTimer(1, 1) { element.tick() }
    }

    override fun teardown(element: Ticker) {
        tasks.remove(element)?.cancel()
    }
}
```

`@ComponentHandler(depends = [OtherHandler::class])`로 순서를 보장할 수 있습니다. `depends`에 적힌 핸들러의 setup이 끝난 뒤에 setup되고, teardown은 그 반대 순서입니다.

### 어노테이션 핸들러

`@Scannable`을 붙인 어노테이션을 정의하면, 그 어노테이션이 달린 클래스가 생성되어 `HQAnnotationHandler`로 전달됩니다. 프레임워크의 `@Listener`, `@Module`, `@Command`, `@Table`이 모두 이 방식으로 구현되어 있습니다.

```kotlin
@Scannable
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Placeholder(val identifier: String)

@AnnotationHandler
class PlaceholderHandler(private val plugin: HQBukkitPlugin) : HQAnnotationHandler<Placeholder> {
    override fun setup(instance: Any, annotation: Placeholder) {
        PlaceholderRegistry.register(annotation.identifier, instance as PlaceholderExpansion)
    }

    override fun teardown(instance: Any, annotation: Placeholder) {
        PlaceholderRegistry.unregister(annotation.identifier)
    }
}

@Placeholder("points")
class PointPlaceholder(private val repository: PointRepository) : PlaceholderExpansion
```

실행 순서는 컴포넌트 생성 → 어노테이션 핸들러 setup → 컴포넌트 핸들러 setup입니다. teardown은 역순입니다.

---

## Bukkit 기능

### 이벤트 리스너

패키지: `kr.hqservice.framework.bukkit.core.listener`

```kotlin
@Listener
class ChatListener(private val repository: PointRepository) {
    @Subscribe(handleOrder = HandleOrder.LATE, ignoreCancelled = true)
    fun onChat(event: AsyncChatEvent) {
        event.isCancelled = repository[event.player.uniqueId] == null
    }

    @Subscribe
    suspend fun onQuit(event: PlayerQuitEvent) {
        val stats = withContext(Dispatchers.BukkitAsync) { loadStats(event.player) }
        event.player.server.broadcastMessage("${event.player.name}: $stats")
    }
}
```

- `HandleOrder`는 `FIRST, EARLY, NORMAL, LATE, LAST, MONITOR`이며 Bukkit의 `EventPriority`에 대응합니다.
- `suspend fun` 핸들러는 이벤트 스레드에서 시작해 첫 suspend 지점까지 동기로 실행되고, 이후에는 메인 스레드에서 재개됩니다. 따라서 `event.isCancelled` 변경은 첫 suspend 전에 해야 반영됩니다.
- Bukkit의 `@EventHandler`는 `@Listener` 클래스 안에서 동작하지 않습니다. `@Subscribe`를 쓰세요.
- 플러그인이 disable되면 리스너는 자동으로 해제됩니다.

### 모듈

enable/disable 시점에 한 번 실행할 로직을 둡니다. 패키지: `kr.hqservice.framework.bukkit.core.component.module`

```kotlin
@Module
class EconomyModule(private val server: Server, private val logger: Logger) {
    @Setup
    fun setup() {
        server.servicesManager.register(Economy::class.java, PointEconomy(), plugin, ServicePriority.Normal)
    }

    @Teardown
    suspend fun teardown() {
        logger.info("economy unregistered")
    }
}
```

`@Setup`/`@Teardown` 함수는 suspend여도 됩니다.

### 코루틴

`HQBukkitPlugin`은 `CoroutineScope`입니다. 기본 디스패처는 메인 스레드(`Dispatchers.BukkitMain`)이고 SupervisorJob이라 자식 하나가 실패해도 다른 자식에 전파되지 않습니다.

```kotlin
plugin.launch {
    val data = withContext(Dispatchers.BukkitAsync) { database.load(uuid) }
    bukkitDelay(20)
    player.sendMessage("loaded: $data")
}
```

| 디스패처 | 스레드 |
|---|---|
| `Dispatchers.BukkitMain` | 메인 스레드. 호출 시점이 메인이라도 다음 틱에 실행 (플러그인 enable/disable 중에는 즉시 실행) |
| `Dispatchers.BukkitAsync` | Bukkit 비동기 스케줄러 |
| `Dispatchers.FoliaRegion(location)` / `FoliaRegionAsync(location)` | Folia 리전 스레드 |

- `delay(ms)`는 틱 단위로 올림되며 최소 1틱입니다. `bukkitDelay(ticks)`도 있습니다.
- `launch(TeardownOptionCoroutineContextElement(true)) { }`로 띄운 코루틴은 플러그인 disable 시 기다리지 않고 즉시 취소됩니다. 그 외 코루틴은 5초까지 완료를 기다립니다.
- 플러그인이 disable된 뒤의 디스패치는 취소 상태로 IO 스레드에서 마무리됩니다. 영원히 멈추지 않습니다.
- 별도 스코프가 필요하면 `HQCoroutineScope`를 상속합니다. teardown 시 자동으로 정리됩니다.

```kotlin
@Component
class WorkerScope(plugin: HQBukkitPlugin) : HQCoroutineScope(plugin, Dispatchers.Default) {
    override fun getCoroutineName() = CoroutineName("Worker")
}
```

플레이어별 직렬 실행이 필요하면 `PlayerScopes`를 씁니다. 같은 UUID의 `launch` 작업은 suspend 중에도 겹치지 않고 호출 순서대로 하나씩 실행되며, 작업이 끝나 비면 스코프가 자동 해제됩니다. 한 작업 안에서 같은 UUID로 `launch`한 작업을 `join`하면 앞 작업이 끝나기를 서로 기다려 교착되므로 하지 마세요. `scope(id)`로 직접 띄운 코루틴은 이 순서 보장 밖입니다.

```kotlin
private val playerScopes = PlayerScopes(plugin, Dispatchers.IO)

playerScopes.launch(player.uniqueId) { save(player) }
playerScopes.awaitIdle(player.uniqueId)
```

### 코루틴 예외 처리

플러그인 스코프에서 던져진 예외는 `@CoroutineScopeAdvice` 클래스의 `@ExceptionHandler` 메서드로 전달됩니다. 처리되지 않은 예외는 콘솔에 요약되고 `hq-errors/` 폴더에 스택트레이스가 저장됩니다.

```kotlin
@CoroutineScopeAdvice(type = AdviceType.PLUGIN)
class MyExceptionAdvice(private val logger: Logger) {
    @ExceptionHandler(priority = 10)
    fun onNotFound(exception: PlayerNotFoundException) {
        logger.warning(exception.message)
    }

    @ExceptionHandler
    @MustBeStored
    fun onAny(exception: Exception) {
        logger.severe("unexpected: ${exception.message}")
    }
}
```

- 핸들러는 파라미터 타입에 대입 가능한 예외(서브클래스 포함)를 받습니다. `priority` 오름차순으로 평가하고 처음 처리한 곳에서 멈춥니다.
- `AdviceType.GLOBAL`은 모든 HQ 플러그인의 코루틴에 적용됩니다. 소유 플러그인이 disable되면 해제됩니다.
- `@MustBeStored`가 붙으면 처리된 뒤에도 스택트레이스를 파일로 남깁니다.

### 스케줄러와 Folia

`plugin.getScheduler()`는 Bukkit과 Folia 어느 쪽에서든 동작하는 `HQScheduler`를 돌려줍니다.

```kotlin
val task = plugin.getScheduler().runTaskTimer(0, 20) { tick() }
plugin.getScheduler(location).runTask { location.block.type = Material.AIR }
task.cancel()
```

`runTaskLater`, `runTaskTimer`, `runTaskAsynchronously`, `runTaskLaterAsynchronously`, `runTaskTimerAsynchronously`가 있고 `HQTask`로 취소할 수 있습니다. `Plugin.getScheduler()` 확장은 HQ 플러그인이 아닌 일반 플러그인에서도 쓸 수 있습니다.

---

## YAML 설정

패키지: `kr.hqservice.framework.yaml`

```kotlin
val config = File(plugin.dataFolder, "data.yml").yaml()

val limit = config.getInt("limit", 10)
val owner = config.findString("owner")
config.getSection("items")?.getKeys()?.forEach { key ->
    val lore = config.getStringList("items.$key.lore")
}
config.set("limit", limit + 1)
config.save(File(plugin.dataFolder, "data.yml"))
config.reload()
```

- 키는 `.`으로 구분한 경로입니다.
- `getX(key, default)`는 키가 없으면 기본값을 돌려주고 파일을 건드리지 않습니다. `findX(key)`는 없으면 null입니다. X는 `String`, `Boolean`, `Int`, `Long`, `Double`, `Float`입니다.
- 리스트는 `getStringList`, `getIntegerList`, `getLongList`, `getDoubleList`, `getFloatList`입니다.
- bukkit-core 확장으로 `getMaterial(path)`, `findMaterial(path)`가 있습니다.

---

## 데이터베이스

모듈 `hqframework-bukkit-database`. Exposed 위에 얹혀 있으며 HQFramework의 `config.yml`에서 설정한 DB(H2, SQLite, MySQL) 하나를 모든 플러그인이 공유합니다.

### 테이블

```kotlin
@Table
object PointTable : org.jetbrains.exposed.sql.Table("points") {
    val owner = uuid("owner")
    val point = long("point").default(0)
    val level = integer("level").default(1)
    val lastLocation = location("last_location").nullable()
    override val primaryKey = PrimaryKey(owner)
}
```

enable 시 테이블이 없으면 생성하고, 있으면 누락된 컬럼과 인덱스를 추가합니다. 컬럼 삭제나 타입 변경은 하지 않습니다. `@Table(withLogs = false)`로 생성 로그를 끌 수 있습니다.

제공 컬럼: `itemStack(name)`(blob), `location(name)`(varchar 255). DAO에서는 `Entity<*>.itemStack(column)`, `Entity<*>.location(column)` 위임을 씁니다.

### PlayerRepository

플레이어별 데이터를 메모리에 두고 접속 시 load, 주기적으로·퇴장 시 save하는 캐시입니다. `@Component`로 등록해야 합니다.

```kotlin
class PointData(var point: Long, var level: Int)

@Component
class PointRepository : PlayerRepository<PointData>(SavePolicy.periodic()) {
    override suspend fun load(player: Player): PointData {
        val row = PointTable.selectAll().where { PointTable.owner eq player.uniqueId }.singleOrNull()
        return PointData(row?.get(PointTable.point) ?: 0L, row?.get(PointTable.level) ?: 1)
    }

    override suspend fun save(player: Player, value: PointData) {
        PointTable.upsert {
            it[owner] = player.uniqueId
            it[point] = value.point
            it[level] = value.level
        }
    }

    override suspend fun loadOffline(uuid: UUID): PointData? {
        val row = PointTable.selectAll().where { PointTable.owner eq uuid }.singleOrNull() ?: return null
        return PointData(row[PointTable.point], row[PointTable.level])
    }

    override fun fingerprint(value: PointData): Any? = value.point to value.level
}
```

- `load`, `save`, `loadOffline`은 이미 IO 트랜잭션 안에서 호출됩니다. Exposed DSL을 그대로 씁니다.
- 생성자 인자 `SavePolicy`:
  - `SavePolicy.periodic(dirtyInterval, fullInterval, batchSize)` (기본값): 바뀐(dirty) 항목은 `dirtyInterval`마다, 전체는 `fullInterval` 동안 나눠서 저장합니다. 인자를 생략하면 `player-data.dirty-flush-seconds`, `player-data.full-flush-seconds`를 따르고, `batchSize`는 `접속자 수 / (full / dirty)`로 자동 계산됩니다.
  - `SavePolicy.onQuitOnly()`: 퇴장, 플러그인 disable, 서버 종료 때만 저장합니다. 소유권 lease 갱신은 계속됩니다.
- `fingerprint(value)` (선택): 전체 저장 주기에서 지문이 바뀐 항목만 저장합니다. 기본값 `null`이면 전체 저장 주기마다 모두 저장합니다.
- `loadOffline(uuid)` (선택): `peek`이 사용합니다. 오버라이드하지 않으면 `peek`은 항상 `null`입니다.
- `saveOffline(uuid, value)` (선택): `writeOffline`이 사용합니다. 오버라이드하지 않으면 `writeOffline`은 `UnsupportedOperationException`을 던집니다.

| 호출 | 동작 |
|---|---|
| `repo[uuid]` | 캐시의 값을 그대로 반환. 이 서버가 소유하지 않으면 `null` |
| `repo[uuid] = value` | 값을 교체하고 dirty 표시. 소유하지 않은 플레이어에는 set이 무시됩니다 |
| `repo.update(uuid) { it.point += 10 }` | 플레이어별 락 안에서 블록을 실행하고 dirty 표시. 호출한 스레드에서 바로 실행되며 suspend하지 않음. 캐시에 없으면 `false` |
| `repo.update(uuid, immediate = true) { }` | 위와 같고, 플레이어 큐에 즉시 저장을 예약 |
| `repo.flush(uuid)` (suspend) | dirty 여부와 무관하게 즉시 저장하고 완료를 기다림. flush는 저장이 커밋되면 `true`, 실패·소유권 상실·미등록이면 `false` |
| `repo.peek(uuid)` (suspend) | `loadOffline`으로 DB의 마지막 저장본을 읽음. 캐시와 소유권은 건드리지 않음(읽기 전용). 기본 Database(`TransactionManager.defaultDatabase`)의 트랜잭션에서 실행 |
| `repo.writeOffline(uuid, value)` (suspend) | 접속하지 않은 플레이어의 데이터를 소유권을 잠깐 얻어 저장. 세션을 획득한 뒤 트랜잭션에서 `saveOffline`을 호출하고 버전을 올리고 Redis 사본(있으면)을 갱신한 뒤 해제. 다른 서버가 소유 중이면 `OfflineWriteResult.Held(owner)`(owner = 그 서버의 포트 문자열)를 돌려주므로 호출자가 그 서버로 변경을 전달해야 함. 이 서버에 접속 중이거나 로드 중이면 `Held(자기 포트)`. `saveOffline`이 던진 예외는 호출자에게 그대로 전달되고 소유권은 해제됨 |

- 접속 시 모든 리포지토리의 load가 끝나면 `PlayerRepositoryLoadedEvent`가 메인 스레드에서 발생합니다. 로딩 중에는 이동, 클릭, 드래그, 손 교체, 슬롯 변경, 명령, 줍기, 버리기가 차단됩니다. load가 두 번 실패하면 플레이어를 킥합니다.
- 퇴장 시 모든 리포지토리를 한 트랜잭션으로 저장한 뒤 소유권을 놓고 캐시를 비웁니다. 플러그인 disable이나 서버 종료 시에도 접속 중인 플레이어를 저장합니다(메인 스레드 블로킹, 리포지토리당 5초 상한).

#### 주의사항

- 같은 플레이어의 저장·로드는 플레이어 큐에서 한 번에 하나씩 실행됩니다. `save`/`load`/`loadOffline` 안에서 같은 플레이어의 `flush`/`update(immediate = true)`를 호출하면 교착되므로 금지합니다(`flush`는 자기 뒤에 줄 선 저장을 기다리게 됩니다).
- 저장에 넘어가는 스냅샷은 복사본이 아니라 캐시의 객체 그대로입니다. `update {}` 밖에서 객체를 바꾸지 마세요. 저장 중에 `update`로 바뀐 내용은 dirty로 남아 다음 주기에 다시 저장됩니다.
- 리포지토리별 `dirtyInterval`은 전역 `dirty-flush-seconds`의 배수로 반올림됩니다(예: 전역 5초에 7초를 주면 5초, 8초를 주면 10초).
- `join-timeout-seconds`(기본 5초)가 `lease-seconds`(기본 30초)보다 짧으므로, 소유 서버가 크래시한 뒤 lease가 만료될 때까지 최대 30초 동안은 그 플레이어의 접속이 거부될 수 있습니다.

#### 소유권

한 플레이어의 데이터는 항상 한 서버만 씁니다. 소유권은 DB 테이블 `hqframework_player_session`(자동 생성)에 기록되며 모든 서버가 같은 DB를 바라봐야 합니다. `player-data.backend: redis`면 소유권은 Redis에 기록됩니다([Redis (선택)](#redis-선택) 참고).

- 서버를 이동하면 새 서버는 이전 서버가 저장을 끝내고 소유권을 놓을 때까지 기다린 뒤 load합니다. `player-data.join-timeout-seconds` 안에 얻지 못하면 "잠시 후 다시 접속해 주세요"로 킥합니다. 프록시 연결(`netty.enabled: true`)이 있으면 저장 완료 패킷으로 바로 재시도하고, 없어도 `retry-interval-millis`마다 재시도하므로 동작합니다.
- 서버가 크래시하면 lease(`lease-seconds`)가 만료된 뒤 다음 접속 서버가 마지막 저장본으로 인계받습니다. 손실 범위는 마지막 저장 이후의 변경입니다.
- 퇴장 저장이 실패하면 소유권과 캐시를 유지한 채 dirty 주기마다 다시 저장하고, 성공하면 그때 소유권을 놓습니다. 끝내 실패하면 lease 만료로 인계됩니다.
- 다른 서버가 소유권을 가져간 것이 감지되면(버전 불일치) 저장을 버리고 캐시를 지운 뒤 접속 중이면 킥합니다. 같은 서버가 소유 중인데 버전만 어긋나면 서버 쪽 버전을 받아들여 계속 진행하고, 소유자가 바뀐 경우에만 소유권 상실로 처리합니다.

#### 기존 코드에서 옮기기

1. **아무것도 하지 않음**: `repo[uuid]`로 얻은 객체를 직접 고치는 기존 코드도 그대로 컴파일되고 동작합니다. 다만 dirty 표시가 되지 않으므로 전체 저장 주기(기본 60초)와 퇴장 때만 저장됩니다.
2. **`fingerprint` 추가**: 전체 저장 주기에서 실제로 바뀐 플레이어만 저장해 DB 부하를 줄입니다.
3. **`update { }`로 전환**: 변경이 dirty 주기(기본 5초) 안에 저장됩니다. 중요한 변경(결제, 거래 등)은 `immediate = true`나 `flush(uuid)`를 씁니다.

`PlayerRepository`는 더 이상 `MutableMap`이 아닙니다. `get`/`set`/`remove`만 유지되며 `clear`, `putAll`, 순회 등은 쓸 수 없습니다.

#### 설정

```yaml
player-data:
  backend: database
  lease-seconds: 30
  renew-seconds: 10
  join-timeout-seconds: 5
  retry-interval-millis: 200
  dirty-flush-seconds: 5
  full-flush-seconds: 60
```

`backend`는 `database` 또는 `redis`이며 그 외 값이면 기동에 실패합니다. 네트워크의 모든 서버가 같은 값을 써야 합니다. 처음 기동한 서버의 값이 DB 테이블 `hqframework_player_data_backend`에 기록되고, 이후 다른 값으로 기동하면 거부됩니다. `lease-seconds > renew-seconds > 0`, `dirty-flush-seconds > 0`, `full-flush-seconds >= dirty-flush-seconds`, `join-timeout-seconds > 0`, `retry-interval-millis > 0`을 만족하지 않아도 기동에 실패하며 오류 메시지에 해당 키가 나옵니다. `SavePolicy.periodic`의 간격과 `batchSize`도 양수여야 합니다. `lease-seconds`는 `renew-seconds`의 3배 정도로 두어 GC 멈춤이나 DB 지연 한 번에 소유권을 잃지 않게 합니다.

### Redis (선택)

```yaml
player-data:
  backend: redis

redis:
  uri: "redis://:password@127.0.0.1:6379/0"
  key-prefix: hq
  data-ttl-seconds: 3600
```

`redis.uri`를 비워 두면 Redis를 전혀 쓰지 않습니다. `backend: redis`인데 `redis.uri`가 비어 있으면 기동에 실패합니다. 단일 Redis(또는 Sentinel 뒤의 마스터)만 지원하며 **Redis 5 이상이 필요하고 Redis Cluster는 지원하지 않습니다**(소유권 갱신 스크립트가 여러 플레이어 키를 한 번에 다룹니다).

`backend`를 `database`에서 `redis`로(또는 반대로) 바꾸는 절차:
1. 네트워크의 모든 서버를 정지합니다.
2. 공유 DB의 `hqframework_player_data_backend` 테이블 행의 `backend` 값을 새 값으로 바꿉니다.
3. 모든 서버의 `config.yml`에서 `player-data.backend`(와 `redis.uri`)를 바꿉니다.
4. 서버를 기동합니다.

바뀌는 것:
- **소유권 조율이 Redis로 이동**합니다(`<key-prefix>:session:<uuid>` 해시, Lua 스크립트로 원자적 처리). 소유권을 놓아도 저장 버전은 남으며, 키는 마지막 기록 후 30일 뒤 자동 삭제됩니다. 영속 저장은 여전히 `database` 설정의 MySQL/H2/SQLite입니다.
- **소유권을 잃은 서버가 MySQL에 한 번 쓸 수 있습니다.** 저장 직전에 소유권을 확인하지만 확인과 저장 사이에 다른 서버가 소유권을 가져가면 그 저장은 MySQL에 반영된 뒤 커밋 단계에서 거부됩니다. Redis가 최신 데이터의 기준이므로 새 소유 서버의 다음 영속 저장이 이를 덮어씁니다. Redis 사본 쓰기는 Redis에 기록된 소유자가 이 서버일 때만 반영됩니다.
- **소유권 해제 알림이 즉시 전달**됩니다. 이전 서버가 소유권을 놓으면 Redis pub/sub으로 알려 새 서버가 `retry-interval-millis`를 기다리지 않고 바로 재시도합니다. 프록시 연결이 없어도 됩니다.
- **`CachedPlayerRepository`는 최신 데이터를 Redis에** 둡니다. `update {}`/`set`으로 바뀐 값은 곧바로 Redis에 쓰이고, MySQL에는 `SavePolicy`에 따라 주기적으로 저장됩니다. 서버를 옮기면 새 서버는 MySQL 대신 Redis 사본을 읽습니다.

```kotlin
@Serializable
data class PointData(var point: Long, var level: Int)

@Component
class PointRepository : CachedPlayerRepository<PointData>(PointData.serializer()) {
    override suspend fun load(player: Player): PointData { ... }
    override suspend fun save(player: Player, value: PointData) { ... }
}
```

- 패키지: `kr.hqservice.framework.database.repository.player`. 생성자의 두 번째 인자로 `SavePolicy`를 줄 수 있고, 나머지 API는 `PlayerRepository`와 같습니다.
- 직렬화 생성자(`PointData.serializer()`)를 쓰면 `V`는 `@Serializable`이어야 하고, Redis에는 kotlinx.serialization JSON으로 저장됩니다. `CachedPlayerRepository(JsonPlayerDataCodec(serializer, json))`과 같습니다.
- 데이터가 크거나 JSON으로 다루기 어려우면(예: `ItemStack`은 `ItemStack.serializeAsBytes`) `PlayerDataCodec<V>`를 직접 구현해 `CachedPlayerRepository(codec: PlayerDataCodec<V>)` 생성자에 넘깁니다. 이때 `V`는 `@Serializable`일 필요가 없습니다. 코덱 인터페이스는 `kr.hqservice.framework.database.repository.player.cache` 패키지에 있습니다.

```kotlin
data class Backpack(val item: ItemStack)

class BackpackCodec : PlayerDataCodec<Backpack> {
    override fun encode(value: Backpack): ByteArray = value.item.serializeAsBytes()
    override fun decode(bytes: ByteArray): Backpack = Backpack(ItemStack.deserializeBytes(bytes))
}

class BackpackRepository : CachedPlayerRepository<Backpack>(BackpackCodec()) { ... }
```

- `redis.uri`가 비어 있으면 `PlayerRepository`와 완전히 같게 동작하며 직렬화는 호출되지 않습니다. `redis.uri`가 있으면 `backend`가 `database`여도 Redis 캐시를 씁니다.
- Redis 키는 `<key-prefix>:data:<cacheName>:<uuid>`입니다. `cacheName` 기본값은 클래스 FQCN이라 패키지나 클래스 이름을 바꾸면 키가 바뀌어 기존 사본을 못 읽습니다. `override val cacheName = "point"`처럼 고정하는 것을 권장합니다.
- `peek(uuid)`는 Redis 사본을 먼저 보고, 없으면 `loadOffline`으로 DB를 읽습니다.
- 접속 시에도 Redis 사본이 있으면 `load` 대신 그것을 쓰고 만료 시간을 없앱니다. 없으면 `load` 결과를 Redis에 기록합니다.
- DB 저장이 끝날 때마다 저장한 값으로 Redis 사본을 갱신합니다. 퇴장 후 저장이면 사본은 `redis.data-ttl-seconds` 뒤 만료됩니다. 그 사이에 MySQL을 직접 고쳐도 다음 접속에서는 Redis 사본이 우선합니다.
- **값은 반드시 `update {}`나 `set`으로만 수정하세요.** `repository[uuid]!!.point = 10`처럼 직접 고친 값은 다음 전체 저장 때 MySQL과 Redis에 함께 반영되지만, 그 전에 플레이어가 다른 서버로 옮기거나 재접속하면 Redis의 이전 값이 보입니다.
- 역직렬화에 실패한 사본은 경고를 남기고 무시한 뒤 `load`로 읽습니다.
- Redis에 접속할 수 없으면 플레이어 접속이 로딩 상태로 보류되고, 회복되지 않으면 킥됩니다. DB로 자동 전환하지 않습니다.
- Redis가 유실되면 마지막 DB 저장 이후의 변경이 사라집니다. Redis에 AOF(`appendfsync everysec`)를 켜 두세요.

### 일반 리포지토리

```kotlin
object Shops : LongIdTimestampTable("shops") {
    val name = varchar("name", 32)
    override val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    override val updatedAt = datetime("updated_at").nullable()
}

class Shop(id: EntityID<Long>) : LongTimestampEntity(id, Shops) {
    companion object : LongTimestampEntityClass<Shop>(Shops)
    var name by Shops.name
}

@Bean
class ShopRepository : CrudExposedRepository<Long, Shop>(Shop)
```

`CrudExposedRepository`는 `count`, `new {}`, `delete`, `deleteById`, `existsById`, `findAll`, `findById`를 suspend로 제공하며 모두 IO 트랜잭션에서 실행됩니다. `TimestampEntityClass`는 엔티티가 수정될 때 `updatedAt`을 갱신합니다. `EntityClass.findForUpdate {}`, `findByIdForUpdate`, `getForUpdate` 확장으로 행 잠금을 걸 수 있습니다.

### 설정

```yaml
database:
  type: h2
  file-path: "hq-database/database"
  mysql:
    host: localhost
    port: 3306
    user: root
    password: password
    database: hq
    maximum-pool-size: 10
```

`type`은 `h2`, `sqlite`, `mysql`입니다. 상대 `file-path`는 HQFramework의 데이터 폴더 기준입니다. SQLite는 WAL 모드와 풀 크기 1로 동작합니다.

---

## Redis 메시징과 임시 저장소

모듈 `hqframework-bukkit-database`, 패키지 `kr.hqservice.framework.database.redis`. HQFramework `config.yml`의 `redis.uri`를 설정하면 서버 간 메시지와 임시 데이터에 Redis를 쓸 수 있습니다. `RedisMessenger`, `RedisStores`, `RedisProvider`는 전역 빈이라 생성자로 주입받습니다.

```kotlin
@Serializable
data class Party(val leader: String, val members: List<String>)

@Serializable
data class PartyInvite(val partyId: String, val target: String)

@Module
class PartyModule(private val messenger: RedisMessenger, stores: RedisStores, private val plugin: HQBukkitPlugin) {
    private val parties = stores.create<Party>("myplugin:party", Duration.ofHours(1))

    @Setup
    fun setup() {
        messenger.subscribe("party-invite", PartyInvite.serializer(), plugin) { invite ->
            plugin.server.getPlayer(invite.target)?.sendMessage("파티 초대: ${invite.partyId}")
        }
    }

    suspend fun create(partyId: String, leader: String): Party? =
        parties.update(partyId) { current -> current ?: Party(leader, listOf(leader)) }

    suspend fun join(partyId: String, player: String): Party? =
        parties.update(partyId) { current -> current?.copy(members = (current.members + player).distinct()) }

    suspend fun leave(partyId: String, player: String): Party? =
        parties.update(partyId) { current ->
            val members = current?.members.orEmpty() - player
            if (current == null || members.isEmpty()) null else current.copy(members = members)
        }

    suspend fun invite(partyId: String, target: String) {
        if (!parties.touch(partyId)) return
        messenger.publish("party-invite", PartyInvite.serializer(), PartyInvite(partyId, target))
    }
}
```

**`RedisMessenger`**
- `publish(channel, serializer, value)`: 값을 JSON으로 직렬화해 보냅니다. 비동기로 보내고 기다리지 않습니다.
- `subscribe(channel, serializer, scope) { value -> }`: 핸들러는 `suspend`이며 메시지마다 `scope.launch`로 실행됩니다. 스코프의 디스패처를 따르므로 `plugin`을 넘기면 메인 스레드에서 실행되어 Bukkit API를 바로 쓸 수 있습니다. 핸들러 안에서 `plugin.launch { }`로 다시 감쌀 필요가 없습니다.
- `Subscription`(`AutoCloseable`)을 돌려줍니다. `close()`하면 더 받지 않으며 여러 번 불러도 됩니다. 스코프의 `Job`이 끝나면(취소·완료) 구독도 자동으로 해제됩니다. 플러그인을 스코프로 넘기면 플러그인이 disable될 때 함께 해제됩니다.
- 실제 Redis 채널 이름은 `<key-prefix>:msg:<channel>`(기본 `hq:msg:<channel>`)입니다. 보낸 서버 자신도 구독 중이면 메시지를 받습니다.
- 핸들러가 던진 예외는 스코프의 예외 처리로 전달됩니다(`plugin`이면 플러그인 예외 핸들러). `SupervisorJob`을 가진 스코프라면 다른 구독과 다음 메시지에는 영향을 주지 않습니다.
- 역직렬화에 실패한 메시지는 경고를 남기고 버립니다.

**`RedisStore`**
- `RedisStores.create<T>(prefix, ttl)`(또는 `create(serializer, prefix, ttl)`)로 만듭니다. 한 번 만들어 필드에 두고 재사용하세요. `T`는 `@Serializable`이어야 하며 값은 JSON으로 저장됩니다. `redis.uri`가 비어 있으면 `create`가 `IllegalStateException`을 던지므로 선택 기능이라면 `RedisProvider.enabled`로 먼저 분기하세요.
- 키는 `<prefix>:<id>`입니다. `key-prefix`(기본 `hq`)는 프레임워크가 쓰므로 `myplugin:party`처럼 플러그인 고유 접두어를 쓰세요. `key(id)`로 실제 키를 확인할 수 있습니다.
- `get`, `set`, `delete`, `exists`는 모두 `suspend`입니다. `ttl`을 주면 `set`과 `update`로 쓸 때마다 만료 시간이 다시 걸립니다. `ttl`이 없으면 만료되지 않습니다.
- `touch(id)`: 값은 그대로 두고 만료 시간만 `ttl`로 연장합니다. 키가 없거나 `ttl`이 없으면 `false`입니다.
- `update(id) { current -> next }`: 현재 값을 읽어 블록의 결과로 원자적으로 바꿉니다(낙관적 동시성). 블록이 `null`을 돌려주면 키를 지웁니다. 그 사이에 다른 서버가 값을 바꾸면 다시 읽고 블록을 재실행하며, 5번 모두 충돌하면 `IllegalStateException`을 던집니다. **블록은 여러 번 실행될 수 있으므로 순수 함수여야 합니다**(메시지 전송, 다른 저장소 쓰기 같은 부수 효과는 `update`가 돌려준 값으로 블록 밖에서 하세요).

**`RedisProvider`**
- `enabled`: `redis.uri`가 설정되어 있는지 확인합니다. 선택 기능이라면 이 값으로 분기하세요.
- `connection()`: Lettuce `StatefulRedisConnection<String, ByteArray>`를 돌려줍니다. 처음 호출할 때 연결하며 연결과 명령 타임아웃은 3초입니다. 메인 스레드에서 `sync()`를 쓰면 그만큼 멈출 수 있으니 `async()`를 쓰세요. 모든 플러그인이 공유하는 연결이므로 `close()`하지 마세요. `RedisStore`로 부족할 때(해시, 정렬 집합 등) Lettuce API(`sync()`, `async()`, `reactive()`)를 직접 씁니다. 값 코덱이 `ByteArray`이므로 문자열 값은 `toByteArray()`로 넣습니다.
- `redis.uri`가 비어 있으면 `connection()`과 `RedisMessenger`의 `publish`/`subscribe`는 `IllegalStateException`을 던집니다. `redis.uri` 형식이 잘못되면 기동에 실패합니다.
- 키는 플러그인 고유 접두어(예: `myplugin:`)를 쓰세요. `key-prefix`(기본 `hq`)는 프레임워크가 씁니다.

---

## 명령어

모듈 `hqframework-bukkit-command`. 패키지: `kr.hqservice.framework.command`

```kotlin
@Command(label = "point", aliases = ["포인트"], permission = "myplugin.point")
class PointCommand(private val repository: PointRepository) {
    @CommandExecutor("show", description = "포인트 확인")
    fun show(player: Player) {
        player.sendMessage("${repository[player.uniqueId]?.point ?: 0}")
    }

    @CommandExecutor("give", description = "포인트 지급", isOp = true)
    suspend fun give(sender: CommandSender, target: Player, @ArgumentLabel("수량") amount: Long?) {
        repository.update(target.uniqueId) { it.point += amount ?: 1 }
    }
}

@Command(label = "admin", parent = PointCommand::class, permission = "myplugin.admin")
class PointAdminCommand {
    @CommandExecutor("reset")
    fun reset(sender: CommandSender, target: Player) { }
}
```

- `@Command(parent = ...)`로 하위 명령 트리를 만듭니다. 위 예시는 `/point show`, `/point give <대상> [수량]`, `/point admin reset <대상>`을 등록합니다.
- 첫 파라미터는 `CommandSender`, `Player`, `ConsoleCommandSender` 중 하나입니다. `Player`로 선언하면 콘솔에서는 거부됩니다.
- 나머지 파라미터는 `CommandArgumentProvider<T>`가 변환합니다. 기본 제공: `Int`, `Long`, `Double`, `Float`, `Boolean`, `String`, `Material`, `Player`, `NettyPlayer`, `LocalDateTime`, `HQBukkitPlugin`. 직접 만들려면 `@Component`를 붙여 구현합니다.
- nullable 파라미터는 인자가 없을 때 null, Kotlin 기본값이 있는 파라미터는 기본값이 됩니다. 도움말에서 `<필수>`, `[선택]`으로 표시됩니다.
- `suspend` 실행자는 비동기 스레드, 일반 실행자는 메인 스레드에서 실행됩니다.
- 권한은 루트부터 실행자까지 경로의 모든 `permission`과 `isOp`를 만족해야 합니다. 권한이 없는 항목은 도움말과 탭완성에서 숨겨집니다.
- 변환 실패는 `ArgumentFeedback`(`Message`, `RequireArgument`, `NotNumber`, `PlayerNotFound` 등)을 던지면 됩니다. 다른 예외는 `CommandArgumentExceptionHandler<T, S>`를 `@Component`로 등록해 처리합니다.

```kotlin
@Component
class WorldArgumentProvider(private val server: Server) : CommandArgumentProvider<World> {
    override suspend fun cast(context: CommandContext, argument: String?): World {
        if (argument == null) throw ArgumentFeedback.RequireArgument
        return server.getWorld(argument) ?: throw ArgumentFeedback.Message("월드를 찾을 수 없습니다")
    }

    override suspend fun getTabComplete(context: CommandContext, location: Location?): List<String> {
        return server.worlds.map { it.name }
    }
}
```

---

## 인벤토리 UI

모듈 `hqframework-bukkit-inventory`.

### View와 Navigator

상태(`State`)를 구독하는 버튼으로 구성된 화면입니다. `Navigator`가 플레이어별 화면 스택을 관리합니다.

```kotlin
class CounterViewModel(val navigator: Navigator) : ViewModel() {
    val count = state(1)
}

class CounterView : View(27, "&0카운터") {
    private val viewModel by viewModels(CounterViewModel::class)

    override suspend fun CreateScope.onCreate() {
        button(13) {
            subscribe(viewModel.count)
            item(Material.EMERALD) { amount = viewModel.count.get().coerceIn(1, 64) }
            onClick { viewModel.count.set(viewModel.count.get() + 1) }
        }
        button(26) {
            item(Material.BARRIER)
            onClick { viewModel.navigator.goPrevious(it.getPlayer()) }
        }
    }
}
```

```kotlin
@Listener
class MenuListener(private val navigator: Navigator, private val plugin: HQBukkitPlugin) {
    @Subscribe
    fun onJoin(event: PlayerJoinEvent) {
        plugin.launch { navigator.goNext(CounterView(), event.player) }
    }
}
```

- `onCreate`는 인스턴스당 한 번 실행됩니다. 구독한 `State`가 바뀌면 해당 슬롯만 다시 렌더링됩니다.
- `Navigator`: `goNext(view, vararg players)`, `goPrevious(player)`, `goFirst(player)`, `clearViewsAndClose(player)`, `current(uuid)`, `openedViews(uuid)`. 모두 suspend입니다.
- `View(size, title, cancel = true)`에서 `cancel`이 true면 클릭이 취소됩니다. 버튼 슬롯은 항상 취소됩니다. 드래그도 취소됩니다.
- `ViewModel` 생성자 파라미터는 플러그인 범위에서 주입되며, View가 닫히면 함께 정리됩니다.
- 플레이어가 퇴장하면 스택이 정리됩니다.

### HQContainer (레거시)

```kotlin
class MenuContainer : HQContainer(27, "&0메뉴") {
    override fun initialize(inventory: Inventory) {
        HQButtonBuilder(Material.DIAMOND)
            .setDisplayName("&b클릭")
            .setClickFunction { event -> event.getWhoClicked().sendMessage("clicked") }
            .build()
            .setSlot(this, 13)
    }
}

MenuContainer().open(player)
```

`onOpen`, `onClose`, `onClick`, `onDrag`를 오버라이드할 수 있고 `refresh()`로 다시 그립니다. 다른 컨테이너가 열려 있는 상태에서 `open`을 호출하면 다음 틱에 전환됩니다.

---

## 리전

모듈 `hqframework-bukkit-region`.

```kotlin
val range = pos1.asBlockLocation()..pos2.asBlockLocation()

if (range.contains(player.location)) { }
val overlaps = range.collidesWith(otherRange)
val center = range.getCenter()

if (range is DimensionRange) {
    val floor = range.getPlaneRange(PlaneAxis.HORIZONTAL, Offset.MIN)
    val corner = floor.getLineRange(LineAxis.HORIZONTAL_X, Offset.MIN).getPoint(Offset.MAX)
}

range.forEach { it.getBlock().type = Material.AIR }
```

- 두 점이 일치하는 축의 수에 따라 `PointRange`, `LineRange`, `PlaneRange`, `DimensionRange`가 만들어집니다. `RangeFactory`를 주입받아 `makeRange(a, b)`로 만들 수도 있습니다.
- `Range`는 `Collection<BlockLocation>`입니다. 블록을 미리 만들지 않고 순회할 때 생성하므로 큰 영역도 메모리를 쓰지 않습니다. `size`는 계산값입니다.
- `collidesWith`는 AABB 겹침 판정입니다. `getCenter`는 음수 좌표에서도 바닥 나눗셈입니다.

---

## NMS

모듈 `hqframework-bukkit-nms`. 서버 버전에 맞는 구현을 enable 시 자동으로 고릅니다.

| 구현 | 버전 |
|---|---|
| legacy | 1.17 ~ 1.20.4 (리플렉션) |
| V20_6 | 1.20.6 |
| V21 | 1.21, 1.21.1 |
| V21_3 | 1.21.3, 1.21.4 |
| V21_5 / V21_6 / V21_7 | 1.21.5 / 1.21.6 / 1.21.7, 1.21.8 |
| V21_11 | 1.21.11 |
| V26_1 / V26_2 | 26.1 / 26.2 |

목록에 없는 버전은 같은 메이저의 가장 가까운 하위 구현을 쓰며 콘솔에 경고를 남깁니다. 1.20.5처럼 하위 구현이 없는 버전은 enable에 실패합니다.

### ItemStack

```kotlin
itemStack.nms {
    tag {
        setString("exampleKey", "exampleValue")
    }
}

val nmsItemStack = itemStack.getNmsItemStack()
if (nmsItemStack.hasTag() && nmsItemStack.getTag().hasKey("exampleKey")) {
    player.sendMessage(nmsItemStack.getTag().getString("exampleKey"))
}
```

1.20.5 이상에서는 태그가 PersistentDataContainer의 `hq_tag`에 저장됩니다. `getDisplayName()`은 config의 `lang`(기본 `ko_kr`)에 맞는 현지화 이름을 돌려줍니다.

### Virtual (클라이언트 사이드 패킷)

```kotlin
player.virtual {
    inventory {
        setItem(slot, ItemStack(Material.BARRIER))
    }

    val display = VirtualTextDisplay(player.location.add(0.0, 2.0, 0.0)) {
        text = TextComponent("환영합니다")
    }
    updateEntity(display)
    delay(3000)
    display.destroy()
    updateEntity(display)

    anvil(TextComponent("이름 입력")) {
        setConfirmHandler { text ->
            player.sendMessage(text)
            true
        }
    }
}
```

- `Player.virtual { }`는 한 명, `Player.virtual(distance) { }`와 `Location.virtual(distance) { }`는 범위 안의 모든 플레이어가 대상입니다. 블록은 suspend이며 전용 코루틴 스코프에서 실행됩니다.
- 사용 가능: `inventory { setItem, setTitle }`, `anvil { setBaseItem, setResultItem, setInputHandler, setConfirmHandler, setButtonHandler, setCloseHandler }`, `sign { setConfirmHandler }`, `setCamera(entity)`, `updateEntity(entity)`, `updateWorldBorder(VirtualWorldBorder)`.
- 가상 엔티티: `VirtualArmorStand(location, name)`, `VirtualTextDisplay(location) { }`. 속성을 바꾼 뒤 `updateEntity`로 전송합니다. 이름의 색 코드는 생성자 인자에만 적용되므로 `setName`에는 `colorize()`한 문자열이나 `BaseComponent`를 넘깁니다.
- `Player.virtualView { condition { slot, item -> }; item { slot, item -> } }`로 실제 인벤토리 아이템을 클라이언트에서만 다르게 보여줄 수 있습니다.
- 모루와 표지판 콜백은 메인 스레드에서 실행됩니다. `sign`의 confirm이 false를 반환하면 다시 열립니다.

주입 가능한 서비스: `NmsItemStackService`, `NmsBaseComponentService`, `NmsWorldBorderService`, `NmsContainerService`, `NmsNettyInjectService`, `NmsArmorStandService`, `NmsTextDisplayService` 등. 패키지는 `kr.hqservice.framework.nms.service`입니다.

---

## Packet I/O (프록시 통신)

HQFramework가 설치된 프록시(Bungee, Velocity)와 백엔드(Bukkit) 사이에 TCP 채널이 열립니다. 백엔드는 프록시에 접속하고, 프록시는 백엔드 간 패킷을 중계합니다.

### 설정

프록시와 백엔드 양쪽 `config.yml`:

```yaml
netty:
  enabled: true
  host: 127.0.0.1
  port: 11286
  secret: "change-me"
```

- 백엔드는 `host:port`로 접속하고, 프록시는 그 주소에 바인드합니다. 다른 호스트의 백엔드를 받으려면 프록시 host를 바꾸세요.
- `secret`은 양쪽이 같아야 연결됩니다. 비워 두면 연결은 되지만 누구나 백엔드로 등록할 수 있다는 경고가 뜹니다.
- 백엔드는 접속 후 10초 안에 핸드셰이크를 마쳐야 하고, 끊기면 3초 뒤 재접속합니다. 프레임 상한은 32MB, relay 상한은 16MB입니다.
- 프록시 `netty.shutdown-servers: true`면 프록시 종료 시 모든 백엔드가 함께 종료됩니다. 기본값은 false입니다.

### 패킷 정의

송신 측과 수신 측이 같은 클래스(같은 FQCN)를 공유해야 합니다.

```kotlin
class GreetPacket(var name: String, var uuid: UUID) : Packet() {
    override fun write(buf: ByteBuf) {
        buf.writeString(name)
        buf.writeUUID(uuid)
    }

    override fun read(buf: ByteBuf) {
        name = buf.readString()
        uuid = buf.readUUID()
    }
}
```

- 생성자의 모든 파라미터는 같은 이름의 `var` 프로퍼티여야 합니다. 아니면 등록 시 예외가 납니다. 빈 생성자는 필요 없습니다.
- `ByteBuf` 확장: `writeString/readString`, `writeUUID/readUUID`, `writeVarInt/readVarInt`, `writeStringArray/readStringArray`, `writeChannel/readChannel`, `writePlayer/readPlayer`, `writePlayers/readPlayers`.

### 등록, 수신, 송신 (Bukkit)

```kotlin
@Module
class GreetModule(private val nettyServer: NettyServer, private val logger: Logger) {
    @Setup
    fun setup() {
        nettyServer.registerOuterPacket(GreetPacket::class)
        nettyServer.registerInnerPacket(GreetPacket::class) { packet, channel ->
            logger.info("${packet.name} greeted from port ${channel.port}")
        }
    }
}

@Listener
class GreetListener(private val packetSender: PacketSender) {
    @Subscribe
    fun onJoin(event: PlayerJoinEvent) {
        packetSender.sendPacketAll(GreetPacket(event.player.name, event.player.uniqueId))
    }
}
```

- `registerOuterPacket`은 보낼 패킷, `registerInnerPacket`은 받을 패킷을 등록합니다. 받을 패킷을 등록하지 않으면 조용히 버려집니다.
- `PacketSender`: `sendPacketToProxy(packet)`, `sendPacketAll(packet)`(자기 자신 포함 모든 백엔드), `sendPacket(port, packet)`, `sendPacket(serverName, packet)`, `broadcast(component)`, `sendMessageToPlayers(players, component)`.
- `NettyServer`: `getChannels()`, `getChannel(name)`, `getPlayer(uuid)`, `getPlayers()`로 네트워크 전체의 서버와 플레이어를 조회합니다.
- suspend 리스너가 필요하면 `Direction.INBOUND.registerPacket(...)` 후 `Direction.INBOUND.addListener(GreetPacket::class) { packet, channel -> }`를 씁니다.
- 수신한 모든 패킷에 대해 `AsyncNettyPacketReceivedEvent`가, 연결/해제 시 `NettyClientConnectedEvent`/`NettyClientDisconnectedEvent`가 발생합니다.
- 응답이 필요한 요청은 `channel.startCallback(request, Response::class) { response -> }`로 보내고, 응답 측은 `response.setCallbackResult(true)` 후 같은 채널로 보냅니다.
- 리스너는 채널마다 순서대로 하나씩 실행됩니다. 느린 작업은 코루틴으로 넘기세요.
- 프레임워크 내부 패킷(`ShutdownPacket`, `HandShakePacket` 등)은 백엔드 간 relay가 거부됩니다.

### 프록시 측

```kotlin
@Module
class ProxyGreetModule(
    private val nettyServer: NettyServer,
    private val packetSender: PacketSender,
    private val logger: Logger
) {
    @Setup
    fun setup() {
        nettyServer.registerOuterPacket(GreetPacket::class)
        nettyServer.registerInnerPacket(GreetPacket::class) { packet, channel ->
            logger.info("greet from ${channel.port}: ${packet.name}")
            packetSender.sendPacket("lobby", packet)
        }
    }
}
```

- Bungee 플러그인은 `HQBungeePlugin`, Velocity 플러그인은 `HQVelocityPlugin`을 상속합니다. `@Module`/`@Setup`은 각 플랫폼 패키지(`kr.hqservice.framework.bungee.core.component.module`, `kr.hqservice.framework.velocity.core.component.module`)에 있습니다.
- 리스너는 Bungee에서 `@Component class X : HQListener` + `@EventHandler`, Velocity에서 `@Listener` + Velocity `@Subscribe`입니다.
- `PacketSender`의 메시지 타입은 Bungee가 `BaseComponent`, Velocity가 Adventure `Component`입니다.
- 백엔드 이름은 프록시 서버 목록의 포트와 매칭해 정해집니다. 매칭되지 않으면 `Unknown-<port>`입니다.
- Velocity는 `last-connection: true`로 마지막 접속 서버 기억 기능을 켤 수 있습니다.

---

## Quartz 스케줄러

모듈 `hqframework-bukkit-scheduler`. HQFramework DB를 JobStore로 쓰는 Quartz 스케줄러가 전역 빈 `org.quartz.Scheduler`로 제공됩니다. 잡 클래스는 플러그인 패키지 안에 두면 생성자 주입을 받습니다.

```kotlin
class DailyRewardJob(private val service: RewardService, private val logger: Logger) : SuspendedJob() {
    var amount: Int = 0

    override suspend fun executeSuspend(context: JobExecutionContext) {
        service.giveAll(amount)
        logger.info("reward done")
    }
}

@Module
class JobRegistrar(private val scheduler: Scheduler) {
    @Setup
    fun schedule() {
        val job = JobBuilder.newJob(DailyRewardJob::class.java)
            .withIdentity("daily", "myplugin")
            .usingJobData("amount", 100)
            .build()
        val trigger = TriggerBuilder.newTrigger()
            .withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 * * ?"))
            .build()
        if (!scheduler.checkExists(job.key)) scheduler.scheduleJob(job, trigger)
    }
}
```

- `SuspendedJob`을 상속하면 `executeSuspend`가 플러그인 컨텍스트의 코루틴에서 실행됩니다. 일반 `org.quartz.Job`도 됩니다.
- JobDataMap의 값은 같은 이름의 `var` 프로퍼티에 주입됩니다.
- `HQJobListener`, `HQTriggerListener`를 `@Component`로 등록하면 자동으로 리스너가 붙습니다.
- 클러스터 환경은 `scheduler.job-store.is-clustered: true`와 고유한 `scheduler.instance-id`를 설정합니다. 비워 두면 `서버IP:포트`가 ID입니다.

---

## HQFramework config.yml

`plugins/HQFramework/config.yml`

| 키 | 기본값 | 설명 |
|---|---|---|
| `config-version` | 2.3.0 | 바뀌면 누락 키를 자동 병합 |
| `lang` | ko_kr | NMS 현지화 언어 (`lang/*.json`) |
| `netty.enabled` | false | 프록시 통신 |
| `netty.thread` | 2 | IO 스레드 수 (최대 코어 수) |
| `netty.host`, `netty.port` | 127.0.0.1, 11286 | 프록시 주소 |
| `netty.secret` | "" | 프록시와 공유하는 인증 비밀값 |
| `log.error.store-limit` | 1000 | 보관할 에러 파일 수 |
| `log.error.store-path` | hq-errors/ | 에러 파일 폴더 |
| `log.error.print-stack-traces-when-unhandled` | false | 미처리 코루틴 예외의 스택트레이스 출력 |
| `database.type` | h2 | h2, sqlite, mysql |
| `database.file-path` | hq-database/database | H2/SQLite 파일 경로 (확장자 제외) |
| `database.mysql.*` | | 접속 정보와 HikariCP 풀 설정 |
| `player-data.backend` | database | 플레이어 데이터 소유권 백엔드. `database` 또는 `redis`. 모든 서버가 같아야 함 |
| `player-data.lease-seconds` | 30 | 소유권 lease. 갱신이 끊기면 이 시간 뒤 다른 서버가 인계 |
| `player-data.renew-seconds` | 10 | lease 갱신 주기 |
| `player-data.join-timeout-seconds` | 5 | 접속 시 소유권 대기 상한. 넘으면 킥 |
| `player-data.retry-interval-millis` | 200 | 소유권 획득 재시도 간격 |
| `player-data.dirty-flush-seconds` | 5 | dirty 항목 저장 주기 (`SavePolicy.periodic` 기본값) |
| `player-data.full-flush-seconds` | 60 | 전체 저장을 나눠 끝내는 주기 (`SavePolicy.periodic` 기본값) |
| `redis.uri` | "" | Redis 접속 URI. 비우면 Redis 미사용. `backend: redis`면 필수 |
| `redis.key-prefix` | hq | 프레임워크 Redis 키 접두어 |
| `redis.data-ttl-seconds` | 3600 | 퇴장 후 `CachedPlayerRepository` 사본을 Redis에 남겨 두는 시간 |
| `scheduler.instance-id` | "" | Quartz 인스턴스 ID |
| `scheduler.thread-pool.thread-count` | 10 | Quartz 스레드 수 |
| `scheduler.job-store.is-clustered` | false | Quartz 클러스터 모드 |
| `command.tab-complete.limit-per-second` | 20 | 플레이어당 초당 탭완성 횟수 |

사용자 플러그인의 `config.yml`도 같은 방식으로 복사·병합됩니다. 새 키를 추가할 때 `config-version`을 올리세요.

---

## 변경 사항

### 2.2.0 → 2.3.0 (하드닝 + 플레이어 데이터 소유권)

**함께 올려야 하는 것**
- 프록시와 모든 백엔드를 동시에 업그레이드해야 합니다. 핸드셰이크·플레이어 목록·채널 null 표기·채팅 메시지의 와이어 포맷이 바뀌어 신·구 버전은 연결되지 않습니다.
- `netty.secret`을 프록시와 백엔드에 같은 값으로 넣으세요. 비우면 경고가 뜨고 누구나 백엔드로 등록할 수 있습니다.
- 모든 백엔드가 같은 DB를 바라봐야 합니다. `hqframework_player_session` 테이블이 자동 생성됩니다.

**설정**
- 신설: `netty.secret`, `scheduler.instance-id`, `player-data.*`(backend, lease-seconds, renew-seconds, join-timeout-seconds, retry-interval-millis, dirty-flush-seconds, full-flush-seconds)
- 신설: `redis.*`(uri, key-prefix, data-ttl-seconds). `player-data.backend`에 `redis` 추가
- 신설 API: `PlayerRepository.writeOffline`/`saveOffline`(접속하지 않은 플레이어 데이터 쓰기), 로드 가드가 드래그·손 교체·슬롯 변경·픽업 시도 이벤트도 차단
- 기본값 변경: 프록시 `netty.shutdown-servers` `true` → `false`(신규 설치만)
- `config-version` 2.1.0 → 2.3.0. 키가 없을 때 코드 기본값이 실제로 적용되도록 YAML getter 버그를 고쳤고, 코드 기본값은 번들 config와 같게 맞췄습니다.

**와이어 포맷**
- `HandShakePacket`에 `secret` 추가. 채널 null 표기는 boolean 플래그. 플레이어 목록은 `count` + 인라인 인코딩(직렬화·deflate 제거). `MessagePacket`/`BroadcastPacket` 본문은 JSON 컴포넌트.
- 상한: 프레임 32MB, relay 16MB, inflate 32MB. 핸드셰이크 전 패킷과 알 수 없는 패킷 이름은 드롭. 프레임워크 서버 전용 패킷은 relay 거부.

**런타임**
- 공통 모듈은 Java 17 바이트코드(1.20.6~1.21.x NMS는 21, 26.x는 25). 한 jar로 1.17~26.x 서버에서 동작합니다.
- NIO 전송 전용(Epoll/KQueue 제거).
- Lettuce 6.5.5가 plugin.yml `libraries`로 추가됨. netty는 Lettuce와 맞춰 4.1.118로 상향.

**모듈 구조**
- `netty-core` 신설(global-netty와 velocity-netty가 공유). `multi-netty` 삭제. `proxy-multi-core`는 `proxy-velocity-core`를 확장하고 RedisBungee 전용 클래스만 남음. `proxy-core`에 공용 채널 레지스트리·하트비트·기본 리스너.
- nms: `V26_1`이 `V21_11`을 상속, `V21_5/6/7`은 V21 provider 상속. `v21_8` 패키지 → `v21_6`.

**하위 플러그인 호환성 파괴 (주요)**
- `PlayerRepository`가 `MutableMap`이 아님. `get`/`set`/`remove`만 유지, `update`/`flush`/`peek` 추가, 생성자 `SavePolicy`. `preLoad` API, `SwitchGate`, `DefermentLock`, `PlayerDataSavePacket`, `PlayerConnectionPacketHandler` 삭제. 소유하지 않은 플레이어에 대한 `set`은 무시.
- `HQBukkitPlugin.reload()` 삭제. `Range`는 `List`가 아니라 `Collection`. `View._childLifecycles` → 읽기 전용 `childLifecycles`.
- 컴포넌트 레지스트리: 서드파티 플러그인의 `@ComponentHandler`/`@AnnotationHandler`는 다른 플러그인에 공유되지 않음(프레임워크 핸들러만 상속). `@Primary`가 실제로 우선. `@Configuration` 1회 생성.
- suspend `@Subscribe` 리스너는 이벤트 스레드에서 시작해 메인 스레드에서 재개. 첫 suspend 전의 `isCancelled` 변경만 반영.
- 명령어: op 자동 우회 없음(`permission`과 `isOp` 모두 만족해야 함). 트리 경로 권한 검사. `findRoot`, `findProvider`, `getArgumentsByType`, `findTreeAll` 삭제. `CommandTabCompletionHandler.initialize()`로 변경.
- netty: `ChannelWrapper.sendPacket(): Boolean`, `HandShakePacket(Int, String)`, `BossHandler`/`HQChannelInitializer`/`HQNettyClient`/`HQNettyServer` 생성자 변경, 최상위 `blockingGroup` 제거, backing field 없는 패킷 파라미터는 등록 시 거부.
- 프록시: `PingPongManagementThread` 삭제(→ `Heartbeat`), Velocity `NettyChannelRegistryImpl` 생성자에서 `ProxyServer` 제거, multi-core 클래스 이름 변경(`RedisNettyModule`, `RedisNettyChannelRegistry`, `RedisProxyNettyServer`, `RedisPlayerConnectionListener`).
- nms: `provideNBTTagService`, `getServerChannels`, `Version.majorVersionOf`, preload 이벤트 삭제. 미정의 마이너 버전은 가장 가까운 하위 구현으로 폴백, 1.20.5는 미지원.
- DB: `@Table` 마이그레이션이 실제로 ALTER 실행. MySQL URL에서 `allowMultiQueries`·`autoReconnect` 제거. 상대 파일 경로는 플러그인 dataFolder 기준(기존 위치 파일이 있으면 경고 후 사용). Location 컬럼 새 포맷 `L2;...`(구 포맷 읽기 가능).
- 의존성 노출: `global-yaml`이 coroutines를, `global-netty`가 adventure-legacy·byte-buddy-agent를 transitive로 노출하지 않음.
- `SessionCoordinator`에 `commitsInsideTransaction`(추상)과 `onReleased`(기본 구현 있음) 추가. 외부 구현체는 컴파일이 깨짐.
- `PlayerRepository`에 internal 훅(`onMutated`, `afterPersisted` 등) 추가. 하위 플러그인에는 영향 없음.

**라이브 서버에서 확인이 필요한 것**
- secret 설정/미설정에서 Bukkit(1.20.4, 1.20.6, 1.21.x, 26.x) ↔ Bungee/Velocity/Multi 핸드셰이크·재접속·종료.
- Velocity 실제 서버 이동 지연과 소유권 인계, 두 서버 동시 접속 경쟁, 소유 서버 크래시 후 lease 만료 인계.
- MySQL의 lease SQL(`DATE_ADD`)은 코드 리뷰로만 검증됨(H2·SQLite는 테스트 있음). 모든 서버의 MySQL 세션 time_zone이 같아야 함.
- Bungee IP-forwarding과 Velocity modern forwarding에서 모루·표지판 virtual handler, 모루 더블클릭.
- Linux에서 NIO 전송, Folia 스케줄러.
- 실제 Redis에서 소유권 Lua 스크립트, `RedisStore`의 compare-and-set 스크립트, pub/sub(해제 알림, `RedisMessenger`)은 미검증. `HQ_TEST_REDIS_URI=redis://...`를 주면 `RedisIntegrationTest`가 실행됨.
- Paper에서 plugin.yml `libraries`의 netty-all과 Lettuce가 끌어오는 netty 개별 아티팩트가 중복 로딩되어도 문제없는지.
