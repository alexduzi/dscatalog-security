package com.devsuperior.dscatalog.projections;

import com.devsuperior.dscatalog.entities.Product;

public interface ProductProjection extends IdProjection<Long> {
    String getName();
}
