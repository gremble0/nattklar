package com.example.myapplication

import com.example.nattklar.model.dataobjects.Article
import com.example.myapplication.viewmodel.WikiViewModel
import com.google.gson.reflect.TypeToken

import org.junit.Assert.*
import org.junit.Test
import java.lang.reflect.ParameterizedType

class WikiArticlesUnitTest {

    // TODO: skille på store/små bokstaver
    @Test
    fun filterArticlesByTopic_isCorrect() {
        // The lines below are to omit the private-encapsulation
        val methodName = "filterArticlesByTopic"

        val listType = object : TypeToken<List<Article>>() {}.type
        val classType = (listType as ParameterizedType).rawType as Class<*>
        val parameterTypes = arrayOf<Class<*>>(
            classType,
            String::class.java,
        )

        val method = WikiViewModel::class.java.getDeclaredMethod(methodName, *parameterTypes)
        method.isAccessible = true

        // Arrange
        val articles = listOf(
            Article(
                1,
                "title1",
                "cons1",
                "body1",
                "katter"
            ),
            Article(
                2,
                "title2",
                "cons2",
                "body2",
                "hunder"
            ),
            Article(
                3,
                "title3",
                "cons3",
                "body3",
                "katter"
            ),
            Article(
                4,
                "title4",
                "cons4",
                "body4",
                "griser"
            ),
            Article(
                5,
                "title5",
                "cons5",
                "body5",
                "griser"
            ),
            Article(
                6,
                "title6",
                "cons6",
                "body6",
                "suppe"
            ),
            Article(
                7,
                "title7",
                "cons7",
                "body7",
                "hunder"
            ),
            Article(
                8,
                "title8",
                "cons8",
                "body8",
                "griser"
            )
        )

        val filters = listOf(
            "hunder",
            "katter",
            "griser",
            "suppe",
            "oppskrifter"
        )

        val expectedArticleIDs = listOf(
            listOf(2, 7),      // hunder
            listOf(1, 3),      // katter
            listOf(4, 5, 8),   // griser
            listOf(6),         // suppe
            listOf()           // oppskrifter
        )


        // Act
        val result = mutableListOf<List<Int>>()
        filters.forEach {
            val ids = mutableListOf<Int>()
            val list: List<Article> = method.invoke(WikiViewModel(), articles, it) as List<Article>
            list.forEach { article ->
                ids.add(article.id)
            }
            result.add(ids)
        }

        // Assert
        result.zip(expectedArticleIDs).forEach {
            assertEquals(it.first, it.second)
        }
    }
}