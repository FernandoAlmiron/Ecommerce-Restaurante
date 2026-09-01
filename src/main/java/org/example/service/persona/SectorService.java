package org.example.service.persona;

import org.example.Modelo.persona.Sector;
import org.example.repository.persona.SectorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SectorService {

    private final SectorRepository sectorRepository;

    public SectorService(SectorRepository sectorRepository) {
        this.sectorRepository = sectorRepository;
    }

    public Sector guardar(Sector sector) {
        return sectorRepository.save(sector);
    }

    public Sector buscarPorId(int idSector) {
        return sectorRepository.findById(idSector)
                .orElseThrow(() -> new RuntimeException("Sector no encontrado"));
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
        sector.desactivar();
        return sectorRepository.save(sector);
    }
}
