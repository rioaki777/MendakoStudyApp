# データモデル定義

## 概要
ローカルストレージとして Room Database を使用する。テーブル構成は以下の通り。

---

## テーブル一覧

### 1. `user_state` — ユーザーの現在状態

アプリ全体で1レコードのみ（id=1固定）。

| フィールド | 型 | 説明 |
|-----------|-----|------|
| `id` | Int (PK) | 常に1 |
| `current_points` | Int | 現在の保有ポイント（≥0） |
| `equipped_accessories` | String | 装着中アクセサリーIDのJSON配列 例: `"[1,3]"` |

---

### 2. `lesson_stats` — 教材ごとの統計

教材の種類ごとに1レコード（subjectが一意）。

| フィールド | 型 | 説明 |
|-----------|-----|------|
| `subject` | String (PK) | `"ADDITION"` / `"SUBTRACTION"` / `"HIRAGANA"` |
| `recent_results` | String | 直近20問の正誤をJSON配列で保持 例: `"[true,false,true,...]"` |
| `total_attempts` | Int | 累計出題数 |
| `total_correct` | Int | 累計正解数 |

---

### 3. `point_history` — ポイント履歴

| フィールド | 型 | 説明 |
|-----------|-----|------|
| `id` | Int (PK, autoGenerate) | |
| `type` | String | `"EARN"` / `"SPEND"` |
| `amount` | Int | ポイント量（正の整数） |
| `subject` | String? | EARN時: 教材種別、SPEND時: null |
| `item_id` | Int? | SPEND時: 購入商品ID、EARN時: null |
| `timestamp` | Long | Unix timestamp (ms) |

---

### 4. `shop_item` — 商品マスタ（固定データ）

アプリ初回起動時にシードデータとして投入する。

| フィールド | 型 | 説明 |
|-----------|-----|------|
| `id` | Int (PK) | |
| `name` | String | 商品名 |
| `category` | String | `"FOOD"` / `"ACCESSORY"` / `"FURNITURE"` |
| `price` | Int | 必要ポイント数 |
| `image_res_name` | String | drawableリソース名 |

---

### 5. `owned_item` — 所持品

| フィールド | 型 | 説明 |
|-----------|-----|------|
| `item_id` | Int (PK, FK → shop_item.id) | |
| `quantity` | Int | 所持数（消耗品は減算、非消耗品は1固定） |

---

### 6. `furniture_placement` — 家具の配置状態

| フィールド | 型 | 説明 |
|-----------|-----|------|
| `item_id` | Int (PK, FK → shop_item.id) | |
| `x` | Float | 部屋ビュー内の相対X座標 (0.0〜1.0) |
| `y` | Float | 部屋ビュー内の相対Y座標 (0.0〜1.0) |

---

### 7. `hiragana_question` — ひらがな問題文

管理画面から登録・管理される。

| フィールド | 型 | 説明 |
|-----------|-----|------|
| `id` | Int (PK, autoGenerate) | |
| `text` | String | 問題文（例: 「いぬ」「あおい そら」） |
| `created_at` | Long | 登録日時 (Unix ms) |
| `attempt_count` | Int | 受講回数 |
| `correct_count` | Int | 正解回数（全文字正解でカウント） |

---

## エンティティ間の関係

```
user_state (1)
    └── equipped_accessories → shop_item.id (ACCESSORY category)

owned_item.item_id → shop_item.id
furniture_placement.item_id → shop_item.id (FURNITURE category)

point_history.item_id → shop_item.id (nullable)
```

---

## Roomデータベース設定

```kotlin
@Database(
    entities = [
        UserState::class,
        LessonStats::class,
        PointHistory::class,
        ShopItem::class,
        OwnedItem::class,
        FurniturePlacement::class,
        HiraganaQuestion::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userStateDao(): UserStateDao
    abstract fun lessonStatsDao(): LessonStatsDao
    abstract fun pointHistoryDao(): PointHistoryDao
    abstract fun shopItemDao(): ShopItemDao
    abstract fun ownedItemDao(): OwnedItemDao
    abstract fun furniturePlacementDao(): FurniturePlacementDao
    abstract fun hiraganaQuestionDao(): HiraganaQuestionDao
}
```

初回起動時のシードデータ投入には `RoomDatabase.Callback.onCreate` を使用する。
