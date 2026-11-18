
package unam.fes.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import unam.fes.service.BloqueClienteService;

@Controller
@RequestMapping("/admin/bloques")
public class BloqueClienteController {

    private final BloqueClienteService bloqueClienteService;

    public BloqueClienteController(
            BloqueClienteService bloqueClienteService) {
        this.bloqueClienteService = bloqueClienteService;
    }

    @GetMapping
    public String listarBloques(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!"ADMINISTRADOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        try {
            model.addAttribute(
                    "bloques",
                    bloqueClienteService.listarBloques()
            );

            return "admin-bloques";

        } catch (RestClientException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "No fue posible consultar los bloques."
            );

            return "redirect:/admin";
        }
    }

    @PostMapping("/crear")
    public String crearBloque(
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!"ADMINISTRADOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        try {
            bloqueClienteService.crearBloque(nombre, descripcion);

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Bloque creado exitosamente."
            );

        } catch (RestClientException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "No fue posible crear el bloque. Verifica que el nombre no esté repetido."
            );
        }

        return "redirect:/admin/bloques";
    }

    @PostMapping("/modificar/{id}")
    public String modificarBloque(
            @PathVariable Long id,
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!"ADMINISTRADOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        try {
            bloqueClienteService.modificarBloque(
                    id,
                    nombre,
                    descripcion
            );

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Bloque modificado exitosamente."
            );

        } catch (RestClientException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "No fue posible modificar el bloque."
            );
        }

        return "redirect:/admin/bloques";
    }

    @PostMapping("/estado/{id}")
    public String cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activar,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!"ADMINISTRADOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        try {
            bloqueClienteService.cambiarEstado(id, activar);

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    activar
                            ? "Bloque activado exitosamente."
                            : "Bloque desactivado exitosamente."
            );

        } catch (RestClientException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "No fue posible cambiar el estado del bloque."
            );
        }

        return "redirect:/admin/bloques";
    }
}
