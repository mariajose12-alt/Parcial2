package com.pucmm.csti19105488.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registros")
public class Registro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "participante_id", nullable = false)
    private Usuario participante;

    @ManyToOne
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Column(nullable = false, unique = true)
    private String qrToken;

    @Column(nullable = false)
    private boolean asistenciaConfirmada = false;

    private LocalDateTime fechaAsistencia;

    @ManyToOne
    @JoinColumn(name = "confirma_asistencia_id")
    private Usuario quienConfirmaAsistencia;


    public Registro() {}

    public Registro(Usuario participante, Evento evento, String qrToken) {
        this.participante = participante;
        this.evento = evento;
        this.qrToken = qrToken;
        this.fechaRegistro = LocalDateTime.now();
        this.asistenciaConfirmada = false;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getParticipante() { return participante; }
    public void setParticipante(Usuario participante) { this.participante = participante; }

    public Evento getEvento() { return evento; }
    public void setEvento(Evento evento) { this.evento = evento; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getQrToken() { return qrToken; }
    public void setQrToken(String qrToken) { this.qrToken = qrToken; }

    public boolean isAsistenciaConfirmada() { return asistenciaConfirmada; }
    public void setAsistenciaConfirmada(boolean asistenciaConfirmada) { this.asistenciaConfirmada = asistenciaConfirmada; }

    public LocalDateTime getFechaAsistencia() { return fechaAsistencia; }
    public void setFechaAsistencia(LocalDateTime fechaAsistencia) { this.fechaAsistencia = fechaAsistencia; }

    public Usuario getQuienConfirmaAsistencia() { return quienConfirmaAsistencia; }
    public void setQuienConfirmaAsistencia(Usuario quienConfirmaAsistencia) { this.quienConfirmaAsistencia = quienConfirmaAsistencia; }
}