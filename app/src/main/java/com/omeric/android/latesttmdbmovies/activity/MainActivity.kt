package com.omeric.android.latesttmdbmovies.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.util.Log
import com.omeric.android.latesttmdbmovies.data.remote.TmdbApiService
import io.reactivex.disposables.Disposable
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.omeric.android.latesttmdbmovies.R
import com.omeric.android.latesttmdbmovies.adapter.MoviesAdapter
import com.omeric.android.latesttmdbmovies.data.model.DiscoverMoviesModel
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity()
{
    companion object
    {
        private val TAG = "gipsy:" + this::class.java.name
//        const val BASE_URL_API = "https://api.github.com/"
        const val BASE_URL_API = "https://api.themoviedb.org/3/"
        const val BASE_URL_MOVIE_POSTER = "https://image.tmdb.org/t/p/w185"
        const val API_KEY = "1e0dcaa7e93980fb84e1d2430d01b887" //junk key
    }

    /**
     * [CompositeDisposable] is a convenient class for bundling up multiple Disposables,
     * so that you can dispose them all with one method call on [CompositeDisposable.dispose].
     */
    private var compositeDisposable: CompositeDisposable? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        Log.d(TAG, ":onCreate")
        setContentView(R.layout.activity_main)

        // Trailing slash is needed
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL_API)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        recyclerView = findViewById(R.id.recycler_view_main_activity)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        progressBar = findViewById(R.id.progressbar_main_activity)

        // create an instance of the TmdbApiService
        val tmdbApiService = retrofit.create(TmdbApiService::class.java)
        val time = Calendar.getInstance().time
        Log.d(TAG, ":onCreate:: Current time => $time")

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val formattedDate = simpleDateFormat.format(time)
        Log.d(TAG, ":onCreate:: formattedDate = $formattedDate")

        //TODO - change to simpler form of function
        //example: https://api.themoviedb.org/3/discover/movie?api_key=1e0dcaa7e93980fb84e1d2430d01b887&language=en-US&sort_by=release_date.desc&include_adult=false&include_video=false&page=1&primary_release_date.lte=2013-08-30
        tmdbApiService.getLatestMovies(API_KEY, "en-US", "release_date.desc", "false",
            "false", 1, formattedDate)
/*
        tmdbApiService.getMoviesFromDiscover(mapOf(
            "api_key" to API_KEY,
            "language" to "en-US",
            "include_adult" to "false",
            "sort_by" to "release_date.desc",
            "include_video" to "false.desc",
            "page" to "1",
            "primary_release_date.lte" to "2013-08-30"))
*/
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<DiscoverMoviesModel>
            {
                override fun onSubscribe(disposable: Disposable)
                {
                    Log.d(TAG, "moviesSingle::onSubscribe")
                    add(disposable)
                    showProgressBar()
                }

                override fun onSuccess(movies : DiscoverMoviesModel)
                {
                    // data is ready and we can update the UI
                    Log.d(TAG, "moviesSingle::onSuccess: Number of movies received:  ${movies.totalResults}")
                    recyclerView.adapter = MoviesAdapter(movies.results!!, R.layout.list_item_movie, applicationContext)
                    hideProgressBar()
                }

                override fun onError(e: Throwable)
                {
                    // oops, we best show some error message
                    Log.e(TAG, "moviesSingle::onError: $e")
                    Toast.makeText(this@MainActivity, "Error connecting to TMDb", Toast.LENGTH_SHORT).show()
                    hideProgressBar()
                }
            })
    }

    override fun onDestroy()
    {
        Log.d(TAG, ":onDestroy")
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
        Log.d(TAG, ":add")
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
        Log.d(TAG, "::hideProgressBar:")
        progressBar.visibility = View.INVISIBLE
        recyclerView.visibility = View.VISIBLE
    }

    /**
     * Showing the progress bar by inverting the visibility of [progressBar] and [recyclerView]
     */
    private fun showProgressBar()
    {
        Log.d(TAG, "::hideProgressBar:")
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.INVISIBLE
    }
}
