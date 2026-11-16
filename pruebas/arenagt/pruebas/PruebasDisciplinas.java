package arenagt.pruebas;

import arenagt.modelo.Empresa;
import arenagt.modelo.Participante;
import arenagt.modelo.disciplinas.Baloncesto;
import arenagt.modelo.disciplinas.Disciplina;
import arenagt.modelo.disciplinas.DisciplinaEncuentros;
import arenagt.modelo.disciplinas.FutbolSala;
import arenagt.modelo.disciplinas.FutbolSalaPenales;
import arenagt.modelo.disciplinas.Relevos;
import arenagt.modelo.disciplinas.Voleibol;
import arenagt.modelo.eventos.EstadoEvento;
import arenagt.modelo.eventos.Evento;
import arenagt.modelo.resultados.DatosResultado;
import arenagt.modelo.resultados.Puntuacion;
import arenagt.modelo.resultados.ResultadoEvento;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Pruebas de las reglas deportivas de ArenaGT.
 *
 * Utiliza las clases reales del proyecto.
 * No requiere bibliotecas externas.
 *
 * Cada comprobación muestra lo esperado y lo obtenido.
 * Un grupo fallido provoca código de salida 1.
 */
public final class PruebasDisciplinas {

    private static int aprobadas;
    private static int fallidas;
    private static int comprobaciones;

    public static void main(String[] args) {
        ejecutar("Cuotas, límites, cotizaciones y premios",
                PruebasDisciplinas::probarCuotas);

        ejecutar("Puntuación de fútbol sala",
                PruebasDisciplinas::probarFutbolSala);

        ejecutar("Fútbol con penales y ganador reglamentario",
                PruebasDisciplinas::probarPenalesReglamentarios);

        ejecutar("Definición por penales",
                PruebasDisciplinas::probarDefinicionPorPenales);

        ejecutar("Puntuación de baloncesto",
                PruebasDisciplinas::probarBaloncesto);

        ejecutar("Puntuación de voleibol",
                PruebasDisciplinas::probarVoleibol);

        ejecutar("Ocho lugares de relevos",
                PruebasDisciplinas::probarLugaresRelevos);

        ejecutar("Descalificaciones de relevos",
                PruebasDisciplinas::probarDescalificaciones);

        ejecutar("Rechazo de marcadores inválidos",
                PruebasDisciplinas::probarMarcadoresInvalidos);

        ejecutar("Rechazo de tiempos inválidos",
                PruebasDisciplinas::probarTiemposInvalidos);

        ejecutar("Generación de encuentros y carrera",
                PruebasDisciplinas::probarGeneracionEventos);

        ejecutar("Límites al generar eventos",
                PruebasDisciplinas::probarLimitesGeneracion);

        System.out.println();
        System.out.println("RESUMEN DE DISCIPLINAS");
        System.out.println("GRUPOS APROBADOS: " + aprobadas);
        System.out.println("GRUPOS FALLIDOS: " + fallidas);
        System.out.println(
                "COMPROBACIONES APROBADAS: " + comprobaciones
        );

        if (fallidas > 0) {
            System.exit(1);
        }
    }

    private static void probarCuotas() {
        Disciplina[] disciplinas = disciplinas();

        String[] cuotas = {
                "600.00", "750.00", "500.00", "450.00", "300.00"
        };

        String[] cotizaciones = {
                "2400.00", "3000.00", "2000.00", "1800.00", "1200.00"
        };

        String[] premios = {
                "1200.00", "1500.00", "1000.00", "900.00", "600.00"
        };

        int[] minimos = {4, 4, 4, 4, 3};
        int[] maximos = {8, 8, 6, 6, 8};

        for (int i = 0; i < disciplinas.length; i++) {
            Disciplina disciplina = disciplinas[i];
            String nombre = disciplina.getNombre();

            comprobar(
                    nombre + " - cuota",
                    new BigDecimal(cuotas[i]),
                    disciplina.getCuotaInscripcion()
            );

            comprobar(
                    nombre + " - cotización de 4 participantes",
                    new BigDecimal(cotizaciones[i]),
                    disciplina.cotizarInscripcion(4)
            );

            comprobar(
                    nombre + " - premio con 4 participantes",
                    new BigDecimal(premios[i]),
                    disciplina.calcularPremio(4)
            );

            comprobar(
                    nombre + " - mínimo",
                    minimos[i],
                    disciplina.getMinimoParticipantes()
            );

            comprobar(
                    nombre + " - máximo",
                    maximos[i],
                    disciplina.getMaximoParticipantes()
            );
        }

        comprobar(
                "Baloncesto - premio con 5 participantes",
                new BigDecimal("1250.00"),
                new Baloncesto().calcularPremio(5)
        );

        esperarRechazo(
                "Cotizar cero participantes",
                () -> new FutbolSala().cotizarInscripcion(0)
        );

        esperarRechazo(
                "Cotizar cantidad negativa",
                () -> new Relevos().cotizarInscripcion(-1)
        );
    }

