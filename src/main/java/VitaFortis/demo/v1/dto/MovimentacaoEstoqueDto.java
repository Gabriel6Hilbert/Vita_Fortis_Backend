package VitaFortis.demo.v1.dto;
import java.time.LocalDateTime;
public record MovimentacaoEstoqueDto(Long id,Long produtoId,String produtoNome,int quantidadeAnterior,int quantidadeNova,int variacao,String motivo,String responsavel,LocalDateTime criadoEm){}
