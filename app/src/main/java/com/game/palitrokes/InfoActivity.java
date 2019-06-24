package com.game.palitrokes;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.VideoView;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        getSupportActionBar().hide();

        VideoView infoVideoView = findViewById(R.id.videoView);
        infoVideoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video));
        infoVideoView.start();

        infoVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
               salir();
            }
        });


    }

    @Override
    public void onBackPressed() {
        salir();
    }

    private void salir() {
        Intent juegoIntent = new Intent(getApplicationContext(), MainActivity.class);
        finish();
        startActivity(juegoIntent);


    }
}
