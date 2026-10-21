package org.example.web.admin;

import org.example.Modelo.persona.Cliente;
import org.example.service.persona.ClienteService;
import org.example.service.reserva.ReservaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/clientes")
public class AdminClienteController {

    private final ClienteService clienteService;
    private final ReservaService reservaService;

    public AdminClienteController(ClienteService clienteService, ReservaService reservaService) {
        this.clienteService = clienteService;
        this.reservaService = reservaService;
    }

    // página completa
    @GetMapping
    public String verClientes(Model model) {
        model.addAttribute("clientes", clienteService.listarTodos());
        return "admin/clientes";
    }

    // búsqueda en vivo: devuelve solo las filas que coinciden
    @GetMapping("/buscar")
    public String buscar(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("clientes", filtrar(q));
        return "fragments/admin/cliente-fila :: filas";
    }

    // historial de un cliente (panel derecho)
    @GetMapping("/{id}")
    public String verHistorial(@PathVariable int id, Model model) {
        model.addAttribute("c", clienteService.buscarPorId(id));
        model.addAttribute("reservas", reservaService.listarTodos().stream()
                .filter(r -> r.getCliente() != null && r.getCliente().getIdCliente() == id)
                .toList());
        return "fragments/admin/cliente-detalle :: detalle";
    }

    // alta rápida (sin cuenta web)
    @PostMapping
    public String crear(@ModelAttribute Cliente cliente, Model model) {
        cliente.setIdCliente(0);
        model.addAttribute("c", clienteService.guardar(cliente));
        return "fragments/admin/cliente-fila :: fila";
    }

    // eliminar (si tiene reservas o envíos, la base lo rechaza)
    @DeleteMapping("/{id}")
    @ResponseBody
    public String eliminar(@PathVariable int id) {
        clienteService.eliminar(id);
        return "";
    }

    // filtra por nombre, apellido o DNI (sin importar mayúsculas)
    private List<Cliente> filtrar(String q) {
        if (q == null || q.isBlank()) {
            return clienteService.listarTodos();
        }
        String texto = q.trim().toLowerCase();
        return clienteService.listarTodos().stream()
                .filter(c -> (c.getNombre() + " " + c.getApellido()).toLowerCase().contains(texto)
                        || String.valueOf(c.getDni()).contains(texto))
                .toList();
    }
}