package com.backend.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.modelos.RecetaEntity;

@Repository
public interface RecetaRepositorio extends JpaRepository<RecetaEntity, Long> {
    List<RecetaEntity> findByUsuarioId(Long usuarioId);
}
