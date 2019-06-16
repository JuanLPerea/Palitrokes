package com.game.palitrokes.Modelos;

import android.util.Log;

import com.game.palitrokes.Utilidades.Constantes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Juego del NIM (A.K.A. Palitrokes)
 * Procesar un tablero para obtener una jugada de salida
 * según estrategia: El que coge el último palo, pierde
 * (C) Juan Luis Perea López (2019)
 */
public class JugadaCom {

    private List<String> montonesBinario = new ArrayList<>();
    private int maxLargoBinario = 0;
    private int[] sumaColumnas;

    public String jugadaCom(Tablero tablero) {

        String jugadaSalida = null;

        // Posicion perdedora, todas las columnas pares
        // ---------------------------------------------------------------------------------------------
        if (!esGanador(tablero)) {
            Log.d(Constantes.TAG, "Posición perdedora");

            // No podemos hacer nada, jugamos al azar con la esperanza de que se equivoque el otro jugador
            Random r = new Random();
            int montonAleatorio = r.nextInt(tablero.getMontones().size());
            int palosAleatorios = r.nextInt(tablero.getMontones().get(montonAleatorio).getPalos().size());
            // Al menos que quite un palo siempre
            if (palosAleatorios == 0) palosAleatorios = 1;
            jugadaSalida = montonAleatorio + "#" + palosAleatorios;
          //  jugadaSalida = "0#1";

            Log.d(Constantes.TAG, "Jugada de salida: " + jugadaSalida);
            Log.d(Constantes.TAG, "-----------------------------------------------------");
            return jugadaSalida;

        } else {
            // Posicion ganadora, alguna columna es impar
            // ---------------------------------------------------------------------------------------------
            Log.d(Constantes.TAG, "Posición ganadora");


            // En la estrategia de que pierde el que se quede con el último palo:
            // Hay que comprobar que solo queda un monton con mas de 1 palo...
            int numeroDeMontonesConMasDeUnPalo = 0;
            int montonConMasDeUnPalo = 0;

            for (Monton montonTmp : tablero.getMontones()) {
                if (montonTmp.getPalos().size() > 1) {
                    numeroDeMontonesConMasDeUnPalo++;
                    montonConMasDeUnPalo = montonTmp.getNumeroMonton();
                }
            }
            Log.d(Constantes.TAG, "Quedan " + numeroDeMontonesConMasDeUnPalo  + " Montones con mas de 1 palo. Total Montones: " + (tablero.getNumMontones()));

            // Hay 3 casos:
            // 1) que quede solo un monton con 1 palo
            // 2) que queden todos los montones con 1 palo
            // 3) que queden mas de 1 monton con mas de 1 palo.....
            // -------------------------------------------------------------------------------------------------------------------
            if (numeroDeMontonesConMasDeUnPalo == 1) {
                // Si solo queda un monton con mas de 1 palo,
                // Si el numero de montones es par quitamos todos los palos menos unodel montón que tenga mas de 1 palo
                // Si el numero de montones es impar, quitamos todos los palos del montón que tenga mas de 1 palo
                Log.d(Constantes.TAG, "Queda solo un monton con mas de 1 palo ");
                if (tablero.getMontones().size() % 2 == 0) {
                    jugadaSalida = montonConMasDeUnPalo + "#" + (tablero.getMontones().get(montonConMasDeUnPalo).getPalos().size());
                } else {
                    jugadaSalida = montonConMasDeUnPalo + "#" + (tablero.getMontones().get(montonConMasDeUnPalo).getPalos().size() - 1);
                }

            } else if (numeroDeMontonesConMasDeUnPalo == 0) {
                // Si todos los montones tienen solo 1 palo
                // Quitamos cualquiera (en este caso palo que queda en el primer montón)
                Log.d(Constantes.TAG, "Todos los montones que quedan tienen solo 1 palo");
                jugadaSalida = "0#1";

            } else if (numeroDeMontonesConMasDeUnPalo > 1) {
                // En este caso usamos la estrategia analizando los montones
                //
                // Seleccionamos el monton que tenga una suma impar en las columnas
                Log.d(Constantes.TAG, "Quedan muchos palos, usamos estrategia");
                int columnaImpar = 0;
                for (int cnd = 0; cnd <= maxLargoBinario; cnd++) {
                    if (sumaColumnas[cnd] % 2 != 0) {
                        columnaImpar = cnd;
                        break;
                    }
                }

                Log.d(Constantes.TAG, "Columna Impar: " + columnaImpar);

                // Nos quedamos con un monton que tenga un 1 en la posicion de la columnaImpar
                int numeroMontonATratar = -1;
                for (int cnd = 0; cnd < montonesBinario.size(); cnd++) {
                    String montonTmp = montonesBinario.get(cnd);
                    Character buscarMonton = montonTmp.charAt(columnaImpar);
                    if (buscarMonton == '1') {
                        numeroMontonATratar = cnd;
                    }
                }


                // Tratamos las columnas que eran impares para que sean pares
                // O si son enteramente son unos, quitamos todos los palos de ese montón
                String montonATratar = montonesBinario.get(numeroMontonATratar);
                String montonTratado = "";
                Log.d(Constantes.TAG, "Monton a tratar: " + numeroMontonATratar + " - " + montonATratar);

                for (int cnd = 0; cnd < sumaColumnas.length; cnd++) {
                    // Sacar los caracteres uno a uno
                    Character caracter = montonATratar.charAt(cnd);
                    // en las posiciones que eran impares cambiamos 0 por 1 y viceversa
                    if (sumaColumnas[cnd] % 2 != 0) {
                        if (caracter == '0') {
                            montonTratado = montonTratado + "1";
                        } else {
                            montonTratado = montonTratado + "0";
                        }
                    } else {
                        // Si la columna es par la dejamos como está
                        montonTratado = montonTratado + caracter;
                    }
                }
                Log.d(Constantes.TAG, "Monton tratado: " + montonTratado);

                // Si el resultado es 'unos', quitaremos todos los palos del montón elegido
                int todoUnos = montonTratado.indexOf("0");
                if (todoUnos == -1) {
                    jugadaSalida = numeroMontonATratar + "#" + tablero.getMontones().get(numeroMontonATratar).getPalos().size();
                } else {
                    // Si el resultado tiene algún cero, Convertir monton tratado a decimal
                    // y habrá que restar del total de palos, los que tienen que quedar,
                    // que es el número que sale de tratar el montón elegido pasado a decimal
                    int numeroPalosOriginal = tablero.getMontones().get(numeroMontonATratar).getPalos().size();
                    int numeroPalosAQuitar = Integer.parseInt(montonTratado, 2);
                    int numeroPalosSalida = numeroPalosOriginal - numeroPalosAQuitar;

                    jugadaSalida = numeroMontonATratar + "#" + numeroPalosSalida;
                }

            }
        }


        Log.d(Constantes.TAG, "Jugada de salida: " + jugadaSalida);
        Log.d(Constantes.TAG, "-----------------------------------------------------");
        return jugadaSalida;

    }


