package com.pfcsergio.backendspringboot.FiltrosProyecto;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IFiltrosProyectoRepository extends JpaRepository<FiltrosProyecto, Integer> {

    @Query("SELECT f.archivo FROM FiltrosProyecto f WHERE f.nombre = :nombre")
    List<String> findArchivosByNombre(@Param("nombre") String nombre);

    @Query("SELECT DISTINCT f.nombre FROM FiltrosProyecto f")
    List<String> findAllNombresFiltros();

    boolean existsByNombreAndArchivo(String nombre, String archivo);

    List<FiltrosProyecto> findByNombre(String nombre);

    @Modifying
    @Transactional
    @Query("DELETE FROM FiltrosProyecto f WHERE f.nombre = :nombre")
    void eliminarArchivosDelFiltro(@Param("nombre") String nombre);
}
