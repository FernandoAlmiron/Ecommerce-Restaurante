package org.example.Modelo.menu;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.example.Modelo.inventario.Stock;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Getter @Setter @NoArgsConstructor
public class MenuIngrediente {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idMenuIngrediente;
    @ManyToOne
    @JoinColumn(name = "idMenu")
    @JsonBackReference
    private Menu menu;
    @ManyToOne
    @JoinColumn(name = "idStock")
    private Stock stock;
    private double cantidadNecesaria;

    public double calcularCantidadADescontar(int cantidadPedida) {
        return cantidadNecesaria * cantidadPedida;
    }
}
