package com.rioaki.mendakostudyapp.util

object PointCalculator {
    private const val POINTS_PER_CORRECT = 1

    fun calc(correctCount: Int, totalCount: Int): Int {
        return correctCount * POINTS_PER_CORRECT
    }
}
