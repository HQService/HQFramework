package kr.hqservice.framework.nms.service.world

import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.Service
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.world.WorldWrapper
import org.bukkit.World

@Qualifier("nms.world")
@Service
interface WorldService : NmsService<World, WorldWrapper>