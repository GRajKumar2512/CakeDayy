plugins {
    alias(libs.plugins.cakeday.kotlin.library)
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(libs.kotlinx.coroutines.test)
    implementation(libs.turbine)
    implementation(libs.junit)
}