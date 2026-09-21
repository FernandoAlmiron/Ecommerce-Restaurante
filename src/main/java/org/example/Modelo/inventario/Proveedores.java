package org.example.Modelo.inventario;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Proveedores {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int nroProveedor;
    @Column(unique = true, nullable = false, length = 11)
    private String cuit;
    private String telefono;
    private String email;
    private String razonSocial;
    @ManyToOne
    @JoinColumn(name = "nroCategoria")
    private Categoria categoria;
}
