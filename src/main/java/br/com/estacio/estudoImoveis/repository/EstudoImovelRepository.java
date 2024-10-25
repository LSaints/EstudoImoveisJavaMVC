package br.com.estacio.estudoImoveis.repository;

import br.com.estacio.estudoImoveis.models.EstudoImovel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstudoImovelRepository extends JpaRepository<EstudoImovel, Long>  {
}
