package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.dto.HomeSecaoDto;
import VitaFortis.demo.v1.dto.HomeOrdemRequestDto;
import VitaFortis.demo.v1.entity.HomeSecao;
import VitaFortis.demo.v1.entity.Produto;
import VitaFortis.demo.v1.repository.HomeSecaoRepository;
import VitaFortis.demo.v1.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(MockitoExtension.class)
class HomeSecaoServiceTest {
    @Mock HomeSecaoRepository secoes;
    @Mock ProdutoRepository produtos;
    HomeSecaoService service;

    @BeforeEach void configurar() { service = new HomeSecaoService(secoes, produtos); }

    @Test
    void salvaCuradoriaManualNaOrdemEscolhida() {
        HomeSecao entity = secao("MAIS_VENDIDOS");
        HomeSecaoDto dto = dto("MAIS_VENDIDOS");
        dto.setModo(HomeSecao.Modo.MANUAL); dto.setLimite(3); dto.setProdutoIds(List.of(9L, 4L));
        when(secoes.findByChaveIgnoreCase("MAIS_VENDIDOS")).thenReturn(Optional.of(entity));
        when(produtos.findById(9L)).thenReturn(Optional.of(produtoAtivo(9L))); when(produtos.findById(4L)).thenReturn(Optional.of(produtoAtivo(4L)));
        when(secoes.save(entity)).thenReturn(entity);

        HomeSecaoDto saved = service.atualizar("MAIS_VENDIDOS", dto);

        assertThat(saved.getProdutoIds()).containsExactly(9L, 4L);
        assertThat(saved.getModo()).isEqualTo(HomeSecao.Modo.MANUAL);
        assertThatCode(() -> entity.getProdutoIds().clear()).doesNotThrowAnyException();
    }

    @Test
    void modoDinamicoRemoveSelecaoManual() {
        HomeSecao entity = secao("OFERTAS"); entity.setProdutoIds(new ArrayList<>(List.of(3L)));
        HomeSecaoDto dto = dto("OFERTAS"); dto.setModo(HomeSecao.Modo.DINAMICO); dto.setProdutoIds(List.of(3L));
        when(secoes.findByChaveIgnoreCase("OFERTAS")).thenReturn(Optional.of(entity)); when(secoes.save(entity)).thenReturn(entity);

        assertThat(service.atualizar("OFERTAS", dto).getProdutoIds()).isEmpty();
    }

    @Test
    void rejeitaProdutoInexistenteNaCuradoria() {
        HomeSecao entity = secao("DESTAQUES");
        HomeSecaoDto dto = dto("DESTAQUES"); dto.setModo(HomeSecao.Modo.MANUAL); dto.setProdutoIds(List.of(999L));
        when(secoes.findByChaveIgnoreCase("DESTAQUES")).thenReturn(Optional.of(entity)); when(produtos.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar("DESTAQUES", dto)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("ativos e existentes");
    }

    @Test
    void rejeitaProdutoInativoNaCuradoria() {
        HomeSecao entity = secao("DESTAQUES");
        HomeSecaoDto dto = dto("DESTAQUES"); dto.setModo(HomeSecao.Modo.MANUAL); dto.setProdutoIds(List.of(8L));
        Produto inativo = produtoAtivo(8L); inativo.setAtivo(false);
        when(secoes.findByChaveIgnoreCase("DESTAQUES")).thenReturn(Optional.of(entity)); when(produtos.findById(8L)).thenReturn(Optional.of(inativo));

        assertThatThrownBy(() -> service.atualizar("DESTAQUES", dto)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("ativos e existentes");
    }

    @Test
    void criaTodasAsSecoesPadraoQuandoBancoEstaVazio() {
        when(secoes.findAll()).thenReturn(List.of());
        when(secoes.findAllByOrderByOrdemAscIdAsc()).thenReturn(List.of());
        ArgumentCaptor<List<HomeSecao>> captor = ArgumentCaptor.forClass(List.class);

        service.listarAdmin();

        verify(secoes).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(9)
                .extracting(HomeSecao::getChave)
                .containsExactlyInAnyOrder("HERO", "OBJETIVOS", "OFERTAS", "MAIS_VENDIDOS", "DESTAQUES", "VIDEO", "LINHAS", "ATALHOS", "INSTITUCIONAL");
    }

    @Test
    void preservaConfiguracaoExistenteECriaSomenteChavesAusentes() {
        HomeSecao hero = secao("HERO");
        hero.setTitulo("Título personalizado");
        when(secoes.findAll()).thenReturn(List.of(hero));
        when(secoes.findAllByOrderByOrdemAscIdAsc()).thenReturn(List.of(hero));
        ArgumentCaptor<List<HomeSecao>> captor = ArgumentCaptor.forClass(List.class);

        service.listarAdmin();

        verify(secoes).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(8).noneMatch(secao -> secao.getChave().equals("HERO"));
        assertThat(hero.getTitulo()).isEqualTo("Título personalizado");
    }

    @Test void salvaOrdemCompletaEmUmaOperacao() {
        List<String> chaves=List.of("HERO","OBJETIVOS","OFERTAS","MAIS_VENDIDOS","DESTAQUES","VIDEO","LINHAS","ATALHOS","INSTITUCIONAL");
        List<HomeSecao> entities=new ArrayList<>(); for(int i=0;i<chaves.size();i++){HomeSecao s=secao(chaves.get(i));s.setId((long)i+1);entities.add(s);}
        when(secoes.findAll()).thenReturn(entities);doAnswer(invocation->new ArrayList<>((java.util.Collection<HomeSecao>)invocation.getArgument(0))).when(secoes).saveAll(org.mockito.ArgumentMatchers.any());
        var itens=new ArrayList<HomeOrdemRequestDto.Item>();for(int i=0;i<chaves.size();i++)itens.add(new HomeOrdemRequestDto.Item(chaves.get(i),(chaves.size()-i)*10));
        var salvas=service.atualizarOrdem(new HomeOrdemRequestDto(itens));
        assertThat(salvas).extracting(HomeSecaoDto::getChave).containsExactly("INSTITUCIONAL","ATALHOS","LINHAS","VIDEO","DESTAQUES","MAIS_VENDIDOS","OFERTAS","OBJETIVOS","HERO");
        verify(secoes).saveAll(org.mockito.ArgumentMatchers.any());
    }

    @Test void rejeitaOrdemIncompletaAntesDePersistir() {
        assertThatThrownBy(()->service.atualizarOrdem(new HomeOrdemRequestDto(List.of(new HomeOrdemRequestDto.Item("HERO",10))))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("todas");
    }

    private HomeSecao secao(String chave) { HomeSecao value = new HomeSecao(); value.setId(1L); value.setChave(chave); value.setAtiva(true); value.setOrdem(10); value.setLimite(4); return value; }
    private HomeSecaoDto dto(String chave) { HomeSecaoDto value = new HomeSecaoDto(); value.setChave(chave); value.setAtiva(true); value.setOrdem(10); value.setModo(HomeSecao.Modo.DINAMICO); value.setLimite(4); return value; }
    private Produto produtoAtivo(Long id) { Produto value = new Produto(); value.setId(id); value.setAtivo(true); return value; }
}
