package org.example;

import org.apache.commons.csv.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CsvMerger {
  // BOM 제거 메서드 추가
  private static Reader removeBOM(String filePath) throws IOException {
    InputStream inputStream = new FileInputStream(filePath);
    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    br.mark(1);
    if (br.read() != 0xFEFF) {
      br.reset(); // BOM이 없으면 리셋시킴
    }
    return br;
  }

  public static void main(String[] args) throws IOException {
    String path = "C:\\Users\\user\\Desktop\\real\\";

    // 격자 데이터 로드
    Reader readerGeom = removeBOM(path + "서울시격자_202412_보간완료.csv");
    CSVParser parserGeom = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(readerGeom);

    Map<String, String> gridGeometry = new HashMap<>();
    Map<String, String> gridHJDong = new HashMap<>();

    for (CSVRecord record : parserGeom) {
      gridGeometry.put(record.get("grid_id"), record.get("geometry"));
      gridHJDong.put(record.get("grid_id"), record.get("행정동명"));
    }

    // MCDA 결과 로드 
    Reader readerMCDA = removeBOM(path + "서울형키즈카페_MCDA결과.csv");
    CSVParser parserMCDA = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(readerMCDA);

    Map<String, String> mcdaScores = new HashMap<>();

    for (CSVRecord record : parserMCDA) {
      mcdaScores.put(record.get("행정동명"), record.get("MCDA_점수"));
    }

    // 병합 결과 저장
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + "merged_result.csv"), StandardCharsets.UTF_8));
    CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("grid_id", "geometry", "행정동명", "MCDA_점수"));

    for (String gridId : gridGeometry.keySet()) {
      String dongName = gridHJDong.get(gridId);
      String mcdaScore = mcdaScores.getOrDefault(dongName, "0");  

      csvPrinter.printRecord(gridId, gridGeometry.get(gridId), dongName, mcdaScore);
    }

    csvPrinter.flush();
    csvPrinter.close();

    System.out.println("병합 완료: merged_result.csv 파일이 성공적으로 생성되었습니다.");
  }
}
