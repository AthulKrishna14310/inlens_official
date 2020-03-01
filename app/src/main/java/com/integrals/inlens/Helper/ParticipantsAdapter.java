package com.integrals.inlens.Helper;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.integrals.inlens.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ParticipantsAdapter extends RecyclerView.Adapter<ParticipantsAdapter.ParticipantsViewHolder> {

    List<String> ImagesList;
    List<String> NamesList;
    Context context;

    public ParticipantsAdapter(List<String> imagesList, List<String> namesList, Context context) {
        ImagesList = imagesList;
        NamesList = namesList;
        this.context = context;
    }

    @NonNull
    @Override
    public ParticipantsAdapter.ParticipantsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.member_card,parent,false);
        return new ParticipantsAdapter.ParticipantsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParticipantsAdapter.ParticipantsViewHolder holder, int position) {

        if(NamesList.get(position).length() > 6)
        {
            String name =NamesList.get(position).substring(0,5)+"...";
            holder.PName.setText(name);

        }
        else
        {
            holder.PName.setText(NamesList.get(position));

        }

        RequestOptions rq = new RequestOptions().placeholder(R.drawable.image_avatar_background);
        Glide.with(context).load(ImagesList.get(position)).apply(rq).into(holder.PImage);

    }

    @Override
    public int getItemCount() {
        return ImagesList.size();
    }

    public class ParticipantsViewHolder extends RecyclerView.ViewHolder {

        CircleImageView PImage;
        TextView PName;

        public ParticipantsViewHolder(View itemView) {
            super(itemView);

            PImage = itemView.findViewById(R.id.participants_profile_pic);
            PName = itemView.findViewById(R.id.participants_username);
        }
    }
}
