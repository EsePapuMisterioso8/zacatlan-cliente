
package unam.fes.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;

import unam.fes.service.VentaClienteService;
import unam.fes.service.VendedorClienteService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/ventas")
public class HistorialVentasAdminController {

    private final VentaClienteService ventaClienteService;
    private final VendedorClienteService vendedorClienteService;

    public HistorialVentasAdminController(
            VentaClienteService ventaClienteService,
            VendedorClienteService vendedorClienteService) {

        this.ventaClienteService = ventaClienteService;
        this.vendedorClienteService = vendedorClienteService;
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

            @RequestParam(required = false)
            String buscarVendedor,

            @RequestParam(required = false)
            String producto,

            HttpSession session,
            Model model) {

        if (!"ADMINISTRADOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        try {
            model.addAttribute(
                    "vendedores",
                    buscarVendedor != null && !buscarVendedor.isBlank()
                            ? vendedorClienteService.buscarVendedores(buscarVendedor)
                            : Collections.emptyList()
            );

            model.addAttribute("buscarVendedor", buscarVendedor);
            model.addAttribute("fecha", fecha);
            model.addAttribute("fechaInicio", fechaInicio);
            model.addAttribute("fechaFin", fechaFin);
            model.addAttribute("idVendedor", idVendedor);
            model.addAttribute("producto", producto);

            LocalDate hoy = LocalDate.now();

            // No permitir fechas futuras.
            if ((fecha != null && fecha.isAfter(hoy))
                    || (fechaInicio != null && fechaInicio.isAfter(hoy))
                    || (fechaFin != null && fechaFin.isAfter(hoy))) {

                model.addAttribute(
                        "error",
                        "No puedes consultar ventas de fechas futuras."
                );
                model.addAttribute("ventas", Collections.emptyList());

                return "historial-ventas-admin";
            }

            List<Map<String, Object>> ventas;

            // Fecha individual: puede combinarse con un vendedor.
            // No puede combinarse con un rango.
            if (fecha != null) {

                if (fechaInicio != null || fechaFin != null) {

                    model.addAttribute(
                            "error",
                            "Para consultar un día específico, no utilices fechas de rango."
                    );
                    model.addAttribute("ventas", Collections.emptyList());

                    return "historial-ventas-admin";

                } else if (idVendedor != null) {

                    ventas = ventaClienteService.listarVentasPorVendedorYDia(
                            idVendedor,
                            fecha
                    );

                } else {

                    ventas = ventaClienteService.listarVentasPorDia(fecha);
                }

                // Para consultar por rango deben indicarse ambas fechas.
            } else if ((fechaInicio == null && fechaFin != null)
                    || (fechaInicio != null && fechaFin == null)) {

                model.addAttribute(
                        "error",
                        "Para consultar por rango debes indicar la fecha inicial y la fecha final."
                );
                model.addAttribute("ventas", Collections.emptyList());

                return "historial-ventas-admin";

                // Validar que el rango esté en orden.
            } else if (fechaInicio != null && fechaFin != null
                    && fechaInicio.isAfter(fechaFin)) {

                model.addAttribute(
                        "error",
                        "La fecha inicial no puede ser posterior a la fecha final."
                );
                model.addAttribute("ventas", Collections.emptyList());

                return "historial-ventas-admin";

                // Vendedor y rango de fechas.
            } else if (idVendedor != null
                    && fechaInicio != null
                    && fechaFin != null) {

                ventas = ventaClienteService.listarVentasPorVendedorYRango(
                        idVendedor,
                        fechaInicio,
                        fechaFin
                );

                // Todas las ventas de un vendedor.
            } else if (idVendedor != null) {

                ventas = ventaClienteService.listarVentasPorVendedor(idVendedor);

                // Ventas de cualquier vendedor dentro del rango.
            } else if (fechaInicio != null && fechaFin != null) {

                ventas = ventaClienteService.listarVentasPorRango(
                        fechaInicio,
                        fechaFin
                );

                // Sin filtros: mostrar todas las ventas.
            } else {

                ventas = ventaClienteService.listarTodasLasVentas();
            }

            // Filtrar las ventas por nombre de producto.
            if (producto != null && !producto.isBlank()) {

                String productoBuscado = producto.trim().toLowerCase();

                ventas = ventas.stream()
                        .filter(venta -> {
                            Object nombres = venta.get("nombresProductos");

                            if (!(nombres instanceof List<?> nombresProductos)) {
                                return false;
                            }

                            return nombresProductos.stream()
                                    .filter(String.class::isInstance)
                                    .map(String.class::cast)
                                    .anyMatch(nombre ->
                                            nombre.toLowerCase().contains(productoBuscado)
                                    );
                        })
                        .collect(Collectors.toList());
            }

            model.addAttribute("ventas", ventas);

            return "historial-ventas-admin";

        } catch (RestClientException e) {

            model.addAttribute(
                    "error",
                    "No fue posible consultar el historial de ventas."
            );

            model.addAttribute("ventas", Collections.emptyList());
            model.addAttribute("vendedores", Collections.emptyList());

            return "historial-ventas-admin";
        }
    }
}
