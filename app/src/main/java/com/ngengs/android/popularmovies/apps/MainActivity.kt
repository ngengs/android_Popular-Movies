package com.ngengs.android.popularmovies.apps

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import com.ngengs.android.popularmovies.apps.data.remote.MoviesDetail
import com.ngengs.android.popularmovies.apps.databinding.ActivityMainBinding
import com.ngengs.android.popularmovies.apps.fragments.DetailMovieFragment
import com.ngengs.android.popularmovies.apps.fragments.GridFragment
import com.ngengs.android.popularmovies.apps.globals.Values
import com.ngengs.android.popularmovies.apps.utils.ResourceHelpers.getDrawable
import com.squareup.picasso.Picasso

/**
 * Created by rizky.kharisma on 10/11/22.
 * @ngengs
 */
class MainActivity: AppCompatActivity(), GridFragment.OnFragmentInteractionListener, DetailMovieFragment.OnFragmentInteractionListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var actionBar: ActionBar
    private lateinit var menuDetail: Menu
    private lateinit var gridFragment: GridFragment
    private var detailMovieFragment: DetailMovieFragment? = null
    private lateinit var fragmentManager: FragmentManager
    private lateinit var sharedPref: SharedPreferences
    private var openDetail = false

    private val resultLauncher = registerForActivityResult(StartActivityForResult()) {
        if (gridFragment.getSortType() == Values.TYPE_FAVORITE) {
            Log.d(TAG, "onActivityResult: Refresh the favorite")
            gridFragment.changeType(Values.TYPE_FAVORITE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        actionBar = supportActionBar!!
        Log.d(TAG, "onCreate: now")
        sharedPref = getPreferences(MODE_PRIVATE)
        var sortType = sharedPref.getInt("SORT_TYPE_NOW", Values.TYPE_POPULAR)
        when (sortType) {
            Values.TYPE_POPULAR, Values.TYPE_HIGH_RATED, Values.TYPE_FAVORITE -> {}
            else -> sortType = Values.TYPE_POPULAR
        }
        fragmentManager = supportFragmentManager
        if (savedInstanceState == null) {
            Log.d(TAG, "onCreate: attach fragment")
            gridFragment = GridFragment.newInstance(sortType)
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.add(binding.fragmentGrid.id, gridFragment)
            fragmentTransaction.commit()
        } else {
            gridFragment = fragmentManager.findFragmentById(binding.fragmentGrid.id) as GridFragment
            if (binding.fragmentDetail != null) {
                if (fragmentManager.findFragmentById(binding.fragmentDetail!!.id) != null) {
                    detailMovieFragment = fragmentManager.findFragmentById(binding.fragmentDetail!!.id) as DetailMovieFragment?
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
                            Log.d(TAG, "onMenuItemClick: Share")
                            val sendIntent = Intent()
                            sendIntent.action = Intent.ACTION_SEND
                            Log.d(TAG, "onClick: " + detailMovieFragment?.getShareContent().orEmpty())
                            sendIntent.putExtra(Intent.EXTRA_TEXT, detailMovieFragment?.getShareContent().orEmpty())
                            sendIntent.type = "text/plain"
                            startActivity(Intent.createChooser(sendIntent, resources.getText(R.string.send_to)))
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

    private fun showMultiLayout(show: Boolean) {
        if (isMultiLayout()) {
            val params = binding.guideline?.layoutParams as ConstraintLayout.LayoutParams?
            if (!show) {
                openDetail = false
                binding.rootDetailView?.visibility = View.GONE
                params?.guidePercent = 1f
                gridFragment.updateSpanColumn(4)
            } else {
                openDetail = true
                binding.rootDetailView?.visibility = View.VISIBLE
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                    params?.guidePercent = 0.35f else params?.guidePercent = 0.5f
                gridFragment.updateSpanColumn(2)
            }
            binding.guideline?.layoutParams = params
        }
    }

    private fun onFavoriteClick() {
        if (isMultiLayout()) {
            Log.d(TAG, "onFavoriteClick: now")
            detailMovieFragment?.changeFavorite()
        }
    }

    private fun onCloseMultiLayout() {
        if (isMultiLayout()) {
            if (detailMovieFragment == null) detailMovieFragment = fragmentManager.findFragmentById(R.id.fragmentDetail) as DetailMovieFragment?
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.remove(detailMovieFragment!!)
            fragmentTransaction.commit()
            binding.detailHeaderImage?.setImageResource(0)
            detailMovieFragment = null
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
        if (gridFragment.getSortType() == Values.TYPE_POPULAR)
            menu.findItem(R.id.menu_sort_by_popular).isChecked = true
        else if (gridFragment.getSortType() == Values.TYPE_HIGH_RATED)
            menu.findItem(R.id.menu_sort_by_top_rated).isChecked = true
        else menu.findItem(R.id.menu_sort_by_favorite).isChecked = true
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
            gridFragment.changeType(sortType)
            item.isChecked = true
            sharedPref.edit().putInt("SORT_TYPE_NOW", sortType).apply()
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
                binding.fabFavorite?.setImageDrawable(getDrawable(this, R.drawable.ic_favorite_white))
            else
                binding.fabFavorite?.setImageDrawable(getDrawable(this, R.drawable.ic_favorite_border_white))
            if (gridFragment.getSortType() == Values.TYPE_FAVORITE && isRefresh) {
                data?.let {
                    if (isFavorite) gridFragment.addMovies(it) else gridFragment.removeMovies(it)
                }
            }
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

    override fun onFragmentChangeHeaderImage(imageUri: String?) {
        if (isMultiLayout()) {
            Picasso.get()
                .load(imageUri)
                .centerCrop()
                .resize(Resources.getSystem().displayMetrics.widthPixels, resources.getDimensionPixelSize(R.dimen.image_description_header))
                .into(binding.detailHeaderImage)
        }
    }

    override fun onFragmentClickMovies(position: Int, data: MoviesDetail) {
        if (!isMultiLayout()) {
            val intent = Intent(this, DetailMovieActivity::class.java)
                .putExtra("DATA", data)
            resultLauncher.launch(intent)
        } else {
            showMultiLayout(true)
            val fragmentDetail = binding.fragmentDetail
            if (isMultiLayout() && fragmentDetail != null) {
                var changeFragment = true
                if (detailMovieFragment != null) {
                    // Check is fragment same as the clicked data
                    val temp =
                        fragmentManager.findFragmentById(fragmentDetail.id) as DetailMovieFragment?
                    val tempMoviesId = temp?.getMoviesId() ?: -1
                    Log.d(TAG, "onFragmentClickMovies: old id: $tempMoviesId")
                    Log.d(TAG, "onFragmentClickMovies: new id: " + data.id)
                    if (data.id == tempMoviesId) changeFragment = false
                }
                Log.d(TAG, "onFragmentClickMovies: can change: $changeFragment")
                if (changeFragment) {
                    // Clear button favorite
                    onFragmentChangeFavorite(null, isFavorite = false, isRefresh = false)
                    detailMovieFragment = DetailMovieFragment.newInstance(data)
                    val fragmentTransaction = fragmentManager.beginTransaction()
                    fragmentTransaction.replace(fragmentDetail.id, detailMovieFragment!!)
                    fragmentTransaction.commit()
                    binding.appbarDetail?.setExpanded(true)
                    binding.scrollDetail?.scrollTo(0, 0)
                    gridFragment.scrollToPosition(position)
                }
            }
        }
    }

    override fun onFragmentChangeTitle(sortType: Int) {
        actionBar.title = when (sortType) {
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
        private const val RESULT_DETAIL = 10
    }
}