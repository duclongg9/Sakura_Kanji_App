plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.kanji_learning_sakura"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.kanji_learning_sakura"
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)

    // Google Sign-In (Credential Manager + Google ID)
    implementation("androidx.credentials:credentials:1.6.0-beta03")
    implementation("androidx.credentials:credentials-play-services-auth:1.6.0-beta03")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.0") // <-- sửa dòng này
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // HTTP client gọi backend
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Unit test (JDBC chỉ dùng ở test)
    testImplementation("junit:junit:4.13.2")
    testRuntimeOnly("mysql:mysql-connector-java:8.0.33")

    // Android test
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
