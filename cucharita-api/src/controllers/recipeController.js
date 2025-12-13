const { supabase } = require('../config/supabase');
const { v4: uuidv4 } = require('uuid');

// Helper para agregar estado de favorito a las recetas
const addFavoriteStatus = async (recipes, userId) => {
  if (!recipes || recipes.length === 0) return [];

  const recipeIds = recipes.map(r => r.id);

  // Obtener favoritos del usuario para estas recetas
  const { data: favorites, error: favError } = await supabase
    .from('user_favorites')
    .select('recipe_id')
    .eq('user_id', userId)
    .in('recipe_id', recipeIds);

  if (favError) {
    console.error('Error fetching favorites:', favError);
    return recipes.map(r => ({ ...r, is_favorite: false }));
  }

  const favoriteIds = new Set(favorites?.map(f => f.recipe_id) || []);

  return recipes.map(recipe => ({
    ...recipe,
    is_favorite: favoriteIds.has(recipe.id)
  }));
};

// Obtener todas las recetas (con info de si el usuario actual las tiene como favoritas)
const getAllRecipes = async (req, res) => {
  try {
    const userId = req.user.id;

    // Obtener todas las recetas
    const { data: recipes, error } = await supabase
      .from('recipes')
      .select('*')
      .order('created_at', { ascending: false });

    if (error) throw error;

    // Agregar estado de favorito
    const recipesWithFavorites = await addFavoriteStatus(recipes, userId);

    res.json({
      recipes: recipesWithFavorites
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
    const userId = req.user.id;

    const { data: recipes, error } = await supabase
      .from('recipes')
      .select('*')
      .eq('user_id', userId)
      .order('created_at', { ascending: false });

    if (error) throw error;

    // Agregar estado de favorito
    const recipesWithFavorites = await addFavoriteStatus(recipes, userId);

    res.json({
      recipes: recipesWithFavorites
    });
  } catch (error) {
    console.error('Get my recipes error:', error);
    res.status(500).json({ 
      error: 'Failed to fetch recipes' 
    });
  }
};

// Obtener recetas favoritas del usuario actual
const getFavorites = async (req, res) => {
  try {
    const userId = req.user.id;

    // Obtener IDs de recetas favoritas
    const { data: favorites, error: favError } = await supabase
      .from('user_favorites')
      .select('recipe_id')
      .eq('user_id', userId);

    if (favError) throw favError;

    if (!favorites || favorites.length === 0) {
      return res.json({ recipes: [] });
    }

    const recipeIds = favorites.map(f => f.recipe_id);

    // Obtener las recetas completas
    const { data: recipes, error } = await supabase
      .from('recipes')
      .select('*')
      .in('id', recipeIds)
      .order('created_at', { ascending: false });

    if (error) throw error;

    // Todas estas recetas son favoritas del usuario
    const recipesWithFavorites = recipes.map(recipe => ({
      ...recipe,
      is_favorite: true
    }));

    res.json({
      recipes: recipesWithFavorites
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
    const userId = req.user.id;

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

    // Verificar si es favorita para este usuario
    const { data: favorite } = await supabase
      .from('user_favorites')
      .select('id')
      .eq('user_id', userId)
      .eq('recipe_id', id)
      .maybeSingle();

    const recipeWithFavorite = {
      ...data,
      is_favorite: !!favorite
    };

    res.json({
      recipe: recipeWithFavorite
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
    const userId = req.user.id;

    if (!query) {
      return res.status(400).json({ 
        error: 'Search query is required' 
      });
    }

    const { data: recipes, error } = await supabase
      .from('recipes')
      .select('*')
      .or(`title.ilike.%${query}%,description.ilike.%${query}%`)
      .order('created_at', { ascending: false });

    if (error) throw error;

    // Agregar estado de favorito
    const recipesWithFavorites = await addFavoriteStatus(recipes, userId);

    res.json({
      recipes: recipesWithFavorites
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
        steps: stepsArray
      }])
      .select()
      .single();

    if (error) throw error;

    // Nueva receta no es favorita por defecto
    const recipeWithFavorite = {
      ...data,
      is_favorite: false
    };

    res.status(201).json({
      message: 'Recipe created successfully',
      recipe: recipeWithFavorite
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
    const { title, description, ingredients, steps } = req.body;
    const userId = req.user.id;

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

    if (existingRecipe.user_id !== userId) {
      return res.status(403).json({ 
        error: 'Not authorized to update this recipe' 
      });
    }

    // Preparar datos para actualizar
    const updateData = {};
    if (title !== undefined) updateData.title = title;
    if (description !== undefined) updateData.description = description;
    
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

    // Verificar si es favorita
    const { data: favorite } = await supabase
      .from('user_favorites')
      .select('id')
      .eq('user_id', userId)
      .eq('recipe_id', id)
      .maybeSingle();

    const recipeWithFavorite = {
      ...data,
      is_favorite: !!favorite
    };

    res.json({
      message: 'Recipe updated successfully',
      recipe: recipeWithFavorite
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
    const userId = req.user.id;

    // Verificar que la receta existe
    const { data: recipe, error: recipeError } = await supabase
      .from('recipes')
      .select('*')
      .eq('id', id)
      .single();

    if (recipeError || !recipe) {
      return res.status(404).json({ 
        error: 'Recipe not found' 
      });
    }

    // Verificar si ya es favorita
    const { data: existingFavorite } = await supabase
      .from('user_favorites')
      .select('id')
      .eq('user_id', userId)
      .eq('recipe_id', id)
      .maybeSingle();

    let isFavorite;

    if (existingFavorite) {
      // Ya es favorita, eliminarla
      const { error: deleteError } = await supabase
        .from('user_favorites')
        .delete()
        .eq('user_id', userId)
        .eq('recipe_id', id);

      if (deleteError) throw deleteError;
      isFavorite = false;
    } else {
      // No es favorita, agregarla
      const { error: insertError } = await supabase
        .from('user_favorites')
        .insert([{
          user_id: userId,
          recipe_id: id
        }]);

      if (insertError) throw insertError;
      isFavorite = true;
    }

    const recipeWithFavorite = {
      ...recipe,
      is_favorite: isFavorite
    };

    res.json({
      message: 'Favorite status updated',
      recipe: recipeWithFavorite
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
      try {
        const imagePath = recipe.image_url.split('/').pop();
        await supabase.storage
          .from('recipe-images')
          .remove([`${req.user.id}/${imagePath}`]);
      } catch (err) {
        console.warn('Error deleting image:', err);
      }
    }

    // Los favoritos se eliminarán automáticamente por ON DELETE CASCADE
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
    const userId = req.user.id;

    if (!req.file) {
      console.error('No file in request');
      return res.status(400).json({ 
        error: 'No image file provided'
      });
    }

    console.log('File received:', {
      originalname: req.file.originalname,
      mimetype: req.file.mimetype,
      size: req.file.size
    });

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

    if (recipe.user_id !== userId) {
      return res.status(403).json({ 
        error: 'Not authorized' 
      });
    }

    // Eliminar imagen anterior si existe
    if (recipe.image_url) {
      try {
        const oldImagePath = recipe.image_url.split('/').pop();
        await supabase.storage
          .from('recipe-images')
          .remove([`${userId}/${oldImagePath}`]);
      } catch (err) {
        console.warn('Error deleting old image:', err);
      }
    }

    // Generar nombre único para la imagen
    const fileExt = req.file.originalname.split('.').pop();
    const fileName = `${uuidv4()}.${fileExt}`;
    const filePath = `${userId}/${fileName}`;

    console.log('Uploading to path:', filePath);

    // Subir imagen a Supabase Storage
    const { error: uploadError } = await supabase.storage
      .from('recipe-images')
      .upload(filePath, req.file.buffer, {
        contentType: req.file.mimetype,
        upsert: false
      });

    if (uploadError) {
      console.error('Supabase upload error:', uploadError);
      throw uploadError;
    }

    // Obtener URL pública de la imagen
    const { data: { publicUrl } } = supabase.storage
      .from('recipe-images')
      .getPublicUrl(filePath);

    console.log('Public URL:', publicUrl);

    // Actualizar receta con la URL de la imagen
    const { data: updatedRecipe, error: updateError } = await supabase
      .from('recipes')
      .update({ image_url: publicUrl })
      .eq('id', id)
      .select()
      .single();

    if (updateError) throw updateError;

    // Verificar si es favorita
    const { data: favorite } = await supabase
      .from('user_favorites')
      .select('id')
      .eq('user_id', userId)
      .eq('recipe_id', id)
      .maybeSingle();

    const recipeWithFavorite = {
      ...updatedRecipe,
      is_favorite: !!favorite
    };

    res.json({
      message: 'Image uploaded successfully',
      recipe: recipeWithFavorite,
      image_url: publicUrl
    });
  } catch (error) {
    console.error('Upload image error:', error);
    res.status(500).json({ 
      error: 'Failed to upload image',
      details: error.message
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