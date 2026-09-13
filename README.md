# PetCare 360 (CLYVO VET) — Sprint 3

**Challenge FIAP 2026 — Java Advanced**  
**Empresa Parceira:** CLYVO VET  
**Turma:** 2TDSPW  
**Professor:** Leonardo Real (`lgsreal`)

---

## Integrantes da Equipe

| Nome Completo | RM | Turma | Papel / Responsabilidade Principal na Sprint 3 |
|---|---|---|---|
| **João Vitor Lacerda** | 565565 | 2TDSPW | Arquitetura de Segurança Spring Security 6 RBAC, Sessão Web e Suíte de Testes |
| **Kauan Vieira de Lima** | 565403 | 2TDSPW | Fluxo Não-CRUD 1 (Triagem Preventiva e Agendamento) e Regras de Negócio |
| **Murillo Fernandes Carapia** | 564969 | 2TDSPW | Fluxo Não-CRUD 2 (Fila de Atendimento Clínico Integrado) e Controllers |
| **Pedro de Matos Previtali** | 564184 | 2TDSPW | Migrações Flyway (DDL V1 / DML V2), Modelagem e Telas Thymeleaf / Bootstrap |

---

## 1. Visão Geral do Projeto

O **PetCare 360** é uma plataforma desenvolvida para a clínica e rede parceira **CLYVO VET**, focada na **gestão contínua e saúde preventiva de animais domésticos**. Diferente de um sistema puramente cadastral (CRUD básico), o PetCare 360 conecta tutores, clínicas e veterinários em um ecossistema integrado:
- Acompanhamento histórico completo do pet (prontuário clínico unificado).
- Triagem inteligente de consultas com detecção automática de pendências sanitárias (vacinas atrasadas e medicamentos em curso).
- Fila clínica em tempo real para atendimento veterinário com registro integrado de consultas, vacinação, prescrição medicamentosa e atualização biométrica de peso.
- Emissão proativa de alertas preventivos aos tutores para evitar perda de prazos de imunização e reforços.

---

## 2. Tecnologias Utilizadas na Sprint 3

- **Java 21 LTS** (Records, Pattern Matching, Type Inference, Stream API).
- **Spring Boot 3.3.5** com Gradle Wrapper (`gradlew`).
- **Spring MVC + Thymeleaf**: Server-Side Rendering (SSR) dinâmico, layouts modulares com fragments (`header.html`, `footer.html`, `alerts.html`), mensagens de validação e feedback com flash attributes.
- **Thymeleaf Extras Spring Security 6**: Renderização condicional de menus, botões e ações no frontend conforme as roles do usuário logado (`sec:authorize`).
- **Bootstrap 5.3.3 + Bootstrap Icons (WebJars)**: Interface visual responsiva, limpa e moderna.
- **Flyway Database Migration**: Controle de versão do banco relacional com validação estrita (`V1__create_tables.sql` e `V2__insert_initial_data.sql`).
- **Spring Security 6 (RBAC)**: Autenticação via formulário (`/login`), proteção CSRF, gerenciamento de sessão, controle de acesso baseado em papéis (`ADMIN`, `VETERINARIO`, `USER`/TUTOR) e página de erro 403 customizada.
- **Spring Data JPA & Hibernate**: Persistência relacional com `hibernate.ddl-auto: validate` garantindo fidelidade absoluta às migrações do Flyway.
- **H2 In-Memory Database**: Execução ágil para testes e apresentação sem dependências externas.
- **Bean Validation (Hibernate Validator)**: Validação de formulários no backend com feedback visual inline nos templates.
- **JUnit 5 & MockMvc**: Suíte de testes automatizados cobrindo migrações, fluxos de segurança e regras transacionais não-CRUD.

---

## 3. Requisitos da Sprint 3 Atendidos (100 Pontos)

