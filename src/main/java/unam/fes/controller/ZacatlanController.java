package unam.fes.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import unam.fes.model.LoginRequestDTO;
import unam.fes.model.VendedorDTO;
import unam.fes.service.AuthClienteService;

@Controller
public class ZacatlanController {

    private final AuthClienteService authClienteService;

    public ZacatlanController(AuthClienteService authClienteService) {
        this.authClienteService = authClienteService;
    }

    // Página de inicio
    @GetMapping("/")
    public String inicio() {
        return "inicio";
    }

    // Mostrar formulario de login
    @GetMapping("/login")
    public String mostrarLogin(Model model) {
        model.addAttribute("loginRequest", new LoginRequestDTO());
        return "login";
    }

    // Procesar inicio de sesión
    @PostMapping("/login")
    public String procesarLogin(
            LoginRequestDTO loginRequest,
            HttpSession session,
            Model model) {

        session.removeAttribute("usuarioLogueado");
        session.removeAttribute("rol");

        String rol = authClienteService.autenticar(loginRequest, session);

        if (rol == null) {
            model.addAttribute(
                    "error",
                    "Usuario o contraseña incorrectos"
            );
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

    // Mostrar registro de vendedor
    @GetMapping("/registro-vendedor")
    public String mostrarRegistro(Model model) {
        model.addAttribute("vendedor", new VendedorDTO());
        return "registro-vendedor";
    }

    // Panel del administrador
    @GetMapping("/admin")
    public String panelAdmin(HttpSession session) {

        if (!"ADMINISTRADOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        return "admin";
    }

    // Panel del vendedor
    @GetMapping("/vendedor")
    public String panelVendedor(HttpSession session) {

        if (!"VENDEDOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        return "vendedor";
    }

    // Cerrar sesión
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}