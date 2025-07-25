///////////////////////////////////////////////////////////////////////////////
//  GRADLE CONFIGURATION
///////////////////////////////////////////////////////////////////////////////
plugins {
  kotlin("jvm") version "1.7.0"
  id("com.diffplug.spotless") version "7.0.4"
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

val javaSourceLevel: String by project
val javaTargetLevel: String by project

java {
  sourceCompatibility = JavaVersion.toVersion(javaSourceLevel)
  targetCompatibility = JavaVersion.toVersion(javaTargetLevel)
  println("Compiling Java $sourceCompatibility to Java $targetCompatibility.")
}

tasks {
  spotless {
    kotlin {
      target("src/**/*.kt")
      licenseHeaderFile("../../LICENSE_HEADER")
      ktfmt()
    }
    kotlinGradle {
      target("**/*.kts")
      ktfmt()
    }
  }
  test {
    useJUnitPlatform()
    testLogging { events("passed", "skipped", "failed") }
  }
}
