package kr.hqservice.framework.nms.service.math

import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.Service
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.math.Vector3fWrapper

@Qualifier("vector3f")
@Service
interface Vector3fService : NmsService<Triple<Float, Float, Float>, Vector3fWrapper>