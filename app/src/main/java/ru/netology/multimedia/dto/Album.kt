package ru.netology.multimedia.dto

data class Album(
    val id: Long = 1,
    val title: String = "MySong",
    val subtitle: String = "www.111.ru",
    val artist: String = "Me",
    val published: String = "2026",
    val genre: String = "rock",
    val tracks: List<Track>,
)

data class Track(
    val id: Long = 1,
    val file: String = "0.mp3",
    var isLiked: Boolean = false,
    var isPlaying: Boolean = false,
)