### A. Frontend com Thymeleaf e Bootstrap (30 Pontos)
- Interface visual completa, intuitiva e responsiva para todas as entidades e fluxos.
- Navegação fluida e coerente, sem links quebrados ou telas inacabadas.
- Fragmentos reutilizáveis para cabeçalho, rodapé com dados dos 4 integrantes e container global de alertas/notificações (`alerts.html`).
- Formulários com validação visual: campos com erro destacados em vermelho e mensagens claras de validação (`th:errors`).
- Modais e botões de confirmação para ações destrutivas (exclusões) e botões de transição de estado.
- Páginas de erro amigáveis para HTTP 403 (Acesso Negado), 404 (Não Encontrado) e 500 (Erro Interno).

### B. Controle de Versão com Flyway (20 Pontos)
- Localização padronizada: `src/main/resources/db/migration/`.
- `V1__create_tables.sql`: Script DDL idempotente criando as 9 tabelas relacionais (`TB_USUARIO`, `TB_TUTOR`, `TB_CLINICA`, `TB_PET`, `TB_CONSULTA`, `TB_VACINA`, `TB_MEDICAMENTO`, `TB_AGENDAMENTO`, `TB_ALERTA`), definindo chaves primárias UUID, índices e integridade referencial (FKs).
- `V2__insert_initial_data.sql`: Script DML que inicializa a aplicação com usuários pré-cadastrados (senhas codificadas com algoritmo BCrypt), clínicas, tutores, pets com prontuários, consultas anteriores e agendamentos pendentes.
- Configurado com `spring.jpa.hibernate.ddl-auto: validate`, assegurando que o Hibernate valide o banco gerado exclusivamente pelo Flyway.

### C. Spring Security e Autenticação/Autorização RBAC (30 Pontos)
- Formulário de login customizado em `/login` estilizado com Bootstrap e proteção contra ataques CSRF.
- Três perfis de usuário bem definidos (Role-Based Access Control):
  1. **ADMIN**: Acesso total (gestão de clínicas, tutores, pets, agendamentos e alertas).
  2. **VETERINARIO**: Acesso ao Dashboard, Prontuário dos Pets, Fila de Atendimento Clínico e execução de consultas com prescrição e vacinas.
  3. **USER (Tutor)**: Acesso ao Dashboard, Meus Pets, solicitação de Agendamentos com Triagem Preventiva e Meus Alertas.
- Rotas restritas protegidas por `.hasRole(...)` e `.hasAnyRole(...)`.
- Usuários sem permissão recebem redirecionamento transparente para `/acesso-negado` com status HTTP 403 Forbidden estilizado.

### D. Dois Fluxos Não-CRUD Completos e Transacionais (20 Pontos)
1. **Fluxo 1 — Triagem Preventiva e Agendamento Inteligente (`TriagemAgendamentoService`)**:
   - O tutor/atendente escolhe o pet, clínica, data/hora e tipo de serviço.
   - O sistema valida atomicamente a disponibilidade da clínica para evitar conflito de horários duplicados.
   - Realiza uma **varredura clínica automática no histórico do pet**:
     - Detecta se há vacinas vencidas ou com vencimento nos próximos 30 dias.
     - Identifica se o pet faz uso de medicamentos ativos de uso contínuo que necessitam de avaliação prévia.
   - Emite automaticamente **alertas preventivos** vinculados ao tutor no prontuário do pet.
   - Cria o agendamento no estado `PENDENTE` ou `CONFIRMADO` e redireciona para a tela de **Comprovante de Agendamento com Resumo da Triagem Preventiva**.
2. **Fluxo 2 — Atendimento Clínico Completo Integrado (`AtendimentoClinicoService`)**:
   - O veterinário visualiza a **Fila de Espera** de agendamentos do dia (`/atendimento/fila`).
   - Ao iniciar o atendimento (`/atendimento/{id}/iniciar`), tem acesso aos dados biométricos do pet e histórico médico.
   - No mesmo formulário transacional (`@Transactional`), o veterinário:
     - Registra os dados da consulta (motivo, diagnóstico clínico e valor cobrado).
     - Atualiza o **peso corporal do pet** em seu prontuário cadastral.
     - Opcionalmente aplica uma vacina (nome, lote) e o sistema calcula automaticamente a data da **próxima dose (+365 dias)**.
     - Opcionalmente prescreve medicamentos (nome, dosagem, frequência e duração em dias).
     - Conclui o agendamento atualizando seu status para `REALIZADO`.
     - Emite um alerta de sistema ao tutor avisando sobre a conclusão e eventuais cuidados prescritos.

