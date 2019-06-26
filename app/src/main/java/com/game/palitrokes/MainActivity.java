package com.game.palitrokes;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.game.palitrokes.Adapters.RecordsAdapter;
import com.game.palitrokes.Modelos.Jugador;
import com.game.palitrokes.Modelos.Partida;
import com.game.palitrokes.Modelos.Records;
import com.game.palitrokes.Modelos.Tablero;
import com.game.palitrokes.Utilidades.Constantes;
import com.game.palitrokes.Utilidades.SharedPrefs;
import com.game.palitrokes.Utilidades.Sonidos;
import com.game.palitrokes.Utilidades.Utilidades;
import com.game.palitrokes.Utilidades.UtilityNetwork;
import com.game.palitrokes.Utilidades.UtilsFirebase;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference userRef;
    private DatabaseReference jugadoresRef;
    private DatabaseReference recordsRef;
    private EditText nickET;
    private TextView onlineTV, victoriasTV;
    private Button botonOnline;
    private ImageView avatarJugador;
    private Jugador jugador;
    private RecyclerView recordsRecycler;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private FloatingActionButton fab;
    private List<Jugador> jugadores;
    private List<Records> records;
    private ValueEventListener jugadoresListener;
    private ValueEventListener partidasListener;
    private ValueEventListener recordsListener;
    private Partida salaSeleccionada;
    private Sonidos sonidos;
    private static final String[] PERMISOS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private Uri photo_uri;//para almacenar la ruta de la imagen
    private String ruta_foto;//nombre fichero creado
    private boolean permisosOK;
    private String salaAnterior;
    private CountDownTimer changeImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        Intent intent = getIntent();
        salaAnterior = intent.getStringExtra("SALA_ANTERIOR");

        // Sonidos
        sonidos = new Sonidos(this);

        // Referencias a las vistas
        nickET = findViewById(R.id.nickET);
        victoriasTV = findViewById(R.id.victoriasET);
        onlineTV = findViewById(R.id.onlineTV);
        recordsRecycler = findViewById(R.id.recordsRecycler);
        layoutManager = new LinearLayoutManager(this);
        recordsRecycler.setLayoutManager(layoutManager);
        botonOnline = findViewById(R.id.jugaronlineBTN);
        avatarJugador = findViewById(R.id.avatarIV);
        fab = findViewById(R.id.fab);
        fab.bringToFront();

        // Animacion del logo
        animacionPalitrokes();

        //Lista de jugadores online
        jugadores = new ArrayList<>();

        // Recycler View para los Records
        records = new ArrayList<>();
        adapter = new RecordsAdapter(getApplicationContext(), records);
        recordsRecycler.setAdapter(adapter);

        jugador = new Jugador();

        // Pedir permisos para las fotos y avatares
        ActivityCompat.requestPermissions
                (this, PERMISOS, Constantes.CODIGO_PETICION_PERMISOS);

        // Recuperamos los datos del Shared Preferences
        recuperarDatosSharedPreferences();

        // Comprobar si tenemos internet
        if (UtilityNetwork.isNetworkAvailable(this) || UtilityNetwork.isWifiAvailable(this)) {

            // Si tenemos internet recuperamos los datos del usuario de Firebase
            // Nos autenticamos de forma anónima en Firebase
            mAuth = FirebaseAuth.getInstance();
            mAuth.signInAnonymously()
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(Constantes.TAG, "signInAnonymously:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                endSignIn(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(Constantes.TAG, "signInAnonymously:failure", task.getException());
                                Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                endSignIn(null);
                            }
                        }
                    });
        }


    }


    // Al finalizar la autenticación, recuperamos nuestro usuario o lo creamos
    // También cargamos la lista de jugadores online
    //
    private void endSignIn(final FirebaseUser currentUser) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        userRef = mDatabase.child("USUARIOS").child(currentUser.getUid());
        jugadoresRef = mDatabase.child("USUARIOS");

        // Si volvemos de jugar online limpiamos la sala por si acaso
        if (salaAnterior != null) {
            Log.d(Constantes.TAG, "Ha vuelto de otro intent " + salaAnterior);
            pausa(1000);
            limpiarSala(salaAnterior);
            pausa(1000);
        }

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
                    jugador = new Jugador(currentUser.getUid(), nickName);
                    jugador.setOnline(true);
                    userRef.setValue(jugador);

                    // Subimos una imagen a Firebase Storage con el nombre del ID del jugador
                    // para usarla como avatar
                    Bitmap avatarNuevo = BitmapFactory.decodeResource(getApplicationContext().getResources() , R.drawable.camera);
                    UtilsFirebase.subirImagenFirebase(currentUser.getUid(), avatarNuevo);
                    Utilidades.guardarImagenMemoriaInterna(getApplicationContext(), Constantes.ARCHIVO_IMAGEN_JUGADOR, Utilidades.bitmapToArrayBytes(avatarNuevo));
                    avatarJugador.setImageBitmap(avatarNuevo);
                    SharedPrefs.saveJugadorPrefs(getApplicationContext(), jugador);

                } else {
                    jugador = SharedPrefs.getJugadorPrefs(getApplicationContext());
                    jugador.setOnline(true);
                    userRef.setValue(jugador);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(Constantes.TAG, "Error Usuario BBDD o Crear nuevo");
            }
        };
        userRef.addListenerForSingleValueEvent(userListener);

        cargarRecords();


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
                if (jugadores.size() > 1) {
                    botonOnline.setEnabled(true);
                } else {
                    botonOnline.setEnabled(false);
                }
                botonOnline.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        jugadoresRef.addValueEventListener(jugadoresListener);


    }

    private void cargarRecords() {


        // Cargar los records y mostrarlos en el Recycler
        recordsRef = FirebaseDatabase.getInstance().getReference().child("RECORDS");
        ValueEventListener cargarRecords = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int n = 0;
                records.removeAll(records);
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Records recordTmp = snapshot.getValue(Records.class);
                    records.add(recordTmp);
                    // Descargamos imagen de Firebase y la guardamos en el dispositivo para usarla mas tarde
                    Utilidades.eliminarArchivo(getApplicationContext(), "RECORDIMG" + n + ".jpg");
                    UtilsFirebase.descargarImagenFirebaseYGuardarla(getApplicationContext(), recordTmp.getIdJugador(), "RECORDIMG" + n );
                    n++;
                }
                adapter.notifyDataSetChanged();
                // Guardamos los records actualizados de Firebase en el Shared Preferences
                SharedPrefs.saveRecordsPrefs(getApplicationContext(),records);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        recordsRef.addListenerForSingleValueEvent(cargarRecords);

    }


    // Cuando pulsamos el botón de Jugar Online hacemos esto ...
    public void jugarOnline(View view) {


        // Quitar el listener de los jugadores
        userRef.removeEventListener(jugadoresListener);

        //Subimos nuestro avatar a Firebase (Aquí es seguro que tenemos internet)
        UtilsFirebase.subirImagenFirebase(mAuth.getCurrentUser().getUid(), Utilidades.recuperarImagenMemoriaInterna(getApplicationContext(), Constantes.ARCHIVO_IMAGEN_JUGADOR));

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

        //  Controlar si el usuario pulsa back en el dispositivo: cancelar los listener, cerrar el dialog y
        //  volver a poner el listener de Usuarios/Jugadores
        jugarOnline.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.d(Constantes.TAG, "Cancelado Dialog");

                // Quitar el listener
                mDatabase.child("PARTIDAS").removeEventListener(partidasListener);
                borrarJugadorSalaFirebase();
                jugador.setPartida("0");
                salaSeleccionada = null;
                mDatabase.child("USUARIOS").addValueEventListener(jugadoresListener);
                jugarOnline.dismiss();
            }
        });

        final TextView rivalReady = jugarOnline.findViewById(R.id.estadoRivalTV);
        final TextView jugadorReady = jugarOnline.findViewById(R.id.estadoJugadorTV);
        final ImageView readyJugadorIMG = jugarOnline.findViewById(R.id.imageReadyJugadorTV);
        final ImageView readyRivalIMG = jugarOnline.findViewById(R.id.imageReadyRivalIV);
        final TextView mensajeEstado = jugarOnline.findViewById(R.id.mensajeEstadoTV);
        final ProgressBar progressBar = jugarOnline.findViewById(R.id.progressBar2);
        final ImageView avatarRival = jugarOnline.findViewById(R.id.rivalImageIV);

        // Hacemos una lista con las salas disponibles en Firebase
        partidasListener = new ValueEventListener() {
            List<Partida> salasDisponibles = new ArrayList<>();


            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // Actualizamos los views del Dialog
                jugadorReady.setText(jugador.getNickname());
                mensajeEstado.setText("Buscando Rival...");
                avatarRival.setImageResource(R.drawable.camera);

                progressBar.setVisibility(View.VISIBLE);
                // Actualizamos los datos de las salas
                // Tenemos una lista con todas las salas
                // y otra con las salas que tienen sitio disponible
                // Log.d(Constantes.TAG, "Buscando sala");
                salasDisponibles.removeAll(salasDisponibles);
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Partida salaTMP = snapshot.getValue(Partida.class);

                    // si ya tenemos la sala seleccionada, actualizamos los datos
                    // con los cambios que ha habido en Firebase
                    if (salaSeleccionada != null) {
                        if (salaTMP.getNumeroSala() == salaSeleccionada.getNumeroSala()) {
                            salaSeleccionada = salaTMP;
                            break;
                        }
                    } else {
                        // si no tenemos sala seleccionada, buscamos una añadiendo las disponibles a la lista
                        if (salaTMP.getJugador1ID().equals("0") || salaTMP.getJugador2ID().equals("0")) {
                            salasDisponibles.add(salaTMP);
                        }
                    }
                }

                Log.d(Constantes.TAG, "Hay " + salasDisponibles.size() + " salas disponibles");


                // si no tenemos una sala seleccionada, buscamos una libre
                // TODO puede pasar que una sala se reserve justo en el tiempo que tardamos
                //  hasta que actualizamos (en ese caso se machacarían los datos en Firebase
                //  y podría haber poblemas)
                if (salaSeleccionada == null) {
                    if (salasDisponibles.size() > 0) {
                        // Seleccionamos una sala para jugar de las disponibles
                        //   int salaAleatoria = new Random().nextInt(salasDisponibles.size());
                        salaSeleccionada = salasDisponibles.get(0);
                        // Reservamos el hueco
                        if (salaSeleccionada.getJugador1ID().equals("0")) {
                            // Nos quedamos con el hueco 1
                            Log.d(Constantes.TAG, "Encontrado hueco 1");
                            jugador.setNumeroJugador(1);
                            jugador.setPartida(salaSeleccionada.getPartidaID());
                            salaSeleccionada.setJugador1ID(jugador.getJugadorId());
                        } else if (salaSeleccionada.getJugador2ID().equals("0")) {
                            // Si el hueco 1 está ocupado, usamos el hueco 2 que está vacío
                            Log.d(Constantes.TAG, "Encontrado hueco 2");
                            jugador.setNumeroJugador(2);
                            jugador.setPartida(salaSeleccionada.getPartidaID());
                            salaSeleccionada.setJugador2ID(jugador.getJugadorId());
                        }
                        // Actualizamos Firebase y de paso limpiamos la sala por si hubiera quedado algo de la ultima partida
                        jugador.setNickname(nickET.getText().toString());
                        userRef.setValue(jugador);
                        //     salaSeleccionada.setTablero(new Tablero());
                        salaSeleccionada.setGanador(0);
                        salaSeleccionada.setJugando(false);
                        salaSeleccionada.setJugador1Ready(false);
                        salaSeleccionada.setJugador2Ready(false);
                        salaSeleccionada.setTurno(1);
                        mDatabase.child("PARTIDAS").child(salaSeleccionada.getPartidaID()).setValue(salaSeleccionada);


                    } else {
                        // No hay salas disponibles
                        Toast.makeText(getApplicationContext(), "De momento no hay salas vacías", Toast.LENGTH_LONG).show();
                    }

                } else {

                    // si ya tenemos una sala seleccionada, miramos que haya rival y que los 2 estén preparados
                    // Si nosotros estamos en el hueco 1 esperamos a que el hueco 2 esté ocupado
                    if (jugador.getNumeroJugador() == 1) {
                        Log.d(Constantes.TAG, "Estamos en el hueco 1 de la " + salaSeleccionada.getPartidaID());
                        if (!salaSeleccionada.getJugador2ID().equals("0")) {
                            // Hay otro jugador que ha seleccionado esta sala
                            Log.d(Constantes.TAG, "Hay otro jugador en el hueco 2");
                            mensajeEstado.setText("Encontrado Rival, esperando que esté preparado");
                            // Descargamos la imagen del Rival y la visualizamos
                            UtilsFirebase.descargarImagenFirebaseView(getApplicationContext(), salaSeleccionada.getJugador2ID(), avatarRival);

                            pausa(1000);
                            progressBar.setVisibility(View.INVISIBLE);
                            if (salaSeleccionada.isJugador2Ready()) {
                                // El otro jugador está listo
                                Log.d(Constantes.TAG, "El jugador 2 está preparado");
                                readyRivalIMG.setImageResource(R.drawable.ic_check_black_24dp);
                                readyRivalIMG.setBackgroundColor(getResources().getColor(R.color.verde));
                                rivalReady.setText("¡Preparado!");
                                if (salaSeleccionada.isJugador1Ready()) {

                                    // Los 2 estamos listos. Lanzar Intent de juego
                                    // Como somos el jugador 1 tenemos el turno (Estas son mis reglas)
                                    mDatabase.child("PARTIDAS").removeEventListener(partidasListener);
                                    Log.d(Constantes.TAG, "Lanzar Juego");
                                    Intent jugar = new Intent(jugarOnline.getContext(), JuegoOnlineActivity.class);
                                    jugar.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    jugar.putExtra("PARTIDA", salaSeleccionada.getPartidaID());
                                    jugar.putExtra(Constantes.RIVALID, salaSeleccionada.getJugador2ID());
                                    finish();
                                    changeImage.cancel();
                                    startActivity(jugar);
                                    jugarOnline.dismiss();
                                }
                            } else {
                                readyRivalIMG.setImageResource(R.drawable.ic_cached_black_24dp);
                                readyRivalIMG.setBackgroundColor(getResources().getColor(R.color.rojo));
                                rivalReady.setText("Esperando al rival");
                            }
                        }

                    } else {
                        // si estamos en el hueco 2 hacemos lo mismo que antes, pero para este hueco
                        Log.d(Constantes.TAG, "Estamos en el hueco 2 de la " + salaSeleccionada.getPartidaID());
                        if (!salaSeleccionada.getJugador1ID().equals("0")) {
                            // Hay otro jugador que ha seleccionado esta sala
                            Log.d(Constantes.TAG, "Hay otro jugador en el hueco 1");
                            mensajeEstado.setText("Encontrado Rival, esperando que esté preparado");
                            // Descargamos la imagen del Rival y la visualizamos
                            UtilsFirebase.descargarImagenFirebaseView(getApplicationContext(), salaSeleccionada.getJugador1ID(), avatarRival);

                            pausa(1000);
                            progressBar.setVisibility(View.INVISIBLE);
                            if (salaSeleccionada.isJugador1Ready()) {
                                // El otro jugador está listo
                                Log.d(Constantes.TAG, "El jugador 1 está preparado");
                                readyRivalIMG.setImageResource(R.drawable.ic_check_black_24dp);
                                readyRivalIMG.setBackgroundColor(getResources().getColor(R.color.verde));
                                rivalReady.setText("¡Preparado!");
                                if (salaSeleccionada.isJugador2Ready()) {
                                    mDatabase.child("PARTIDAS").removeEventListener(partidasListener);
                                    // Los 2 estamos listos. Lanzar Intent de juego
                                    Intent jugar = new Intent(jugarOnline.getContext(), JuegoOnlineActivity.class);
                                    jugar.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    jugar.putExtra("PARTIDA", salaSeleccionada.getPartidaID());
                                    jugar.putExtra(Constantes.RIVALID, salaSeleccionada.getJugador1ID());
                                    changeImage.cancel();
                                    finish();
                                    startActivity(jugar);
                                    jugarOnline.dismiss();
                                }
                            } else {
                                readyRivalIMG.setImageResource(R.drawable.ic_cached_black_24dp);
                                readyRivalIMG.setBackgroundColor(getResources().getColor(R.color.rojo));
                                rivalReady.setText("Esperando al rival");
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {


            }
        };
        mDatabase.child("PARTIDAS").addValueEventListener(partidasListener);


        // Listener para el botón
        // Cuando pulsamos es que estamos preparados para jugar
        // Actualizamos Firebase para notificarlo
        Button botonReady = jugarOnline.findViewById(R.id.readyBTN);
        botonReady.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mensajeEstado.setText("Preparado ... Esperando Rival ...");
                jugadorReady.setText("¡Preparado!");
                readyJugadorIMG.setImageResource(R.drawable.ic_check_black_24dp);
                readyJugadorIMG.setBackgroundColor(getResources().getColor(R.color.verde));
                if (salaSeleccionada != null) {
                    if (jugador.getNumeroJugador() == 1) {
                        salaSeleccionada.setJugador1Ready(true);
                    } else {
                        salaSeleccionada.setJugador2Ready(true);
                    }
                    mDatabase.child("PARTIDAS").child(salaSeleccionada.getPartidaID()).setValue(salaSeleccionada);
                }

            }
        });


    }


    // Controlar que si cerramos la aplicación y el juegador tiene
    //  asignado una sala, borrarlo de Firebase para que no se quede pillada la sala

    @Override
    protected void onStop() {

        if (UtilityNetwork.isNetworkAvailable(this) || UtilityNetwork.isWifiAvailable(this)) {
            if (jugador != null) {
                jugador.setOnline(false);
                userRef.setValue(jugador);
            }
            if (salaSeleccionada != null) {
                limpiarSala(salaSeleccionada.getPartidaID());
            }
        }
        changeImage.cancel();
        super.onStop();



        /*
        if (salaSeleccionada.getTurno() == 0){
            borrarJugadorSalaFirebase();
        }

        */
    }


    //
    // Aquí lanzamos el juego contra el ordenador (Móvil en este caso)
    //
    public void jugar(View view) {


        // Ponemos al false el que el jugador está online para que no le tengan en cuenta para jugar en este modo
        if (jugador != null) {
            if (UtilityNetwork.isWifiAvailable(this) || UtilityNetwork.isNetworkAvailable(this)) {
                jugador.setOnline(false);
                userRef.setValue(jugador);
            }
        } else {
            jugador = new Jugador();
        }

        if (nickET.getText() != null) {
            jugador.setNickname(nickET.getText().toString());
        }


        SharedPrefs.saveJugadorPrefs(getApplicationContext(), jugador);

        Intent intentvscom = new Intent(this, JuegoVsComActivity.class);
        intentvscom.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        changeImage.cancel();
        finish();
        startActivity(intentvscom);


    }


