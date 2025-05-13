package com.pfcsergio.backendspringboot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ProyectoController {

    private final IProyectoService service;

    @ResponseBody
    @CrossOrigin(origins = "http://localhost:5173")  // Asegúrate de que el origen esté bien especificado
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
}
