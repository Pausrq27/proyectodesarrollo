package com.pausrq.cucharita.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pausrq.cucharita.R
import com.pausrq.cucharita.models.Recipe

class GalleryAdapter(
    private val recipeList: List<Recipe>,
    private val onClick: (Recipe) -> Unit
) : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.itemImage)
        val title: TextView = view.findViewById(R.id.itemTitle)
        val desc: TextView = view.findViewById(R.id.itemDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gallery, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recipe = recipeList[position]
        holder.title.text = recipe.getTitle()
        holder.desc.text = recipe.getDescription()

        // Cargar imagen con Glide
        val imageUrl = recipe.getImageUrl()
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(holder.image)
        } else {
            holder.image.setImageResource(R.drawable.ic_launcher_background)
        }

        // Click listener
        holder.itemView.setOnClickListener {
            onClick(recipe)
        }
    }

    override fun getItemCount(): Int = recipeList.size
}