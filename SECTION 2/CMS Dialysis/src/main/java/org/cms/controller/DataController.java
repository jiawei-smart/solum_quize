package org.cms.controller;

import org.cms.entity.SummaryParameter;
import org.cms.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cms")
public class DataController {
    @Autowired
    private DataService analysisService;

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(
            @RequestParam String measureId,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String zipCode,
            @RequestParam(required = false) String facilityName,
            @RequestParam(required = false, defaultValue = "1") String page,
            @RequestParam(required = false, defaultValue = "10") String pageSize) {
        SummaryParameter parameter = new SummaryParameter();
        return ResponseEntity.ok(analysisService.calculateSummary(measureId, year, month, state, zipCode, facilityName, Integer.parseInt(page), Integer.parseInt(pageSize)));
    }

    @GetMapping("/measureIds")
    public ResponseEntity<?> getMeasureIds() {
        return ResponseEntity.ok(analysisService.getAllMeasureIds());
    }

    @GetMapping("/facilities")
    public ResponseEntity<?> getFacilities() {
        return ResponseEntity.ok(analysisService.getAllFacilities());
    }

    @GetMapping("/states")
    public ResponseEntity<?> getStates() {
        return ResponseEntity.ok(analysisService.getAllStates());
    }

    @GetMapping("/zipCodes")
    public ResponseEntity<?> getZipCodes() {
        return ResponseEntity.ok(analysisService.getAllZipCodes());
    }


    @GetMapping("/analysis")
    public ResponseEntity<?> getAnalysisData(
            @RequestParam String measureId,
            @RequestParam (required = false) String facilityId,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String zipCode) {
        return ResponseEntity.ok(analysisService.getAnalysisData(measureId, facilityId, state, zipCode));
    }

}
