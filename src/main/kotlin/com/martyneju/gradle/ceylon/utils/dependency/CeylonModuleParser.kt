package com.martyneju.gradle.ceylon.utils.dependency

import org.gradle.api.artifacts.ResolvedDependency
import java.lang.Exception
import java.lang.RuntimeException

sealed class State

class AnnotationState(
    val parsingName: Boolean = false,
    val parsingOpenBracket: Boolean = false,
    val afterOpenBracket: Boolean = false, // could be close bracket or argument
    val parsingArgument: Boolean = false,
    val parsingCloseBracket: Boolean = false
): State() {
  override fun toString() =
  "AnnotationState(parsingName:${parsingName}, parsingOpenBracket:${parsingOpenBracket}, afterOpenBracket:${afterOpenBracket}, parsingArgument:${parsingArgument}, parsingCloseBracket:${parsingCloseBracket})"
}

class ParsingDocs(val on: Boolean, val isTripleQuote: Boolean)

abstract class BaseState(
    open val parsingDocs: ParsingDocs = ParsingDocs(false, false),
    open var parsingAnnotationState: AnnotationState? = null,
    open val parsingName: Boolean = false,
    open val parsingVersion: Boolean= false
): State()

class ModuleDeclarationState(
    val parsingStartModule: Boolean = false,
    override val parsingDocs: ParsingDocs = ParsingDocs(false, false),
    override var parsingAnnotationState: AnnotationState? = null,
    override val parsingName: Boolean = false,
    override val parsingVersion: Boolean= false
) : BaseState() {
  override fun toString() =
  "ModuleDeclarationState(parsingStartModule:${parsingStartModule}, super:BaseState(ParsingDocs(${parsingDocs.on}, ${parsingDocs.isTripleQuote}), $parsingAnnotationState, $parsingName, $parsingVersion))"
}

class ModuleImportsState(
    val parsingSemiColon: Boolean = false,
    override val parsingDocs: ParsingDocs = ParsingDocs(false, false),
    override var parsingAnnotationState: AnnotationState? = null,
    override val parsingName: Boolean = false,
    override val parsingVersion: Boolean= false
): BaseState() {
  override fun toString() =
  "ModuleImportsState(parsingSemiColon:${parsingSemiColon}, super:BaseState(ParsingDocs(${parsingDocs.on}, ${parsingDocs.isTripleQuote}), $parsingAnnotationState, $parsingName, $parsingVersion)"
}

class DoneState : State()

class CeylonModule() {
    var moduleName = ""
    var version = ""
    var hasDocs = false
    val imports: MutableList<CeylonImport> = mutableListOf()
    var shared = false
}

class CeylonImport() {
    var shared: Boolean? = null
    var namespace: String? = null
    var name: String? = null
    var version: String? = null
    var resolvedDependency: ResolvedDependency? = null

    fun modify(another: CeylonImport) {
        if(another.namespace != null) this.namespace = another.namespace
        if(another.name != null) this.name = another.name
        if(another.version != null) this.version = another.version
        if(another.shared != null) this.shared = another.shared
    }

    override fun toString(): String =
        (if(shared != null) "shared=${shared} " else "") +
        (if(namespace!=null) "namespace=${namespace} " else "") +
        (if(name != null) "name=${name} " else "") +
        (if(version != null) "version=${version}" else "")
}

typealias map = CeylonModule

/**
 * Parser of Ceylon module files.
 */
open class CeylonModuleParser(val fileName: String) {
    val moduleNamespaceRegex = """^[a-z_][a-zA-Z_0-9]*""".toRegex()
    val moduleIdentifierRegex = """[a-z][a-zA-Z_0-9\.]*[a-zA-Z_0-9]""".toRegex()
    val mavenModuleIdentifierRegex = """\"[a-z][a-zA-Z_0-9\.:\-]*[a-zA-Z_0-9]\"""".toRegex()

