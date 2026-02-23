package com.darerm1.whatcha.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.darerm1.whatcha.R
import com.darerm1.whatcha.ui.NavigationListener
import com.darerm1.whatcha.ui.fragments.details.DetailFragment
import com.darerm1.whatcha.ui.fragments.favorites.FavoritesFragment
import com.darerm1.whatcha.ui.fragments.home.HomeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), NavigationListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            openHome()
        }

        findViewById<BottomNavigationView>(R.id.bottom_navigation).setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    openHome()
                    true
                }
                R.id.navigation_favorites -> {
                    openFavorites()
                    true
                }
                R.id.navigation_profile -> {
                    openProfile()
                    true
                }
                else -> false
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun openHome() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()
    }

    override fun openFavorites() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, FavoritesFragment())
            .commit()
    }

    override fun openProfile() {
        startActivity(Intent(this, ProfileActivity::class.java))
    }

    override fun openDetails(movieId: Long) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, DetailFragment.newInstance(movieId))
            .addToBackStack(null)
            .commit()
    }
}
