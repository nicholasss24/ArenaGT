package arenagt.modelo;

import java.util.Locale;

/**
 * Representa una empresa cuyos participantes pueden
 * competir en uno o varios torneos.
 *
 * Es inmutable para conservar su identidad durante
 * las inscripciones y el cálculo de reportes.
 */
public final class Empresa {

    private final String nombre;
    private final String nombreNormalizado;

    /**
     * Construye una empresa con un nombre obligatorio.
     *
     * Los espacios sobrantes se normalizan y la identidad
     * se compara sin distinguir mayúsculas y minúsculas.
     *
     * @param nombre nombre de la empresa
     * @throws IllegalArgumentException si el nombre es nulo
     *         o está vacío
     */
    public Empresa(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre de la empresa es obligatorio."
            );
        }

        this.nombre = nombre.trim().replaceAll("\\s+", " ");
        this.nombreNormalizado =
                this.nombre.toLowerCase(Locale.ROOT);
    }

    public String getNombre() {
        return nombre;
    }

    /**
     * Compara la identidad de dos empresas.
     *
     * La comprobación de clase pertenece al contrato
     * de igualdad; no selecciona reglas deportivas.
     */
    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (objeto == null || objeto.getClass() != Empresa.class) {
            return false;
        }

        Empresa otra = (Empresa) objeto;

        return nombreNormalizado.equals(otra.nombreNormalizado);
    }

    @Override
    public int hashCode() {
        return nombreNormalizado.hashCode();
    }

    @Override
    public String toString() {
        return nombre;
    }
}