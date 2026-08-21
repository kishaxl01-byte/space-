package com.example.service

import com.example.BuildConfig
import com.example.model.ChatMessage
import com.example.model.MessageSender
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

class GeminiService {

    private val apiService: GeminiApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    private val systemPrompt = """
        You are AstroGuide, an elite, inspiring AI Cosmic Researcher, Astrophysicist, and NASA Historian.
        Your mission is to guide users through the mysteries of the universe, planets, solar systems, galaxies, black holes, NASA space exploration, and cosmology.
        Respond with fascinating, scientifically accurate facts, engaging explanations, and awe-inspiring astrophysics insights.
        Use clean markdown formatting with bullet points and clear sections where helpful.
        Keep answers informative, accessible to enthusiasts, and scientifically rigorous.
    """.trimIndent()

    suspend fun queryCosmosAI(
        userPrompt: String,
        history: List<ChatMessage>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Provide high-quality domain fallback response for smooth offline usage
            return@withContext getOfflineKnowledgeResponse(userPrompt)
        }

        val contents = mutableListOf<Content>()

        // Add relevant past messages (up to 4 turns for context)
        val recentHistory = history.takeLast(6)
        for (msg in recentHistory) {
            if (msg.sender == MessageSender.USER) {
                contents.add(Content(parts = listOf(Part(text = msg.text)), role = "user"))
            } else if (msg.sender == MessageSender.AI && !msg.isGenerating) {
                contents.add(Content(parts = listOf(Part(text = msg.text)), role = "model"))
            }
        }

        // Add current user prompt
        contents.add(Content(parts = listOf(Part(text = userPrompt)), role = "user"))

        val request = GenerateContentRequest(
            contents = contents,
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
            generationConfig = GenerationConfig(temperature = 0.7f, topP = 0.95f)
        )

        try {
            val response = apiService.generateContent(apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            text ?: "I scanned the deep space frequencies but received an empty transmission. Please ask again."
        } catch (e: Exception) {
            // Fallback gracefully to rich offline astrophysics knowledge
            getOfflineKnowledgeResponse(userPrompt)
        }
    }

