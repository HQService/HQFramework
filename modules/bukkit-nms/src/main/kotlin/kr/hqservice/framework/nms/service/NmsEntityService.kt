package kr.hqservice.framework.nms.service

import kr.hqservice.framework.nms.wrapper.EntityWrapper
import org.bukkit.Location

interface NmsEntityService<W: EntityWrapper> : NmsService<Location, W>