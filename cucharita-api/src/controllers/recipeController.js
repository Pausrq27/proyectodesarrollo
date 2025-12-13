const { supabase } = require('../config/supabase');
const { v4: uuidv4 } = require('uuid');

// Obtener todas las recetas
const getAllRecipes = async (req, res) => {
  try {
    const { data, error } = await supabase
      .from('recipes')
      .select('*')
      .order('created_at', { ascending: false });

    if (error) throw error;

    res.json({
      recipes: data
    });
  } catch (error) {
    console.error('Get recipes error:', error);
    res.status(500).json({ 
      error: 'Failed to fetch recipes' 
    });
  }
};

// Obtener recetas del usuario actual
const getMyRecipes = async (req, res) => {
  try {
    const { data, error } = await supabase
      .from('recipes')
      .select('*')
      .eq('user_id', req.user.id)
      .order('created_at', { ascending: false });

    if (error) throw error;

    res.json({
      recipes: data
    });
  } catch (error) {
    console.error('Get my recipes error:', error);
    res.status(500).json({ 
      error: 'Failed to fetch recipes' 
    });
  }
};

// Obtener recetas favoritas
const getFavorites = async (req, res) => {
  try {
    const { data, error } = await supabase
      .from('recipes')
      .select('*')
      .eq('user_id', req.user.id)
      .eq('is_favorite', true)
      .order('created_at', { ascending: false });

    if (error) throw error;

    res.json({
      recipes: data
    });
  } catch (error) {
    console.error('Get favorites error:', error);
    res.status(500).json({ 
      error: 'Failed to fetch favorites' 
    });
  }
};

// Obtener una receta por ID
const getRecipeById = async (req, res) => {
  try {
    const { id } = req.params;

    const { data, error } = await supabase
      .from('recipes')
      .select('*')
      .eq('id', id)
      .single();

    if (error) throw error;

    if (!data) {
      return res.status(404).json({ 
        error: 'Recipe not found' 
      });
    }

    res.json({
      recipe: data
    });
  } catch (error) {
    console.error('Get recipe error:', error);
    res.status(500).json({ 
      error: 'Failed to fetch recipe' 
    });
  }
};

// Buscar receta por nombre
const searchRecipe = async (req, res) => {
  try {
    const { query } = req.query;

    if (!query) {
      return res.status(400).json({ 
        error: 'Search query is required' 
      });
    }

    const { data, error } = await supabase
      .from('recipes')
      .select('*')
      .or(`title.ilike.%${query}%,description.ilike.%${query}%`)
      .order('created_at', { ascending: false });

    if (error) throw error;

    res.json({
      recipes: data
    });
  } catch (error) {
    console.error('Search recipe error:', error);
    res.status(500).json({ 
      error: 'Failed to search recipes' 
    });
  }
};

// Crear nueva receta
const createRecipe = async (req, res) => {
  try {
    const { title, description, ingredients, steps } = req.body;

    if (!title || !description) {
      return res.status(400).json({ 
        error: 'Title and description are required' 
      });
    }

    // Convertir arrays si vienen como strings
    const ingredientsArray = Array.isArray(ingredients) 
      ? ingredients 
      : (ingredients ? ingredients.split(',').map(i => i.trim()) : []);
    
    const stepsArray = Array.isArray(steps) 
      ? steps 
      : (steps ? steps.split(',').map(s => s.trim()) : []);

    const { data, error } = await supabase
      .from('recipes')
      .insert([{
        user_id: req.user.id,
        title,
        description,
        ingredients: ingredientsArray,
        steps: stepsArray,
        is_favorite: false
      }])
      .select()
      .single();

    if (error) throw error;

    res.status(201).json({
      message: 'Recipe created successfully',
      recipe: data
    });
  } catch (error) {
    console.error('Create recipe error:', error);
    res.status(500).json({ 
      error: 'Failed to create recipe' 
    });
  }
};

// Actualizar receta
const updateRecipe = async (req, res) => {
  try {
    const { id } = req.params;
    const { title, description, ingredients, steps, is_favorite } = req.body;

    // Verificar que la receta pertenece al usuario
    const { data: existingRecipe, error: checkError } = await supabase
      .from('recipes')
      .select('user_id')
      .eq('id', id)
      .single();

    if (checkError || !existingRecipe) {
      return res.status(404).json({ 
        error: 'Recipe not found' 
      });
    }

    if (existingRecipe.user_id !== req.user.id) {
      return res.status(403).json({ 
        error: 'Not authorized to update this recipe' 
      });
    }

    // Preparar datos para actualizar
    const updateData = {};
    if (title !== undefined) updateData.title = title;
    if (description !== undefined) updateData.description = description;
    if (is_favorite !== undefined) updateData.is_favorite = is_favorite;
    
    if (ingredients !== undefined) {
      updateData.ingredients = Array.isArray(ingredients) 
        ? ingredients 
        : ingredients.split(',').map(i => i.trim());
    }
    
    if (steps !== undefined) {
      updateData.steps = Array.isArray(steps) 
        ? steps 
        : steps.split(',').map(s => s.trim());
    }

    const { data, error } = await supabase
      .from('recipes')
      .update(updateData)
      .eq('id', id)
      .select()
      .single();

    if (error) throw error;

    res.json({
      message: 'Recipe updated successfully',
      recipe: data
    });
  } catch (error) {
    console.error('Update recipe error:', error);
    res.status(500).json({ 
      error: 'Failed to update recipe' 
    });
  }
};

