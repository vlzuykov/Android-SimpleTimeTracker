import com.example.util.simpletimetracker.Base
import com.example.util.simpletimetracker.applyAndroidLibrary

plugins {
    alias(libs.plugins.gradleLibrary)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ksp)
}

applyAndroidLibrary()

android {
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }
    }

    namespace = "${Base.namespace}.data_local"
}

dependencies {
    implementation(project(":core"))

    implementation(libs.androidx.room)
    implementation(libs.ktx.room)

    ksp(libs.kapt.room)
    ksp(libs.kapt.dagger)
}
