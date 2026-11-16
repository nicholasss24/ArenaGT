package arenagt.pruebas;

import arenagt.modelo.Inscripcion;
import arenagt.modelo.PosicionEmpresa;
import arenagt.modelo.PremioEntregado;
import arenagt.modelo.RegistroFinanciero;
import arenagt.modelo.Torneo;
import arenagt.modelo.disciplinas.Disciplina;
import arenagt.modelo.disciplinas.DisciplinaEncuentros;
import arenagt.modelo.disciplinas.FutbolSala;
import arenagt.modelo.disciplinas.Relevos;
import arenagt.modelo.eventos.Calendario;
import arenagt.modelo.eventos.Evento;
import arenagt.modelo.eventos.Programacion;
import arenagt.modelo.resultados.DatosResultado;
import arenagt.servicio.CargaInicial;
import arenagt.servicio.ServicioArena;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Prueba calendarios, conflictos, desempates y reportes.
 *
 * Utiliza eventos y servicios reales del proyecto.
 * Los rechazos comprueban la conservación del estado.
 */
public final class PruebasCalendariosReportes {

    private static final LocalDate FECHA =
            LocalDate.of(2040, 1, 1);

    private static int aprobadas;
    private static int fallidas;
    private static int comprobaciones;

    public static void main(String[] args) {
        ejecutar("Orden cronológico con encuentros y carreras",
                PruebasCalendariosReportes::probarOrdenGeneral);

        ejecutar("Calendarios de torneo y participante",
                PruebasCalendariosReportes::probarFiltros);

        ejecutar("Conflicto de sede entre torneos",
                PruebasCalendariosReportes::probarConflictoSede);

        ejecutar("Conflicto de participante entre tipos de evento",
                PruebasCalendariosReportes::probarConflictoParticipante);

        ejecutar("Reprogramación y bloqueo de eventos finalizados",
                PruebasCalendariosReportes::probarReprogramacion);

        ejecutar("Horas válidas y eventos sin programar",
                PruebasCalendariosReportes::probarHoras);

        ejecutar("Prioridad de puntos, diferencia y victorias",
                PruebasCalendariosReportes::probarDesempatesDeportivos);

        ejecutar("Desempates alfabéticos de tabla y ranking",
                PruebasCalendariosReportes::probarDesempatesAlfabeticos);

        ejecutar("Ranking acumulado entre torneos y disciplinas",
                PruebasCalendariosReportes::probarRankingAcumulado);

        ejecutar("Protección de las colecciones",
                PruebasCalendariosReportes::probarProteccion);

        System.out.println();
        System.out.println("RESUMEN DE CALENDARIOS Y REPORTES");
        System.out.println("GRUPOS APROBADOS: " + aprobadas);
        System.out.println("GRUPOS FALLIDOS: " + fallidas);
        System.out.println(
                "COMPROBACIONES APROBADAS: " + comprobaciones
        );

        if (fallidas > 0) {
            System.exit(1);
        }
    }

    private static void probarOrdenGeneral() {
        ServicioArena servicio = crearCalendarioMixto();

        comprobar(
                "Orden por fecha, hora y sede",
                Arrays.asList(
                        "F1-E006",
                        "R1-C001",
                        "F2-E001",
                        "F1-E001"
                ),
                codigos(servicio.getCalendarioGeneral())
        );

        List<String> tipos = new ArrayList<>();

        for (Evento evento : servicio.getCalendarioGeneral()) {
            tipos.add(evento.getTipo());
        }

        comprobar(
                "Tipos de evento mezclados en el mismo recorrido",
                Arrays.asList(
                        "Encuentro",
                        "Carrera",
                        "Encuentro",
                        "Encuentro"
                ),
                tipos
        );

        comprobar(
                "Solo se muestran los cuatro eventos programados",
                4,
                servicio.getCalendarioGeneral().getCantidadEventos()
        );
    }

    private static void probarFiltros() {
        ServicioArena servicio = crearCalendarioMixto();

        comprobar(
                "Calendario de F1 conserva el orden cronológico",
                Arrays.asList("F1-E006", "F1-E001"),
                codigos(servicio.getCalendarioTorneo("F1"))
        );

        comprobar(
                "Calendario de P1 incluye carrera y encuentro",
                Arrays.asList("R1-C001", "F1-E001"),
                codigos(servicio.getCalendarioParticipante("P1"))
        );

        comprobar(
                "Participante sin eventos programados",
                0,
                servicio.getCalendarioParticipante("P8")
                        .getCantidadEventos()
        );

        rechazarSinCambios(
                "Calendario de participante inexistente",
                servicio,
                () -> servicio.getCalendarioParticipante("NO-EXISTE")
        );

        rechazarSinCambios(
                "Calendario de torneo inexistente",
                servicio,
                () -> servicio.getCalendarioTorneo("NO-EXISTE")
        );
    }

