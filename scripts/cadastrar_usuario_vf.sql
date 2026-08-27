USE vitafortis;

-- Conta local para acesso administrativo ao Vita Fortis.
-- A senha 963852 abaixo esta armazenada como hash BCrypt, nunca em texto puro.
INSERT INTO USUARIO (
    NOME,
    EMAIL,
    SENHA,
    CPF,
    TELEFONE,
    PONTOS_FIDELIDADE,
    TIPO_USUARIO,
    ATIVO,
    CREATED_AT,
    UPDATED_AT
)
VALUES (
    'Administrador Vita Fortis',
    'vf@gmail.com',
    '$2a$10$vMRSDYRsqd/4bXltjmmpxO6pEccukOKUNeZv9SgMrPFRlwyZnlv0S',
    '12345678909',
    NULL,
    0,
    'ADMIN',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON DUPLICATE KEY UPDATE
    NOME = VALUES(NOME),
    SENHA = VALUES(SENHA),
    TIPO_USUARIO = 'ADMIN',
    ATIVO = TRUE,
    UPDATED_AT = CURRENT_TIMESTAMP;

SELECT USUARIO_ID, NOME, EMAIL, TIPO_USUARIO, ATIVO
FROM USUARIO
WHERE EMAIL = 'vf@gmail.com';
