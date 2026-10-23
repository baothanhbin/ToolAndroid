/*
* USER: Cuong
* version: 2.2.9
* update: 20250123
* */

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
}

android {
    namespace = "com.b9default"
    compileSdk = 36

    defaultConfig {
        minSdk = 29

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

//    buildTypes {
//        release {
//            isMinifyEnabled = false
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
//        }
//    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            //isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            isMinifyEnabled = false
            //isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    //ADS
    implementation("com.google.android.gms:play-services-ads:24.7.0")

    //Dot circle intro
    implementation("com.tbuonomo:dotsindicator:5.1.0")

    //Lib google play
    implementation ("com.android.installreferrer:installreferrer:2.2")

    //Splash
    implementation ("androidx.core:core-splashscreen:1.0.1")

    //Multidex
    implementation ("androidx.multidex:multidex:2.0.1")

    //Fb Shimmer
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    // gson
    implementation("com.google.code.gson:gson:2.13.1")

    // Import Module Lib
    implementation(project(":B9Ads"))
    implementation(project(":B9Util"))
    implementation(project(":B9UI"))

    // lottie animation
    implementation("com.airbnb.android:lottie:6.6.7")
}
