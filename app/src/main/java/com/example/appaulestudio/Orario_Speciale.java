package com.example.appaulestudio;

public class Orario_Speciale {
    private String data, apertura, chiusura;

    public Orario_Speciale(String data, String apertura, String chiusura) {
        this.data = data;
        this.apertura = apertura;
        this.chiusura = chiusura;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getApertura() {
        return apertura;
    }

    public void setApertura(String apertura) {
        this.apertura = apertura;
    }

    public String getChiusura() {
        return chiusura;
    }

    public void setChiusura(String chiusura) {
        this.chiusura = chiusura;
    }
}
