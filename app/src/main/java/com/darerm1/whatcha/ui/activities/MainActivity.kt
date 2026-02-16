package com.darerm1.whatcha.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.darerm1.whatcha.R
import com.darerm1.whatcha.ui.fragments.PlaceholderFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, PlaceholderFragment())
            .commit()

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

    override fun openHome() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, PlaceholderFragment()) // HomeFragment() instead of PlaceholderFragment
            .commit()
    }

    override fun openFavorites() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, PlaceholderFragment()) // FavoritesFragment instead of PlaceholderFragment
            .commit()
    }

    /*override fun openProfile() {
        startActivity(Intent(this, ProfileActivity::class.java)) // ProfileActivity doesnt exist yet
    }*/

    override fun openDetails(movieId: Long) {
        val bundle = Bundle().apply { putLong("movie_id", movieId) }
        val fragment = PlaceholderFragment().apply { arguments = bundle } // DetailFragment instead of PlaceholderFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
