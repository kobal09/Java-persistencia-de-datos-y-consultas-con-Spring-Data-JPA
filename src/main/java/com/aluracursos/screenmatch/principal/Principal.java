package com.aluracursos.screenmatch.principal;

import com.aluracursos.screenmatch.model.*;
import com.aluracursos.screenmatch.repositorio.SerieRepositorio;
import com.aluracursos.screenmatch.service.ConsumoAPI;
import com.aluracursos.screenmatch.service.ConvierteDatos;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private final String URL_BASE = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=4fc7c187";
    private ConvierteDatos conversor = new ConvierteDatos();
    private List<DatosSerie> datosSeries = new ArrayList<>();
    private SerieRepositorio serieRepositorio;
    private List<Serie> series;

    public Principal(SerieRepositorio serieRepositorio) {
        this.serieRepositorio = serieRepositorio;
    }

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    1 - Buscar series 
                    2 - Buscar episodios
                    3 - Mostrar series buscadas
                    4 - Buscar series por titulo
                    5 - Buscar Top 5 Series
                    6 - Buscar series por categoria
                                  
                    0 - Salir
                    """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    mostrarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriesPorTitulo();
                    break;
                case 5:
                    buscarTop5Series();
                    break;
                case 6:
                    buscarSeriesPorCategoria();
                    break;
                case 7:
                    seriesPorTemporadaYEvaluacion();
                    break;

                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida");
            }
        }

    }

    private DatosSerie getDatosSerie() {
        System.out.println("Escribe el nombre de la serie que deseas buscar");
        var nombreSerie = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + nombreSerie.replace(" ", "+") + API_KEY);
        System.out.println(json);
        DatosSerie datos = conversor.obtenerDatos(json, DatosSerie.class);
        return datos;
    }
    private void buscarEpisodioPorSerie() {
        mostrarSeriesBuscadas();
        System.out.println("Escribe la serie, que quieres ver los episodios: ");
        var nombreSerie = teclado.nextLine();

        Optional<Serie> serieOptional = series.stream()
                .filter(s -> s.getTitulo().toLowerCase().contains(nombreSerie.toLowerCase()))
                .findFirst();
        if (serieOptional.isPresent()) {
            var serieEncontrada = serieOptional.get();
            List<DatosTemporadas> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumoApi.obtenerDatos(URL_BASE + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DatosTemporadas datosTemporada = conversor.obtenerDatos(json, DatosTemporadas.class);
                temporadas.add(datosTemporada);
            }
            temporadas.forEach(System.out::println);
            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());
            serieEncontrada.setEpisodios(episodios);
            serieRepositorio.save(serieEncontrada);
        }

    }
    private void buscarSerieWeb() {
        DatosSerie datos = getDatosSerie();
        //datosSeries.add(datos);
        Serie serie = new Serie(datos);
        serieRepositorio.save(serie);
        System.out.println(datos);
    }

    private void mostrarSeriesBuscadas() {
        series = serieRepositorio.findAll();

        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }


    private void buscarSeriesPorTitulo() {
        System.out.println("Escribe la serie, que quieres buscar: ");
        var nombreSerie = teclado.nextLine();
        //usando el metodo que hay en la Interfaz SerieRepositorio.java
        Optional<Serie> serieBuscada = serieRepositorio.findByTituloContainsIgnoreCase(nombreSerie);

        if(serieBuscada.isPresent()) {
            System.out.println("la serie buscada es: " + serieBuscada.get());
        }else {
            System.out.println("serie no encontrada");
        }
    }

    private void buscarTop5Series() {
        List<Serie> topSeries = serieRepositorio.findTop5ByOrderByEvaluacionDesc();
        topSeries.forEach(s -> System.out.println("Serie: " + s.getTitulo() + " Evaluacion: " + s.getEvaluacion()));
    }

    private void buscarSeriesPorCategoria() {
        System.out.println("escribe el genero de la seria a buscar: ");
        var genero = teclado.nextLine();
        var categoria = Categoria.fromEspaniol(genero);
        List<Serie> seriesPorCategoria = serieRepositorio.findByGenero(categoria);
        System.out.println("las series de la categoria " + genero);
        seriesPorCategoria.forEach(System.out::println);
    }

    private void seriesPorTemporadaYEvaluacion() {
        System.out.println("Filtrar series, con cuantas temporadas? ");
        var totalTemporadas = teclado.nextInt();
        teclado.nextLine();
        System.out.println("Con evaluacion a partir de cuanto? ");
        var evaluacion = teclado.nextDouble();
        teclado.nextLine();
        List<Serie> filtroSeries = serieRepositorio.seriesPorTemporadaYEvaluacion(totalTemporadas,evaluacion);
        System.out.println("*** Series Filtradas ***");
        filtroSeries.forEach(s -> System.out.println(s.getTitulo() + "  - evaluacion: " + s.getEvaluacion()));
    }
}

