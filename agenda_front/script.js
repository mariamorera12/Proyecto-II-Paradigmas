const API_AGENDA = "http://localhost:8081/api";
const API_PROLOG = "http://localhost:8080/api/prolog";

let configuracion = {
    prioridades: [],
    climas: [],
    tareas: []
};

// ========== INICIALIZACIÓN ==========
window.addEventListener('DOMContentLoaded', async () => {
    await cargarConfiguracion();
    await cargarPrioridades();
    await cargarClimas();
    await verTareas();
    setFechaHoy();
    updateStatus('success', 'Conectado');
});

function setFechaHoy() {
    const hoy = new Date().toISOString().split('T')[0];
    document.getElementById('fecha').value = hoy;
    document.getElementById('fechaPlan').value = hoy;
    document.getElementById('fechaFiltro').value = hoy;
}

function updateStatus(type, msg) {
    const badge = document.getElementById('statusBadge');
    badge.className = `badge ${type === 'success' ? 'bg-success' : 'bg-danger'}`;
    badge.textContent = msg;
}

// ========== UTILIDADES ==========
async function handleFetch(url, options = {}) {
    try {
        const response = await fetch(url, options);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        return await response.json();
    } catch (error) {
        console.error('Error en petición:', error);
        showAlert('danger', `Error: ${error.message}`);
        throw error;
    }
}

function showAlert(type, message, containerId = 'healthResult') {
    const container = document.getElementById(containerId);
    container.innerHTML = `<div class="alert alert-${type} mt-2">${message}</div>`;
    setTimeout(() => { container.innerHTML = ''; }, 5000);
}

async function testSum() {
    try {
        const res = await fetch(`${API_PROLOG}/sum?a=5&b=3`);
        const text = await res.text();
        showAlert('success', `Suma 5+3 = ${text}`);
    } catch (error) {
        showAlert('danger', 'Error en test de suma');
    }
}

// ========== CONFIGURACIÓN ==========
async function cargarConfiguracion() {
    try {
        const [prioridades, climas] = await Promise.all([
            handleFetch(`${API_AGENDA}/prioridades`),
            handleFetch(`${API_AGENDA}/climas`)
        ]);

        configuracion.prioridades = prioridades;
        configuracion.climas = climas;

        llenarSelectPrioridad('prioridad', prioridades);
        llenarSelectClima('clima', climas);
        llenarSelectClima('climaActual', climas);

        showAlert('success', `Configuración cargada: ${prioridades.length} prioridades, ${climas.length} climas`, 'configResult');
    } catch (error) {
        showAlert('danger', 'Error al cargar configuración', 'configResult');
    }
}

// ========== GESTIÓN DE CONFIGURACIÓN ==========
function mostrarTab(tab) {
    document.getElementById('panel-prioridades').style.display = tab === 'prioridades' ? 'block' : 'none';
    document.getElementById('panel-climas').style.display = tab === 'climas' ? 'block' : 'none';

    document.getElementById('tab-prioridades').classList.toggle('active', tab === 'prioridades');
    document.getElementById('tab-climas').classList.toggle('active', tab === 'climas');
}

// ---- PRIORIDADES ----
async function cargarPrioridades() {
    try {
        const prioridades = await handleFetch(`${API_AGENDA}/prioridades`);
        renderPrioridades(prioridades);
    } catch (e) {
        document.getElementById('tablaPrioridades').innerHTML =
            `<tr><td colspan="4" class="text-center text-danger">Error al cargar prioridades</td></tr>`;
    }
}

function renderPrioridades(prioridades) {
    const tbody = document.getElementById('tablaPrioridades');
    if (!prioridades.length) {
        tbody.innerHTML = `<tr><td colspan="4" class="text-center text-muted">Sin prioridades registradas</td></tr>`;
        return;
    }

    tbody.innerHTML = prioridades.map(p => `
    <tr>
      <td>${p.id || '-'}</td>
      <td>${p.nivel}</td>
      <td>${p.peso}</td>
      <td>
        <button class="btn btn-primary btn-sm" onclick="editarPrioridad(${p.id}, '${p.nivel}', ${p.peso})">✏️</button>
        <button class="btn btn-danger btn-sm" onclick="eliminarPrioridad(${p.id})">🗑️</button>
      </td>
    </tr>`).join('');
}

