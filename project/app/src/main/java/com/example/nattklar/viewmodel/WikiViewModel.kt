package com.example.nattklar.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.nattklar.model.dataobjects.WikiUiState
import com.example.nattklar.model.dataobjects.Article
import com.example.nattklar.model.dataprocessing.Articles
import kotlinx.coroutines.flow.update

/**
 * Responsible for holding and managing the UI-related data to be displayed on the wiki screen.
 */
class WikiViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(WikiUiState(listOf(), listOf(), null))
    val uiState: StateFlow<WikiUiState> = _uiState.asStateFlow()

    /**
     * Loads wiki articles.
     */
    fun setArticles() {
        // set articles
        uiState.value.articles = Articles.getArticles()
    }

    /**
     * @return a list of articles that have the same category as the [category] parameter.
     */
    private fun filterArticlesByTopic(articles: List<Article>, category: String) : List<Article> {
        return articles.filter{ it.category?.equals(category, ignoreCase = true) == true }
    }

    /**
     * Updates the [_uiState] with a new given [category]
     */
    fun setNewArticleCategory(category: String) {
        val articlesInCategory = filterArticlesByTopic(uiState.value.articles, category)

        _uiState.update { currentState ->
            currentState.copy(categorisedArticles = articlesInCategory)
        }
    }

    /**
     * Updates the [_uiState] by setting the currentArticle to [article]
     */
    fun setCurrentArticle(article: Article) {
        _uiState.update { currentState ->
            currentState.copy(currentArticle = article)
        }
    }
}