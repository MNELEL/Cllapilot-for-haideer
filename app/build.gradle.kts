plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.classpro.wkhpyq"
    minSdk = 24
    targetSdk = 36
    versionCode = 4
    versionName = "4.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

tasks.register("replaceColors") {
  doLast {
    val dir = file("src/main/java/com/example/ui/screen")
    val replacements = mapOf(
      "Color(0xFF1E293B)" to "com.example.ui.theme.ChocolateBrown",
      "Color(0xFF64748B)" to "com.example.ui.theme.MochaTaupe",
      "Color(0xFF6366F1)" to "com.example.ui.theme.GoldGingerEnd",
      "Color(0xFF10B981)" to "com.example.ui.theme.PositiveGreen",
      "Color(0xFFF59E0B)" to "com.example.ui.theme.GoldGingerStart",
      "Color(0xFFEF4444)" to "Color(0xFFC0392B)",
      "Color(0xFFEEF2FF)" to "com.example.ui.theme.CreamBeige",
      "Color(0xFFC7D2FE)" to "com.example.ui.theme.MochaTaupe",
      "Color(0xFF2D2319)" to "com.example.ui.theme.ChocolateBrown",
      "Color(0xFF1E1B4B)" to "com.example.ui.theme.ChocolateBrown.copy(alpha=0.9f)",
      "Color(0xFFA5B4FC)" to "com.example.ui.theme.GoldGingerStart",
      "Color(0xFFF8FAFC)" to "com.example.ui.theme.CreamBeige",
      "Color(0xFFFCD34D)" to "com.example.ui.theme.GoldGingerStart"
    )
    dir.walk().forEach { f ->
      if (f.extension == "kt") {
        var content = f.readText()
        for ((k, v) in replacements) {
          content = content.replace(k, v)
        }
        content = content.replace(
           "Brush.verticalGradient(\n                    listOf(darkBg, darkBg.copy(alpha = 0.9f))\n                )",
           "Brush.verticalGradient(listOf(com.example.ui.theme.CreamBeige, com.example.ui.theme.WhiteWarm))"
        )
        content = content.replace(
           "Brush.verticalGradient(\n                                    listOf(darkBg, darkBg.copy(alpha = 0.9f))\n                                )",
           "Brush.verticalGradient(listOf(com.example.ui.theme.CreamBeige, com.example.ui.theme.WhiteWarm))"
        )
        f.writeText(content)
      }
    }
    
    val mainAct = file("src/main/java/com/example/MainActivity.kt")
    var mainStr = mainAct.readText()
    for ((k, v) in replacements) {
      mainStr = mainStr.replace(k, v)
    }
    mainStr = mainStr.replace("{ currentDestination = nav.route }", "{ com.example.ui.SoundManager.playClick(); currentDestination = nav.route }")
    mainAct.writeText(mainStr)
  }
}

tasks.register("addHaptics") {
  doLast {
    val dir = file("src/main/java/com/example")
    dir.walk().forEach { f ->
      if (f.extension == "kt" && f.name != "SoundManager.kt" && f.name != "UIComponents.kt") {
        var content = f.readText()
        content = content.replace("com.example.ui.SoundManager.playClick(); ", "")
        content = content.replace("onClick = {", "onClick = { com.example.ui.SoundManager.playClick(); ")
        content = content.replace(".clickable {", ".clickable { com.example.ui.SoundManager.playClick(); ")
        f.writeText(content)
      }
    }
  }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  // implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.work.runtime.ktx)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  // implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}
