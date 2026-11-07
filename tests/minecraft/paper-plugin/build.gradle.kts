plugins {
    java
    id("xyz.jpenilla.run-paper") version "3.1.0"
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") { name = "papermc" }
}

dependencies {
    // Pin for reproducible CI; use "26.3.build.+" to track latest (never "latest.release": metadata <latest> is 26.3-pre-2.build.0-alpha)
    compileOnly("io.papermc.paper:paper-api:26.3.build.244-stable")
    // compileOnly does not reach the test classpath, and MockBukkit does not bring paper-api itself
    testImplementation("io.papermc.paper:paper-api:26.3.build.244-stable")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v26.3:4.119.0")
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java { toolchain.languageVersion = JavaLanguageVersion.of(25) }

tasks {
    compileJava {
        options.release = 25
        options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:removal"))
    }
    processResources {
        val props = mapOf("version" to project.version)
        inputs.properties(props)
        filesMatching("paper-plugin.yml") { expand(props) }
    }
    test { useJUnitPlatform() }
    runServer {
        minecraftVersion("26.3")
        jvmArgs("-Dcom.mojang.eula.agree=true")
    }
}
