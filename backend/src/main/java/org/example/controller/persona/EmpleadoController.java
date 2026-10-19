package org.example.controller.persona;

import org.example.Modelo.persona.Empleado;
import org.example.Modelo.persona.Sector;
import org.example.service.persona.EmpleadoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    public EmpleadoController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    @GetMapping
    public List<Empleado> listarTodos() {
        return empleadoService.listarTodos();
    }

    @GetMapping("/{id}")
    public Empleado buscarPorId(@PathVariable int id) {
        return empleadoService.buscarPorId(id);
    }

    @PostMapping
    public Empleado crear(@RequestBody Empleado empleado, Authentication authentication) {
        // el id lo genera la base: evita que un POST pise un registro existente
        empleado.setLegajo(0);
        return empleadoService.guardar(empleado, esAdmin(authentication));
    }

    @PutMapping("/{id}/cambiar-sector")
    public Empleado cambiarSector(@PathVariable int id, @RequestBody Sector nuevoSector,
                                  Authentication authentication) {
        return empleadoService.cambiarSector(id, nuevoSector, esAdmin(authentication));
    }

    @PutMapping("/{id}")
    public Empleado actualizar(@PathVariable int id, @RequestBody Empleado empleado,
                               Authentication authentication) {
        return empleadoService.actualizar(id, empleado, esAdmin(authentication));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id, Authentication authentication) {
        empleadoService.eliminar(id, esAdmin(authentication));
        return ResponseEntity.noContent().build();
    }

    private boolean esAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}