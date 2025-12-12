
import com.pausrq.cucharita.api.models.AuthResponse
import com.pausrq.cucharita.api.models.CreateRecipeRequest
import com.pausrq.cucharita.api.models.LoginRequest
import com.pausrq.cucharita.api.models.MessageResponse
import com.pausrq.cucharita.api.models.RecipeResponse
import com.pausrq.cucharita.api.models.RegisterRequest
import com.pausrq.cucharita.api.models.UpdateRecipeRequest
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

// ============================================
// INTERFAZ DE API
// ============================================

interface ApiService {

    // ========== AUTH ==========
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<MessageResponse>

    @GET("api/auth/me")
    suspend fun getCurrentUser(): Response<AuthResponse>

    // ========== RECIPES ==========
    @GET("api/recipes")
    suspend fun getAllPublicRecipes(): Response<List<RecipeResponse>>

    @GET("api/recipes/me")
    suspend fun getMyRecipes(): Response<List<RecipeResponse>>

    @GET("api/recipes/{id}")
    suspend fun getRecipe(@Path("id") id: Int): Response<RecipeResponse>

    @POST("api/recipes")
    suspend fun createRecipe(@Body request: CreateRecipeRequest): Response<RecipeResponse>

    @PUT("api/recipes/{id}")
    suspend fun updateRecipe(
        @Path("id") id: Int,
        @Body request: UpdateRecipeRequest
    ): Response<RecipeResponse>

    @DELETE("api/recipes/{id}")
    suspend fun deleteRecipe(@Path("id") id: Int): Response<MessageResponse>

    @Multipart
    @POST("api/recipes/upload")
    suspend fun uploadImage(@Part image: MultipartBody.Part): Response<Map<String, String>>
}

// ============================================
// CLIENTE RETROFIT
// ============================================

object RetrofitClient {

    // ⚠️ CAMBIAR ESTO A TU IP LOCAL O URL DE PRODUCCIÓN
    private const val BASE_URL = "http://10.0.2.2:3000/" // Para emulador Android
    // private const val BASE_URL = "http://192.168.1.X:3000/" // Para dispositivo físico
    // private const val BASE_URL = "https://tu-api.com/" // Para producción

    private var authToken: String? = null

    fun setAuthToken(token: String?) {
        authToken = token
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = okhttp3.Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()

        // Agregar token si existe
        authToken?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }

        chain.proceed(requestBuilder.build())
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: ApiService = retrofit.create(ApiService::class.java)
}

// ============================================
// REPOSITORIO
// ============================================

class ApiRepository {

    private val api = RetrofitClient.api

    // ========== AUTH ==========
    suspend fun register(email: String, password: String, username: String?, fullName: String?) =
        api.register(RegisterRequest(email, password, username, fullName))

    suspend fun login(email: String, password: String) =
        api.login(LoginRequest(email, password))

    suspend fun logout() = api.logout()

    suspend fun getCurrentUser() = api.getCurrentUser()

    // ========== RECIPES ==========
    suspend fun getAllPublicRecipes() = api.getAllPublicRecipes()

    suspend fun getMyRecipes() = api.getMyRecipes()

    suspend fun getRecipe(id: Int) = api.getRecipe(id)

    suspend fun createRecipe(
        title: String,
        description: String,
        ingredients: List<String>,
        steps: List<String>,
        imageUrl: String?,
        isFavorite: Boolean
    ) = api.createRecipe(
        CreateRecipeRequest(title, description, ingredients, steps, imageUrl, isFavorite)
    )

    suspend fun updateRecipe(
        id: Int,
        title: String,
        description: String,
        ingredients: List<String>,
        steps: List<String>,
        imageUrl: String?,
        isFavorite: Boolean
    ) = api.updateRecipe(
        id,
        UpdateRecipeRequest(title, description, ingredients, steps, imageUrl, isFavorite)
    )

    suspend fun deleteRecipe(id: Int) = api.deleteRecipe(id)

    suspend fun uploadImage(imagePart: MultipartBody.Part) = api.uploadImage(imagePart)
}