import com.example.util.simpletimetracker.Base
import com.example.util.simpletimetracker.Deps
import com.example.util.simpletimetracker.applyAndroidLibrary

plugins {
    id("com.android.library")
    id("kotlin-android")
    id(com.example.util.simpletimetracker.BuildPlugins.ksp)
    id("dagger.hilt.android.plugin")
}

applyAndroidLibrary()

android {
    namespace = "${Base.namespace}.feature_pomodoro"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":feature_settings:api"))
    implementation(project(":feature_settings:views"))
    implementation(Deps.Google.dagger)
    ksp(Deps.Kapt.dagger)
}
