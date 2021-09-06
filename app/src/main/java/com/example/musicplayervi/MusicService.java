package com.example.musicplayervi;

import static com.example.musicplayervi.PlayerActivity.list_music;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {

    IBinder myBinder=new MyBinder();
    MediaPlayer mediaPlayer;
    ArrayList<Music> musicFiles=new ArrayList<>();
    Uri uri;
    int position=-1;
    ActionPlaying actionPlaying;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("Bind", "onBind: Method" );
        return myBinder;
    }



    public class MyBinder extends Binder{
        MusicService getService(){
            return MusicService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int mysposition=intent.getIntExtra("servicePosition",-1);
        if(mysposition!=-1) {
            playMedia(mysposition);
        }
        return START_STICKY;
    }

    private void playMedia(int startposition) {
        musicFiles=list_music;
        position=startposition;
        if(mediaPlayer!=null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            if(musicFiles!=null)
            {
                createMediaPlayer(position);
                mediaPlayer.start();
            }
        }
        else {
            createMediaPlayer(position);
            mediaPlayer.start();
        }
    }

    void start()
    {
        mediaPlayer.start();
    }

    boolean isPlaying()
    {
        return mediaPlayer.isPlaying();
    }

    void stop(){
        mediaPlayer.stop();
    }

    void release(){
        mediaPlayer.release();
    }

    void reset()
    {
        mediaPlayer.reset();
    }

    int getDuration()
    {
        return mediaPlayer.getDuration();
    }
    void seekTo(int position)
    {
         mediaPlayer.seekTo(position);
    }

    void pause()
    {
        mediaPlayer.pause();
    }

    int getCurrentPosition()
    {
        return mediaPlayer.getCurrentPosition();
    }
    void createMediaPlayer(int position){
        uri=Uri.parse(musicFiles.get(position).getPath());
        mediaPlayer=MediaPlayer.create(getBaseContext(),uri);
    }

    void onCompleted(){
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(actionPlaying!=null) {
            actionPlaying.nextBtnClicked();
        }
        createMediaPlayer(position);
        mediaPlayer.start();
        onCompleted();

    }
}
