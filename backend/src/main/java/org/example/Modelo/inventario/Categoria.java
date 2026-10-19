package org.example.Modelo.inventario;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Categoria {
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int nroCategoria;
    private String nombre;
    private String descripcion;
    private boolean activo;
}
