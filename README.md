# Conta em Dia

Aplicativo Android nativo para organizacao local de contas, boletos, vencimentos e pagamentos pessoais.

O app nao possui login, integracao bancaria, Open Finance, DDA, nuvem ou APIs externas para armazenar contas. Os dados cadastrados pelo usuario ficam salvos localmente no dispositivo via Room. A monetizacao atual usa banners do Google AdMob.

## Funcionalidades

- Cadastro manual de contas e boletos.
- Edicao e exclusao de contas.
- Marcacao de conta como paga ou em aberto.
- Dashboard mensal com totais de:
  - total do mes;
  - total pago;
  - total em aberto;
  - total atrasado;
  - total com vencimento nos proximos 7 dias.
- Lista de contas ordenada e agrupada por prioridade visual.
- Filtros por status e categoria.
- Ordenacao por vencimento, maior valor e menor valor.
- Tela de detalhe da conta.
- Tela de resumo mensal com agrupamento por categoria e maiores contas do mes.
- Recorrencia mensal simples ao marcar uma conta recorrente como paga.
- Notificacoes locais de vencimento com WorkManager:
  - 3 dias antes;
  - no dia do vencimento.
- Anuncios com Google AdMob:
  - banners no dashboard;
  - banners no resumo mensal;
  - banner no detalhe da conta.
- Politica de privacidade em `docs/privacy-policy.html`.
- Assets de loja em `store-assets/`.

## Stack

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- MVVM com eventos de UI em estilo MVI
- Room
- Koin
- Kotlin Coroutines
- Flow e StateFlow
- Java Time API
- WorkManager
- Google Mobile Ads SDK
- Google User Messaging Platform
- Gradle Kotlin DSL

## Arquitetura

O projeto esta organizado em camadas:

- `core`: utilitarios de data, dinheiro e mensagens.
- `data`: Room, DAO, entidades, conversores, mappers e implementacao do repositorio.
- `domain`: modelos, contrato de repositorio e casos de uso.
- `presentation`: telas Compose, ViewModels, navegacao, componentes e anuncios.
- `notification`: agendamento e execucao dos lembretes locais.
- `di`: modulos do Koin.

Principais telas:

- `DashboardScreen`
- `BillFormScreen`
- `BillDetailScreen`
- `SummaryScreen`

## Modelo principal

A conta/boleto possui campos como:

- titulo;
- valor em centavos;
- data de vencimento;
- categoria;
- status;
- recorrencia;
- observacoes;
- data de pagamento;
- datas de criacao e atualizacao.

Categorias disponiveis:

- Casa
- Agua
- Luz
- Internet
- Cartao
- Aluguel
- Assinatura
- Mercado
- Saude
- Educacao
- Outros

## Como rodar

Abra o projeto no Android Studio e execute a variante `debug`.

Pela linha de comando:

```powershell
.\gradlew.bat assembleDebug
```

Para rodar testes unitarios:

```powershell
.\gradlew.bat testDebugUnitTest
```

Para compilar release:

```powershell
.\gradlew.bat assembleRelease
```

## Assinatura release

O projeto le a configuracao de assinatura a partir do `local.properties`.

Exemplo:

```properties
CONTAS_EM_DIA_RELEASE_STORE_FILE=conta-em-dia-release.jks
CONTAS_EM_DIA_RELEASE_STORE_PASSWORD=sua_senha
CONTAS_EM_DIA_RELEASE_KEY_ALIAS=conta-em-dia
CONTAS_EM_DIA_RELEASE_KEY_PASSWORD=sua_senha
```

O arquivo `local.properties` e arquivos `.jks` nao devem ser versionados.

## AdMob

O App ID do AdMob e lido via propriedade Gradle:

```properties
ADMOB_APP_ID=ca-app-pub-5315870199108015~6437857355
```

Os banners usam IDs de teste no build `debug` e IDs reais no build `release`, configurados em `app/build.gradle.kts`.

Logs de carregamento dos anuncios podem ser filtrados no Logcat por:

```text
ContaEmDiaAds
```

## Politica de privacidade

A politica de privacidade esta em:

```text
docs/privacy-policy.html
```

Para publicar via GitHub Pages, configure o repositorio para servir a pasta `/docs`.

URL esperada:

```text
https://bianchiniii.github.io/duetoday/privacy-policy.html
```

## Assets de loja

Os assets gerados ficam em:

```text
store-assets/
```

Arquivos existentes:

- `icon-512.png`
- `icon-114.png`
- `promotional-1024x500.png`
- `tablet-dashboard-1280x800.png`
- `tablet-summary-1280x800.png`
- `tablet-form-1280x800.png`

## Observacoes

- O app e 100% local para dados de contas e boletos.
- O app utiliza AdMob para anuncios.
- O app solicita permissao de notificacao em Android 13+.
- A entrega de anuncios reais depende da aprovacao/configuracao da conta AdMob.
