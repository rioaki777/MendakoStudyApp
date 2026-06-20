package com.rioaki.mendakostudyapp.util

object PointCalculator {
    private const val POINTS_PER_CORRECT = 10
    private const val BONUS_ALL_CORRECT = 20

    fun calc(correctCount: Int, totalCount: Int): Int {
        val base = correctCount * POINTS_PER_CORRECT
        val bonus = if (correctCount == totalCount && totalCount > 0) BONUS_ALL_CORRECT else 0
        return base + bonus
    }
}
