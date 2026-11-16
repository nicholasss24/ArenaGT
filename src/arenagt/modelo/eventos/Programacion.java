package arenagt.modelo.eventos;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

/**
 * Representa la programación de un evento deportivo.
 *
 * Es inmutable y establece el orden cronológico:
 * fecha, hora de inicio y nombre de sede.
 *
 * No comprueba conflictos con otros eventos. Esa validación
 * corresponde al servicio que conoce todos los torneos.
 */
public final class Programacion implements Comparable<Programacion> {

    private final LocalDate fecha;
    private final int horaInicio;
    private final String sede;
    private final String claveSede;

    /**
     * Construye una programación.
     *
     * No se restringen las fechas al presente o al futuro,
     * porque el enunciado no establece esa condición.
     *
     * @param fecha fecha del evento
     * @param horaInicio hora completa entre 0 y 23
     * @param sede nombre de la sede
     */
    public Programacion(
            LocalDate fecha,
            int horaInicio,
            String sede
    ) {
        if (fecha == null) {
            throw new IllegalArgumentException(
                    "La fecha del evento es obligatoria."
            );
        }

        if (horaInicio < 0 || horaInicio > 23) {
            throw new IllegalArgumentException(
                    "La hora de inicio debe estar entre 0 y 23."
            );
        }

        if (sede == null || sede.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "La sede del evento es obligatoria."
            );
        }

        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.sede = sede.trim().replaceAll("\\s+", " ");
        this.claveSede = this.sede.toLowerCase(Locale.ROOT);
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public int getHoraInicio() {
        return horaInicio;
    }

    public String getSede() {
        return sede;
    }

    /**
     * Comprueba si dos programaciones tienen
     * la misma fecha y hora de inicio.
     *
     * La sede no interviene en esta comparación.
     */
    public boolean coincideHorario(Programacion otra) {
        validarOtraProgramacion(otra);

        return fecha.equals(otra.fecha)
                && horaInicio == otra.horaInicio;
    }

    /**
     * Comprueba si dos programaciones ocupan la misma sede
     * en la misma fecha y hora de inicio.
     */
    public boolean coincideSedeYHorario(Programacion otra) {
        return coincideHorario(otra)
                && claveSede.equals(otra.claveSede);
    }

    /**
     * Orden ascendente por fecha, hora y nombre de sede.
     */
    @Override
    public int compareTo(Programacion otra) {
        Objects.requireNonNull(
                otra,
                "La programación comparada no puede ser nula."
        );

        int comparacion = fecha.compareTo(otra.fecha);

        if (comparacion != 0) {
            return comparacion;
        }

        comparacion = Integer.compare(
                horaInicio,
                otra.horaInicio
        );

        if (comparacion != 0) {
            return comparacion;
        }

        return claveSede.compareTo(otra.claveSede);
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (objeto == null || objeto.getClass() != Programacion.class) {
            return false;
        }

        Programacion otra = (Programacion) objeto;

        return fecha.equals(otra.fecha)
                && horaInicio == otra.horaInicio
                && claveSede.equals(otra.claveSede);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fecha, horaInicio, claveSede);
    }

    @Override
    public String toString() {
        return "Programacion{"
                + "fecha=" + fecha
                + ", horaInicio=" + horaInicio
                + ", sede='" + sede + '\''
                + '}';
    }

    private static void validarOtraProgramacion(Programacion otra) {
        if (otra == null) {
            throw new IllegalArgumentException(
                    "La otra programación es obligatoria."
            );
        }
    }
}