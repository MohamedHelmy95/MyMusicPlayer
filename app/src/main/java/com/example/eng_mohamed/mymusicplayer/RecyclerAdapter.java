package com.example.eng_mohamed.mymusicplayer;

/**
 * Created by Eng_Mohamed on 11/8/2016.
 */
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyHolder> {

    private List<SongModel> mList;
     public static Context con;
    public RecyclerAdapter(List<SongModel> list,Context con) {
        mList = list;
        this.con=con;
    }

    @Override
    public RecyclerAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);

        return new MyHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(RecyclerAdapter.MyHolder holder, int position) {
        SongModel song = mList.get(position);
        holder.bindSong(song);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mItemImage;
        private TextView mItemTitle;
        private TextView mItemArtist;


        public MyHolder(View v) {
            super(v);
            mItemImage = (ImageView) v.findViewById(R.id.thumb);
            mItemTitle = (TextView) v.findViewById(R.id.song_title);
            mItemArtist = (TextView) v.findViewById(R.id.song_Artist);
            v.setOnClickListener(this);
        }

        public void bindSong(SongModel model) {
            mItemArtist.setText(model.getArtist());
            mItemTitle.setText(model.getTitle());
            mItemImage.setImageResource(R.drawable.ic_audiotrack);
        }

        @Override
        public void onClick(View v) {

            ((MainActivity) con).handleOnClick(getAdapterPosition());
        }


    }
}