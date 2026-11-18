package unam.fes.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import unam.fes.model.BloqueVendedorDTO;
import unam.fes.model.ProductoDTO;
import unam.fes.service.CatalogoVendedorClienteService;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/vendedor")
public class CatalogoVendedorController {

    private final CatalogoVendedorClienteService catalogoService;

    public CatalogoVendedorController(
            CatalogoVendedorClienteService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping("/catalogo")
    public String mostrarCatalogo(HttpSession session, Model model) {

        if (!"VENDEDOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        try {
            BloqueVendedorDTO bloque =
                    catalogoService.obtenerBloqueVendedor();

            List<ProductoDTO> productos =
                    catalogoService.listarCatalogo(bloque.getIdBloque());

            model.addAttribute("bloque", bloque);
            model.addAttribute("productos", productos);

        } catch (RestClientException e) {

            model.addAttribute("bloque", null);
            model.addAttribute("productos", List.of());
            model.addAttribute(
                    "error",
                    "No fue posible consultar el catálogo."
            );
        }

        return "catalogo-vendedor";
    }

    @PostMapping("/catalogo/agregar")
    public String agregarProducto(
            @RequestParam Long idProducto,
            @RequestParam BigDecimal precio,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!"VENDEDOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        try {
            catalogoService.agregarProducto(idProducto, precio);

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Producto agregado a tu inventario exitosamente."
            );

        } catch (RestClientResponseException e) {

            String mensaje = e.getResponseBodyAsString();

            redirectAttributes.addFlashAttribute(
                    "error",
                    mensaje.isBlank()
                            ? "No fue posible agregar el producto."
                            : mensaje
            );

        } catch (RestClientException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No fue posible comunicarse con la API REST."
            );
        }

        return "redirect:/vendedor/catalogo";
    }
}