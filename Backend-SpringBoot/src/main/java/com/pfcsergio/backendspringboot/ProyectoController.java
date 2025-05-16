package com.pfcsergio.backendspringboot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ProyectoController {

    private final IProyectoService service;

    @ResponseBody
    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping("/test-regresion")
    public ResponseEntity<List<String>> findFilesByName() {
        List<String> result = new ArrayList<>();
        try {
            result = service.findFilesByName();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @DeleteMapping("/delete-test")
    public ResponseEntity<Boolean> deleteFile(@RequestBody List<String> pruebas) {
        try {
            service.deletePruebas(pruebas);
            return new ResponseEntity<>(true, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @PostMapping("/upload-file")
    public ResponseEntity<String> uploadFiles(@RequestParam("files") List<MultipartFile> files) {
        try {
            System.out.println("Número de archivos recibidos: " + files.size());
            // Guardar los archivos
            for (MultipartFile file : files) {
                System.out.println("Archivo recibido: " + file.getOriginalFilename());
                service.guardarArchivos(file);
            }
            return ResponseEntity.ok("Archivos subidos correctamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al subir los archivos.");
        }
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @PostMapping("/ejecutar-script")
    public ResponseEntity<String> ejecutarScript(@RequestBody List<String> archivosSeleccionados) {
        try {
            service.ejecutarPruebas(archivosSeleccionados);
            return ResponseEntity.ok("Script ejecutado correctamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al ejecutar el script: " + e.getMessage());
        }
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping("/get-contenido")
    public ResponseEntity<Map<String, String>> obtenerContenidoArchivo(@RequestParam String nombreArchivo) {
        System.out.println("Recibiendo archivo: " + nombreArchivo);  // Verifica si el nombre del archivo se recibe correctamente

        try {
            String contenido = service.leerContenidoArchivo(nombreArchivo);
            Map<String, String> response = new HashMap<>();
            response.put("contenido", contenido);
            return ResponseEntity.ok(response);
        } catch (FileNotFoundException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());  // Más detalles sobre el error
            Map<String, String> error = new HashMap<>();
            error.put("error", "Archivo no encontrado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());  // Más detalles sobre el error
            Map<String, String> error = new HashMap<>();
            error.put("error", "No se pudo leer el archivo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @PostMapping("/guardar-contenido")
    public ResponseEntity<Map<String, String>> guardarContenidoArchivo(@RequestBody Map<String, String> request) {
        String nombreArchivo = request.get("nombreArchivo");
        String contenido = request.get("contenido");
        try {
            service.guardarContenidoArchivo(nombreArchivo, contenido);  // Llamamos al servicio para guardar el archivo
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Contenido guardado correctamente");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "No se pudo guardar el archivo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
