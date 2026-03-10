let formPendiente = null;

document.addEventListener("DOMContentLoaded", () => {

    const modal = new bootstrap.Modal(document.getElementById("confirmModal"));
    const mensaje = document.getElementById("confirmMessage");
    const aceptar = document.getElementById("confirmAccept");

    document.querySelectorAll("[data-confirm]").forEach(element => {

        element.addEventListener("click", function(e) {

            e.preventDefault();

            formPendiente = this.closest("form");

            mensaje.textContent = this.dataset.confirm || "¿Confirmar acción?";

            modal.show();

        });

    });

    aceptar.addEventListener("click", () => {

        if (formPendiente) {
            formPendiente.submit();
        }

    });

});