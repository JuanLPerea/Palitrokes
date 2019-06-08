package com.game.palitrokes.Modelos;

public class Palo {

    private int numeroPalo;
    private boolean seleccionado;

    public Palo(int numeroPalo) {
        this.numeroPalo = numeroPalo;
        this.seleccionado = false;
    }

    public Palo() {
    }

    public int getNumeroPalo() {
        return numeroPalo;
    }

    public void setNumeroPalo(int numeroPalo) {
        this.numeroPalo = numeroPalo;
    }

    public boolean isSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(boolean seleccionado) {
        this.seleccionado = seleccionado;
    }


}
