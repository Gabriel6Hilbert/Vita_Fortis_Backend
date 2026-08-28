const overlay = document.getElementById("assinaturaOverlay");
const btnsAbrir = document.querySelectorAll(".comprarOfertas");
const btnFechar = overlay.querySelector(".btn-fechar");
const btnConfirmar = document.getElementById("proximoAssinatura");

function abrirModal(){
  overlay.classList.remove("hidden");
  overlay.setAttribute("aria-hidden", "false");
  document.body.style.overflow = "hidden"; // trava scroll do fundo
}

function fecharModal(){
  overlay.classList.add("hidden");
  overlay.setAttribute("aria-hidden", "true");
  document.body.style.overflow = "";
}

/* Abre ao clicar no "ASSINAR CLUBE" */
btnsAbrir.forEach((btn) => {
  btn.addEventListener("click", (e) => {
    e.preventDefault(); // impede o # de subir a página
    abrirModal();
  });
});

/* Fecha no X */
btnFechar.addEventListener("click", fecharModal);

/* Fecha clicando fora */
overlay.addEventListener("click", (e) => {
  if (e.target === overlay) fecharModal();
});

/* Fecha no ESC */
document.addEventListener("keydown", (e) => {
  if (e.key === "Escape" && !overlay.classList.contains("hidden")) {
    fecharModal();
  }
});

/* Confirmar assinatura */
btnConfirmar.addEventListener("click", () => {
  const selecionado = document.querySelector('input[name="plano"]:checked');
  const plano = selecionado ? selecionado.value : "mensal";

  // Aqui você decide o que fazer:
  // 1) Ir pra uma página de checkout do clube:
  // window.location.href = `checkoutClube.html?plano=${plano}`;

  // 2) Ou só mostrar no console:
  console.log("Plano escolhido:", plano);

  fecharModal();
});
