CREATE TABLE home_secao (
    home_secao_id BIGINT NOT NULL AUTO_INCREMENT,
    chave VARCHAR(40) NOT NULL,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    ordem INT NOT NULL,
    modo VARCHAR(20) NOT NULL DEFAULT 'DINAMICO',
    titulo VARCHAR(120),
    subtitulo VARCHAR(500),
    media_url VARCHAR(500),
    texto VARCHAR(1000),
    limite INT NOT NULL DEFAULT 4,
    PRIMARY KEY (home_secao_id),
    CONSTRAINT uk_home_secao_chave UNIQUE (chave)
);

CREATE TABLE home_secao_produto (
    home_secao_id BIGINT NOT NULL,
    posicao INT NOT NULL,
    produto_id BIGINT NOT NULL,
    PRIMARY KEY (home_secao_id, posicao),
    CONSTRAINT fk_home_secao_produto_secao FOREIGN KEY (home_secao_id) REFERENCES home_secao (home_secao_id),
    CONSTRAINT fk_home_secao_produto_produto FOREIGN KEY (produto_id) REFERENCES produto (produto_id)
);

INSERT INTO home_secao (chave, ativa, ordem, modo, titulo, subtitulo, media_url, texto, limite) VALUES
('HERO', TRUE, 10, 'DINAMICO', NULL, NULL, NULL, NULL, 3),
('OBJETIVOS', TRUE, 20, 'DINAMICO', 'EU QUERO...', 'Escolha seu objetivo e encontre produtos relacionados.', NULL, NULL, 4),
('OFERTAS', TRUE, 30, 'DINAMICO', 'Ofertas para aproveitar agora', NULL, NULL, NULL, 4),
('MAIS_VENDIDOS', TRUE, 40, 'DINAMICO', 'Mais vendidos', 'Os produtos mais escolhidos na Vita Fortis.', NULL, NULL, 8),
('DESTAQUES', TRUE, 50, 'DINAMICO', 'Produtos em destaque', NULL, NULL, NULL, 4),
('VIDEO', TRUE, 60, 'DINAMICO', 'Evolução também é constância.', 'Um espaço preparado para receber histórias, treinos e escolhas que acompanham diferentes ritmos.', '/assets/imagens/hero-performance-desktop.webp', 'Conteúdo audiovisual em preparação', 1),
('LINHAS', TRUE, 70, 'DINAMICO', 'Uma Linha, Uma Função', NULL, NULL, NULL, 5),
('ATALHOS', TRUE, 80, 'DINAMICO', NULL, NULL, NULL, NULL, 4),
('INSTITUCIONAL', TRUE, 90, 'DINAMICO', 'Frase institucional provisória — aguardando aprovação', NULL, '/assets/imagens/banner-foto-1.jpg', NULL, 1);
