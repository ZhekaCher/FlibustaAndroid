package kz.gimmick.flibusta

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.children
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.core.view.size
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.apptakk.http_request.HttpRequest
import com.apptakk.http_request.HttpRequestTask
import com.apptakk.http_request.HttpResponse
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator
import kotlinx.android.synthetic.main.book_card_view.view.*
import kotlinx.android.synthetic.main.bottom_book_detailed.*
import kotlinx.android.synthetic.main.bottom_navigation_search.*
import kotlinx.android.synthetic.main.bottom_navigation_search.search_navigation_view
import kz.gimmick.flibusta.api.Responses
import java.lang.Exception


class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private val server = "http://75485940.ngrok.io"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewManager = LinearLayoutManager(this)

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView).apply {
            setHasFixedSize(true)
            layoutManager = viewManager

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.bottomappbar_menu, menu)
        return true
    }

    private val bottomNavDrawerFragment: BottomNavigationSearch = BottomNavigationSearch()
    fun openBottomNavigationSearch(view: View) {
        bottomNavDrawerFragment.show(supportFragmentManager, bottomNavDrawerFragment.tag)
    }

    fun search(view: View) {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val searchPogressBar = findViewById<ProgressBar>(R.id.searchProgressBar)
        val mainConstraintLayout = findViewById<ConstraintLayout>(R.id.mainConstraintLayout)
        val alertTextView = findViewById<TextView>(R.id.alertTextView)
        recyclerView.adapter = null
        alertTextView.isVisible = false
        alertTextView.text = ""
        mainConstraintLayout.isVisible = true


        searchPogressBar.isVisible = true
//        val progressBarThread = Thread {
//            try {
//                for (i in 1..100) {
//                    searchPogressBar.progress = i
//                    Thread.sleep(200)
//                }
//            } catch (e: Exception) {
//            }
//        }
//        progressBarThread.start()

        val title = bottomNavDrawerFragment.view?.findViewById<EditText>(R.id.bookTitleEditText)
        val authorName =
            bottomNavDrawerFragment.view?.findViewById<EditText>(R.id.authorNameEditText)
        val authorSurname =
            bottomNavDrawerFragment.view?.findViewById<EditText>(R.id.authorSurnameEditText)


        var requestString = server + "/api/widesearch?"
        if (title?.text?.isNotEmpty()!!)
            requestString += "book_title=" + title?.text + "&"
        if (authorName?.text?.isNotEmpty()!!)
            requestString += "author_fname=" + authorName?.text + "&"
        if (authorSurname?.text?.isNotEmpty()!!)
            requestString += "author_lname=" + authorSurname?.text + "&"

        supportFragmentManager.beginTransaction().remove(bottomNavDrawerFragment).commit()
        HttpRequestTask(
            HttpRequest(requestString, HttpRequest.GET),
            HttpRequest.Handler { res ->


                val response = Gson().fromJson<Responses.SearchResponse>(
                    res.body,
                    Responses.SearchResponse::class.java
                )
//                progressBarThread.interrupt()
                searchPogressBar.isVisible = false
                if (response.books != null) {
                    mainConstraintLayout.isVisible = false
                    recyclerView.adapter = AlphaInAnimationAdapter(
                        RecyclerViewAdapter(
                            response.books,
                            supportFragmentManager
                        )
                    ).apply {
                        // Change the durations.
                        setDuration(300)
                        // Disable the first scroll mode.
                        setFirstOnly(false)
                    }
                    recyclerView.adapter = ScaleInAnimationAdapter(recyclerView.adapter).apply {
                        // Change the durations.
                        setDuration(300)
                        // Disable the first scroll mode.
                        setFirstOnly(false)
                    }
                } else {
                    mainConstraintLayout.isVisible = true
                    alertTextView.isVisible = true
                    alertTextView.text = "THE SEARCH DIDN'T FIND ANY INFORMATION"
                }

            }).execute()

    }

    class RecyclerViewAdapter(
        private val books: List<Responses.Book>,
        private val supportFragmentManager: FragmentManager
    ) :
        RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>() {
        class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): MyViewHolder {
            val bookCardView = LayoutInflater.from(parent.context)
                .inflate(R.layout.book_card_view, parent, false) as CardView
            return MyViewHolder(bookCardView)
        }


        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val book = books[position]
            holder.itemView.cardView[0].setOnClickListener {


                val bottomBookDetailedFragment = BottomBookDetailed(book)
                bottomBookDetailedFragment.show(
                    supportFragmentManager,
                    bottomBookDetailedFragment.tag
                )
            }

            val bookTitle = holder.itemView.findViewById<TextView>(R.id.bookCardViewBookTitle)
            val authors = holder.itemView.findViewById<TextView>(R.id.bookCardViewAuthors)
            val genres = holder.itemView.findViewById<TextView>(R.id.bookCardViewGenres)
            bookTitle.text = book.book_title
            authors.text = ""
            genres.text = ""
            if (book.authors != null) {
                for (author in book.authors)
                    authors.text = authors.text.toString() + author.author_fullname + ";\n"
                authors.text = authors.text.substring(0, authors.text.length - 1)
            } else
                authors.text = "-"
            if (book.genres != null) {
                for (genre in book.genres)
                    genres.text = genres.text.toString() + genre.genre_title + ";\n"
                genres.text = genres.text.substring(0, genres.text.length - 1)
            } else
                genres.text = "-"
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = books.size
    }

    class BottomNavigationSearch : BottomSheetDialogFragment() {

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.bottom_navigation_search, container, false)
        }

        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)

            search_navigation_view.setNavigationItemSelectedListener { menuItem ->
                // Bottom Navigation Drawer menu item clicks
                when (menuItem.itemId) {
                    // R.id.nav1 -> context!!.toast(getString(R.string.nav1_clicked))
                }
                // Add code here to update the UI based on the item selected
                // For example, swap UI fragments here
                true
            }
        }
    }


    class BottomBookDetailed(book: Responses.Book) : BottomSheetDialogFragment() {

        private val book = book
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.bottom_book_detailed, container, false)
        }

        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            val bookReadButton = bottom_book_detailed.findViewById<Button>(R.id.bookReadButton)
            val bookFb2Button = bottom_book_detailed.findViewById<Button>(R.id.bookFb2Button)
            val bookEpubButton = bottom_book_detailed.findViewById<Button>(R.id.bookEpubButton)
            val bookMobiButton = bottom_book_detailed.findViewById<Button>(R.id.bookMobiButton)
            val bookTitle = bottom_book_detailed.findViewById<TextView>(R.id.bookDetailedTitle)
            val bookRating = bottom_book_detailed.findViewById<TextView>(R.id.bookDetailedRating)
            val bookAuthors = bottom_book_detailed.findViewById<TextView>(R.id.bookDetailedAuthors)
            val bookGenres = bottom_book_detailed.findViewById<TextView>(R.id.bookDetailedGenres)
            val bookTranslators =
                bottom_book_detailed.findViewById<TextView>(R.id.bookDetailedTranslators)
            val bookSeries = bottom_book_detailed.findViewById<TextView>(R.id.bookDetailedSeries)
            val bookSize = bottom_book_detailed.findViewById<TextView>(R.id.bookDetailedSize)

            bookTitle.text = book.book_title
            if (book.size != null)
                bookSize.text = book.size
            else
                bookSize.text = "-"

            if (book.rating != null)
                bookRating.text = book.rating
            else
                bookRating.text = "-"

            if (book.authors != null) {
                for (author in book.authors)
                    bookAuthors.text = bookAuthors.text.toString() + author.author_fullname + ";\n"
                bookAuthors.text = bookAuthors.text.substring(0, bookAuthors.text.length - 1)
            } else
                bookAuthors.text = "-"

            if (book.genres != null) {
                for (genre in book.genres)
                    bookGenres.text = bookGenres.text.toString() + genre.genre_title + ";\n"
                bookGenres.text = bookGenres.text.substring(0, bookGenres.text.length - 1)
            } else
                bookGenres.text = "-"

            if (book.series != null) {
                for (series in book.series)
                    bookSeries.text = bookSeries.text.toString() + series.series_name + ";\n"
                bookSeries.text = bookSeries.text.substring(0, bookSeries.text.length - 1)
            } else
                bookSeries.text = "-"

            if (book.translators != null) {
                for (translator in book.translators)
                    bookTranslators.text =
                        bookTranslators.text.toString() + translator.translator_fullname + ";\n"
                bookTranslators.text =
                    bookTranslators.text.substring(0, bookTranslators.text.length - 1)
            } else
                bookTranslators.text = "-"


            val downloadLink = "https://flibusta.site/b/" + book.book_id + "/"
            bookReadButton.setOnClickListener {
                download(
                    bottom_book_detailed.context,
                    downloadLink + "read"
                )
            }
            bookFb2Button.setOnClickListener {
                download(
                    bottom_book_detailed.context,
                    downloadLink + "fb2"
                )
            }
            bookEpubButton.setOnClickListener {
                download(
                    bottom_book_detailed.context,
                    downloadLink + "epub"
                )
            }
            bookMobiButton.setOnClickListener {
                download(
                    bottom_book_detailed.context,
                    downloadLink + "mobi"
                )
            }
        }

        fun download(context: Context, url: String) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }
    }
}

