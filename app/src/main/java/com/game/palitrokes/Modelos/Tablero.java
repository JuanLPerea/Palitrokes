package com.game.palitrokes.Modelos;

import android.util.Log;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Tablero {

    // En el tablero habrá un numero de montones de monedas aleatorio de 3 a 6

    private final int MAX_MONTONES = 6;
    private final int MIN_MONTONES = 3;

    private List<Monton> montones;
    private int numMontones;

    // Constructor generamos un nuevo tablero con montones y monedas aleatoriamente
    public Tablero() {
        Random r = new Random();

        this.numMontones = r.nextInt(MAX_MONTONES - MIN_MONTONES) + MIN_MONTONES;

        montones = new ArrayList<>();

        for (int n = 0; n < this.numMontones; n++) {
            montones.add(new Monton());
        }
    }

    public List<Monton> getMontones() {
        return montones;
    }

    public void setMontones(List<Monton> montones) {
        this.montones = montones;
    }

    public int getNumMontones() {
        return numMontones;
    }

    public void setNumMontones(int numMontones) {
        this.numMontones = numMontones;
    }
}
