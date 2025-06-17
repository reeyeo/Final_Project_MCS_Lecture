package com.example.after

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SongSelectAdapter(
    private val songs: List<Song>
) : RecyclerView.Adapter<SongSelectAdapter.SongSelectViewHolder>() {
    private val selectedIds = mutableSetOf<Long>()

    inner class SongSelectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.songCheckBox)
        val titleText: TextView = itemView.findViewById(R.id.songTitle)
        val artistText: TextView = itemView.findViewById(R.id.songArtist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongSelectViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song_select, parent, false)
        return SongSelectViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongSelectViewHolder, position: Int) {
        val song = songs[position]
        holder.titleText.text = song.title
        holder.artistText.text = song.artist
        holder.checkBox.isChecked = selectedIds.contains(song.id)
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedIds.add(song.id) else selectedIds.remove(song.id)
        }
        holder.itemView.setOnClickListener {
            holder.checkBox.isChecked = !holder.checkBox.isChecked
        }
    }

    override fun getItemCount() = songs.size

    fun getSelectedSongIds(): List<Long> = selectedIds.toList()
} 