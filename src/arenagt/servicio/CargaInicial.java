package arenagt.servicio;

import arenagt.modelo.Empresa;
import arenagt.modelo.Participante;
import arenagt.modelo.Torneo;
import arenagt.modelo.disciplinas.Baloncesto;
import arenagt.modelo.disciplinas.Disciplina;
import arenagt.modelo.disciplinas.DisciplinaEncuentros;
import arenagt.modelo.disciplinas.FutbolSala;
import arenagt.modelo.disciplinas.FutbolSalaPenales;
import arenagt.modelo.disciplinas.Relevos;
import arenagt.modelo.disciplinas.Voleibol;
import arenagt.modelo.eventos.Evento;
import arenagt.modelo.eventos.Programacion;
import arenagt.modelo.resultados.DatosResultado;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Prepara los datos de demostración de ArenaGT.
 *
 * Utiliza la fábrica de carga directa de Torneo.
 * No invoca la inscripción ni el registro de resultados
 * del servicio, por lo que no genera ingresos ni premios.
 *
 * Las fechas son fijas para mantener ejemplos reproducibles.
 */
public final class CargaInicial {

    /**
     * Construye todos los datos y solicita su incorporación
     * al servicio vacío.
     */
    public void cargar(ServicioArena servicio) {
        if (servicio == null) {
            throw new IllegalArgumentException(
                    "El servicio es obligatorio."
            );
        }

        FutbolSala futbolSala = new FutbolSala();
        FutbolSalaPenales penales = new FutbolSalaPenales();
        Baloncesto baloncesto = new Baloncesto();
        Voleibol voleibol = new Voleibol();
        Relevos relevos = new Relevos();

        List<Disciplina> disciplinas = Arrays.<Disciplina>asList(
                futbolSala,
                penales,
                baloncesto,
                voleibol,
                relevos
        );

        Empresa tecnologia = new Empresa("TecnoGT");
        Empresa salud = new Empresa("SaludGT");
        Empresa logistica = new Empresa("LogísticaGT");
        Empresa energia = new Empresa("EnergíaGT");

        Participante p1 = new Participante(
                "P01", "Zorros", tecnologia
        );

        Participante p2 = new Participante(
                "P02", "Titanes", tecnologia
        );

        Participante p3 = new Participante(
                "P03", "Halcones", salud
        );

        Participante p4 = new Participante(
                "P04", "Leones", salud
        );

        Participante p5 = new Participante(
                "P05", "Cóndores", logistica
        );

        Participante p6 = new Participante(
                "P06", "Pumas", logistica
        );

        Participante p7 = new Participante(
                "P07", "Jaguares", energia
        );

        Participante p8 = new Participante(
                "P08", "Lobos", energia
        );

        List<Participante> participantes = Arrays.asList(
                p1, p2, p3, p4, p5, p6, p7, p8
        );

        List<Participante> grupoFutbol = Arrays.asList(
                p1, p2, p3, p4
        );

        List<Participante> grupoBaloncesto = Arrays.asList(
                p1, p2, p5, p6
        );

        List<Participante> grupoVoleibol = Arrays.asList(
                p3, p4, p7, p8
        );

        LocalDate fechaBase = LocalDate.of(2026, 11, 16);

        /*
         * Fútbol sala:
         * Zorros y Halcones tienen tres puntos y una victoria.
         * Zorros queda primero por diferencia: +3 frente a +1.
         */
        Torneo copaFutbol = prepararTorneo(
                "FUT-01",
                "Copa empresarial de fútbol sala",
                futbolSala,
                grupoFutbol,
                fechaBase,
                "Cancha Central"
        );

        /*
         * Dos encuentros con participantes distintos
         * comparten fecha y hora en sedes diferentes.
         * Permite observar el desempate del calendario por sede.
         */
        copaFutbol = copaFutbol.programarEvento(
                "FUT-01-E006",
                new Programacion(
                        fechaBase,
                        8,
                        "Cancha Anexa"
                )
        );

        copaFutbol = copaFutbol.registrarResultado(
                "FUT-01-E001",
                marcador(4, 1)
        );

        copaFutbol = copaFutbol.registrarResultado(
                "FUT-01-E006",
                marcador(2, 1)
        );

        /*
         * Ejemplo adicional requerido: empate reglamentario.
         * Titanes y Leones reciben un punto cada uno y diferencia cero.
         * Se conservan los desempates de los dos primeros lugares.
         */
        copaFutbol = copaFutbol.registrarResultado(
                "FUT-01-E005",
                marcador(1, 1)
        );

        /*
         * Fútbol sala con penales:
         * Zorros y Titanes tienen cuatro puntos y diferencia cero.
         * Zorros queda primero por sus dos victorias,
         * frente a una victoria de Titanes.
         */
        Torneo copaPenales = prepararTorneo(
                "PEN-01",
                "Copa empresarial con penales",
                penales,
                grupoFutbol,
                fechaBase.plusDays(2),
                "Cancha de Penales"
        );

        copaPenales = copaPenales.registrarResultado(
                "PEN-01-E001",
                marcadorConPenales(2, 2, 4, 3)
        );

        copaPenales = copaPenales.registrarResultado(
                "PEN-01-E002",
                marcadorConPenales(1, 1, 5, 4)
        );

        copaPenales = copaPenales.registrarResultado(
                "PEN-01-E004",
                marcador(1, 0)
        );

        copaPenales = copaPenales.registrarResultado(
                "PEN-01-E005",
                marcador(0, 1)
        );

        /*
         * Baloncesto:
         * Muestra victorias de ambos lados del marcador.
         * Ganador: dos puntos de tabla. Perdedor: uno.
         */
        Torneo copaBaloncesto = prepararTorneo(
                "BAL-01",
                "Copa empresarial de baloncesto",
                baloncesto,
                grupoBaloncesto,
                fechaBase.plusDays(4),
                "Gimnasio Central"
        );

        copaBaloncesto = copaBaloncesto.registrarResultado(
                "BAL-01-E001",
                marcador(80, 70)
        );

        copaBaloncesto = copaBaloncesto.registrarResultado(
                "BAL-01-E006",
                marcador(60, 75)
        );

        /*
         * Voleibol:
         * Muestra las tres combinaciones válidas:
         * 3-0, 3-1 y 3-2, incluyendo un ganador del lado B.
         */
        Torneo copaVoleibol = prepararTorneo(
                "VOL-01",
                "Copa empresarial de voleibol",
                voleibol,
                grupoVoleibol,
                fechaBase.plusDays(6),
                "Gimnasio de Voleibol"
        );

        copaVoleibol = copaVoleibol.registrarResultado(
                "VOL-01-E001",
                marcador(3, 0)
        );

        copaVoleibol = copaVoleibol.registrarResultado(
                "VOL-01-E002",
                marcador(1, 3)
        );

        copaVoleibol = copaVoleibol.registrarResultado(
                "VOL-01-E003",
                marcador(3, 2)
        );

        /*
         * Primera carrera:
         * Muestra los ocho lugares y sus puntuaciones:
         * 10, 8, 6, 5, 4, 3, 2 y 1.
         */
        Torneo copaRelevos = prepararTorneo(
                "REL-01",
                "Copa empresarial de relevos",
                relevos,
                participantes,
                fechaBase.plusDays(8),
                "Pista Central"
        );

        copaRelevos = copaRelevos.registrarResultado(
                "REL-01-C001",
                resultadoCarrera(
                        participantes,
                        "50.1",
                        "51.9",
                        "52.4",
                        "55.0",
                        "55.7",
                        "56.2",
                        "57.3",
                        "58.4"
                )
        );

        /*
         * Segunda carrera:
         * Los descalificados reciben cero y no ocupan lugar.
         */
        Torneo relevosConDescalificaciones = prepararTorneo(
                "REL-02",
                "Copa de relevos con descalificaciones",
                relevos,
                participantes,
                fechaBase.plusDays(10),
                "Pista del Valle"
        );

        relevosConDescalificaciones =
                relevosConDescalificaciones.registrarResultado(
                        "REL-02-C001",
                        resultadoCarrera(
                                participantes,
                                "DQ",
                                "60.1",
                                "61.2",
                                "DQ",
                                "62.3",
                                "63.4",
                                "64.5",
                                "65.6"
                        )
                );

        /*
         * Torneo abierto para probar inscripciones normales
         * desde el menú, una vez iniciada la aplicación.
         */
        Torneo copaAbierta = new Torneo(
                "ABI-01",
                "Copa abierta de fútbol sala",
                futbolSala
        );

        List<Torneo> torneos = Arrays.asList(
                copaFutbol,
                copaPenales,
                copaBaloncesto,
                copaVoleibol,
                copaRelevos,
                relevosConDescalificaciones,
                copaAbierta
        );

        /*
         * El servicio valida los registros, las referencias
         * y los conflictos antes de confirmar la carga.
         *
         * Los ingresos y premios registrados comienzan en cero,
         * incluso si un torneo inicial ya tiene campeón.
         */
        servicio.cargarDatosIniciales(
                disciplinas,
                participantes,
                torneos
        );
    }

