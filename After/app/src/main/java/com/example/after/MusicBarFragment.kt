package com.example.after

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first

class MusicBarFragment : Fragment() {
    private lateinit var textTitle: TextView
    private lateinit var textArtist: TextView
    private lateinit var btnPrev: ImageButton
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var seekBar: SeekBar
    private var mediaPlayer: MediaPlayer? = null
    private var songList: List<Song> = emptyList()
    private var currentIndex: Int = 0
    private val handler = Handler(Looper.getMainLooper())
    private var isUserSeeking = false

    companion object {
        private var instance: MusicBarFragment? = null
        private var pendingSongList: List<Song>? = null
        fun playSongList(songs: List<Song>) {
            if (instance != null && instance!!.isAdded) {
                instance!!.setSongListAndPlay(songs)
            } else {
                pendingSongList = songs
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) instance = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_music_bar, container, false)
        textTitle = view.findViewById(R.id.textBarTitle)
        textArtist = view.findViewById(R.id.textBarArtist)
        btnPrev = view.findViewById(R.id.btnBarPrev)
        btnPlayPause = view.findViewById(R.id.btnBarPlayPause)
        btnNext = view.findViewById(R.id.btnBarNext)
        seekBar = view.findViewById(R.id.barSeek)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Do not auto-play any song on launch
        lifecycleScope.launch {
            val context = requireContext().applicationContext
            val db = MusicDatabase.getDatabase(context)
            songList = withContext(Dispatchers.IO) {
                db.musicDao().getAllSongs().first()
            }
            // Do not call playSong(0) here
        }
        btnPlayPause.setOnClickListener { togglePlayPause() }
        btnNext.setOnClickListener { playNext() }
        btnPrev.setOnClickListener { playPrev() }
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && mediaPlayer != null) {
                    isUserSeeking = true
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = true
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = false
                mediaPlayer?.seekTo(seekBar?.progress ?: 0)
            }
        })
        updateSeekBar()
        pendingSongList?.let {
            setSongListAndPlay(it)
            pendingSongList = null
        }
    }

    private fun playSong(index: Int) {
        if (songList.isEmpty()) return
        currentIndex = index.coerceIn(0, songList.size - 1)
        val song = songList[currentIndex]
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(song.filePath)
            prepare()
            start()
        }
        textTitle.text = song.title
        textArtist.text = song.artist
        btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
        seekBar.max = mediaPlayer?.duration ?: 0
        seekBar.progress = 0
        mediaPlayer?.setOnCompletionListener { playNext() }
        mediaPlayer?.setOnErrorListener { _, _, _ ->
            Toast.makeText(requireContext(), "Playback error", Toast.LENGTH_SHORT).show()
            true
        }
    }

    private fun playNext() {
        if (songList.isEmpty()) return
        val nextIndex = (currentIndex + 1) % songList.size
        playSong(nextIndex)
    }

    private fun playPrev() {
        if (songList.isEmpty()) return
        val prevIndex = if (currentIndex - 1 < 0) songList.size - 1 else currentIndex - 1
        playSong(prevIndex)
    }

    private fun togglePlayPause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
            } else {
                it.start()
                btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
            }
        }
    }

    private fun updateSeekBar() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (!isUserSeeking && mediaPlayer != null) {
                    seekBar.progress = mediaPlayer?.currentPosition ?: 0
                }
                handler.postDelayed(this, 500)
            }
        }, 500)
    }

    private fun setSongListAndPlay(songs: List<Song>) {
        songList = songs
        if (songList.isNotEmpty()) {
            playSong(0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacksAndMessages(null)
    }
} 