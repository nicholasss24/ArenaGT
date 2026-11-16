package arenagt.pruebas;

import arenagt.controlador.ControladorArena;
import arenagt.modelo.EstadoTorneo;
import arenagt.modelo.Inscripcion;
import arenagt.modelo.Participante;
import arenagt.modelo.PremioEntregado;
import arenagt.modelo.RegistroFinanciero;
import arenagt.modelo.Torneo;
import arenagt.modelo.disciplinas.Disciplina;
import arenagt.modelo.eventos.Evento;
import arenagt.servicio.CargaInicial;
import arenagt.servicio.ServicioArena;
import arenagt.vista.VistaConsola;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Scanner;

/**
 * Prueba la interacción real de vista, controlador y servicio.
 *
 * Sustituye el teclado por entradas preparadas y captura
 * los mensajes que normalmente aparecerían en la terminal.
 *
 * No requiere bibliotecas externas.
 */
public final class PruebasConsola {

    private static int aprobadas;
    private static int fallidas;
    private static int comprobaciones;

    public static void main(String[] args) {
        ejecutar("Carga inicial y consultas del menú",
                PruebasConsola::probarConsultasIniciales);

        ejecutar("Opciones de menú inválidas",
                PruebasConsola::probarOpcionesInvalidas);

        ejecutar("Lectura de números y fechas",
                PruebasConsola::probarLectura);

        ejecutar("Registro, cotización e inscripción",
                PruebasConsola::probarRegistros);

        ejecutar("Resultados inválidos conservan los datos",
                PruebasConsola::probarResultadosInvalidos);

        ejecutar("Ciclo completo de relevos desde el menú",
                PruebasConsola::probarCicloRelevos);

        ejecutar("Consultas e identificadores inexistentes",
                PruebasConsola::probarConsultasInvalidas);

        ejecutar("Conflicto de programación desde el menú",
                PruebasConsola::probarConflicto);

        ejecutar("Fin de entrada controlado",
                PruebasConsola::probarFinEntrada);

        System.out.println();
        System.out.println("RESUMEN DE CONSOLA");
        System.out.println("GRUPOS APROBADOS: " + aprobadas);
        System.out.println("GRUPOS FALLIDOS: " + fallidas);
        System.out.println(
                "COMPROBACIONES APROBADAS: " + comprobaciones
        );

        if (fallidas > 0) {
            System.exit(1);
        }
    }

    private static void probarConsultasIniciales() {
        Sesion sesion = new Sesion(
                "1\n3\n5\n16\n17\n9\nFUT-01\n0\n"
        );

        sesion.ejecutar();

        comprobar(
                "Participantes cargados al iniciar",
                8,
                contar(sesion.servicio.getParticipantes())
        );

        comprobar(
                "Torneos cargados al iniciar",
                7,
                contar(sesion.servicio.getTorneos())
        );

        contiene(sesion, "FUT-01-E006",
                "El detalle incluye el encuentro de Cancha Anexa");

        contiene(sesion, "FUT-01-E001",
                "El detalle incluye el encuentro de Cancha Central");

        comprobar(
                "El detalle del torneo respeta el orden por sede",
                true,
                sesion.getSalida().indexOf("FUT-01-E006")
                        < sesion.getSalida().indexOf("FUT-01-E001")
        );

        contiene(sesion, "DISCIPLINAS",
                "Consulta de disciplinas");

        contiene(sesion, "PARTICIPANTES",
                "Consulta de participantes");

        contiene(sesion, "TORNEOS",
                "Consulta de torneos");

        contiene(sesion, "Ingresos totales: Q0.00",
                "Reporte de ingresos iniciales");

        contiene(sesion, "No hay premios entregados registrados.",
                "Reporte de premios iniciales");

        contiene(sesion, "RANKING DE EMPRESAS",
                "Consulta del ranking");

        comprobar(
                "Consultar no registra ingresos",
                new BigDecimal("0.00"),
                sesion.servicio.getRegistroFinanciero()
                        .getIngresosTotales()
        );

        contiene(sesion, "Aplicación finalizada.",
                "Salida normal del menú");
    }

    private static void probarOpcionesInvalidas() {
        Sesion sesion = new Sesion(
                "abc\n-1\n18\n1.5\n0\n"
        );

        sesion.ejecutar();

        comprobar(
                "Se rechazan cuatro opciones inválidas",
                4,
                contarApariciones(
                        sesion.getSalida(),
                        "Ingrese una opción entera entre 0 y 17."
                )
        );

        contiene(sesion, "Aplicación finalizada.",
                "El menú permite salir después de los errores");
    }

