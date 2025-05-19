package org.example;

import org.geotools.geometry.jts.WKTReader2;
import org.geotools.map.*;
import org.geotools.swing.JMapFrame;
import org.geotools.feature.simple.*;
import org.geotools.styling.*;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.*;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.apache.commons.csv.*;
import org.opengis.filter.expression.Function;
import org.opengis.filter.FilterFactory2;
import org.geotools.factory.CommonFactoryFinder;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class MapViewer {
  public static void main(String[] args) throws Exception {
    String path = "C:\\Users\\user\\Desktop\\real\\서울형키즈카페_MCDA결과_보완완료_geometry.csv";

    SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
    typeBuilder.setName("Location");
    CoordinateReferenceSystem crs = CRS.decode("EPSG:4326", true);
    typeBuilder.setCRS(crs);
    typeBuilder.add("geometry", Geometry.class);
    typeBuilder.add("행정동명", String.class);
    typeBuilder.add("MCDA_점수", Double.class);
    final SimpleFeatureType TYPE = typeBuilder.buildFeatureType();

    ListFeatureCollection collection = new ListFeatureCollection(TYPE);

    Reader reader = new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8);
    CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
    WKTReader2 wktReader = new WKTReader2();

    double minScore = Double.MAX_VALUE, maxScore = Double.MIN_VALUE;

    for (CSVRecord record : parser) {
      String mcdaScoreStr = record.get("MCDA_점수");
      if (mcdaScoreStr.isEmpty()) continue; 
      double score = Double.parseDouble(mcdaScoreStr);
      minScore = Math.min(minScore, score);
      maxScore = Math.max(maxScore, score);
    }

    reader.close();
    reader = new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8);
    parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);

    for (CSVRecord record : parser) {
      String geometryStr = record.get("geometry");
      String dongName = record.get("행정동명");
      String mcdaScoreStr = record.get("MCDA_점수");

      if (geometryStr.isEmpty() || mcdaScoreStr.isEmpty()) {
        System.out.println("빈 값: " + dongName);
        continue;
      }

      Geometry geom = wktReader.read(geometryStr);
      Double mcdaScore = Double.parseDouble(mcdaScoreStr);

      SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
      featureBuilder.add(geom);
      featureBuilder.add(dongName);
      featureBuilder.add(mcdaScore);
      SimpleFeature feature = featureBuilder.buildFeature(null);

      collection.add(feature);
    }

    MapContent map = new MapContent();
    map.setTitle("서울형 키즈카페 MCDA 점수 시각화");

    StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
    FilterFactory2 filterFactory = CommonFactoryFinder.getFilterFactory2();

    Function colorFn = filterFactory.function("Interpolate",
        filterFactory.property("MCDA_점수"),
        filterFactory.literal(minScore), filterFactory.literal("#FFFFCC"),
        filterFactory.literal(maxScore), filterFactory.literal("#800026"),
        filterFactory.literal("color"), filterFactory.literal("linear"));

    Stroke stroke = styleFactory.createStroke(filterFactory.literal("#333333"), filterFactory.literal(0.2));
    Fill fill = styleFactory.createFill(colorFn, filterFactory.literal(0.7));

    PolygonSymbolizer symbolizer = styleFactory.createPolygonSymbolizer(stroke, fill, "geometry");
    Rule rule = styleFactory.createRule();
    rule.symbolizers().add(symbolizer);

    FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[]{rule});
    Style style = styleFactory.createStyle();
    style.featureTypeStyles().add(fts);

    Layer layer = new FeatureLayer(collection, style);
    map.addLayer(layer);

    JMapFrame frame = new JMapFrame(map);
    frame.enableToolBar(true);
    frame.enableStatusBar(true);
    frame.setSize(800, 600);
    frame.setVisible(true);
  }
}
