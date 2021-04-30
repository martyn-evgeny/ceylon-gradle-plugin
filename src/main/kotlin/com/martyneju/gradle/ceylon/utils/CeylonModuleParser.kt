package com.martyneju.gradle.ceylon.utils

import java.util.regex.Matcher

class AnnotationState(
    val parsingName: Boolean = false,
    val parsingOpenBracket: Boolean = false,
    val afterOpenBracket: Boolean = false, // could be close bracket or argument
    val parsingArgument: Boolean = false,
    val parsingCloseBracket: Boolean = false
) {
  override fun toString() =
  "AnnotationState(parsingName:${parsingName}, parsingOpenBracket:${parsingOpenBracket}, afterOpenBracket:${afterOpenBracket}, parsingArgument:${parsingArgument}, parsingCloseBracket:${parsingCloseBracket})"
}

class ParsingDocs(val on: Boolean, val isTripleQuote: Boolean)

abstract class BaseState(
    open val parsingDocs: ParsingDocs = ParsingDocs(false, false),
    open val parsingAnnotationState: AnnotationState? = null,
    open val parsingName: Boolean = false,
    open val parsingVersion: Boolean= false
)

class ModuleDeclarationState(
    val parsingStartModule: Boolean = false,
    override val parsingDocs: ParsingDocs = ParsingDocs(false, false),
    override val parsingAnnotationState: AnnotationState? = null,
    override val parsingName: Boolean = false,
    override val parsingVersion: Boolean= false
) : BaseState() {
  override fun toString() =
  "ModuleDeclarationState(parsingStartModule:${parsingStartModule}, super:BaseState(ParsingDocs(${parsingDocs.on}, ${parsingDocs.isTripleQuote}), $parsingAnnotationState, $parsingName, $parsingVersion))"
}

class ModuleImportsState(
    val parsingSemiColon: Boolean = false,
    override val parsingDocs: ParsingDocs = ParsingDocs(false, false),
    override val parsingAnnotationState: AnnotationState? = null,
    override val parsingName: Boolean = false,
    override val parsingVersion: Boolean= false
): BaseState() {
  override fun toString() =
  "ModuleImportsState(parsingSemiColon:${parsingSemiColon}, super:BaseState(ParsingDocs(${parsingDocs.on}, ${parsingDocs.isTripleQuote}), $parsingAnnotationState, $parsingName, $parsingVersion)"
}

class DoneState()

/**
 * Parser of Ceylon module files.
 */
open class CeylonModuleParser {
  companion object{
      val moduleNamespaceRegex = """^[a-z_][a-zA-Z_0-9]*"""
      val moduleIdentifierRegex = """[a-z][a-zA-Z_0-9\.]*[a-zA-Z_0-9]"""
      val mavenModuleIdentifierRegex = """\"[a-z][a-zA-Z_0-9\.:\-]*[a-zA-Z_0-9]\""""
      val annotationNameRegex = """^[a-z_][a-zA-Z_0-9]*"""
      val versionRegex = """^\"[a-zA-Z_0-9][a-zA-Z_0-9\.\-\+]*\"""
      val nonEscapedTripleQuoteRegex = """.*(?<!\\)\"\"\""""
      val nonEscapedQuoteRegex = """.*(?<!\\)\""""
      val endBlockCommentRegex = """.*(?<!\\)\*\/"""

      private var state = ModuleDeclarationState()
      private var currentLine = 0
      private var filname = ""
      private val words = mutableListOf<String>()
      private var lineComment = false
      private var blockComment = false

      fun parse( name: String, text: String ) {
          TODO()
      }

  }
}