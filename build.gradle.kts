description = "Windows COM wrappers"

plugins {
    `java-library`
    `maven-publish`
    `signing`
}

group = "io.github.osobolev"
version = "2.0"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
    withJavadocJar()
}

sourceSets {
    main {
        java.srcDir("src")
    }
}

dependencies {
    api("net.sf.jacob-project:jacob:1.14.3")
}

tasks {
    withType(JavaCompile::class) {
        options.encoding = "UTF-8"
    }
    javadoc {
        (options as CoreJavadocOptions).addBooleanOption("Xdoclint:none", true)
        options.quiet()
    }
    jar {
        manifest.attributes["Implementation-Version"] = project.version
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            pom {
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
            from(components["java"])
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

tasks.named("clean").configure {
    doLast {
        project.delete("$projectDir/out")
    }
}
