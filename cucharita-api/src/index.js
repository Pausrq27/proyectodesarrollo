const express = require('express');
const cors = require('cors');
require('dotenv').config();

const authRoutes = require('./routes/authRoutes');
const recipeRoutes = require('./routes/recipeRoutes');

const app = express();
const PORT = process.env.PORT || 3000;

// Middlewares
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Log de requests (opcional, para desarrollo)
app.use((req, res, next) => {
  console.log(`${new Date().toISOString()} - ${req.method} ${req.path}`);
  next();
});

// Rutas
app.get('/', (req, res) => {
  res.json({
    message: 'Cucharita API - Recipe Management System',
    version: '1.0.0',
    endpoints: {
      auth: '/api/auth',
      recipes: '/api/recipes'
    }
  });
});

app.use('/api/auth', authRoutes);
app.use('/api/recipes', recipeRoutes);

// Manejo de errores 404
app.use((req, res) => {
  res.status(404).json({
    error: 'Route not found'
  });
});

// Manejo de errores global
app.use((err, req, res, next) => {
  console.error('Global error handler:', err);
  res.status(err.status || 500).json({
    error: err.message || 'Internal server error'
  });
});

// Iniciar servidor
app.listen(PORT, () => {
  console.log(`\n🚀 Cucharita API running on port ${PORT}`);
  console.log(`📝 Environment: ${process.env.NODE_ENV || 'development'}`);
  console.log(`🔗 API URL: http://localhost:${PORT}`);
  console.log(`\nEndpoints:`);
  console.log(`  Auth: http://localhost:${PORT}/api/auth`);
  console.log(`  Recipes: http://localhost:${PORT}/api/recipes\n`);
});

module.exports = app;