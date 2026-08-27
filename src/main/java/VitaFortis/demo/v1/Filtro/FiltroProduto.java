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
        return Specification.allOf(
                publico(),
                buscaContem(f.getBusca() == null ? f.getNome() : f.getBusca()),
                marcaIgual(f.getMarca()),
                categoriaIgual(f.getCategoria()),
                precoEntre(f.getPrecoMin(), f.getPrecoMax()),
                resgatavelIgual(f.getResgatavel()),
                conjuntoContem("objetivos", f.getObjetivos()), conjuntoContem("esportes", f.getEsportes()),
                booleano("vegano", f.getVegano()), booleano("vegetariano", f.getVegetariano()),
                booleano("linhaClinica", f.getLinhaClinica()), booleano("lancamento", f.getLancamento()),
                textoIgual("subcategoria", f.getSubcategoria()), oferta(f.getOferta()),
                emEstoque(f.getEmEstoque()), descontoMin(f.getDescontoMin())
                );
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
                cb.isTrue(root.get("ativo"))
        );
    }

    public static Sort buildSort(ProdutoFiltroDto f) {
        if (Boolean.TRUE.equals(f.getMaisVendidos())) return Sort.by(Sort.Order.desc("totalVendido"), Sort.Order.asc("nome"));
        return buildSort(f.getOrdenacao());
    }

    private static Specification<Produto> buscaContem(String termo) {
        return (root, q, cb) -> {
            if (termo == null || termo.isBlank()) return cb.conjunction();
            String like = "%" + termo.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("nome")), like),
                    cb.like(cb.lower(root.get("descricao")), like),
                    cb.like(cb.lower(root.get("codigo")), like),
                    cb.like(cb.lower(root.get("marca")), like)
            );
        };
    }

    private static Specification<Produto> marcaIgual(String marca) {
        return (root, q, cb) -> (marca == null || marca.isBlank())
                ? cb.conjunction()
                : cb.equal(cb.lower(root.get("marca")), marca.trim().toLowerCase());
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

    private static Specification<Produto> booleano(String campo, Boolean valor) {
        return (root, q, cb) -> valor == null ? cb.conjunction() : cb.equal(root.get(campo), valor);
    }
    private static Specification<Produto> textoIgual(String campo, String valor) {
        return (root, q, cb) -> valor == null || valor.isBlank() ? cb.conjunction()
                : cb.equal(cb.lower(root.get(campo)), valor.trim().toLowerCase());
    }
    private static Specification<Produto> conjuntoContem(String campo, java.util.Set<String> valores) {
        return (root, q, cb) -> {
            if (valores == null || valores.isEmpty()) return cb.conjunction();
            q.distinct(true);
            var join = root.<Produto, String>joinSet(campo);
            return join.in(valores.stream().map(v -> v.trim().toUpperCase()).toList());
        };
    }
    private static Specification<Produto> oferta(Boolean valor) {
        return (root, q, cb) -> !Boolean.TRUE.equals(valor) ? cb.conjunction()
                : cb.or(cb.greaterThan(root.get("descontoPercentual"), BigDecimal.ZERO), cb.greaterThan(root.get("descontoValor"), BigDecimal.ZERO));
    }
    private static Specification<Produto> emEstoque(Boolean valor) {
        return (root, q, cb) -> valor == null ? cb.conjunction()
                : (valor ? cb.greaterThan(root.get("quantidadeEstoque"), 0) : cb.equal(root.get("quantidadeEstoque"), 0));
    }
    private static Specification<Produto> descontoMin(BigDecimal valor) {
        return (root, q, cb) -> valor == null ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("descontoPercentual"), valor);
    }
}

