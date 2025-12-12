package com.pausrq.cucharita.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pausrq.cucharita.R
import com.pausrq.cucharita.api.models.RecipeResponse

class RecipeApiAdapter(
    private var recipes: List<RecipeResponse>,
    private val onClick: (RecipeResponse) -> Unit
) : RecyclerView.Adapter<RecipeApiAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvRecipeTitle)
        val desc: TextView = itemView.findViewById(R.id.tvRecipeDescription)
        val author: TextView = itemView.findViewById(R.id.tvRecipeAuthor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_api, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.title.text = recipe.title
        holder.desc.text = recipe.description

        // Mostrar autor si está disponible
        val authorName = recipe.profiles?.username ?: "Usuario"
        holder.author.text = "Por: $authorName"

        holder.itemView.setOnClickListener { onClick(recipe) }
    }

    override fun getItemCount(): Int = recipes.size

    fun updateData(newList: List<RecipeResponse>) {
        recipes = newList
        notifyDataSetChanged()
    }
}