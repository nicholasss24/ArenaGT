package arenagt.modelo.eventos;

import arenagt.modelo.Participante;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Representa una consulta inmutable de eventos programados.
 *
 * Incluye encuentros, carreras u otros objetos que cumplan
 * el contrato Evento, sin comprobar su clase concreta.
 *
 * El orden se obtiene mediante Comparable<Evento>.
 * La colección interna no se expone.
 */
public final class Calendario implements Iterable<Evento> {

    private final List<Evento> eventos;

    /**
     * Construye un calendario vacío.
     */
    public Calendario() {
        this(Collections.<Evento>emptyList());
    }

    /**
     * Construye un calendario con los eventos programados
     * que encuentre en el origen.
     *
     * También incluye eventos finalizados, porque conservan
     * su programación y forman parte del historial.
     *
     * @param origen eventos de uno o varios torneos
     */
    public Calendario(Iterable<? extends Evento> origen) {
        if (origen == null) {
            throw new IllegalArgumentException(
                    "El origen de eventos es obligatorio."
            );
        }

        List<Evento> copia = new ArrayList<>();

        for (Evento evento : origen) {
            if (evento == null) {
                throw new IllegalArgumentException(
                        "El calendario no admite eventos nulos."
                );
            }

            if (evento.getProgramacion().isPresent()) {
                copia.add(evento);
            }
        }

        Collections.sort(copia);

        this.eventos = Collections.unmodifiableList(copia);
    }

    public int getCantidadEventos() {
        return eventos.size();
    }

    public boolean isVacio() {
        return eventos.isEmpty();
    }

    /**
     * Devuelve otro calendario con los eventos
     * en los que participa el objeto indicado.
     *
     * No modifica este calendario ni sus eventos.
     *
     * @param participante participante que se desea consultar
     * @return calendario del participante
     */
    public Calendario filtrarPorParticipante(
            Participante participante
    ) {
        if (participante == null) {
            throw new IllegalArgumentException(
                    "El participante es obligatorio."
            );
        }

        List<Evento> seleccionados = new ArrayList<>();

        for (Evento evento : eventos) {
            if (evento.getParticipantes().contains(participante)) {
                seleccionados.add(evento);
            }
        }

        return new Calendario(seleccionados);
    }

    /**
     * Permite recorrer el calendario con un ciclo for-each.
     *
     * El iterador conserva el orden y no permite eliminar
     * elementos de la colección interna.
     */
    @Override
    public Iterator<Evento> iterator() {
        return eventos.iterator();
    }

    @Override
    public String toString() {
        return "Calendario{"
                + "cantidadEventos=" + eventos.size()
                + '}';
    }
}