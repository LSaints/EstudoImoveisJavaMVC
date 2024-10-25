package br.com.estacio.estudoImoveis.repository;

import br.com.estacio.estudoImoveis.models.Imovel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImovelRepository extends JpaRepository<Imovel, Long> {

}
