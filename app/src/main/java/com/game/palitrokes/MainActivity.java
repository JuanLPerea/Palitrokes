package com.game.palitrokes;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
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
import com.game.palitrokes.Utilidades.Utilidades;
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
    private Button botonOnline;
    private ImageView avatarJugador;
    private Jugador jugador;
    private RecyclerView recordsRecycler;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<Jugador> jugadores;
    private ValueEventListener jugadoresListener;
    private ValueEventListener partidasListener;
    private ValueEventListener salaListener;

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
        botonOnline = findViewById(R.id.jugaronlineBTN);
        avatarJugador = findViewById(R.id.avatarIV);

        //Lista de jugadores online
        jugadores = new ArrayList<>();

        // Recycler View para los Records
        adapter = new RecordsAdapter(jugadores);
        recordsRecycler.setAdapter(adapter);

        // Nos autenticamos de forma anónima en Firebase
        // TODO Controlar que no se pueda jugar online
        //  hasta que nos hayamos autenticado (Si no casca)
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
                    // Si el jugador es nuevo lo creamos
                    String nickName = "";
                    if (nickET.getText() == null) {
                        nickName = "Jugador";
                    } else {
                        nickName = nickET.getText().toString();
                    }
                    userRef.setValue(new Jugador(currentUser.getUid(), nickName));

                    // Subimos una imágen a Firebase Storage con el nombre del ID del jugador
                    // para usarla como avatar
                    Utilidades.subirImagenFirebase(getApplicationContext(), jugador.getJugadorId());

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
                botonOnline.setVisibility(View.VISIBLE);


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

        String partidaID = "";

        //  Controlar si el usuario pulsa back en el dispositivo y cancelar los listener, cerrar el dialog y
        //  volver a poner el listener de Usuarios/Jugadores
        jugarOnline.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.d(TAG, "Cancelado Dialog");

                // Quitar los listener
                mDatabase.removeEventListener(salaListener);
                mDatabase.removeEventListener(partidasListener);

                borrarJugadorSalaFirebase();

                mDatabase.child("USUARIOS").addValueEventListener(jugadoresListener);
                jugarOnline.dismiss();
            }
        });


        final TextView rivalReady = jugarOnline.findViewById(R.id.estadoRivalTV);
        final TextView jugadorReady = jugarOnline.findViewById(R.id.estadoJugadorTV);
        final ImageView readyJugadorIMG = jugarOnline.findViewById(R.id.imageReadyJugadorTV);
        final ImageView readyRivalIMG = jugarOnline.findViewById(R.id.imageReadyRivalIV);
        final TextView mensajeEstado = jugarOnline.findViewById(R.id.mensajeEstadoTV);

        jugadorReady.setText(jugador.getNickname());
        mensajeEstado.setText("Buscando Rival...");


        // Ponemos un Listener en las partidas (Salas)
        // Si encontramos una sala en la que haya un hueco, lo usamos
        partidasListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "Buscando Partida");

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final Partida partidaTMP = snapshot.getValue(Partida.class);
                    // Creamos una objeto partida para guardar los datos y luego pasarlos a Firebase

                    // Mirar si en esta sala hay hueco libre

                    if (partidaTMP.getJugador1ID().equals("0")) {
                        // Nos quedamos con el hueco 1
                        Log.d(TAG, "Encontrado hueco 1");
                        jugador.setNumeroJugador(1);
                        jugador.setPartida(partidaTMP.getPartidaID());
                        partidaTMP.setJugador1ID(jugador.getJugadorId());
                        partidaTMP.setPartidaID(partidaTMP.getPartidaID());
                    } else if (partidaTMP.getJugador2ID().equals("0")) {
                        // Si el hueco 1 está ocupado, usamos el hueco 2 que está vacío
                        Log.d(TAG, "Encontrado hueco 2");
                        jugador.setNumeroJugador(2);
                        jugador.setPartida(partidaTMP.getPartidaID());
                        partidaTMP.setJugador2ID(jugador.getJugadorId());
                        partidaTMP.setPartidaID(partidaTMP.getPartidaID());
                    }

                    Log.d(TAG, "Partida id:" + partidaTMP.getPartidaID());
                    // Hemos encontrado sala vacía
                    if (!partidaTMP.getPartidaID().equals("0") && partidaTMP.getPartidaID() != null) {
                        // Actualizamos Firebase
                        mDatabase.child("PARTIDAS").child(partidaTMP.getPartidaID()).setValue(partidaTMP);
                        // Quitamos el listener de las partidas y pasamos a escuchar solo esta sala
                        mDatabase.child("PARTIDAS").removeEventListener(partidasListener);


                        salaListener = new ValueEventListener() {
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
                                        jugadorReady.setText("¡Preparado!");
                                        readyJugadorIMG.setImageResource(R.drawable.ic_check_black_24dp);
                                        readyJugadorIMG.setBackgroundColor(getResources().getColor(R.color.verde));
                                        if (jugador.getNumeroJugador() == 1) {
                                            partidaTMP.setJugador1Ready(true);
                                        } else {
                                            partidaTMP.setJugador2Ready(true);
                                        }
                                        mDatabase.child("PARTIDAS").child(partidaTMP.getPartidaID()).setValue(partidaTMP);
                                    }
                                });


                                // Si el rival está preparado para jugar, actualizar las vistas
                                if (jugador.getNumeroJugador() == 1) {
                                    if (miPartida.isJugador2Ready()) {
                                        readyRivalIMG.setImageResource(R.drawable.ic_check_black_24dp);
                                        readyRivalIMG.setBackgroundColor(getResources().getColor(R.color.verde));
                                    }
                                } else {
                                    if (miPartida.isJugador1Ready()) {
                                        readyRivalIMG.setImageResource(R.drawable.ic_check_black_24dp);
                                        readyRivalIMG.setBackgroundColor(getResources().getColor(R.color.verde));
                                    }
                                    rivalReady.setText("¡Preparado!");
                                }

                                // Si nuestra sala ya tiene los 2 jugadores asignados
                                // lanzamos el intent del Juego
                                if (miPartida.isJugador1Ready() && miPartida.isJugador2Ready()) {
                                    Log.d(TAG, "Habemus Partidum");
                                    Toast.makeText(getApplicationContext(), "A Jugar ...", Toast.LENGTH_LONG).show();
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.d(TAG, "Cancelado esperando Rival");
                            }
                        };
                        mDatabase.child("PARTIDAS").child(partidaTMP.getPartidaID()).addValueEventListener(salaListener);
                        if (partidaTMP.getPartidaID() != null) break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "Cancelado Buscando Hueco");
            }
        };

        mDatabase.child("PARTIDAS").addValueEventListener(partidasListener);

        // No hay salas vacías
        //  if (newPartida.getPartidaID() == null) {
        Toast.makeText(this, "De momento no hay salas vacías", Toast.LENGTH_LONG).show();
        //  }



        /*
        Intent jugar = new Intent(this, JuegoActivity.class);
        startActivity(jugar);
        */


    }


    // Controlar que si cerramos la aplicación y el juegador tiene
    //  asignado una sala, borrarlo de Firebase para que no se quede pillada la sala

    @Override
    protected void onStop() {
        super.onStop();
        borrarJugadorSalaFirebase();
    }

    public void jugar(View view) {


        Utilidades.descargarImagenFirebase(getApplicationContext(), jugador.getJugadorId(), avatarJugador);

        /*
        // Crear Salas en Firebase
        for (int n = 0; n < 10; n++) {
            mDatabase.child("PARTIDAS").child("Sala " + n).setValue(new Partida("Sala " + n, "0", "0", new Tablero()));
        }
        */

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

    public void borrarJugadorSalaFirebase() {
        // Si el jugador ya tenía partida asignada, la borramos de Firebase
        if (!jugador.getPartida().equals("0")) {
            if (jugador.getNumeroJugador() == 1) {
                mDatabase.child("PARTIDAS").child("").child(jugador.getPartida()).child("jugador1ID").setValue("0");
                mDatabase.child("PARTIDAS").child(jugador.getPartida()).child("jugador1Ready").setValue(false);
            } else {

                mDatabase.child("PARTIDAS").child(jugador.getPartida()).child("jugador2ID").setValue("0");
                mDatabase.child("PARTIDAS").child(jugador.getPartida()).child("jugador2Ready").setValue(false);
            }
            jugador.setPartida("0");
        }
    }



}
