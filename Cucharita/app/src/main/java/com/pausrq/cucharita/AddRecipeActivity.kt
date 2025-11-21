package com.pausrq.cucharita

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pausrq.cucharita.R
import com.pausrq.cucharita.controllers.RecipeController
import com.pausrq.cucharita.models.Recipe
import com.pausrq.cucharita.utils.Util

class AddRecipeActivity : AppCompatActivity() {

    private val controller = RecipeController() // control access to recipes
    private lateinit var titleField: EditText
    private lateinit var descField: EditText
    private lateinit var ingredientsField: EditText
    private lateinit var stepsField: EditText
    private lateinit var saveBtn: Button
    private lateinit var selectImageBtn: Button
    private lateinit var imagePreview: ImageView

    private var selectedImage: Bitmap? = null
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        // link UI elements
        titleField = findViewById(R.id.editTitle)
        descField = findViewById(R.id.editDescription)
        ingredientsField = findViewById(R.id.editIngredients)
        stepsField = findViewById(R.id.editSteps)
        saveBtn = findViewById(R.id.btnSave)
        selectImageBtn = findViewById(R.id.btnSelectImage)
        imagePreview = findViewById(R.id.imagePreview)

        // open gallery to select an image
        selectImageBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // save recipe when button pressed
        saveBtn.setOnClickListener {
            val title = titleField.text.toString().trim()
            val desc = descField.text.toString().trim()

            // validate that title and description are not empty
            if (!Util.validateRecipeData(title, desc)) {
                Toast.makeText(this, getString(R.string.toast_empty_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // convert text to lists
            val ingredients = Util.textToList(ingredientsField.text.toString())
            val steps = Util.textToList(stepsField.text.toString())

            // create recipe object
            val recipe = Recipe(title, desc, ingredients, steps)
            recipe.setImage(selectedImage) // ✅ store selected image

            // add recipe to memory
            controller.addNewRecipe(recipe)
            Toast.makeText(this, getString(R.string.toast_recipe_added), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // handle gallery result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            selectedImage = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            imagePreview.setImageBitmap(selectedImage)
            Toast.makeText(this, getString(R.string.toast_image_selected), Toast.LENGTH_SHORT).show()
        }
    }
}
