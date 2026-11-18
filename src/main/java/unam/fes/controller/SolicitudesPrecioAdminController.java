
package unam.fes.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import unam.fes.service.SolicitudPrecioClienteService;

import java.util.List;

@Controller
@RequestMapping("/admin/solicitudes-precio")
public class SolicitudesPrecioAdminController {

    private final SolicitudPrecioClienteService solicitudService;

    public SolicitudesPrecioAdminController(
            SolicitudPrecioClienteService solicitudService) {
        this.solicitudService = solicitudService;
    }

    // Consultar todas las solicitudes pendientes
    @GetMapping
    public String mostrarSolicitudes(
            HttpSession session,
            Model model) {

        if (!"ADMINISTRADOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        try {
            model.addAttribute(
                    "solicitudes",
                    solicitudService.listarPendientes()
            );

        } catch (RestClientException e) {

            model.addAttribute("solicitudes", List.of());
            model.addAttribute(
                    "error",
                    "No fue posible consultar las solicitudes pendientes."
            );
        }

        return "solicitudes-precio-admin";
    }

    // Autorizar solicitud
    @PostMapping("/{id}/autorizar")
    public String autorizarSolicitud(
            @PathVariable Long id,
            @RequestParam(required = false) String observacion,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!"ADMINISTRADOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        try {
            solicitudService.autorizarSolicitud(id, observacion);

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Solicitud autorizada y precio actualizado correctamente."
            );

        } catch (RestClientResponseException e) {

            String mensaje = e.getResponseBodyAsString();

            redirectAttributes.addFlashAttribute(
                    "error",
                    mensaje.isBlank()
                            ? "No fue posible autorizar la solicitud."
                            : mensaje
            );

        } catch (RestClientException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No fue posible comunicarse con la API REST."
            );
        }

        return "redirect:/admin/solicitudes-precio";
    }

    // Rechazar solicitud
    @PostMapping("/{id}/rechazar")
    public String rechazarSolicitud(
            @PathVariable Long id,
            @RequestParam(required = false) String observacion,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!"ADMINISTRADOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        try {
            solicitudService.rechazarSolicitud(id, observacion);

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Solicitud rechazada correctamente."
            );

        } catch (RestClientResponseException e) {

            String mensaje = e.getResponseBodyAsString();

            redirectAttributes.addFlashAttribute(
                    "error",
                    mensaje.isBlank()
                            ? "No fue posible rechazar la solicitud."
                            : mensaje
            );

        } catch (RestClientException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No fue posible comunicarse con la API REST."
            );
        }

        return "redirect:/admin/solicitudes-precio";
    }
}
