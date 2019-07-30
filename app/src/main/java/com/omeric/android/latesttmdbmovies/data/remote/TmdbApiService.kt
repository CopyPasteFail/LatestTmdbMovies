package com.omeric.android.latesttmdbmovies.data.remote

import com.omeric.android.latesttmdbmovies.data.model.DiscoverMoviesModel
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query


/**
 * This interface defines methods used by Retrofit to communicate with a given API.
 * Each interface method, can be thought of as a RESTful method.
 */
interface TmdbApiService
{
    @GET("discover/movie\n")
    fun getLatestMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String, //en-US
        @Query("sort_by") sortBy: String, //release_date.desc
        @Query("include_adult") includeAdult: String, //false
        @Query("include_video") includeVideo: String, //false
        @Query("page") pageNumber: Int, //1
        @Query("primary_release_date.lte") releaseDateMax: String //2013-08-30
    ): Single<DiscoverMoviesModel>
}
