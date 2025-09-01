package ar.edu.utn.dds.k3003.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PdIRepository extends JpaRepository<PdIEntity, Integer> {
    List<PdIEntity> findByHechoId(Integer hechoId);
}