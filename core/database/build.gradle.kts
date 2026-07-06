plugins {
    alias(libs.plugins.cakeday.android.library)
    alias(libs.plugins.cakeday.android.hilt)
}

android {
    namespace = "com.pocketaps.cakeday.core.database"
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    sourceSets {
        getByName("test").assets.srcDir("$projectDir/schemas")
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core.ktx)
}