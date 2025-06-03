package com.pfcsergio.backendspringboot.FiltrosProyecto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor

public class FiltrosProyectoServiceImpl implements IFiltrosProyectoService{

    private final IFiltrosProyectoRepository repository;

    private final FiltrosProyectoMapper mapper;

    @Autowired
    private IFiltrosProyectoRepository filtrosPruebasRegresionRepository;

    @Override
    public List<String> findArchivosPorNombreFiltro(String nombre) {
        try {
            return repository.findArchivosByNombre(nombre);
        } catch (Exception e) {
            log.error("Error al buscar archivos por nombre de filtro '{}': {}", nombre, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> findAllNombresFiltros() {
        return repository.findAllNombresFiltros();
    }

    @Override
    public String guardarFiltro(FiltrosProyectoDTO filtro) {
        try {
            String nombre = filtro.getNombre().trim();
            List<String> archivos = filtro.getArchivos();

            if (nombre.isEmpty() || archivos == null || archivos.isEmpty()) {
                return "Nombre del filtro o lista de archivos vacía.";
            }

            for (String archivo : archivos) {
                archivo = archivo.trim();

                boolean yaExiste = repository.existsByNombreAndArchivo(nombre, archivo);
                if (!yaExiste) {
                    FiltrosProyecto nuevoFiltro = new FiltrosProyecto();
                    nuevoFiltro.setNombre(nombre);
                    nuevoFiltro.setArchivo(archivo);
                    repository.save(nuevoFiltro);
                }
            }

            return "Filtro guardado con éxito.";
        } catch (Exception e) {
            e.printStackTrace();
            return "No se pudo guardar el filtro.";
        }
    }

    @Override
    public boolean eliminarFiltroPorNombre(String nombre) {
        try {
            List<FiltrosProyecto> filtros = repository.findByNombre(nombre);
            if (filtros.isEmpty()) return false;

            repository.deleteAll(filtros);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional
    public void actualizarFiltro(FiltrosProyectoDTO filtro) {
        if (filtro.getNombreAntiguo() != null && !filtro.getNombreAntiguo().equals(filtro.getNombre())) {
            repository.eliminarArchivosDelFiltro(filtro.getNombreAntiguo());
        } else {
            repository.eliminarArchivosDelFiltro(filtro.getNombre());
        }

        List<FiltrosProyecto> nuevasRelaciones = filtro.getArchivos().stream()
                .map(nombreArchivo -> {
                    FiltrosProyecto rel = new FiltrosProyecto();
                    rel.setNombre(filtro.getNombre());
                    rel.setArchivo(nombreArchivo);
                    return rel;
                })
                .collect(Collectors.toList());

        repository.saveAll(nuevasRelaciones);
    }


}
