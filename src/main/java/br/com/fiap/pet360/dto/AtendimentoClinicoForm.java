package br.com.fiap.pet360.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public class AtendimentoClinicoForm {

    @NotNull(message = "ID do agendamento é obrigatório")
    private UUID agendamentoId;

    @NotBlank(message = "Motivo da consulta é obrigatório")
    @Size(max = 500, message = "Motivo deve ter até 500 caracteres")
    private String motivo;

    @NotBlank(message = "Diagnóstico clínico é obrigatório")
    @Size(max = 500, message = "Diagnóstico deve ter até 500 caracteres")
    private String diagnostico;

    @NotNull(message = "Peso atual do pet é obrigatório")
    @DecimalMin(value = "0.01", message = "Peso deve ser maior que zero")
    private BigDecimal pesoAtual;

    @NotNull(message = "Valor da consulta é obrigatório")
    @DecimalMin(value = "0.00", message = "Valor não pode ser negativo")
    private BigDecimal valor;

    // Dados de Vacina (Opcional)
    private boolean aplicarVacina = false;
    private String nomeVacina;
    private String loteVacina;
    private Integer diasProximaDose = 365;

    // Dados de Prescrição Medicamentosa (Opcional)
    private boolean prescreverMedicamento = false;
    private String nomeMedicamento;
    private String dosagem;
    private String frequencia;
    private Integer duracaoDias = 7;
    private BigDecimal custoMedicamento;

    public AtendimentoClinicoForm() {}

    public UUID getAgendamentoId() { return agendamentoId; }
    public void setAgendamentoId(UUID agendamentoId) { this.agendamentoId = agendamentoId; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }

    public BigDecimal getPesoAtual() { return pesoAtual; }
    public void setPesoAtual(BigDecimal pesoAtual) { this.pesoAtual = pesoAtual; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public boolean isAplicarVacina() { return aplicarVacina; }
    public void setAplicarVacina(boolean aplicarVacina) { this.aplicarVacina = aplicarVacina; }

    public String getNomeVacina() { return nomeVacina; }
    public void setNomeVacina(String nomeVacina) { this.nomeVacina = nomeVacina; }

    public String getLoteVacina() { return loteVacina; }
    public void setLoteVacina(String loteVacina) { this.loteVacina = loteVacina; }

    public Integer getDiasProximaDose() { return diasProximaDose; }
    public void setDiasProximaDose(Integer diasProximaDose) { this.diasProximaDose = diasProximaDose; }

    public boolean isPrescreverMedicamento() { return prescreverMedicamento; }
    public void setPrescreverMedicamento(boolean prescreverMedicamento) { this.prescreverMedicamento = prescreverMedicamento; }

    public String getNomeMedicamento() { return nomeMedicamento; }
    public void setNomeMedicamento(String nomeMedicamento) { this.nomeMedicamento = nomeMedicamento; }

    public String getDosagem() { return dosagem; }
    public void setDosagem(String dosagem) { this.dosagem = dosagem; }

    public String getFrequencia() { return frequencia; }
    public void setFrequencia(String frequencia) { this.frequencia = frequencia; }

    public Integer getDuracaoDias() { return duracaoDias; }
    public void setDuracaoDias(Integer duracaoDias) { this.duracaoDias = duracaoDias; }

    public BigDecimal getCustoMedicamento() { return custoMedicamento; }
    public void setCustoMedicamento(BigDecimal custoMedicamento) { this.custoMedicamento = custoMedicamento; }
}
