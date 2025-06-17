package com.example.after

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.EditText
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import android.widget.Toast
import android.widget.ImageView
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import androidx.recyclerview.widget.GridLayoutManager

class LibraryFragment : Fragment() {
    private val viewModel: LibraryViewModel by viewModels()
    private val homeViewModel: HomeViewModel by lazy { androidx.lifecycle.ViewModelProvider(requireActivity())[HomeViewModel::class.java] }
    private var selectedAudioUri: Uri? = null
    private val pickAudioLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedAudioUri = uri
                Toast.makeText(requireContext(), "Audio file selected", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_library, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.libraryRecyclerView)
        val emptyLibraryText = view.findViewById<TextView>(R.id.emptyLibraryText)
        val adapter = PlaylistAdapter(
            emptyList(),
            onClick = { playlist -> openPlaylistFragment(playlist.id) },
            onLongClick = { playlist -> showDeletePlaylistDialog(playlist) }
        )
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter
        viewModel.allPlaylists.observe(viewLifecycleOwner) { playlists ->
            try {
                adapter.updateData(playlists ?: emptyList())
                emptyLibraryText.visibility = if (playlists.isNullOrEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error loading playlists: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        val addPlaylistFab = view.findViewById<View>(R.id.addPlaylistFab)
        val addSongFab = view.findViewById<View>(R.id.addSongFab)
        addPlaylistFab.setOnClickListener {
            showAddPlaylistDialog()
        }
        addSongFab.setOnClickListener {
            showAddSongDialog()
        }
        return view
    }

    private fun openPlaylistFragment(playlistId: Long) {
        val fragment = PlaylistFragment().apply {
            arguments = Bundle().apply { putLong("playlistId", playlistId) }
        }
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showDeletePlaylistDialog(playlist: Playlist) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Playlist")
            .setMessage("Delete playlist '${playlist.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deletePlaylist(playlist)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddPlaylistDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_playlist, null)
        val editName = dialogView.findViewById<EditText>(R.id.editPlaylistName)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.songsSelectRecyclerView)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelPlaylist)
        val btnAdd = dialogView.findViewById<Button>(R.id.btnAddPlaylist)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Load all songs for selection
        homeViewModel.allSongs.observe(viewLifecycleOwner) { songs ->
            val adapter = SongSelectAdapter(songs ?: emptyList())
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = adapter

            btnAdd.setOnClickListener {
                val name = editName.text.toString().trim()
                val selectedSongIds = adapter.getSelectedSongIds()
                if (name.isEmpty() || selectedSongIds.isEmpty()) {
                    Toast.makeText(requireContext(), "Please enter a name and select at least one song", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                // Save playlist and crossrefs
                lifecycleScope.launch {
                    val playlistId = homeViewModel.repository.insertPlaylist(Playlist(name = name))
                    selectedSongIds.forEach { songId ->
                        homeViewModel.repository.addSongToPlaylist(playlistId, songId)
                    }
                }
                dialog.dismiss()
            }
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showAddSongDialog() {
        // Reset selected URIs each time dialog opens
        selectedAudioUri = null
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_song, null)
        val editTitle = dialogView.findViewById<EditText>(R.id.editSongTitle)
        val editArtist = dialogView.findViewById<EditText>(R.id.editArtistName)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnAdd = dialogView.findViewById<Button>(R.id.btnAddSong)
        val btnPickAudio = dialogView.findViewById<Button>(R.id.btnPickAudio)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnAdd.setOnClickListener {
            val title = editTitle.text.toString().trim()
            val artist = editArtist.text.toString().trim()
            val audioUri = selectedAudioUri
            if (title.isEmpty() || artist.isEmpty() || audioUri == null) {
                Toast.makeText(requireContext(), "Please fill all fields and pick an audio file", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Copy audio file to app storage
            val audioPath = copyUriToAppStorage(requireContext(), audioUri, "audio_${System.currentTimeMillis()}.mp3")
            // Add song to database
            homeViewModel.addSong(Song(title = title, artist = artist, filePath = audioPath))
            // Reset selected URIs after adding
            selectedAudioUri = null
            dialog.dismiss()
        }
        btnPickAudio.setOnClickListener {
            pickAudioLauncher.launch("audio/*")
        }

        dialog.show()
    }

    private fun copyUriToAppStorage(context: Context, uri: Uri, fileName: String): String {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val file = File(context.filesDir, fileName)
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return file.absolutePath
    }
} 