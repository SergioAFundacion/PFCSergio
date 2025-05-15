package com.pfcsergio.backendspringboot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

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
            // Guardar los archivos
            for (MultipartFile file : files) {
                service.guardarArchivo(file);
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
}
