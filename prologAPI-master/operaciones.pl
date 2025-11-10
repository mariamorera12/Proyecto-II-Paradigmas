% ===============================================
% Sistema de Planificación de Tareas Diarias
% ===============================================

:- dynamic tarea/6.
:- dynamic completada/1.
:- dynamic clima_actual/1.
:- dynamic tiempo_disponible/1.

% ===============================================
% Predicado principal de planificación
% ===============================================
planificar_tareas(TiempoDisponible, ClimaActual, TareasResultado) :-
    retractall(tiempo_disponible(_)),
    retractall(clima_actual(_)),
    assert(tiempo_disponible(TiempoDisponible)),
    assert(clima_actual(ClimaActual)),

    findall(Nombre, tarea(Nombre, _, _, _, _, _), TodasTareas),

    % Verificar si hay tiempo suficiente
    (puede_completar_todas(TodasTareas, TiempoDisponible) ->
        % Filtrar tareas por clima
        filtrar_por_clima(TodasTareas, ClimaActual, TareasPorClima),

        % Ordenar respetando dependencias y prioridades
        ordenar_tareas(TareasPorClima, TareasOrdenadas),

        % Calcular horas de inicio
        calcular_horas_inicio(TareasOrdenadas, 8, 0, TareasResultado)
    ;
        TareasResultado = []
    ).

% ===============================================
% Regla 1: Verificar dependencias
% ===============================================
puede_realizar(Tarea) :-
    tarea(Tarea, _, _, _, _, Dependencia),
    (Dependencia = ninguna ; completada(Dependencia)).

% ===============================================
% Regla 2: Filtrar tareas por clima
% ===============================================
filtrar_por_clima([], _, []).
filtrar_por_clima([Tarea|Resto], ClimaActual, [Tarea|Resultado]) :-
    tarea_permitida_por_clima(Tarea, ClimaActual),
    !,
    filtrar_por_clima(Resto, ClimaActual, Resultado).
filtrar_por_clima([_|Resto], ClimaActual, Resultado) :-
    filtrar_por_clima(Resto, ClimaActual, Resultado).

tarea_permitida_por_clima(Tarea, ClimaActual) :-
    tarea(Tarea, _, _, _, ClimaRequerido, _),
    (ClimaRequerido = cualquiera ; ClimaRequerido = ClimaActual).

% ===============================================
% Regla 3: Verificar restricciones de tiempo
% ===============================================
puede_completar_todas(Tareas, TiempoDisponible) :-
    suma_tiempos(Tareas, TotalMinutos),
    TiempoDisponibleMinutos is TiempoDisponible * 60,
    TotalMinutos =< TiempoDisponibleMinutos.

suma_tiempos([], 0).
suma_tiempos([Tarea|Resto], Total) :-
    tarea(Tarea, _, Tiempo, _, _, _),
    suma_tiempos(Resto, SubTotal),
    Total is SubTotal + Tiempo.

% ===============================================
% Regla 4: Ordenar tareas (dependencias + prioridad)
% ===============================================
ordenar_tareas(Tareas, TareasOrdenadas) :-
    ordenar_por_dependencias(Tareas, [], TareasConDep),
    ordenar_por_prioridad(TareasConDep, TareasOrdenadas).

% Ordenar respetando dependencias
ordenar_por_dependencias([], Acumulado, Acumulado).
ordenar_por_dependencias(Tareas, Acumulado, Resultado) :-
    findall(T, (member(T, Tareas), puede_realizar(T)), TareasSinDep),
    TareasSinDep \= [],
    append(Acumulado, TareasSinDep, NuevoAcumulado),
    subtract(Tareas, TareasSinDep, TareasRestantes),
    marcar_completadas(TareasSinDep),
    ordenar_por_dependencias(TareasRestantes, NuevoAcumulado, Resultado).

marcar_completadas([]).
marcar_completadas([Tarea|Resto]) :-
    assertz(completada(Tarea)),
    marcar_completadas(Resto).

% Ordenar por prioridad (peso)
ordenar_por_prioridad(Tareas, TareasOrdenadas) :-
    agregar_pesos(Tareas, TareasConPeso),
    sort(1, @>=, TareasConPeso, TareasOrdenadasConPeso),
    extraer_nombres(TareasOrdenadasConPeso, TareasOrdenadas).

agregar_pesos([], []).
agregar_pesos([Tarea|Resto], [Peso-Tarea|RestoConPeso]) :-
    tarea(Tarea, _, _, Peso, _, _),
    agregar_pesos(Resto, RestoConPeso).

extraer_nombres([], []).
extraer_nombres([_-Nombre|Resto], [Nombre|NombresResto]) :-
    extraer_nombres(Resto, NombresResto).

% ===============================================
% Calcular horas de inicio
% ===============================================
calcular_horas_inicio([], _, _, []).
calcular_horas_inicio([Tarea|Resto], Hora, Minuto, [tarea(Tarea, HoraStr)|RestoHoras]) :-
    tarea(Tarea, _, Tiempo, _, _, _),
    format_hora(Hora, Minuto, HoraStr),

    % Calcular siguiente hora
    MinutosTotales is Minuto + Tiempo,
    NuevaHora is Hora + (MinutosTotales // 60),
    NuevoMinuto is MinutosTotales mod 60,

    calcular_horas_inicio(Resto, NuevaHora, NuevoMinuto, RestoHoras).

format_hora(Hora, Minuto, HoraStr) :-
    format(atom(H), '~|~`0t~d~2+', [Hora]),
    format(atom(M), '~|~`0t~d~2+', [Minuto]),
    atom_concat(H, ':', Temp),
    atom_concat(Temp, M, HoraStr).

% ===============================================
% Limpiar base de conocimiento
% ===============================================
limpiar_tareas :-
    retractall(tarea(_, _, _, _, _, _)),
    retractall(completada(_)),
    retractall(clima_actual(_)),
    retractall(tiempo_disponible(_)).

% ===============================================
% Agregar tarea dinámica
% ===============================================
agregar_tarea(Nombre, Prioridad, Tiempo, Peso, Clima, Dependencia) :-
    assertz(tarea(Nombre, Prioridad, Tiempo, Peso, Clima, Dependencia)).