document.getElementById('formPrioridad').addEventListener('submit', async (e) => {
    e.preventDefault();
    const body = {
        nivel: document.getElementById('nivelPrioridad').value,
        peso: parseInt(document.getElementById('pesoPrioridad').value)
    };
    try {
        await handleFetch(`${API_AGENDA}/prioridades`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(body)
        });
        showAlert('success', 'Prioridad guardada correctamente');
        e.target.reset();
        await cargarConfiguracion();
        await cargarPrioridades();
    } catch {
        showAlert('danger', 'Error al guardar prioridad');
    }
});

async function editarPrioridad(id, nivel, peso) {
    const nuevoNivel = prompt('Nuevo nivel:', nivel);
    const nuevoPeso = prompt('Nuevo peso:', peso);
    if (!nuevoNivel || !nuevoPeso) return;
    try {
        await handleFetch(`${API_AGENDA}/prioridades/${id}`, {
            method: 'PUT',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({nivel: nuevoNivel, peso: parseInt(nuevoPeso)})
        });
        showAlert('success', 'Prioridad actualizada');
        await cargarConfiguracion();
        await cargarPrioridades();
    } catch {
        showAlert('danger', 'Error al actualizar prioridad');
    }
}

async function eliminarPrioridad(id) {
    if (!confirm('¿Eliminar esta prioridad?')) return;
    try {
        await handleFetch(`${API_AGENDA}/prioridades/${id}`, {method: 'DELETE'});
        showAlert('success', 'Prioridad eliminada');
        await cargarConfiguracion();
        await cargarPrioridades();
    } catch {
        showAlert('danger', 'Error al eliminar prioridad');
    }
}

// ---- CLIMAS ----
async function cargarClimas() {
    try {
        const climas = await handleFetch(`${API_AGENDA}/climas`);
        renderClimas(climas);
    } catch {
        document.getElementById('tablaClimas').innerHTML =
            `<tr><td colspan="3" class="text-center text-danger">Error al cargar climas</td></tr>`;
    }
}

function renderClimas(climas) {
    const tbody = document.getElementById('tablaClimas');
    if (!climas.length) {
        tbody.innerHTML = `<tr><td colspan="3" class="text-center text-muted">Sin climas registrados</td></tr>`;
        return;
    }

    tbody.innerHTML = climas.map(c => `
    <tr>
      <td>${c.id || '-'}</td>
      <td>${c.tipo}</td>
      <td>
        <button class="btn btn-primary btn-sm" onclick="editarClima(${c.id}, '${c.tipo}')">✏️</button>
        <button class="btn btn-danger btn-sm" onclick="eliminarClima(${c.id})">🗑️</button>
      </td>
    </tr>`).join('');
}

document.getElementById('formClima').addEventListener('submit', async (e) => {
    e.preventDefault();
    const body = { tipo: document.getElementById('tipoClima').value };
    try {
        await handleFetch(`${API_AGENDA}/climas`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(body)
        });
        showAlert('success', 'Clima guardado correctamente');
        e.target.reset();
        await cargarConfiguracion();
        await cargarClimas();
    } catch {
        showAlert('danger', 'Error al guardar clima');
    }
});

async function editarClima(id, tipo) {
    const nuevoTipo = prompt('Nuevo tipo de clima:', tipo);
    if (!nuevoTipo) return;
    try {
        await handleFetch(`${API_AGENDA}/climas/${id}`, {
            method: 'PUT',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({ tipo: nuevoTipo })
        });
        showAlert('success', 'Clima actualizado');
        await cargarConfiguracion();
        await cargarClimas();
    } catch {
        showAlert('danger', 'Error al actualizar clima');
    }
}

async function eliminarClima(id) {
    if (!confirm('¿Eliminar este clima?')) return;
    try {
        await handleFetch(`${API_AGENDA}/climas/${id}`, { method: 'DELETE' });
        showAlert('success', 'Clima eliminado');
        await cargarConfiguracion();
        await cargarClimas();
    } catch {
        showAlert('danger', 'Error al eliminar clima');
    }
}

function llenarSelectPrioridad(id, prioridades) {
    const select = document.getElementById(id);
    select.innerHTML = '<option value="">Seleccione...</option>' +
        prioridades.map(p => `<option value="${p.nivel}">${p.nivel} (Peso: ${p.peso})</option>`).join('');
}

function llenarSelectClima(id, climas) {
    const select = document.getElementById(id);
    select.innerHTML = '<option value="">Seleccione...</option>' +
        climas.map(c => `<option value="${c.tipo}">${c.tipo}</option>`).join('');
}

async function cargarTareasDependencia() {
    try {
        const tareas = await handleFetch(`${API_AGENDA}/tareas`);
        configuracion.tareas = tareas;

        const select = document.getElementById('dependencia');
        select.innerHTML = '<option value="">Sin dependencia</option>' +
            tareas.map(t => `<option value="${t.id}">${t.nombre} (ID: ${t.id})</option>`).join('');
    } catch (error) {
        console.error('Error al cargar tareas para dependencia:', error);
    }
}

