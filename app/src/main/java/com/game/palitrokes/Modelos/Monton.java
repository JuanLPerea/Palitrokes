package com.game.palitrokes.Modelos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Monton {
    public static final int MAX_PALOS = 8;
    public static final int MIN_PALOS = 1;
    private List<Palo> palos;
    private int numeroMonton;


    public Monton(int numeroMonton, int level) {

        this.palos = new ArrayList<>();
        this.numeroMonton = numeroMonton;

        Random r = new Random();
        // int numPalosAleat = r.nextInt(MAX_PALOS - MIN_PALOS) + MIN_PALOS;
        // el número de palos en cada monton depende del nivel y además va variando del primer monton al último de menos palos a mas

//        int numPalosAleat = (numeroMonton * ((level+1) * (r.nextInt(MAX_PALOS - MIN_PALOS) + MIN_PALOS))) + 1;

        int numPalosAleat = 0;

        switch (numeroMonton) {
            case 0:
                numPalosAleat = r.nextInt(2) +1;
                break;
            case 1:
                numPalosAleat = r.nextInt(4) +1;
                break;
            case 2:
                numPalosAleat = r.nextInt(6) +1;
                break;
            default:
                numPalosAleat = r.nextInt(8) +1;
                break;
        }



        for (int n = 0; n < numPalosAleat; n++) {
            this.palos.add(new Palo(n));
        }

    }

    public Monton() {

    }

    public int getNumeroMonton() {
        return numeroMonton;
    }

    public void setNumeroMonton(int numeroMonton) {
        this.numeroMonton = numeroMonton;
    }

    public List<Palo> getPalos() {
        return palos;
    }

    public void setPalos(List<Palo> palos) {
        this.palos = palos;
    }

    public int getPalosseleccionados() {

        int palosSeleccionados = 0;
        for (Palo paloTmp : palos) {
            if (paloTmp.isSeleccionado()) palosSeleccionados++;
        }

        return palosSeleccionados;
    }

    public void deseleccionarTodo() {
        for (Palo paloTmp : this.getPalos()) {
            paloTmp.setSeleccionado(false);
        }
    }

    public void renumerarPalos() {
        for (int n = 0; n < palos.size(); n++) {
            palos.get(n).setNumeroPalo(n);
        }
    }


}
