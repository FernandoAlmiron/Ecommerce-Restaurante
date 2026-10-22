package org.example.Modelo.persona;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.Modelo.Restaurante;

@Entity
@Getter @Setter @NoArgsConstructor
public class Sector {
    /**
     * Sectores que usa el login para dar los permisos (CustomUserDetailsService.mapearRol):
     * no se pueden renombrar ni eliminar, y ADMINISTRACION tampoco se puede desactivar.
     */
    public static final java.util.Set<String> DEL_SISTEMA = java.util.Set.of(
            "ADMINISTRACION", "COCINA", "SALON", "BARRA", "CAJA", "REPARTIDOR", "RECEPCION", "RECURSOSHUMANOS");

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idSector;
    private String nombre;
    private boolean activa;
    @ManyToOne
    @JoinColumn(name = "nroRestaurante")
    private Restaurante restaurante;
    public void activar(){
        activa=true;
    }
    public void desactivar(){
        activa=false;
    }

    public boolean esDelSistema() {
        return nombre != null && DEL_SISTEMA.contains(nombre.toUpperCase());
    }
}
