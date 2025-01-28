import com.example.util.simpletimetracker.Base
import com.example.util.simpletimetracker.applyAndroidLibrary

plugins {
    alias(libs.plugins.gradleLibrary)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlinParcelize)
    alias(libs.plugins.ksp)
}

applyAndroidLibrary()

android {
    namespace = "${Base.namespace}.navigation"
}

dependencies {
    implementation(project(":domain"))

    implementation(libs.ktx.fragment)
    implementation(libs.ktx.navigationFragment)
    implementation(libs.ktx.navigationUi)
}
