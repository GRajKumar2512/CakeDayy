plugins {
    alias(libs.plugins.cakeday.android.library)
    alias(libs.plugins.cakeday.android.hilt)
}

android {
    namespace = "com.pocketaps.cakeday.core.datastore"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(libs.androidx.datastore.preferences)
}