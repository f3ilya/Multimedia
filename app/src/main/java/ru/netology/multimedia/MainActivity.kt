package ru.netology.multimedia

import ru.netology.multimedia.adapter.OnInteractionListener
import ru.netology.multimedia.adapter.TracksAdapter
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import ru.netology.multimedia.api.RetrofitClient
import ru.netology.multimedia.dto.Track
import kotlinx.coroutines.launch
import ru.netology.multimedia.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: TracksAdapter

    private var mediaPlayer: MediaPlayer? = null
    private var currentTrack: Track? = null
    private var trackList: List<Track> = emptyList()

    private val baseAudioUrl =
        "https://raw.githubusercontent.com/netology-code/andad-homeworks/master/09_multimedia/data/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupListeners()
        loadAlbum()
    }

    private fun setupRecyclerView() {
        adapter = TracksAdapter(object : OnInteractionListener {
            override fun onLike(track: Track) {
                track.isLiked = !track.isLiked
                adapter.notifyDataSetChanged()
            }

            override fun onPlay(track: Track) {
                switchTrack(track)
            }
        })
        binding.listTracks.adapter = adapter
    }

    private fun setupListeners() {
        binding.playAlbum.setOnClickListener {
            if (trackList.isNotEmpty()) {
                val track = currentTrack ?: trackList.first()
                switchTrack(track)
            }
        }

        binding.retryButton.setOnClickListener {
            loadAlbum()
        }
    }

    private fun playAndPause(track: Track, flag: Boolean) {
        track.isPlaying = flag
        binding.playAlbum.isChecked = flag
    }

    private fun switchTrack(track: Track) {
        if (currentTrack?.id == track.id) {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                playAndPause(track, false)
            } else {
                mediaPlayer?.start()
                playAndPause(track, true)
            }
        } else {
            currentTrack?.isPlaying = false
            mediaPlayer?.release()

            currentTrack = track
            playAndPause(track, true)

            startMediaPlayer(track)
        }
        adapter.notifyDataSetChanged()
    }

    private fun startMediaPlayer(track: Track) {
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA).build()
            )

            setDataSource(baseAudioUrl + track.file)
            prepareAsync()

            setOnPreparedListener {
                it.start()
            }

            setOnCompletionListener {
                playNextTrack()
            }

            setOnErrorListener { _, _, _ ->
                Toast.makeText(
                    this@MainActivity, getString(R.string.playback_error), Toast.LENGTH_SHORT
                ).show()
                false
            }
        }
    }

    private fun playNextTrack() {
        if (trackList.isEmpty()) return

        currentTrack?.isPlaying = false

        val currentIndex = trackList.indexOfFirst { it.id == currentTrack?.id }
        val nextTrack = if (currentIndex == 1 || currentIndex == trackList.lastIndex) {
            trackList.first()
        } else {
            trackList[currentIndex + 1]
        }

        mediaPlayer?.release()
        currentTrack = nextTrack
        nextTrack.isPlaying = true
        binding.playAlbum.isChecked = true

        adapter.notifyDataSetChanged()
        startMediaPlayer(nextTrack)
    }

    private fun loadAlbum() {
        binding.apply {
            shimmer.startShimmer()
            shimmer.isVisible = true
            mainCL.isVisible = false
            error.isVisible = false
            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.apiService.getAlbum()

                    if (!response.isSuccessful) {
                        when (response.code()) {
                            404 -> throw RuntimeException(getString(R.string.album_not_found))
                            500 -> throw RuntimeException(getString(R.string.server_error))
                            else -> throw RuntimeException(
                                getString(
                                    R.string.error, response.code()
                                )
                            )
                        }
                    }
                    val album =
                        response.body() ?: throw RuntimeException(getString(R.string.body_is_null))

                    title.text = album.title
                    subtitle.text = album.subtitle
                    artist.text = album.artist
                    year.text = album.published
                    genre.text = album.genre

                    trackList = album.tracks
                    adapter.submitList(trackList)

                    shimmer.stopShimmer()
                    shimmer.isVisible = false
                    mainCL.isVisible = true
                } catch (e: Exception) {
                    shimmer.stopShimmer()
                    shimmer.isVisible = false
                    error.isVisible = true

                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.network_error, e.localizedMessage),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}