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
import com.omeric.android.latesttmdbmovies.data.model.RepositoryModel
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
        const val INTENT_USERNAME_ID = "USERNAME_ID"
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
        val username = intent.getStringExtra(INTENT_USERNAME_ID)
        val repositoryName = intent.getStringExtra(INTENT_REPOSITORY_NAME_ID)
        Log.d(TAG, ":onCreate: GET /repos/$username/$repositoryName")

        val repositoryTitle: TextView = findViewById(R.id.TextVw_repo_details_title)
        val repositoryDescription: TextView = findViewById(R.id.TextVw_repo_details_description)
        val stars: TextView = findViewById(R.id.TextVw_repo_details_stars)
        val forks: TextView = findViewById(R.id.TextVw_repo_details_forks)
        val watchers: TextView = findViewById(R.id.TextVw_repo_details_watchers)
        val openIssues: TextView = findViewById(R.id.TextVw_repo_details_open_issues)
        val avatarImage: ImageView = findViewById(R.id.ImgVw_repo_details_avatar_image)
        val htmlUrl: TextView = findViewById(R.id.TextVw_repo_details_link)
        val usernameTextView: TextView = findViewById(R.id.TextVw_repo_details_username)
        val creationTime: TextView = findViewById(R.id.TextVw_repo_details_creation_time)
        val updatedTime: TextView = findViewById(R.id.TextVw_repo_details_updated_time)

        progressBar = findViewById(R.id.progressbar_repo_details)
        detailsLayout = findViewById(R.id.repo_details_layout)

        // Trailing slash is needed
        val retrofit = Retrofit.Builder()
            .baseUrl(MainActivity.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()


        // create an instance of the TmdbApiService
        val gitHubApiService = retrofit.create(TmdbApiService::class.java)

        // make a request by calling the corresponding method
        /*
         * Single allows us to recieve a single set of data from the API, do some stuff with it in the background, and,
         * when done, present it to the user 
         * Internally, it is based on the observer pattern with data being pushed to interested observers
         * at the moment of subscription
         */

        //https://api.github.com/repos/:owner/:repo
        gitHubApiService.getMovieDetails(username, repositoryName)
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
            .subscribe(object : SingleObserver<RepositoryModel>
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
                    Log.d(DetailsActivity.TAG, "repositorySingle::onSubscribe")
                    if (compositeDisposable == null)
                    {
                        compositeDisposable = CompositeDisposable()
                    }
                    compositeDisposable?.add(disposable)
                }

                override fun onSuccess(repository: RepositoryModel)
                {
                    // data is ready and we can update the UI
                    Log.d(TAG, "repositorySingle::onSuccess: repository name: ${repository.name}")

                    repositoryTitle.text = repository.name
                    repositoryDescription.text = repository.description
                    stars.text = repository.stargazersCount.toString()
                    forks.text = repository.forksCount.toString()
                    watchers.text = repository.subscribersCount.toString()
                    openIssues.text = repository.openIssues.toString()
                    htmlUrl.text = repository.htmlUrl
                    usernameTextView.text = username
                    val repoCreatedTime = "Created at ${repository.createdAt?.removeSuffix("Z")?.replace("T", " ")}"
                    val repoUpdatedTime = "Updated at ${repository.updatedAt?.removeSuffix("Z")?.replace("T", " ")}"
                    creationTime.text = repoCreatedTime
                    updatedTime.text = repoUpdatedTime

                    val avatarUrl = repository.owner?.avatarUrl
                    Picasso
                        .get()
                        .load(avatarUrl)
                        .placeholder(android.R.drawable.sym_def_app_icon)
                        .error(android.R.drawable.sym_def_app_icon)
                        .into(avatarImage)
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
