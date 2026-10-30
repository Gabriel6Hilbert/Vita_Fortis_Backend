# Banco, backup e Flyway - procedimento seguro

Este documento define a estrategia tecnica. Ele nao autoriza conexao, migracao ou restauracao
no banco de producao.

## Estado atual e risco

- O perfil padrao usa `ddl-auto=update` e nao executa Flyway.
- O perfil `prod` usa `ddl-auto=validate` e Flyway, mas ainda nao esta ativado explicitamente no Render.
- As migracoes V1 e V2 podem ja ter sido aplicadas indiretamente pelo Hibernate. Executa-las de novo
  pode colidir com colunas ou tabelas existentes.
- O inicializador do catalogo ainda altera `produto.preco` no startup. Esse DDL devera virar uma nova
  migracao somente depois do inventario do esquema real.

## Ativacao controlada do Flyway

1. Fazer inventario somente leitura do MySQL real: versao, esquema, `flyway_schema_history`, efeitos
   completos de V1/V2, indices, chaves e tipo de `produto.preco`.
2. Gerar backup verificavel e restaura-lo em MySQL isolado da mesma versao.
3. Se V1 e V2 ja estiverem integralmente presentes e nao houver historico, criar baseline controlado
   na versao 2 no clone. Nunca usar baseline automatico na versao 0.
4. Se houver divergencia, criar uma V3 aditiva/reconciliadora. Nao editar V1/V2 ja publicadas.
5. No clone, executar `validate`, `migrate`, uma segunda execucao sem mudancas, Hibernate `validate`,
   testes e verificacoes de integridade.
6. Com backup verificado e autorizacao explicita, repetir o procedimento controlado em producao e,
   somente depois, definir `SPRING_PROFILES_ACTIVE=prod`.

O estado permanente usa `baseline-on-migrate=false`, `clean-disabled=true`,
`validate-on-migrate=true`, `out-of-order=false` e Hibernate `ddl-auto=validate`.

## Backup

- Primario: snapshot ou PITR nativo do provedor, se o plano disponibilizar.
- Secundario: dump logico diario, criptografado e armazenado fora do disco efemero do Render.
- Retencao minima: 7 copias diarias e 4 semanais. Nunca sobrescrever a unica copia valida.
- O dump MySQL deve usar `--single-transaction --quick --hex-blob --routines --triggers --events`
  e `--default-character-set=utf8mb4`. `--hex-blob` e necessario porque imagens podem estar no banco.
- Cada copia deve ter manifesto com horario UTC, versao do MySQL, tamanho, SHA-256 e contagens-chave.

O script `scripts/database/backup-mysql.ps1` implementa dump consistente, `hex-blob`, manifesto SHA-256,
7 copias diarias e 4 semanais. A senha e lida apenas de `VITAFORTIS_BACKUP_DB_PASSWORD`.

```powershell
$env:VITAFORTIS_BACKUP_DB_PASSWORD = '<segredo-do-cofre>'
.\scripts\database\backup-mysql.ps1 -DatabaseHost '<host>' -DatabasePort 3306 `
  -DatabaseName '<banco>' -DatabaseUser '<usuario-backup>' -OutputDirectory '<diretorio-externo>'
```

## Teste de restauracao

Mensalmente, restaurar uma copia em banco isolado com nome inequivoco. Nunca restaurar por cima de
producao. Comparar contagens de usuarios, produtos, pedidos, itens, solicitacoes LGPD e imagens;
verificar orfaos, chaves, Flyway, Hibernate validate e smoke test. Registrar RPO, RTO e o hash da copia.

O script `scripts/database/restore-verify-mysql.ps1` recusa destino cujo nome nao comece por `restore_`
ou `vitafortis_restore_`, exige confirmacao nominal e valida o SHA-256 antes da restauracao. Ele nunca
deve receber o nome do banco de producao.

## Evidencia local de 28/10/2026

- scripts de backup e restauracao passaram pela validacao sintatica do PowerShell;
- Flyway aplicou V1/V2, validou o historico e uma segunda execucao aplicou zero migracoes;
- um esquema legado simulado com os efeitos de V1/V2 foi baselined na versao 2 e validado;
- o ensaio usou H2 em modo MySQL, pois esta maquina nao possui Docker, `mysql` ou `mysqldump`;
- nenhum acesso ao MySQL real e nenhuma alteracao de producao foram executados.

Dependencias externas que permanecem: identificar o provedor/versao do MySQL, escolher o armazenamento
externo e cadastrar credenciais de backup com privilegios minimos.
