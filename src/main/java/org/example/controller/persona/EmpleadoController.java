package org.example.controller.persona;

import org.example.Modelo.persona.Empleado;
import org.example.Modelo.persona.Sector;
import org.example.service.persona.EmpleadoService;
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
    public Empleado crear(@RequestBody Empleado empleado) {
        return empleadoService.guardar(empleado);
    }

    @PutMapping("/{id}/cambiar-sector")
    public Empleado cambiarSector(@PathVariable int id, @RequestBody Sector nuevoSector) {
        return empleadoService.cambiarSector(id, nuevoSector);
    }
}