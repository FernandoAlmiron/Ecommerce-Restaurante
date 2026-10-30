package org.example.web.cliente;

import org.example.Modelo.persona.Cliente;
import org.example.Modelo.reserva.Reserva;
import org.example.Modelo.reserva.Senia;
import org.example.Modelo.reserva.Tarjeta;
import org.example.Modelo.reserva.Zona;
import org.example.service.RestauranteService;
import org.example.service.persona.ClienteService;
import org.example.service.reserva.ReservaService;
import org.example.service.reserva.ZonaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Comparator;

@Controller
@RequestMapping("/reservas")
public class ClienteReservaController {

    private final ReservaService reservaService;
    private final ZonaService zonaService;
    private final ClienteService clienteService;
    private final RestauranteService restauranteService;

    // el mismo valor que usa ReservaService (application.properties, o 30000 si no está)
    @Value("${reservas.monto-senia:30000}")
    private BigDecimal montoSenia;

    public ClienteReservaController(ReservaService reservaService, ZonaService zonaService,
                                    ClienteService clienteService, RestauranteService restauranteService) {
        this.reservaService = reservaService;
        this.zonaService = zonaService;
        this.clienteService = clienteService;
        this.restauranteService = restauranteService;
    }

    // MIS RESERVAS: solo las del cliente logueado, de la más nueva a la más vieja
    @GetMapping
    public String misReservas(Authentication auth, Model model) {
        Cliente cliente = clienteService.buscarPorUsername(auth.getName());
        model.addAttribute("reservas", reservaService.listarPorCliente(cliente.getIdCliente()).stream()
                .sorted(Comparator.comparing(Reserva::getFechaReserva)
                        .thenComparing(Reserva::getHoraReserva).reversed())
                .toList());
        return "cliente/reserva/reservas";
    }

    // el formulario vacío
    @GetMapping("/nueva")
    public String nueva(Model model) {
        cargarFormulario(model);
        return "cliente/reserva/reserva-nueva";
    }

    // guardar la reserva
    @PostMapping("/nueva")
    public String crear(@RequestParam int cantComensales,
                        @RequestParam String fecha,          // "2026-10-25" (input type="date")
                        @RequestParam String hora,           // "21:30"      (input type="time")
                        @RequestParam int idZona,
                        @RequestParam String titular,
                        @RequestParam String vencimiento,    // "2028-12"    (input type="month")
                        Authentication auth, Model model) {
        try {
            Reserva reserva = new Reserva();
            reserva.setCantComensales(cantComensales);
            reserva.setFechaReserva(LocalDate.parse(fecha));
            reserva.setHoraReserva(LocalTime.parse(hora));

            Zona zona = new Zona();          // solo el id: ReservaService busca la zona completa
            zona.setIdZona(idZona);
            reserva.setZona(zona);

            Tarjeta tarjeta = new Tarjeta();
            tarjeta.setTitular(titular);
            tarjeta.setVencimiento(YearMonth.parse(vencimiento));
            Senia senia = new Senia();
            senia.setTarjeta(tarjeta);
            reserva.setSenia(senia);

            // el cliente y el restaurante los pone el service a partir del usuario logueado
            reservaService.crear(reserva, auth.getName());
            return "redirect:/reservas?creada";

        } catch (DateTimeException e) {
            // la fecha, la hora o el vencimiento vinieron mal escritos (ej: año 275760)
            model.addAttribute("error", "Revisá la fecha, la hora y el vencimiento de la tarjeta");
            cargarFormulario(model);
            return "cliente/reserva/reserva-nueva";

        } catch (IllegalArgumentException | IllegalStateException e) {
            // "No hay lugar en esa zona", "El restaurante está cerrado a esa hora", etc.
            model.addAttribute("error", e.getMessage());
            cargarFormulario(model);
            return "cliente/reserva/reserva-nueva";
        }
    }

    // cancelar desde la tarjeta (HTMX): devuelve la tarjeta actualizada
    @PutMapping("/{id}/cancelar")
    public String cancelar(@PathVariable int id, Authentication auth, Model model) {
        Reserva reserva = reservaService.buscarPorId(id);
        // seguridad: que nadie pueda cancelar la reserva de otro cambiando el número en la URL
        if (!auth.getName().equals(reserva.getCliente().getUsername())) {
            throw new IllegalStateException("Esa reserva no es tuya");
        }
        model.addAttribute("r", reservaService.cancelar(id));
        return "fragments/cliente/reserva-card :: card";
    }

    // lo que necesita el formulario
    private void cargarFormulario(Model model) {
        var restaurante = restauranteService.listarTodos().get(0);
        model.addAttribute("zonas", zonaService.listarActivas(restaurante.getNroRestaurante()));
        model.addAttribute("restaurante", restaurante);
        model.addAttribute("montoSenia", montoSenia);
        model.addAttribute("hoy", LocalDate.now());                      // no deja elegir días pasados
        model.addAttribute("maxFecha", LocalDate.now().plusMonths(3));   // ni reservar a más de 3 meses
        model.addAttribute("mesActual", YearMonth.now());                // ni tarjetas vencidas
        model.addAttribute("maxMes", YearMonth.now().plusYears(15));     // ni vencimientos absurdos
    }
}