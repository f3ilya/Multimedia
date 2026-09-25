package ru.netology.multimedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.multimedia.dto.Track
import ru.netology.multimedia.databinding.CardTrackBinding

interface OnInteractionListener {
    fun onLike(track: Track) {}
    fun onPlay(track: Track) {}
}

class TracksAdapter(
    private val onInteractionListener: OnInteractionListener
): androidx.recyclerview.widget.ListAdapter<Track, TracksAdapter.TrackViewHolder>(TrackDiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TrackViewHolder {
        val binding = CardTrackBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TrackViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TrackViewHolder(
        private val binding: CardTrackBinding,
        private val onInteractionListener: OnInteractionListener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(track: Track) {
            binding.apply {
                nameTrack.text = track.file
                likeTrack.isChecked = track.isLiked
                play.isChecked = track.isPlaying

                likeTrack.setOnClickListener {
                    onInteractionListener.onLike(track)
                }

                play.setOnClickListener {
                    onInteractionListener.onPlay(track)
                }
            }
        }
    }

    class TrackDiffCallback : DiffUtil.ItemCallback<Track>() {
        override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
            return oldItem == newItem
        }
    }
}