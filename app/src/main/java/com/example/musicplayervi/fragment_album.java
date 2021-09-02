package com.example.musicplayervi;

import static com.example.musicplayervi.MainActivity.musicfiles;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class fragment_album extends Fragment {


    RecyclerView recyclerView;
    AlbumAdapter albumAdaptor;

    public fragment_album() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_album, container, false);
        recyclerView=view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        if(musicfiles!=null) {
            if (!(musicfiles.size() < 1)) {
                albumAdaptor = new AlbumAdapter(getContext(), musicfiles);
                recyclerView.setAdapter(albumAdaptor);
                recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));
            }

        }
        return view;
    }
}