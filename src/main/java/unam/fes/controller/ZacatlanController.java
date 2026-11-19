
package unam.fes.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import unam.fes.model.LoginRequestDTO;
import unam.fes.model.VendedorDTO;
import unam.fes.service.AuthClienteService;
import unam.fes.service.HistorialClienteService;
import unam.fes.service.VendedorClienteService;
import unam.fes.service.BloqueClienteService;
import unam.fes.service.VentaClienteService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ZacatlanController {

    private final AuthClienteService authClienteService;
    private final HistorialClienteService historialClienteService;
    private final VendedorClienteService vendedorClienteService;
    private final BloqueClienteService bloqueClienteService;
    private final VentaClienteService ventaClienteService;

    public ZacatlanController(
            AuthClienteService authClienteService,
            HistorialClienteService historialClienteService,
            VendedorClienteService vendedorClienteService,
            BloqueClienteService bloqueClienteService,
            VentaClienteService ventaClienteService) {

        this.authClienteService = authClienteService;
        this.historialClienteService = historialClienteService;
        this.vendedorClienteService = vendedorClienteService;
        this.bloqueClienteService = bloqueClienteService;
        this.ventaClienteService = ventaClienteService;
    }

    @GetMapping("/")
    public String inicio() {
        return "inicio";
    }

    @GetMapping("/login")
    public String mostrarLogin(Model model) {
        model.addAttribute("loginRequest", new LoginRequestDTO());
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(
            LoginRequestDTO loginRequest,
            HttpSession session,
            Model model) {

        session.removeAttribute("usuarioLogueado");
        session.removeAttribute("rol");

        String rol = authClienteService.autenticar(loginRequest, session);

        if (rol == null) {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
            model.addAttribute("loginRequest", loginRequest);
            return "login";
        }

        session.setAttribute("usuarioLogueado", loginRequest.getUsername());
        session.setAttribute("rol", rol);

        if ("ADMINISTRADOR".equals(rol)) {
            return "redirect:/admin";
        }

        if ("VENDEDOR".equals(rol)) {
            return "redirect:/vendedor";
        }

        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/registro-vendedor")
    public String mostrarRegistro(Model model) {

        model.addAttribute("vendedor", new VendedorDTO());

        cargarBloques(model);

        return "registro-vendedor";
    }

    @PostMapping("/registro-vendedor")
    public String registrarVendedor(
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String telefono,
            @RequestParam Long idBloque,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmarPassword,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (nombre.isBlank() || apellido.isBlank()
                || telefono.isBlank() || username.isBlank()
                || password.isBlank() || confirmarPassword.isBlank()) {

            model.addAttribute("error", "Todos los campos son obligatorios.");
            cargarBloques(model);
            return "registro-vendedor";
        }

        if (!password.equals(confirmarPassword)) {
            model.addAttribute("error", "Las contraseñas no coinciden.");
            cargarBloques(model);
            return "registro-vendedor";
        }

        if (password.length() < 8 || password.length() > 72
                || password.contains(" ")) {
            model.addAttribute(
                    "error",
                    "La contraseña debe tener entre 8 y 72 caracteres y no contener espacios."
            );
            cargarBloques(model);
            return "registro-vendedor";
        }

        try {
            vendedorClienteService.registrarSolicitud(
                    nombre.trim(),
                    apellido.trim(),
                    telefono.trim(),
                    idBloque,
                    username.trim(),
                    password
            );

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Solicitud registrada exitosamente. Espera la aprobación del administrador."
            );

            return "redirect:/login";

        } catch (RestClientResponseException e) {

            String mensaje = e.getResponseBodyAsString();

            model.addAttribute(
                    "error",
                    mensaje == null || mensaje.isBlank()
                            ? "No se pudo registrar la solicitud."
                            : mensaje
            );

            cargarBloques(model);
            return "registro-vendedor";
        }
    }

    private void cargarBloques(Model model) {
        try {
            model.addAttribute(
                    "bloques",
                    bloqueClienteService.listarBloques().stream()
                            .filter(bloque ->
                                    Boolean.TRUE.equals(bloque.get("activo")))
                            .toList()
            );
        } catch (RestClientResponseException e) {
            model.addAttribute("bloques", List.of());
            model.addAttribute(
                    "error",
                    "No se pudieron cargar los bloques disponibles."
            );
        }
    }

    @GetMapping("/admin")
    public String panelAdmin(HttpSession session, Model model) {

        if (!"ADMINISTRADOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        try {
            model.addAttribute(
                    "solicitudes",
                    vendedorClienteService.listarSolicitudesPendientes()
            );
        } catch (RestClientResponseException e) {
            model.addAttribute("solicitudes", List.of());
            model.addAttribute(
                    "error",
                    "No se pudieron cargar las solicitudes pendientes."
            );
        }

        return "admin";
    }

    @PostMapping("/admin/aprobar/{id}")
    public String aprobarSolicitud(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!"ADMINISTRADOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        try {
            vendedorClienteService.aprobarSolicitud(id);
            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Solicitud de vendedor aprobada correctamente."
            );
        } catch (RestClientResponseException e) {
            String mensaje = e.getResponseBodyAsString();

            redirectAttributes.addFlashAttribute(
                    "error",
                    mensaje == null || mensaje.isBlank()
                            ? "No se pudo aprobar la solicitud."
                            : mensaje
            );
        }

        return "redirect:/admin";
    }

    @PostMapping("/admin/rechazar/{id}")
    public String rechazarSolicitud(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!"ADMINISTRADOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        try {
            vendedorClienteService.rechazarSolicitud(id);
            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Solicitud de vendedor rechazada correctamente."
            );
        } catch (RestClientResponseException e) {
            String mensaje = e.getResponseBodyAsString();

            redirectAttributes.addFlashAttribute(
                    "error",
                    mensaje == null || mensaje.isBlank()
                            ? "No se pudo rechazar la solicitud."
                            : mensaje
            );
        }

        return "redirect:/admin";
    }

    @GetMapping("/admin/usuarios")
    public String mostrarUsuarios(HttpSession session, Model model) {

        if (!"ADMINISTRADOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        model.addAttribute(
                "vendedores",
                vendedorClienteService.listarVendedores()
        );

        return "admin-usuarios";
    }

    @PostMapping("/admin/usuarios/{id}/estado")
    public String cambiarEstadoVendedor(
            @PathVariable Long id,
            @RequestParam boolean activar,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!"ADMINISTRADOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        try {
            vendedorClienteService.cambiarEstado(id, activar);

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    activar
                            ? "Vendedor activado correctamente."
                            : "Vendedor desactivado correctamente."
            );

        } catch (RestClientResponseException e) {

            String mensaje = e.getResponseBodyAsString();

            redirectAttributes.addFlashAttribute(
                    "error",
                    mensaje == null || mensaje.isBlank()
                            ? "No se pudo cambiar el estado del vendedor."
                            : mensaje
            );
        }

        return "redirect:/admin/usuarios";
    }

    @PostMapping("/admin/usuarios/{id}/editar")
    public String editarVendedor(
            @PathVariable Long id,
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String telefono,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!"ADMINISTRADOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        if (nombre.isBlank() || apellido.isBlank() || telefono.isBlank()) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Todos los campos son obligatorios."
            );
            return "redirect:/admin/usuarios";
        }

        try {
            vendedorClienteService.modificarVendedor(
                    id,
                    nombre.trim(),
                    apellido.trim(),
                    telefono.trim()
            );

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Vendedor actualizado correctamente."
            );

        } catch (RestClientResponseException e) {

            String mensaje = e.getResponseBodyAsString();

            redirectAttributes.addFlashAttribute(
                    "error",
                    mensaje == null || mensaje.isBlank()
                            ? "No se pudo actualizar el vendedor."
                            : mensaje
            );
        }

        return "redirect:/admin/usuarios";
    }


    @GetMapping("/admin/historial")
    public String mostrarHistorial(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaInicio,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaFin,

            @RequestParam(required = false) String administrador,
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) String entidad,
            @RequestParam(required = false) String buscar,

            HttpSession session,
            Model model) {

        if (!"ADMINISTRADOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        try {
            List<unam.fes.model.HistorialDTO> historial =
                    historialClienteService.listarHistorial();

            // Opciones disponibles para los filtros.
            model.addAttribute(
                    "administradores",
                    historial.stream()
                            .map(unam.fes.model.HistorialDTO::getAdministrador)
                            .filter(valor -> valor != null && !valor.isBlank())
                            .distinct()
                            .sorted()
                            .toList()
            );

            model.addAttribute(
                    "acciones",
                    historial.stream()
                            .map(unam.fes.model.HistorialDTO::getAccion)
                            .filter(valor -> valor != null && !valor.isBlank())
                            .distinct()
                            .sorted()
                            .toList()
            );

            model.addAttribute(
                    "entidades",
                    historial.stream()
                            .map(unam.fes.model.HistorialDTO::getEntidad)
                            .filter(valor -> valor != null && !valor.isBlank())
                            .distinct()
                            .sorted()
                            .toList()
            );

            // Validar que se indiquen ambas fechas.
            if ((fechaInicio == null && fechaFin != null)
                    || (fechaInicio != null && fechaFin == null)) {

                model.addAttribute(
                        "error",
                        "Para consultar por rango debes indicar ambas fechas."
                );

            } else if (fechaInicio != null
                    && fechaInicio.isAfter(fechaFin)) {

                model.addAttribute(
                        "error",
                        "La fecha inicial no puede ser posterior a la fecha final."
                );

            } else if ((fechaInicio != null
                    && fechaInicio.isAfter(LocalDate.now()))
                    || (fechaFin != null
                    && fechaFin.isAfter(LocalDate.now()))) {

                model.addAttribute(
                        "error",
                        "No puedes consultar fechas futuras."
                );

            } else {

                // Aplicar filtros.
                historial = historial.stream()
                        .filter(m -> fechaInicio == null
                                || (m.getFechaHora() != null
                                && !m.getFechaHora().toLocalDate()
                                .isBefore(fechaInicio)))
                        .filter(m -> fechaFin == null
                                || (m.getFechaHora() != null
                                && !m.getFechaHora().toLocalDate()
                                .isAfter(fechaFin)))
                        .filter(m -> administrador == null
                                || administrador.isBlank()
                                || administrador.equalsIgnoreCase(
                                m.getAdministrador()))
                        .filter(m -> accion == null
                                || accion.isBlank()
                                || accion.equalsIgnoreCase(m.getAccion()))
                        .filter(m -> entidad == null
                                || entidad.isBlank()
                                || entidad.equalsIgnoreCase(m.getEntidad()))
                        .filter(m -> buscar == null
                                || buscar.isBlank()
                                || coincideTexto(m, buscar))
                        .toList();
            }

            model.addAttribute("historial", historial);

        } catch (RestClientResponseException e) {

            model.addAttribute("historial", List.of());
            model.addAttribute("administradores", List.of());
            model.addAttribute("acciones", List.of());
            model.addAttribute("entidades", List.of());

            model.addAttribute(
                    "error",
                    "No se pudo consultar la bitácora administrativa."
            );
        }

        // Conservar los valores seleccionados.
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("administradorSeleccionado", administrador);
        model.addAttribute("accionSeleccionada", accion);
        model.addAttribute("entidadSeleccionada", entidad);
        model.addAttribute("buscar", buscar);

        return "admin-historial";
    }

    // Buscar texto en los datos del movimiento.
    private boolean coincideTexto(
            unam.fes.model.HistorialDTO movimiento,
            String texto) {

        String busqueda = texto.trim().toLowerCase();

        return contiene(movimiento.getDescripcion(), busqueda)
                || contiene(movimiento.getDatosAnteriores(), busqueda)
                || contiene(movimiento.getDatosNuevos(), busqueda)
                || contiene(movimiento.getAdministrador(), busqueda)
                || contiene(movimiento.getAccion(), busqueda)
                || contiene(movimiento.getEntidad(), busqueda)
                || (movimiento.getIdMovimiento() != null
                && movimiento.getIdMovimiento().toString().contains(busqueda))
                || (movimiento.getIdRegistro() != null
                && movimiento.getIdRegistro().toString().contains(busqueda));
    }

    private boolean contiene(String valor, String busqueda) {
        return valor != null
                && valor.toLowerCase().contains(busqueda);
    }




    // REPORTE DIARIO Y POR RANGO DE VENTAS
    @GetMapping("/admin/reportes/ventas")
    public String reporteDiario(
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
            String tipoReporte,

            @RequestParam(required = false)
            String producto,

            @RequestParam(required = false)
            Long idVendedor,

            HttpSession session,
            Model model) {

        if (!"ADMINISTRADOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        if (tipoReporte == null || tipoReporte.isBlank()) {
            tipoReporte = fechaInicio != null || fechaFin != null
                    ? "rango"
                    : "diario";
        }

        if (fecha == null) {
            fecha = LocalDate.now();
        }

        if ("rango".equals(tipoReporte)
                && (fechaInicio == null || fechaFin == null)) {

            model.addAttribute(
                    "error",
                    "Para consultar por rango debes indicar ambas fechas."
            );

            prepararReporteVacio(model, tipoReporte, fecha,
                    fechaInicio, fechaFin, producto, idVendedor);

            return "admin-reporte-diario";
        }

        if ("rango".equals(tipoReporte)
                && fechaInicio.isAfter(fechaFin)) {

            model.addAttribute(
                    "error",
                    "La fecha inicial no puede ser posterior a la fecha final."
            );

            prepararReporteVacio(model, tipoReporte, fecha,
                    fechaInicio, fechaFin, producto, idVendedor);

            return "admin-reporte-diario";
        }

        if ("rango".equals(tipoReporte)
                && fechaFin.isAfter(LocalDate.now())) {

            model.addAttribute(
                    "error",
                    "No puedes consultar rangos que incluyan fechas futuras."
            );

            prepararReporteVacio(model, tipoReporte, fecha,
                    fechaInicio, fechaFin, producto, idVendedor);

            return "admin-reporte-diario";
        }

        if ("diario".equals(tipoReporte)
                && fecha.isAfter(LocalDate.now())) {

            model.addAttribute(
                    "error",
                    "No puedes consultar fechas futuras."
            );

            prepararReporteVacio(model, tipoReporte, fecha,
                    fechaInicio, fechaFin, producto, idVendedor);

            return "admin-reporte-diario";
        }

        try {

            List<Map<String, Object>> ventas;

            if ("rango".equals(tipoReporte)) {

                if (idVendedor != null) {
                    ventas = ventaClienteService.listarVentasPorVendedorYRango(
                            idVendedor,
                            fechaInicio,
                            fechaFin
                    );
                } else {
                    ventas = ventaClienteService.listarVentasPorRango(
                            fechaInicio,
                            fechaFin
                    );
                }

            } else {

                if (idVendedor != null) {
                    ventas = ventaClienteService.listarVentasPorVendedorYDia(
                            idVendedor,
                            fecha
                    );
                } else {
                    ventas = ventaClienteService.listarVentasPorDia(fecha);
                }
            }

            ventas = filtrarVentasPorProducto(ventas, producto);

            BigDecimal total = BigDecimal.ZERO;
            Map<String, BigDecimal> ventasPorVendedor =
                    new LinkedHashMap<>();

            for (Map<String, Object> venta : ventas) {

                Object valorTotal = venta.get("total");

                BigDecimal totalVenta = valorTotal instanceof BigDecimal
                        ? (BigDecimal) valorTotal
                        : new BigDecimal(valorTotal.toString());

                total = total.add(totalVenta);

                String nombreVendedor =
                        String.valueOf(venta.get("nombreVendedor"));

                ventasPorVendedor.merge(
                        nombreVendedor,
                        totalVenta,
                        BigDecimal::add
                );
            }

            model.addAttribute("fecha", fecha);
            model.addAttribute("fechaInicio", fechaInicio);
            model.addAttribute("fechaFin", fechaFin);
            model.addAttribute("tipoReporte", tipoReporte);
            model.addAttribute("producto", producto);
            model.addAttribute("idVendedor", idVendedor);

            model.addAttribute("ventas", ventas);
            model.addAttribute("totalDia", total);
            model.addAttribute("totalRango", total);
            model.addAttribute("cantidadVentas", ventas.size());
            model.addAttribute("ventasPorVendedor", ventasPorVendedor);

            model.addAttribute(
                    "vendedores",
                    ventaClienteService.listarVendedores()
            );

        } catch (RestClientResponseException e) {

            prepararReporteVacio(model, tipoReporte, fecha,
                    fechaInicio, fechaFin, producto, idVendedor);

            model.addAttribute(
                    "error",
                    "No se pudo consultar el reporte de ventas."
            );
        }

        return "admin-reporte-diario";
    }

    // Filtrar las ventas por nombre de producto
    private List<Map<String, Object>> filtrarVentasPorProducto(
            List<Map<String, Object>> ventas,
            String producto) {

        if (producto == null || producto.isBlank()) {
            return ventas;
        }

        String productoBuscado = producto.trim().toLowerCase();

        return ventas.stream()
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
                .toList();
    }

    // Preparar valores cuando no hay resultados o existe un error
    private void prepararReporteVacio(
            Model model,
            String tipoReporte,
            LocalDate fecha,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String producto,
            Long idVendedor) {

        model.addAttribute("fecha", fecha);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("tipoReporte", tipoReporte);
        model.addAttribute("producto", producto);
        model.addAttribute("idVendedor", idVendedor);

        model.addAttribute("ventas", List.of());
        model.addAttribute("totalDia", BigDecimal.ZERO);
        model.addAttribute("totalRango", BigDecimal.ZERO);
        model.addAttribute("cantidadVentas", 0);
        model.addAttribute("ventasPorVendedor", Map.of());

        try {
            model.addAttribute(
                    "vendedores",
                    ventaClienteService.listarVendedores()
            );
        } catch (RestClientResponseException e) {
            model.addAttribute("vendedores", List.of());
        }
    }


    // DESCARGAR REPORTE DIARIO EN PDF
    @GetMapping("/admin/reportes/ventas/pdf")
    public ResponseEntity<byte[]> descargarReportePdf(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha,

            @RequestParam(required = false)
            String producto,

            @RequestParam(required = false)
            Long idVendedor,

            HttpSession session) {

        if (!"ADMINISTRADOR".equals(session.getAttribute("rol"))) {
            return ResponseEntity.status(403).build();
        }

        if (fecha == null) {
            fecha = LocalDate.now();
        }

        if (fecha.isAfter(LocalDate.now())) {
            return ResponseEntity.badRequest().build();
        }

        byte[] pdf = ventaClienteService.descargarReportePdf(
                fecha,
                producto,
                idVendedor
        );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=reporte-ventas-" + fecha + ".pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }


    // DESCARGAR REPORTE POR RANGO EN PDF
    @GetMapping("/admin/reportes/ventas/pdf/rango")
    public ResponseEntity<byte[]> descargarReportePdfRango(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaInicio,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaFin,

            @RequestParam(required = false)
            String producto,

            @RequestParam(required = false)
            Long idVendedor,

            HttpSession session) {

        if (!"ADMINISTRADOR".equals(session.getAttribute("rol"))) {
            return ResponseEntity.status(403).build();
        }

        if (fechaInicio.isAfter(fechaFin)
                || fechaFin.isAfter(LocalDate.now())) {
            return ResponseEntity.badRequest().build();
        }

        byte[] pdf = ventaClienteService.descargarReportePdfRango(
                fechaInicio,
                fechaFin,
                producto,
                idVendedor
        );

        String nombreArchivo = "reporte-ventas-"
                + fechaInicio + "-a-" + fechaFin + ".pdf";

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + nombreArchivo
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }


    @GetMapping("/vendedor")
    public String panelVendedor(HttpSession session) {

        if (!"VENDEDOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        return "vendedor";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