    private static void probarConflictoSede() {
        ServicioArena servicio = crearEscenario();

        servicio.programarEvento(
                "F1", "F1-E001", FECHA, 8, "Cancha Norte"
        );

        /*
         * F2 tiene participantes diferentes.
         * El rechazo debe deberse a la sede, no al participante.
         */
        rechazarSinCambios(
                "Misma sede, fecha y hora en otro torneo",
                servicio,
                () -> servicio.programarEvento(
                        "F2",
                        "F2-E001",
                        FECHA,
                        8,
                        " cancha norte "
                )
        );

        comprobar(
                "El evento rechazado continúa sin programar",
                false,
                obtenerEvento(servicio, "F2", "F2-E001")
                        .getProgramacion().isPresent()
        );

        servicio.programarEvento(
                "F2", "F2-E001", FECHA, 9, "Cancha Norte"
        );

        comprobar(
                "La misma sede puede utilizarse en otra hora",
                2,
                servicio.getCalendarioGeneral().getCantidadEventos()
        );
    }

    private static void probarConflictoParticipante() {
        ServicioArena servicio = crearEscenario();

        servicio.programarEvento(
                "F1", "F1-E001", FECHA, 8, "Sede A"
        );

        /*
         * R1 incluye a P1, que ya participa en F1-E001.
         * La sede es diferente para aislar el conflicto personal.
         */
        rechazarSinCambios(
                "Carrera que coincide con un encuentro de P1",
                servicio,
                () -> servicio.programarEvento(
                        "R1", "R1-C001", FECHA, 8, "Pista B"
                )
        );

        servicio.programarEvento(
                "F2", "F2-E001", FECHA, 8, "Sede B"
        );

        comprobar(
                "Participantes y sedes distintos permiten la misma hora",
                2,
                servicio.getCalendarioGeneral().getCantidadEventos()
        );

        ServicioArena inverso = crearEscenario();

        inverso.programarEvento(
                "R1", "R1-C001", FECHA, 8, "Pista"
        );

        rechazarSinCambios(
                "Encuentro que coincide con una carrera de P5",
                inverso,
                () -> inverso.programarEvento(
                        "F2", "F2-E001", FECHA, 8, "Cancha"
                )
        );
    }

    private static void probarReprogramacion() {
        ServicioArena servicio = crearEscenario();

        servicio.programarEvento(
                "F1", "F1-E001", FECHA, 8, "Sede A"
        );

        Calendario consultaAnterior =
                servicio.getCalendarioGeneral();

        /*
         * Repetir la propia programación es válido.
         * El evento no debe entrar en conflicto consigo mismo.
         */
        servicio.programarEvento(
                "F1", "F1-E001", FECHA, 8, "Sede A"
        );

        comprobar(
                "Reprogramar no duplica el evento",
                1,
                servicio.getCalendarioGeneral().getCantidadEventos()
        );

        servicio.programarEvento(
                "F1",
                "F1-E001",
                FECHA.plusDays(1),
                9,
                "Nueva sede"
        );

        Programacion anterior = consultaAnterior.iterator().next()
                .getProgramacion().get();

        Programacion actual = obtenerEvento(
                servicio, "F1", "F1-E001"
        ).getProgramacion().get();

        comprobar(
                "La consulta anterior conserva su fecha",
                FECHA,
                anterior.getFecha()
        );

        comprobar(
                "La consulta actual refleja la fecha nueva",
                FECHA.plusDays(1),
                actual.getFecha()
        );

        comprobar(
                "La consulta actual refleja la hora nueva",
                9,
                actual.getHoraInicio()
        );

        servicio.programarEvento(
                "F2",
                "F2-E001",
                FECHA.plusDays(1),
                10,
                "Sede ocupada"
        );

        rechazarSinCambios(
                "Reprogramación hacia una sede ocupada",
                servicio,
                () -> servicio.programarEvento(
                        "F1",
                        "F1-E001",
                        FECHA.plusDays(1),
                        10,
                        "Sede ocupada"
                )
        );

        servicio.registrarResultado(
                "F1", "F1-E001", marcador(1, 0)
        );

        rechazarSinCambios(
                "Reprogramar un encuentro finalizado",
                servicio,
                () -> servicio.programarEvento(
                        "F1",
                        "F1-E001",
                        FECHA.plusDays(2),
                        11,
                        "Otra sede"
                )
        );

        rechazarSinCambios(
                "Repetir resultado de un evento finalizado",
                servicio,
                () -> servicio.registrarResultado(
                        "F1", "F1-E001", marcador(2, 0)
                )
        );
    }

