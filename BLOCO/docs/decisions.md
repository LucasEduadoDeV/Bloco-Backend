# Decisões técnicas

Este documento acompanha decisões técnicas tomadas ao longo do projeto.
Registre o contexto e as consequências, além da escolha, para que decisões
futuras possam ser avaliadas com base no histórico.

## Configuração técnica atual

Os itens abaixo descrevem a configuração encontrada no projeto. A motivação
original não está registrada:

- Java 21 e Spring Boot 4.1.2.
- PostgreSQL com acesso por Spring Data JPA/Hibernate.
- `spring.jpa.hibernate.ddl-auto=update`.
- Spring MVC, Spring Security e SpringDoc OpenAPI. Spring Data REST foi removido
  ao implementar endpoints explícitos de autenticação.

Não presumir que a inclusão de uma dependência representa uma decisão de uso
em produção. Documente a decisão quando o comportamento e a intenção forem
definidos.

## Registro de decisões

Adicione cada nova decisão usando este modelo:

### ADR-NNN — Título curto

- **Status:** proposta | aceita | substituída | rejeitada
- **Data:** AAAA-MM-DD
- **Contexto:** problema, restrições e motivação
- **Decisão:** escolha feita
- **Alternativas consideradas:** opções avaliadas e por que não foram escolhidas
- **Consequências:** benefícios, custos e impactos para manutenção/operação

Mantenha as decisões antigas no histórico. Se uma decisão for substituída,
atualize seu status e indique a ADR que a substituiu.

### ADR-001 — Autenticação temporária sem sessão

- **Status:** substituída pela ADR-002
- **Data:** 2026-11-14
- **Contexto:** implementar cadastro e login antes de definir como a sessão do
  usuário será mantida.
- **Decisão:** o cadastro e o login retornam somente o perfil público. O login
  valida as credenciais, mas não cria sessão, cookie ou token. O Spring Security
  usa configuração stateless.
- **Alternativas consideradas:** token no corpo da resposta; sessão/cookie.
- **Consequências:** não há credencial de sessão para proteger chamadas futuras.
  Essa decisão foi substituída pela ADR-002.

### ADR-002 — JWT de acesso em cookie HttpOnly

- **Status:** aceita
- **Data:** 2026-11-15
- **Contexto:** implementar sessão autenticada sem expor o token ao JavaScript
  nem armazená-lo em `localStorage`.
- **Decisão:** emitir um JWT HS256 de acesso com validade de 15 minutos no
  cookie `BLOCO_AUTH`, marcado `HttpOnly`, `SameSite=Lax` e `Secure` por padrão.
  Não emitir refresh token nesta etapa. Proteger operações mutáveis com CSRF.
- **Alternativas consideradas:** token no corpo da resposta e sessão HTTP
  gerenciada pelo servidor.
- **Consequências:** chamadas protegidas usam o cookie automaticamente e
  precisam enviar o token CSRF. Sem refresh token, é necessário autenticar
  novamente após 15 minutos. Logout apaga o cookie, mas um JWT copiado continua
  válido até expirar. O segredo HS256 deve ser configurado externamente.