    private static void probarLectura() {
        Sesion sesion = new Sesion(
                "abc\n0\n-1\n2\n"
                + "24\n-1\n0\n"
                + "2040-02-30\nfecha\n2040-02-29\n"
        );

        comprobar(
                "Entero positivo después de datos inválidos",
                2,
                sesion.vista.leerEnteroPositivo("Cantidad: ")
        );

        comprobar(
                "Hora cero después de horas fuera de rango",
                0,
                sesion.vista.leerEnteroEnRango("Hora: ", 0, 23)
        );

        comprobar(
                "Fecha válida después de fechas incorrectas",
                LocalDate.of(2040, 2, 29),
                sesion.vista.leerFecha("Fecha: ")
        );

        contiene(
                sesion,
                "Ingrese una fecha válida con formato AAAA-MM-DD.",
                "Se explica el formato de fecha"
        );
    }

    private static void probarRegistros() {
        Sesion sesion = new Sesion(
                "2\nP09\nEquipo nuevo\nEmpresa Nueva\n"
                + "4\nNUEVO-FUT\nNueva copa\nFUTSAL\n"
                + "6\nFUTSAL\n2\n"
                + "7\nNUEVO-FUT\nP09\n"
                + "2\nP09\nEquipo repetido\nEmpresa distinta\n"
                + "0\n"
        );

        sesion.ejecutar();

        comprobar(
                "Nuevo participante registrado",
                true,
                sesion.servicio.buscarParticipante("P09").isPresent()
        );

        comprobar(
                "El duplicado no agrega otro participante",
                9,
                contar(sesion.servicio.getParticipantes())
        );

        Torneo torneo = obtenerTorneo(
                sesion.servicio,
                "NUEVO-FUT"
        );

        comprobar(
                "Participante inscrito desde el menú",
                1,
                torneo.getCantidadParticipantes()
        );

        comprobar(
                "Solo se cobra la inscripción confirmada",
                new BigDecimal("600.00"),
                sesion.servicio.getRegistroFinanciero()
                        .getIngresosTotales()
        );

        contiene(sesion, "Cotización de 2 inscripciones",
                "La cotización se muestra");

        contiene(sesion, "Q1200.00",
                "Monto de la cotización");

        contiene(sesion, "Operación rechazada:",
                "Se informa el código duplicado");

        contiene(sesion, "Aplicación finalizada.",
                "El rechazo no termina el menú");
    }

    private static void probarResultadosInvalidos() {
        Sesion sesion = new Sesion(
                "11\nBAL-01\nBAL-01-E002\n70\n70\n"
                + "11\nPEN-01\nPEN-01-E003\n2\n2\n\n\n"
                + "11\nVOL-01\nVOL-01-E004\n2\n1\n"
                + "5\n0\n"
        );

        sesion.ejecutar();

        ServicioArena referencia = crearReferenciaInicial();

        comprobar(
                "Se rechazan los tres resultados inválidos",
                3,
                contarApariciones(
                        sesion.getSalida(),
                        "Operación rechazada:"
                )
        );

        comprobar(
                "Los rechazos conservan el estado inicial completo",
                true,
                huella(referencia).equals(huella(sesion.servicio))
        );

        contiene(sesion, "TORNEOS",
                "Se puede consultar después de los rechazos");

        contiene(sesion, "Aplicación finalizada.",
                "El programa continúa hasta la salida solicitada");
    }

