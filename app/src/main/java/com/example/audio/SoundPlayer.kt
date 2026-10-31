package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object SoundPlayer {

    /**
     * Synthesizes and plays a 1.8-second original, cinematic KRAS TV intro chime.
     * Respects device silent/vibration mode and user settings.
     */
    suspend fun playCinematicIntro(context: Context, enabled: Boolean) {
        if (!enabled) return

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        if (audioManager != null) {
            val ringerMode = audioManager.ringerMode
            if (ringerMode == AudioManager.RINGER_MODE_SILENT || ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
                return
            }
        }

        withContext(Dispatchers.Default) {
            try {
                val sampleRate = 44100
                val durationSec = 1.8f
                val totalSamples = (sampleRate * durationSec).toInt()
                val buffer = ShortArray(totalSamples)

                // Harmonics for a deep, luxury cinematic chime (F# minor resonant open chord + crystalline shimmer)
                // Root: 92.5 Hz (warm sub), 185 Hz, 277 Hz (C#), 440 Hz (A shimmer), 880 Hz (crystal overtone)
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    
                    // Attack envelope (quick 30ms rise), then smooth exponential decay
                    val attack = if (t < 0.04) (t / 0.04) else 1.0
                    val decay = exp(-t * 2.2)
                    val envelope = attack * decay

                    // Harmonic synthesis
                    val sub = 0.40 * sin(2.0 * PI * 92.5 * t)
                    val mid = 0.30 * sin(2.0 * PI * 185.0 * t)
                    val fifth = 0.20 * sin(2.0 * PI * 277.18 * t)
                    val chime = 0.15 * sin(2.0 * PI * 440.0 * t) * exp(-t * 1.5)
                    val crystal = 0.08 * sin(2.0 * PI * 880.0 * t) * exp(-t * 3.0)

                    val sampleValue = ((sub + mid + fifth + chime + crystal) * envelope * 0.75).coerceIn(-1.0, 1.0)
                    buffer[i] = (sampleValue * Short.MAX_VALUE).toInt().toShort()
                }

                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                val audioFormat = AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()

                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val track = AudioTrack.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(maxOf(minBufferSize, buffer.size * 2))
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()

                // Release track after playback completes
                kotlinx.coroutines.delay(2000L)
                track.stop()
                track.release()
            } catch (_: Exception) {
                // Non-fatal, gracefully handle audio playback exceptions
            }
        }
    }
}
