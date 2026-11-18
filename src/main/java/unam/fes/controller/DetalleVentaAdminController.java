
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
@RequestMapping("/admin/ventas")
public class DetalleVentaAdminController {

    private final HistorialVentasClienteService historialVentasService;

    public DetalleVentaAdminController(
            HistorialVentasClienteService historialVentasService) {
        this.historialVentasService = historialVentasService;
    }

    @GetMapping("/{id}")
    public String mostrarDetalle(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!"ADMINISTRADOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        try {
            model.addAttribute(
                    "venta",
                    historialVentasService.obtenerDetalleVenta(id)
            );

            return "detalle-venta-admin";

        } catch (RestClientException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "No fue posible consultar el detalle de la venta."
            );

            return "redirect:/admin/ventas";
        }
    }
}
