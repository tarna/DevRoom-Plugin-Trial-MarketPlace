plugins {
    kotlin("jvm") version libs.versions.kotlin
    kotlin("plugin.serialization") version libs.versions.kotlin
    alias(libs.plugins.shadow)
    alias(libs.plugins.pluginyml)
    alias(libs.plugins.runtask)
}

group = "dev.tarna"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly(libs.paper)
    compileOnly(libs.vaultapi)
    implementation(libs.mcchestuiplus)
    implementation(libs.bundles.mccoroutine)
    implementation(libs.bundles.cloud)
    implementation(libs.bundles.mongodb)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.kreds)
    implementation(libs.discordwebhook)
}

bukkit {
    name = "MarketPlace"
    version = project.version.toString()
    description = "DevRoom Plugin Development Trial Marketplace Plugin"
    author = "Tarna"
    main = "dev.tarna.marketplace.MarketPlacePlugin"
    apiVersion = "1.21"
    libraries = listOf(
        "com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.21.0",
        "com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.21.0"
    )
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks {
    build {
        dependsOn("shadowJar")
    }

    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    runServer {
        minecraftVersion("1.21")
    }
}