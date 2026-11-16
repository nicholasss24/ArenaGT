package arenagt.modelo.eventos;

/**
 * Estados del ciclo de vida de un evento deportivo.
 *
 * Se utilizan tanto en encuentros como en carreras.
 * Las operaciones del evento controlan las transiciones;
 * no se permite cambiar el estado directamente desde el menú.
 */
public enum EstadoEvento {

    SIN_PROGRAMAR("Sin programar"),
    PROGRAMADO("Programado"),
    FINALIZADO("Finalizado");

    private final String descripcion;

    EstadoEvento(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}