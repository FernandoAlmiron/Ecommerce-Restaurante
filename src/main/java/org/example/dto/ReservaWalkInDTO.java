package org.example.dto;

public class ReservaWalkInDTO {
    private Integer idCliente; // null si es cliente nuevo
    private String nombre;
    private String apellido;
    private int celular;
    private int nroRestaurante;
    private int idZona;
    private int cantComensales;

    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public int getCelular() { return celular; }
    public void setCelular(int celular) { this.celular = celular; }
    public int getNroRestaurante() { return nroRestaurante; }
    public void setNroRestaurante(int nroRestaurante) { this.nroRestaurante = nroRestaurante; }
    public int getIdZona() { return idZona; }
    public void setIdZona(int idZona) { this.idZona = idZona; }
    public int getCantComensales() { return cantComensales; }
    public void setCantComensales(int cantComensales) { this.cantComensales = cantComensales; }
}