    private static void probarHoras() {
        ServicioArena servicio = crearEscenario();

        comprobar(
                "Los eventos sin programar no aparecen en el calendario",
                0,
                servicio.getCalendarioGeneral().getCantidadEventos()
        );

        rechazarSinCambios(
                "Hora negativa",
                servicio,
                () -> servicio.programarEvento(
                        "F1", "F1-E001", FECHA, -1, "Sede"
                )
        );

        rechazarSinCambios(
                "Hora 24",
                servicio,
                () -> servicio.programarEvento(
                        "F1", "F1-E001", FECHA, 24, "Sede"
                )
        );

        rechazarSinCambios(
                "Sede vacía",
                servicio,
                () -> servicio.programarEvento(
                        "F1", "F1-E001", FECHA, 8, " "
                )
        );

        rechazarSinCambios(
                "Fecha nula",
                servicio,
                () -> servicio.programarEvento(
                        "F1", "F1-E001", null, 8, "Sede"
                )
        );

        servicio.programarEvento(
                "F1", "F1-E001", FECHA, 0, "Sede"
        );

        comprobar(
                "Medianoche es una hora válida",
                0,
                obtenerEvento(servicio, "F1", "F1-E001")
                        .getProgramacion().get().getHoraInicio()
        );

        servicio.programarEvento(
                "F1", "F1-E001", FECHA, 23, "Sede"
        );

        comprobar(
                "Las 23 horas son válidas",
                23,
                obtenerEvento(servicio, "F1", "F1-E001")
                        .getProgramacion().get().getHoraInicio()
        );
    }

    private static void probarDesempatesDeportivos() {
        ServicioArena servicio = new ServicioArena();
        new CargaInicial().cargar(servicio);

        List<Inscripcion> futbol =
                servicio.getTablaPosiciones("FUT-01");

        comprobar(
                "Fútbol: primero por diferencia",
                "P01",
                futbol.get(0).getParticipante().getCodigo()
        );

        comprobar(
                "Fútbol: segundo con los mismos puntos",
                "P03",
                futbol.get(1).getParticipante().getCodigo()
        );

        comprobar(
                "Ambos tienen tres puntos",
                Arrays.asList(3, 3),
                Arrays.asList(
                        futbol.get(0).getPuntuacion().getPuntosTabla(),
                        futbol.get(1).getPuntuacion().getPuntosTabla()
                )
        );

        comprobar(
                "Diferencia que resuelve el empate",
                Arrays.asList(3L, 1L),
                Arrays.asList(
                        futbol.get(0).getPuntuacion().getDiferencia(),
                        futbol.get(1).getPuntuacion().getDiferencia()
                )
        );

        List<Inscripcion> penales =
                servicio.getTablaPosiciones("PEN-01");

        List<String> orden = new ArrayList<>();

        for (Inscripcion fila : penales) {
            orden.add(fila.getParticipante().getCodigo());
        }

        comprobar(
                "Orden completo de la tabla con penales",
                Arrays.asList("P01", "P02", "P04", "P03"),
                orden
        );

        comprobar(
                "Empate en puntos y diferencia de los dos primeros",
                Arrays.asList(4, 4, 0L, 0L),
                Arrays.asList(
                        penales.get(0).getPuntuacion().getPuntosTabla(),
                        penales.get(1).getPuntuacion().getPuntosTabla(),
                        penales.get(0).getPuntuacion().getDiferencia(),
                        penales.get(1).getPuntuacion().getDiferencia()
                )
        );

        comprobar(
                "Victorias que resuelven el empate",
                Arrays.asList(2, 1),
                Arrays.asList(
                        penales.get(0).getPuntuacion().getVictorias(),
                        penales.get(1).getPuntuacion().getVictorias()
                )
        );
    }

