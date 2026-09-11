package VitaFortis.demo.v1.enums;

public enum ObjetivoProduto {
    EMAGRECIMENTO("Emagrecimento"),
    GANHO_DE_MASSA("Ganho de massa"),
    PERFORMANCE("Performance"),
    SAUDE_E_BEM_ESTAR("Saúde e bem-estar");

    private final String nome;
    ObjetivoProduto(String nome) { this.nome = nome; }
    public String getNome() { return nome; }
}
