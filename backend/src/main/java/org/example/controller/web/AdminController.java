package org.example.controller.web;

import org.example.dto.ReservaResponseDTO;
import org.example.dto.ReservaWalkInDTO;
import org.example.service.RestauranteService;
import org.example.service.reserva.ReservaService;
import org.example.service.reserva.ZonaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ReservaService reservaService;
    private final ZonaService zonaService;
    private final RestauranteService restauranteService;

    public AdminController(ReservaService reservaService, ZonaService zonaService,
                           RestauranteService restauranteService) {
        this.reservaService = reservaService;
        this.zonaService = zonaService;
        this.restauranteService = restauranteService;
    }

    @GetMapping
    public String inicio() {
        return "admin/index";
    }

    // ver reservas (+ zonas y restaurantes para los desplegables del walk-in)
    @GetMapping("/reservas")
    public String verReservas(Model model) {
        List<ReservaResponseDTO> reservas = reservaService.listarTodos()
                .stream()
                .map(ReservaResponseDTO::desde)
                .toList();

        model.addAttribute("reservas", reservas);
        model.addAttribute("zonas", zonaService.listarTodos());
        model.addAttribute("restaurantes", restauranteService.listarTodos());
        return "admin/reservas";
    }

    // confirmar
    @PutMapping("/reservas/{id}/confirmar")
    public String confirmar(@PathVariable int id, Model model) {
        model.addAttribute("r", ReservaResponseDTO.desde(reservaService.confirmar(id)));
        return "fragments/admin/reserva-fila :: fila";
    }

    // cancelar
    @PutMapping("/reservas/{id}/cancelar")
    public String cancelar(@PathVariable int id, Model model) {
        model.addAttribute("r", ReservaResponseDTO.desde(reservaService.cancelar(id)));
        return "fragments/admin/reserva-fila :: fila";
    }

    // marcar asistencia (vino / no vino)
    @PutMapping("/reservas/{id}/asistencia")
    public String marcarAsistencia(@PathVariable int id, @RequestParam boolean asistio, Model model) {
        model.addAttribute("r", ReservaResponseDTO.desde(reservaService.marcarAsistencia(id, asistio)));
        return "fragments/admin/reserva-fila :: fila";
    }

    // reserva walk-in
    @PostMapping("/reservas/walkin")
    public String walkIn(@ModelAttribute ReservaWalkInDTO dto, Model model) {
        model.addAttribute("r", ReservaResponseDTO.desde(reservaService.reservarWalkIn(dto)));
        return "fragments/admin/reserva-fila :: fila";
    }
}