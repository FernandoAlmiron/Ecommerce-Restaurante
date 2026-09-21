package org.example.service.persona;

import org.example.Modelo.persona.Empleado;
import org.example.Modelo.persona.Sector;
import org.example.repository.persona.EmpleadoRepository;
import org.example.repository.persona.SectorRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final SectorRepository sectorRepository;
    private final PasswordEncoder passwordEncoder;

    public EmpleadoService(EmpleadoRepository empleadoRepository, SectorRepository sectorRepository,
                           PasswordEncoder passwordEncoder) {
        this.empleadoRepository = empleadoRepository;
        this.sectorRepository = sectorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // quienGuardaEsAdmin: si quien hace el pedido es ADMIN (lo calcula el controller con el login)
    public Empleado guardar(Empleado empleado, boolean quienGuardaEsAdmin) {
        if (empleado.getUsername() == null || empleado.getUsername().isBlank()) {
            throw new IllegalArgumentException("El username es obligatorio");
        }
        if (empleado.getPassword() == null || empleado.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        if (empleado.getSector() == null) {
            throw new IllegalArgumentException("El empleado necesita un sector");
        }
        Sector sector = sectorRepository.findById(empleado.getSector().getIdSector())
                .orElseThrow(() -> new IllegalArgumentException("Sector no encontrado"));
        exigirAdminSiEsAdministracion(sector, quienGuardaEsAdmin);

        empleado.setSector(sector);
        empleado.setPassword(passwordEncoder.encode(empleado.getPassword()));
        return empleadoRepository.save(empleado);
    }

    public Empleado buscarPorId(int legajo) {
        return empleadoRepository.findById(legajo)
                .orElseThrow(() -> new NoSuchElementException("Empleado no encontrado"));
    }

    public List<Empleado> listarTodos() {
        return empleadoRepository.findAll();
    }

    public Empleado cambiarSector(int legajo, Sector nuevoSector, boolean quienCambiaEsAdmin) {
        Empleado empleado = buscarPorId(legajo);
        Sector sector = sectorRepository.findById(nuevoSector.getIdSector())
                .orElseThrow(() -> new IllegalArgumentException("Sector no encontrado"));
        exigirAdminSiEsAdministracion(empleado.getSector(), quienCambiaEsAdmin);  // no se le puede sacar el cargo a un admin
        exigirAdminSiEsAdministracion(sector, quienCambiaEsAdmin);                // ni se puede nombrar a uno nuevo
        empleado.cambiarSector(sector);
        return empleadoRepository.save(empleado);
    }

    // Sin esto, RRHH podria crearse (o ascender a alguien) al sector ADMINISTRACION y quedar como admin
    private void exigirAdminSiEsAdministracion(Sector sector, boolean esAdmin) {
        if (!esAdmin && sector != null && "ADMINISTRACION".equalsIgnoreCase(sector.getNombre())) {
            throw new AccessDeniedException("Solo un administrador puede tocar el sector ADMINISTRACION");
        }
    }
}