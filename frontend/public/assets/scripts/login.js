/*JS da página de login*/

document.addEventListener('DOMContentLoaded', () => {

    /*Faz abrir o novo formulário quando clica na frase "cadastre-se agora" e voltar quando clica na frase "já tenho conta"*/
    /*pego os elementos com esses ids:*/
    const btnCadastro = document.getElementById('botaoCadastreAgora');
    const btnCriarConta = document.getElementById('btnEntreAgora');
    const primeiroCard = document.getElementById('cardLogin');
    const segundoCard = document.getElementById('cardCadastro');

    /*adiciono um evento de click no bntcadastro (cadastre-se):*/
    btnCadastro.addEventListener('click', (e) => {
        e.preventDefault();
        primeiroCard.style.display = 'none'; /*esconde login e mostra cadastro*/
        segundoCard.style.display = 'block';
    });

    /*adiciono um evento de click no btnCriarConta (fazer login):*/
    btnCriarConta.addEventListener('click', (e) => {
        e.preventDefault();
        segundoCard.style.display = 'none'; /*esconde cadastro e mostra login*/
        primeiroCard.style.display = 'block';
    });

    /********************************************************************************/

    /*Lógica de login - usuários (admin, cliente e colaborador)*/

    /*pegando os ids e atribuindo a consts:*/
    const cardLogin = document.getElementById("cardLogin");
    const cardCadastro = document.getElementById("cardCadastro");
    const botaoCadastreAgora = document.getElementById("botaoCadastreAgora");
    const btnEntreAgora = document.getElementById("btnEntreAgora");
    const formLogin = document.getElementById("formLogin");
    const formCadastro = document.getElementById("formCadastro");
    const mensagemLogin = document.getElementById("mensagemLogin");
    const mensagemCadastro = document.getElementById("mensagemCadastro");

    /*fazendo login:*/
    formLogin.addEventListener("submit", async function (e) { /*atribuindo um evento submit ao form de login*/
        e.preventDefault(); /*evita recarregar a página*/

        const loginDigitado = document.getElementById("emailLogin").value.trim(); /*value.trim() remove espeaços vazios*/
        const senhaDigitada = document.getElementById("senhaLogin").value.trim();

        try {
            const resposta = await fetch("users.json");
            const usuarios = await resposta.json();

            const usuarioEncontrado = usuarios.find(function (usuario) {
                return (
                    (usuario.email.toLowerCase() === loginDigitado.toLowerCase() ||
                        usuario.cpf === loginDigitado) &&
                    usuario.senha === senhaDigitada
                );
            });

            if (!usuarioEncontrado) {
                mensagemLogin.textContent = "E-mail/CPF ou senha inválidos.";
                return;
            }

            if (!usuarioEncontrado.ativo) {
                mensagemLogin.textContent = "Usuário inativo.";
                return;
            }

            const sessao = {
                id: usuarioEncontrado.id,
                nome: usuarioEncontrado.nome,
                email: usuarioEncontrado.email,
                cpf: usuarioEncontrado.cpf,
                tipo: usuarioEncontrado.tipo,
                token: "token-fake-" + usuarioEncontrado.id
            };

            localStorage.setItem("usuarioLogado", JSON.stringify(sessao));

            if (usuarioEncontrado.tipo === "ADMIN") {
                window.location.href = "admin.html";
            } else {
                window.location.href = "index.html";
            }

        } catch (erro) {
            console.error("Erro no login:", erro);
            mensagemLogin.textContent = "Erro ao processar login.";
        }
    });

    /* cadastro */
    formCadastro.addEventListener("submit", async function (e) {
        e.preventDefault();

        mensagemCadastro.textContent = "";

        const nome = document.getElementById("nomeCadastro").value.trim();
        const email = document.getElementById("emailCadastro").value.trim();
        const cpf = document.getElementById("cpfCadastro").value.trim();
        const senha = document.getElementById("senhaCadastro").value.trim();

        if (!nome || !email || !cpf || !senha) {
            mensagemCadastro.textContent = "Preencha todos os campos.";
            return;
        }

        try {
            const resposta = await fetch("users.json");
            const usuarios = await resposta.json();

            const emailJaExiste = usuarios.some(function (usuario) {
                return usuario.email.toLowerCase() === email.toLowerCase();
            });

            const cpfJaExiste = usuarios.some(function (usuario) {
                return usuario.cpf === cpf;
            });

            if (emailJaExiste) {
                mensagemCadastro.textContent = "Este e-mail já está cadastrado.";
                return;
            }

            if (cpfJaExiste) {
                mensagemCadastro.textContent = "Este CPF já está cadastrado.";
                return;
            }

            mensagemCadastro.classList.remove("text-danger");
            mensagemCadastro.classList.add("text-success");
            mensagemCadastro.textContent = "Cadastro simulado com sucesso. Quando ligar ao backend, os dados serão salvos de verdade.";

            formCadastro.reset();

        } catch (erro) {
            console.error("Erro no cadastro:", erro);
            mensagemCadastro.textContent = "Erro ao processar cadastro.";
        }
    });

});