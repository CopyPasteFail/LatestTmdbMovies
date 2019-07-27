package com.omeric.android.latesttmdbmovies.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.omeric.android.latesttmdbmovies.R
import com.omeric.android.latesttmdbmovies.data.model.MovieModel
import com.omeric.android.latesttmdbmovies.data.remote.TmdbApiService
import com.squareup.picasso.Picasso
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import io.reactivex.disposables.CompositeDisposable
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

/**
 * This activity shows the details of the movie when the user clicks on it from the main activity
 */
class DetailsActivity : AppCompatActivity()
{
    companion object
    {
        private val TAG = "gipsy:" + this::class.java.name
        const val INTENT_MOVIE_ID = "USERNAME_ID"
        const val INTENT_REPOSITORY_NAME_ID = "REPOSITORY_NAME_ID"
    }

    private var compositeDisposable: CompositeDisposable? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var detailsLayout: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        Log.d(TAG, ":onCreate")
        val movieId = intent.getStringExtra(INTENT_MOVIE_ID)
        Log.d(TAG, ":onCreate: GET /movie/$movieId/") //TODO - change this

        val movieTitle: TextView = findViewById(R.id.TextVw_movie_details_title)
        val movieOverview: TextView = findViewById(R.id.TextVw_movie_details_overview)
        val popularity: TextView = findViewById(R.id.TextVw_movie_details_popularity)
        val voteAverage: TextView = findViewById(R.id.TextVw_movie_details_vote_average)
        val voteCount: TextView = findViewById(R.id.TextVw_movie_details_vote_count)
//        val openIssues: TextView = findViewById(R.id.TextVw_repo_details_open_issues)
        val posterImage: ImageView = findViewById(R.id.ImgVw_movie_details_poster_image)
        val homepageUrl: TextView = findViewById(R.id.TextVw_movie_homepage_url)
//        val usernameTextView: TextView = findViewById(R.id.TextVw_repo_details_username)
        val releaseDate: TextView = findViewById(R.id.TextVw_movie_details_release_date)
//        val updatedTime: TextView = findViewById(R.id.TextVw_repo_details_updated_time)

        progressBar = findViewById(R.id.progressbar_movie_details)
        detailsLayout = findViewById(R.id.movie_details_layout)

        // Trailing slash is needed
        val retrofit = Retrofit.Builder()
            .baseUrl(MainActivity.BASE_URL_API)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()


        // create an instance of the TmdbApiService
        val tmtbApiService = retrofit.create(TmdbApiService::class.java)

        // make a request by calling the corresponding method
        /*
         * Single allows us to recieve a single set of data from the API, do some stuff with it in the background, and,
         * when done, present it to the user 
         * Internally, it is based on the observer pattern with data being pushed to interested observers
         * at the moment of subscription
         */

        //https://api.github.com/repos/:owner/:repo
        tmtbApiService.getMovieDetails(movieId, MainActivity.API_KEY)
            /*
             * With subscribeOn() we tell RxJava to do all the work on the background(io) thread.
             * When the work is done and our data is ready, observeOn() ensures that onSuccess() or onError()
             * are called on the main thread
             */
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

            /*
            * To receive the data we only have to subscribe to a Single,
            * calling the subscribe() method on it and passing a SingleObserver as an argument.
            * SingleObserver is an interface containing 3 methods.
            * By subscribing we ensure that the data is pushed when ready,
            * and is passed to us in onSuccess() method if request was successfully completed 
            *  if not, onError() is invoked, enabling us to deal with the exception as we see fit
            */
            .subscribe(object : SingleObserver<MovieModel>
            {
                /**
                 * Called in the moment of subscription and it can serve us to prevent potential memory leaks.
                 * It gives us access to a Disposable object, which is just a fancy name for the reference
                 * to the connection we established between our Single and a SingleObserver — the subscription
                 *
                 * @param disposable the newly [Disposable] to add
                 */
                override fun onSubscribe(disposable: Disposable)
                {
                    Log.d(DetailsActivity.TAG, "movieSingle::onSubscribe")
                    if (compositeDisposable == null)
                    {
                        compositeDisposable = CompositeDisposable()
                    }
                    compositeDisposable?.add(disposable)
                }

                override fun onSuccess(movie: MovieModel)
                {
                    // data is ready and we can update the UI
                    Log.d(TAG, "repositorySingle::onSuccess: movie name: ${movie.originalTitle}")

                    movieTitle.text = movie.originalTitle
                    movieOverview.text = movie.overview
                    popularity.text = movie.popularity.toString()
                    voteAverage.text = movie.voteAverage.toString()
                    voteCount.text = movie.voteCount.toString()
//                    openIssues.text = movie.openIssues.toString()
                    homepageUrl.text = movie.homePageUrl
//                    usernameTextView.text = movieId
                    val movieReleaseDate = "Created at ${movie.releaseDate?.removeSuffix("Z")?.replace("T", " ")}"
//                    val repoUpdatedTime = "Updated at ${movie.updatedAt?.removeSuffix("Z")?.replace("T", " ")}"
                    releaseDate.text = movieReleaseDate
//                    updatedTime.text = repoUpdatedTime

                    val posterPath = movie.posterPath
                    Picasso
                        .get()
                        .load(posterPath)
                        .placeholder(android.R.drawable.sym_def_app_icon)
                        .error(android.R.drawable.sym_def_app_icon)
                        .into(posterImage)
                    hideProgressBar()
                }

                /**
                 * Notifies the SingleObserver that the {@link Single} has experienced an error condition.
                 *
                 * @param e the exception encountered by the Single
                 */
                override fun onError(e: Throwable)
                {
                    // oops, we best show some error message
                    Log.e(TAG, "repositorySingle::onError: $e")
                    Toast.makeText(this@DetailsActivity, "Error connecting to GitHub", Toast.LENGTH_SHORT).show()
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
            compositeDisposable?.dispose()
            compositeDisposable = null
        }
        super.onDestroy()
    }

    /**
     * Hiding the progress bar by inverting the visibility of [progressBar] and [detailsLayout]
     */
    private fun hideProgressBar()
    {
        Log.d(TAG, "::hideProgressBar:")
        progressBar.visibility = View.INVISIBLE
        detailsLayout.visibility = View.VISIBLE
    }
}
