
subprojects {
    buildscript {
        repositories {
            mavenLocal()
            jcenter()
            mavenCentral()
        }

        dependencies {
            classpath( "com.martyneju:ceylon-plugin:0.0.1")
//            classpath("org.ceylon-lang:com.redhat.ceylon.common:1.3.3")
        }
    }
}

