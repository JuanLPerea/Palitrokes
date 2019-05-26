package com.game.palitrokes.Modelos;

import java.util.Random;

public class Monton {
    public static final int MAX_PALOS = 9;
    public static final int MIN_PALOS = 1;
    private int palos;
    private int palosseleccionados;

    public Monton() {
        Random r = new Random();
        this.palos = r.nextInt(MAX_PALOS - MIN_PALOS) + MIN_PALOS;
        this.palosseleccionados= 0;
    }

    public int getPalos() {
        return palos;
    }

    public void setPalos(int palos) {
        this.palos = palos;
    }

    public int getPalosseleccionados() {
        return palosseleccionados;
    }

    public void setPalosseleccionados(int palosseleccionados) {
        this.palosseleccionados = palosseleccionados;
    }
}
