package com.example.pharmaaggregatorserver.entity.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_serving_size_master")
public class ServingSizeUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "serving_size")
    private String servingSizeUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dosage_form_id")
    @JsonIgnore
    private DosageForm dosageForm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_form_id")
    @JsonIgnore
    private ProductFormMaster productForm;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
