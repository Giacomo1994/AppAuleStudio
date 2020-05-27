package com.example.appaulestudio;

public class AlarmClass {
    private int id_prenotazione;
    private String orario_alarm;

    public AlarmClass(int id_prenotazione, String orario_alarm) {
        this.id_prenotazione = id_prenotazione;
        this.orario_alarm = orario_alarm;
    }

    public int getId_prenotazione() {
        return id_prenotazione;
    }

    public void setId_prenotazione(int id_prenotazione) {
        this.id_prenotazione = id_prenotazione;
    }

    public String getOrario_alarm() {
        return orario_alarm;
    }

    public void setOrario_alarm(String orario_alarm) {
        this.orario_alarm = orario_alarm;
    }
}
