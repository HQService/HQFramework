# HQFramework / next-generation bukkit/proxy development framework
[![GitHub license](https://img.shields.io/badge/license-GPL%20v3-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
[![Kotlin](https://img.shields.io/badge/kotlin-1.7.21-blue.svg?logo=kotlin)](http://kotlinlang.org)
### Bukkit 및 Proxy 플랫폼을 더 생산적, 직관적, 객체지향적으로 만들어줍니다.

HQFramework는 SpringFramework 에서 영감을 받아 Bukkit 및 Proxy 플랫폼에서의 동작을 위해 제작된 프레임워크입니다. 
 스프링의 기능 여러가지를 Bukkit 및 Proxy 플랫폼에 맞게끔 구현하였습니다.
 
## Thanks to
<a href="https://jb.gg/OpenSourceSupport"><img src="https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.png" alt="JetBrains Logo (Main) logo." width="120"></a>

## Features
* [Component](#component)
* [Packet I/O](#packet-io)
* [NMS](#nms)
    
## Component
### Table of contents
* [HQComponent의 사용](#hqcomponent의-사용)
* [복잡한 의존관계에서의 HQComponent](#복잡한-의존관계에서의-hqcomponent)
* [HQComponentHandler의 사용](#hqcomponenthandler의-사용)
* [HQComponentHandler의 초기화 순서보장](#hqcomponenthandler의-초기화-순서-보장)
* [Qualifier의 사용](#qualifier의-사용)
  * [Named](#named)
  * [MutableNamed](#mutablenamed)

HQFramework는 완성도가 높은 의존성 주입 라이브러리 Koin과 연결할 수 있는 Component 기능을 제공합니다.
 Component는 Singleton의 형태로 자동 생성될 수 있으며, 의존성 주입과 함께 생성되며, 필요에 따라 생성된 Component를 Bean으로 등록해주기도 합니다.
 
---
### HQComponent의 사용
아래는 Bukkit의 Listener를 자동으로 등록해주는 컴포넌트의 예시입니다. 컴포넌트의 생성과 동시에 Service 및 Plugin을 주입받아보겠습니다.
```kotlin
package kr.hqservice.exampleplugin.listener

@Component
class ExampleListener(
  private val exampleService: ExampleService,
  private val plugin: Plugin
) : HQListener {
  @EventHandler
  fun onExampleEvent(event: BukkitExampleEvent) {
    exampleService.doAnything(plugin)
  }
}
```
```kotlin
package kr.hqservice.exampleplugin.service

interface ExampleService : HQService {
  fun doAnything(plugin: Plugin)
}
```
```kotlin
package kr.hqservice.exampleplugin.service.impl

@Component
@HQSingleton(binds = [ExampleService::class])
class ExampleServiceImpl : ExampleService {
  override fun doAnything(plugin: Plugin) {
    println("Hello ${plugin.name}!")
  }
}
```
```kotlin
package kr.hqservice.exampleplugin

class ExamplePlugin : HQBukkitPlugin()
```
> BukkitExampleEvent를 handle 하였을 때 결과:
```
Hello ExamplePlugin!
```
`ExampleListener`의 생성자의 첫번째 인자에는, Singleton으로 `ExampleServiceImpl` 구현체가 주입되었습니다.
두번째 인자의 plugin은, 플러그인 메인 클래스 패키지 기준으로 자동으로 주입됩니다.

---
### 복잡한 의존관계에서의 HQComponent
이번에는 여러 계층의 의존관계를 지닌 컴포넌트들로 예시를 들어보겠습니다.
```kotlin
@Component
class ExampleListener(
  private val exampleService: ExampleService,
  private val exampleConfig: ExampleConfig
) : HQListener {
  @EventHandler
  fun onExampleEvent(event: BukkitExampleEvent) {
    val result = exampleService.printAndReturn()
    if (result == exampleConfig.getConfiguratedString()) {
      println("true")
    }
  }
}
```
```kotlin
interface ExampleService : HQService { fun printAndReturn(): String }
interface ExampleConfig : ConfigurationSection, HQSimpleComponent { fun getConfiguratedString(): String }
```
```kotlin
@Component
@HQSingleton(binds = [ExamplePrimaryService::class])
class ExampleServiceImpl(private val config: ExampleConfig) : ExampleService {
  override fun printAndReturn(): String {
    val string = config.getConfiguratedString()
    println("configuratedString: $string"
    return string
  }
}

@Component
@HQSingleton(binds = [ExampleConfig::class])
class ExampleConfigImpl(private val plugin: Plugin) : ExampleConfig {
  override fun getConfiguratedString(): String {
    return plugin.config.getString("string") ?: throw Exception()
  }
}
```
> BukkitExampleEvent를 handle 하였을 때 결과: 
```
configuratedString: whateverexampleconfiguratedstring
true
```

아래는 3개의 컴포넌트로 이루어져 있는 이 예시의 의존관계 그래프입니다.
```
      +----------------+              +------------------+
      |    Listener    | -----+-----> |      Service     |
      +----------------+      |       +------------------+
                              |                | 
                              |                V
                              |       +------------------+
                              +-----> |      Config      |
                                      +------------------+
```

이 경우에는, `Config` 컴포넌트가 제일 먼저 생성되며, 그 이후로 `Service`, `Listener` 순으로 컴포넌트가 생성됩니다.
생성자 주입을 사용하게 되면, 의존관계를 개발자가 생각할 필요 없이 HQFramework 가 자동으로 정리해줍니다.

---
### HQComponentHandler의 사용
아래는 컴포넌트가 생성될 때 초기화됐음을 출력하는 컴포넌트를 제작하는 예제입니다. HQComponent를 구현하고, HQComponentHandler를 구현 후 ComponentHandler 어노테이션을 달기만 하면 굉장히 간편하게 제작할 수 있습니다.
```kotlin
class ExampleComponent : HQComponent {
  fun getLogger()
  fun getComponentName()

  fun setup()
  fun teardown()
}
```
```kotlin
@ComponentHandler
class ExampleComponentHandler : HQComponentHandler<ExampleComponent> {
  override fun setup(element: ExampleComponent) {
    element.setup()
    element.getLogger().info("${element.getComponentName()} 컴포넌트가 초기화되었습니다.")
  }

  override fun teardown(element: ExampleComponent) {
    element.teardown()
  }
}
```

---
### HQComponentHandler의 초기화 순서 보장
컴포넌트가 초기화 될 때 순서가 중요한 경우가 있습니다. 이럴 경우, ComponentHandler 어노테이션의 depends 인자에 다른 ComponentHandler 의 클래스를 명시하기만 하면 됩니다.
 아래는 초기화 순서 보장에 대한 예제입니다.
```kotlin
class DependedExampleComponent : HQComponent
```
```kotlin
@ComponentHandler(depends = [ExampleComponentHandler::class, ServiceComponentHandler::class])
class DependedExampleComponentHandler : HQComponentHandler<DependedExampleComponent>
```
```
                                      setup
                        <-------------------------------

      +--------------------------+              +-------------------------+
      | DependedComponentHandler | -----+-----> | ExampleComponentHandler |
      +--------------------------+      |       +-------------------------+
                                        |
                                        |
                                        |       +-------------------------+
                                        +-----> | ServiceComponentHandler |
                                                +-------------------------+
                         
                       ------------------------------->
                                    teardown
```

위 예제에서 `DependedComponentHandler` 는 `ExampleComponentHandler` 와 `ServiceComponentHandler` 를 의존하고 있습니다.
 이 경우에는, `ExampleComponentHandler` 와 `ServiceComponentHandler` 의 setup이 끝난 후에 `DependedComponentHandler`가 setup됩니다.
 이와 반대로, teardown 시에는 `ExampleComponentHandler`와 `ServiceComponentHandler` 의 teardown 이전에 `DependedComponentHandler` 가 teardown됩니다.

---
### Qualifier의 사용
컴포넌트를 Bean 으로 사용하게 될 경우, 하나의 인터페이스에 여러개의 definition을 선언해야 하거나 선언하고 싶은 경우가 있습니다.
 이럴때는 Qualifier 을 통해 인스턴스를 가져올 수 있습니다.

---
### Named
Named Qualifier는 Koin의 Qualifier 입니다. HQFramework는 Koin의 Named Qualifier를 지원합니다.
 아래는 Named Qualifier 사용에 대한 예제입니다.
```kotlin
interface ExampleService : HQService { fun get(): String }
```
```kotlin
@Component
@HQSingleton(binds = [ExampleService::class])
@Named("item")
class ItemService : ExampleService { override fun get(): String { return "item" } } 

@Component
@HQSingleton(binds = [ExampleService::class])
@Named("material")
class MaterialService : ExampleService { override fun get(): String { return "material" } }
```
```kotlin
@Component
class ExampleItemListener(@Named("item") private val service: ExampleService) : HQListener {
  @EventHandler
  fun onEvent(event: BukkitExampleEvent) {
    println(service.get())
  }
}
```
> BukkitExampleEvent를 handle 하였을 때 결과:
```
item
```
이처럼 생성자에 원하는 구현체를 Named Qualifier 를 통해 종단에서 주입받을 수 있습니다.

---
### MutableNamed
MutableNamed Qualifier는 HQframework의 Qualifier 입니다. 어노테이션 기반의 Qualifier 에는 로직이 들어갈 수 없는 단점을 보완하기 위하여 만들어졌습니다.
 아래는 MutableNamed Qualifier 사용에 대한 예제입니다.
```kotlin
@QualifierProvider(key = "exampleplugin.data-source.type")
class DataSourceQualifierProvider(private val plugin: Plugin) : MutableNamedProvider {
  override fun provideQualifier(): String {
    return plugin.config.getString("data-source.type")
  }
}
```
> 아래는 예제 플러그인의 config.yml 입니다.
```
data-source:
 type: mysql
```
```kotlin
interface ExampleDataSource : HQDataSource { fun getName(): String }

@Named("mysql")
@Component
@HQSingleton(binds = [ExampleDataSource::class])
class MySQLDataSource : ExampleDataSource { 
  override fun getName(): String { return "mysql datasource" }
}

@Named("sqlite")
@Component
@HQSingleton(binds = [ExampleDataSource::class])
class SQLiteDataSource : ExampleDataSource { 
  override fun getName(): String { return "sqlite datasource" }
}
```
```kotlin
interface ExampleRepository : HQRepository { fun getDataSourceName(): String }

@HQSingleton(binds = [ExampleRepository::class])
@Component
class ExampleRepositoryImpl(
  @MutableNamed(key = "exampleplugin.data-source.type") private val dataSource: ExampleDataSource
) : ExampleRepository {
  override fun getDataSourceName(): String {
    return dataSource.getName()
  }
}
```
```kotlin
@Component
class ExampleListener(private val repository: ExampleRepository) : HQListener {
  @EventHandler
  fun onEvent(event: BukkitExampleEvent) {
    println(repository.getDataSourceName())
  }
}
```
> BukkitExampleEvent를 handle 하였을 때 결과:
```
mysql
```

---
### Packet I/O
### Table of contents
* [Bukkit 서버와 Proxy 서버간의 양방향 통신하기](#bukkit-서버와-proxy-서버간의-양방향-통신하기)
* [NettyChannel 및 NettyPlayer 의 사용](#nettyChannel-및-nettyplayer-의-사용)
* [간결한 방식으로 Packet 선언하기](#간결한-방식으로-packet-선언하기)

HQFramework 를 사용하는 proxy 환경의 모든 서버에서 Netty Module 을 통한 통신이 가능합니다.

---
### Bukkit 서버와 Proxy 서버간의 양방향 통신하기
HQFramework 의 netty 가 활성화 된 proxy 환경의 서버에서는 Packet 을 상속받은 data class 를 다른 채널로 간편하게 보낼 수 있습니다.
또한, Packet 을 read/write 하는 과정에서 간편하게 사용할 수 있는 ByteBuf Extension 을 제공합니다.
 아래는 Packet 을 보내기/받기 전 서버에 register 하는 방법과 보내는 방법에 대한 간단한 예제입니다.
> Packet 클래스는 해당 패킷을 송/수신 하는 모듈에서 공통으로 선언되어야 합니다.
```kotlin
data class ExampleHelloPacket(
 var playerName: String,
 var playerUniqueId: UUID
) : Packet() {
 override fun write(buf: ByteBuf) {
  playerName = buf.readString()
  playerUniqueId = buf.readUUID()
 }
 
 override fun read(buf: ByteBuf) {
  buf.writeString(playerName)
  buf.writeUUID(playerUniqueId)
 }
}
```
> 패킷을 송신 할 모듈의 예제입니다.
```kotlin
@Component
class ExampleNettyModule(
  private val nettyServer: NettyServer
) : HQModule {
  override onEnable() {
    nettyServer.registerOuterPacket(ExampleHelloPacket::class)
  }
}
```
```kotlin
@Component
class ExampleChatListener(
  private val packetSender: PacketSender
) : HQListener {
  @EventHandler
  fun onExampleEvent(event: AsyncChatEvent) {
    val player = event.player
    packetSender.sendPacketToProxy(ExampleHelloPacket(player.name, player.uniqueId))
  }
}
```
> 패킷을 수신 할 모듈의 예제입니다.
```kotlin
@Component
class ExampleNettyModule(
  private val nettyServer: NettyServer,
  private val logger: Logger
) : HQModule {
  override onEnable() {
    nettyServer.registerInnerPacket(ExampleHelloPacket::class) { packet, channelWrapper ->
      logger.info("${packet.playerName}(${packet.playerUniqueId}) 님의 Hello Packet 수신")
    }
  }
}
```

---
### NettyChannel 및 NettyPlayer 의 사용
HQFramework 의 NettyServer 를 통해 연결 된 모든 채널의 정보와 플레이어를 제공합니다.
 아래는 NettyChannel/NettyPlayer 를 이용한 간단한 예제입니다.
```kotlin
@Component
class ExampleListener(
  private val nettyServer: NettyServer,
  private val packetSender: PacketSender
) : HQListener {
  @EventHandler
  fun onExampleEvent(event: AsyncChatEvent) {
    event.isCancelled = true
    val player = event.player
    val myChannel = nettyServer.getPlayer(player.uniqueId)?.getChannel()
    
    val stringMessage = (event.message() as TextComponent).content()
    if (myChannel != null) {
      myChannel.sendMessage("${player.name} 님의 메세지 $stringMessage")
    }
  }
}
```

---
### 간결한 방식으로 Packet 선언하기
HQFramework 는 Netty 채널 간 데이터를 송/수신 할 때, 해당 데이터(bytes)를 다시 객체로, 객체를 데이터로 Encode/Decode 하는 과정에서
 Boilerplate code 를 줄이기 위해 HQFramework 에서는 간편한 방식으로 Packet 을 송/수신 할 수 있도록 도와줍니다.
 먼저, 다른 프로젝트에서 흔히 사용되는 방식을 설명 드리겠습니다.
```kotlin
class PacketPlayOutChat : Packet<PacketListenerPlayOut> {
  lateinit var a: IChatBaseComponent
  lateinit var b: ChatMessageType
  
  constructor()
  // 중략
  constructor(var1: IChatBaseComponent, var2: ChatMessageType) {
    a = var1
    b = var2
  }
  
  // Packet 을 read 하는 method
  fun a(var1: PacketDataSerializer) {
    this.a = var1.f()
    this.b = ChatMessageType.a(var1.readByte())
  }
  
  // Packet 을 write 하는 method
  fun b(var1: PacketDataSerializer) {
    var1.a(this.a)
    var1.writeByte(this.b.a())
  }
}
```
> 위의 코드는 net.minecraft.server.v1_12_R1 내부 서버 구현체에서 발췌 하였습니다.

위의 코드는 패킷을 읽을 때 비어있는 생성자를 통하여 인스턴스를 생성하고, 그 인스턴스의 필드를 read 하는 method 를 통해 decoding 하는 방식입니다.
 통용적으로 사용되는 방식이나, Boilerplate 인 비어있는 constructor 에 대한 작성이 매 Packet 클래스마다 요구됩니다.
 아래는 HQFramework 가 ByteBuddy 를 사용하여 위의 클래스를 저희의 방식으로 정의했을 때의 대한 예제입니다.
```kotlin
data class PacketPlayOutChat(
  var a: IChatBaseComponent
  var b: ChatMessageType
): Packet() {
  override fun read(byteBuf: ByteBuf) {
    this.a = byteBuf.readIChatBaseComponent()
    this.b = ChatMessageType.a(byteBuf.readByte())
  }
  
  override fun write(byteBuf: ByteBuf) {
    byteBuf.writeIChatBaseComponent(this.a)
    byteBuf.writeByte(this.b.a())
  }
}
```
이 방식은 비어있는 constructor 를 가진 class 를 재정의하여, read method 를 통해 수신받은 데이터를 실제 Packet class 의 생성자에 주입하는 방식으로 구현 되었습니다.

---
### NMS
### Table of contents
* [간결한 NMS-ItemStack 편집](#nms-itemstack-편집)
* [NMS-Packet 을 쉽게 보내기](#nms-packet-을-virtual-을-통해-Client-Side-로-보내기)

HQFramework 를 사용하여 개발하기 껄끄러웠던 NMS 단의 코드를 더 쉽게 사용하여, 개발 경험을 더 풍부하게 늘릴 수 있습니다.

---
### NMS ItemStack 편집
HQFramework 에서는 NMS 의 구현부 없이 Bukkit-APi 의 ItemStack 을 통해서도 NMS 단의 코드 사용을 지원합니다.
 아래는 ItemStack 의 NBTTagCompound 를 편집하는 간단한 예제입니다.
> NBTTagCompound 에 Key 와 Value 설정을 간단하게 적용할 수 있습니다.
```kotlin
@Component
class ExampleItemListener : HQListener {
  @EventHandler
  fun exampleSetString(event: PlayerInteractEvent) {
    val player = event.player
    val itemStack = player.inventory.itemInMainHand
    itemStack.nms {
      tag {
        setString("exampleKey", "exampleValue")
      }
    }
  }
}
```
> 반대로 NBTTagCompound 의 값을 읽어 올 때는 아래와 같은 방식으로도 접근이 가능합니다.
```kotlin
@Component
class ExampleItemListener : HQListener {
  @EventHandler
  fun exampleGetString(event: PlayerInteractEvent) {
    val player = event.player
    val itemStack = player.inventory.itemInMainHand
    val nmsItemStack = itemStack.getNmsItemStack()
    if (nmsItemStack.hasTag()) {
      val nbtTagCompound = nmsItemStack.getTag()
      if (nbtTagCompound.hasKey("exampleKey")) {
        player.sendMessage(nbtTagCompound.getString("exampleKey"))
      }
    }
  }
}
```

---
### NMS Packet 을 Virtual 을 통해 Client-Side 로 보내기
HQFramework 를 통해 NMS 의 Packet 을 Bukkit-API 의 코드만으로 쉽게 생성하여 서버에 보낼 수 있습니다.
HQFramework 에서는 Client-Side 로 NMS Packet 을 보내는 과정을 Virtual 로 정의하였습니다.
 아래는 플레이어가 보고있는 인벤토리에 가상으로 아이템을 설정하는 간단한 예제입니다.
> 플레이어가 인벤토리를 클릭하면 해당 슬롯에 Barrier 를 Client-Side 로 설정합니다.
```kotlin
@Component
class ExampleItemListener : HQListener {
  @EventHandler
  fun exampleVirtualItem(event: InventoryClickEvent) {
    event.isCancelled = true
    val player = event.whoClicked as Player
    val slot = event.rawSlot
    player.virtual {
      inventory {
        setItem(slot, ItemStack(Material.BARRIER)
      }
    }
  }
}
```
> 이번에는 플레이어가 접속하면 플레이어가 있는 위치에 가상의 ArmorStand 를 소환 해보겠습니다.
```kotlin
@Component
class ExampleJoinListener : HQListener {
  @EventHandler
  fun exampleVirtualArmorStand(event: PlayerJoinEvent) {
    val player = event.player
    player.virtual {
      val virtualEntity = VirtualArmorStand(player.location, "&7VirtualEntity &eName")
      updateEntity(virtualEntity)
      delay(1000)
      virtualEntity.setName("&7VirtualEntity &aNewName")
      updateEntity(virtualEntity)
    }
  }
}
```
