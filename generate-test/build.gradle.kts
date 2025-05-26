plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    ksp(project(":dsl"))
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
    arg("projectRootClasspath", "io.violabs.picard.generateTest")
    arg("dslBuilderClasspath", "io.violabs.picard.generateTest")
    arg("dslMarkerClass", "io.violabs.picard.generateTest.TestDslMarker")
}