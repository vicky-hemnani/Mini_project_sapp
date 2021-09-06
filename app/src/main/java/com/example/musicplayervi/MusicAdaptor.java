package com.example.musicplayervi;

import static com.example.musicplayervi.MainActivity.likefiles;
import static com.example.musicplayervi.MainActivity.musicfiles;
import static com.example.musicplayervi.MainActivity.shuffleBool;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;

public class MusicAdaptor extends RecyclerView.Adapter<MusicAdaptor.MyViewHolder> {

    Boolean b=false;
    int pos=0;
    private Context nContext;
    static ArrayList<Music> nFiles;

    MusicAdaptor(Context context,ArrayList<Music> nFiles)
    {
        this.nFiles=nFiles;
        this.nContext=context;
    }

    @NonNull
    @Override

    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(nContext).inflate(R.layout.music_items,parent,false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicAdaptor.MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.file_name.setText(nFiles.get(position).getTitle());
        b=false;
        String ids=nFiles.get(position).getId();
        if(likefiles.isEmpty()==false){
        for(Music mu:likefiles)
        {
            if(mu.getId().equals(ids))
            {
                b=true;
            }
            Log.d("What is like", "Say so: "+mu);
        }
            Log.d("LikesFile", "is null "+likefiles.get(0)+" ----"+nFiles.get(position));
            if(b==true){
            holder.like_music.setImageResource(R.drawable.heart_on);}
        else{
            holder.like_music.setImageResource(R.drawable.heart_vec);}}
        byte[] image=getalbum(nFiles.get(position).getPath());
        if(image!=null)
        {
            Glide.with(nContext).asBitmap().load(image).into(holder.album_art);
        }
        else
        {
            Glide.with(nContext).asBitmap().load(R.drawable.logo4).into(holder.album_art);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(nContext,PlayerActivity.class);
                intent.putExtra("position",position);
                nContext.startActivity(intent);
            }
        });
        holder.menu_opt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu=new PopupMenu(nContext,v);
                popupMenu.getMenuInflater().inflate(R.menu.popup,popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId())
                    {
                        case R.id.delete:
                           // Toast.makeText(nContext,"Deleted",Toast.LENGTH_SHORT).show();
                            deleteSong(position,v);
                    }
                    return true;
                });
            }
        });

        holder.like_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean likeBool=false;
                int i=0;
                String ids=nFiles.get(position).getId();
                if(likefiles.isEmpty()==false) {
                    for (Music mu : likefiles) {

                        if (mu.getId().equals(ids)) {
                            likeBool = true;
                            pos=i;
                        }
                        i++;
                        Log.d("What is like", "Say so: " + mu);
                    }
                }
                //Boolean likeBool=(holder.like_music.getDrawable().getConstantState()== nContext.getResources().getDrawable( R.drawable.heart_vec).getConstantState());
                if(likeBool)
                {
                    likefiles.remove(pos);
                    holder.like_music.setImageResource(R.drawable.heart_vec);
                    //likeBool=false;
                }
                else {
                    likefiles.add(musicfiles.get(position));
                    holder.like_music.setImageResource(R.drawable.heart_on);
                }
            }
        });

    }

    private void deleteSong(int position, View v) {
        Uri contentUri= ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                Long.parseLong(nFiles.get(position).getId()));
        File file=new File(nFiles.get(position).getPath());
        Log.d("FIle problem","Why not deleting + ................"+file+".........."+contentUri);
        boolean deleted= file.delete();
        if(deleted) {

            nContext.getContentResolver().delete(contentUri,null,null);
            nFiles.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, nFiles.size());
            Snackbar.make(v, "Song Deleted", Snackbar.LENGTH_LONG)
                    .show();
        }
        else {
            Snackbar.make(v, "Song can't be Deleted", Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public int getItemCount() {
        return nFiles.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {

        TextView file_name;
        ImageView album_art,menu_opt,like_music;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            file_name=itemView.findViewById(R.id.music_file_name);
            album_art=itemView.findViewById(R.id.music_img);
            menu_opt=itemView.findViewById(R.id.menu_more);
            like_music=itemView.findViewById(R.id.like_song);

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

    void updateList(ArrayList<Music> musicFilesArrayList)
    {
        nFiles=new ArrayList<>();
        nFiles.addAll(musicFilesArrayList);
        notifyDataSetChanged();
    }
}
