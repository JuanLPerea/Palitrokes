package com.game.palitrokes.Modelos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Monton {
    public static final int MAX_PALOS = 9;
    public static final int MIN_PALOS = 1;
    private List<Palo> palos;
    private int numeroMonton;


    public Monton(int numeroMonton) {

        this.palos = new ArrayList<>();
        this.numeroMonton = numeroMonton;

        Random r = new Random();
        int numPalosAleat = r.nextInt(MAX_PALOS - MIN_PALOS) + MIN_PALOS;
        for (int n=0 ; n<numPalosAleat ; n++) {
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
        for (int n= 0 ; n< palos.size() ; n++) {
            palos.get(n).setNumeroPalo(n);
        }
    }



}
