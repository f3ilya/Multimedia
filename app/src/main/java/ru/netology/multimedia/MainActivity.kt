package ru.netology.multimedia

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import ru.netology.multimedia.adapter.OnInteractionListener
import ru.netology.multimedia.adapter.TracksAdapter
import ru.netology.multimedia.databinding.ActivityMainBinding
import ru.netology.multimedia.dto.Track
import ru.netology.multimedia.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: TracksAdapter
    private val viewModel: MainViewModel by viewModels()

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
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = TracksAdapter(object : OnInteractionListener {
            override fun onLike(track: Track) {
                viewModel.like(track)
            }

            override fun onPlay(track: Track) {
                viewModel.playTrack(track)
            }
        })
        binding.listTracks.adapter = adapter
    }

    private fun setupListeners() {
        binding.playAlbum.setOnClickListener {
            viewModel.playAlbum()
        }

        binding.retryButton.setOnClickListener {
            viewModel.loadAlbum()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.apply {
                        shimmer.isVisible = state.isLoading
                        if (state.isLoading) {
                            shimmer.startShimmer()
                            mainCL.isVisible = false
                            error.isVisible = false
                        } else {
                            shimmer.stopShimmer()
                        }

                        state.error?.let { uiText ->
                            mainCL.isVisible = false
                            error.isVisible = true

                            val errorMessage = uiText.asString(this@MainActivity)
                            Toast.makeText(
                                this@MainActivity,
                                errorMessage,
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        if (!state.isLoading && state.error == null) {
                            mainCL.isVisible = true
                            error.isVisible = false

                            state.album?.let { album ->
                                title.text = album.title
                                subtitle.text = album.subtitle
                                artist.text = album.artist
                                year.text = album.published
                                genre.text = album.genre
                            }

                            adapter.submitList(state.tracks)
                            playAlbum.isChecked = state.isAlbumPlaying
                        }
                    }
                }
            }
        }
    }
}