package com.example.blueprintai.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.blueprintai.R
import com.example.blueprintai.data.model.Project

class ProjectAdapter(
    private val onProjectClick: (Project) -> Unit
) : ListAdapter<Project, ProjectAdapter.ProjectViewHolder>(ProjectDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_project, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.projectTitle)
        private val descriptionText: TextView = itemView.findViewById(R.id.projectDescription)
        private val statusBadge: TextView = itemView.findViewById(R.id.statusBadge)
        private val dateText: TextView = itemView.findViewById(R.id.projectDate)

        fun bind(project: Project) {
            titleText.text = project.ideaTitle
            descriptionText.text = project.ideaDescription
            
            statusBadge.text = project.status.uppercase()
            statusBadge.setBackgroundResource(
                when (project.status) {
                    "done" -> R.drawable.bg_badge_success
                    "processing" -> R.drawable.bg_badge_processing
                    "failed" -> R.drawable.bg_badge_error
                    else -> R.drawable.bg_badge_processing
                }
            )
            
            dateText.text = android.text.format.DateFormat.format(
                "MMM dd, yyyy",
                project.createdAt.toDate()
            )
            
            itemView.setOnClickListener { onProjectClick(project) }
        }
    }

    class ProjectDiffCallback : DiffUtil.ItemCallback<Project>() {
        override fun areItemsTheSame(oldItem: Project, newItem: Project) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Project, newItem: Project) = oldItem == newItem
    }
}
