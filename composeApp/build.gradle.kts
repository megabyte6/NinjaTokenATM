import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)

    alias(libs.plugins.jetbrainsCompose)

    kotlin("plugin.serialization") version "1.9.22"
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)

            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

            implementation("org.apache.poi:poi-ooxml:5.2.5")
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}


compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(
                TargetFormat.Msi,
                TargetFormat.Dmg,
                TargetFormat.Pkg,
                TargetFormat.Deb,
                TargetFormat.Rpm,
                TargetFormat.AppImage
            )
            packageName = "Ninja Token ATM"
            packageVersion = "1.0.0"
            description = "Displays the number of tokens in a Ninja's bank account."
            copyright = "© 2024 megabyte6. All rights reserved."
            licenseFile = project.file("../LICENSE")

            windows {
                iconFile = project.file("src/desktopMain/resources/icon.ico")
                dirChooser = true
            }

            macOS {
                iconFile = project.file("src/desktopMain/resources/icon.icns")
            }

            linux {
                iconFile = project.file("src/desktopMain/resources/icon.png")
                packageName = "ninja-token-atm"
            }
        }
    }
}
