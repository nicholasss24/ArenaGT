package arenagt.modelo;

import java.util.Objects;

/**
 * Representa una fila del ranking de empresas.
 *
 * Conserva una empresa y la suma de sus puntos de tabla
 * en todos los torneos consultados.
 *
 * Es una consulta inmutable. No modifica la empresa,
 * las inscripciones ni los resultados deportivos.
 */
public final class PosicionEmpresa
        implements Comparable<PosicionEmpresa> {

    private final Empresa empresa;
    private final long puntosTabla;

    /**
     * Construye una posición con el total calculado.
     *
     * Se utiliza long porque los puntos pueden acumularse
     * entre muchos participantes y torneos.
     *
     * @param empresa empresa representada
     * @param puntosTabla total de puntos de tabla
     */
    public PosicionEmpresa(
            Empresa empresa,
            long puntosTabla
    ) {
        if (empresa == null) {
            throw new IllegalArgumentException(
                    "La empresa del ranking es obligatoria."
            );
        }

        if (puntosTabla < 0) {
            throw new IllegalArgumentException(
                    "Los puntos del ranking no pueden ser negativos."
            );
        }

        this.empresa = empresa;
        this.puntosTabla = puntosTabla;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public long getPuntosTabla() {
        return puntosTabla;
    }

    /**
     * Orden descendente por puntos y después alfabético
     * por nombre, sin distinguir mayúsculas y minúsculas.
     */
    @Override
    public int compareTo(PosicionEmpresa otra) {
        Objects.requireNonNull(
                otra,
                "La posición comparada no puede ser nula."
        );

        int comparacion = Long.compare(
                otra.puntosTabla,
                this.puntosTabla
        );

        if (comparacion != 0) {
            return comparacion;
        }

        return empresa.getNombre().compareToIgnoreCase(
                otra.empresa.getNombre()
        );
    }

    @Override
    public String toString() {
        return "PosicionEmpresa{"
                + "empresa=" + empresa
                + ", puntosTabla=" + puntosTabla
                + '}';
    }
}