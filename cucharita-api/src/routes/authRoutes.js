const express = require('express');
const router = express.Router();
const { authenticateToken } = require('../middleware/auth');
const {
  register,
  login,
  logout,
  getCurrentUser
} = require('../controllers/authController');

// Rutas públicas
router.post('/register', register);
router.post('/login', login);

// Rutas protegidas
router.post('/logout', authenticateToken, logout);
router.get('/me', authenticateToken, getCurrentUser);

module.exports = router;