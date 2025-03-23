plugins {
    id("com.android.application")
    id("com.google.gms.google-services") // Google services plugin
}

android {
    namespace = "com.example.fresh_picks"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.fresh_picks"
        minSdk = 24
        targetSdk = 34
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // 🔹 Core AndroidX libraries
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // 🔹 Firebase BOM (Bill of Materials) - Manages versions automatically
    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")

    // 🔹 Glide for Image Loading
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation("androidx.activity:activity:1.8.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

    // 🔹 Lottie for Animations
    implementation("com.airbnb.android:lottie:5.0.3")

    // 🔹 Room Database
    implementation("androidx.room:room-runtime:2.5.2")
    annotationProcessor("androidx.room:room-compiler:2.5.2")

    // 🔹 Retrofit for API Calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // 🔹 Google Play Services (Auth)
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // 🔹 Testing Libraries
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

}
