package com.omeric.android.latesttmdbmovies.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class MovieModel
{
    @SerializedName("homepage")
    @Expose
    var homePageUrl: String? = null

    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("original_title")
    @Expose
    var originalTitle: String? = null

    @SerializedName("overview")
    @Expose
    var overview: String? = null

    @SerializedName("popularity")
    @Expose
    var popularity: /*Number*/Float? = null

    @SerializedName("poster_path")
    @Expose
    var posterPath: String? = null

    @SerializedName("release_date")
    @Expose
    var releaseDate: String? = null

    @SerializedName("vote_average")
    @Expose
    var voteAverage: /*Number*/Float? = null

    @SerializedName("vote_count")
    @Expose
    var voteCount: Int? = null
}
