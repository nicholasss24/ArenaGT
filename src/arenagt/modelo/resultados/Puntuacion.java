package arenagt.modelo.resultados;

import java.util.Objects;

/**
 * Representa estadísticas utilizadas en la tabla de posiciones.
 *
 * Los puntos de tabla son diferentes de los goles, puntos
 * anotados o sets registrados dentro de un encuentro.
 *
 * Es inmutable: las operaciones devuelven nuevos objetos.
 */
public final class Puntuacion {

    private final int puntosTabla;
    private final long diferencia;
    private final int victorias;

    /**
     * Construye una puntuación inicial sin resultados.
     */
    public Puntuacion() {
        this(0, 0L, 0);
    }

    /**
     * Construye una puntuación.
     *
     * La diferencia puede ser positiva, negativa o cero.
     * Las reglas de cada disciplina determinan los valores
     * que corresponden a un resultado deportivo.
     *
     * @param puntosTabla puntos otorgados para la tabla
     * @param diferencia diferencia deportiva
     * @param victorias cantidad de victorias
     */
    public Puntuacion(
            int puntosTabla,
            long diferencia,
            int victorias
    ) {
        if (puntosTabla < 0) {
            throw new IllegalArgumentException(
                    "Los puntos de tabla no pueden ser negativos."
            );
        }

        if (victorias < 0) {
            throw new IllegalArgumentException(
                    "La cantidad de victorias no puede ser negativa."
            );
        }

        this.puntosTabla = puntosTabla;
        this.diferencia = diferencia;
        this.victorias = victorias;
    }

    public int getPuntosTabla() {
        return puntosTabla;
    }

    public long getDiferencia() {
        return diferencia;
    }

    public int getVictorias() {
        return victorias;
    }

    /**
     * Devuelve la suma sin modificar ninguno de los operandos.
     *
     * Comprueba los límites numéricos para impedir que
     * un desbordamiento produzca estadísticas incorrectas.
     *
     * @param otra puntuación que se desea sumar
     * @return nueva puntuación acumulada
     */
    public Puntuacion sumar(Puntuacion otra) {
        if (otra == null) {
            throw new IllegalArgumentException(
                    "La puntuación que se desea sumar es obligatoria."
            );
        }

        try {
            int nuevosPuntos = Math.addExact(
                    puntosTabla,
                    otra.puntosTabla
            );

            long nuevaDiferencia = Math.addExact(
                    diferencia,
                    otra.diferencia
            );

            int nuevasVictorias = Math.addExact(
                    victorias,
                    otra.victorias
            );

            return new Puntuacion(
                    nuevosPuntos,
                    nuevaDiferencia,
                    nuevasVictorias
            );
        } catch (ArithmeticException excepcion) {
            throw new IllegalArgumentException(
                    "La suma supera los límites numéricos permitidos.",
                    excepcion
            );
        }
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (objeto == null || objeto.getClass() != Puntuacion.class) {
            return false;
        }

        Puntuacion otra = (Puntuacion) objeto;

        return puntosTabla == otra.puntosTabla
                && diferencia == otra.diferencia
                && victorias == otra.victorias;
    }

    @Override
    public int hashCode() {
        return Objects.hash(puntosTabla, diferencia, victorias);
    }

    @Override
    public String toString() {
        return "Puntuacion{"
                + "puntosTabla=" + puntosTabla
                + ", diferencia=" + diferencia
                + ", victorias=" + victorias
                + '}';
    }
}