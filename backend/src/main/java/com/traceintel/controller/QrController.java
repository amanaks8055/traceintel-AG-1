package com.traceintel.controller;

import com.google.zxing.WriterException;
import com.traceintel.exception.ResourceNotFoundException;
import com.traceintel.repository.SerialNumberRepository;
import com.traceintel.util.QRCodeUtil;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class QrController {

    private final SerialNumberRepository serialRepository;

    public QrController(SerialNumberRepository serialRepository) {
        this.serialRepository = serialRepository;
    }

    @GetMapping(value = "/api/qr/{serial}", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] qr(@PathVariable String serial) throws WriterException, IOException {
        serialRepository.findBySerial(serial)
                .orElseThrow(() -> new ResourceNotFoundException("Serial not found: " + serial));
        return QRCodeUtil.generate(serial, 240);
    }
}
