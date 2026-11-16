package arenagt.modelo.disciplinas;

import arenagt.modelo.Participante;
import arenagt.modelo.eventos.Encuentro;
import arenagt.modelo.eventos.Evento;
import arenagt.modelo.resultados.CampoResultado;
import arenagt.modelo.resultados.DatosResultado;
import arenagt.modelo.resultados.Puntuacion;
import arenagt.modelo.resultados.ResultadoEvento;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Base de las disciplinas que utilizan encuentros
 * de todos contra todos, a una sola vuelta.
 *
 * Comparte la generación de parejas y la descripción
 * básica de los campos del marcador.
 *
 * Las subclases conservan la responsabilidad de validar
 * sus resultados y calcular los puntos de tabla.
 */
public abstract class DisciplinaEncuentros extends Disciplina {

    public static final String MARCADOR_A = "marcadorA";
    public static final String MARCADOR_B = "marcadorB";

    /**
     * Inicializa las características de la disciplina
     * mediante el constructor de la clase base.
     */
    protected DisciplinaEncuentros(
            String codigo,
            String nombre,
            int minimoParticipantes,
            int maximoParticipantes,
            BigDecimal cuotaInscripcion
    ) {
        super(
                codigo,
                nombre,
                minimoParticipantes,
                maximoParticipantes,
                cuotaInscripcion
        );
    }

    /**
     * Genera una sola vez cada pareja de participantes.
     *
     * Los eventos se crean sin programación ni resultado.
     * La lista devuelta no permite modificaciones.
     */
    @Override
    public final List<Evento> crearEventos(
            String codigoTorneo,
            List<Participante> participantes
    ) {
        if (codigoTorneo == null || codigoTorneo.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El código del torneo es obligatorio."
            );
        }

        if (participantes == null) {
            throw new IllegalArgumentException(
                    "Los participantes son obligatorios."
            );
        }

        List<Participante> copia = new ArrayList<>(participantes);

        validarCierreInscripciones(copia.size());

        Set<Participante> encontrados = new HashSet<>();

        for (Participante participante : copia) {
            if (participante == null) {
                throw new IllegalArgumentException(
                        "No se admiten participantes nulos."
                );
            }

            if (!encontrados.add(participante)) {
                throw new IllegalArgumentException(
                        "Los participantes no pueden repetirse."
                );
            }
        }

        String prefijo = codigoTorneo.trim()
                .toUpperCase(Locale.ROOT);

        List<Evento> eventos = new ArrayList<>();
        int numero = 1;

        for (int i = 0; i < copia.size(); i++) {
            for (int j = i + 1; j < copia.size(); j++) {
                String codigoEvento = prefijo
                        + "-E"
                        + String.format(Locale.ROOT, "%03d", numero);

                eventos.add(new Encuentro(
                        codigoEvento,
                        this,
                        copia.get(i),
                        copia.get(j)
                ));

                numero++;
            }
        }

        return Collections.unmodifiableList(eventos);
    }

    /**
     * Cada disciplina indica qué mide su marcador.
     *
     * Por ejemplo: goles, puntos anotados o sets ganados.
     */
    protected abstract String getEtiquetaMarcador();

    /**
     * Describe los dos campos básicos del marcador.
     *
     * Una subclase puede ampliar estos campos, como ocurre
     * con los penales del fútbol sala con penales.
     */
    @Override
    public List<CampoResultado> getCamposResultado(
            List<Participante> participantes
    ) {
        validarParticipantesEncuentro(participantes);

        List<CampoResultado> campos = Arrays.asList(
                new CampoResultado(
                        MARCADOR_A,
                        getEtiquetaMarcador() + " de "
                                + participantes.get(0).getNombre(),
                        true
                ),
                new CampoResultado(
                        MARCADOR_B,
                        getEtiquetaMarcador() + " de "
                                + participantes.get(1).getNombre(),
                        true
                )
        );

        return Collections.unmodifiableList(campos);
    }

    /**
     * Comprueba la estructura común de un encuentro.
     */
    protected final void validarParticipantesEncuentro(
            List<Participante> participantes
    ) {
        if (participantes == null || participantes.size() != 2) {
            throw new IllegalArgumentException(
                    "Un encuentro requiere exactamente "
                            + "dos participantes."
            );
        }

        Participante primero = participantes.get(0);
        Participante segundo = participantes.get(1);

        if (primero == null || segundo == null) {
            throw new IllegalArgumentException(
                    "Los participantes del encuentro son obligatorios."
            );
        }

        if (primero.equals(segundo)) {
            throw new IllegalArgumentException(
                    "Un participante no puede competir contra sí mismo."
            );
        }
    }

    /**
     * Reúne las puntuaciones calculadas por la disciplina.
     *
     * No decide los puntos, la diferencia ni las victorias.
     * Tampoco modifica las inscripciones.
     */
    protected final ResultadoEvento crearResultado(
            List<Participante> participantes,
            DatosResultado datos,
            Puntuacion puntuacionA,
            Puntuacion puntuacionB
    ) {
        validarParticipantesEncuentro(participantes);

        Map<Participante, Puntuacion> puntuaciones =
                new LinkedHashMap<>();

        puntuaciones.put(participantes.get(0), puntuacionA);
        puntuaciones.put(participantes.get(1), puntuacionB);

        return new ResultadoEvento(datos, puntuaciones);
    }
}