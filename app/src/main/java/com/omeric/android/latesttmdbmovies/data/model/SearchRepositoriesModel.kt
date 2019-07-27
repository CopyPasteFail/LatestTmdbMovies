package com.omeric.android.latesttmdbmovies.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class SearchRepositoriesModel
{
    @SerializedName("total_count")
    @Expose
    var totalCount: Int? = null
    @SerializedName("items")
    @Expose
    var items: List<RepositoryModel>? = null
}
