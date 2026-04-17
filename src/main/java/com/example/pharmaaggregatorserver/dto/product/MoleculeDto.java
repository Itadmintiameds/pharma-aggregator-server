package com.example.pharmaaggregatorserver.dto.product;

import lombok.Data;

@Data
public class MoleculeDto {

    private Long moleculeId;
    private String moleculeName;
    private String mechanismOfAction;
    private String primaryUse;
    private String drugSchedule;
    private String strength;

}
