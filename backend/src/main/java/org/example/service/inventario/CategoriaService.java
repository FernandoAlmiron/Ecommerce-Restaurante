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
        validarNombre(categoria.getNombre(), categoria.getNroCategoria());
        categoria.setNombre(categoria.getNombre().trim());
        return categoriaRepository.save(categoria);
    }

    public Categoria buscarPorId(int nroCategoria) {
        return categoriaRepository.findById(nroCategoria)
                .orElseThrow(() -> new java.util.NoSuchElementException("Categoria no encontrada"));
    }

    public List<Categoria> listarTodos() {
        return categoriaRepository.findAll();
    }

    public Categoria actualizar(int nroCategoria, Categoria datos) {
        Categoria actual = buscarPorId(nroCategoria);
        validarNombre(datos.getNombre(), nroCategoria);
        actual.setNombre(datos.getNombre().trim());
        actual.setDescripcion(datos.getDescripcion());
        actual.setActivo(datos.isActivo());
        return categoriaRepository.save(actual);
    }

    /** Obligatorio y sin repetir (sin importar mayúsculas). */
    private void validarNombre(String nombre, int idPropio) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio");
        }
        if (categoriaRepository.existsByNombreIgnoreCaseAndNroCategoriaNot(nombre.trim(), idPropio)) {
            throw new IllegalStateException("Ya hay una categoría llamada " + nombre.trim());
        }
    }

    public void eliminar(int nroCategoria) {
        buscarPorId(nroCategoria);   // 404 si no existe
        categoriaRepository.deleteById(nroCategoria);   // si tiene datos asociados la base lo rechaza (409)
    }
}
