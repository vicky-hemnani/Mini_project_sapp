package com.example.musicplayervi;

import static android.content.Context.MODE_PRIVATE;
import static com.example.musicplayervi.MainActivity.likefiles;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;


public class fragment_likes extends Fragment {


    RecyclerView recyclerView;
    LikesAdaptor likesAdaptor;
    public fragment_likes() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_songs, container, false);
        recyclerView=view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);


        if(likefiles!=null) {
            if (!(likefiles.size() < 1)) {
                likesAdaptor = new LikesAdaptor(getContext(), likefiles);
                recyclerView.setAdapter(likesAdaptor);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
            }

        }
        return view;
    }
    @Override
    public void onPause() {
        super.onPause();
        Log.d("Liked", "onPause: here it is");
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(likefiles);
        editor.putString("task list", json);
        editor.apply();
    }

}