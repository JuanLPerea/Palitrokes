package com.game.palitrokes.Modelos;

public class Records implements Comparable<Records> {

    private String idJugador;
    private String nickname;
    private int victorias;
    private int level;


    public Records(String idJugador, String nickname, int victorias, int level) {
        this.idJugador = idJugador;
        this.nickname = nickname;
        this.victorias = victorias;
        this.level = level;
    }

    public Records() {
    }


    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getIdJugador() {
        return idJugador;
    }

    public void setIdJugador(String idJugador) {
        this.idJugador = idJugador;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getVictorias() {
        return victorias;
    }

    public void setVictorias(int victorias) {
        this.victorias = victorias;
    }

    @Override
    public int compareTo(Records o) {

        // Ordenar por nivel y luego por victorias
        if (this.level > o.level ) {
            return -1;
        } else if (this.level == o.level) {
            if (this.victorias > o.victorias) {
                return -1;
            } else {
                return 1;
            }
        } else {
            return 1;
        }


    }
}
