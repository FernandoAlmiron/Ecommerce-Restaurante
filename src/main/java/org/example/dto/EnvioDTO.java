package org.example.dto;

import org.example.Modelo.enums.MetodoPago;

import java.util.List;

public class EnvioDTO {
    private int idCliente;          // opcional: el cliente se identifica por su DNI
    private int dni;
    private String nombre;          // obligatorios solo si es la primera vez que pide
    private String apellido;
    private String celular;
    private String email;
    private int nroRestaurante;
    private String direccionEntrega;
    private MetodoPago metodoPago;  // EFECTIVO, TARJETA o QR
    private String nombreReceptor;
    private List<PedidoDTO> pedidos;

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public int getDni() { return dni; }
    public void setDni(int dni) { this.dni = dni; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getCelular() { return celular; }
    public void setCelular(String celular) { this.celular = celular; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public int getNroRestaurante() { return nroRestaurante; }
    public void setNroRestaurante(int nroRestaurante) { this.nroRestaurante = nroRestaurante; }
    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }
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
