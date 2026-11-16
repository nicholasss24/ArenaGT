package arenagt.modelo.resultados;

import arenagt.modelo.Participante;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Conserva los datos de un resultado y las puntuaciones
 * calculadas para los participantes de un evento.
 *
 * La disciplina valida las reglas deportivas y calcula
 * los aportes antes de construir este objeto.
 *
 * Es inmutable y no modifica inscripciones ni ingresos.
 */
public final class ResultadoEvento {

    private final DatosResultado datos;
    private final Map<Participante, Puntuacion> puntuaciones;

    /**
     * Construye un resultado con una copia de sus puntuaciones.
     *
     * Este constructor valida la estructura de los datos.
     * La disciplina comprueba que los participantes y los
     * valores correspondan a las reglas del evento.
     *
     * @param datos datos utilizados para registrar el resultado
     * @param puntuaciones aporte de cada participante
     */
    public ResultadoEvento(
            DatosResultado datos,
            Map<Participante, Puntuacion> puntuaciones
    ) {
        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos del resultado son obligatorios."
            );
        }

        if (puntuaciones == null || puntuaciones.isEmpty()) {
            throw new IllegalArgumentException(
                    "El resultado debe incluir puntuaciones."
            );
        }

        Map<Participante, Puntuacion> copia =
                new LinkedHashMap<>();

        for (Map.Entry<Participante, Puntuacion> entrada
                : puntuaciones.entrySet()) {
            Participante participante = entrada.getKey();
            Puntuacion puntuacion = entrada.getValue();

            if (participante == null) {
                throw new IllegalArgumentException(
                        "El resultado no puede contener "
                                + "un participante nulo."
                );
            }

            if (puntuacion == null) {
                throw new IllegalArgumentException(
                        "Cada participante debe tener una puntuación."
                );
            }

            copia.put(participante, puntuacion);
        }

        this.datos = datos;
        this.puntuaciones = Collections.unmodifiableMap(copia);
    }

    public DatosResultado getDatos() {
        return datos;
    }

    /**
     * Permite consultar los aportes sin modificarlos.
     *
     * Participante y Puntuacion también son inmutables.
     */
    public Map<Participante, Puntuacion> getPuntuaciones() {
        return puntuaciones;
    }

    /**
     * Consulta el aporte de un participante del evento.
     *
     * No crea puntuaciones para participantes ausentes.
     */
    public Puntuacion getPuntuacion(Participante participante) {
        if (participante == null) {
            throw new IllegalArgumentException(
                    "El participante es obligatorio."
            );
        }

        Puntuacion puntuacion = puntuaciones.get(participante);

        if (puntuacion == null) {
            throw new IllegalArgumentException(
                    "El participante no pertenece a este resultado."
            );
        }

        return puntuacion;
    }

    @Override
    public String toString() {
        return "ResultadoEvento{"
                + "datos=" + datos
                + ", puntuaciones=" + puntuaciones
                + '}';
    }
}