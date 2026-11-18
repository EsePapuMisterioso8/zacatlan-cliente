
package unam.fes.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import unam.fes.service.HistorialVentasClienteService;

@Controller
@RequestMapping("/vendedor/ventas")
public class DetalleVentaVendedorController {

    private final HistorialVentasClienteService historialVentasClienteService;

    public DetalleVentaVendedorController(
            HistorialVentasClienteService historialVentasClienteService) {
        this.historialVentasClienteService = historialVentasClienteService;
    }

    @GetMapping("/{id}")
    public String mostrarDetalle(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!"VENDEDOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        try {
            model.addAttribute(
                    "venta",
                    historialVentasClienteService.obtenerDetalleVenta(id)
            );

            return "detalle-venta-vendedor";

        } catch (RestClientException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "No fue posible consultar el detalle. Verifica que la venta te pertenezca."
            );

            return "redirect:/vendedor/ventas";
        }
    }
}
