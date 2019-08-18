package com.game.palitrokes;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.game.palitrokes.Adapters.RecordsAdapter;
import com.game.palitrokes.Modelos.Jugador;
import com.game.palitrokes.Modelos.Partida;
import com.game.palitrokes.Modelos.Records;
import com.game.palitrokes.Utilidades.AnimacionTitulo;
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
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String[] PERMISOS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
  //  AnimacionTitulo animacionTitulo;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference userRef;
    private DatabaseReference jugadoresRef;
    private DatabaseReference recordsRef;
    private EditText nickET;
    private TextView onlineTV, victoriasTV;
    private Button botonOnline;
    private ImageView avatarJugador;
    private ImageView palitrokesIV, lemaIV, nombreIV;
    private ImageButton favoritosBTN;
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
    private ExecutorService executorService;
    private Uri photo_uri;//para almacenar la ruta de la imagen
    private String ruta_foto;//nombre fichero creado
    private boolean permisosOK;
    private String salaAnterior;
    private boolean soloFavoritos;
    private MediaPlayer mediaPlayer;
    private int easterEgg;
    private CountDownTimer animacionTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        Intent intent = getIntent();
        salaAnterior = intent.getStringExtra(Constantes.SALA_ANTERIOR);

        // Sonidos y BGM
        Sonidos.getInstance(getApplicationContext());
    //    bgm();


        // Referencias a las vistas
        nickET = findViewById(R.id.nickET);
        victoriasTV = findViewById(R.id.victoriasET);
        onlineTV = findViewById(R.id.onlineTV);
        recordsRecycler = findViewById(R.id.recordsRecycler);
        layoutManager = new LinearLayoutManager(this);
        recordsRecycler.setLayoutManager(layoutManager);
        botonOnline = findViewById(R.id.jugaronlineBTN);
        avatarJugador = findViewById(R.id.avatarIV);
        lemaIV = findViewById(R.id.lemaIV);
        favoritosBTN = findViewById(R.id.favoritosBTN);
        nombreIV = findViewById(R.id.nombreIV);
        fab = findViewById(R.id.fab);
        fab.bringToFront();


        // Mirar si el idioma es Inglés para cambiar el ImageView del título
        String idioma = Locale.getDefault().getLanguage(); // es
        if (!idioma.equals("es")) {
            lemaIV.setImageResource(R.drawable.lemaen);
            nombreIV.setImageResource(R.drawable.logoen);
            Log.d(Constantes.TAG, "El idioma no es español");
        }

        // EasterEgg
        easterEgg = 0;

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




    }

    private void bgm() {
        Random rnd = new Random();
        switch (rnd.nextInt(3)) {
            case 0:
                mediaPlayer = MediaPlayer.create(this, R.raw.cutebgm);
                break;
            case 1:
                mediaPlayer = MediaPlayer.create(this, R.raw.sunny);
                break;
            case 2:
                mediaPlayer = MediaPlayer.create(this, R.raw.ukelele);
                break;
        }

    }

    private void signIn() {
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
                                Toast.makeText(MainActivity.this, R.string.fallo_auth, Toast.LENGTH_SHORT).show();
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
                        nickName = getString(R.string.jugador);
                    } else {
                        nickName = nickET.getText().toString();
                        if (Utilidades.eliminarPalabrotas(nickName)) {
                            Toast.makeText(getApplicationContext(), (getString(R.string.palabrota)), Toast.LENGTH_LONG).show();
                            nickName = getString(R.string.jugador);
                        }
                    }
                    jugador = new Jugador(currentUser.getUid(), nickName);
                    jugador.setOnline(true);
                    jugador.setActualizado(System.currentTimeMillis() + "");
                    userRef.setValue(jugador);

                    // Subimos una imagen a Firebase Storage con el nombre del ID del jugador
                    // para usarla como avatar
                    Bitmap avatarNuevo = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.picture);
                    UtilsFirebase.subirImagenFirebase(currentUser.getUid(), avatarNuevo);
                    Utilidades.guardarImagenMemoriaInterna(getApplicationContext(), Constantes.ARCHIVO_IMAGEN_JUGADOR, Utilidades.bitmapToArrayBytes(avatarNuevo));
                    Utilidades.guardarImagenMemoriaInterna(getApplicationContext(), jugador.getJugadorId(), Utilidades.bitmapToArrayBytes(avatarNuevo));
                    avatarJugador.setImageBitmap(avatarNuevo);
                    SharedPrefs.saveJugadorPrefs(getApplicationContext(), jugador);

                } else {
                    jugador = SharedPrefs.getJugadorPrefs(getApplicationContext());
                    jugador.setOnline(true);
                    jugador.setActualizado(System.currentTimeMillis() + "");
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

      /*              // chequear los jugadores por si se ha quedado pillado alguno 'online' por salir de la app mal, etc
                    long tiempoonline = 0;
                    if (jugadorTMP.getActualizado() != null){
                        tiempoonline = System.currentTimeMillis() - Long.parseLong(jugadorTMP.getActualizado());
                    }
                    Log.d(Constantes.TAG, "Jugador que lleva " + (tiempoonline / 1000) + " segundos online");
                    // si lleva mas de 10 minutos online, pero sin actualizar, cambiamos el estado a 'false'
                    if (tiempoonline > (10 * 60 * 1000)) {
                        jugadorTMP.setOnline(false);
                        jugadoresRef.child(jugadorTMP.getJugadorId()).setValue(jugadorTMP);
                    }*/

                    if (jugadorTMP.isOnline()) {
                        // Filtrar si solo queremos jugar con favoritos
                        if (!jugador.getJugadorId().equals(jugadorTMP.getJugadorId())) {
                            if (!soloFavoritos) {
                                jugadores.add(jugadorTMP);
                            } else {
                                if (jugador.getFavoritosID() != null) {
                                    for (String favorito : jugador.getFavoritosID()) {
                                        if (favorito.equals(jugadorTMP.getJugadorId())) {
                                            jugadores.add(jugadorTMP);
                                        }
                                    }
                                }
                            }
                        }

                    }
                }

                // Mostramos en pantalla en número de jugadores disponibles online (o favoritos online)
                if (soloFavoritos) {
                    onlineTV.setText((getString(R.string.amigosonline)) + jugadores.size());
                } else {
                    onlineTV.setText((getString(R.string.jugonline)) + jugadores.size());
                }

                if (jugadores.size() > 0) {
                    botonOnline.setEnabled(true);
                    favoritosBTN.setEnabled(true);
                } else {
                    botonOnline.setEnabled(false);
                }
                botonOnline.setVisibility(View.VISIBLE);
                favoritosBTN.setVisibility(View.VISIBLE);

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
                    UtilsFirebase.descargarImagenFirebaseYGuardarla(getApplicationContext(), recordTmp.getIdJugador());
                    n++;
                }
                adapter.notifyDataSetChanged();
                // Guardamos los records actualizados de Firebase en el Shared Preferences
                SharedPrefs.saveRecordsPrefs(getApplicationContext(), records);

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
      //  userRef.removeEventListener(jugadoresListener);

        //Subimos nuestro avatar a Firebase (Aquí es seguro que tenemos internet)
        SharedPrefs.saveJugadorPrefs(getApplicationContext(), jugador);
        UtilsFirebase.subirImagenFirebase(mAuth.getCurrentUser().getUid(), Utilidades.recuperarImagenMemoriaInterna(getApplicationContext(), jugador.getJugadorId()));

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
        jugarOnline.setTitle(R.string.jugar_online);
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
        final ImageButton favoritoAdd = jugarOnline.findViewById(R.id.dialog_favoritosBTN);

        // Hacemos una lista con las salas disponibles en Firebase
        partidasListener = new ValueEventListener() {
            List<Partida> salasDisponibles = new ArrayList<>();


            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // Actualizamos los views del Dialog
                jugadorReady.setText(jugador.getNickname());
                mensajeEstado.setText(R.string.buscando);
                avatarRival.setImageResource(R.drawable.search);
                favoritoAdd.setVisibility(View.INVISIBLE);

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

                        String nickName = nickET.getText().toString();
                        if (Utilidades.eliminarPalabrotas(nickName)) {
                            Toast.makeText(getApplicationContext(), (getString(R.string.palabrota)), Toast.LENGTH_LONG).show();
                            nickET.setText(getString(R.string.jugador));
                            nickName = getString(R.string.jugador);
                        }

                        jugador.setNickname(nickName);
                        jugador.setActualizado(System.currentTimeMillis() + "");
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
                        Toast.makeText(getApplicationContext(), R.string.sin_salas, Toast.LENGTH_LONG).show();
                    }

                } else {

                    // si ya tenemos una sala seleccionada, miramos que haya rival y que los 2 estén preparados
                    // Si nosotros estamos en el hueco 1 esperamos a que el hueco 2 esté ocupado
                    if (jugador.getNumeroJugador() == 1) {
                        Log.d(Constantes.TAG, "Estamos en el hueco 1 de la " + salaSeleccionada.getPartidaID());
                        if (!salaSeleccionada.getJugador2ID().equals("0")) {
                            // Hay otro jugador que ha seleccionado esta sala
                            Log.d(Constantes.TAG, "Hay otro jugador en el hueco 2");
                            mensajeEstado.setText(R.string.encontrado_rival);
                            // Descargamos la imagen del Rival y la visualizamos
                            UtilsFirebase.descargarImagenFirebaseView(getApplicationContext(), salaSeleccionada.getJugador2ID(), avatarRival);
                            pausa(1000);
                            progressBar.setVisibility(View.INVISIBLE);
                            favoritoAdd.setVisibility(View.VISIBLE);
                            if (salaSeleccionada.isJugador2Ready()) {
                                // El otro jugador está listo
                                Log.d(Constantes.TAG, "El jugador 2 está preparado");
                                readyRivalIMG.setImageResource(R.drawable.tick);
                                readyRivalIMG.setBackgroundColor(getResources().getColor(R.color.verde));
                                rivalReady.setText(R.string.preparado);
                                if (salaSeleccionada.isJugador1Ready()) {

                                    // Los 2 estamos listos. Lanzar Intent de juego
                                    // Como somos el jugador 1 tenemos el turno (Estas son mis reglas)
                                    mDatabase.child("PARTIDAS").removeEventListener(partidasListener);
                                    Log.d(Constantes.TAG, "Lanzar Juego");
                                    Intent jugar = new Intent(jugarOnline.getContext(), JuegoOnlineActivity.class);
                                    jugar.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    jugar.putExtra("PARTIDA", salaSeleccionada.getPartidaID());
                                    jugar.putExtra(Constantes.RIVALID, salaSeleccionada.getJugador2ID());
                                  //  animacionTitulo.cancel(true);
                                    animacionTimer.cancel();
                                  //  finish();
                                    Sonidos.getInstance(getApplicationContext()).play(Sonidos.Efectos.START);
                                    mediaPlayer.stop();
                                    startActivity(jugar);
                                    jugarOnline.dismiss();
                                }
                            } else {
                                readyRivalIMG.setImageResource(R.drawable.update);
                                readyRivalIMG.setBackgroundColor(getResources().getColor(R.color.rojo));
                                rivalReady.setText(R.string.esperandorival);
                            }
                        }

                    } else {
                        // si estamos en el hueco 2 hacemos lo mismo que antes, pero para este hueco
                        Log.d(Constantes.TAG, "Estamos en el hueco 2 de la " + salaSeleccionada.getPartidaID());
                        if (!salaSeleccionada.getJugador1ID().equals("0")) {
                            // Hay otro jugador que ha seleccionado esta sala
                            Log.d(Constantes.TAG, "Hay otro jugador en el hueco 1");
                            mensajeEstado.setText(R.string.encontrado_rival);
                            // Descargamos la imagen del Rival y la visualizamos
                            UtilsFirebase.descargarImagenFirebaseView(getApplicationContext(), salaSeleccionada.getJugador1ID(), avatarRival);
                            pausa(1000);
                            progressBar.setVisibility(View.INVISIBLE);
                            favoritoAdd.setVisibility(View.VISIBLE);
                            if (salaSeleccionada.isJugador1Ready()) {
                                // El otro jugador está listo
                                Log.d(Constantes.TAG, "El jugador 1 está preparado");
                                readyRivalIMG.setImageResource(R.drawable.tick);
                                readyRivalIMG.setBackgroundColor(getResources().getColor(R.color.verde));
                                rivalReady.setText(R.string.preparado);
                                if (salaSeleccionada.isJugador2Ready()) {
                                    mDatabase.child("PARTIDAS").removeEventListener(partidasListener);
                                    // Los 2 estamos listos. Lanzar Intent de juego
                                    Intent jugar = new Intent(jugarOnline.getContext(), JuegoOnlineActivity.class);
                                    jugar.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    jugar.putExtra("PARTIDA", salaSeleccionada.getPartidaID());
                                    jugar.putExtra(Constantes.RIVALID, salaSeleccionada.getJugador1ID());
                                    //animacionTitulo.cancel(true);
                                    animacionTimer.cancel();
                                 //   finish();
                                    Sonidos.getInstance(getApplicationContext()).play(Sonidos.Efectos.START);
                                    mediaPlayer.stop();
                                    startActivity(jugar);
                                    jugarOnline.dismiss();
                                }
                            } else {
                                readyRivalIMG.setImageResource(R.drawable.update);
                                readyRivalIMG.setBackgroundColor(getResources().getColor(R.color.rojo));
                                rivalReady.setText(R.string.esperandorival);
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
                mensajeEstado.setText(R.string.preparado_esperando);
                jugadorReady.setText(R.string.preparado);
                readyJugadorIMG.setImageResource(R.drawable.tick);
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


        // Botón añadir jugador a favoritos
        favoritoAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(Constantes.TAG, "Favoritos");
                if (jugador.getFavoritosID() == null)
                    jugador.setFavoritosID(new ArrayList<String>());

                if (salaSeleccionada != null) {


                    if (jugador.getNumeroJugador() == 1) {
                        if (!jugador.getFavoritosID().contains(salaSeleccionada.getJugador2ID())) {
                            // Añadir el jugador encontrado como favorito
                            jugador.getFavoritosID().add(salaSeleccionada.getJugador2ID());
                            Toast.makeText(MainActivity.this, R.string.amigo_add, Toast.LENGTH_LONG).show();
                            favoritoAdd.setImageResource(R.drawable.corazonrojo);
                        } else {
                            // Borrar este jugador como favorito
                            jugador.getFavoritosID().remove(salaSeleccionada.getJugador2ID());
                            Toast.makeText(MainActivity.this, R.string.amigo_del, Toast.LENGTH_LONG).show();
                            favoritoAdd.setImageResource(R.drawable.corazon);

                        }
                    } else if (jugador.getNumeroJugador() == 2) {
                        if (!jugador.getFavoritosID().contains(salaSeleccionada.getJugador1ID())) {
                            // Añadir el jugador encontrado como favorito
                            jugador.getFavoritosID().add(salaSeleccionada.getJugador1ID());
                            Toast.makeText(MainActivity.this, R.string.amigo_add, Toast.LENGTH_LONG).show();
                            favoritoAdd.setImageResource(R.drawable.corazonrojo);
                        } else {
                            // Borrar este jugador como favorito
                            jugador.getFavoritosID().remove(salaSeleccionada.getJugador1ID());
                            Toast.makeText(MainActivity.this, R.string.amigo_del, Toast.LENGTH_LONG).show();
                            favoritoAdd.setImageResource(R.drawable.corazon);
                        }
                    }

                    // Actualizamos la BB.DD.
                    jugador.setActualizado(System.currentTimeMillis() + "");
                    userRef.setValue(jugador);


                }

            }
        });


    }


    // Controlar que si cerramos la aplicación y el juegador tiene
    //  asignado una sala, borrarlo de Firebase para que no se quede pillada la sala

    @Override
    protected void onStop() {

        if (UtilityNetwork.isNetworkAvailable(this) || UtilityNetwork.isWifiAvailable(this)) {
            if (jugador != null && userRef != null) {
                jugador.setOnline(false);
                jugador.setActualizado(System.currentTimeMillis() + "");
                userRef.setValue(jugador);
            }
     /*       if (salaSeleccionada != null) {
                limpiarSala(salaSeleccionada.getPartidaID());
            }*/
        }
        //   animacionTitulo.cancel(true);
        mediaPlayer.stop();

        super.onStop();

    }


    //
    // Aquí lanzamos el juego contra el ordenador (Móvil en este caso)
    //
    public void jugar(View view) {


        // Ponemos al false el que el jugador está online para que no le tengan en cuenta para jugar en este modo
        if (jugador != null) {
            if (UtilityNetwork.isWifiAvailable(this) || UtilityNetwork.isNetworkAvailable(this)) {
                jugador.setOnline(false);
                jugador.setActualizado(System.currentTimeMillis() + "");
                userRef.setValue(jugador);
            }
        } else {
            jugador = new Jugador();
        }

        if (nickET.getText() != null) {

            String nickName = nickET.getText().toString();
            if (Utilidades.eliminarPalabrotas(nickName)) {
                Toast.makeText(getApplicationContext(), (getString(R.string.palabrota)), Toast.LENGTH_LONG).show();
                nickET.setText(getString(R.string.jugador));
                nickName = getString(R.string.jugador);
            }

            jugador.setNickname(nickName);
        }


        SharedPrefs.saveJugadorPrefs(getApplicationContext(), jugador);

        Intent intentvscom = new Intent(this, JuegoVsComActivity.class);
        intentvscom.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
       // animacionTitulo.cancel(true);
        animacionTimer.cancel();
        mediaPlayer.stop();
       // finish();
        Sonidos.getInstance(getApplicationContext()).play(Sonidos.Efectos.START);
        startActivity(intentvscom);


    }

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

        easterEgg++;
        Sonidos.getInstance(getApplicationContext()).play(Sonidos.Efectos.TICK);


        if (easterEgg == 10) {

            easterEgg = 0;
            Sonidos.getInstance(getApplicationContext()).play(Sonidos.Efectos.MAGIA);
            palitrokesIV.setImageDrawable(null);
            palitrokesIV.setImageResource(R.drawable.pic149);
            RotateAnimation rotate = new RotateAnimation(0, 360,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                    0.5f);

            rotate.setDuration(2000);
            rotate.setRepeatCount(0);
            nombreIV.startAnimation(rotate);


        }

        //  resetearRecords();
