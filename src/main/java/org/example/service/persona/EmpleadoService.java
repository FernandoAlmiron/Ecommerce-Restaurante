package org.example.service.persona;

import org.example.Modelo.persona.Empleado;
import org.example.Modelo.persona.Sector;
import org.example.repository.persona.EmpleadoRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final PasswordEncoder passwordEncoder;

    public EmpleadoService(EmpleadoRepository empleadoRepository, PasswordEncoder passwordEncoder) {
        this.empleadoRepository = empleadoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Empleado guardar(Empleado empleado) {
        empleado.setPassword(passwordEncoder.encode(empleado.getPassword()));
        return empleadoRepository.save(empleado);
    }

    public Empleado buscarPorId(int legajo) {
        return empleadoRepository.findById(legajo)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
    }

    public List<Empleado> listarTodos() {
        return empleadoRepository.findAll();
    }

    public Empleado cambiarSector(int legajo, Sector nuevoSector) {
        Empleado empleado = buscarPorId(legajo);
        empleado.cambiarSector(nuevoSector);
        return empleadoRepository.save(empleado);
    }
}
