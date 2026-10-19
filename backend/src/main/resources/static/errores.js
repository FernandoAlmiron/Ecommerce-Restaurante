
// Mostramos en pantalla los errores
document.addEventListener('htmx:responseError', function (evento) {
    let mensaje = 'Ocurrió un error';
    try {
        mensaje = JSON.parse(evento.detail.xhr.responseText).error || mensaje;
    } catch (e) {}

    const caja = document.getElementById('mensaje-error');
    if (!caja) return;

    caja.textContent = mensaje;
    caja.hidden = false;
    setTimeout(function () { caja.hidden = true; }, 5000);
});