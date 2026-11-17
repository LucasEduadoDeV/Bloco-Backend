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
  persistência. `users` armazena TMB (`basalMetabolicRateKcal`, kcal/dia), gasto
  diário estimado para manutenção (`estimatedDailyEnergyExpenditureKcal`,
  kcal/dia) e meta diária de água (`waterConsumptionGoalMl`, ml/dia). A TMB usa
  Mifflin–St Jeor e o gasto diário aplica um fator de atividade; nenhum dos dois
  define déficit ou superávit calórico.
  O perfil também contém idade, gênero (`FEMALE`/`MALE`), altura em
  centímetros, peso em quilogramas e nível de atividade (`ActivityLevel`):
  `1` sedentário, `2` levemente ativo, `3` moderadamente ativo, `4` muito
  ativo e `5` extremamente ativo. Esses dados ficam sem valor até serem
  configurados.
- `daily_intakes` guarda um registro por usuário e data, com calorias (kcal) e
  água (ml) consumidas; sem registro para o dia, o `/me` retorna `0` para ambos.
- `user/controller`, `user/service` e `user/dto`: atualização do perfil em
  `PUT /api/v1/me/profile`. Altura e peso recebidos em cm/pés-polegadas e
  kg/lb são normalizados e persistidos como cm e kg. O cálculo da TMB é
  isolado em `BasalMetabolicRateCalculator`; `DailyEnergyExpenditureCalculator`
  aplica o fator correspondente ao nível de atividade.
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
