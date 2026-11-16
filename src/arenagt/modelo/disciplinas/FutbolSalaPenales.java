package arenagt.modelo.disciplinas;

import arenagt.modelo.Participante;
import arenagt.modelo.resultados.CampoResultado;
import arenagt.modelo.resultados.DatosResultado;
import arenagt.modelo.resultados.Puntuacion;
import arenagt.modelo.resultados.ResultadoEvento;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Variante del fútbol sala que resuelve los empates
 * mediante penales.
 *
 * Conserva los límites de participantes del fútbol sala.
 * La cuota agrega Q150 a la cuota base.
 *
 * Los penales no se incluyen en la diferencia.
 */
public final class FutbolSalaPenales extends FutbolSala {

    public static final String PENALES_A = "penalesA";
    public static final String PENALES_B = "penalesB";

    private static final BigDecimal RECARGO =
            new BigDecimal("150.00");

    /**
     * Construye la variante con cuota de Q750.
     *
     * Reutiliza la cuota base y los límites del fútbol sala.
     */
    public FutbolSalaPenales() {
        super(
                "FUTSAL-PEN",
                "Fútbol sala con penales",
                CUOTA_BASE.add(RECARGO)
        );
    }

    /**
     * Amplía los campos heredados con los penales.
     *
     * Su obligatoriedad depende del marcador reglamentario,
     * por lo que se comprueba al evaluar el resultado.
     */
    @Override
    public List<CampoResultado> getCamposResultado(
            List<Participante> participantes
    ) {
        List<CampoResultado> campos = new ArrayList<>(
                super.getCamposResultado(participantes)
        );

        campos.add(new CampoResultado(
                PENALES_A,
                "Penales convertidos de "
                        + participantes.get(0).getNombre()
                        + " (solo si empataron en goles)",
                false
        ));

        campos.add(new CampoResultado(
                PENALES_B,
                "Penales convertidos de "
                        + participantes.get(1).getNombre()
                        + " (solo si empataron en goles)",
                false
        ));

        return Collections.unmodifiableList(campos);
    }

    /**
     * Aplica las reglas reglamentarias o de penales.
     *
     * Toda la validación ocurre antes de construir el resultado.
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

        boolean tienePenalesA = datos.tieneValor(PENALES_A);
        boolean tienePenalesB = datos.tieneValor(PENALES_B);

        if (golesA != golesB) {
            if (tienePenalesA || tienePenalesB) {
                throw new IllegalArgumentException(
                        "No deben registrarse penales cuando "
                                + "hay ganador en tiempo reglamentario."
                );
            }

            return super.evaluarResultado(participantes, datos);
        }

        if (!tienePenalesA || !tienePenalesB) {
            throw new IllegalArgumentException(
                    "El empate en goles requiere los penales "
                            + "convertidos por ambos participantes."
            );
        }

        int penalesA = datos.obtenerEnteroNoNegativo(PENALES_A);
        int penalesB = datos.obtenerEnteroNoNegativo(PENALES_B);

        if (penalesA == penalesB) {
            throw new IllegalArgumentException(
                    "Los penales no pueden terminar empatados."
            );
        }

        Puntuacion puntuacionA;
        Puntuacion puntuacionB;

        if (penalesA > penalesB) {
            puntuacionA = new Puntuacion(2, 0L, 1);
            puntuacionB = new Puntuacion(1, 0L, 0);
        } else {
            puntuacionA = new Puntuacion(1, 0L, 0);
            puntuacionB = new Puntuacion(2, 0L, 1);
        }

        return crearResultado(
                participantes,
                datos,
                puntuacionA,
                puntuacionB
        );
    }
}