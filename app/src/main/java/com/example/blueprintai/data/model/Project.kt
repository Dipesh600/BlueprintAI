package com.example.blueprintai.data.model

import com.google.firebase.Timestamp

data class Project(
    val id: String = "",
    val ideaTitle: String = "",
    val ideaDescription: String = "",
    val features: List<String> = emptyList(),
    val status: String = "processing", // processing, done, failed
    val createdAt: Timestamp = Timestamp.now(),
    val outputs: GeneratedOutput? = null
)

data class GeneratedOutput(
    val prdUrl: String = "",
    val wireframeUrl: String = "",
    val systemDesignUrl: String = ""
)
