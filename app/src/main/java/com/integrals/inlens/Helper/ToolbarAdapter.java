package com.integrals.inlens.Helper;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.integrals.inlens.MainActivity;
import com.integrals.inlens.Models.CommunityModel;
import com.integrals.inlens.R;

import java.util.List;

public class ToolbarAdapter extends RecyclerView.Adapter<ToolbarAdapter.ToolbarViewHolder> {

    List<CommunityModel> CommunityDetails;
    Context ctx;


    public ToolbarAdapter(List<CommunityModel> communityDetails, Context ctx) {
        CommunityDetails = communityDetails;
        this.ctx = ctx;
    }

    @NonNull
    @Override
    public ToolbarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ToolbarViewHolder(LayoutInflater.from(ctx).inflate(R.layout.custom_toolbar_item_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ToolbarViewHolder holder, int position) {
        holder.AlbumNameTextview.setText(CommunityDetails.get(position).getTitle());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MainActivity().setVerticalRecyclerView(CommunityDetails.get(position).getCommunityID());
            }
        });
    }

    @Override
    public int getItemCount() {
        return CommunityDetails.size();
    }

    public class ToolbarViewHolder extends RecyclerView.ViewHolder {

        TextView AlbumNameTextview;

        public ToolbarViewHolder(View itemView) {
            super(itemView);

            AlbumNameTextview = itemView.findViewById(R.id.custom_toolbar_item_textview);
        }
    }
}
