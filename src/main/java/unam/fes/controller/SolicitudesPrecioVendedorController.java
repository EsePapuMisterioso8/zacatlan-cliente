
package unam.fes.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import unam.fes.model.InventarioVendedorDTO;
import unam.fes.service.InventarioClienteService;
import unam.fes.service.SolicitudPrecioClienteService;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/vendedor/solicitudes-precio")
public class SolicitudesPrecioVendedorController {

    private final SolicitudPrecioClienteService solicitudService;
    private final InventarioClienteService inventarioService;

    public SolicitudesPrecioVendedorController(
            SolicitudPrecioClienteService solicitudService,
            InventarioClienteService inventarioService) {

        this.solicitudService = solicitudService;
        this.inventarioService = inventarioService;
    }

    // Consultar solicitudes propias
    @GetMapping
    public String mostrarSolicitudes(
            HttpSession session,
            Model model) {

        if (!"VENDEDOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        try {
            model.addAttribute(
                    "solicitudes",
                    solicitudService.listarMisSolicitudes()
            );

            model.addAttribute(
                    "inventario",
                    inventarioService.listarInventarioPropio()
            );

        } catch (RestClientException e) {

            model.addAttribute("solicitudes", List.of());
            model.addAttribute("inventario", List.of());

            model.addAttribute(
                    "error",
                    "No fue posible consultar la información. Intenta nuevamente."
            );
        }

        return "solicitudes-precio-vendedor";
    }

    // Enviar una nueva solicitud de cambio de precio
    @PostMapping("/crear")
    public String crearSolicitud(
            @RequestParam Long idProductoVendedor,
            @RequestParam BigDecimal precioSolicitado,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!"VENDEDOR".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        try {
            if (idProductoVendedor == null || precioSolicitado == null) {
                redirectAttributes.addFlashAttribute(
                        "error",
                        "Debes seleccionar un producto e indicar el nuevo precio."
                );

                return "redirect:/vendedor/solicitudes-precio";
            }

            if (precioSolicitado.compareTo(BigDecimal.ZERO) <= 0) {
                redirectAttributes.addFlashAttribute(
                        "error",
                        "El precio solicitado debe ser mayor que cero."
                );

                return "redirect:/vendedor/solicitudes-precio";
            }

            solicitudService.crearSolicitud(
                    idProductoVendedor,
                    precioSolicitado
            );

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Solicitud enviada a revisión exitosamente."
            );

        } catch (RestClientResponseException e) {

            String mensaje = e.getResponseBodyAsString();

            redirectAttributes.addFlashAttribute(
                    "error",
                    mensaje.isBlank()
                            ? "No fue posible registrar la solicitud."
                            : mensaje
            );

        } catch (RestClientException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No fue posible comunicarse con la API REST."
            );
        }

        return "redirect:/vendedor/solicitudes-precio";
    }
}
