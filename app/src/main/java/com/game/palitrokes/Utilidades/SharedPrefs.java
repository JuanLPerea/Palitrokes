package com.game.palitrokes.Utilidades;

import android.content.Context;
import android.content.SharedPreferences;

import com.game.palitrokes.Modelos.Jugador;
import com.game.palitrokes.Modelos.Records;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SharedPrefs {


    public static void saveJugadorPrefs(Context context, Jugador jugador) {

        Set<String> amigos = new HashSet<String>();
        if (jugador.getFavoritosID() != null) {
            amigos.addAll(jugador.getFavoritosID());
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constantes.ARCHIVO_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constantes.NICKNAME_PREFS, jugador.getNickname());
        editor.putString(Constantes.JUGADORID_PREFS, jugador.getJugadorId());
        editor.putInt(Constantes.VICTORIAS_PREFS, jugador.getVictorias());
        editor.putBoolean(Constantes.FIRST_RUN, jugador.isFirstRun());
        editor.putStringSet(Constantes.AMIGOS, amigos);
        editor.commit();
    }

    public static Jugador getJugadorPrefs(Context context) {

        Jugador jugador = new Jugador();
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constantes.ARCHIVO_PREFS, Context.MODE_PRIVATE);
        jugador.setNickname(sharedPreferences.getString(Constantes.NICKNAME_PREFS, "Jugador"));
        jugador.setJugadorId(sharedPreferences.getString(Constantes.JUGADORID_PREFS, null));
        jugador.setVictorias(sharedPreferences.getInt(Constantes.VICTORIAS_PREFS, 0));
        jugador.setFirstRun(sharedPreferences.getBoolean(Constantes.FIRST_RUN, true));

        Set<String> amigos = sharedPreferences.getStringSet(Constantes.AMIGOS, null);
        List<String> amigosList = new ArrayList<>();
        if (amigos != null) {
            amigosList.addAll(amigos);
            jugador.setFavoritosID(amigosList);
        }




        return jugador;
    }

    public static void saveRecordsPrefs(Context context, List<Records> records) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constantes.ARCHIVO_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int n = 0;
        for (Records record : records) {
            editor.putString(Constantes.RECORDS_PREFS + n, record.getIdJugador() + "#" + record.getNickname() + "#" + record.getLevel() + "#" + record.getVictorias());
            n++;
        }
        editor.commit();
    }

    public static List<Records> getRecordsPrefs(Context context) {
        List<Records> records = new ArrayList<>();
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constantes.ARCHIVO_PREFS, Context.MODE_PRIVATE);

            for (int n = 0; n < 10; n++) {
                String recordGuardado = sharedPreferences.getString(Constantes.RECORDS_PREFS + n, null);

                if (recordGuardado != null) {
                    String[] recordSplit = recordGuardado.split("#");
                    if (recordSplit != null) {
                        records.add(new Records(recordSplit[0], recordSplit[1], Integer.parseInt(recordSplit[3]), Integer.parseInt(recordSplit[2])));
                    }
                } else {
                    records.add(new Records("idJugador" ,"Jugador " + n, 0,0));
                }
            }

        return records;
    }


}
