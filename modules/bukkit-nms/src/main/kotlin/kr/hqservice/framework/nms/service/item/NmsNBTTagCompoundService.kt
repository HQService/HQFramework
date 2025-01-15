package kr.hqservice.framework.nms.service.item

import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.Service
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.item.NmsNBTTagCompoundWrapper

@Service
@Qualifier("tag")
interface NmsNBTTagCompoundService : NmsService<Any?, NmsNBTTagCompoundWrapper>