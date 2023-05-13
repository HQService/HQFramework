package kr.hqservice.framework.database.component.handler

import kr.hqservice.framework.core.component.handler.ComponentHandler
import kr.hqservice.framework.core.component.handler.HQComponentHandler
import kr.hqservice.framework.database.component.HQEntityClass

@ComponentHandler(depends = [DataSourceComponentHandler::class])
class EntityClassComponentHandler : HQComponentHandler<HQEntityClass<*, *>>