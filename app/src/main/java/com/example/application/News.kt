package com.example.application

data class News(
    val id: Int,
    val title: String,
    val content: String,
    val likes: Int = 0,
)