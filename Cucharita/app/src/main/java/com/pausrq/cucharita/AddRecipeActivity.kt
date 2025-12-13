// 📁 app/src/main/java/com/pausrq/cucharita/AddRecipeActivity.kt
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
import androidx.lifecycle.lifecycleScope
import com.pausrq.cucharita.controllers.RecipeController
import com.pausrq.cucharita.models.Recipe
import com.pausrq.cucharita.utils.Util
import kotlinx.coroutines.launch

class AddRecipeActivity : AppCompatActivity() {

    private val controller = RecipeController()
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

        // Vincular elementos UI
        titleField = findViewById(R.id.editTitle)
        descField = findViewById(R.id.editDescription)
        ingredientsField = findViewById(R.id.editIngredients)
        stepsField = findViewById(R.id.editSteps)
        saveBtn = findViewById(R.id.btnSave)
        selectImageBtn = findViewById(R.id.btnSelectImage)
        imagePreview = findViewById(R.id.imagePreview)

        // Seleccionar imagen
        selectImageBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Guardar receta
        saveBtn.setOnClickListener {
            saveRecipe()
        }
    }

    private fun saveRecipe() {
        val title = titleField.text.toString().trim()
        val desc = descField.text.toString().trim()

        if (!Util.validateRecipeData(title, desc)) {
            Toast.makeText(this, "Por favor completa título y descripción", Toast.LENGTH_SHORT).show()
            return
        }

        val ingredients = Util.textToList(ingredientsField.text.toString())
        val steps = Util.textToList(stepsField.text.toString())

        // Crear receta (sin imagen inicialmente)
        val recipe = Recipe(
            title = title,
            description = desc,
            ingredients = ingredients,
            steps = steps
        )

        saveBtn.isEnabled = false
        saveBtn.text = "Guardando..."

        lifecycleScope.launch {
            try {
                // 1. Crear receta en API
                val createResult = controller.createRecipe(recipe)

                createResult.onSuccess { createdRecipe ->
                    // 2. Si hay imagen, subirla
                    if (selectedImage != null) {
                        val uploadResult = controller.uploadImage(createdRecipe.getId(), selectedImage!!)

                        uploadResult.onSuccess {
                            Toast.makeText(
                                this@AddRecipeActivity,
                                "Receta guardada con imagen",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }

                        uploadResult.onFailure { error ->
                            Toast.makeText(
                                this@AddRecipeActivity,
                                "Receta guardada, pero error al subir imagen: ${error.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        }
                    } else {
                        Toast.makeText(
                            this@AddRecipeActivity,
                            "Receta guardada exitosamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }

                createResult.onFailure { error ->
                    Toast.makeText(
                        this@AddRecipeActivity,
                        "Error al guardar: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    saveBtn.isEnabled = true
                    saveBtn.text = "Guardar Receta"
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@AddRecipeActivity,
                    "Error de conexión: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                saveBtn.isEnabled = true
                saveBtn.text = "Guardar Receta"
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            selectedImage = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            imagePreview.setImageBitmap(selectedImage)
            Toast.makeText(this, "Imagen seleccionada", Toast.LENGTH_SHORT).show()
        }
    }
}