// Marcar/desmarcar como favorito
const toggleFavorite = async (req, res) => {
  try {
    const { id } = req.params;

    // Obtener estado actual
    const { data: recipe, error: getError } = await supabase
      .from('recipes')
      .select('is_favorite, user_id')
      .eq('id', id)
      .single();

    if (getError || !recipe) {
      return res.status(404).json({ 
        error: 'Recipe not found' 
      });
    }

    if (recipe.user_id !== req.user.id) {
      return res.status(403).json({ 
        error: 'Not authorized' 
      });
    }

    // Toggle favorito
    const { data, error } = await supabase
      .from('recipes')
      .update({ is_favorite: !recipe.is_favorite })
      .eq('id', id)
      .select()
      .single();

    if (error) throw error;

    res.json({
      message: 'Favorite status updated',
      recipe: data
    });
  } catch (error) {
    console.error('Toggle favorite error:', error);
    res.status(500).json({ 
      error: 'Failed to toggle favorite' 
    });
  }
};

// Eliminar receta
const deleteRecipe = async (req, res) => {
  try {
    const { id } = req.params;

    // Verificar que la receta pertenece al usuario
    const { data: recipe, error: checkError } = await supabase
      .from('recipes')
      .select('user_id, image_url')
      .eq('id', id)
      .single();

    if (checkError || !recipe) {
      return res.status(404).json({ 
        error: 'Recipe not found' 
      });
    }

    if (recipe.user_id !== req.user.id) {
      return res.status(403).json({ 
        error: 'Not authorized to delete this recipe' 
      });
    }

    // Si tiene imagen, eliminarla del storage
    if (recipe.image_url) {
      const imagePath = recipe.image_url.split('/').pop();
      await supabase.storage
        .from('recipe-images')
        .remove([`${req.user.id}/${imagePath}`]);
    }

    // Eliminar receta
    const { error } = await supabase
      .from('recipes')
      .delete()
      .eq('id', id);

    if (error) throw error;

    res.json({
      message: 'Recipe deleted successfully'
    });
  } catch (error) {
    console.error('Delete recipe error:', error);
    res.status(500).json({ 
      error: 'Failed to delete recipe' 
    });
  }
};

// Subir imagen para una receta
const uploadImage = async (req, res) => {
  try {
    const { id } = req.params;

    if (!req.file) {
      return res.status(400).json({ 
        error: 'No image file provided' 
      });
    }

    // Verificar que la receta pertenece al usuario
    const { data: recipe, error: checkError } = await supabase
      .from('recipes')
      .select('user_id, image_url')
      .eq('id', id)
      .single();

    if (checkError || !recipe) {
      return res.status(404).json({ 
        error: 'Recipe not found' 
      });
    }

    if (recipe.user_id !== req.user.id) {
      return res.status(403).json({ 
        error: 'Not authorized' 
      });
    }

    // Eliminar imagen anterior si existe
    if (recipe.image_url) {
      const oldImagePath = recipe.image_url.split('/').pop();
      await supabase.storage
        .from('recipe-images')
        .remove([`${req.user.id}/${oldImagePath}`]);
    }

    // Generar nombre único para la imagen
    const fileExt = req.file.originalname.split('.').pop();
    const fileName = `${uuidv4()}.${fileExt}`;
    const filePath = `${req.user.id}/${fileName}`;

    // Subir imagen a Supabase Storage
    const { error: uploadError } = await supabase.storage
      .from('recipe-images')
      .upload(filePath, req.file.buffer, {
        contentType: req.file.mimetype,
        upsert: false
      });

    if (uploadError) throw uploadError;

    // Obtener URL pública de la imagen
    const { data: { publicUrl } } = supabase.storage
      .from('recipe-images')
      .getPublicUrl(filePath);

    // Actualizar receta con la URL de la imagen
    const { data, error: updateError } = await supabase
      .from('recipes')
      .update({ image_url: publicUrl })
      .eq('id', id)
      .select()
      .single();

    if (updateError) throw updateError;

    res.json({
      message: 'Image uploaded successfully',
      recipe: data,
      image_url: publicUrl
    });
  } catch (error) {
    console.error('Upload image error:', error);
    res.status(500).json({ 
      error: 'Failed to upload image' 
    });
  }
};

module.exports = {
  getAllRecipes,
  getMyRecipes,
  getFavorites,
  getRecipeById,
  searchRecipe,
  createRecipe,
  updateRecipe,
  toggleFavorite,
  deleteRecipe,
  uploadImage
};