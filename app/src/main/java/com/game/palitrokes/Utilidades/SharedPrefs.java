package com.game.palitrokes.Utilidades;

import android.content.Context;
import android.content.SharedPreferences;

import com.game.palitrokes.Modelos.Jugador;
import com.game.palitrokes.Modelos.Records;

public class SharedPrefs {


    public static void saveJugadorPrefs(Context context, Jugador jugador) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constantes.ARCHIVO_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constantes.NICKNAME_PREFS, jugador.getNickname());
        editor.putString(Constantes.JUGADORID_PREFS, jugador.getJugadorId());
        editor.putInt(Constantes.VICTORIAS_PREFS, jugador.getVictorias());
    }

    public static Jugador getJugadorPrefs(Context context) {

        Jugador jugador = new Jugador();

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constantes.ARCHIVO_PREFS, Context.MODE_PRIVATE);
        jugador.setNickname(sharedPreferences.getString(Constantes.NICKNAME_PREFS, "Jugador"));
        jugador.setJugadorId(sharedPreferences.getString(Constantes.JUGADORID_PREFS, null));
        jugador.setVictorias(sharedPreferences.getInt(Constantes.JUGADORID_PREFS, 0));

        return jugador;
    }

    public static void saveRecordsPrefs (Context context, Records records) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constantes.ARCHIVO_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
    }


}
