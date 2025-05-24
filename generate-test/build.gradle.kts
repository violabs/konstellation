
plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    ksp(project(":dsl"))
    implementation(project(":meta-dsl"))
    implementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation(project(":common"))
    testImplementation(project(":core-test"))
//    ksp(project(":dsl-test"))
    kspTest(project(":dsl-test"))
}

kotlin {
    sourceSets {
        main {
            kotlin {
                // add KSP’s output dir for main
                srcDir("${layout.buildDirectory}/generated/ksp/main/kotlin")
            }
        }

        test {
            kotlin {
                // add KSP’s output dir for test
                srcDir("${layout.buildDirectory}/generated/ksp/test/kotlin")
            }
        }
    }
}


ksp {
    arg("projectRootClasspath", "io.violabs.picard.generateTest")
    arg("dslBuilderClasspath", "io.violabs.picard.generateTest")
    arg("dslMarkerClass", "io.violabs.picard.generateTest.TestDslMarker")
}