package authsystem.services;

import authsystem.entity.Role;
import authsystem.entity.User;
import authsystem.repository.PermissionRepository;
import authsystem.repository.RoleRepository;
import authsystem.repository.UserRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.element.Paragraph;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.Document;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;


    public byte[] generateUserReport(LocalDateTime fromDate, LocalDateTime toDate, String reportType) {
        List<User> users = userRepository.findByCreatedOnBetweenOrUpdatedOnBetween(fromDate, toDate, fromDate, toDate);
        return generateReport(users.stream()
                .map(user -> new String[]{user.getUsername(), user.getRole().getName()})
                .collect(Collectors.toList()), new String[]{"Username", "Role"}, reportType);
    }


    public byte[] generateRoleReport(LocalDateTime fromDate, LocalDateTime toDate, String reportType) {
        List<Role> roles = roleRepository.findByCreatedOnBetweenOrUpdatedOnBetween(fromDate, toDate, fromDate, toDate);
        return generateReport(roles.stream()
                .map(role -> new String[]{role.getName(), String.join(", ", role.getPermissions().stream().map(p -> p.getName()).collect(Collectors.toList()))})
                .collect(Collectors.toList()), new String[]{"Role", "Permissions"}, reportType);
    }


    private byte[] generateReport(List<String[]> data, String[] headers, String reportType) {
        switch (reportType.toLowerCase()) {
            case "xlsx":
                return generateExcelReport(data, headers);
            case "csv":
                return generateCsvReport(data, headers);
            case "pdf":
                return generatePdfReport(data, headers);
            default:
                throw new IllegalArgumentException("Invalid report type: " + reportType);
        }
    }


    private byte[] generateExcelReport(List<String[]> data, String[] headers) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Report");
            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (String[] rowData : data) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < rowData.length; i++) {
                    row.createCell(i).setCellValue(rowData[i]);
                }
            }

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                workbook.write(out);
                return out.toByteArray();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }


    private byte[] generateCsvReport(List<String[]> data, String[] headers) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", headers)).append("\n");
        for (String[] row : data) {
            sb.append(String.join(",", row)).append("\n");
        }
        return sb.toString().getBytes();
    }


    public byte[] generatePdfReport(List<String[]> data, String[] headers) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }
}
