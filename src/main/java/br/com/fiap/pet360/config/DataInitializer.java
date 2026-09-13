package br.com.fiap.pet360.config;

import br.com.fiap.pet360.model.Agendamento;
import br.com.fiap.pet360.model.Alerta;
import br.com.fiap.pet360.model.Clinica;
import br.com.fiap.pet360.model.Consulta;
import br.com.fiap.pet360.model.Especie;
import br.com.fiap.pet360.model.Medicamento;
import br.com.fiap.pet360.model.Pet;
import br.com.fiap.pet360.model.Role;
import br.com.fiap.pet360.model.StatusAgendamento;
import br.com.fiap.pet360.model.StatusConsulta;
import br.com.fiap.pet360.model.TipoAgendamento;
import br.com.fiap.pet360.model.TipoAlerta;
import br.com.fiap.pet360.model.Tutor;
import br.com.fiap.pet360.model.Usuario;
import br.com.fiap.pet360.model.Vacina;
import br.com.fiap.pet360.repository.AgendamentoRepository;
import br.com.fiap.pet360.repository.AlertaRepository;
import br.com.fiap.pet360.repository.ClinicaRepository;
import br.com.fiap.pet360.repository.ConsultaRepository;
import br.com.fiap.pet360.repository.MedicamentoRepository;
import br.com.fiap.pet360.repository.PetRepository;
import br.com.fiap.pet360.repository.TutorRepository;
import br.com.fiap.pet360.repository.UsuarioRepository;
import br.com.fiap.pet360.repository.VacinaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(UsuarioRepository usuarioRepository,
                               TutorRepository tutorRepository,
                               ClinicaRepository clinicaRepository,
                               PetRepository petRepository,
                               ConsultaRepository consultaRepository,
                               VacinaRepository vacinaRepository,
                               MedicamentoRepository medicamentoRepository,
                               AgendamentoRepository agendamentoRepository,
                               AlertaRepository alertaRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            if (usuarioRepository.count() > 0 || tutorRepository.count() > 0) {
                return;
            }

            Tutor ana = tutorRepository.save(new Tutor("Ana Lima", "111.222.333-44", "ana@email.com", "11999990001", "Rua A, 100"));
            Tutor bruno = tutorRepository.save(new Tutor("Bruno Souza", "222.333.444-55", "bruno@email.com", "11999990002", "Rua B, 200"));

            Clinica vetCare = clinicaRepository.save(new Clinica("VetCare SP", "12.345.678/0001-90", "contato@vetcare.com", "1133330001", "Av. Paulista, 1000"));
            Clinica petLife = clinicaRepository.save(new Clinica("Pet Life Hospital", "98.765.432/0001-10", "contato@petlife.com", "1133330002", "R. Augusta, 500"));

            Pet rex = petRepository.save(new Pet("Rex", Especie.CACHORRO, "Labrador", LocalDate.of(2020, 3, 15), new BigDecimal("28.50"), ana));
            Pet mia = petRepository.save(new Pet("Mia", Especie.GATO, "Persa", LocalDate.of(2021, 7, 22), new BigDecimal("4.20"), ana));
            Pet thor = petRepository.save(new Pet("Thor", Especie.CACHORRO, "Golden Retriever", LocalDate.of(2019, 1, 10), new BigDecimal("32.00"), bruno));

            Consulta consulta1 = consultaRepository.save(new Consulta(
                    LocalDateTime.of(2026, 1, 10, 14, 30),
                    StatusConsulta.REALIZADA,
                    "Check-up anual",
                    new BigDecimal("150.00"),
                    rex,
                    vetCare));
            consulta1.setDiagnostico("Saudável");
            consultaRepository.save(consulta1);

            Consulta consulta2 = consultaRepository.save(new Consulta(
                    LocalDateTime.of(2026, 6, 15, 10, 0),
                    StatusConsulta.AGENDADA,
                    "Vacinação anual",
                    new BigDecimal("90.00"),
                    mia,
                    vetCare));

            Vacina v10 = new Vacina("V10", LocalDate.of(2026, 1, 10), LocalDate.of(2027, 1, 10), rex);
            v10.setLote("LOT-001");
            v10.setConsulta(consulta1);
            vacinaRepository.save(v10);

            Vacina antirrabica = new Vacina("Antirrábica", LocalDate.of(2025, 8, 12), LocalDate.of(2026, 8, 12), rex);
            antirrabica.setLote("LOT-002");
            vacinaRepository.save(antirrabica);

            Vacina v4 = new Vacina("V4", LocalDate.of(2025, 12, 1), LocalDate.of(2026, 6, 1), mia);
            v4.setLote("LOT-003");
            vacinaRepository.save(v4);

            Medicamento antibiotico = new Medicamento(
                    "Amoxicilina",
                    "10mg/kg",
                    "2x ao dia",
                    LocalDate.of(2026, 1, 10),
                    LocalDate.of(2026, 1, 20),
                    new BigDecimal("45.90"),
                    rex);
            antibiotico.setConsulta(consulta1);
            antibiotico.setAtivo(false);
            medicamentoRepository.save(antibiotico);

            Medicamento antipulgas = new Medicamento(
                    "Antipulgas Bravecto",
                    "1 comprimido",
                    "1x a cada 3 meses",
                    LocalDate.of(2026, 3, 1),
                    null,
                    new BigDecimal("180.00"),
                    thor);
            medicamentoRepository.save(antipulgas);

            agendamentoRepository.save(new Agendamento(
                    LocalDateTime.of(2026, 7, 1, 9, 0),
                    TipoAgendamento.CONSULTA,
                    StatusAgendamento.PENDENTE,
                    rex,
                    vetCare));

            agendamentoRepository.save(new Agendamento(
                    LocalDateTime.of(2026, 8, 15, 15, 30),
                    TipoAgendamento.VACINA,
                    StatusAgendamento.CONFIRMADO,
                    mia,
                    petLife));

            alertaRepository.save(new Alerta(
                    TipoAlerta.VACINA,
                    "Vacina V10 do Rex próxima do vencimento",
                    LocalDateTime.now(),
                    rex,
                    ana));

            alertaRepository.save(new Alerta(
                    TipoAlerta.MEDICAMENTO,
                    "Renovar antipulgas do Thor",
                    LocalDateTime.now().minusDays(2),
                    thor,
                    bruno));
        };
    }
}
