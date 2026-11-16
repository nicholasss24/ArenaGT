package arenagt.servicio;

import arenagt.modelo.Empresa;
import arenagt.modelo.Inscripcion;
import arenagt.modelo.Participante;
import arenagt.modelo.PosicionEmpresa;
import arenagt.modelo.RegistroFinanciero;
import arenagt.modelo.EstadoTorneo;
import arenagt.modelo.Torneo;
import arenagt.modelo.disciplinas.Disciplina;
import arenagt.modelo.eventos.Calendario;
import arenagt.modelo.eventos.Evento;
import arenagt.modelo.eventos.Programacion;
import arenagt.modelo.resultados.DatosResultado;

import java.math.BigDecimal;
import java.time.LocalDate;
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
 * Coordina los registros y las operaciones de ArenaGT.
 *
 * Conserva versiones protegidas de los catálogos.
 * Las reglas deportivas se resuelven mediante Disciplina
 * y Evento, sin identificar sus clases concretas.
 *
 * Prepara todos los cambios antes de confirmarlos.
 * No realiza entradas ni salidas por consola.
 */
public final class ServicioArena {

    private Map<String, Disciplina> disciplinas;
    private Map<String, Participante> participantes;
    private Map<String, Torneo> torneos;

    private RegistroFinanciero registroFinanciero;

    /**
     * Construye un sistema vacío.
     *
     * La carga inicial se prepara en una clase independiente.
     */
    public ServicioArena() {
        disciplinas = Collections.emptyMap();
        participantes = Collections.emptyMap();
        torneos = Collections.emptyMap();

        registroFinanciero = new RegistroFinanciero();
    }

    public Iterable<Disciplina> getDisciplinas() {
        return disciplinas.values();
    }

    public Iterable<Participante> getParticipantes() {
        return participantes.values();
    }

    public Iterable<Torneo> getTorneos() {
        return torneos.values();
    }

    /**
     * El registro es inmutable y puede consultarse
     * sin permitir cambios en las finanzas del servicio.
     */
    public RegistroFinanciero getRegistroFinanciero() {
        return registroFinanciero;
    }

    public Optional<Disciplina> buscarDisciplina(String codigo) {
        return Optional.ofNullable(
                disciplinas.get(normalizarCodigo(codigo))
        );
    }

    public Optional<Participante> buscarParticipante(String codigo) {
        return Optional.ofNullable(
                participantes.get(normalizarCodigo(codigo))
        );
    }

    public Optional<Torneo> buscarTorneo(String codigo) {
        return Optional.ofNullable(
                torneos.get(normalizarCodigo(codigo))
        );
    }

    /**
     * Permite incorporar nuevas disciplinas sin modificar
     * las operaciones deportivas del servicio.
     */
    public void registrarDisciplina(Disciplina disciplina) {
        if (disciplina == null) {
            throw new IllegalArgumentException(
                    "La disciplina es obligatoria."
            );
        }

        if (disciplinas.containsKey(disciplina.getCodigo())) {
            throw new IllegalArgumentException(
                    "Ya existe una disciplina con ese código."
            );
        }

        Map<String, Disciplina> nuevas =
                new LinkedHashMap<>(disciplinas);

        nuevas.put(disciplina.getCodigo(), disciplina);

        disciplinas = Collections.unmodifiableMap(nuevas);
    }

    /**
     * Registra un participante con código global único.
     *
     * Reutiliza la empresa existente si su nombre
     * representa la misma identidad.
     */
    public Participante registrarParticipante(
            String codigo,
            String nombre,
            String nombreEmpresa
    ) {
        Empresa empresa = new Empresa(nombreEmpresa);

        for (Participante existente : participantes.values()) {
            if (existente.getEmpresa().equals(empresa)) {
                empresa = existente.getEmpresa();
                break;
            }
        }

        Participante nuevo = new Participante(
                codigo,
                nombre,
                empresa
        );

        if (participantes.containsKey(nuevo.getCodigo())) {
            throw new IllegalArgumentException(
                    "Ya existe un participante con ese código."
            );
        }

        Map<String, Participante> nuevos =
                new LinkedHashMap<>(participantes);

        nuevos.put(nuevo.getCodigo(), nuevo);

        participantes = Collections.unmodifiableMap(nuevos);

        return nuevo;
    }

    /**
     * Crea un torneo vacío y en estado de inscripción.
     */
    public Torneo crearTorneo(
            String codigo,
            String nombre,
            String codigoDisciplina
    ) {
        Disciplina disciplina = obtenerDisciplina(codigoDisciplina);

        Torneo nuevo = new Torneo(codigo, nombre, disciplina);

        if (torneos.containsKey(nuevo.getCodigo())) {
            throw new IllegalArgumentException(
                    "Ya existe un torneo con ese código."
            );
        }

        torneos = conTorneo(nuevo);

        return nuevo;
    }

