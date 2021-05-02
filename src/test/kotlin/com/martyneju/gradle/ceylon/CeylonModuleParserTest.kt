package com.martyneju.gradle.ceylon

import com.martyneju.gradle.ceylon.utils.CeylonModuleParser
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

class CeylonModuleParserTest {
    val parser = CeylonModuleParser("example.ceylon")

    @Test
    fun `can parse module with-multi line comment`() {

       val result = parser.parse("""
       /* a comment
           spanning several lines
           Some [[Example]] markup.
           And even some code:
           <code>shared function() {
               print("Hello world!");
           }
           </code>
        */
        module my.test.module "3.3.0" {
           // this is an import
           import other.module "3.1.0";
        }
       """)

        assertEquals(result.moduleName, "my.test.module")
        assertEquals(result.version, "3.3.0")
        assertEquals(result.imports.toString(),"[{name=other.module, version=3.1.0}]")
    }

    @Test
    fun `Can parse module with multi-line doc comment with block comment inside it`( ) {
        val result = parser.parse("""
         "A comment
         spanning several lines
         Some [[Example]] markup.
         And even some code:
             shared function() {
                 print(\"Hello world!\");
             }

          /* block comments
             are acceptable inside doc comments.
           */
         "
        module my.test.module "0" {
           "This import is required.
            The \"1.0\" is the version!
            "
           import other.module "1.0";
        }
        """)

        assertEquals(result.moduleName,"my.test.module")
        assertEquals(result.version, "0")
        assertEquals(result.imports.toString(), "[{name=other.module, version=1.0}]")
    }

    @Test
    fun `Can parse module with literal String comment with block comment inside it`() {
        val result = parser.parse("""
         ${"\"\"\""}A comment
         spanning several lines
         Some [[Example]] markup.
         And even some code:
             shared function() {
                 print("Hello world!");
             }
         
         /* block comments
             are acceptable inside doc comments.
           */
         ${"\"\"\""}
         module my.test.module "0" {
           ${"\"\"\""}This import is required.
            The "1.0" is the version!
           ${"\"\"\""}
           import other.module "1.0";
         } 
        """)

        assertEquals(result.moduleName, "my.test.module")
        assertEquals(result.version, "0")
        assertEquals(result.imports.toString(), "[{name=other.module, version=1.0}]")
    }

    @Test
    fun `Can parse a simple module file`() {
        val result = parser.parse("""
        // some module
        module com.hello.world "1.0.0" {
            import java.lang.base  "7";
        } 
        """)

        assertEquals(result.moduleName, "com.hello.world")
        assertEquals(result.version, "1.0.0")
        assertEquals(result.imports.toString(), "[{name=java.lang.base, version=7}]")
    }

    @Test
    fun `Can parse a simple module file without any imports`() {
        val result = parser.parse("module com.hello.world \"1.0\" {}")

        assertEquals(result.moduleName, "com.hello.world")
        assertEquals(result.version, "1.0")
        assertEquals(result.imports.toString(),"[]")
    }

    @Test
    fun `Can parse a module file with many imports`() {
        val result = parser.parse("""
         // some module
         module com.hello.world "5.3" {
            import java.lang.base  "7"; // another comment
            import ceylon.collection "1.3.3";
            // comment           
            import one.more.ceylon.module "4.3";
            /* that's it!
            */
         } 
        """)

        assertEquals(result.moduleName,"com.hello.world")
        assertEquals(result.version, "5.3")
        assertEquals(result.imports.toString(), "[{name=java.lang.base, version=7}, {name=ceylon.collection, version=1.3.3}, {name=one.more.ceylon.module, version=4.3}]")
    }

    @Test
    fun `Can parse a module file with doc Strings`() {
        val result = parser.parse("""
         "This module is very \"cool\".
          It lets you do lots of things with [[MyType]].

          Example:

            value something = MyType(\"Hello there!\");

          Enjoy!!!"
         module com.hello.world "1.0.0" {
            import java.lang.base  "7";
         }
        """)

        println(result.moduleName)
        println(result.version)
        println(result.imports.toString())
    }

    @Test
    fun `Can parse a module file with Maven imports`() {
        val result = parser.parse("""
        module com.hello.world "1.3.3" {
            shared import maven:org.eclipse.paho:"org.eclipse.paho.client.mqttv3" "1.3.3";
            import maven:"my-group:other-module" "1.3.3";
            import ceylon.collection "1.3.3";
            import "org.junit:junit"  "4.12";
            shared import my_repo:org.liquibase.core "3.4.2";
        } 
        """)

        assertEquals(result.moduleName, "com.hello.world")
        assertEquals(result.version, "1.3.3")
        assertEquals(result.imports.toString(), "[{name=org.eclipse.paho:org.eclipse.paho.client.mqttv3, namespace=maven, version=1.3.3}, {name=my-group:other-module, namespace=maven, version=1.3.3}, {name=ceylon.collection, version=1.3.3}, {name=org.junit:junit, namespace=maven, version=4.12}, {name=org.liquibase.core, namespace=my_repo, version=3.4.2}]")
    }

