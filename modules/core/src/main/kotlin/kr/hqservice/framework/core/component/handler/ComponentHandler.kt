package kr.hqservice.framework.core.component.handler

import kotlin.reflect.KClass

/**
 * <ComponentHandler 끼리의 의존관계에 관한 서술>
 *
 * 하나의 ComponentHandler 는 다른 여러개의 ComponentHandler 들을 의존할 수 있습니다.
 *
 * ComponentHandler 의 setup 의 경우: 의존하고 있는 ComponentHandler 들이 setup 된 후에 setup 됩니다.
 *      ex) Repository 와 DataSource, TableInitializer 의 경우,
 *          Repository 는 DataSource 와 TableInitializer 가 setup 된 후에 setup 되어야 합니다.
 *          그러므로, Repository 는 DataSource 와 TableInitializer 를 의존하고 있다고 할 수 있습니다.
 *
 * ComponentHandler 의 teardown 의 경우: 의존하고 있는 ComponentHandler 들이 teardown 되기 전에 teardown 됩니다.
 *      ex) Repository 와 DataSource, TableInitializer 의 경우,
 *          DataSource 와 TableInitializer 는 Repository 가 teardown 이 끝난 후 teardown 되어야 합니다.
 *          그러므로, Repository 는 DataSource 와 TableInitializer 를 의존하고 있다고 할 수 있습니다.
 *
 *                           setup
 *         <----------------------------------------
 *
 *     +----------------+    depend    +------------------+
 *     |   Repository   | -----+-----> |    DataSource    |
 *     +----------------+      |       +------------------+
 *                             |
 *                             |       +------------------+
 *                             +-----> | TableInitializer |
 *                                     +------------------+
 *         ---------------------------------------->
 *                         teardown
 *
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ComponentHandler(val depends: Array<KClass<*>> = [])
