package com.example.appaulestudio;

public class Universita {
    private String codice;
    private String nome;
    private double latitudine;
    private double longitudine;
    private int ingresso;
    private int pausa;
    private int slot;
    private String frist_slot;
    private String url_registrazione;
    private String url_corsi;
    private String last_update;

    public Universita(String codice, String nome, double latitudine, double longitudine, int ingresso, int pausa, int slot, String frist_slot, String url_registrazione, String url_corsi) {
        this.codice = codice;
        this.nome = nome;
        this.latitudine = latitudine;
        this.longitudine = longitudine;
        this.ingresso = ingresso;
        this.pausa = pausa;
        this.slot = slot;
        this.frist_slot = frist_slot;
        this.url_registrazione = url_registrazione;
        this.url_corsi = url_corsi;
        last_update=null;
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

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public String getFirst_slot() {
        return frist_slot;
    }

    public void setFirst_slot(String frist_slot) {
        this.frist_slot = frist_slot;
    }

    public String getUrl_registrazione() {
        return url_registrazione;
    }

    public void setUrl_registrazione(String url_registrazione) {
        this.url_registrazione = url_registrazione;
    }

    public String getUrl_corsi() {
        return url_corsi;
    }

    public void setUrl_corsi(String url_corsi) {
        this.url_corsi = url_corsi;
    }

    public String getFrist_slot() {
        return frist_slot;
    }

    public void setFrist_slot(String frist_slot) {
        this.frist_slot = frist_slot;
    }

    public String getLast_update() {
        return last_update;
    }

    public void setLast_update(String last_update) {
        this.last_update = last_update;
    }

    @Override
    public String toString() {
        return nome;
    }
}
