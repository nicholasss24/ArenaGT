package arenagt.modelo.eventos;

import arenagt.modelo.Participante;
import arenagt.modelo.disciplinas.Disciplina;
import arenagt.modelo.resultados.CampoResultado;
import arenagt.modelo.resultados.DatosResultado;
import arenagt.modelo.resultados.ResultadoEvento;

import java.util.List;
import java.util.Optional;

/**
 * Representa una carrera con los participantes inscritos.
 *
 * Implementa Evento sin heredar de Encuentro.
 * Comparte el manejo de sus datos mediante composición
 * con RegistroEvento.
 *
 * La disciplina establece los límites de participantes
 * y las reglas para evaluar tiempos y descalificaciones.
 *
 * Es inmutable.
 */
public final class Carrera implements Evento {

    private final RegistroEvento registro;

    /**
     * Construye una carrera sin programación ni resultado.
     *
     * La lista recibida se copia y se protege dentro
     * de RegistroEvento.
     *
     * @param codigo identificador de la carrera
     * @param disciplina disciplina que evaluará el resultado
     * @param participantes participantes de la carrera
     */
    public Carrera(
            String codigo,
            Disciplina disciplina,
            List<Participante> participantes
    ) {
        this(new RegistroEvento(
                codigo,
                disciplina,
                participantes
        ));

        disciplina.validarCierreInscripciones(
                registro.getParticipantes().size()
        );
    }

    /**
     * Construye una versión a partir de un registro validado.
     */
    private Carrera(RegistroEvento registro) {
        if (registro == null) {
            throw new IllegalArgumentException(
                    "El registro de la carrera es obligatorio."
            );
        }

        this.registro = registro;
    }

    @Override
    public String getCodigo() {
        return registro.getCodigo();
    }

    @Override
    public String getTipo() {
        return "Carrera";
    }

    @Override
    public List<Participante> getParticipantes() {
        return registro.getParticipantes();
    }

    @Override
    public EstadoEvento getEstado() {
        return registro.getEstado();
    }

    @Override
    public Optional<Programacion> getProgramacion() {
        return registro.getProgramacion();
    }

    @Override
    public Optional<ResultadoEvento> getResultado() {
        return registro.getResultado();
    }

    @Override
    public List<CampoResultado> getCamposResultado() {
        return registro.getCamposResultado();
    }

    /**
     * Devuelve una carrera con la nueva programación.
     *
     * No modifica la versión original.
     */
    @Override
    public Carrera programar(Programacion programacion) {
        return new Carrera(
                registro.programar(programacion)
        );
    }

    /**
     * Devuelve una carrera finalizada si todos los datos
     * cumplen las reglas de la disciplina.
     *
     * La validación completa ocurre antes de crear
     * la nueva versión.
     */
    @Override
    public Carrera registrarResultado(DatosResultado datos) {
        return new Carrera(
                registro.registrarResultado(datos)
        );
    }

    @Override
    public String toString() {
        return "Carrera{"
                + "codigo='" + getCodigo() + '\''
                + ", estado=" + getEstado()
                + ", cantidadParticipantes="
                + getParticipantes().size()
                + '}';
    }
}