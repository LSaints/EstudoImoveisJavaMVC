package br.com.estacio.estudoImoveis.services.Imoveis;

import br.com.estacio.estudoImoveis.models.EstudoImovel;
import br.com.estacio.estudoImoveis.models.Imovel;
import br.com.estacio.estudoImoveis.repository.EstudoImovelRepository;
import br.com.estacio.estudoImoveis.repository.ImovelRepository;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
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

            pdf.setDefaultPageSize(PageSize.A4);
            document.setMargins(20, 20, 20, 20);

            PdfFont titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont contentFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            Paragraph header = new Paragraph("Relatório de Imóvel")
                    .setFont(titleFont)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(header);

            Imovel imovel = imovelRepository.getById(imovelId);

            if (imovel == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Imóvel não encontrado");
                return "redirect:/compare";
            }

            document.add(new Paragraph("ID do Imóvel: " + imovel.getId())
                    .setFont(contentFont)
                    .setFontSize(12)
                    .setMarginBottom(5));
            document.add(new Paragraph("Nome: " + imovel.getNome())
                    .setFont(contentFont)
                    .setFontSize(12)
                    .setMarginBottom(5));

            document.add(new LineSeparator(new SolidLine()));

            Table table = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
            table.addHeaderCell(new Cell().add(new Paragraph("Detalhe").setFont(titleFont)));
            table.addHeaderCell(new Cell().add(new Paragraph("Valor").setFont(titleFont)));

            table.addCell("Área Total");
            table.addCell(String.valueOf(imovel.getAreaUtil()) + " m²");

            table.addCell("Preço");
            table.addCell("R$ " + String.format("%.2f", imovel.getPreco()));

            document.add(table);

            List<Imovel> allImoveis = fetchAllImoveis();
            List<Imovel> filteredImoveis = allImoveis.stream()
                    .filter(imoveis -> !imoveis.isPrincipal())
                    .collect(Collectors.toList());
            model.addAttribute("listImovel", filteredImoveis);

            EstudoImovel estudoImovel = new EstudoImovel();
            estudoImovel.setImovel(imovel);
            estudoImovel.setPrecoMedio(calcularMediaPreco(filteredImoveis));
            estudoImovel.setAreaPrivMedia(calcularMediaAreaPriv(filteredImoveis));
            estudoImovel.setPrecom2Medio(calcularMediaPrecom2(filteredImoveis));

            estudoImovelRepository.save(estudoImovel);

            Paragraph analisePrecoMedio = new Paragraph("Preço Médio: R$ " + String.format("%.2f", estudoImovel.getPrecoMedio()))
                    .setFont(contentFont)
                    .setFontSize(12)
                    .setMarginTop(10);
            document.add(analisePrecoMedio);

            Paragraph analiseAreaMedia = new Paragraph("Área Privada Média: " + String.format("%.2f", estudoImovel.getAreaPrivMedia()) + " m²")
                    .setFont(contentFont)
                    .setFontSize(12);
            document.add(analiseAreaMedia);

            Paragraph analisePrecoMetro2 = new Paragraph("Preço m2 médio: R$" + String.format("%.2f", estudoImovel.getPrecom2Medio()))
                    .setFont(contentFont)
                    .setFontSize(12);
            document.add(analisePrecoMetro2);

            Paragraph conclusion = new Paragraph("Este relatório foi gerado com base nas informações mais recentes.")
                    .setFont(contentFont)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20)
                    .setItalic();
            document.add(conclusion);

        } catch (IOException e) {
            e.printStackTrace();
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao gerar o PDF");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

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

        int listSize = imoveis.size() + 1;
        return somaPreco / listSize;
    }

    private double calcularMediaAreaPriv(List<Imovel> imoveis) {
        if (imoveis == null || imoveis.isEmpty()) {
            return 0.0;
        }

        double somaAreaPriv = 0;

        for (Imovel imovel : imoveis) {
            somaAreaPriv += imovel.getAreaUtil();
        }

        int listCount = imoveis.size() + 1;
        return somaAreaPriv / listCount;
    }

    private double calcularMediaPrecom2(List<Imovel> imoveis) {
        if (imoveis == null || imoveis.isEmpty()) {
            return 0.0;
        }

        double somaPrecom2 = 0;

        for (Imovel imovel : imoveis) {
            somaPrecom2 += imovel.getPrecom2();
        }
        int listCount = imoveis.size() + 1;
        return somaPrecom2 / listCount;
    }
}

