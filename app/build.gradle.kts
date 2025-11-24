plugins {
    id("application")
    id("checkstyle")
    id("com.github.ben-manes.versions") version "0.53.0"
    id("jacoco")
    id("org.sonarqube") version "7.0.1.6134"
    id("io.freefair.lombok") version "8.14"
}

group = "hexlet.code"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass.set("hexlet.code.App")
}

dependencies {
    implementation("io.javalin:javalin:6.7.0")
    implementation("org.slf4j:slf4j-simple:2.0.9")

    implementation("com.zaxxer:HikariCP:7.0.2")
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("com.h2database:h2:2.3.232")

    implementation("gg.jte:jte:3.2.1")  // runtime
    implementation("io.javalin:javalin-rendering:6.7.0")    // интеграция с Javalin

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    testImplementation("io.javalin:javalin-testtools:6.7.0")
    testImplementation("com.konghq:unirest-java:3.14.5")
    testImplementation("org.assertj:assertj-core:3.27.6")
}

sonar {
    properties {
        property("sonar.projectKey", "Jora777F_java-project-72")
        property("sonar.organization", "jora777f")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
    }
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}
