package com.pucmm.csti19105488.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// clase auxiliar para transportar estadísticas
public class ResumenEvento {

    private Long idEvento;
    private int totalInscritos;
    private int totalAsistentes;
    private double porcentajeAsistencia;
    private Map<String, Long> registrosPorDia;
    private Map<String, Long> asistenciaPorHora;

    public ResumenEvento() {}

    public ResumenEvento(Long idEvento, int totalInscritos, int totalAsistentes,
                         Map<String, Long> registrosPorDia,
                         Map<String, Long> asistenciaPorHora) {
        this.idEvento = idEvento;
        this.totalInscritos = totalInscritos;
        this.totalAsistentes = totalAsistentes;
        this.porcentajeAsistencia = totalInscritos > 0
                ? (totalAsistentes * 100.0) / totalInscritos
                : 0;
        this.registrosPorDia = registrosPorDia;
        this.asistenciaPorHora = asistenciaPorHora;
    }

    // Getters y Setters
    public Long getIdEvento() { return idEvento; }
    public void setIdEvento(Long idEvento) { this.idEvento = idEvento; }

    public int getTotalInscritos() { return totalInscritos; }
    public void setTotalInscritos(int totalInscritos) { this.totalInscritos = totalInscritos; }

    public int getTotalAsistentes() { return totalAsistentes; }
    public void setTotalAsistentes(int totalAsistentes) { this.totalAsistentes = totalAsistentes; }

    public double getPorcentajeAsistencia() { return porcentajeAsistencia; }
    public void setPorcentajeAsistencia(double porcentajeAsistencia) { this.porcentajeAsistencia = porcentajeAsistencia; }

    public Map<String, Long> getRegistrosPorDia() { return registrosPorDia; }
    public void setRegistrosPorDia(Map<String, Long> registrosPorDia) { this.registrosPorDia = registrosPorDia;}

    public Map<String, Long> getAsistenciaPorHora() { return asistenciaPorHora; }
    public void setAsistenciaPorHora(Map<String, Long> asistenciaPorHora) { this.asistenciaPorHora = asistenciaPorHora; }
}