package arenagt.modelo;

/**
 * Estados del ciclo de vida de un torneo.
 *
 * El torneo controla sus transiciones. No se permite
 * cambiar su estado directamente desde el menú.
 */
public enum EstadoTorneo {

    EN_INSCRIPCION("En inscripción"),
    EN_JUEGO("En juego"),
    FINALIZADO("Finalizado");

    private final String descripcion;

    EstadoTorneo(String descripcion) {
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