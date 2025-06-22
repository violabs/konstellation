plugins {
    id("com.google.devtools.ksp")
}

repositories {
    maven {
        url = uri("https://reliquary.open.nyc3.cdn.digitaloceanspaces.com")
    }
}

dependencies {
//    ksp(project(":dsl"))
    ksp("io.violabs.konstellation:dsl:0.0.1")
    implementation(project(":meta-dsl"))
    implementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation(project(":core-test"))
}

kotlin {
    sourceSets {
        main {
            kotlin {
                // add KSP’s output dir for main
                srcDir("${layout.buildDirectory}/generated/ksp/main/kotlin")
            }
        }
    }
}


ksp {
    arg("projectRootClasspath", "io.violabs.konstellation.generateTest")
    arg("dslBuilderClasspath", "io.violabs.konstellation.generateTest")
    arg("dslMarkerClass", "io.violabs.konstellation.generateTest.TestDslMarker")
}