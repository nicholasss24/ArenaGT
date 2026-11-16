package arenagt.modelo.disciplinas;

import arenagt.modelo.Participante;
import arenagt.modelo.resultados.DatosResultado;
import arenagt.modelo.resultados.Puntuacion;
import arenagt.modelo.resultados.ResultadoEvento;

import java.math.BigDecimal;
import java.util.List;

/**
 * Reglas del baloncesto.
 *
 * Admite entre cuatro y seis participantes.
 * La inscripción cuesta Q500 por participante.
 *
 * La victoria otorga dos puntos de tabla y la derrota uno.
 * No se permiten marcadores empatados.
 */
public final class Baloncesto extends DisciplinaEncuentros {

    /**
     * Construye la disciplina con sus límites y cuota.
     */
    public Baloncesto() {
        super(
                "BALONCESTO",
                "Baloncesto",
                4,
                6,
                new BigDecimal("500.00")
        );
    }

    @Override
    protected String getEtiquetaMarcador() {
        return "Puntos anotados";
    }

    /**
     * Valida el marcador y calcula las puntuaciones.
     *
     * Los puntos anotados en el encuentro son diferentes
     * de los puntos otorgados para la tabla de posiciones.
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

        int puntosAnotadosA =
                datos.obtenerEnteroNoNegativo(MARCADOR_A);

        int puntosAnotadosB =
                datos.obtenerEnteroNoNegativo(MARCADOR_B);

        if (puntosAnotadosA == puntosAnotadosB) {
            throw new IllegalArgumentException(
                    "Un encuentro de baloncesto no puede "
                            + "terminar empatado."
            );
        }

        long diferenciaA = (long) puntosAnotadosA - puntosAnotadosB;
        long diferenciaB = -diferenciaA;

        Puntuacion puntuacionA;
        Puntuacion puntuacionB;

        if (puntosAnotadosA > puntosAnotadosB) {
            puntuacionA = new Puntuacion(2, diferenciaA, 1);
            puntuacionB = new Puntuacion(1, diferenciaB, 0);
        } else {
            puntuacionA = new Puntuacion(1, diferenciaA, 0);
            puntuacionB = new Puntuacion(2, diferenciaB, 1);
        }

        return crearResultado(
                participantes,
                datos,
                puntuacionA,
                puntuacionB
        );
    }
}