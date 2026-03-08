package com.pucmm.csti19105488.service;

import com.pucmm.csti19105488.dao.EventoDAO;
import com.pucmm.csti19105488.model.enums.EstadoEvento;
import com.pucmm.csti19105488.model.Evento;
import com.pucmm.csti19105488.model.Usuario;
import java.time.LocalDateTime;
import java.util.List;

public class EventoService {

    private static final EventoDAO eventoDAO = new EventoDAO();

    public String crearEvento(String titulo, String descripcion, LocalDateTime fecha,
                              String lugar, int capacidadMax, Usuario organizador) {

        if(eventoDAO.existeEvento(titulo, fecha, lugar)) {
            throw new RuntimeException("Ya existe un evento con esos datos");
        }

        if (titulo == null || titulo.isBlank()) return "El título es requerido";
        if (fecha.isBefore(LocalDateTime.now())) return "La fecha debe ser futura";
        if (capacidadMax <= 0) return "El cupo debe ser mayor a 0";

        Evento evento = new Evento(titulo, descripcion, fecha, lugar, capacidadMax, organizador);
        eventoDAO.guardar(evento);
        return null;
    }

    public String editarEvento(Long id, String titulo, String descripcion,
                               LocalDateTime fecha, String lugar, int capacidadMax) {
        Evento evento = eventoDAO.buscarPorId(id);
        if (evento == null) return "Evento no encontrado";
        if (evento.getEstado() == EstadoEvento.CANCELADO) return "No puedes editar un evento cancelado";

        evento.setTitulo(titulo);
        evento.setDescripcion(descripcion);
        evento.setFecha(fecha);
        evento.setLugar(lugar);
        evento.setCapacidadMax(capacidadMax);
        eventoDAO.actualizar(evento);
        return null;
    }

    public String cancelarEvento(Long id) {
        Evento evento = eventoDAO.buscarPorId(id);
        if (evento == null) return "Evento no encontrado";
        evento.setEstado(EstadoEvento.CANCELADO);
        eventoDAO.actualizar(evento);
        return null;
    }

    public String publicarEvento(Long id) {
        Evento evento = eventoDAO.buscarPorId(id);
        if (evento == null) return "Evento no encontrado";
        if (evento.getEstado() == EstadoEvento.CANCELADO) return "No puedes publicar un evento cancelado";
        evento.setEstado(EstadoEvento.PUBLICADO);
        eventoDAO.actualizar(evento);
        return null;
    }

    public String despublicarEvento(Long id) {
        Evento evento = eventoDAO.buscarPorId(id);
        if (evento == null) return "Evento no encontrado";
        evento.setEstado(EstadoEvento.BORRADOR);
        eventoDAO.actualizar(evento);
        return null;
    }

    public String eliminarEvento(Long id) {
        Evento evento = eventoDAO.buscarPorId(id);
        if (evento == null) return "Evento no encontrado";
        eventoDAO.eliminar(id);
        return null;
    }

    public List<Evento> listarTodos() {
        return eventoDAO.listarTodos();
    }

    public List<Evento> listarPublicados() {
        return eventoDAO.listarPublicados();
    }

    public List<Evento> listarPorOrganizador(Long organizadorId) {
        return eventoDAO.listarPorOrganizador(organizadorId);
    }

    public static Evento buscarPorId(Long id) {
        return eventoDAO.buscarPorId(id);
    }
}