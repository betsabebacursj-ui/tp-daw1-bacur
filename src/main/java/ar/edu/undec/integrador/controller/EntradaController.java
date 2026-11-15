package ar.edu.undec.integrador.controller;

import ar.edu.undec.integrador.model.Entrada;
import ar.edu.undec.integrador.service.EntradaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/entradas")
public class EntradaController {

    private final EntradaService service;

    public EntradaController(EntradaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Entrada> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Entrada obtener(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<Entrada> crear(@RequestBody Map<String, Long> body) {
        Long eventoId = body != null ? body.get("eventoId") : null;
        Long asistenteId = body != null ? body.get("asistenteId") : null;
        Entrada creada = service.crear(eventoId, asistenteId);
        return ResponseEntity
                .created(URI.create("/api/entradas/" + creada.getId()))
                .body(creada);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Entrada> cambiarEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String estado = body != null ? body.get("estado") : null;
        return ResponseEntity.ok(service.cambiarEstado(id, estado));
    }

    @PostMapping("/validar")
    public ResponseEntity<Entrada> validar(@RequestBody Map<String, String> body) {
        String codigo = body != null ? body.get("codigo") : null;
        return ResponseEntity.ok(service.validarCodigo(codigo));
    }

    @PatchMapping("/{id}/transferir")
    public ResponseEntity<Entrada> transferir(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        Long asistenteId = body != null ? body.get("asistenteId") : null;
        return ResponseEntity.ok(service.transferir(id, asistenteId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrar(@PathVariable Long id) {
        service.borrar(id);
        return ResponseEntity.noContent().build();
    }
}