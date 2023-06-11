package kr.hqservice.framework.command

import kr.hqservice.framework.command.component.HQCommandRoot
import kr.hqservice.framework.command.component.HQCommandTree
import kr.hqservice.framework.command.component.ParentCommand
import kr.hqservice.framework.global.core.component.Component

@Component
class HQFrameworkCommand : HQCommandRoot("hq") {
    @Component
    @ParentCommand(binds = [HQFrameworkCommand::class])
    class Plugin : HQCommandTree("plugin") {
        @Component
        @ParentCommand(binds = [HQFrameworkCommand.Plugin::class])
        class Description : HQCommandTree("description")
    }
}