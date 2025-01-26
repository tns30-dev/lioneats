plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.lioneats"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.lioneats"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.kotlin.stdlib)

    // Google Play Services
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // AndroidX Libraries
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.activity:activity:1.7.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Network Libraries
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // JSON Libraries
    implementation("com.google.code.gson:gson:2.10.1")

    // Image Loading
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")

    // Lombok
    implementation("org.projectlombok:lombok:1.18.28")
    annotationProcessor("org.projectlombok:lombok:1.18.28")

    // Testing Libraries
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.11.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test:runner:1.5.2")

    // Validation
    implementation("com.mobsandgeeks:android-saripaar:2.0.3")
}

// Enforce a consistent Kotlin version across all dependencies
configurations.all {
    resolutionStrategy {
        eachDependency {
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion(libs.versions.kotlin.get()) // Ensure this matches your version catalog
            }
        }
    }
}
