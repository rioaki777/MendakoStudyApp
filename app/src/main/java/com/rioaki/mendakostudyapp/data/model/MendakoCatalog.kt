package com.rioaki.mendakostudyapp.data.model

/**
 * メンダコ個体の静的メタ情報。
 * デフォルト(id=0)に加え、ポイントでアンロックできる友達3体を定義する。
 * 友達専用の画像は後日追加予定のため、画像が未解決の間は [placeholderColor] で
 * 本体に色フィルタをかけて区別する（[imageResName] が解決できれば実画像を使う）。
 */
data class MendakoDef(
    val id: Int,
    val name: String,
    val price: Int,
    val imageResName: String,
    val placeholderColor: Int // 0 のときは色フィルタなし
)

object MendakoCatalog {

    const val DEFAULT_ID = 0

    val all = listOf(
        MendakoDef(0, "メンダコ", 0, "mendako_body", 0),
        MendakoDef(10, "あおメンダコ", 100, "mendako_friend1", 0xFF4FA3FF.toInt()),
        MendakoDef(11, "ももメンダコ", 150, "mendako_friend2", 0xFFFF8FC7.toInt()),
        MendakoDef(12, "みどりメンダコ", 200, "mendako_friend3", 0xFF6FCF6F.toInt())
    )

    fun byId(id: Int): MendakoDef = all.firstOrNull { it.id == id } ?: all.first()
}
