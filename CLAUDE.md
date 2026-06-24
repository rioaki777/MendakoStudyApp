# CLAUDE.md

このファイルは、このリポジトリで作業する Claude Code 向けのガイドです。

## プロジェクト概要

4〜6歳児向けの **Androidの知育アプリ**。タコの「メンダコ」がメインキャラクター。
教材（足し算・引き算・ひらがな）を受講してポイントを貯め、ショップでメンダコの
ごはん・アクセサリー・家具と交換し、メンダコを育てて部屋を飾る、という育成要素を持つ。

詳細な仕様は `doc/` 配下に日本語で整理されている（`doc/overview.md` が起点）。

## 技術スタック

- 言語: **Kotlin** / **Java 17**
- UI: View + **ViewBinding**（Jetpack Compose は不使用）、Material Components、ConstraintLayout
- アーキテクチャ: **MVVM**（Fragment + `AndroidViewModel` + LiveData）
- 画面遷移: **Navigation Component**（単一 Activity `MainActivity` + `res/navigation/nav_graph.xml`、起点は `HomeFragment`）
- 永続化: **Room**（`ksp` でコンパイル時生成）
- 非同期: **Kotlin Coroutines**
- 手書き認識: **ML Kit Digital Ink Recognition**
- SDK: `minSdk 26` / `compile/targetSdk 34`
- DI フレームワークは不使用（シングルトン `object` と `AppDatabase.getInstance()` で代替）

## ビルド・実行

Windows 環境。シェルは PowerShell（Bash ツールも利用可）。ラッパーは `gradlew.bat`。

```powershell
# デバッグ APK をビルド（コンパイル + Room/KSP 検証 + リソース検証まで通る）
.\gradlew.bat :app:assembleDebug --console=plain

# Kotlin だけ素早く検証
.\gradlew.bat :app:compileDebugKotlin --console=plain

# 単体テスト
.\gradlew.bat :app:testDebugUnitTest
```

> コード変更後はまず `:app:assembleDebug` を通すこと。Room の DAO クエリは
> **コンパイル時に検証**されるため、SQL の誤りや型不整合はここで検出できる。
> 実機/エミュレータでの目視確認は `/run` スキルが使える。

## ディレクトリ構成

```
app/src/main/java/com/rioaki/mendakostudyapp/
  MendakoApp.kt              # Application。起動時に SeedData.seed と StrokeRepository.load を IO で実行
  audio/                     # AppAudioManager（効果音）
  data/
    db/AppDatabase.kt        # Room DB 定義（version=4、Migration もここ）
    db/dao/                  # DAO 群
    db/entity/               # Entity 群（8テーブル）
    model/                   # MendakoCatalog（個体定義）, SubjectType
    seed/SeedData.kt         # 初期データ投入（冪等）
    stroke/                  # ひらがな書き順データとリポジトリ
  ui/
    MainActivity.kt          # NavHost を持つ唯一の Activity
    home/                    # ホーム画面
    lesson/                  # addition / subtraction / hiragana / selection / result
    mendako/                 # メンダコ描画と育成系（下記参照）
    shop/                    # ショップ（ポイント交換）
    admin/                   # 隠し管理画面（書き順登録など）
  util/                      # AdaptiveDifficulty, PointCalculator, QuestionGenerator,
                             # StrokeOrderJudge, StrokeResampler
app/src/main/res/
  drawable/                  # ベクター画像（item_*.xml, mendako_* など）
  layout/                    # 各画面の XML
  navigation/nav_graph.xml   # 画面遷移定義
doc/                         # 日本語の設計ドキュメント
```

各 UI 機能は `Fragment` + `ViewModel`（+ 必要なら `Adapter`）の組で構成される。
ViewModel は `AndroidViewModel` で、`AppDatabase.getInstance(application)` から DAO を取得し
LiveData を `map` / `switchMap` で UI 向けに加工する。

## データベース

- DB 名 `mendako_db`、現行 **version 4**。`AppDatabase` は `getInstance()` のダブルチェックロックなシングルトン。
- Entity（8テーブル）: `UserState`, `LessonStats`, `PointHistory`, `ShopItem`,
  `OwnedItem`, `FurniturePlacement`, `HiraganaQuestion`, `MendakoCharacterState`
