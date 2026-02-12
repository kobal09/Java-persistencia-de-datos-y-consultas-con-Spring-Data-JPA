package com.aluracursos.screenmatch.repositorio;

import com.aluracursos.screenmatch.model.Categoria;
import com.aluracursos.screenmatch.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SerieRepositorio extends JpaRepository<Serie, Long> {
    //metodo usado para buscar una serie, usado en la clase Principal.java, metodo buscarSeriesPorTitulo()
    Optional<Serie> findByTituloContainsIgnoreCase(String nombreSerie);

    List<Serie> findTop5ByOrderByEvaluacionDesc();

    List<Serie> findByGenero(Categoria categoria);
}
