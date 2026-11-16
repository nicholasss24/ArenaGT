package arenagt.modelo;

import arenagt.modelo.disciplinas.Disciplina;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Conserva los ingresos por inscripciones y los premios.
 *
 * Es inmutable: registrar un movimiento devuelve
 * una nueva versión sin modificar el registro original.
 *
 * Los totales se calculan a partir de los movimientos
 * conservados, evitando almacenar totales duplicados.
 */
public final class RegistroFinanciero {

    private static final BigDecimal CERO =
            new BigDecimal("0.00");

    private final Map<String, BigDecimal> ingresosPorDisciplina;
    private final Map<String, PremioEntregado> premiosPorTorneo;

    /**
     * Construye un registro sin ingresos ni premios.
     */
    public RegistroFinanciero() {
        this(
                Collections.<String, BigDecimal>emptyMap(),
                Collections.<String, PremioEntregado>emptyMap()
        );
    }

    /**
     * Construye una versión interna con copias protegidas.
     */
    private RegistroFinanciero(
            Map<String, BigDecimal> ingresosPorDisciplina,
            Map<String, PremioEntregado> premiosPorTorneo
    ) {
        this.ingresosPorDisciplina =
                Collections.unmodifiableMap(
                        new LinkedHashMap<>(ingresosPorDisciplina)
                );

        this.premiosPorTorneo =
                Collections.unmodifiableMap(
                        new LinkedHashMap<>(premiosPorTorneo)
                );
    }

    /**
     * Prepara el ingreso de una inscripción.
     *
     * El servicio debe utilizar este método después de
     * preparar una inscripción válida en el torneo.
     *
     * Cotizar o cargar los datos iniciales no debe
     * invocar esta operación.
     */
    public RegistroFinanciero registrarIngresoInscripcion(
            Disciplina disciplina
    ) {
        validarDisciplina(disciplina);

        BigDecimal ingresoActual =
                getIngresosDeDisciplina(disciplina);

        BigDecimal nuevoIngreso = ingresoActual.add(
                disciplina.getCuotaInscripcion()
        );

        Map<String, BigDecimal> nuevosIngresos =
                new LinkedHashMap<>(ingresosPorDisciplina);

        nuevosIngresos.put(
                disciplina.getCodigo(),
                nuevoIngreso
        );

        return new RegistroFinanciero(
                nuevosIngresos,
                premiosPorTorneo
        );
    }

    /**
     * Prepara el registro del premio de un torneo finalizado.
     *
     * Cada torneo puede registrar un solo premio.
     * La construcción de PremioEntregado valida el estado,
     * el campeón y el monto.
     */
    public RegistroFinanciero registrarPremio(Torneo torneo) {
        if (torneo == null) {
            throw new IllegalArgumentException(
                    "El torneo del premio es obligatorio."
            );
        }

        if (premiosPorTorneo.containsKey(torneo.getCodigo())) {
            throw new IllegalArgumentException(
                    "El premio de este torneo ya fue registrado."
            );
        }

        PremioEntregado premio = new PremioEntregado(torneo);

        Map<String, PremioEntregado> nuevosPremios =
                new LinkedHashMap<>(premiosPorTorneo);

        nuevosPremios.put(
                torneo.getCodigo(),
                premio
        );

        return new RegistroFinanciero(
                ingresosPorDisciplina,
                nuevosPremios
        );
    }

    /**
     * Consulta los ingresos de una disciplina.
     *
     * Devuelve cero si todavía no tiene inscripciones cobradas.
     * No modifica el registro al realizar la consulta.
     */
    public BigDecimal getIngresosDeDisciplina(
            Disciplina disciplina
    ) {
        validarDisciplina(disciplina);

        BigDecimal ingreso = ingresosPorDisciplina.get(
                disciplina.getCodigo()
        );

        return ingreso == null ? CERO : ingreso;
    }

    /**
     * Calcula el total de ingresos por inscripciones.
     */
    public BigDecimal getIngresosTotales() {
        BigDecimal total = CERO;

        for (BigDecimal ingreso : ingresosPorDisciplina.values()) {
            total = total.add(ingreso);
        }

        return total;
    }

    /**
     * Calcula el total de premios registrados.
     */
    public BigDecimal getTotalPremiosEntregados() {
        BigDecimal total = CERO;

        for (PremioEntregado premio : premiosPorTorneo.values()) {
            total = total.add(premio.getMonto());
        }

        return total;
    }

    public int getCantidadPremiosEntregados() {
        return premiosPorTorneo.size();
    }

    /**
     * Permite recorrer los premios sin modificar
     * la colección interna.
     */
    public Iterable<PremioEntregado> getPremiosEntregados() {
        return premiosPorTorneo.values();
    }

    private static void validarDisciplina(Disciplina disciplina) {
        if (disciplina == null) {
            throw new IllegalArgumentException(
                    "La disciplina es obligatoria."
            );
        }
    }

    @Override
    public String toString() {
        return "RegistroFinanciero{"
                + "ingresosTotales=" + getIngresosTotales()
                + ", premiosEntregados="
                + getCantidadPremiosEntregados()
                + '}';
    }
}