- マイグレーションは `AppDatabase` 内に `MIGRATION_x_y` として定義し `addMigrations(...)` で登録済み（v1→v4）。
  **スキーマ（列・テーブル・主キー）を変更したら version を上げて Migration を必ず追加すること。**
- `exportSchema = false`。

### 初期データ（SeedData）

`MendakoApp.onCreate` → `SeedData.seed(db)` が毎回起動時に走る。
`shopItemDao().upsertAll(...)`（`@Upsert`）は同一 id の既存行を**最新定義で上書き**する
冪等な自己修復。旧バージョンで古い `imageResName` 等がシードされた端末でも起動時に正しい定義へ揃う。
**ショップアイテムの追加・定義変更（名前/値段/画像名）はスキーマ変更ではないので DB version を
上げる必要はなく、既存ユーザーの DB にも次回起動時に反映される。**

## メンダコ描画・育成系（ui/mendako）

- `MendakoCatalog`（data/model）: メンダコの個体（デフォルト + 友達）の定義。`imageResName` を
  `getIdentifier` で解決し、未追加なら `placeholderColor` の色フィルタで代用。
- `MendakoRenderer`: 本体描画（`applyBody`）とアクセサリーの重ね描画（`applyAccessories`）を集約。
  ホーム / 部屋 / ごはん / アクセサリー画面で共通利用。
  アクセサリーは固定 ImageView ではなく、**装備状態に応じてコンテナ（`mendako_container`）へ
  ImageView を動的生成/削除**する方式。位置・サイズの既定値は `AccessoryCatalog` を参照。
- `AccessoryCatalog`: アクセサリー（`id` は `ShopItem.id` と一致）ごとの画像名・サイズ(dp)・配置(gravity/余白)。
- `FurnitureRoomRenderer`: 部屋に置いた家具を比率座標で配置・描画。
- `MendakoAnimator` / `MendakoState`: 表情・アニメーション状態。

## ショップとアイテム

- カテゴリは **`FOOD` / `ACCESSORY` / `FURNITURE`** の3種（`ShopItem.category` の文字列）。
- 各画面の絞り込みは **カテゴリ文字列ベース**（`ShopItemDao.observeByCategory` /
  `OwnedItemDao.observeByCategory`）。ID 範囲でのハードコードはしない。
- アイテム画像は `res/drawable/item_*.xml`（48×48 viewport のベクター）。
  `imageResName` を `resources.getIdentifier(name, "drawable", packageName)` で解決して表示する。

### アイテムを追加する手順

1. `res/drawable/item_xxx.xml` を作成（既存の `item_*.xml` のスタイルに合わせる）。
2. `SeedData.kt` の `seedShopItems` に `ShopItem(id, 名前, カテゴリ, 値段, "item_xxx")` を1行追加。
3. **アクセサリーの場合のみ**、`AccessoryCatalog.all` に `AccessoryDef(...)`（サイズ・配置）を追加。
   → これでホーム等で本体に重ねて表示・ドラッグ移動できるようになる。レイアウト XML や
   `MendakoRenderer` の変更は不要。

## 教材（ui/lesson）

- 足し算（20以下）・引き算（10以下）・ひらがな書き取りの3種。`SubjectType` で区別。
- `QuestionGenerator` が問題生成、`AdaptiveDifficulty` が正答率に応じて難度を調整。
- ひらがなは ML Kit の手書き認識 + 書き順判定（`StrokeOrderJudge` / `StrokeResampler`、
  `HiraganaCanvasView`）。問題文は管理画面で登録、書き順データは `data/stroke` + `assets`。
- 受講結果でポイント付与（`PointCalculator`、`PointHistory` に履歴）。

## 管理画面（ui/admin）

ホーム画面のバージョン表示を **5回タップ**すると遷移する隠し画面。
ひらがな問題の管理や書き順キャプチャ（`StrokeCaptureFragment`）に使う。

## コーディング規約・慣習

- **コメント・ドキュメントは日本語**で書かれている。既存スタイルに合わせること。
- ViewBinding を使用（`_binding` を `onDestroyView` で null 解除する既存パターンに従う）。
- ViewModel から DB へは coroutine（`viewModelScope.launch`）で書き込み、読み取りは LiveData。
- 文字列・色などのリソースは `res/values` を利用。子ども向けに明るくポップな配色。
- ユーザーは日本語で対話する。応答も日本語で。
