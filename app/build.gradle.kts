import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompileTool

plugins {
    id("kotlin-android")
    alias(libs.plugins.gms.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.nav.safe.args)
    alias(libs.plugins.dagger.hilt)
    id("kotlin-parcelize")
}

android {
    namespace = "io.vonley.mi"
    compileSdk = 34
    ndkVersion = "25.1.8937393"
    buildToolsVersion = "34.0.0"
    defaultConfig {
        applicationId = "io.vonley.mi"
        minSdk = 21
        targetSdk = 34
        versionCode = 7
        versionName = "1.1.1-alpha"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += arrayOf(
                    "room.schemaLocation" to "${projectDir}/schemas",
                    "room.incremental" to "true",
                    "room.expandProjection" to "true"
                )
            }
        }

    }
    externalNativeBuild {
        ndkBuild {
            path = File("src/main/jni/Android.mk")
        }
    }
    flavorDimensions.add("playstation")
    productFlavors {
        create("playstation") {
            dimension = "playstation"
            applicationId = "io.vonley.mi"
            externalNativeBuild.cmake {
                cppFlags += "-DPROD"
            }
        }
        create("playstationTest") {
            dimension = "playstation"
            applicationId = "io.vonley.mi"
            externalNativeBuild.cmake {
                cppFlags += "-DDEBUG"
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjvm-default=all-compatibility")
    }
    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }
    lint {
        abortOnError = false
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }

    /*
    androidComponents {
        onVariants(selector().all()) { variant ->
            afterEvaluate {
                val capName = variant.name.capitalize()
                tasks.getByName("ksp${capName}Kotlin") {
                    (this as AbstractKotlinCompileTool<*>).setSource(tasks.getByName("compile${capName}Aidl").outputs)
                }
            }
        }
    }*/
}

dependencies {

    // Accompanist

    implementation(libs.nanohttpd)
    implementation(libs.nanohttpd.webserver)
    implementation(libs.commons.net)
    implementation(libs.circleimageview)
    implementation(libs.picasso)
    implementation(libs.coil.compose)
    implementation(libs.markwon.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.javax.inject)
    implementation(libs.android.joda)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.firebase.analytics.ktx)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraint)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.swiperefreshlayout)

    implementation(libs.gson)
    implementation(libs.jsoup)
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.pager.indicators)
    implementation(libs.accompanist.themeadapter)
    implementation(libs.androidx.animation.graphics.android)

    ksp(libs.glide.ksp)
    implementation(libs.glide)
    implementation(libs.glide.okhttp3)

    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.window.size.cls)
    implementation(libs.androidx.compose.material.icon.core)
    implementation(libs.androidx.compose.material.icon.extended)

    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.viewbinding)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    //TODO: Fix - androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    implementation(libs.androidx.lifecycle.extensions)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Optional - Integration with activities


    implementation(libs.hilt.android)
    testImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.hilt.android.testing)
    implementation(libs.hilt.navigation.compose)

    ksp(libs.hilt.android.compiler)
    kspTest(libs.hilt.android.compiler)
    kspAndroidTest(libs.hilt.android.compiler)


    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.testing)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.fragment.ktx)
    androidTestImplementation(libs.androidx.navigation.testing)

    ksp(libs.androidx.room)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)


    implementation(libs.retrofit)
    implementation(libs.retrofit.gson.converter)
    implementation(libs.retrofit.converters)

    implementation(libs.okhttp3)
    implementation(libs.okhttp3.intecepter)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}