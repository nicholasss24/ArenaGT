package arenagt.vista;

import arenagt.modelo.Inscripcion;
import arenagt.modelo.Participante;
import arenagt.modelo.PosicionEmpresa;
import arenagt.modelo.PremioEntregado;
import arenagt.modelo.RegistroFinanciero;
import arenagt.modelo.Torneo;
import arenagt.modelo.disciplinas.Disciplina;
import arenagt.modelo.eventos.Calendario;
import arenagt.modelo.eventos.Evento;
import arenagt.modelo.eventos.Programacion;
import arenagt.modelo.resultados.CampoResultado;
import arenagt.modelo.resultados.DatosResultado;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

/**
 * Presenta la consola y solicita datos al usuario.
 *
 * No calcula puntuaciones ni confirma operaciones.
 * Captura resultados recorriendo los campos proporcionados
 * por los eventos, sin identificar disciplinas concretas.
 */
public final class VistaConsola {

    private final Scanner entrada;
    private final PrintStream salida;

    /**
     * Construye la vista para la consola del sistema.
     */
    public VistaConsola() {
        this(new Scanner(System.in), System.out);
    }

    /**
     * Permite sustituir entrada y salida durante las pruebas.
     */
    public VistaConsola(Scanner entrada, PrintStream salida) {
        if (entrada == null || salida == null) {
            throw new IllegalArgumentException(
                    "La entrada y la salida son obligatorias."
            );
        }

        this.entrada = entrada;
        this.salida = salida;
    }

    public void mostrarBienvenida() {
        salida.println();
        salida.println("ARENAGT");
        salida.println("Gestión de copas deportivas empresariales");
        salida.println(
                "Datos en memoria. Montos expresados en quetzales."
        );
    }

    public void mostrarMenu() {
        salida.println();
        salida.println("MENÚ PRINCIPAL");

        salida.println("Registros y consultas");
        salida.println(" 1. Consultar disciplinas");
        salida.println(" 2. Registrar participante");
        salida.println(" 3. Consultar participantes");
        salida.println(" 4. Crear torneo");
        salida.println(" 5. Consultar torneos");

        salida.println("Inscripciones y eventos");
        salida.println(" 6. Cotizar inscripciones");
        salida.println(" 7. Inscribir participante");
        salida.println(" 8. Cerrar inscripciones");
        salida.println(" 9. Ver detalle y eventos de un torneo");
        salida.println("10. Programar o reprogramar evento");
        salida.println("11. Registrar resultado");

        salida.println("Reportes");
        salida.println("12. Tabla de posiciones y campeón");
        salida.println("13. Calendario general");
        salida.println("14. Calendario de un torneo");
        salida.println("15. Calendario de un participante");
        salida.println("16. Ingresos y premios entregados");
        salida.println("17. Ranking de empresas");
        salida.println(" 0. Salir");
    }

    /**
     * Devuelve cero si se termina la entrada.
     */
    public int leerOpcion() {
        while (true) {
            salida.print("Seleccione una opción: ");

            if (!entrada.hasNextLine()) {
                return 0;
            }

            String texto = entrada.nextLine().trim();

            try {
                int opcion = Integer.parseInt(texto);

                if (opcion >= 0 && opcion <= 17) {
                    return opcion;
                }
            } catch (NumberFormatException excepcion) {
                // Se solicita nuevamente la opción.
            }

            salida.println(
                    "Ingrese una opción entera entre 0 y 17."
            );
        }
    }

    public String leerTexto(String pregunta) {
        salida.print(pregunta);
        return entrada.nextLine().trim();
    }

    public String leerTextoObligatorio(String pregunta) {
        while (true) {
            String texto = leerTexto(pregunta);

            if (!texto.isEmpty()) {
                return texto;
            }

            salida.println("Este dato es obligatorio.");
        }
    }

    public int leerEnteroEnRango(
            String pregunta,
            int minimo,
            int maximo
    ) {
        if (minimo > maximo) {
            throw new IllegalArgumentException(
                    "El rango de entrada no es válido."
            );
        }

        while (true) {
            String texto = leerTexto(pregunta);

            try {
                int valor = Integer.parseInt(texto);

                if (valor >= minimo && valor <= maximo) {
                    return valor;
                }
            } catch (NumberFormatException excepcion) {
                // Se informa el rango y se vuelve a solicitar.
            }

            salida.println(
                    "Ingrese un entero entre "
                            + minimo + " y " + maximo + "."
            );
        }
    }

    public int leerEnteroPositivo(String pregunta) {
        return leerEnteroEnRango(
                pregunta,
                1,
                Integer.MAX_VALUE
        );
    }

    public LocalDate leerFecha(String pregunta) {
        while (true) {
            String texto = leerTexto(pregunta);

            try {
                return LocalDate.parse(texto);
            } catch (DateTimeParseException excepcion) {
                salida.println(
                        "Ingrese una fecha válida con formato AAAA-MM-DD."
                );
            }
        }
    }

