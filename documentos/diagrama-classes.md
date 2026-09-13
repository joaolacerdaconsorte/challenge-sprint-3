# Diagrama de Classes de Entidade — Pet360

> Challenge FIAP — Java Advanced 2026 — CLYVO VET / Pet360
> Vale **até 10 pontos** (em conjunto com o DER).

Diagrama de classes das entidades JPA. Cardinalidades e tipos seguem fielmente o código em `src/main/java/br/com/fiap/pet360/model/`.

## Diagrama (Mermaid)

```mermaid
classDiagram
    direction LR

    class Tutor {
        -UUID id
        -String nome
        -String cpf
        -String email
        -String telefone
        -String endereco
        -List~Pet~ pets
        -List~Alerta~ alertas
        +getters/setters()
    }

    class Clinica {
        -UUID id
        -String nome
        -String cnpj
        -String email
        -String telefone
        -String endereco
        -List~Consulta~ consultas
        -List~Agendamento~ agendamentos
        +getters/setters()
    }

    class Pet {
        -UUID id
        -String nome
        -Especie especie
        -String raca
        -LocalDate dataNascimento
        -BigDecimal peso
        -String observacoes
        -Tutor tutor
        -List~Consulta~ consultas
        -List~Vacina~ vacinas
        -List~Medicamento~ medicamentos
        -List~Agendamento~ agendamentos
        -List~Alerta~ alertas
        +getters/setters()
    }

    class Consulta {
        -UUID id
        -LocalDateTime dataHora
        -StatusConsulta status
        -String motivo
        -String diagnostico
        -BigDecimal valor
        -Pet pet
        -Clinica clinica
        -List~Vacina~ vacinas
        -List~Medicamento~ medicamentos
        +getters/setters()
    }

    class Vacina {
        -UUID id
        -String nome
        -LocalDate dataAplicacao
        -LocalDate dataProxima
        -String lote
        -Pet pet
        -Consulta consulta
        +getters/setters()
    }

    class Medicamento {
        -UUID id
        -String nome
        -String dosagem
        -String frequencia
        -LocalDate dataInicio
        -LocalDate dataFim
        -Boolean ativo
        -BigDecimal custo
        -Pet pet
        -Consulta consulta
        +getters/setters()
    }

    class Agendamento {
        -UUID id
        -LocalDateTime dataHora
        -TipoAgendamento tipo
        -StatusAgendamento status
        -String observacoes
        -Pet pet
        -Clinica clinica
        +getters/setters()
    }

    class Alerta {
        -UUID id
        -TipoAlerta tipo
        -String descricao
        -LocalDateTime dataAlerta
        -Boolean lido
        -Pet pet
        -Tutor tutor
        +getters/setters()
    }

    class Usuario {
        -UUID id
        -String username
        -String passwordHash
        -Role role
        +getters/setters()
    }

    class Especie {
        <<enumeration>>
        CACHORRO
        GATO
        PASSARO
        ROEDOR
        REPTIL
        OUTRO
    }

    class StatusConsulta {
        <<enumeration>>
        AGENDADA
        REALIZADA
        CANCELADA
    }

    class StatusAgendamento {
        <<enumeration>>
        PENDENTE
        CONFIRMADO
        REALIZADO
        CANCELADO
    }

    class TipoAgendamento {
        <<enumeration>>
        CONSULTA
        VACINA
        EXAME
        CIRURGIA
        RETORNO
        BANHO_TOSA
        OUTRO
    }

    class TipoAlerta {
        <<enumeration>>
        VACINA
        MEDICAMENTO
        CONSULTA
        AGENDAMENTO
        BEM_ESTAR
        OUTRO
    }

    class Role {
        <<enumeration>>
        ADMIN
        USER
    }

    Tutor "1" --> "0..*" Pet : tem
    Tutor "1" --> "0..*" Alerta : recebe
    Clinica "1" --> "0..*" Consulta : realiza
    Clinica "1" --> "0..*" Agendamento : agenda
    Pet "1" --> "0..*" Consulta
    Pet "1" --> "0..*" Vacina
    Pet "1" --> "0..*" Medicamento
    Pet "1" --> "0..*" Agendamento
    Pet "1" --> "0..*" Alerta
    Consulta "1" --> "0..*" Vacina
    Consulta "1" --> "0..*" Medicamento

    Pet --> Especie : usa
    Consulta --> StatusConsulta : usa
    Agendamento --> StatusAgendamento : usa
    Agendamento --> TipoAgendamento : usa
    Alerta --> TipoAlerta : usa
    Usuario --> Role : usa
```

## Convenções de implementação

Todas as entidades seguem o estilo do professor Luiz Real (lgsreal) e o material compartilhado em aula:

| Convenção | Detalhe |
|---|---|
| **Sem Lombok** | Getters/setters explícitos. Lombok é problemático com `equals/hashCode` em entidades Hibernate. |
| **UUID como ID** | `@GeneratedValue(strategy = GenerationType.UUID)`. Mais seguro que sequence/identity em sistemas distribuídos. |
| **BigDecimal para dinheiro** | `peso`, `valor`, `custo`. Nunca `double`/`float` (problema do `0.1 + 0.2 = 0.30000000000000004`). |
| **LocalDate / LocalDateTime** | Sem `java.util.Date` legado. |
| **Dois construtores** | Vazio (exigido pelo JPA) + construtor com campos principais. |
| **Equals/HashCode** | Baseados apenas no ID, com guarda para `id != null`. |
| **FK lazy** | `@ManyToOne(fetch = FetchType.LAZY)` para evitar N+1 acidental. |
| **Enums como STRING** | `@Enumerated(EnumType.STRING)` — legível e robusto contra reordenação. |

## Mapeamento Classe ↔ Tabela

| Classe Java | Tabela H2/Oracle |
|---|---|
| `Tutor` | `TB_TUTOR` |
| `Clinica` | `TB_CLINICA` |
| `Pet` | `TB_PET` |
| `Consulta` | `TB_CONSULTA` |
| `Vacina` | `TB_VACINA` |
| `Medicamento` | `TB_MEDICAMENTO` |
| `Agendamento` | `TB_AGENDAMENTO` |
| `Alerta` | `TB_ALERTA` |
| `Usuario` | `TB_USUARIO` |

Coerência com o DER: o nome lógico em camelCase no Java mapeia para o nome físico em SNAKE_CASE com prefixo `TB_` no banco — convenção da FIAP herdada do DDL Oracle original.
