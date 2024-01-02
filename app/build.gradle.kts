plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  kotlin("plugin.serialization") version "1.5.20"
}

android {
  namespace = "com.example.chatapp"
  compileSdk = 34

  packaging { resources { excludes.add("META-INF/DEPENDENCIES") } }

  defaultConfig {
    applicationId = "com.example.chatapp"
    minSdk = 26
    targetSdk = 33
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions { jvmTarget = "1.8" }
}

dependencies {
  implementation("androidx.core:core-ktx:1.9.0")
  implementation("androidx.appcompat:appcompat:1.6.1")
  implementation("com.google.android.material:material:1.10.0")
  implementation("androidx.constraintlayout:constraintlayout:2.1.4")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")

  implementation("com.google.code.gson:gson:2.8.9")

  implementation("org.apache.logging.log4j:log4j-api:2.14.1")
  implementation("org.apache.logging.log4j:log4j-core:2.14.1")

  implementation("androidx.compose.runtime:runtime:1.0.0")

  implementation("org.apache.logging.log4j:log4j-core:your_log4j_version")
  implementation("org.apache.logging.log4j:log4j-core:2.x.x")
  implementation("androidx.appcompat:appcompat:1.3.1")
  implementation("androidx.constraintlayout:constraintlayout:2.1.1")
  implementation("androidx.recyclerview:recyclerview:1.2.1")

  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
