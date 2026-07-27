package org.devopsnotes.kguard.ai.controller;

import org.devopsnotes.kguard.ai.dto.ServiceCapabilitiesResponse;
import org.devopsnotes.kguard.ai.service.ServiceCapabilitiesService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/service")
public class ServiceCapabilitiesController {

    private final ServiceCapabilitiesService serviceCapabilitiesService;

    public ServiceCapabilitiesController(ServiceCapabilitiesService serviceCapabilitiesService) {
        this.serviceCapabilitiesService = serviceCapabilitiesService;
    }

    @GetMapping("/capabilities")
    public ServiceCapabilitiesResponse capabilities() {
        return serviceCapabilitiesService.getCapabilities();
    }
}
