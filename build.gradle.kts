/*
 * Copyright (c) 2017. Sunghyouk Bae <sunghyouk.bae@gmail.com>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright (c) 2016. Sunghyouk Bae <sunghyouk.bae@gmail.com>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import org.gradle.jvm.tasks.Jar
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.junit.platform.gradle.plugin.JUnitPlatformExtension

buildscript {
  extra["kotlinVersion"] = "1.1.51"
  val kotlinVersion = extra["kotlinVersion"] as String

  repositories {
    jcenter()
    maven { setUrl("https://plugins.gradle.org/m2/") }
    maven { setUrl("https://dl.bintray.com/kotlin/dokka/") }
  }

  dependencies {
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    classpath("com.netflix.nebula:nebula-kotlin-plugin:$kotlinVersion")
    classpath("org.jetbrains.dokka:dokka-gradle-plugin:0.9.15")
    classpath("org.junit.platform:junit-platform-gradle-plugin:1.0.1")
  }
}

plugins {
  // Gradle Home page에서 제공하는 build 정보
  // kotlin-math build 정보는 다음 링크에서 참조
  // https://scans.gradle.com/s/nw26kfa32rnx4
  `build-scan`
  `maven-publish`
  kotlin("jvm", "1.1.51")
}

apply {
  plugin("maven")
  plugin("nebula.kotlin")
  plugin("org.jetbrains.dokka")
  plugin("org.junit.platform.gradle.plugin")
}

group = "com.github.debop"

val kotlinVersion = extra["kotlinVersion"] as String

repositories {
  jcenter()
  maven { setUrl("http://dl.bintray.com/kyonifer/maven") }
}

dependencies {
  implementation(kotlin("stdlib", kotlinVersion))
  implementation(kotlin("reflect", kotlinVersion))

  testImplementation(kotlin("test", kotlinVersion))


  implementation("org.slf4j:slf4j-api:1.7.25")
  implementation("io.github.microutils:kotlin-logging:1.4.6")

  testImplementation("org.junit.jupiter:junit-jupiter-engine:5.0.1")
  testRuntime("org.junit.platform:junit-platform-launcher:1.0.1")

  // To avoid compiler warnings about @API annotations in JUnit code
  testCompileOnly("org.apiguardian:apiguardian-api:1.0.0")
}

buildScan {
  setLicenseAgreementUrl("https://gradle.com/terms-of-service")
  setLicenseAgree("yes")

  publishAlways()
}

kotlin {
  experimental.coroutines = Coroutines.ENABLE
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    jvmTarget = "1.6"
  }
}

val sourcesJar = task<Jar>("sourcesJar") {
  classifier = "sources"
  from(the<JavaPluginConvention>().sourceSets.getByName("main").allSource)
}

val dokka by tasks.getting(DokkaTask::class) {
  outputFormat = "html"
  outputDirectory = "$buildDir/javadoc"
}

val dokkaJar by tasks.creating(Jar::class) {
  group = JavaBasePlugin.DOCUMENTATION_GROUP
  description = "Assembles Kotlin docs with Dokka"
  classifier = "javadoc"
  from(dokka)
}

publishing {
  publications {
    create("default", MavenPublication::class.java) {
      from(components["java"])
      artifact(dokkaJar)
      artifact(sourcesJar)
    }
  }

  repositories {
    maven {
      url = uri("$buildDir/repository")
    }
  }
}
