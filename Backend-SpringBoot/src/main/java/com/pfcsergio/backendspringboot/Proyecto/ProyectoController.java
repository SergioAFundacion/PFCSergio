package com.pfcsergio.backendspringboot.Proyecto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ProyectoController {

    private final IProyectoService service;

    @ResponseBody
    @GetMapping("/test-regresion")
    public ResponseEntity<List<CarpetasDTO>> findByZonas() {
        List<CarpetasDTO> result = new ArrayList<>();
        try {
            result = service.findFilesByName();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(value = "/test-regresion/{folder}/{file:.+}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getTestRegressionFile(@PathVariable String folder, @PathVariable String file) throws IOException {
        Path filePath = Paths.get("C:/Users/sergi/Desktop/JSONS", folder, file);
        if (!Files.exists(filePath)) {
            return ResponseEntity.status(404).body("{\"error\": \"File not found\"}");
        }

        String content = Files.readString(filePath);
        return ResponseEntity.ok(content);
    }


    @GetMapping("/test-regresion/{fileName}")
    public ResponseEntity<String> getJsonFileContent(@PathVariable String fileName) {
        try {
            String content = service.leerContenidoArchivo(fileName);
            System.out.println(">> Leyendo archivo: " + fileName);
            System.out.println(">> Contenido: " + content);
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(content);
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Archivo no encontrado: " + fileName);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al leer archivo: " + e.getMessage());
        }
    }


    @PostMapping("/delete-carpeta")
    public ResponseEntity<Boolean> deleteFile(@RequestBody List<String> pruebas) {
        try {
            service.deletePruebas(pruebas);
            return new ResponseEntity<>(true, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/upload-carpeta")
    public ResponseEntity<String> uploadCarpeta(
            @RequestParam("nombreCarpeta") String nombreCarpeta,
            @RequestParam("files") List<MultipartFile> files) {

        if (nombreCarpeta == null || nombreCarpeta.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El nombre de la carpeta es obligatorio.");
        }

        if (files == null || files.isEmpty() || files.stream().allMatch(MultipartFile::isEmpty)) {
            return ResponseEntity.badRequest().body("Debe subir al menos un archivo válido.");
        }

        try {
            service.guardarArchivosEnCarpeta(nombreCarpeta.trim(), files);
            return ResponseEntity.ok("Carpeta y archivos subidos correctamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al subir los archivos en la carpeta.");
        }
    }

    @GetMapping("/get-contenido")
    public ResponseEntity<Map<String, String>> obtenerContenidoArchivo(@RequestParam String nombreArchivo) {
        System.out.println("Recibiendo archivo: " + nombreArchivo);
        try {
            String contenido = service.leerContenidoArchivo(nombreArchivo);
            Map<String, String> response = new HashMap<>();
            response.put("contenido", contenido);
            return ResponseEntity.ok(response);
        } catch (FileNotFoundException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Archivo no encontrado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "No se pudo leer el archivo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/guardar-contenido")
    public ResponseEntity<Map<String, String>> guardarContenidoArchivo(@RequestBody Map<String, String> request) {
        String nombreArchivo = request.get("nombreArchivo");
        String contenido = request.get("contenido");
        try {
            service.guardarContenidoArchivo(nombreArchivo, contenido);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Contenido guardado correctamente");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "No se pudo guardar el archivo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/rename-carpeta")
    public ResponseEntity<Map<String, String>> renameCarpeta(@RequestBody Map<String, String> request) {
        String oldName = request.get("oldName");
        String newName = request.get("newName");

        System.out.println("Recibiendo solicitud: oldName=" + oldName + ", newName=" + newName);

        if (oldName == null || newName == null || oldName.trim().isEmpty() || newName.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Los nombres de carpeta son obligatorios y no pueden estar vacíos");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        if (oldName.contains("/") || oldName.contains("\\") || newName.contains("/") || newName.contains("\\")) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Los nombres no pueden contener / ni \\");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        try {
            service.renameCarpeta(oldName, newName);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Carpeta renombrada correctamente");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "No se pudo renombrar la carpeta: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}