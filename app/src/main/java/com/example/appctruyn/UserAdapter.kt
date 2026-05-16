package com.example.appctruyn

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appctruyn.model.User

class UserAdapter(
    private var users: List<User>,
    private val onUserClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    fun updateData(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivAvatar = view.findViewById<ImageView>(R.id.ivUserAvatar)
        private val tvName = view.findViewById<TextView>(R.id.tvUserName)
        private val tvEmail = view.findViewById<TextView>(R.id.tvUserEmail)
        private val tvRole = view.findViewById<TextView>(R.id.tvUserRole)

        fun bind(user: User) {
            tvName.text = user.displayName.ifEmpty { "Người dùng" }
            tvEmail.text = user.email
            tvRole.text = user.role.uppercase()

            val avatarData = user.avatarUrl
            if (avatarData != null) {
                if (avatarData.startsWith("data:image")) {
                    try {
                        val cleanBase64 = if (avatarData.contains(",")) avatarData.split(",")[1] else avatarData
                        val imageBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                        val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        Glide.with(itemView.context)
                            .load(decodedImage)
                            .placeholder(R.drawable.ic_nav_account)
                            .circleCrop()
                            .into(ivAvatar)
                    } catch (e: Exception) {
                        ivAvatar.setImageResource(R.drawable.ic_nav_account)
                    }
                } else {
                    // Trường hợp là URL
                    Glide.with(itemView.context)
                        .load(avatarData)
                        .placeholder(R.drawable.ic_nav_account)
                        .circleCrop()
                        .into(ivAvatar)
                }
            } else {
                ivAvatar.setImageResource(R.drawable.ic_nav_account)
            }

            itemView.setOnClickListener { onUserClick(user) }
        }
    }
}
