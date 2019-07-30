package com.omeric.android.latesttmdbmovies.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.omeric.android.latesttmdbmovies.R
import com.omeric.android.latesttmdbmovies.adapter.EndlessRecyclerViewScrollListener
import com.omeric.android.latesttmdbmovies.adapter.MoviesAdapter
import com.omeric.android.latesttmdbmovies.data.model.DiscoverMoviesModel
import com.omeric.android.latesttmdbmovies.data.model.MovieModel
import com.omeric.android.latesttmdbmovies.data.remote.TmdbApiService
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity()
{
    companion object
    {
        const val BASE_URL_API = "https://api.themoviedb.org/3/"
        const val BASE_URL_MOVIE_POSTER = "https://image.tmdb.org/t/p/w185"
        const val API_KEY = "1e0dcaa7e93980fb84e1d2430d01b887" //junk key
    }

    //init movies list
    var movies = arrayListOf<MovieModel>()

    /**
     * [CompositeDisposable] is a convenient class for bundling up multiple Disposables,
     * so that you can dispose them all with one method call on [CompositeDisposable.dispose].
     */
    private var compositeDisposable: CompositeDisposable? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private var totalPages = 0

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recycler_view_main_activity)
        /**
         * RecyclerView can perform several optimizations if it can know in advance that changes in adapter
         * content cannot change the size (dimensions) of the RecyclerView itself
         */
        recyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        progressBar = findViewById(R.id.progressbar_main_activity)
        loadNextDataFromApi(1)

        recyclerView.addOnScrollListener(object : EndlessRecyclerViewScrollListener(linearLayoutManager)
        {
            override fun onLoadMore(page: Int, recyclerView: RecyclerView)
            {
                // New data needs to be appended to the list
                if ((page + 1) <= totalPages)
                {
                    loadNextDataFromApi(page + 1)
                }
            }
        })
    }

    override fun onDestroy()
    {
        /** dispose all [Disposable]s */
        if (compositeDisposable?.isDisposed == false)
        {
            /*
             * The subscription can be disposed with a simple method call,
             * thus preventing those nasty situations when, for example,
             * rotating the device in the middle of a running background task causes a memory leak
             */
            compositeDisposable?.dispose()
            compositeDisposable = null
        }
        super.onDestroy()
    }

    /**
     * Create a CompositeDisposable object which acts as a container for disposables
     * (think Recycle Bin) and add our Disposable to it
     *
     * @param disposable the newly [Disposable] to add
     */
    fun add(disposable: Disposable)
    {
        if (compositeDisposable == null)
        {
            compositeDisposable = CompositeDisposable()
        }
        compositeDisposable?.add(disposable)
    }

    /**
     * Hiding the progress bar by inverting the visibility of [progressBar] and [recyclerView]
     */
    private fun hideProgressBar()
    {
        progressBar.visibility = View.INVISIBLE
        recyclerView.visibility = View.VISIBLE
    }

    /**
     * Showing the progress bar by inverting the visibility of [progressBar] and [recyclerView]
     */
    private fun showProgressBar()
    {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.INVISIBLE
    }

    /**
     * This method sends out a network request and appends new data items to the adapter, by:
     * 1. Sending an API request including an offset value (i.e `page`) as a query parameter.
     * 2. Deserializing and constructing new model objects from the API response
     * 3. Appending the new data objects to the existing set of items inside the array of items
     * 4. Notifying the adapter of the new items made with `notifyDataSetChanged()`
     *
     * @param page The page number to be loaded
     */
    fun loadNextDataFromApi(page: Int)
    {
        //get current date and
        val time = Calendar.getInstance().time
        //convert to the format yyyy-MM-dd
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val formattedDate = simpleDateFormat.format(time)

        // Trailing slash is needed
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL_API)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        // create an instance of the TmdbApiService
        val tmdbApiService = retrofit.create(TmdbApiService::class.java)
        //example: https://api.themoviedb.org/3/discover/movie?api_key=1e0dcaa7e93980fb84e1d2430d01b887&language=en-US&sort_by=release_date.desc&include_adult=false&include_video=false&page=1&primary_release_date.lte=2013-08-30
        tmdbApiService.getLatestMovies(API_KEY, "en-US", "release_date.desc", "false",
            "false", page, formattedDate)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<DiscoverMoviesModel>
            {
                override fun onSubscribe(disposable: Disposable)
                {
                    add(disposable)
                    showProgressBar()
                }

                // data is ready and we can update the UI
                override fun onSuccess(discoverMoviesModel : DiscoverMoviesModel)
                {
                    // for the first page we connect the adapter and the movies list
                    if (page == 1) {
                        totalPages = discoverMoviesModel.totalPages!!

                        movies = discoverMoviesModel.results!!

                        /** Hooking up [MoviesAdapter] and [recyclerView] */
                        recyclerView.adapter = MoviesAdapter(movies, R.layout.list_item_movie)
                        hideProgressBar()
                    }
                    else
                    {
                        // for any further pages, we append the data and notify the adapter on the change
                        movies.addAll(discoverMoviesModel.results!!)
                        recyclerView.adapter!!.notifyDataSetChanged()

                        hideProgressBar()
                    }
                }

                override fun onError(e: Throwable)
                {
                    // oops, we best show some error message
                    Toast.makeText(this@MainActivity, "Error connecting to TMDb", Toast.LENGTH_SHORT).show()
                    hideProgressBar()
                }
            })
    }
}
