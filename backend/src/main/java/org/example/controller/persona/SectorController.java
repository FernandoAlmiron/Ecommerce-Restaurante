package org.example.controller.persona;

import org.example.Modelo.persona.Sector;
import org.example.service.persona.SectorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sectores")
public class SectorController {

    private final SectorService sectorService;

    public SectorController(SectorService sectorService) {
        this.sectorService = sectorService;
    }

    @GetMapping
    public List<Sector> listarTodos() {
        return sectorService.listarTodos();
    }

    @GetMapping("/{id}")
    public Sector buscarPorId(@PathVariable int id) {
        return sectorService.buscarPorId(id);
    }

    @PostMapping
    public Sector crear(@RequestBody Sector sector) {
        sector.setIdSector(0);
        return sectorService.guardar(sector);
    }
    @PutMapping("/{id}")
    public Sector actualizar(@PathVariable int id, @RequestBody Sector sector) {
        return sectorService.actualizar(id, sector);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        sectorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
