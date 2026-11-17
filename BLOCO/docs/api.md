# API

Base URL local: `http://localhost:3000`.

## `POST /api/v1/register`

Cria uma conta de usuário comum.

Requisição:

```json
{
  "name": "Nome Sobrenome",
  "email": "pessoa@example.com",
  "password": "senha-com-8-ou-mais",
  "phone": "(11) 99999-9999"
}
```

`name`, `email` e `password` são obrigatórios. A senha deve ter no mínimo oito
caracteres. `phone` é opcional e pode ser omitido ou enviado como `null`.
`name` aceita até 255 caracteres, e `phone` até 50.
E-mails são removidos de espaços externos e normalizados para minúsculas antes
de serem armazenados.

Sucesso: `201 Created`, com o perfil público criado:

```json
{
  "id": "uuid",
  "name": "Nome Sobrenome",
  "email": "pessoa@example.com",
  "phone": "(11) 99999-9999"
}
```

A senha e seu hash nunca fazem parte da resposta. E-mail já cadastrado retorna
`409 Conflict` com o código `EMAIL_ALREADY_REGISTERED`. Entradas inválidas
retornam `400 Bad Request` com o código `VALIDATION_ERROR` e detalhes por campo.

## `POST /api/v1/login`

Verifica as credenciais de uma conta existente.

Requisição:

```json
{
  "email": "pessoa@example.com",
  "password": "senha-com-8-ou-mais"
}
```

Sucesso: `200 OK`, com o mesmo formato de perfil público do cadastro.
Credenciais incorretas retornam `401 Unauthorized` com o código
`INVALID_CREDENTIALS` e uma mensagem genérica, sem indicar se o e-mail existe.

No sucesso, a resposta inclui `Set-Cookie` com o JWT de acesso. O cookie chama-se
`BLOCO_AUTH`, tem `HttpOnly`, `SameSite=Lax`, caminho `/api` e validade de 15
minutos. `Secure` é habilitado por padrão e pode ser desabilitado em
desenvolvimento local usando `AUTH_COOKIE_SECURE=false`. O JWT não é incluído no
corpo da resposta.

## `GET /api/v1/csrf`

Inicializa a proteção CSRF e define o cookie `XSRF-TOKEN` (caminho `/`),
retornando `204 No Content`. O cliente deve ler o valor desse cookie e enviá-lo no header
`X-XSRF-TOKEN` nas requisições `POST`, `PUT`, `PATCH` e `DELETE`. O cookie CSRF
não é HttpOnly para que uma aplicação SPA possa usá-lo; o cookie de
autenticação permanece HttpOnly.

## `GET /api/v1/me`

Retorna o perfil público associado ao JWT válido do cookie `BLOCO_AUTH`.
Respostas sem token, com token inválido ou expirado retornam `401 Unauthorized`.
Os dados do perfil e as metas são lidos do usuário. Os campos
`dailyCalorieConsumptionKcal` e `dailyWaterConsumptionMl` representam os totais
consumidos na data atual, vindos do registro diário; sem registro, retornam `0`.
Exemplo:

```json
{
  "id": "uuid",
  "name": "Nome Sobrenome",
  "email": "pessoa@example.com",
  "phone": null,
  "age": 29,
  "gender": "FEMALE",
  "heightCm": 170.5,
  "weightKg": 62.4,
  "activityLevel": 3,
  "basalMetabolicRateKcal": 1650,
  "estimatedDailyEnergyExpenditureKcal": 2558,
  "dailyCalorieConsumptionKcal": 2100,
  "waterConsumptionGoalMl": 2500,
  "dailyWaterConsumptionMl": 900
}
```

## `PUT /api/v1/me/profile`

Atualiza os dados do perfil usados para a configuração do TMB. Exige JWT no
cookie `BLOCO_AUTH` e proteção CSRF (`XSRF-TOKEN` e `X-XSRF-TOKEN`). Todos os
campos são obrigatórios. Altura e peso aceitam unidades métricas ou imperiais;
o backend converte e armazena em centímetros e quilogramas.

Exemplo com unidades imperiais:

```json
{
  "gender": "FEMALE",
  "ageYears": 29,
  "height": {
    "unit": "FT_IN",
    "feet": 5,
    "inches": 7
  },
  "weight": {
    "unit": "LB",
    "value": 150
  },
  "activityLevel": 3
}
```

Para unidades métricas, informe `height: {"unit":"CM","value":170.18}` e
`weight: {"unit":"KG","value":68.04}`. Não envie campos de outra unidade no
mesmo objeto de medida.

`gender` aceita `FEMALE` ou `MALE`; `activityLevel` aceita os códigos `1` a `5`
(sedentário, levemente ativo, moderadamente ativo, muito ativo e extremamente
ativo). A idade deve estar entre 1 e 120 anos, a altura entre 50 e 250 cm, e o
peso entre 20 e 300 kg após conversão.

Em sucesso, retorna `200 OK` com o perfil normalizado:

```json
{
  "id": "uuid",
  "gender": "FEMALE",
  "ageYears": 29,
  "heightCm": 170.18,
  "weightKg": 68.04,
  "activityLevel": 3,
  "basalMetabolicRateKcal": 1438,
  "estimatedDailyEnergyExpenditureKcal": 2229
}
```

Entradas inválidas retornam `400 Bad Request` no formato de erro padrão da API.
Ao salvar o perfil, o endpoint calcula e salva `basalMetabolicRateKcal` pela fórmula Mifflin–St Jeor:
`10 × peso(kg) + 6,25 × altura(cm) − 5 × idade(anos) + 5` para `MALE` e a mesma
fórmula com `−161` para `FEMALE`, arredondando o resultado para o inteiro mais
próximo. Também calcula `estimatedDailyEnergyExpenditureKcal` como TMB
multiplicada pelo fator de manutenção do nível: `1.20`, `1.375`, `1.55`,
`1.725` ou `1.90`, respectivamente, arredondando para kcal inteiras. Esse valor
é uma estimativa de gasto para manutenção, não é uma meta de consumo e não
inclui déficit ou superávit. O `GET /api/v1/me` reflete os dados calculados.

## `POST /api/v1/logout`

Remove o cookie JWT do navegador e retorna `204 No Content`. A chamada exige o
header CSRF. Como os JWTs são stateless, logout não revoga uma cópia do token;
ela continua válida até expirar.

## Erros

Os erros seguem o formato:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Verifique os campos enviados.",
  "fields": {
    "email": "Informe um e-mail válido."
  }
}
```

## Notas de segurança

- Senhas são armazenadas com hash usando o `PasswordEncoder` do Spring Security.
- O segredo HS256 é configurado por `JWT_SECRET_BASE64` e deve decodificar para
  pelo menos 32 bytes aleatórios. Gere um segredo com `openssl rand -base64 32`;
  não o versione.
- `Secure` fica ligado por padrão. Em produção, use HTTPS e mantenha essa opção
  ligada; para HTTP local, configure `AUTH_COOKIE_SECURE=false`.
- A validade é configurável por `JWT_ACCESS_TOKEN_TTL` (padrão `PT15M`). Não há
  refresh token; após expirar, o usuário precisa fazer login novamente.
