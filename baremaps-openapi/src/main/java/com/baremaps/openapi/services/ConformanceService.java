package com.baremaps.openapi.services;

import com.baremaps.api.ConformanceApi;
import com.baremaps.model.ConfClasses;

import java.util.Arrays;

public class ConformanceService implements ConformanceApi {

    @Override
    public ConfClasses getConformanceDeclaration() {

        ConfClasses confClasses = new ConfClasses();

        confClasses.conformsTo(Arrays.asList(
                "http://www.opengis.net/spec/ogcapi-common-1/1.0/conf/core",
                "http://www.opengis.net/spec/ogcapi-styles-1/1.0/conf/core",
                "http://www.opengis.net/spec/ogcapi-styles-1/1.0/conf/json",
                "http://www.opengis.net/spec/ogcapi-styles-1/1.0/conf/manage-styles",
//                "http://www.opengis.net/spec/ogcapi-styles-1/1.0/conf/style-validation",
//                "http://www.opengis.net/spec/ogcapi-styles-1/1.0/conf/resources",
//                "http://www.opengis.net/spec/ogcapi-styles-1/1.0/conf/manage-resources",
                "http://www.opengis.net/spec/ogcapi-styles-1/1.0/conf/mapbox-styles"
        ));

        return confClasses;
    }
}
