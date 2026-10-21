package org.example.web.admin;

import org.example.Modelo.inventario.Proveedores;
import org.example.Modelo.inventario.Stock;
import org.example.service.RestauranteService;
import org.example.service.inventario.CategoriaService;
import org.example.service.inventario.ProveedoresService;
import org.example.service.inventario.StockService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/insumos")
public class AdminInsumosController {

    private final StockService stockService;
    private final ProveedoresService proveedoresService;
    private final CategoriaService categoriaService;
    private final RestauranteService restauranteService;

    public AdminInsumosController(StockService stockService, ProveedoresService proveedoresService,
                                  CategoriaService categoriaService, RestauranteService restauranteService) {
        this.stockService = stockService;
        this.proveedoresService = proveedoresService;
        this.categoriaService = categoriaService;
        this.restauranteService = restauranteService;
    }

    // página completa: insumos + proveedores + listas para los desplegables
    @GetMapping
    public String verInsumos(Model model) {
        model.addAttribute("insumos", stockService.listarTodos());
        model.addAttribute("proveedores", proveedoresService.listarTodos());
        model.addAttribute("categorias", categoriaService.listarTodos());
        model.addAttribute("restaurantes", restauranteService.listarTodos());
        return "admin/insumos";
    }

    // ===================== INSUMOS =====================

    // crear un insumo
    @PostMapping
    public String crear(@ModelAttribute Stock insumo,
                        @RequestParam int nroRestaurante,
                        @RequestParam int nroCategoria,
                        Model model) {
        insumo.setRestaurante(restauranteService.buscarPorId(nroRestaurante));
        insumo.setCategoria(categoriaService.buscarPorId(nroCategoria));
        model.addAttribute("s", stockService.guardar(insumo));
        return "fragments/admin/insumo-fila :: fila";
    }

    // sumar stock (llegó mercadería)
    @PutMapping("/{id}/reponer")
    public String reponer(@PathVariable int id, @RequestParam double cantidad, Model model) {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        model.addAttribute("s", stockService.reponerStock(id, cantidad));
        return "fragments/admin/insumo-fila :: fila";
    }

    // restar stock (se usó / se tiró)
    @PutMapping("/{id}/descontar")
    public String descontar(@PathVariable int id, @RequestParam double cantidad, Model model) {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        model.addAttribute("s", stockService.disminuirStock(id, cantidad));
        return "fragments/admin/insumo-fila :: fila";
    }

    // eliminar un insumo
    @DeleteMapping("/{id}")
    @ResponseBody
    public String eliminar(@PathVariable int id) {
        stockService.eliminar(id);
        return "";
    }

    // ===================== PROVEEDORES =====================

    // crear un proveedor
    @PostMapping("/proveedores")
    public String crearProveedor(@ModelAttribute Proveedores proveedor,
                                 @RequestParam int nroCategoria,
                                 Model model) {
        proveedor.setCategoria(categoriaService.buscarPorId(nroCategoria));
        model.addAttribute("prov", proveedoresService.guardar(proveedor));
        return "fragments/admin/proveedor-fila :: fila";
    }

    // eliminar un proveedor
    @DeleteMapping("/proveedores/{id}")
    @ResponseBody
    public String eliminarProveedor(@PathVariable int id) {
        proveedoresService.eliminar(id);
        return "";
    }
}