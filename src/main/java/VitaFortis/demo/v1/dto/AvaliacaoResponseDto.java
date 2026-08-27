package VitaFortis.demo.v1.dto;
import java.time.LocalDateTime;
public record AvaliacaoResponseDto(Long id, Long produtoId, Long usuarioId, String usuarioNome, int nota,
                                   String comentario, boolean aprovado, LocalDateTime criadoEm) {}
