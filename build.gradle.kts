import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar

description = "Windows COM wrappers"

plugins {
    id("com.vanniktech.maven.publish") version "0.37.0"
    id("module-lib")
}

group = "io.github.osobolev"
version = "2.5"

dependencies {
    api("io.github.osobolev:jacob:1.21")
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates("${project.group}", "${project.name}", "${project.version}")
    configure(JavaLibrary(
        javadocJar = JavadocJar.Javadoc(),
        sourcesJar = SourcesJar.Sources()
    ))
}

mavenPublishing.pom {
    name = "wincom"
    description = "Thread-safe wrappers for Windows COM objects"
    url = "https://github.com/osobolev/wincom"
    licenses {
        license {
            name = "The Apache License, Version 2.0"
            url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
        }
    }
    developers {
        developer {
            name = "Oleg Sobolev"
            organizationUrl = "https://github.com/osobolev"
        }
    }
    scm {
        connection = "scm:git:https://github.com/osobolev/wincom.git"
        developerConnection = "scm:git:https://github.com/osobolev/wincom.git"
        url = "https://github.com/osobolev/wincom"
    }
}
