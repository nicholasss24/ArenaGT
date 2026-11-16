package arenagt;

import arenagt.controlador.ControladorArena;
import arenagt.servicio.CargaInicial;
import arenagt.servicio.ServicioArena;
import arenagt.vista.VistaConsola;

/**
 * Punto de entrada de ArenaGT.
 *
 * Construye los componentes y entrega el control
 * al controlador.
 *
 * No contiene reglas deportivas, carga de datos
 * ni el ciclo del menú.
 */
public final class Main {

    public static void main(String[] args) {
        ServicioArena servicio = new ServicioArena();

        VistaConsola vista = new VistaConsola();

        CargaInicial cargaInicial = new CargaInicial();

        ControladorArena controlador = new ControladorArena(
                servicio,
                vista,
                cargaInicial
        );

        controlador.iniciar();
    }
}