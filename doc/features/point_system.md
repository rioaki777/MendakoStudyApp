# ポイント管理システム

## 機能概要
レッスン受講で獲得したポイントを管理し、ショップでの商品購入に使用する。ポイントの加算・減算とその履歴をDBで管理する。

## ポイントの加算（獲得）

### 加算タイミング
レッスン結果画面が表示されたとき（`onViewCreated` または `onResume`）に1回だけ加算する。

### 計算式

```
獲得ポイント = 正解数 × 10 + ボーナス

ボーナス:
  全問正解（5/5）→ +20pt
  それ以外        →  +0pt
```

**例:**
| 正解数 | ボーナス | 合計 |
|--------|---------|------|
| 5/5 | +20 | 70pt |
| 4/5 | 0 | 40pt |
| 3/5 | 0 | 30pt |
| 0/5 | 0 | 0pt |

### 加算処理

```kotlin
fun calcEarnedPoints(correctCount: Int, totalCount: Int): Int {
    val base = correctCount * 10
    val bonus = if (correctCount == totalCount) 20 else 0
    return base + bonus
}

// DB更新
userStateDao.addPoints(earnedPoints)
pointHistoryDao.insert(PointHistory(
    type = EARN,
    amount = earnedPoints,
    subject = subjectType,
    timestamp = System.currentTimeMillis()
))
```

## ポイントの減算（消費）

### 減算タイミング
ショップ画面で購入確認ダイアログOKをタップしたとき。

### 条件チェック
```kotlin
if (currentPoints >= item.price) {
    userStateDao.subtractPoints(item.price)
    ownedItemDao.insert(OwnedItem(itemId = item.id, quantity = 1))
    pointHistoryDao.insert(PointHistory(type = SPEND, amount = item.price))
} else {
    // UI: 「ポイントがたりません」を表示
}
```

## データ定義

`UserState.currentPoints` — 現在の保有ポイント（Int、負にならない）

`PointHistory` — ポイント増減の履歴（デバッグ・将来の機能拡張用）

詳細は `data/data_model.md` を参照。

## 関連画面

- レッスン結果画面 (`screens/lesson_result.md`) — ポイント加算
- ショップ画面 (`screens/shop.md`) — ポイント消費
- ホーム画面 (`screens/home.md`) — ポイント表示
