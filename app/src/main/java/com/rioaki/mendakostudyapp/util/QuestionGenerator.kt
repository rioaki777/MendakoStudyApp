package com.rioaki.mendakostudyapp.util

import kotlin.random.Random

data class AdditionQuestion(
    val operandA: Int,
    val operandB: Int,
    val answer: Int,
    val choices: List<Int>
)

data class SubtractionQuestion(
    val operandA: Int,
    val operandB: Int,
    val answer: Int,
    val choices: List<Int>
)

object QuestionGenerator {

    fun generateAdditionSet(maxAnswer: Int, count: Int = 5): List<AdditionQuestion> =
        (0 until count).map { generateAddition(maxAnswer) }

    fun generateSubtractionSet(maxAnswer: Int, count: Int = 5): List<SubtractionQuestion> =
        (0 until count).map { generateSubtraction(maxAnswer) }

    fun generateAddition(maxAnswer: Int): AdditionQuestion {
        val max = maxAnswer.coerceAtLeast(3)
        val a = Random.nextInt(1, max)
        val b = Random.nextInt(1, max - a + 1)
        val answer = a + b
        return AdditionQuestion(a, b, answer, generateChoices(answer, 1, max + 3))
    }

    fun generateSubtraction(maxAnswer: Int): SubtractionQuestion {
        // 答えが maxAnswer 以下になるように、答えと引く数を先に決めてから引かれる数を求める。
        val max = maxAnswer.coerceAtLeast(3)
        val answer = Random.nextInt(0, max + 1)   // 答え 0〜max
        val b = Random.nextInt(1, max + 1)        // 引く数 1〜max
        val a = answer + b                         // 引かれる数 = 答え + 引く数
        return SubtractionQuestion(a, b, answer, generateChoices(answer, 0, max))
    }

    private fun generateChoices(answer: Int, min: Int, max: Int): List<Int> {
        val pool = (min..max).toMutableList()
        pool.remove(answer)
        pool.shuffle()
        val choices = mutableListOf(answer)
        choices.addAll(pool.take(3))
        return choices.shuffled()
    }
}
