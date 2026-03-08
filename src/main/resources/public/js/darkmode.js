// ===== MODO OSCURO =====
function toggleDarkMode() {
    const body = document.body;
    const icon = document.getElementById('darkModeIcon');

    body.classList.toggle('dark-mode');

    if (body.classList.contains('dark-mode')) {
        localStorage.setItem('darkMode', 'enabled');
        icon.classList.replace('bi-moon-fill', 'bi-sun-fill');
    } else {
        localStorage.setItem('darkMode', 'disabled');
        icon.classList.replace('bi-sun-fill', 'bi-moon-fill');
    }
}

// Aplicar modo oscuro al cargar si estaba activado
document.addEventListener('DOMContentLoaded', function() {
    const darkMode = localStorage.getItem('darkMode');
    const icon = document.getElementById('darkModeIcon');

    if (darkMode === 'enabled') {
        document.body.classList.add('dark-mode');
        if (icon) icon.classList.replace('bi-moon-fill', 'bi-sun-fill');
    }
});