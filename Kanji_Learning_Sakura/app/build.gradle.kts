plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.kanji_learning_sakura"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.kanji_learning_sakura"
        minSdk = 26
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

    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // JDBC driver để đăng nhập trực tiếp MySQL trong ứng dụng
    testRuntimeOnly("mysql:mysql-connector-java:8.0.33")

    // Unit test (JDBC chỉ dùng ở test)
    testImplementation("junit:junit:4.13.2")

    // Android test
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2")
}
