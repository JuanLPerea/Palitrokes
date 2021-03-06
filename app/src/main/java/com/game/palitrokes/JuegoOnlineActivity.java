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
import android.widget.ImageButton;
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
import com.game.palitrokes.Utilidades.SharedPrefs;
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
    private String salaJuego;
    private String rivalID_STR;
    private ImageView avatarJ1, avatarJ2;
    private TextView nickJ1, nickJ2, winsJ1, winsJ2, tiempoJ1, tiempoJ2;
    private ImageButton okJ1, okJ2;
    private int[] colores;
    private CountDownTimer cronometro1;
    private CountDownTimer cronometro2;
    private boolean finTiempo;
    private boolean abandono;
    private Sonidos sonidos;
    private int ultimoTurno = 0;

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

        avatarJ2.setBackground(null);

        // Sonidos
        Sonidos.getInstance(getApplicationContext()).play(Sonidos.Efectos.PLING);

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
        abandono = false;

        // Cronometro para el jugador 1
        cronometro1 = new CountDownTimer(Constantes.TIEMPOTURNO, Constantes.TIEMPOACTUALIZACRONO) {
            @Override
            public void onTick(long millisUntilFinished) {
                tiempoJ1.setText(millisUntilFinished / 1000 + "");
            }

            @Override
            public void onFinish() {
                finTiempo = true;
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
                finTurno();
            }
        };

        // Inicializamos el array de colores aleatorios para el fondo de la pantalla
        colores = new int[6];
        Random rnd = new Random();
        for (int n = 0; n < colores.length; n++) {
            colores[n] = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        }

        // creamos objetos
        rival = new Jugador();
        jugador = new Jugador();
        partida = new Partida();
        partida.setTurno(1);

        // referencias Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        Log.d(Constantes.TAG, "Usuario: " + user);

        // Recuperamos datos Intent
        Intent intent = getIntent();
        salaJuego = intent.getStringExtra(Constantes.PARTIDA);
        rivalID_STR = intent.getStringExtra(Constantes.RIVALIDONLINE);
        rival.setJugadorId(rivalID_STR);

        // El jugador lo recuperamos del Shared Preferences
        jugador = SharedPrefs.getJugadorPrefs(getApplicationContext());

        Log.d(Constantes.TAG, "Intent Jugador ID " + jugador.getJugadorId());
        actualizarVistaJugador(jugador);

        // Referencia al jugador en Firebase
        jugadorRef = mDatabase.child("USUARIOS").child(jugador.getJugadorId());


        // Recuperar datos del rival
        rivalRef = mDatabase.child("USUARIOS").child(rivalID_STR);
        rivalListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Jugador rival = dataSnapshot.getValue(Jugador.class);

                corregirErrorNumRival();

                actualizarVistaJugador(rival);

                if (rival.getNumeroJugador() == 1) {
                    okJ1.setVisibility(View.INVISIBLE);
                } else {
                    okJ2.setVisibility(View.INVISIBLE);
                }
                Log.d(Constantes.TAG, "Rival: " + rival.getNickname() + " " + rival.getJugadorId());

                pausa(500);

                jugar();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(Constantes.TAG, "No se ha podido recuperar el rival de la BBDD " + databaseError);
            }
        };
        rivalRef.addListenerForSingleValueEvent(rivalListener);

    }

    private void corregirErrorNumRival() {
        // Por alguna extraña razón no recupera bien esto de Firebase
        // Así que lo seteamos en función del número del jugador
        if (jugador.getNumeroJugador() == 1) {
            rival.setNumeroJugador(2);
        } else {
            rival.setNumeroJugador(1);
        }
    }

    private void actualizarVistaJugador(Jugador jugadorView) {
        if (jugadorView != null) {
            switch (jugadorView.getNumeroJugador()) {
                case 1:
                    UtilsFirebase.descargarImagenFirebaseView(getApplicationContext(), jugadorView.getJugadorId(), avatarJ1);
                    nickJ1.setText(jugadorView.getNickname());
                    winsJ1.setText((getString(R.string.victorias2)) + jugadorView.getVictorias());
                    break;
                case 2:
                    UtilsFirebase.descargarImagenFirebaseView(getApplicationContext(), jugadorView.getJugadorId(), avatarJ2);
                    nickJ2.setText(jugadorView.getNickname());
                    winsJ2.setText((getString(R.string.victorias2)) + jugadorView.getVictorias() + "");
                    break;
                default:
                    Log.d(Constantes.TAG, "Actualizar vista jugador es 0");
                    // Actualizar rival, da un error por lo que hacemos esto
                    if (jugador.getNumeroJugador() == 1) {
                        UtilsFirebase.descargarImagenFirebaseView(getApplicationContext(), jugadorView.getJugadorId(), avatarJ2);
                        nickJ2.setText(jugadorView.getNickname());
                        winsJ2.setText((getString(R.string.victorias2)) + jugadorView.getVictorias() + "");
                    } else {
                        UtilsFirebase.descargarImagenFirebaseView(getApplicationContext(), jugadorView.getJugadorId(), avatarJ1);
                        nickJ1.setText(jugadorView.getNickname());
                        winsJ1.setText((getString(R.string.victorias2)) + jugadorView.getVictorias());
                    }
                    break;
            }

        } else {
            Log.d(Constantes.TAG, "Actualizar vista jugador null");
        }

    }

    private void jugar() {
        // Recuperar la partida
        // Establecemos un listener para la sala de juego
        // La pantalla se actualizará de acuerdo con los datos recuperados de Firebase
        // Dependiendo del actualizarViewsCambioTurno, podremos hacer cambios en la pantalla o no y
        // gestionaremos el juego de acuerdo con esto.
        // Cada vez que hay un cambio en la BBDD se lanza este evento...
        //
        //  cronometro1.start();
        // partida = null;
        ultimoTurno = 0;

        salaRef = mDatabase.child("PARTIDAS").child(salaJuego);
        salaListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Partida partidaTMP = dataSnapshot.getValue(Partida.class);
                if (partidaTMP != null) {
                    partida.setGanador(partidaTMP.getGanador());
                    partida.setTurno(partidaTMP.getTurno());
                    partida.setJugador1ID(partidaTMP.getJugador1ID());
                    partida.setJugador2ID(partidaTMP.getJugador2ID());
                    partida.setNumeroSala(partidaTMP.getNumeroSala());
                    partida.setPartidaID(partidaTMP.getPartidaID());
                    partida.setTablero(partidaTMP.getTablero());
                    // Los palitrokes se actualizan en la pantalla cuando hay algún
                    // cambio en la BB.DD. y por lo tanto se lanza este Listener
                    Log.d(Constantes.TAG, "DataChange Num Jugador: " + jugador.getNumeroJugador());
                    Log.d(Constantes.TAG, "DataChange Num Rival: " + rival.getNumeroJugador());

                    visualizarTablero(partida.getTablero());
                    // Sonido cuando el rival selecciona un palo ...
                    if (partida.getTurno() != jugador.getNumeroJugador()) {
                        Sonidos.getInstance(getApplicationContext()).play(Sonidos.Efectos.TICK);
                    }
                    // Si no hay ganador, mirar de quien es el turno
                    if (partida.getGanador() != 0) {
                        // Aquí detectamos si hay ganador. La partida termina
                        finJuego();
                    }

                    if (ultimoTurno != partida.getTurno()) {
                        actualizarViewsCambioTurno();
                        ultimoTurno = partida.getTurno();
                        Sonidos.getInstance(getApplicationContext()).play(Sonidos.Efectos.PLING);
                    }
                } else {
                    // No hay sala, el otro jugador ha abandonado o ha habido un error
                    Log.d(Constantes.TAG, "No hay sala");

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
                Sonidos.getInstance(getApplicationContext()).play(Sonidos.Efectos.TICK);
            }

            Log.d(Constantes.TAG, "SET SELECCIONAR PALO");
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
                    okJ1.setImageResource(R.drawable.yapeque);
                    okJ1.setBackgroundResource(0);
                    okJ1.setVisibility(View.VISIBLE);            //Botón del jugador 1 visible, puede jugar
                    okJ2.setVisibility(View.INVISIBLE);          //Botón del jugador 2 invisible
                } else {
                    // si somos el jugador 2
                    okJ1.setVisibility(View.VISIBLE);            //Botón del jugador 1 invisible (No hay botón en ningun lado porque el turno es del otro)
                    okJ1.setImageResource(R.drawable.pensando);      // Cambiamos la imagen del botón para que vea que está jugando el otro
                    okJ1.setBackgroundResource(0);
                    okJ2.setVisibility(View.INVISIBLE);          //Botón del jugador 2 invisible
                }
                // El tiempo estará visible en el jugador 1 que es el que tiene el crono
                tiempoJ1.setVisibility(View.VISIBLE);        //Tiempo del jugador 1 visible
                tiempoJ2.setVisibility(View.INVISIBLE);      //Tiempo del jugador 2 invisible
                // Seamos el jugador 1 o el 2 el crono lo tiene el jugador 1, porque es su turno
                //  cronometro2.cancel();
                cronometro1.start();
                break;
            case 2:
                // Es el turno del jugador 2
                if (jugador.getNumeroJugador() == 1) {
                    // si somos el jugador 1
                    okJ2.setImageResource(R.drawable.pensando);
                    okJ2.setBackgroundResource(0);
                    okJ1.setVisibility(View.INVISIBLE);            //Botón del jugador 1 invisible (No hay botón en ningun lado porque el turno es del otro)
                    okJ2.setVisibility(View.VISIBLE);          //Botón del jugador 2 invisible (No hay botón en ningun lado porque el turno es del otro)
                } else {
                    // si somos el jugador 2
                    okJ2.setImageResource(R.drawable.yapeque);
                    okJ2.setBackgroundResource(0);
                    okJ1.setVisibility(View.INVISIBLE);            //Botón del jugador 1 invisible
                    okJ2.setVisibility(View.VISIBLE);          //Botón del jugador 2 activado, podemos jugar
                }
                // El tiempo estará visible en el jugador 1 que es el que tiene el crono
                tiempoJ1.setVisibility(View.INVISIBLE);        //Tiempo del jugador 1 INvisible
                tiempoJ2.setVisibility(View.VISIBLE);      //Tiempo del jugador 2 invisible
                // Seamos el jugador 1 o el 2 el crono lo tiene el jugador 1, porque es su turno
                //  cronometro1.cancel();
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

                // Reseteamos los cronos
                cronometro1.cancel();
                cronometro2.cancel();

                // Eliminamos los palos seleccionados del Tablero
                partida.getTablero().eliminarSeleccionados();
                Log.d(Constantes.TAG, "SET OK JUGADA");
                salaRef.setValue(partida);

                // comprobamos, si solo queda uno, hemos ganado!!
                if (partida.getTablero().palosTotales() == 1) {

                    //    Toast.makeText(this, "¡¡Has Ganado!!", Toast.LENGTH_LONG).show();
                    partida.setGanador(jugador.getNumeroJugador());
                    Log.d(Constantes.TAG, "SET HAS GANADO");
                    salaRef.setValue(partida);

                } else {
                    // Si aún quedan palos Cambiamos el turno y se lo pasamos al rival
                    Log.d(Constantes.TAG, "Fin de turno botón OK");
                    finTurno();
                }


            } else {
                Toast.makeText(this, (getString(R.string.selecciona)), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, (getString(R.string.unpalo)), Toast.LENGTH_LONG).show();
        }
    }


    // Aactualizar Firebase con los cambios que haya
    // En la pantalla en ese momento
    // Si no se ha hecho ningún movimiento, el jugador pierde.
    private void finTurno() {
        // Si llegamos aquí tenemos que saber si ha sido porque ha terminado
        // Nuestro crono o ha sido el del rival (Es nuestro turno o no)
        // También llegamos si hemos pulsado el boton de jugadaOK y pasamos el turno al contrario

        // Paramos el crono
        cronometro1.cancel();
        cronometro2.cancel();

        // Si no hay ningún palo seleccionado esque el jugador pasa bastante de jugar y se ha acabado el tiempo sin hacer nada
        Log.d(Constantes.TAG, "Fin del turno " + partida.getTurno() + " Fin tiempo? " + finTiempo);
        if (finTiempo) {
            if (partida.getTurno() == jugador.getNumeroJugador()) {
                if (partida.getTablero().palosSeleccionadosTotal() == 0) {
                    // Si es el jugador el que no ha hecho nada, pierde
                    corregirErrorNumRival();
                    partida.setGanador(rival.getNumeroJugador());
                    // Notificamos el ganador
                    Log.d(Constantes.TAG, "SET NO HA SELECCIONADO NADA" + rival.getNumeroJugador());
                } else {
                    okJugada(null);
                }

                actualizarViewsCambioTurno();
                // Cambiamos el turno en Firebase
                Log.d(Constantes.TAG, "SET CAMBIO DE TURNO 1");
                salaRef.setValue(partida);

            }
        } else {
            // Cambiamos el turno
            partida.turnoToggle();
            actualizarViewsCambioTurno();
            // Cambiamos el turno en Firebase
            Log.d(Constantes.TAG, "SET CAMBIO DE TURNO 2");
            salaRef.setValue(partida);
        }
    }


    @Override
    protected void onStop() {
        Log.d(Constantes.TAG, "On Stop Juego ONline - Borrar Sala");
        limpiarSala();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        // si salimos, abandonamos la partida
        abandono = true;
        partida.setGanador(rival.getNumeroJugador());
        Log.d(Constantes.TAG, "SET ON BACK PRESSED");
        salaRef.setValue(partida);
        finJuego();
    }

    private void finJuego() {
        // Quitar todos los listeners
        //(Importante para no volverse loco con callbacks que no se sabe de donde vienen)
        salaRef.removeEventListener(salaListener);
        rivalRef.removeEventListener(rivalListener);
        cronometro1.cancel();
        cronometro2.cancel();


        Log.d(Constantes.TAG, "Ganador: " + partida.getGanador());

        if (partida == null) {
            // No hay sala (El otro jugador ha abandonado)
            Toast.makeText(getApplicationContext(), "El otro jugador ha abandonado. ¡Has ganado!", Toast.LENGTH_LONG).show();
            jugador.setVictorias(10);
            Sonidos.getInstance(getApplicationContext()).play(Sonidos.Efectos.GANAR);
            SharedPrefs.saveJugadorPrefs(getApplicationContext(), jugador);
        } else {
            String resultado = "";

            if (finTiempo || abandono) {
                resultado = getString(R.string.abandono);
            }
            // Mirar quien es el ganador
            if (partida.getGanador() == jugador.getNumeroJugador()) {
                jugador.setVictorias(10);
                resultado += (getString(R.string.win));
                Sonidos.getInstance(getApplicationContext()).play(Sonidos.Efectos.GANAR);
                SharedPrefs.saveJugadorPrefs(getApplicationContext(), jugador);

            } else {
                resultado += (getString(R.string.perdido));
                Sonidos.getInstance(getApplicationContext()).play(Sonidos.Efectos.PERDER);
            }

            // Mensaje
            Toast.makeText(getApplicationContext(), resultado, Toast.LENGTH_LONG).show();

            // Actualizamos Firebase y limpiamos los datos...
            limpiarSala();
            actualizarJugadores();

        }

        // Salimos de la Activity (Vuelve al Main)
        finish();

    }

    private void limpiarSala() {
        // eliminamos la partida
        pausa(2000);
        salaRef.removeValue();

    }

    private void actualizarJugadores() {
        // Actualizar puntuaciones de los jugadores en Firebase
        //    jugador.setNumeroJugador(0);
        jugador.setPartida("0");
        jugadorRef.setValue(jugador);
        // Guardamos en el Shared Preferences los datos del jugador
        SharedPrefs.saveJugadorPrefs(getApplicationContext(), jugador);
    }

    private int newId() {
        Random r = new Random();
        int resultado = -1;
        do {
            resultado = r.nextInt(Integer.MAX_VALUE);
        } while (findViewById(resultado) != null);
        return resultado;
    }

    @Override
    protected void onPause() {
        // Si bloqueamos es como abandonar la partida
        if (partida.getGanador() == 0) {
            abandono = true;
            partida.setGanador(rival.getNumeroJugador());
            Log.d(Constantes.TAG, "SET ON PAUSE");
            salaRef.setValue(partida);
            finJuego();
        }


        super.onPause();
    }

    private void pausa(int tiempo) {
        // Dejamos una pausa para que se actualice la sala
        try {
            Thread.sleep(tiempo);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
