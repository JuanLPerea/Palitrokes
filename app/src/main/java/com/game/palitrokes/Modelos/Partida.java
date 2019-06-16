package com.game.palitrokes.Modelos;

public class Partida {

    private String partidaID;
    private String jugador1ID;
    private String jugador2ID;
    private boolean jugador1Ready;
    private boolean jugador2Ready;
    private boolean jugando;
    private int turno;
    private Tablero tablero;
    private int ganador;
    private int numeroSala;
    private int level;

    public Partida(String partidaID, int numeroSala, String jugador1ID, String jugador2ID, Tablero tablero, int level) {
        this.partidaID = partidaID;
        this.jugador1ID = jugador1ID;
        this.jugador2ID = jugador2ID;
        this.tablero = tablero;
        this.turno = 0;
        this.ganador = 0;
        this.jugando = false;
        this.jugador1Ready = false;
        this.jugador2Ready = false;
        this.numeroSala = numeroSala;
        this.level = level;
    }

    public Partida() {
        this.partidaID = null;
        this.jugador1ID = null;
        this.jugador2ID = null;
        this.tablero = new Tablero(8);
        this.turno = 0;
        this.ganador = 0;
        this.jugando = false;
        this.jugador1Ready = false;
        this.jugador2Ready = false;
    }

    public int getLevel() {
        return level;
    }

    public void levelUp() {
        level++;
    }

    public int getNumeroSala() {
        return numeroSala;
    }

    public void setNumeroSala(int numeroSala) {
        this.numeroSala = numeroSala;
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

    public int getTurno() {
        return turno;
    }

    public void setTurno(int turno) {
        this.turno = turno;
    }

    public Tablero getTablero() {
        return tablero;
    }

    public void setTablero(Tablero tablero) {
        this.tablero = tablero;
    }

    public int getGanador() {
        return ganador;
    }

    public void setGanador(int ganador) {
        this.ganador = ganador;
    }

    public void turnoToggle(){
        if (this.getTurno()== 2)
        {
            this.setTurno(1);
        } else {
            this.setTurno(2);
        }
    }
}