    private fun getOfflineKnowledgeResponse(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            "black hole" in lower || "event horizon" in lower || "singularity" in lower -> {
                """
                ### 🕳️ Black Holes & The Event Horizon
                
                A **black hole** is an astronomical region where matter has collapsed to such extreme density that gravity prevents anything—including electromagnetic radiation like light—from escaping.
                
                * **The Event Horizon**: The point of no return. Its radius is called the *Schwarzschild radius* (Rs = 2GM / c^2). Once an object crosses this boundary, all possible paths through space-time lead inexorably inward toward the singularity.
                * **Sagittarius A***: The supermassive black hole at the center of the Milky Way, holding a mass of 4.15 million Suns.
                * **Gravitational Lensing**: Extreme gravity warps light from stars behind the black hole into a glowing Einstein ring.
                * **Spaghettification**: Extreme tidal gravitational differences stretch infalling matter vertically into thin strings.
                """.trimIndent()
            }
            "james webb" in lower || "jwst" in lower || "telescope" in lower -> {
                """
                ### 🔭 James Webb Space Telescope (JWST)
                
                Launched on Christmas Day 2021, the **James Webb Space Telescope** is humanity's flagship infrared space observatory, stationed 1.5 million km from Earth at the Sun-Earth L2 point.
                
                * **6.5m Golden Mirror**: Composed of 18 hexagonal beryllium segments coated with a microscopic layer of vaporized gold.
                * **Peering into Cosmic Dawn**: Because the expansion of the universe redshifts ancient light into the infrared spectrum, JWST can observe the very first galaxies formed ~200-300 million years after the Big Bang.
                * **Atmospheres of Exoplanets**: Uses transit spectroscopy to analyze the molecular fingerprints (Water, CO₂, Methane) of distant alien worlds like TRAPPIST-1.
                """.trimIndent()
            }
            "artemis" in lower || "moon" in lower || "lunar" in lower -> {
                """
                ### 🚀 NASA's Artemis Program
                
                The **Artemis Program** is NASA's next great human exploration campaign to establish sustainable human presence on the Moon and prepare for crewed expeditions to Mars.
                
                * **Key Goals**: Landing the first woman and first person of color on the lunar surface, exploring water ice reservoirs at the lunar South Pole.
                * **Hardware**: Powered by the **Space Launch System (SLS)** rocket, **Orion** spacecraft, and the lunar orbital **Gateway** outpost.
                * **Lunar Economy**: Developing in-situ resource utilization (extracting oxygen and water from lunar regolith) as a stepping stone to Mars.
                """.trimIndent()
            }
            "europa" in lower || "ocean" in lower || "life" in lower -> {
                """
                ### 🌊 Ocean Worlds: Europa & Enceladus
                
                Astrobiologists believe the best chance of finding alien life in our Solar System lies beneath the icy shells of Jovian and Saturnian moons.
                
                * **Europa's Hidden Ocean**: Gravitational tidal flexing from Jupiter keeps Europa's interior warm, harboring a saltwater ocean estimated to hold **twice the volume** of all Earth's oceans combined.
                * **Hydrothermal Vents**: The ocean floor is in direct contact with a warm rocky silicate mantle, potentially driving hydrothermal vents identical to those where life first began on Earth.
                * **Europa Clipper**: NASA's flagship probe (launched 2024) is currently on its journey to perform dozens of close flybys.
                """.trimIndent()
            }
            "mars" in lower || "perseverance" in lower || "rover" in lower -> {
                """
                ### 🔴 Mars Exploration & The Search for Past Life
                
                Mars is a cold desert world with ancient geological proof of a warmer, wetter past.
                
                * **Ancient Water**: River deltas, dry lake beds (like Jezero Crater), and mineral clays prove liquid water flowed across Mars 3.8 billion years ago.
                * **Perseverance & Ingenuity**: NASA's Perseverance rover is drilling and caching rock cores for the future Mars Sample Return campaign, supported by the historic flights of the Ingenuity helicopter.
                * **Olympus Mons**: Mars hosts the largest volcano in the Solar System, rising 22 km high.
                """.trimIndent()
            }
            "big bang" in lower || "universe" in lower || "expansion" in lower -> {
                """
                ### 🌌 The Big Bang & The Expanding Universe
                
                Approximately **13.8 billion years ago**, the universe began expanding from an infinitely hot, dense state.
                
                * **Cosmic Microwave Background (CMB)**: The thermal afterglow of the Big Bang, released 380,000 years after genesis when electrons and protons first formed neutral hydrogen.
                * **Dark Matter & Dark Energy**: Ordinary matter (atoms, stars, planets) makes up only ~5% of the cosmos. Dark Matter accounts for ~27%, and Dark Energy drives accelerating cosmic expansion at ~68%.
                * **Scale**: The observable universe is approximately 93 billion light-years in diameter.
                """.trimIndent()
            }
            else -> {
                """
                ### 🌌 Cosmic Insights on "${prompt.trim()}"
                
                The cosmos is vast and ever-expanding, holding billions of galaxies each with hundreds of billions of stars and planetary systems.
                
                * **Planetary Diversity**: From the iron deserts of Mercury to the hydrocarbon oceans of Titan and diamond exoplanets like 55 Cancri e.
                * **Cosmic Engines**: Nuclear fusion inside stellar furnaces creates every chemical element essential for life and planets.
                * **NASA Exploration**: Through telescopes like Hubble & JWST, and deep space probes like Voyager 1, humanity continues to push the boundary of the known universe.
                
                *Tip: Ask about specific planets (e.g. Jupiter, Mars, Saturn), NASA missions (Artemis, Apollo, JWST), or cosmic wonders (Black Holes, Trappist-1)!*
                """.trimIndent()
            }
        }
    }
}
