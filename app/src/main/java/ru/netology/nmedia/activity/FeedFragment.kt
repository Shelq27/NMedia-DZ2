package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.util.idArg
import ru.netology.nmedia.viewmodel.PostViewModel


@AndroidEntryPoint
class  FeedFragment : Fragment() {

    companion object {
        var Bundle.text by StringArg
        var Bundle.id by idArg
    }

    private val viewModel: PostViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)
        viewModel.loadPost()
        val adapter = PostsAdapter(object : OnInteractionListener {

            override fun onEdit(post: Post) {

                findNavController().navigate(R.id.action_feedFragment_to_editPostFragment,
                    Bundle().also { it.text = post.content })
                viewModel.edit(post)
            }

            override fun onLike(post: Post) {
                viewModel.likeByPost(post)

            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onPlay(post: Post) {
//                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.video))
//                startActivity(intent)
            }

            override fun onOpen(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_postCardFragment,
                    Bundle().also { it.id = post.id })

            }

            override fun onOpenFullScreen(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_imageFullScreenFragment,
                    Bundle().also { it.id = post.id })

            }


            override fun onRepost(post: Post) {
                viewModel.repostById(post.id)
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

        })
        binding.list.adapter = adapter

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swiperefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.retry_loading) { viewModel.loadPost() }.show()
            }
        }
        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest {
                adapter.submitData(it)
            }
        }


//        viewModel.newerCount.observe(viewLifecycleOwner) {
//            if (it > 0) {
//                binding.recentRecording.visibility = View.VISIBLE
//            }
//        }
        binding.recentRecording.setOnClickListener {
            viewModel.loadLocalDBPost()
            binding.recentRecording.visibility = View.GONE
            binding.list.smoothScrollToPosition(0)

        }
        binding.swiperefresh.setOnRefreshListener {
            viewModel.refreshPosts()
        }


        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }
        return binding.root
    }


}



