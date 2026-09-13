# Arquitetura — Pet360

> Challenge FIAP — Java Advanced 2026

Documento complementar que explica as camadas, o fluxo de uma requisição típica e as escolhas de design.

## Visão em camadas

```
┌──────────────────────────────────────────────────┐
│           CLIENTE (Swagger / Postman)             │
└────────────────────┬─────────────────────────────┘
                     │ HTTP + Bearer JWT
                     ▼
┌──────────────────────────────────────────────────┐
│  JwtAuthFilter  →  SecurityFilterChain            │
│  valida token, popula SecurityContext             │
└────────────────────┬─────────────────────────────┘
                     ▼
┌──────────────────────────────────────────────────┐
│  Controller (REST)                                │
│  - @RestController, @RequestMapping               │
│  - Bean Validation no body (@Valid)               │
│  - Pageable, Sort, RequestParam                   │
│  - HATEOAS: adiciona links nos Response DTOs      │
└────────────────────┬─────────────────────────────┘
                     ▼
┌──────────────────────────────────────────────────┐
│  Service                                          │
│  - @Transactional                                 │
│  - @Cacheable / @CacheEvict                       │
│  - Regras de negócio (histórico, alertas, etc)    │
│  - Validações que vão além do Bean Validation     │
└────────────────────┬─────────────────────────────┘
                     ▼
┌──────────────────────────────────────────────────┐
│  Repository (Spring Data JPA)                     │
│  - JpaRepository<E, UUID>                         │
│  - Query Methods + @Query JPQL                    │
└────────────────────┬─────────────────────────────┘
                     ▼
┌──────────────────────────────────────────────────┐
│  Hibernate ORM  →  H2 in-memory                   │
└──────────────────────────────────────────────────┘
```

## Fluxo de uma requisição: criar um Pet

```
POST /api/pets
Authorization: Bearer eyJhbGc...
Content-Type: application/json

{ "nome": "Rex", "especie": "CACHORRO", "tutorId": "...", "peso": 28.5 }
```

1. **JwtAuthFilter** intercepta a requisição, extrai o token do header `Authorization`, valida assinatura/expiração via `JwtService`, carrega o `UserDetails` do banco e popula o `SecurityContextHolder`.
2. **SecurityFilterChain** verifica que `/api/pets` exige autenticação — aprovado.
3. **PetController#criar** recebe um `PetRequest` (record).
   - `@Valid` dispara o Bean Validation. Se houver erro, `GlobalExceptionHandler` retorna 400 com lista de `fieldErrors`.
4. **PetService#criar** abre transação, busca o `Tutor` pelo ID (ou lança `ResourceNotFoundException` → 404), cria a entidade `Pet`, salva via `PetRepository`.
   - `@CacheEvict("pets", allEntries=true)` invalida o cache de listagens.
5. **Response DTO** é montado via `PetResponse.from(Pet)` e enriquecido com links HATEOAS (self, listar, tutor, histórico, vacinas, medicamentos).
6. Controller retorna **201 Created** + header `Location` apontando para `/api/pets/{id}`.

## Decisões de design

### Por que UUID e não Long sequence?
- Não vaza ordem de criação
- Suporta merge entre ambientes (dev/staging/prod) sem colisão
- Reflete o padrão moderno do GitHub do professor (lgsreal/api-rest-pw)

### Por que BigDecimal?
Campos monetários (`Consulta.valor`, `Medicamento.custo`) e contínuos (`Pet.peso`) usam `BigDecimal` por causa da imprecisão de ponto flutuante (`0.1 + 0.2 = 0.30000000000000004`).

### Por que records nos DTOs?
- Imutáveis por padrão
- Sem boilerplate (sem Lombok)
- Java 21 nativo
- Perfeitos para representar payloads de entrada/saída

### Por que HATEOAS?
O modelo de maturidade de Richardson nível 3 (último) exige hipermídia. Cada `Response DTO` herda de `RepresentationModel` e expõe links para navegação. Vale pontos no critério "RESTful" (até 15 pts no PDF).

### Por que cache em listagens?
Listagens de tutores/pets/consultas tendem a se repetir muito (Swagger UI, dashboard). `@Cacheable` reduz hits no banco; `@CacheEvict` em criações/updates garante consistência.

### Por que JPQL ao invés de Criteria/Specifications?
- Para os filtros compostos (consultas por status + clínica + datas), JPQL com parâmetros nullable (`:x IS NULL OR campo = :x`) é mais legível que Criteria/Specifications.
- Para esta entrega, os filtros por parametros ja cobrem as buscas usadas nos endpoints.

### Por que CommandLineRunner ao invés de data.sql?
- BCrypt do admin precisa ser gerado em runtime (`PasswordEncoder.encode("admin123")`)
- IDs UUID não precisam ser hardcoded
- Código Java de seed é mais legível e testável

## Padrões de projeto aplicados (com prudência)

| Padrão | Uso |
|---|---|
| **DTO** | Records `*Request`/`*Response`/`HistoricoPetItem` desacoplam contrato HTTP do modelo de domínio |
| **Repository** | `JpaRepository<E, UUID>` por entidade |
| **Service Layer** | `*Service` concentra regras de negócio e transação |
| **Mapper estático** | `PetResponse.from(Pet)`, evita biblioteca extra (MapStruct/ModelMapper) |
| **Filter** | `JwtAuthFilter` (filter chain pattern) |
| **Builder implícito** | Construtor com campos principais + setters; aceita customização sem builder verboso |
| **Strategy implícito** | `PetController#vacinasDoPet` escolhe entre `listarPorPet` vs `vencidasOuProximas` pelo flag |

## Segurança

- **Stateless**: nenhuma sessão HTTP, JWT carrega tudo
- **HS512** com chave Base64 de 256+ bits configurada em `application.yml`
- **BCryptPasswordEncoder** (cost default 10) para hash de senha
- **Filtro `OncePerRequestFilter`** posicionado antes de `UsernamePasswordAuthenticationFilter`
- **Public endpoints**: `/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`, `/h2-console/**`
- **CORS** liberado para qualquer origem em dev (revisar em produção)
- **CSRF** desabilitado (API stateless com JWT)
- **H2 console**: `frameOptions` desabilitado para permitir o iframe da console

## Tratamento de erros

Tabela de mapeamento de exceção → status HTTP em `GlobalExceptionHandler`:

| Exceção | HTTP | Quando |
|---|---|---|
| `ResourceNotFoundException` | 404 | ID inexistente |
| `BusinessException` | 400 | Regra de negócio violada (ex.: CPF duplicado) |
| `MethodArgumentNotValidException` | 400 | Bean Validation falhou — inclui `fieldErrors[]` |
| `DataIntegrityViolationException` | 409 | UNIQUE/FK violado no banco |
| `MethodArgumentTypeMismatchException` | 400 | Param URL com tipo errado (ex.: UUID inválido) |
| `HttpMessageNotReadableException` | 400 | JSON malformado |
| `BadCredentialsException` | 401 | Login inválido |
| `AccessDeniedException` | 403 | Token válido mas sem permissão |
| `Exception` (catch-all) | 500 | Erro inesperado |

Todos os erros retornam o mesmo shape (`ErrorResponse`): `timestamp`, `status`, `error`, `message`, `path`, `fieldErrors?`.
