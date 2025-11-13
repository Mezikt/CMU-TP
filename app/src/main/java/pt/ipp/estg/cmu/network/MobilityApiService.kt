package pt.ipp.estg.cmu.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * API REST para obter dados de mobilidade
 * Exemplo: OpenStreetMap Overpass API ou similar
 * NOTA: Validar API específica com o docente
 */
interface MobilityApiService {

    /**
     * Exemplo: Buscar pontos de interesse (bicicletas, trotinetas, etc)
     * Usar API real validada com docente
     */
    @GET("api/interpreter")
    suspend fun getNearbyMobilityPoints(
        @Query("data") query: String
    ): Response<OverpassResponse>

    companion object {
        private const val BASE_URL = "https://overpass-api.de/"

        fun create(): MobilityApiService {
            val logger = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MobilityApiService::class.java)
        }
    }
}

// DTOs para resposta da API
data class OverpassResponse(
    val version: Double?,
    val elements: List<OverpassElement>
)

data class OverpassElement(
    val type: String?,
    val id: Long?,
    val lat: Double?,
    val lon: Double?,
    val tags: Map<String, String>?
)

/**
 * Constrói query Overpass para buscar estações de bicicletas/trotinetas
 */
fun buildOverpassQuery(lat: Double, lon: Double, radiusMeters: Int = 1000): String {
    return """
        [out:json];
        (
          node["amenity"="bicycle_rental"](around:$radiusMeters,$lat,$lon);
          node["amenity"="bicycle_parking"](around:$radiusMeters,$lat,$lon);
        );
        out body;
    """.trimIndent()
}

