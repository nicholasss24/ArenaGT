package arenagt.modelo;

import arenagt.modelo.resultados.Puntuacion;

import java.util.Objects;

/**
 * Representa la inscripción de un participante en un torneo.
 *
 * El torneo conserva sus propias inscripciones. Por eso,
 * un participante puede tener estadísticas independientes
 * en diferentes torneos.
 *
 * Es inmutable y define el orden de la tabla de posiciones.
 */
public final class Inscripcion implements Comparable<Inscripcion> {

    private final Participante participante;
    private final Puntuacion puntuacion;

    /**
     * Construye una inscripción sin resultados acumulados.
     *
     * @param participante participante inscrito
     */
    public Inscripcion(Participante participante) {
        this(participante, new Puntuacion());
    }

    /**
     * Construye una nueva versión de la inscripción.
     *
     * Solo esta clase puede asignar directamente
     * la puntuación acumulada.
     */
    private Inscripcion(
            Participante participante,
            Puntuacion puntuacion
    ) {
        if (participante == null) {
            throw new IllegalArgumentException(
                    "El participante de la inscripción es obligatorio."
            );
        }

        if (puntuacion == null) {
            throw new IllegalArgumentException(
                    "La puntuación de la inscripción es obligatoria."
            );
        }

        this.participante = participante;
        this.puntuacion = puntuacion;
    }

    public Participante getParticipante() {
        return participante;
    }

    public Puntuacion getPuntuacion() {
        return puntuacion;
    }

    /**
     * Devuelve una inscripción con la puntuación acumulada.
     *
     * No modifica la inscripción original. El torneo podrá
     * preparar las nuevas estadísticas antes de confirmar
     * el resultado completo de un evento.
     *
     * @param aporte puntuación obtenida en un evento
     * @return nueva versión de la inscripción
     */
    public Inscripcion acumular(Puntuacion aporte) {
        Puntuacion nuevaPuntuacion = puntuacion.sumar(aporte);

        return new Inscripcion(participante, nuevaPuntuacion);
    }

    /**
     * Orden de la tabla de posiciones:
     *
     * 1. Puntos de tabla, de mayor a menor.
     * 2. Diferencia, de mayor a menor.
     * 3. Victorias, de mayor a menor.
     * 4. Nombre, en orden alfabético sin distinguir
     *    mayúsculas y minúsculas.
     */
    @Override
    public int compareTo(Inscripcion otra) {
        Objects.requireNonNull(
                otra,
                "La inscripción comparada no puede ser nula."
        );

        int comparacion = Integer.compare(
                otra.puntuacion.getPuntosTabla(),
                this.puntuacion.getPuntosTabla()
        );

        if (comparacion != 0) {
            return comparacion;
        }

        comparacion = Long.compare(
                otra.puntuacion.getDiferencia(),
                this.puntuacion.getDiferencia()
        );

        if (comparacion != 0) {
            return comparacion;
        }

        comparacion = Integer.compare(
                otra.puntuacion.getVictorias(),
                this.puntuacion.getVictorias()
        );

        if (comparacion != 0) {
            return comparacion;
        }

        return this.participante.getNombre().compareToIgnoreCase(
                otra.participante.getNombre()
        );
    }

    @Override
    public String toString() {
        return "Inscripcion{"
                + "participante=" + participante
                + ", puntuacion=" + puntuacion
                + '}';
    }
}