package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.dto.HomeSecaoDto;
import VitaFortis.demo.v1.dto.HomeOrdemRequestDto;
import VitaFortis.demo.v1.entity.HomeSecao;
import VitaFortis.demo.v1.repository.HomeSecaoRepository;
import VitaFortis.demo.v1.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@Service
public class HomeSecaoService {
    private static final Set<String> CHAVES = Set.of("HERO", "OBJETIVOS", "OFERTAS", "MAIS_VENDIDOS", "DESTAQUES", "VIDEO", "LINHAS", "ATALHOS", "INSTITUCIONAL");
    private static final Set<String> VITRINES = Set.of("OFERTAS", "MAIS_VENDIDOS", "DESTAQUES");
    private final HomeSecaoRepository secoes;
    private final ProdutoRepository produtos;

    public HomeSecaoService(HomeSecaoRepository secoes, ProdutoRepository produtos) {
        this.secoes = secoes;
        this.produtos = produtos;
    }

    @Transactional
    public synchronized List<HomeSecaoDto> listarPublicas() {
        garantirPadroes();
        return secoes.findAllByAtivaTrueOrderByOrdemAscIdAsc().stream().map(this::dto).toList();
    }

    @Transactional
    public synchronized List<HomeSecaoDto> listarAdmin() {
        garantirPadroes();
        return secoes.findAllByOrderByOrdemAscIdAsc().stream().map(this::dto).toList();
    }

    @Transactional
    public HomeSecaoDto atualizar(String chave, HomeSecaoDto entrada) {
        String normalizada = chave.trim().toUpperCase();
        if (!CHAVES.contains(normalizada) || !normalizada.equalsIgnoreCase(entrada.getChave())) {
            throw new IllegalArgumentException("Secao da Home invalida");
        }
        HomeSecao secao = secoes.findByChaveIgnoreCase(normalizada)
                .orElseThrow(() -> new IllegalArgumentException("Secao da Home nao encontrada"));
        secao.setAtiva(entrada.isAtiva());
        secao.setOrdem(entrada.getOrdem());
        secao.setTitulo(limpar(entrada.getTitulo()));
        secao.setSubtitulo(limpar(entrada.getSubtitulo()));
        secao.setMediaUrl(validarMidia(entrada.getMediaUrl()));
        secao.setTexto(limpar(entrada.getTexto()));
        secao.setLimite(Math.max(1, Math.min(20, entrada.getLimite())));
        secao.setModo(VITRINES.contains(normalizada) ? entrada.getModo() : HomeSecao.Modo.DINAMICO);

        var ids = new LinkedHashSet<>(entrada.getProdutoIds() == null ? List.<Long>of() : entrada.getProdutoIds());
        if (!VITRINES.contains(normalizada) || secao.getModo() == HomeSecao.Modo.DINAMICO) ids.clear();
        if (ids.size() > secao.getLimite()) throw new IllegalArgumentException("A selecao manual excede o limite da secao");
        if (ids.stream().anyMatch(id -> id == null || produtos.findById(id).filter(produto -> produto.isAtivo()).isEmpty())) {
            throw new IllegalArgumentException("A selecao manual deve conter somente produtos ativos e existentes");
        }
        secao.setProdutoIds(new ArrayList<>(ids));
        return dto(secoes.save(secao));
    }

    @Transactional
    public List<HomeSecaoDto> atualizarOrdem(HomeOrdemRequestDto entrada) {
        List<HomeOrdemRequestDto.Item> itens=entrada.secoes();
        Set<String> chaves=new LinkedHashSet<>(); Set<Integer> posicoes=new LinkedHashSet<>();
        for(var item:itens){String chave=item.chave().trim().toUpperCase();if(!CHAVES.contains(chave))throw new IllegalArgumentException("Secao da Home desconhecida");if(!chaves.add(chave))throw new IllegalArgumentException("Nao repita secoes na nova posicao");if(!posicoes.add(item.posicao()))throw new IllegalArgumentException("Nao repita posicoes na nova ordem");}
        if(!chaves.equals(CHAVES))throw new IllegalArgumentException("Informe todas as secoes conhecidas da Home");
        Map<String,HomeSecao> atuais=secoes.findAll().stream().collect(java.util.stream.Collectors.toMap(s->s.getChave().toUpperCase(),s->s));
        if(!atuais.keySet().containsAll(CHAVES))throw new IllegalArgumentException("Configuracao da Home incompleta");
        for(var item:itens)atuais.get(item.chave().trim().toUpperCase()).setOrdem(item.posicao());
        return secoes.saveAll(atuais.values()).stream().sorted(java.util.Comparator.comparingInt(HomeSecao::getOrdem)).map(this::dto).toList();
    }

