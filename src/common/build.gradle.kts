///////////////////////////////////////////////////////////////////////////////
//  GRADLE CONFIGURATION
///////////////////////////////////////////////////////////////////////////////
plugins {
  kotlin("jvm") version "1.7.0"
  `maven-publish`
  signing
  id("com.diffplug.spotless") version "7.0.4"
  id("org.jetbrains.dokka") version "1.9.20"
}

///////////////////////////////////////////////////////////////////////////////
//  APP CONFIGURATION
///////////////////////////////////////////////////////////////////////////////

dependencies {
  implementation("com.github.devnied:bit-lib4j:1.4.5") { exclude(group = "org.slf4j") }
  implementation("org.eclipse.keyple:keyple-util-java-lib:2.4.0")
  testImplementation(kotlin("test"))
  testImplementation("org.assertj:assertj-core:3.15.0")
  testImplementation("com.github.devnied:bit-lib4j:1.4.5")
}

///////////////////////////////////////////////////////////////////////////////
//  STANDARD CONFIGURATION FOR KOTLIN PROJECTS
///////////////////////////////////////////////////////////////////////////////

if (project.hasProperty("releaseTag")) {
  project.version = project.property("releaseTag") as String
  println("Release mode: version set to ${project.version}")
} else {
  println("Development mode: version is ${project.version}")
}

val title: String by project
val javaSourceLevel: String by project
val javaTargetLevel: String by project
val generatedOverviewFile = layout.buildDirectory.file("tmp/overview-dokka.md")
val dokkaOutputDir = layout.buildDirectory.dir("dokkaHtml")

java {
  sourceCompatibility = JavaVersion.toVersion(javaSourceLevel)
  targetCompatibility = JavaVersion.toVersion(javaTargetLevel)
  println("Compiling Java $sourceCompatibility to Java $targetCompatibility.")
  withJavadocJar()
  withSourcesJar()
}

fun copyLicenseFiles() {
  val metaInfDir = File(layout.buildDirectory.get().asFile, "resources/main/META-INF")
  val licenseFile = File("${project.rootDir}/../../", "LICENSE")
  val noticeFile = File("${project.rootDir}/../../", "NOTICE.md")
  metaInfDir.mkdirs()
  licenseFile.copyTo(File(metaInfDir, "LICENSE"), overwrite = true)
  noticeFile.copyTo(File(metaInfDir, "NOTICE.md"), overwrite = true)
}

tasks {
  spotless {
    kotlin {
      target("src/**/*.kt")
      licenseHeaderFile("$${project.rootDir}/../../LICENSE_HEADER")
      ktfmt()
    }
    kotlinGradle {
      target("**/*.kts")
      ktfmt()
    }
  }
  register("generateDokkaOverview") {
    outputs.file(generatedOverviewFile)
    doLast {
      val file = generatedOverviewFile.get().asFile
      file.parentFile.mkdirs()
      file.writeText(
          buildString {
            appendLine("# Module $title")
            appendLine()
            appendLine(
                file("src/main/kdoc/overview.md")
                    .takeIf { it.exists() }
                    ?.readText()
                    .orEmpty()
                    .trim())
            appendLine()
            appendLine("<br>")
            appendLine()
            appendLine("> ${project.findProperty("javadoc.copyright") as String}")
          })
    }
  }
  named<org.jetbrains.dokka.gradle.DokkaTask>("dokkaHtml") {
    dependsOn("generateDokkaOverview")
    outputDirectory.set(dokkaOutputDir.get().asFile)
    dokkaSourceSets {
      named("main") {
        noAndroidSdkLink.set(false)
        includeNonPublic.set(false)
        includes.from(files(generatedOverviewFile))
        moduleName.set(title)
      }
    }
    doFirst { println("Generating Dokka HTML for ${project.name} version ${project.version}") }
  }
  jar {
    dependsOn(processResources)
    doFirst { copyLicenseFiles() }
    manifest {
      attributes(
          mapOf(
              "Implementation-Title" to (project.findProperty("title") as String),
              "Implementation-Version" to project.version,
              "Implementation-Vendor" to (project.findProperty("organization.name") as String),
              "Implementation-URL" to (project.findProperty("project.url") as String),
              "Specification-Title" to (project.findProperty("title") as String),
              "Specification-Version" to project.version,
              "Specification-Vendor" to (project.findProperty("organization.name") as String),
              "Created-By" to
                  "${System.getProperty("java.version")} (${System.getProperty("java.vendor")})",
              "Build-Jdk" to System.getProperty("java.version")))
    }
  }
  named<Jar>("sourcesJar") {
    dependsOn(processResources)
    from(layout.buildDirectory.dir("resources/main"))
    doFirst { copyLicenseFiles() }
    manifest {
      attributes(
          mapOf(
              "Implementation-Title" to "$title Documentation",
              "Implementation-Version" to project.version))
    }
  }
  named<Jar>("javadocJar") {
    dependsOn(dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaOutputDir)
    from(layout.buildDirectory.dir("resources/main"))
    doFirst { copyLicenseFiles() }
    manifest {
      attributes(
          mapOf(
              "Implementation-Title" to "$title Documentation",
              "Implementation-Version" to project.version))
    }
  }
  test {
    useJUnitPlatform()
    testLogging { events("passed", "skipped", "failed") }
  }
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      from(components["java"])
      pom {
        name.set(project.findProperty("title") as String)
        description.set(project.findProperty("description") as String)
        url.set(project.findProperty("project.url") as String)
        licenses {
          license {
            name.set(project.findProperty("license.name") as String)
            url.set(project.findProperty("license.url") as String)
            distribution.set(project.findProperty("license.distribution") as String)
          }
        }
        developers {
          developer {
            name.set(project.findProperty("developer.name") as String)
            email.set(project.findProperty("developer.email") as String)
          }
        }
        organization {
          name.set(project.findProperty("organization.name") as String)
          url.set(project.findProperty("organization.url") as String)
        }
        scm {
          connection.set(project.findProperty("scm.connection") as String)
          developerConnection.set(project.findProperty("scm.developerConnection") as String)
          url.set(project.findProperty("scm.url") as String)
        }
        ciManagement {
          system.set(project.findProperty("ci.system") as String)
          url.set(project.findProperty("ci.url") as String)
        }
        properties.set(
            mapOf(
                "project.build.sourceEncoding" to "UTF-8",
                "maven.compiler.source" to javaSourceLevel,
                "maven.compiler.target" to javaTargetLevel))
      }
    }
  }
  repositories {
    maven {
      if (project.hasProperty("sonatypeURL")) {
        url = uri(project.property("sonatypeURL") as String)
        credentials {
          username = project.property("sonatypeUsername") as String
          password = project.property("sonatypePassword") as String
        }
      }
    }
  }
}

signing {
  if (project.hasProperty("releaseTag")) {
    useGpgCmd()
    sign(publishing.publications["mavenJava"])
  }
}
