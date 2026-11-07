plugins {
    java
    id("xyz.jpenilla.run-velocity") version "3.1.0"
}
repositories {
    maven("https://repo.papermc.io/repository/maven-public/") { name = "papermc" }
}
dependencies {
    compileOnly("com.velocitypowered:velocity-api:4.3.1")
    annotationProcessor("com.velocitypowered:velocity-api:4.3.1") // generates velocity-plugin.json
}
java { toolchain.languageVersion = JavaLanguageVersion.of(25) }
tasks.compileJava { options.release = 25 }
tasks.runVelocity { velocityVersion("4.3.1") }
