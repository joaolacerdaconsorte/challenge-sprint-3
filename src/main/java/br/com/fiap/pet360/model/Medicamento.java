package br.com.fiap.pet360.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "TB_MEDICAMENTO")
public class Medicamento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_MEDICAMENTO")
    private UUID id;

    @Column(name = "NM_MEDICAMENTO", nullable = false, length = 150)
    private String nome;

    @Column(name = "DOSAGEM", length = 100)
    private String dosagem;

    @Column(name = "FREQUENCIA", length = 100)
    private String frequencia;

    @Column(name = "DT_INICIO", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "DT_FIM")
    private LocalDate dataFim;

    @Column(name = "ATIVO", nullable = false)
    private Boolean ativo = Boolean.TRUE;

    @Column(name = "CUSTO", precision = 10, scale = 2)
    private BigDecimal custo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PET", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CONSULTA")
    private Consulta consulta;

    public Medicamento() {
    }

    public Medicamento(String nome, String dosagem, String frequencia, LocalDate dataInicio, LocalDate dataFim, BigDecimal custo, Pet pet) {
        this.nome = nome;
        this.dosagem = dosagem;
        this.frequencia = frequencia;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.custo = custo;
        this.pet = pet;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDosagem() {
        return dosagem;
    }

    public void setDosagem(String dosagem) {
        this.dosagem = dosagem;
    }

    public String getFrequencia() {
        return frequencia;
    }

    public void setFrequencia(String frequencia) {
        this.frequencia = frequencia;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public BigDecimal getCusto() {
        return custo;
    }

    public void setCusto(BigDecimal custo) {
        this.custo = custo;
    }

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public Consulta getConsulta() {
        return consulta;
    }

    public void setConsulta(Consulta consulta) {
        this.consulta = consulta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Medicamento m)) return false;
        return id != null && Objects.equals(id, m.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
