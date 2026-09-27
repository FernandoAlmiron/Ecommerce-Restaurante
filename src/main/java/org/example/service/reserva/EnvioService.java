package org.example.service.reserva;

import org.example.Modelo.Restaurante;
import org.example.Modelo.enums.EstadoEnvio;
import org.example.Modelo.enums.OrigenTicket;
import org.example.Modelo.facturacion.Ticket;
import org.example.Modelo.menu.Menu;
import org.example.Modelo.menu.Pedido;
import org.example.Modelo.persona.Cliente;
import org.example.Modelo.persona.Empleado;
import org.example.Modelo.reserva.Envio;
import org.example.dto.EnvioDTO;
import org.example.repository.RestauranteRepository;
import org.example.repository.facturacion.TicketRepository;
import org.example.repository.menu.MenuRepository;
import org.example.repository.persona.ClienteRepository;
import org.example.repository.persona.EmpleadoRepository;
import org.example.repository.reserva.EnvioRepository;
import org.example.service.menu.MenuIngredienteService;
import org.example.service.menu.PedidoService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EnvioService {
    private final EnvioRepository envioRepository;
    private final TicketRepository ticketRepository;
    private final ClienteRepository clienteRepository;
    private final EmpleadoRepository empleadoRepository;
    private final MenuRepository menuRepository;
    private final RestauranteRepository restauranteRepository;
    private final PedidoService pedidoService;
    private static final int MAX_PLATOS_DISTINTOS = 10;
    private static final int MAX_UNIDADES_POR_PLATO = 10;

    public EnvioService(EnvioRepository envioRepository, TicketRepository ticketRepository,
                        ClienteRepository clienteRepository, EmpleadoRepository empleadoRepository,
                        MenuRepository menuRepository, RestauranteRepository restauranteRepository,
                        PedidoService pedidoService, MenuIngredienteService menuIngredienteService) {
        this.envioRepository = envioRepository;
        this.ticketRepository = ticketRepository;
        this.clienteRepository = clienteRepository;
        this.empleadoRepository = empleadoRepository;
        this.menuRepository = menuRepository;
        this.restauranteRepository = restauranteRepository;
        this.pedidoService = pedidoService;
    }

    @Transactional
    public Envio crearEnvioConPedido(EnvioDTO dto) {
        if (dto.getPedidos() == null || dto.getPedidos().isEmpty()) {
            throw new IllegalArgumentException("El envio necesita al menos un pedido");
        }
        if (dto.getDireccionEntrega() == null || dto.getDireccionEntrega().isBlank()) {
            throw new IllegalArgumentException("La direccion de entrega es obligatoria");
        }
        // los limites se validan ANTES de crear el ticket y descontar stock
        if (dto.getPedidos().size() > MAX_PLATOS_DISTINTOS) {
            throw new IllegalArgumentException("Un envio admite hasta " + MAX_PLATOS_DISTINTOS + " platos distintos");
        }
        for (EnvioDTO.PedidoDTO pd : dto.getPedidos()) {
            if (pd.getCantidad() > MAX_UNIDADES_POR_PLATO) {
                throw new IllegalArgumentException("Un plato admite hasta " + MAX_UNIDADES_POR_PLATO + " unidades por envio");
            }
        }
        Restaurante restaurante = restauranteRepository.findById(dto.getNroRestaurante())
                .orElseThrow(() -> new IllegalArgumentException("Restaurante no encontrado"));
        Cliente cliente = clienteRepository.findById(dto.getIdCliente())
                .filter(c -> c.getDni() == dto.getDni())
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado o el DNI no coincide"));

        Ticket ticket = new Ticket();
        ticket.setRestaurante(restaurante);
        ticket.setOrigen(OrigenTicket.DELIVERY);
        ticket = ticketRepository.save(ticket);

        for (EnvioDTO.PedidoDTO pd : dto.getPedidos()) {
            Menu menu = menuRepository.findById(pd.getIdMenu())
                    .orElseThrow(() -> new IllegalArgumentException("Menu no encontrado: " + pd.getIdMenu()));
            Pedido pedido = new Pedido();
            pedido.setMenu(menu);
            pedido.setTicket(ticket);
            pedido.setCantidad(pd.getCantidad());
            pedido.setObservaciones(pd.getObservaciones());
            pedidoService.guardar(pedido);   // valida, pone el precio y descuenta el stock
        }

        Envio envio = new Envio();
        envio.setCliente(cliente);
        envio.setTicket(ticket);
        envio.setDireccionEntrega(dto.getDireccionEntrega());
        envio.setNombreReceptor(dto.getNombreReceptor());
        envio.setEstado(EstadoEnvio.PENDIENTE);
        return envioRepository.save(envio);
    }

    @Transactional
    public Envio asignarRepartidor(int idEnvio, int legajoRepartidor) {
        Envio envio = envioRepository.findById(idEnvio).orElseThrow();
        if (envio.getEstado() == EstadoEnvio.ENTREGADO || envio.getEstado() == EstadoEnvio.CANCELADO) {
            throw new IllegalStateException("El envio ya esta " + envio.getEstado());
        }
        Empleado repartidor = empleadoRepository.findById(legajoRepartidor)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));
        if (repartidor.getSector() == null || !"REPARTIDOR".equalsIgnoreCase(repartidor.getSector().getNombre())) {
            throw new IllegalArgumentException("El empleado no pertenece al sector REPARTIDOR");
        }
        envio.setRepartidor(repartidor);
        return envioRepository.save(envio);
    }
    // El repartidor solo puede mover los envios que tiene asignados; el admin, cualquiera
    @Transactional
    public Envio actualizarEstado(int idEnvio, EstadoEnvio nuevoEstado, String username, boolean esAdmin) {
        Envio envio = envioRepository.findById(idEnvio).orElseThrow();
        if (!esAdmin && (envio.getRepartidor() == null
                || !envio.getRepartidor().getUsername().equals(username))) {
            throw new AccessDeniedException("El envio no esta asignado a este repartidor");
        }
        envio.cambiarEstado(nuevoEstado);   // valida que el cambio tenga sentido
        return envioRepository.save(envio);
    }

    public Envio buscarPorId(int idEnvio) { return envioRepository.findById(idEnvio).orElseThrow(); }
    public List<Envio> listarTodos() { return envioRepository.findAll(); }
}