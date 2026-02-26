package com.example.blueprintai.ui.generate

import android.content.Intent
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGeneratingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val projectId = intent.getStringExtra("PROJECT_ID") ?: return
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return

        listenToProjectStatus(userId, projectId)
    }

    private fun listenToProjectStatus(userId: String, projectId: String) {
        firestore.collection("users").document(userId)
            .collection("projects").document(projectId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                
                if (snapshot != null && snapshot.exists()) {
                    val status = snapshot.getString("status")
                    updateUI(status)
                    
                    if (status == "done") {
                        val intent = Intent(this, OutputActivity::class.java)
                        intent.putExtra("PROJECT_ID", projectId)
                        startActivity(intent)
                        finish()
                    }
                }
            }
    }

    private fun updateUI(status: String?) {
        when (status) {
            "processing" -> {
                binding.stepText.text = "AI is refining your PRD..."
            }
            "failed" -> {
                binding.statusText.text = "Generation Failed"
                binding.stepText.text = "Something went wrong. Please try again."
                binding.progressBar.isIndeterminate = false
            }
        }
    }
}
