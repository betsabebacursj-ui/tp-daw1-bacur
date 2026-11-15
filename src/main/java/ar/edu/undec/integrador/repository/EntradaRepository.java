package ar.edu.undec.integrador.repository;

import ar.edu.undec.integrador.model.Entrada;
import ar.edu.undec.integrador.model.EstadoEntrada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EntradaRepository extends JpaRepository<Entrada, Long> {

    List<Entrada> findByEventoId(Long eventoId);

    long countByEventoId(Long eventoId);

    boolean existsByEventoId(Long eventoId);

    long countByEventoIdAndEstadoNot(Long eventoId, EstadoEntrada estado);

    boolean existsByEventoIdAndAsistenteIdAndEstadoIn(
            Long eventoId, Long asistenteId, Collection<EstadoEntrada> estados);

    Optional<Entrada> findByCodigo(String codigo);

    List<Entrada> findByEstadoAndFechaReservaBefore(EstadoEntrada estado, LocalDateTime limite);
}