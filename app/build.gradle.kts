import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

val hasReleaseSigningConfig =
    localProperties.getProperty("CONTAS_EM_DIA_RELEASE_STORE_FILE") != null &&
        localProperties.getProperty("CONTAS_EM_DIA_RELEASE_STORE_PASSWORD") != null &&
        localProperties.getProperty("CONTAS_EM_DIA_RELEASE_KEY_ALIAS") != null &&
        localProperties.getProperty("CONTAS_EM_DIA_RELEASE_KEY_PASSWORD") != null

android {
    namespace = "br.com.contaemdia"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "br.com.contaemdia"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["admobApplicationId"] =
            providers.gradleProperty("ADMOB_APP_ID").get()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    signingConfigs {
        create("release") {
            val storeFilePath = localProperties.getProperty("CONTAS_EM_DIA_RELEASE_STORE_FILE")
            val storePasswordValue = localProperties.getProperty("CONTAS_EM_DIA_RELEASE_STORE_PASSWORD")
            val keyAliasValue = localProperties.getProperty("CONTAS_EM_DIA_RELEASE_KEY_ALIAS")
            val keyPasswordValue = localProperties.getProperty("CONTAS_EM_DIA_RELEASE_KEY_PASSWORD")

            if (
                storeFilePath != null &&
                storePasswordValue != null &&
                keyAliasValue != null &&
                keyPasswordValue != null
            ) {
                storeFile = rootProject.file(storeFilePath)
                storePassword = storePasswordValue
                keyAlias = keyAliasValue
                keyPassword = keyPasswordValue
            }
        }
    }
    buildTypes {
        debug {
            buildConfigField("String", "ADMOB_DASHBOARD_INLINE_BANNER_ID", "\"ca-app-pub-3940256099942544/9214589741\"")
            buildConfigField("String", "ADMOB_DASHBOARD_BOTTOM_BANNER_ID", "\"ca-app-pub-3940256099942544/9214589741\"")
            buildConfigField("String", "ADMOB_SUMMARY_INLINE_BANNER_ID", "\"ca-app-pub-3940256099942544/9214589741\"")
            buildConfigField("String", "ADMOB_SUMMARY_BOTTOM_BANNER_ID", "\"ca-app-pub-3940256099942544/9214589741\"")
            buildConfigField("String", "ADMOB_DETAIL_BOTTOM_BANNER_ID", "\"ca-app-pub-3940256099942544/9214589741\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (hasReleaseSigningConfig) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "ADMOB_DASHBOARD_INLINE_BANNER_ID", "\"ca-app-pub-5315870199108015/9681070580\"")
            buildConfigField("String", "ADMOB_DASHBOARD_BOTTOM_BANNER_ID", "\"ca-app-pub-5315870199108015/5533942013\"")
            buildConfigField("String", "ADMOB_SUMMARY_INLINE_BANNER_ID", "\"ca-app-pub-5315870199108015/2279120498\"")
            buildConfigField("String", "ADMOB_SUMMARY_BOTTOM_BANNER_ID", "\"ca-app-pub-5315870199108015/7054907247\"")
            buildConfigField("String", "ADMOB_DETAIL_BOTTOM_BANNER_ID", "\"ca-app-pub-5315870199108015/4428743902\"")
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.google.play.services.ads)
    implementation(libs.google.user.messaging.platform)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    ksp(libs.androidx.room.compiler)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
