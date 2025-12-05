import express from "express";
import cors from "cors";
import recipeRoutes from "./routes/recipeRoutes.js";
import dotenv from "dotenv";

dotenv.config();

const app = express();

app.use(cors());
app.use(express.json());

// Rutas
app.use("/api/recipes", recipeRoutes);

// Puerto
const PORT = process.env.PORT || 3000;

app.listen(PORT, () => {
    console.log(`Cucharita API corriendo en puerto ${PORT}`);
});