// ========== GESTIÓN DE TAREAS ==========
document.getElementById('formTarea').addEventListener('submit', async (e) => {
    e.preventDefault();

    const body = {
        nombre: document.getElementById('nombre').value,
        prioridad: document.getElementById('prioridad').value,
        tiempoEstimado: parseInt(document.getElementById('tiempo').value),
        clima: document.getElementById('clima').value,
        fecha: document.getElementById('fecha').value
    };

    const depId = document.getElementById('dependencia').value;
    if (depId) {
        body.tareaDependenciaId = parseInt(depId);
    }

    try {
        await handleFetch(`${API_AGENDA}/tareas`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });

        showAlert('success', 'Tarea creada correctamente');
        e.target.reset();
        setFechaHoy();
        await verTareas();
    } catch (error) {
        showAlert('danger', 'Error al crear tarea');
    }
});

async function verTareas() {
    try {
        const tareas = await handleFetch(`${API_AGENDA}/tareas`);
        configuracion.tareas = tareas;
        renderTareas(tareas);
        await cargarTareasDependencia();
    } catch (error) {
        document.getElementById('tablaTareas').innerHTML =
            '<tr><td colspan="9" class="text-center text-danger">Error al cargar tareas</td></tr>';
    }
}

async function verTareasPorFecha() {
    const fecha = document.getElementById('fechaFiltro').value;
    if (!fecha) {
        showAlert('warning', 'Seleccione una fecha');
        return;
    }

    try {
        const tareas = await handleFetch(`${API_AGENDA}/tareas/fecha/${fecha}`);
        renderTareas(tareas);
        showAlert('info', `Mostrando tareas del ${fecha}`);
    } catch (error) {
        showAlert('danger', 'Error al filtrar tareas');
    }
}

function renderTareas(tareas) {
    const tbody = document.getElementById('tablaTareas');

    if (!tareas || tareas.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="text-center text-muted">No hay tareas registradas</td></tr>';
        return;
    }

    tbody.innerHTML = tareas.map(t => {
        const nombreDep = t.tareaDependencia ? t.tareaDependencia : '-';

        return `
            <tr>
                <td>${t.id}</td>
                <td><strong>${t.nombre}</strong></td>
                <td><span class="badge bg-info">${t.prioridad}</span></td>
                <td>${t.tiempoEstimado}</td>
                <td>${t.clima}</td>
                <td>${t.fecha}</td>
                <td>
                    <span class="badge ${t.completada ? 'badge-completada' : 'badge-pendiente'}">
                        ${t.completada ? 'Completada' : 'Pendiente'}
                    </span>
                </td>
                <td>${nombreDep}</td>
                <td>
                    ${!t.completada ? `<button class="btn btn-success btn-sm" onclick="completarTarea(${t.id})" title="Completar">✔</button>` : ''}
                    <button class="btn btn-primary btn-sm" onclick="editarTarea(${t.id})" title="Editar">✏️</button>
                    <button class="btn btn-danger btn-sm" onclick="eliminarTarea(${t.id})" title="Eliminar">🗑️</button>
                </td>
            </tr>
        `;
    }).join('');
}

async function completarTarea(id) {
    if (!confirm('¿Marcar como completada?')) return;

    try {
        await handleFetch(`${API_AGENDA}/tareas/${id}/completar`, {
            method: 'PATCH'
        });
        showAlert('success', 'Tarea completada');
        await verTareas();
    } catch (error) {
        showAlert('danger', 'Error al completar tarea');
    }
}

