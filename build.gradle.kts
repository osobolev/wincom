description = "Windows COM wrappers"

plugins {
    `base-lib`
}

group = "io.github.osobolev"
version = "2.4"

dependencies {
    api("io.github.osobolev:jacob:1.20")
}

tasks.jar {
    manifest.attributes["Automatic-Module-Name"] = "${project.group}.${project.name}"
}

(publishing.publications["mavenJava"] as MavenPublication).pom {
    name.set("wincom")
    description.set("Thread-safe wrappers for Windows COM objects")
    url.set("https://github.com/osobolev/wincom")
    licenses {
        license {
            name.set("The Apache License, Version 2.0")
            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
        }
    }
    developers {
        developer {
            name.set("Oleg Sobolev")
            organizationUrl.set("https://github.com/osobolev")
        }
    }
    scm {
        connection.set("scm:git:https://github.com/osobolev/wincom.git")
        developerConnection.set("scm:git:https://github.com/osobolev/wincom.git")
        url.set("https://github.com/osobolev/wincom")
    }
}
