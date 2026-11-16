package arenagt.modelo.resultados;

/**
 * Describe un campo que se solicita para registrar
 * el resultado de un evento.
 *
 * La disciplina proporciona los campos necesarios.
 * La vista utiliza esta información para solicitar datos
 * sin identificar la clase concreta de la disciplina.
 *
 * Es inmutable y no almacena el valor ingresado.
 */
public final class CampoResultado {

    private final String clave;
    private final String etiqueta;
    private final boolean obligatorio;

    /**
     * Construye la descripción de un campo.
     *
     * @param clave identificador utilizado en DatosResultado
     * @param etiqueta descripción que mostrará la vista
     * @param obligatorio indica si debe ingresarse contenido
     */
    public CampoResultado(
            String clave,
            String etiqueta,
            boolean obligatorio
    ) {
        if (clave == null || clave.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "La clave del campo es obligatoria."
            );
        }

        if (etiqueta == null || etiqueta.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "La etiqueta del campo es obligatoria."
            );
        }

        this.clave = clave.trim();
        this.etiqueta = etiqueta.trim();
        this.obligatorio = obligatorio;
    }

    public String getClave() {
        return clave;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public boolean isObligatorio() {
        return obligatorio;
    }

    @Override
    public String toString() {
        return "CampoResultado{"
                + "clave='" + clave + '\''
                + ", etiqueta='" + etiqueta + '\''
                + ", obligatorio=" + obligatorio
                + '}';
    }
}