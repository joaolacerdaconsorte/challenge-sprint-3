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

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "TB_VACINA")
public class Vacina {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_VACINA")
    private UUID id;

    @Column(name = "NM_VACINA", nullable = false, length = 100)
    private String nome;

    @Column(name = "DT_APLICACAO", nullable = false)
    private LocalDate dataAplicacao;

    @Column(name = "DT_PROXIMA")
    private LocalDate dataProxima;

    @Column(name = "LOTE", length = 50)
    private String lote;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PET", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CONSULTA")
    private Consulta consulta;

    public Vacina() {
    }

    public Vacina(String nome, LocalDate dataAplicacao, LocalDate dataProxima, Pet pet) {
        this.nome = nome;
        this.dataAplicacao = dataAplicacao;
        this.dataProxima = dataProxima;
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

    public LocalDate getDataAplicacao() {
        return dataAplicacao;
    }

    public void setDataAplicacao(LocalDate dataAplicacao) {
        this.dataAplicacao = dataAplicacao;
    }

    public LocalDate getDataProxima() {
        return dataProxima;
    }

    public void setDataProxima(LocalDate dataProxima) {
        this.dataProxima = dataProxima;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
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
        if (!(o instanceof Vacina v)) return false;
        return id != null && Objects.equals(id, v.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
