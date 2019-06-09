package com.game.palitrokes.Modelos;

import android.util.Log;

import com.game.palitrokes.Utilidades.Constantes;

import java.util.ArrayList;
import java.util.List;
/**
 *
 *  Procesar un tablero para obtener una jugada de salida
 *
 *
 *
 */
public class JugadaCom {

    public static String jugadaCom(Tablero tablero) {

        String jugadaSalida = null;


        List<String> montonesBinario = new ArrayList<>();
        int maxLargoBinario = 0;

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
        int[] sumaColumnas = new int[maxLargoBinario + 1];
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

        // Posicion perdedora, todas las columnas pares
        // ---------------------------------------------------------------------------------------------
        if (numeroDePares == maxLargoBinario + 1) {
            Log.d(Constantes.TAG, "Posición perdedora");

            // No podemos hacer nada, jugamos al azar con la esperanza de que se equivoque
            jugadaSalida = "1#1";
            return jugadaSalida;

        } else {
        // Posicion ganadora, alguna columna es impar
        // ---------------------------------------------------------------------------------------------
            Log.d(Constantes.TAG, "Posición ganadora");
            // Seleccionamos el monton que tenga una suma impar en las columnas
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
                // Comprobar que no sean todo '1' en el monton seleccionado
                int todounos = 0;
                for (int pos = 0; pos < montonTmp.length() - 1; pos++) {
                    todounos += Integer.parseInt(montonTmp.charAt(pos) + "");
                }
                if (todounos != montonTmp.length()) {
                    Character buscarMonton = montonTmp.charAt(columnaImpar);
                    if (buscarMonton == '1') {
                        numeroMontonATratar = cnd;
                    }
                }
            }

            if (numeroMontonATratar == -1) {
                Log.d(Constantes.TAG, "Esto no debería pasar, por lo menos tiene que haber un monton para tratar");
            }

            // Tratamos las columnas que eran impares para que sean pares
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


            // Convertir monton tratado a decimal
            int numeroPalosOriginal = tablero.getMontones().get(numeroMontonATratar).getPalos().size();
            int numeroPalosAQuitar = Integer.parseInt(montonTratado, 2);
            int numeroPalosSalida = numeroPalosOriginal - numeroPalosAQuitar;

            jugadaSalida = numeroMontonATratar + "#" + numeroPalosSalida;


        }

        Log.d(Constantes.TAG, "Jugada de salida: " + jugadaSalida);
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


}
