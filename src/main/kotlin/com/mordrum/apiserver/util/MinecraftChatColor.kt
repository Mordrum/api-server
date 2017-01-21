package com.mordrum.apiserver.util

enum class MinecraftChatColor private constructor(val char: Char, private val intCode: Int, val isFormat: Boolean = false) {
    BLACK('0', 0),
    DARK_BLUE('1', 1),
    DARK_GREEN('2', 2),
    DARK_AQUA('3', 3),
    DARK_RED('4', 4),
    DARK_PURPLE('5', 5),
    GOLD('6', 6),
    GRAY('7', 7),
    DARK_GRAY('8', 8),
    BLUE('9', 9),
    GREEN('a', 10),
    AQUA('b', 11),
    RED('c', 12),
    LIGHT_PURPLE('d', 13),
    YELLOW('e', 14),
    WHITE('f', 15),
    MAGIC('k', 16, true),
    BOLD('l', 17, true),
    STRIKETHROUGH('m', 18, true),
    UNDERLINE('n', 19, true),
    ITALIC('o', 20, true),
    RESET('r', 21);

    private val toString: String

    init {
        this.toString = String(charArrayOf('\u00a7', char))
    }

    override fun toString(): String {
        return this.toString
    }

    val isColor: Boolean
        get() = !this.isFormat && this != RESET
//
//    companion object {
//
//        val COLOR_CHAR: Char = 167.toChar()
//
//        fun translateAlternateColorCodes(altColorChar: Char, textToTranslate: String): String {
//            val b = textToTranslate.toCharArray()
//            for (i in 0..b.size - 1 - 1) {
//                if (b[i] != altColorChar || "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1].toInt()) <= -1) continue
//                b[i] = 167.toChar()
//                b[i + 1] = Character.toLowerCase(b[i + 1])
//            }
//            return String(b)
//        }
//    }
}