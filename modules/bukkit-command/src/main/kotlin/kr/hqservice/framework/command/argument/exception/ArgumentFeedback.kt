package kr.hqservice.framework.command.argument.exception

@Suppress("JavaIoSerializableObjectMustHaveReadResolve")
object ArgumentFeedback {
    class Message(
        override val message: String
    ) : Throwable()

    object RequireArgument : Throwable()
    object NotNumber : Throwable()
    object NotBoolean : Throwable()
    object PlayerNotFound : Throwable()
}