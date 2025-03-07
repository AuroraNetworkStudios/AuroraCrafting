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
    id("com.gradleup.shadow") version "8.3.3"
    id("maven-publish")
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "gg.auroramc"
version = "2.0.0"

java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21

repositories {
    flatDir {
        dirs("libs")
    }
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.auroramc.gg/releases/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    // Quests (pikamug)
    maven("https://repo.codemc.io/repository/maven-public/")
    // BetonQuest (2)
    maven("https://nexus.betonquest.org/repository/betonquest/")
    maven("https://repo.dmulloy2.net/nexus/repository/public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly("gg.auroramc:Aurora:2.1.6")
    compileOnly("gg.auroramc:AuroraQuests:1.3.16")
    // Quests
    compileOnly("me.pikamug.quests:quests-core:5.1.4")
    // Quests (LMBishop)
    compileOnly(name = "Quests-3.15.2-lmbishop", group = "com.leonardobishop", version = "3.15.2")
    // BetonQuest (2)
    compileOnly("org.betonquest:betonquest:2.1.3")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0")
    // Jobs
    compileOnly(name = "Jobs5.2.4.6", group = "com.github.Zrips", version = "5.2.4.6")

    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:3.0.2")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
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
        downloadPlugins {
            modrinth("AuroraLib", "2.1.6")
        }
        minecraftVersion("1.21.4")
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
//        languageVersion.set(JavaLanguageVersion.of(21))
//    }
    jvmArgs(
        // "-XX:+AllowEnhancedClassRedefinition", //
        "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005" // Enable remote debugging
    )
}