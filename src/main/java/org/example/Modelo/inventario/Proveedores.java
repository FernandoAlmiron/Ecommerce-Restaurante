package org.example.Modelo.inventario;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Proveedores {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int cuit;
    private int nroProveedor;
    private int telefono;
    private String email;
    private String razonSocial;
    @ManyToOne
    @JoinColumn(name = "nroCategoria")
    private Categoria categoria;

}
