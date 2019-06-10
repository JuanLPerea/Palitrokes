package com.game.palitrokes;

import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.game.palitrokes.Modelos.JugadaCom;
import com.game.palitrokes.Modelos.Jugador;
import com.game.palitrokes.Modelos.Monton;
import com.game.palitrokes.Modelos.Palo;
import com.game.palitrokes.Modelos.Partida;
import com.game.palitrokes.Modelos.Tablero;
import com.game.palitrokes.Utilidades.Constantes;
import com.game.palitrokes.Utilidades.Utilidades;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class JuegoVsComActivity extends AppCompatActivity {


    private LinearLayout linearBase;
    private Partida partida;
    private Jugador jugador;
    private Jugador rival;
    private String salaJuego;
    private ImageView avatarJ1, avatarJ2;
    private TextView nickJ1, nickJ2, winsJ1, winsJ2, tiempoJ1, tiempoJ2;
    private Button okJ1, okJ2;
    private int[] colores;
    private CountDownTimer cronometro1;
    private CountDownTimer cronometro2;
    private boolean finTiempo;


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


        finTiempo = false;

        // Recuperamos los datos del jugador del Intent
        Intent intent = getIntent();
        String idJugador = intent.getStringExtra("IDJUGADOR");
        String nickJugador = intent.getStringExtra("NICKNAME");
        int victorias = intent.getIntExtra("VICTORIAS", 0);


        //Seteamos el jugador con los datos del intent
        jugador = new Jugador();
        jugador.setNumeroJugador(1);
        jugador.setVictorias(victorias);
        jugador.setNickname(nickJugador);
        jugador.setJugadorId(idJugador);
        actualizarVistaJugador(jugador);

        // creamos un rival vacío
        rival = new Jugador();
        rival.setNickname("Palitrokes");
        rival.setVictorias(999);
        rival.setNumeroJugador(2);
        rival.setJugadorId("PALITROKES");
        actualizarVistaJugador(rival);


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

        // Inicializamos el array de colores aleatorios para el fondo de la pantalla
        colores = new int[6];
        Random rnd = new Random();
        for (int n = 0; n < colores.length; n++) {
            colores[n] = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        }

        // Creamos una nueva partida
        // El turno será aleatorio para que no haya ventajas
        partida = new Partida();
        //  partida.setTurno(rnd.nextInt(2) + 1);
        partida.setTurno(1);
        partida.setJugador1ID(jugador.getJugadorId());
        partida.setJugador2ID(rival.getJugadorId());

        visualizarTablero(partida.getTablero());
        actualizarViewsCambioTurno();

    }

    private void actualizarVistaJugador(Jugador jugadorView) {
        if (jugadorView.getNumeroJugador() == 1) {
            // Descargar la imagen de Firebase solo 1 vez
            if (avatarJ1.getTag().equals("false")) {
                Utilidades.descargarImagenFirebase(jugadorView.getJugadorId(), avatarJ1);
                avatarJ1.setTag("true");
            }
            nickJ1.setText(jugadorView.getNickname());
            winsJ1.setText("Victorias: " + jugadorView.getVictorias());

        } else {
            // Descargar la imagen de Firebase solo 1 vez
            if (avatarJ2.getTag().equals("false")) {
                Utilidades.descargarImagenFirebase(jugadorView.getJugadorId(), avatarJ2);
                avatarJ2.setTag("true");
            }
            nickJ2.setText(jugadorView.getNickname());
            winsJ2.setText("Victorias: " + jugadorView.getVictorias() + "");
        }
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
                    Log.d(Constantes.TAG, "El ganador es: " + partida.getTurno());
                    partida.setGanador(partida.getTurno());
                } else {
                    // Si aún quedan palos Cambiamos el turno y se lo pasamos al rival
                    Log.d(Constantes.TAG, "Fin de turno botón OK");
                }
            } else {
                Toast.makeText(this, "Selecciona al menos 1 palo", Toast.LENGTH_LONG).show();
                if (finTiempo) {
                    Log.d(Constantes.TAG, "Ha terminado el tiempo sin seleccionar ningun palo");
                    partida.setGanador(2);
                }
            }
            finTurno();
        } else {
            Toast.makeText(this, "Debes dejar al menos 1 palo y ganas!!!", Toast.LENGTH_LONG).show();
        }
        visualizarTablero(partida.getTablero());
    }


    private void finTurno() {

        if (partida.getGanador() == 0) {

            partida.turnoToggle();
            actualizarViewsCambioTurno();

            if (partida.getTurno() == 2) {
                Log.d(Constantes.TAG, "Turno del ordenador");
                //  Pasar el tablero a la Clase
                //  JugadaCom Nos devuelve un String con la
                //  jugada que va ha hacer el ordenador
                String jugadaCom = JugadaCom.jugadaCom(partida.getTablero());

                // Hacemos los cambios que sean
                hacerJugadaCom(jugadaCom);

                // Cambiamos el turno y Actualizamos el tablero y los views
                visualizarTablero(partida.getTablero());
                partida.turnoToggle();
                actualizarViewsCambioTurno();
            }

        } else {
            Log.d(Constantes.TAG, "Fin juego");
            finJuego();
        }


    }

    private void hacerJugadaCom(String jugadaCom) {

        int montonEnJuego = Integer.parseInt(jugadaCom.split("#")[0]);
        int palosAQuitar = Integer.parseInt(jugadaCom.split("#")[1]);

        partida.getTablero().setMontonSeleccionado(montonEnJuego);

        // Simulamos que se seleccionan los palos y se quitan
        // Haciendo una pausa de medio segundo entre palo y palo
        // TODO poner un sonido para que quede mas chulo
        for (int cnd = 0 ; cnd < palosAQuitar ; cnd++) {
            // Seleccionamos un palo del montón
            partida.getTablero().getMontones().get(montonEnJuego).getPalos().get(cnd).setSeleccionado(true);


            // Y lo visualizamos
            visualizarTablero(partida.getTablero());

            // Dejamos una pausa de medio segundo para que al jugador
            // le de tiempo a ver la jugada ...
            // TODO crear AsyncTask que actualice las views sin bloquear la app

        }


        // Eliminamos el palos seleccionados del Tablero
        partida.getTablero().eliminarSeleccionados();

        // Detectar si solo queda 1 palo, le queda al jugador y ...
        if (partida.getTablero().palosTotales() == 1) {
            // Solo queda un palo. Ha ganado el ordenador
            partida.setGanador(2);
        }

    }


    private void finJuego() {

        // Quitar los cronómetros
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
        } else {
            jugador.setDerrotas(1);
            resultado += "Lo siento ¡has perdido!";
        }

        Toast.makeText(this, resultado , Toast.LENGTH_LONG).show();



        Log.d(Constantes.TAG, "Esperamos 1 segundo");
        // Dejamos una pausa para que se actualice la sala
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Lanzamos el intent del MainActivity
        startActivity(volverIntent);

        finish();
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
        finTiempo = true;
        finJuego();
    }


    @Override
    protected void onStop() {
        if (finTiempo == true) {
            finJuego();
        }
        super.onStop();
    }
}
