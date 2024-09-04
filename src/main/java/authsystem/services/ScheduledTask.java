package authsystem.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ScheduledTask {

    @Autowired
    private ReportService reportService;

    @Autowired
    private EmailService emailService;

    private static final Logger log = LoggerFactory.getLogger(ScheduledTask.class);

    @Scheduled(cron = "0 0 12 * * ?")
    // 0 * * * * ?
    public void sendDailyReportSummary() {
      log.info("------------Running every minute------------");
      LocalDateTime fromDate = LocalDateTime.now().minusDays(1);
      LocalDateTime toDate = LocalDateTime.now();

        byte[] userReportPdf = reportService.generateUserReport(fromDate, toDate, "pdf");

        byte[] roleReportPdf = reportService.generateRoleReport(fromDate, toDate, "pdf");

        String reportSummary = "Daily Summary Report\n\n" +
                "Please find the attached User and Role reports for the selected period.\n\n" +
                "User Report: http://localhost:8084/users/report?fromDate=2024-09-01T00:00:00&toDate=2024-09-05T00:00:00&reportType=pdf\n" +
                "Role Report: http://localhost:8084/roles/report?fromDate=2024-09-01T00:00:00&toDate=2024-09-03T00:00:00&reportType=pdf\n";;

        emailService.sendSummaryEmail(
                "sandunimanju.27@gmail.com",
                "Daily Report Summary",
                reportSummary
               // userReportPdf,
               // roleReportPdf
        );

        log.info("Email sent with daily summary report.");
    }
}
