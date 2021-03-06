package com.game.palitrokes;

import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.game.palitrokes.Modelos.JugadaCom;
import com.game.palitrokes.Modelos.Jugador;
import com.game.palitrokes.Modelos.Monton;
import com.game.palitrokes.Modelos.Palo;
import com.game.palitrokes.Modelos.Partida;
import com.game.palitrokes.Modelos.Records;
import com.game.palitrokes.Modelos.Tablero;
import com.game.palitrokes.Utilidades.Constantes;
import com.game.palitrokes.Utilidades.SharedPrefs;
import com.game.palitrokes.Utilidades.Sonidos;
import com.game.palitrokes.Utilidades.Utilidades;
import com.game.palitrokes.Utilidades.UtilityNetwork;
import com.game.palitrokes.Utilidades.UtilsFirebase;

import java.util.Random;

public class JuegoVsComActivity extends AppCompatActivity {


    private LinearLayout linearBase;
    private Partida partida;
    private Jugador jugador;
    private ImageView avatarJ1, avatarJ2;
    private TextView nickJ1, nickJ2, winsJ1, winsJ2, tiempoJ1, tiempoJ2, level;
    private ImageButton okJ1, okJ2;
    private int[] colores;
    private CountDownTimer cronometro1;
    private CountDownTimer cronometro2;
    CountDownTimer jugadaComTimer;
    private boolean finTiempo;
    private int palosQuitados;
    private boolean abandono;


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
        level = findViewById(R.id.levelTV);

