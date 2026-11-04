package com.traceintel.controller;

import com.traceintel.dto.trace.BackwardTraceRequest;
import com.traceintel.dto.trace.BackwardTraceResponse;
import com.traceintel.dto.trace.ForwardTraceRequest;
import com.traceintel.dto.trace.TraceResponse;
import com.traceintel.service.TraceService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trace")
public class TraceController {

    private final TraceService traceService;

    public TraceController(TraceService traceService) {
        this.traceService = traceService;
    }

    @PostMapping("/forward")
    public TraceResponse forward(
            @Valid @RequestBody ForwardTraceRequest request,
            @AuthenticationPrincipal UserDetails user
    ) {
        return traceService.forwardTrace(request, user.getUsername());
    }

    @PostMapping("/backward")
    public BackwardTraceResponse backward(
            @Valid @RequestBody BackwardTraceRequest request,
            @AuthenticationPrincipal UserDetails user
    ) {
        return traceService.backwardTrace(request, user.getUsername());
    }
}
