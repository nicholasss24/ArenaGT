package arenagt.modelo;

import java.util.Locale;

/**
 * Representa a un equipo o cuarteto que participa
 * en los torneos de ArenaGT.
 *
 * Es inmutable y su identidad depende del código.
 * Sus estadísticas se almacenan en cada inscripción,
 * porque puede competir en varios torneos.
 */
public final class Participante {

    private final String codigo;
    private final String nombre;
    private final Empresa empresa;

    /**
     * Construye un participante con sus datos obligatorios.
     *
     * El código se normaliza a mayúsculas para evitar
     * diferencias de identidad por capitalización.
     *
     * La unicidad del código en todo el sistema se comprobará
     * en el servicio encargado de registrar participantes.
     *
     * @param codigo código identificador del participante
     * @param nombre nombre del equipo o cuarteto
     * @param empresa empresa a la que representa
     */
    public Participante(
            String codigo,
            String nombre,
            Empresa empresa
    ) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El código del participante es obligatorio."
            );
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre del participante es obligatorio."
            );
        }

        if (empresa == null) {
            throw new IllegalArgumentException(
                    "La empresa del participante es obligatoria."
            );
        }

        this.codigo = codigo.trim().toUpperCase(Locale.ROOT);
        this.nombre = nombre.trim().replaceAll("\\s+", " ");
        this.empresa = empresa;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    /**
     * Dos participantes tienen la misma identidad
     * si sus códigos normalizados son iguales.
     *
     * Esta comprobación no selecciona reglas deportivas.
     */
    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (objeto == null || objeto.getClass() != Participante.class) {
            return false;
        }

        Participante otro = (Participante) objeto;

        return codigo.equals(otro.codigo);
    }

    @Override
    public int hashCode() {
        return codigo.hashCode();
    }

    @Override
    public String toString() {
        return nombre + " [" + codigo + "]";
    }
}