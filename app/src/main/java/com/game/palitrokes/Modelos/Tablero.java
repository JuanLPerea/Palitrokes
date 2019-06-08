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
    private int montonSeleccionado;

    // Constructor generamos un nuevo tablero con montones y monedas aleatoriamente
    public Tablero() {
        Random r = new Random();

        this.numMontones = r.nextInt(MAX_MONTONES - MIN_MONTONES) + MIN_MONTONES;
        this.montonSeleccionado = -1;

        montones = new ArrayList<>();

        for (int n = 0; n < this.numMontones; n++) {
            montones.add(new Monton(n));
        }
    }

    public int getMontonSeleccionado() {
        return montonSeleccionado;
    }

    public void setMontonSeleccionado(int montonSeleccionado) {
        this.montonSeleccionado = montonSeleccionado;
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

    public int palosSeleccionadosTotal() {

        int totalPalosSeleccionados = 0;

        for (Monton montonTmp : montones) {
            totalPalosSeleccionados += montonTmp.getPalosseleccionados();
        }

        return totalPalosSeleccionados;
    }

    public void eliminarSeleccionados() {

        // En el montón seleccionado, eliminamos tantos palos como indique que tenemos seleccionados (Por el final de la lista).

        List<Palo> newPalos = new ArrayList<>();
        newPalos.removeAll(newPalos);

        for (Palo paloTmp : this.montones.get(montonSeleccionado).getPalos()) {
            if (!paloTmp.isSeleccionado()) {
               newPalos.add(new Palo());
            }
        }
        this.getMontones().get(montonSeleccionado).setPalos(newPalos);



        // Si el montón está vacío, quitar también el montón
        if (this.montones.get(montonSeleccionado).getPalos().size() == 0) {
            this.montones.remove(montonSeleccionado);
            renumerarMontones();
        } else {
            // Si el montón todavía no está vacío indicar que no tenemos nada seleccionado
          //  this.montones.get(montonSeleccionado).deseleccionarTodo();
            this.montones.get(montonSeleccionado).renumerarPalos();

        }
        this.montonSeleccionado = -1;

    }

    public void renumerarMontones() {
        for (int n= 0 ; n < montones.size() ; n++) {
            montones.get(n).setNumeroMonton(n);
        }
    }

    public int maxPalosMontones() {

        int maxPalos = 0;

        for (Monton montonTmp : montones) {
            if (montonTmp.getPalos() != null) {
                if (montonTmp.getPalos().size() > maxPalos) maxPalos = montonTmp.getPalos().size();
            }
        }

        return maxPalos;
    }

    public int palosTotales() {

        int numPalos = 0;

        for (Monton montonTmp : montones) {
            numPalos += montonTmp.getPalos().size();
        }



        return numPalos;

    }

}