    /**
     * Solicita los campos definidos por la disciplina.
     *
     * No interpreta marcadores, penales ni tiempos.
     * Esa validación corresponde al modelo.
     */
    public DatosResultado leerDatosResultado(
            List<CampoResultado> campos
    ) {
        Map<String, String> valores = new LinkedHashMap<>();

        salida.println(
                "Use punto para decimales. "
                        + "Deje vacíos los campos opcionales que no apliquen."
        );

        for (CampoResultado campo : campos) {
            String pregunta = campo.getEtiqueta()
                    + (campo.isObligatorio()
                        ? ": "
                        : " [opcional]: ");

            String valor = campo.isObligatorio()
                    ? leerTextoObligatorio(pregunta)
                    : leerTexto(pregunta);

            valores.put(campo.getClave(), valor);
        }

        return new DatosResultado(valores);
    }

    public void mostrarDisciplinas(
            Iterable<Disciplina> disciplinas
    ) {
        salida.println();
        salida.println("DISCIPLINAS");

        for (Disciplina disciplina : disciplinas) {
            salida.printf(
                    Locale.ROOT,
                    "%s | %s | Participantes: %d a %d | Cuota: Q%.2f%n",
                    disciplina.getCodigo(),
                    disciplina.getNombre(),
                    disciplina.getMinimoParticipantes(),
                    disciplina.getMaximoParticipantes(),
                    disciplina.getCuotaInscripcion()
            );
        }
    }

    public void mostrarParticipantes(
            Iterable<Participante> participantes
    ) {
        salida.println();
        salida.println("PARTICIPANTES");

        int cantidad = 0;

        for (Participante participante : participantes) {
            salida.println(
                    participante.getCodigo()
                            + " | " + participante.getNombre()
                            + " | " + participante.getEmpresa().getNombre()
            );

            cantidad++;
        }

        salida.println("Total: " + cantidad);
    }

    public void mostrarTorneos(Iterable<Torneo> torneos) {
        salida.println();
        salida.println("TORNEOS");

        int cantidad = 0;

        for (Torneo torneo : torneos) {
            salida.println(
                    torneo.getCodigo() + " | " + torneo.getNombre()
            );

            salida.println(
                    "  Disciplina: " + torneo.getDisciplina().getNombre()
                            + " | Estado: " + torneo.getEstado()
            );

            salida.println(
                    "  Inscritos: " + torneo.getCantidadParticipantes()
                            + " | Resultados: "
                            + torneo.getCantidadResultados()
                            + "/" + torneo.getCantidadEventos()
            );

            cantidad++;
        }

        salida.println("Total: " + cantidad);
    }

    public void mostrarDetalleTorneo(Torneo torneo) {
        salida.println();
        salida.println("DETALLE DEL TORNEO");
        salida.println("Código: " + torneo.getCodigo());
        salida.println("Nombre: " + torneo.getNombre());
        salida.println(
                "Disciplina: " + torneo.getDisciplina().getNombre()
        );
        salida.println("Estado: " + torneo.getEstado());

        salida.println();
        salida.println("Inscritos:");

        for (Inscripcion inscripcion : torneo.getInscripciones()) {
            Participante participante =
                    inscripcion.getParticipante();

            salida.println(
                    participante.getCodigo()
                            + " | " + participante.getNombre()
                            + " | " + participante.getEmpresa().getNombre()
            );
        }

        salida.println(
                "Total de inscritos: "
                        + torneo.getCantidadParticipantes()
        );

        salida.println();
        salida.println("Eventos:");

        /*
         * Los programados se recorren mediante Calendario para
         * reutilizar el mismo orden cronológico en todas las consultas.
         */
        for (Evento evento : torneo.getCalendario()) {
            mostrarEvento(evento);

            if (evento.getResultado().isPresent()) {
                mostrarResultadoEvento(evento);
            }
        }

        /*
         * Los pendientes no tienen una posición cronológica.
         * Se muestran después para permitir su programación.
         */
        for (Evento evento : torneo.getEventos()) {
            if (!evento.getProgramacion().isPresent()) {
                mostrarEvento(evento);
            }
        }

        salida.println(
                "Total de eventos: " + torneo.getCantidadEventos()
        );
    }

    public void mostrarCotizacion(
            Disciplina disciplina,
            int cantidad,
            BigDecimal monto
    ) {
        salida.printf(
                Locale.ROOT,
                "Cotización de %d inscripciones en %s: Q%.2f%n",
                cantidad,
                disciplina.getNombre(),
                monto
        );

        salida.println(
                "La cotización no inscribe participantes ni registra cobros."
        );
    }