    val annotationNameRegex = """^[a-z_][a-zA-Z_0-9]*""".toRegex()
    val versionRegex = """^\"[a-zA-Z_0-9][a-zA-Z_0-9\.\-\+]*\"""".toRegex()
    val nonEscapedTripleQuoteRegex = """.*(?<!\\)\"\"\"""".toRegex()
    val nonEscapedQuoteRegex = """.*(?<!\\)\"""".toRegex()
    val endBlockCommentRegex = """.*(?<!\\)\*\/""".toRegex()

    private var state: State = ModuleDeclarationState()
    private var currentLine = 0
    private var words = mutableListOf<String>()
    private var lineComment = false
    private var blockComment = false

    fun parse(text: String): map {
      val result: map = CeylonModule()
      text.lines().forEach {
          words.addAll(it.split(" ","\t").filter { !it.isEmpty() })
          currentLine++
          lineComment = false
          while( !words.isEmpty() ) {
              val word = words.removeAt(0)
              if( !lineComment ) {
                  val _state = state
                  when( _state ) {
                      is ModuleDeclarationState -> parseModuleDeclaration( word, _state, result )
                      is ModuleImportsState -> parseModuleImports(word, _state, result)
                      is DoneState -> return@forEach
                      else -> throw Exception("internal state not recognized: ${_state}")
                  }
              }
          }
      }
      return result
    }

    private fun parseModuleDeclaration(word: String, state: ModuleDeclarationState, result: map) {
        if( blockComment ) {
            val matchRes = endBlockCommentRegex.find(word)
            if( matchRes != null) {
                blockComment = false
                val lastIndex = matchRes.range.last
                consumeChars(lastIndex, word, result)
            }
        } else if (word.startsWith("/*") && !state.parsingDocs.on) {
            blockComment = true
            consumeChars(2, word,result)
        } else if ( word.startsWith( "//")) {
            lineComment = true
        } else if( state.parsingAnnotationState != null ) {
            parseAnnotation( word, state, state.parsingAnnotationState!!, result)
        } else if (state.parsingName) {
            val matcherRes = moduleIdentifierRegex.find(word)
            if( matcherRes != null ) {
                val lastIndex = matcherRes.range.last
                result.moduleName =  word.substring(0,lastIndex + 1)
                this.state = ModuleDeclarationState(parsingVersion = true)
                consumeChars(lastIndex, word, result)
            } else {
                throw error("expected module name, found <${word}>")
            }
        } else if ( state.parsingVersion ) {
            val matcherRes = versionRegex.find(word)
            if(matcherRes != null) {
                val lastIndex = matcherRes.range.last
                result.version = word.substring(1, lastIndex)
                this.state = ModuleDeclarationState(parsingStartModule = true)
                consumeChars(lastIndex, word, result)
            } else {
                throw error("expecte module version, found $word")
            }
        } else if (state.parsingStartModule ) {
            if(word.startsWith("{")) {
                this.state = ModuleImportsState()
                if( word.length > 1) {
                    val new_words = mutableListOf(word.substring(1))
                    new_words.addAll(words)
                    this.words = new_words
                }
            }
        } else if (state.parsingDocs.on ) {
            val matcherRes = matcherFor(state.parsingDocs, word)
            if(matcherRes != null) {
                val lastIndex = matcherRes.range.last
                result.hasDocs = true
                this.state = ModuleDeclarationState()
                consumeChars(lastIndex, word, result)
            }
        } else { //begin
            when {
                word == "module" -> this.state = ModuleDeclarationState(parsingName = true)
                word.startsWith("\"") -> {
                    if(result.hasDocs) {
                        throw error("more than one doc String is not allowed")
                    }
                    val isTripleQuote = word.startsWith("\"\"\"")
                    this.state = ModuleDeclarationState(parsingDocs = ParsingDocs(true, isTripleQuote))
                    consumeChars(0, word, result)
                }
                else -> {
                    this.state = ModuleDeclarationState(parsingAnnotationState = AnnotationState(parsingName = true))
                    val new_words = mutableListOf(word)
                    new_words.addAll(words)
                    this.words = new_words
                }
            }
        }
    }

