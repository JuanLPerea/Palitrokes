package com.game.palitrokes;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.game.palitrokes.Adapters.RecordsAdapter;
import com.game.palitrokes.Modelos.Jugador;
import com.game.palitrokes.Modelos.Partida;
import com.game.palitrokes.Modelos.Tablero;
import com.game.palitrokes.Utilidades.Constantes;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    
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
    private Partida salaSeleccionada;
    private static final String[] PERMISOS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private Uri photo_uri;//para almacenar la ruta de la imagen
    private String ruta_foto;//nombre fichero creado
    private boolean permisosOK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        Intent intent = getIntent();
        String salaAnterior = intent.getStringExtra("SALA_ANTERIOR");

                if (salaAnterior != null){

                    Log.d(Constantes.TAG, "Ha vuelto de otro intent " + salaAnterior);
                    // Dejamos una pausa para que se actualice la sala
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

        // Referencias a las vistas
        nickET = findViewById(R.id.nickET);
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

        // Pedir permisos para las fotos y avatares
        ActivityCompat.requestPermissions
                (this, PERMISOS, Constantes.CODIGO_PETICION_PERMISOS );

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

                    // Subimos una imagen a Firebase Storage con el nombre del ID del jugador
                    // para usarla como avatar
                    Utilidades.subirImagenFirebase(currentUser.getUid(), getResources().getDrawable(R.drawable.paco));
/*
                } else {
                    nickET.setText(jugador.getNickname());

                    // Recuperamos la imagen del usuario del archivo en el dispositivo, si existe
                    // si no existe, la cargamos de Firebase, que siempre la tenemos
                    File fotopath = getDatabasePath(Utilidades.crearNombreArchivo());
                    boolean existe_foto = fotopath.exists();
                    if (existe_foto) {
                        InputStream imageStream = null;
                        try {
                            imageStream = getContentResolver().openInputStream(Uri.fromFile(fotopath));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        selectedImage = Utilidades.getResizedBitmap(selectedImage, 128);// 400 is for example, replace with desired size
                        avatarJugador.setImageBitmap(selectedImage);
                    } else {
                        Utilidades.descargarImagenFirebase(jugador.getJugadorId() , avatarJugador);
                    }

*/
                }


                // Log.d(Constantes.TAG, "Usuario BBDD " + jugador.getNickname());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(Constantes.TAG, "Error Usuario BBDD o Crear nuevo");
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

        //  Controlar si el usuario pulsa back en el dispositivo: cancelar los listener, cerrar el dialog y
        //  volver a poner el listener de Usuarios/Jugadores
        jugarOnline.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.d(Constantes.TAG, "Cancelado Dialog");

                // Quitar el listener
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
        final ProgressBar progressBar = jugarOnline.findViewById(R.id.progressBar2);
        final ImageView avatarRival = jugarOnline.findViewById(R.id.rivalImageIV);

        jugadorReady.setText(jugador.getNickname());
        mensajeEstado.setText("Buscando Rival...");


        // Hacemos una lista con las salas disponibles en Firebase
        partidasListener = new ValueEventListener() {
            List<Partida> salasDisponibles = new ArrayList<>();

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // Actualizamos los datos de las salas
                // Tenemos una lista con todas las salas
                // y otra con las salas que tienen sitio disponible
               // Log.d(Constantes.TAG, "Buscando sala");

                salasDisponibles.removeAll(salasDisponibles);
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Partida salaTMP = snapshot.getValue(Partida.class);
                    if (salaTMP.getJugador1ID().equals("0") || salaTMP.getJugador2ID().equals("0")) {
                        salasDisponibles.add(salaTMP);
                    } else {
                        // si ya tenemos la sala seleccionada, actualizamos los datos
                        // con los cambios que ha habido en Firebase
                        if (salaSeleccionada != null && salaTMP.getNumeroSala() == salaSeleccionada.getNumeroSala()) {
                            salaSeleccionada = salaTMP;
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
                            salaSeleccionada.setPartidaID(salaSeleccionada.getPartidaID());
                        } else if (salaSeleccionada.getJugador2ID().equals("0")) {
                            // Si el hueco 1 está ocupado, usamos el hueco 2 que está vacío
                            Log.d(Constantes.TAG, "Encontrado hueco 2");
                            jugador.setNumeroJugador(2);
                            jugador.setPartida(salaSeleccionada.getPartidaID());
                            salaSeleccionada.setJugador2ID(jugador.getJugadorId());
                            salaSeleccionada.setPartidaID(salaSeleccionada.getPartidaID());
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
                            Utilidades.descargarImagenFirebase(salaSeleccionada.getJugador2ID(), avatarRival);
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
                                    Intent jugar = new Intent(jugarOnline.getContext(), JuegoActivity.class);
                                    jugar.putExtra("PARTIDA", salaSeleccionada.getPartidaID());
                                    jugar.putExtra(Constantes.RIVALID, salaSeleccionada.getJugador2ID());
                                    startActivity(jugar);
                                    jugarOnline.dismiss();
                                    // Eliminamos el listener

                                    finish();
                                }
                            }
                        }

                    } else {
                        // si estamos en el hueco 2 hacemos lo mismo que antes, pero para este hueco
                        Log.d(Constantes.TAG, "Estamos en el hueco 2 de la " + salaSeleccionada.getPartidaID());
                        if (!salaSeleccionada.getJugador1ID().equals("0")) {
                            // Hay otro jugador que ha seleccionado esta sala
                            Log.d(Constantes.TAG, "Hay otro jugador en el hueco 1");
                            mensajeEstado.setText("Encontrado Rival, esperando que esté preparado");
                            Utilidades.descargarImagenFirebase(salaSeleccionada.getJugador1ID(), avatarRival);
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
                                    Intent jugar = new Intent(jugarOnline.getContext(), JuegoActivity.class);
                                    jugar.putExtra("PARTIDA", salaSeleccionada.getPartidaID());
                                    jugar.putExtra(Constantes.RIVALID, salaSeleccionada.getJugador1ID());
                                    startActivity(jugar);
                                    jugarOnline.dismiss();
                                    // Eliminamos el listener

                                    finish();

                                }
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
        super.onStop();

        /*
        if (salaSeleccionada.getTurno() == 0){
            borrarJugadorSalaFirebase();
        }

        */
    }

    public void jugar(View view) {


    }
