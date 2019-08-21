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
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;

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
    private DatabaseReference partidasRef;
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
    private List<Records> records;
    private List<Partida> partidas;
    private Partida partida;
    private ValueEventListener partidasListener;
    private ValueEventListener recordsListener;
    private ExecutorService executorService;
    private Uri photo_uri;//para almacenar la ruta de la imagen
    private String ruta_foto;//nombre fichero creado
    private boolean permisosOK;
    private boolean soloFavoritos;
    private MediaPlayer mediaPlayer;
    private int easterEgg;
    private CountDownTimer animacionTimer;
    private Dialog jugarOnline;
    private TextView rivalReady;
    private TextView jugadorReady;
    private ImageView readyJugadorIMG;
    private ImageView readyRivalIMG;
    private TextView mensajeEstado;
    private ProgressBar progressBar;
    private ImageView avatarRival;
    private ImageButton favoritoAdd;
    private Button readyBTN;
    private boolean rivalEncontrado;
    private Partida partidaActualizacion;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

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

        // Lanzamos el diálogo para esperar a que los 2 jugadores estén preparados
        dialogoJuegoOnline();

        // Mirar si el idioma es Inglés para cambiar el ImageView del título
        String idioma = Locale.getDefault().getLanguage(); // es
        if (!idioma.equals("es")) {
            lemaIV.setImageResource(R.drawable.lemaen);
            nombreIV.setImageResource(R.drawable.logoen);
            Log.d(Constantes.TAG, "El idioma no es español");
        }

        // EasterEgg
        easterEgg = 0;

        // Flag para detectar si tenemos rival
        rivalEncontrado = false;

        // Animacion del logo
        animacionPalitrokes();

        //Lista de partidas disponibles
        partidas = new ArrayList<>();
        partida = null;
        partidaActualizacion = new Partida();

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
        partidasRef = mDatabase.child("PARTIDAS");
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


        // Hacemos una lista con las partidas existentes
        // (La partida se crea cuando un jugador da al botón jugar online)
        // y se destruye al terminar de jugar la partida o al abandonar el dialogo de esperar rival
        // Ponemos un listener que llenará la lista actualizando cada vez que los datos cambien en Firebase
        //
        partidasListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                partidas.removeAll(partidas);
                int n = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Partida partidaTmp = snapshot.getValue(Partida.class);
                    partidaTmp.setNumeroSala(n);

                    // Si el jugador tiene una partida creada o seleccionada, aquí hacemos
                    // lo que corresponda si hay cambios en su estado
                    if (jugador.getPartida() != null) {
                        // Si esta es nuestra partida...
                        if (jugador.getPartida().equals(partidaTmp.getPartidaID())) {
                            partida = partidaTmp;
                            actualizarDialogOnline();
                        }
                    }


                    // Mirar si hay un hueco disponible en la sala
                    if (partidaTmp.getJugador1ID().equals("0") || partidaTmp.getJugador2ID().equals("0")) {
                        // Mirar si solo queremos jugar con amigos
                        if (soloFavoritos) {
                            // si está en nuestra lista de favoritos lo añadimos a la lista
                            if (jugador.getFavoritosID().contains(partidaTmp.getJugador1ID()) || jugador.getFavoritosID().contains(partidaTmp.getJugador2ID())) {
                                partidas.add(partidaTmp);
                            }
                        } else {
                            // Si queremos jugar con cualquiera que esté disponible sea amigo o no, lo añadimos aquí
                            partidas.add(partidaTmp);
                        }
                    }
                    n++;
                }

                // Mostrar las partidas disponibles, distinguiendo si queremos solo amigos o todos
                String textopartidas = "";
                if (soloFavoritos) {
                    textopartidas = getString(R.string.amigos_online);
                } else {
                    textopartidas = getString(R.string.jugadores_online);

                }
                onlineTV.setText(textopartidas + partidas.size());

                botonOnline.setEnabled(true);
                favoritosBTN.setEnabled(true);
                botonOnline.setVisibility(View.VISIBLE);
                favoritosBTN.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        partidasRef.addValueEventListener(partidasListener);

    }

    private void actualizarDialogOnline() {

        if (partida != null) {

            // Si los dos huecos están ocupados, cargamos la imagen del rival
            // También actualizamos nuestro estado 'ready'
            if (!partida.getJugador1ID().equals("0") && !partida.getJugador2ID().equals("0")) {
                // Encontrado rival, desactivamos progressbar
                progressBar.setVisibility(View.INVISIBLE);
                rivalEncontrado = true;
                // Seteamos el botón del corazón dependiendo si es amigo o no
                favoritoAdd.setVisibility(View.VISIBLE);
                if (jugador.getNumeroJugador() == 1) {
                    if (jugador.getFavoritosID().contains(partida.getJugador2ID())) {
                        favoritoAdd.setImageResource(R.drawable.corazonrojo);
                    }
                } else {
                    if (jugador.getFavoritosID().contains(partida.getJugador1ID())) {
                        favoritoAdd.setImageResource(R.drawable.corazonrojo);
                    }
                }


                // Y descargamos imagen
                if (jugador.getNumeroJugador() == 1) {
                    UtilsFirebase.descargarImagenFirebaseView(getApplicationContext(), partida.getJugador2ID(), avatarRival);
                } else {
                    UtilsFirebase.descargarImagenFirebaseView(getApplicationContext(), partida.getJugador1ID(), avatarRival);
                }
            } else {
                avatarRival.setImageResource(R.drawable.search);
                progressBar.setVisibility(View.VISIBLE);
                rivalEncontrado = false;
                favoritoAdd.setVisibility(View.INVISIBLE);
            }

            // Actualizar imagen 'preparado' y mensajes
            if (jugador.getNumeroJugador() == 1) {
                if (partida.isJugador1Ready()) {
                    readyJugadorIMG.setImageResource(R.drawable.tick);
                    readyJugadorIMG.setBackgroundColor(getResources().getColor(R.color.verde));
                    jugadorReady.setText(R.string.preparado);
                } else {
                    readyJugadorIMG.setImageResource(R.drawable.update);
                    readyJugadorIMG.setBackgroundColor(getResources().getColor(R.color.rojo));
                    jugadorReady.setText(R.string.buscando);
                }
                if (partida.isJugador2Ready()) {
                    readyRivalIMG.setImageResource(R.drawable.tick);
                    readyRivalIMG.setBackgroundColor(getResources().getColor(R.color.verde));
                    rivalReady.setText(R.string.preparado);
                } else {
                    readyRivalIMG.setImageResource(R.drawable.update);
                    readyRivalIMG.setBackgroundColor(getResources().getColor(R.color.rojo));
                    rivalReady.setText(R.string.buscando);
                }
            } else {
                if (partida.isJugador2Ready()) {
                    readyJugadorIMG.setImageResource(R.drawable.tick);
                    readyJugadorIMG.setBackgroundColor(getResources().getColor(R.color.verde));
                    jugadorReady.setText(R.string.preparado);
                } else {
                    readyJugadorIMG.setImageResource(R.drawable.update);
                    readyJugadorIMG.setBackgroundColor(getResources().getColor(R.color.rojo));
                    jugadorReady.setText(R.string.buscando);
                }
                if (partida.isJugador1Ready()) {
                    readyRivalIMG.setImageResource(R.drawable.tick);
                    readyRivalIMG.setBackgroundColor(getResources().getColor(R.color.verde));
                    rivalReady.setText(R.string.preparado);
                } else {
                    readyRivalIMG.setImageResource(R.drawable.update);
                    readyRivalIMG.setBackgroundColor(getResources().getColor(R.color.rojo));
                    rivalReady.setText(R.string.buscando);
                }
            }

            // Si los 2 jugadores están preparados, lanzamos el juego
            if (partida.isJugador1Ready() && partida.isJugador2Ready()) {
             /*   partida.setJugando(true);
                partida.setGanador(0);
                partidasRef.child(partida.getPartidaID()).setValue(partida);
             */

                // Quitar el listener de las partidas
                partidasRef.removeEventListener(partidasListener);

                // Los 2 estamos listos. Lanzar Intent de juego
                Intent jugar = new Intent(jugarOnline.getContext(), JuegoOnlineActivity.class);
                jugar.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                jugar.putExtra(Constantes.PARTIDA, partida.getPartidaID());

                if (jugador.getNumeroJugador() == 1) {
                    jugar.putExtra(Constantes.RIVALID, partida.getJugador2ID());
                    jugar.putExtra(Constantes.JUGADORID, partida.getJugador1ID());
                } else {
                    jugar.putExtra(Constantes.RIVALID, partida.getJugador1ID());
                    jugar.putExtra(Constantes.JUGADORID, partida.getJugador2ID());
                }

                //animacionTitulo.cancel(true);
                animacionTimer.cancel();
                //   finish();
                Sonidos.getInstance(getApplicationContext()).play(Sonidos.Efectos.START);
                mediaPlayer.stop();
                startActivity(jugar);
                jugarOnline.dismiss();
            }

        }
    }


    // ------------------------------------------------------------------------------------------------------------------
    // Cuando pulsamos el botón de Jugar Online hacemos esto ...
    public void jugarOnline(View view) {

        // Tenemos un listener que apunte a las salas siempre escuchando
        // Creamos la sala cuando el jugador da al botón de jugar online
        // O si hay otra persona esperando rival (Ya hay sala creada con hueco libre), lo ocupamos
        //Subimos nuestro avatar a Firebase (Aquí es seguro que tenemos internet)
        SharedPrefs.saveJugadorPrefs(getApplicationContext(), jugador);
        UtilsFirebase.subirImagenFirebase(mAuth.getCurrentUser().getUid(), Utilidades.recuperarImagenMemoriaInterna(getApplicationContext(), jugador.getJugadorId()));

        jugarOnline.show();

        // Limitar a 20 partidas simultáneas
        if (partidas.size() > 20) {
            Toast.makeText(MainActivity.this, R.string.sin_salas, Toast.LENGTH_LONG).show();
        } else {
            // Si no hay mas jugadores esperando partida, lo avisamos
            // Y creamos una sala con un hueco disponible
            if (partidas.size() == 0) {
                Toast.makeText(MainActivity.this, R.string.noplayers, Toast.LENGTH_LONG).show();
                // Creamos partida con hueco vacío
                partida = new Partida("SALA" + System.currentTimeMillis(), 0, jugador.getJugadorId(), "0", new Tablero(12), 12);
                // Añadimos a Firebase la nueva partida
                partidasRef.child(partida.getPartidaID()).setValue(partida);
                // En nuestro jugador indicamos que tenemos partida
                jugador.setPartida(partida.getPartidaID());
                jugador.setNumeroJugador(1);

            } else {
                // Hay jugadores disponibles online, ocupamos un hueco libre en la primera sala que encontremos
                // Seleccionamos una partida y llenamos el hueco disponible con nuestro id (La primera disponible)
                partida = partidas.get(0);
                // Mirar si el hueco disponible es el 1 o el 2
                if (partida.getJugador1ID().equals("0")) {
                    partida.setJugador1ID(jugador.getJugadorId());
                    jugador.setNumeroJugador(1);
                } else {
                    partida.setJugador2ID(jugador.getJugadorId());
                    jugador.setNumeroJugador(2);
                }
                // Actualizamos a Firebase con la nueva partida
                partidasRef.child(partida.getPartidaID()).setValue(partida);
                // En nuestro jugador indicamos que tenemos partida
                jugador.setPartida(partida.getPartidaID());

            }

        }

    }

    private void dialogoJuegoOnline() {

        // Dialog jugar online
        jugarOnline = new Dialog(this);
        jugarOnline.setContentView(R.layout.dialog_jugar);
        jugarOnline.setTitle(R.string.jugar_online);

        rivalReady = jugarOnline.findViewById(R.id.estadoRivalTV);
        jugadorReady = jugarOnline.findViewById(R.id.estadoJugadorTV);
        readyJugadorIMG = jugarOnline.findViewById(R.id.imageReadyJugadorTV);
        readyRivalIMG = jugarOnline.findViewById(R.id.imageReadyRivalIV);
        mensajeEstado = jugarOnline.findViewById(R.id.mensajeEstadoTV);
        progressBar = jugarOnline.findViewById(R.id.progressBar2);
        avatarRival = jugarOnline.findViewById(R.id.rivalImageIV);
        favoritoAdd = jugarOnline.findViewById(R.id.dialog_favoritosBTN);
        readyBTN = jugarOnline.findViewById(R.id.readyBTN);

        // Inicializamos vistas
        mensajeEstado.setText(R.string.buscando);
        jugadorReady.setText(R.string.jugador);
        readyJugadorIMG.setImageResource(R.drawable.update);
        readyJugadorIMG.setBackgroundColor(getResources().getColor(R.color.rojo));
        rivalReady.setText(R.string.rival);
        readyRivalIMG.setImageResource(R.drawable.update);
        readyRivalIMG.setBackgroundColor(getResources().getColor(R.color.rojo));

        //  Controlar si el usuario pulsa back en el dispositivo:
        //  Actualizar Firebase para notificar los cambios y cerrar el diálogo
        jugarOnline.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.d(Constantes.TAG, "Cancelado Dialog");

                if (partida != null) {
                    // borramos nuestro id de jugador de  la partida y actualizamos Firebase
                    if (jugador.getNumeroJugador() == 1) {
                        partida.setJugador1ID("0");
                        partida.setJugador1Ready(false);
                    } else {
                        partida.setJugador2ID("0");
                        partida.setJugador2Ready(false);
                    }
                    partidasRef.child(partida.getPartidaID()).setValue(partida);
                }

                limpiarSala();

                jugarOnline.dismiss();
            }
        });


        // Si hacemos click en el botón 'Preparado' notificamos a Firebase,
        // para que salte el evento correspondiente
        readyBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (jugador.getNumeroJugador() == 1) {
                    partida.setJugador1Ready(true);
                    partidasRef.child(partida.getPartidaID()).setValue(partida);
                } else {
                    partida.setJugador2Ready(true);
                    partidasRef.child(partida.getPartidaID()).setValue(partida);
                }

            }
        });


        favoritoAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rivalEncontrado) {
                    amigosSwitch();
                }
            }
        });

    }

    private void amigosSwitch() {

        Log.d(Constantes.TAG, "Favoritos");
        if (jugador.getFavoritosID() == null)
            jugador.setFavoritosID(new ArrayList<String>());


        if (jugador.getNumeroJugador() == 1) {
            // Si el rival encontrado ya lo teníamos como favorito, lo borramos
            if (jugador.getFavoritosID().contains(partida.getJugador2ID())) {
                jugador.getFavoritosID().remove(partida.getJugador2ID());
                Toast.makeText(MainActivity.this, R.string.amigo_del, Toast.LENGTH_LONG).show();
                favoritoAdd.setImageResource(R.drawable.corazon);
            } else {
                // Si no era nuestro amigo, lo añadimos
                jugador.getFavoritosID().add(partida.getJugador2ID());
                Toast.makeText(MainActivity.this, R.string.amigo_add, Toast.LENGTH_LONG).show();
                favoritoAdd.setImageResource(R.drawable.corazonrojo);
            }
        } else {
            // Si el rival encontrado ya lo teníamos como favorito, lo borramos
            if (jugador.getFavoritosID().contains(partida.getJugador1ID())) {
                jugador.getFavoritosID().remove(partida.getJugador1ID());
                Toast.makeText(MainActivity.this, R.string.amigo_del, Toast.LENGTH_LONG).show();
                favoritoAdd.setImageResource(R.drawable.corazon);
            } else {
                // Si no era nuestro amigo, lo añadimos
                jugador.getFavoritosID().add(partida.getJugador1ID());
                Toast.makeText(MainActivity.this, R.string.amigo_add, Toast.LENGTH_LONG).show();
                favoritoAdd.setImageResource(R.drawable.corazonrojo);
            }
        }

        // Actualizamos la BB.DD.
        jugador.setActualizado(System.currentTimeMillis() + "");
        userRef.setValue(jugador);


    }

    // ------------------------------------------------------------------------------------------------------------------

    // Controlar que si cerramos la aplicación y el jugador tiene
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
        for (int n = 0; n < 5; n++) {
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

    private void limpiarSala() {
        if (partida.getJugador2ID().equals("0") && partida.getJugador1ID().equals("0")) {
            partidasRef.child(partida.getPartidaID()).removeValue();
        }
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
        recuperarDatosSharedPreferences();
        // Iniciar música
        bgm();
        mediaPlayer.start();
        animacionTimer.start();
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
            partidaActualizacion.setPartidaID(System.currentTimeMillis() + "");
            partidaActualizacion.setJugador1ID("SALA ACTUALIZACIONES");
            partidaActualizacion.setJugador2ID("SALA ACTUALIZACIONES");
            partidasRef.child("SALA1566320669253").setValue(partidaActualizacion);
        }


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


}