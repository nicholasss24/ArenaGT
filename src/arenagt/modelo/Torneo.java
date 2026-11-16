package arenagt.modelo;

import arenagt.modelo.disciplinas.Disciplina;
import arenagt.modelo.eventos.Calendario;
import arenagt.modelo.eventos.EstadoEvento;
import arenagt.modelo.eventos.Evento;
import arenagt.modelo.eventos.Programacion;
import arenagt.modelo.resultados.DatosResultado;
import arenagt.modelo.resultados.Puntuacion;
import arenagt.modelo.resultados.ResultadoEvento;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Representa un torneo de una sola disciplina.
 *
 * Es inmutable: sus operaciones devuelven nuevas versiones.
 * No registra cobros ni entrega premios directamente.
 */
public final class Torneo {

    private final String codigo;
    private final String nombre;
    private final Disciplina disciplina;
    private final EstadoTorneo estado;

    private final Map<String, Inscripcion> inscripciones;
    private final Map<String, Evento> eventos;

    /**
     * Construye un torneo vacío y en inscripción.
     */
    public Torneo(
            String codigo,
            String nombre,
            Disciplina disciplina
    ) {
        this(
                codigo,
                nombre,
                disciplina,
                EstadoTorneo.EN_INSCRIPCION,
                Collections.<String, Inscripcion>emptyMap(),
                Collections.<String, Evento>emptyMap()
        );
    }

