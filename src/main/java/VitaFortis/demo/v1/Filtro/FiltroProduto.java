package VitaFortis.demo.v1.Filtro;

import VitaFortis.demo.v1.dto.ProdutoFiltroDto;
import VitaFortis.demo.v1.entity.Produto;
import VitaFortis.demo.v1.enums.CategoriaProduto;
import VitaFortis.demo.v1.enums.ProdutoOrdenacao;
import jakarta.persistence.criteria.Path;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class FiltroProduto {

    private FiltroProduto() {}

    public static Specification<Produto> buildSpec(ProdutoFiltroDto f) {
        return Specification
                .where(publico())
                .and(nomeContem(f.getNome()))
                .and(categoriaIgual(f.getCategoria()))
                .and(precoEntre(f.getPrecoMin(), f.getPrecoMax()))
                .and(resgatavelIgual(f.getResgatavel()));
    }

    public static Sort buildSort(ProdutoOrdenacao ord) {
        if (ord == null) return Sort.by(Sort.Order.asc("nome").ignoreCase());
        switch (ord) {
            case NOME_ASC:   return Sort.by(Sort.Order.asc("nome").ignoreCase());
            case NOME_DESC:  return Sort.by(Sort.Order.desc("nome").ignoreCase());
            case PRECO_ASC:  return Sort.by(Sort.Order.asc("preco"));
            case PRECO_DESC: return Sort.by(Sort.Order.desc("preco"));
            default:         return Sort.by(Sort.Order.asc("nome").ignoreCase());
        }
    }


    private static Specification<Produto> publico() {
        return (root, q, cb) -> cb.and(
                cb.isTrue(root.get("ativo")),
                cb.greaterThan(root.get("quantidadeEstoque"), 0)
        );
    }

    private static Specification<Produto> nomeContem(String nome) {
        return (root, q, cb) -> (nome == null || nome.isBlank())
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("nome")), "%" + nome.trim().toLowerCase() + "%");
    }

    private static Specification<Produto> categoriaIgual(CategoriaProduto cat) {
        return (root, q, cb) -> (cat == null)
                ? cb.conjunction()
                : cb.equal(root.get("categoria"), cat);
    }

    private static Specification<Produto> precoEntre(BigDecimal min, BigDecimal max) {
        return (root, q, cb) -> {
            if (min == null && max == null) return cb.conjunction();
            Path<BigDecimal> preco = root.get("preco");
            if (min != null && max != null) return cb.between(preco, min, max);
            return (min != null) ? cb.greaterThanOrEqualTo(preco, min)
                    : cb.lessThanOrEqualTo(preco, max);
        };
    }

    private static Specification<Produto> resgatavelIgual(Boolean r) {
        return (root, q, cb) -> (r == null)
                ? cb.conjunction()
                : (r ? cb.isTrue(root.get("resgatavel")) : cb.isFalse(root.get("resgatavel")));
    }
}

