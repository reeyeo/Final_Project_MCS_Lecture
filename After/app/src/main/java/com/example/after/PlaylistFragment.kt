package com.example.after

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class PlaylistFragment : Fragment() {
    private lateinit var playlistNameText: TextView
    private lateinit var playButton: Button
    private lateinit var recyclerView: RecyclerView
    private var playlistId: Long = -1L
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: SongAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_playlist, container, false)
        playlistNameText = view.findViewById(R.id.textPlaylistName)
        playButton = view.findViewById(R.id.btnPlayPlaylist)
        recyclerView = view.findViewById(R.id.playlistSongsRecyclerView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val backButton: ImageButton = view.findViewById(R.id.btnBack)

        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        playlistId = arguments?.getLong("playlistId") ?: -1L
        if (playlistId == -1L) {
            Toast.makeText(requireContext(), "Invalid playlist", Toast.LENGTH_SHORT).show()
            return
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        var playlistSongs: List<Song> = emptyList()
        adapter = SongAdapter(emptyList()) { song ->
            showRemoveSongDialog(song)
        }
        recyclerView.adapter = adapter
        recyclerView.setOnLongClickListener {
            true
        }
        lifecycleScope.launch {
            viewModel.repository.getPlaylistWithSongs(playlistId).collect { playlistWithSongs ->
                playlistNameText.text = playlistWithSongs.playlist.name
                playlistSongs = playlistWithSongs.songs
                adapter.updateData(playlistWithSongs.songs)
            }
        }
        playButton.setOnClickListener {
            if (playlistSongs.isNotEmpty()) {
                MusicBarFragment.playSongList(playlistSongs)
            } else {
                Toast.makeText(requireContext(), "No songs in playlist", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showRemoveSongDialog(song: Song) {
        AlertDialog.Builder(requireContext())
            .setTitle("Remove Song")
            .setMessage("Remove '${song.title}' from this playlist?")
            .setPositiveButton("Remove") { _, _ ->
                lifecycleScope.launch {
                    viewModel.repository.removeSongFromPlaylist(playlistId, song.id)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
} 