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
import com.omeric.android.latesttmdbmovies.adapter.EndlessRecyclerViewScrollListener2


class MainActivity : AppCompatActivity()
{
    companion object
    {
        private val TAG = "gipsy:" + this::class.java.name
        const val BASE_URL_API = "https://api.themoviedb.org/3/"
        const val BASE_URL_MOVIE_POSTER = "https://image.tmdb.org/t/p/w185"
        const val API_KEY = "1e0dcaa7e93980fb84e1d2430d01b887" //junk key
    }

    //TODO - add auto-complete search bar
    //TODO - add infinite scrolling

    /**
     * [CompositeDisposable] is a convenient class for bundling up multiple Disposables,
     * so that you can dispose them all with one method call on [CompositeDisposable.dispose].
     */
    private var compositeDisposable: CompositeDisposable? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private var totalPages: Int = 0

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
        /**
         * RecyclerView can perform several optimizations if it can know in advance that changes in adapter
         * content cannot change the size (dimensions) of the RecyclerView itself
         */
        recyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        progressBar = findViewById(R.id.progressbar_main_activity)
//        ListView lvItems = (ListView) findViewById(R.id.lvItems);

        recyclerView.addOnScrollListener(object : EndlessRecyclerViewScrollListener2(linearLayoutManager)
        {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView)
            {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to your AdapterView
                if ((page + 1) <= totalPages)
                {
                    loadNextDataFromApi(page + 1);
                }
            }
        })

/*
        // Attach the listener to the AdapterView onCreate
        recyclerView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to your AdapterView
                loadNextDataFromApi(page);
                // or loadNextDataFromApi(totalItemsCount);
                return true; // ONLY if more data is actually being loaded; false otherwise.
            }
        });
*/

        // create an instance of the TmdbApiService
        val tmdbApiService = retrofit.create(TmdbApiService::class.java)
        val time = Calendar.getInstance().time
        Log.d(TAG, ":onCreate:: Current time => $time")

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val formattedDate = simpleDateFormat.format(time)
        Log.d(TAG, ":onCreate:: formattedDate = $formattedDate")

        //example: https://api.themoviedb.org/3/discover/movie?api_key=1e0dcaa7e93980fb84e1d2430d01b887&language=en-US&sort_by=release_date.desc&include_adult=false&include_video=false&page=1&primary_release_date.lte=2013-08-30
        tmdbApiService.getLatestMovies(API_KEY, "en-US", "release_date.desc", "false",
            "false", 1, formattedDate)
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
                    // Hooking up the Adapter and RecyclerView
                    recyclerView.adapter = MoviesAdapter(movies.results!!, R.layout.list_item_movie)
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

    // Append the next page of data into the adapter
    // This method probably sends out a network request and appends new data items to your adapter.
    fun loadNextDataFromApi(offset: Int )
    {
        // Send an API request to retrieve appropriate paginated data
        //  --> Send the request including an offset value (i.e `page`) as a query parameter.
        //  --> Deserialize and construct new model objects from the API response
        //  --> Append the new data objects to the existing set of items inside the array of items
        //  --> Notify the adapter of the new items made with `notifyDataSetChanged()`
    }
}
