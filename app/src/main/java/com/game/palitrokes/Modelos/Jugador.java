package com.game.palitrokes.Modelos;

public class Jugador {

    private String jugadorId;
    private String rivalId;
    private String nickname;
    private String partida;
    private int victorias;
    private int derrotas;
    private boolean online;
    private boolean turno;
    private boolean empezarPartida;
    private String actualizado;
    private int numeroJugador;

    public Jugador(String jugadorId, String nickname) {
        this.jugadorId = jugadorId;
        this.nickname = nickname;
        this.partida = "";
        this.victorias = 0;
        this.derrotas = 0;
        this.online = true;
        this.turno = false;
        this.empezarPartida = false;
        this.actualizado = System.currentTimeMillis() + "*";
        this.numeroJugador = 0;
    }

    public Jugador() {
    }

    public int getNumeroJugador() {
        return numeroJugador;
    }

    public void setNumeroJugador(int numeroJugador) {
        this.numeroJugador = numeroJugador;
    }

    public boolean isEmpezarPartida() {
        return empezarPartida;
    }

    public void setEmpezarPartida(boolean empezarPartida) {
        this.empezarPartida = empezarPartida;
    }

    public String getRivalId() {
        return rivalId;
    }

    public void setRivalId(String rivalId) {
        this.rivalId = rivalId;
    }


    public String getJugadorId() {
        return jugadorId;
    }

    public void setJugadorId(String jugadorId) {
        this.jugadorId = jugadorId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPartida() {
        return partida;
    }

    public void setPartida(String partida) {
        this.partida = partida;
    }

    public int getVictorias() {
        return victorias;
    }

    public void setVictorias(int victorias) {
        this.victorias = victorias;
    }

    public int getDerrotas() {
        return derrotas;
    }

    public void setDerrotas(int derrotas) {
        this.derrotas = derrotas;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public boolean isTurno() {
        return turno;
    }

    public void setTurno(boolean turno) {
        this.turno = turno;
    }

    public String getActualizado() {
        return actualizado;
    }

    public void setActualizado(String actualizado) {
        this.actualizado = actualizado;
    }
}
