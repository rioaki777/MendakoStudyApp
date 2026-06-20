package com.rioaki.mendakostudyapp.util

import org.json.JSONArray

object AdaptiveDifficulty {

    private const val HISTORY_SIZE = 20

    fun calcAdditionMaxAnswer(recentResults: String): Int {
        val rate = calcAccuracyRate(recentResults)
        return when {
            rate >= 0.90f -> 20
            rate >= 0.75f -> 16
            rate >= 0.60f -> 12
            rate >= 0.40f -> 8
            else -> 5
        }
    }

    fun calcSubtractionMaxOperand(recentResults: String): Int {
        val rate = calcAccuracyRate(recentResults)
        return when {
            rate >= 0.90f -> 10
            rate >= 0.75f -> 9
            rate >= 0.60f -> 7
            rate >= 0.40f -> 5
            else -> 3
        }
    }

    fun calcAccuracyPercent(recentResults: String): Int =
        (calcAccuracyRate(recentResults) * 100).toInt()

    fun appendResults(recentResults: String, newResults: List<Boolean>): String {
        val list = parseResults(recentResults).toMutableList()
        list.addAll(newResults)
        val trimmed = if (list.size > HISTORY_SIZE) list.takeLast(HISTORY_SIZE) else list
        val arr = JSONArray()
        trimmed.forEach { arr.put(it) }
        return arr.toString()
    }

    private fun calcAccuracyRate(recentResults: String): Float {
        val list = parseResults(recentResults)
        if (list.isEmpty()) return 0f
        return list.count { it }.toFloat() / list.size
    }

    private fun parseResults(json: String): List<Boolean> {
        val arr = try { JSONArray(json) } catch (e: Exception) { return emptyList() }
        return (0 until arr.length()).map { arr.getBoolean(it) }
    }
}
