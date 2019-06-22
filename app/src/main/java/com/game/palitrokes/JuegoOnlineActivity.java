package com.game.palitrokes;

import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.game.palitrokes.Modelos.Jugador;
import com.game.palitrokes.Modelos.Monton;
import com.game.palitrokes.Modelos.Palo;
import com.game.palitrokes.Modelos.Partida;
import com.game.palitrokes.Modelos.Tablero;
import com.game.palitrokes.Utilidades.Constantes;
import com.game.palitrokes.Utilidades.Sonidos;
import com.game.palitrokes.Utilidades.Utilidades;
import com.game.palitrokes.Utilidades.UtilsFirebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class JuegoOnlineActivity extends AppCompatActivity {

    private LinearLayout linearBase;
    private Partida partida;
    private Jugador jugador;
    private Jugador rival;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference jugadorRef;
    private DatabaseReference rivalRef;
    private DatabaseReference salaRef;
    private ValueEventListener salaListener;
    private ValueEventListener rivalListener;
    ValueEventListener jugadorListener;
    private String salaJuego;
    private ImageView avatarJ1, avatarJ2;
    private TextView nickJ1, nickJ2, winsJ1, winsJ2, tiempoJ1, tiempoJ2;
    private Button okJ1, okJ2;
    private int[] colores;
    private CountDownTimer cronometro1;
    private CountDownTimer cronometro2;
    private boolean finTiempo;
    private Sonidos sonidos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_juego);
        getSupportActionBar().hide();

        // recuperamos las Views
        linearBase = findViewById(R.id.tableroLL);
        avatarJ1 = findViewById(R.id.avatarJ1_TV);
        avatarJ2 = findViewById(R.id.avatarJ2_TV);
        nickJ1 = findViewById(R.id.nicknameJ1_TV);
        nickJ2 = findViewById(R.id.nicknameJ2_TV);
        winsJ1 = findViewById(R.id.vitoriasJ1_TV);
        winsJ2 = findViewById(R.id.vitoriasJ2_TV);
        tiempoJ1 = findViewById(R.id.tiempoJ1_TV);
        tiempoJ2 = findViewById(R.id.tiempoJ2_TV);
        okJ1 = findViewById(R.id.ok_J1_BTN);
        okJ2 = findViewById(R.id.ok_J2_BTN);

        // Sonidos
        sonidos = new Sonidos(this);
        sonidos.play(Sonidos.Efectos.ONLINE);

        okJ1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                okJugada(v);
            }
        });

        okJ2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                okJugada(v);
            }
        });

        finTiempo = false;

        // Cronometro para el jugador 1
        cronometro1 = new CountDownTimer(Constantes.TIEMPOTURNO, Constantes.TIEMPOACTUALIZACRONO) {
            @Override
            public void onTick(long millisUntilFinished) {
                tiempoJ1.setText(millisUntilFinished / 1000 + "");
            }

            @Override
            public void onFinish() {
                finTiempo = true;
                cronometro1.cancel();
                finTurno();
            }
        };

        // Cronometro para el jugador 2
        cronometro2 = new CountDownTimer(Constantes.TIEMPOTURNO, Constantes.TIEMPOACTUALIZACRONO) {
            @Override
            public void onTick(long millisUntilFinished) {
                tiempoJ2.setText(millisUntilFinished / 1000 + "");
            }

            @Override
            public void onFinish() {
                finTiempo = true;
                cronometro2.cancel();
                finTurno();
            }
        };

        // Inicializamos el array de colores aleatorios para el fondo de la pantalla
        colores = new int[6];
        Random rnd = new Random();
        for (int n = 0; n < colores.length; n++) {
            colores[n] = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        }

        // creamos un rival vacío
        rival = new Jugador();

        // referencias Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        Log.d(Constantes.TAG, "Usuario: " + user);

        // Recuperamos datos Intent
        Intent intent = getIntent();
        salaJuego = intent.getStringExtra(Constantes.PARTIDA);
        rival.setJugadorId(intent.getStringExtra(Constantes.RIVALID));

        // Recuperar datos del rival
        rivalRef = mDatabase.child("USUARIOS").child(rival.getJugadorId()).getRef();
        rivalListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                rival = dataSnapshot.getValue(Jugador.class);
                actualizarVistaJugador(rival);
                if (rival.getNumeroJugador() == 1) {
                    okJ1.setVisibility(View.INVISIBLE);
                } else {
                    okJ2.setVisibility(View.INVISIBLE);
                }
                Log.d(Constantes.TAG, "Rival: " + rival.getNickname() + " " + rival.getJugadorId());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(Constantes.TAG, "No se ha podido recuperar el rival de la BBDD " + databaseError);
            }
        };
        rivalRef.addListenerForSingleValueEvent(rivalListener);

        // Recuperar datos del jugador
        // Y jugar
        jugadorRef = mDatabase.child("USUARIOS").child(mAuth.getCurrentUser().getUid()).getRef();
        jugadorListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                jugador = dataSnapshot.getValue(Jugador.class);
                Log.d(Constantes.TAG, "Jugador: " + jugador.getNickname() + " " + jugador.getJugadorId());
                actualizarVistaJugador(jugador);
                jugar(salaJuego);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(Constantes.TAG, "No se ha podido recuperar el jugador de la BBDD " + databaseError);
            }
        };
        jugadorRef.addListenerForSingleValueEvent(jugadorListener);

    }

    private void actualizarVistaJugador(Jugador jugadorView) {
        if (jugadorView.getNumeroJugador() == 1) {
            avatarJ1.setImageBitmap(Utilidades.recuperarImagenMemoriaInterna(getApplicationContext(), Constantes.ARCHIVO_IMAGEN_JUGADOR));
            nickJ1.setText(jugadorView.getNickname());
            winsJ1.setText("Victorias: " + jugadorView.getVictorias());
        } else {
            avatarJ2.setImageBitmap(Utilidades.recuperarImagenMemoriaInterna(getApplicationContext(), Constantes.ARCHIVO_IMAGEN_JUGADOR));
            nickJ2.setText(jugadorView.getNickname());
            winsJ2.setText("Victorias: " + jugadorView.getVictorias() + "");
        }
    }

    private void jugar(String salaJuego) {
        // Recuperar la partida
        // Establecemos un listener para la sala de juego
        // La pantalla se actualizará de acuerdo con los datos recuperados de Firebase
        // Dependiendo del actualizarViewsCambioTurno, podremos hacer cambios en la pantalla o no y
        // gestionaremos el juego de acuerdo con esto.
        // Cada vez que hay un cambio en la BBDD se lanza este evento...
        //

        salaRef = mDatabase.child("PARTIDAS").child(salaJuego).getRef();

        salaListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                partida = dataSnapshot.getValue(Partida.class);
                // Los palitrokes se actualizan en la pantalla cuando hay algún
                // cambio en la BB.DD. y por lo tanto se lanza este Listener
                visualizarTablero(partida.getTablero());
                // Si no hay ganador, mirar de quien es el turno
                if (partida.getGanador() == 0) {
                    // Actualizar los botones y crono en la pantalla
                    // dependiendo si es nuestro turno o el del rival
                    actualizarViewsCambioTurno();
                } else {
                    // Aquí detectamos si hay ganador. La partida termina
                    finJuego();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(Constantes.TAG, "No se ha podido recuperar la partida de la BBDD " + databaseError);
            }
        };
        salaRef.addValueEventListener(salaListener);


    }

    private void visualizarTablero(Tablero tablero) {

        linearBase.removeAllViews();

        int pesoMonton = 100 / tablero.getNumMontones();

        // Calcular el alto de los palos en función del tamaño de la pantalla
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        //int width = metrics.widthPixels; // ancho absoluto en pixels
        int height = metrics.heightPixels; // alto absoluto en pixels


        // Miramos la cantidad de palos mas alta de los montones
        int maxpalos = partida.getTablero().maxPalosMontones();

        // Hacemos el alto del ImageView se reparta entre el espacio disponible
        int alturaPalo = (height / 2) / (maxpalos + 1);


        for (final Monton montonTMP : tablero.getMontones()) {

            LinearLayout montonLL = new LinearLayout(this);
            montonLL.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, pesoMonton));
            montonLL.setOrientation(LinearLayout.VERTICAL);
            montonLL.setId(newId());
            montonLL.setPadding(10, 0, 10, 0);
            montonLL.setBackgroundColor(colores[montonTMP.getNumeroMonton()]);


