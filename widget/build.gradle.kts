plugins {
    alias(libs.plugins.cakeday.android.library)
    alias(libs.plugins.cakeday.android.compose)
}

android {
    namespace = "com.pocketaps.cakeday.widget"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:designsystem"))
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
}