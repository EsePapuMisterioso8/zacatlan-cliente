
package unam.fes.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import unam.fes.model.ProductoDTO;
import unam.fes.service.BloqueClienteService;
import unam.fes.service.ProductoClienteService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/productos")
public class ProductoClienteController {

    private final ProductoClienteService productoService;
    private final BloqueClienteService bloqueService;

    public ProductoClienteController(
            ProductoClienteService productoService,
            BloqueClienteService bloqueService) {
        this.productoService = productoService;
        this.bloqueService = bloqueService;
    }

    @GetMapping
    public String listarProductos(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!"ADMINISTRADOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        try {
            model.addAttribute("productos", productoService.obtenerTodos());

            List<Map<String, Object>> bloquesActivos =
                    bloqueService.listarBloques().stream()
                            .filter(b -> Boolean.TRUE.equals(b.get("activo")))
                            .collect(Collectors.toList());

            model.addAttribute("bloques", bloquesActivos);

            return "admin-productos";

        } catch (RestClientException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "No fue posible consultar los productos o bloques."
            );
            return "redirect:/admin";
        }
    }

    @PostMapping("/crear")
    public String registrarProducto(
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam Long idBloque,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!"ADMINISTRADOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        try {
            productoService.registrarProducto(nombre, descripcion, idBloque);

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Producto registrado exitosamente."
            );

        } catch (RestClientResponseException e) {
            String mensaje = e.getResponseBodyAsString();

            redirectAttributes.addFlashAttribute(
                    "error",
                    mensaje.isBlank()
                            ? "No fue posible registrar el producto."
                            : mensaje
            );

        } catch (RestClientException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "No fue posible comunicarse con la API REST."
            );
        }

        return "redirect:/admin/productos";
    }

    @PostMapping("/editar/{id}")
    public String actualizarProducto(
            @PathVariable Long id,
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!"ADMINISTRADOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        try {
            productoService.actualizarProducto(id, nombre, descripcion);

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Producto actualizado exitosamente."
            );

        } catch (RestClientResponseException e) {
            String mensaje = e.getResponseBodyAsString();

            redirectAttributes.addFlashAttribute(
                    "error",
                    mensaje.isBlank()
                            ? "No fue posible actualizar el producto."
                            : mensaje
            );

        } catch (RestClientException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "No fue posible comunicarse con la API REST."
            );
        }

        return "redirect:/admin/productos";
    }

    @PostMapping("/estado/{id}")
    public String cambiarEstado(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!"ADMINISTRADOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        try {
            productoService.cambiarEstado(id);

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Estado del producto actualizado exitosamente."
            );

        } catch (RestClientResponseException e) {
            String mensaje = e.getResponseBodyAsString();

            redirectAttributes.addFlashAttribute(
                    "error",
                    mensaje.isBlank()
                            ? "No fue posible cambiar el estado del producto."
                            : mensaje
            );

        } catch (RestClientException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "No fue posible comunicarse con la API REST."
            );
        }

        return "redirect:/admin/productos";
    }
}
