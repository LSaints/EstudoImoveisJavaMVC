package br.com.estacio.estudoImoveis.controller;

import br.com.estacio.estudoImoveis.models.Imovel;
import br.com.estacio.estudoImoveis.services.Imoveis.ImovelServices;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ImovelController implements ImovelServices {

    @Autowired
    private ImovelServices imovelServices;

    @Override
    @GetMapping("/")
    public String Home(Model model) {
        Imovel imovel = new Imovel();
        imovel.setPrincipal(true);
        model.addAttribute("imovel", imovel);
        return imovelServices.Home(model);
    }



    @Override
    @GetMapping("/gerar-relatorio")
    public String GerarRelatorio(Model model, @RequestParam long imovelId, HttpServletResponse response) {
        imovelServices.GerarRelatorio(model, imovelId, response);
        return null;
    }

    @GetMapping("/compare")
    public String Compare(Model model) {
        return imovelServices.Compare(model);
    }

    @Override
    @RequestMapping(path = "/imovel", method = RequestMethod.POST)
    public String SaveImovel(@ModelAttribute Imovel imovel, Model model) {
        double precom2 = imovel.getPreco() / imovel.getAreaUtil();
        imovel.setPrecom2(precom2);
        return imovelServices.SaveImovel(imovel, model);
    }

    @Override
    @GetMapping("/imovel")
    public List<Imovel> fetchAllImoveis() {
        return  imovelServices.fetchAllImoveis();
    }

    @Override
    @RequestMapping(path = "/imovel/{id}", method = RequestMethod.POST)
    public String DeleteImovel(@PathVariable Long id, Model model) {
        imovelServices.deleteImovelById(id);
        return "redirect:/compare";
    }


    @Override
    public Imovel getImovelById(Long id) {
        return null;
    }

    @Override
    public String deleteImovelById(Long id) {
        return "";
    }
}
