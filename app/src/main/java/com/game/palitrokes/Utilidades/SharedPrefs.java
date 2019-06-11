package com.game.palitrokes.Utilidades;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefs {

    public static String getNicknamePrefs(Context context) {
        String nickPrefs = null;
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constantes.ARCHIVO_PREFS, Context.MODE_PRIVATE);
        nickPrefs = sharedPreferences.getString(Constantes.NICKNAME_PREFS, "");
        return nickPrefs;
    }


    public static void saveNickPrefs(Context context,String nickname) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constantes.ARCHIVO_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constantes.NICKNAME_PREFS, nickname);
        editor.commit();
    }


    public static String getAvatarPrefs(Context context) {
        String nickPrefs = null;
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constantes.ARCHIVO_PREFS, Context.MODE_PRIVATE);
        nickPrefs = sharedPreferences.getString(Constantes.RUTA_FOTO_PREFS, "");
        return nickPrefs;
    }


    public static void saveAvatarPrefs(Context context,String rutaFoto) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constantes.ARCHIVO_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constantes.RUTA_FOTO_PREFS, rutaFoto);
        editor.commit();
    }


}