/*
        salaSeleccionada = new Partida();
        // Los 2 estamos listos. Lanzar Intent de juego
        Intent jugar = new Intent(this, JuegoActivity.class);
        jugar.putExtra(Constantes.PARTIDA, "Sala 0");
        jugar.putExtra(Constantes.RIVALID, "DrKCdhn2a1Z4LAgX1DNeZks7F2u1");
        startActivity(jugar);
        finish();

    }


    */
        //  Utilidades.descargarImagenFirebase(getApplicationContext(), jugador.getJugadorId(), avatarJugador);

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
            jugador.setPartida("0");
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

        // Aprovechamos para guardar en Firebase el Nickname
        if (nickET.getText() != null) {
            jugador.setNickname(nickET.getText().toString());
            userRef.setValue(jugador);
        }


    }



    public void crearSalas(View view) {
        // Crear Salas en Firebase
        for (int n = 0; n < 10; n++) {
            mDatabase.child("PARTIDAS").child("Sala " + n).setValue(new Partida("Sala " + n, n , "0", "0", new Tablero()));
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ((grantResults[0]== PackageManager.PERMISSION_GRANTED)
                && (grantResults[1]== PackageManager.PERMISSION_GRANTED))
        {
            Log.d("MIAPP", "ME ha concecido los permisos");
        } else {
            Log.d("MIAPP", "NO ME ha concecido los permisos");
            Toast.makeText(this,"Hace falta que actives los permisos para Personalizar tu Avatar ...", Toast.LENGTH_SHORT).show();
            permisosOK = false;
        }
    }

    public void tomarFoto() {

        Log.d("MIAPP", "Quiere hacer una foto");
        Intent intent_foto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        this.photo_uri = Utilidades.crearFicheroImagen ();
        intent_foto.putExtra(MediaStore.EXTRA_OUTPUT, this.photo_uri);
        Utilidades.desactivarModoEstricto();
        startActivityForResult(intent_foto, Constantes.CODIGO_PETICION_HACER_FOTO);

    }

    public void seleccionarFoto() {
        Log.d("MIAPP", "Quiere seleccionar una foto");
        Intent intent_pide_foto = new Intent();
        //intent_pide_foto.setAction(Intent.ACTION_PICK);//seteo la acción para galeria
        intent_pide_foto.setAction(Intent.ACTION_GET_CONTENT);//seteo la acción
        intent_pide_foto.setType("image/*");//tipo mime

        startActivityForResult(intent_pide_foto, Constantes.CODIGO_PETICION_SELECCIONAR_FOTO);

    }

    private void setearImagenDesdeArchivo (int resultado, Intent data)
    {
        switch (resultado){
            case RESULT_OK:
                Log.d("MIAPP", "La foto ha sido seleccionada");

                this.photo_uri = data.getData();//obtenemos la uri de la foto seleccionada

                InputStream imageStream = null;
                try {
                    imageStream = getContentResolver().openInputStream(this.photo_uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                selectedImage = Utilidades.getResizedBitmap(selectedImage, 128);// 400 is for example, replace with desired size

                this.avatarJugador.setImageBitmap(selectedImage);

                Drawable d = new BitmapDrawable(getResources(), selectedImage);
                Utilidades.subirImagenFirebase(jugador.getJugadorId(), d);

                //    this.avatarJugador.setImageURI(photo_uri);
              //  this.avatarJugador.setScaleType(ImageView.ScaleType.FIT_XY);

                break;
            case RESULT_CANCELED:
                Log.d("MIAPP", "La foto NO ha sido seleccionada canceló");
                break;
        }
    }

    private void setearImagenDesdeCamara (int resultado, Intent intent)
    {
        switch (resultado)
        {
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

                Drawable d = new BitmapDrawable(getResources(), selectedImage);
                Utilidades.subirImagenFirebase(jugador.getJugadorId(), d);


                //this.avatarJugador.setImageURI(this.photo_uri);
                //this.avatarJugador.setScaleType(ImageView.ScaleType.FIT_XY);
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
        if (requestCode==Constantes.CODIGO_PETICION_SELECCIONAR_FOTO)
        {
            setearImagenDesdeArchivo (resultCode, data);
        } else if (requestCode == Constantes.CODIGO_PETICION_HACER_FOTO)
        {
            setearImagenDesdeCamara (resultCode, data);
        }
    }
}
