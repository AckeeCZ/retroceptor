package cz.ackee.sample.detail

import android.app.ListActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import cz.ackee.sample.App
import cz.ackee.sample.model.SampleItem
import cz.ackee.sample.model.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Activity with some detail
 */
class DetailActivity : ListActivity(), CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var viewModel: DetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = App.diContainer.create(DetailViewModel::class.java)

        launch {
            viewModel.viewState.consumeEach { state ->
                when (state) {
                    is State.Loaded -> showData(state.data)
                    is State.Error -> state.error.printStackTrace()
                }
            }
        }
    }

    private fun showData(items: List<SampleItem>) {
        listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add("Refresh")
            .setOnMenuItemClickListener {
                viewModel.fetchData()
                false
            }
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)

        menu.add("Invalidate access token")
            .setOnMenuItemClickListener {
                viewModel.invalidateAccessToken()
                false
            }
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)

        menu.add("Invalidate refresh token")
            .setOnMenuItemClickListener {
                viewModel.invalidateRefreshToken()
                false
            }
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)

        menu.add("Logout")
            .setOnMenuItemClickListener {
                viewModel.logout()
                false
            }
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.complete()
    }
}


