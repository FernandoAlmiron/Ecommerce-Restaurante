package org.example.controller.reserva;

import org.example.Modelo.reserva.Reserva;
import org.example.dto.ReservaWalkInDTO;
import org.example.service.reserva.ReservaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping
    public List<Reserva> listarTodas() {
        return reservaService.listarTodos();
    }

    @GetMapping("/{id}")
    public Reserva buscarPorId(@PathVariable int id) {
        return reservaService.buscarPorId(id);
    }

    @PostMapping
    public Reserva crear(@RequestBody Reserva reserva) {
        return reservaService.guardar(reserva);
    }

    @PutMapping("/{id}/confirmar")
    public Reserva confirmar(@PathVariable int id) {
        return reservaService.confirmar(id);
    }

    @PutMapping("/{id}/cancelar")
    public Reserva cancelar(@PathVariable int id) {
        return reservaService.cancelar(id);
    }

    @PutMapping("/{id}/asistencia")
    public Reserva marcarAsistencia(@PathVariable int id, @RequestParam boolean asistio) {
        return reservaService.marcarAsistencia(id, asistio);
    }

    @PostMapping("/walkin")
    public Reserva reservarWalkIn(@RequestBody ReservaWalkInDTO dto) {
        return reservaService.reservarWalkIn(dto);
    }
}
