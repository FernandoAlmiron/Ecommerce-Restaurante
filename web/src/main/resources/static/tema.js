// Aplica el tema guardado apenas carga la página (antes de pintar, así no "parpadea")
(function () {
    var guardado = localStorage.getItem('tema');
    var prefiereOscuro = window.matchMedia('(prefers-color-scheme: dark)').matches;
    var tema = guardado || (prefiereOscuro ? 'oscuro' : 'claro');
    document.documentElement.setAttribute('data-tema', tema);
})();

// Lo llama el botón 🌙 / ☀️
function cambiarTema() {
    var actual = document.documentElement.getAttribute('data-tema');
    var nuevo = actual === 'oscuro' ? 'claro' : 'oscuro';
    document.documentElement.setAttribute('data-tema', nuevo);
    localStorage.setItem('tema', nuevo);
}