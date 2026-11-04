package com.traceintel.service;

import com.traceintel.dto.hold.HoldApprovalRequest;
import com.traceintel.dto.hold.HoldRequest;
import com.traceintel.dto.hold.QualityHoldResponse;

public interface HoldService {
    QualityHoldResponse requestHold(HoldRequest request, String username);
    QualityHoldResponse approveHold(Long holdId, HoldApprovalRequest request, String username);
}
