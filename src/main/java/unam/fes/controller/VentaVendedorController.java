package unam.fes.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import unam.fes.model.InventarioVendedorDTO;
import unam.fes.service.InventarioClienteService;
import unam.fes.service.VentaClienteService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/vendedor/ventas")
public class VentaVendedorController {

    private final VentaClienteService ventaClienteService;
    private final InventarioClienteService inventarioClienteService;

    public VentaVendedorController(
            VentaClienteService ventaClienteService,
            InventarioClienteService inventarioClienteService) {

        this.ventaClienteService = ventaClienteService;
        this.inventarioClienteService = inventarioClienteService;
    }

    @GetMapping("/nueva")
    public String mostrarFormulario(HttpSession session, Model model) {

        if (!"VENDEDOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        try {
            List<InventarioVendedorDTO> inventario =
                    inventarioClienteService.listarInventarioPropio();

            model.addAttribute("inventario", inventario);

        } catch (RestClientException e) {
            model.addAttribute(
                    "error",
                    "No fue posible consultar tu inventario."
            );
        }

        return "venta-nueva";
    }

    @PostMapping("/registrar")
    public String registrarVenta(
            @RequestParam(name = "idProductoVendedor", required = false)
            List<Long> idsProductos,

            @RequestParam(name = "cantidad", required = false)
            List<Integer> cantidades,

            @RequestParam(name = "precioVenta", required = false)
            List<BigDecimal> precios,

            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!"VENDEDOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        if (idsProductos == null || idsProductos.isEmpty()
                || cantidades == null || precios == null
                || idsProductos.size() != cantidades.size()
                || idsProductos.size() != precios.size()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Debes agregar al menos un producto y verificar sus datos."
            );

            return "redirect:/vendedor/ventas/nueva";
        }

        List<Map<String, Object>> items = new ArrayList<>();

        for (int i = 0; i < idsProductos.size(); i++) {

            Long idProductoVendedor = idsProductos.get(i);
            Integer cantidad = cantidades.get(i);
            BigDecimal precioVenta = precios.get(i);

            if (idProductoVendedor == null || idProductoVendedor <= 0) {
                redirectAttributes.addFlashAttribute(
                        "error",
                        "Uno de los productos seleccionados no es válido."
                );

                return "redirect:/vendedor/ventas/nueva";
            }

            if (cantidad == null || cantidad <= 0) {
                redirectAttributes.addFlashAttribute(
                        "error",
                        "Todas las cantidades deben ser mayores que cero."
                );

                return "redirect:/vendedor/ventas/nueva";
            }

            if (precioVenta == null
                    || precioVenta.compareTo(BigDecimal.ZERO) <= 0) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "Todos los precios deben ser mayores que cero."
                );

                return "redirect:/vendedor/ventas/nueva";
            }

            Map<String, Object> item = new HashMap<>();
            item.put("idProductoVendedor", idProductoVendedor);
            item.put("cantidad", cantidad);
            item.put("precioVenta", precioVenta);

            items.add(item);
        }

        try {

            ventaClienteService.registrarVenta(items);

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "La venta se registró correctamente."
            );

        } catch (RestClientException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No se pudo registrar la venta. Verifica los productos, sus precios y su disponibilidad."
            );
        }

        return "redirect:/vendedor/ventas/nueva";
    }
}