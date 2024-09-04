package authsystem.controller;
import authsystem.services.ScheduledTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private ScheduledTask scheduledTask;

    @GetMapping("/testEmail")
    public String testEmail() {
        scheduledTask.sendDailyReportSummary();
        return "Email process triggered manually!";
    }
}
