package com.darerm1.whatcha.ui.activities

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.darerm1.whatcha.R
import com.darerm1.whatcha.databinding.ActivityProfileBinding
import com.darerm1.whatcha.infrastructure.MovieListService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val movieListService = MovieListService.instance

    private val prefs by lazy { getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    private val pickAvatarLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
            }
            prefs.edit().putString(KEY_AVATAR_URI, uri.toString()).apply()
            renderAvatar()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.avatarImage.setOnClickListener { pickAvatar() }
        binding.editAvatarFab.setOnClickListener { pickAvatar() }

        binding.editNicknameButton.setOnClickListener { showEditNicknameDialog() }
        binding.nicknameRow.setOnClickListener { showEditNicknameDialog() }

        setupThemeToggle()

        renderNickname()
        renderAvatar()
        renderStats()
    }

    private fun pickAvatar() {
        pickAvatarLauncher.launch(arrayOf("image/*"))
    }

    private fun renderAvatar() {
        val uriString = prefs.getString(KEY_AVATAR_URI, null)
        if (uriString.isNullOrBlank()) {
            binding.avatarImage.setImageResource(R.drawable.ic_avatar)
            return
        }
        val uri = Uri.parse(uriString)
        binding.avatarImage.setImageURI(uri)
    }

    private fun renderNickname() {
        val nickname = prefs.getString(KEY_NICKNAME, null)
        binding.nicknameText.text = if (nickname.isNullOrBlank()) {
            getString(R.string.profile_user_name_default)
        } else {
            nickname
        }
    }

    private fun renderStats() {
        val movies = movieListService.getMovies()
        val count = movies.size
        val rated = movies.mapNotNull { it.personalRating }
        val avg = if (rated.isNotEmpty()) rated.average() else null

        binding.favoritesCount.text = getString(R.string.profile_favorites_count, count)
        binding.averageRating.text = if (avg == null) {
            getString(R.string.profile_average_rating, 0.0)
        } else {
            getString(R.string.profile_average_rating, avg)
        }
    }

    private fun showEditNicknameDialog() {
        val inputLayout = TextInputLayout(this)
        val editText = TextInputEditText(this)
        editText.setText(prefs.getString(KEY_NICKNAME, ""))
        editText.hint = getString(R.string.profile_edit_nickname_hint)
        inputLayout.addView(editText)

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.profile_edit_nickname_title))
            .setView(inputLayout)
            .setPositiveButton(getString(R.string.profile_save)) { _, _ ->
                val newName = editText.text?.toString()?.trim().orEmpty()
                prefs.edit().putString(KEY_NICKNAME, newName).apply()
                renderNickname()
            }
            .setNegativeButton(getString(R.string.profile_cancel), null)
            .show()
    }

    private fun setupThemeToggle() {
        val mode = prefs.getInt(KEY_THEME_MODE, THEME_SYSTEM)

        when (mode) {
            THEME_LIGHT -> binding.themeToggle.check(R.id.themeLight)
            THEME_DARK -> binding.themeToggle.check(R.id.themeDark)
            else -> binding.themeToggle.check(R.id.themeSystem)
        }

        binding.themeToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            val newMode = when (checkedId) {
                R.id.themeLight -> THEME_LIGHT
                R.id.themeDark -> THEME_DARK
                else -> THEME_SYSTEM
            }
            prefs.edit().putInt(KEY_THEME_MODE, newMode).apply()
            applyTheme(newMode)
        }

        applyTheme(mode)
    }

    private fun applyTheme(mode: Int) {
        val nightMode = when (mode) {
            THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    companion object {
        private const val PREFS_NAME = "profile_prefs"
        private const val KEY_NICKNAME = "nickname"
        private const val KEY_AVATAR_URI = "avatar_uri"
        private const val KEY_THEME_MODE = "theme_mode"

        private const val THEME_SYSTEM = 0
        private const val THEME_LIGHT = 1
        private const val THEME_DARK = 2
    }
}
