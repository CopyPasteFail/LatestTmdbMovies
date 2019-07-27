package com.omeric.android.latesttmdbmovies.data.remote

import com.omeric.android.latesttmdbmovies.data.model.RepositoryModel
import com.omeric.android.latesttmdbmovies.data.model.SearchRepositoriesModel
import io.reactivex.Single
import retrofit2.http.*


/**
 * This interface defines methods used by Retrofit to communicate with a given API.
 * Each interface method, can be thought of as a RESTful method.
 */
interface TmdbApiService
{
    //example: https://api.github.com/repos/BracketCove/SpaceNotes
    //example: "https://api.themoviedb.org/3/movie/299536?api_key=cc37499b567edef6d7fad0608ea992c5&language=en-US"
    @GET("repos/{user}/{repoName}")
    fun getMovieDetails(
        @Path("user") user: String,
        @Path("repoName") repoName: String
    ): Single<RepositoryModel>

    //example: "https://api.themoviedb.org/3/movie/latest?language=en-US&api_key=1e0dcaa7e93980fb84e1d2430d01b887"
    @GET("users/{user}/repos")
    fun getLatestMovies(
        @Path("user") user: String
    ): Single<List<RepositoryModel>>

    //example: https://api.github.com/search/repositories?q=topic:android&sort=stars&order=desc
    @GET("search/repositories")
    fun getRepositoriesFromSearch(
        @QueryMap filters : Map<String, String>
    ): Single<SearchRepositoriesModel>
}