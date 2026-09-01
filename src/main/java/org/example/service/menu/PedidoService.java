package org.example.service.menu;

import org.example.Modelo.menu.Pedido;
import org.example.repository.menu.PedidoRepository;
import org.example.service.menu.MenuIngredienteService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final MenuIngredienteService menuIngredienteService;

    public PedidoService(PedidoRepository pedidoRepository, MenuIngredienteService menuIngredienteService) {
        this.pedidoRepository = pedidoRepository;
        this.menuIngredienteService = menuIngredienteService;
    }

    public Pedido guardar(Pedido pedido) {
        Pedido guardado = pedidoRepository.save(pedido);
        menuIngredienteService.descontarStockPorPedido(pedido.getMenu(), pedido.getCantidad());
        return guardado;
    }

    public Pedido buscarPorId(int id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }
}
