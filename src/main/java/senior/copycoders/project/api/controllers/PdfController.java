package senior.copycoders.project.api.controllers;

import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.properties.UnitValue;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import senior.copycoders.project.api.controllers.helpers.ControllerHelper;
import senior.copycoders.project.api.dto.InitialDataOfCreditDto;
import senior.copycoders.project.api.exceptions.ErrorDto;
import senior.copycoders.project.api.services.PdfService;
import senior.copycoders.project.store.entities.CreditEntity;
import senior.copycoders.project.store.entities.PaymentEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@RestController
@Tag(name = "PDF", description = "Выгрузка pdf платежей")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PdfController {
    PdfService pdfService;

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/api/download-pdf/{credit_id}")
    @Operation(
            summary = "Скачивание платежей в pdf формате"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful response."),
            @ApiResponse(responseCode = "404", description = "Credit with {credit_id} not found.",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "Missing or invalid token.")
    })
    public ResponseEntity<ByteArrayResource> downloadPdf(@PathVariable(name = "credit_id") Long creditId) throws IOException {
        return pdfService.generatePdfOfPayments(creditId);
    }




}