    public void mostrarTabla(Torneo torneo) {
        salida.println();
        salida.println(
                "TABLA DE POSICIONES - " + torneo.getCodigo()
        );

        salida.printf(
                Locale.ROOT,
                "%-4s %-10s %-20s %6s %8s %9s%n",
                "Pos.",
                "Código",
                "Participante",
                "Puntos",
                "Dif.",
                "Victorias"
        );

        int posicion = 1;

        for (Inscripcion inscripcion : torneo.getTablaPosiciones()) {
            Participante participante =
                    inscripcion.getParticipante();

            salida.printf(
                    Locale.ROOT,
                    "%-4d %-10s %-20s %6d %8d %9d%n",
                    posicion,
                    participante.getCodigo(),
                    participante.getNombre(),
                    inscripcion.getPuntuacion().getPuntosTabla(),
                    inscripcion.getPuntuacion().getDiferencia(),
                    inscripcion.getPuntuacion().getVictorias()
            );

            posicion++;
        }

        if (torneo.getCampeon().isPresent()) {
            Participante campeon = torneo.getCampeon().get();

            salida.println(
                    "Campeón: " + campeon.getNombre()
                            + " | Empresa: "
                            + campeon.getEmpresa().getNombre()
            );
        } else {
            salida.println(
                    "Campeón pendiente. Estado: " + torneo.getEstado()
            );
        }
    }

    public void mostrarCalendario(Calendario calendario) {
        salida.println();
        salida.println("CALENDARIO");

        for (Evento evento : calendario) {
            mostrarEvento(evento);
        }

        salida.println(
                "Eventos programados: "
                        + calendario.getCantidadEventos()
        );
    }

    public void mostrarFinanzas(
            RegistroFinanciero registro,
            Iterable<Disciplina> disciplinas
    ) {
        salida.println();
        salida.println("INGRESOS POR INSCRIPCIONES");

        for (Disciplina disciplina : disciplinas) {
            salida.printf(
                    Locale.ROOT,
                    "%s: Q%.2f%n",
                    disciplina.getNombre(),
                    registro.getIngresosDeDisciplina(disciplina)
            );
        }

        salida.printf(
                Locale.ROOT,
                "Ingresos totales: Q%.2f%n",
                registro.getIngresosTotales()
        );

        salida.println();
        salida.println("PREMIOS ENTREGADOS");

        for (PremioEntregado premio
                : registro.getPremiosEntregados()) {
            salida.printf(
                    Locale.ROOT,
                    "%s | %s | Q%.2f%n",
                    premio.getCodigoTorneo(),
                    premio.getCampeon().getNombre(),
                    premio.getMonto()
            );
        }

        if (registro.getCantidadPremiosEntregados() == 0) {
            salida.println("No hay premios entregados registrados.");
        }

        salida.printf(
                Locale.ROOT,
                "Total de premios entregados: Q%.2f%n",
                registro.getTotalPremiosEntregados()
        );
    }

    public void mostrarRanking(
            Iterable<PosicionEmpresa> ranking
    ) {
        salida.println();
        salida.println("RANKING DE EMPRESAS");

        int posicion = 1;

        for (PosicionEmpresa fila : ranking) {
            salida.println(
                    posicion + ". "
                            + fila.getEmpresa().getNombre()
                            + " | Puntos: " + fila.getPuntosTabla()
            );

            posicion++;
        }

        if (posicion == 1) {
            salida.println("No hay empresas para mostrar.");
        }
    }

    public void mostrarMensaje(String mensaje) {
        salida.println(mensaje);
    }

    public void mostrarDespedida() {
        salida.println("Aplicación finalizada.");
    }

    private void mostrarEvento(Evento evento) {
        salida.println();
        salida.println(
                evento.getCodigo()
                        + " | " + evento.getTipo()
                        + " | " + evento.getEstado()
        );

        if (evento.getProgramacion().isPresent()) {
            Programacion programacion =
                    evento.getProgramacion().get();

            salida.printf(
                    Locale.ROOT,
                    "  %s | %02d:00 | %s%n",
                    programacion.getFecha(),
                    programacion.getHoraInicio(),
                    programacion.getSede()
            );
        } else {
            salida.println("  Sin fecha, hora ni sede asignadas.");
        }

        StringBuilder nombres = new StringBuilder();

        for (Participante participante : evento.getParticipantes()) {
            if (nombres.length() > 0) {
                nombres.append(" / ");
            }

            nombres.append(participante.getNombre());
        }

        salida.println("  Participantes: " + nombres);
    }

    /**
     * Presenta los resultados mediante sus campos descriptivos,
     * sin mostrar claves internas ni identificar el deporte.
     */
    private void mostrarResultadoEvento(Evento evento) {
        DatosResultado datos = evento.getResultado().get().getDatos();

        salida.println("  Resultado:");

        for (CampoResultado campo : evento.getCamposResultado()) {
            if (datos.tieneValor(campo.getClave())) {
                salida.println(
                        "    " + campo.getEtiqueta()
                                + ": "
                                + datos.obtenerTexto(campo.getClave())
                );
            }
        }
    }
}