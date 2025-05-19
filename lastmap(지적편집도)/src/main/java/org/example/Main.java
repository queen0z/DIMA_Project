package org.example;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

public class Main {
  public static void main(String[] args) {

    String geojsonPath = "C:/map_viewer/seoul_landuse.geojson";
    String csvOutputPath = "C:/map_viewer/seoul_landuse_wkt.csv";
    String htmlMapPath = "C:/map_viewer/map.html";

    try {
      File file = new File(geojsonPath);
      FeatureJSON featureJSON = new FeatureJSON();

      // GeoJSON 파일 로딩
      SimpleFeatureCollection features = (SimpleFeatureCollection) featureJSON.readFeatureCollection(file);

      // CSV 파일 생성 (UTF-8 BOM 포함)
      FileWriter csvWriter = new FileWriter(csvOutputPath, StandardCharsets.UTF_8);
      csvWriter.write('\ufeff'); // 한글 깨짐 방지
      csvWriter.append("DGM_NM,DGM_AR,CREATE_DAT,WKT\n");

      // 데이터 순회 및 CSV 파일에 저장
      try (SimpleFeatureIterator iterator = features.features()) {
        int count = 0;
        while (iterator.hasNext()) {
          SimpleFeature feature = iterator.next();
          count++;

          String dgm_nm = feature.getAttribute("DGM_NM") != null
              ? feature.getAttribute("DGM_NM").toString()
              : "";

          String dgm_ar = feature.getAttribute("DGM_AR") != null
              ? feature.getAttribute("DGM_AR").toString()
              : "";

          String create_dat = feature.getAttribute("CREATE_DAT") != null
              ? feature.getAttribute("CREATE_DAT").toString()
              : "";

          Geometry geom = (Geometry) feature.getDefaultGeometry();

          csvWriter.append("\"").append(dgm_nm).append("\",");
          csvWriter.append(dgm_ar).append(",");
          csvWriter.append("\"").append(create_dat).append("\",");
          csvWriter.append("\"").append(geom.toText()).append("\"\n");
        }
        System.out.println("총 Feature 개수: " + count);
      }

      csvWriter.flush();
      csvWriter.close();

      System.out.println("CSV(WKT) 저장 완료: " + csvOutputPath);

      // 지도 HTML 파일 자동 열기
      File htmlMap = new File(htmlMapPath);
      if (htmlMap.exists()) {
        Desktop.getDesktop().browse(htmlMap.toURI());
        System.out.println("HTML 지도 열기 완료!");
      } else {
        System.out.println("HTML 지도 파일이 없습니다: " + htmlMapPath);
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
