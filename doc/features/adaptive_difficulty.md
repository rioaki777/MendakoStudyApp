# 適応難易度アルゴリズム

## 機能概要
子どもの直近の正答率に応じて出題する数の範囲を動的に調整する。正答率が高いほど大きい数が出題されやすくなり、低いと小さい数に戻る。

## 対象教材

| 教材 | 調整するパラメータ | 上限値の範囲 |
|------|-----------------|------------|
| 足し算 | 出題する数の上限 | 5〜20 |
| 引き算 | 出題する数の上限 | 3〜10 |

## アルゴリズム

### 1. 正答率の計算

直近 **N = 20問** の回答履歴を参照する。

```
正答率 = 直近N問の正解数 / N
（回答数がN未満の場合は実際の回答数で割る）
```

### 2. 上限値の決定

正答率をもとに上限値テーブルを参照する。

**足し算（答えの上限: 5〜20）:**

| 正答率 | 上限値 |
|--------|--------|
| 0〜39% | 5 |
| 40〜59% | 8 |
| 60〜74% | 12 |
| 75〜89% | 16 |
| 90〜100% | 20 |

**引き算（被減数の上限: 3〜10）:**

| 正答率 | 上限値 |
|--------|--------|
| 0〜39% | 3 |
| 40〜59% | 5 |
| 60〜74% | 7 |
| 75〜89% | 9 |
| 90〜100% | 10 |

### 3. 問題の生成

```kotlin
// 足し算の場合
fun generateAdditionQuestion(maxAnswer: Int): AdditionQuestion {
    val a = Random.nextInt(1, maxAnswer)          // 1〜(maxAnswer-1)
    val b = Random.nextInt(1, maxAnswer - a + 1)  // 1〜(maxAnswer-a)
    val answer = a + b
    val choices = generateChoices(answer, min=1, max=maxAnswer)
    return AdditionQuestion(a, b, answer, choices)
}

// 引き算の場合
fun generateSubtractionQuestion(maxOperand: Int): SubtractionQuestion {
    val a = Random.nextInt(2, maxOperand + 1)  // 2〜maxOperand
    val b = Random.nextInt(1, a)              // 1〜a-1（答えが0以上になる）
    val answer = a - b
    val choices = generateChoices(answer, min=0, max=maxOperand)
    return SubtractionQuestion(a, b, answer, choices)
}
```

### 4. 選択肢の生成

```kotlin
fun generateChoices(answer: Int, min: Int, max: Int): List<Int> {
    val choices = mutableSetOf(answer)
    val offsets = listOf(-3, -2, -1, 1, 2, 3).shuffled()
    for (offset in offsets) {
        val candidate = answer + offset
        if (candidate in min..max && candidate != answer) {
            choices.add(candidate)
        }
        if (choices.size == 4) break
    }
    // 4つに満たない場合は範囲内の未使用整数で補完
    var fill = min
    while (choices.size < 4) {
        if (fill !in choices) choices.add(fill)
        fill++
    }
    return choices.toList().shuffled()
}
```

## 入出力

**入力:** `LessonStats` (subject, recentResults: List\<Boolean\>)

**出力:** 上限値 Int → `AdditionQuestion` or `SubtractionQuestion`

## 関連画面

- 足し算レッスン画面 (`screens/addition_lesson.md`)
- 引き算レッスン画面 (`screens/subtraction_lesson.md`)
- レッスン結果画面 (`screens/lesson_result.md`) — 正答率の更新
- データモデル (`data/data_model.md`) — `LessonStats` テーブル
