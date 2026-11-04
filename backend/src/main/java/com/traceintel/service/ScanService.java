package com.traceintel.service;

import com.traceintel.dto.scan.MaterialLotScanRequest;
import com.traceintel.dto.scan.ScanRequest;
import com.traceintel.dto.scan.ScanResponse;

public interface ScanService {
    ScanResponse validateScan(ScanRequest request, String username);
    void recordMaterialLotScan(MaterialLotScanRequest request, String username);
}
