package com.example.blueprintai.ui.output

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.blueprintai.databinding.FragmentPreviewBinding
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PRDPreviewFragment : Fragment() {
    private var _binding: FragmentPreviewBinding? = null
    private val binding get() = _binding!!
    
    @Inject
    lateinit var firestore: FirebaseFirestore

    private var projectId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        projectId = arguments?.getString("PROJECT_ID")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadPRD()
    }

    private fun loadPRD() {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        projectId?.let { id ->
            firestore.collection("users").document(userId)
                .collection("projects").document(id)
                .get()
                .addOnSuccessListener { snapshot ->
                    // For now displaying raw data, later Markdown
                    val prd = snapshot.get("prdContent") as? String ?: "PRD content not found yet."
                    binding.previewText.text = prd
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(projectId: String?) = PRDPreviewFragment().apply {
            arguments = Bundle().apply { putString("PROJECT_ID", projectId) }
        }
    }
}
