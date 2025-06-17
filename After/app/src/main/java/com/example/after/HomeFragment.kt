package com.example.after

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.AlertDialog
import android.widget.EditText
import android.widget.Button
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import android.content.ContentResolver
import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import android.os.Build
import androidx.constraintlayout.widget.Guideline
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

class HomeFragment : Fragment() {
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var songAdapter: SongAdapter
    private var selectedAudioUri: Uri? = null
    private val pickAudioLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedAudioUri = uri
                Toast.makeText(requireContext(), "Audio file selected", Toast.LENGTH_SHORT).show()
            }
        }
    private val requestAudioPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                pickAudioLauncher.launch("audio/*")
            } else {
                Toast.makeText(requireContext(), "Permission denied to read audio files", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val songRecyclerView = view.findViewById<RecyclerView>(R.id.songRecyclerView)
        val emptySongsText = view.findViewById<TextView>(R.id.emptySongsText)

        songAdapter = SongAdapter(emptyList()) { song ->
            // Play the selected song and show the playbar
            val activity = requireActivity()
            val musicBar = activity.findViewById<View>(R.id.music_bar_fragment)
            musicBar?.visibility = View.VISIBLE
            MusicBarFragment.playSongList(listOf(song))
        }

        songRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        songRecyclerView.adapter = songAdapter

        // Add long-press listener for song deletion
        songRecyclerView.setOnLongClickListener {
            val position = (it as? RecyclerView)?.getChildAdapterPosition(it)
            position?.let { pos ->
                val song = songAdapter.songs[pos]
                showDeleteSongDialog(song)
            }
            true
        }

        viewModel.allSongs.observe(viewLifecycleOwner) { songs ->
            songAdapter.updateData(songs ?: emptyList())
            emptySongsText.visibility = if (songs.isNullOrEmpty()) View.VISIBLE else View.GONE
        }

        return view
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

        val dialog = AlertDialog.Builder(requireContext())
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
            viewModel.addSong(Song(title = title, artist = artist, filePath = audioPath))
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

    private fun showDeleteSongDialog(song: Song) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Song")
            .setMessage("Delete song '${song.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteSong(song)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Listen for playbar visibility changes and update FAB guideline
    override fun onResume() {
        super.onResume()
        val fabBottomGuideline = view?.findViewById<Guideline>(R.id.fabBottomGuideline)
        val musicBar = requireActivity().findViewById<View>(R.id.music_bar_fragment)
        musicBar?.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
            if (fabBottomGuideline != null) {
                if (v.visibility == View.VISIBLE) {
                    setFabGuidelineAbovePlaybar(fabBottomGuideline)
                } else {
                    setFabGuidelineToBottom(fabBottomGuideline)
                }
            }
        }
    }

    private fun setFabGuidelineToBottom(guideline: Guideline) {
        val params = guideline.layoutParams as ConstraintLayout.LayoutParams
        params.guideEnd = -1
        params.guidePercent = 1.0f
        guideline.layoutParams = params
    }

    private fun setFabGuidelineAbovePlaybar(guideline: Guideline) {
        val params = guideline.layoutParams as ConstraintLayout.LayoutParams
        // 88dp above the bottom
        params.guideEnd = (88 * resources.displayMetrics.density).toInt()
        params.guidePercent = 1.0f // Keep percent at 1.0f to avoid null assignment
        guideline.layoutParams = params
    }
} 