    /**
     * Consulta una cotización sin modificar registros.
     */
    public BigDecimal cotizarInscripcion(
            String codigoDisciplina,
            int cantidad
    ) {
        return obtenerDisciplina(codigoDisciplina)
                .cotizarInscripcion(cantidad);
    }

    /**
     * Prepara la inscripción y su ingreso.
     *
     * Las asignaciones finales se realizan después
     * de completar las validaciones y los cálculos.
     */
    public Torneo inscribirParticipante(
            String codigoTorneo,
            String codigoParticipante
    ) {
        Torneo anterior = obtenerTorneo(codigoTorneo);

        Participante participante =
                obtenerParticipante(codigoParticipante);

        Torneo actualizado = anterior.inscribir(participante);

        RegistroFinanciero nuevasFinanzas =
                registroFinanciero.registrarIngresoInscripcion(
                        anterior.getDisciplina()
                );

        Map<String, Torneo> nuevosTorneos =
                conTorneo(actualizado);

        torneos = nuevosTorneos;
        registroFinanciero = nuevasFinanzas;

        return actualizado;
    }

    /**
     * Cierra inscripciones y valida la unicidad global
     * de los códigos de los eventos generados.
     */
    public Torneo cerrarInscripciones(String codigoTorneo) {
        Torneo actualizado = obtenerTorneo(codigoTorneo)
                .cerrarInscripciones();

        Map<String, Torneo> nuevosTorneos =
                conTorneo(actualizado);

        validarEventosUnicos(nuevosTorneos.values());

        torneos = nuevosTorneos;

        return actualizado;
    }

