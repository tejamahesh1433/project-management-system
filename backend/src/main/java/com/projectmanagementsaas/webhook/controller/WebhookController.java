package com.projectmanagementsaas.webhook.controller;

import com.projectmanagementsaas.integration.entity.IntegrationType;
import com.projectmanagementsaas.webhook.dto.WebhookResponse;
import com.projectmanagementsaas.webhook.service.WebhookHandlerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {
    private final WebhookHandlerService webhookHandlerService;

    public WebhookController(WebhookHandlerService webhookHandlerService) {
        this.webhookHandlerService = webhookHandlerService;
    }

    @PostMapping("/github")
    ResponseEntity<WebhookResponse> github(@RequestBody(required = false) String payload) {
        int count = webhookHandlerService.handle(IntegrationType.GITHUB, payload);
        return ResponseEntity.ok(new WebhookResponse("github", "Processed " + count + " subscriptions"));
    }

    @PostMapping("/gitlab")
    ResponseEntity<WebhookResponse> gitlab(@RequestBody(required = false) String payload) {
        int count = webhookHandlerService.handle(IntegrationType.GITLAB, payload);
        return ResponseEntity.ok(new WebhookResponse("gitlab", "Processed " + count + " subscriptions"));
    }

    @PostMapping("/jenkins")
    ResponseEntity<WebhookResponse> jenkins(@RequestBody(required = false) String payload) {
        int count = webhookHandlerService.handle(IntegrationType.JENKINS, payload);
        return ResponseEntity.ok(new WebhookResponse("jenkins", "Processed " + count + " subscriptions"));
    }
}
