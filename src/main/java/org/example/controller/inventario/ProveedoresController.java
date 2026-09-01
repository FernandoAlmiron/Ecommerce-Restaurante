package org.example.controller.inventario;

import org.example.Modelo.inventario.Proveedores;
import org.example.service.inventario.ProveedoresService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedoresController {

    private final ProveedoresService proveedoresService;

    public ProveedoresController(ProveedoresService proveedoresService) {
        this.proveedoresService = proveedoresService;
    }

    @GetMapping
    public List<Proveedores> listarTodos() {
        return proveedoresService.listarTodos();
    }

    @GetMapping("/{id}")
    public Proveedores buscarPorId(@PathVariable int id) {
        return proveedoresService.buscarPorId(id);
    }

    @PostMapping
    public Proveedores crear(@RequestBody Proveedores proveedores) {
        return proveedoresService.guardar(proveedores);
    }
}
