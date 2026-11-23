CREATE TABLE home_objetivo (
    home_objetivo_id BIGINT NOT NULL AUTO_INCREMENT,
    chave VARCHAR(40) NOT NULL,
    titulo VARCHAR(80) NOT NULL,
    imagem_url VARCHAR(500),
    texto_alternativo VARCHAR(180) NOT NULL,
    ordem INT NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (home_objetivo_id),
    CONSTRAINT uk_home_objetivo_chave UNIQUE (chave),
    CONSTRAINT uk_home_objetivo_ordem UNIQUE (ordem)
);

INSERT INTO home_objetivo (chave,titulo,imagem_url,texto_alternativo,ordem,ativo) VALUES
('EMAGRECIMENTO','Emagrecer','/assets/imagens/objetivos-vita/emagrecer.png','Pessoa praticando atividade física com foco em emagrecimento',10,TRUE),
('GANHO_DE_MASSA','Ganhar massa','/assets/imagens/objetivos-vita/ganhar-massa.png','Pessoa treinando força com foco em ganho de massa muscular',20,TRUE),
('PERFORMANCE','Melhorar performance','/assets/imagens/objetivos-vita/performance.png','Atleta em movimento com foco em performance',30,TRUE),
('SAUDE_E_BEM_ESTAR','Saúde e bem-estar','/assets/imagens/objetivos-vita/bem-estar.png','Pessoa em momento de equilíbrio e bem-estar',40,TRUE);