    /**
     * Programa o reprograma un evento.
     *
     * Comprueba sede y participantes en todos los torneos.
     * Se excluye el propio evento al revisar una reprogramación.
     */
    public Torneo programarEvento(
            String codigoTorneo,
            String codigoEvento,
            LocalDate fecha,
            int horaInicio,
            String sede
    ) {
        Torneo anterior = obtenerTorneo(codigoTorneo);

        Evento evento = anterior.buscarEvento(codigoEvento)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El evento no pertenece al torneo."
                ));

        Programacion programacion = new Programacion(
                fecha,
                horaInicio,
                sede
        );

        Torneo actualizado = anterior.programarEvento(
                codigoEvento,
                programacion
        );

        validarConflictos(
                torneos.values(),
                evento.getCodigo(),
                programacion,
                evento.getParticipantes()
        );

        Map<String, Torneo> nuevosTorneos =
                conTorneo(actualizado);

        torneos = nuevosTorneos;

        return actualizado;
    }

    /**
     * Registra un resultado y prepara el premio cuando
     * el torneo finaliza automáticamente.
     *
     * Si falla el resultado o la preparación del premio,
     * se conservan el torneo y las finanzas anteriores.
     */
    public Torneo registrarResultado(
            String codigoTorneo,
            String codigoEvento,
            DatosResultado datos
    ) {
        Torneo anterior = obtenerTorneo(codigoTorneo);

        Torneo actualizado = anterior.registrarResultado(
                codigoEvento,
                datos
        );

        RegistroFinanciero nuevasFinanzas = registroFinanciero;

        if (actualizado.getEstado() == EstadoTorneo.FINALIZADO) {
            nuevasFinanzas = registroFinanciero.registrarPremio(
                    actualizado
            );
        }

        Map<String, Torneo> nuevosTorneos =
                conTorneo(actualizado);

        torneos = nuevosTorneos;
        registroFinanciero = nuevasFinanzas;

        return actualizado;
    }

    public List<Inscripcion> getTablaPosiciones(
            String codigoTorneo
    ) {
        return obtenerTorneo(codigoTorneo).getTablaPosiciones();
    }

    /**
     * Reúne todos los tipos de evento.
     *
     * Calendario filtra los no programados y centraliza
     * el orden y el recorrido.
     */
    public Calendario getCalendarioGeneral() {
        List<Evento> todos = new ArrayList<>();

        for (Torneo torneo : torneos.values()) {
            for (Evento evento : torneo.getEventos()) {
                todos.add(evento);
            }
        }

        return new Calendario(todos);
    }

    public Calendario getCalendarioTorneo(String codigoTorneo) {
        return obtenerTorneo(codigoTorneo).getCalendario();
    }

    public Calendario getCalendarioParticipante(
            String codigoParticipante
    ) {
        Participante participante =
                obtenerParticipante(codigoParticipante);

        return getCalendarioGeneral()
                .filtrarPorParticipante(participante);
    }

    /**
     * Calcula el ranking a partir de las inscripciones.
     *
     * Incluye con cero puntos a las empresas registradas
     * cuyos participantes todavía no tienen resultados.
     *
     * No conserva un segundo total permanente por empresa.
     */
    public List<PosicionEmpresa> getRankingEmpresas() {
        Map<Empresa, Long> acumulados = new LinkedHashMap<>();

        for (Participante participante : participantes.values()) {
            acumulados.putIfAbsent(
                    participante.getEmpresa(),
                    0L
            );
        }

        for (Torneo torneo : torneos.values()) {
            for (Inscripcion inscripcion : torneo.getInscripciones()) {
                Empresa empresa = inscripcion.getParticipante()
                        .getEmpresa();

                long anterior = acumulados.getOrDefault(
                        empresa,
                        0L
                );

                long nuevoTotal;

                try {
                    nuevoTotal = Math.addExact(
                            anterior,
                            inscripcion.getPuntuacion().getPuntosTabla()
                    );
                } catch (ArithmeticException excepcion) {
                    throw new IllegalArgumentException(
                            "El ranking supera los límites numéricos.",
                            excepcion
                    );
                }

                acumulados.put(empresa, nuevoTotal);
            }
        }

        List<PosicionEmpresa> ranking = new ArrayList<>();

        for (Map.Entry<Empresa, Long> entrada
                : acumulados.entrySet()) {
            ranking.add(new PosicionEmpresa(
                    entrada.getKey(),
                    entrada.getValue()
            ));
        }

        Collections.sort(ranking);

        return Collections.unmodifiableList(ranking);
    }

    /**
     * Incorpora los datos iniciales directamente.
     *
     * Recibe torneos preparados sin utilizar las operaciones
     * financieras de inscripción y premiación.
     *
     * Solo se permite sobre un servicio vacío. Todos los datos
     * se validan antes de confirmar la carga.
     */
    public void cargarDatosIniciales(
            Iterable<Disciplina> disciplinasIniciales,
            Iterable<Participante> participantesIniciales,
            Iterable<Torneo> torneosIniciales
    ) {
        if (disciplinasIniciales == null
                || participantesIniciales == null
                || torneosIniciales == null) {
            throw new IllegalArgumentException(
                    "Los datos iniciales son obligatorios."
            );
        }

        if (!disciplinas.isEmpty()
                || !participantes.isEmpty()
                || !torneos.isEmpty()) {
            throw new IllegalArgumentException(
                    "La carga inicial requiere un servicio vacío."
            );
        }

        Map<String, Disciplina> nuevasDisciplinas =
                new LinkedHashMap<>();

        for (Disciplina disciplina : disciplinasIniciales) {
            if (disciplina == null
                    || nuevasDisciplinas.containsKey(
                            disciplina.getCodigo()
                    )) {
                throw new IllegalArgumentException(
                        "Las disciplinas iniciales no pueden "
                                + "ser nulas ni repetir códigos."
                );
            }

            nuevasDisciplinas.put(
                    disciplina.getCodigo(),
                    disciplina
            );
        }

        Map<String, Participante> nuevosParticipantes =
                new LinkedHashMap<>();

        for (Participante participante : participantesIniciales) {
            if (participante == null
                    || nuevosParticipantes.containsKey(
                            participante.getCodigo()
                    )) {
                throw new IllegalArgumentException(
                        "Los participantes iniciales no pueden "
                                + "ser nulos ni repetir códigos."
                );
            }

            nuevosParticipantes.put(
                    participante.getCodigo(),
                    participante
            );
        }

        Map<String, Torneo> nuevosTorneos =
                new LinkedHashMap<>();

        for (Torneo torneo : torneosIniciales) {
            if (torneo == null
                    || nuevosTorneos.containsKey(torneo.getCodigo())) {
                throw new IllegalArgumentException(
                        "Los torneos iniciales no pueden "
                                + "ser nulos ni repetir códigos."
                );
            }

            Disciplina registrada = nuevasDisciplinas.get(
                    torneo.getDisciplina().getCodigo()
            );

            if (registrada != torneo.getDisciplina()) {
                throw new IllegalArgumentException(
                        "El torneo debe utilizar una disciplina "
                                + "del registro inicial."
                );
            }

            for (Inscripcion inscripcion : torneo.getInscripciones()) {
                validarParticipanteInicial(
                        inscripcion.getParticipante(),
                        nuevosParticipantes
                );
            }

            for (Evento evento : torneo.getEventos()) {
                for (Participante participante
                        : evento.getParticipantes()) {
                    validarParticipanteInicial(
                            participante,
                            nuevosParticipantes
                    );
                }
            }

            nuevosTorneos.put(torneo.getCodigo(), torneo);
        }

        validarEventosUnicos(nuevosTorneos.values());

        for (Torneo torneo : nuevosTorneos.values()) {
            for (Evento evento : torneo.getEventos()) {
                if (evento.getProgramacion().isPresent()) {
                    validarConflictos(
                            nuevosTorneos.values(),
                            evento.getCodigo(),
                            evento.getProgramacion().get(),
                            evento.getParticipantes()
                    );
                }
            }
        }

        Map<String, Disciplina> disciplinasPreparadas =
                Collections.unmodifiableMap(nuevasDisciplinas);

        Map<String, Participante> participantesPreparados =
                Collections.unmodifiableMap(nuevosParticipantes);

        Map<String, Torneo> torneosPreparados =
                Collections.unmodifiableMap(nuevosTorneos);

        RegistroFinanciero finanzasPreparadas =
                new RegistroFinanciero();

        disciplinas = disciplinasPreparadas;
        participantes = participantesPreparados;
        torneos = torneosPreparados;
        registroFinanciero = finanzasPreparadas;
    }

    /**
     * Comprueba conflictos sin identificar tipos de evento.
     *
     * También considera los eventos ya finalizados,
     * porque conservan su programación.
     */
    private static void validarConflictos(
            Iterable<Torneo> catalogo,
            String codigoExcluido,
            Programacion propuesta,
            List<Participante> participantesEvento
    ) {
        Set<Participante> involucrados =
                new HashSet<>(participantesEvento);

        String excluido = normalizarCodigo(codigoExcluido);

        for (Torneo torneo : catalogo) {
            for (Evento existente : torneo.getEventos()) {
                if (normalizarCodigo(existente.getCodigo())
                        .equals(excluido)) {
                    continue;
                }

                if (!existente.getProgramacion().isPresent()) {
                    continue;
                }

                Programacion ocupada =
                        existente.getProgramacion().get();

                if (!propuesta.coincideHorario(ocupada)) {
                    continue;
                }

                if (propuesta.coincideSedeYHorario(ocupada)) {
                    throw new IllegalArgumentException(
                            "La sede ya tiene otro evento "
                                    + "en esa fecha y hora."
                    );
                }

                for (Participante participante
                        : existente.getParticipantes()) {
                    if (involucrados.contains(participante)) {
                        throw new IllegalArgumentException(
                                "El participante "
                                        + participante.getCodigo()
                                        + " ya tiene otro evento "
                                        + "en esa fecha y hora."
                        );
                    }
                }
            }
        }
    }

    private static void validarEventosUnicos(
            Iterable<Torneo> catalogo
    ) {
        Set<String> codigos = new HashSet<>();

        for (Torneo torneo : catalogo) {
            for (Evento evento : torneo.getEventos()) {
                if (!codigos.add(
                        normalizarCodigo(evento.getCodigo())
                )) {
                    throw new IllegalArgumentException(
                            "Los códigos de evento deben "
                                    + "ser únicos entre todos los torneos."
                    );
                }
            }
        }
    }

    private static void validarParticipanteInicial(
            Participante participante,
            Map<String, Participante> registro
    ) {
        if (participante == null
                || registro.get(participante.getCodigo())
                        != participante) {
            throw new IllegalArgumentException(
                    "Los torneos deben utilizar los participantes "
                            + "del registro inicial."
            );
        }
    }

    private Map<String, Torneo> conTorneo(Torneo torneo) {
        Map<String, Torneo> copia =
                new LinkedHashMap<>(torneos);

        copia.put(torneo.getCodigo(), torneo);

        return Collections.unmodifiableMap(copia);
    }

    private Disciplina obtenerDisciplina(String codigo) {
        return buscarDisciplina(codigo).orElseThrow(
                () -> new IllegalArgumentException(
                        "No existe una disciplina con ese código."
                )
        );
    }

    private Participante obtenerParticipante(String codigo) {
        return buscarParticipante(codigo).orElseThrow(
                () -> new IllegalArgumentException(
                        "No existe un participante con ese código."
                )
        );
    }

    private Torneo obtenerTorneo(String codigo) {
        return buscarTorneo(codigo).orElseThrow(
                () -> new IllegalArgumentException(
                        "No existe un torneo con ese código."
                )
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
}