package com.example.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.Log
import com.example.model.AcousticStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max
import kotlin.math.sqrt

private const val TAG = "StudioAudioRecorder"

enum class RecordingChannel {
    NONE,
    STUDIO,
    QUICK_TWISTER,
    CANDLE_MONITOR
}

class StudioAudioRecorder(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private var recordJob: Job? = null
    private var timerJob: Job? = null
    private var playJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null

    private val _activeChannel = MutableStateFlow(RecordingChannel.NONE)
    val activeChannel: StateFlow<RecordingChannel> = _activeChannel.asStateFlow()

    private val _liveRms = MutableStateFlow(0f)
    val liveRms: StateFlow<Float> = _liveRms.asStateFlow()

    private val _waveformSamples = MutableStateFlow<List<Float>>(emptyList())
    val waveformSamples: StateFlow<List<Float>> = _waveformSamples.asStateFlow()

    private val _studioElapsedSec = MutableStateFlow(0)
    val studioElapsedSec: StateFlow<Int> = _studioElapsedSec.asStateFlow()

    private val _quickElapsedSec = MutableStateFlow(0)
    val quickElapsedSec: StateFlow<Int> = _quickElapsedSec.asStateFlow()

    private val _studioWavBytes = MutableStateFlow<ByteArray?>(null)
    val studioWavBytes: StateFlow<ByteArray?> = _studioWavBytes.asStateFlow()

    private val _studioAcousticStats = MutableStateFlow<AcousticStats?>(null)
    val studioAcousticStats: StateFlow<AcousticStats?> = _studioAcousticStats.asStateFlow()

    private val _hasQuickRecording = MutableStateFlow(false)
    val hasQuickRecording: StateFlow<Boolean> = _hasQuickRecording.asStateFlow()

    private val _playingTarget = MutableStateFlow<RecordingChannel>(RecordingChannel.NONE)
    val playingTarget: StateFlow<RecordingChannel> = _playingTarget.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    private val studioFile: File
        get() = File(context.cacheDir, "diktor_studio_take.wav")

    private val quickFile: File
        get() = File(context.cacheDir, "diktor_quick_twister.wav")

    @SuppressLint("MissingPermission")
    fun startRecording(channel: RecordingChannel, onError: (String) -> Unit) {
        stopAllCapture()
        stopPlayback()

        val minBuf = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        if (minBuf == AudioRecord.ERROR || minBuf == AudioRecord.ERROR_BAD_VALUE) {
            onError("Аудиовход устройства недоступен")
            return
        }

        val bufferSize = max(minBuf * 2, 4096)
        val recorder = try {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )
        } catch (e: SecurityException) {
            onError("Необходимо разрешение на доступ к микрофону")
            return
        } catch (e: Exception) {
            onError("Не удалось инициализировать микрофон: ${e.localizedMessage}")
            return
        }

        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            recorder.release()
            onError("Микрофон не готов к записи")
            return
        }

        _activeChannel.value = channel
        if (channel == RecordingChannel.STUDIO) {
            _studioElapsedSec.value = 0
            _waveformSamples.value = emptyList()
            _studioWavBytes.value = null
            _studioAcousticStats.value = null
        } else if (channel == RecordingChannel.QUICK_TWISTER) {
            _quickElapsedSec.value = 0
            _hasQuickRecording.value = false
        }

        val startTimeMs = System.currentTimeMillis()
        if (channel == RecordingChannel.STUDIO || channel == RecordingChannel.QUICK_TWISTER) {
            timerJob = scope.launch {
                while (isActive && _activeChannel.value == channel) {
                    val elapsed = ((System.currentTimeMillis() - startTimeMs) / 1000L).toInt()
                    if (channel == RecordingChannel.STUDIO) {
                        _studioElapsedSec.value = elapsed
                    } else {
                        _quickElapsedSec.value = elapsed
                    }
                    delay(200L)
                }
            }
        }

        recordJob = scope.launch {
            val pcmStream = ByteArrayOutputStream()
            val rmsHistory = mutableListOf<Double>()
            val waveQueue = ArrayDeque<Float>(64)
            val shortBuf = ShortArray(1024)

            try {
                recorder.startRecording()
                while (isActive && _activeChannel.value == channel) {
                    val read = recorder.read(shortBuf, 0, shortBuf.size)
                    if (read > 0) {
                        var sumSq = 0.0
                        for (i in 0 until read) {
                            val norm = shortBuf[i].toDouble() / 32768.0
                            sumSq += norm * norm
                        }
                        val rms = sqrt(sumSq / read)
                        _liveRms.value = rms.toFloat()

                        if (channel != RecordingChannel.CANDLE_MONITOR) {
                            rmsHistory.add(rms)
                            val byteBuf = ByteBuffer.allocate(read * 2).order(ByteOrder.LITTLE_ENDIAN)
                            for (i in 0 until read) {
                                byteBuf.putShort(shortBuf[i])
                            }
                            // Cap buffer around 90s (~2.88 MB PCM) to protect memory & Gemini payload size
                            if (pcmStream.size() < 2_880_000) {
                                pcmStream.write(byteBuf.array())
                            }
                        }

                        if (channel == RecordingChannel.STUDIO) {
                            if (waveQueue.size >= 48) waveQueue.removeFirst()
                            waveQueue.addLast((rms.toFloat() * 4.2f).coerceIn(0.04f, 1.0f))
                            _waveformSamples.value = waveQueue.toList()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Audio capture error", e)
            } finally {
                try {
                    if (recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                        recorder.stop()
                    }
                } catch (_: Throwable) {}
                try {
                    recorder.release()
                } catch (_: Throwable) {}

                val pcmBytes = pcmStream.toByteArray()
                if (channel == RecordingChannel.STUDIO && pcmBytes.isNotEmpty()) {
                    val wav = wrapPcmAsWav(pcmBytes, sampleRate, 1, 16)
                    saveWavToFile(studioFile, wav)
                    _studioWavBytes.value = wav

                    val durationSec = max(1.0, (System.currentTimeMillis() - startTimeMs) / 1000.0)
                    val avgRms = if (rmsHistory.isNotEmpty()) rmsHistory.average() else 0.08
                    val maxRms = rmsHistory.maxOrNull() ?: 0.2
                    val silentFrames = rmsHistory.count { it < 0.015 }
                    val pauseRatio = if (rmsHistory.isNotEmpty()) silentFrames.toDouble() / rmsHistory.size else 0.2
                    val tailCount = max(5, (rmsHistory.size * 0.2).toInt())
                    val tailSlice = if (rmsHistory.size > tailCount) rmsHistory.takeLast(tailCount) else rmsHistory
                    val tailRms = if (tailSlice.isNotEmpty()) tailSlice.average() else avgRms

                    _studioAcousticStats.value = AcousticStats(
                        durationSec = durationSec,
                        avgRms = avgRms,
                        maxRms = maxRms,
                        pauseRatio = pauseRatio,
                        tailRms = tailRms
                    )
                } else if (channel == RecordingChannel.QUICK_TWISTER && pcmBytes.isNotEmpty()) {
                    val wav = wrapPcmAsWav(pcmBytes, sampleRate, 1, 16)
                    saveWavToFile(quickFile, wav)
                    _hasQuickRecording.value = true
                }
            }
        }
    }

    fun stopAllCapture() {
        _activeChannel.value = RecordingChannel.NONE
        _liveRms.value = 0f
        timerJob?.cancel()
        timerJob = null
        recordJob?.cancel()
        recordJob = null
    }

    fun togglePlayback(target: RecordingChannel, onError: (String) -> Unit) {
        if (_playingTarget.value == target) {
            stopPlayback()
            return
        }
        val file = when (target) {
            RecordingChannel.STUDIO -> studioFile
            RecordingChannel.QUICK_TWISTER -> quickFile
            else -> return
        }
        if (!file.exists() || file.length() <= 44L) {
            onError("Сначала запишите дубль")
            return
        }
        stopPlayback()
        try {
            val mp = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                setOnCompletionListener {
                    stopPlayback()
                }
                start()
            }
            mediaPlayer = mp
            _playingTarget.value = target
            _playbackProgress.value = 0f

            playJob = scope.launch {
                while (isActive && _playingTarget.value == target) {
                    val player = mediaPlayer ?: break
                    try {
                        if (player.isPlaying && player.duration > 0) {
                            _playbackProgress.value = (player.currentPosition.toFloat() / player.duration.toFloat()).coerceIn(0f, 1f)
                        }
                    } catch (_: Throwable) {
                        break
                    }
                    delay(100L)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Playback error", e)
            onError("Ошибка воспроизведения записи")
            stopPlayback()
        }
    }

    fun stopPlayback() {
        playJob?.cancel()
        playJob = null
        try {
            mediaPlayer?.stop()
        } catch (_: Throwable) {}
        try {
            mediaPlayer?.release()
        } catch (_: Throwable) {}
        mediaPlayer = null
        _playingTarget.value = RecordingChannel.NONE
        _playbackProgress.value = 0f
    }

    fun release() {
        stopAllCapture()
        stopPlayback()
    }

    private fun saveWavToFile(file: File, wavBytes: ByteArray) {
        try {
            FileOutputStream(file).use { it.write(wavBytes) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed saving wav file", e)
        }
    }

    private fun wrapPcmAsWav(pcmData: ByteArray, sampleRate: Int, channels: Int, bitsPerSample: Int): ByteArray {
        val totalDataLen = pcmData.size + 36
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = channels * bitsPerSample / 8
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray(Charsets.US_ASCII))
            putInt(totalDataLen)
            put("WAVE".toByteArray(Charsets.US_ASCII))
            put("fmt ".toByteArray(Charsets.US_ASCII))
            putInt(16)
            putShort(1)
            putShort(channels.toShort())
            putInt(sampleRate)
            putInt(byteRate)
            putShort(blockAlign.toShort())
            putShort(bitsPerSample.toShort())
            put("data".toByteArray(Charsets.US_ASCII))
            putInt(pcmData.size)
        }.array()
        return header + pcmData
    }
}
