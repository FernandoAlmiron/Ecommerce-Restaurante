package org.example.dto;

import java.util.List;

public class EnvioDTO {
    private int idCliente;
    private int nroRestaurante;
    private String direccionEntrega;
    private String nombreReceptor;
    private List<PedidoDTO> pedidos;

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public int getNroRestaurante() { return nroRestaurante; }
    public void setNroRestaurante(int nroRestaurante) { this.nroRestaurante = nroRestaurante; }
    public String getDireccionEntrega() { return direccionEntrega; }
    public void setDireccionEntrega(String direccionEntrega) { this.direccionEntrega = direccionEntrega; }
    public String getNombreReceptor() { return nombreReceptor; }
    public void setNombreReceptor(String nombreReceptor) { this.nombreReceptor = nombreReceptor; }
    public List<PedidoDTO> getPedidos() { return pedidos; }
    public void setPedidos(List<PedidoDTO> pedidos) { this.pedidos = pedidos; }

    public static class PedidoDTO {
        private int idMenu;
        private int cantidad;
        private String observaciones;

        public int getIdMenu() { return idMenu; }
        public void setIdMenu(int idMenu) { this.idMenu = idMenu; }
        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }
        public String getObservaciones() { return observaciones; }
        public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    }
}
