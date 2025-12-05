import express from "express";
import {
    createRecipe,
    getAllRecipes,
    getRecipe,
    updateRecipe,
    deleteRecipe
} from "../controllers/recipeController.js";

const router = express.Router();

router.post("/", createRecipe);
router.get("/", getAllRecipes);
router.get("/:id", getRecipe);
router.put("/:id", updateRecipe);
router.delete("/:id", deleteRecipe);

export default router;
