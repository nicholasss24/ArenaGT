package arenagt.pruebas;

import arenagt.modelo.Empresa;
import arenagt.modelo.EstadoTorneo;
import arenagt.modelo.Inscripcion;
import arenagt.modelo.Participante;
import arenagt.modelo.PremioEntregado;
import arenagt.modelo.RegistroFinanciero;
import arenagt.modelo.Torneo;
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
import arenagt.servicio.CargaInicial;
import arenagt.servicio.ServicioArena;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Prueba las operaciones reales del servicio de ArenaGT.
 *
 * Comprueba ingresos, estados, premios y conservación
 * de los datos ante operaciones rechazadas.
 *
 * No requiere bibliotecas externas.
 */
public final class PruebasTorneos {

    private static int aprobadas;
    private static int fallidas;
    private static int comprobaciones;

    public static void main(String[] args) {
        ejecutar("Carga inicial sin movimientos financieros",
                PruebasTorneos::probarCargaInicial);

        ejecutar("Códigos vacíos y repetidos",
                PruebasTorneos::probarCodigos);

        ejecutar("Cotizaciones sin cambios",
                PruebasTorneos::probarCotizaciones);

        ejecutar("Inscripción, cobro y rechazo de duplicados",
                PruebasTorneos::probarInscripcion);

        ejecutar("Máximo de dos participantes por empresa",
                PruebasTorneos::probarLimiteEmpresa);

        ejecutar("Capacidad máxima del torneo",
                PruebasTorneos::probarCapacidad);

        ejecutar("Estadísticas independientes entre torneos",
                PruebasTorneos::probarIndependencia);

        ejecutar("Cierre de inscripciones y estados",
                PruebasTorneos::probarCierre);

        ejecutar("Resultados inválidos sin cambios",
                PruebasTorneos::probarResultadosInvalidos);

        ejecutar("Finalización y premios de las cinco disciplinas",
                PruebasTorneos::probarPremios);

        System.out.println();
        System.out.println("RESUMEN DE TORNEOS");
        System.out.println("GRUPOS APROBADOS: " + aprobadas);
        System.out.println("GRUPOS FALLIDOS: " + fallidas);
        System.out.println(
                "COMPROBACIONES APROBADAS: " + comprobaciones
        );

        if (fallidas > 0) {
            System.exit(1);
        }
    }

    private static void probarCargaInicial() {
        ServicioArena servicio = new ServicioArena();
        new CargaInicial().cargar(servicio);

        comprobar("Disciplinas iniciales",
                5, contar(servicio.getDisciplinas()));

        comprobar("Participantes iniciales",
                8, contar(servicio.getParticipantes()));

        comprobar("Torneos iniciales",
                7, contar(servicio.getTorneos()));

        Set<Empresa> empresas = new HashSet<>();

        for (Participante participante : servicio.getParticipantes()) {
            empresas.add(participante.getEmpresa());
        }

        comprobar("Empresas iniciales", 4, empresas.size());

        int enJuego = 0;
        int finalizados = 0;
        int enInscripcion = 0;
        int resultados = 0;

        for (Torneo torneo : servicio.getTorneos()) {
            if (torneo.getEstado() == EstadoTorneo.EN_JUEGO) {
                enJuego++;
            } else if (torneo.getEstado() == EstadoTorneo.FINALIZADO) {
                finalizados++;
            } else {
                enInscripcion++;
            }

            resultados += torneo.getCantidadResultados();
        }

        comprobar("Torneos en juego", 4, enJuego);
        comprobar("Torneos finalizados", 2, finalizados);
        comprobar("Torneos en inscripción", 1, enInscripcion);
        comprobar("Resultados iniciales", 14, resultados);

        Torneo futbol = servicio.buscarTorneo("FUT-01").get();
        Evento empate = futbol.buscarEvento("FUT-01-E005").get();

        comprobar(
                "Empate inicial de fútbol: aporte de Titanes",
                new Puntuacion(1, 0, 0),
                empate.getResultado().orElseThrow(
                        () -> new AssertionError("Falta el empate inicial.")
                ).getPuntuacion(servicio.buscarParticipante("P02").get())
        );

        comprobar(
                "Empate inicial de fútbol: aporte de Leones",
                new Puntuacion(1, 0, 0),
                empate.getResultado().get().getPuntuacion(
                        servicio.buscarParticipante("P04").get()
                )
        );

        comprobar(
                "Eventos del calendario inicial",
                26,
                servicio.getCalendarioGeneral().getCantidadEventos()
        );

        comprobar(
                "Ingresos iniciales",
                new BigDecimal("0.00"),
                servicio.getRegistroFinanciero().getIngresosTotales()
        );

        comprobar(
                "Premios registrados inicialmente",
                0,
                servicio.getRegistroFinanciero()
                        .getCantidadPremiosEntregados()
        );

        rechazarSinCambios(
                "Repetir la carga inicial",
                servicio,
                () -> new CargaInicial().cargar(servicio)
        );
    }

