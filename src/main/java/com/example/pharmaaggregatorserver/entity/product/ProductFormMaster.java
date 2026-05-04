package com.example.pharmaaggregatorserver.entity.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tm_product_form_master")
public class ProductFormMaster {

    @Id
    @Column(name = "product_form_id")
    private Long productFormId;

    @Column(name = "product_form")
    private String productForm;

}
