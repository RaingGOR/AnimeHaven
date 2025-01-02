plugins {
    id("java")
    id("org.springframework.boot") version "3.4.1" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

group = "Raingor.ru"
version = "1.0-SNAPSHOT"
allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")

    tasks.register("prepareKotlinBuildScriptModel"){}

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("FAILED", "SKIPPED", "PASSED")
        }
    }
}