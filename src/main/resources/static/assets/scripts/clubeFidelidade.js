/*JS da página do clube fidelidade*/

document.addEventListener("DOMContentLoaded", () => {

    //Preenchendo o coração do favorito ao clicar
    document.querySelectorAll('.favorito-btn').forEach(function (btn) {
        btn.addEventListener('click', function () {
            const icon = this.querySelector('i');
            icon.classList.toggle('bi-heart');
            icon.classList.toggle('bi-heart-fill');
        });
    });

    //Perguntas frequentes - abre e fecha
    document.querySelectorAll("#faq .faq-item").forEach((item) => {
        const btn = item.querySelector(".faq-question");
        const answer = item.querySelector(".faq-answer");
        const icon = item.querySelector(".faq-icon");
      
        btn.addEventListener("click", () => {
          const isOpen = item.classList.contains("is-open");
      
          //só fica aberto 1 por vez:
          document.querySelectorAll("#faq .faq-item.is-open").forEach((openItem) => {
            if (openItem !== item) {
              openItem.classList.remove("is-open");
              const b = openItem.querySelector(".faq-question");
              const a = openItem.querySelector(".faq-answer");
              const ic = openItem.querySelector(".faq-icon");
              b.setAttribute("aria-expanded", "false");
              a.setAttribute("aria-hidden", "true");
              ic.textContent = "+";
            }
          });
      
          item.classList.toggle("is-open", !isOpen);
      
          btn.setAttribute("aria-expanded", String(!isOpen));
          answer.setAttribute("aria-hidden", String(isOpen));
        });
      });

});