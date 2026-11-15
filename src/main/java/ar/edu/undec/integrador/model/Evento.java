package ar.edu.undec.integrador.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;

@Entity
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @NotBlank(message = "no debe estar vacío")
    private String nombre;

    @NotNull(message = "es obligatoria")
    @Future(message ="debe ser una fecha futura")
    private LocalDateTime fecha;

    @NotNull (message = "es obligatoria")
    @Positive (message = "debe ser mayor a 0")
    private Integer capacidad;

    @NotNull (message = "es obligatorio")
    @PositiveOrZero(message = " no puede ser negativo")
    private Double precio;

    public Long getId () {
        return Id;
    }

    public void setId (Long id) {
        this.Id = id;
    }

    public String getNombre () {
        return nombre;
    }
    public void setNombre (String nombre) {
        this.nombre = nombre;
    }

    public LocalDateTime getFecha () {
        return fecha;
    }

    public void setFecha (LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Integer getCapacidad() {
        return capacidad;
    }
    public void setCapacidad (Integer capacidad) {
        this.capacidad = capacidad;
    }
    public Double getPrecio() {
        return precio;
    }
    public void setPrecio(Double precio) {
        this.precio = precio;
    }

}