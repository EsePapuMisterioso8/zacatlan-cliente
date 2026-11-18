
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
            model.addAttribute("error", "No fue posible consultar tu inventario.");
        }

        return "venta-nueva";
    }

    @PostMapping("/registrar")
    public String registrarVenta(
            @RequestParam Long idProductoVendedor,
            @RequestParam Integer cantidad,
            @RequestParam BigDecimal precioVenta,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!"VENDEDOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        if (cantidad == null || cantidad <= 0) {
            redirectAttributes.addFlashAttribute(
                    "error", "La cantidad debe ser mayor que cero.");
            return "redirect:/vendedor/ventas/nueva";
        }

        if (precioVenta == null || precioVenta.compareTo(BigDecimal.ZERO) <= 0) {
            redirectAttributes.addFlashAttribute(
                    "error", "El precio de venta debe ser mayor que cero.");
            return "redirect:/vendedor/ventas/nueva";
        }

        try {
            Map<String, Object> item = new HashMap<>();
            item.put("idProductoVendedor", idProductoVendedor);
            item.put("cantidad", cantidad);
            item.put("precioVenta", precioVenta);

            ventaClienteService.registrarVenta(List.of(item));

            redirectAttributes.addFlashAttribute(
                    "mensaje", "La venta se registró correctamente.");

        } catch (RestClientException e) {
            redirectAttributes.addFlashAttribute(
                    "error", "No se pudo registrar la venta. Verifica el producto y su disponibilidad.");
        }

        return "redirect:/vendedor/ventas/nueva";
    }
}
