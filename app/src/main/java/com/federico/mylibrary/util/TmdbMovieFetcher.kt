package com.federico.mylibrary

import android.util.Log
import com.federico.mylibrary.BuildConfig
import com.federico.mylibrary.util.Logger
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.Locale

@Serializable
data class TmdbSearchResponse(val results: List<TmdbMovieResult>)

@Serializable
data class TmdbMovieResult(
    val id: Int,
    val title: String,
    @SerialName("original_title") val originalTitle: String,
    val overview: String,
    @SerialName("release_date") val releaseDate: String,
    @SerialName("poster_path") val posterPath: String? = null
)

@Serializable
data class TmdbDetailsResponse(
    val runtime: Int? = null,
    val genres: List<Genre> = emptyList(),
    @SerialName("production_companies") val productionCompanies: List<Company> = emptyList(),
    val credits: Credits = Credits(),
    @SerialName("spoken_languages") val spokenLanguages: List<Language> = emptyList()
)

@Serializable data class Genre(val name: String)
@Serializable data class Company(val name: String)
@Serializable data class Language(val name: String)
@Serializable data class Credits(
    val cast: List<Person> = emptyList(),
    val crew: List<Person> = emptyList()
)

@Serializable data class Person(val name: String, val job: String = "")

data class MovieInfo(
    val title: String = "",
    val originalTitle: String = "",
    val description: String = "",
    val publishDate: String = "",
    val coverUrl: String = "",
    val genre: String = "",
    val duration: Int = 0,
    val productionCompany: String = "",
    val director: String = "",
    val cast: String = "",
    val language: String = ""
)

suspend fun fetchMovieInfoFromTmdb(title: String): MovieInfo? {
    val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    return try {
        val searchResponse: TmdbSearchResponse = client.get("https://api.themoviedb.org/3/search/movie") {
            parameter("api_key", BuildConfig.TMDB_API_KEY)
            parameter("query", title)
            parameter("language", "it-IT")
        }.body()

        val movie = searchResponse.results.firstOrNull() ?: return null

        val details: TmdbDetailsResponse = client.get("https://api.themoviedb.org/3/movie/${movie.id}") {
            parameter("api_key", BuildConfig.TMDB_API_KEY)
            parameter("append_to_response", "credits")
            parameter("language", "it-IT")
        }.body()

        val director = details.credits.crew.firstOrNull { it.job.lowercase(Locale.ROOT) == "director" }?.name.orEmpty()
        val cast = details.credits.cast.take(3).joinToString(", ") { it.name }
        val genre = details.genres.firstOrNull()?.name.orEmpty()
        val company = details.productionCompanies.firstOrNull()?.name.orEmpty()
        val language = details.spokenLanguages.firstOrNull()?.name.orEmpty()

        MovieInfo(
            title = movie.title,
            originalTitle = movie.originalTitle,
            description = movie.overview,
            publishDate = movie.releaseDate,
            coverUrl = movie.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" } ?: "",
            genre = genre,
            duration = details.runtime ?: 0,
            productionCompany = company,
            director = director,
            cast = cast,
            language = language
        )
    } catch (e: Exception) {
        Logger.e("TMDB_FETCH", "Errore durante il fetch da TMDb: ${e.message}", e)
        null
    } finally {
        client.close()
    }
}
