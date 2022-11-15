package com.ngengs.android.popularmovies.apps.screen.movielist

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.ngengs.android.popularmovies.apps.screen.moviedetail.MovieDetailActivity
import com.ngengs.android.popularmovies.apps.MoviesViewModelFactory
import com.ngengs.android.popularmovies.apps.R
import com.ngengs.android.popularmovies.apps.data.remote.MoviesDetail
import com.ngengs.android.popularmovies.apps.databinding.ActivityMainBinding
import com.ngengs.android.popularmovies.apps.screen.moviedetail.MovieDetailFragment
import com.ngengs.android.popularmovies.apps.globals.Values
import com.ngengs.android.popularmovies.apps.utils.ResourceHelpers.getDrawable
import com.ngengs.android.popularmovies.apps.utils.images.GlideUtils
import com.ngengs.android.popularmovies.apps.utils.pref.MenuPref

/**
 * Created by rizky.kharisma on 10/11/22.
 * @ngengs
 */
class MovieListActivity: AppCompatActivity(), MovieListFragment.OnFragmentInteractionListener, MovieDetailFragment.OnFragmentInteractionListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var menuDetail: Menu
    private lateinit var movieListFragment: MovieListFragment
    private var movieDetailFragment: MovieDetailFragment? = null
    private lateinit var fragmentManager: FragmentManager
    private lateinit var menuPref: MenuPref
    private var openDetail = false

    private val viewModel: MovieListViewModel by viewModels { MoviesViewModelFactory }

    private val resultLauncher = registerForActivityResult(StartActivityForResult()) {
        if (viewModel.isFavoriteType()) viewModel.refreshData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        Log.d(TAG, "onCreate: now")
        menuPref = MenuPref.instantiate(this)
        val sortType = menuPref.sortType
        viewModel.changeSortType(sortType, true)
        fragmentManager = supportFragmentManager
        if (savedInstanceState == null) {
            Log.d(TAG, "onCreate: attach fragment")
            movieListFragment = MovieListFragment.newInstance(sortType)
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.add(binding.fragmentGrid.id, movieListFragment)
            fragmentTransaction.commit()
        } else {
            movieListFragment = fragmentManager.findFragmentById(binding.fragmentGrid.id) as MovieListFragment
            if (binding.fragmentDetail != null) {
                if (fragmentManager.findFragmentById(binding.fragmentDetail!!.id) != null) {
                    movieDetailFragment = fragmentManager.findFragmentById(binding.fragmentDetail!!.id) as MovieDetailFragment?
                }
            }
            openDetail = savedInstanceState.getBoolean("OPEN_DETAIL", false)
        }
        initializeViewAction()
    }

    private fun initializeViewAction() {
        binding.fabFavorite?.setOnClickListener { onFavoriteClick() }
    }

    private fun isMultiLayout(): Boolean {
        return binding.rootDetailView != null && binding.guideline != null
    }

    private fun createMultiLayout() {
        if (isMultiLayout()) {
            Log.d(TAG, "createMultiLayout: success")
            binding.toolbarDetail?.let { toolbarDetail ->
                toolbarDetail.inflateMenu(R.menu.menu_detail)
                menuDetail = toolbarDetail.menu
                menuDetail.findItem(R.id.menu_detail_close).isVisible = true
                menuDetail.findItem(R.id.menu_detail_share).isVisible = false
                toolbarDetail.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.menu_detail_close -> {
                            onCloseMultiLayout()
                            return@setOnMenuItemClickListener true
                        }
                        R.id.menu_detail_share -> {
                            shareItem()
                            return@setOnMenuItemClickListener true
                        }
                        else -> {
                            return@setOnMenuItemClickListener false
                        }
                    }
                }
                showMultiLayout(openDetail)
            }
        }
    }

    private fun shareItem() {
        if (movieDetailFragment?.getStatusLoadedFromServer() == true) {
            Log.d(TAG, "onMenuItemClick: Share")
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            Log.d(TAG, "onClick: " + movieDetailFragment?.getShareContent().orEmpty())
            sendIntent.putExtra(Intent.EXTRA_TEXT, movieDetailFragment?.getShareContent().orEmpty())
            sendIntent.type = "text/plain"
            startActivity(Intent.createChooser(sendIntent, resources.getText(R.string.send_to)))
        }
    }

    private fun showMultiLayout(show: Boolean) {
        if (isMultiLayout()) {
            val params = binding.guideline?.layoutParams as ConstraintLayout.LayoutParams?
            if (!show) {
                openDetail = false
                binding.rootDetailView?.visibility = View.GONE
                params?.guidePercent = 1f
                movieListFragment.updateSpanColumn(4)
            } else {
                openDetail = true
                binding.rootDetailView?.visibility = View.VISIBLE
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                    params?.guidePercent = 0.35f else params?.guidePercent = 0.5f
                movieListFragment.updateSpanColumn(2)
            }
            binding.guideline?.layoutParams = params
        }
    }

    private fun onFavoriteClick() {
        if (isMultiLayout()) {
            Log.d(TAG, "onFavoriteClick: now")
            movieDetailFragment?.changeFavorite()
        }
    }

    private fun onCloseMultiLayout() {
        if (isMultiLayout()) {
            if (movieDetailFragment == null) movieDetailFragment = fragmentManager.findFragmentById(
                R.id.fragmentDetail
            ) as MovieDetailFragment?
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.remove(movieDetailFragment!!)
            fragmentTransaction.commit()
            binding.detailHeaderImage?.setImageResource(0)
            movieDetailFragment = null
            showMultiLayout(false)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("OPEN_DETAIL", openDetail)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_popular, menu)
        if (viewModel.isPopularType())
            menu.findItem(R.id.menu_sort_by_popular).isChecked = true
        else if (viewModel.isTopRatedType())
            menu.findItem(R.id.menu_sort_by_top_rated).isChecked = true
        else if(viewModel.isFavoriteType())
            menu.findItem(R.id.menu_sort_by_favorite).isChecked = true
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val sortType = when (item.itemId) {
            R.id.menu_sort_by_popular -> Values.TYPE_POPULAR
            R.id.menu_sort_by_top_rated -> Values.TYPE_HIGH_RATED
            R.id.menu_sort_by_favorite -> Values.TYPE_FAVORITE
            else -> -1
        }
        if (sortType > -1) {
            movieListFragment.changeType(sortType)
            item.isChecked = true
            menuPref.sortType = sortType
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (isMultiLayout()) onCloseMultiLayout() else super.onBackPressed()
    }

    override fun onFragmentShowShare() {
        if (isMultiLayout()) {
            menuDetail.findItem(R.id.menu_detail_share).isVisible = true
        }
    }

    override fun onFragmentChangeFavorite(
        data: MoviesDetail?,
        isFavorite: Boolean,
        isRefresh: Boolean
    ) {
        if (isMultiLayout()) {
            Log.d(TAG, "onFragmentChangeFavorite: now")
            if (isFavorite)
                binding.fabFavorite?.setImageDrawable(getDrawable(this,
                    R.drawable.ic_favorite_white
                ))
            else
                binding.fabFavorite?.setImageDrawable(getDrawable(this,
                    R.drawable.ic_favorite_border_white
                ))

            if (data != null && isRefresh) viewModel.addOrRemoveFavoriteMovie(data, isFavorite)
        }
    }

    override fun onFragmentChangeTitle(title: String) {
        Log.d(TAG, "onFragmentChangeTitle: start")
        if (isMultiLayout()) {
            Log.d(TAG, "onFragmentChangeTitle: change to: $title")
            binding.collapsingToolbar?.title = title
            Log.d(TAG, "onFragmentChangeTitle: changed to: " + binding.collapsingToolbar?.title)
        }
    }

    override fun onFragmentChangeHeaderImage(imageUri: String?, thumbnailUri: String?) {
        if (isMultiLayout()) {
            binding.detailHeaderImage?.let { imageView ->
                Glide.with(this)
                    .load(imageUri)
                    .thumbnail(GlideUtils.thumbnailBuilder(imageView.context, thumbnailUri))
                    .centerCrop()
                    .into(imageView)
            }
        }
    }

    override fun onFragmentClickMovies(position: Int, data: MoviesDetail) {
        if (!isMultiLayout()) {
            val intent = Intent(this, MovieDetailActivity::class.java)
                .putExtra(MovieDetailActivity.ARG_DATA, data)
            resultLauncher.launch(intent)
        } else {
            showMultiLayout(true)
            val fragmentDetail = binding.fragmentDetail
            if (isMultiLayout() && fragmentDetail != null) {
                var changeFragment = true
                if (movieDetailFragment != null) {
                    // Check is fragment same as the clicked data
                    val temp =
                        fragmentManager.findFragmentById(fragmentDetail.id) as MovieDetailFragment?
                    val tempMoviesId = temp?.getMoviesId() ?: -1
                    Log.d(TAG, "onFragmentClickMovies: old id: $tempMoviesId")
                    Log.d(TAG, "onFragmentClickMovies: new id: " + data.id)
                    if (data.id == tempMoviesId) changeFragment = false
                }
                Log.d(TAG, "onFragmentClickMovies: can change: $changeFragment")
                if (changeFragment) {
                    // Clear button favorite
                    onFragmentChangeFavorite(null, isFavorite = false, isRefresh = false)
                    movieDetailFragment = MovieDetailFragment.newInstance(data)
                    val fragmentTransaction = fragmentManager.beginTransaction()
                    fragmentTransaction.replace(fragmentDetail.id, movieDetailFragment!!)
                    fragmentTransaction.commit()
                    binding.appbarDetail?.setExpanded(true)
                    binding.scrollDetail?.scrollTo(0, 0)
                    movieListFragment.scrollToPosition(position)
                }
            }
        }
    }

    override fun onFragmentChangeTitle(sortType: Int) {
        supportActionBar?.title = when (sortType) {
            Values.TYPE_POPULAR -> resources.getString(R.string.title_popular)
            Values.TYPE_HIGH_RATED -> resources.getString(R.string.title_top_rated)
            else -> resources.getString(R.string.title_favorite)
        }
    }

    override fun onAttachHandler() {
        createMultiLayout()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}