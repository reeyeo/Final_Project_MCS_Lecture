package com.example.after

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlaylistAdapter(
    var playlists: List<Playlist>,
    private val onClick: (Playlist) -> Unit,
    private val onLongClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    inner class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.playlistTitle)
        val subtitleText: TextView = itemView.findViewById(R.id.playlistSubtitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist_card, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.titleText.text = playlist.name
        holder.subtitleText.text = "Playlist"
        holder.itemView.setOnClickListener { onClick(playlist) }
        holder.itemView.setOnLongClickListener { 
            onLongClick(playlist)
            true
        }
    }

    override fun getItemCount() = playlists.size

    fun updateData(newPlaylists: List<Playlist>) {
        playlists = newPlaylists
        notifyDataSetChanged()
    }
} 