/*
        // Crear Salas en Firebase
        for (int n = 0; n < 30; n++) {
            mDatabase.child("PARTIDAS").child("Sala " + n).setValue(new Partida("Sala " + n, n, "0", "0", new Tablero(0), 0));
        }
*/
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ((grantResults[0] == PackageManager.PERMISSION_GRANTED)
                && (grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
            Log.d("MIAPP", "ME ha concecido los permisos");
        } else {
            Log.d("MIAPP", "NO ME ha concecido los permisos");
            Toast.makeText(this, R.string.mensaje_permisos, Toast.LENGTH_SHORT).show();
            permisosOK = false;
        }
    }

    public void tomarFoto() {

        Log.d("MIAPP", "Quiere hacer una foto");
        Intent intent_foto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        this.photo_uri = Utilidades.crearFicheroImagen();
        intent_foto.putExtra(MediaStore.EXTRA_OUTPUT, this.photo_uri);
        Utilidades.desactivarModoEstricto();
       // animacionTitulo.cancel(true);
        animacionTimer.cancel();
        mediaPlayer.stop();
        startActivityForResult(intent_foto, Constantes.CODIGO_PETICION_HACER_FOTO);

    }

    public void seleccionarFoto() {
        Log.d("MIAPP", "Quiere seleccionar una foto");
       // animacionTitulo.cancel(true);
        animacionTimer.cancel();
        Intent intent_pide_foto = new Intent();
        //intent_pide_foto.setAction(Intent.ACTION_PICK);//seteo la acción para galeria
        intent_pide_foto.setAction(Intent.ACTION_GET_CONTENT);//seteo la acción
        intent_pide_foto.setType("image/*");//tipo mime
        mediaPlayer.stop();
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


                // De paso guardamos los datos del jugador (Nickname, id, victorias en el Shared Preferences)
                if (jugador == null) {
                    jugador = new Jugador();
                }
                if (!nickET.getText().toString().equals("")) {
                    String nickName = nickET.getText().toString();
                    if (Utilidades.eliminarPalabrotas(nickName)) {
                        Toast.makeText(getApplicationContext(), (getString(R.string.palabrota)), Toast.LENGTH_LONG).show();
                        nickET.setText(getString(R.string.jugador));
                        nickName = getString(R.string.jugador);
                    }

                    jugador.setNickname(nickName);
                }
                SharedPrefs.saveJugadorPrefs(getApplicationContext(), jugador);

                // Guardamos una copia del archivo en el dispositivo para utilizarlo mas tarde
                Utilidades.guardarImagenMemoriaInterna(getApplicationContext(), jugador.getJugadorId(), Utilidades.bitmapToArrayBytes(selectedImage));


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


                // De paso guardamos los datos del jugador (Nickname, id, victorias en el Shared Preferences)
                if (jugador == null) {
                    jugador = new Jugador();
                }
                if (!nickET.getText().toString().equals("")) {
                    String nickName = nickET.getText().toString();
                    if (Utilidades.eliminarPalabrotas(nickName)) {
                        Toast.makeText(getApplicationContext(), (getString(R.string.palabrota)), Toast.LENGTH_LONG).show();
                        nickET.setText(getString(R.string.jugador));
                        nickName = getString(R.string.jugador);
                    }

                    jugador.setNickname(nickName);
                }
                SharedPrefs.saveJugadorPrefs(getApplicationContext(), jugador);


                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                selectedImage = Utilidades.getResizedBitmap(selectedImage, 128);// 400 is for example, replace with desired size
                this.avatarJugador.setImageBitmap(selectedImage);

                // Guardamos una copia del archivo en el dispositivo para utilizarlo mas tarde
                Utilidades.guardarImagenMemoriaInterna(getApplicationContext(), jugador.getJugadorId(), Utilidades.bitmapToArrayBytes(selectedImage));


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
//            Utilidades.guardarImagenMemoriaInterna(getApplicationContext(), jugador.getJugadorId(), Utilidades.bitmapToArrayBytes());
          //  animacionTitulo.cancel(true);
            animacionTimer.cancel();
            Intent infointent = new Intent(getApplicationContext(), InfoActivity.class);
            //mediaPlayer.stop();
        //    finish();
            startActivity(infointent);


        }

        // Seteamos la imagen del avatar con el archivo guardado localmente en el dispositivo
        // Este archivo se actualiza cada vez que lo personalizamos con una imagen de la galería o la cámara
        Bitmap recuperaImagen = Utilidades.recuperarImagenMemoriaInterna(getApplicationContext(), jugador.getJugadorId());
        if (recuperaImagen != null) {
            avatarJugador.setImageBitmap(recuperaImagen);
        } else {
            avatarJugador.setImageResource(R.drawable.picture);
        }
        // Mostramos el nick del jugador y las victorias
        nickET.setText(jugador.getNickname());
        victoriasTV.setText((getString(R.string.victorias2)) + jugador.getVictorias());

        // Cargamos records y los mostramos
        records = SharedPrefs.getRecordsPrefs(getApplicationContext());
        adapter = new RecordsAdapter(getApplicationContext(), records);
        recordsRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }


    @Override
    protected void onPause() {

        if (UtilityNetwork.isWifiAvailable(this) || UtilityNetwork.isNetworkAvailable(this)) {
            if (jugador != null && jugador.getJugadorId() != null && userRef != null) {
                jugador.setOnline(false);
                jugador.setActualizado(System.currentTimeMillis() + "");
                userRef.setValue(jugador);
            }
        }
        mediaPlayer.stop();


        super.onPause();
     //   animacionTitulo.cancel(true);

      //    finish();

    }

    @Override
    protected void onDestroy() {

        avatarJugador.setBackground(null);
        avatarJugador.setImageDrawable(null);
//        palitrokesIV.setImageDrawable(null);
        mediaPlayer = null;
        System.gc();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        Log.d(Constantes.TAG, "On resume");

        // Iniciar música
        bgm();
        mediaPlayer.start();
        animacionTimer.start();
    //    animacionTitulo.setRunning(true);
       // if (animacionTitulo.isCancelled()) animacionTitulo.executeOnExecutor(executorService);


        signIn();

        super.onResume();

    }



    @Override
    public void onBackPressed() {
        if (UtilityNetwork.isWifiAvailable(this) || UtilityNetwork.isNetworkAvailable(this)) {
            if (jugador != null && jugador.getJugadorId() != null && userRef != null) {
                jugador.setOnline(true);
                jugador.setActualizado(System.currentTimeMillis() + "");
                userRef.setValue(jugador);
            }
        }

        //  changeImage.cancel();
        mediaPlayer.stop();
        finish();
        // super.onBackPressed();

    }

    public void infoButton(View view) {
        Log.d(Constantes.TAG, "Tocado info");
      //  animacionTitulo.cancel(true);
        animacionTimer.cancel();
        Intent infointent = new Intent(getApplicationContext(), InfoActivity.class);
        mediaPlayer.stop();
      //  finish();
        startActivity(infointent);


    }


    public void animacionPalitrokes() {
/*  Quito esto porque come muchos recursos
        // Añadir nubes en un Asynctask al layout del título
        ImageView nube1 = findViewById(R.id.nube1);
        ImageView nube2 = findViewById(R.id.nube2);
        ImageView nube3 = findViewById(R.id.nube3);
        nubeAdd(nube1);
        nubeAdd(nube2);
        nubeAdd(nube3);
*/
/*
        // movidas para que ejecute todos los hilos simultaneamente
        int processors = Runtime.getRuntime().availableProcessors();
        executorService = Executors.newFixedThreadPool(processors);
*/
        // Cambiar la imagen del personaje cada tiempo en un asynctask
        palitrokesIV = findViewById(R.id.palitrokesIV);


         animacionTimer = new CountDownTimer(60000, 3000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Random rnd = new Random();
                String name = "pic" + (rnd.nextInt(116) + 34);
                int resource = getResources().getIdentifier(name, "drawable", "com.game.palitrokes");
                palitrokesIV.setImageDrawable(null);
                palitrokesIV.setImageResource(resource);
            }

            @Override
            public void onFinish() {
                this.start();
            }
        };

        animacionTimer.start();



/*
        animacionTitulo = new AnimacionTitulo();
        animacionTitulo.recuperarImageView(getApplicationContext(), palitrokesIV);
        animacionTitulo.executeOnExecutor(executorService);
*/

    }

    public void favoritosToggle(View view) {
        if (soloFavoritos) {
            soloFavoritos = false;
            favoritosBTN.setImageResource(R.drawable.corazon);
        } else {
            soloFavoritos = true;
            favoritosBTN.setImageResource(R.drawable.corazonrojo);
        }


        // Refrescamos la base de datos si tenemos internet
        if (UtilityNetwork.isNetworkAvailable(this) || UtilityNetwork.isWifiAvailable(this)) {
            jugador.setActualizado(System.currentTimeMillis() + "");
            userRef.setValue(jugador);
        }

    }


}
