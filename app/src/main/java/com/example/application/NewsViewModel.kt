package com.example.application

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class NewsViewModel : ViewModel() {
    private val allNews = mutableStateListOf<News>()
    val displayedNews = mutableStateListOf<News>()
    private var updateJob: Job? = null

    init {
        loadInitialNews()
        startNewsRotation()
    }

    private fun loadInitialNews() {
        val newsList = listOf(
            News(1, "Новость 1", "Содержание первой новости. Интересные события..."),
            News(2, "Новость 2", "Важное обновление в мире технологий..."),
            News(3, "Новость 3", "Спортивные достижения нашей команды..."),
            News(4, "Новость 4", "Культурные мероприятия этой недели..."),
            News(5, "Новость 5", "Экономические новости и прогнозы..."),
            News(6, "Новость 6", "Научное открытие последних дней..."),
            News(7, "Новость 7", "Погода на выходные обещает быть хорошей..."),
            News(8, "Новость 8", "Новые функции в приложении..."),
            News(9, "Новость 9", "Советы по продуктивности..."),
            News(10, "Новость 10", "Итоги месяца и планы на будущее...")
        )

        allNews.addAll(newsList)

        displayedNews.clear()
        repeat(4) {
            displayedNews.add(allNews.random())
        }
    }

    fun likeNews(index: Int) {
        if (index in 0 until displayedNews.size) {
            val news = displayedNews[index]
            val updatedNews = news.copy(likes = news.likes + 1)
            displayedNews[index] = updatedNews

            val allNewsIndex = allNews.indexOfFirst { it.id == news.id }
            if (allNewsIndex != -1) {
                allNews[allNewsIndex] = updatedNews
            }
        }
    }

    fun replaceRandomNews() {
        val indexToReplace = Random.nextInt(0, 4)
        val currentNews = displayedNews.map { it.id }
        val availableNews = allNews.filter { it.id !in currentNews }

        if (availableNews.isNotEmpty()) {
            val newNews = availableNews.random()
            displayedNews[indexToReplace] = newNews
        }
    }

    private fun startNewsRotation() {
        updateJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                delay(5000)
                replaceRandomNews()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        updateJob?.cancel()
    }
}