    private static void probarCodigos() {
        ServicioArena servicio = crearEscenario(new FutbolSala(), 4);

        rechazarSinCambios(
                "Código de participante repetido con espacios y minúsculas",
                servicio,
                () -> servicio.registrarParticipante(
                        " p1 ", "Otro equipo", "Otra empresa"
                )
        );

        rechazarSinCambios(
                "Código de participante vacío",
                servicio,
                () -> servicio.registrarParticipante(
                        " ", "Otro equipo", "Otra empresa"
                )
        );

        rechazarSinCambios(
                "Código de torneo repetido",
                servicio,
                () -> servicio.crearTorneo(
                        " t1 ", "Otro torneo", "FUTSAL"
                )
        );

        rechazarSinCambios(
                "Código de torneo vacío",
                servicio,
                () -> servicio.crearTorneo(
                        "", "Otro torneo", "FUTSAL"
                )
        );

        rechazarSinCambios(
                "Código de disciplina repetido",
                servicio,
                () -> servicio.registrarDisciplina(new FutbolSala())
        );
    }

    private static void probarCotizaciones() {
        ServicioArena servicio = crearEscenario(new FutbolSala(), 4);

        String antes = huella(servicio);

        comprobar(
                "Cotizar tres inscripciones de fútbol sala",
                new BigDecimal("1800.00"),
                servicio.cotizarInscripcion("FUTSAL", 3)
        );

        comprobar(
                "Cotización válida conserva todos los registros",
                true,
                antes.equals(huella(servicio))
        );

        rechazarSinCambios(
                "Cotización con cantidad cero",
                servicio,
                () -> servicio.cotizarInscripcion("FUTSAL", 0)
        );

        rechazarSinCambios(
                "Cotización con cantidad negativa",
                servicio,
                () -> servicio.cotizarInscripcion("FUTSAL", -2)
        );

        rechazarSinCambios(
                "Cotización de disciplina inexistente",
                servicio,
                () -> servicio.cotizarInscripcion("NO-EXISTE", 2)
        );
    }

    private static void probarInscripcion() {
        ServicioArena servicio = crearEscenario(new FutbolSala(), 4);

        Torneo versionAnterior = obtenerTorneo(servicio);
        RegistroFinanciero finanzasAnteriores =
                servicio.getRegistroFinanciero();

        servicio.inscribirParticipante("T1", "P1");

        comprobar(
                "Primer cobro",
                new BigDecimal("600.00"),
                servicio.getRegistroFinanciero().getIngresosTotales()
        );

        comprobar(
                "La versión anterior del torneo sigue vacía",
                0,
                versionAnterior.getCantidadParticipantes()
        );

        comprobar(
                "La versión financiera anterior conserva cero",
                new BigDecimal("0.00"),
                finanzasAnteriores.getIngresosTotales()
        );

        servicio.inscribirParticipante("T1", "P2");

        comprobar(
                "Dos participantes inscritos",
                2,
                obtenerTorneo(servicio).getCantidadParticipantes()
        );

        comprobar(
                "Dos cobros",
                new BigDecimal("1200.00"),
                servicio.getRegistroFinanciero().getIngresosTotales()
        );

        rechazarSinCambios(
                "Inscripción duplicada",
                servicio,
                () -> servicio.inscribirParticipante("T1", "P1")
        );

        rechazarSinCambios(
                "Participante inexistente",
                servicio,
                () -> servicio.inscribirParticipante("T1", "P99")
        );

        rechazarSinCambios(
                "Torneo inexistente",
                servicio,
                () -> servicio.inscribirParticipante("NO-EXISTE", "P3")
        );
    }

