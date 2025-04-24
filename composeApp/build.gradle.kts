import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

version = "2.1.0"

kotlin {
    jvm("desktop")
    
    sourceSets {
        val desktopMain by getting
        
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.viewmodel)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}


compose.desktop {
    application {
        mainClass = "com.velocity.kmpwinget.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Exe)
            packageName = "kmpwinget"
            vendor = "Trishiraj"
            packageVersion = project.version.toString()
            description = "A Package manager app made with kotlin multiplatform for windows that leverages Winget"
            copyright = "© 2024 - 25 Trishiraj. All rights reserved."
            windows {
                iconFile.set(project.file("kmp-winget.ico"))
                upgradeUuid = "996e0739-7f6b-4022-b35a-8aef171389a0"
                dirChooser = true
                menuGroup = "KMP Winget"
                shortcut = true
                menu = true
            }

            buildTypes.release.proguard {
                isEnabled = true
                optimize = true
                obfuscate = true
                configurationFiles.from("src/desktopMain/proguard-rules.pro")
            }
        }
    }
}
