package br.com.estacio.estudoImoveis.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_imovel")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Imovel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = true)
    private String link;

    private String nome;
    private String imagemUrl;
    private String endereco;
    private String bairro;
    private double areaUtil;
    private int dormitorios;
    private int suites;
    private int banheiros;
    private int vagas;
    private double preco;
    private double precom2;
    private boolean principal;

    public Imovel(String link) {
        this.link = link;
    }

    public Imovel(
            String nome,
            String endereco,
            String bairro,
            double areaUtil,
            int dormitorios,
            int suites,
            int banheiros,
            int vagas,
            double preco,
            double precom2,
            boolean principal
    ) {
        this.nome = nome;
        this.endereco = endereco;
        this.bairro = bairro;
        this.areaUtil = areaUtil;
        this.dormitorios = dormitorios;
        this.suites = suites;
        this.banheiros = banheiros;
        this.vagas = vagas;
        this.preco = preco;
        this.precom2 = precom2;
        this.principal = principal;
    }
}
