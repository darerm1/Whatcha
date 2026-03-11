package com.darerm1.whatcha.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.darerm1.whatcha.R
import com.darerm1.whatcha.databinding.ActivitySplashBinding
import com.darerm1.whatcha.ui.views.SkySceneView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Whatcha)
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGreeting()
        animateContent()

        lifecycleScope.launch {
            delay(SPLASH_DURATION_MS)
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
    }

    private fun setupGreeting() {
        val hour = LocalTime.now().hour
        val (title, subtitle, phase) = when (hour) {
            in 5..11 -> Triple(MORNING_TITLE, MORNING_SUBTITLE, SkySceneView.Phase.MORNING)
            in 12..17 -> Triple(DAY_TITLE, DAY_SUBTITLE, SkySceneView.Phase.DAY)
            in 18..21 -> Triple(EVENING_TITLE, EVENING_SUBTITLE, SkySceneView.Phase.EVENING)
            else -> Triple(NIGHT_TITLE, NIGHT_SUBTITLE, SkySceneView.Phase.NIGHT)
        }

        binding.greetingTitle.text = title
        binding.greetingSubtitle.text = subtitle
        binding.skyScene.phase = phase
    }

    private fun animateContent() {
        binding.content.alpha = 0f
        binding.content.translationY = 32f
        binding.content.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(550L)
            .start()
    }

    companion object {
        private const val SPLASH_DURATION_MS = 1800L

        private const val MORNING_TITLE = "\u0414\u043e\u0431\u0440\u043e\u0435 \u0443\u0442\u0440\u043e"
        private const val MORNING_SUBTITLE = "\u041f\u043e\u0434\u0431\u0435\u0440\u0435\u043c \u0447\u0442\u043e-\u043d\u0438\u0431\u0443\u0434\u044c \u043b\u0435\u0433\u043a\u043e\u0435 \u0434\u043b\u044f \u043d\u0430\u0447\u0430\u043b\u0430 \u0434\u043d\u044f."
        private const val DAY_TITLE = "\u0414\u043e\u0431\u0440\u044b\u0439 \u0434\u0435\u043d\u044c"
        private const val DAY_SUBTITLE = "\u0421\u043e\u0431\u0438\u0440\u0430\u0435\u043c \u043f\u043e\u0434\u0431\u043e\u0440\u043a\u0443 \u043d\u0430 \u0441\u0432\u043e\u0431\u043e\u0434\u043d\u044b\u0439 \u0447\u0430\u0441."
        private const val EVENING_TITLE = "\u0414\u043e\u0431\u0440\u044b\u0439 \u0432\u0435\u0447\u0435\u0440"
        private const val EVENING_SUBTITLE = "\u0421\u0435\u0439\u0447\u0430\u0441 \u043d\u0430\u0439\u0434\u0435\u043c \u0447\u0442\u043e-\u043d\u0438\u0431\u0443\u0434\u044c \u0430\u0442\u043c\u043e\u0441\u0444\u0435\u0440\u043d\u043e\u0435."
        private const val NIGHT_TITLE = "\u0414\u043e\u0431\u0440\u043e\u0439 \u043d\u043e\u0447\u0438"
        private const val NIGHT_SUBTITLE = "\u0413\u043e\u0442\u043e\u0432\u0438\u043c \u0441\u043f\u043e\u043a\u043e\u0439\u043d\u044b\u0439 \u043d\u043e\u0447\u043d\u043e\u0439 \u0441\u0435\u0430\u043d\u0441."
    }
}