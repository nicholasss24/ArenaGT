package arenagt.modelo.disciplinas;

import arenagt.modelo.Inscripcion;
import arenagt.modelo.Participante;
import arenagt.modelo.eventos.Evento;
import arenagt.modelo.resultados.CampoResultado;
import arenagt.modelo.resultados.DatosResultado;
import arenagt.modelo.resultados.ResultadoEvento;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

/**
 * Define las reglas de una disciplina deportiva.
 *
 * Centraliza las condiciones comunes de inscripción,
 * las cotizaciones y el cálculo del premio.
 *
 * Las subclases generan sus eventos, describen los campos
 * del resultado y aplican sus reglas deportivas.
 *
 * Ningún método de esta clase registra cobros o premios.
 */
public abstract class Disciplina {

    private final String codigo;
    private final String nombre;
    private final int minimoParticipantes;
    private final int maximoParticipantes;
    private final BigDecimal cuotaInscripcion;

    /**
     * Inicializa los datos comunes de la disciplina.
     *
     * Las subclases proporcionan sus valores mediante super.
     */
    protected Disciplina(
            String codigo,
            String nombre,
            int minimoParticipantes,
            int maximoParticipantes,
            BigDecimal cuotaInscripcion
    ) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El código de la disciplina es obligatorio."
            );
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre de la disciplina es obligatorio."
            );
        }

        if (minimoParticipantes <= 0
                || maximoParticipantes < minimoParticipantes) {
            throw new IllegalArgumentException(
                    "Los límites de participantes no son válidos."
            );
        }

        if (cuotaInscripcion == null
                || cuotaInscripcion.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "La cuota de inscripción debe ser positiva."
            );
        }

        if (cuotaInscripcion.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException(
                    "La cuota admite un máximo de dos decimales."
            );
        }

        this.codigo = codigo.trim().toUpperCase(Locale.ROOT);
        this.nombre = nombre.trim();
        this.minimoParticipantes = minimoParticipantes;
        this.maximoParticipantes = maximoParticipantes;
        this.cuotaInscripcion = cuotaInscripcion.setScale(2);
    }

    public final String getCodigo() {
        return codigo;
    }

    public final String getNombre() {
        return nombre;
    }

    public final int getMinimoParticipantes() {
        return minimoParticipantes;
    }

    public final int getMaximoParticipantes() {
        return maximoParticipantes;
    }

    public final BigDecimal getCuotaInscripcion() {
        return cuotaInscripcion;
    }

    /**
     * Calcula una cotización sin inscribir ni cobrar.
     *
     * La cantidad representa una solicitud general para
     * esta disciplina, no una inscripción en un torneo.
     */
    public BigDecimal cotizarInscripcion(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad que se desea cotizar debe ser positiva."
            );
        }

        return cuotaInscripcion.multiply(
                BigDecimal.valueOf(cantidad)
        );
    }

    /**
     * Valida las condiciones comunes de una inscripción.
     *
     * El torneo verifica por separado que su estado
     * permita inscribir participantes.
     *
     * No modifica las inscripciones recibidas.
     */
    public void validarInscripcion(
            Participante participante,
            Iterable<Inscripcion> inscripciones
    ) {
        if (participante == null || inscripciones == null) {
            throw new IllegalArgumentException(
                    "El participante y las inscripciones son obligatorios."
            );
        }

        int cantidad = 0;
        int cantidadDeEmpresa = 0;

        for (Inscripcion inscripcion : inscripciones) {
            if (inscripcion == null) {
                throw new IllegalArgumentException(
                        "Las inscripciones no pueden contener valores nulos."
                );
            }

            Participante inscrito = inscripcion.getParticipante();

            if (inscrito.equals(participante)) {
                throw new IllegalArgumentException(
                        "El participante ya está inscrito en este torneo."
                );
            }

            if (inscrito.getEmpresa().equals(participante.getEmpresa())) {
                cantidadDeEmpresa++;
            }

            cantidad++;
        }

        if (cantidad >= maximoParticipantes) {
            throw new IllegalArgumentException(
                    "El torneo alcanzó el máximo de participantes."
            );
        }

        if (cantidadDeEmpresa >= 2) {
            throw new IllegalArgumentException(
                    "La empresa ya tiene dos participantes "
                            + "inscritos en este torneo."
            );
        }
    }

    /**
     * Valida la cantidad requerida para cerrar inscripciones.
     */
    public void validarCierreInscripciones(int cantidad) {
        if (cantidad < minimoParticipantes
                || cantidad > maximoParticipantes) {
            throw new IllegalArgumentException(
                    "La disciplina requiere entre "
                            + minimoParticipantes + " y "
                            + maximoParticipantes + " participantes."
            );
        }
    }

    /**
     * Calcula el premio sin registrar su entrega.
     *
     * Corresponde al 50 % de la cuota multiplicada
     * por la cantidad de participantes.
     */
    public BigDecimal calcularPremio(int cantidadParticipantes) {
        validarCierreInscripciones(cantidadParticipantes);

        return cuotaInscripcion
                .multiply(BigDecimal.valueOf(cantidadParticipantes))
                .multiply(new BigDecimal("0.50"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Construye los eventos que corresponden a la disciplina.
     *
     * El código del torneo permite generar códigos de evento
     * identificables en el registro general.
     *
     * No programa eventos ni registra resultados.
     */
    public abstract List<Evento> crearEventos(
            String codigoTorneo,
            List<Participante> participantes
    );

    /**
     * Describe los campos que solicitará la vista para
     * registrar el resultado de un evento.
     *
     * Debe devolver una lista no modificable.
     */
    public abstract List<CampoResultado> getCamposResultado(
            List<Participante> participantes
    );

    /**
     * Valida el resultado completo y calcula sus puntuaciones.
     *
     * No modifica eventos, inscripciones ni registros financieros.
     */
    public abstract ResultadoEvento evaluarResultado(
            List<Participante> participantes,
            DatosResultado datos
    );

    @Override
    public String toString() {
        return nombre;
    }
}