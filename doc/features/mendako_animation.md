# メンダコ アニメーション仕様

## 概要

メンダコキャラクターは以下の3つの動作を持つ。

- ふわふわと上下に浮く（常時）
- 正解時：笑顔になる
- 不正解時：悲しい顔になる

表情は「体」「目」「口」を別レイヤーの画像で重ねることで実現する。

---

## 必要な画像ファイル（全7枚）

### 共通仕様

| 項目 | 内容 |
|---|---|
| フォーマット | 透過PNG（.png） |
| キャンバスサイズ | 全7枚とも同じサイズ（後述） |
| 解像度 | xxhdpi：420×420px（必須）、xxxhdpi：560×560px（任意） |
| 配置先 | `res/drawable-xxhdpi/`（xxxhdpiは `res/drawable-xxxhdpi/`） |

> **重要:** 全7枚を同一キャンバスサイズで作成すること。
> 目・口の画像はパーツ以外の領域をすべて透明にする。
> レイアウト上でFrameLayoutに重ねて表示するため、位置は体に合わせてキャンバス内で正確に配置する。

---

### ファイル一覧

| ファイル名 | 説明 | 描く内容 |
|---|---|---|
| `mendako_body.png` | 体（共通） | 体全体。目・口は描かない。周囲は透明。 |
| `mendako_eyes_normal.png` | 目：通常 | 通常の目。他の領域は透明。 |
| `mendako_eyes_happy.png` | 目：正解時 | 笑い目（弧型・細目など）。他は透明。 |
| `mendako_eyes_sad.png` | 目：不正解時 | 悲しい目（下がり眉・涙など）。他は透明。 |
| `mendako_mouth_normal.png` | 口：通常 | 通常の口（小さめ）。他は透明。 |
| `mendako_mouth_happy.png` | 口：正解時 | 笑った口（大きな弧）。他は透明。 |
| `mendako_mouth_sad.png` | 口：不正解時 | 悲しい口（への字）。他は透明。 |

---

## 表情の組み合わせ

| 状態 | 目 | 口 |
|---|---|---|
| 通常（待機中） | `eyes_normal` | `mouth_normal` |
| 正解 | `eyes_happy` | `mouth_happy` |
| 不正解 | `eyes_sad` | `mouth_sad` |

---

## レイアウト構成（実装参考）

```xml
<FrameLayout
    android:id="@+id/mendako_container"
    android:layout_width="140dp"
    android:layout_height="140dp">

    <!-- 体（常時表示） -->
    <ImageView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:src="@drawable/mendako_body" />

    <!-- 目（表情に応じて切り替え） -->
    <ImageView
        android:id="@+id/iv_mendako_eyes"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:src="@drawable/mendako_eyes_normal" />

    <!-- 口（表情に応じて切り替え） -->
    <ImageView
        android:id="@+id/iv_mendako_mouth"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:src="@drawable/mendako_mouth_normal" />

</FrameLayout>
```

ふわふわアニメーションは `mendako_container`（FrameLayout）に対してかける。
これにより体・目・口が一体となって動く。

---

## アニメーション仕様

### ふわふわ（常時）

- 対象：`mendako_container`
- プロパティ：`translationY`
- 値：-20f → +20f（ピクセル）
- 時間：2000ms
- 繰り返し：無限、リバース
- イージング：AccelerateDecelerateInterpolator

### 表情切り替えタイミング

- 正解・不正解の判定直後に表情を切り替える
- 1500ms後に通常表情へ戻す
- ふわふわアニメーションは表情切り替え中も継続する
