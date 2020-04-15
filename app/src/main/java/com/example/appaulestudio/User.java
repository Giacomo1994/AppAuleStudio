package com.example.appaulestudio;

public class User {
    String matricola;
    String universita;
    String password;
    boolean studente;

    public User(String matricola, String universita, String password, boolean studente) {
        this.matricola = matricola;
        this.universita = universita;
        this.password = password;
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
