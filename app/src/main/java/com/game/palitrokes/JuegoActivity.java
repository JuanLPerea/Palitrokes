package com.game.palitrokes;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.game.palitrokes.Modelos.Monton;
import com.game.palitrokes.Modelos.Tablero;

import java.util.Random;

public class JuegoActivity extends AppCompatActivity {

    private final String TAG = "MIAPP";
    private LinearLayout linearBase;
    private Tablero tablero;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_juego);

        linearBase = findViewById(R.id.tableroLL);

        tablero = new Tablero();
        visualizarTablero(tablero);

    }

    private void visualizarTablero(Tablero tablero) {

        int pesoMonton = 100 / tablero.getNumMontones();

        for (final Monton montonTMP : tablero.getMontones()) {

            LinearLayout monton = new LinearLayout(this);
            monton.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, pesoMonton));
            monton.setOrientation(LinearLayout.VERTICAL);
            monton.setId(newId());
            monton.setPadding(10,0,10,0);
            monton.setBackgroundColor(Color.rgb((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255)));

            int numeroPalos = montonTMP.getPalos();
            int alturaPalo = 50;
            Log.d(TAG, "palos monton " + numeroPalos + " altura " + alturaPalo);

            for (int n = 0; n < numeroPalos; n++) {
                final ImageView newImageView = new ImageView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, alturaPalo);
                        params.setMargins(5,0,5,5);
                newImageView.setLayoutParams(params);
                newImageView.setImageResource(R.drawable.palo);
                newImageView.setId(newId());
                newImageView.setTag(monton);
                newImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                newImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        seleccionarPalo(newImageView, montonTMP);
                    }
                });
                monton.addView(newImageView);
            }

        linearBase.addView(monton);
        }

    }


    private void seleccionarPalo(ImageView imageView, Monton monton){
        Log.d(TAG, "Seleccionado palo " + imageView.getId());



        if (imageView.getTag() != "Seleccionado") {
            monton.setPalosseleccionados(1);

            // Comprobar si tenemos palos seleccionados en los otros montones
            int montonesSeleccionados = 0;
            for (Monton montonCheck : tablero.getMontones()) {
                if (montonCheck.getPalosseleccionados()>0) montonesSeleccionados++;
            }
            if (montonesSeleccionados>1) {
                Toast.makeText(this, "Solo puedes seleccionar de un montón cada vez", Toast.LENGTH_LONG).show();
                monton.setPalosseleccionados(-1);
            } else {
                imageView.setTag("Seleccionado");
                imageView.setAlpha(0.5F);
            }



        } else {
            monton.setPalosseleccionados(-1);
            imageView.setTag("");
            imageView.setAlpha(1F);
        }

    }


    private int newId() {
        Random r = new Random();
        int resultado = -1;
        do {
            resultado = r.nextInt(Integer.MAX_VALUE);
        } while (findViewById(resultado) != null);
        return resultado;
    }


    private boolean soloUnMonton(Tablero tablero){

        for (Monton monton : tablero.getMontones()) {

        }


        return true;
    }
}
