package com.pucmm.csti19105488.model;

import com.pucmm.csti19105488.model.enums.EstadoEvento;
import com.pucmm.csti19105488.model.enums.TipoEvento;
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
    private LocalDateTime fechaInicio;

    @Column(nullable = false)
    private LocalDateTime fechaFin;

    @Column(nullable = false)
    private String lugar;

    @Column(nullable = false)
    private int capacidadMax;

    @Column(nullable = false)
    private int inscritosActuales = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEvento estado = EstadoEvento.BORRADOR;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEvento tipo;

    @ManyToOne
    @JoinColumn(name = "organizador_id", nullable = false)
    private Usuario organizador;

    @Transient
    private boolean usuarioInscrito;

    public Evento() {}

    public Evento(String titulo, String descripcion, LocalDateTime fechaInicio, LocalDateTime fechaFin, TipoEvento tipo, String lugar, int capacidadMax, Usuario organizador) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.tipo = tipo;
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

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public TipoEvento getTipo() {
        return tipo;
    }

    public void setTipo(TipoEvento tipo) {
        this.tipo = tipo;
    }

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

    public String getFechaInicioFormateada() {
        return fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getFechaFinFormateada() {
        return fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getRangoFechasFormateado() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return fechaInicio.format(formatter) + " - " + fechaFin.format(formatter);
    }

    public String getFechaInicioParaInput() {
        if (fechaInicio == null) return "";
        return fechaInicio.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    }

    public String getFechaFinParaInput() {
        if (fechaFin == null) return "";
        return fechaFin.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    }

    public boolean haComenzado() {
        return LocalDateTime.now().isAfter(fechaInicio);
    }

    public boolean haTerminado() {
        return LocalDateTime.now().isAfter(fechaFin);
    }

    public boolean estaEnCurso() {
        LocalDateTime ahora = LocalDateTime.now();
        return ahora.isAfter(fechaInicio) && ahora.isBefore(fechaFin);
    }

    public boolean isUsuarioInscrito() {
        return usuarioInscrito;
    }

    public void setUsuarioInscrito(boolean usuarioInscrito) {
        this.usuarioInscrito = usuarioInscrito;
    }
}