---
name: ecommerce-security-review
description: Revisar segurança do Vita Fortis em sessão, CSRF, autenticação, autorização por perfil, isolamento de dados e exposição de informações.
---

Verifique, conforme o escopo:

- sessão/cookie no Spring e ausência de JWT ou `localStorage` como substituto de autenticação;
- CSRF em operações mutáveis, obtenção/renovação de token, login e logout;
- autorização no controller/service para ADMIN, CLIENTE e COLABORADOR;
- derivação do proprietário a partir de `Authentication`, nunca de `usuarioId` confiado do cliente;
- respostas, logs, erros e exports sem credenciais ou dados indevidos;
- regras de rate limit e cabeçalhos proxy sem confundir defesa local com proteção de borda;
- RCP-02 técnico sem definir bases legais, retenção ou políticas jurídicas.

Classifique achados por impacto e cite arquivo/linha. Diferencie revisão estática, teste local e homologação pública. Não publique correções automaticamente.
