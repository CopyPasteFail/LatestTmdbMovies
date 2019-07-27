package com.omeric.android.latesttmdbmovies.data.remote

import com.omeric.android.latesttmdbmovies.data.model.MovieModel
import com.omeric.android.latesttmdbmovies.data.model.DiscoverMoviesModel
import io.reactivex.Single
import retrofit2.http.*


/**
 * This interface defines methods used by Retrofit to communicate with a given API.
 * Each interface method, can be thought of as a RESTful method.
 */
interface TmdbApiService
{
    //example: https://api.themoviedb.org/3/movie/299536?api_key=1e0dcaa7e93980fb84e1d2430d01b887&language=en-US
    @GET("movie/{id}?api_key={apiKey}&language=en-US")
    fun getMovieDetails(
        @Path("id") id: String,
        @Path("apiKey") apiKey: String
    ): Single<MovieModel>

    //example: https://api.themoviedb.org/3/discover/movie?api_key=1e0dcaa7e93980fb84e1d2430d01b887&language=en-US&sort_by=release_date.desc&include_adult=false&include_video=false&page=1
    //example: https://api.themoviedb.org/3/discover/movie?api_key=1e0dcaa7e93980fb84e1d2430d01b887&language=en-US&sort_by=release_date.desc&include_adult=false&include_video=false&page=1&primary_release_date.lte=2013-08-30
    @GET("discover/movie?api_key={api_key}&language=en-US&sort_by=release_date.desc&include_adult=false&include_video=false&page={page}&primary_release_date.lte={date}\n")
    fun getLatestMovies(
        @Path("api_key") apiKey: String,
        @Path("date") releaseDateMax: String,
        @Path("page") pageNumber: Int
    ): Single<DiscoverMoviesModel>

    //example: https://api.themoviedb.org/3/discover/movie?api_key=1e0dcaa7e93980fb84e1d2430d01b887&language=en-US&sort_by=release_date.desc&include_adult=false&include_video=false&page=1&primary_release_date.lte=2013-08-30
    @GET("discover/movie")
    fun getMoviesFromDiscover(
        @QueryMap filters : Map<String, String>
    ): Single<DiscoverMoviesModel>

    //example: "https://api.themoviedb.org/3/movie/latest?language=en-US&api_key=1e0dcaa7e93980fb84e1d2430d01b887"
    @GET("movie/latest?language=en-US&api_key={apiKey}")
    fun getLatestMovie(
        @Path("apiKey") apiKey: String
    ): Single<MovieModel>

    //example: https://api.github.com/search/repositories?q=topic:android&sort=stars&order=desc
/*
    @GET("search/repositories")
    fun getMoviesFromSearch(
        @QueryMap filters : Map<String, String>
    ): Single<DiscoverMoviesModel>
*/
}