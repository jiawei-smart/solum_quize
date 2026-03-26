package org.cms.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MortalityData {

    private String facilityId;
    private String facilityName;
    private String address;
    private String cityTown;
    private String state;
    private String zipCode;
    private String countyParish;
    private String telephoneNumber;
    private String measureId;
    private String measureName;
    private String comparedToNational;
    private int denominator;
    private double score;
    private double lowerEstimate;
    private double higherEstimate;
    private String footnote;
    private String startDate;
    private String endDate;
}
