plugins {
    `java-library`
    `maven-publish`

    id("com.gradleup.shadow") version "8.3.5"
    id("io.papermc.paperweight.userdev") version "1.6.3"
    id("xyz.jpenilla.run-paper") version "2.2.4"
}

group = "lol.ysmu"
version = "0.0.3"
description = "PingFFA"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://oss.sonatype.org/content/groups/public/") }

    // ProtocolLib needs both the public and the snapshot repository
    maven { url = uri("https://repo.dmulloy2.net/repository/public/") }
    maven { url = uri("https://repo.dmulloy2.net/repository/snapshots/") }

    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") }
    maven { url = uri("https://maven.enginehub.org/repo/") }
    maven { url = uri("https://repo.maven.apache.org/maven2/") }
    maven("https://repo.xyrisdev.com/repository/maven-public/")
}

dependencies {
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.apache.httpcomponents:httpmime:4.5.6")

    paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT")

    compileOnly("net.dmulloy2:ProtocolLib:5.4.0")
    compileOnly("me.clip:placeholderapi:2.11.5")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.8")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core:2.7.0")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit:2.7.0")

    // Ensure you've run 'publishToMavenLocal' on the XyrisKits-API project first
    compileOnly("dev.darkxx:XyrisKits-API:1.0.0")
}

tasks.shadowJar {
    minimize()
    archiveFileName.set("${project.name}-${project.version}.jar")
    //relocate("com.zaxxer.hikari", "lol.ysmu.ffa.shaded.hikari")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
        artifact(tasks.shadowJar)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

layout.buildDirectory.set(layout.projectDirectory.dir("output"))