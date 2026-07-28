import re

with open('app/src/main/java/com/example/audio/SoundManager.kt', 'r') as f:
    content = f.read()

# Add BuildConfig import and Toast
content = content.replace("import android.util.Log", "import android.util.Log\nimport android.widget.Toast\nimport com.example.BuildConfig")

init_old = """        try {
            bgmPlayer = MediaPlayer.create(context, R.raw.background_music)?.apply {
                isLooping = true
                setVolume(0.25f, 0.25f)
            }
            laserPlayer = MediaPlayer.create(context, R.raw.laser_beam)?.apply {
                isLooping = true
                setVolume(0.65f, 0.65f)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }"""

init_new = """        try {
            bgmPlayer = MediaPlayer.create(context.applicationContext, R.raw.background_music)?.apply {
                isLooping = true
                setVolume(0.25f, 0.25f)
            }
            laserPlayer = MediaPlayer.create(context.applicationContext, R.raw.laser_beam)?.apply {
                isLooping = true
                setVolume(0.65f, 0.65f)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }"""
content = content.replace(init_old, init_new)

# Add updatePlayback logs
update_old = """    private fun updatePlayback() {
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
    }"""

update_new = """    private fun updatePlayback() {
        Log.d("AudioDiagnostic", "updatePlayback: soundEnabled=$isSoundEnabled, bgmPlaying=$isBgmPlaying, adActive=$isAdActive, inBackground=$isInBackground, laserActive=$isLaserActive")
        if (bgmPlayer != null) {
            if (isSoundEnabled && isBgmPlaying && !isAdActive && !isInBackground) {
                if (!bgmPlayer!!.isPlaying) {
                    bgmPlayer!!.start()
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(context, "AUDIO OK — BGM PLAYING", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                if (bgmPlayer!!.isPlaying) {
                    bgmPlayer!!.pause()
                }
                if (isBgmPlaying && BuildConfig.DEBUG) {
                    val msg = "AUDIO BLOCKED — sound=$isSoundEnabled, foreground=${!isInBackground}, ad=$isAdActive, requested=$isBgmPlaying"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        } else if (isBgmPlaying && BuildConfig.DEBUG) {
             Toast.makeText(context, "AUDIO ERROR — PLAYER NULL", Toast.LENGTH_SHORT).show()
        }

        if (laserPlayer != null) {
            if (isSoundEnabled && isLaserActive && !isAdActive && !isInBackground) {
                if (!laserPlayer!!.isPlaying) {
                    laserPlayer!!.start()
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(context, "LASER AUDIO STARTED", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                if (laserPlayer!!.isPlaying) {
                    laserPlayer!!.pause()
                    laserPlayer!!.seekTo(0)
                }
            }
        }
    }"""
content = content.replace(update_old, update_new)

with open('app/src/main/java/com/example/audio/SoundManager.kt', 'w') as f:
    f.write(content)
