package to.bnt.trackmf.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import to.bnt.trackmf.databinding.StatusItemBinding
import to.bnt.trackmf.model.parcel.ParcelStatus

class StatusListAdapter : ListAdapter<ParcelStatus, StatusListAdapter.StatusItemHolder>(DiffCallback) {
    class StatusItemHolder(private val binding: StatusItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun update(status: ParcelStatus, position: Int, itemCount: Int) {
            binding.statusTitle.text = status.title
            binding.statusPlace.text = status.place
            binding.statusDate.text = status.date

            binding.startConnection.visibility = if (position == 0) {
                View.INVISIBLE
            } else {
                View.VISIBLE
            }

            binding.endConnection.visibility = if (position + 1 == itemCount) {
                View.INVISIBLE
            } else {
                View.VISIBLE
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ParcelStatus>() {
        override fun areItemsTheSame(oldItem: ParcelStatus, newItem: ParcelStatus) =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: ParcelStatus, newItem: ParcelStatus) =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusItemHolder {
        return StatusItemHolder(
            StatusItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: StatusItemHolder, position: Int) {
        holder.update(getItem(position), position, itemCount)
    }
}