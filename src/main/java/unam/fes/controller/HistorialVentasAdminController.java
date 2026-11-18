
package unam.fes.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import unam.fes.service.VentaClienteService;

import java.time.LocalDate;
import java.util.Collections;

@Controller
@RequestMapping("/admin/ventas")
public class HistorialVentasAdminController {

    private final VentaClienteService ventaClienteService;

    public HistorialVentasAdminController(
            VentaClienteService ventaClienteService) {
        this.ventaClienteService = ventaClienteService;
    }

    @GetMapping
    public String listarVentas(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaInicio,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaFin,

            @RequestParam(required = false)
            Long idVendedor,

            HttpSession session,
            Model model) {

        if (!"ADMINISTRADOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        try {
            model.addAttribute(
                    "vendedores",
                    ventaClienteService.listarVendedores()
            );

            model.addAttribute("fecha", fecha);
            model.addAttribute("fechaInicio", fechaInicio);
            model.addAttribute("fechaFin", fechaFin);
            model.addAttribute("idVendedor", idVendedor);

            if (fecha == null
                    && fechaInicio == null
                    && fechaFin == null
                    && idVendedor == null) {

                model.addAttribute(
                        "ventas",
                        ventaClienteService.listarTodasLasVentas()
                );

            } else if (fecha != null && idVendedor == null
                    && fechaInicio == null && fechaFin == null) {

                model.addAttribute(
                        "ventas",
                        ventaClienteService.listarVentasPorDia(fecha)
                );

            } else if (idVendedor != null
                    && fechaInicio != null
                    && fechaFin != null
                    && fecha == null) {

                model.addAttribute(
                        "ventas",
                        ventaClienteService.listarVentasPorVendedorYRango(
                                idVendedor,
                                fechaInicio,
                                fechaFin
                        )
                );

            } else if (idVendedor != null
                    && fechaInicio == null
                    && fechaFin == null
                    && fecha == null) {

                model.addAttribute(
                        "ventas",
                        ventaClienteService.listarVentasPorVendedor(idVendedor)
                );

            } else if (idVendedor == null
                    && fecha == null
                    && fechaInicio != null
                    && fechaFin != null) {

                model.addAttribute(
                        "ventas",
                        ventaClienteService.listarVentasPorRango(
                                fechaInicio,
                                fechaFin
                        )
                );

            } else {
                model.addAttribute("ventas", Collections.emptyList());
                model.addAttribute(
                        "error",
                        "Selecciona un filtro válido. Para consultar por rango debes indicar ambas fechas."
                );
            }

            return "historial-ventas-admin";

        } catch (RestClientException e) {
            model.addAttribute(
                    "error",
                    "No fue posible consultar el historial de ventas."
            );

            model.addAttribute("ventas", Collections.emptyList());

            return "historial-ventas-admin";
        }
    }

    // Descargar reporte PDF consumido desde la API
    @GetMapping("/reporte/pdf")
    public ResponseEntity<?> descargarReportePdf(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha,
            HttpSession session) {

        if (!"ADMINISTRADOR".equals(session.getAttribute("rol"))) {
            return ResponseEntity.status(403).body("Acceso no autorizado.");
        }

        if (fecha.isAfter(LocalDate.now())) {
            return ResponseEntity.badRequest()
                    .body("No se pueden descargar reportes de fechas futuras.");
        }

        try {
            byte[] pdf = ventaClienteService.descargarReportePdf(fecha);

            String nombreArchivo = "reporte-ventas-" + fecha + ".pdf";

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + nombreArchivo + "\""
                    )
                    .body(pdf);

        } catch (RestClientException e) {
            return ResponseEntity.internalServerError()
                    .body("No fue posible obtener el reporte PDF desde la API.");
        }
    }
}
