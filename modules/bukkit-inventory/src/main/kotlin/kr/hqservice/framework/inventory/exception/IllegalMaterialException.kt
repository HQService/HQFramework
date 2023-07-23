package kr.hqservice.framework.inventory.exception

import org.bukkit.Material

class IllegalMaterialException(
    material: Material
) : Exception("'$material' 타입의 버튼을 생성할 수 없습니다.")