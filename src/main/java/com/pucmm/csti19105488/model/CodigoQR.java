package com.pucmm.csti19105488.model;

import jakarta.persistence.*;

@Entity
@Table(name = "codigos_qr")
public class CodigoQR {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "registro_id", nullable = false)
    private Registro registro;

    @Column(nullable = false)
    private Long idEvento;

    @Column(nullable = false)
    private Long idUsuario;

    @Column(nullable = false, unique = true)
    private String validacionToken;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String imageBase64;


    public CodigoQR() {}

    public CodigoQR(Registro registro, String validacionToken, String imageBase64) {
        this.registro = registro;
        this.idEvento = registro.getEvento().getId();
        this.idUsuario = registro.getParticipante().getId();
        this.validacionToken = validacionToken;
        this.imageBase64 = imageBase64;
    }

    public boolean validar(String token) {
        return this.validacionToken.equals(token);
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Registro getRegistro() { return registro; }
    public void setRegistro(Registro registro) { this.registro = registro; }

    public Long getIdEvento() { return idEvento; }
    public void setIdEvento(Long idEvento) { this.idEvento = idEvento; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getValidacionToken() { return validacionToken; }
    public void setValidacionToken(String validacionToken) { this.validacionToken = validacionToken; }

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
}