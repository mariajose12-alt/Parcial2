// ===== PAGINACIÓN REUTILIZABLE =====
function inicializarPaginacion(tablaId, filasPorPagina = 10) {
    const tabla = document.getElementById(tablaId);
    if (!tabla) return;

    const tbody = tabla.querySelector('tbody');
    const filas = Array.from(tbody.querySelectorAll('tr[data-paginar]'));
    let paginaActual = 1;
    const totalPaginas = () => Math.ceil(filas.length / filasPorPagina);

    function mostrarPagina(pagina) {
        paginaActual = pagina;
        const inicio = (pagina - 1) * filasPorPagina;
        const fin = inicio + filasPorPagina;

        filas.forEach((fila, index) => {
            fila.style.display = (index >= inicio && index < fin) ? '' : 'none';
        });

        renderizarControles();
    }

    function renderizarControles() {
        const existente = document.getElementById('paginacion-' + tablaId);
        if (existente) existente.remove();

        if (filas.length <= filasPorPagina) return;

        const total = totalPaginas();
        const wrapper = document.createElement('div');
        wrapper.id = 'paginacion-' + tablaId;
        wrapper.className = 'd-flex justify-content-between align-items-center mt-3 px-1';

        // Info
        const inicio = ((paginaActual - 1) * filasPorPagina) + 1;
        const fin = Math.min(paginaActual * filasPorPagina, filas.length);
        const info = document.createElement('small');
        info.className = 'text-muted';
        info.textContent = `Mostrando ${inicio}–${fin} de ${filas.length} registros`;

        // Botones
        const nav = document.createElement('nav');
        const ul = document.createElement('ul');
        ul.className = 'pagination pagination-sm mb-0';

        // Anterior
        const liAnterior = document.createElement('li');
        liAnterior.className = 'page-item' + (paginaActual === 1 ? ' disabled' : '');
        liAnterior.innerHTML = `<a class="page-link" href="#">«</a>`;
        liAnterior.addEventListener('click', e => {
            e.preventDefault();
            if (paginaActual > 1) mostrarPagina(paginaActual - 1);
        });
        ul.appendChild(liAnterior);

        // Páginas
        for (let i = 1; i <= total; i++) {
            const li = document.createElement('li');
            li.className = 'page-item' + (i === paginaActual ? ' active' : '');
            li.innerHTML = `<a class="page-link" href="#">${i}</a>`;
            li.addEventListener('click', e => {
                e.preventDefault();
                mostrarPagina(i);
            });
            ul.appendChild(li);
        }

        // Siguiente
        const liSiguiente = document.createElement('li');
        liSiguiente.className = 'page-item' + (paginaActual === total ? ' disabled' : '');
        liSiguiente.innerHTML = `<a class="page-link" href="#">»</a>`;
        liSiguiente.addEventListener('click', e => {
            e.preventDefault();
            if (paginaActual < total) mostrarPagina(paginaActual + 1);
        });
        ul.appendChild(liSiguiente);

        nav.appendChild(ul);
        wrapper.appendChild(info);
        wrapper.appendChild(nav);

        tabla.parentElement.parentElement.appendChild(wrapper);
    }

    mostrarPagina(1);
}

// Reiniciar paginación cuando se filtra
function reiniciarPaginacion(tablaId, filasPorPagina = 10) {
    const tabla = document.getElementById(tablaId);
    if (!tabla) return;
    const tbody = tabla.querySelector('tbody');
    const todasFilas = Array.from(tbody.querySelectorAll('tr'));

    // Marcar filas visibles para paginar
    todasFilas.forEach(fila => {
        if (fila.style.display !== 'none') {
            fila.setAttribute('data-paginar', 'true');
        } else {
            fila.removeAttribute('data-paginar');
        }
    });

    inicializarPaginacion(tablaId, filasPorPagina);
}