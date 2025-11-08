package com.example.agrokrishiseva.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.agrokrishiseva.R
import com.example.agrokrishiseva.UserData
import java.text.SimpleDateFormat
import java.util.*

class AdminUserAdapter(
    private val users: List<UserData>,
    private val onUserClick: (UserData) -> Unit
) : RecyclerView.Adapter<AdminUserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserName: TextView = itemView.findViewById(R.id.tv_user_name)
        val tvUserEmail: TextView = itemView.findViewById(R.id.tv_user_email)
        val tvFarmLocation: TextView = itemView.findViewById(R.id.tv_farm_location)
        val tvJoinDate: TextView = itemView.findViewById(R.id.tv_join_date)
        val tvOrderCount: TextView = itemView.findViewById(R.id.tv_order_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        
        holder.tvUserName.text = user.name.ifEmpty { "No Name" }
        holder.tvUserEmail.text = user.email
        holder.tvFarmLocation.text = user.farmLocation.ifEmpty { "Location not set" }
        
        // Format join date
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        holder.tvJoinDate.text = "Joined: ${dateFormat.format(Date(user.joinDate))}"
        
        holder.tvOrderCount.text = "Orders: ${user.totalOrders}"
        
        holder.itemView.setOnClickListener {
            onUserClick(user)
        }
    }

    override fun getItemCount(): Int = users.size
}