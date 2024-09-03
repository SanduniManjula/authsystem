package authsystem.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduledTask {

    @Autowired
    private ReportService reportService;

    @Autowired
    private EmailService emailService;

    @Scheduled(cron = "0 0 12 * * ?")
    public void sendDailyReportSummary() {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(1);
        LocalDateTime toDate = LocalDateTime.now();


        byte[] userReportPdf = reportService.generateUserReport(fromDate, toDate, Arrays.toString(new String[]{"Username", "Role"}));

        byte[] roleReportPdf = reportService.generateRoleReport(fromDate, toDate, Arrays.toString(new String[]{"Role", "Permissions"}));


        String reportSummary = "Daily Summary Report\n\n" +
                "User Report: file:///C:/Users/User/Downloads/users_report.pdf\n" +
                "Role Report: file:///C:/Users/User/Downloads/roles_report.pdf\n";


        emailService.sendSummaryEmail(
                "sandunimanju.27@gmail.com",
                "Daily Report Summary",
                reportSummary
        );
    }
}
