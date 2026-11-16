package arenagt.modelo.disciplinas;

import arenagt.modelo.Participante;
import arenagt.modelo.resultados.DatosResultado;
import arenagt.modelo.resultados.Puntuacion;
import arenagt.modelo.resultados.ResultadoEvento;

import java.math.BigDecimal;
import java.util.List;

/**
 * Reglas del voleibol.
 *
 * Admite entre cuatro y seis participantes.
 * La inscripción cuesta Q450 por participante.
 *
 * El ganador debe obtener exactamente tres sets
 * y el perdedor puede obtener cero, uno o dos.
 */
public final class Voleibol extends DisciplinaEncuentros {

    /**
     * Construye la disciplina con sus límites y cuota.
     */
    public Voleibol() {
        super(
                "VOLEIBOL",
                "Voleibol",
                4,
                6,
                new BigDecimal("450.00")
        );
    }

    @Override
    protected String getEtiquetaMarcador() {
        return "Sets ganados";
    }

    /**
     * Valida los sets y calcula las puntuaciones.
     *
     * La diferencia corresponde a sets a favor
     * menos sets en contra.
     */
    @Override
    public ResultadoEvento evaluarResultado(
            List<Participante> participantes,
            DatosResultado datos
    ) {
        validarParticipantesEncuentro(participantes);

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos del resultado son obligatorios."
            );
        }

        int setsA = datos.obtenerEnteroNoNegativo(MARCADOR_A);
        int setsB = datos.obtenerEnteroNoNegativo(MARCADOR_B);

        boolean ganaA = setsA == 3 && setsB < 3;
        boolean ganaB = setsB == 3 && setsA < 3;

        if (!ganaA && !ganaB) {
            throw new IllegalArgumentException(
                    "El ganador debe tener exactamente 3 sets "
                            + "y el perdedor entre 0 y 2."
            );
        }

        int setsPerdedor = ganaA ? setsB : setsA;

        int puntosGanador = setsPerdedor == 2 ? 2 : 3;
        int puntosPerdedor = setsPerdedor == 2 ? 1 : 0;

        long diferenciaA = (long) setsA - setsB;
        long diferenciaB = -diferenciaA;

        Puntuacion puntuacionA;
        Puntuacion puntuacionB;

        if (ganaA) {
            puntuacionA = new Puntuacion(
                    puntosGanador,
                    diferenciaA,
                    1
            );

            puntuacionB = new Puntuacion(
                    puntosPerdedor,
                    diferenciaB,
                    0
            );
        } else {
            puntuacionA = new Puntuacion(
                    puntosPerdedor,
                    diferenciaA,
                    0
            );

            puntuacionB = new Puntuacion(
                    puntosGanador,
                    diferenciaB,
                    1
            );
        }

        return crearResultado(
                participantes,
                datos,
                puntuacionA,
                puntuacionB
        );
    }
}