    private fun parseModuleImports(word: String, state: ModuleImportsState, result: map) {
        if( blockComment) {
            val matcherRes = endBlockCommentRegex.find(word)
            if (matcherRes != null ) {
                blockComment = false
                val lastIndex = matcherRes.range.last
                consumeChars( lastIndex, word, result)
            }
        } else if (state.parsingDocs.on) {
            val matcherRes = matcherFor(state.parsingDocs, word)
            if( matcherRes != null ) {
                val lastIndex = matcherRes.range.last
                this.state = ModuleImportsState()
                consumeChars( lastIndex, word, result )
            }
        } else if (word.startsWith("/*")) {
            blockComment = true
            consumeChars( 2 , word, result)
        } else if (word.startsWith("//")) {
            lineComment = true
        } else if (state.parsingAnnotationState != null){
            parseAnnotation(word, state, state.parsingAnnotationState!!, result)
        } else if (state.parsingName) {
            var namespace: String? = null
            var _word = word
            var matcherRes : MatchResult? = null
            if (word.startsWith("maven:")) {
                namespace = "maven"
                _word = _word.substring(6)
                matcherRes = mavenModuleIdentifierRegex.find(_word)
            } else if( word.startsWith("\"") ) {
                namespace = "maven"
                matcherRes = mavenModuleIdentifierRegex.find(_word)
            } else if(word.startsWith("npm:")) {
                throw error("namespace npm not support yet")
            } else {
                if(_word.contains(":")) {
                    val index = _word.indexOf(":")
                    namespace = _word.substring(0, index)
                    _word = _word.substring(index+1)
                }
                matcherRes = moduleIdentifierRegex.find(_word)
            }
            if(matcherRes != null ) {
                var name = _word.substring(0,matcherRes.range.last+1)
                if( namespace == "maven" ) {
                    /* remove the quotes if it's a Maven match */
                    name = name.replace("\"","")
                }
                val imports = result.imports

                val ceylonImport = CeylonImport()
                ceylonImport.name = name
                if( namespace != null )  ceylonImport.namespace = namespace

                if(imports.isNotEmpty() && imports.last().name==null) { // newest entry has no name yet
                    assert(imports.last().shared != null)
                    imports.last().modify(ceylonImport)
                } else {
                    imports.add(ceylonImport)
                }
                this.state = ModuleImportsState(parsingVersion = true)
            } else {
                throw error("expected imported module name, found $word")
            }
        } else if (state.parsingVersion) {
            val macherRes = versionRegex.find(word)
            if (macherRes != null) {
                val lastIndex = macherRes.range.last
                result.imports.last().version = word.substring(1,lastIndex)
                this.state = ModuleImportsState(parsingSemiColon = true)
                consumeChars(lastIndex, word, result)
            } else {
                throw error("expected module version, found $word")
            }
        } else if (state.parsingSemiColon) {
            if (word.startsWith(";")) {
                this.state = ModuleImportsState()
                consumeChars( 0, word, result)
            } else {
                throw error("expected semi-colon, found $word")
            }
        } else { // begin or end
            if (word == "import") {
                this.state = ModuleImportsState(parsingName = true)
            } else if (word.startsWith("\"")) {
                val isTripleQuote = word.startsWith("\"\"\"")
                this.state = ModuleImportsState(parsingDocs = ParsingDocs(true, isTripleQuote))
                consumeChars(0, word, result)
            } else if (word == "}") {
                this.state = DoneState()
            } else {
                this.state = ModuleImportsState(parsingAnnotationState = AnnotationState(parsingName = true))
                // put word back in the queue as we don't know what it is
                val new_words = mutableListOf(word)
                new_words.addAll(words)
                words = new_words
            }
        }
    }

