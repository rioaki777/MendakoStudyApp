package com.rioaki.mendakostudyapp.ui.lesson.hiragana

import android.graphics.PointF

/**
 * ひらがな受講で実際にユーザーが書いた線を、結果画面へ受け渡すための一時保持。
 * 線データは Bundle で渡すには大きいため、直近1回分をシングルトンで保持する。
 * 座標は 0〜1 に正規化済み（描画側で任意サイズへスケールできる）。
 */
object HiraganaResultHolder {

    data class DrawnChar(val char: Char, val strokes: List<List<PointF>>)

    var lastResult: List<DrawnChar> = emptyList()
}
