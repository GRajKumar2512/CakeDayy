plugins {
    alias(libs.plugins.cakeday.android.library)
    alias(libs.plugins.cakeday.android.compose)
    alias(libs.plugins.cakeday.android.hilt)
}

android {
    namespace = "com.pocketaps.cakeday.widget"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:designsystem"))
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
}