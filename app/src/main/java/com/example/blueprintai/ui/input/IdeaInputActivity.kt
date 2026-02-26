package com.example.blueprintai.ui.input

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.blueprintai.databinding.ActivityIdeaInputBinding
import com.example.blueprintai.viewmodel.InputViewModel
import com.example.blueprintai.ui.generate.GeneratingActivity
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class IdeaInputActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIdeaInputBinding
    private val viewModel: InputViewModel by viewModels()
    private val features = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIdeaInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeState()
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.addFeatureButton.setOnClickListener {
            val feature = binding.featureEditText.text.toString().trim()
            if (feature.isNotEmpty()) {
                addFeatureChip(feature)
                binding.featureEditText.text?.clear()
            }
        }

        binding.generateButton.setOnClickListener {
            val title = binding.titleEditText.text.toString().trim()
            val description = binding.descriptionEditText.text.toString().trim()
            
            if (title.isNotEmpty() && description.isNotEmpty()) {
                viewModel.generatePRD(title, description, features)
            } else {
                Toast.makeText(this, "Please fill Title and Description", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addFeatureChip(feature: String) {
        features.add(feature)
        val chip = Chip(this).apply {
            text = feature
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                binding.featureChipGroup.removeView(this)
                features.remove(feature)
            }
        }
        binding.featureChipGroup.addView(chip)
    }

    private fun observeState() {
        lifecycleScope.launchWhenStarted {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is InputViewModel.GenerateState.Loading -> {
                        binding.generateButton.isEnabled = false
                    }
                    is InputViewModel.GenerateState.Success -> {
                        val intent = Intent(this@IdeaInputActivity, GeneratingActivity::class.java)
                        intent.putExtra("PROJECT_ID", state.projectId)
                        startActivity(intent)
                        finish()
                    }
                    is InputViewModel.GenerateState.Error -> {
                        binding.generateButton.isEnabled = true
                        Toast.makeText(this@IdeaInputActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }
}
