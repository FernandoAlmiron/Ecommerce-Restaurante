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
        if (proveedor.getCuit() == null || !proveedor.getCuit().matches("\\d{11}")) {
            throw new IllegalArgumentException("El CUIT debe tener 11 digitos, sin guiones");
        }
        return proveedoresRepository.save(proveedor);
    }

    public Proveedores buscarPorId(int nroProveedor) {
        return proveedoresRepository.findById(nroProveedor)
                .orElseThrow(() -> new java.util.NoSuchElementException("Proveedor no encontrado"));
    }

    public List<Proveedores> listarTodos() {
        return proveedoresRepository.findAll();
    }

}
