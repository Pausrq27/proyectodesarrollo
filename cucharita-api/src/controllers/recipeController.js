const { supabase } = require('../config/supabase');
const { v4: uuidv4 } = require('uuid');

const addFavoriteStatus = async (recipes, userId) => {
  if (!recipes || recipes.length === 0) return [];

  const recipeIds = recipes.map(r => r.id);

  const { data: favorites, error: favError } = await supabase
    .from('user_favorites')
    .select('recipe_id')
    .eq('user_id', userId)
    .in('recipe_id', recipeIds);

  if (favError) {
    return recipes.map(r => ({ ...r, is_favorite: false }));
  }

  const favoriteIds = new Set(favorites?.map(f => f.recipe_id) || []);

  return recipes.map(recipe => ({
    ...recipe,
    is_favorite: favoriteIds.has(recipe.id)
  }));
};

const getAllRecipes = async (req, res) => {
  try {
    const userId = req.user.id;

    const { data: recipes, error } = await supabase
      .from('recipes')
      .select('*')
      .order('created_at', { ascending: false });

    if (error) throw error;

    const recipesWithFavorites = await addFavoriteStatus(recipes, userId);

    res.json({ recipes: recipesWithFavorites });
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch recipes' });
  }
};

const getMyRecipes = async (req, res) => {
  try {
    const userId = req.user.id;

    const { data: recipes, error } = await supabase
      .from('recipes')
      .select('*')
      .eq('user_id', userId)
      .order('created_at', { ascending: false });

    if (error) throw error;

    const recipesWithFavorites = await addFavoriteStatus(recipes, userId);

    res.json({ recipes: recipesWithFavorites });
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch recipes' });
  }
};

const getFavorites = async (req, res) => {
  try {
    const userId = req.user.id;

    const { data: favorites, error: favError } = await supabase
      .from('user_favorites')
      .select('recipe_id')
      .eq('user_id', userId);

    if (favError) throw favError;

    if (!favorites || favorites.length === 0) {
      return res.json({ recipes: [] });
    }

    const recipeIds = favorites.map(f => f.recipe_id);

    const { data: recipes, error } = await supabase
      .from('recipes')
      .select('*')
      .in('id', recipeIds)
      .order('created_at', { ascending: false });

    if (error) throw error;

    const recipesWithFavorites = recipes.map(recipe => ({
      ...recipe,
      is_favorite: true
    }));

    res.json({ recipes: recipesWithFavorites });
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch favorites' });
  }
};

const getRecipeById = async (req, res) => {
  try {
    const { id } = req.params;
    const userId = req.user.id;

    const { data, error } = await supabase
      .from('recipes')
      .select('*')
      .eq('id', id)
      .single();

    if (error || !data) {
      return res.status(404).json({ error: 'Recipe not found' });
    }

    const { data: favorite } = await supabase
      .from('user_favorites')
      .select('id')
      .eq('user_id', userId)
      .eq('recipe_id', id)
      .maybeSingle();

    res.json({
      recipe: {
        ...data,
        is_favorite: !!favorite
      }
    });
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch recipe' });
  }
};

const searchRecipe = async (req, res) => {
  try {
    const { query } = req.query;
    const userId = req.user.id;

    if (!query) {
      return res.status(400).json({ error: 'Search query is required' });
    }

    const { data: recipes, error } = await supabase
      .from('recipes')
      .select('*')
      .or(`title.ilike.%${query}%,description.ilike.%${query}%`)
      .order('created_at', { ascending: false });

    if (error) throw error;

    const recipesWithFavorites = await addFavoriteStatus(recipes, userId);

    res.json({ recipes: recipesWithFavorites });
  } catch (error) {
    res.status(500).json({ error: 'Failed to search recipes' });
  }
};

const createRecipe = async (req, res) => {
  try {
    const { title, description, ingredients, steps } = req.body;

    if (!title || !description) {
      return res.status(400).json({ error: 'Title and description are required' });
    }

    const ingredientsArray = Array.isArray(ingredients)
      ? ingredients
      : ingredients
      ? ingredients.split(',').map(i => i.trim())
      : [];

    const stepsArray = Array.isArray(steps)
      ? steps
      : steps
      ? steps.split(',').map(s => s.trim())
      : [];

    const { data, error } = await supabase
      .from('recipes')
      .insert([
        {
          user_id: req.user.id,
          title,
          description,
          ingredients: ingredientsArray,
          steps: stepsArray
        }
      ])
      .select()
      .single();

    if (error) throw error;

    res.status(201).json({
      message: 'Recipe created successfully',
      recipe: { ...data, is_favorite: false }
    });
  } catch (error) {
    res.status(500).json({ error: 'Failed to create recipe' });
  }
};

