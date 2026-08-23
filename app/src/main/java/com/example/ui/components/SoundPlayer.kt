package com.example.ui.components

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.sin

/**
 * Generates and plays a gentle, ambient calming tone (432Hz sine wave with soft harmonic envelope)
 * entirely on-device with zero network and zero asset dependency.
 */
class CalmingSoundPlayer {
    private var isPlaying = false
    private var audioTrack: AudioTrack? = null

    suspend fun startAmbientSound() = withContext(Dispatchers.Default) {
        if (isPlaying) return@withContext
        isPlaying = true

        val sampleRate = 44100
        val durationSeconds = 4
        val numSamples = sampleRate * durationSeconds
        val samples = DoubleArray(numSamples)
        val generatedSnd = ShortArray(numSamples)

        val baseFreq = 432.0 // Soothing frequency
        val overtoneFreq = 648.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            // Soft sine envelope for breathing cadence
            val envelope = 0.5 * (1.0 - kotlin.math.cos(2.0 * PI * t / durationSeconds))
            val wave = 0.7 * sin(2.0 * PI * baseFreq * t) + 0.3 * sin(2.0 * PI * overtoneFreq * t)
            samples[i] = wave * envelope * 0.25 // Gentle soft volume
            generatedSnd[i] = (samples[i] * Short.MAX_VALUE).toInt().toShort()
        }

        try {
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(maxOf(minBufSize, generatedSnd.size * 2))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack?.write(generatedSnd, 0, generatedSnd.size)
            audioTrack?.setLoopPoints(0, generatedSnd.size, -1) // Loop continuously
            audioTrack?.play()
        } catch (e: Exception) {
            // Audio generation fallback safely handled
            e.printStackTrace()
        }
    }

    fun stop() {
        isPlaying = false
        try {
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
