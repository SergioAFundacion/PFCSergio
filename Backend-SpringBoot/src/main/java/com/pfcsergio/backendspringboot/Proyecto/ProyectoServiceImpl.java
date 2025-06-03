package com.pfcsergio.backendspringboot.Proyecto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProyectoServiceImpl implements IProyectoService {

    @Override
    public List<CarpetasDTO> findFilesByName() {
        String path = "C:/Users/sergi/Desktop/JSONS";
        File rootFolder = new File(path);

        if (!rootFolder.exists() || !rootFolder.isDirectory()) {
            log.error("La carpeta especificada no existe o no es un directorio: {}", path);
            return Collections.emptyList();
        }

        File[] folders = rootFolder.listFiles(File::isDirectory);
        List<CarpetasDTO> result = new ArrayList<>();

        if (folders != null) {
            for (File folder : folders) {
                File[] files = folder.listFiles(File::isFile);
                List<String> archivos = new ArrayList<>();

                if (files != null) {
                    for (File file : files) {
                        String fileName = file.getName().replaceAll("[\\u200B-\\u200D\\uFEFF]", "");
                        System.out.println("Archivo encontrado: " + fileName); // Log para depurar
                        archivos.add(fileName);
                    }
                }

                CarpetasDTO dto = new CarpetasDTO();
                dto.setNombreCarpeta(folder.getName());
                dto.setArchivosCarpeta(archivos);
                result.add(dto);
            }
        }

        return result;
    }

    @Override
    public void deletePruebas(List<String> pruebas) {
        for (String nombreArchivo : pruebas) {
            String filePath = "C:/Users/sergi/Desktop/JSONS/" + nombreArchivo;
            File fileOrFolder = new File(filePath);
            if (fileOrFolder.exists()) {
                boolean eliminado = deleteFolderRecursively(fileOrFolder);
                if (eliminado) {
                    System.out.println("Eliminado correctamente: " + filePath);
                } else {
                    System.out.println("No se pudo eliminar: " + filePath);
                }
            } else {
                System.out.println("No encontrado: " + filePath);
            }
        }
    }

    private boolean deleteFolderRecursively(File file) {
        if (file.isDirectory()) {
            File[] contenidos = file.listFiles();
            if (contenidos != null) {
                for (File contenido : contenidos) {
                    if (!deleteFolderRecursively(contenido)) {
                        return false;
                    }
                }
            }
        }
        return file.delete();
    }


    @Override
    public void guardarArchivosEnCarpeta(String nombreCarpeta, List<MultipartFile> archivos) {
        if (archivos == null || archivos.isEmpty()) {
            throw new IllegalArgumentException("No se han proporcionado archivos.");
        }

        String basePath = "C:/Users/sergi/Desktop/JSONS/";
        String carpetaPath = basePath + nombreCarpeta;

        File carpeta = new File(carpetaPath);
        if (!carpeta.exists()) {
            boolean creada = carpeta.mkdirs();
            if (!creada) {
                throw new RuntimeException("No se pudo crear la carpeta: " + carpetaPath);
            }
        }

        for (MultipartFile archivo : archivos) {
            File destino = new File(carpetaPath + "/" + archivo.getOriginalFilename());
            try {
                archivo.transferTo(destino);
            } catch (IOException e) {
                throw new RuntimeException("Error al guardar archivo: " + archivo.getOriginalFilename(), e);
            }
        }
    }

    private static final String DIRECTORIO_BASE = "C:\\Users\\sergi\\Desktop\\JSONS";

    @Override
    public String leerContenidoArchivo(String nombreArchivo) throws IOException {
        Path path = Paths.get(DIRECTORIO_BASE, nombreArchivo);

        if (!Files.exists(path)) {
            throw new FileNotFoundException("Archivo no encontrado: " + nombreArchivo);
        }

        if (!Files.isReadable(path)) {
            throw new IOException("No se puede leer el archivo, permisos insuficientes: " + path);
        }

        System.out.println("Leyendo archivo: " + path.toString());
        String contenido = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        System.out.println("Contenido del archivo: " + contenido);

        return contenido;
    }

    @Override
    public void guardarContenidoArchivo(String nombreArchivo, String contenido) throws IOException {
        Path path = Paths.get(DIRECTORIO_BASE, nombreArchivo);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("Archivo no encontrado: " + nombreArchivo);
        }

        Files.write(path, contenido.getBytes(StandardCharsets.UTF_8));
        System.out.println("Contenido del archivo " + nombreArchivo + " actualizado.");
    }

    @Override
    public void renameCarpeta(String oldName, String newName) throws IOException {
        System.out.println("Recibiendo solicitud para renombrar: oldName=" + oldName + ", newName=" + newName);
        Path oldPath = Paths.get(DIRECTORIO_BASE, oldName);
        Path newPath = Paths.get(DIRECTORIO_BASE, newName);
        System.out.println("Rutas: oldPath=" + oldPath + ", newPath=" + newPath);
        System.out.println("Permisos de oldPath - Existe: " + Files.exists(oldPath) + ", Es directorio: " + Files.isDirectory(oldPath) + ", Lectura: " + Files.isReadable(oldPath) + ", Escritura: " + Files.isWritable(oldPath));

        if (!Files.exists(oldPath)) {
            throw new FileNotFoundException("Carpeta no encontrada: " + oldName);
        }
        if (Files.exists(newPath)) {
            throw new IOException("El nombre ya está en uso: " + newName);
        }
        if (!Files.isDirectory(oldPath)) {
            throw new IOException("No es una carpeta: " + oldName);
        }

        try {
            Files.move(oldPath, newPath);
            System.out.println("Carpeta renombrada de " + oldName + " a " + newName);
        } catch (IOException e) {
            System.err.println("Error al renombrar: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
