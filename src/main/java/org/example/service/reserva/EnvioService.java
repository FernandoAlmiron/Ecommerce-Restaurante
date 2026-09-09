package org.example.service.reserva;

import org.example.Modelo.reserva.Envio;
import org.example.Modelo.facturacion.Ticket;
import org.example.Modelo.Restaurante;
import org.example.Modelo.enums.EstadoEnvio;
import org.example.Modelo.enums.OrigenTicket;
import org.example.Modelo.menu.Pedido;
import org.example.Modelo.menu.Menu;
import org.example.repository.reserva.EnvioRepository;
import org.example.repository.facturacion.TicketRepository;
import org.example.repository.persona.ClienteRepository;
import org.example.repository.persona.EmpleadoRepository;
import org.example.repository.menu.MenuRepository;
import org.example.repository.RestauranteRepository;
import org.example.dto.EnvioDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnvioService {
    private final EnvioRepository envioRepository;
    private final TicketRepository ticketRepository;
    private final ClienteRepository clienteRepository;
    private final EmpleadoRepository empleadoRepository;
    private final MenuRepository menuRepository;
    private final RestauranteRepository restauranteRepository;
    private final org.example.service.menu.PedidoService pedidoService;

    public EnvioService(EnvioRepository envioRepository, TicketRepository ticketRepository,
                        ClienteRepository clienteRepository, EmpleadoRepository empleadoRepository,
                        MenuRepository menuRepository, RestauranteRepository restauranteRepository,
                        org.example.service.menu.PedidoService pedidoService) {
        this.envioRepository = envioRepository;
        this.ticketRepository = ticketRepository;
        this.clienteRepository = clienteRepository;
        this.empleadoRepository = empleadoRepository;
        this.menuRepository = menuRepository;
        this.restauranteRepository = restauranteRepository;
        this.pedidoService = pedidoService;
    }

    public Envio crearEnvioConPedido(EnvioDTO dto) {
        Restaurante restaurante = restauranteRepository.findById(dto.getNroRestaurante()).orElseThrow();

        Ticket ticket = new Ticket();
        ticket.setRestaurante(restaurante);
        ticket.setOrigen(OrigenTicket.DELIVERY);
        ticket = ticketRepository.save(ticket);

        for (EnvioDTO.PedidoDTO pd : dto.getPedidos()) {
            Menu menu = menuRepository.findById(pd.getIdMenu()).orElseThrow();
            Pedido pedido = new Pedido();
            pedido.setMenu(menu);
            pedido.setTicket(ticket);
            pedido.setCantidad(pd.getCantidad());
            pedido.setPrecioUnitario(menu.getPrecio());
            pedido.setObservaciones(pd.getObservaciones());
            pedidoService.guardar(pedido);
        }

        Envio envio = new Envio();
        envio.setCliente(clienteRepository.findById(dto.getIdCliente()).orElseThrow());
        envio.setTicket(ticket);
        envio.setDireccionEntrega(dto.getDireccionEntrega());
        envio.setNombreReceptor(dto.getNombreReceptor());
        envio.setEstado(EstadoEnvio.PENDIENTE);
        return envioRepository.save(envio);
    }

    public Envio asignarRepartidor(int idEnvio, int legajoRepartidor) {
        Envio envio = envioRepository.findById(idEnvio).orElseThrow();
        envio.setRepartidor(empleadoRepository.findById(legajoRepartidor).orElseThrow());
        return envioRepository.save(envio);
    }

    public Envio actualizarEstado(int idEnvio, EstadoEnvio nuevoEstado) {
        Envio envio = envioRepository.findById(idEnvio).orElseThrow();
        envio.setEstado(nuevoEstado);
        return envioRepository.save(envio);
    }

    public Envio buscarPorId(int idEnvio) { return envioRepository.findById(idEnvio).orElseThrow(); }
    public List<Envio> listarTodos() { return envioRepository.findAll(); }
}
