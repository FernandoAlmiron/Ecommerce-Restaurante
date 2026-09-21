package org.example.controller.facturacion;

import org.example.Modelo.facturacion.CierreCaja;
import org.example.service.facturacion.CierreCajaService;
import org.example.dto.CierreCajaDTO;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cierres-caja")
public class CierreCajaController {
    private final CierreCajaService cierreCajaService;

    public CierreCajaController(CierreCajaService cierreCajaService) { this.cierreCajaService = cierreCajaService; }

    @PostMapping
    public CierreCaja crear(@RequestBody CierreCajaDTO dto, Authentication authentication) {
        return cierreCajaService.crearCierre(dto, authentication.getName());
    }
    @GetMapping
    public List<CierreCaja> listar() { return cierreCajaService.listarTodos(); }

    @GetMapping("/{id}")
    public CierreCaja buscar(@PathVariable int id) { return cierreCajaService.buscarPorId(id); }
}