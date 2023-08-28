package kr.hqservice.framework.nms.virtual.scope.impl

import kr.hqservice.framework.nms.virtual.scope.AbstractVirtualScope
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import org.bukkit.entity.Player

class SingleVirtualScope(
    receiver: Player,
    reflectionWrapper: NmsReflectionWrapper,
) : AbstractVirtualScope(listOf(receiver), reflectionWrapper)