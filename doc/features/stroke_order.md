# ひらがな書き順システム

## 機能概要
ひらがな1文字を正しい書き順でなぞれたかを判定するシステム。各ひらがなの書き順は事前定義したストロークパスデータとして保持し、ユーザーの入力と照合する。

## アーキテクチャ概要

```
[HiraganaStrokeData] --- 定義済みストロークパス（アセット）
         ↓
[StrokeOrderJudge]  --- 入力パスとの一致判定
         ↑
[HiraganaLessonScreen] --- ユーザーのCanvasタッチ入力
```

## ストロークパスデータ

各ひらがな1文字につき、書き順ごとのストロークを定義する。

```kotlin
data class HiraganaStrokeData(
    val char: Char,                      // 例: 'あ'
    val strokes: List<StrokePath>        // 書き順通りのストロークリスト
)

data class StrokePath(
    val order: Int,                      // 書き順（1始まり）
    val keyPoints: List<PointF>,         // 正解パスの代表点（正規化座標 0.0〜1.0）
    val direction: StrokeDirection       // LEFT_TO_RIGHT / TOP_TO_BOTTOM / CURVE 等
)
```

データはJSONファイルとしてassets/に格納し、アプリ起動時に一度読み込む。

```
assets/stroke_data/hiragana_strokes.json
```

## 判定アルゴリズム

### ステップ1: ストローク数チェック
ユーザーが入力したストローク数が正解のストローク数と一致するか確認する。
- 不一致 → 「もう一度」

### ステップ2: 各ストロークの方向チェック

ユーザーのi番目のストロークを正解のi番目のストロークパスと比較する。

```kotlin
fun judgeStroke(userPath: List<PointF>, expected: StrokePath): Boolean {
    // ユーザーのパスを正規化
    val normalized = normalizePoints(userPath, canvasWidth, canvasHeight)

    // 始点・終点の距離チェック（許容誤差: 0.15）
    val startOk = distance(normalized.first(), expected.keyPoints.first()) < 0.15f
    val endOk   = distance(normalized.last(),  expected.keyPoints.last())  < 0.15f

    // 中間点との一致（代表点との平均距離が閾値以内か）
    val midOk = averageMinDistance(normalized, expected.keyPoints) < 0.20f

    return startOk && endOk && midOk
}
```

### ステップ3: 合否判定

全ストロークが判定OKなら「正解」、1つでもNGなら「不正解」。

```kotlin
fun judge(userStrokes: List<List<PointF>>, expected: HiraganaStrokeData): Boolean {
    if (userStrokes.size != expected.strokes.size) return false
    return userStrokes.zip(expected.strokes).all { (userStroke, expectedStroke) ->
        judgeStroke(userStroke, expectedStroke)
    }
}
```

## 入出力

**入力:**
- `userStrokes: List<List<PointF>>` — Canvas上の指軌跡（ストロークごと）
- `char: Char` — 判定対象のひらがな文字

**出力:** `Boolean` — 正解 / 不正解

## 関連画面・機能

- ひらがな書き方レッスン画面 (`screens/hiragana_lesson.md`)
- データモデル (`data/data_model.md`) — `HiraganaQuestion` テーブル

## 実装上の注意

- Canvasへの描画はハードウェアアクセラレーション有効化で滑らかにする
- ストロークは指を離したタイミングで1本分として確定する
- 入力中のリアルタイム描画と判定処理は分離する（UI thread / coroutine）
- 46文字分のストロークデータ定義が必要（濁点・半濁点は任意対応）