    /**
     * Construye una versión interna del torneo.
     */
    private Torneo(
            String codigo,
            String nombre,
            Disciplina disciplina,
            EstadoTorneo estado,
            Map<String, Inscripcion> inscripciones,
            Map<String, Evento> eventos
    ) {
        this.codigo = normalizarCodigo(codigo);

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre del torneo es obligatorio."
            );
        }

        if (disciplina == null) {
            throw new IllegalArgumentException(
                    "La disciplina del torneo es obligatoria."
            );
        }

        this.nombre = nombre.trim();
        this.disciplina = disciplina;
        this.estado = estado;

        this.inscripciones = Collections.unmodifiableMap(
                new LinkedHashMap<>(inscripciones)
        );

        this.eventos = Collections.unmodifiableMap(
                new LinkedHashMap<>(eventos)
        );
    }

    /**
     * Prepara directamente los participantes de un torneo inicial.
     *
     * Decisión de diseño: la carga de demostración utiliza
     * esta fábrica en lugar de la operación normal inscribir.
     * Conserva las validaciones sin generar movimientos financieros.
     *
     * No permite modificar un torneo existente.
     */
    public static Torneo conParticipantesIniciales(
            String codigo,
            String nombre,
            Disciplina disciplina,
            List<Participante> participantes
    ) {
        Torneo nuevo = new Torneo(codigo, nombre, disciplina);

        if (participantes == null) {
            throw new IllegalArgumentException(
                    "Los participantes iniciales son obligatorios."
            );
        }

        List<Participante> copia = new ArrayList<>(participantes);

        Map<String, Inscripcion> cargadas = new LinkedHashMap<>();

        for (Participante participante : copia) {
            disciplina.validarInscripcion(
                    participante,
                    Collections.unmodifiableMap(cargadas).values()
            );

            cargadas.put(
                    participante.getCodigo(),
                    new Inscripcion(participante)
            );
        }

        return nuevo.crearVersion(
                EstadoTorneo.EN_INSCRIPCION,
                cargadas,
                nuevo.eventos
        );
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    public EstadoTorneo getEstado() {
        return estado;
    }

    public int getCantidadParticipantes() {
        return inscripciones.size();
    }

    public int getCantidadEventos() {
        return eventos.size();
    }

    public int getCantidadResultados() {
        int cantidad = 0;

        for (Evento evento : eventos.values()) {
            if (evento.getResultado().isPresent()) {
                cantidad++;
            }
        }

        return cantidad;
    }

    public Iterable<Inscripcion> getInscripciones() {
        return inscripciones.values();
    }

    /**
     * Incluye todos los eventos, también los no programados.
     */
    public Iterable<Evento> getEventos() {
        return eventos.values();
    }

    public Optional<Inscripcion> buscarInscripcion(
            String codigoParticipante
    ) {
        return Optional.ofNullable(
                inscripciones.get(normalizarCodigo(codigoParticipante))
        );
    }

    public Optional<Evento> buscarEvento(String codigoEvento) {
        return Optional.ofNullable(
                eventos.get(normalizarCodigo(codigoEvento))
        );
    }

    /**
     * Prepara una inscripción normal.
     *
     * El servicio coordina esta nueva versión con el cobro.
     */
    public Torneo inscribir(Participante participante) {
        exigirEstado(EstadoTorneo.EN_INSCRIPCION);

        disciplina.validarInscripcion(
                participante,
                inscripciones.values()
        );

        Map<String, Inscripcion> nuevasInscripciones =
                new LinkedHashMap<>(inscripciones);

        nuevasInscripciones.put(
                participante.getCodigo(),
                new Inscripcion(participante)
        );

        return crearVersion(
                estado,
                nuevasInscripciones,
                eventos
        );
    }

    /**
     * Cierra inscripciones y genera los eventos
     * mediante las reglas de la disciplina.
     */
    public Torneo cerrarInscripciones() {
        exigirEstado(EstadoTorneo.EN_INSCRIPCION);

        disciplina.validarCierreInscripciones(
                inscripciones.size()
        );

        List<Participante> participantes = new ArrayList<>();

        for (Inscripcion inscripcion : inscripciones.values()) {
            participantes.add(inscripcion.getParticipante());
        }

        List<Evento> generados = disciplina.crearEventos(
                codigo,
                Collections.unmodifiableList(participantes)
        );

        if (generados == null || generados.isEmpty()) {
            throw new IllegalArgumentException(
                    "La disciplina debe generar eventos."
            );
        }

        Map<String, Evento> nuevosEventos = new LinkedHashMap<>();
        Set<Participante> inscritos = new HashSet<>(participantes);
        Set<Participante> incluidos = new HashSet<>();

        for (Evento evento : generados) {
            if (evento == null) {
                throw new IllegalArgumentException(
                        "La disciplina no puede generar eventos nulos."
                );
            }

            String codigoEvento = normalizarCodigo(
                    evento.getCodigo()
            );

            if (nuevosEventos.containsKey(codigoEvento)) {
                throw new IllegalArgumentException(
                        "Los códigos de evento no pueden repetirse."
                );
            }

            if (evento.getEstado() != EstadoEvento.SIN_PROGRAMAR
                    || evento.getProgramacion().isPresent()
                    || evento.getResultado().isPresent()) {
                throw new IllegalArgumentException(
                        "Los eventos deben generarse sin "
                                + "programación ni resultado."
                );
            }

            List<Participante> integrantes = evento.getParticipantes();

            if (integrantes == null || integrantes.isEmpty()) {
                throw new IllegalArgumentException(
                        "Cada evento debe tener participantes."
                );
            }

            Set<Participante> unicos = new HashSet<>();

            for (Participante participante : integrantes) {
                if (participante == null
                        || !inscritos.contains(participante)) {
                    throw new IllegalArgumentException(
                            "Los eventos solo pueden incluir "
                                    + "participantes inscritos."
                    );
                }

                if (!unicos.add(participante)) {
                    throw new IllegalArgumentException(
                            "Un evento no puede repetir participantes."
                    );
                }
            }

            incluidos.addAll(unicos);
            nuevosEventos.put(codigoEvento, evento);
        }

        if (!incluidos.equals(inscritos)) {
            throw new IllegalArgumentException(
                    "Todos los inscritos deben participar "
                            + "en algún evento."
            );
        }

        return crearVersion(
                EstadoTorneo.EN_JUEGO,
                inscripciones,
                nuevosEventos
        );
    }

    /**
     * Prepara una programación o reprogramación.
     *
     * El servicio comprueba los conflictos entre torneos.
     */
    public Torneo programarEvento(
            String codigoEvento,
            Programacion programacion
    ) {
        exigirEstado(EstadoTorneo.EN_JUEGO);

        Evento original = obtenerEvento(codigoEvento);
        Evento actualizado = original.programar(programacion);

        validarIdentidadEvento(original, actualizado);

        if (actualizado.getEstado() != EstadoEvento.PROGRAMADO
                || actualizado.getResultado().isPresent()
                || !actualizado.getProgramacion().isPresent()
                || !actualizado.getProgramacion().get()
                        .equals(programacion)) {
            throw new IllegalArgumentException(
                    "El evento no devolvió una programación válida."
            );
        }

        Map<String, Evento> nuevosEventos =
                new LinkedHashMap<>(eventos);

        nuevosEventos.put(
                normalizarCodigo(codigoEvento),
                actualizado
        );

        return crearVersion(
                estado,
                inscripciones,
                nuevosEventos
        );
    }

    /**
     * Prepara el resultado y todas las estadísticas.
     *
     * Finaliza automáticamente si todos los eventos
     * tienen resultado.
     */
    public Torneo registrarResultado(
            String codigoEvento,
            DatosResultado datos
    ) {
        exigirEstado(EstadoTorneo.EN_JUEGO);

        Evento original = obtenerEvento(codigoEvento);
        Evento actualizado = original.registrarResultado(datos);

        validarIdentidadEvento(original, actualizado);

        if (actualizado.getEstado() != EstadoEvento.FINALIZADO
                || !actualizado.getResultado().isPresent()
                || !actualizado.getProgramacion()
                        .equals(original.getProgramacion())) {
            throw new IllegalArgumentException(
                    "El evento no devolvió un resultado válido."
            );
        }

        ResultadoEvento resultado =
                actualizado.getResultado().get();

        Set<Participante> esperados = new HashSet<>(
                original.getParticipantes()
        );

        if (!resultado.getPuntuaciones().keySet().equals(esperados)) {
            throw new IllegalArgumentException(
                    "El resultado debe incluir exactamente "
                            + "a los participantes del evento."
            );
        }

        Map<String, Inscripcion> nuevasInscripciones =
                new LinkedHashMap<>(inscripciones);

        for (Map.Entry<Participante, Puntuacion> aporte
                : resultado.getPuntuaciones().entrySet()) {
            String codigoParticipante =
                    aporte.getKey().getCodigo();

            Inscripcion anterior =
                    inscripciones.get(codigoParticipante);

            if (anterior == null) {
                throw new IllegalArgumentException(
                        "El resultado incluye un participante "
                                + "no inscrito."
                );
            }

            nuevasInscripciones.put(
                    codigoParticipante,
                    anterior.acumular(aporte.getValue())
            );
        }

        Map<String, Evento> nuevosEventos =
                new LinkedHashMap<>(eventos);

        nuevosEventos.put(
                normalizarCodigo(codigoEvento),
                actualizado
        );

        boolean todosFinalizados = true;

        for (Evento evento : nuevosEventos.values()) {
            if (evento.getEstado() != EstadoEvento.FINALIZADO
                    || !evento.getResultado().isPresent()) {
                todosFinalizados = false;
                break;
            }
        }

        EstadoTorneo nuevoEstado = todosFinalizados
                ? EstadoTorneo.FINALIZADO
                : EstadoTorneo.EN_JUEGO;

        return crearVersion(
                nuevoEstado,
                nuevasInscripciones,
                nuevosEventos
        );
    }

    /**
     * El criterio de orden se encuentra en Inscripcion.
     */
    public List<Inscripcion> getTablaPosiciones() {
        List<Inscripcion> tabla =
                new ArrayList<>(inscripciones.values());

        Collections.sort(tabla);

        return Collections.unmodifiableList(tabla);
    }

    public Optional<Participante> getCampeon() {
        if (estado != EstadoTorneo.FINALIZADO) {
            return Optional.empty();
        }

        return Optional.of(
                getTablaPosiciones().get(0).getParticipante()
        );
    }

    public Calendario getCalendario() {
        return new Calendario(eventos.values());
    }

    private Evento obtenerEvento(String codigoEvento) {
        Evento evento = eventos.get(
                normalizarCodigo(codigoEvento)
        );

        if (evento == null) {
            throw new IllegalArgumentException(
                    "El evento no pertenece a este torneo."
            );
        }

        return evento;
    }

    private void exigirEstado(EstadoTorneo requerido) {
        if (estado != requerido) {
            throw new IllegalArgumentException(
                    "La operación requiere un torneo en estado "
                            + requerido.getDescripcion() + "."
            );
        }
    }

    private void validarIdentidadEvento(
            Evento original,
            Evento actualizado
    ) {
        if (actualizado == null) {
            throw new IllegalArgumentException(
                    "La operación debe devolver un evento."
            );
        }

        if (!normalizarCodigo(original.getCodigo()).equals(
                normalizarCodigo(actualizado.getCodigo())
        )) {
            throw new IllegalArgumentException(
                    "La operación no puede cambiar "
                            + "el código del evento."
            );
        }

        if (!original.getParticipantes().equals(
                actualizado.getParticipantes()
        )) {
            throw new IllegalArgumentException(
                    "La operación no puede cambiar "
                            + "los participantes del evento."
            );
        }
    }

    private Torneo crearVersion(
            EstadoTorneo nuevoEstado,
            Map<String, Inscripcion> nuevasInscripciones,
            Map<String, Evento> nuevosEventos
    ) {
        return new Torneo(
                codigo,
                nombre,
                disciplina,
                nuevoEstado,
                nuevasInscripciones,
                nuevosEventos
        );
    }

    private static String normalizarCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El código es obligatorio."
            );
        }

        return codigo.trim().toUpperCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return "Torneo{"
                + "codigo='" + codigo + '\''
                + ", nombre='" + nombre + '\''
                + ", disciplina=" + disciplina.getNombre()
                + ", estado=" + estado
                + '}';
    }
}