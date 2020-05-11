package com.example.appaulestudio;

public class User {
    private String matricola;
    private String universita;
    private String password;
    private String email;
    private String nome;
    private String cognome;
    private boolean studente;

    public User(String matricola,String nome, String cognome, String universita, String email, String password, boolean studente) {
        this.matricola = matricola;
        this.universita = universita;
        this.email=email;
        this.password = password;
        this.studente = studente;
        this.nome=nome;
        this.cognome=cognome;

    }

    public String getMatricola() {
        return matricola;
    }

    public void setMatricola(String matricola) {
        this.matricola = matricola;
    }

    public String getUniversita() {
        return universita;
    }

    public void setUniversita(String universita) {
        this.universita = universita;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public boolean isStudente() {
        return studente;
    }

    public void setStudente(boolean studente) {
        this.studente = studente;
    }

    @Override
    public String toString() {
        return "User{" +
                "matricola='" + matricola + '\'' +
                ", universita='" + universita + '\'' +
                ", password='" + password + '\'' +
                ", studente=" + studente +
                '}';
    }
}
