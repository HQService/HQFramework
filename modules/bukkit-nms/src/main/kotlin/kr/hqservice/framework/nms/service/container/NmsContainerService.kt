package kr.hqservice.framework.nms.service.container

import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.Service
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.ContainerWrapper
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.container.ContainerWrapperImpl
import org.bukkit.entity.Player
import kotlin.reflect.KClass

@Qualifier("container")
@Service
interface NmsContainerService : NmsService<Player, ContainerWrapper>