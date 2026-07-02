package com.mario.gtavi

import android.media.MediaPlayer
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.mario.gtavi.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide ActionBar if it exists
        supportActionBar?.hide()

        playIntroVideo()
    }

    private fun playIntroVideo() {
        val videoView = binding.videoView
        videoView.visibility = View.VISIBLE
        
        // Make it full screen
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        try {
            val assetFileName = "GTAVI.mp4"
            val afd = assets.openFd(assetFileName)
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()

            videoView.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    mediaPlayer?.setDisplay(holder)
                    mediaPlayer?.prepare()
                    mediaPlayer?.start()
                }
                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
                override fun surfaceDestroyed(holder: SurfaceHolder) {}
            })

            mediaPlayer?.setOnCompletionListener {
                showCrashPopup()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showCrashPopup()
        }
    }

    private fun showCrashPopup() {
        val errors = listOf(
            "ERR_GFX_D3D_INIT: Failed to initialize graphics driver.",
            "OUT_OF_MEMORY: System heap exhausted while allocating 4GB for texture streaming.",
            "GPU_TIMEOUT: The graphics processor has stopped responding.",
            "CRITICAL_EXCEPTION: Illegal memory access at 0x00007FF7B3C2.",
            "VRAM_OVERFLOW: Video memory limit exceeded. 12288MB requested.",
            "THREAD_STUCK_IN_DEVICE_DRIVER: A fatal hardware error occurred."
        ).shuffled()

        val errorMessage = "GTA VI has encountered a critical error and needs to close.\n\n" +
                "Error Codes:\n" + errors.take(3).joinToString("\n") +
                "\n\nPress OK to exit and restart your device."

        AlertDialog.Builder(this)
            .setTitle("Grand Theft Auto VI - Fatal Error")
            .setMessage(errorMessage)
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                finishAffinity()
                System.exit(0)
            }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}