// =============================================
// routes/authRoutes.js - ACTUALIZADO
// =============================================
import express from 'express';
import { 
    register, 
    login, 
    logout, 
    getCurrentUser,
    updateProfile 
} from '../controllers/authController.js';
import { authenticateUser } from '../middleware/auth.js';

const authRouter = express.Router();

// Rutas públicas
authRouter.post('/register', register);
authRouter.post('/login', login);

// Rutas protegidas
authRouter.post('/logout', authenticateUser, logout);
authRouter.get('/me', authenticateUser, getCurrentUser);
authRouter.put('/profile', authenticateUser, updateProfile);

export default authRouter;