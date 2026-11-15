package ar.edu.undec.integrador.service;

import ar.edu.undec.integrador.exception.NoEncontradoException;
import ar.edu.undec.integrador.model.Evento;
import ar.edu.undec.integrador.repository.EventoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventoService {

    private final EventoRepository repository;

    public EventoService(EventoRepository repository) {
        this.repository = repository;
    }

    public List<Evento> listar(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return repository.findAll();
        }
        return repository.findByNombreContainingIgnoreCase(nombre);
    }

    public Evento obtener(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("No existe el evento con id " + id));
    }

    public Evento crear(Evento evento) {
        evento.setId(null);
        return repository.save(evento);
    }

    public Evento actualizar(Long id, Evento datos) {
        Evento existente = obtener(id);
        existente.setNombre(datos.getNombre());
        existente.setFecha(datos.getFecha());
        existente.setCapacidad(datos.getCapacidad());
        existente.setPrecio(datos.getPrecio());
        return repository.save(existente);
    }

    public void borrar(Long id) {
        Evento existente = obtener(id);
        repository.delete(existente);
    }
}