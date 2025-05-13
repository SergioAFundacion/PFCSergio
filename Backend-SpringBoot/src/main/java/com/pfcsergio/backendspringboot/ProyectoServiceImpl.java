package com.pfcsergio.backendspringboot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
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
}
