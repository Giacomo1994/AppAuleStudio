package com.example.appaulestudio;

public class CalendarEvent {
    private int id_prenotazione;
    private int id_calendar;
    private int id_event;

    public CalendarEvent(int id_prenotazione, int id_calendar, int id_event) {
        this.id_prenotazione = id_prenotazione;
        this.id_calendar = id_calendar;
        this.id_event = id_event;
    }

    public int getId_prenotazione() {
        return id_prenotazione;
    }

    public void setId_prenotazione(int id_prenotazione) {
        this.id_prenotazione = id_prenotazione;
    }

    public int getId_calendar() {
        return id_calendar;
    }

    public void setId_calendar(int id_calendar) {
        this.id_calendar = id_calendar;
    }

    public int getId_event() {
        return id_event;
    }

    public void setId_event(int id_event) {
        this.id_event = id_event;
    }
}
