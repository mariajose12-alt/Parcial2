package com.pucmm.csti19105488.model;

import com.pucmm.csti19105488.model.enums.EstadoEvento;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "eventos", uniqueConstraints = {@UniqueConstraint(columnNames = {"titulo", "fecha", "lugar"})})
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, length = 1000)
    private String descripcion;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private String lugar;

    @Column(nullable = false)
    private int capacidadMax;

    @Column(nullable = false)
    private int inscritosActuales = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEvento estado = EstadoEvento.BORRADOR;

    @ManyToOne
    @JoinColumn(name = "organizador_id", nullable = false)
    private Usuario organizador;


    public Evento() {}

    public Evento(String titulo, String descripcion, LocalDateTime fecha,
                  String lugar, int capacidadMax, Usuario organizador) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.lugar = lugar;
        this.capacidadMax = capacidadMax;
        this.organizador = organizador;
        this.estado = EstadoEvento.BORRADOR;
        this.inscritosActuales = 0;
    }

    public boolean tieneDisponibilidad() {
        return inscritosActuales < capacidadMax;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }

    public int getCapacidadMax() { return capacidadMax; }
    public void setCapacidadMax(int capacidadMax) { this.capacidadMax = capacidadMax; }

    public int getInscritosActuales() { return inscritosActuales; }
    public void setInscritosActuales(int inscritosActuales) { this.inscritosActuales = inscritosActuales; }

    public EstadoEvento getEstado() { return estado; }
    public void setEstado(EstadoEvento estado) { this.estado = estado; }

    public Usuario getOrganizador() { return organizador; }
    public void setOrganizador(Usuario organizador) { this.organizador = organizador; }

    public String getFechaFormateada() {
        return fecha.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getFechaParaInput() {
        if (fecha == null) return "";
        return fecha.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    }
}