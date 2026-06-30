plugins {
    alias(libs.plugins.cakeday.kotlin.library)
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(libs.kotlinx.coroutines.core)
}