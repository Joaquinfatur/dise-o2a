package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.persistence.PdIEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PdIRepository extends JpaRepository<PdIEntity, Integer> {
    List<PdIEntity> findByHechoId(String hechoId);
    long countByProcesado(boolean procesado);
    Optional<PdIEntity> findByImagenUrl(String imagenUrl); 
}