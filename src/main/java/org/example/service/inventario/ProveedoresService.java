package org.example.service.inventario;

import org.example.Modelo.inventario.Proveedores;
import org.example.repository.inventario.ProveedoresRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProveedoresService {

    private final ProveedoresRepository proveedoresRepository;

    public ProveedoresService(ProveedoresRepository proveedoresRepository) {
        this.proveedoresRepository = proveedoresRepository;
    }

    public Proveedores guardar(Proveedores proveedor) {
        return proveedoresRepository.save(proveedor);
    }

    public Proveedores buscarPorId(int cuit) {
        return proveedoresRepository.findById(cuit)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
    }

    public List<Proveedores> listarTodos() {
        return proveedoresRepository.findAll();
    }

}
