package com.example.game.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

enum class FootstepTerrain {
    GRASS_DIRT,
    STONE,
    CAVERN,
    WOOD,
    ICE_SNOW,
    WATER
}

open class FantasySoundEngine {
    protected val sampleRate = 22050
    private val executor = Executors.newFixedThreadPool(2)
    var isMuted = false

    // Looping background ambient track
    private var ambientTrack: AudioTrack? = null
    var currentAmbienceArea: String? = null
        private set

    protected fun playPcm(samples: ShortArray) {
        if (isMuted) return
        executor.execute {
            try {
                val bufferSize = samples.size * 2
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
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
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(samples, 0, samples.size)
                track.play()

                // Release track after playback
                val durationMs = (samples.size * 1000L) / sampleRate
                Thread.sleep(durationMs + 50)
                track.stop()
                track.release()
            } catch (_: Exception) {
                // Ignore audio playback issues
            }
        }
    }

    // ==========================================
    // 1. SWORD SWINGS
    // ==========================================

    fun playSwordSlash() {
        playSwordSwing(1)
    }

    fun playSwordSwing(comboStep: Int = 1) {
        val duration = if (comboStep == 2) 0.18f else 0.13f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        var lastNoise = 0f

        val startFreq = when (comboStep) {
            2 -> 380f // Heavy upward cleave
            3 -> 550f // Air spin slash
            else -> 480f // Standard downward slash
        }
        val endFreq = when (comboStep) {
            2 -> 110f
            3 -> 220f
            else -> 160f
        }

        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / duration
            val env = sin(progress * PI.toFloat())

            // Pitch swoosh
            val freq = startFreq + (endFreq - startFreq) * progress
            val tone = sin(2.0 * PI * freq * t).toFloat()

            // High velocity air displacement turbulence
            val white = (Math.random().toFloat() * 2f - 1f)
            lastNoise = lastNoise * 0.72f + white * 0.28f

            // Metallic blade edge overtone on heavy combo
            val metallic = if (comboStep == 2) sin(2.0 * PI * 920.0 * t).toFloat() * 0.22f else 0f

            val sampleVal = (tone * 0.45f + lastNoise * 0.45f + metallic) * env * 24000f
            samples[i] = sampleVal.toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(samples)
    }

    // ==========================================
    // 2. FOOTFALLS ON DIFFERENT TERRAINS
    // ==========================================

    fun playFootstep(terrain: FootstepTerrain) {
        if (isMuted) return
        when (terrain) {
            FootstepTerrain.GRASS_DIRT -> playGrassFootstep()
            FootstepTerrain.STONE -> playStoneFootstep()
            FootstepTerrain.CAVERN -> playCavernFootstep()
            FootstepTerrain.WOOD -> playWoodFootstep()
            FootstepTerrain.ICE_SNOW -> playSnowFootstep()
            FootstepTerrain.WATER -> playWaterFootstep()
        }
    }

    private fun playGrassFootstep() {
        // Soft cushioned earth thud + gentle rustle
        val duration = 0.08f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        var lastNoise = 0f
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / duration
            val env = (1f - progress) * (1f - progress)
            val white = (Math.random().toFloat() * 2f - 1f)
            lastNoise = lastNoise * 0.8f + white * 0.2f // low pass
            val thud = sin(2.0 * PI * 115.0 * t).toFloat() * 0.45f
            val sampleVal = (thud + lastNoise * 0.55f) * env * 14000f
            samples[i] = sampleVal.toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(samples)
    }

    private fun playStoneFootstep() {
        // Crisp solid stone tap with quick high-frequency transient
        val duration = 0.07f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val env = exp(-t * 55.0).toFloat()
            val click = sin(2.0 * PI * 1350.0 * t).toFloat() * 0.35f
            val body = sin(2.0 * PI * 290.0 * t).toFloat() * 0.5f
            val noise = (Math.random().toFloat() * 2f - 1f) * 0.15f
            val sampleVal = (click + body + noise) * env * 17000f
            samples[i] = sampleVal.toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(samples)
    }

