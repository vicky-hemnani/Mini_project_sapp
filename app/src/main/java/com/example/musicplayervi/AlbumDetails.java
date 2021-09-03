package com.example.musicplayervi;

import static com.example.musicplayervi.MainActivity.musicfiles;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class AlbumDetails extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageView albumPic;
    String albumName;
    ArrayList<Music> albumSongs=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_details);
        recyclerView = findViewById(R.id.recyclerView);
        albumPic = findViewById(R.id.albumPic);
        albumName = getIntent().getStringExtra("albumName");
        int j = 0;
        String st;
        for (int i = 0; i < musicfiles.size(); i++)
        {
            st=musicfiles.get(i).getAlbum().toString();
            if(albumName.equals(st))
            {
                Log.d("Album Name", "onCreate: "+st+"--------"+albumName);
                albumSongs.add(j,musicfiles.get(i));
                j++;
            }
        }
        byte[] image=getalbum(albumSongs.get(0).getPath());
        Log.d("what", "Waht is error "+albumName);
        if(image!=null)
        {
            Glide.with(this).load(image).into(albumPic);
        }
        else
        {
            Glide.with(this).load(R.drawable.imgg).into(albumPic);
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