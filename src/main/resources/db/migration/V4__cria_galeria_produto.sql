CREATE TABLE produto_galeria (
    produto_id BIGINT NOT NULL,
    posicao INT NOT NULL,
    url VARCHAR(500) NOT NULL,
    PRIMARY KEY (produto_id, posicao),
    CONSTRAINT fk_produto_galeria FOREIGN KEY (produto_id) REFERENCES produto (produto_id)
);
