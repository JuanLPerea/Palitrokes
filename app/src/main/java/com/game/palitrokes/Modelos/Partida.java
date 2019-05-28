package com.game.palitrokes.Modelos;

public class Partida {

    private String partidaID;
    private String jugador1ID;
    private String jugador2ID;
    private boolean jugador1Ready;
    private boolean jugador2Ready;
    private boolean jugando;
    private String turno;
    private Tablero tablero;
    private String ganador;

    public Partida(String partidaID, String jugador1ID, String jugador2ID, Tablero tablero) {
        this.partidaID = partidaID;
        this.jugador1ID = jugador1ID;
        this.jugador2ID = jugador2ID;
        this.tablero = tablero;
        this.turno = this.jugador1ID;
        this.ganador = "-";
        this.jugando = false;
        this.jugador1Ready = false;
        this.jugador2Ready = false;

    }

    public Partida() {
        this.partidaID = null;
        this.jugador1ID = null;
        this.jugador2ID = null;
        this.tablero = new Tablero();
        this.turno = null;
        this.ganador = null;
        this.jugando = false;
        this.jugador1Ready = false;
        this.jugador2Ready = false;
    }

    public boolean isJugador1Ready() {
        return jugador1Ready;
    }

    public void setJugador1Ready(boolean jugador1Ready) {
        this.jugador1Ready = jugador1Ready;
    }

    public boolean isJugador2Ready() {
        return jugador2Ready;
    }

    public void setJugador2Ready(boolean jugador2Ready) {
        this.jugador2Ready = jugador2Ready;
    }

    public boolean isJugando() {
        return jugando;
    }

    public void setJugando(boolean jugando) {
        this.jugando = jugando;
    }

    public String getPartidaID() {
        return partidaID;
    }

    public void setPartidaID(String partidaID) {
        this.partidaID = partidaID;
    }

    public String getJugador1ID() {
        return jugador1ID;
    }

    public void setJugador1ID(String jugador1ID) {
        this.jugador1ID = jugador1ID;
    }

    public String getJugador2ID() {
        return jugador2ID;
    }

    public void setJugador2ID(String jugador2ID) {
        this.jugador2ID = jugador2ID;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    public Tablero getTablero() {
        return tablero;
    }

    public void setTablero(Tablero tablero) {
        this.tablero = tablero;
    }

    public String getGanador() {
        return ganador;
    }

    public void setGanador(String ganador) {
        this.ganador = ganador;
    }
}
