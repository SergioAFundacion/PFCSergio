package com.pfcsergio.backendspringboot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProyectoServiceImpl implements IProyectoService {

    @Override
    public List<String> findFilesByName() {
        String path = "C:/Users/sergi/Desktop/JSONS";

        File folder = new File(path);

        if (!folder.exists() || !folder.isDirectory()) {
            log.error("La carpeta especificada no existe o no es un directorio: {}", path);
            return Collections.emptyList();
        }

        File[] files = folder.listFiles();

        List<String> fileNames = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    fileNames.add(file.getName());
                }
            }
        }
        return fileNames;
    }

    @Override
    public void deletePruebas(List<String> pruebas) {
        for (String nombreArchivo : pruebas) {
            String filePath = "C:/Users/sergi/Desktop/JSONS/" + nombreArchivo;
            File file = new File(filePath);
            if (file.exists()) {
                boolean eliminado = file.delete();
                if (eliminado) {
                    System.out.println("Archivo eliminado: " + filePath);
                } else {
                    System.out.println("No se pudo eliminar el archivo: " + filePath);
                }
            } else {
                System.out.println("Archivo no encontrado: " + filePath);
            }
        }
    }

    @Override
    public void guardarArchivos(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío o es nulo.");
        }

        String rutaDestino = "C:/Users/sergi/Desktop/JSONS/" + archivo.getOriginalFilename();
        File destino = new File(rutaDestino);

        try {
            archivo.transferTo(destino);
            System.out.println("Archivo guardado en: " + rutaDestino);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo: " + e.getMessage(), e);
        }
    }

    @Override
    public void ejecutarPruebas(List<String> archivos) {
        try {
            // Ruta del script PowerShell
            String scriptPath = "C:/Users/sergi/Desktop/JSONS/json1.json";

            // Verificar si el archivo existe
            File scriptFile = new File(scriptPath);
            if (!scriptFile.exists()) {
                throw new RuntimeException("El script PowerShell no se encuentra en la ruta: " + scriptPath);
            }

            // Validación de la carpeta de entrada (InputFolder)
            String inputFolder = "C:/Users/sergi/Desktop/JSONS";
            File inputFolderFile = new File(inputFolder);
            if (!inputFolderFile.exists() || !inputFolderFile.isDirectory()) {
                throw new RuntimeException("La carpeta de entrada no existe o no es una carpeta válida: " + inputFolder);
            }

            // Establecer el comando a ejecutar
            List<String> command = new ArrayList<>();
            command.add("powershell.exe");
            command.add("-ExecutionPolicy");
            command.add("Bypass");
            command.add("-File");
            command.add(scriptPath);

            // Pasar los parámetros obligatorios
            command.add("-InputFolder");
            command.add(inputFolder);  // Aquí pasas la carpeta real de entrada

            // Pasar la URL de destino
            command.add("-TargetUrl");
            command.add("http://localhost:8080/genesis-json");

            //Pasar el metodo POST
            command.add("-HttpMethod");
            command.add("POST");

            // Pasar archivos filtrados (si se pasaron)
            if (archivos != null && !archivos.isEmpty()) {
                command.add("-ArchivosFiltrados");
                command.add(String.join(",", archivos));  // Unir los archivos seleccionados en un solo string
            }

            // Imprimir el comando para depuración
            System.out.println("Comando ejecutado: " + String.join(" ", command));

            // Ejecutar el script
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.inheritIO();  // Muestra la salida del script en consola
            Process process = pb.start();

            // Esperar a que termine el proceso
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("El script terminó con código de error: " + exitCode);
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al ejecutar el script PowerShell. Detalles: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
            throw new RuntimeException("El proceso del script PowerShell fue interrumpido. Detalles: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new RuntimeException("Error en la ejecución del script PowerShell. Detalles: " + e.getMessage(), e);
        }
    }

}
