// Pantalla de pago del delivery (cliente/delivery/delivery-pago.html). Usa js/tarjeta.js y js/campos.js.
// El pago es SIMULADO: los datos de la tarjeta no se envían. Acá solo se les da formato
// mientras se escriben y se revisan al tocar "Pagar". Para Mercado Pago y MODO se dibuja un QR
// de ejemplo, y "Abrir..." muestra que te lleva a la app y confirma el pedido.

document.addEventListener('DOMContentLoaded', function () {
    const numero = document.getElementById('tarjeta-numero');
    const titular = document.getElementById('tarjeta-titular');
    const vencimiento = document.getElementById('tarjeta-vencimiento');
    const cvv = document.getElementById('tarjeta-cvv');
    const verCvv = document.getElementById('ver-cvv');
    const marca = document.getElementById('marca-tarjeta');
    const error = document.getElementById('error-tarjeta');
    const formulario = document.querySelector('form.pago');
    if (!numero || !formulario) return;

    // ---------- número: solo 16 números, de 4 en 4 ----------
    numero.addEventListener('input', function () {
        const digitos = soloNumeros(numero.value).slice(0, 16);
        numero.value = digitos.replace(/(\d{4})(?=\d)/g, '$1 ');
        // la marca según el primer número (4 = Visa, 5 = Mastercard)
        marca.textContent = digitos.startsWith('4') ? 'VISA' : digitos.startsWith('5') ? 'MASTERCARD' : '';
        numero.classList.remove('invalido');
    });

    // ---------- vencimiento: MM/AA, la barra se pone sola (js/tarjeta.js) ----------
    formatoVencimiento(vencimiento, function () { vencimiento.classList.remove('invalido'); });

    // ---------- código de seguridad: 3 números y el ojito para verlo ----------
    cvv.addEventListener('input', function () {
        cvv.value = soloNumeros(cvv.value).slice(0, 3);
        cvv.classList.remove('invalido');
    });
    verCvv.addEventListener('click', function () {
        const mostrar = cvv.type === 'password';
        cvv.type = mostrar ? 'text' : 'password';
        // ojo abierto = se está viendo el código; tachado = oculto
        verCvv.innerHTML = mostrar ? OJO_ABIERTO : OJO_TACHADO;
        verCvv.classList.toggle('activo', mostrar);
        verCvv.title = mostrar ? 'Ocultar el código' : 'Mostrar el código';
        verCvv.setAttribute('aria-label', verCvv.title);
    });

    // ---------- titular: solo letras (js/tarjeta.js) ----------
    soloLetras(titular, function () { titular.classList.remove('invalido'); });

    // ---------- al pagar con tarjeta, que los datos estén completos ----------
    formulario.addEventListener('submit', function (evento) {
        const elegido = formulario.querySelector('input[name="metodoPago"]:checked');
        if (!elegido || elegido.dataset.medio !== 'tarjeta') return;

        const problemas = [];
        if (soloNumeros(numero.value).length !== 16) problemas.push([numero, 'el número tiene que tener 16 dígitos']);
        const problemaTitular = revisarTitular(titular.value);
        if (problemaTitular) problemas.push([titular, problemaTitular]);
        const problemaVencimiento = revisarVencimiento(vencimiento.value);
        if (problemaVencimiento) problemas.push([vencimiento, problemaVencimiento]);
        if (soloNumeros(cvv.value).length !== 3) problemas.push([cvv, 'el código de seguridad tiene 3 números']);

        if (problemas.length > 0) {
            evento.preventDefault();
            problemas.forEach(function (p) { p[0].classList.add('invalido'); });
            const lista = problemas.map(function (p) { return p[1]; }).join(', ');
            error.textContent = 'Revisá la tarjeta: ' + lista + '.';
            error.hidden = false;
            problemas[0][0].focus();
        } else {
            error.hidden = true;
        }
    });

    // ---------- Mercado Pago y MODO: "te lleva" a la app y confirma el pedido ----------
    document.querySelectorAll('.qr').forEach(dibujarQrDeEjemplo);
    const redireccion = document.getElementById('redireccion');
    const titulo = document.getElementById('redireccion-titulo');
    const texto = document.getElementById('redireccion-texto');
    document.querySelectorAll('.abrir-app').forEach(function (boton) {
        boton.addEventListener('click', function () {
            // primero lo que pide el formulario (la dirección de entrega)
            if (!formulario.reportValidity()) return;
            const app = boton.dataset.app;
            titulo.textContent = 'Te estamos llevando a ' + app;
            texto.textContent = 'Aboná desde la app y volvés a La Nona.';
            redireccion.hidden = false;
            setTimeout(function () {
                titulo.textContent = 'Pago aprobado';
                texto.textContent = 'Volviendo a La Nona para confirmar tu pedido…';
                setTimeout(function () { formulario.submit(); }, 1200);
            }, 2200);
        });
    });
});

/**
 * Dibuja un QR de ejemplo (no se puede escanear): los tres cuadrados de las esquinas
 * y cuadraditos al azar en el medio, para que se vea como uno de verdad.
 */
function dibujarQrDeEjemplo(caja) {
    const n = 25;
    const svg = ['<svg viewBox="0 0 ' + n + ' ' + n + '" xmlns="http://www.w3.org/2000/svg" shape-rendering="crispEdges">'];
    const esquina = function (x, y) {
        return x < 8 && y < 8 || x >= n - 8 && y < 8 || x < 8 && y >= n - 8;
    };
    const ojo = function (x, y) {
        svg.push('<rect x="' + x + '" y="' + y + '" width="7" height="7" fill="#2A2420"/>');
        svg.push('<rect x="' + (x + 1) + '" y="' + (y + 1) + '" width="5" height="5" fill="#fff"/>');
        svg.push('<rect x="' + (x + 2) + '" y="' + (y + 2) + '" width="3" height="3" fill="#2A2420"/>');
    };
    ojo(0, 0);
    ojo(n - 7, 0);
    ojo(0, n - 7);
    for (let y = 0; y < n; y++) {
        for (let x = 0; x < n; x++) {
            if (!esquina(x, y) && Math.random() < 0.45) {
                svg.push('<rect x="' + x + '" y="' + y + '" width="1" height="1" fill="#2A2420"/>');
            }
        }
    }
    svg.push('</svg>');
    caja.innerHTML = svg.join('');
}
