package kr.hqservice.framework.nms.service.container

import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.ContainerWrapper
import org.bukkit.entity.Player

interface NmsContainerService : NmsService<Player, ContainerWrapper>