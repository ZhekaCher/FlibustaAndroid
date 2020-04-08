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
import kotlinx.android.synthetic.main.bottom_navigation_search.*
import kz.gimmick.flibusta.api.Responses
import java.lang.Exception


class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

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
        searchPogressBar.isVisible = true
        val progressBarThread = Thread {
            try {
                for (i in 1..100) {
                    searchPogressBar.progress = i
                    Thread.sleep(200)
                }
            } catch (e: Exception) {
            }
        }
        progressBarThread.start()

        val title = bottomNavDrawerFragment.view?.findViewById<EditText>(R.id.bookTitleEditText)
        val authorName =
            bottomNavDrawerFragment.view?.findViewById<EditText>(R.id.authorNameEditText)
        val authorSurname =
            bottomNavDrawerFragment.view?.findViewById<EditText>(R.id.authorSurnameEditText)


        var requestString = "http://a91f3989.ngrok.io/api/widesearch?"
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
                progressBarThread.interrupt()
                searchPogressBar.isVisible = false
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

        fun download(context: Context, url: String) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val book = books[position]
            holder.itemView.cardView[0].setOnClickListener {


                val bottomBookDetailedFragment = BottomBookDetailed()
                val bookReadButton =
                    bottomBookDetailedFragment.view?.findViewById<Button>(R.id.bookReadButton)
                val bookFb2Button =
                    bottomBookDetailedFragment.view?.findViewById<Button>(R.id.bookFb2Button)
                val bookEpubButton =
                    bottomBookDetailedFragment.view?.findViewById<Button>(R.id.bookEpubButton)
                val bookMobiButton =
                    bottomBookDetailedFragment.view?.findViewById<Button>(R.id.bookMobiButton)
                bottomBookDetailedFragment.show(
                    supportFragmentManager,
                    bottomBookDetailedFragment.tag
                )

                val downloadLink = "https://flibusta.site/b/" + book.book_id + "/"
                bookReadButton?.setOnClickListener { download(holder.itemView.context, downloadLink+"") }
                bookFb2Button?.setOnClickListener { download(holder.itemView.context, downloadLink+"fb2") }
                bookEpubButton?.setOnClickListener { download(holder.itemView.context, downloadLink+"epub") }
                bookMobiButton?.setOnClickListener { download(holder.itemView.context, downloadLink+"mobi") }
            }

            val bookTitle = holder.itemView.findViewById<TextView>(R.id.bookCardViewBookTitle)
            val authors = holder.itemView.findViewById<TextView>(R.id.bookCardViewAuthors)
            val genres = holder.itemView.findViewById<TextView>(R.id.bookCardViewGenres)
            bookTitle.text = book.book_title
            authors.text = ""
            genres.text = ""
            if (book.authors != null) {
                for (author in book.authors!!)
                    authors.text = authors.text.toString() + author.author_fullname + ";\n"
                authors.text = authors.text.substring(0, authors.text.length - 2)
            } else
                authors.text = "-"
            if (book.genres != null) {
                for (genre in book.genres!!)
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


    class BottomBookDetailed : BottomSheetDialogFragment() {

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.bottom_book_detailed, container, false)
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
}

