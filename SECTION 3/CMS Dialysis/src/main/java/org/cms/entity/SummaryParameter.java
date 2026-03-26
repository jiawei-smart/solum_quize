package org.cms.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SummaryParameter {

    private String measureId;
    private String year;
    private String month;
    private String state;
    private String zipCode;
    private String facilityName;
    private int page;
    private int pageSize;
}
