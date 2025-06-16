package com.sukuna.animestudio.domain.model

data class Anime(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val genre: List<AnimeGenre> = emptyList(),
    val rating: Double = 0.0,
    val episodes: Int = 12,
    val status: Status = Status.NOT_STARTED,
    val animeStatus: AnimeStatus = AnimeStatus.IN_PROGRESS,
    val isFavorite: Boolean = false,
    val isWatched: Boolean = false,
    val isInWatchlist: Boolean = false,
    val releaseDate: String = "",
    )

enum class Status {
    NOT_STARTED,
    WATCHING,
    COMPLETED,
    ON_HOLD,
    DROPPED
}

enum class AnimeStatus {
    IN_PROGRESS,
    COMPLETED,
    SOON_ARRIVING,
}

enum class AnimeGenre {
    ACTION,
    ADVENTURE,
    COMEDY,
    DRAMA,
    FANTASY,
    SUPERNATURAL,
    SLICE_OF_LIFE,
    MYSTERY,
    HORROR,
    PSYCHOLOGICAL,
    ROMANCE,
    SCI_FI,
    SPORTS,
    THRILLER,
    MECHA,
    MAGIC,
    MUSIC,
    HISTORICAL,
    PARODY,
    GAME,
    SCHOOL,
    DEMONS,
    VAMPIRE,
    HAREM,
    ECCHI,
    SHOUNEN,
    SHOUJO,
    JOSEI,
    SEINEN,
    MILITARY,
    POLICE,
    SPACE,
    YAOI,
    YURI,
    MARTIAL_ARTS,
    KIDS,
    DEMENTIA,
    CARS
}
