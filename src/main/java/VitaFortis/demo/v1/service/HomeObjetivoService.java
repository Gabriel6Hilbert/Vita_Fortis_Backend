package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.dto.HomeObjetivoDto;
import VitaFortis.demo.v1.entity.HomeObjetivo;
import VitaFortis.demo.v1.repository.HomeObjetivoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class HomeObjetivoService {
    public static final List<String> CHAVES=List.of("EMAGRECIMENTO","GANHO_DE_MASSA","PERFORMANCE","SAUDE_E_BEM_ESTAR");
    private final HomeObjetivoRepository repository;
    public HomeObjetivoService(HomeObjetivoRepository repository){this.repository=repository;}

    @Transactional public List<HomeObjetivoDto> listarPublicos(){garantirPadroes();return repository.findAllByAtivoTrueOrderByOrdemAscIdAsc().stream().map(this::dto).toList();}
    @Transactional public List<HomeObjetivoDto> listarAdmin(){garantirPadroes();return repository.findAllByOrderByOrdemAscIdAsc().stream().map(this::dto).toList();}

    @Transactional public HomeObjetivoDto atualizar(String chave, HomeObjetivoDto entrada){
        String normalizada=chave.trim().toUpperCase(Locale.ROOT);
        if(!CHAVES.contains(normalizada)||!normalizada.equals(entrada.getChave())) throw new IllegalArgumentException("Objetivo da Home invalido");
        HomeObjetivo objetivo=repository.findByChaveIgnoreCase(normalizada).orElseThrow(()->new IllegalArgumentException("Objetivo da Home nao encontrado"));
        objetivo.setTitulo(entrada.getTitulo().trim()); objetivo.setTextoAlternativo(entrada.getTextoAlternativo().trim());
        objetivo.setImagemUrl(validarMidia(entrada.getImagemUrl())); objetivo.setAtivo(entrada.isAtivo()); objetivo.setOrdem(entrada.getOrdem());
        return dto(repository.save(objetivo));
    }
    private String validarMidia(String valor){if(valor==null||valor.isBlank())return null;String v=valor.trim();if(!(v.startsWith("/")||v.startsWith("https://")))throw new IllegalArgumentException("A imagem deve usar um caminho local ou HTTPS");return v;}
    private void garantirPadroes(){
        Set<String> existentes=repository.findAll().stream().map(HomeObjetivo::getChave).filter(Objects::nonNull).map(String::toUpperCase).collect(java.util.stream.Collectors.toSet());
        String[][] dados={{"EMAGRECIMENTO","Emagrecer","/assets/imagens/objetivos-vita/emagrecer.png","Pessoa praticando atividade física com foco em emagrecimento"},{"GANHO_DE_MASSA","Ganhar massa","/assets/imagens/objetivos-vita/ganhar-massa.png","Pessoa treinando força com foco em ganho de massa muscular"},{"PERFORMANCE","Melhorar performance","/assets/imagens/objetivos-vita/performance.png","Atleta em movimento com foco em performance"},{"SAUDE_E_BEM_ESTAR","Saúde e bem-estar","/assets/imagens/objetivos-vita/bem-estar.png","Pessoa em momento de equilíbrio e bem-estar"}};
        List<HomeObjetivo> novos=new ArrayList<>();for(int i=0;i<dados.length;i++){if(existentes.contains(dados[i][0]))continue;HomeObjetivo o=new HomeObjetivo();o.setChave(dados[i][0]);o.setTitulo(dados[i][1]);o.setImagemUrl(dados[i][2]);o.setTextoAlternativo(dados[i][3]);o.setOrdem((i+1)*10);o.setAtivo(true);novos.add(o);}if(!novos.isEmpty())repository.saveAll(novos);
    }
    private HomeObjetivoDto dto(HomeObjetivo o){HomeObjetivoDto d=new HomeObjetivoDto();d.setId(o.getId());d.setChave(o.getChave());d.setTitulo(o.getTitulo());d.setImagemUrl(o.getImagemUrl());d.setTextoAlternativo(o.getTextoAlternativo());d.setOrdem(o.getOrdem());d.setAtivo(o.isAtivo());return d;}
}
