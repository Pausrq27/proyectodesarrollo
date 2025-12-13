#  Cucharita

# Cucharita API

API REST para la gestión de recetas. Permite autenticación de usuarios, creación y edición de recetas, manejo de favoritos y subida de imágenes.

Base URL (producción):
```
https://proyectodesarrollo-0c7d.onrender.com
```

---

## Autenticación

La API funciona con autenticación mediante token.

Luego de registrarse o iniciar sesión, el backend devuelve un `access_token` que debe enviarse en los headers de los endpoints protegidos:

```
Authorization: Bearer <access_token>
```

---

## Endpoints de autenticación

### Registrar usuario

```
POST /api/auth/register
```

Body:
```json
{
  "email": "test@example.com",
  "password": "password123",
  "name": "Test User"
}
```

---

### Login

```
POST /api/auth/login
```

Body:
```json
{
  "email": "test@example.com",
  "password": "password123"
}
```

Devuelve un `access_token` que se utiliza para las siguientes requests.

---

### Obtener usuario actual

```
GET /api/auth/me
```

---

### Logout

```
POST /api/auth/logout
```

---

## Endpoints de recetas

### Obtener todas las recetas

```
GET /api/recipes
```

Devuelve todas las recetas del sistema junto con el estado de favorito para el usuario autenticado.

---

### Obtener mis recetas

```
GET /api/recipes/my-recipes
```

Devuelve únicamente las recetas creadas por el usuario autenticado.

---

### Obtener recetas favoritas

```
GET /api/recipes/favorites
```

---

### Buscar recetas

```
GET /api/recipes/search?query=pasta
```

Busca recetas por título o descripción.

---

### Obtener receta por ID

```
GET /api/recipes/:recipe_id
```

---

## Crear y modificar recetas

### Crear receta

```
POST /api/recipes
```

Body:
```json
{
  "title": "Pasta Carbonara",
  "description": "Receta italiana clásica",
  "ingredients": ["Pasta", "Huevos", "Queso"],
  "steps": ["Hervir la pasta", "Mezclar ingredientes"]
}
```

---

### Actualizar receta

```
PUT /api/recipes/:recipe_id
```

Solo el usuario que creó la receta puede actualizarla.

---

### Marcar o desmarcar como favorita

```
PATCH /api/recipes/:recipe_id/favorite
```

---

### Subir imagen a una receta

```
POST /api/recipes/:recipe_id/image
```

Se envía una imagen usando `form-data` con la key `image`.

---

### Eliminar receta

```
DELETE /api/recipes/:recipe_id
```

Solo el creador de la receta puede eliminarla.

---

## Favoritos

Las recetas incluyen el campo:

```json
"is_favorite": true | false
```

Este valor indica si la receta está marcada como favorita por el usuario autenticado.

---

## Health Check

```
GET /
```

Endpoint simple para verificar que la API está funcionando.

---

## Postman

El proyecto incluye una colección de Postman con:
- Variables para `base_url`, `access_token` y `recipe_id`
- Scripts automáticos para guardar el token
- Endpoints listos para pruebas

---

## Métodos HTTP utilizados

| Método | Uso |
|------|----|
| GET | Obtener información |
| POST | Crear datos |
| PUT | Actualizar datos |
| PATCH | Actualización parcial |
| DELETE | Eliminar datos |

---

Proyecto desarrollado como sistema de gestión de recetas con backend REST y autenticación.


###  Descripción del proyecto

Mi idea de proyecto es una **App de Recetas**
La aplicación permite **ver, agregar y guardar recetas de cocina**, mostrando una lista con imagen, nombre y descripción.  
Cada receta puede incluir su foto, lista de ingredientes y pasos de preparación.  
El usuario también puede **buscar recetas** por nombre y **marcarlas como favoritas** para acceder fácilmente después...  

---

##  Mockups de la aplicación

Los siguientes mockups representan las pantallas principales que tendrá la aplicación.  
Están creados con diseño tipo móvil y se ubican dentro de la carpeta `/Mockups`.



###  Pantalla principal – Lista de Recetas
Muestra las recetas guardadas con imagen, nombre y descripción.
  

![Lista de Recetas](Mockups/mockup_lista_recetas.png)

---

### Pantalla – Agregar Receta
Formulario para registrar una nueva receta con nombre, descripción, ingredientes y pasos.



![Agregar Receta](Mockups/mockup_agregar_receta.png)

---

###  Pantalla – Detalle de Receta
Muestra la información completa de una receta, con su foto, ingredientes y pasos en cuadros redondeados.



![Detalle de Receta](Mockups/mockup_detalle_receta.png)

---

###  Pantalla – Favoritos
Lista con las recetas marcadas como favoritas por el usuario.



![Favoritos](Mockups/mockup_favoritos.png)

---
