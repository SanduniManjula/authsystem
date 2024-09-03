package authsystem.services;

import authsystem.entity.Permission;
import authsystem.entity.Role;
import authsystem.entity.User;
import authsystem.repository.PermissionRepository;
import authsystem.repository.RoleRepository;
import authsystem.repository.UserRepository;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.ListItem;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Tab;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
                .collect(Collectors.toList()), new String[]{"Username", "Role"}, reportType, "user");
    }



    public byte[] generateRoleReport(LocalDateTime fromDate, LocalDateTime toDate, String reportType) {
        List<Role> roles = roleRepository.findByCreatedOnBetweenOrUpdatedOnBetween(fromDate, toDate, fromDate, toDate);
        return generateReport(roles.stream()
                .map(role -> new String[]{role.getName(), String.join(", ", role.getPermissions().stream().map(p -> p.getName()).collect(Collectors.toList()))})
                .collect(Collectors.toList()), new String[]{"Role", "Permissions"}, reportType, "role");
    }



    private byte[] generateReport(List<String[]> data, String[] headers, String reportType, String reportCategory) {
        switch (reportType.toLowerCase()) {
            case "xlsx":
                return generateExcelReport(data, headers);
            case "csv":
                return generateCsvReport(data, headers);
            case "pdf":

                if ("user".equalsIgnoreCase(reportCategory)) {
                    return generateUserPdfReport(data, headers);
                } else if ("role".equalsIgnoreCase(reportCategory)) {
                    return generateRolePdfReport(data, headers);
                } else {
                    throw new IllegalArgumentException("Invalid report category: " + reportCategory);
                }
            default:
                throw new IllegalArgumentException("Invalid report type: " + reportType);
        }
    }


    private byte[] generateExcelReport(List<String[]> data, String[] headers) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Report");
            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
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


    public byte[] generateRolePdfReport(List<String[]> data, String[] headers) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);


            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            document.setFont(font);
            Paragraph title = new Paragraph("Role and Permission Report")
                    .setFontSize(18)
                    .setBold()
                    .setUnderline()
                    .setMarginBottom(20);
            document.add(title);

            for (String[] entry : data) {
                String roleName = entry[0];
                String permissions = entry[1];

                Paragraph roleTitle = new Paragraph("Role: " + roleName)
                        .setFontSize(14)
                        .setBold()
                        .setMarginTop(10)
                        .setMarginBottom(5);
                document.add(roleTitle);

                if (permissions != null && !permissions.isEmpty()) {

                    ListItem permissionList = new ListItem();


                    for (String permission : permissions.split(", ")) {
                        permissionList.add(new ListItem(permission.trim()));
                    }


                    document.add(permissionList);
                } else {

                    Paragraph noPermissions = new Paragraph("No permissions assigned to this role.")
                            .setItalic()
                            .setMarginLeft(20)
                            .setFontSize(12);
                    document.add(noPermissions);
                }
            }

            Paragraph closingNote = new Paragraph("End of Report")
                    .setFontSize(12)
                    .setItalic()
                    .setMarginTop(20);
            document.add(closingNote);

            document.close();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }


    public byte[] generateUserPdfReport(List<String[]> data, String[] headers) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            document.setFont(font);


            Paragraph title = new Paragraph("USER REPORT")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setUnderline()
                    .setMarginBottom(20);
            document.add(title);

            Table table = new Table(UnitValue.createPercentArray(headers.length)).useAllAvailableWidth();


            float[] columnWidths = new float[] { 3, 2 };
            table.setWidth(UnitValue.createPercentValue(100));


            for (String header : headers) {
                Cell headerCell = new Cell()
                        .add(new Paragraph(header).setBold())
                        .setBackgroundColor(new DeviceRgb(0, 102, 204))
                        .setFontColor(DeviceRgb.WHITE)
                        .setTextAlignment(TextAlignment.CENTER);
                table.addHeaderCell(headerCell);
            }


            for (String[] entry : data) {
                for (String cellData : entry) {
                    Cell cell = new Cell()
                            .add(new Paragraph(cellData))
                            .setTextAlignment(TextAlignment.CENTER);
                    table.addCell(cell);
                }
            }

            document.add(table);

/*
            for (String[] entry : data) {
                String username = entry[0];
                String roleName = entry[1];

                Paragraph userTitle = new Paragraph("Username: " + username)
                        .setFontSize(12)
                        .setBold()
                        .setMarginTop(10)
                        .setMarginBottom(5);
                document.add(userTitle);

                Paragraph userRole = new Paragraph("Role: " + roleName)
                        .setFontSize(12)
                        .setItalic()
                        .setMarginLeft(20)
                        .setMarginBottom(10);
                document.add(userRole);
            }

 */
            Paragraph closingNote = new Paragraph("******End of User Report******")
                    .setFontSize(12)
                    .setItalic()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(40);
            document.add(closingNote);

            document.close();

            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate User PDF report", e);
        }
    }
}
