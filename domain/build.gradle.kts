import com.example.util.simpletimetracker.Deps

plugins {
    id(com.example.util.simpletimetracker.BuildPlugins.javaLibrary)
    id(com.example.util.simpletimetracker.BuildPlugins.kotlinLibrary)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    api(Deps.javax)
    api(Deps.coroutines)
    api(Deps.timber)
    api(Deps.kotlin)

    testImplementation(Deps.Test.junit)
    testImplementation(Deps.Test.mockitoKotlin)
}