    private static void probarFutbolSala() {
        verificarEncuentro(
                "Fútbol 4-1",
                new FutbolSala(),
                marcador(4, 1),
                new Puntuacion(3, 3, 1),
                new Puntuacion(0, -3, 0)
        );

        verificarEncuentro(
                "Fútbol 1-4",
                new FutbolSala(),
                marcador(1, 4),
                new Puntuacion(0, -3, 0),
                new Puntuacion(3, 3, 1)
        );

        verificarEncuentro(
                "Fútbol 0-0",
                new FutbolSala(),
                marcador(0, 0),
                new Puntuacion(1, 0, 0),
                new Puntuacion(1, 0, 0)
        );
    }

    private static void probarPenalesReglamentarios() {
        verificarEncuentro(
                "Fútbol con penales - goles 3-1 sin penales",
                new FutbolSalaPenales(),
                marcador(3, 1),
                new Puntuacion(3, 2, 1),
                new Puntuacion(0, -2, 0)
        );

        verificarEncuentro(
                "Fútbol con penales - goles 1-3 sin penales",
                new FutbolSalaPenales(),
                marcador(1, 3),
                new Puntuacion(0, -2, 0),
                new Puntuacion(3, 2, 1)
        );
    }

    private static void probarDefinicionPorPenales() {
        verificarEncuentro(
                "Goles 2-2 y penales 4-3",
                new FutbolSalaPenales(),
                marcadorConPenales(2, 2, 4, 3),
                new Puntuacion(2, 0, 1),
                new Puntuacion(1, 0, 0)
        );

        verificarEncuentro(
                "Goles 0-0 y penales 0-1",
                new FutbolSalaPenales(),
                marcadorConPenales(0, 0, 0, 1),
                new Puntuacion(1, 0, 0),
                new Puntuacion(2, 0, 1)
        );
    }

    private static void probarBaloncesto() {
        verificarEncuentro(
                "Baloncesto 80-70",
                new Baloncesto(),
                marcador(80, 70),
                new Puntuacion(2, 10, 1),
                new Puntuacion(1, -10, 0)
        );

        verificarEncuentro(
                "Baloncesto 0-10",
                new Baloncesto(),
                marcador(0, 10),
                new Puntuacion(1, -10, 0),
                new Puntuacion(2, 10, 1)
        );
    }

    private static void probarVoleibol() {
        verificarEncuentro(
                "Voleibol 3-0",
                new Voleibol(),
                marcador(3, 0),
                new Puntuacion(3, 3, 1),
                new Puntuacion(0, -3, 0)
        );

        verificarEncuentro(
                "Voleibol 0-3",
                new Voleibol(),
                marcador(0, 3),
                new Puntuacion(0, -3, 0),
                new Puntuacion(3, 3, 1)
        );

        verificarEncuentro(
                "Voleibol 3-1",
                new Voleibol(),
                marcador(3, 1),
                new Puntuacion(3, 2, 1),
                new Puntuacion(0, -2, 0)
        );

        verificarEncuentro(
                "Voleibol 1-3",
                new Voleibol(),
                marcador(1, 3),
                new Puntuacion(0, -2, 0),
                new Puntuacion(3, 2, 1)
        );

        verificarEncuentro(
                "Voleibol 3-2",
                new Voleibol(),
                marcador(3, 2),
                new Puntuacion(2, 1, 1),
                new Puntuacion(1, -1, 0)
        );

        verificarEncuentro(
                "Voleibol 2-3",
                new Voleibol(),
                marcador(2, 3),
                new Puntuacion(1, -1, 0),
                new Puntuacion(2, 1, 1)
        );
    }

