package arenagt.modelo;

import java.math.BigDecimal;

/**
 * Representa la información de un premio entregado.
 *
 * Conserva el torneo, el campeón y el monto calculado
 * mediante las reglas de la disciplina.
 *
 * Es inmutable. Construir este objeto no modifica
 * el torneo ni registra automáticamente un movimiento.
 */
public final class PremioEntregado {

    private final String codigoTorneo;
    private final Participante campeon;
    private final BigDecimal monto;

    /**
     * Prepara el registro del premio de un torneo finalizado.
     *
     * El registro financiero comprobará que el premio
     * no haya sido entregado anteriormente.
     *
     * @param torneo torneo que acaba de finalizar
     */
    public PremioEntregado(Torneo torneo) {
        if (torneo == null) {
            throw new IllegalArgumentException(
                    "El torneo del premio es obligatorio."
            );
        }

        if (torneo.getEstado() != EstadoTorneo.FINALIZADO) {
            throw new IllegalArgumentException(
                    "Solo puede premiarse un torneo finalizado."
            );
        }

        Participante ganador = torneo.getCampeon().orElseThrow(
                () -> new IllegalArgumentException(
                        "El torneo finalizado debe tener un campeón."
                )
        );

        BigDecimal premioCalculado =
                torneo.getDisciplina().calcularPremio(
                        torneo.getCantidadParticipantes()
                );

        if (premioCalculado == null
                || premioCalculado.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El monto del premio debe ser positivo."
            );
        }

        if (premioCalculado.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException(
                    "El monto del premio admite "
                            + "un máximo de dos decimales."
            );
        }

        this.codigoTorneo = torneo.getCodigo();
        this.campeon = ganador;
        this.monto = premioCalculado.setScale(2);
    }

    public String getCodigoTorneo() {
        return codigoTorneo;
    }

    public Participante getCampeon() {
        return campeon;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    @Override
    public String toString() {
        return "PremioEntregado{"
                + "codigoTorneo='" + codigoTorneo + '\''
                + ", campeon=" + campeon
                + ", monto=" + monto
                + '}';
    }
}