    @Test
    fun `Can parse a module file with annotated imports and module`() {
        val result = parser.parse("""
         "An example module
          with \"lots\" of annotations"
          shared("jvm")
          module com.hello.world "1.0.0" {
                shared import java.lang.base  "7";
                shared("js") import org.jquery "2.5";
                // normal import
                import ceylon.promise "1.2";
          }    
        """)

        assertEquals(result.moduleName, "com.hello.world")
        assertEquals(result.version, "1.0.0")
        assertEquals(result.imports.toString(),"[{name=java.lang.base, version=7}, {name=org.jquery, version=2.5}, {name=ceylon.promise, version=1.2}]")
    }

    @Test
    fun `Can parse module file used in Ceylon Specification`() {
        val result = parser.parse("""
         "The best-ever ORM solution!"
          license ("http://www.gnu.org/licenses/lgpl.html")
          module org.hibernate "3.0.0.beta" {
            shared import ceylon.language "1.0.1";
            import javax.sql "4.0";
          } 
        """)

        assertEquals(result.moduleName, "org.hibernate")
        assertEquals(result.version, "3.0.0.beta")
        assertEquals(result.imports.toString(), "[{name=ceylon.language, version=1.0.1}, {name=javax.sql, version=4.0}]")
    }

    @Test
    fun `Can parse a module file with comments everywhere`() {
        val result = parser.parse("""
         // some module
         module // comment
         com.hello.world /* not code */ "1.0.0"
         {// comment
            import /*
            crazy but possible
            */
            java.lang.base  "7" /* ?? */;
         } // done

         /*
           comment
         */
         //END 
        """)

        assertEquals(result.moduleName, "com.hello.world")
        assertEquals(result.version,"1.0.0")
        assertEquals(result.imports.toString(),"[{name=java.lang.base, version=7}]")
    }

    @Test
    fun `Can parse a realistic module file with tabs indentation`() {
        val result = parser.parse("""
         native("jvm")
         module ru.bia.maven "1.0.0" {
         ${"\t\t"}import java.base "8";
         ${"\t\t"}//import "org.apache.logging.log4j:log4j-api" "2.4.1";
         ${"\t\t"}shared import "org.apache.logging.log4j:log4j-core" "2.4.1";
         ${"\t\t"}import "org.spockframework:spock-core" "1.0-groovy-2.4";
         }
        """)

        assertEquals(result.moduleName,"ru.bia.maven")
        assertEquals(result.version, "1.0.0")
        assertEquals(result.imports.toString(),"[{name=java.base, version=8}, {name=org.apache.logging.log4j:log4j-core, namespace=maven, version=2.4.1}, {name=org.spockframework:spock-core, namespace=maven, version=1.0-groovy-2.4}]")
    }

    @Test
    fun `Can parse name-spaced module imports`() {
        val result = parser.parse("""
        native("jvm")
        module flight "1.0.0" {
           import ceylon.interop.java "1.2.3";
        
           import "org.springframework.boot:spring-boot-starter-web" "1.3.3.RELEASE";
           import "org.springframework.boot:spring-boot-starter-undertow" "1.3.3.RELEASE";
           import "org.springframework.cloud:spring-cloud-starter-eureka" "1.1.0.RC1";
        
           shared import "org.springframework.boot:spring-boot-starter-data-jpa" "1.3.3.RELEASE";
        
           import maven:"postgresql:postgresql" "9.1-901-1.jdbc4"; //Using new prefix here
           shared import my_repo:org.liquibase.core "3.4.2";
           import maven:"junit:junit" "4.12";
        } 
        """)

        assertEquals(result.moduleName, "flight")
        assertEquals(result.version, "1.0.0")
        assertEquals(result.imports.toString(),"[{name=ceylon.interop.java, version=1.2.3}, {name=org.springframework.boot:spring-boot-starter-web, namespace=maven, version=1.3.3.RELEASE}, {name=org.springframework.boot:spring-boot-starter-undertow, namespace=maven, version=1.3.3.RELEASE}, {name=org.springframework.cloud:spring-cloud-starter-eureka, namespace=maven, version=1.1.0.RC1}, {name=org.springframework.boot:spring-boot-starter-data-jpa, namespace=maven, version=1.3.3.RELEASE}, {name=postgresql:postgresql, namespace=maven, version=9.1-901-1.jdbc4}, {name=org.liquibase.core, namespace=my_repo, version=3.4.2}, {name=junit:junit, namespace=maven, version=4.12}]")
    }
}
