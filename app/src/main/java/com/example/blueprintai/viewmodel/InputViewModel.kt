package com.example.blueprintai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blueprintai.data.model.Project
import com.example.blueprintai.data.repository.ProjectRepository
import com.example.blueprintai.data.repository.AuthRepository
import com.google.firebase.functions.FirebaseFunctions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class InputViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val authRepository: AuthRepository,
    private val functions: FirebaseFunctions
) : ViewModel() {

    private val _uiState = MutableStateFlow<GenerateState>(GenerateState.Idle)
    val uiState: StateFlow<GenerateState> = _uiState

    fun generatePRD(title: String, description: String, features: List<String>) {
        val userId = authRepository.currentUser?.uid ?: return
        
        viewModelScope.launch {
            _uiState.value = GenerateState.Loading
            try {
                // 1. Create project in Firestore with status "processing"
                val project = Project(
                    ideaTitle = title,
                    ideaDescription = description,
                    features = features,
                    status = "processing"
                )
                val projectId = projectRepository.createProject(userId, project)
                
                // 2. Call Firebase Function to trigger AI generation
                val data = hashMapOf(
                    "projectId" to projectId,
                    "userId" to userId,
                    "ideaTitle" to title,
                    "ideaDescription" to description,
                    "features" to features
                )
                
                functions.getHttpsCallable("generatePRD")
                    .call(data)
                    .await()
                
                _uiState.value = GenerateState.Success(projectId)
            } catch (e: Exception) {
                _uiState.value = GenerateState.Error(e.message ?: "Failed to start generation")
            }
        }
    }

    sealed class GenerateState {
        object Idle : GenerateState()
        object Loading : GenerateState()
        data class Success(val projectId: String) : GenerateState()
        data class Error(val message: String) : GenerateState()
    }
}
