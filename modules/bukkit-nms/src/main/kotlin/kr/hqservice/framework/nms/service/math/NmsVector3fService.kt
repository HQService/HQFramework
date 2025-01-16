package kr.hqservice.framework.nms.service.math

import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.math.Vector3fWrapper

interface NmsVector3fService : NmsService<Triple<Float, Float, Float>, Vector3fWrapper>