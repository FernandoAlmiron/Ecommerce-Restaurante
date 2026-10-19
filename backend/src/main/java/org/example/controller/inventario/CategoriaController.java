package org.example.controller.inventario;

import org.example.Modelo.inventario.Categoria;
import org.example.service.inventario.CategoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public List<Categoria> listarTodos() {
        return categoriaService.listarTodos();
    }

    @GetMapping("/{id}")
    public Categoria buscarPorId(@PathVariable int id) {
        return categoriaService.buscarPorId(id);
    }

    @PostMapping
    public Categoria crear(@RequestBody Categoria categoria) {
        categoria.setNroCategoria(0);
        return categoriaService.guardar(categoria);
    }
    @PutMapping("/{id}")
    public Categoria actualizar(@PathVariable int id, @RequestBody Categoria categoria) {
        return categoriaService.actualizar(id, categoria);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
