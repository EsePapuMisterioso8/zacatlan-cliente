
package unam.fes.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import unam.fes.model.SolicitudPrecioClienteDTO;
import unam.fes.service.SolicitudPrecioClienteService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/admin/solicitudes-precio")
public class SolicitudesPrecioAdminController {

    private final SolicitudPrecioClienteService solicitudService;

    public SolicitudesPrecioAdminController(
            SolicitudPrecioClienteService solicitudService) {
        this.solicitudService = solicitudService;
    }

    // Consultar y filtrar solicitudes
    @GetMapping
    public String mostrarSolicitudes(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String vendedor,
            @RequestParam(required = false) String producto,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            HttpSession session,
            Model model) {

        if (!"ADMINISTRADOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        // Por defecto se muestran las solicitudes pendientes
        if (estado == null || estado.isBlank()) {
            estado = "PENDIENTE";
        }

        String estadoSeleccionado = estado;
        String vendedorSeleccionado = vendedor;
        String productoSeleccionado = producto;

        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("vendedor", vendedor);
        model.addAttribute("producto", producto);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);

        model.addAttribute("estados", List.of(
                "PENDIENTE",
                "APLICADA",
                "RECHAZADA"
        ));

        try {
            List<SolicitudPrecioClienteDTO> solicitudes =
                    solicitudService.listarTodasLasSolicitudes();

            // Filtrar por estado
            if (!"TODAS".equalsIgnoreCase(estadoSeleccionado)) {
                solicitudes = solicitudes.stream()
                        .filter(s -> s.getEstado() != null
                                && s.getEstado().equalsIgnoreCase(estadoSeleccionado))
                        .toList();
            }

            // Filtrar por nombre del vendedor
            if (vendedorSeleccionado != null
                    && !vendedorSeleccionado.isBlank()) {

                String busquedaVendedor =
                        vendedorSeleccionado.trim().toLowerCase(Locale.ROOT);

                solicitudes = solicitudes.stream()
                        .filter(s -> contiene(s.getNombreVendedor(), busquedaVendedor))
                        .toList();
            }

            // Filtrar por nombre del producto
            if (productoSeleccionado != null
                    && !productoSeleccionado.isBlank()) {

                String busquedaProducto =
                        productoSeleccionado.trim().toLowerCase(Locale.ROOT);

                solicitudes = solicitudes.stream()
                        .filter(s -> contiene(s.getNombreProducto(), busquedaProducto))
                        .toList();
            }

            // Validar y filtrar por fechas
            if (fechaInicio != null || fechaFin != null) {

                if (fechaInicio == null || fechaFin == null) {
                    model.addAttribute(
                            "error",
                            "Para filtrar por fechas debes indicar ambas fechas."
                    );

                } else if (fechaInicio.isAfter(fechaFin)) {
                    model.addAttribute(
                            "error",
                            "La fecha inicial no puede ser posterior a la fecha final."
                    );

                } else {
                    LocalDateTime inicio = fechaInicio.atStartOfDay();
                    LocalDateTime fin = fechaFin.atTime(23, 59, 59, 999999999);

                    solicitudes = solicitudes.stream()
                            .filter(s -> s.getFechaSolicitud() != null
                                    && !s.getFechaSolicitud().isBefore(inicio)
                                    && !s.getFechaSolicitud().isAfter(fin))
                            .toList();
                }
            }

            model.addAttribute("solicitudes", solicitudes);

        } catch (RestClientException e) {

            model.addAttribute("solicitudes", List.of());
            model.addAttribute(
                    "error",
                    "No fue posible consultar las solicitudes."
            );
        }

        return "solicitudes-precio-admin";
    }

    // Autorizar solicitud
    @PostMapping("/{id}/autorizar")
    public String autorizarSolicitud(
            @PathVariable Long id,
            @RequestParam(required = false) String observacion,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String vendedor,
            @RequestParam(required = false) String producto,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
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

        conservarFiltros(
                redirectAttributes, estado, vendedor, producto,
                fechaInicio, fechaFin
        );

        return "redirect:/admin/solicitudes-precio";
    }

    // Rechazar solicitud
    @PostMapping("/{id}/rechazar")
    public String rechazarSolicitud(
            @PathVariable Long id,
            @RequestParam(required = false) String observacion,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String vendedor,
            @RequestParam(required = false) String producto,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
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

        conservarFiltros(
                redirectAttributes, estado, vendedor, producto,
                fechaInicio, fechaFin
        );

        return "redirect:/admin/solicitudes-precio";
    }

    // Conservar filtros después de autorizar o rechazar
    private void conservarFiltros(
            RedirectAttributes redirectAttributes,
            String estado,
            String vendedor,
            String producto,
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        if (estado != null && !estado.isBlank()) {
            redirectAttributes.addAttribute("estado", estado);
        }

        if (vendedor != null && !vendedor.isBlank()) {
            redirectAttributes.addAttribute("vendedor", vendedor);
        }

        if (producto != null && !producto.isBlank()) {
            redirectAttributes.addAttribute("producto", producto);
        }

        if (fechaInicio != null) {
            redirectAttributes.addAttribute("fechaInicio", fechaInicio);
        }

        if (fechaFin != null) {
            redirectAttributes.addAttribute("fechaFin", fechaFin);
        }
    }

    // Comparación de texto sin distinguir mayúsculas
    private boolean contiene(String valor, String busqueda) {
        return valor != null
                && valor.toLowerCase(Locale.ROOT).contains(busqueda);
    }
}