    private String validarMidia(String valor) {
        String limpa = limpar(valor);
        if (limpa != null && !(limpa.startsWith("/") || limpa.startsWith("https://"))) {
            throw new IllegalArgumentException("A midia deve usar um caminho local ou HTTPS");
        }
        return limpa;
    }

    private String limpar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private void garantirPadroes() {
        Set<String> existentes = secoes.findAll().stream()
                .map(HomeSecao::getChave)
                .filter(chave -> chave != null)
                .map(String::toUpperCase)
                .collect(java.util.stream.Collectors.toSet());

        List<HomeSecao> ausentes = padroes().entrySet().stream()
                .filter(entry -> !existentes.contains(entry.getKey()))
                .map(entry -> novaSecao(entry.getKey(), entry.getValue()))
                .toList();
        if (!ausentes.isEmpty()) secoes.saveAll(ausentes);
    }

    private Map<String, Consumer<HomeSecao>> padroes() {
        return Map.ofEntries(
                Map.entry("HERO", secao -> configurar(secao, 10, 3, null, null, null, null)),
                Map.entry("OBJETIVOS", secao -> configurar(secao, 20, 4, "EU QUERO...", "Escolha seu objetivo e encontre produtos relacionados.", null, null)),
                Map.entry("OFERTAS", secao -> configurar(secao, 30, 4, "Ofertas para aproveitar agora", null, null, null)),
                Map.entry("MAIS_VENDIDOS", secao -> configurar(secao, 40, 8, "Mais vendidos", "Os produtos mais escolhidos na Vita Fortis.", null, null)),
                Map.entry("DESTAQUES", secao -> configurar(secao, 50, 4, "Produtos em destaque", null, null, null)),
                Map.entry("VIDEO", secao -> configurar(secao, 60, 1, "Evolução também é constância.", "Um espaço preparado para receber histórias, treinos e escolhas que acompanham diferentes ritmos.", "/assets/imagens/hero-performance-desktop.webp", "Conteúdo audiovisual em preparação")),
                Map.entry("LINHAS", secao -> configurar(secao, 70, 5, "Uma Linha, Uma Função", null, null, null)),
                Map.entry("ATALHOS", secao -> configurar(secao, 80, 4, null, null, null, null)),
                Map.entry("INSTITUCIONAL", secao -> configurar(secao, 90, 1, "Frase institucional provisória — aguardando aprovação", null, "/assets/imagens/banner-foto-1.jpg", null))
        );
    }

    private HomeSecao novaSecao(String chave, Consumer<HomeSecao> configuracao) {
        HomeSecao secao = new HomeSecao();
        secao.setChave(chave);
        secao.setAtiva(true);
        secao.setModo(HomeSecao.Modo.DINAMICO);
        configuracao.accept(secao);
        return secao;
    }

    private void configurar(HomeSecao secao, int ordem, int limite, String titulo, String subtitulo, String mediaUrl, String texto) {
        secao.setOrdem(ordem);
        secao.setLimite(limite);
        secao.setTitulo(titulo);
        secao.setSubtitulo(subtitulo);
        secao.setMediaUrl(mediaUrl);
        secao.setTexto(texto);
    }

    private HomeSecaoDto dto(HomeSecao secao) {
        HomeSecaoDto dto = new HomeSecaoDto();
        dto.setId(secao.getId()); dto.setChave(secao.getChave()); dto.setAtiva(secao.isAtiva()); dto.setOrdem(secao.getOrdem());
        dto.setModo(secao.getModo()); dto.setTitulo(secao.getTitulo()); dto.setSubtitulo(secao.getSubtitulo());
        dto.setMediaUrl(secao.getMediaUrl()); dto.setTexto(secao.getTexto()); dto.setLimite(secao.getLimite());
        dto.setProdutoIds(List.copyOf(secao.getProdutoIds()));
        return dto;
    }
}
