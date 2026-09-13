# Diagrama de Entidade-Relacionamento (DER) — Pet360

> Challenge FIAP — Java Advanced 2026 — CLYVO VET / Pet360
> Vale **até 10 pontos** (em conjunto com o diagrama de classes).

Este DER reflete fielmente as 9 tabelas físicas criadas pelo Hibernate a partir das entidades JPA do projeto. Cores de coluna seguem o padrão do DDL Oracle original (`Challenge 1.sql`).

## Diagrama (Mermaid)

```mermaid
erDiagram
    TB_TUTOR ||--o{ TB_PET : possui
    TB_TUTOR ||--o{ TB_ALERTA : recebe
    TB_CLINICA ||--o{ TB_CONSULTA : realiza
    TB_CLINICA ||--o{ TB_AGENDAMENTO : agenda
    TB_PET ||--o{ TB_CONSULTA : tem
    TB_PET ||--o{ TB_VACINA : recebe
    TB_PET ||--o{ TB_MEDICAMENTO : usa
    TB_PET ||--o{ TB_AGENDAMENTO : possui
    TB_PET ||--o{ TB_ALERTA : gera
    TB_CONSULTA ||--o{ TB_VACINA : aplica
    TB_CONSULTA ||--o{ TB_MEDICAMENTO : prescreve

    TB_TUTOR {
        UUID id_tutor PK
        VARCHAR nm_tutor "NOT NULL"
        VARCHAR cpf "NOT NULL UNIQUE"
        VARCHAR email "NOT NULL UNIQUE"
        VARCHAR telefone
        VARCHAR endereco
    }

    TB_CLINICA {
        UUID id_clinica PK
        VARCHAR nm_clinica "NOT NULL"
        VARCHAR cnpj "NOT NULL UNIQUE"
        VARCHAR email
        VARCHAR telefone
        VARCHAR endereco
    }

    TB_PET {
        UUID id_pet PK
        VARCHAR nm_pet "NOT NULL"
        VARCHAR especie "NOT NULL (enum)"
        VARCHAR raca
        DATE dt_nascimento
        NUMERIC peso "5,2"
        VARCHAR observacoes
        UUID id_tutor FK "NOT NULL"
    }

    TB_CONSULTA {
        UUID id_consulta PK
        TIMESTAMP dt_consulta "NOT NULL"
        VARCHAR status_consulta "NOT NULL (enum)"
        VARCHAR motivo
        VARCHAR diagnostico
        NUMERIC valor "10,2"
        UUID id_pet FK "NOT NULL"
        UUID id_clinica FK "NOT NULL"
    }

    TB_VACINA {
        UUID id_vacina PK
        VARCHAR nm_vacina "NOT NULL"
        DATE dt_aplicacao "NOT NULL"
        DATE dt_proxima
        VARCHAR lote
        UUID id_pet FK "NOT NULL"
        UUID id_consulta FK "NULLABLE"
    }

    TB_MEDICAMENTO {
        UUID id_medicamento PK
        VARCHAR nm_medicamento "NOT NULL"
        VARCHAR dosagem
        VARCHAR frequencia
        DATE dt_inicio "NOT NULL"
        DATE dt_fim
        BOOLEAN ativo "NOT NULL DEFAULT TRUE"
        NUMERIC custo "10,2"
        UUID id_pet FK "NOT NULL"
        UUID id_consulta FK "NULLABLE"
    }

    TB_AGENDAMENTO {
        UUID id_agendamento PK
        TIMESTAMP dt_agendamento "NOT NULL"
        VARCHAR tipo "NOT NULL (enum)"
        VARCHAR status_agendamento "NOT NULL (enum)"
        VARCHAR observacoes
        UUID id_pet FK "NOT NULL"
        UUID id_clinica FK "NOT NULL"
    }

    TB_ALERTA {
        UUID id_alerta PK
        VARCHAR tipo_alerta "NOT NULL (enum)"
        VARCHAR descricao
        TIMESTAMP dt_alerta "NOT NULL"
        BOOLEAN lido "NOT NULL DEFAULT FALSE"
        UUID id_pet FK "NOT NULL"
        UUID id_tutor FK "NOT NULL"
    }

    TB_USUARIO {
        UUID id_usuario PK
        VARCHAR username "NOT NULL UNIQUE"
        VARCHAR password_hash "NOT NULL"
        VARCHAR role "NOT NULL (enum)"
    }
```

## Constraints e regras

### Chaves primárias
Todas as PK são `UUID` geradas pelo banco via `@GeneratedValue(strategy = GenerationType.UUID)`.

### Chaves estrangeiras (cardinalidade)

| Tabela origem | Coluna FK | Tabela destino | Cardinalidade |
|---|---|---|---|
| `TB_PET` | `ID_TUTOR` | `TB_TUTOR` | N:1 (obrigatório) |
| `TB_CONSULTA` | `ID_PET` | `TB_PET` | N:1 (obrigatório) |
| `TB_CONSULTA` | `ID_CLINICA` | `TB_CLINICA` | N:1 (obrigatório) |
| `TB_VACINA` | `ID_PET` | `TB_PET` | N:1 (obrigatório) |
| `TB_VACINA` | `ID_CONSULTA` | `TB_CONSULTA` | N:1 (opcional) |
| `TB_MEDICAMENTO` | `ID_PET` | `TB_PET` | N:1 (obrigatório) |
| `TB_MEDICAMENTO` | `ID_CONSULTA` | `TB_CONSULTA` | N:1 (opcional) |
| `TB_AGENDAMENTO` | `ID_PET` | `TB_PET` | N:1 (obrigatório) |
| `TB_AGENDAMENTO` | `ID_CLINICA` | `TB_CLINICA` | N:1 (obrigatório) |
| `TB_ALERTA` | `ID_PET` | `TB_PET` | N:1 (obrigatório) |
| `TB_ALERTA` | `ID_TUTOR` | `TB_TUTOR` | N:1 (obrigatório) |

### Unique constraints
- `TB_TUTOR.CPF`, `TB_TUTOR.EMAIL`
- `TB_CLINICA.CNPJ`
- `TB_USUARIO.USERNAME`

### Cascade e orphanRemoval
- `Tutor → Pet`: cascade ALL + orphanRemoval (remover tutor remove seus pets)
- `Pet → Vacina/Medicamento/Consulta/Agendamento/Alerta`: cascade ALL + orphanRemoval
- `Consulta → Vacina/Medicamento`: cascade ALL (vínculo opcional, sem orphanRemoval)

## Justificativa do modelo

O modelo separa **Vacina**, **Medicamento**, **Agendamento** e **Alerta** em tabelas próprias (ao invés de consolidar em "EventoSaude") por três motivos:

1. **Riqueza semântica** — cada entidade tem atributos próprios (Vacina tem `dt_proxima`/`lote`; Medicamento tem `dosagem`/`frequencia`/`ativo`/`custo`; Agendamento tem `status` e `tipo` distintos do Alerta).
2. **Consultas otimizadas** — buscar "medicamentos ativos" ou "vacinas vencidas" fica natural sem `WHERE tipo = X`.
3. **Coerência com KPIs da CLYVO VET** — taxa de adesão vacinal e LTV por pet exigem tabelas dedicadas.

`TB_USUARIO` existe apenas para autenticação JWT e não tem relacionamentos com o domínio funcional.
