package org.example.service.inventario;

import org.example.Modelo.inventario.Categoria;
import org.example.repository.inventario.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public Categoria guardar(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    public Categoria buscarPorId(int nroCategoria) {
        return categoriaRepository.findById(nroCategoria)
                .orElseThrow(() -> new java.util.NoSuchElementException("Categoria no encontrada"));
    }

    public List<Categoria> listarTodos() {
        return categoriaRepository.findAll();
    }

}
