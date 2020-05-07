package com.example.appaulestudio;

public class Prenotazione {
    private int id_prenotazione;
    private String matricola;
    private String id_aula;
    private String num_tavolo;
    private String orario_prenotazione;
    private String orario_ultima_uscita;
    private String orario_fine_prenotazione;
    private int stato;

    public Prenotazione(int id_prenotazione, String matricola, String id_aula, String num_tavolo, String orario_prenotazione, String orario_ultima_uscita, String orario_fine_prenotazione, int stato) {
        this.id_prenotazione = id_prenotazione;
        this.matricola = matricola;
        this.id_aula = id_aula;
        this.num_tavolo = num_tavolo;
        this.orario_prenotazione = orario_prenotazione;
        this.orario_ultima_uscita = orario_ultima_uscita;
        this.orario_fine_prenotazione = orario_fine_prenotazione;
        this.stato = stato;
    }

    public int getId_prenotazione() {
        return id_prenotazione;
    }

    public void setId_prenotazione(int id_prenotazione) {
        this.id_prenotazione = id_prenotazione;
    }

    public String getMatricola() {
        return matricola;
    }

    public void setMatricola(String matricola) {
        this.matricola = matricola;
    }

    public String getId_aula() {
        return id_aula;
    }

    public void setId_aula(String id_aula) {
        this.id_aula = id_aula;
    }

    public String getNum_tavolo() {
        return num_tavolo;
    }

    public void setNum_tavolo(String num_tavolo) {
        this.num_tavolo = num_tavolo;
    }

    public String getOrario_prenotazione() {
        return orario_prenotazione;
    }

    public void setOrario_prenotazione(String orario_prenotazione) {
        this.orario_prenotazione = orario_prenotazione;
    }

    public String getOrario_ultima_uscita() {
        return orario_ultima_uscita;
    }

    public void setOrario_ultima_uscita(String orario_ultima_uscita) {
        this.orario_ultima_uscita = orario_ultima_uscita;
    }

    public String getOrario_fine_prenotazione() {
        return orario_fine_prenotazione;
    }

    public void setOrario_fine_prenotazione(String orario_fine_prenotazione) {
        this.orario_fine_prenotazione = orario_fine_prenotazione;
    }

    public int getStato() {
        return stato;
    }

    public void setStato(int stato) {
        this.stato = stato;
    }
}
