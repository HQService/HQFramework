# HQFramework

### Bukkit 및 Proxy 플랫폼을 더 객체지향적으로 만들어줍니다.

HQFramework는 SpringFramework 에서 영감을 받아 제작된 프레임워크입니다. 
 스프링의 기능 여러가지를 Bukkit 및 Proxy 플랫폼에 맞게끔 구현하였습니다.

## Features
* [Component](#component)

## Component
### 목차
* [HQComponent 예제](#hqcomponent-예제)
* [복잡한 의존관계에서의 HQComponent](#복잡한-의존관계에서의-hqcomponent)
* [HQComponentHandler 예제](#hqcomponenthandler-예제)
* [HQComponentHandler의 초기화 순서보장](#hqcomponenthandler의-초기화-순서-보장)

HQFramework는 완성도가 높은 의존성 주입 라이브러리와 연결할 수 있는 Component 기능을 제공합니다.
 Component는 Singleton의 형태로 자동 생성되며, 의존성 주입과 함께 생성되며, 필요에 따라 생성된 Component를 Bean으로 등록해주기도 합니다.
 
---
### HQComponent 예제
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

class ExamplePlugin : HQPlugin()
```
> BukkitExampleEvent를 handle 하였을 때 결과:
```
Hello ExamplePlugin!
```
ExampleListener의 생성자의 첫번째 인자에는, Singleton으로 ExampleServiceImpl 구현체가 주입되었습니다.
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
class ExamplePrimaryServiceImpl(private val config: ExampleConfig) : ExamplePrimaryService {
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
                              |                |  
                              |                V
                              |       +------------------+
                              +-----> |      Config      |
                                      +------------------+
```

이 경우에는, Config 컴포넌트가 제일 먼저 생성되며, 그 이후로 Service, Listener 순으로 컴포넌트가 생성됩니다.
생성자 주입을 사용하게 되면, 의존관계를 개발자가 생각할 필요 없이 HQFramework 가 자동으로 정리해줍니다.

---
### HQComponentHandler 예제
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
컴포넌트가 초기화될때 순서가 중요한 경우가 있습니다. 이럴 경우, ComponentHandler 어노테이션의 depends 인자에 다른 ComponentHandler 의 클래스를 명시하기만 하면 됩니다.

```kotlin
class ExampleComponentDependedComponent : HQComponent
```
```kotlin
@ComponentHandler(depends = [ExampleComponentHandler::class])
class ExampleComponentDependedComponentHandler : HQComponentHandler<ExampleComponentDependedComponent>
```
이렇게 의존한 경우, 컴포넌트 핸들러의 teardown 시에는 역순으로 동작하게 됩니다.

