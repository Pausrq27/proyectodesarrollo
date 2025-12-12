import { supabase } from '../config/supabase.js';

// CREAR RECETA
export const createRecipe = async (req, res) => {
    try {
        const { title, description, ingredients, steps, imageUrl, isFavorite } = req.body;

        const { data, error } = await supabase
            .from('recipes')
            .insert([{
                user_id: req.user.id,
                title,
                description,
                ingredients,
                steps,
                image_url: imageUrl,
                is_favorite: isFavorite || false
            }])
            .select()
            .single();

        if (error) throw error;

        res.status(201).json(data);

    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// OBTENER MIS RECETAS
export const getMyRecipes = async (req, res) => {
    try {
        const { data, error } = await supabase
            .from('recipes')
            .select('*')
            .eq('user_id', req.user.id)
            .order('created_at', { ascending: false });

        if (error) throw error;

        res.json(data);

    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// OBTENER TODAS LAS RECETAS PÚBLICAS (de todos los usuarios)
export const getAllPublicRecipes = async (req, res) => {
    try {
        const { data, error } = await supabase
            .from('recipes')
            .select(`
                *,
                profiles (
                    username,
                    full_name,
                    avatar_url
                )
            `)
            .order('created_at', { ascending: false });

        if (error) throw error;

        res.json(data);

    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// OBTENER UNA RECETA
export const getRecipe = async (req, res) => {
    try {
        const { data, error } = await supabase
            .from('recipes')
            .select(`
                *,
                profiles (
                    username,
                    full_name,
                    avatar_url
                )
            `)
            .eq('id', req.params.id)
            .single();

        if (error) throw error;

        // RLS se encarga de verificar si el usuario puede ver esta receta
        res.json(data);

    } catch (err) {
        res.status(404).json({ error: 'Recipe not found' });
    }
};

// ACTUALIZAR RECETA
export const updateRecipe = async (req, res) => {
    try {
        const { title, description, ingredients, steps, imageUrl, isFavorite } = req.body;

        const { data, error } = await supabase
            .from('recipes')
            .update({
                title,
                description,
                ingredients,
                steps,
                image_url: imageUrl,
                is_favorite: isFavorite
            })
            .eq('id', req.params.id)
            .eq('user_id', req.user.id) // Solo el dueño puede actualizar
            .select()
            .single();

        if (error) throw error;

        res.json(data);

    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// ELIMINAR RECETA
export const deleteRecipe = async (req, res) => {
    try {
        const { error } = await supabase
            .from('recipes')
            .delete()
            .eq('id', req.params.id)
            .eq('user_id', req.user.id); // Solo el dueño puede eliminar

        if (error) throw error;

        res.json({ message: 'Recipe deleted' });

    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// SUBIR IMAGEN
export const uploadImage = async (req, res) => {
    try {
        if (!req.file) {
            return res.status(400).json({ error: 'No file provided' });
        }

        const fileExt = req.file.originalname.split('.').pop();
        const fileName = `${req.user.id}-${Date.now()}.${fileExt}`;
        const filePath = `recipes/${fileName}`;

        const { data, error } = await supabase.storage
            .from('recipe-images')
            .upload(filePath, req.file.buffer, {
                contentType: req.file.mimetype,
                upsert: false
            });

        if (error) throw error;

        // Obtener URL pública
        const { data: publicData } = supabase.storage
            .from('recipe-images')
            .getPublicUrl(filePath);

        res.json({
            message: 'Image uploaded successfully',
            url: publicData.publicUrl,
            path: filePath
        });

    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};