    private static void probarLugaresRelevos() {
        List<Participante> participantes = crearParticipantes(8);

        /*
         * Se ingresan tiempos desordenados para comprobar
         * que los puntos no dependan del orden de captura.
         */
        DatosResultado datos = datosCarrera(
                participantes,
                "55.0", "50.1", "58.4", "52.4",
                "57.3", "51.9", "56.2", "55.7"
        );

        ResultadoEvento resultado = new Relevos()
                .evaluarResultado(participantes, datos);

        int[] esperados = {5, 10, 1, 6, 2, 8, 3, 4};

        for (int i = 0; i < participantes.size(); i++) {
            comprobar(
                    "Tiempo de " + participantes.get(i).getCodigo(),
                    new Puntuacion(
                            esperados[i],
                            0,
                            i == 1 ? 1 : 0
                    ),
                    resultado.getPuntuacion(participantes.get(i))
            );
        }
    }

    private static void probarDescalificaciones() {
        List<Participante> participantes = crearParticipantes(4);

        ResultadoEvento resultado = new Relevos()
                .evaluarResultado(
                        participantes,
                        datosCarrera(
                                participantes,
                                "DQ", "51.9", "dq", "50.1"
                        )
                );

        comprobar(
                "Primer participante descalificado",
                new Puntuacion(),
                resultado.getPuntuacion(participantes.get(0))
        );

        comprobar(
                "Segundo lugar entre los tiempos válidos",
                new Puntuacion(8, 0, 0),
                resultado.getPuntuacion(participantes.get(1))
        );

        comprobar(
                "Descalificación en minúsculas",
                new Puntuacion(),
                resultado.getPuntuacion(participantes.get(2))
        );

        comprobar(
                "Primer lugar entre los tiempos válidos",
                new Puntuacion(10, 0, 1),
                resultado.getPuntuacion(participantes.get(3))
        );
    }

    private static void probarMarcadoresInvalidos() {
        List<Participante> pareja = crearParticipantes(2);

        esperarRechazo(
                "Goles negativos",
                () -> new FutbolSala().evaluarResultado(
                        pareja,
                        marcador(-1, 0)
                )
        );

        esperarRechazo(
                "Baloncesto empatado 70-70",
                () -> new Baloncesto().evaluarResultado(
                        pareja,
                        marcador(70, 70)
                )
        );

        int[][] voleibolInvalidos = {
                {2, 1}, {3, 3}, {4, 2}, {0, 0}
        };

        for (int[] marcador : voleibolInvalidos) {
            esperarRechazo(
                    "Voleibol inválido "
                            + marcador[0] + "-" + marcador[1],
                    () -> new Voleibol().evaluarResultado(
                            pareja,
                            marcador(marcador[0], marcador[1])
                    )
            );
        }

        esperarRechazo(
                "Empate en goles sin penales",
                () -> new FutbolSalaPenales().evaluarResultado(
                        pareja,
                        marcador(2, 2)
                )
        );

        esperarRechazo(
                "Penales iguales",
                () -> new FutbolSalaPenales().evaluarResultado(
                        pareja,
                        marcadorConPenales(2, 2, 3, 3)
                )
        );

        esperarRechazo(
                "Penales con ganador reglamentario",
                () -> new FutbolSalaPenales().evaluarResultado(
                        pareja,
                        marcadorConPenales(3, 1, 4, 2)
                )
        );

        esperarRechazo(
                "Penales negativos",
                () -> new FutbolSalaPenales().evaluarResultado(
                        pareja,
                        marcadorConPenales(1, 1, -1, 0)
                )
        );

        Map<String, String> incompletos = new LinkedHashMap<>();
        incompletos.put(DisciplinaEncuentros.MARCADOR_A, "2");

        esperarRechazo(
                "Marcador incompleto",
                () -> new FutbolSala().evaluarResultado(
                        pareja,
                        new DatosResultado(incompletos)
                )
        );

        Map<String, String> textoInvalido = new LinkedHashMap<>();
        textoInvalido.put(DisciplinaEncuentros.MARCADOR_A, "abc");
        textoInvalido.put(DisciplinaEncuentros.MARCADOR_B, "1");

        esperarRechazo(
                "Marcador con texto no numérico",
                () -> new Baloncesto().evaluarResultado(
                        pareja,
                        new DatosResultado(textoInvalido)
                )
        );
    }

