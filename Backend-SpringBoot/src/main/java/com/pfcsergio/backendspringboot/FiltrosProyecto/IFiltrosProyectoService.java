package com.pfcsergio.backendspringboot.FiltrosProyecto;

import java.util.List;

public interface IFiltrosProyectoService {

    List<String> findArchivosPorNombreFiltro(String nombre);

    String guardarFiltro(FiltrosProyectoDTO filtro);

    List<String> findAllNombresFiltros();

    boolean eliminarFiltroPorNombre(String nombre);

    void actualizarFiltro(FiltrosProyectoDTO filtro);

}
