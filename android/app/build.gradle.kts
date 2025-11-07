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
    // ------------- App -------------
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-storage")


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

    // ✅ CRITICAL FIX: use mockito-android NOT mockito-inline
    androidTestImplementation("org.mockito:mockito-android:5.4.0")

    // ✅ FragmentScenario must be debugImplementation
    debugImplementation("androidx.fragment:fragment-testing:1.6.2")
}