    private fun parseAnnotation(word: String, state: BaseState, aState: AnnotationState, result: map) {
        if (blockComment) {
            val matcherRes = endBlockCommentRegex.find(word)
            if (matcherRes != null) {
                blockComment =false
                val lastIndex = matcherRes.range.last
                consumeChars( lastIndex, word, result)
            }
        } else if (aState.parsingArgument ) {
            val matcherRes = nonEscapedQuoteRegex.find(word)
            if (matcherRes != null ) {
                state.parsingAnnotationState = AnnotationState(parsingCloseBracket = true)
                val lastIndex = matcherRes.range.last
                consumeChars( lastIndex, word, result)
            }
        } else if (word.startsWith("/*")) {
            blockComment = true
            consumeChars(2, word, result)
        } else if( word.startsWith("//")) {
            lineComment = true
        } else if (aState.parsingName ) {
            if(state is ModuleImportsState && word == "import") {
                this.state = ModuleImportsState(parsingName = true)
            } else if (state is ModuleDeclarationState && word == "module") {
                this.state = ModuleDeclarationState(parsingName = true)
            } else {
                val matcherRes = annotationNameRegex.find(word)
                if (matcherRes != null ) {
                    val lastIndex = matcherRes.range.last
                    val annotation = word.substring(0, lastIndex+1)
                    if (annotation == "shared") {
                        if(state is ModuleDeclarationState) {
                            result.shared = true
                        } else {
                            val ceylonImport = CeylonImport()
                            ceylonImport.shared = true
                            result.imports.add(ceylonImport)
                        }
                    }
                    state.parsingAnnotationState = AnnotationState(parsingOpenBracket = true)
                    consumeChars(lastIndex, word, result)
                } else {
                    throw error("expected annotation or module name, found $word")
                }
            }
        } else if (aState.parsingOpenBracket) {
            if(word.startsWith("(")) {
                state.parsingAnnotationState = AnnotationState(afterOpenBracket = true)
                consumeChars(0, word, result)
            } else {
                /*  T O D O: annotations don't need args!!! */
                val new_words = mutableListOf(word)
                new_words.addAll(words)
                words = new_words
                if(state is ModuleDeclarationState) {
                    this.state = ModuleDeclarationState()
                } else {
                    this.state = ModuleImportsState()
                }
            }
        } else if (aState.afterOpenBracket) {
            if (word.startsWith("\"")) {
                state.parsingAnnotationState = AnnotationState(parsingArgument = true)
                consumeChars(0, word, result)
            } else {
                state.parsingAnnotationState = AnnotationState(parsingCloseBracket = true)
            }
        } else if (aState.parsingCloseBracket) {
            if(word.startsWith(")")) {
                if(state is ModuleDeclarationState) {
                    this.state = ModuleDeclarationState()
                } else {
                    this.state = ModuleImportsState()
                }
                if (word.length > 1) {
                    val new_word = word.drop(1)
                    if( state is ModuleDeclarationState) {
                        parseModuleDeclaration(new_word, state, result)
                    } else if ( state is ModuleImportsState) {
                        parseModuleImports(new_word, state, result)
                    }
                }
            } else {
                throw error("expecte annotation <)>, found $word")
            }
        } else {  // start over
            state.parsingAnnotationState = AnnotationState(parsingName = true)
        }
    }

    private fun consumeChars(count: Int, word: String, result: map) {
        if( count < word.length - 1 ) {
           val wordSub = word.substring(count+1)
            when(val _state = state){
                is ModuleImportsState -> parseModuleImports(wordSub, _state, result)
                is ModuleDeclarationState -> parseModuleDeclaration(wordSub, _state, result)
            }
        }
    }

    private fun error(message: String) =
        RuntimeException("Cannot parse module [${fileName}]. Error on line ${currentLine}: $message")

    private fun matcherFor(state: ParsingDocs, word: String) =
        if(state.isTripleQuote) nonEscapedTripleQuoteRegex.find(word) else nonEscapedQuoteRegex.find(word)

}