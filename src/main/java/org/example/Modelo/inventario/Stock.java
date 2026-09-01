package org.example.Modelo.inventario;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.Modelo.Restaurante;

@Entity
@Getter @Setter @NoArgsConstructor
public class Stock {
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int idStock;
    private String nombre;
    private String unidadMedida;
    private double cantidadActual;
    private double stockMinimo;
    private boolean stockMinimoActivo;
    @ManyToOne
    @JoinColumn(name = "nroRestaurante")
    @JsonBackReference
    private Restaurante restaurante;
    @ManyToOne
    @JoinColumn(name = "nroCategoria")
    private Categoria categoria;

    public void aumentarStock(double cantidad){
        cantidadActual+=cantidad;
    }
    public void disminuirStock(double cantidad){
        if(hayStockSuficiente(cantidad)){
            cantidadActual-=cantidad;
        }else {
            System.out.println("No hay stock suficiente de "+nombre);
        }
    }
    public boolean hayStockSuficiente(double cantidad){
        return cantidadActual >= cantidad;
    }
    public boolean alcanzoStockMinimo(){
        return stockMinimoActivo && cantidadActual <= stockMinimo;
    }


}
