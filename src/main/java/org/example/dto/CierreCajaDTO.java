package org.example.dto;

public class CierreCajaDTO {
    private int nroRestaurante;
    private int legajoCajera;
    private double montoContado;
    private String observaciones;

    public int getNroRestaurante() { return nroRestaurante; }
    public void setNroRestaurante(int nroRestaurante) { this.nroRestaurante = nroRestaurante; }
    public int getLegajoCajera() { return legajoCajera; }
    public void setLegajoCajera(int legajoCajera) { this.legajoCajera = legajoCajera; }
    public double getMontoContado() { return montoContado; }
    public void setMontoContado(double montoContado) { this.montoContado = montoContado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