    private static void probarLimiteEmpresa() {
        ServicioArena servicio = new ServicioArena();

        servicio.registrarDisciplina(new FutbolSala());
        servicio.crearTorneo("T1", "Prueba de empresas", "FUTSAL");

        servicio.registrarParticipante(
                "P1", "Equipo uno", "Empresa Norte"
        );

        servicio.registrarParticipante(
                "P2", "Equipo dos", " empresa norte "
        );

        servicio.registrarParticipante(
                "P3", "Equipo tres", "EMPRESA NORTE"
        );

        servicio.inscribirParticipante("T1", "P1");
        servicio.inscribirParticipante("T1", "P2");

        rechazarSinCambios(
                "Tercer participante de la misma empresa",
                servicio,
                () -> servicio.inscribirParticipante("T1", "P3")
        );

        comprobar(
                "Solo dos inscritos de la empresa",
                2,
                obtenerTorneo(servicio).getCantidadParticipantes()
        );

        comprobar(
                "El rechazo no registra un tercer cobro",
                new BigDecimal("1200.00"),
                servicio.getRegistroFinanciero().getIngresosTotales()
        );
    }

    private static void probarCapacidad() {
        ServicioArena servicio = crearEscenario(new FutbolSala(), 9);

        for (int i = 1; i <= 8; i++) {
            servicio.inscribirParticipante("T1", "P" + i);
        }

        comprobar(
                "Capacidad máxima de fútbol sala",
                8,
                obtenerTorneo(servicio).getCantidadParticipantes()
        );

        rechazarSinCambios(
                "Noveno participante en fútbol sala",
                servicio,
                () -> servicio.inscribirParticipante("T1", "P9")
        );

        comprobar(
                "Ingresos de ocho inscripciones",
                new BigDecimal("4800.00"),
                servicio.getRegistroFinanciero().getIngresosTotales()
        );
    }

    private static void probarIndependencia() {
        ServicioArena servicio = crearEscenario(new FutbolSala(), 4);
        inscribirPrimeros(servicio, 4);

        servicio.crearTorneo(
                "T2", "Segundo torneo", "FUTSAL"
        );

        servicio.inscribirParticipante("T2", "P1");

        servicio.cerrarInscripciones("T1");

        servicio.programarEvento(
                "T1",
                "T1-E001",
                LocalDate.of(2040, 1, 1),
                8,
                "Cancha de prueba"
        );

        servicio.registrarResultado(
                "T1", "T1-E001", marcador(4, 1)
        );

        Torneo primero = obtenerTorneo(servicio);

        Torneo segundo = servicio.buscarTorneo("T2")
                .orElseThrow(() -> new AssertionError("Falta T2."));

        Inscripcion inscripcionPrimera = primero
                .buscarInscripcion("P1")
                .orElseThrow(() -> new AssertionError("Falta P1 en T1."));

        Inscripcion inscripcionSegunda = segundo
                .buscarInscripcion("P1")
                .orElseThrow(() -> new AssertionError("Falta P1 en T2."));

        comprobar(
                "Puntos de P1 en T1",
                3,
                inscripcionPrimera.getPuntuacion().getPuntosTabla()
        );

        comprobar(
                "Puntos de P1 en T2",
                0,
                inscripcionSegunda.getPuntuacion().getPuntosTabla()
        );

        comprobar(
                "Se comparte el participante, no las estadísticas",
                true,
                inscripcionPrimera.getParticipante()
                        == inscripcionSegunda.getParticipante()
        );

        comprobar(
                "Ingresos de cinco inscripciones entre dos torneos",
                new BigDecimal("3000.00"),
                servicio.getRegistroFinanciero().getIngresosTotales()
        );
    }

    private static void probarCierre() {
        ServicioArena servicio = crearEscenario(new FutbolSala(), 5);

        inscribirPrimeros(servicio, 3);

        rechazarSinCambios(
                "Cerrar fútbol sala con tres inscritos",
                servicio,
                () -> servicio.cerrarInscripciones("T1")
        );

        servicio.inscribirParticipante("T1", "P4");
        servicio.cerrarInscripciones("T1");

        comprobar(
                "Estado después del cierre válido",
                EstadoTorneo.EN_JUEGO,
                obtenerTorneo(servicio).getEstado()
        );

        comprobar(
                "Encuentros generados con cuatro inscritos",
                6,
                obtenerTorneo(servicio).getCantidadEventos()
        );

        rechazarSinCambios(
                "Inscribir después del cierre",
                servicio,
                () -> servicio.inscribirParticipante("T1", "P5")
        );

        rechazarSinCambios(
                "Cerrar nuevamente las inscripciones",
                servicio,
                () -> servicio.cerrarInscripciones("T1")
        );

        rechazarSinCambios(
                "Resultado sobre un evento sin programar",
                servicio,
                () -> servicio.registrarResultado(
                        "T1", "T1-E001", marcador(1, 0)
                )
        );
    }

