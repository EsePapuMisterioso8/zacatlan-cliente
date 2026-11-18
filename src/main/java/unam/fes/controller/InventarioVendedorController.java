package unam.fes.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestClientException;

import unam.fes.service.InventarioClienteService;

import java.util.List;

@Controller
@RequestMapping("/vendedor")
public class InventarioVendedorController {

    private final InventarioClienteService inventarioClienteService;

    public InventarioVendedorController(
            InventarioClienteService inventarioClienteService) {
        this.inventarioClienteService = inventarioClienteService;
    }

    @GetMapping("/productos")
    public String listarMisProductos(HttpSession session, Model model) {

        // Verificar que exista una sesión de vendedor
        if (!"VENDEDOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        try {
            model.addAttribute(
                    "inventario",
                    inventarioClienteService.listarInventarioPropio()
            );

        } catch (RestClientException e) {
            model.addAttribute("inventario", List.of());
            model.addAttribute(
                    "error",
                    "No fue posible consultar tu inventario. Intenta nuevamente."
            );
        }

        return "productos-vendedor";
    }
}