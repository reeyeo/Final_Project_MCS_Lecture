package com.example.after

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SongAdapter(
    var songs: List<Song>,
    private val onClick: (Song) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.songTitle)
        val artistText: TextView = itemView.findViewById(R.id.songArtist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song_card, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.titleText.text = song.title
        holder.artistText.text = song.artist
        holder.itemView.setOnClickListener { onClick(song) }
    }

    override fun getItemCount() = songs.size

    fun updateData(newSongs: List<Song>) {
        songs = newSongs
        notifyDataSetChanged()
    }
} 