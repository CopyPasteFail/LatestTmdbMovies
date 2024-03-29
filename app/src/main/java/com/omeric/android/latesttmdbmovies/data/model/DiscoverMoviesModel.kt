package com.omeric.android.latesttmdbmovies.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class DiscoverMoviesModel
{
    @SerializedName("page")
    @Expose
    var page: Int? = null

    @SerializedName("total_results")
    @Expose
    var totalResults: Int? = null

    @SerializedName("total_pages")
    @Expose
    var totalPages: Int? = null

    @SerializedName("results")
    @Expose
    var results: ArrayList<MovieModel>? = null
}
