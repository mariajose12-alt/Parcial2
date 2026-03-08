package com.pucmm.csti19105488.service;

import com.pucmm.csti19105488.dao.RegistroDAO;
import com.pucmm.csti19105488.model.Registro;
import com.pucmm.csti19105488.model.ResumenEvento;
import com.pucmm.csti19105488.model.Usuario;
import com.pucmm.csti19105488.model.enums.TipoUsuario;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EstadisticaService {

    private static final RegistroDAO registroDAO = new RegistroDAO();

    public ResumenEvento generarResumen(Long eventoId) {
        List<Registro> registros = registroDAO.listarPorEvento(eventoId);

        int totalInscritos = registros.size();
        int totalAsistentes = (int) registros.stream()
                .filter(Registro::isAsistenciaConfirmada)
                .count();

        // Inscripciones agrupadas por día
        Map<String, Long> registrosPorDia = registros.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getFechaRegistro().toLocalDate().toString(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        // Asistencia agrupada por hora
        Map<String, Long> asistenciaPorHora = registros.stream()
                .filter(Registro::isAsistenciaConfirmada)
                .filter(r -> r.getFechaAsistencia() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getFechaAsistencia().getHour() + ":00",
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        return new ResumenEvento(
                eventoId,
                totalInscritos,
                totalAsistentes,
                registrosPorDia,
                asistenciaPorHora
        );
    }

    public static List<Registro> filtrarRegistros(Usuario usuario, String eventoId, String desde, String hasta, String tipo) {
        List<Registro> todos;

        if (usuario.getRol() == TipoUsuario.ADMINISTRADOR) {
            todos = registroDAO.listarTodos();
        } else {
            todos = registroDAO.listarPorOrganizador(usuario.getId());
        }

        return todos.stream()
                .filter(r -> {
                    if (eventoId != null && !eventoId.isEmpty())
                        return r.getEvento().getId().equals(Long.parseLong(eventoId));
                    return true;
                })
                .filter(r -> {
                    if (desde != null && !desde.isEmpty())
                        return !r.getFechaRegistro().toLocalDate().isBefore(LocalDate.parse(desde));
                    return true;
                })
                .filter(r -> {
                    if (hasta != null && !hasta.isEmpty())
                        return !r.getFechaRegistro().toLocalDate().isAfter(LocalDate.parse(hasta));
                    return true;
                })
                .filter(r -> {
                    if (tipo != null && !tipo.isEmpty())
                        return r.getEvento().getEstado().name().equals(tipo);
                    return true;
                })
                .collect(Collectors.toList());
    }

    public static Map<String, Object> calcularEstadisticas(List<Registro> registros) {
        int totalInscritos = registros.size();
        int totalAsistentes = (int) registros.stream().filter(Registro::isAsistenciaConfirmada).count();
        double porcentaje = totalInscritos > 0 ? (totalAsistentes * 100.0) / totalInscritos : 0;

        Map<String, Long> porEvento = registros.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getEvento().getTitulo(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        Map<String, Long> porDia = registros.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getFechaRegistro().toLocalDate().toString(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalInscritos", totalInscritos);
        stats.put("totalAsistentes", totalAsistentes);
        stats.put("porcentajeAsistencia", Math.round(porcentaje * 10.0) / 10.0);
        stats.put("inscritosPorEvento", porEvento);
        stats.put("inscritosPorDia", porDia);
        return stats;
    }


}