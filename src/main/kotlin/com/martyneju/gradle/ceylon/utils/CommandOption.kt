package com.martyneju.gradle.ceylon.utils

/**
 * A simple command option.
 *
 */
open class CommandOption(val option:String, val argument: String? = null ) {
    /**
     * returns the option without quoting the argument (if there is an argument).
     */
    override fun toString(): String = "${option} ${ if(argument==null) "=${argument}" else "" }"

    /**
     * To get the argument quoted.
     */
    fun withQuotedArgument():String = "${option} ${ if(argument==null) "=\"${argument}\"" else "" }"
}