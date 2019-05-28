package com.game.palitrokes;

import android.app.Dialog;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.constraint.solver.widgets.Snapshot;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.game.palitrokes.Adapters.RecordsAdapter;
import com.game.palitrokes.Modelos.Jugador;
import com.game.palitrokes.Modelos.Partida;
import com.game.palitrokes.Modelos.Tablero;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MIAPP";
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference userRef;
    private DatabaseReference jugadoresRef;
    private EditText nickET;
    private TextView onlineTV;
    private Jugador jugador;
    private RecyclerView recordsRecycler;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<Jugador> jugadores;
    private ValueEventListener jugadoresListener;
    private ValueEventListener partidasListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        // Referencias a las vistas
        nickET = findViewById(R.id.editText);
        onlineTV = findViewById(R.id.onlineTV);
        recordsRecycler = findViewById(R.id.recordsRecycler);
        layoutManager = new LinearLayoutManager(this);
        recordsRecycler.setLayoutManager(layoutManager);

        //Lista de jugadores online
        jugadores = new ArrayList<>();

        // Recycler View para los Records
        adapter = new RecordsAdapter(jugadores);
        recordsRecycler.setAdapter(adapter);

        // Nos autenticamos de forma anónima en Firebase
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInAnonymously:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            endSignIn(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            endSignIn(null);
                        }
                    }
                });


    }


    // Al finalizar la autenticación, recuperamos nuestro usuario o lo creamos
    // También cargamos la lista de jugadores online
    //
    private void endSignIn(final FirebaseUser currentUser) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        userRef = mDatabase.child("USUARIOS").child(currentUser.getUid());
        jugadoresRef = mDatabase.child("USUARIOS");

        // Cargamos los datos del usuario o los creamos si no existen (La primera vez que instalamos la APP)
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                jugador = dataSnapshot.getValue(Jugador.class);
                if (jugador == null) {
                    String nickName = "";
                    if (nickET.getText() == null) {
                        nickName = "Jugador";
                    } else {
                        nickName = nickET.getText().toString();
                    }
                    userRef.setValue(new Jugador(currentUser.getUid(), nickName));
                } else {
                    nickET.setText(jugador.getNickname());
                }


                // Log.d(TAG, "Usuario BBDD " + jugador.getNickname());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "Error Usuario BBDD o Crear nuevo");
            }
        };
        userRef.addValueEventListener(userListener);


        // Cargar la lista de jugadores online
        jugadoresListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                jugadores.removeAll(jugadores);

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Jugador jugadorTMP = snapshot.getValue(Jugador.class);
                    if (jugadorTMP.isOnline()) jugadores.add(jugadorTMP);
                }
                onlineTV.setText("Online: " + jugadores.size());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        jugadoresRef.addValueEventListener(jugadoresListener);


    }


    // Cuando pulsamos el botón de Jugar Online hacemos esto ...
    public void jugarOnline(View view) {


        // Quitar el listener de los jugadores
        userRef.removeEventListener(jugadoresListener);

        // Establecer un listener para las partidas
        // Hay salas creadas previamente en Firebase (Partidas)
        // Mirar si hay alguna sala con espacio disponible para el jugador
        // Esperamos hasta que haya dos jugadores en la sala y
        // Los 2 hayan pulsado el botón de 'Preparado'
        // Cuando esto se cumpla, lanzar el intent de la actividad de Juego

        // lanzar un dialog para encontrar un rival
        //

        final Dialog jugarOnline = new Dialog(this);
        jugarOnline.setContentView(R.layout.dialog_jugar);
        jugarOnline.setTitle("Jugar Online");
        jugarOnline.show();

        final TextView rivalReady = jugarOnline.findViewById(R.id.estadoRivalTV);
        final TextView jugadorReady = jugarOnline.findViewById(R.id.estadoJugadorTV);
        final ImageView readyJugadorIMG = jugarOnline.findViewById(R.id.imageReadyJugadorTV);
        final ImageView readyRivalIMG = jugarOnline.findViewById(R.id.imageReadyRivalIV);
        final TextView mensajeEstado = jugarOnline.findViewById(R.id.mensajeEstadoTV);

        jugadorReady.setText(jugador.getNickname());
        mensajeEstado.setText("Buscando Rival...");

        // Creamos una objeto partida para guardar los datos y luego pasarlos a Firebase
        final Partida newPartida = new Partida();

        // También una lista con las partidas actuales en Firebase (Para buscar una sala vacía)
        final List<Partida> salas = new ArrayList<>();

        // Ponemos un Listener en las partidas
        // Aquí llenamos nuestra lista de salas
        // Si encontramos una sala en la que haya un hueco, lo usamos
        partidasListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "Buscando Partida");
                salas.removeAll(salas);

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Partida partidaTMP = snapshot.getValue(Partida.class);
                    // Mirar si en esta sala hay hueco libre
                    if (partidaTMP.getJugador1ID() == "") {
                        // Nos quedamos con el hueco 1
                        jugador.setNumeroJugador(1);
                        newPartida.setJugador1ID(jugador.getJugadorId());
                        newPartida.setPartidaID(partidaTMP.getPartidaID());
                        break;
                    } else if (partidaTMP.getJugador2ID() == "") {
                        // Si el hueco 1 está ocupado, usamos el hueco 2 que está vacío
                        jugador.setNumeroJugador(2);
                        newPartida.setJugador2ID(jugador.getJugadorId());
                        newPartida.setPartidaID(partidaTMP.getPartidaID());
                        break;
                    }

                    // Hemos encontrado sala vacía
                    if (newPartida.getPartidaID() != null) {
                        jugadorReady.setText(newPartida.getPartidaID());
                        // Actualizamos Firebase
                        mDatabase.child("PARTIDAS").child(newPartida.getPartidaID()).setValue(newPartida);
                        // Quitamos el listener de las partidas y pasamos a escuchar solo esta sala
                        mDatabase.child("PARTIDAS").removeEventListener(partidasListener);


                        ValueEventListener salaListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                // Recuperamos los datos de la sala de Firebase cuando hay algún cambio
                                Partida miPartida = dataSnapshot.getValue(Partida.class);

                                // Actualizar el Dialog y esperar a que la sala tenga los
                                // 2 jugadores asignados y que los 2 hayan pulsado el botón 'Preparado'
                                Button botonReady = jugarOnline.findViewById(R.id.readyBTN);
                                botonReady.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mensajeEstado.setText("Preparado ... Esperando Rival ...");
                                        readyJugadorIMG.setImageResource(R.drawable.ic_check_black_24dp);
                                        readyJugadorIMG.setBackgroundColor(getResources().getColor(R.color.verde));
                                        if (jugador.getNumeroJugador() == 1) {
                                            newPartida.setJugador1Ready(true);
                                        } else {
                                            newPartida.setJugador2Ready(true);
                                        }
                                        mDatabase.child("PARTIDAS").child(newPartida.getPartidaID()).setValue(newPartida);

                                    }
                                });


                                // Si el rival está preparado para jugar, actualizar las vistas
                                if (jugador.getNumeroJugador() == 1) {
                                    if (miPartida.isJugador2Ready()) {
                                        readyRivalIMG.setImageResource(R.drawable.ic_check_black_24dp);
                                        readyJugadorIMG.setBackgroundColor(getResources().getColor(R.color.verde));
                                    }
                                } else {
                                    if (miPartida.isJugador1Ready()) {
                                        readyRivalIMG.setImageResource(R.drawable.ic_check_black_24dp);
                                        readyJugadorIMG.setBackgroundColor(getResources().getColor(R.color.verde));
                                    }
                                }

                                // Si nuestra sala ya tiene los 2 jugadores asignados
                                // lanzamos el intent del Juego
                                if (miPartida.isJugador1Ready() && miPartida.isJugador2Ready()) {
                                    Log.d(TAG, "Habemus Partidum");
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        };
                        mDatabase.child("PARTIDAS").child(newPartida.getPartidaID()).addValueEventListener(salaListener);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mDatabase.child("PARTIDAS").addValueEventListener(partidasListener);

        // No hay salas vacías
        if (newPartida.getPartidaID() == null) {
            Toast.makeText(this, "De momento no hay salas vacías", Toast.LENGTH_LONG).show();
        }



        /*
        Intent jugar = new Intent(this, JuegoActivity.class);
        startActivity(jugar);
        */


    }

    public void jugar(View view) {

        // Crear Salas en Firebase
        for (int n = 0; n < 10; n++) {
            mDatabase.child("PARTIDAS").setValue(new Partida());
        }
    }





    /*

        @Override
        public void onStart() {
            super.onStart();
            // Check if user is signed in (non-null) and update UI accordingly.
            FirebaseUser currentUser = mAuth.getCurrentUser();
            endSignIn(currentUser);
        }
    */
}
