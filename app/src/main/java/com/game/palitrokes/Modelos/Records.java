package com.game.palitrokes.Modelos;

public class Records {

    private String idJugador;
    private String nickname;
    private String victorias;


    public Records(String idJugador, String nickname, String victorias) {
        this.idJugador = idJugador;
        this.nickname = nickname;
        this.victorias = victorias;
    }

    public Records() {
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

    public String getVictorias() {
        return victorias;
    }

    public void setVictorias(String victorias) {
        this.victorias = victorias;
    }


}
