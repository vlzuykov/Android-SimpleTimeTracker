import com.example.util.simpletimetracker.Base
import com.example.util.simpletimetracker.applyAndroidLibrary

plugins {
    alias(libs.plugins.gradleApplication)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

applyAndroidLibrary()

android {
    namespace = Base.namespace

    defaultConfig {
        applicationId = Base.applicationId
        versionCode = Base.versionCode
        versionName = Base.versionName

        testInstrumentationRunner = "com.example.util.simpletimetracker.utils.CustomTestRunner"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = true
            isCrunchPngs = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-debug-rules.pro",
            )
            testProguardFile("proguard-test-rules.pro")
        }
        release {
            isMinifyEnabled = true
            isCrunchPngs = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "VERSION_NAME", "\"${defaultConfig.versionName}\"")
        }
        release {
            buildConfigField("String", "VERSION_NAME", "\"${defaultConfig.versionName}\"")
        }
    }

    flavorDimensions += "version"
    val baseFlavor = "base"
    val playFlavor = "play"
    productFlavors {
        // F-Droid version, no google play services, no Wear OS support.
        create(baseFlavor) {
            dimension = "version"
        }
        // Google Play version, with google play services, Wear OS support.
        create(playFlavor) {
            dimension = "version"
            isDefault = true
        }
    }

    // Disables dependency metadata when building APKs.
    // If enabled, creates a file in app/build/outputs/sdk-dependencies/
    dependenciesInfo {
        val taskName = gradle.startParameter.taskRequests.toString().lowercase()
        val enabled = taskName.contains("assemble${playFlavor}release")
        includeInApk = enabled
        includeInBundle = enabled
    }

    buildFeatures {
        buildConfig = true
    }

    testOptions {
        animationsDisabled = true
    }

    sourceSets {
        // Adds exported schema location as test app assets.
        getByName("androidTest").assets.srcDir("$projectDir/../data_local/schemas")
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":data_local"))
    implementation(project(":resources"))
    implementation(project(":feature_main"))
    implementation(project(":feature_running_records"))
    implementation(project(":feature_change_record_type"))
    implementation(project(":feature_records"))
    implementation(project(":feature_records_all"))
    implementation(project(":feature_change_record"))
    implementation(project(":feature_change_running_record"))
    implementation(project(":feature_statistics"))
    implementation(project(":feature_statistics_detail"))
    implementation(project(":feature_settings"))
    implementation(project(":feature_settings:api"))
    implementation(project(":feature_settings:views"))
    implementation(project(":feature_dialogs"))
    implementation(project(":feature_widget"))
    implementation(project(":feature_notification"))
    implementation(project(":feature_categories"))
    implementation(project(":feature_change_category"))
    implementation(project(":feature_change_record_tag"))
    implementation(project(":feature_change_activity_filter"))
    implementation(project(":feature_archive"))
    implementation(project(":feature_tag_selection"))
    implementation(project(":feature_data_edit"))
    implementation(project(":feature_records_filter"))
    implementation(project(":feature_goals"))
    implementation(project(":feature_pomodoro"))
    implementation(project(":feature_complex_rules"))
    implementation(project(":feature_suggestions"))
    implementation(project(":feature_change_complex_rule"))
    implementation(project(":feature_change_goals"))
    implementation(project(":feature_change_goals:api"))
    implementation(project(":feature_change_goals:views"))
    "playImplementation"(project(":feature_wear"))

    implementation(libs.androidx.room)
    implementation(libs.ktx.navigationFragment)
    implementation(libs.ktx.navigationUi)
    implementation(libs.google.dagger)

    ksp(libs.kapt.dagger)
    kspAndroidTest(libs.kapt.dagger)

    androidTestImplementation(libs.uitest.junit)
    androidTestImplementation(libs.uitest.espresso)
    androidTestImplementation(libs.uitest.espressoContrib)
    androidTestImplementation(libs.uitest.dagger)
    androidTestImplementation(libs.uitest.room)
}
