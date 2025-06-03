package com.pfcsergio.backendspringboot.Proyecto;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IProyectoService {

    List<CarpetasDTO> findFilesByName();

    void deletePruebas(List<String> pruebas);

    void guardarArchivosEnCarpeta(String nombreCarpeta, List<MultipartFile> archivos);

    String leerContenidoArchivo(String nombreArchivo) throws IOException;

    void guardarContenidoArchivo(String nombreArchivo, String contenido) throws IOException;

    void renameCarpeta(String oldName, String newName) throws IOException;

}