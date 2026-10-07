package com.example.pharmaaggregatorserver.dto.seller;

import com.example.pharmaaggregatorserver.entity.master.DistrictMaster;
import com.example.pharmaaggregatorserver.entity.master.StateMaster;
import com.example.pharmaaggregatorserver.entity.master.TalukaMaster;
import com.example.pharmaaggregatorserver.entity.seller.Seller;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class SellerAddressDTO {

    private Long sellerAddressId;

    private Long stateId;
    private String stateName;

    private Long districtId;
    private String districtName;

    private Long talukaId;
    private String talukaName;

    private String city;

    private String street;

    private String buildingNo;

    private String landmark;

    private String pinCode;
}
