package com.example.pharmaaggregatorserver.entity.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

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

//    @OneToMany(mappedBy = "productFormMaster", cascade = CascadeType.ALL, orphanRemoval = true)
//    @JsonIgnore
//    private Set<ProductAttributeFoodInfant> productAttributeFoodInfants;

}
