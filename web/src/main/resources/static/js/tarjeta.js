// Lo que comparten los datos de tarjeta del pago del delivery (js/pago.js) y de la seña de la
// reserva (cliente/reserva/reserva-nueva.html): el vencimiento se escribe como MM/AA, solo con
// números, y la barra se pone sola; el titular, solo con letras.

/** Deja solo los números de un texto. */
function soloNumeros(texto) {
    return texto.replace(/\D/g, '');
}

/**
 * Le da formato MM/AA al campo mientras se escribe. El mes va de 01 a 12: si empieza con 2..9 es
 * un mes de un dígito ("827" → "08/27"), y si empieza con 1 y sigue 3..9 también ("13" → "01/3").
 * @param alCambiar opcional: se llama después de cada cambio (por ejemplo, para sacar la marca roja)
 */
function formatoVencimiento(campo, alCambiar) {
    campo.addEventListener('input', function (evento) {
        const borrando = evento.inputType && evento.inputType.startsWith('delete');
        let d = soloNumeros(campo.value);
        if (d.length >= 1 && d[0] > '1') d = '0' + d;
        if (d.length >= 2 && d[0] === '1' && d[1] > '2') d = '0' + d;
        if (d.length >= 2 && d[0] === '0' && d[1] === '0') d = '0' + d.slice(2); // "00" no es un mes
        d = d.slice(0, 4);
        if (d.length > 2) {
            campo.value = d.slice(0, 2) + '/' + d.slice(2);
        } else if (d.length === 2 && !borrando) {
            campo.value = d + '/';
        } else {
            campo.value = d;
        }
        if (alCambiar) alCambiar();
    });
}

/** Lo que no puede ir en el nombre de un titular: todo menos letras (con tilde y ñ), espacios, ' . y - */
const NO_VA_EN_UN_NOMBRE = /[^\p{L}\s'.-]/gu;

/**
 * El campo del titular acepta solo letras: lo demás (números, símbolos) se saca mientras se escribe,
 * igual que los espacios repetidos, sin mover el cursor de lugar.
 * @param alCambiar opcional: se llama después de cada cambio (por ejemplo, para sacar la marca roja)
 */
function soloLetras(campo, alCambiar) {
    campo.addEventListener('input', function () {
        const antes = campo.value;
        const limpio = antes.replace(NO_VA_EN_UN_NOMBRE, '').replace(/\s{2,}/g, ' ');   // y un solo espacio entre palabras
        if (limpio !== antes) {
            const cursor = campo.selectionStart - (antes.length - limpio.length);
            campo.value = limpio;
            campo.setSelectionRange(Math.max(0, cursor), Math.max(0, cursor));
        }
        if (alCambiar) alCambiar();
    });
}

/** null si el nombre del titular está bien; si no, qué le pasa. */
function revisarTitular(texto) {
    const nombre = texto.trim();
    if (nombre === '') return 'falta el nombre del titular';
    if (!/^\p{L}[\p{L}\s'.-]*$/u.test(nombre)) return 'el titular solo puede tener letras';
    return null;
}

/** null si el vencimiento MM/AA está bien; si no, qué le pasa. */
function revisarVencimiento(texto) {
    const d = soloNumeros(texto);
    if (d.length !== 4) return 'el vencimiento va como MM/AA';
    const mes = parseInt(d.slice(0, 2), 10);
    const anio = 2000 + parseInt(d.slice(2), 10);
    if (mes < 1 || mes > 12) return 'el mes del vencimiento va de 01 a 12';
    const hoy = new Date();
    // la tarjeta sirve hasta el último día del mes que dice
    if (anio < hoy.getFullYear() || (anio === hoy.getFullYear() && mes < hoy.getMonth() + 1)) {
        return 'la tarjeta está vencida';
    }
    if (anio > hoy.getFullYear() + 15) return 'el vencimiento es de dentro de más de 15 años';
    return null;
}

/** "08/27" → "2027-08" (como lo recibe el servidor); "" si todavía no está completo. */
function vencimientoParaEnviar(texto) {
    const d = soloNumeros(texto);
    return d.length === 4 ? '20' + d.slice(2) + '-' + d.slice(0, 2) : '';
}

/** "2027-08" → "08/27" (para volver a mostrar lo que se había cargado). */
function vencimientoParaMostrar(valor) {
    const m = /^(\d{4})-(\d{2})$/.exec(valor || '');
    return m ? m[2] + '/' + m[1].slice(2) : '';
}
