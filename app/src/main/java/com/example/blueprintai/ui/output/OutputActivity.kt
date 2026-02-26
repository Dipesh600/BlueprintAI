package com.example.blueprintai.ui.output

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.blueprintai.R
import com.example.blueprintai.databinding.ActivityOutputBinding
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OutputActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOutputBinding
    private var projectId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOutputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        projectId = intent.getStringExtra("PROJECT_ID")
        
        setupTabLayout()
        setupListeners()
        
        // Initial fragment
        replaceFragment(PRDPreviewFragment.newInstance(projectId))
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> replaceFragment(PRDPreviewFragment.newInstance(projectId))
                    1 -> replaceFragment(WireframePreviewFragment.newInstance(projectId))
                    2 -> replaceFragment(SystemDesignPreviewFragment.newInstance(projectId))
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.downloadButton.setOnClickListener {
            // Handle download logic
        }
        binding.saveButton.setOnClickListener {
            finish()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
