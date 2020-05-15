package com.example.appaulestudio;

public class Prenotazione implements Comparable<Prenotazione>{
    private int id_prenotazione;
    private String matricola;
    private String nome_aula;
    private String id_aula;
    private int num_tavolo;
    private String orario_prenotazione;
    private String orario_ultima_uscita;
    private String orario_fine_prenotazione;
    private int stato;
    private String gruppo;
    private String in_corso;

    public Prenotazione(int id_prenotazione, String matricola, String id_aula, String nome_aula, int num_tavolo, String orario_prenotazione, String orario_ultima_uscita, String orario_fine_prenotazione, int stato, String gruppo, String in_corso) {
        this.id_prenotazione = id_prenotazione;
        this.matricola = matricola;
        this.nome_aula = nome_aula;
        this.num_tavolo = num_tavolo;
        this.orario_prenotazione = orario_prenotazione;
        this.orario_ultima_uscita = orario_ultima_uscita;
        this.orario_fine_prenotazione = orario_fine_prenotazione;
        this.stato = stato;
        this.gruppo = gruppo;
        this.in_corso=in_corso;
        this.id_aula=id_aula;
    }

    public String getId_aula() {
        return id_aula;
    }

    public void setId_aula(String id_aula) {
        this.id_aula = id_aula;
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

    public String getAula() {
        return nome_aula;
    }

    public void setAula(String aula) {
        this.nome_aula = aula;
    }

    public int getNum_tavolo() {
        return num_tavolo;
    }

    public void setNum_tavolo(int num_tavolo) {
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

    public String getGruppo() {
        return gruppo;
    }

    public void setGruppo(String gruppo) {
        this.gruppo = gruppo;
    }

    public String getIn_corso() {
        return in_corso;
    }

    public void setIn_corso(String in_corso) {
        this.in_corso = in_corso;
    }

    @Override
    public int compareTo(Prenotazione o) {
        return orario_prenotazione.compareTo(o.getOrario_prenotazione());
    }
}