    private fun playCavernFootstep() {
        // Subterranean hollow gravel echo step
        val duration = 0.11f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val env = exp(-t * 30.0).toFloat()
            val deep = sin(2.0 * PI * 98.0 * t).toFloat() * 0.6f
            val hollow = sin(2.0 * PI * 210.0 * t).toFloat() * 0.25f
            val grit = (Math.random().toFloat() * 2f - 1f) * 0.2f
            val sampleVal = (deep + hollow + grit) * env * 15000f
            samples[i] = sampleVal.toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(samples)
    }

    private fun playWoodFootstep() {
        // Hollow wooden plank knock
        val duration = 0.08f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val env = exp(-t * 46.0).toFloat()
            val fundamental = sin(2.0 * PI * 220.0 * t).toFloat() * 0.65f
            val overtone = sin(2.0 * PI * 440.0 * t).toFloat() * 0.25f
            val noise = (Math.random().toFloat() * 2f - 1f) * 0.1f
            val sampleVal = (fundamental + overtone + noise) * env * 16000f
            samples[i] = sampleVal.toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(samples)
    }

    private fun playSnowFootstep() {
        // Crunchy granular snow / ice step
        val duration = 0.10f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val env = (1f - (t / duration)).coerceAtLeast(0f)
            val crunch = (Math.random().toFloat() * 2f - 1f) * (0.6f + 0.4f * sin(2.0 * PI * 3200.0 * t).toFloat())
            val sampleVal = crunch * env * 13500f
            samples[i] = sampleVal.toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(samples)
    }

    private fun playWaterFootstep() {
        // Liquid splash slosh
        val duration = 0.11f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val env = exp(-t * 32.0).toFloat()
            val tone = sin(2.0 * PI * (320.0 - 160.0 * (t / duration)) * t).toFloat() * 0.5f
            val slosh = (Math.random().toFloat() * 2f - 1f) * 0.5f
            val sampleVal = (tone + slosh) * env * 14000f
            samples[i] = sampleVal.toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(samples)
    }

    // ==========================================
    // 3. BACKGROUND AMBIENT TRACKS (FOREST & CAVE)
    // ==========================================

    @Synchronized
    fun setAreaAmbience(areaName: String) {
        val isCave = areaName.contains("Cavern", ignoreCase = true) ||
                areaName.contains("Cave", ignoreCase = true) ||
                areaName.contains("Undercroft", ignoreCase = true) ||
                areaName.contains("Crypt", ignoreCase = true)

        val targetKey = if (isCave) "CAVE" else "FOREST"
        if (currentAmbienceArea == targetKey && ambientTrack != null) return

        if (isCave) {
            playCaveAmbience()
        } else {
            playForestAmbience()
        }
    }

    @Synchronized
    fun playForestAmbience() {
        if (currentAmbienceArea == "FOREST" && ambientTrack != null) return
        currentAmbienceArea = "FOREST"
        stopAmbienceTrackOnly()
        if (isMuted) return

        executor.execute {
            try {
                val samples = generateForestAmbienceSamples()
                startLoopingTrack(samples, "FOREST")
            } catch (_: Exception) {}
        }
    }

    @Synchronized
    fun playCaveAmbience() {
        if (currentAmbienceArea == "CAVE" && ambientTrack != null) return
        currentAmbienceArea = "CAVE"
        stopAmbienceTrackOnly()
        if (isMuted) return

        executor.execute {
            try {
                val samples = generateCaveAmbienceSamples()
                startLoopingTrack(samples, "CAVE")
            } catch (_: Exception) {}
        }
    }

