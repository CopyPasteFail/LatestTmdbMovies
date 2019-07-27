package com.omeric.android.latesttmdbmovies.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class RepositoryModel
{
    @SerializedName("name")
    @Expose
    var name: String? = null
    @SerializedName("owner")
    @Expose
    var owner: Owner? = null
    @SerializedName("html_url")
    @Expose
    var htmlUrl: String? = null
    @SerializedName("description")
    @Expose
    var description: String? = null
    @SerializedName("stargazers_count")
    @Expose
    var stargazersCount: Int? = null
    @SerializedName("open_issues")
    @Expose
    var openIssues: Int? = null
    @SerializedName("subscribers_count")
    @Expose
    var subscribersCount: Int? = null
    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null
    @SerializedName("updated_at")
    @Expose
    var updatedAt: String? = null
    @SerializedName("forks_count")
    @Expose
    var forksCount: Int? = null

    class Owner
    {
        @SerializedName("login")
        @Expose
        var login: String? = null
        @SerializedName("avatar_url")
        @Expose
        var avatarUrl: String? = null
    }
}
