plugins {
  id("com.android.application")
  kotlin("android")
  kotlin("android.extensions")
  id("com.mapbox.maps.token")
}

android {
  compileSdkVersion(AndroidVersions.compileSdkVersion)
  defaultConfig {
    applicationId = "com.mapbox.maps.testapp.auto"
    minSdkVersion(AndroidVersions.minAndroidAutoSdkVersion)
    targetSdkVersion(AndroidVersions.targetSdkVersion)
    versionCode = 1
    versionName = "0.1.0"
    multiDexEnabled = true
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    testInstrumentationRunnerArguments["clearPackageData"] = "true"
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  testOptions {
    if (!project.hasProperty("android.injected.invoked.from.ide")) {
      execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }
  }
}

androidExtensions {
  isExperimental = true
}

dependencies {
  implementation(project(":extension-androidauto"))
  implementation(project(":sdk"))
  implementation(Dependencies.googleCarAppLibrary)
  implementation(Dependencies.kotlin)
  implementation(Dependencies.androidxAppCompat)

  // By default, the Maps SDK uses the Android Location Provider to obtain raw location updates.
  // And with Android 11, the raw location updates might suffer from precision issue.

  // The Maps SDK also comes pre-compiled with support for the [Google's Fused Location Provider](https://developers.google.com/location-context/fused-location-provider)
  // if that dependency is available. This means, that if your target devices support Google Play
  // Services, [we recommend adding the Google Play Location Services dependency to your project](https://developers.google.com/android/guides/setup).
  implementation(Dependencies.googlePlayServicesLocation)

  androidTestUtil(Dependencies.androidxOrchestrator)
  androidTestImplementation(Dependencies.androidxTestRunner)
  androidTestImplementation(Dependencies.androidxJUnitTestRules)
  androidTestImplementation(Dependencies.androidxRules)
  androidTestImplementation(Dependencies.androidxTestJUnit)
  androidTestImplementation(Dependencies.androidxEspresso)
  androidTestImplementation(Dependencies.androidxUiAutomator)
  testImplementation(Dependencies.junit)
}

project.apply {
  from("$rootDir/gradle/ktlint.gradle")
  from("$rootDir/gradle/lint.gradle")
}

the<com.mapbox.AccessTokenExtension>().file =
  "${project.rootDir}/android-auto-app/src/main/res/values/developer-config.xml"