package com.gamevault.domain.model

data class Game(
    val id: Long,
    val name: String,
    val coverUrl: String?,
    val rating: Double?,
    val releaseDate: Long?,
    val genres: List<String> = emptyList(),
    val platforms: List<String> = emptyList()
)