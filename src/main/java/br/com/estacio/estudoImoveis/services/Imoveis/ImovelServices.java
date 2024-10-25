package br.com.estacio.estudoImoveis.services.Imoveis;

import br.com.estacio.estudoImoveis.models.Imovel;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ui.Model;

import java.util.List;

public interface ImovelServices {
    String Compare(Model model);
    String SaveImovel(Imovel imovel, Model model);
    String Home(Model model);
    String GerarRelatorio(Model model, long imovelId, HttpServletResponse response);
    String DeleteImovel(Long id, Model model);
    List<Imovel> fetchAllImoveis();
    Imovel getImovelById(Long id);
    String deleteImovelById(Long id);
}