---

## 4. Credenciais de Acesso ao Sistema (Login)

Todos os usuários já vêm inseridos pelo Flyway na migração `V2` com senhas criptografadas via **BCrypt**:

| Usuário | Senha | Perfil (Role) | O que pode acessar no sistema |
|---|---|---|---|
| **`admin`** | `admin123` | **ADMIN** | Acesso completo a todas as áreas, incluindo cadastro de novas clínicas e exclusões |
| **`vet`** | `vet123` | **VETERINARIO** | Dashboard, Prontuário de Pets, Fila de Espera e Atendimento Clínico Integrado |
| **`user`** | `user123` | **USER (Tutor)** | Dashboard, Meus Pets, Novo Agendamento com Triagem Preventiva e Alertas |

---

## 5. Mapa de Rotas e Telas da Aplicação Web

| Rota Web | Método | Perfil Mínimo | Descrição da Tela / Funcionalidade |
|---|---|---|---|
| `/login` | GET | Público | Tela de login visual com Bootstrap e mensagens de erro |
| `/login` | POST | Público | Processamento da autenticação via Spring Security com CSRF |
| `/logout` | GET/POST | Autenticado | Encerramento seguro da sessão |
| `/acesso-negado` | GET | Autenticado | Tela de erro HTTP 403 personalizada para perfis sem permissão |
| `/` ou `/dashboard` | GET | Todos | Painel principal com métricas, resumo da fila e alertas recentes |
| `/pets` | GET | Todos | Listagem de pets com filtros, busca por nome e paginação |
| `/pets/novo` | GET/POST | ADMIN, VET | Formulário de cadastro de novo pet com validação de campos |
| `/pets/{id}` | GET | Todos | **Prontuário completo**: histórico de consultas, vacinas e medicamentos |
| `/pets/{id}/excluir` | POST | ADMIN | Exclusão com confirmação e integridade referencial |
| `/tutores` | GET | Todos | Listagem paginada de tutores e seus dados de contato |
| `/tutores/novo` | GET/POST | ADMIN | Cadastro de tutor com validação de CPF e e-mail único |
| `/tutores/{id}` | GET | Todos | Detalhes do tutor e relação de seus animais vinculados |
| `/clinicas` | GET | Todos | Listagem de clínicas credenciadas CLYVO VET |
| `/clinicas/nova` | GET/POST | ADMIN | Cadastro de nova clínica (exclusivo ADMIN — teste de 403 com tutor) |
| `/agendamentos` | GET | Todos | Listagem geral de agendamentos com status e filtros |
| `/agendamentos/novo` | GET | Todos | **Fluxo 1**: Formulário de agendamento com dados para triagem preventiva |
| `/agendamentos/triagem`| POST | Todos | Processamento da triagem, validação de conflito e emissão de alertas |
| `/agendamentos/{id}/comprovante` | GET | Todos | Visualização do comprovante de agendamento e alertas preventivos |
| `/agendamentos/{id}/cancelar` | POST | Todos | Cancelamento de agendamento |
| `/atendimento/fila` | GET | ADMIN, VET | **Fluxo 2**: Fila de espera clínica de agendamentos confirmados/pendentes |
| `/atendimento/{id}/iniciar` | GET | ADMIN, VET | Tela de execução clínica: diagnóstico, peso, vacina e medicamentos |
| `/atendimento/salvar` | POST | ADMIN, VET | Gravação transacional completa do atendimento e atualização de status |
| `/alertas` | GET | Todos | Central de notificações e alertas preventivos dos pets |
| `/alertas/{id}/marcar-lido` | POST | Todos | Marcação rápida de alerta lido |
| `/swagger-ui.html` | GET | Público | Documentação OpenAPI / Swagger da API REST legada |
| `/h2-console` | GET | ADMIN | Console de administração do banco H2 em memória |

---

## 6. Como Executar o Projeto

### Pré-requisitos
- **Java Development Kit (JDK) 21** instalado e configurado no `PATH` (`java -version`).
- Não é necessário instalar o Gradle, pois o projeto utiliza o **Gradle Wrapper** embutido.

