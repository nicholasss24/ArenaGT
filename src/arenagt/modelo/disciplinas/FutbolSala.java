package arenagt.modelo.disciplinas;

import arenagt.modelo.Participante;
import arenagt.modelo.resultados.DatosResultado;
import arenagt.modelo.resultados.Puntuacion;
import arenagt.modelo.resultados.ResultadoEvento;

import java.math.BigDecimal;
import java.util.List;

/**
 * Reglas del fútbol sala.
 *
 * Admite entre cuatro y ocho participantes.
 * La victoria otorga tres puntos de tabla,
 * el empate uno y la derrota cero.
 *
 * Puede extenderse para definir variantes que aprovechen
 * las reglas del fútbol sala reglamentario.
 */
public class FutbolSala extends DisciplinaEncuentros {

    protected static final BigDecimal CUOTA_BASE =
            new BigDecimal("600.00");

    /**
     * Construye la disciplina de fútbol sala.
     */
    public FutbolSala() {
        this(
                "FUTSAL",
                "Fútbol sala",
                CUOTA_BASE
        );
    }

    /**
     * Permite que una variante proporcione su código,
     * nombre y cuota conservando los límites del fútbol sala.
     */
    protected FutbolSala(
            String codigo,
            String nombre,
            BigDecimal cuotaInscripcion
    ) {
        super(
                codigo,
                nombre,
                4,
                8,
                cuotaInscripcion
        );
    }

    @Override
    protected String getEtiquetaMarcador() {
        return "Goles";
    }

    /**
     * Valida los goles y calcula las puntuaciones.
     *
     * No modifica las estadísticas de los participantes.
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

        int golesA = datos.obtenerEnteroNoNegativo(MARCADOR_A);
        int golesB = datos.obtenerEnteroNoNegativo(MARCADOR_B);

        long diferenciaA = (long) golesA - golesB;
        long diferenciaB = -diferenciaA;

        Puntuacion puntuacionA;
        Puntuacion puntuacionB;

        if (golesA > golesB) {
            puntuacionA = new Puntuacion(3, diferenciaA, 1);
            puntuacionB = new Puntuacion(0, diferenciaB, 0);
        } else if (golesB > golesA) {
            puntuacionA = new Puntuacion(0, diferenciaA, 0);
            puntuacionB = new Puntuacion(3, diferenciaB, 1);
        } else {
            puntuacionA = new Puntuacion(1, 0L, 0);
            puntuacionB = new Puntuacion(1, 0L, 0);
        }

        return crearResultado(
                participantes,
                datos,
                puntuacionA,
                puntuacionB
        );
    }
}