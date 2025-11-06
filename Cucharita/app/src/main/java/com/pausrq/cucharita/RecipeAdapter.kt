package com.pausrq.cucharita

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pausrq.cucharita.models.Recipe

// Adapter used to connect recipes to the RecyclerView
class RecipeAdapter(
    private var recipes: List<Recipe>,
    private val onClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.ViewHolder>() {

    // Represents each item view in the list
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvRecipeTitle)
        val desc: TextView = itemView.findViewById(R.id.tvRecipeDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.title.text = recipe.getTitle()
        holder.desc.text = recipe.getDescription()
        holder.itemView.setOnClickListener { onClick(recipe) }
    }

    override fun getItemCount(): Int = recipes.size

    // Update list when data changes
    fun updateData(newList: List<Recipe>) {
        recipes = newList
        notifyDataSetChanged()
    }
}