    private static void probarDesempatesAlfabeticos() {
        ServicioArena servicio = new ServicioArena();
        servicio.registrarDisciplina(new FutbolSala());

        String[] nombres = {"Zeta", "Alfa", "Beta", "Delta"};

        for (int i = 0; i < nombres.length; i++) {
            servicio.registrarParticipante(
                    "P" + (i + 1),
                    nombres[i],
                    "Empresa " + nombres[i]
            );
        }

        servicio.crearTorneo("ALF", "Empate alfabético", "FUTSAL");

        for (int i = 1; i <= 4; i++) {
            servicio.inscribirParticipante("ALF", "P" + i);
        }

        List<String> participantes = new ArrayList<>();

        for (Inscripcion fila : servicio.getTablaPosiciones("ALF")) {
            participantes.add(fila.getParticipante().getNombre());
        }

        comprobar(
                "Tabla empatada en puntos, diferencia y victorias",
                Arrays.asList("Alfa", "Beta", "Delta", "Zeta"),
                participantes
        );

        List<String> empresas = new ArrayList<>();

        for (PosicionEmpresa fila : servicio.getRankingEmpresas()) {
            empresas.add(fila.getEmpresa().getNombre());

            comprobar(
                    fila.getEmpresa().getNombre() + " sin puntos",
                    0L,
                    fila.getPuntosTabla()
            );
        }

        comprobar(
                "Ranking empresarial empatado en puntos",
                Arrays.asList(
                        "Empresa Alfa",
                        "Empresa Beta",
                        "Empresa Delta",
                        "Empresa Zeta"
                ),
                empresas
        );
    }

    private static void probarRankingAcumulado() {
        ServicioArena servicio = new ServicioArena();
        new CargaInicial().cargar(servicio);

        List<PosicionEmpresa> ranking =
                servicio.getRankingEmpresas();

        List<String> empresas = new ArrayList<>();
        List<Long> puntos = new ArrayList<>();

        for (PosicionEmpresa fila : ranking) {
            empresas.add(fila.getEmpresa().getNombre());
            puntos.add(fila.getPuntosTabla());
        }

        comprobar(
                "Orden de empresas por puntos acumulados",
                Arrays.asList(
                        "TecnoGT",
                        "SaludGT",
                        "LogísticaGT",
                        "EnergíaGT"
                ),
                empresas
        );

        comprobar(
                "Suma de todos los participantes y torneos",
                Arrays.asList(43L, 32L, 21L, 14L),
                puntos
        );

        String antes = huella(servicio);

        servicio.getRankingEmpresas();
        servicio.getCalendarioGeneral();
        servicio.getTablaPosiciones("FUT-01");

        comprobar(
                "Consultar reportes no modifica los registros",
                true,
                antes.equals(huella(servicio))
        );
    }

    private static void probarProteccion() {
        ServicioArena servicio = crearCalendarioMixto();

        rechazarModificacion(
                "Eliminar mediante el iterador del calendario",
                servicio,
                () -> {
                    Iterator<Evento> iterador = servicio
                            .getCalendarioGeneral().iterator();

                    iterador.next();
                    iterador.remove();
                }
        );

        rechazarModificacion(
                "Vaciar la tabla de posiciones",
                servicio,
                () -> servicio.getTablaPosiciones("F1").clear()
        );

        rechazarModificacion(
                "Vaciar el ranking de empresas",
                servicio,
                () -> servicio.getRankingEmpresas().clear()
        );

        rechazarModificacion(
                "Modificar participantes de un evento",
                servicio,
                () -> obtenerEvento(
                        servicio, "F1", "F1-E001"
                ).getParticipantes().clear()
        );
    }

    /**
     * Crea dos torneos de encuentros con participantes distintos
     * y una carrera que comparte participantes con ambos.
     */
    private static ServicioArena crearEscenario() {
        ServicioArena servicio = new ServicioArena();

        servicio.registrarDisciplina(new FutbolSala());
        servicio.registrarDisciplina(new Relevos());

        for (int i = 1; i <= 8; i++) {
            servicio.registrarParticipante(
                    "P" + i,
                    "Equipo " + i,
                    "Empresa " + ((i - 1) / 2 + 1)
            );
        }

        servicio.crearTorneo("F1", "Primera copa", "FUTSAL");
        servicio.crearTorneo("F2", "Segunda copa", "FUTSAL");
        servicio.crearTorneo("R1", "Carrera empresarial", "RELEVOS");

        for (int i = 1; i <= 4; i++) {
            servicio.inscribirParticipante("F1", "P" + i);
        }

        for (int i = 5; i <= 8; i++) {
            servicio.inscribirParticipante("F2", "P" + i);
        }

        servicio.inscribirParticipante("R1", "P1");
        servicio.inscribirParticipante("R1", "P5");
        servicio.inscribirParticipante("R1", "P7");

        servicio.cerrarInscripciones("F1");
        servicio.cerrarInscripciones("F2");
        servicio.cerrarInscripciones("R1");

        return servicio;
    }

