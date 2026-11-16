package arenagt.modelo.eventos;

import arenagt.modelo.Participante;
import arenagt.modelo.disciplinas.Disciplina;
import arenagt.modelo.resultados.CampoResultado;
import arenagt.modelo.resultados.DatosResultado;
import arenagt.modelo.resultados.ResultadoEvento;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Comparte los datos y las operaciones comunes de los eventos.
 *
 * Encuentro y Carrera utilizarán esta clase por composición.
 * No es una implementación de Evento ni una superclase.
 *
 * Su acceso se limita al paquete de eventos para mantener
 * este detalle de implementación fuera de los servicios.
 *
 * Es inmutable y no modifica tablas ni registros financieros.
 */
final class RegistroEvento {

    private final String codigo;
    private final Disciplina disciplina;
    private final List<Participante> participantes;
    private final Programacion programacion;
    private final ResultadoEvento resultado;

    /**
     * Construye un registro sin programación ni resultado.
     */
    RegistroEvento(
            String codigo,
            Disciplina disciplina,
            List<Participante> participantes
    ) {
        this(codigo, disciplina, participantes, null, null);
    }

    /**
     * Construye una versión del registro.
     *
     * Solo las operaciones de esta clase pueden proporcionar
     * una programación o un resultado.
     */
    private RegistroEvento(
            String codigo,
            Disciplina disciplina,
            List<Participante> participantes,
            Programacion programacion,
            ResultadoEvento resultado
    ) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El código del evento es obligatorio."
            );
        }

        if (disciplina == null) {
            throw new IllegalArgumentException(
                    "La disciplina del evento es obligatoria."
            );
        }

        if (participantes == null || participantes.isEmpty()) {
            throw new IllegalArgumentException(
                    "El evento debe tener participantes."
            );
        }

        List<Participante> copia = new ArrayList<>();
        Set<Participante> encontrados = new HashSet<>();

        for (Participante participante : participantes) {
            if (participante == null) {
                throw new IllegalArgumentException(
                        "El evento no admite participantes nulos."
                );
            }

            if (!encontrados.add(participante)) {
                throw new IllegalArgumentException(
                        "Un participante no puede repetirse "
                                + "dentro del mismo evento."
                );
            }

            copia.add(participante);
        }

        if (resultado != null && programacion == null) {
            throw new IllegalArgumentException(
                    "Un resultado requiere una programación."
            );
        }

        this.codigo = codigo.trim().toUpperCase(Locale.ROOT);
        this.disciplina = disciplina;
        this.participantes = Collections.unmodifiableList(copia);
        this.programacion = programacion;
        this.resultado = resultado;
    }

    String getCodigo() {
        return codigo;
    }

    List<Participante> getParticipantes() {
        return participantes;
    }

    /**
     * El estado se obtiene a partir de los datos existentes,
     * evitando almacenar otro valor que pueda contradecirlos.
     */
    EstadoEvento getEstado() {
        if (resultado != null) {
            return EstadoEvento.FINALIZADO;
        }

        if (programacion != null) {
            return EstadoEvento.PROGRAMADO;
        }

        return EstadoEvento.SIN_PROGRAMAR;
    }

    Optional<Programacion> getProgramacion() {
        return Optional.ofNullable(programacion);
    }

    Optional<ResultadoEvento> getResultado() {
        return Optional.ofNullable(resultado);
    }

    /**
     * Solicita los campos mediante polimorfismo.
     *
     * Protege la lista y comprueba que las claves
     * no se repitan durante la captura.
     */
    List<CampoResultado> getCamposResultado() {
        List<CampoResultado> campos =
                disciplina.getCamposResultado(participantes);

        if (campos == null) {
            throw new IllegalArgumentException(
                    "La disciplina debe proporcionar "
                            + "los campos del resultado."
            );
        }

        List<CampoResultado> copia = new ArrayList<>();
        Set<String> claves = new HashSet<>();

        for (CampoResultado campo : campos) {
            if (campo == null) {
                throw new IllegalArgumentException(
                        "Los campos del resultado no pueden ser nulos."
                );
            }

            if (!claves.add(campo.getClave())) {
                throw new IllegalArgumentException(
                        "Las claves de los campos no pueden repetirse."
                );
            }

            copia.add(campo);
        }

        return Collections.unmodifiableList(copia);
    }

    /**
     * Devuelve una versión programada o reprogramada.
     *
     * Los conflictos globales se comprueban en el servicio.
     */
    RegistroEvento programar(Programacion nuevaProgramacion) {
        if (nuevaProgramacion == null) {
            throw new IllegalArgumentException(
                    "La programación es obligatoria."
            );
        }

        if (resultado != null) {
            throw new IllegalArgumentException(
                    "Un evento finalizado no puede reprogramarse."
            );
        }

        return new RegistroEvento(
                codigo,
                disciplina,
                participantes,
                nuevaProgramacion,
                null
        );
    }

    /**
     * Calcula y valida el resultado completo antes de devolver
     * una versión finalizada del registro.
     */
    RegistroEvento registrarResultado(DatosResultado datos) {
        if (programacion == null) {
            throw new IllegalArgumentException(
                    "Debe programar el evento antes "
                            + "de registrar su resultado."
            );
        }

        if (resultado != null) {
            throw new IllegalArgumentException(
                    "El evento ya tiene un resultado registrado."
            );
        }

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos del resultado son obligatorios."
            );
        }

        ResultadoEvento nuevoResultado =
                disciplina.evaluarResultado(participantes, datos);

        if (nuevoResultado == null) {
            throw new IllegalArgumentException(
                    "La disciplina debe devolver un resultado."
            );
        }

        Set<Participante> esperados = new HashSet<>(participantes);

        if (!nuevoResultado.getPuntuaciones().keySet().equals(esperados)) {
            throw new IllegalArgumentException(
                    "El resultado debe incluir exactamente "
                            + "a los participantes del evento."
            );
        }

        return new RegistroEvento(
                codigo,
                disciplina,
                participantes,
                programacion,
                nuevoResultado
        );
    }
}