    private fun startLoopingTrack(samples: ShortArray, areaKey: String) {
        synchronized(this) {
            stopAmbienceTrackOnly()
            if (isMuted || currentAmbienceArea != areaKey) return
            try {
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(samples.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(samples, 0, samples.size)
                track.setLoopPoints(0, samples.size, -1) // Infinite seamless loop
                track.play()
                ambientTrack = track
            } catch (_: Exception) {
                // Ignore audio init errors
            }
        }
    }

    @Synchronized
    fun stopAmbience() {
        stopAmbienceTrackOnly()
        currentAmbienceArea = null
    }

    private fun stopAmbienceTrackOnly() {
        try {
            ambientTrack?.let {
                if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    it.stop()
                }
                it.release()
            }
        } catch (_: Exception) {}
        ambientTrack = null
    }

    @Synchronized
    fun pauseAmbience() {
        try {
            ambientTrack?.let {
                if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    it.pause()
                }
            }
        } catch (_: Exception) {}
    }

    @Synchronized
    fun resumeAmbience() {
        if (isMuted) return
        try {
            ambientTrack?.let {
                if (it.playState == AudioTrack.PLAYSTATE_PAUSED) {
                    it.play()
                }
            }
        } catch (_: Exception) {}
    }

    private fun generateForestAmbienceSamples(): ShortArray {
        // 4.0 second loopable forest soundscape: gentle wind rustle + harmonic woodwind chimes
        val duration = 4.0f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        var lastNoise = 0f
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate

            // 1. Soft wind swell (filtered noise with smooth 4s breathing swell)
            val swell = 0.5f + 0.5f * sin(2.0 * PI * 0.25 * t).toFloat()
            val white = (Math.random().toFloat() * 2f - 1f)
            lastNoise = lastNoise * 0.85f + white * 0.15f // Low pass filter
            val wind = lastNoise * swell * 3200f

            // 2. Chimes / pastoral woodwind notes:
            // Bell 1 at t ~ 0.5s (E5 = 659.25Hz)
            val bell1Env = if (t in 0.5f..1.6f) exp(-(t - 0.5) * 3.5).toFloat() else 0f
            val bell1 = sin(2.0 * PI * 659.25 * t).toFloat() * bell1Env * 3000f

            // Bell 2 at t ~ 2.0s (A5 = 880.00Hz)
            val bell2Env = if (t in 2.0f..3.1f) exp(-(t - 2.0) * 3.5).toFloat() else 0f
            val bell2 = sin(2.0 * PI * 880.00 * t).toFloat() * bell2Env * 2800f

            // Bell 3 at t ~ 3.2s (C6 = 1046.5Hz)
            val bell3Env = if (t in 3.2f..4.0f) exp(-(t - 3.2) * 4.0).toFloat() else 0f
            val bell3 = sin(2.0 * PI * 1046.50 * t).toFloat() * bell3Env * 2400f

            val mixed = wind + bell1 + bell2 + bell3
            samples[i] = mixed.toInt().coerceIn(-32767, 32767).toShort()
        }
        return samples
    }

    private fun generateCaveAmbienceSamples(): ShortArray {
        // 4.0 second loopable cave soundscape: deep subterranean drone + mystical water droplets
        val duration = 4.0f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate

            // 1. Subterranean deep drone: 55Hz (A1) + 110Hz + subtle 1.5Hz beat
            val beat = 0.8f + 0.2f * sin(2.0 * PI * 0.5 * t).toFloat()
            val drone1 = sin(2.0 * PI * 55.0 * t).toFloat() * 4500f
            val drone2 = sin(2.0 * PI * 110.0 * t).toFloat() * 2200f
            val drone = (drone1 + drone2) * beat

            // 2. Cavern water droplets:
            // Drop 1 at t ~ 1.2s (1568Hz dropping to 1480Hz)
            val drop1Env = if (t in 1.2f..1.5f) exp(-(t - 1.2) * 12.0).toFloat() else 0f
            val drop1Freq = 1568f - (t - 1.2f).coerceAtLeast(0f) * 300f
            val drop1 = sin(2.0 * PI * drop1Freq * t).toFloat() * drop1Env * 3800f

            // Drop 2 at t ~ 2.8s (1864Hz)
            val drop2Env = if (t in 2.8f..3.1f) exp(-(t - 2.8) * 14.0).toFloat() else 0f
            val drop2Freq = 1864f - (t - 2.8f).coerceAtLeast(0f) * 350f
            val drop2 = sin(2.0 * PI * drop2Freq * t).toFloat() * drop2Env * 3400f

            val mixed = drone + drop1 + drop2
            samples[i] = mixed.toInt().coerceIn(-32767, 32767).toShort()
        }
        return samples
    }

    open fun release() {
        stopAmbience()
        executor.shutdown()
    }

    fun playJump() {
        // Upward pitch bend "boing"
        val duration = 0.12f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / duration
            val freq = 220f + progress * 480f
            val envelope = 1f - progress
            val tone = sin(2.0 * PI * freq * t).toFloat()
            val sampleVal = tone * envelope * 20000f
            samples[i] = sampleVal.toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(samples)
    }

    fun playCoin() {
        // High-pitched two-tone chime (B5 to E6)
        val duration = 0.16f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        val split = numSamples / 2
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val freq = if (i < split) 987.77f else 1318.51f
            val env = exp(-t * 14.0).toFloat()
            val tone = sin(2.0 * PI * freq * t).toFloat()
            val sampleVal = tone * env * 22000f
            samples[i] = sampleVal.toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(samples)
    }

    fun playCrystal() {
        // Shimmering harmonic arpeggio
        val duration = 0.28f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / duration
            val freq = when {
                progress < 0.25f -> 1046.50f // C6
                progress < 0.50f -> 1318.51f // E6
                progress < 0.75f -> 1567.98f // G6
                else -> 2093.00f             // C7
            }
            val env = exp(-progress * 5.0).toFloat()
            val tone = sin(2.0 * PI * freq * t).toFloat()
            val sampleVal = tone * env * 22000f
            samples[i] = sampleVal.toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(samples)
    }

    fun playHit() {
        // Punchy impact crunch
        val duration = 0.12f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / duration
            val freq = 160f - progress * 100f
            val env = (1f - progress)
            val tone = sin(2.0 * PI * freq * t).toFloat()
            val noise = (Math.random().toFloat() * 2f - 1f) * 0.6f
            val sampleVal = (tone * 0.5f + noise) * env * 28000f
            samples[i] = sampleVal.toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(samples)
    }

    fun playEnemyDeath() {
        // Poof sound with crumbling noise
        val duration = 0.2f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / duration
            val env = (1f - progress) * (1f - progress)
            val noise = (Math.random().toFloat() * 2f - 1f)
            val sampleVal = noise * env * 22000f
            samples[i] = sampleVal.toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(samples)
    }

    fun playCheckpoint() {
        // Radiant fantasy chord
        val duration = 0.45f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val env = (1f - t / duration)
            val f1 = sin(2.0 * PI * 523.25 * t).toFloat() // C5
            val f2 = sin(2.0 * PI * 659.25 * t).toFloat() // E5
            val f3 = sin(2.0 * PI * 783.99 * t).toFloat() // G5
            val f4 = sin(2.0 * PI * 1046.50 * t).toFloat() // C6
            val sampleVal = ((f1 + f2 + f3 + f4) / 4f) * env * 24000f
            samples[i] = sampleVal.toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(samples)
    }

    fun playChestOpen() {
        // Fanfare sweep
        val duration = 0.35f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / duration
            val freq = 440f + progress * 880f
            val env = 1f - progress * 0.5f
            val tone = sin(2.0 * PI * freq * t).toFloat()
            val sampleVal = tone * env * 22000f
            samples[i] = sampleVal.toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(samples)
    }

    fun playVictory() {
        // Victory fanfare sequence
        val duration = 0.8f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / duration
            val freq = when {
                progress < 0.2f -> 523.25f // C5
                progress < 0.4f -> 659.25f // E5
                progress < 0.6f -> 783.99f // G5
                else -> 1046.50f           // C6
            }
            val env = exp(-((progress % 0.2f) * 8.0)).toFloat()
            val tone = sin(2.0 * PI * freq * t).toFloat()
            val sampleVal = tone * env * 24000f
            samples[i] = sampleVal.toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(samples)
    }

    fun playSwordSwing() {
        playSwordSlash()
    }

    fun playMenuClick() {
        // Crisp click
        val duration = 0.05f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / duration
            val freq = 800f
            val env = 1f - progress
            val tone = sin(2.0 * PI * freq * t).toFloat()
            samples[i] = (tone * env * 18000f).toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(samples)
    }

    fun playLevelUp() {
        // High harmonic triumph chime
        val duration = 0.5f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / duration
            val freq = when {
                progress < 0.33f -> 587.33f // D5
                progress < 0.66f -> 739.99f // F#5
                else -> 880.00f             // A5
            }
            val env = exp(-((progress % 0.33f) * 6.0)).toFloat()
            val tone = sin(2.0 * PI * freq * t).toFloat()
            samples[i] = (tone * env * 24000f).toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(samples)
    }

    fun playGameOver() {
        // Descending low defeat notes
        val duration = 0.6f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / duration
            val freq = when {
                progress < 0.33f -> 329.63f // E4
                progress < 0.66f -> 293.66f // D4
                else -> 220.00f            // A3
            }
            val env = (1f - progress)
            val tone = sin(2.0 * PI * freq * t).toFloat()
            val sampleVal = tone * env * 24000f
            samples[i] = sampleVal.toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(samples)
    }

    fun playPoisonTick() {
        // Toxic squish & bubbling acid sizzle
        val duration = 0.12f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / duration
            val freq = 310f - progress * 140f + sin(2.0 * PI * 42.0 * t).toFloat() * 60f
            val env = exp(-progress * 5.0).toFloat()
            val tone = sin(2.0 * PI * freq * t).toFloat()
            val noise = (Math.random().toFloat() * 2f - 1f) * 0.25f
            samples[i] = ((tone * 0.6f + noise) * env * 17000f).toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(samples)
    }

    fun playBurnTick() {
        // Fiery crackle & pop
        val duration = 0.14f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / duration
            val env = exp(-progress * 7.0).toFloat()
            val crackle = if (Math.random() < 0.15) (Math.random().toFloat() * 2f - 1f) * 0.8f else 0f
            val flameTone = sin(2.0 * PI * (440.0 - progress * 180.0) * t).toFloat() * 0.4f
            samples[i] = ((flameTone + crackle) * env * 19000f).toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(samples)
    }

    fun playExtinguish() {
        // Sizzling steam quench
        val duration = 0.22f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        var lastNoise = 0f
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / duration
            val env = exp(-progress * 4.2).toFloat()
            val white = (Math.random().toFloat() * 2f - 1f)
            lastNoise = lastNoise * 0.6f + white * 0.4f // high-frequency hiss
            val tone = sin(2.0 * PI * (920.0 - progress * 600.0) * t).toFloat() * 0.25f
            samples[i] = ((lastNoise * 0.75f + tone) * env * 18000f).toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(samples)
    }

    fun playSpeedBuff() {
        // Ascending agile wind gust & crystal resonance
        val duration = 0.32f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / duration
            val freq = 440f + progress * 660f
            val env = sin(progress * PI.toFloat())
            val tone1 = sin(2.0 * PI * freq * t).toFloat() * 0.6f
            val tone2 = sin(2.0 * PI * (freq * 1.5f) * t).toFloat() * 0.3f
            val wind = (Math.random().toFloat() * 2f - 1f) * 0.2f
            samples[i] = ((tone1 + tone2 + wind) * env * 22000f).toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(samples)
    }

    fun playHealTick() {
        // Gentle warm harmonic chime
        val duration = 0.25f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / duration
            val freq = 659.25f + progress * 220f // E5 -> A5
            val env = exp(-progress * 4.0).toFloat()
            val tone = sin(2.0 * PI * freq * t).toFloat()
            samples[i] = (tone * env * 20000f).toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(samples)
    }
}
