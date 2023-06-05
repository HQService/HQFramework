# HQFramework / Next Generation Bukkit/Proxy Development Framework
[![GitHub license](https://img.shields.io/badge/license-GPL%20v3-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
[![Kotlin](https://img.shields.io/badge/kotlin-1.7.21-blue.svg?logo=kotlin)](http://kotlinlang.org)
### Bukkit 및 Proxy 플랫폼을 더 생산적, 직관적, 객체지향적으로 만들어줍니다.

HQFramework는 SpringFramework 에서 영감을 받아 Bukkit 및 Proxy 플랫폼에서의 동작을 위해 제작된 프레임워크입니다. 
 스프링의 기능 여러가지를 Bukkit 및 Proxy 플랫폼에 맞게끔 구현하였습니다.

## Features
* [Component](#component)
    
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
 Component는 Singleton의 형태로 자동 생성되며, 의존성 주입과 함께 생성되며, 필요에 따라 생성된 Component를 Bean으로 등록해주기도 합니다.
 
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
    println("Hello ${plugin.name}!"
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
