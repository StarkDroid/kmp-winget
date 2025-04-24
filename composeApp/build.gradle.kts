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
            packageVersion = project.version.toString()
            description = "Compose Multiplatform based windows app for winget GUI package manager"
            copyright = "© 2024 Trishiraj. All rights reserved."
            windows {
                iconFile.set(project.file("kmp-winget.ico"))
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
