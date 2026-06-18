package gcy.system.controller.admin;

import gcy.system.entity.dto.Result;
import gcy.system.entity.dto.SendNotificationFormDTO;
import gcy.system.service.INotificationService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/notification")
public class NotificationManageController {

    @Resource
    private INotificationService notificationService;

    @PostMapping("/send")
    public Result sendNotification(@RequestBody @Valid SendNotificationFormDTO dto) {
        return notificationService.sendNotification(dto);
    }

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestParam(required = false) String type) {
        return notificationService.getAllNotifications(current, size, type);
    }

    @PutMapping("/update/{id}")
    public Result update(@PathVariable Long id, @RequestBody @Valid SendNotificationFormDTO dto) {
        return notificationService.updateNotification(id, dto);
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        return notificationService.deleteNotification(id);
    }
}
