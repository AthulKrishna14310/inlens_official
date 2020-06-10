package com.integrals.inlens.Helper;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.integrals.inlens.R;

public class MainCommunityViewHolder extends RecyclerView.ViewHolder {


    public ImageView AlbumCoverButton, AlbumOptions;
    public TextView AlbumNameTextView;
    public TextView AlbumDescriptionTextView;
    public Button Indicator;
    public Button menuOptionsButton;
    public CardView coverPhotoChangeCard;

    public MainCommunityViewHolder(View itemView) {
        super(itemView);
        AlbumOptions = itemView.findViewById(R.id.albumcard_options);
        AlbumCoverButton = itemView.findViewById(R.id.albumcard_image_view);
        AlbumNameTextView = itemView.findViewById(R.id.album_card_textview);
        menuOptionsButton = itemView.findViewById(R.id.coverphotochangebutton);
        coverPhotoChangeCard = itemView.findViewById(R.id.cardcoverphotochange);

        Indicator = itemView.findViewById(R.id.indication_button);
    }


}