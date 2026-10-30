package org.example.web.reparto;

import org.example.Modelo.enums.EstadoEnvio;
import org.example.Modelo.reserva.Envio;
import org.example.service.reserva.EnvioService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Comparator;
import java.util.List;

/**
 * Pantalla del repartidor (para usar desde el celular).
 * Solo ve SUS envíos sin terminar y los mueve: "Salí con el pedido" y "Entregado y cobrado".
 * Usa los mismos métodos del back que el área Reparto de Desktop (mismas validaciones).
 */
@Controller
@RequestMapping("/reparto")
public class repartoController {

    private final EnvioService envioService;

    public repartoController(EnvioService envioService) {
        this.envioService = envioService;
    }

    // los envíos asignados a este repartidor que todavía no se entregaron ni cancelaron
    @GetMapping
    public String misEnvios(Authentication auth, Model model) {
        List<Envio> envios = envioService.listarTodos().stream()
                .filter(e -> e.getRepartidor() != null
                        && e.getRepartidor().getUsername().equals(auth.getName()))
                .filter(e -> e.getEstado() == EstadoEnvio.PENDIENTE
                        || e.getEstado() == EstadoEnvio.EN_CAMINO)
                .sorted(Comparator.comparingInt(Envio::getIdEnvio))
                .toList();
        model.addAttribute("envios", envios);
        model.addAttribute("usuario", auth.getName());
        return "reparto/envios";
    }

    // "Salí con el pedido" (HTMX): PENDIENTE -> EN_CAMINO, devuelve la tarjeta actualizada
    @PutMapping("/{id}/salir")
    public String salir(@PathVariable int id, Authentication auth, Model model) {
        try {
            model.addAttribute("e", envioService.actualizarEstado(id, EstadoEnvio.EN_CAMINO, auth.getName(), false));
        } catch (AccessDeniedException ex) {
            throw new IllegalStateException("Este envío no está asignado a vos");
        }
        return "fragments/reparto/envio-card :: card";
    }

    // "Entregado y cobrado" (HTMX): factura, cobra y marca ENTREGADO, todo junto o nada
    @PutMapping("/{id}/entregar")
    public String entregar(@PathVariable int id, Authentication auth, Model model) {
        try {
            model.addAttribute("e", envioService.entregarYCobrar(id, auth.getName(), false));
        } catch (AccessDeniedException ex) {
            throw new IllegalStateException("Este envío no está asignado a vos");
        }
        return "fragments/reparto/envio-card :: card";
    }
}