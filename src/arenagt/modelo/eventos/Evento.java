package arenagt.modelo.eventos;

import arenagt.modelo.Participante;
import arenagt.modelo.resultados.CampoResultado;
import arenagt.modelo.resultados.DatosResultado;
import arenagt.modelo.resultados.ResultadoEvento;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Contrato común de los eventos deportivos.
 *
 * Los encuentros y las carreras pueden programarse,
 * recibir resultados y aparecer en un mismo calendario.
 *
 * Las implementaciones deben ser inmutables y proteger
 * las colecciones que devuelven.
 */
public interface Evento extends Comparable<Evento> {

    /**
     * Devuelve el código identificador del evento.
     */
    String getCodigo();

    /**
     * Devuelve el nombre de su tipo para presentarlo al usuario.
     *
     * No debe utilizarse para seleccionar reglas deportivas.
     */
    String getTipo();

    /**
     * Devuelve los participantes en su orden de registro.
     *
     * La lista no debe permitir modificaciones externas.
     */
    List<Participante> getParticipantes();

    /**
     * Devuelve el estado actual del evento.
     */
    EstadoEvento getEstado();

    /**
     * Devuelve la programación o un Optional vacío
     * si todavía no se ha asignado.
     */
    Optional<Programacion> getProgramacion();

    /**
     * Devuelve el resultado o un Optional vacío
     * si todavía no se ha registrado.
     */
    Optional<ResultadoEvento> getResultado();

    /**
     * Describe los datos necesarios para capturar un resultado.
     *
     * La vista recorre estos campos sin identificar
     * la disciplina ni el tipo concreto de evento.
     *
     * La lista no debe permitir modificaciones externas.
     */
    List<CampoResultado> getCamposResultado();

    /**
     * Devuelve una nueva versión con la programación solicitada.
     *
     * Permite programar o reprogramar mientras el evento
     * no tenga resultado.
     *
     * El servicio general debe comprobar previamente
     * los conflictos entre todos los torneos.
     *
     * @param programacion nueva fecha, hora y sede
     * @return nueva versión del evento
     * @throws IllegalArgumentException si la programación
     *         es nula o el evento está finalizado
     */
    Evento programar(Programacion programacion);

    /**
     * Valida los datos y devuelve una versión finalizada.
     *
     * Solo se permite si el evento está programado
     * y todavía no tiene resultado.
     *
     * No modifica el evento original, las inscripciones
     * ni los registros financieros.
     *
     * @param datos información ingresada para el resultado
     * @return nueva versión finalizada del evento
     * @throws IllegalArgumentException si el estado
     *         o los datos no permiten registrar el resultado
     */
    Evento registrarResultado(DatosResultado datos);

    /**
     * Orden común utilizado por los calendarios.
     *
     * Los eventos programados se comparan mediante Programacion,
     * que establece el orden por fecha, hora y sede.
     *
     * Los eventos sin programar se colocan después. Si ambos
     * carecen de programación, se ordenan por código.
     *
     * Los calendarios mostrarán únicamente los programados.
     */
    @Override
    default int compareTo(Evento otro) {
        Objects.requireNonNull(
                otro,
                "El evento comparado no puede ser nulo."
        );

        Optional<Programacion> estaProgramacion = getProgramacion();
        Optional<Programacion> otraProgramacion = otro.getProgramacion();

        if (estaProgramacion.isPresent()
                && otraProgramacion.isPresent()) {
            return estaProgramacion.get().compareTo(
                    otraProgramacion.get()
            );
        }

        if (estaProgramacion.isPresent()) {
            return -1;
        }

        if (otraProgramacion.isPresent()) {
            return 1;
        }

        return getCodigo().compareTo(otro.getCodigo());
    }
}