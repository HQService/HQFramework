group = extra["projectGroup"]!!
version = extra["projectVersion"]!!

subprojects {
    plugins.apply(JavaPlugin::class.java)
}