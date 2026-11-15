# Arquitetura do backend

## Estado atual

O backend é uma aplicação Java iniciada pelo Spring Boot. A classe
`com.example.BLOCO.BlocoApplication` é o ponto de entrada e usa
`@SpringBootApplication` para iniciar a aplicação e a configuração automática
do Spring. A implementação está organizada por funcionalidade:

- `auth/controller`: endpoints de cadastro/login.
- `auth/service`: validação de credenciais e regras de cadastro/login.
- `auth/dto`: contratos de entrada e saída; entidades JPA não são retornadas
  diretamente pela API.
- `auth/exception`: erros específicos do fluxo de autenticação.
- `user/entity` e `user/repository`: entidade `UserAccount` e acesso à
  persistência.
- `common/web`: tratamento compartilhado de erros HTTP.
- `config`: configuração do Spring Security.

O fluxo de cadastro/login passa pelo controller, serviço de autenticação e
repositório de usuários. O controller valida os dados de entrada e só retorna
o perfil público do usuário.

## Tecnologias e persistência

- Java 21.
- Spring Boot 4.1.2.
- Spring MVC para a camada web.
- Jakarta Bean Validation para validar os contratos de entrada.
- Spring Data JPA e Hibernate para persistência relacional.
- PostgreSQL como banco de dados configurado.
- Spring Security para configurar acesso aos endpoints.
- SpringDoc OpenAPI para documentação da API.

As configurações de conexão estão em `src/main/resources/application.properties`.
`DB_URL`, `DB_USER` e `DB_PASSWORD` podem ser fornecidas pelo ambiente; na
ausência delas, são usados os valores locais definidos nesse arquivo.
O Hibernate está configurado com `spring.jpa.hibernate.ddl-auto=update`.

O Spring Security não cria sessão de servidor. Após validar o login, o backend
emite um JWT HS256 em cookie `HttpOnly`, com validade de 15 minutos. O filtro de
resource server lê o token desse cookie e valida assinatura, emissor e
expiração. A rota `GET /api/v1/me` exige um JWT válido. As demais rotas exigem
autenticação até que tenham uma política de acesso definida.

Como a autenticação usa cookies, requisições mutáveis também exigem proteção
CSRF. `GET /api/v1/csrf` inicializa o cookie `XSRF-TOKEN`; o frontend envia seu
valor no header `X-XSRF-TOKEN`. Esse cookie é legível pelo frontend, enquanto o
cookie JWT não pode ser lido por JavaScript. O encerramento de sessão remove o
cookie do navegador, mas não revoga um token já copiado; este permanece válido
até expirar.

Registre mudanças arquiteturais relevantes em
[decisions.md](decisions.md).
