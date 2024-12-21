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
    namespace = "${Base.namespace}.feature_goals"
}

dependencies {
    implementation(project(":core"))
    implementation(Deps.Google.dagger)
    ksp(Deps.Kapt.dagger)
}
