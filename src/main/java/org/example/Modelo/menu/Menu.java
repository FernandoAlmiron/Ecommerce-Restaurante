package org.example.Modelo.menu;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.Modelo.Restaurante;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter @Setter @NoArgsConstructor
public class Menu {
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int idMenu;
    @ManyToOne
    @JoinColumn(name = "nroRestaurante")
    private Restaurante restaurante;
    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL)
    @JsonManagedReference
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<MenuIngrediente> ingredientes;
    private String nombre;
    private String descripcion;
    @Column(precision = 10, scale = 2)
    private BigDecimal precio;
    private boolean disponible;

    public boolean estaDisponible(){
        return disponible;
    }
    public void marcarDisponible(){
        this.disponible=true;
    }
    public void marcarNoDisponible(){
        this.disponible=false;
    }
}
