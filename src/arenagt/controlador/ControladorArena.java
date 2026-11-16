package arenagt.controlador;

import arenagt.modelo.EstadoTorneo;
import arenagt.modelo.Participante;
import arenagt.modelo.Torneo;
import arenagt.modelo.disciplinas.Disciplina;
import arenagt.modelo.eventos.Evento;
import arenagt.modelo.resultados.DatosResultado;
import arenagt.servicio.CargaInicial;
import arenagt.servicio.ServicioArena;
import arenagt.vista.VistaConsola;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NoSuchElementException;

/**
 * Coordina la interacción entre la vista y el servicio.
 *
 * Conserva el ciclo del menú y atiende sus opciones.
 * No calcula puntuaciones, conflictos ni premios.
 */
public final class ControladorArena {

    private final ServicioArena servicio;
    private final VistaConsola vista;
    private final CargaInicial cargaInicial;

    /**
     * Recibe los componentes de la aplicación.
     */
    public ControladorArena(
            ServicioArena servicio,
            VistaConsola vista,
            CargaInicial cargaInicial
    ) {
        if (servicio == null
                || vista == null
                || cargaInicial == null) {
            throw new IllegalArgumentException(
                    "Todos los componentes del controlador "
                            + "son obligatorios."
            );
        }

        this.servicio = servicio;
        this.vista = vista;
        this.cargaInicial = cargaInicial;
    }

    /**
     * Carga los ejemplos si el servicio está vacío
     * y mantiene el menú hasta la salida del usuario.
     *
     * Respeta los datos proporcionados previamente,
     * lo que permite utilizar escenarios de prueba.
     */
    public void iniciar() {
        boolean servicioVacio =
                !servicio.getDisciplinas().iterator().hasNext()
                && !servicio.getParticipantes().iterator().hasNext()
                && !servicio.getTorneos().iterator().hasNext();

        if (servicioVacio) {
            cargaInicial.cargar(servicio);
        }

        vista.mostrarBienvenida();

        boolean continuar = true;

        while (continuar) {
            try {
                vista.mostrarMenu();
                int opcion = vista.leerOpcion();

                switch (opcion) {
                    case 1:
                        vista.mostrarDisciplinas(
                                servicio.getDisciplinas()
                        );
                        break;

                    case 2:
                        registrarParticipante();
                        break;

                    case 3:
                        vista.mostrarParticipantes(
                                servicio.getParticipantes()
                        );
                        break;

                    case 4:
                        crearTorneo();
                        break;

                    case 5:
                        vista.mostrarTorneos(
                                servicio.getTorneos()
                        );
                        break;

                    case 6:
                        cotizarInscripciones();
                        break;

                    case 7:
                        inscribirParticipante();
                        break;

                    case 8:
                        cerrarInscripciones();
                        break;

                    case 9:
                        vista.mostrarDetalleTorneo(
                                solicitarTorneo()
                        );
                        break;

                    case 10:
                        programarEvento();
                        break;

                    case 11:
                        registrarResultado();
                        break;

                    case 12:
                        vista.mostrarTabla(
                                solicitarTorneo()
                        );
                        break;

                    case 13:
                        vista.mostrarMensaje("Calendario general");
                        vista.mostrarCalendario(
                                servicio.getCalendarioGeneral()
                        );
                        break;

                    case 14:
                        mostrarCalendarioTorneo();
                        break;

                    case 15:
                        mostrarCalendarioParticipante();
                        break;

                    case 16:
                        vista.mostrarFinanzas(
                                servicio.getRegistroFinanciero(),
                                servicio.getDisciplinas()
                        );
                        break;

                    case 17:
                        vista.mostrarRanking(
                                servicio.getRankingEmpresas()
                        );
                        break;

                    case 0:
                        continuar = false;
                        break;

                    default:
                        vista.mostrarMensaje(
                                "Opción no reconocida."
                        );
                        break;
                }
            } catch (NoSuchElementException excepcion) {
                vista.mostrarMensaje(
                        "Se terminó la entrada de datos."
                );
                continuar = false;
            } catch (IllegalArgumentException excepcion) {
                vista.mostrarMensaje(
                        "Operación rechazada: "
                                + excepcion.getMessage()
                );
            }
        }

        vista.mostrarDespedida();
    }

    private void registrarParticipante() {
        String codigo = vista.leerTextoObligatorio(
                "Código del participante: "
        );

        String nombre = vista.leerTextoObligatorio(
                "Nombre del equipo o cuarteto: "
        );

        String empresa = vista.leerTextoObligatorio(
                "Nombre de la empresa: "
        );

        Participante participante =
                servicio.registrarParticipante(
                        codigo,
                        nombre,
                        empresa
                );

        vista.mostrarMensaje(
                "Participante registrado: "
                        + participante.getCodigo()
        );
    }