/*
        salaSeleccionada = new Partida();
        // Los 2 estamos listos. Lanzar Intent de juego
        Intent jugar = new Intent(this, JuegoOnlineActivity.class);
        jugar.putExtra(Constantes.PARTIDA, "Sala 0");
        jugar.putExtra(Constantes.RIVALID, "DrKCdhn2a1Z4LAgX1DNeZks7F2u1");
        startActivity(jugar);
        finish();

    }


    */
    //  Utilidades.descargarImagenFirebaseYGuardarla(getApplicationContext(), jugador.getJugadorId(), avatarJugador);

/*
        // Crear Salas en Firebase
        for (int n = 0; n < 10; n++) {
            mDatabase.child("PARTIDAS").child("Sala " + n).setValue(new Partida("Sala " + n, n , "0", "0", new Tablero()));
        }

    }
*/

/*
        @Override
        public void onStart() {
            super.onStart();


        }
*/


    public void borrarJugadorSalaFirebase() {
        // Si el jugador ya tenía partida asignada, la borramos de Firebase
        if (!jugador.getPartida().equals("0")) {
            if (jugador.getNumeroJugador() == 1) {
                mDatabase.child("PARTIDAS").child(jugador.getPartida()).child("jugador1ID").setValue("0");
                mDatabase.child("PARTIDAS").child(jugador.getPartida()).child("jugador1Ready").setValue(false);
            } else {

                mDatabase.child("PARTIDAS").child(jugador.getPartida()).child("jugador2ID").setValue("0");
                mDatabase.child("PARTIDAS").child(jugador.getPartida()).child("jugador2Ready").setValue(false);
            }

        }
    }


    public void personalizarAvatar(View view) {

        PopupMenu popup = new PopupMenu(this, view);
        //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.personaliza_avatar_menu, popup.getMenu());

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.foto_camara:
                        tomarFoto();
                        return true;
                    case R.id.foto_galeria:
                        seleccionarFoto();
                        return true;
                }
                return true;
            }
        });

        popup.show();//showing popup menu


    }


    public void crearSalas(View view) {

        //  resetearRecords();

        // Crear Salas en Firebase
        for (int n = 0; n < 10; n++) {
            mDatabase.child("PARTIDAS").child("Sala " + n).setValue(new Partida("Sala " + n, n, "0", "0", new Tablero(0), 0));
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ((grantResults[0] == PackageManager.PERMISSION_GRANTED)
                && (grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
            Log.d("MIAPP", "ME ha concecido los permisos");
        } else {
            Log.d("MIAPP", "NO ME ha concecido los permisos");
            Toast.makeText(this, "Hace falta que actives los permisos para Personalizar tu Avatar ...", Toast.LENGTH_SHORT).show();
            permisosOK = false;
        }
    }

    public void tomarFoto() {

        Log.d("MIAPP", "Quiere hacer una foto");
        Intent intent_foto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        this.photo_uri = Utilidades.crearFicheroImagen();
        intent_foto.putExtra(MediaStore.EXTRA_OUTPUT, this.photo_uri);
        Utilidades.desactivarModoEstricto();
        changeImage.cancel();
        startActivityForResult(intent_foto, Constantes.CODIGO_PETICION_HACER_FOTO);

    }

    public void seleccionarFoto() {
        Log.d("MIAPP", "Quiere seleccionar una foto");
        changeImage.cancel();
        Intent intent_pide_foto = new Intent();
        //intent_pide_foto.setAction(Intent.ACTION_PICK);//seteo la acción para galeria
        intent_pide_foto.setAction(Intent.ACTION_GET_CONTENT);//seteo la acción
        intent_pide_foto.setType("image/*");//tipo mime
        startActivityForResult(intent_pide_foto, Constantes.CODIGO_PETICION_SELECCIONAR_FOTO);

    }

    private void setearImagenDesdeArchivo(int resultado, Intent data) {
        switch (resultado) {
            case RESULT_OK:
                Log.d("MIAPP", "La foto ha sido seleccionada");

                this.photo_uri = data.getData();//obtenemos la uri de la foto seleccionada
                Log.d(Constantes.TAG, photo_uri.getPath());

                InputStream imageStream = null;
                try {
                    imageStream = getContentResolver().openInputStream(this.photo_uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                selectedImage = Utilidades.getResizedBitmap(selectedImage, 128);// 400 is for example, replace with desired size

                this.avatarJugador.setImageBitmap(selectedImage);

                // Guardamos una copia del archivo en el dispositivo para utilizarlo mas tarde
                Utilidades.guardarImagenMemoriaInterna(getApplicationContext(), Constantes.ARCHIVO_IMAGEN_JUGADOR ,Utilidades.bitmapToArrayBytes(selectedImage));


                // De paso guardamos los datos del jugador (Nickname, id, victorias en el Shared Preferences)
                if (jugador == null) {
                    jugador = new Jugador();
                }
                if (!nickET.getText().toString().equals("") ) {
                    jugador.setNickname(nickET.getText().toString());
                }
                SharedPrefs.saveJugadorPrefs(getApplicationContext(), jugador);

                break;

            case RESULT_CANCELED:
                Log.d("MIAPP", "La foto NO ha sido seleccionada canceló");
                break;
        }
    }

    private void setearImagenDesdeCamara(int resultado, Intent intent) {
        switch (resultado) {
            case RESULT_OK:
                Log.d("MIAPP", "Tiró la foto bien");

                InputStream imageStream = null;
                try {
                    imageStream = getContentResolver().openInputStream(this.photo_uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                selectedImage = Utilidades.getResizedBitmap(selectedImage, 128);// 400 is for example, replace with desired size
                this.avatarJugador.setImageBitmap(selectedImage);

                // Guardamos una copia del archivo en el dispositivo para utilizarlo mas tarde
                Utilidades.guardarImagenMemoriaInterna(getApplicationContext(), Constantes.ARCHIVO_IMAGEN_JUGADOR ,Utilidades.bitmapToArrayBytes(selectedImage));

                // De paso guardamos los datos del jugador (Nickname, id, victorias en el Shared Preferences)
                if (jugador == null) {
                    jugador = new Jugador();
                }
                if (!nickET.getText().toString().equals("")) {
                    jugador.setNickname(nickET.getText().toString());
                }
                SharedPrefs.saveJugadorPrefs(getApplicationContext(), jugador);

                // Actualizamos la galería
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, photo_uri));
                break;
            case RESULT_CANCELED:
                Log.d("MIAPP", "Canceló la foto");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);//no llamamos al padre
        if (requestCode == Constantes.CODIGO_PETICION_SELECCIONAR_FOTO) {
            setearImagenDesdeArchivo(resultCode, data);
        } else if (requestCode == Constantes.CODIGO_PETICION_HACER_FOTO) {
            setearImagenDesdeCamara(resultCode, data);
        }
    }


    private void resetearRecords() {
        // Crear Records en Firebase
        for (int n = 0; n < 10; n++) {
            mDatabase.child("RECORDS").child("" + n).setValue(new Records("adfadfadfasdfadf", "Jugador " + n, 0, n));
        }
    }

    private void limpiarSala(String sala) {
        // Dejamos la sala vacía para poder reusarla
        Partida partida = new Partida();
        partida.setPartidaID(sala);
        partida.setJugador1ID("0");
        partida.setJugador2ID("0");
        partida.setGanador(0);
        partida.setJugador2Ready(false);
        partida.setJugador1Ready(false);
        partida.setJugando(false);

        partida.setTurno(1);
        mDatabase.child("PARTIDAS").child(sala).setValue(partida);
    }


    private void pausa(int tiempo) {
        // Dejamos una pausa para que se actualice la sala
        try {
            Thread.sleep(tiempo);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void recuperarDatosSharedPreferences() {
        // Recuperamos datos del Shared Preferences
        // Cargamos datos del jugador
        jugador = SharedPrefs.getJugadorPrefs(getApplicationContext());

        if (jugador.isFirstRun()) {
            // La primera vez que instalamos la aplicación lanzamos la actividad de Info
            // Para explicar el juego
            Log.d(Constantes.TAG, "Lanzar info");
            jugador.setFirstRun(false);
            SharedPrefs.saveJugadorPrefs(getApplicationContext(), jugador);
            changeImage.cancel();
            Intent infointent = new Intent(getApplicationContext(), InfoActivity.class);
            finish();
            startActivity(infointent);


        }

        // Seteamos la imagen del avatar con el archivo guardado localmente en el dispositivo
        // Este archivo se actualiza cada vez que lo personalizamos con una imagen de la galería o la cámara
        avatarJugador.setImageBitmap(Utilidades.recuperarImagenMemoriaInterna(getApplicationContext(), Constantes.ARCHIVO_IMAGEN_JUGADOR));
        // Mostramos el nick del jugador y las victorias
        nickET.setText(jugador.getNickname());
        victoriasTV.setText("Victorias: " + jugador.getVictorias());

        // Cargamos records y los mostramos
        records = SharedPrefs.getRecordsPrefs(getApplicationContext());
        adapter = new RecordsAdapter(getApplicationContext(), records);
        recordsRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }


    @Override
    protected void onPause() {
        super.onPause();
        if (UtilityNetwork.isWifiAvailable(this) || UtilityNetwork.isNetworkAvailable(this)) {
            if (jugador != null && jugador.getJugadorId() != null  && userRef != null) {
                jugador.setOnline(false);
                userRef.setValue(jugador);
            }
        }


        //   finish();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (UtilityNetwork.isWifiAvailable(this) || UtilityNetwork.isNetworkAvailable(this)) {
            if (jugador != null && jugador.getJugadorId() != null && userRef != null) {
                jugador.setOnline(true);
                userRef.setValue(jugador);
            }
        }
        changeImage.start();
    }

    @Override
    public void onBackPressed() {
        if (UtilityNetwork.isWifiAvailable(this) || UtilityNetwork.isNetworkAvailable(this)) {
            if (jugador != null && jugador.getJugadorId() != null && userRef != null) {
                jugador.setOnline(true);
                userRef.setValue(jugador);
            }
        }

        changeImage.cancel();
        finish();
       // super.onBackPressed();

    }

    public void infoButton(View view) {
        Log.d(Constantes.TAG, "Tocado info");
        changeImage.cancel();
        Intent infointent = new Intent(getApplicationContext(), InfoActivity.class);
        finish();
        startActivity(infointent);


    }



    public void animacionPalitrokes () {

        nubeAdd();

        changeImage = new CountDownTimer(6000, 1200) {
            @Override
            public void onTick(long millisUntilFinished) {
                ImageView palitrokesIV = findViewById(R.id.palitrokesIV);
                Random rnd = new Random();
                String name = "pic" + (rnd.nextInt(116) + 34);
                int resource = getResources().getIdentifier(name, "drawable", "com.game.palitrokes");
                palitrokesIV.setImageResource(resource);
            }

            @Override
            public void onFinish() {
                sonidos.play(Sonidos.Efectos.PLING);
                this.cancel();
                this.start();
                nubeAdd();
            }
        };
        changeImage.start();


    }


    public void nubeAdd () {

        RelativeLayout frameTitulo = findViewById(R.id.frameTitulo);
        int top = frameTitulo.getTop();
        int left = frameTitulo.getLeft();
        int bottom = frameTitulo.getBottom();
        int right = frameTitulo.getRight();


        final ImageView nube1 = new ImageView(getApplicationContext());
        Random rnd = new Random();
        String name = "nube" + (rnd.nextInt(7) + 1);
        int resource = getResources().getIdentifier(name, "drawable", "com.game.palitrokes");
        nube1.setImageResource(resource);
        nube1.bringToFront();
        nube1.setVisibility(View.VISIBLE);
        frameTitulo.addView(nube1);


        int inicio = 0;
        int fin = 0;

        int aleatorio = rnd.nextInt(2);

        switch (aleatorio) {
            case 0:
                 inicio = -200;
                 fin = 1000;
                break;
            case 1:
                inicio = 1000;
                fin = -200;
                break;
        }


        Log.d(Constantes.TAG, "Aleat: " + aleatorio);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) nube1.getLayoutParams();
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        nube1.setLayoutParams(layoutParams);

        TranslateAnimation animation = new TranslateAnimation(inicio, fin, rnd.nextInt(200), rnd.nextInt(200));
        animation.setDuration(rnd.nextInt(5000) + 5000);
        animation.setInterpolator(new AccelerateInterpolator());
        animation.setRepeatCount(1);
        nube1.startAnimation(animation);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                nube1.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }
}
