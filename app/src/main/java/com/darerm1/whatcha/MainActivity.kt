package com.darerm1.whatcha

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.darerm1.whatcha.repositories.AllMoviesRepositoryImpl
import com.darerm1.whatcha.repositories.MovieListRepositoryImpl
import com.darerm1.whatcha.infrastructure.AllMoviesService
import com.darerm1.whatcha.infrastructure.MovieListService

class MainActivity : AppCompatActivity() {
    private val TAG: String = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val allMoviListService = AllMoviesService(AllMoviesRepositoryImpl())
        val movieListService = MovieListService(MovieListRepositoryImpl())


//        enableEdgeToEdge()
//        setContentView(R.layout.activity_main)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
    }
}