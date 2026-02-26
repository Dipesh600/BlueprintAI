package com.example.blueprintai.ui.output

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.blueprintai.R
import com.example.blueprintai.databinding.ActivityOutputBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OutputActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOutputBinding
    private var projectId: String? = null
    private var currentTabIndex = 0

    // Store PDF URLs once loaded
    private var prdUrl: String? = null
    private var wireframeUrl: String? = null
    private var systemDesignUrl: String? = null

    @Inject
    lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOutputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        projectId = intent.getStringExtra("PROJECT_ID")
        
        loadOutputUrls()
        setupTabLayout()
        setupListeners()
        
        // Initial fragment
        replaceFragment(PRDPreviewFragment.newInstance(projectId))
    }

    private fun loadOutputUrls() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        projectId?.let { id ->
            firestore.collection("users").document(userId)
                .collection("projects").document(id)
                .get()
                .addOnSuccessListener { snapshot ->
                    val outputs = snapshot.get("outputs") as? Map<*, *>
                    prdUrl = outputs?.get("prdUrl") as? String
                    wireframeUrl = outputs?.get("wireframeUrl") as? String
                    systemDesignUrl = outputs?.get("systemDesignUrl") as? String
                }
        }
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTabIndex = tab?.position ?: 0
                when (currentTabIndex) {
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
            val url = when (currentTabIndex) {
                0 -> prdUrl
                1 -> wireframeUrl
                2 -> systemDesignUrl
                else -> null
            }
            val name = when (currentTabIndex) {
                0 -> "PRD"
                1 -> "Wireframes"
                2 -> "System_Design"
                else -> "Document"
            }
            if (url != null) {
                downloadPdf(url, "BlueprintAI_$name.pdf")
            } else {
                Toast.makeText(this, "PDF not available yet", Toast.LENGTH_SHORT).show()
            }
        }

        binding.saveButton.setOnClickListener {
            // Share the current PDF URL
            val url = when (currentTabIndex) {
                0 -> prdUrl
                1 -> wireframeUrl
                2 -> systemDesignUrl
                else -> null
            }
            if (url != null) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "BlueprintAI - Generated Document")
                    putExtra(Intent.EXTRA_TEXT, "Check out my AI-generated blueprint: $url")
                }
                startActivity(Intent.createChooser(shareIntent, "Share via"))
            } else {
                Toast.makeText(this, "Document not ready yet", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun downloadPdf(url: String, fileName: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle(fileName)
                .setDescription("Downloading from BlueprintAI")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
            Toast.makeText(this, "📥 Download started", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