    private static void probarTiemposInvalidos() {
        List<Participante> participantes = crearParticipantes(3);

        String[][] invalidos = {
                {"50.1", "50.10", "52.0"},
                {"0", "51.0", "52.0"},
                {"-1", "51.0", "52.0"},
                {"NaN", "51.0", "52.0"},
                {"Infinity", "51.0", "52.0"},
                {"abc", "51.0", "52.0"}
        };

        for (String[] tiempos : invalidos) {
            esperarRechazo(
                    "Relevos: "
                            + tiempos[0] + ", "
                            + tiempos[1] + ", "
                            + tiempos[2],
                    () -> new Relevos().evaluarResultado(
                            participantes,
                            datosCarrera(participantes, tiempos)
                    )
            );
        }

        Map<String, String> incompletos = new LinkedHashMap<>();

        incompletos.put(
                Relevos.claveResultado(participantes.get(0)),
                "50.1"
        );

        esperarRechazo(
                "Carrera con participantes sin resultado",
                () -> new Relevos().evaluarResultado(
                        participantes,
                        new DatosResultado(incompletos)
                )
        );
    }

    private static void probarGeneracionEventos() {
        List<Participante> participantes = crearParticipantes(4);

        Disciplina[] encuentros = {
                new FutbolSala(),
                new FutbolSalaPenales(),
                new Baloncesto(),
                new Voleibol()
        };

        for (Disciplina disciplina : encuentros) {
            List<Evento> eventos = disciplina.crearEventos(
                    "PRUEBA-" + disciplina.getCodigo(),
                    participantes
            );

            comprobar(
                    disciplina.getNombre() + " con 4 inscritos",
                    6,
                    eventos.size()
            );

            Set<Set<Participante>> parejas = new HashSet<>();
            Set<String> codigos = new HashSet<>();

            for (Evento evento : eventos) {
                comprobar(
                        evento.getCodigo() + " - dos participantes",
                        2,
                        evento.getParticipantes().size()
                );

                comprobar(
                        evento.getCodigo() + " - estado inicial",
                        EstadoEvento.SIN_PROGRAMAR,
                        evento.getEstado()
                );

                comprobar(
                        evento.getCodigo() + " - pareja sin repetir",
                        true,
                        parejas.add(
                                new HashSet<>(evento.getParticipantes())
                        )
                );

                comprobar(
                        evento.getCodigo() + " - código sin repetir",
                        true,
                        codigos.add(evento.getCodigo())
                );
            }
        }

        List<Evento> carrera = new Relevos().crearEventos(
                "PRUEBA-RELEVOS",
                participantes
        );

        comprobar("Relevos genera una carrera", 1, carrera.size());

        comprobar(
                "La carrera incluye a todos los inscritos",
                participantes,
                carrera.get(0).getParticipantes()
        );
    }

    private static void probarLimitesGeneracion() {
        for (Disciplina disciplina : disciplinas()) {
            List<Participante> insuficientes = crearParticipantes(
                    disciplina.getMinimoParticipantes() - 1
            );

            List<Participante> excesivos = crearParticipantes(
                    disciplina.getMaximoParticipantes() + 1
            );

            esperarRechazo(
                    disciplina.getNombre() + " por debajo del mínimo",
                    () -> disciplina.crearEventos(
                            "MINIMO",
                            insuficientes
                    )
            );

            esperarRechazo(
                    disciplina.getNombre() + " por encima del máximo",
                    () -> disciplina.crearEventos(
                            "MAXIMO",
                            excesivos
                    )
            );
        }
    }

