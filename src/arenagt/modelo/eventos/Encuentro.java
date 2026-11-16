package arenagt.modelo.eventos;

import arenagt.modelo.Participante;
import arenagt.modelo.disciplinas.Disciplina;
import arenagt.modelo.resultados.CampoResultado;
import arenagt.modelo.resultados.DatosResultado;
import arenagt.modelo.resultados.ResultadoEvento;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Representa un evento entre dos participantes distintos.
 *
 * Implementa Evento y comparte el manejo de sus datos
 * mediante composición con RegistroEvento.
 *
 * Es inmutable. Programar o registrar un resultado
 * devuelve una nueva versión del encuentro.
 */
public final class Encuentro implements Evento {

    private final RegistroEvento registro;

    /**
     * Construye un encuentro sin programación ni resultado.
     *
     * El orden de los participantes se conserva para
     * interpretar correctamente los campos del marcador.
     *
     * @param codigo identificador del encuentro
     * @param disciplina disciplina que evaluará el resultado
     * @param participanteA primer participante
     * @param participanteB segundo participante
     */
    public Encuentro(
            String codigo,
            Disciplina disciplina,
            Participante participanteA,
            Participante participanteB
    ) {
        this(new RegistroEvento(
                codigo,
                disciplina,
                Arrays.asList(participanteA, participanteB)
        ));
    }

    /**
     * Construye una versión a partir de un registro validado.
     */
    private Encuentro(RegistroEvento registro) {
        if (registro == null
                || registro.getParticipantes().size() != 2) {
            throw new IllegalArgumentException(
                    "Un encuentro requiere exactamente "
                            + "dos participantes."
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
        return "Encuentro";
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
     * Devuelve un encuentro con la nueva programación.
     *
     * El encuentro original permanece sin cambios.
     */
    @Override
    public Encuentro programar(Programacion programacion) {
        return new Encuentro(
                registro.programar(programacion)
        );
    }

    /**
     * Devuelve un encuentro finalizado si el resultado
     * cumple las reglas de la disciplina.
     *
     * El encuentro original permanece sin cambios.
     */
    @Override
    public Encuentro registrarResultado(DatosResultado datos) {
        return new Encuentro(
                registro.registrarResultado(datos)
        );
    }

    @Override
    public String toString() {
        return "Encuentro{"
                + "codigo='" + getCodigo() + '\''
                + ", estado=" + getEstado()
                + '}';
    }
}