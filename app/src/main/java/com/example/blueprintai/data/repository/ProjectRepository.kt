package com.example.blueprintai.data.repository

import com.example.blueprintai.data.model.Project
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun getProjects(userId: String): Flow<List<Project>> = callbackFlow {
        val subscription = firestore.collection("users").document(userId)
            .collection("projects")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val projects = snapshot?.toObjects(Project::class.java) ?: emptyList()
                trySend(projects)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun createProject(userId: String, project: Project): String {
        val docRef = firestore.collection("users").document(userId)
            .collection("projects")
            .document()
        val newProject = project.copy(id = docRef.id)
        docRef.set(newProject).await()
        return docRef.id
    }
}