    /**
     * Carga participantes directamente, genera los eventos
     * y les asigna una programación.
     *
     * No utiliza las operaciones financieras del servicio.
     */
    private Torneo prepararTorneo(
            String codigo,
            String nombre,
            Disciplina disciplina,
            List<Participante> participantes,
            LocalDate fechaBase,
            String sede
    ) {
        Torneo torneo = Torneo.conParticipantesIniciales(
                codigo,
                nombre,
                disciplina,
                participantes
        );

        torneo = torneo.cerrarInscripciones();

        List<String> codigos = new ArrayList<>();

        for (Evento evento : torneo.getEventos()) {
            codigos.add(evento.getCodigo());
        }

        for (int i = 0; i < codigos.size(); i++) {
            LocalDate fecha = fechaBase.plusDays(i / 12);
            int hora = 8 + (i % 12);

            torneo = torneo.programarEvento(
                    codigos.get(i),
                    new Programacion(fecha, hora, sede)
            );
        }

        return torneo;
    }

    private DatosResultado marcador(int valorA, int valorB) {
        Map<String, String> valores = new LinkedHashMap<>();

        valores.put(
                DisciplinaEncuentros.MARCADOR_A,
                Integer.toString(valorA)
        );

        valores.put(
                DisciplinaEncuentros.MARCADOR_B,
                Integer.toString(valorB)
        );

        return new DatosResultado(valores);
    }

    private DatosResultado marcadorConPenales(
            int golesA,
            int golesB,
            int penalesA,
            int penalesB
    ) {
        Map<String, String> valores = new LinkedHashMap<>(
                marcador(golesA, golesB).getValores()
        );

        valores.put(
                FutbolSalaPenales.PENALES_A,
                Integer.toString(penalesA)
        );

        valores.put(
                FutbolSalaPenales.PENALES_B,
                Integer.toString(penalesB)
        );

        return new DatosResultado(valores);
    }

    private DatosResultado resultadoCarrera(
            List<Participante> participantes,
            String... resultados
    ) {
        if (resultados.length != participantes.size()) {
            throw new IllegalArgumentException(
                    "Cada participante debe tener un resultado inicial."
            );
        }

        Map<String, String> valores = new LinkedHashMap<>();

        for (int i = 0; i < participantes.size(); i++) {
            valores.put(
                    Relevos.claveResultado(participantes.get(i)),
                    resultados[i]
            );
        }

        return new DatosResultado(valores);
    }
}