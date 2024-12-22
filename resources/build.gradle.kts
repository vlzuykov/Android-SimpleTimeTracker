import com.example.util.simpletimetracker.Base
import com.example.util.simpletimetracker.applyAndroidLibrary

plugins {
    id(com.example.util.simpletimetracker.BuildPlugins.gradleLibrary)
    id(com.example.util.simpletimetracker.BuildPlugins.kotlin)
}

applyAndroidLibrary()

android {
    namespace = "${Base.namespace}.resources"
}