    private static Disciplina[] disciplinas() {
        return new Disciplina[] {
                new FutbolSala(),
                new FutbolSalaPenales(),
                new Baloncesto(),
                new Voleibol(),
                new Relevos()
        };
    }

    private static List<Participante> crearParticipantes(int cantidad) {
        List<Participante> participantes = new ArrayList<>();

        for (int i = 0; i < cantidad; i++) {
            participantes.add(new Participante(
                    "P" + (i + 1),
                    "Equipo " + (i + 1),
                    new Empresa("Empresa " + (i / 2 + 1))
            ));
        }

        return participantes;
    }

    private static DatosResultado marcador(int a, int b) {
        Map<String, String> valores = new LinkedHashMap<>();

        valores.put(
                DisciplinaEncuentros.MARCADOR_A,
                Integer.toString(a)
        );

        valores.put(
                DisciplinaEncuentros.MARCADOR_B,
                Integer.toString(b)
        );

        return new DatosResultado(valores);
    }

    private static DatosResultado marcadorConPenales(
            int golesA,
            int golesB,
            int penalesA,
            int penalesB
    ) {
        Map<String, String> valores = new LinkedHashMap<>(
                marcador(golesA, golesB).getValores()
        );

        valores.put(
                FutbolSalaPenales.PENALES_A,
                Integer.toString(penalesA)
        );

        valores.put(
                FutbolSalaPenales.PENALES_B,
                Integer.toString(penalesB)
        );

        return new DatosResultado(valores);
    }

    private static DatosResultado datosCarrera(
            List<Participante> participantes,
            String... tiempos
    ) {
        if (participantes.size() != tiempos.length) {
            throw new IllegalArgumentException(
                    "Error en la preparación de los datos de prueba."
            );
        }

        Map<String, String> valores = new LinkedHashMap<>();

        for (int i = 0; i < participantes.size(); i++) {
            valores.put(
                    Relevos.claveResultado(participantes.get(i)),
                    tiempos[i]
            );
        }

        return new DatosResultado(valores);
    }

    private static void verificarEncuentro(
            String caso,
            Disciplina disciplina,
            DatosResultado datos,
            Puntuacion esperadaA,
            Puntuacion esperadaB
    ) {
        List<Participante> pareja = crearParticipantes(2);

        ResultadoEvento resultado =
                disciplina.evaluarResultado(pareja, datos);

        comprobar(
                caso + " - participante A",
                esperadaA,
                resultado.getPuntuacion(pareja.get(0))
        );

        comprobar(
                caso + " - participante B",
                esperadaB,
                resultado.getPuntuacion(pareja.get(1))
        );
    }

    private static void comprobar(
            String caso,
            Object esperado,
            Object obtenido
    ) {
        if (!Objects.equals(esperado, obtenido)) {
            throw new AssertionError(
                    caso
                            + " | Esperado: " + esperado
                            + " | Obtenido: " + obtenido
            );
        }

        comprobaciones++;

        System.out.println(
                "  " + caso
                        + " | Esperado: " + esperado
                        + " | Obtenido: " + obtenido
        );
    }

    private static void esperarRechazo(
            String caso,
            Runnable operacion
    ) {
        try {
            operacion.run();
        } catch (IllegalArgumentException excepcion) {
            comprobaciones++;

            System.out.println(
                    "  " + caso
                            + " | Esperado: RECHAZO"
                            + " | Obtenido: RECHAZO"
                            + " | Motivo: " + excepcion.getMessage()
            );

            return;
        }

        throw new AssertionError(
                caso + " | Se esperaba el rechazo de la operación."
        );
    }

    private static void ejecutar(String nombre, Runnable prueba) {
        System.out.println();
        System.out.println("PRUEBA: " + nombre);

        try {
            prueba.run();
            aprobadas++;
            System.out.println("[APROBADA] " + nombre);
        } catch (RuntimeException | AssertionError error) {
            fallidas++;
            System.out.println("[FALLIDA] " + nombre);
            System.out.println("  Motivo: " + error.getMessage());
        }
    }
}