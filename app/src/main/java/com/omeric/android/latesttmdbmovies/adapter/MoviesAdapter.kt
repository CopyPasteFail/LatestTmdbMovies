package com.omeric.android.latesttmdbmovies.adapter

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.omeric.android.latesttmdbmovies.R
import com.omeric.android.latesttmdbmovies.data.model.MovieModel
import com.squareup.picasso.Picasso
import android.content.Intent
import com.omeric.android.latesttmdbmovies.activity.DetailsActivity
import com.omeric.android.latesttmdbmovies.activity.MainActivity
import com.squareup.picasso.Callback


class MoviesAdapter(
    private val movies: List<MovieModel>,
    private val rowLayout: Int,
    private val context: Context
) : RecyclerView.Adapter<MoviesAdapter.MovieViewHolder>()
{
    companion object
    {
        private val TAG = "gipsy:" + this::class.java.name
    }

    //A view holder inner class where we get reference to the views in the layout using their ID
    class MovieViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
        internal var movieItemLayout: ConstraintLayout = view.findViewById(R.id.movie_item_layout)
        internal var movieTitle: TextView = view.findViewById(R.id.title_list_item)
        //            data = (TextView) view.findViewById(R.id.date);
//        internal var movieOverview: TextView = view.findViewById(R.id.overview_list_item)
//        internal var stars: TextView = view.findViewById(R.id.stars_list_item)
        internal var releaseDate: TextView = view.findViewById(R.id.TextVw_release_date_list_item)
        internal var posterImage: ImageView = view.findViewById(R.id.poster_image_list_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoviesAdapter.MovieViewHolder
    {
        Log.d(TAG,"onCreateViewHolder:")
        val view = LayoutInflater.from(parent.context).inflate(rowLayout, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int)
    {
        Log.d(TAG,"onBindViewHolder:")
        if (movies[position].posterPath != null)
        {
            val posterUrl = MainActivity.BASE_URL_MOVIE_POSTER + movies[position].posterPath
            Log.d(TAG, "onBindViewHolder: posterUrl = $posterUrl")
            Picasso
                .get()
                .load(posterUrl)
                .placeholder(android.R.drawable.sym_def_app_icon)
                .error(android.R.drawable.stat_notify_error)
                .into(holder.posterImage)
        }
        else {holder.posterImage.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)}
        holder.movieTitle.text = movies[position].originalTitle
        holder.releaseDate.text = movies[position].releaseDate

        holder.movieItemLayout.setOnClickListener {
            context.startActivity(Intent(context, DetailsActivity::class.java)
                .putExtra(DetailsActivity.INTENT_MOVIE_ID, movies[position].id)
//                .putExtra(DetailsActivity.INTENT_REPOSITORY_NAME_ID, movies[position].name)
            )
        }
    }

    override fun getItemCount(): Int
    {
        return movies.size
    }
}
