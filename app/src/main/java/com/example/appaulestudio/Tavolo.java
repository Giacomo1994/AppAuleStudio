package com.example.appaulestudio;

public class Tavolo {
    private String id_aula;
    private int num_tavolo;
    private int posti_totali;
    private int posti_liberi;

    public Tavolo(String id_aula, int numero_tavolo, int posti_totali, int posti_liberi) {
        this.id_aula = id_aula;
        this.num_tavolo = numero_tavolo;
        this.posti_totali = posti_totali;
        this.posti_liberi = posti_liberi;
    }

    public String getId_aula() {
        return id_aula;
    }

    public void setId_aula(String id_aula) {
        this.id_aula = id_aula;
    }

    public int getNum_tavolo() {
        return num_tavolo;
    }

    public void setNum_tavolo(int num_tavolo) {
        this.num_tavolo = num_tavolo;
    }

    public int getPosti_totali() {
        return posti_totali;
    }

    public void setPosti_totali(int posti_totali) {
        this.posti_totali = posti_totali;
    }

    public int getPosti_liberi() {
        return posti_liberi;
    }

    public void setPosti_liberi(int posti_liberi) {
        this.posti_liberi = posti_liberi;
    }

    @Override
    public String toString() {
        return "Tavolo " + num_tavolo;
    }
}