    /**
     * Programa deliberadamente en un orden diferente
     * del que debe mostrar el calendario.
     */
    private static ServicioArena crearCalendarioMixto() {
        ServicioArena servicio = crearEscenario();

        servicio.programarEvento(
                "F1",
                "F1-E001",
                FECHA.plusDays(1),
                10,
                "Sede Z"
        );

        servicio.programarEvento(
                "R1",
                "R1-C001",
                FECHA.plusDays(1),
                9,
                "Pista"
        );

        servicio.programarEvento(
                "F2",
                "F2-E001",
                FECHA.plusDays(1),
                10,
                "Sede A"
        );

        servicio.programarEvento(
                "F1",
                "F1-E006",
                FECHA,
                12,
                "Cancha"
        );

        return servicio;
    }

    private static Evento obtenerEvento(
            ServicioArena servicio,
            String codigoTorneo,
            String codigoEvento
    ) {
        Torneo torneo = servicio.buscarTorneo(codigoTorneo)
                .orElseThrow(() -> new AssertionError(
                        "Falta el torneo de prueba."
                ));

        return torneo.buscarEvento(codigoEvento)
                .orElseThrow(() -> new AssertionError(
                        "Falta el evento de prueba."
                ));
    }

    private static List<String> codigos(Calendario calendario) {
        List<String> codigos = new ArrayList<>();

        for (Evento evento : calendario) {
            codigos.add(evento.getCodigo());
        }

        return codigos;
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

    /**
     * Captura los datos consultables para detectar cambios
     * indebidos tras una operación rechazada.
     */
    private static String huella(ServicioArena servicio) {
        StringBuilder texto = new StringBuilder();

        for (arenagt.modelo.Participante participante
                : servicio.getParticipantes()) {
            texto.append(participante.getCodigo()).append('|')
                    .append(participante.getNombre()).append('|')
                    .append(participante.getEmpresa().getNombre())
                    .append(';');
        }

        for (Torneo torneo : servicio.getTorneos()) {
            texto.append(torneo).append(';');

            for (Inscripcion inscripcion : torneo.getInscripciones()) {
                texto.append(inscripcion).append(';');
            }

            for (Evento evento : torneo.getEventos()) {
                texto.append(evento.getCodigo()).append('|')
                        .append(evento.getEstado()).append('|')
                        .append(evento.getParticipantes()).append('|')
                        .append(evento.getProgramacion()).append('|')
                        .append(evento.getResultado()).append(';');
            }
        }

        RegistroFinanciero finanzas =
                servicio.getRegistroFinanciero();

        for (Disciplina disciplina : servicio.getDisciplinas()) {
            texto.append(disciplina.getCodigo()).append('|')
                    .append(finanzas.getIngresosDeDisciplina(disciplina))
                    .append(';');
        }

        texto.append(finanzas.getIngresosTotales()).append('|')
                .append(finanzas.getTotalPremiosEntregados());

        for (PremioEntregado premio : finanzas.getPremiosEntregados()) {
            texto.append(premio.getCodigoTorneo()).append('|')
                    .append(premio.getCampeon().getCodigo()).append('|')
                    .append(premio.getMonto()).append(';');
        }

        texto.append(servicio.getRankingEmpresas());

        return texto.toString();
    }

    private static void rechazarSinCambios(
            String caso,
            ServicioArena servicio,
            Runnable operacion
    ) {
        String antes = huella(servicio);

        try {
            operacion.run();
        } catch (IllegalArgumentException excepcion) {
            if (!antes.equals(huella(servicio))) {
                throw new AssertionError(
                        caso + " modificó registros pese al rechazo."
                );
            }

            comprobaciones++;

            System.out.println(
                    "  " + caso
                            + " | Esperado: RECHAZO SIN CAMBIOS"
                            + " | Obtenido: RECHAZO SIN CAMBIOS"
                            + " | Motivo: " + excepcion.getMessage()
            );

            return;
        }

        throw new AssertionError(
                caso + " debía ser rechazado."
        );
    }

    private static void rechazarModificacion(
            String caso,
            ServicioArena servicio,
            Runnable operacion
    ) {
        String antes = huella(servicio);

        try {
            operacion.run();
        } catch (UnsupportedOperationException excepcion) {
            if (!antes.equals(huella(servicio))) {
                throw new AssertionError(
                        caso + " alteró una colección protegida."
                );
            }

            comprobaciones++;

            System.out.println(
                    "  " + caso
                            + " | Esperado: MODIFICACIÓN IMPEDIDA"
                            + " | Obtenido: MODIFICACIÓN IMPEDIDA"
            );

            return;
        }

        throw new AssertionError(
                caso + " debía impedir la modificación."
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