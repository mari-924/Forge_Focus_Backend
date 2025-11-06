plugins {
	id("org.springframework.boot") version "3.3.1"
	id("io.spring.dependency-management") version "1.1.5"
	java
}

group = "com.focusforge"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

dependencies {
	// --- Spring Boot Starters ---
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

	// --- Database & Migration ---
	implementation("org.flywaydb:flyway-core:9.22.3") // ✅ stable MySQL 8 compatible
	runtimeOnly("com.mysql:mysql-connector-j")

	// --- Google API Client ---
	implementation("com.google.api-client:google-api-client:2.2.0")
	implementation("com.google.http-client:google-http-client-gson:1.43.3")

	// --- JWT ---
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
	implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")

	// --- Lombok ---
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	// --- Testing ---
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.rest-assured:rest-assured:5.4.0")
	testImplementation("org.hamcrest:hamcrest:2.2")
}

tasks.test {
	useJUnitPlatform()
}
