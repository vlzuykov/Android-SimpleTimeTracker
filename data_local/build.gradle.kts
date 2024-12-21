import com.example.util.simpletimetracker.Base
import com.example.util.simpletimetracker.Deps
import com.example.util.simpletimetracker.applyAndroidLibrary

plugins {
    id("com.android.library")
    id("kotlin-android")
    id(com.example.util.simpletimetracker.BuildPlugins.ksp)
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

    implementation(Deps.Androidx.room)
    implementation(Deps.Ktx.room)

    ksp(Deps.Kapt.room)
    ksp(Deps.Kapt.dagger)
}
