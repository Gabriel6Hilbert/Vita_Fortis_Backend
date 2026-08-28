document.addEventListener("DOMContentLoaded", () => {

  /*ALTERNA OS SUB-BOTÕES DE MODA E ACESSÓRIOS*/
  const btnModa = document.getElementById("botaoModa");
  const btnAcessorios = document.getElementById("botaoAcessorios");
  const subModa = document.getElementById("subModa");
  const subAcessorios = document.getElementById("subAcessorios");

  btnModa.addEventListener("click", () => {
    subModa.classList.replace("d-none", "d-flex");
    subAcessorios.classList.replace("d-flex", "d-none");
  });

  btnAcessorios.addEventListener("click", () => {
    subAcessorios.classList.replace("d-none", "d-flex");
    subModa.classList.replace("d-flex", "d-none");
  });


  /*MUDA A COR DE FUNDO DOS CARDS QUANDO CLICA EM ALGUM SUB-BOTÃO DE MODA E ACESSÓRIOS*/
  const subBtns = document.querySelectorAll("#subModa .subBtn, #subAcessorios .subBtn");
  const cardsContainer = document.getElementById("cardsOutletFundo");

  subBtns.forEach(btn => {
    btn.addEventListener("click", () => {
      subBtns.forEach(b => b.classList.remove("active"));
      btn.classList.add("active");
      cardsContainer.classList.add("filtrado");
    });
  });


  /*PREENCHE O CORAÇÃO DE FAVORITO AO CLICAR*/
  document.querySelectorAll('.favorito-btn').forEach(function (btn) {
    btn.addEventListener('click', function () {
      const icon = this.querySelector('i');
      icon.classList.toggle('bi-heart');
      icon.classList.toggle('bi-heart-fill');
    });
  });


  /*"PREPARAÇÃO" DA FUNÇÃO DE ATUALIZAR O NÚMERO DE ITENS NO CARRINHO*/
  function atualizarCarrinho(qtd) {
    const contador = document.getElementById('contadorCarrinho');
    contador.textContent = qtd;
  }


  /*OPÇÃO "CATEGORIAS" NA PARTE DE BAIXO DO CABEÇALHO PARA CELULAR*/
  const btnCategorias = document.getElementById("btnCategorias");
  const megaCategorias = btnCategorias.closest(".mega");

  const btnMarcas = document.getElementById("btnMarcas");
  const megaMarcas = btnMarcas.closest(".mega");

  function isMobile() {
    return window.matchMedia("(hover: none)").matches || window.innerWidth <= 992;
  }

  btnCategorias.addEventListener("click", (e) => {
    if (!isMobile()) return;
    e.preventDefault();
    megaCategorias.classList.toggle("is-open");
  });

  btnMarcas.addEventListener("click", (e) => {
    if (!isMobile()) return;
    e.preventDefault();
    megaMarcas.classList.toggle("is-open");
  });

  document.addEventListener("click", (e) => {
    if (!megaCategorias.contains(e.target)) megaCategorias.classList.remove("is-open");
    if (!megaMarcas.contains(e.target)) megaMarcas.classList.remove("is-open");
  });

  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape") {
      megaCategorias.classList.remove("is-open");
      megaMarcas.classList.remove("is-open");
    }
  });

  /*Apenas a "preparação" da função de atualizar o número de itens no carrinho*/
  function atualizarCarrinho(qtd) {
    const contador = document.getElementById('contadorCarrinho');
    contador.textContent = qtd;
}

});