### Passo 1: Clonar o Repositório
```bash
git clone https://github.com/joaolacerdaconsorte/challenge-sprint-3.git
cd challenge-sprint-3
```

### Passo 2: Executar a Aplicação

**No Windows (PowerShell ou CMD):**
```powershell
.\gradlew.bat bootRun
```

**No Linux ou macOS:**
```bash
./gradlew bootRun
```

A aplicação será iniciada na porta **8080**.

### Passo 3: Acessar pelo Navegador
- **Página de Login:** [http://localhost:8080/login](http://localhost:8080/login)
- **Dashboard:** [http://localhost:8080/dashboard](http://localhost:8080/dashboard)
- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **H2 Database Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)  
  *(JDBC URL: `jdbc:h2:mem:pet360` | Usuário: `sa` | Senha: em branco)*

### Passo 4: Executar a Suíte de Testes Automatizados
```powershell
.\gradlew.bat test
```
Todos os testes unitários e integrados (Flyway, Security RBAC, Fluxo 1 e Fluxo 2) serão executados e validados.

---

## 7. Roteiro de Apresentação em Vídeo (10 Minutos)

Para a gravação do vídeo de entrega de até 10 minutos exigido pelo professor Leonardo Real, a equipe deve seguir este roteiro cronometrado e dividido entre todos os 4 integrantes:

```
[00:00 - 01:30] Abertura e Visão de Negócio — João Vitor e Pedro Previtali
  - Apresentação dos 4 integrantes: João Vitor, Kauan, Murillo e Pedro Previtali.
  - Apresentação da proposta CLYVO VET / PetCare 360 e da evolução da Sprint 1 para a Sprint 3.
  - Destaque para os 4 pilares: Thymeleaf SSR, Flyway Migrations, Spring Security RBAC e Fluxos Não-CRUD.

[01:30 - 03:30] Flyway Migrations e Banco de Dados — Pedro Previtali
  - Demonstração da pasta db/migration com V1__create_tables.sql e V2__insert_initial_data.sql.
  - Explicação do ddl-auto: validate no application.yml provando que o Flyway governa a estrutura.
  - Acesso ao H2 Console exibindo as 9 tabelas criadas e os dados iniciais com senhas em BCrypt.

[03:30 - 05:30] Spring Security e Controle de Acesso (RBAC) — João Vitor Lacerda
  - Demonstração da tela de /login e do formulário com proteção CSRF.
  - Login como USER ('user'/'user123'): visualização do menu restrito aos tutores.
  - Tentativa intencional de acessar /clinicas/nova como USER: exibição da tela customizada /acesso-negado (HTTP 403).
  - Logout e login como ADMIN ('admin'/'admin123') e VETERINARIO ('vet'/'vet123') mostrando o controle de rotas dinâmico.

[05:30 - 07:30] Fluxo Não-CRUD 1: Triagem e Agendamento Preventivo — Kauan Vieira
  - Navegação até /agendamentos/novo.
  - Seleção de um pet com pendências sanitárias (ex: Rex, com vacina antirrábica vencida).
  - Submissão do agendamento: demonstração do algoritmo de triagem detectando a vacina vencida.
  - Exibição do Comprovante de Agendamento (/agendamentos/{id}/comprovante) com a emissão automática do alerta preventivo.

[07:30 - 09:00] Fluxo Não-CRUD 2: Atendimento Clínico Integrado — Murillo Carapia
  - Acesso com o usuário VETERINARIO à Fila de Atendimento (/atendimento/fila).
  - Início do atendimento médico (/atendimento/{id}/iniciar).
  - Preenchimento simultâneo: diagnóstico, nova pesagem do pet, aplicação de vacina (com cálculo de próxima dose) e prescrição de remédio.
  - Gravação transacional: exibição da conclusão do agendamento (REALIZADO) e do prontuário atualizado em /pets/{id}.

[09:00 - 10:00] Conclusão, Suíte de Testes e Boas Práticas — Toda a Equipe
  - Execução ao vivo do comando '.\gradlew test' no terminal exibindo 'BUILD SUCCESSFUL'.
  - Considerações finais sobre Clean Code, SOLID, ausência de código comentado e satisfação total dos critérios de avaliação.
```

---

## 8. Guia de Preparação para a Arguição Individual

Durante a avaliação individual com o professor Leonardo Real, qualquer integrante pode ser arguido sobre qualquer parte do projeto. Abaixo estão as perguntas-chave e respostas técnicas preparadas:

### Perguntas sobre Flyway e Banco de Dados (Foco: Pedro de Matos Previtali)
1. **P: Por que usamos `hibernate.ddl-auto: validate` em vez de `update` ou `create-drop`?**  
   *R: Em ambiente profissional com Flyway, o controle de schema pertence exclusivamente às migrações versionadas (V1, V2). O `validate` garante que o Hibernate apenas valide se as entidades Java estão estritamente compatíveis com as tabelas criadas pelo Flyway, impedindo que o ORM altere o banco por conta própria.*
2. **P: O que acontece se alguém alterar o arquivo `V1__create_tables.sql` depois que ele já rodou?**  
   *R: O Flyway armazena o checksum de cada script executado na tabela `flyway_schema_history`. Se o arquivo for alterado, o checksum não baterá na inicialização e o Spring Boot interromperá o startup com erro de integridade.*

### Perguntas sobre Spring Security e RBAC (Foco: João Vitor Lacerda)
1. **P: Como as senhas dos usuários foram armazenadas no banco e como o Spring Security as valida?**  
   *R: As senhas foram geradas com o algoritmo de hash unidirecional BCrypt (salt embutido) e salvas na coluna `PASSWORD_HASH`. Na classe `SecurityConfig`, configuramos o `BCryptPasswordEncoder`. Durante a autenticação, o Spring compara o texto plano digitado no formulário com o hash via `passwordEncoder.matches()`.*
2. **P: Como foi tratada a tentativa de acesso a uma rota não autorizada?**  
   *R: O `SecurityFilterChain` intercepta as requisições via `.hasRole("ADMIN")` ou `.hasAnyRole(...)`. Quando um usuário autenticado tenta acessar uma rota acima do seu privilégio, a exceção `AccessDeniedException` é capturada e redirecionada pelo `.accessDeniedPage("/acesso-negado")`, renderizando um template Thymeleaf com código HTTP 403.*

### Perguntas sobre o Fluxo Não-CRUD 1: Triagem Preventiva (Foco: Kauan Vieira de Lima)
1. **P: Por que o fluxo de agendamento não é apenas um CRUD de inserção?**  
   *R: Porque ele encapsula regras de negócio ricas: valida atomicamente se a clínica tem sobreposição de horário (`existsByClinicaIdAndDataAgendamentoAndStatusAgendamentoNot`), faz uma varredura preventiva nas vacinas do pet procurando vencidas ou prestes a vencer (`findVacinasVencidasOuProximas`) e medicamentos ativos, e gera de forma autônoma um alerta clínico para o tutor no prontuário.*
2. **P: Onde os alertas gerados na triagem ficam armazenados?**  
   *R: Na tabela `TB_ALERTA`, vinculados tanto ao ID do Pet quanto ao ID do Tutor, permitindo que apareçam no Dashboard, na Central de Alertas e no Prontuário do animal.*

### Perguntas sobre o Fluxo Não-CRUD 2: Atendimento Clínico (Foco: Murillo Fernandes Carapia)
1. **P: Como garantimos que o registro da consulta, da vacina, do medicamento e o peso do pet ocorram de forma atômica?**  
   *R: O método `registrarAtendimento` na classe `AtendimentoClinicoService` é anotado com `@Transactional`. Se ocorrer qualquer erro durante a prescrição do medicamento ou atualização do peso, toda a transação sofre rollback, evitando inconsistência de dados no prontuário.*
2. **P: Como é calculada a data da próxima dose de vacina?**  
   *R: No service, quando uma vacina é informada pelo veterinário, a data da próxima aplicação é calculada automaticamente através do método `LocalDate.now().plusDays(365)` (reforço anual padrão) e persistida na entidade `Vacina`.*

---

## 9. Estrutura de Diretórios do Projeto

```text
challenge-sprint-3/
├── build.gradle                               # Dependências: Thymeleaf, Security, Flyway, Bootstrap
├── settings.gradle
├── gradlew / gradlew.bat                      # Gradle Wrapper v8.10.2
├── README.md                                  # Documentação completa da Sprint 3
├── documentos/                                # Documentação auxiliar do Challenge
│   ├── DER.md                                 # Diagrama Entidade-Relacionamento
│   ├── diagrama-classes.md                    # Diagrama de Classes UML das entidades
│   ├── arquitetura.md                         # Descrição das camadas e padrões arquiteturais
│   ├── cronograma.md                          # Divisão de tarefas dos 4 integrantes
│   └── pet360.postman_collection.json         # Coleção Postman da API
└── src/
    ├── main/
    │   ├── java/br/com/fiap/pet360/
    │   │   ├── Pet360Application.java
    │   │   ├── config/                        # Configurações (WebMvc, Cache, OpenAPI)
    │   │   ├── controller/                    # Controllers REST legados (API)
    │   │   ├── controller/web/                # Web Controllers (Thymeleaf MVC)
    │   │   │   ├── LoginWebController.java
    │   │   │   ├── DashboardWebController.java
    │   │   │   ├── PetWebController.java
    │   │   │   ├── TutorWebController.java
    │   │   │   ├── ClinicaWebController.java
    │   │   │   ├── AgendamentoWebController.java
    │   │   │   ├── AtendimentoWebController.java
    │   │   │   └── AlertaWebController.java
    │   │   ├── dto/                           # DTOs records e formulários web
    │   │   │   ├── AtendimentoClinicoForm.java
    │   │   │   ├── TriagemAgendamentoRequest.java
    │   │   │   └── TriagemAgendamentoResult.java
    │   │   ├── model/                         # Entidades JPA de domínio (9 tabelas)
    │   │   ├── repository/                    # Spring Data JPA Repositories
    │   │   ├── security/                      # SecurityConfig, UserDetailsServiceImpl, Roles
    │   │   └── service/                       # Services transacionais e fluxos de negócio
    │   │       ├── TriagemAgendamentoService.java
    │   │       └── AtendimentoClinicoService.java
    │   └── resources/
    │       ├── application.yml                # Configuração do H2, Flyway e Thymeleaf
    │       ├── db/migration/
    │       │   ├── V1__create_tables.sql       # Flyway DDL (9 tabelas)
    │       │   └── V2__insert_initial_data.sql # Flyway DML (Seed inicial com BCrypt)
    │       └── templates/                     # Páginas Thymeleaf SSR
    │           ├── fragments/                 # header.html, footer.html, alerts.html
    │           ├── login.html
    │           ├── dashboard.html
    │           ├── error/                     # 403.html, 404.html, 500.html
    │           ├── pets/                      # lista.html, formulario.html, detalhes.html
    │           ├── tutores/                   # lista.html, formulario.html, detalhes.html
    │           ├── clinicas/                  # lista.html, formulario.html
    │           ├── agendamentos/              # lista.html, novo.html, comprovante.html
    │           ├── atendimento/               # fila.html, executar.html
    │           └── alertas/                   # lista.html
    └── test/
        └── java/br/com/fiap/pet360/
            └── PetCare360Sprint3ApplicationTests.java # Suíte completa de testes JUnit 5
```

---

## 10. Conclusão e Conformidade com os Critérios do Challenge

O projeto foi inteiramente construído observando os rigores técnicos solicitados na disciplina:
- **Clean Code & Boas Práticas**: Métodos concisos, nomes semânticos em português/inglês conforme a base original, injeção por construtor (`@Autowired` via construtor), separação estrita de camadas (Model-View-Controller-Service-Repository).
- **Sem Códigos Comentados**: Nenhum bloco morto de código deixado nos arquivos.
- **Robustez nos Fluxos**: Tratamento de exceções, validação em todas as entradas de dados e atomicidade transacional com `@Transactional`.
- **Equipe 100% Integrada**: Divisão clara de tarefas contemplando todos os 4 alunos, refletida no código, no rodapé das telas e na documentação.
