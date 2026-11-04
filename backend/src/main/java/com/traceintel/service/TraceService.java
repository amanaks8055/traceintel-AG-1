package com.traceintel.service;

import com.traceintel.dto.trace.BackwardTraceRequest;
import com.traceintel.dto.trace.ForwardTraceRequest;
import com.traceintel.dto.trace.TraceResponse;
import com.traceintel.dto.trace.BackwardTraceResponse;

public interface TraceService {
    TraceResponse forwardTrace(ForwardTraceRequest request, String username);
    BackwardTraceResponse backwardTrace(BackwardTraceRequest request, String username);
}
