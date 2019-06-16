package com.game.palitrokes.Utilidades;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.game.palitrokes.R;

public class Sonidos {

    private Context contexto;
    private SoundPool player;
    public enum Efectos {TICK, GANAR, PERDER, BGM, ONLINE};
    private int tick, ganar, perder, bgm, online;


    public Sonidos(Context contexto) {
        this.contexto = contexto;
        player = new SoundPool(8, AudioManager.STREAM_MUSIC , 0);
        tick = player.load(contexto, R.raw.toc, 1);
        ganar = player.load(contexto, R.raw.win, 1);
        perder = player.load(contexto, R.raw.lose, 1);
        bgm = player.load(contexto, R.raw.fiiiu, 1);
        online = player.load(contexto, R.raw.uiiiiu, 1);


    }

    public void play(Efectos efecto) {

        switch (efecto) {
            case TICK:
                player.play(tick, 1,1,0,0,1);
                break;
            case GANAR:
                player.play(ganar, 1,1,0,0,1);
                break;
            case PERDER:
                player.play(perder, 1,1,0,0,1);
                break;
            case BGM:
                player.play(bgm, 1,1,0,0,1);
                break;
            case ONLINE:
                player.play(online, 1,1,0,0,1);
                break;
        }
    }



}
