package com.example.musicplayervi;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static com.example.musicplayervi.MainActivity.musicfiles;


public class fragment_songs extends Fragment {

    RecyclerView recyclerView;
    MusicAdaptor musicAdaptor;
    public fragment_songs() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_songs, container, false);
        recyclerView=view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        if(musicfiles!=null) {
            if (!(musicfiles.size() < 1)) {
                musicAdaptor = new MusicAdaptor(getContext(), musicfiles);
                recyclerView.setAdapter(musicAdaptor);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
            }

        }
        return view;
    }
}