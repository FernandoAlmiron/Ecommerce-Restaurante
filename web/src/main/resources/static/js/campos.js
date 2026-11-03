// Campos comunes de los formularios del cliente (registro, perfil, contacto, pago).

// Celular: solo números y como máximo 10 (código de área + número, ej. 1155551234).
// Se aplica solo a los campos con class="celular" de la página.
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('input.celular').forEach(function (campo) {
        campo.addEventListener('input', function () {
            const numeros = campo.value.replace(/\D/g, '').slice(0, 10);
            if (numeros !== campo.value) campo.value = numeros;
        });
    });
});

// íconos del ojito para ver una contraseña (los mismos trazos que usan la mayoría de las páginas)
const OJO_ABIERTO = '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" '
    + 'stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">'
    + '<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>';
const OJO_TACHADO = '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/><path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/><path d="M14.12 14.12a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>';

/**
 * Le agrega a un campo de contraseña el ojito para verla: tachado mientras está oculta,
 * abierto mientras se ve. El campo tiene que estar dentro de un <span class="con-ojo">.
 */
function agregarOjo(campo) {
    const boton = document.createElement('button');
    boton.type = 'button';
    boton.className = 'ojo';
    boton.innerHTML = OJO_TACHADO;
    boton.title = 'Mostrar la contraseña';
    boton.setAttribute('aria-label', boton.title);
    boton.addEventListener('click', function () {
        const mostrar = campo.type === 'password';
        campo.type = mostrar ? 'text' : 'password';
        boton.innerHTML = mostrar ? OJO_ABIERTO : OJO_TACHADO;
        boton.title = mostrar ? 'Ocultar la contraseña' : 'Mostrar la contraseña';
        boton.setAttribute('aria-label', boton.title);
    });
    campo.insertAdjacentElement('afterend', boton);
}

document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.con-ojo input[data-ojo]').forEach(agregarOjo);
});
