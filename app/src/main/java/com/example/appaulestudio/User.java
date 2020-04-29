package com.example.appaulestudio;

public class User {
    String matricola;
    String universita;
    String password;
    String email;
    String email_calendar;
    String nome;
    String cognome;
    boolean studente;

    public User(String matricola,String nome, String cognome, String universita, String email, String password, boolean studente, String email_calendar) {
        this.matricola = matricola;
        this.universita = universita;
        this.email=email;
        this.password = password;
        this.studente = studente;
        this.email_calendar=email_calendar;
        this.nome=nome;
        this.cognome=cognome;

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
