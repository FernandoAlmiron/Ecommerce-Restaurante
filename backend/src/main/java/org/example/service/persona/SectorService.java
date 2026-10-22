package org.example.service.persona;

import org.example.Modelo.Restaurante;
import org.example.Modelo.persona.Sector;
import org.example.repository.persona.EmpleadoRepository;
import org.example.repository.persona.SectorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SectorService {

    private final SectorRepository sectorRepository;
    private final EmpleadoRepository empleadoRepository;

    public SectorService(SectorRepository sectorRepository, EmpleadoRepository empleadoRepository) {
        this.sectorRepository = sectorRepository;
        this.empleadoRepository = empleadoRepository;
    }

    public Sector guardar(Sector sector) {
        validarNombre(sector.getNombre(), sector.getRestaurante(), sector.getIdSector());
        sector.setNombre(sector.getNombre().trim());
        return sectorRepository.save(sector);
    }

    public Sector buscarPorId(int idSector) {
        return sectorRepository.findById(idSector)
                .orElseThrow(() -> new java.util.NoSuchElementException("Sector no encontrado"));
    }

    public List<Sector> listarTodos() {
        return sectorRepository.findAll();
    }

    public Sector activar(int idSector) {
        Sector sector = buscarPorId(idSector);
        sector.activar();
        return sectorRepository.save(sector);
    }

    public Sector desactivar(int idSector) {
        Sector sector = buscarPorId(idSector);
        if ("ADMINISTRACION".equalsIgnoreCase(sector.getNombre())) {
            throw new IllegalStateException("El sector ADMINISTRACION no se puede desactivar: es el de los administradores");
        }
        sector.desactivar();
        return sectorRepository.save(sector);
    }
    /** Cambia solo el nombre: activar y desactivar tienen sus propios métodos. */
    public Sector actualizar(int idSector, Sector datos) {
        Sector actual = buscarPorId(idSector);
        String nombre = datos.getNombre() == null ? "" : datos.getNombre().trim();
        if (actual.esDelSistema() && !nombre.equalsIgnoreCase(actual.getNombre())) {
            throw new IllegalStateException("El sector " + actual.getNombre()
                    + " es del sistema (de él salen los permisos) y no se puede renombrar");
        }
        validarNombre(nombre, actual.getRestaurante(), idSector);
        actual.setNombre(nombre);
        return sectorRepository.save(actual);
    }

    public void eliminar(int idSector) {
        Sector sector = buscarPorId(idSector);   // 404 si no existe
        if (sector.esDelSistema()) {
            throw new IllegalStateException("El sector " + sector.getNombre()
                    + " es del sistema (de él salen los permisos) y no se puede eliminar");
        }
        long empleados = empleadoRepository.countBySector_IdSector(idSector);
        if (empleados > 0) {
            throw new IllegalStateException("El sector " + sector.getNombre() + " tiene " + empleados
                    + (empleados == 1 ? " empleado" : " empleados")
                    + ": pasalos a otro sector o desactivalo en lugar de eliminarlo");
        }
        sectorRepository.deleteById(idSector);
    }

    /** Obligatorio y sin repetir dentro del restaurante (sin importar mayúsculas). */
    private void validarNombre(String nombre, Restaurante restaurante, int idPropio) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del sector es obligatorio");
        }
        if (restaurante != null && sectorRepository.existsByNombreIgnoreCaseAndRestaurante_NroRestauranteAndIdSectorNot(
                nombre.trim(), restaurante.getNroRestaurante(), idPropio)) {
            throw new IllegalStateException("Ya hay un sector llamado " + nombre.trim());
        }
    }

}
