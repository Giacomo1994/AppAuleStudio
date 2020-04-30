package com.example.appaulestudio;

public class Orario_Ufficiale implements Comparable<Orario_Ufficiale>{
    private String data;
    private String giorno;
    private String apertura;
    private String chiusura;
    private String riapertura_intermedia;
    private String chiusura_intermedia;


    public Orario_Ufficiale(String data, String giorno, String apertura, String chiusura_intermedia, String riapertura_intermedia, String chiusura) {
        this.data = data;
        this.giorno = giorno;
        this.apertura = apertura;
        this.chiusura = chiusura;
        this.chiusura_intermedia=chiusura_intermedia;
        this.riapertura_intermedia=riapertura_intermedia;
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

    public String getRiapertura_intermedia() {
        return riapertura_intermedia;
    }

    public void setRiapertura_intermedia(String riapertura_intermedia) {
        this.riapertura_intermedia = riapertura_intermedia;
    }

    public String getChiusura_intermedia() {
        return chiusura_intermedia;
    }

    public void setChiusura_intermedia(String chiusura_intermedia) {
        this.chiusura_intermedia = chiusura_intermedia;
    }

    @Override
    public int compareTo(Orario_Ufficiale o) {
        return this.getData().compareTo(o.getData());
    }
}
