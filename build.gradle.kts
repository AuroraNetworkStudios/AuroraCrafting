import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import xyz.jpenilla.runtask.task.AbstractRun
import java.net.URI
import java.util.*

fun loadProperties(filename: String): Properties {
    val properties = Properties()
    if (!file(filename).exists()) {
        return properties
    }
    file(filename).inputStream().use { properties.load(it) }
    return properties
}

plugins {
    id("java")
    id("com.gradleup.shadow") version "9.4.2"
    id("maven-publish")
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = "gg.auroramc"

val baseVersion = providers.gradleProperty("baseVersion").get()
val supportedMinecraftVersions = providers.gradleProperty("supportedMinecraftVersions")
    .map { versions ->
        versions.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
    .get()
val ciBuildNumber = providers.gradleProperty("buildNumber")
    .orElse(providers.environmentVariable("GITHUB_RUN_NUMBER"))
    .map { it.trim() }
    .orNull

version = if (ciBuildNumber.isNullOrBlank()) {
    baseVersion
} else if (baseVersion.endsWith("-SNAPSHOT")) {
    "${baseVersion.removeSuffix("-SNAPSHOT")}-b$ciBuildNumber-SNAPSHOT"
} else {
    "$baseVersion-b$ciBuildNumber"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

repositories {
    flatDir {
        dirs("libs")
    }
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.auroramc.gg/releases/")
    maven("https://repo.auroramc.gg/snapshots/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    // Quests (pikamug)
    maven("https://repo.codemc.io/repository/maven-public/")
    // BetonQuest (2)
    maven("https://repo.betonquest.org/betonquest/")
    maven("https://repo.dmulloy2.net/nexus/repository/public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.31-alpha")
    compileOnly("gg.auroramc:Aurora:2.6.0-SNAPSHOT")
    compileOnly("gg.auroramc:AuroraQuests:2.0.0")
    // Quests
    compileOnly("me.pikamug.quests:quests-core:5.1.4")
    // Quests (LMBishop)
    compileOnly(files("libs/Quests-3.15.2-lmbishop.jar"))
    // BetonQuest (2)
    compileOnly("org.betonquest:betonquest:2.1.3") {
        exclude("com.comphenix.packetwrapper")
    }
    compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0")
    // ItemsAdder
    compileOnly("com.github.LoneDev6:api-itemsadder:3.6.1")
    // HeadDatabase
    compileOnly("com.arcaniax:HeadDatabase-API:1.3.2")
    // Jobs
    compileOnly(files("libs/Jobs5.2.4.6.jar"))
    // AdvancedEnchantments
    compileOnly(files("libs/AdvancedEnchantments-8.7.4.jar"))

    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:3.0.2")

    compileOnly("org.projectlombok:lombok:1.18.46")
    annotationProcessor("org.projectlombok:lombok:1.18.46")

    testImplementation("io.papermc.paper:paper-api:26.2.build.31-alpha")
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.1.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.withType<ShadowJar> {
    archiveFileName.set("AuroraCrafting-${project.version}.jar")

    manifest {
        attributes["paperweight-mappings-namespace"] = "mojang"
    }

    relocate("co.aikar.commands", "gg.auroramc.crafting.libs.acf")
    relocate("co.aikar.locales", "gg.auroramc.crafting.libs.locales")
    relocate("org.bstats", "gg.auroramc.crafting.libs.bstats")

    exclude("acf-*.properties")
}

tasks.processResources {
    filteringCharset = "UTF-8"
    inputs.property("version", project.version)
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}

tasks {
    build {
        dependsOn(shadowJar)
        dependsOn("apiJar")
    }
    runServer {
        minecraftVersion(supportedMinecraftVersions.last())
    }
}

tasks.register<Jar>("apiJar") {
    archiveBaseName.set("AuroraCraftingAPI")

    from(sourceSets.main.get().output) {
        include("gg/auroramc/crafting/api/**")
    }
}

val publishing = loadProperties("publish.properties")

publishing {
    repositories {
        maven {
            name = "AuroraMC"
            url = if (version.toString().endsWith("SNAPSHOT")) {
                URI.create("https://repo.auroramc.gg/snapshots/")
            } else {
                URI.create("https://repo.auroramc.gg/releases/")
            }
            credentials {
                username = publishing.getProperty("username")
                password = publishing.getProperty("password")
            }
        }
    }

    publications.create<MavenPublication>("mavenJava") {
        groupId = "gg.auroramc"
        artifactId = "AuroraCraftingAPI"
        version = project.version.toString()

        artifact(tasks.named("apiJar"))
    }
}

tasks.withType<AbstractRun>().configureEach {
//    javaLauncher = javaToolchains.launcherFor {
//        vendor.set(JvmVendorSpec.JETBRAINS)
//        languageVersion.set(JavaLanguageVersion.of(25))
//    }
    jvmArgs(
        // "-XX:+AllowEnhancedClassRedefinition", //
        "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005" // Enable remote debugging
    )
}
