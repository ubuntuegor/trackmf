package to.bnt.trackmf.main

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ConcatAdapter
import androidx.work.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import to.bnt.trackmf.Constants
import to.bnt.trackmf.R
import to.bnt.trackmf.databinding.ActivityMainBinding
import to.bnt.trackmf.databinding.TextEditDialogBinding
import to.bnt.trackmf.model.parcel.NetworkError
import to.bnt.trackmf.model.parcel.NoParcelError
import to.bnt.trackmf.model.parcel.ParsingError
import to.bnt.trackmf.work.UpdateWorker
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val model: MainActivityViewModel by viewModels()
    private var currentTrackId: String? = null
    private var loading: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val statusesAdapter = StatusListAdapter()
        val headerAdapter = HeaderAdapter(
            onClick = { this.onHeaderClick() }
        )

        binding.mainViewRecycler.adapter = ConcatAdapter(headerAdapter, statusesAdapter)

        enqueueUpdates()

        binding.refreshButton.setOnClickListener {
            model.refresh()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch {
                    model.trackId.collect {
                        currentTrackId = it
                        headerAdapter.title = it ?: getString(R.string.no_track_number)
                        if (it == null) {
                            headerAdapter.subtitle =
                                getString(R.string.no_track_number_subtitle)
                        }
                        updateRefreshButton()
                    }
                }
                launch {
                    model.parcel.collect {
                        if (it != null) {
                            statusesAdapter.submitList(it.statuses)
                            val date = Date(it.lastUpdated)
                            val dateString =
                                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                    .format(date)
                            headerAdapter.subtitle =
                                getString(R.string.last_updated, dateString)
                        } else statusesAdapter.submitList(listOf())
                    }
                }
                launch {
                    model.status.collect {
                        when (it) {
                            StateStatus.Loading -> {
                                headerAdapter.loading = true
                                loading = true
                                updateRefreshButton()
                            }
                            StateStatus.Loaded -> {
                                headerAdapter.loading = false
                                loading = false
                                updateRefreshButton()
                            }
                        }
                    }
                }
                launch {
                    model.errors.collect {
                        handleException(it)
                    }
                }
            }
        }
    }

    private fun updateRefreshButton() {
        if (loading || currentTrackId == null) {
            binding.refreshButton.hide()
        } else {
            binding.refreshButton.show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun ensureNotificationPermission() {
        val notificationPermission =
            ContextCompat.checkSelfPermission(
                this,
                POST_NOTIFICATIONS
            )

        if (notificationPermission == PackageManager.PERMISSION_DENIED) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
                .launch(POST_NOTIFICATIONS)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val name = getString(R.string.notification_updates_channel)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(Constants.DEFAULT_CHANNEL_ID, name, importance)
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun enqueueUpdates() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ensureNotificationPermission()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<UpdateWorker>(1, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            Constants.UNIQUE_WORK_ID,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun handleException(e: Throwable) {
        val text = when (e) {
            is NetworkError -> {
                getString(R.string.network_error, e.message)
            }
            is ParsingError -> {
                getString(R.string.parsing_error)
            }
            is NoParcelError -> {
                getString(R.string.no_parcel_error)
            }
            else -> null
        }

        text?.let {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun onHeaderClick() {
        val dialogBinding = TextEditDialogBinding.inflate(LayoutInflater.from(this))
        currentTrackId?.let { dialogBinding.textField.editText?.setText(it) }
        MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.save)) { dialog, _ ->
                dialogBinding.textField.editText?.text?.toString()?.let {
                    model.setTrackId(it)
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}