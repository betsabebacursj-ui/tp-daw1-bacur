package ar.edu.undec.integrador.service;

import ar.edu.undec.integrador.exception.ConflictoException;
import ar.edu.undec.integrador.exception.NoEncontradoException;
import ar.edu.undec.integrador.model.Entrada;
import ar.edu.undec.integrador.model.EstadoEntrada;
import ar.edu.undec.integrador.model.Evento;
import ar.edu.undec.integrador.repository.AsistenteRepository;
import ar.edu.undec.integrador.repository.EntradaRepository;
import ar.edu.undec.integrador.repository.EventoRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class EntradaService {

    private static final List<EstadoEntrada> ESTADOS_ACTIVOS =
            List.of(EstadoEntrada.RESERVADA, EstadoEntrada.PAGADA);

    private final EntradaRepository entradaRepository;
    private final EventoRepository eventoRepository;
    private final AsistenteRepository asistenteRepository;

    public EntradaService(EntradaRepository entradaRepository,
                          EventoRepository eventoRepository,
                          AsistenteRepository asistenteRepository) {
        this.entradaRepository = entradaRepository;
        this.eventoRepository = eventoRepository;
        this.asistenteRepository = asistenteRepository;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void anularReservasVencidas() {
        LocalDateTime limite = LocalDateTime.now().minusHours(48);
        List<Entrada> vencidas = entradaRepository
                .findByEstadoAndFechaReservaBefore(EstadoEntrada.RESERVADA, limite);
        for (Entrada e : vencidas) {
            e.setEstado(EstadoEntrada.ANULADA);
        }
        if (!vencidas.isEmpty()) {
            entradaRepository.saveAll(vencidas);
        }
    }

    @Transactional
    public List<Entrada> listar() {
        anularReservasVencidas();
        return entradaRepository.findAll();
    }

    @Transactional
    public Entrada obtenerPorId(Long id) {
        anularReservasVencidas();
        return entradaRepository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("Entrada no encontrada con id: " + id));
    }

    @Transactional
    public List<Entrada> listarPorEvento(Long eventoId) {
        anularReservasVencidas();
        if (!eventoRepository.existsById(eventoId)) {
            throw new NoEncontradoException("Evento no encontrado con id: " + eventoId);
        }
        return entradaRepository.findByEventoId(eventoId);
    }

    @Transactional
    public Entrada crear(Long eventoId, Long asistenteId) {
        anularReservasVencidas();

        if (eventoId == null) {
            throw new IllegalArgumentException("eventoId es obligatorio");
        }
        if (asistenteId == null) {
            throw new IllegalArgumentException("asistenteId es obligatorio");
        }

        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new NoEncontradoException("Evento no encontrado con id: " + eventoId));

        if (!asistenteRepository.existsById(asistenteId)) {
            throw new NoEncontradoException("Asistente no encontrado con id: " + asistenteId);
        }

        long noAnuladas = entradaRepository.countByEventoIdAndEstadoNot(eventoId, EstadoEntrada.ANULADA);
        if (noAnuladas >= evento.getCapacidad()) {
            throw new ConflictoException("El evento alcanzó su capacidad máxima");
        }

        boolean yaTieneActiva = entradaRepository.existsByEventoIdAndAsistenteIdAndEstadoIn(
                eventoId, asistenteId, ESTADOS_ACTIVOS);
        if (yaTieneActiva) {
            throw new ConflictoException("El asistente ya tiene una entrada activa para este evento");
        }

        Entrada entrada = new Entrada();
        entrada.setEventoId(eventoId);
        entrada.setAsistenteId(asistenteId);
        entrada.setEstado(EstadoEntrada.RESERVADA);
        entrada.setCodigo(UUID.randomUUID().toString());
        entrada.setFechaReserva(LocalDateTime.now());

        return entradaRepository.save(entrada);
    }

    @Transactional
    public Entrada cambiarEstado(Long id, String nuevoEstadoStr) {
        anularReservasVencidas();

        if (nuevoEstadoStr == null || nuevoEstadoStr.isBlank()) {
            throw new IllegalArgumentException("El campo estado es obligatorio");
        }

        EstadoEntrada nuevoEstado;
        try {
            nuevoEstado = EstadoEntrada.valueOf(nuevoEstadoStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado inválido: " + nuevoEstadoStr);
        }

        Entrada entrada = obtenerPorId(id);
        EstadoEntrada actual = entrada.getEstado();

        boolean valida = switch (actual) {
            case RESERVADA -> nuevoEstado == EstadoEntrada.PAGADA || nuevoEstado == EstadoEntrada.ANULADA;
            case PAGADA -> nuevoEstado == EstadoEntrada.USADA || nuevoEstado == EstadoEntrada.ANULADA;
            case USADA, ANULADA -> false;
        };

        if (!valida) {
            throw new ConflictoException("No se puede pasar de " + actual + " a " + nuevoEstado);
        }

        if (actual == EstadoEntrada.RESERVADA && nuevoEstado == EstadoEntrada.PAGADA) {
            Evento evento = eventoRepository.findById(entrada.getEventoId())
                    .orElseThrow(() -> new NoEncontradoException("Evento no encontrado"));
            double importe = evento.getPrecio();
            if (LocalDateTime.now().isBefore(evento.getFecha().minusDays(7))) {
                importe = importe * 0.8;
            }
            entrada.setImportePagado(importe);
        }

        entrada.setEstado(nuevoEstado);
        return entradaRepository.save(entrada);
    }

    @Transactional
    public Entrada validarCodigo(String codigo) {
        anularReservasVencidas();

        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El campo codigo es obligatorio");
        }

        Entrada entrada = entradaRepository.findByCodigo(codigo.trim())
                .orElseThrow(() -> new NoEncontradoException("No existe entrada con código: " + codigo));

        if (entrada.getEstado() != EstadoEntrada.PAGADA) {
            throw new ConflictoException("La entrada no se puede usar en estado " + entrada.getEstado());
        }

        entrada.setEstado(EstadoEntrada.USADA);
        return entradaRepository.save(entrada);
    }

    @Transactional
    public Entrada transferir(Long id, Long nuevoAsistenteId) {
        anularReservasVencidas();

        if (nuevoAsistenteId == null) {
            throw new IllegalArgumentException("El campo asistenteId es obligatorio");
        }

        Entrada entrada = obtenerPorId(id);

        if (entrada.getEstado() != EstadoEntrada.PAGADA) {
            throw new ConflictoException("Solo se pueden transferir entradas en estado PAGADA");
        }

        if (!asistenteRepository.existsById(nuevoAsistenteId)) {
            throw new NoEncontradoException("Asistente no encontrado con id: " + nuevoAsistenteId);
        }

        boolean yaTieneActiva = entradaRepository.existsByEventoIdAndAsistenteIdAndEstadoIn(
                entrada.getEventoId(), nuevoAsistenteId, ESTADOS_ACTIVOS);
        if (yaTieneActiva) {
            throw new ConflictoException("El asistente destino ya tiene una entrada activa para este evento");
        }

        entrada.setAsistenteId(nuevoAsistenteId);
        return entradaRepository.save(entrada);
    }

    @Transactional
    public Map<String, Object> obtenerOcupacion(Long eventoId) {
        anularReservasVencidas();

        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new NoEncontradoException("Evento no encontrado con id: " + eventoId));

        long vendidas = entradaRepository.countByEventoIdAndEstadoNot(eventoId, EstadoEntrada.ANULADA);
        int capacidad = evento.getCapacidad();
        long disponibles = Math.max(0, capacidad - vendidas);
        double porcentaje = capacidad > 0 ? Math.round((vendidas * 10000.0) / capacidad) / 100.0 : 0.0;

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("vendidas", vendidas);
        res.put("disponibles", disponibles);
        res.put("porcentajeOcupado", porcentaje);
        return res;
    }

    @Transactional
    public void borrar(Long id) {
        Entrada entrada = obtenerPorId(id);
        entradaRepository.delete(entrada);
    }
}