//            int numeroPalos = montonTMP.getPalos().size();

            //     Log.d(Constantes.TAG, "palos monton " + numeroPalos + " altura " + alturaPalo);

            for (final Palo paloTmp : montonTMP.getPalos()) {
                final ImageView newImageView = new ImageView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, alturaPalo);
                params.setMargins(5, 0, 5, 5);
                newImageView.setLayoutParams(params);
                if (paloTmp.isSeleccionado()) {
                    newImageView.setAlpha(0.5F);
                } else {
                    newImageView.setAlpha(1F);
                }
                newImageView.setImageResource(R.drawable.palo);
                newImageView.setId(newId());
                newImageView.setTag(montonTMP.getNumeroMonton() + "#" + paloTmp.getNumeroPalo());
                newImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                newImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        seleccionarPalo(newImageView);
                    }
                });
                montonLL.addView(newImageView);
            }

            linearBase.addView(montonLL);
        }

    }

    private void seleccionarPalo(ImageView imageView) {
        // Log.d(Constantes.TAG, "Seleccionado palo " + imageView.getId());

        // Si es nuestro actualizarViewsCambioTurno podemos seleccionar cosas en la pantalla...
        if (partida.getTurno() == jugador.getNumeroJugador()) {

            int montonTocado = Integer.parseInt(imageView.getTag().toString().split("#")[0]);
            int paloTocado = Integer.parseInt(imageView.getTag().toString().split("#")[1]);

            //   Log.d(Constantes.TAG, "Palo tocado: " + montonTocado + "-" + paloTocado);

            // Si el palo que hemos tocado está en el mismo montón o no hay ningún montón seleccionado (-1)
            // Cambiamos el estado del palo entre seleccionado o no seleccionado
            if (partida.getTablero().getMontonSeleccionado() == partida.getTablero().getMontones().get(montonTocado).getNumeroMonton() ||
                    partida.getTablero().getMontonSeleccionado() == -1) {
                if (partida.getTablero().getMontones().get(montonTocado).getPalos().get(paloTocado).isSeleccionado()) {
                    partida.getTablero().getMontones().get(montonTocado).getPalos().get(paloTocado).setSeleccionado(false);
                    if (partida.getTablero().getMontones().get(montonTocado).getPalosseleccionados() == 0) {
                        partida.getTablero().setMontonSeleccionado(-1);
                    }

                } else {
                    partida.getTablero().getMontones().get(montonTocado).getPalos().get(paloTocado).setSeleccionado(true);
                    partida.getTablero().getMontones().get(montonTocado).setNumeroMonton(montonTocado);
                    partida.getTablero().setMontonSeleccionado(montonTocado);
                }
                sonidos.play(Sonidos.Efectos.TICK);
            }

            salaRef.setValue(partida);
        }

    }

    private void actualizarViewsCambioTurno() {
        // Si somos el jugador 1 (Estamos arriba)
        // El jugador 2 está abajo en la pantalla
        //
        switch (partida.getTurno()) {
            // Es el turno del jugador 1
            case 1:
                if (jugador.getNumeroJugador() == 1) {
                    // si somos el jugador 1
                    okJ1.setVisibility(View.VISIBLE);            //Botón del jugador 1 visible, puede jugar
                    okJ2.setVisibility(View.INVISIBLE);          //Botón del jugador 2 invisible
                } else {
                    // si somos el jugador 2
                    okJ1.setVisibility(View.INVISIBLE);            //Botón del jugador 1 invisible (No hay botón en ningun lado porque el turno es del otro)
                    okJ2.setVisibility(View.INVISIBLE);          //Botón del jugador 2 invisible (No hay botón en ningun lado porque el turno es del otro)
                }
                // El tiempo estará visible en el jugador 1 que es el que tiene el crono
                tiempoJ1.setVisibility(View.VISIBLE);        //Tiempo del jugador 1 visible
                tiempoJ2.setVisibility(View.INVISIBLE);      //Tiempo del jugador 2 invisible
                // Seamos el jugador 1 o el 2 el crono lo tiene el jugador 1, porque es su turno
                cronometro1.start();
                break;
            case 2:
                // Es el turno del jugador 2
                if (jugador.getNumeroJugador() == 1) {
                    // si somos el jugador 1
                    okJ1.setVisibility(View.INVISIBLE);            //Botón del jugador 1 invisible (No hay botón en ningun lado porque el turno es del otro)
                    okJ2.setVisibility(View.INVISIBLE);          //Botón del jugador 2 invisible (No hay botón en ningun lado porque el turno es del otro)
                } else {
                    // si somos el jugador 2
                    okJ1.setVisibility(View.INVISIBLE);            //Botón del jugador 1 invisible
                    okJ2.setVisibility(View.VISIBLE);          //Botón del jugador 2 activado, podemos jugar
                }
                // El tiempo estará visible en el jugador 1 que es el que tiene el crono
                tiempoJ1.setVisibility(View.INVISIBLE);        //Tiempo del jugador 1 INvisible
                tiempoJ2.setVisibility(View.VISIBLE);      //Tiempo del jugador 2 invisible
                // Seamos el jugador 1 o el 2 el crono lo tiene el jugador 1, porque es su turno
                cronometro2.start();
                break;
        }

    }

    // si es nuestro turno y pulsamos el botón es que estamos contentos con
    // nuestra jugada. Cambiamos el turno o detectamos si es el último palo
    // con lo cual hemos perdido...
    public void okJugada(View view) {
        // No puede seleccionar todos los palos si solo queda 1 monton
        // Tiene que haber seleccionado al menos 1 palo
        if (partida.getTablero().palosSeleccionadosTotal() != partida.getTablero().palosTotales()) {
            if (partida.getTablero().palosSeleccionadosTotal() > 0) {
                finTiempo = false;

                // Eliminamos los palos seleccionados del Tablero
                partida.getTablero().eliminarSeleccionados();
                salaRef.setValue(partida);
                Log.d(Constantes.TAG, "Aceptar jugada");

                // comprobamos, si solo queda uno, hemos ganado!!
                if (partida.getTablero().palosTotales() == 1) {

                    //    Toast.makeText(this, "¡¡Has Ganado!!", Toast.LENGTH_LONG).show();
                    partida.setGanador(jugador.getNumeroJugador());
                    salaRef.setValue(partida);

                } else {
                    // Si aún quedan palos Cambiamos el turno y se lo pasamos al rival
                    Log.d(Constantes.TAG, "Fin de turno botón OK");
                    finTurno();
                }


            } else {
                Toast.makeText(this, "Selecciona al menos 1 palo", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Debes dejar al menos 1 palo y ganas!!!", Toast.LENGTH_LONG).show();
        }
    }


    // Aactualizar Firebase con los cambios que haya
    // En la pantalla en ese momento
    // Si no se ha hecho ningún movimiento, el jugador pierde.
    private void finTurno() {
        // Si llegamos aquí tenemos que saber si ha sido porque ha terminado
        // Nuestro crono o ha sido el del rival (Es nuestro turno o no)
        // También llegamos si hemos pulsado el boton de jugadaOK y pasamos el turno al contrario
        //
        // Cuando cambie el campo 'TURNO' en Firebase, se lanzará un evento en nuestra Aplicación
        // y iniciará el método 'actualizarViewsCambioTurno' de la APP

        // Paramos el crono
        cronometro1.cancel();
        cronometro2.cancel();

        // Si no hay ningún palo seleccionado esque el jugador pasa bastante de jugar y se ha acabado el tiempo sin hacer nada
        Log.d(Constantes.TAG, "Fin del turno " + partida.getTurno() + " Fin tiempo? " + finTiempo);
        if (finTiempo && partida.getTurno() == jugador.getNumeroJugador()) {
            // Si es el jugador el que no ha hecho nada, pierde
            // Toast.makeText(this, "Lo siento, si no haces ninguna jugada, pierdes" , Toast.LENGTH_LONG).show();
            partida.setGanador(rival.getNumeroJugador());
            // Notificamos el ganador
            salaRef.setValue(partida);
        }

        // Cambiamos el turno
        partida.turnoToggle();

        // Cambiamos el turno en Firebase
        salaRef.setValue(partida);

    }


    @Override
    protected void onStop() {
        super.onStop();
        limpiarSala();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        Log.d(Constantes.TAG, keyCode + "");

        if (keyCode == KeyEvent.KEYCODE_HOME) {
            Log.d(Constantes.TAG, "Home button pressed!");
        }

        return super.onKeyDown(keyCode, event);

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        finTiempo = true;
        partida.setGanador(rival.getNumeroJugador());
        salaRef.setValue(partida);
        finJuego();
    }

    private void finJuego() {

        // Quitar todos los listeners
        //(Importante para no volverse loco con callbacks que no se sabe de donde vienen)
        salaRef.removeEventListener(salaListener);
        rivalRef.removeEventListener(rivalListener);
        jugadorRef.removeEventListener(jugadorListener);
        cronometro1.cancel();
        cronometro2.cancel();

        Intent volverIntent = new Intent(this, MainActivity.class);
        volverIntent.putExtra("SALA_ANTERIOR", partida.getPartidaID());

        String resultado = "";

        if (finTiempo) {
            resultado = "Ha habido abandono ... \n";
        }

        Log.d(Constantes.TAG, "Ganador: " + partida.getGanador());
        if (partida.getGanador() == jugador.getNumeroJugador()) {
            jugador.setVictorias(1);
            resultado += "Has Ganado ¡Enhorabuena!";
            sonidos.play(Sonidos.Efectos.GANAR);
        } else {
            jugador.setDerrotas(1);
            resultado += "Lo siento ¡has perdido!";
            sonidos.play(Sonidos.Efectos.PERDER);
        }

        Toast.makeText(this, resultado, Toast.LENGTH_LONG).show();

        // Actualizamos Firebase y limpiamos los datos...
        limpiarSala();
        actualizarJugadores();


        Log.d(Constantes.TAG, "Esperamos 1 segundo");
        // Dejamos una pausa para que se actualice la sala
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finish();
        // Lanzamos el intent del MainActivity
        startActivity(volverIntent);

    }

    private void limpiarSala() {
        // Dejamos la sala vacía para poder reusarla
        partida.setJugador1ID("0");
        partida.setJugador2ID("0");
        partida.setGanador(0);
        partida.setJugador2Ready(false);
        partida.setJugador1Ready(false);
        partida.setJugando(false);
        partida.setTablero(new Tablero(0));
        partida.setTurno(1);
        salaRef.setValue(partida);
    }

    private void actualizarJugadores() {
        // Actualizar puntuaciones de los jugadores en Firebase
        jugador.setNumeroJugador(0);
        jugador.setPartida("0");
        jugador.setOnline(true);
        jugadorRef.setValue(jugador);
    }

    private int newId() {
        Random r = new Random();
        int resultado = -1;
        do {
            resultado = r.nextInt(Integer.MAX_VALUE);
        } while (findViewById(resultado) != null);
        return resultado;
    }


}
