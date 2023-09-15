package to.bnt.trackmf.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import to.bnt.trackmf.databinding.HeaderBinding
import to.bnt.trackmf.databinding.LoadingDividerBinding

class HeaderAdapter(private val onClick: View.OnClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class HeaderViewHolder(val binding: HeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        val title = binding.headerTitle
        val subtitle = binding.headerSubtitle
    }

    class LoadingViewHolder(val binding: LoadingDividerBinding) :
        RecyclerView.ViewHolder(binding.root)

    var title = ""
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var subtitle = ""
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var loading = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> HeaderViewHolder(
                HeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            1 -> {
                LoadingViewHolder(
                    LoadingDividerBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> throw IllegalStateException("Wrong item type in HeaderAdapter")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == 0) {
            holder as HeaderViewHolder
            holder.title.text = title
            holder.subtitle.text = subtitle
            holder.binding.root.setOnClickListener(onClick)
        } else if (position == 1) {
            holder as LoadingViewHolder
            holder.binding.progressBar.visibility = if (loading) View.VISIBLE else View.INVISIBLE
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount() = 2
}