async function editarTarea(id) {
    const tareas = await handleFetch(`${API_AGENDA}/tareas`);
    const tarea = tareas.find(t => t.id === id);
    if (!tarea) {
        showAlert('danger', 'Tarea no encontrada');
        return;
    }

    const nombre = prompt('Nombre:', tarea.nombre);
    if (nombre === null) return;

    const prioridad = prompt('Prioridad (Alta/Media/Baja):', tarea.prioridad);
    if (prioridad === null) return;

    const tiempo = prompt('Tiempo estimado (min):', tarea.tiempoEstimado);
    if (tiempo === null) return;

    const clima = prompt('Clima:', tarea.clima);
    if (clima === null) return;

    const fecha = prompt('Fecha (YYYY-MM-DD):', tarea.fecha);
    if (fecha === null) return;

    // Dependencia
    let depActualTexto = 'Ninguna';
    let depActualId = null;
    if (tarea.tareaDependencia) {
        const depTarea = tareas.find(t => t.nombre === tarea.tareaDependencia);
        if (depTarea) {
            depActualId = depTarea.id;
            depActualTexto = `${depTarea.id}: ${depTarea.nombre}`;
        } else {
            depActualTexto = tarea.tareaDependencia;
        }
    }

    // Mostrar tareas disponibles para dependencia
    const posiblesDeps = tareas
        .filter(t => t.id !== id)
        .map(t => `${t.id}: ${t.nombre}`)
        .join('\n');

    const mensajeDep = posiblesDeps ? `\n\nTareas disponibles:\n${posiblesDeps}\n\n` : '\n\n(No hay tareas disponibles para dependencia)\n\n';

    let depInput = prompt(
        `Tarea dependiente actual: ${depActualTexto}${mensajeDep}Ingrese el ID de la nueva tarea dependiente (o deje vacío para ninguna):`,
        depActualId || ''
    );

    let tareaDependenciaId = null;
    if (depInput && depInput.trim() !== '') {
        const depId = parseInt(depInput);
        const depExiste = tareas.some(t => t.id === depId && t.id !== id);
        if (!depExiste) {
            showAlert('warning', 'ID de dependencia inválido. Se dejará sin dependencia.');
        } else {
            tareaDependenciaId = depId;
        }
    }

    const body = {
        nombre,
        prioridad,
        tiempoEstimado: parseInt(tiempo),
        clima,
        fecha,
        tareaDependenciaId
    };

    try {
        await handleFetch(`${API_AGENDA}/tareas/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
        showAlert('success', 'Tarea actualizada correctamente');
        await verTareas();
    } catch (error) {
        showAlert('danger', 'Error al actualizar tarea: ' + error.message);
    }
}

async function eliminarTarea(id) {
    if (!confirm('¿Eliminar esta tarea?')) return;

    try {
        await handleFetch(`${API_AGENDA}/tareas/${id}`, {
            method: 'DELETE'
        });
        showAlert('success', 'Tarea eliminada');
        await verTareas();
    } catch (error) {
        showAlert('danger', 'Error al eliminar tarea');
    }
}

// ========== PLANIFICACIÓN ==========
document.getElementById('formPlan').addEventListener('submit', async (e) => {
    e.preventDefault();

    const body = {
        tiempoDisponible: parseInt(document.getElementById('tiempoDisponible').value),
        climaActual: document.getElementById('climaActual').value,
        fecha: document.getElementById('fechaPlan').value
    };

    const container = document.getElementById('resultadoPlan');
    container.innerHTML = '<div class="alert alert-info">Generando plan...</div>';

    try {
        const response = await fetch(`${API_AGENDA}/tareas/planificar`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        const plan = await response.json();

        console.log('Respuesta del servidor:', plan);
        console.log('Tipo de respuesta:', typeof plan);
        console.log('Es array?', Array.isArray(plan));

        let planArray = plan;
        if (!Array.isArray(plan)) {
            if (plan.plan) planArray = plan.plan;
            else if (plan.tareas) planArray = plan.tareas;
            else if (plan.resultado) planArray = plan.resultado;
            else if (typeof plan === 'object' && plan !== null) planArray = [plan];
            else planArray = [];
        }

        if (!planArray || planArray.length === 0) {
            container.innerHTML = `
                <div class="alert alert-danger">
                    No es posible generar un plan con las condiciones actuales.
                    <br><small>Intenta aumentar el tiempo disponible o verifica que haya tareas pendientes.</small>
                </div>`;
        } else {
            let html = `
                <div class="alert alert-success">
                    ${plan.mensaje || `Plan generado exitosamente (${planArray.length} tareas programadas)`}
                </div>
                <table class="table table-hover table-striped">
                    <thead>
                        <tr>
                            <th>Orden</th>
                            <th>Tarea</th>
                            <th>Hora de Inicio</th>
                        </tr>
                    </thead>
                    <tbody>`;

            planArray.forEach((item, idx) => {
                html += `
                    <tr>
                        <td><strong>${idx + 1}</strong></td>
                        <td>${item.tarea || item.nombre || item.Tarea || 'Sin nombre'}</td>
                        <td><span class="badge bg-primary">${item.horaInicio || item.Hora || item.hora || item.horario || 'N/A'}</span></td>
                    </tr>`;
            });

            html += `</tbody></table>`;
            container.innerHTML = html;
        }
    } catch (error) {
        container.innerHTML = `<div class="alert alert-danger">Error al planificar: ${error.message}</div>`;
    }
});