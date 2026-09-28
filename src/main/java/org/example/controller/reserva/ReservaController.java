package org.example.controller.reserva;

import org.example.Modelo.reserva.Reserva;
import org.example.dto.ReservaWalkInDTO;
import org.example.service.reserva.ReservaService;
import org.springframework.web.bind.annotation.*;
import org.example.dto.ReservaResponseDTO;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping
    public List<ReservaResponseDTO> listarTodas() {
        return reservaService.listarTodos().stream().map(ReservaResponseDTO::desde).toList();
    }

    @GetMapping("/{id}")
    public ReservaResponseDTO buscarPorId(@PathVariable int id) {
        return ReservaResponseDTO.desde(reservaService.buscarPorId(id));
    }

    @PostMapping
    public ReservaResponseDTO crear(@RequestBody Reserva reserva) {
        return ReservaResponseDTO.desde(reservaService.crear(reserva));
    }

    @PutMapping("/{id}/confirmar")
    public ReservaResponseDTO confirmar(@PathVariable int id) {
        return ReservaResponseDTO.desde(reservaService.confirmar(id));
    }

    @PutMapping("/{id}/cancelar")
    public ReservaResponseDTO cancelar(@PathVariable int id) {
        return ReservaResponseDTO.desde(reservaService.cancelar(id));
    }

    @PutMapping("/{id}/asistencia")
    public ReservaResponseDTO marcarAsistencia(@PathVariable int id, @RequestParam boolean asistio) {
        return ReservaResponseDTO.desde(reservaService.marcarAsistencia(id, asistio));
    }

    @PostMapping("/walkin")
    public ReservaResponseDTO reservarWalkIn(@RequestBody ReservaWalkInDTO dto) {
        return ReservaResponseDTO.desde(reservaService.reservarWalkIn(dto));
    }
}
