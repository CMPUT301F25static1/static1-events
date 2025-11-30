import java.util.Properties

val secretsPropertiesFile = rootProject.file("secrets.properties")
val secrets = Properties().apply {
    if (secretsPropertiesFile.exists()) {
        secretsPropertiesFile.inputStream().use { load(it) }
    }
}

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.static1.fishylottery"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.static1.fishylottery"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["MAPS_API_KEY"] = secrets.getProperty("MAPS_API_KEY") ?: ""
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // General app dependencies
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    // Images
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    // Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-storage")
    // QR code generation
    implementation("com.google.zxing:core:3.5.2")
    // CameraX core
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")
    implementation("com.google.guava:guava:32.1.3-android")
    // ML Kit Barcode Scanning
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    implementation(libs.coordinatorlayout)
    // Maps and Location
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")


    //Onboarding screens
    implementation("com.tbuonomo:dotsindicator:4.3")
    implementation("androidx.viewpager2:viewpager2:1.1.0")


    // Testing dependencies


    // --------------------------------------------------------------------
    // ✅ Unit tests (src/test)
    // --------------------------------------------------------------------
    testImplementation(libs.junit)
    testImplementation("org.mockito:mockito-core:5.4.0")
    // ✅ KEEP INLINE MOCKITO HERE ONLY IN src/test — SAFE
    testImplementation("org.mockito:mockito-inline:5.2.0")

    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.robolectric:robolectric:4.12.2")


    // --------------------------------------------------------------------
    // ✅ Instrumented tests (src/androidTest)
    // --------------------------------------------------------------------
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")

    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.navigation:navigation-testing:2.7.3")
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")

    androidTestImplementation("org.mockito:mockito-android:5.4.0")
    debugImplementation("androidx.fragment:fragment-testing:1.6.2")
}