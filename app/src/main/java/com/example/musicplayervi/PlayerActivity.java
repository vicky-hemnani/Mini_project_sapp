package com.example.musicplayervi;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.palette.graphics.Palette;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaParser;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Random;

import static com.example.musicplayervi.AlbumDetailsAdaptor.albumFilesHash;
import static com.example.musicplayervi.ApplicationClass.ACTION_NEXT;
import static com.example.musicplayervi.ApplicationClass.ACTION_PLAY;
import static com.example.musicplayervi.ApplicationClass.ACTION_PREVIOUS;
import static com.example.musicplayervi.ApplicationClass.CHANNEL_ID_2;
import static com.example.musicplayervi.MainActivity.musicfiles;
import static com.example.musicplayervi.MainActivity.*;
import static com.example.musicplayervi.MusicAdaptor.nFiles;

public class PlayerActivity extends AppCompatActivity implements ActionPlaying, ServiceConnection {


    TextView song_name,artist_name,duration_total,duration_played;
    ImageView cover_art,nextBtn,prevBtn,backBtn,shuffleBtn,repeatBtn,back_btn;
    FloatingActionButton playPauseBtn;
    SeekBar seekBar;
    int position=-1;
    static Uri uri;
    static ArrayList<Music> list_music=new ArrayList<>();
    //static MediaPlayer media_obj;
    private Handler handler=new Handler();
    MusicService musicService;
    MediaSessionCompat mediaSessionCompat;
    private Thread playThread,preThread,nextThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        mediaSessionCompat=new MediaSessionCompat(getBaseContext(),"My Audio");
        initViews();
        getIntentFunction();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(musicService!=null && fromUser)
                {
                    musicService.seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(musicService!=null)
                {
                    int currentpos=musicService.getCurrentPosition()/1000;
                    seekBar.setProgress(currentpos);
                    duration_played.setText(formattedText(currentpos));

                }
                handler.postDelayed(this,1000);
            }
        });
        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(shuffleBool)
                {
                    shuffleBool=false;
                    shuffleBtn.setImageResource(R.drawable.shuffle_vec);
                }
                else {
                    shuffleBool=true;
                    shuffleBtn.setImageResource(R.drawable.shuffle_on);
                }
            }
        });
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatBool)
                {
                    repeatBool=false;
                    repeatBtn.setImageResource(R.drawable.repeat_vec);
                }
                else
                {
                    repeatBool=true;
                    repeatBtn.setImageResource(R.drawable.repeat_on);
                }
            }
        });

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private String formattedText(int currentpos)
    {
        String total_out="";
        String total_New="";
        String seconds=String.valueOf(currentpos%60);
        String minutes=String.valueOf(currentpos/60);
        total_out=minutes + ":" +seconds;
        total_New=minutes + ":" + "0"+seconds;
        if(seconds.length()==1)
        {
            return total_New;
        }
        else {
            return total_out;
        }

    }
    private void getIntentFunction() {
        position=getIntent().getIntExtra("position",-1);
        String sender=getIntent().getStringExtra("sender");
        if(sender!=null && sender.equals("albumDetails"))
        {
            Log.d("check", "getIntentFunction: HEllo");
            list_music=albumFilesHash;
        }
        else if (sender!=null && sender.equals("Playlikes"))
        {
            list_music=likefiles;
        }
        else {
            list_music=nFiles;
        }
        if(list_music!=null)
        {
            playPauseBtn.setImageResource(R.drawable.pause_vec);
            uri=Uri.parse(list_music.get(position).getPath());

        }
        /*if(musicService!=null)
        {
            musicService.stop();
            musicService.release();
            //media_obj=MediaPlayer.create(getApplicationContext(),uri);
        }

            //media_obj=MediaPlayer.create(getApplicationContext(),uri);
            musicService.createMediaPlayer(position);
            musicService.start();*/
        showNotification(R.drawable.pause_vec);
        Intent intent=new Intent(this,MusicService.class);
        intent.putExtra("servicePosition",position);
        startService(intent);

    }

    private void initViews() {
        song_name=findViewById(R.id.song_name);
        artist_name=findViewById(R.id.song_artist);
        duration_total=findViewById(R.id.durationtotal);
        duration_played=findViewById(R.id.durationplayed);
        cover_art=findViewById(R.id.cover_art);
        nextBtn=findViewById(R.id.skip_next);
        prevBtn=findViewById(R.id.skip_prev);
        backBtn=findViewById(R.id.back_btn);
        shuffleBtn=findViewById(R.id.shuffle);
        repeatBtn=findViewById(R.id.repeat);
        playPauseBtn=findViewById(R.id.play_pause);
        seekBar=findViewById(R.id.seekbar);
        back_btn=findViewById(R.id.back_btn);
        shuffleBool=false;
        repeatBool=false;

    }

    private void metadata(Uri uri)
    {
        MediaMetadataRetriever retriever=new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int durationTotal=Integer.parseInt(list_music.get(position).getDuration())/1000;
        duration_total.setText(formattedText(durationTotal));
        byte[] art=retriever.getEmbeddedPicture();
        Bitmap bitmap;
        if(art!=null)
        {
            bitmap= BitmapFactory.decodeByteArray(art,0,art.length);
            ImageAniation(this,cover_art,bitmap);
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@Nullable Palette palette) {
                    Palette.Swatch swatch=palette.getDominantSwatch();
                    if(swatch!=null)
                    {
                        ImageView gradient=findViewById(R.id.imageViewGradient);
                        RelativeLayout nContainer=findViewById(R.id.nContainer);
                        gradient.setBackgroundResource(R.drawable.gradient_bg);
                        nContainer.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gradientDrawable=new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,new int[]{swatch.getRgb(),0x00000000});
                        gradient.setBackground(gradientDrawable);
                        GradientDrawable gradientDrawableBg=new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,new int[]{swatch.getRgb(),swatch.getRgb()});
                        nContainer.setBackground(gradientDrawableBg);
                        song_name.setTextColor(swatch.getTitleTextColor());
                        artist_name.setTextColor(swatch.getBodyTextColor());
                    }
                    else {
                        ImageView gradient=findViewById(R.id.imageViewGradient);
                        RelativeLayout nContainer=findViewById(R.id.nContainer);
                        gradient.setBackgroundResource(R.drawable.gradient_bg);
                        nContainer.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gradientDrawable=new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,new int[]{0xff000000,0x00000000});
                        gradient.setBackground(gradientDrawable);
                        GradientDrawable gradientDrawableBg=new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,new int[]{0xff000000,0xff000000});
                        nContainer.setBackground(gradientDrawableBg);
                        song_name.setTextColor(Color.WHITE);
                        artist_name.setTextColor(Color.DKGRAY);
                    }
                }
            });
        }
        else {
            Glide.with(this).asBitmap().load(R.drawable.imgg).into(cover_art);
            ImageView gradient=findViewById(R.id.imageViewGradient);
            RelativeLayout nContainer=findViewById(R.id.nContainer);
            gradient.setBackgroundResource(R.drawable.gradient_bg);
            nContainer.setBackgroundResource(R.drawable.main_bg);
            song_name.setTextColor(Color.WHITE);
            artist_name.setTextColor(Color.DKGRAY);

        }
    }

    public void ImageAniation(Context context,ImageView imageView,Bitmap bitmap)
    {
        Animation animout= AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        Animation animin= AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        animout.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);
                animin.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                imageView.startAnimation(animin);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(animout);

    }

    protected void onResume() {
        Intent intent=new Intent(this,MusicService.class);
        bindService(intent,this,BIND_AUTO_CREATE);
        playThreadBtn();
        nextThreadBtn();
        preThreadBtn();
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    private void preThreadBtn() {
        preThread=new Thread()
        {
            @Override
            public void run()
            {
                super.run();
                prevBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        prevBtnClicked();
                    }
                });
            }
        };
        preThread.start();
    }

    public void prevBtnClicked() {
        if(musicService.isPlaying())
        {
            musicService.stop();
            musicService.reset();
            musicService.release();
            if(shuffleBool && !repeatBool)
            {
                position=getRandom(list_music.size()-1);
            }
            else if(!shuffleBool && !repeatBool) {
                position=((position-1)<0? (list_music.size()-1):position-1);
            }
            uri=Uri.parse(list_music.get(position).getPath());
            //media_obj=MediaPlayer.create(getApplicationContext(),uri);
            musicService.createMediaPlayer(position);
            metadata(uri);
            song_name.setText(list_music.get(position).getTitle());
            artist_name.setText(list_music.get(position).getArtist());
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService!=null)
                    {
                        int currentpos=musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(currentpos);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            //media_obj.setOnCompletionListener(this);
            musicService.onCompleted();
            showNotification(R.drawable.pause_vec);
            playPauseBtn.setBackgroundResource(R.drawable.pause_vec);
            musicService.start();
        }
        else {
            musicService.stop();
            musicService.reset();
            musicService.release();
            if(shuffleBool && !repeatBool)
            {
                position=getRandom(list_music.size()-1);
            }
            else if(!shuffleBool && !repeatBool) {
                position=((position-1)<0? (list_music.size()-1):position-1);
            }
            uri=Uri.parse(list_music.get(position).getPath());
            //media_obj=MediaPlayer.create(getApplicationContext(),uri);
            musicService.createMediaPlayer(position);
            metadata(uri);
            song_name.setText(list_music.get(position).getTitle());
            artist_name.setText(list_music.get(position).getArtist());
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService!=null)
                    {
                        int currentpos=musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(currentpos);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            //media_obj.setOnCompletionListener(this);
            musicService.onCompleted();
            showNotification(R.drawable.play_vec);
            playPauseBtn.setBackgroundResource(R.drawable.play_vec);
        }
    }

    private void nextThreadBtn() {
        nextThread=new Thread()
        {
            @Override
            public void run()
            {
                super.run();
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nextBtnClicked();
                    }
                });
            }
        };
        nextThread.start();
    }

    public void nextBtnClicked() {
        if(musicService.isPlaying())
        {
            musicService.stop();
            musicService.reset();
            musicService.release();
            if(shuffleBool && !repeatBool)
            {
                position=getRandom(list_music.size()-1);
            }
            else if(!shuffleBool && !repeatBool)
            {
                position=((position+1)%list_music.size());
            }
            //else

            uri=Uri.parse(list_music.get(position).getPath());
            musicService.createMediaPlayer(position);
            //media_obj=MediaPlayer.create(getApplicationContext(),uri);
            metadata(uri);
            song_name.setText(list_music.get(position).getTitle());
            artist_name.setText(list_music.get(position).getArtist());
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService!=null)
                    {
                        int currentpos=musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(currentpos);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            //media_obj.setOnCompletionListener(this);
            musicService.onCompleted();
            showNotification(R.drawable.pause_vec);
            playPauseBtn.setBackgroundResource(R.drawable.pause_vec);
            musicService.start();
        }
        else {
            musicService.stop();
            musicService.reset();
            musicService.release();
            if(shuffleBool && !repeatBool)
            {
                position=getRandom(list_music.size()-1);
            }
            else if(!shuffleBool && !repeatBool) {
                position = ((position + 1) % list_music.size());
            }
            uri=Uri.parse(list_music.get(position).getPath());
            //media_obj=MediaPlayer.create(getApplicationContext(),uri);
            musicService.createMediaPlayer(position);
            metadata(uri);
            song_name.setText(list_music.get(position).getTitle());
            artist_name.setText(list_music.get(position).getArtist());
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService!=null)
                    {
                        int currentpos=musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(currentpos);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            //media_obj.setOnCompletionListener(this);
            musicService.onCompleted();
            showNotification(R.drawable.play_vec);
            playPauseBtn.setBackgroundResource(R.drawable.play_vec);
        }
    }

    private int getRandom(int i) {
        Random random=new Random();

        return random.nextInt(i+1);
    }

    private void playThreadBtn() {
        playThread=new Thread()
        {
            @Override
            public void run()
            {
                  super.run();
                  playPauseBtn.setOnClickListener(new View.OnClickListener() {
                      @Override
                      public void onClick(View v) {
                          playPauseBtnClicked();
                      }
                  });
            }
        };
        playThread.start();
    }

    public void playPauseBtnClicked() {
        if(musicService.isPlaying()){
            playPauseBtn.setImageResource(R.drawable.play_vec);
            showNotification(R.drawable.play_vec);
            musicService.pause();
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService!=null)
                    {
                        int currentpos=musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(currentpos);
                    }
                    handler.postDelayed(this,1000);
                }
            });
        }
        else {
            playPauseBtn.setImageResource(R.drawable.pause_vec);
            showNotification(R.drawable.pause_vec);
            musicService.start();
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService!=null)
                    {
                        int currentpos=musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(currentpos);
                    }
                    handler.postDelayed(this,1000);
                }
            });
        }
    }

    /**@Override
    public void onCompletion(MediaPlayer mp) {
        nextBtnClicked();
        if(musicService!=null)
        {
            //musicService=MediaPlayer.create(getApplicationContext(),uri);
            musicService.createMediaPlayer(position);
            musicService.start();
            musicService.onCompleted();
        }
    }*/

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        MusicService.MyBinder myBinder=(MusicService.MyBinder)service;
        musicService=myBinder.getService();
        Toast.makeText(this,"Connected"+musicService,Toast.LENGTH_SHORT).show();
        seekBar.setMax(musicService.getDuration()/1000);
        metadata(uri);
        song_name.setText(list_music.get(position).getTitle());
        artist_name.setText(list_music.get(position).getArtist());
        //media_obj.setOnCompletionListener(this);
        musicService.onCompleted();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService=null;
    }

    void showNotification(int playPauseBtn)
    {
        Intent intent=new Intent(this,PlayerActivity.class);
        PendingIntent contentIntent=PendingIntent.getActivity(this,0,intent,0);

        Intent previntent=new Intent(this, NotificationReceiver.class).setAction(ACTION_PREVIOUS);
        PendingIntent prevPending=PendingIntent.getBroadcast(this,0,previntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseintent=new Intent(this, NotificationReceiver.class).setAction(ACTION_PLAY);
        PendingIntent pausePending=PendingIntent.getBroadcast(this,0,pauseintent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextintent=new Intent(this, NotificationReceiver.class).setAction(ACTION_NEXT);
        PendingIntent nextPending=PendingIntent.getBroadcast(this,0,nextintent,PendingIntent.FLAG_UPDATE_CURRENT);

        byte[] picture=null;
        picture=getalbum(musicfiles.get(position).getPath());
        Bitmap thumb=null;
        if(picture!=null)
        {
            thumb=BitmapFactory.decodeByteArray(picture,0,picture.length);
        }
        else {
            thumb=BitmapFactory.decodeResource(getResources(),R.drawable.imgg);
        }

        Notification notification=new NotificationCompat.Builder(this,CHANNEL_ID_2)
                .setSmallIcon(playPauseBtn)
                .setLargeIcon(thumb)
                .setContentTitle(musicfiles.get(position).getTitle())
                .setContentText(musicfiles.get(position).getArtist())
                .addAction(R.drawable.skip_prev,"Previous",prevPending)
                .addAction(playPauseBtn,"Pause",pausePending)
                .addAction(R.drawable.skip_next,"Next",nextPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .build();

        NotificationManager notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0,notification);

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