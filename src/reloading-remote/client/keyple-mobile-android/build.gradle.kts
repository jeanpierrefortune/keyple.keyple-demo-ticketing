///////////////////////////////////////////////////////////////////////////////
//  GRADLE CONFIGURATION
///////////////////////////////////////////////////////////////////////////////

plugins {
    id("com.diffplug.spotless") version "7.0.4"
}
buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.20")
        classpath("com.android.tools.build:gradle:7.4.2")
    }
}

tasks {
    spotless {
        kotlinGradle {
            target("**/*.kts")
            ktfmt()
        }
    }
}

