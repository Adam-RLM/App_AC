package com.example.assistcontrol;

public class Justificaciones {
    private String Año;
    private String Mes;
    private String Dia;
    private String Justificacion;
    private String Hora;

    public Justificaciones(){

    }

    public Justificaciones(String anio, String mes, String dia, String justificacion, String hora) {
        Año = anio;
        Mes = mes;
        Dia = dia;
        Justificacion = justificacion;
        Hora = hora;
    }

    public String getAño() {
        return Año;
    }

    public void setAño(String anio) {
        Año = anio;
    }

    public String getMes() {
        return Mes;
    }

    public void setMes(String mes) {
        Mes = mes;
    }

    public String getDia() {
        return Dia;
    }

    public void setDia(String dia) {
        Dia = dia;
    }

    public String getJustificacion() {
        return Justificacion;
    }

    public void setJustificacion(String justificacion) {
        Justificacion = justificacion;
    }

    public String getHora() {
        return Hora;
    }

    public void setHora(String hora) {
        Hora = hora;
    }
}
