import re

with open('app/src/main/java/com/example/audio/SoundManager.kt', 'r') as f:
    content = f.read()

content = content.replace("import com.example.R", "import com.example.R\nimport android.util.Log")

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
            bgmPlayer = MediaPlayer.create(context, R.raw.background_music)?.apply {
                isLooping = true
                setVolume(0.25f, 0.25f)
                Log.d("AudioDiagnostic", "Background audio resource loaded")
            }
            laserPlayer = MediaPlayer.create(context, R.raw.laser_beam)?.apply {
                isLooping = true
                setVolume(0.65f, 0.65f)
                Log.d("AudioDiagnostic", "Laser audio resource loaded")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }"""
content = content.replace(init_old, init_new)

content = content.replace("isSoundEnabled = enabled", "isSoundEnabled = enabled\n        Log.d(\"AudioDiagnostic\", \"Sound preference enabled/disabled: $enabled\")")

update_old = """    private fun updatePlayback() {
        if (bgmPlayer != null) {
            if (isSoundEnabled && isBgmPlaying && !isAdActive && !isInBackground) {
                if (!bgmPlayer!!.isPlaying) bgmPlayer!!.start()
            } else {
                if (bgmPlayer!!.isPlaying) bgmPlayer!!.pause()
            }
        }

        if (laserPlayer != null) {
            if (isSoundEnabled && isLaserActive && !isAdActive && !isInBackground) {
                if (!laserPlayer!!.isPlaying) laserPlayer!!.start()
            } else {
                if (laserPlayer!!.isPlaying) {
                    laserPlayer!!.pause()
                    laserPlayer!!.seekTo(0)
                }
            }
        }
    }"""

update_new = """    private fun updatePlayback() {
        if (bgmPlayer != null) {
            if (isSoundEnabled && isBgmPlaying && !isAdActive && !isInBackground) {
                if (!bgmPlayer!!.isPlaying) {
                    bgmPlayer!!.start()
                    Log.d("AudioDiagnostic", "Background music started")
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
                    Log.d("AudioDiagnostic", "Laser sound started")
                }
            } else {
                if (laserPlayer!!.isPlaying) {
                    laserPlayer!!.pause()
                    laserPlayer!!.seekTo(0)
                    Log.d("AudioDiagnostic", "Laser sound stopped")
                }
            }
        }
    }"""
content = content.replace(update_old, update_new)

with open('app/src/main/java/com/example/audio/SoundManager.kt', 'w') as f:
    f.write(content)
