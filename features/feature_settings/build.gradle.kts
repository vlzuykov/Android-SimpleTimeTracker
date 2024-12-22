import com.example.util.simpletimetracker.Base
import com.example.util.simpletimetracker.Deps
import com.example.util.simpletimetracker.applyAndroidLibrary

plugins {
    id(com.example.util.simpletimetracker.BuildPlugins.gradleLibrary)
    id(com.example.util.simpletimetracker.BuildPlugins.kotlin)
    id(com.example.util.simpletimetracker.BuildPlugins.kotlinParcelize)
    id(com.example.util.simpletimetracker.BuildPlugins.ksp)
    id(com.example.util.simpletimetracker.BuildPlugins.hiltPlugin)
}

applyAndroidLibrary()

android {
    namespace = "${Base.namespace}.feature_settings"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":feature_settings:api"))
    implementation(project(":feature_settings:views"))
    implementation(Deps.Google.dagger)
    ksp(Deps.Kapt.dagger)
}
