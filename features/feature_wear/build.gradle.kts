/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
import com.example.util.simpletimetracker.Base
import com.example.util.simpletimetracker.Deps
import com.example.util.simpletimetracker.applyAndroidLibrary

plugins {
    id(com.example.util.simpletimetracker.BuildPlugins.gradleLibrary)
    id(com.example.util.simpletimetracker.BuildPlugins.kotlin)
    id(com.example.util.simpletimetracker.BuildPlugins.ksp)
    id(com.example.util.simpletimetracker.BuildPlugins.hiltPlugin)
}

applyAndroidLibrary()

android {
    namespace = "${Base.namespace}.feature_wear"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":wear_api"))
    implementation(Deps.Google.services)
    implementation(Deps.Google.gson)
    implementation(Deps.Google.dagger)
    ksp(Deps.Kapt.dagger)

    testImplementation(Deps.Test.junit)
    testImplementation(Deps.Test.coroutines)
}
