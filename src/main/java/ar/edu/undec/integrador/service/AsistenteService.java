package ar.edu.undec.integrador.service;

import ar.edu.undec.integrador.exception.NoEncontradoException;
import ar.edu.undec.integrador.model.Asistente;
import ar.edu.undec.integrador.repository.AsistenteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AsistenteService {

    private final AsistenteRepository repository;

    public AsistenteService(AsistenteRepository repository) {
        this.repository = repository;
    }

    public List<Asistente> listar() {
        return repository.findAll();
    }

    public Asistente obtenerPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("Asistente no encontrado con id: " + id));
    }

    public Asistente crear(Asistente asistente) {
        asistente.setId(null);
        return repository.save(asistente);
    }

    public Asistente actualizar(Long id, Asistente datos) {
        Asistente existente = obtenerPorId(id);
        existente.setNombre(datos.getNombre());
        existente.setEmail(datos.getEmail());
        return repository.save(existente);
    }

    public void borrar(Long id) {
        Asistente existente = obtenerPorId(id);
        repository.delete(existente);
    }
}