    private void crearTorneo() {
        vista.mostrarDisciplinas(servicio.getDisciplinas());

        String codigo = vista.leerTextoObligatorio(
                "Código del nuevo torneo: "
        );

        String nombre = vista.leerTextoObligatorio(
                "Nombre del torneo: "
        );

        String codigoDisciplina = vista.leerTextoObligatorio(
                "Código de la disciplina: "
        );

        Torneo torneo = servicio.crearTorneo(
                codigo,
                nombre,
                codigoDisciplina
        );

        vista.mostrarMensaje(
                "Torneo creado en inscripción: "
                        + torneo.getCodigo()
        );
    }

    private void cotizarInscripciones() {
        vista.mostrarDisciplinas(servicio.getDisciplinas());

        Disciplina disciplina = solicitarDisciplina();

        int cantidad = vista.leerEnteroPositivo(
                "Cantidad de participantes que desea cotizar: "
        );

        BigDecimal monto = servicio.cotizarInscripcion(
                disciplina.getCodigo(),
                cantidad
        );

        vista.mostrarCotizacion(
                disciplina,
                cantidad,
                monto
        );
    }

    private void inscribirParticipante() {
        Torneo torneo = solicitarTorneo();

        String codigoParticipante = vista.leerTextoObligatorio(
                "Código del participante que desea inscribir: "
        );

        Torneo actualizado = servicio.inscribirParticipante(
                torneo.getCodigo(),
                codigoParticipante
        );

        vista.mostrarMensaje(
                "Inscripción confirmada. Cobro registrado: Q"
                        + actualizado.getDisciplina()
                                .getCuotaInscripcion().toPlainString()
        );

        vista.mostrarMensaje(
                "Participantes inscritos: "
                        + actualizado.getCantidadParticipantes()
        );
    }

    private void cerrarInscripciones() {
        Torneo torneo = solicitarTorneo();

        Torneo actualizado = servicio.cerrarInscripciones(
                torneo.getCodigo()
        );

        vista.mostrarMensaje(
                "Inscripciones cerradas. Eventos generados: "
                        + actualizado.getCantidadEventos()
        );

        vista.mostrarDetalleTorneo(actualizado);
    }

    private void programarEvento() {
        Torneo torneo = solicitarTorneo();
        Evento evento = solicitarEvento(torneo);

        LocalDate fecha = vista.leerFecha(
                "Fecha del evento (AAAA-MM-DD): "
        );

        int hora = vista.leerEnteroEnRango(
                "Hora completa de inicio (0 a 23): ",
                0,
                23
        );

        String sede = vista.leerTextoObligatorio(
                "Nombre de la sede: "
        );

        servicio.programarEvento(
                torneo.getCodigo(),
                evento.getCodigo(),
                fecha,
                hora,
                sede
        );

        vista.mostrarMensaje(
                "Programación confirmada para "
                        + evento.getCodigo() + "."
        );
    }

    /**
     * La captura se define mediante los campos del evento.
     * No se identifica el deporte para elegir preguntas.
     */
    private void registrarResultado() {
        Torneo torneo = solicitarTorneo();
        Evento evento = solicitarEvento(torneo);

        DatosResultado datos = vista.leerDatosResultado(
                evento.getCamposResultado()
        );

        Torneo actualizado = servicio.registrarResultado(
                torneo.getCodigo(),
                evento.getCodigo(),
                datos
        );

        vista.mostrarMensaje(
                "Resultado registrado correctamente."
        );

        if (actualizado.getEstado() == EstadoTorneo.FINALIZADO) {
            vista.mostrarMensaje(
                    "El torneo finalizó automáticamente. "
                            + "El premio quedó registrado."
            );
        }

        vista.mostrarTabla(actualizado);
    }

    private void mostrarCalendarioTorneo() {
        Torneo torneo = solicitarTorneo();

        vista.mostrarMensaje(
                "Calendario del torneo " + torneo.getCodigo()
        );

        vista.mostrarCalendario(
                servicio.getCalendarioTorneo(torneo.getCodigo())
        );
    }

    private void mostrarCalendarioParticipante() {
        String codigo = vista.leerTextoObligatorio(
                "Código del participante: "
        );

        vista.mostrarCalendario(
                servicio.getCalendarioParticipante(codigo)
        );
    }

    private Torneo solicitarTorneo() {
        String codigo = vista.leerTextoObligatorio(
                "Código del torneo: "
        );

        return servicio.buscarTorneo(codigo).orElseThrow(
                () -> new IllegalArgumentException(
                        "No existe un torneo con ese código."
                )
        );
    }

    private Disciplina solicitarDisciplina() {
        String codigo = vista.leerTextoObligatorio(
                "Código de la disciplina: "
        );

        return servicio.buscarDisciplina(codigo).orElseThrow(
                () -> new IllegalArgumentException(
                        "No existe una disciplina con ese código."
                )
        );
    }

    private Evento solicitarEvento(Torneo torneo) {
        String codigo = vista.leerTextoObligatorio(
                "Código del evento: "
        );

        return torneo.buscarEvento(codigo).orElseThrow(
                () -> new IllegalArgumentException(
                        "El evento no pertenece al torneo indicado."
                )
        );
    }
}