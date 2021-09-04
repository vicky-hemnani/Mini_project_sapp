package com.example.musicplayervi;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;

import android.content.Context;
import android.content.Intent;
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
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Random;

import static com.example.musicplayervi.AlbumDetailsAdaptor.albumFilesHash;
import static com.example.musicplayervi.MainActivity.musicfiles;
import static com.example.musicplayervi.MainActivity.*;

public class PlayerActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener{


    TextView song_name,artist_name,duration_total,duration_played;
    ImageView cover_art,nextBtn,prevBtn,backBtn,shuffleBtn,repeatBtn,back_btn;
    FloatingActionButton playPauseBtn;
    SeekBar seekBar;
    int position=-1;
    static Uri uri;
    static ArrayList<Music> list_music=new ArrayList<>();
    static MediaPlayer media_obj;
    private Handler handler=new Handler();

    private Thread playThread,preThread,nextThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initViews();
        getIntentFunction();
        song_name.setText(list_music.get(position).getTitle());
        artist_name.setText(list_music.get(position).getArtist());
        media_obj.setOnCompletionListener(this);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(media_obj!=null && fromUser)
                {
                    media_obj.seekTo(progress * 1000);
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
                if(media_obj!=null)
                {
                    int currentpos=media_obj.getCurrentPosition()/1000;
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
            list_music=musicfiles;
        }
        if(list_music!=null)
        {
            playPauseBtn.setImageResource(R.drawable.pause_vec);
            uri=Uri.parse(list_music.get(position).getPath());

        }
        if(media_obj!=null)
        {
            media_obj.stop();
            media_obj.reset();
            media_obj.release();
            media_obj=MediaPlayer.create(getApplicationContext(),uri);
            media_obj.start();

        }
        else {
            media_obj=MediaPlayer.create(getApplicationContext(),uri);
            media_obj.start();
        }
        seekBar.setMax(media_obj.getDuration()/1000);
        metadata(uri);
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
        playThreadBtn();
        nextThreadBtn();
        preThreadBtn();
        super.onResume();

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

    private void prevBtnClicked() {
        if(media_obj.isPlaying())
        {
            media_obj.stop();
            media_obj.reset();
            media_obj.release();
            if(shuffleBool && !repeatBool)
            {
                position=getRandom(list_music.size()-1);
            }
            else if(!shuffleBool && !repeatBool) {
                position=((position-1)<0? (list_music.size()-1):position-1);
            }
            uri=Uri.parse(list_music.get(position).getPath());
            media_obj=MediaPlayer.create(getApplicationContext(),uri);
            metadata(uri);
            song_name.setText(list_music.get(position).getTitle());
            artist_name.setText(list_music.get(position).getArtist());
            seekBar.setMax(media_obj.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(media_obj!=null)
                    {
                        int currentpos=media_obj.getCurrentPosition()/1000;
                        seekBar.setProgress(currentpos);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            media_obj.setOnCompletionListener(this);
            playPauseBtn.setBackgroundResource(R.drawable.pause_vec);
            media_obj.start();
        }
        else {
            media_obj.stop();
            media_obj.reset();
            media_obj.release();
            if(shuffleBool && !repeatBool)
            {
                position=getRandom(list_music.size()-1);
            }
            else if(!shuffleBool && !repeatBool) {
                position=((position-1)<0? (list_music.size()-1):position-1);
            }
            uri=Uri.parse(list_music.get(position).getPath());
            media_obj=MediaPlayer.create(getApplicationContext(),uri);
            metadata(uri);
            song_name.setText(list_music.get(position).getTitle());
            artist_name.setText(list_music.get(position).getArtist());
            seekBar.setMax(media_obj.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(media_obj!=null)
                    {
                        int currentpos=media_obj.getCurrentPosition()/1000;
                        seekBar.setProgress(currentpos);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            media_obj.setOnCompletionListener(this);
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

    private void nextBtnClicked() {
        if(media_obj.isPlaying())
        {
            media_obj.stop();
            media_obj.reset();
            media_obj.release();
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
            media_obj=MediaPlayer.create(getApplicationContext(),uri);
            metadata(uri);
            song_name.setText(list_music.get(position).getTitle());
            artist_name.setText(list_music.get(position).getArtist());
            seekBar.setMax(media_obj.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(media_obj!=null)
                    {
                        int currentpos=media_obj.getCurrentPosition()/1000;
                        seekBar.setProgress(currentpos);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            media_obj.setOnCompletionListener(this);
            playPauseBtn.setBackgroundResource(R.drawable.pause_vec);
            media_obj.start();
        }
        else {
            media_obj.stop();
            media_obj.reset();
            media_obj.release();
            if(shuffleBool && !repeatBool)
            {
                position=getRandom(list_music.size()-1);
            }
            else if(!shuffleBool && !repeatBool) {
                position = ((position + 1) % list_music.size());
            }
            uri=Uri.parse(list_music.get(position).getPath());
            media_obj=MediaPlayer.create(getApplicationContext(),uri);
            metadata(uri);
            song_name.setText(list_music.get(position).getTitle());
            artist_name.setText(list_music.get(position).getArtist());
            seekBar.setMax(media_obj.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(media_obj!=null)
                    {
                        int currentpos=media_obj.getCurrentPosition()/1000;
                        seekBar.setProgress(currentpos);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            media_obj.setOnCompletionListener(this);
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

    private void playPauseBtnClicked() {
        if(media_obj.isPlaying()){
            playPauseBtn.setImageResource(R.drawable.play_vec);
            media_obj.pause();
            seekBar.setMax(media_obj.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(media_obj!=null)
                    {
                        int currentpos=media_obj.getCurrentPosition()/1000;
                        seekBar.setProgress(currentpos);
                    }
                    handler.postDelayed(this,1000);
                }
            });
        }
        else {
            playPauseBtn.setImageResource(R.drawable.pause_vec);
            media_obj.start();
            seekBar.setMax(media_obj.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(media_obj!=null)
                    {
                        int currentpos=media_obj.getCurrentPosition()/1000;
                        seekBar.setProgress(currentpos);
                    }
                    handler.postDelayed(this,1000);
                }
            });
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        nextBtnClicked();
        if(media_obj!=null)
        {
            media_obj=MediaPlayer.create(getApplicationContext(),uri);
            media_obj.start();
            media_obj.setOnCompletionListener(this);
        }
    }
}