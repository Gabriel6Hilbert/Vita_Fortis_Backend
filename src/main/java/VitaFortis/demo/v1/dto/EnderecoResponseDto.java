package VitaFortis.demo.v1.dto;

public record EnderecoResponseDto(
        Long id,
        String apelido,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf,
        boolean principal
) {
}
