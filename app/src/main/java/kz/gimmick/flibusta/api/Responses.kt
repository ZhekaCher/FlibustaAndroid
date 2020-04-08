package kz.gimmick.flibusta.api

class Responses {
    data class SearchResponse(
        val books: List<Book>
    )

    data class Book(
        val book_id: Int,
        val book_title: String,
        val genres: List<Genre>?,
        val translators: List<Translator>?,
        val authors: List<Author>?,
        val series: List<Series>?,
        val size: String?
    )

    data class Author(
        val author_id: Int,
        val author_fullname: String
    )

    data class Translator(
        val translator_id: Int,
        val translator_fullname: String
    )

    data class Series(
        val series_id: Int,
        val series_name: String
    )

    data class Genre(
        val genre_id: Int,
        val genre_title: String,
        val genre_code: String

    )
}