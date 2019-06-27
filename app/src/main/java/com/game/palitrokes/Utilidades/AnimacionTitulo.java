package com.game.palitrokes.Utilidades;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.game.palitrokes.R;

import java.util.Random;

public class AnimacionTitulo extends AsyncTask<Void, String, Boolean> {

    private boolean running;
    private ImageView imageView;
    private Context context;


    public void recuperarImageView (Context context, ImageView imageView) {
        this.context = context;
        this.imageView = imageView;
    }

    @Override
    protected void onPreExecute() {
        running = true;

    }

    @Override
    protected void onProgressUpdate(String... values) {
        String imagen = values[0];
        int resource = context.getResources().getIdentifier(imagen, "drawable", "com.game.palitrokes");
        imageView.setImageResource(resource);
      //  Log.d(Constantes.TAG, "Imagen cambiada");
    }

    @Override
    protected Boolean doInBackground(Void... voids) {

        while (running) {

                Random rnd = new Random();
                String name = "pic" + (rnd.nextInt(116) + 34);
                publishProgress(name);
                pausa(2000);

        }

        return running;
    }


    @Override
    protected void onCancelled(Boolean aBoolean) {
        running = false;
    }


    private void pausa(int tiempo) {
        // Dejamos una pausa
        try {
            Thread.sleep(tiempo);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
