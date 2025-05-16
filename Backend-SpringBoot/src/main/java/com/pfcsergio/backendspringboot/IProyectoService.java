package com.pfcsergio.backendspringboot;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IProyectoService {

    List<String> findFilesByName();

    void deletePruebas(List<String> pruebas);

    void guardarArchivos(MultipartFile archivo);

    void ejecutarPruebas(List<String> archivos);

    String leerContenidoArchivo(String nombreArchivo) throws IOException;

    void guardarContenidoArchivo(String nombreArchivo, String contenido) throws IOException;

}
