import com.example.util.simpletimetracker.Base
import com.example.util.simpletimetracker.Deps
import com.example.util.simpletimetracker.applyAndroidLibrary

plugins {
    id(com.example.util.simpletimetracker.BuildPlugins.gradleLibrary)
    id(com.example.util.simpletimetracker.BuildPlugins.kotlin)
    id(com.example.util.simpletimetracker.BuildPlugins.ksp)
}

applyAndroidLibrary()

android {
    namespace = "${Base.namespace}.feature_base_adapter"
}

dependencies {
    implementation(project(":feature_views"))
    implementation(project(":domain"))
    implementation(project(":resources"))

    implementation(Deps.Androidx.recyclerView)
    implementation(Deps.Androidx.constraintLayout)
    implementation(Deps.Androidx.cardView)
    implementation(Deps.Androidx.material)
    implementation(Deps.Google.flexBox)
    implementation(Deps.Ktx.core)
}
