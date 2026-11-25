package com.rioaki.mendakostudyapp.audio

import android.content.Context
import android.media.MediaPlayer
import com.rioaki.mendakostudyapp.R

object AppAudioManager {

    enum class BgmType { MAIN, STUDY, HOME_SHOP }

    private var mediaPlayer: MediaPlayer? = null
    private var currentBgmType: BgmType? = null
    private var appContext: Context? = null

    /** BGM の通常音量。 */
    private const val BGM_VOLUME = 1.0f

    /** 効果音再生中に下げる BGM 音量（ダッキング）。効果音を相対的に聞こえやすくする。 */
    private const val BGM_DUCK_VOLUME = 0.2f

    /** 現在鳴っている効果音の数。0 になったら BGM 音量を戻す。 */
    private var activeSeCount = 0

    /** assetPath ごとの再生中カウント。`isSePlaying` での判定に使う。 */
    private val activeAssetCounts = mutableMapOf<String, Int>()

    /** 指定した assets 効果音が現在再生中（準備中含む）かどうか。 */
    fun isSePlaying(assetPath: String): Boolean = (activeAssetCounts[assetPath] ?: 0) > 0

    private fun duckBgm() {
        activeSeCount++
        mediaPlayer?.setVolume(BGM_DUCK_VOLUME, BGM_DUCK_VOLUME)
    }

    private fun restoreBgm() {
        activeSeCount = (activeSeCount - 1).coerceAtLeast(0)
        if (activeSeCount == 0) {
            mediaPlayer?.setVolume(BGM_VOLUME, BGM_VOLUME)
        }
    }

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun playBgm(context: Context, type: BgmType) {
        if (type == currentBgmType && mediaPlayer?.isPlaying == true) return
        val assetPath = when (type) {
            BgmType.MAIN -> "audio/bgm/bgm_main.mp3"
            BgmType.STUDY -> "audio/bgm/bgm_study.mp3"
            BgmType.HOME_SHOP -> "audio/bgm/bgm_home_shop.mp3"
        }
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            context.assets.openFd(assetPath).use { fd ->
                setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
            }
            isLooping = true
            prepare()
            start()
        }
        currentBgmType = type
    }

    fun playSe(context: Context, resId: Int) {
        MediaPlayer.create(context, resId)?.apply {
            setOnCompletionListener { it.release() }
            start()
        }
    }

    private fun incAssetCount(assetPath: String) {
        activeAssetCounts[assetPath] = (activeAssetCounts[assetPath] ?: 0) + 1
    }

    private fun decAssetCount(assetPath: String) {
        val next = (activeAssetCounts[assetPath] ?: 0) - 1
        if (next <= 0) activeAssetCounts.remove(assetPath) else activeAssetCounts[assetPath] = next
    }

    /** assets 配下の効果音を再生する（BGM とは独立した使い捨て MediaPlayer）。 */
    fun playSeAsset(context: Context, assetPath: String, volume: Float = 1.0f) {
        // 準備開始から再生終了まで「再生中」として扱う（isSePlaying 用）。
        // try より前に増やし、失敗時は catch で必ず戻すことで増減を対にする。
        incAssetCount(assetPath)
        try {
            val player = MediaPlayer()
            context.assets.openFd(assetPath).use { fd ->
                player.setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
            }
            player.setVolume(volume, volume)
            player.setOnCompletionListener {
                it.release()
                restoreBgm()
                decAssetCount(assetPath)
            }
            player.setOnPreparedListener {
                duckBgm()
                it.start()
            }
            player.setOnErrorListener { mp, _, _ ->
                // 再生中エラーでも BGM を確実に元の音量へ戻す
                if (mp.isPlaying) restoreBgm()
                mp.release()
                decAssetCount(assetPath)
                true
            }
            player.prepareAsync()
        } catch (e: Exception) {
            // 効果音の失敗はアプリ動作に影響させない
            decAssetCount(assetPath)
        }
    }

    /** 正解時の効果音。 */
    fun playStudyPass(context: Context) = playSeAsset(context, "audio/se/study_pass.mp3")

    /** 不正解時の効果音。 */
    fun playStudyFail(context: Context) = playSeAsset(context, "audio/se/study_fail.mp3")

    fun resumeBgm() {
        if (mediaPlayer?.isPlaying == false) mediaPlayer?.start()
    }

    fun pauseBgm() {
        mediaPlayer?.pause()
    }

    fun release() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        currentBgmType = null
    }
}
