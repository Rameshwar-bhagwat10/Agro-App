package com.example.agrokrishiseva.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.agrokrishiseva.R
import java.text.SimpleDateFormat
import java.util.*

class RecentUsersAdapter(
    private var users: List<RecentUserData>
) : RecyclerView.Adapter<RecentUsersAdapter.RecentUserViewHolder>() {

    class RecentUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserName: TextView = itemView.findViewById(R.id.tv_user_name)
        val tvUserEmail: TextView = itemView.findViewById(R.id.tv_user_email)
        val tvJoinDate: TextView = itemView.findViewById(R.id.tv_join_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentUserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_user, parent, false)
        return RecentUserViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecentUserViewHolder, position: Int) {
        val user = users[position]
        
        holder.tvUserName.text = user.name.ifEmpty { "No Name" }
        holder.tvUserEmail.text = user.email
        
        val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        holder.tvJoinDate.text = dateFormat.format(Date(user.joinDate))
    }

    override fun getItemCount(): Int = users.size

    fun updateUsers(newUsers: List<RecentUserData>) {
        users = newUsers
        notifyDataSetChanged()
    }
}