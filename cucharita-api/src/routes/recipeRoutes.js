const express = require('express');
const router = express.Router();
const multer = require('multer');
const { authenticateToken } = require('../middleware/auth');
const {
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
} = require('../controllers/recipeController');

const upload = multer({
  storage: multer.memoryStorage(),
  limits: {
    fileSize: 5 * 1024 * 1024 // 5MB max
  },
  fileFilter: (req, file, cb) => {
    if (file.mimetype.startsWith('image/')) {
      cb(null, true);
    } else {
      cb(new Error('Only image files are allowed'));
    }
  }
});

router.use(authenticateToken);

router.get('/', getAllRecipes);
router.get('/my-recipes', getMyRecipes);
router.get('/favorites', getFavorites);
router.get('/search', searchRecipe);
router.get('/:id', getRecipeById);

router.post('/', createRecipe);

router.put('/:id', updateRecipe);
router.patch('/:id/favorite', toggleFavorite);

router.post('/:id/image', upload.single('image'), uploadImage);

router.delete('/:id', deleteRecipe);

module.exports = router;