const updateRecipe = async (req, res) => {
  try {
    const { id } = req.params;
    const { title, description, ingredients, steps } = req.body;
    const userId = req.user.id;

    const { data: existingRecipe, error: checkError } = await supabase
      .from('recipes')
      .select('user_id')
      .eq('id', id)
      .single();

    if (checkError || !existingRecipe) {
      return res.status(404).json({ error: 'Recipe not found' });
    }

    if (existingRecipe.user_id !== userId) {
      return res.status(403).json({ error: 'Not authorized to update this recipe' });
    }

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

    const { data: favorite } = await supabase
      .from('user_favorites')
      .select('id')
      .eq('user_id', userId)
      .eq('recipe_id', id)
      .maybeSingle();

    res.json({
      message: 'Recipe updated successfully',
      recipe: { ...data, is_favorite: !!favorite }
    });
  } catch (error) {
    res.status(500).json({ error: 'Failed to update recipe' });
  }
};

const toggleFavorite = async (req, res) => {
  try {
    const { id } = req.params;
    const userId = req.user.id;

    const { data: recipe, error: recipeError } = await supabase
      .from('recipes')
      .select('*')
      .eq('id', id)
      .single();

    if (recipeError || !recipe) {
      return res.status(404).json({ error: 'Recipe not found' });
    }

    const { data: existingFavorite } = await supabase
      .from('user_favorites')
      .select('id')
      .eq('user_id', userId)
      .eq('recipe_id', id)
      .maybeSingle();

    let isFavorite;

    if (existingFavorite) {
      await supabase
        .from('user_favorites')
        .delete()
        .eq('user_id', userId)
        .eq('recipe_id', id);
      isFavorite = false;
    } else {
      await supabase
        .from('user_favorites')
        .insert([{ user_id: userId, recipe_id: id }]);
      isFavorite = true;
    }

    res.json({
      message: 'Favorite status updated',
      recipe: { ...recipe, is_favorite: isFavorite }
    });
  } catch (error) {
    res.status(500).json({ error: 'Failed to toggle favorite' });
  }
};

const deleteRecipe = async (req, res) => {
  try {
    const { id } = req.params;

    const { data: recipe, error: checkError } = await supabase
      .from('recipes')
      .select('user_id, image_url')
      .eq('id', id)
      .single();

    if (checkError || !recipe) {
      return res.status(404).json({ error: 'Recipe not found' });
    }

    if (recipe.user_id !== req.user.id) {
      return res.status(403).json({ error: 'Not authorized to delete this recipe' });
    }

    if (recipe.image_url) {
      const imagePath = recipe.image_url.split('/').pop();
      await supabase.storage
        .from('recipe-images')
        .remove([`${req.user.id}/${imagePath}`]);
    }

    await supabase.from('recipes').delete().eq('id', id);

    res.json({ message: 'Recipe deleted successfully' });
  } catch (error) {
    res.status(500).json({ error: 'Failed to delete recipe' });
  }
};

const uploadImage = async (req, res) => {
  try {
    const { id } = req.params;
    const userId = req.user.id;

    if (!req.file) {
      return res.status(400).json({ error: 'No image file provided' });
    }

    const { data: recipe, error: checkError } = await supabase
      .from('recipes')
      .select('user_id, image_url')
      .eq('id', id)
      .single();

    if (checkError || !recipe) {
      return res.status(404).json({ error: 'Recipe not found' });
    }

    if (recipe.user_id !== userId) {
      return res.status(403).json({ error: 'Not authorized' });
    }

    if (recipe.image_url) {
      const oldImagePath = recipe.image_url.split('/').pop();
      await supabase.storage
        .from('recipe-images')
        .remove([`${userId}/${oldImagePath}`]);
    }

    const fileExt = req.file.originalname.split('.').pop();
    const fileName = `${uuidv4()}.${fileExt}`;
    const filePath = `${userId}/${fileName}`;

    await supabase.storage
      .from('recipe-images')
      .upload(filePath, req.file.buffer, {
        contentType: req.file.mimetype,
        upsert: false
      });

    const { data: { publicUrl } } = supabase.storage
      .from('recipe-images')
      .getPublicUrl(filePath);

    const { data: updatedRecipe } = await supabase
      .from('recipes')
      .update({ image_url: publicUrl })
      .eq('id', id)
      .select()
      .single();

    const { data: favorite } = await supabase
      .from('user_favorites')
      .select('id')
      .eq('user_id', userId)
      .eq('recipe_id', id)
      .maybeSingle();

    res.json({
      message: 'Image uploaded successfully',
      recipe: { ...updatedRecipe, is_favorite: !!favorite },
      image_url: publicUrl
    });
  } catch (error) {
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
