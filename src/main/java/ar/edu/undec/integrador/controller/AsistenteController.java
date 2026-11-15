package ar.edu.undec.integrador.controller;

import ar.edu.undec.integrador.model.Asistente;
import ar.edu.undec.integrador.service.AsistenteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/asistentes")
public class AsistenteController {

    private final AsistenteService service;

    public AsistenteController(AsistenteService service) {
        this.service = service;
    }

    @GetMapping
    public List<Asistente> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Asistente obtener(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<Asistente> crear(@Valid @RequestBody Asistente asistente) {
        Asistente creado = service.crear(asistente);
        return ResponseEntity
                .created(URI.create("/api/asistentes/" + creado.getId()))
                .body(creado);
    }

    @PutMapping("/{id}")
    public Asistente actualizar(@PathVariable Long id, @Valid @RequestBody Asistente asistente) {
        return service.actualizar(id, asistente);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrar(@PathVariable Long id) {
        service.borrar(id);
        return ResponseEntity.noContent().build();
    }
}