package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import ar.edu.utn.dds.k3003.persistence.PdIEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PdIRepository extends JpaRepository<PdIEntity, Integer> {
    
    List<PdIEntity> findByHechoId(String hechoId);
    List<PdIEntity> findByProcesado(Boolean procesado);
}