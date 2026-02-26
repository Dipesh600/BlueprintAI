package com.example.blueprintai

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.blueprintai.databinding.ActivityMainBinding
import com.example.blueprintai.ui.auth.LoginActivity
import com.example.blueprintai.ui.input.IdeaInputActivity
import com.example.blueprintai.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupListeners()
    }

    private fun setupListeners() {
        binding.logoutButton.setOnClickListener {
            // Logout logic
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        
        binding.newBlueprintFab.setOnClickListener {
            startActivity(Intent(this, IdeaInputActivity::class.java))
        }
    }
}