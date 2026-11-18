plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") version "4.6.0" apply false
}

android {
    namespace = "com.example.asm_se08201_7436_nguyen_duy_liem_bh02258"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.asm_se08201_7436_nguyen_duy_liem_bh02258"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:35.2.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-functions")
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
if (file("google-services.json").exists()) apply(plugin = "com.google.gms.google-services")