        okJ1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                okJugada(null);
            }
        });

        okJ2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                okJugada(null);
            }
        });

        avatarJ2.setImageResource(R.drawable.pic34);

        finTiempo = false;

        // Recuperamos los datos del jugador de Shared Preferences
        // Seteamos el jugador
        jugador = new Jugador();
        jugador = SharedPrefs.getJugadorPrefs(getApplicationContext());
        jugador.setNumeroJugador(1);
        actualizarVistaJugador();

        nickJ2.setText(R.string.nombreprota);
        winsJ2.setText(R.string.victorias8);


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
                    okJugada(null);
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
                    okJugada(null);
            }
        };


        // Creamos una nueva partida
        // El turno será aleatorio para que no haya ventajas
        partida = new Partida();
        // Asegurarnos que en la primera jugada, el jugador tenga las de ganar siempre
        Tablero nuevoTablero;
        do {
            nuevoTablero = new Tablero(partida.getLevel());
        } while (!new JugadaCom().esGanador(nuevoTablero));
        partida.setTablero(nuevoTablero);
        partida.setTurno(1);
        partida.setJugador1ID(jugador.getJugadorId());

        inicializarColores();

        mostrarLevel();


    }

    private void actualizarVistaJugador() {
        avatarJ1.setImageBitmap(Utilidades.recuperarImagenMemoriaInterna(getApplicationContext(), jugador.getJugadorId()));
        winsJ1.setText((getString(R.string.victorias2)) + jugador.getVictorias());
        nickJ1.setText(jugador.getNickname());
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
        Log.d(Constantes.TAG, "Seleccionado palo " + imageView.getId());

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

        }

        visualizarTablero(partida.getTablero());
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


    public void okJugada(View v) {
        // No puede seleccionar todos los palos si solo queda 1 monton
        // Tiene que haber seleccionado al menos 1 palo
        if (partida.getTablero().palosSeleccionadosTotal() != partida.getTablero().palosTotales()) {
            if (partida.getTablero().palosSeleccionadosTotal() > 0) {
                finTiempo = false;
                // Eliminamos los palos seleccionados del Tablero
                partida.getTablero().eliminarSeleccionados();
                Log.d(Constantes.TAG, "Aceptar jugada");
                // comprobamos, si solo queda uno, hemos ganado!!
                if (partida.getTablero().palosTotales() == 1) {
                    //    Toast.makeText(this, "¡¡Has Ganado!!", Toast.LENGTH_LONG).show();
                    avatarJ2.setImageResource(R.drawable.pic109);
                    Log.d(Constantes.TAG, (getString(R.string.ganador)) + partida.getTurno());
                    partida.setGanador(partida.getTurno());
                } else {
                    // Si aún quedan palos Cambiamos el turno y se lo pasamos al rival
                    Log.d(Constantes.TAG, "Fin de turno botón OK");
                }
                finTurno();
            } else {
                Toast.makeText(this, R.string.selecciona, Toast.LENGTH_LONG).show();
                if (finTiempo) {
                    Log.d(Constantes.TAG, "Ha terminado el tiempo sin seleccionar ningun palo");
                    abandono = true;
                    partida.setGanador(2);
                    finTurno();
                }
            }

        } else {
            Toast.makeText(this, (getString(R.string.unpalo)), Toast.LENGTH_LONG).show();
        }
        visualizarTablero(partida.getTablero());
    }


    private void finTurno() {

        Sonidos.getInstance(getApplicationContext()).play(Sonidos.Efectos.PLING);

        if (partida.getGanador() == 0) {

            partida.turnoToggle();
            actualizarViewsCambioTurno();


            if (partida.getTurno() == 2) {
                Log.d(Constantes.TAG, "Turno del ordenador");
                //  Pasar el tablero a la Clase
                //  JugadaCom Nos devuelve un String con la
                //  jugada que va ha hacer el ordenador

                // Hacemos los cambios que sean
                JugadaCom jugadaCom = new JugadaCom();
                hacerJugadaCom(jugadaCom.jugadaCom(partida.getTablero()));

            }

        } else {
            Log.d(Constantes.TAG, "Fin juego");
            finJuego();
        }


    }

    private void hacerJugadaCom(String jugadaCom) {

        cronometro1.cancel();
        finTiempo = false;

        final int montonEnJuego = Integer.parseInt(jugadaCom.split("#")[0]);
        final int palosAQuitar = Integer.parseInt(jugadaCom.split("#")[1]);
        palosQuitados = 0;

        partida.getTablero().setMontonSeleccionado(montonEnJuego);

        pausa(500);

        // Simulamos que se seleccionan los palos y se quitan
        // Haciendo una pausa de medio segundo entre palo y palo


        if (palosAQuitar == 1) {
            // Si solo es un palo lo que va a quitar no hacemos lo del crono
            cambiarImagenPalitrokes();
            partida.getTablero().getMontones().get(montonEnJuego).getPalos().get(palosQuitados).setSeleccionado(true);
            Sonidos.getInstance(getApplicationContext()).play(Sonidos.Efectos.TICK);


            CountDownTimer pausaVerJugada = new CountDownTimer(1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    visualizarTablero(partida.getTablero());
                }

                @Override
                public void onFinish() {
                    finJugadaCom();
                    this.cancel();
                }
            };
            pausaVerJugada.start();


        } else {
            // Ponemos un cronómetro que nos vale como si fuera un AsyncTask
            // Cada segundo seleccionamos un palo de los que nos hayan salido
            // en el cálculo de la jugada.
            //
            // Cuando acaba el cronómetro, quitamos los palos y lo mostramos
            // también cambiará el turno pasando al jugador
            //
            jugadaComTimer = new CountDownTimer(1100 * palosAQuitar, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    // Seleccionamos un palo del montón
                    // Y lo visualizamos
                    if (palosQuitados < palosAQuitar) {
                        cambiarImagenPalitrokes();
                        partida.getTablero().getMontones().get(montonEnJuego).getPalos().get(palosQuitados).setSeleccionado(true);
                        palosQuitados++;
                        Sonidos.getInstance(getApplicationContext()).play(Sonidos.Efectos.TICK);
                        visualizarTablero(partida.getTablero());
                    }

                }

                @Override
                public void onFinish() {
                    finJugadaCom();
                }
            };
            jugadaComTimer.start();
        }
    }

    private void finJugadaCom() {
        // Paramos este crono
        if (jugadaComTimer != null) {
            jugadaComTimer.cancel();
        }
        cronometro2.cancel();


        // Eliminamos el palos seleccionados del Tablero
        partida.getTablero().eliminarSeleccionados();

        Sonidos.getInstance(getApplicationContext()).play(Sonidos.Efectos.PLING);

        Log.d(Constantes.TAG, "Palos que quedan: " + partida.getTablero().palosTotales());
        // Detectar si solo queda 1 palo, le queda al jugador y ...
        if (partida.getTablero().palosTotales() == 1) {
            // Solo queda un palo. Ha ganado el ordenador
            partida.setGanador(2);
            finTurno();
        }

        pausa(500);

        // Cambiamos el turno y Actualizamos el tablero y los views
        visualizarTablero(partida.getTablero());
        partida.turnoToggle();
        actualizarViewsCambioTurno();
    }

    private void pausa(int i) {
        // Dejamos una pausa
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void finJuego() {

        // Quitar los cronómetros
        cronometro1.cancel();
        cronometro2.cancel();
        //   Intent volverIntent = new Intent(this, MainActivity.class);
        //  volverIntent.putExtra("SALA_ANTERIOR", partida.getPartidaID());

        String resultado = "";

        if (abandono) {
            resultado = getString(R.string.abandono);
        }

        Log.d(Constantes.TAG, "Ganador: " + partida.getGanador());
        if (partida.getGanador() == jugador.getNumeroJugador()) {
            resultado += (getString(R.string.win));
            Sonidos.getInstance(getApplicationContext()).play(Sonidos.Efectos.GANAR);
            siguienteNivel();
        } else {

            resultado += (getString(R.string.resultado)) + partida.getLevel();
            Sonidos.getInstance(getApplicationContext()).play(Sonidos.Efectos.PERDER);
            // Guardar record si hay internet, podemos mirar si hemos entrado en los records del juego
            // Si no hay internet, solo sumamos la victoria a nuestras estadísticas
            if (UtilityNetwork.isNetworkAvailable(this) || UtilityNetwork.isWifiAvailable(this)) {
                UtilsFirebase.guardarRecords(getApplicationContext(), jugador, partida.getLevel());
            }

            SharedPrefs.updateRecordsPrefs(getApplicationContext(), new Records(jugador.getJugadorId(), jugador.getNickname(), jugador.getVictorias(), partida.getLevel()));
            SharedPrefs.saveJugadorPrefs(getApplicationContext(), jugador);

            finish();
            // Lanzamos el intent del MainActivity
            //  startActivity(volverIntent);
        }

        Toast.makeText(this, resultado, Toast.LENGTH_LONG).show();


        Log.d(Constantes.TAG, "Esperamos 1 segundo");
        // Dejamos una pausa para que se actualice la sala
        pausa(1000);


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
    public void onBackPressed() {
        abandono = true;
        finJuego();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (partida.getGanador() == 0) {
            abandono = true;
            finJuego();
        }
    }

    @Override
    protected void onStop() {
        cronometro1.cancel();
        cronometro2.cancel();
        //  cronometro1 = null;
        //  cronometro2 = null;
        avatarJ2.setImageDrawable(null);
        avatarJ1.setImageDrawable(null);
        linearBase = null;

        if (jugadaComTimer != null) {
            jugadaComTimer.cancel();
        }

        super.onStop();
    }


    private void mostrarLevel() {

        linearBase.setVisibility(View.INVISIBLE);
        level.setVisibility(View.VISIBLE);
        level.setText((getString(R.string.nivel)) + (partida.getLevel() + 1));

        final CountDownTimer levelCrono = new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                level.setVisibility(View.INVISIBLE);
                linearBase.setVisibility(View.VISIBLE);
                visualizarTablero(partida.getTablero());
                actualizarViewsCambioTurno();
                Sonidos.getInstance(getApplicationContext()).play(Sonidos.Efectos.UIIIIU);
            }
        };

        levelCrono.start();

    }

    private void siguienteNivel() {

        partida.levelUp();

        // Asegurarnos que en la primera jugada, el jugador tenga las de ganar siempre
        Tablero nuevoTablero;
        do {
            nuevoTablero = new Tablero(partida.getLevel());
        } while (!new JugadaCom().esGanador(nuevoTablero));

        partida.setTablero(nuevoTablero);
        partida.setGanador(0);
        partida.setTurno(1);
        jugador.setVictorias(1);


        inicializarColores();

        actualizarVistaJugador();

        mostrarLevel();
    }


    private void inicializarColores() {
        // Inicializamos el array de colores aleatorios para el fondo de la pantalla
        colores = new int[6];
        Random rnd = new Random();
        for (int n = 0; n < colores.length; n++) {
            colores[n] = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        }
    }


    public void cambiarImagenPalitrokes() {
        Random rnd = new Random();
        String name = "pic" + (rnd.nextInt(116) + 34);
        int resource = getResources().getIdentifier(name, "drawable", "com.game.palitrokes");
        avatarJ2.setImageResource(resource);
    }

    @Override
    protected void onDestroy() {
        avatarJ1.setImageDrawable(null);
        avatarJ2.setImageDrawable(null);

        super.onDestroy();
    }

    @Override
    protected void onResume() {

        if (partida.getTurno() == 1) {
            cronometro1.start();
        } else {
            cronometro2.start();
        }

        super.onResume();
    }
}
