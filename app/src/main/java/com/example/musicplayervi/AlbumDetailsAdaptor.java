package com.example.musicplayervi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class AlbumDetailsAdaptor extends RecyclerView.Adapter<AlbumDetailsAdaptor.MyHolder> {

    private Context nContext;
    static ArrayList<Music> albumFilesHash;
    View view;

    public AlbumDetailsAdaptor(Context nContext, ArrayList<Music> albumFilesHash) {
        this.nContext = nContext;
        this.albumFilesHash=albumFilesHash;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        view= LayoutInflater.from(nContext).inflate(R.layout.music_items,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.album_name.setText(albumFilesHash.get(position).getTitle());
        byte[] image=getalbum(albumFilesHash.get(position).getPath());
        if(image!=null)
        {
            Glide.with(nContext).asBitmap().load(image).into(holder.album_img);
        }
        else
        {
            Glide.with(nContext).asBitmap().load(R.drawable.imgg).into(holder.album_img);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(nContext,PlayerActivity.class);
                intent.putExtra("sender","albumDetails");
                intent.putExtra("position",position);
                nContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return albumFilesHash.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        ImageView album_img;
        TextView album_name;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            album_img=itemView.findViewById(R.id.music_img);
            album_name=itemView.findViewById(R.id.music_file_name);
        }
    }

    private byte[] getalbum(String uri)
    {
        MediaMetadataRetriever retriever=new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art=retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
}
