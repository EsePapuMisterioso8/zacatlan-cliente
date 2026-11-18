
package unam.fes.controller;

import jakarta.servlet.http.HttpSession;
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

@Controller
public class ZacatlanController {

    private final AuthClienteService authClienteService;
    private final HistorialClienteService historialClienteService;
    private final VendedorClienteService vendedorClienteService;
    private final BloqueClienteService bloqueClienteService;

    public ZacatlanController(
            AuthClienteService authClienteService,
            HistorialClienteService historialClienteService,
            VendedorClienteService vendedorClienteService,
            BloqueClienteService bloqueClienteService) {

        this.authClienteService = authClienteService;
        this.historialClienteService = historialClienteService;
        this.vendedorClienteService = vendedorClienteService;
        this.bloqueClienteService = bloqueClienteService;
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
            model.addAttribute("bloques", java.util.List.of());
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
            model.addAttribute("solicitudes", java.util.List.of());
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
    public String mostrarHistorial(HttpSession session, Model model) {

        if (!"ADMINISTRADOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        model.addAttribute(
                "historial",
                historialClienteService.listarHistorial()
        );

        return "admin-historial";
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
