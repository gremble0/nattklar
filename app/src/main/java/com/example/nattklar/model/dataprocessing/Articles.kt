package com.example.nattklar.model.dataprocessing

import android.content.Context
import com.example.nattklar.model.DataNavigator
import com.example.nattklar.model.JsonAsset
import com.example.nattklar.model.dataobjects.Article
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Responsible for parsing the json files where the articles are stored.
 */
object Articles {
    private val json = Json { ignoreUnknownKeys = true }
    private var articles: List<Article> = listOf()

    /**
     * Loads wiki articles from the [context] of a specific screen. This could be the homescreen
     * or the wikiscreen.
     *
     * @return list of all the [Article]s in the article database
     */
    suspend fun loadArticles(context: Context) : List<Article> {
        // ensure data is not being loaded if it already has been
        if (articles.isEmpty()) {
            val jsonString = DataNavigator.readJsonAsset(context, JsonAsset.WikiText)
            articles = json.decodeFromString(jsonString)
        }
        return articles
    }

    fun getArticles() : List<Article> {
        return articles
    }
}