package senior.copycoders.project.api.services;


import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.layout.LayoutArea;
import com.itextpdf.layout.properties.*;
import com.itextpdf.layout.renderer.IRenderer;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import senior.copycoders.project.api.controllers.helpers.ControllerHelper;
import senior.copycoders.project.store.entities.CreditEntity;
import senior.copycoders.project.store.entities.PaymentEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
public class PdfService {

    ControllerHelper controllerHelper;

    public ResponseEntity<ByteArrayResource> generatePdfOfPayments(Long creditId) throws IOException {
        // получаем желаемый кредит по id
        CreditEntity credit = controllerHelper.getCreditOrThrowException(creditId);

        // список платежей
        List<PaymentEntity> payments = credit.getPaymentList();
        Collections.sort(payments);

        // Генерируем PDF в ByteArrayOutputStream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(baos));
        Document doc = new Document(pdfDoc);

        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 2, 2, 2, 2, 2}));
        table.setWidth(UnitValue.createPercentValue(100));

        PdfFont gilRoyMedium = PdfFontFactory.createFont("src/main/resources/gilroy-medium.ttf", PdfEncodings.IDENTITY_H);

        // Путь к файлу логотипа
//        String logoPath = "src/main/resources/logo.png";

        String logoPath = "src/main/resources/logo.png";

        // Создание объекта Image
        Image logo = new Image(ImageDataFactory.create(logoPath));

        // Установка размеров логотипа
        logo.setWidth(471);
        logo.setHeight(136);


        logo.setHorizontalAlignment(HorizontalAlignment.CENTER);

        // Добавление логотипа в документ
        doc.add(logo);


        Paragraph creditAmount = new Paragraph("Сумма кредита - " + credit.getCreditAmount()).setFont(gilRoyMedium);
        Paragraph initialPayment = new Paragraph("Начальный платёж - " + credit.getInitialPayment()).setFont(gilRoyMedium);
        Paragraph percentRate = new Paragraph("Годовая процентная ставка - " + credit.getPercentRate()).setFont(gilRoyMedium);
        Paragraph creditPeriod = new Paragraph("Срок кредитования (в месяцах) - " + credit.getCreditPeriod()).setFont(gilRoyMedium);
        doc.add(creditAmount);
        doc.add(initialPayment);
        doc.add(percentRate);
        creditPeriod.setMarginBottom(50f);
        doc.add(creditPeriod);

        Paragraph tableTitle = new Paragraph("График платежей").setFont(gilRoyMedium);
        tableTitle.setTextAlignment(TextAlignment.CENTER);
        tableTitle.setFontSize(40);
        tableTitle.setBold();
        doc.add(tableTitle);

        // Добавление заголовков колонок
        Cell dateOfPayment = new Cell().add(new Paragraph("Дата платежа").setFont(gilRoyMedium));
        dateOfPayment.setVerticalAlignment(VerticalAlignment.MIDDLE);
        dateOfPayment.setTextAlignment(TextAlignment.CENTER);
        table.addCell(dateOfPayment);


        Cell creditBeforePayment = new Cell().add(new Paragraph("Долг до платежа").setFont(gilRoyMedium));
        creditBeforePayment.setVerticalAlignment(VerticalAlignment.MIDDLE);
        creditBeforePayment.setTextAlignment(TextAlignment.CENTER);
        table.addCell(creditBeforePayment);

        Cell payment = new Cell().add(new Paragraph("Сумма платежа").setFont(gilRoyMedium));
        payment.setVerticalAlignment(VerticalAlignment.MIDDLE);
        payment.setTextAlignment(TextAlignment.CENTER);
        table.addCell(payment);


        Cell percent = new Cell().add(new Paragraph("Погашение процентов").setFont(gilRoyMedium));
        percent.setVerticalAlignment(VerticalAlignment.MIDDLE);
        percent.setTextAlignment(TextAlignment.CENTER);
        table.addCell(percent);


        Cell repaymentCredit = new Cell().add(new Paragraph("Погашение основного долга").setFont(gilRoyMedium));
        repaymentCredit.setVerticalAlignment(VerticalAlignment.MIDDLE);
        repaymentCredit.setTextAlignment(TextAlignment.CENTER);
        table.addCell(repaymentCredit);

        Cell creditAfterPayment = new Cell().add(new Paragraph("Долг после платежа").setFont(gilRoyMedium));
        creditAfterPayment.setVerticalAlignment(VerticalAlignment.MIDDLE);
        creditAfterPayment.setTextAlignment(TextAlignment.CENTER);
        table.addCell(creditAfterPayment);



        for (int i = 0; i < payments.size(); i++) {
            PaymentEntity currentPayment = payments.get(i);
//
            table.addCell(new Cell().add(new Paragraph(String.valueOf(currentPayment.getPaymentDate())).setFont(gilRoyMedium).setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE)));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(currentPayment.getBeforePayment())).setFont(gilRoyMedium).setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE)));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(currentPayment.getPaymentAmount())).setFont(gilRoyMedium).setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE)));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(currentPayment.getPercent().setScale(2, RoundingMode.HALF_EVEN))).setFont(gilRoyMedium).setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE)));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(currentPayment.getRepaymentCredit())).setFont(gilRoyMedium).setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE)));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(currentPayment.getAfterPayment())).setFont(gilRoyMedium).setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE)));
//
        }

        doc.add(table);
        doc.close();

        // Создаем ByteArrayResource из ByteArrayOutputStream
        ByteArrayResource resource = new ByteArrayResource(baos.toByteArray());

        // Возвращаем PDF как ResponseEntity
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=persons.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(resource.contentLength())
                .body(resource);
    }
}
