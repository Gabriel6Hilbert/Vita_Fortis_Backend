document.addEventListener("DOMContentLoaded", () => {

    /*PREENCHE O CORAÇÃO DE FAVORITO AO CLICAR*/
    document.querySelectorAll('.favorito-btn').forEach(function (btn) {
        btn.addEventListener('click', function () {
            const icon = this.querySelector('i');
            icon.classList.toggle('bi-heart');
            icon.classList.toggle('bi-heart-fill');
        });
    });

});




