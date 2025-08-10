package com.teamsync.userpi.service.impl;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.teamsync.userpi.entity.CertificateRequest;
import com.teamsync.userpi.entity.CertificateRequestPayload;
import com.teamsync.userpi.entity.CertificateStatus;
import com.teamsync.userpi.entity.User;
import com.teamsync.userpi.repository.CertificateRequestRepository;
import com.teamsync.userpi.repository.UserRepository;
import com.teamsync.userpi.service.interfaces.CertificateRequestService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateRequestServiceImpl implements CertificateRequestService {

    private final CertificateRequestRepository requestRepo;
    private final UserRepository userRepo;
    private final JavaMailSender mailSender;

    @Override
    public CertificateRequest requestCertificate(String internId, CertificateRequestPayload payload) {
        User intern = userRepo.findById(internId)
                .orElseThrow(() -> new RuntimeException("Intern not found: " + internId));

        CertificateRequest request = CertificateRequest.builder()
                .internId(intern.getId())
                .internName(intern.getFullName())
                .type(payload.getType())
                .reason(payload.getReason())
                .status(CertificateStatus.REQUESTED)
                .requestedAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        CertificateRequest saved = requestRepo.save(request);
        log.info("✅ Saved CertificateRequest in Mongo: {}", saved.getId());
        return saved;
    }

    @Override
    public List<CertificateRequest> getAllRequests() {
        return requestRepo.findAll();
    }

    @Override
    public List<CertificateRequest> getRequestsByInternId(String internId) {
        return requestRepo.findByInternId(internId);
    }

    @Override
    public CertificateRequest approveRequest(String requestId) {
        CertificateRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));

        req.setStatus(CertificateStatus.APPROVED);
        req.setUpdatedAt(Instant.now());
        CertificateRequest saved = requestRepo.save(req);

        User intern = userRepo.findById(req.getInternId())
                .orElseThrow(() -> new RuntimeException("Intern not found: " + req.getInternId()));

        byte[] pdfBytes = generateCertificatePDF(intern, saved);
        sendCertificateEmail(intern.getEmail(), pdfBytes, saved.getType(), intern.getFullName());

        return saved;
    }

    @Override
    public CertificateRequest rejectRequest(String requestId) {
        CertificateRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));
        req.setStatus(CertificateStatus.REJECTED);
        req.setUpdatedAt(Instant.now());
        return requestRepo.save(req);
    }

    @Override
    public CertificateRequest updateStatus(String id, String status) {
        CertificateRequest req = requestRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Certificate request not found: " + id));

        CertificateStatus newStatus = CertificateStatus.valueOf(status.toUpperCase());
        req.setStatus(newStatus);
        req.setUpdatedAt(Instant.now());
        return requestRepo.save(req);
    }

    @Override
    public void deleteRequest(String requestId) {
        requestRepo.deleteById(requestId);
        log.info("Deleted certificate request {}", requestId);
    }

    private byte[] generateCertificatePDF(User intern, CertificateRequest req) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            pdfDoc.setDefaultPageSize(PageSize.A4);

            Document doc = new Document(pdfDoc);
            doc.setMargins(50, 50, 50, 50);

            var fontRegular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            var fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            Color stegBlue = new DeviceRgb(0, 98, 204);
            Color gray = new DeviceRgb(100, 100, 100);

            // Styles
            Style titleStyle = new Style()
                    .setFont(fontBold).setFontSize(26)
                    .setFontColor(stegBlue)
                    .setTextAlignment(TextAlignment.CENTER);

            Style subtitleStyle = new Style()
                    .setFont(fontBold).setFontSize(14)
                    .setFontColor(gray)
                    .setTextAlignment(TextAlignment.CENTER);

            Style bodyStyle = new Style()
                    .setFont(fontRegular).setFontSize(12)
                    .setTextAlignment(TextAlignment.LEFT);

            Style centerBodyStyle = new Style()
                    .setFont(fontRegular).setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER);

            // Header
            doc.add(new Paragraph("STEG").addStyle(titleStyle).setMarginBottom(10));
            doc.add(new Paragraph("Société Tunisienne de l'Électricité et du Gaz").addStyle(subtitleStyle));
            doc.add(new Paragraph("Internship Certificate").addStyle(titleStyle).setMarginTop(30));

            // Body
            doc.add(new Paragraph("\nThis is to certify that:")
                    .addStyle(centerBodyStyle).setMarginTop(30));

            doc.add(new Paragraph(intern.getFullName())
                    .addStyle(new Style().setFont(fontBold).setFontSize(16).setTextAlignment(TextAlignment.CENTER).setFontColor(stegBlue))
                    .setMarginBottom(10));

            doc.add(new Paragraph("has successfully completed an internship at STEG, demonstrating professionalism and dedication.")
                    .addStyle(centerBodyStyle).setMarginBottom(20));

            doc.add(new Paragraph("Certificate Type: " + req.getType())
                    .addStyle(centerBodyStyle).setFontColor(gray).setItalic().setMarginBottom(10));

            doc.add(new Paragraph("Issued on: " + LocalDate.now())
                    .addStyle(centerBodyStyle).setFontColor(gray).setMarginBottom(30));

            // Line Separator
            doc.add(new Paragraph("__________________________________________________")
                    .addStyle(centerBodyStyle).setFontColor(new DeviceRgb(180, 180, 180)).setMarginBottom(30));

            // Signature
            doc.add(new Paragraph("STEG HR Department")
                    .addStyle(centerBodyStyle).setMarginTop(40).setFont(fontBold).setFontSize(13));

            doc.add(new Paragraph("Official Signature")
                    .addStyle(centerBodyStyle).setFont(fontRegular).setFontSize(11).setFontColor(gray).setMarginTop(5));

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }

    private void sendCertificateEmail(String email, byte[] pdfBytes, String type, String internName) {
        if (!StringUtils.hasText(email)) return;
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("Your STEG Internship Certificate");
            helper.setText("Dear " + internName + ",\n\nYour certificate request (" + type + ") has been approved.\n\nRegards,\nSTEG HR");
            helper.addAttachment("STEG_Certificate.pdf", new ByteArrayResource(pdfBytes));
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Email sending failed", e);
        }
    }
}
