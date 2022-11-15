package com.ngengs.android.popularmovies.apps.screen.moviedetail

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.ngengs.android.popularmovies.apps.R
import com.ngengs.android.popularmovies.apps.data.remote.MoviesDetail
import com.ngengs.android.popularmovies.apps.databinding.ActivityDetailMovieBinding
import com.ngengs.android.popularmovies.apps.screen.moviedetail.MovieDetailFragment.Companion.newInstance
import com.ngengs.android.popularmovies.apps.utils.ResourceHelpers.getDrawable
import com.ngengs.android.popularmovies.apps.utils.images.GlideUtils

/**
 * Created by rizky.kharisma on 10/11/22.
 * @ngengs
 */
class MovieDetailActivity : AppCompatActivity(), MovieDetailFragment.OnFragmentInteractionListener {
    private lateinit var binding: ActivityDetailMovieBinding
    private lateinit var fragmentManager: FragmentManager
    private lateinit var movieDetailFragment: MovieDetailFragment
    private lateinit var actionBar: ActionBar
    private var moviesShare = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailMovieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val data : MoviesDetail? = intent.getParcelableExtra(ARG_DATA) ?: kotlin.run {
            Toast.makeText(this, "Something wrong with detail data", Toast.LENGTH_SHORT).show()
            finish()
            null
        }

        actionBar = supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)

        fragmentManager = supportFragmentManager

        if (savedInstanceState == null) {
            Log.d(TAG, "onCreate: attach fragment")
            movieDetailFragment = newInstance(data!!)
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.add(binding.fragmentDetail.id, movieDetailFragment)
            fragmentTransaction.commit()
        } else {
            movieDetailFragment =
                (fragmentManager.findFragmentById(binding.fragmentDetail.id) as MovieDetailFragment?)!!
        }
        initializeViewAction()
    }

    private fun initializeViewAction() {
        binding.fabFavorite.setOnClickListener { onFavoriteClick() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.d(TAG, "onCreateOptionsMenu: create menu")
        menuInflater.inflate(R.menu.menu_detail, menu)
        menu.findItem(R.id.menu_detail_close).isVisible = false
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.menu_detail_share).isVisible = moviesShare
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.menu_detail_share -> {
                shareItem()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onFavoriteClick() {
        movieDetailFragment.changeFavorite()
    }

    private fun shareItem() {
        if (movieDetailFragment.getStatusLoadedFromServer()) {
            Log.d(TAG, "onClick: " + movieDetailFragment.getShareContent())
            val sendIntent = Intent()
                .putExtra(Intent.EXTRA_TEXT, movieDetailFragment.getShareContent())
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.type = "text/plain"
            startActivity(Intent.createChooser(sendIntent, resources.getText(R.string.send_to)))
        }
    }

    override fun onFragmentShowShare() {
        Log.d(TAG, "onFragmentShowShare: changed")
        moviesShare = true
        invalidateOptionsMenu()
    }

    override fun onFragmentChangeFavorite(
        data: MoviesDetail?,
        isFavorite: Boolean,
        isRefresh: Boolean
    ) {
        if (isFavorite) binding.fabFavorite.setImageDrawable(getDrawable(this,
            R.drawable.ic_favorite_white
        ))
        else binding.fabFavorite.setImageDrawable(getDrawable(this,
            R.drawable.ic_favorite_border_white
        ))
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed")
        super.onBackPressed()
    }

    override fun onFragmentChangeTitle(title: String) {
        actionBar.title = title
    }

    override fun onFragmentChangeHeaderImage(imageUri: String?, thumbnailUri: String?) {
        if (imageUri?.isNotEmpty() == true) {
            Glide.with(this)
                .load(imageUri)
                .thumbnail(
                    GlideUtils.thumbnailBuilder(binding.detailHeaderImage.context, thumbnailUri)
                )
                .centerCrop()
                .into(binding.detailHeaderImage)
        }
    }

    companion object {
        private const val TAG = "DetailMovieActivity"
        const val ARG_DATA = "DATA"
    }
}