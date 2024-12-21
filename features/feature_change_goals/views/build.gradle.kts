import com.example.util.simpletimetracker.Base
import com.example.util.simpletimetracker.applyAndroidLibrary

plugins {
    id("com.android.library")
    id("kotlin-android")
    id(com.example.util.simpletimetracker.BuildPlugins.ksp)
}

applyAndroidLibrary()

android {
    namespace = "${Base.namespace}.feature_change_goals.views"
}

dependencies {
    implementation(project(":feature_change_goals:api"))
    implementation(project(":core"))
}
