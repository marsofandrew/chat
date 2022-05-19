plugins {
    java
    id("application")
}

group = "org.marsofandrew"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation("io.netty:netty-all:4.1.77.Final")
    implementation("log4j:log4j:1.2.17")
    implementation("org.slf4j:slf4j-api:1.7.36")

    testImplementation("org.slf4j:slf4j-api:1.7.36")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    val lombok = "org.projectlombok:lombok:1.18.22"
    compileOnly(lombok)
    annotationProcessor(lombok)
    testCompileOnly(lombok)
    testAnnotationProcessor(lombok)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}