package com.pfcsergio.backendspringboot.FiltrosProyecto;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FiltrosProyectoController {

    private final IFiltrosProyectoService service;

    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping("/obtener-archivos-filtro/{nombre}")
    public ResponseEntity<List<String>> obtenerArchivosFiltro(@PathVariable String nombre) {
        try {
            List<String> archivos = service.findArchivosPorNombreFiltro(nombre);
            return ResponseEntity.ok(archivos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping("/listar-nombres-filtros")
    public ResponseEntity<List<String>> listarNombresFiltros() {
        List<String> nombres = service.findAllNombresFiltros();
        return ResponseEntity.ok(nombres);
    }

    @Autowired
    private IFiltrosProyectoService filtrosProyectoService;

    @CrossOrigin(origins = "http://localhost:5173")
    @PostMapping("/guardar-filtro")
    public ResponseEntity<String> guardarFiltro(@RequestBody FiltrosProyectoDTO filtro) {
        System.out.println("Datos recibidos: " + filtro);
        if (filtro.getNombre() == null || filtro.getArchivos() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Datos incompletos");
        }

        String mensaje = filtrosProyectoService.guardarFiltro(filtro);

        if ("Filtro guardado con éxito.".equals(mensaje)) {
            return ResponseEntity.ok(mensaje);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mensaje);
        }
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @DeleteMapping("/eliminar-filtro/{nombre}")
    public ResponseEntity<String> eliminarFiltro(@PathVariable String nombre) {
        try {
            boolean eliminado = filtrosProyectoService.eliminarFiltroPorNombre(nombre);
            if (eliminado) {
                return ResponseEntity.ok("Filtro eliminado correctamente.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Filtro no encontrado.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar el filtro.");
        }
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @PutMapping("/editar-filtros")
    public ResponseEntity<String> actualizarFiltro(@RequestBody FiltrosProyectoDTO filtro) {
        try {
            filtrosProyectoService.actualizarFiltro(filtro);
            return ResponseEntity.ok("Filtro actualizado correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al actualizar filtro: " + e.getMessage());
        }
    }

}
