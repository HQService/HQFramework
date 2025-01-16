package kr.hqservice.framework.nms.service.container

import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.Service
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.ContainerWrapper
import org.bukkit.entity.Player

@Qualifier("container")
@Service
interface NmsContainerService : NmsService<Player, ContainerWrapper>