    public static String decimalABinario(int numero) {

        String binario = "";
        if (numero > 0) {
            while (numero > 0) {
                if (numero % 2 == 0) {
                    binario = "0" + binario;
                } else {
                    binario = "1" + binario;
                }
                numero = (int) numero / 2;
            }
        } else if (numero == 0) {
            binario = "0";
        }

        return binario;

    }

    public boolean esGanador(Tablero tablero) {

        boolean esGanador = false;

        // Analizar el tablero para saber si estamos en una posición ganadora o perdedora
        // ----------------------------------------------------------------------------------------------------


        // Convertir el número de palos de cada montón a binario y
        // Guardarlo en una lista de Strings
        for (Monton montonTmp : tablero.getMontones()) {
            String cadenaBinario = decimalABinario(montonTmp.getPalos().size());
            // Guardamos el largo máximo de las cadenas en formato binario
            if (cadenaBinario.length() > maxLargoBinario) {
                maxLargoBinario = cadenaBinario.length() - 1;
            }
            montonesBinario.add(cadenaBinario);
            Log.d(Constantes.TAG, montonTmp.getPalos().size() + "");
        }

        Log.d(Constantes.TAG, "Numero de columnas: " + maxLargoBinario);

        // añadir ceros a la izquierda si es necesario
        List<String> nuevoBinarioString = new ArrayList<>();
        for (String binarioString : montonesBinario) {
            String nuevoString = binarioString;
            if (binarioString.length() <= maxLargoBinario) {
                for (int cnd = 0; cnd < maxLargoBinario - binarioString.length() + 1; cnd++) {
                    nuevoString = "0" + nuevoString;
                }
            }
            nuevoBinarioString.add(nuevoString);
        }
        montonesBinario = nuevoBinarioString;


        for (String binarioString : montonesBinario) {
            Log.d(Constantes.TAG, "Montones en binario: " + binarioString);
        }


        // Sumar las columnas
        int numeroDePares = 0;
        sumaColumnas = new int[maxLargoBinario + 1];
        for (int posicion = 0; posicion <= maxLargoBinario; posicion++) {
            for (String binarioString : montonesBinario) {
                if ((binarioString.length() - 1) - posicion >= 0) {
                    sumaColumnas[maxLargoBinario - posicion] += Integer.parseInt(binarioString.charAt(binarioString.length() - posicion - 1) + "");
                }
            }
        }


        for (int cnd = 0; cnd <= maxLargoBinario; cnd++) {
            Log.d(Constantes.TAG, "Suma Columnas: " + sumaColumnas[cnd]);
            if (sumaColumnas[cnd] % 2 == 0) numeroDePares++;
        }

        Log.d(Constantes.TAG, "Numero de pares: " + numeroDePares);

        if (numeroDePares == maxLargoBinario + 1) {
            esGanador = false;
        } else {
            esGanador = true;
        }


        return esGanador;
    }




}
