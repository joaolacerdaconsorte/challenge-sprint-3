package br.com.fiap.pet360;

import br.com.fiap.pet360.dto.AtendimentoClinicoForm;
import br.com.fiap.pet360.dto.TriagemAgendamentoRequest;
import br.com.fiap.pet360.dto.TriagemAgendamentoResult;
import br.com.fiap.pet360.model.Agendamento;
import br.com.fiap.pet360.model.Clinica;
import br.com.fiap.pet360.model.Consulta;
import br.com.fiap.pet360.model.Pet;
import br.com.fiap.pet360.model.StatusAgendamento;
import br.com.fiap.pet360.model.TipoAgendamento;
import br.com.fiap.pet360.repository.AgendamentoRepository;
import br.com.fiap.pet360.repository.AlertaRepository;
import br.com.fiap.pet360.repository.ClinicaRepository;
import br.com.fiap.pet360.repository.PetRepository;
import br.com.fiap.pet360.repository.UsuarioRepository;
import br.com.fiap.pet360.service.AtendimentoClinicoService;
import br.com.fiap.pet360.service.TriagemAgendamentoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PetCare360Sprint3ApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClinicaRepository clinicaRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private TriagemAgendamentoService triagemAgendamentoService;

    @Autowired
    private AtendimentoClinicoService atendimentoClinicoService;

    @Test
    @DisplayName("Flyway migrou o esquema e inseriu usuarios e dados iniciais com sucesso")
    void testFlywayAndInitialData() {
        assertThat(usuarioRepository.count()).isGreaterThanOrEqualTo(3);
        assertThat(petRepository.count()).isGreaterThanOrEqualTo(3);
        assertThat(clinicaRepository.count()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Spring Security redireciona rota protegida para /login quando nao autenticado")
    void testSecurityRedirectToLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Spring Security permite acesso ao dashboard para usuario autenticado")
    void testSecurityAccessDashboard() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("Spring Security bloqueia Tutor de acessar rota restrita de nova clinica (403)")
    void testSecurityForbidsUserFromAdminRoute() throws Exception {
        mockMvc.perform(get("/clinicas/nova"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Fluxo Nao-CRUD 1: Triagem e Agendamento com deteccao preventiva e emissao de alerta")
    void testFluxo1TriagemAgendamento() {
        Pet pet = petRepository.findAll().get(0);
        Clinica clinica = clinicaRepository.findAll().get(0);
        LocalDateTime dataHora = LocalDateTime.now().plusDays(5).withHour(14).withMinute(0);

        TriagemAgendamentoRequest req = new TriagemAgendamentoRequest(
                pet.getId(),
                clinica.getId(),
                dataHora,
                TipoAgendamento.CONSULTA,
                "Exame preventivo semestral"
        );

        TriagemAgendamentoResult resultado = triagemAgendamentoService.processarAgendamentoComTriagem(req);

        assertThat(resultado.agendamento()).isNotNull();
        assertThat(resultado.agendamento().getStatus()).isEqualTo(StatusAgendamento.CONFIRMADO);
        assertThat(resultado.mensagemStatus()).contains("sucesso");
    }

    @Test
    @DisplayName("Fluxo Nao-CRUD 2: Atendimento Clinico integrado com prescricao, vacina e atualizacao de peso")
    void testFluxo2AtendimentoClinico() {
        Pet pet = petRepository.findAll().get(0);
        Clinica clinica = clinicaRepository.findAll().get(0);

        Agendamento agendamento = agendamentoRepository.save(new Agendamento(
                LocalDateTime.now().minusHours(1),
                TipoAgendamento.CONSULTA,
                StatusAgendamento.CONFIRMADO,
                pet,
                clinica
        ));

        AtendimentoClinicoForm form = new AtendimentoClinicoForm();
        form.setAgendamentoId(agendamento.getId());
        form.setMotivo("Consulta de Rotina");
        form.setDiagnostico("Pet saudavel, vacinado e orientado");
        form.setPesoAtual(new BigDecimal("29.80"));
        form.setValor(new BigDecimal("180.00"));

        form.setAplicarVacina(true);
        form.setNomeVacina("Vacina V10 Reforco");
        form.setLoteVacina("LOT-TESTE-2026");
        form.setDiasProximaDose(365);

        form.setPrescreverMedicamento(true);
        form.setNomeMedicamento("Vermifugo Canino");
        form.setDosagem("1 comprimido");
        form.setFrequencia("Dose unica");
        form.setDuracaoDias(1);
        form.setCustoMedicamento(new BigDecimal("45.00"));

        Consulta consulta = atendimentoClinicoService.realizarAtendimento(form);

        assertThat(consulta).isNotNull();
        assertThat(consulta.getDiagnostico()).contains("saudavel");

        Agendamento agendamentoAtualizado = agendamentoRepository.findById(agendamento.getId()).orElseThrow();
        assertThat(agendamentoAtualizado.getStatus()).isEqualTo(StatusAgendamento.REALIZADO);

        Pet petAtualizado = petRepository.findById(pet.getId()).orElseThrow();
        assertThat(petAtualizado.getPeso()).isEqualByComparingTo("29.80");
    }
}
