plugins {
    alias(libs.plugins.cakeday.android.application)
    alias(libs.plugins.cakeday.android.compose)
    alias(libs.plugins.cakeday.android.hilt)
}

android {
    namespace = "com.pocketaps.cakeday"

    defaultConfig {
        applicationId = "com.pocketaps.cakeday"
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:notifications"))
    implementation(project(":core:data"))
    implementation(project(":feature:people"))
    implementation(project(":feature:editperson"))
    implementation(project(":feature:groups"))
    implementation(project(":feature:settings"))
    implementation(project(":widget"))
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    testImplementation(libs.junit)
    testImplementation(project(":core:testing"))
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core.ktx)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}