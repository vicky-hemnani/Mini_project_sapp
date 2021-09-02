package com.example.musicplayervi;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.MyHolder> {

    private Context nContext;
    private ArrayList<Music> albumFiles;
    View view;

    public AlbumAdapter(Context nContext, ArrayList<Music> albumFiles) {
        this.nContext = nContext;
        this.albumFiles = albumFiles;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view= LayoutInflater.from(nContext).inflate(R.layout.album_item,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        holder.album_name.setText(albumFiles.get(position).getAlbum());
        byte[] image=getalbum(albumFiles.get(position).getPath());
        if(image!=null)
        {
            Glide.with(nContext).asBitmap().load(image).into(holder.album_img);
        }
        else
        {
            Glide.with(nContext).asBitmap().load(R.drawable.logo4).into(holder.album_img);
        }
    }

    @Override
    public int getItemCount() {
        return albumFiles.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        ImageView album_img;
        TextView album_name;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            album_img=itemView.findViewById(R.id.album_img);
            album_name=itemView.findViewById(R.id.album_name);
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
