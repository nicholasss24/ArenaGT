package arenagt.modelo.resultados;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Conserva los datos ingresados para registrar un resultado.
 *
 * Es inmutable y no calcula puntos de tabla.
 * Cada disciplina interpreta y valida los campos necesarios.
 *
 * Los campos opcionales pueden contener una cadena vacía.
 * Por ejemplo, los penales no se utilizan cuando hay
 * un ganador en tiempo reglamentario.
 */
public final class DatosResultado {

    private final Map<String, String> valores;

    /**
     * Construye una copia de los datos recibidos.
     *
     * @param valores campos identificados por una clave
     */
    public DatosResultado(Map<String, String> valores) {
        if (valores == null) {
            throw new IllegalArgumentException(
                    "Los datos del resultado son obligatorios."
            );
        }

        Map<String, String> copia = new LinkedHashMap<>();

        for (Map.Entry<String, String> entrada : valores.entrySet()) {
            String clave = entrada.getKey();
            String valor = entrada.getValue();

            validarClave(clave);

            if (valor == null) {
                throw new IllegalArgumentException(
                        "El valor del campo '" + clave
                                + "' no puede ser nulo."
                );
            }

            copia.put(clave, valor.trim());
        }

        this.valores = Collections.unmodifiableMap(copia);
    }

    /**
     * Devuelve los campos sin permitir modificaciones.
     *
     * El mapa original recibido en el constructor tampoco
     * puede modificar los datos conservados.
     */
    public Map<String, String> getValores() {
        return valores;
    }

    /**
     * Indica si existe un campo con contenido.
     *
     * Un campo ausente o vacío se considera sin valor.
     */
    public boolean tieneValor(String clave) {
        validarClave(clave);

        String valor = valores.get(clave);

        return valor != null && !valor.isEmpty();
    }

    /**
     * Obtiene un texto obligatorio.
     *
     * @param clave campo que se desea consultar
     * @return contenido del campo
     */
    public String obtenerTexto(String clave) {
        validarClave(clave);

        String valor = valores.get(clave);

        if (valor == null || valor.isEmpty()) {
            throw new IllegalArgumentException(
                    "El campo '" + clave + "' es obligatorio."
            );
        }

        return valor;
    }

    /**
     * Obtiene un entero no negativo.
     *
     * Permite cero porque es válido en goles, puntos
     * anotados, sets y penales convertidos.
     */
    public int obtenerEnteroNoNegativo(String clave) {
        String texto = obtenerTexto(clave);

        final int valor;

        try {
            valor = Integer.parseInt(texto);
        } catch (NumberFormatException excepcion) {
            throw new IllegalArgumentException(
                    "El campo '" + clave
                            + "' debe ser un entero válido.",
                    excepcion
            );
        }

        if (valor < 0) {
            throw new IllegalArgumentException(
                    "El campo '" + clave
                            + "' no puede ser negativo."
            );
        }

        return valor;
    }

    /**
     * Obtiene un decimal estrictamente positivo.
     *
     * BigDecimal permite comparar tiempos decimales
     * sin aproximaciones binarias.
     *
     * No acepta NaN ni infinito.
     */
    public BigDecimal obtenerDecimalPositivo(String clave) {
        String texto = obtenerTexto(clave);

        final BigDecimal valor;

        try {
            valor = new BigDecimal(texto);
        } catch (NumberFormatException excepcion) {
            throw new IllegalArgumentException(
                    "El campo '" + clave
                            + "' debe ser un decimal válido. "
                            + "Utilice punto para los decimales.",
                    excepcion
            );
        }

        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El campo '" + clave
                            + "' debe ser mayor que cero."
            );
        }

        return valor;
    }

    private static void validarClave(String clave) {
        if (clave == null || clave.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "La clave del campo es obligatoria."
            );
        }
    }

    @Override
    public String toString() {
        return "DatosResultado{"
                + "valores=" + valores
                + '}';
    }
}