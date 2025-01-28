import com.example.util.simpletimetracker.Base
import com.example.util.simpletimetracker.applyAndroidLibrary

plugins {
    alias(libs.plugins.gradleLibrary)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ksp)
}

applyAndroidLibrary()

android {
    namespace = "${Base.namespace}.feature_base_adapter"
}

dependencies {
    implementation(project(":feature_views"))
    implementation(project(":domain"))
    implementation(project(":resources"))

    implementation(libs.androidx.recyclerView)
    implementation(libs.androidx.constraintLayout)
    implementation(libs.androidx.cardView)
    implementation(libs.androidx.material)
    implementation(libs.google.flexBox)
    implementation(libs.ktx.core)
}
