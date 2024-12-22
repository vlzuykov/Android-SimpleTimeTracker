plugins {
    id(com.example.util.simpletimetracker.BuildPlugins.ksp)
        .version(com.example.util.simpletimetracker.Versions.ksp)
        .apply(false)
    id(com.example.util.simpletimetracker.BuildPlugins.gradleApplication)
        .version(com.example.util.simpletimetracker.Versions.gradle)
        .apply(false)
    id(com.example.util.simpletimetracker.BuildPlugins.kotlin)
        .version(com.example.util.simpletimetracker.Versions.kotlin)
        .apply(false)
    id(com.example.util.simpletimetracker.BuildPlugins.ktlint)
        .version(com.example.util.simpletimetracker.Versions.ktlint)
        .apply(false)
    id(com.example.util.simpletimetracker.BuildPlugins.hilt)
        .version(com.example.util.simpletimetracker.Versions.dagger)
        .apply(false)
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }

    apply(plugin = com.example.util.simpletimetracker.BuildPlugins.ktlint)
}

tasks {
    val clean by registering(Delete::class) {
        delete(buildDir)
    }
}
