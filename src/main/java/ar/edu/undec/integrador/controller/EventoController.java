package ar.edu.undec.integrador.controller;

import ar.edu.undec.integrador.model.Entrada;
import ar.edu.undec.integrador.model.Evento;
import ar.edu.undec.integrador.service.EntradaService;
import ar.edu.undec.integrador.service.EventoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    private final EventoService service;
    private final EntradaService entradaService;

    public EventoController(EventoService service, EntradaService entradaService) {
        this.service = service;
        this.entradaService = entradaService;
    }

    @GetMapping
    public List<Evento> listar(@RequestParam(required = false) String nombre) {
        return service.listar(nombre);
    }

    @GetMapping("/{id}")
    public Evento obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping("/{id}/entradas")
    public List<Entrada> entradas(@PathVariable Long id) {
        return entradaService.listarPorEvento(id);
    }

    @GetMapping("/{id}/ocupacion")
    public Map<String, Object> ocupacion(@PathVariable Long id) {
        return entradaService.obtenerOcupacion(id);
    }

    @PostMapping
    public ResponseEntity<Evento> crear(@Valid @RequestBody Evento evento) {
        Evento creado = service.crear(evento);
        return ResponseEntity.created(URI.create("/api/eventos/" + creado.getId())).body(creado);
    }

    @PutMapping("/{id}")
    public Evento actualizar(@PathVariable Long id, @Valid @RequestBody Evento evento) {
        return service.actualizar(id, evento);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrar(@PathVariable Long id) {
        service.borrar(id);
        return ResponseEntity.noContent().build();
    }
}