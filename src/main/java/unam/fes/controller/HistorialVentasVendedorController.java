
package unam.fes.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestClientException;
import unam.fes.service.HistorialVentasClienteService;

@Controller
@RequestMapping("/vendedor/ventas")
public class HistorialVentasVendedorController {

    private final HistorialVentasClienteService historialVentasClienteService;

    public HistorialVentasVendedorController(
            HistorialVentasClienteService historialVentasClienteService) {
        this.historialVentasClienteService = historialVentasClienteService;
    }

    @GetMapping
    public String mostrarHistorial(HttpSession session, Model model) {

        if (!"VENDEDOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        try {
            model.addAttribute(
                    "ventas",
                    historialVentasClienteService.listarMisVentas()
            );

        } catch (RestClientException e) {
            model.addAttribute(
                    "error",
                    "No fue posible consultar el historial de ventas."
            );
        }

        return "historial-ventas-vendedor";
    }
}
