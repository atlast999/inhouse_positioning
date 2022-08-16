package com.example.composeapp

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
//class ExampleUnitTest {
//    @Test
//    fun addition_isCorrect() {
//        assertEquals(4, 2 + 2)
//    }
//}

fun main() {
    println(
//        findSubstring(
//            s = "lingmindraboofooowingdingbarrwingmonkeypoundcake",
//            words = arrayOf("fooo","barr","wing","ding","wing")
//        )
        findSubstring(
            s = "barfoothefoobarman",
            words = arrayOf("foo", "bar")
        )
    )
}

fun findSubstring(s: String, words: Array<String>): List<Int> {
    val res = mutableListOf<Int>()
    val wordSize = words[0].length
    val window = words.size.times(wordSize)
    val wordHash = mutableMapOf<String, Int>().apply {
        words.forEach { word ->
            this[word]?.let {
                this[word] = it + 1
            } ?: run { this[word] = 1 }
        }
    }.toMap()
    outer@ for (i in 0..s.length.minus(window)) {
        val chunks = s.substring(i, i.plus(window)).chunked(wordSize).toTypedArray()
        val map = wordHash.toMutableMap()
        for (chunk in chunks) {
            map[chunk]?.let {
                map[chunk] = it - 1
            } ?: continue@outer
        }
        if (map.all { it.value == 0 }) {
            res.add(i)
        }
    }
    return res
}