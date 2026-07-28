package com.example.audio
import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.R
import android.util.Log
import android.widget.Toast
import com.example.BuildConfig
class SoundManager(private val context: Context) : DefaultLifecycleObserver {
    private var bgmPlayer: MediaPlayer? = null
    private var laserPlayer: MediaPlayer? = null
    private var isSoundEnabled = true
    private var isBgmPlaying = false
    private var isLaserActive = false
    private var isAdActive = false
    private var isInBackground = false
    init {
        try {
            bgmPlayer = MediaPlayer.create(context.applicationContext, R.raw.background_music)?.apply {
                isLooping = true
                setVolume(1.0f, 1.0f)
            }
            laserPlayer = MediaPlayer.create(context.applicationContext, R.raw.laser_beam)?.apply {
                isLooping = true
                setVolume(1.0f, 1.0f)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun setSoundEnabled(enabled: Boolean) {
        isSoundEnabled = enabled
        updatePlayback()
    }
    fun startBgm() {
        isBgmPlaying = true
        updatePlayback()
    }
    fun stopBgm() {
        isBgmPlaying = false
        updatePlayback()
    }
    fun setLaserActive(active: Boolean) {
        isLaserActive = active
        if (!active) {
            laserPlayer?.pause()
            laserPlayer?.seekTo(0)
        }
        updatePlayback()
    }
    fun setAdActive(active: Boolean) {
        isAdActive = active
        updatePlayback()
    }
    private fun updatePlayback() {
        if (bgmPlayer != null) {
            if (isSoundEnabled && isBgmPlaying && !isAdActive && !isInBackground) {
                if (!bgmPlayer!!.isPlaying) {
                    bgmPlayer!!.start()
                }
            } else {
                if (bgmPlayer!!.isPlaying) {
                    bgmPlayer!!.pause()
                }
            }
        }
        if (laserPlayer != null) {
            if (isSoundEnabled && isLaserActive && !isAdActive && !isInBackground) {
                if (!laserPlayer!!.isPlaying) {
                    laserPlayer!!.start()
                }
            } else {
                if (laserPlayer!!.isPlaying) {
                    laserPlayer!!.pause()
                    laserPlayer!!.seekTo(0)
                }
            }
        }
    }
    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        isInBackground = true
        updatePlayback()
    }
    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        isInBackground = false
        updatePlayback()
    }
    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        bgmPlayer?.release()
        bgmPlayer = null
        laserPlayer?.release()
        laserPlayer = null
    }
}
