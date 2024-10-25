package br.com.estacio.estudoImoveis.services.Imoveis;

import br.com.estacio.estudoImoveis.models.EstudoImovel;
import br.com.estacio.estudoImoveis.models.Imovel;
import br.com.estacio.estudoImoveis.repository.EstudoImovelRepository;
import br.com.estacio.estudoImoveis.repository.ImovelRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.io.IOException;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ImovelServicesImpl implements ImovelServices {

    @Autowired
    private ImovelRepository imovelRepository;
    @Autowired
    private EstudoImovelRepository estudoImovelRepository;

    @Transactional
    public String SaveImovel(Imovel imovel, Model model) {
        double precom2 = imovel.getPreco() / imovel.getAreaUtil();
        imovel.setPrecom2(precom2);
        imovelRepository.save(imovel);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');

        return "redirect:/compare";
    }

    @Override
    public String Home(Model model) {
        return "home";
    }

    @Override
    public String GerarRelatorio(Model model, long imovelId, HttpServletResponse response) {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=relatorio-imovel.pdf");

        try (PdfWriter writer = new PdfWriter(response.getOutputStream());
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            // Obtenha os dados do imóvel
            Imovel imovel = imovelRepository.getById(imovelId);
            if (imovel == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Imóvel não encontrado");
                return "redirect:compare";
            }

            // Adicione o conteúdo ao PDF
            document.add(new Paragraph("Relatório de Imóvel"));
            document.add(new Paragraph("ID: " + imovel.getId()));
            document.add(new Paragraph("Nome: " + imovel.getNome()));
            // Adicione mais informações conforme necessário

        } catch (IOException e) {
            e.printStackTrace();
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao gerar o PDF");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        List<Imovel> allImoveis = fetchAllImoveis();
        List<Imovel> filteredImoveis = allImoveis.stream()
                .filter(imovel -> !imovel.isPrincipal())
                .collect(Collectors.toList());
        model.addAttribute("listImovel", filteredImoveis);

        Imovel imovel = getImovelById(imovelId);

        EstudoImovel estudoImovel = new EstudoImovel();
        estudoImovel.setImovel(imovel);
        estudoImovel.setPrecoMedio(calcularMediaPreco(filteredImoveis));
        estudoImovel.setAreaPrivMedia(calcularMediaAreaPriv(filteredImoveis));
        estudoImovel.setPrecom2Medio(calcularMediaPrecom2(filteredImoveis));

        estudoImovelRepository.save(estudoImovel);

        return "redirect:/compare";
    }

    public String Compare(Model model) {
        model.addAttribute("imovel", new Imovel());

        List<Imovel> allImoveis = fetchAllImoveis();
        List<Imovel> filteredImoveis = allImoveis.stream()
                .filter(imovel -> !imovel.isPrincipal())
                .collect(Collectors.toList());
        model.addAttribute("listImovel", filteredImoveis);

        List<Imovel> principaisImoveis = allImoveis.stream()
                .filter(Imovel::isPrincipal)
                .collect(Collectors.toList());
        model.addAttribute("listImovelPrincipais", principaisImoveis);

        return "compare";
    }

    public String DeleteImovel(Long id, Model model) {
        imovelRepository.deleteById(id);
        return "redirect:/compare";
    }

    @Override
    public List<Imovel> fetchAllImoveis() {
        return imovelRepository.findAll();
    }

    @Override
    public Imovel getImovelById(Long id) {
        Optional<Imovel> imovel = imovelRepository.findById(id);
        return imovel.orElse(null);
    }


    @Override
    public String deleteImovelById(Long id) {
        if (imovelRepository.findById(id).isPresent()) {
            imovelRepository.deleteById(id);
            return "Imovel foi deletado com sucesso";
        }
        return "Não foi encontrado o imovel no banco de dados";
    }

    private double calcularMediaPreco(List<Imovel> imoveis) {
        if (imoveis == null || imoveis.isEmpty()) {
            return 0.0;
        }

        double somaPreco = 0;

        for (Imovel imovel : imoveis) {
            somaPreco += imovel.getPreco();
        }

        return somaPreco / imoveis.size();
    }

    private double calcularMediaAreaPriv(List<Imovel> imoveis) {
        if (imoveis == null || imoveis.isEmpty()) {
            return 0.0;
        }

        double somaAreaPriv = 0;

        for (Imovel imovel : imoveis) {
            somaAreaPriv += imovel.getAreaUtil();
        }

        return somaAreaPriv / imoveis.size();
    }

    private double calcularMediaPrecom2(List<Imovel> imoveis) {
        if (imoveis == null || imoveis.isEmpty()) {
            return 0.0;
        }

        double somaPrecom2 = 0;

        for (Imovel imovel : imoveis) {
            somaPrecom2 += imovel.getPrecom2();
        }

        return somaPrecom2 / imoveis.size();
    }
}

