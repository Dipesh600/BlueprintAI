package com.example.blueprintai

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blueprintai.data.repository.ProjectRepository
import com.example.blueprintai.databinding.ActivityMainBinding
import com.example.blueprintai.ui.auth.LoginActivity
import com.example.blueprintai.ui.generate.GeneratingActivity
import com.example.blueprintai.ui.home.ProjectAdapter
import com.example.blueprintai.ui.input.IdeaInputActivity
import com.example.blueprintai.ui.output.OutputActivity
import com.example.blueprintai.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val authViewModel: AuthViewModel by viewModels()
    
    @Inject
    lateinit var projectRepository: ProjectRepository

    private lateinit var projectAdapter: ProjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRecyclerView()
        setupListeners()
        loadProjects()
    }

    private fun setupRecyclerView() {
        projectAdapter = ProjectAdapter { project ->
            // Navigate to Output or Generating based on status
            val intent = when (project.status) {
                "done" -> Intent(this, OutputActivity::class.java)
                "processing" -> Intent(this, GeneratingActivity::class.java)
                else -> Intent(this, OutputActivity::class.java)
            }
            intent.putExtra("PROJECT_ID", project.id)
            startActivity(intent)
        }
        binding.projectsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = projectAdapter
        }
    }

    private fun loadProjects() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        lifecycleScope.launchWhenStarted {
            projectRepository.getProjects(userId).collectLatest { projects ->
                projectAdapter.submitList(projects)
            }
        }
    }

    private fun setupListeners() {
        binding.logoutButton.setOnClickListener {
            authViewModel.logout()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
        
        binding.newBlueprintFab.setOnClickListener {
            startActivity(Intent(this, IdeaInputActivity::class.java))
        }
    }
}