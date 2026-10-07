package com.example.pharmaaggregatorserver.entity.product;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tm_gst_percentage_master")
public class GstPercentageMaster {

    @Id
    @Column(name = "gst_percentage_id")
    private Long gstPercentageId;

    @Column(name = "gst_percentage_value", unique = true)
    private BigDecimal gstPercentageValue;

    @Column(name = "is_active")
    private Boolean isActive = true;

}
