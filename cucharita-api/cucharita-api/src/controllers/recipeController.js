import { db } from "../config/db.js";

// CREATE
export const createRecipe = async (req, res) => {
    try {
        const { title, description, ingredients, steps, imageUri, isFavorite } = req.body;

        const [result] = await db.query(
            `INSERT INTO recipes (title, description, ingredients, steps, imageUri, isFavorite)
             VALUES (?, ?, ?, ?, ?, ?)`,
            [
                title,
                description,
                JSON.stringify(ingredients),
                JSON.stringify(steps),
                imageUri,
                isFavorite ? 1 : 0
            ]
        );

        res.status(201).json({ id: result.insertId, ...req.body });

    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// GET ALL
export const getAllRecipes = async (req, res) => {
    try {
        const [rows] = await db.query("SELECT * FROM recipes");

        const recipes = rows.map(r => ({
            ...r,
            ingredients: JSON.parse(r.ingredients),
            steps: JSON.parse(r.steps),
            isFavorite: Boolean(r.isFavorite)
        }));

        res.json(recipes);

    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// GET ONE
export const getRecipe = async (req, res) => {
    try {
        const [rows] = await db.query("SELECT * FROM recipes WHERE id = ?", [req.params.id]);

        if (rows.length === 0) return res.status(404).json({ error: "Recipe not found" });

        const recipe = rows[0];
        recipe.ingredients = JSON.parse(recipe.ingredients);
        recipe.steps = JSON.parse(recipe.steps);
        recipe.isFavorite = Boolean(recipe.isFavorite);

        res.json(recipe);

    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// UPDATE
export const updateRecipe = async (req, res) => {
    try {
        const { title, description, ingredients, steps, imageUri, isFavorite } = req.body;

        await db.query(
            `UPDATE recipes SET 
                title = ?, 
                description = ?, 
                ingredients = ?, 
                steps = ?, 
                imageUri = ?, 
                isFavorite = ? 
             WHERE id = ?`,
            [
                title,
                description,
                JSON.stringify(ingredients),
                JSON.stringify(steps),
                imageUri,
                isFavorite ? 1 : 0,
                req.params.id
            ]
        );

        res.json({ message: "Recipe updated successfully" });

    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// DELETE
export const deleteRecipe = async (req, res) => {
    try {
        await db.query("DELETE FROM recipes WHERE id = ?", [req.params.id]);
        res.json({ message: "Recipe deleted" });

    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};
