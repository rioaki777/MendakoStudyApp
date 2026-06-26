package com.rioaki.mendakostudyapp.ui.admin

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.rioaki.mendakostudyapp.data.stroke.StrokeRepository
import com.rioaki.mendakostudyapp.databinding.FragmentStrokeCaptureBinding
import java.io.File

/**
 * 手本データ作成ツール。手本文字の上を正しい書き順でなぞると、
 * 表示・判定と同じ座標系で keyPoints を記録し、hiragana_strokes.json を書き出す。
 * ホーム画面のバージョン5回タップ→問題管理→「手本データ作成」から開く。
 */
class StrokeCaptureFragment : Fragment() {

    private var _binding: FragmentStrokeCaptureBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StrokeCaptureViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStrokeCaptureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.canvasCapture.isCaptureMode = true
        binding.canvasCapture.onCaptureChanged = { updateStatus() }

        viewModel.currentChar.observe(viewLifecycleOwner) { ch ->
            binding.tvChar.text = ch.toString()
            // 符号(゛/゜)単体は配置位置が分かるよう、合成例(が/ぱ)を薄い手本として表示し
            // その右肩の符号部分だけをなぞって記録する。
            val guideCh = when (ch) {
                StrokeRepository.DAKUTEN -> 'が'
                StrokeRepository.HANDAKUTEN -> 'ぱ'
                else -> ch
            }
            binding.canvasCapture.setGuideChar(guideCh, null, 0)
            binding.canvasCapture.resetCaptures()
            updateStatus()
        }

        viewModel.position.observe(viewLifecycleOwner) { (pos, total) ->
            binding.tvPosition.text = "$pos / $total"
        }

        viewModel.savedStrokeCount.observe(viewLifecycleOwner) { updateStatus() }

        binding.btnPrev.setOnClickListener { viewModel.prev() }
        binding.btnNext.setOnClickListener { viewModel.next() }
        binding.btnUndo.setOnClickListener { binding.canvasCapture.undoLastCapture() }
        binding.btnClear.setOnClickListener { binding.canvasCapture.resetCaptures() }

        binding.btnSave.setOnClickListener {
            val keyPoints = binding.canvasCapture.getCapturedKeyPoints()
            if (keyPoints.isEmpty()) {
                Toast.makeText(requireContext(), "先になぞってください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.saveCurrent(keyPoints)
            Toast.makeText(requireContext(), "保存しました（${keyPoints.size}画）", Toast.LENGTH_SHORT).show()
            viewModel.next()
        }

        binding.btnExport.setOnClickListener { exportJson() }
    }

    private fun updateStatus() {
        val saved = viewModel.savedStrokeCount.value ?: 0
        val drawn = binding.canvasCapture.capturedStrokeCount()
        binding.tvStatus.text = "保存済み: ${saved}画　／　いまなぞった: ${drawn}画"
    }

    private fun exportJson() {
        val json = viewModel.exportJson()

        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("hiragana_strokes.json", json))

        // 内部ストレージ(run-as で必ず取得可)と外部ストレージの両方に保存
        val internal = File(requireContext().filesDir, "hiragana_strokes.json")
        runCatching { internal.writeText(json) }
        val external = requireContext().getExternalFilesDir(null)
            ?.let { File(it, "hiragana_strokes.json") }
        external?.let { runCatching { it.writeText(json) } }

        Log.i("StrokeCapture", "=== hiragana_strokes.json ===\n$json")
        Toast.makeText(
            requireContext(),
            "コピー＆保存しました\n${internal.absolutePath}",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
