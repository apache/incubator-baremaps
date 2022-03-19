## View a subset of records

    ogrinfo ABRALicenseePt_4326.shp -sql "select * from ABRALicenseePt_4326 where LICENSE='ABRA-089763' or LICENSE='ABRA-089388' or LICENSE='ABRA-088814'

    ogrinfo -al SignedBikeRoute_4326.shp -sql "select * from SignedBikeRoute_4326 where SIS_ID='19126020' or INT_SEQ='493'"

## Truncate records

    ogr2ogr SignedBikeRoute_4326_clipped.shp SignedBikeRoute_4326.shp -sql "select * from SignedBikeRoute_4326 where SIS_ID='19126020' or INT_SEQ='493'"
    

    ogr2ogr ABRALicenseePt_4326_clipped.shp ABRALicenseePt_4326.shp -sql "select * from ABRALicenseePt_4326 where LICENSE='ABRA-089763' or LICENSE='ABRA-089388' or LICENSE='ABRA-088814'"
