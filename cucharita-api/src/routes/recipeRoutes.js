import express from 'express';
import multer from 'multer';
import {
    createRecipe,
    getMyRecipes,
    getAllPublicRecipes,
    getRecipe,
    updateRecipe,
    deleteRecipe,
    uploadImage
} from '../controllers/recipeController.js';
import { authenticateUser } from '../middleware/auth.js';

const router = express.Router();
const upload = multer({ storage: multer.memoryStorage() });

// ============================================
// RUTAS PÚBLICAS (no requieren autenticación)
// ============================================
router.get('/', getAllPublicRecipes);  // GET /api/recipes - TODAS las recetas

// ============================================
// RUTAS PROTEGIDAS (requieren autenticación)
// ============================================

// Mis recetas (debe ir ANTES de /:id para evitar conflictos)
router.get('/me', authenticateUser, getMyRecipes);  // GET /api/recipes/me - Solo mis recetas

// Crear receta
router.post('/', authenticateUser, createRecipe);  // POST /api/recipes

// Subir imagen
router.post('/upload', authenticateUser, upload.single('image'), uploadImage);

// ============================================
// RUTAS CON PARÁMETROS (al final para evitar conflictos)
// ============================================
router.get('/:id', getRecipe);  // GET /api/recipes/:id
router.put('/:id', authenticateUser, updateRecipe);  // PUT /api/recipes/:id
router.delete('/:id', authenticateUser, deleteRecipe);  // DELETE /api/recipes/:id

export default router;