    private static void probarCicloRelevos() {
        Sesion sesion = new Sesion(
                "4\nNUEVO-REL\nNueva copa de relevos\nRELEVOS\n"
                + "6\nRELEVOS\n3\n"
                + "7\nNUEVO-REL\nP01\n"
                + "7\nNUEVO-REL\nP03\n"
                + "7\nNUEVO-REL\nP05\n"
                + "8\nNUEVO-REL\n"
                + "9\nNUEVO-REL\n"
                + "10\nNUEVO-REL\nNUEVO-REL-C001\n"
                + "2040-01-01\n8\nPista de pruebas\n"
                + "11\nNUEVO-REL\nNUEVO-REL-C001\n"
                + "50.1\n50.10\n52.4\n"
                + "11\nNUEVO-REL\nNUEVO-REL-C001\n"
                + "50.1\n51.9\nDQ\n"
                + "12\nNUEVO-REL\n"
                + "13\n"
                + "14\nNUEVO-REL\n"
                + "15\nP01\n"
                + "16\n17\n0\n"
        );

        sesion.ejecutar();

        Torneo torneo = obtenerTorneo(
                sesion.servicio,
                "NUEVO-REL"
        );

        comprobar(
                "Finalización automática desde el menú",
                EstadoTorneo.FINALIZADO,
                torneo.getEstado()
        );

        comprobar(
                "Una sola carrera generada",
                1,
                torneo.getCantidadEventos()
        );

        comprobar(
                "Un solo resultado confirmado",
                1,
                torneo.getCantidadResultados()
        );

        comprobar(
                "Campeón de la carrera",
                "P01",
                torneo.getCampeon()
                        .orElseThrow(() -> new AssertionError(
                                "Falta el campeón."
                        ))
                        .getCodigo()
        );

        comprobar(
                "El resultado inválido no duplicó los puntos del ganador",
                10,
                obtenerInscripcion(torneo, "P01")
                        .getPuntuacion().getPuntosTabla()
        );

        comprobar(
                "Puntos del segundo lugar",
                8,
                obtenerInscripcion(torneo, "P03")
                        .getPuntuacion().getPuntosTabla()
        );

        comprobar(
                "Puntos del descalificado",
                0,
                obtenerInscripcion(torneo, "P05")
                        .getPuntuacion().getPuntosTabla()
        );

        RegistroFinanciero finanzas =
                sesion.servicio.getRegistroFinanciero();

        comprobar(
                "Tres inscripciones cobradas",
                new BigDecimal("900.00"),
                finanzas.getIngresosTotales()
        );

        comprobar(
                "Premio de relevos con tres participantes",
                new BigDecimal("450.00"),
                finanzas.getTotalPremiosEntregados()
        );

        comprobar(
                "Solo se registra el premio del torneo nuevo",
                1,
                finanzas.getCantidadPremiosEntregados()
        );

        comprobar(
                "Calendario general con la carrera nueva",
                27,
                sesion.servicio.getCalendarioGeneral()
                        .getCantidadEventos()
        );

        comprobar(
                "Calendario de P01 actualizado",
                12,
                sesion.servicio.getCalendarioParticipante("P01")
                        .getCantidadEventos()
        );

        contiene(sesion, "Los participantes de relevos",
                "Se informa el rechazo de tiempos repetidos");

        contiene(sesion, "Sin fecha, hora ni sede asignadas.",
                "El detalle también muestra los eventos sin programar");

        contiene(sesion, "El torneo finalizó automáticamente.",
                "Se comunica la finalización");

        contiene(sesion, "Campeón: Zorros",
                "La vista muestra al campeón");

        contiene(sesion, "Total de premios entregados: Q450.00",
                "El reporte muestra el premio confirmado");
    }

    private static void probarConsultasInvalidas() {
        Sesion sesion = new Sesion(
                "9\nNO-EXISTE\n"
                + "14\nNO-EXISTE\n"
                + "15\nNO-EXISTE\n"
                + "11\nFUT-01\nNO-EXISTE\n"
                + "12\nFUT-01\n"
                + "13\n"
                + "14\nFUT-01\n"
                + "15\nP01\n"
                + "17\n0\n"
        );

        sesion.ejecutar();

        comprobar(
                "Se informan cuatro identificadores inexistentes",
                4,
                contarApariciones(
                        sesion.getSalida(),
                        "Operación rechazada:"
                )
        );

        contiene(sesion, "TABLA DE POSICIONES - FUT-01",
                "La tabla sigue disponible");

        contiene(sesion, "Calendario general",
                "El calendario general sigue disponible");

        contiene(sesion, "Calendario del torneo FUT-01",
                "El calendario del torneo sigue disponible");

        contiene(sesion, "RANKING DE EMPRESAS",
                "El ranking sigue disponible");

        comprobar(
                "Las consultas y sus errores no alteran datos",
                true,
                huella(crearReferenciaInicial())
                        .equals(huella(sesion.servicio))
        );
    }

