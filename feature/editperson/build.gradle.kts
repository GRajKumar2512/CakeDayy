plugins {
    alias(libs.plugins.cakeday.android.library)
    alias(libs.plugins.cakeday.android.compose)
    alias(libs.plugins.cakeday.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.pocketaps.cakeday.feature.editperson"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:ui"))
    implementation(project(":core:common"))
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.compose.material.icons.core)

    testImplementation(project(":core:testing"))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}