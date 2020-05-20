package com.example.appaulestudio;

public class Universita {
    private String codice;
    private String nome;
    private double latitudine;
    private double longitudine;
    private int ingresso;
    private int pausa;

    public Universita(String codice, String nome, double latitudine, double longitudine, int ingresso, int pausa) {
        this.codice = codice;
        this.nome = nome;
        this.latitudine = latitudine;
        this.longitudine = longitudine;
        this.ingresso = ingresso;
        this.pausa = pausa;
    }

    public String getCodice() {
        return codice;
    }

    public void setCodice(String codice) {
        this.codice = codice;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getLatitudine() {
        return latitudine;
    }

    public void setLatitudine(double latitudine) {
        this.latitudine = latitudine;
    }

    public double getLongitudine() {
        return longitudine;
    }

    public void setLongitudine(double longitudine) {
        this.longitudine = longitudine;
    }

    public int getIngresso() {
        return ingresso;
    }

    public void setIngresso(int ingresso) {
        this.ingresso = ingresso;
    }

    public int getPausa() {
        return pausa;
    }

    public void setPausa(int pausa) {
        this.pausa = pausa;
    }

    @Override
    public String toString() {
        return nome;
    }
}
