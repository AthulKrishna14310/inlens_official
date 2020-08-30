package com.integrals.inlens.Activities.kotlin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.integrals.inlens.Models.PostModel
import com.integrals.inlens.R
import kotlinx.android.synthetic.main.photoview_item_layout.view.*

class ViewPagerAdapter(val posts: List<PostModel>,val context: Context) : RecyclerView.Adapter<ViewPagerAdapter.ViewPagerViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerViewHolder {
        return ViewPagerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.photoview_item_layout,parent,false))
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    override fun onBindViewHolder(holder: ViewPagerViewHolder, position: Int) {

        Glide.with(context)  //2
                .load(posts.get(position).uri) //3
                .into(holder.itemView.photoview_item_photoview) //8

    }

    inner class ViewPagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}