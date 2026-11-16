cat > README.md <<'EOF'
# ArenaGT
Laboratorio 4 — Polimorfismo

Autor: Nicholas Cordón
Carné: 262293

Aplicación de consola para gestionar torneos deportivos empresariales.
Incluye cinco disciplinas, inscripciones, resultados, calendarios,
tablas de posiciones, ranking empresarial y registros financieros.
Los datos se conservan en memoria.

## Requisitos
JDK 17 o superior.

## Compilar
Ejecutar desde la carpeta principal del proyecto:

    mkdir -p bin
    find src pruebas -name '*.java' -print > fuentes.txt
    javac -encoding UTF-8 -d bin @fuentes.txt

## Ejecutar el programa

    java -cp bin arenagt.Main

## Ejecutar las pruebas

    java -cp bin arenagt.pruebas.PruebasDisciplinas
    java -cp bin arenagt.pruebas.PruebasTorneos
    java -cp bin arenagt.pruebas.PruebasCalendariosReportes
    java -cp bin arenagt.pruebas.PruebasConsola

Resultados: 41 grupos aprobados, 469 comprobaciones y 0 grupos fallidos.

## Carpetas
- src: código fuente del programa.
- pruebas: código fuente de las cuatro suites de pruebas automáticas.
- bin: archivos compilados.
EOF