    private static void probarConflicto() {
        /*
         * FUT-01-E002 incluye a P01 a las 9 del 16/11/2026.
         * PEN-01-E003 también incluye a P01.
         */
        Sesion sesion = new Sesion(
                "10\nPEN-01\nPEN-01-E003\n"
                + "2026-11-16\n9\nOtra sede\n"
                + "14\nPEN-01\n0\n"
        );

        sesion.ejecutar();

        contiene(sesion, "ya tiene otro evento",
                "Se informa el conflicto del participante");

        comprobar(
                "La reprogramación rechazada conserva todos los datos",
                true,
                huella(crearReferenciaInicial())
                        .equals(huella(sesion.servicio))
        );

        contiene(sesion, "Calendario del torneo PEN-01",
                "El calendario sigue disponible después del conflicto");
    }

    private static void probarFinEntrada() {
        Sesion interrumpida = new Sesion(
                "7\nABI-01\n"
        );

        interrumpida.ejecutar();

        contiene(interrumpida, "Se terminó la entrada de datos.",
                "Se detecta la entrada incompleta");

        contiene(interrumpida, "Aplicación finalizada.",
                "La entrada incompleta finaliza de forma controlada");

        comprobar(
                "No se inscribe sin proporcionar el participante",
                0,
                obtenerTorneo(
                        interrumpida.servicio,
                        "ABI-01"
                ).getCantidadParticipantes()
        );

        comprobar(
                "La entrada incompleta no registra ingresos",
                new BigDecimal("0.00"),
                interrumpida.servicio.getRegistroFinanciero()
                        .getIngresosTotales()
        );

        Sesion vacia = new Sesion("");
        vacia.ejecutar();

        contiene(vacia, "Aplicación finalizada.",
                "La ausencia de entrada también permite finalizar");
    }

    private static ServicioArena crearReferenciaInicial() {
        ServicioArena servicio = new ServicioArena();
        new CargaInicial().cargar(servicio);
        return servicio;
    }

    private static Torneo obtenerTorneo(
            ServicioArena servicio,
            String codigo
    ) {
        return servicio.buscarTorneo(codigo).orElseThrow(
                () -> new AssertionError(
                        "Falta el torneo de prueba: " + codigo
                )
        );
    }

    private static Inscripcion obtenerInscripcion(
            Torneo torneo,
            String codigoParticipante
    ) {
        return torneo.buscarInscripcion(codigoParticipante)
                .orElseThrow(() -> new AssertionError(
                        "Falta la inscripción de prueba."
                ));
    }

    /**
     * Captura el estado observable para comprobar
     * que los errores del menú no lo modifiquen.
     */
    private static String huella(ServicioArena servicio) {
        StringBuilder texto = new StringBuilder();

        for (Participante participante : servicio.getParticipantes()) {
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

        return texto.toString();
    }

    private static int contar(Iterable<?> elementos) {
        int cantidad = 0;

        for (Object elemento : elementos) {
            cantidad++;
        }

        return cantidad;
    }

    private static int contarApariciones(
            String texto,
            String buscado
    ) {
        int cantidad = 0;
        int posicion = 0;

        while ((posicion = texto.indexOf(buscado, posicion)) >= 0) {
            cantidad++;
            posicion += buscado.length();
        }

        return cantidad;
    }

    private static void contiene(
            Sesion sesion,
            String textoEsperado,
            String caso
    ) {
        if (!sesion.getSalida().contains(textoEsperado)) {
            throw new AssertionError(
                    caso + " | No apareció: " + textoEsperado
            );
        }

        comprobaciones++;

        System.out.println(
                "  " + caso
                        + " | Esperado: TEXTO PRESENTE"
                        + " | Obtenido: TEXTO PRESENTE"
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

    /**
     * Cada sesión utiliza componentes independientes.
     *
     * El controlador carga los datos iniciales
     * cuando se llama a ejecutar.
     */
    private static final class Sesion {

        private final ServicioArena servicio;
        private final ByteArrayOutputStream captura;
        private final VistaConsola vista;
        private final ControladorArena controlador;

        private Sesion(String instrucciones) {
            servicio = new ServicioArena();
            captura = new ByteArrayOutputStream();

            vista = new VistaConsola(
                    new Scanner(instrucciones),
                    new PrintStream(captura, true)
            );

            controlador = new ControladorArena(
                    servicio,
                    vista,
                    new CargaInicial()
            );
        }

        private void ejecutar() {
            controlador.iniciar();
        }

        private String getSalida() {
            return captura.toString();
        }
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