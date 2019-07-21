package com.game.palitrokes.Modelos;

import java.util.ArrayList;
import java.util.List;

public class Jugador {

    private String jugadorId;
    private String nickname;
    private String partida;
    private int victorias;
    private int derrotas;
    private boolean online;
    private String actualizado;
    private int numeroJugador;
    private boolean firstRun;
    private List<String> favoritosID;

    public Jugador(String jugadorId, String nickname) {
        this.jugadorId = jugadorId;
        this.nickname = nickname;
        this.partida = "";
        this.victorias = 0;
        this.derrotas = 0;
        this.online = false;
        this.actualizado = System.currentTimeMillis() + "";
        this.numeroJugador = 0;
        this.firstRun = true;
        this.favoritosID = new ArrayList<>();
    }

    public Jugador() {
    }

    public int getNumeroJugador() {
        return numeroJugador;
    }

    public void setNumeroJugador(int numeroJugador) {
        this.numeroJugador = numeroJugador;
    }

    public List<String> getFavoritosID() {
        return favoritosID;
    }

    public void setFavoritosID(List<String> favoritosID) {
        this.favoritosID = favoritosID;
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
        this.victorias += victorias;
    }

    public int getDerrotas() {
        return derrotas;
    }

    public void setDerrotas(int derrotas) {
        this.derrotas += derrotas;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public boolean isFirstRun() {
        return firstRun;
    }

    public void setFirstRun(boolean firstRun) {
        this.firstRun = firstRun;
    }

    public String getActualizado() {
        return actualizado;
    }

    public void setActualizado(String actualizado) {
        this.actualizado = actualizado;
    }
}
