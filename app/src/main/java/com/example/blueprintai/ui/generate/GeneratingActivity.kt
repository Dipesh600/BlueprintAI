package com.example.blueprintai.ui.generate

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.example.blueprintai.databinding.ActivityGeneratingBinding
import com.example.blueprintai.ui.output.OutputActivity
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GeneratingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGeneratingBinding
    
    @Inject
    lateinit var firestore: FirebaseFirestore

    private val stepEmojis = mapOf(
        "Market Research" to "🔍",
        "Product Requirements Document" to "📋",
        "Wireframe Descriptions" to "🎨",
        "System Design Document" to "🏗️",
        "Generating PDFs" to "📄",
        "Complete" to "✅"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGeneratingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val projectId = intent.getStringExtra("PROJECT_ID") ?: return
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return

        startPulseAnimation()
        listenToProjectStatus(userId, projectId)
    }

    private fun startPulseAnimation() {
        val pulseAnim = ObjectAnimator.ofFloat(binding.aiPulseView, "scaleX", 1f, 1.2f, 1f)
        pulseAnim.repeatCount = ValueAnimator.INFINITE
        pulseAnim.duration = 2000
        pulseAnim.interpolator = AccelerateDecelerateInterpolator()
        pulseAnim.start()

        val pulseAnimY = ObjectAnimator.ofFloat(binding.aiPulseView, "scaleY", 1f, 1.2f, 1f)
        pulseAnimY.repeatCount = ValueAnimator.INFINITE
        pulseAnimY.duration = 2000
        pulseAnimY.interpolator = AccelerateDecelerateInterpolator()
        pulseAnimY.start()
    }

    private fun listenToProjectStatus(userId: String, projectId: String) {
        firestore.collection("users").document(userId)
            .collection("projects").document(projectId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                
                if (snapshot != null && snapshot.exists()) {
                    val status = snapshot.getString("status")
                    val currentStep = snapshot.getString("currentStep")
                    
                    updateUI(status, currentStep)
                    
                    if (status == "done") {
                        val intent = Intent(this, OutputActivity::class.java)
                        intent.putExtra("PROJECT_ID", projectId)
                        startActivity(intent)
                        finish()
                    }
                }
            }
    }

    private fun updateUI(status: String?, currentStep: String?) {
        when (status) {
            "processing" -> {
                val emoji = stepEmojis[currentStep] ?: "⚡"
                binding.statusText.text = "AI is building your blueprint..."
                binding.stepText.text = "$emoji ${currentStep ?: "Initializing..."}"
            }
            "failed" -> {
                binding.statusText.text = "❌ Generation Failed"
                binding.stepText.text = "Something went wrong. Please try again."
                binding.progressBar.isIndeterminate = false
            }
        }
    }
}
