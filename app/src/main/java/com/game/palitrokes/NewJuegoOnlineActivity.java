package com.game.palitrokes;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.game.palitrokes.Modelos.Jugador;
import com.game.palitrokes.Utilidades.Constantes;
import com.game.palitrokes.Utilidades.SharedPrefs;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NewJuegoOnlineActivity extends AppCompatActivity {

    private TextView j1TV, j2TV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_juego_online);

        j1TV = findViewById(R.id.j1txt);
        j2TV = findViewById(R.id.j2txt);


        // Recuperamos datos Intent
        Intent intent = getIntent();
        String salaJuego = intent.getStringExtra(Constantes.PARTIDA);
        String rivalID_STR = intent.getStringExtra(Constantes.RIVALIDONLINE);

        final Jugador jugador = SharedPrefs.getJugadorPrefs(getApplicationContext());
        j1TV.setText("Jugador ID: " + jugador.getJugadorId() + "\n" +
                "Nickname: " + jugador.getNickname() + "\n" +
                "Victorias: " + jugador.getVictorias() + "\n" +
                "Numero jugador: " + jugador.getNumeroJugador() + "\n");

        // referencias Firebase
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        Log.d(Constantes.TAG, "Usuario: " + user);

        DatabaseReference rivalRef = mDatabase.child("USUARIOS").child(rivalID_STR);
        ValueEventListener rivalListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Jugador rivalTMP = dataSnapshot.getValue(Jugador.class);

                if (jugador.getNumeroJugador() == 1) {
                    rivalTMP.setNumeroJugador(2);
                } else {
                    rivalTMP.setNumeroJugador(1);
                }

                j2TV.setText("Rival ID: " + rivalTMP.getJugadorId() + "\n" +
                        "Nickname: " + rivalTMP.getNickname() + "\n" +
                        "Victorias: " + rivalTMP.getVictorias() + "\n" +
                        "Numero jugador: " + rivalTMP.getNumeroJugador() + "\n");


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        rivalRef.addListenerForSingleValueEvent(rivalListener);

    }
}