package com.example.blueprintai.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.blueprintai.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint

import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.blueprintai.databinding.ActivityLoginBinding
import com.example.blueprintai.viewmodel.AuthViewModel
import com.example.blueprintai.ui.home.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        checkUserSession()
        setupListeners()
        observeState()
    }

    private fun checkUserSession() {
        if (viewModel.currentUser != null) {
            navigateToMain()
        }
    }

    private fun setupListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            
            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.login(email, password)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.googleLoginButton.setOnClickListener {
            // Handle Google login logic
        }
        
        binding.registerText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun observeState() {
        lifecycleScope.launchWhenStarted {
            viewModel.authState.collectLatest { state ->
                when (state) {
                    is AuthViewModel.AuthState.Loading -> {
                        binding.loginButton.isEnabled = false
                    }
                    is AuthViewModel.AuthState.Success -> {
                        navigateToMain()
                    }
                    is AuthViewModel.AuthState.Error -> {
                        binding.loginButton.isEnabled = true
                        Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }
}
