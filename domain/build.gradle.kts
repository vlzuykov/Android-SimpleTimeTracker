import com.example.util.simpletimetracker.Deps

plugins {
    id("java-library")
    id("kotlin")
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