    private static void probarResultadosInvalidos() {
        verificarResultadoRechazado(
                new FutbolSala(),
                marcador(-1, 0),
                "Fútbol con goles negativos"
        );

        verificarResultadoRechazado(
                new FutbolSalaPenales(),
                marcador(2, 2),
                "Fútbol con penales empatado sin penales"
        );

        verificarResultadoRechazado(
                new Baloncesto(),
                marcador(70, 70),
                "Baloncesto empatado"
        );

        verificarResultadoRechazado(
                new Voleibol(),
                marcador(2, 1),
                "Voleibol sin ganador de tres sets"
        );

        verificarResultadoRechazado(
                new Relevos(),
                datosCarrera("50.1", "50.10", "52.0", "53.0"),
                "Relevos con tiempos numéricamente iguales"
        );
    }

    private static void probarPremios() {
        completarYVerificarPremio(
                new FutbolSala(),
                marcador(1, 0),
                "2400.00",
                "1200.00"
        );

        completarYVerificarPremio(
                new FutbolSalaPenales(),
                marcador(1, 0),
                "3000.00",
                "1500.00"
        );

        completarYVerificarPremio(
                new Baloncesto(),
                marcador(80, 70),
                "2000.00",
                "1000.00"
        );

        completarYVerificarPremio(
                new Voleibol(),
                marcador(3, 0),
                "1800.00",
                "900.00"
        );

        completarYVerificarPremio(
                new Relevos(),
                datosCarrera("50.1", "51.9", "52.4", "55.0"),
                "1200.00",
                "600.00"
        );
    }

    private static void verificarResultadoRechazado(
            Disciplina disciplina,
            DatosResultado datos,
            String caso
    ) {
        ServicioArena servicio = crearEscenario(disciplina, 4);

        inscribirPrimeros(servicio, 4);
        servicio.cerrarInscripciones("T1");

        String codigoEvento = obtenerTorneo(servicio)
                .getEventos().iterator().next().getCodigo();

        servicio.programarEvento(
                "T1",
                codigoEvento,
                LocalDate.of(2040, 1, 1),
                8,
                "Sede de prueba"
        );

        rechazarSinCambios(
                caso,
                servicio,
                () -> servicio.registrarResultado(
                        "T1", codigoEvento, datos
                )
        );
    }

    private static void completarYVerificarPremio(
            Disciplina disciplina,
            DatosResultado datos,
            String ingresosEsperados,
            String premioEsperado
    ) {
        ServicioArena servicio = crearEscenario(disciplina, 4);

        inscribirPrimeros(servicio, 4);
        servicio.cerrarInscripciones("T1");

        List<String> codigos = new ArrayList<>();

        for (Evento evento : obtenerTorneo(servicio).getEventos()) {
            codigos.add(evento.getCodigo());
        }

        for (int i = 0; i < codigos.size(); i++) {
            servicio.programarEvento(
                    "T1",
                    codigos.get(i),
                    LocalDate.of(2040, 1, 1).plusDays(i / 12),
                    8 + i % 12,
                    "Sede de prueba"
            );
        }

        for (Evento evento : obtenerTorneo(servicio).getEventos()) {
            comprobar(
                    disciplina.getNombre() + " - evento programado",
                    EstadoEvento.PROGRAMADO,
                    evento.getEstado()
            );
        }

        for (int i = 0; i < codigos.size(); i++) {
            servicio.registrarResultado(
                    "T1", codigos.get(i), datos
            );

            boolean esUltimo = i == codigos.size() - 1;

            comprobar(
                    disciplina.getNombre()
                            + " - estado tras resultado " + (i + 1),
                    esUltimo
                            ? EstadoTorneo.FINALIZADO
                            : EstadoTorneo.EN_JUEGO,
                    obtenerTorneo(servicio).getEstado()
            );

            comprobar(
                    disciplina.getNombre()
                            + " - premios tras resultado " + (i + 1),
                    esUltimo ? 1 : 0,
                    servicio.getRegistroFinanciero()
                            .getCantidadPremiosEntregados()
            );
        }

        Torneo finalizado = obtenerTorneo(servicio);

        comprobar(
                disciplina.getNombre() + " - campeón",
                "P1",
                finalizado.getCampeon()
                        .orElseThrow(() -> new AssertionError(
                                "Falta el campeón."
                        ))
                        .getCodigo()
        );

        comprobar(
                disciplina.getNombre() + " - ingresos",
                new BigDecimal(ingresosEsperados),
                servicio.getRegistroFinanciero().getIngresosTotales()
        );

        comprobar(
                disciplina.getNombre() + " - premio",
                new BigDecimal(premioEsperado),
                servicio.getRegistroFinanciero()
                        .getTotalPremiosEntregados()
        );

        comprobar(
                disciplina.getNombre() + " - todos tienen resultado",
                finalizado.getCantidadEventos(),
                finalizado.getCantidadResultados()
        );

        rechazarSinCambios(
                disciplina.getNombre() + " - repetir un resultado",
                servicio,
                () -> servicio.registrarResultado(
                        "T1", codigos.get(0), datos
                )
        );

        rechazarSinCambios(
                disciplina.getNombre() + " - repetir el premio",
                servicio,
                () -> servicio.getRegistroFinanciero()
                        .registrarPremio(finalizado)
        );
    }

