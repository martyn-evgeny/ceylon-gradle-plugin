package com.martyneju.gradle.ceylon

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class CeylonPluginTest {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun `run test`(){
        print("Hello world!")
    }
}