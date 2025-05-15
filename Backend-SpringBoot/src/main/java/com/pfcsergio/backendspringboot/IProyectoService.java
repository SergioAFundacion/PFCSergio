package com.pfcsergio.backendspringboot;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IProyectoService {

    List<String> findFilesByName();

    void deletePruebas(List<String> pruebas);

    void guardarArchivos(MultipartFile archivo);

    void ejecutarPruebas(List<String> archivos);

}