    /**
     * Prepara participantes y un torneo vacío.
     * No inscribe participantes automáticamente.
     */
    private static ServicioArena crearEscenario(
            Disciplina disciplina,
            int cantidadParticipantes
    ) {
        ServicioArena servicio = new ServicioArena();

        servicio.registrarDisciplina(disciplina);

        for (int i = 1; i <= cantidadParticipantes; i++) {
            servicio.registrarParticipante(
                    "P" + i,
                    "Equipo " + i,
                    "Empresa " + ((i - 1) / 2 + 1)
            );
        }

        servicio.crearTorneo(
                "T1",
                "Torneo de prueba",
                disciplina.getCodigo()
        );

        return servicio;
    }

    private static void inscribirPrimeros(
            ServicioArena servicio,
            int cantidad
    ) {
        for (int i = 1; i <= cantidad; i++) {
            servicio.inscribirParticipante("T1", "P" + i);
        }
    }

    private static Torneo obtenerTorneo(ServicioArena servicio) {
        return servicio.buscarTorneo("T1").orElseThrow(
                () -> new AssertionError("Falta el torneo T1.")
        );
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

    private static DatosResultado datosCarrera(String... tiempos) {
        Map<String, String> valores = new LinkedHashMap<>();

        for (int i = 0; i < tiempos.length; i++) {
            Participante participante = new Participante(
                    "P" + (i + 1),
                    "Equipo " + (i + 1),
                    new Empresa("Empresa de prueba")
            );

            valores.put(
                    Relevos.claveResultado(participante),
                    tiempos[i]
            );
        }

        return new DatosResultado(valores);
    }

    /**
     * Captura el contenido observable para comprobar
     * que una operación rechazada no modifique los registros.
     */
    private static String huella(ServicioArena servicio) {
        StringBuilder texto = new StringBuilder();

        for (Disciplina disciplina : servicio.getDisciplinas()) {
            texto.append(disciplina.getCodigo()).append('|')
                    .append(disciplina.getNombre()).append('|')
                    .append(disciplina.getMinimoParticipantes()).append('|')
                    .append(disciplina.getMaximoParticipantes()).append('|')
                    .append(disciplina.getCuotaInscripcion()).append(';');
        }

        for (Participante participante : servicio.getParticipantes()) {
            texto.append(participante.getCodigo()).append('|')
                    .append(participante.getNombre()).append('|')
                    .append(participante.getEmpresa().getNombre())
                    .append(';');
        }

        for (Torneo torneo : servicio.getTorneos()) {
            texto.append(torneo.getCodigo()).append('|')
                    .append(torneo.getNombre()).append('|')
                    .append(torneo.getDisciplina().getCodigo()).append('|')
                    .append(torneo.getEstado()).append(';');

            for (Inscripcion inscripcion : torneo.getInscripciones()) {
                texto.append(inscripcion.getParticipante().getCodigo())
                        .append('|')
                        .append(inscripcion.getPuntuacion())
                        .append(';');
            }

            for (Evento evento : torneo.getEventos()) {
                texto.append(evento.getCodigo()).append('|')
                        .append(evento.getEstado()).append('|')
                        .append(evento.getProgramacion()).append('|')
                        .append(evento.getParticipantes()).append(';');

                if (evento.getResultado().isPresent()) {
                    texto.append(
                            evento.getResultado().get()
                                    .getDatos().getValores()
                    );

                    texto.append(
                            evento.getResultado().get()
                                    .getPuntuaciones()
                    );
                }
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

        return texto.toString();
    }

    private static int contar(Iterable<?> elementos) {
        int cantidad = 0;

        for (Object elemento : elementos) {
            cantidad++;
        }

        return cantidad;
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