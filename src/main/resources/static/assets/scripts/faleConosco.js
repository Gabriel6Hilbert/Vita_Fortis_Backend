document.addEventListener("DOMContentLoaded", function () {

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


    /*FAZ A TROCA DE CONTEÚDO DE ACORDO COM A OPÇÃO SELECIONADA NO ASIDE*/
    const opcoes = document.querySelectorAll(".atendimentoNavItens");
    const secoes = document.querySelectorAll(".atendimentoSection");

    function ativarSecao(alvoId) {
        /*marca link ativo*/
        opcoes.forEach(l => l.classList.remove("is-active"));
        document.querySelector(`.atendimentoNavItens[href="#${alvoId}"]`)?.classList.add("is-active");

        /*mostra só a seção escolhida*/
        secoes.forEach(s => s.classList.remove("is-active"));
        document.getElementById(alvoId)?.classList.add("is-active");
    }

    opcoes.forEach(link => { /*clique*/
        link.addEventListener("click", function (e) {
            e.preventDefault();
            const alvoId = this.getAttribute("href").replace("#", "");
            ativarSecao(alvoId);
        });
    });

    ativarSecao("secaoFaleConosco"); /*sempre inicia na primeira seção - "fale conosco"*/


    /*FAZ A RESPOSTA DAS PERGUNTAS FREQUENTES APARECER E FECHAR*/
    document.querySelectorAll("#secaoPerguntasFreq .perguntaAoTodo").forEach((perguntaAoTodo) => {
        const btn = perguntaAoTodo.querySelector(".aPergunta");
        const answer = perguntaAoTodo.querySelector(".aResposta");
        const icon = perguntaAoTodo.querySelector(".iconePergunta");

        btn.addEventListener("click", () => {
            const isOpen = perguntaAoTodo.classList.contains("is-open");

            /*só fica aberto 1 por vez:*/
            document.querySelectorAll("#secaoPerguntasFreq .perguntaAoTodo.is-open").forEach((openItem) => {
                if (openItem !== perguntaAoTodo) {
                    openItem.classList.remove("is-open");
                    const b = openItem.querySelector(".aPergunta");
                    const a = openItem.querySelector(".aResposta");
                    const ic = openItem.querySelector(".iconePergunta");
                    b.setAttribute("aria-expanded", "false");
                    a.setAttribute("aria-hidden", "true");
                    ic.textContent = "+";
                }
            });

            perguntaAoTodo.classList.toggle("is-open", !isOpen);

            btn.setAttribute("aria-expanded", String(!isOpen));
            answer.setAttribute("aria-hidden", String(isOpen));
        });
    });
});
