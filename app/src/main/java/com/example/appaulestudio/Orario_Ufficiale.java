package com.example.appaulestudio;

public class Orario_Ufficiale implements Comparable<Orario_Ufficiale>{
    private String data;
    private String giorno;
    private String apertura;
    private String chiusura;



    public Orario_Ufficiale(String data, String giorno, String apertura, String chiusura) {
        this.data = data;
        this.giorno = giorno;
        this.apertura = apertura;
        this.chiusura = chiusura;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getGiorno() {
        return giorno;
    }

    public void setGiorno(String giorno) {
        this.giorno = giorno;
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

    @Override
    public int compareTo(Orario_Ufficiale o) {
        return this.getData().compareTo(o.getData());
    }
}
