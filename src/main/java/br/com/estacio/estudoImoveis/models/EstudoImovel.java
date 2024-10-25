package br.com.estacio.estudoImoveis.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_estudo_imovel")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstudoImovel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "imovel_id", nullable = false)
    private Imovel imovel;

    private double precoMedio;
    private double precom2Medio;
    private double areaPrivMedia;
    private double precoOtimista;
    private double precoMercado;
    private double precoCompetitivo;
}
