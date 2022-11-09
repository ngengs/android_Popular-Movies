package com.ngengs.android.popularmovies.apps

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.ngengs.android.popularmovies.apps.data.remote.MoviesDetail
import com.ngengs.android.popularmovies.apps.databinding.ActivityDetailMovieBinding
import com.ngengs.android.popularmovies.apps.fragments.DetailMovieFragment
import com.ngengs.android.popularmovies.apps.fragments.DetailMovieFragment.Companion.newInstance
import com.ngengs.android.popularmovies.apps.utils.ResourceHelpers.getDrawable
import com.squareup.picasso.Picasso

/**
 * Created by rizky.kharisma on 10/11/22.
 * @ngengs
 */
class DetailMovieActivity : AppCompatActivity(), DetailMovieFragment.OnFragmentInteractionListener {
    private lateinit var binding: ActivityDetailMovieBinding
    private var data: MoviesDetail? = null
    private lateinit var fragmentManager: FragmentManager
    private lateinit var detailMovieFragment: DetailMovieFragment
    private lateinit var actionBar: ActionBar
    private var moviesShare = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailMovieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        data = intent.getParcelableExtra("DATA") ?: kotlin.run {
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
            detailMovieFragment = newInstance(data!!)
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.add(binding.fragmentDetail.id, detailMovieFragment)
            fragmentTransaction.commit()
        } else {
            detailMovieFragment =
                (fragmentManager.findFragmentById(binding.fragmentDetail.id) as DetailMovieFragment?)!!
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
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
            return true
        } else if (id == R.id.menu_detail_share) {
            shareItem()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onFavoriteClick() {
        detailMovieFragment.changeFavorite()
    }

    private fun shareItem() {
        if (detailMovieFragment.getStatusLoadedFromServer()) {
            Log.d(TAG, "onClick: " + detailMovieFragment.getShareContent())
            val sendIntent = Intent()
                .putExtra(Intent.EXTRA_TEXT, detailMovieFragment.getShareContent())
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
        if (isFavorite) binding.fabFavorite.setImageDrawable(getDrawable(this, R.drawable.ic_favorite_white))
        else binding.fabFavorite.setImageDrawable(getDrawable(this, R.drawable.ic_favorite_border_white))
    }

    override fun onFragmentChangeTitle(title: String) {
        actionBar.title = title
    }

    override fun onFragmentChangeHeaderImage(imageUri: String?) {
        if (imageUri?.isNotEmpty() == true) {
            Picasso.get()
                .load(imageUri)
                .centerCrop()
                .resize(Resources.getSystem().displayMetrics.widthPixels, resources.getDimensionPixelSize(R.dimen.image_description_header))
                .into(binding.detailHeaderImage)
        }
    }

    companion object {
        private const val TAG = "DetailMovieActivity"
    }
}