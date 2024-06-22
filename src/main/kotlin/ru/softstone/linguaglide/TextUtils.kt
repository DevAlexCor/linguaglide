package ru.softstone.linguaglide

fun String.replaceNonKeyboardChars(): String {
    // Define a mapping of non-keyboard characters to their keyboard-friendly replacements
    val replacementMap = mapOf(
        '“' to "\"", '”' to "\"", '‘' to "'", '’' to "'",
        '—' to "-", '–' to "-", '…' to "...", '‹' to "<", '›' to ">", '«' to "<<",
        '»' to ">>", '„' to "\"", '‚' to "'", '•' to "*", '′' to "'", '″' to "\"",
        '¢' to "c", '£' to "L", '¤' to "$", '¥' to "Y", '©' to "(c)", '®' to "(r)",
        'µ' to "u", '¶' to "P", '¿' to "?", '×' to "x", '÷' to "/", '§' to "S",
        '°' to "o", '€' to "E", '™' to "TM", '†' to "+", '‡' to "++", '¢' to "c",
        '¨' to "\"", '¡' to "!", 'ˆ' to "^", '˚' to "o", '‹' to "<", '›' to ">",
        '⁄' to "/", 'ﬁ' to "fi", 'ﬂ' to "fl", '‡' to "++", '·' to ".", '‚' to ",",
        '„' to ",,", '‰' to "%", '‹' to "<", '›' to ">", '−' to "-", 'ﬂ' to "fl",
        'ﬂ' to "fl", 'ƒ' to "f", '∂' to "d", '∆' to "D", '∏' to "P", '∑' to "S",
        '√' to "v", '∞' to "8", '∫' to "S", '≈' to "~", '≠' to "!=", '≤' to "<=",
        '≥' to ">="
    )

    // Build the output string with replacements
    val result = StringBuilder()

    for (char in this) {
        if (char in replacementMap) {
            result.append(replacementMap[char])
        } else {
            result.append(char)
        }
    }

    return result.toString()
}

fun String.removeNonQwertyChars(): String {
    // Define a set of valid QWERTY characters
    val qwertyChars =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=~`[]{}|;:'\",.<>?/\\ \t\n\r".toSet()

    // Filter the input string to keep only valid QWERTY characters
    return filter { it in qwertyChars }
}

fun String.normalizeWhitespace(): String {
    // Use a regular expression to replace one or more whitespace characters with a single space
    return replace(Regex("\\s+"), " ").trim()
}

fun String.normalizeNewLines(): String {
    // Use a regular expression to replace one or more newline characters with a single newline
    return replace(Regex("\\n+"), "\n").trim()
}
