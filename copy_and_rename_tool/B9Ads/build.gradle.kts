/*
* USER: Cuong
* version: 2.1.9
* date: 20250123
* */

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    `maven-publish`
}

android {
    namespace = "com.b9ads"
    compileSdk = 36

    defaultConfig {
        minSdk = 29
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            consumerProguardFiles("consumer-rules.pro")
        }

        debug {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    ndkVersion = "28.2.13676358"
}

dependencies {
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("com.google.android.material:material:1.12.0")
    // Lib loading
    implementation("com.github.ybq:Android-SpinKit:1.4.0")

    // Lib google Ads
    implementation("com.google.android.gms:play-services-ads:24.7.0")

    // Lib facebook Ads
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    // Lib Lifecycle (vòng đời)
    implementation ("androidx.lifecycle:lifecycle-process:2.9.2")

    // UMP
    implementation("com.google.android.ump:user-messaging-platform:3.2.0")

//    //Adjust
//    implementation("com.adjust.sdk:adjust-android:5.1.0")
//    //Google Play Referrer API
//    implementation("com.android.installreferrer:installreferrer:2.2")
//    // Add the following if you are using the Adjust SDK inside web views on your app
//    implementation("com.adjust.sdk:adjust-android-webbridge:5.1.0")
//    //play-services-ads-identifier
//    implementation("com.google.android.gms:play-services-ads-identifier:18.2.0")

    //Adjust
    implementation("com.adjust.sdk:adjust-android:5.0.2")
    implementation("com.android.installreferrer:installreferrer:2.2")
    implementation("com.adjust.sdk:adjust-android-webbridge:5.0.2")
    implementation("com.google.android.gms:play-services-ads-identifier:18.2.0")


    //Fan
    implementation("com.facebook.android:audience-network-sdk:6.+")

    // Lib Module
    implementation(project(":B9Util"))
    implementation(project(":B9UI"))

}

configure<PublishingExtension> {
    publishing {
        publications {
            register<MavenPublication>("release") {
                groupId = "com.v1.inhouse"
                artifactId = "ad-lib"
                version = "1.0.1"

                afterEvaluate {
                    from(components["release"])
                }
            }
        }

        repositories {
            mavenLocal()
        }
    }
}
