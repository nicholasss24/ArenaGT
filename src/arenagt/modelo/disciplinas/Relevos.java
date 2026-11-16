package arenagt.modelo.disciplinas;

import arenagt.modelo.Participante;
import arenagt.modelo.eventos.Carrera;
import arenagt.modelo.eventos.Evento;
import arenagt.modelo.resultados.CampoResultado;
import arenagt.modelo.resultados.DatosResultado;
import arenagt.modelo.resultados.Puntuacion;
import arenagt.modelo.resultados.ResultadoEvento;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Reglas de relevos.
 *
 * Admite entre tres y ocho participantes.
 * La inscripción cuesta Q300 por participante.
 *
 * Genera una carrera única y asigna lugares por tiempo,
 * excluyendo de la clasificación a los descalificados.
 */
public final class Relevos extends Disciplina {

    public static final String DESCALIFICADO = "DQ";

    private static final int[] PUNTOS_POR_LUGAR = {
            10, 8, 6, 5, 4, 3, 2, 1
    };

    /**
     * Construye la disciplina con sus límites y cuota.
     */
    public Relevos() {
        super(
                "RELEVOS",
                "Relevos",
                3,
                8,
                new BigDecimal("300.00")
        );
    }

    /**
     * Define una clave de resultado por participante.
     *
     * Se utiliza tanto al describir el formulario
     * como al evaluar los datos recibidos.
     */
    public static String claveResultado(Participante participante) {
        if (participante == null) {
            throw new IllegalArgumentException(
                    "El participante es obligatorio."
            );
        }

        return "resultado_" + participante.getCodigo();
    }

    /**
     * Genera una única carrera con todos los inscritos.
     */
    @Override
    public List<Evento> crearEventos(
            String codigoTorneo,
            List<Participante> participantes
    ) {
        if (codigoTorneo == null || codigoTorneo.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El código del torneo es obligatorio."
            );
        }

        List<Participante> copia =
                copiarYValidarParticipantes(participantes);

        String codigoCarrera = codigoTorneo.trim()
                .toUpperCase(Locale.ROOT) + "-C001";

        return Collections.<Evento>singletonList(
                new Carrera(codigoCarrera, this, copia)
        );
    }

    /**
     * Solicita un tiempo o la marca DQ por cada participante.
     */
    @Override
    public List<CampoResultado> getCamposResultado(
            List<Participante> participantes
    ) {
        List<Participante> copia =
                copiarYValidarParticipantes(participantes);

        List<CampoResultado> campos = new ArrayList<>();

        for (Participante participante : copia) {
            campos.add(new CampoResultado(
                    claveResultado(participante),
                    "Tiempo en segundos de "
                            + participante.getNombre()
                            + " o DQ si fue descalificado",
                    true
            ));
        }

        return Collections.unmodifiableList(campos);
    }

    /**
     * Valida todos los tiempos y las descalificaciones
     * antes de construir el resultado.
     *
     * El primer lugar cuenta como una victoria.
     * La diferencia siempre es cero.
     */
    @Override
    public ResultadoEvento evaluarResultado(
            List<Participante> participantes,
            DatosResultado datos
    ) {
        List<Participante> copia =
                copiarYValidarParticipantes(participantes);

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos del resultado son obligatorios."
            );
        }

        /*
         * TreeMap compara los tiempos numéricamente y los ordena.
         * Por ello, 50.1 y 50.10 se reconocen como el mismo tiempo.
         */
        Map<BigDecimal, Participante> tiempos = new TreeMap<>();

        Map<Participante, Puntuacion> puntuaciones =
                new LinkedHashMap<>();

        for (Participante participante : copia) {
            String clave = claveResultado(participante);
            String texto = datos.obtenerTexto(clave);

            /*
             * Todos comienzan con cero en el resultado preparado.
             * Después se asignan los puntos a quienes tienen tiempo.
             */
            puntuaciones.put(participante, new Puntuacion());

            if (DESCALIFICADO.equalsIgnoreCase(texto)) {
                continue;
            }

            BigDecimal tiempo = datos.obtenerDecimalPositivo(clave);

            if (tiempos.containsKey(tiempo)) {
                throw new IllegalArgumentException(
                        "Los participantes de relevos "
                                + "no pueden tener tiempos iguales."
                );
            }

            tiempos.put(tiempo, participante);
        }

        int posicion = 0;

        for (Participante participante : tiempos.values()) {
            int puntosTabla = PUNTOS_POR_LUGAR[posicion];
            int victorias = posicion == 0 ? 1 : 0;

            puntuaciones.put(
                    participante,
                    new Puntuacion(puntosTabla, 0L, victorias)
            );

            posicion++;
        }

        return new ResultadoEvento(datos, puntuaciones);
    }

    /**
     * Protege la lista y comprueba los límites,
     * los valores nulos y las repeticiones.
     */
    private List<Participante> copiarYValidarParticipantes(
            List<Participante> participantes
    ) {
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
                        "La carrera no admite participantes nulos."
                );
            }

            if (!encontrados.add(participante)) {
                throw new IllegalArgumentException(
                        "Los participantes no pueden repetirse."
                );
            }
        }

        return Collections.unmodifiableList(copia);
    }
}