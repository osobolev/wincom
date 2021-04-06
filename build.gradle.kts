description = "Windows COM wrappers"

plugins {
    `java-library`
    `maven-publish`
}

group = "com.github.osobolev"
version = "2.0"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    withType(JavaCompile::class) {
        options.encoding = "UTF-8"
    }
    jar {
        manifest.attributes["Implementation-Version"] = project.version
    }
}

sourceSets {
    main {
        java.srcDir("src")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    api("net.sf.jacob-project:jacob:1.14.3")
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
