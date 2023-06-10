package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.floor
import kotlin.math.log10

interface OnInteractionListener {
    fun onLike(post: Post)
    fun onRepost(post: Post)
    fun onEdit(post: Post)
    fun onRemove(post: Post)
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener
) : ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}


class PostViewHolder(
    private val binding: CardPostBinding, private val onIntercationListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        binding.apply {
            AuthorTv.text = post.author
            PublishedTv.text = post.published
            ContentTv.text = post.content
            NumberOfLikesTv.text = prettyCount(post.likes)
            NumberOfRepostsTv.text = prettyCount(post.reposted)
            LikeIb.setImageResource(
                if (post.likedByMe) R.drawable.ic_liked else R.drawable.ic_like
            )
            LikeIb.setOnClickListener {
                onIntercationListener.onLike(post)
            }
            RepostIb.setOnClickListener {
                onIntercationListener.onRepost(post)
            }
            MenuIb.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onIntercationListener.onRemove(post)
                                true
                            }

                            R.id.edit -> {
                                onIntercationListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }
        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }

}

fun prettyCount(numb: Int): String? {
    val value = floor(log10(numb.toDouble())).toInt()
    return when {
        value < 3 -> numb.toString() // < 1000
        value < 4 -> DecimalFormat("#.#").apply { roundingMode = RoundingMode.FLOOR }
            .format(numb.toDouble() / 1000) + "K" // < 10_000
        value < 6 -> DecimalFormat("#").apply { roundingMode = RoundingMode.FLOOR }
            .format(numb.toDouble() / 1000) + "K" // < 1_000_000
        else -> DecimalFormat("#.#").apply { roundingMode = RoundingMode.FLOOR }
            .format(numb.toDouble() / 1_000_000) + "M"
    }
}





