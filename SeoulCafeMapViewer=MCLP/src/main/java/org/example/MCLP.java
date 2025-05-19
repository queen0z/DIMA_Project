package org.example;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.*;
import org.apache.commons.csv.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MCLP {

  
  private static Reader removeBOM(String filePath) throws IOException {
    InputStream inputStream = new FileInputStream(filePath);
    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    br.mark(1);
    if (br.read() != 0xFEFF) {
      br.reset(); 
    }
    return br;
  }

  public static void main(String[] args) throws IOException {
    Loader.loadNativeLibraries();

   
    String path = "C:\\Users\\user\\Desktop\\real\\서울형키즈카페_MCDA결과_보완완료.csv";

    Reader reader = removeBOM(path);
    CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);

    List<String> gridIds = new ArrayList<>();
    List<Double> scores = new ArrayList<>();

    for (CSVRecord record : parser) {
      gridIds.add(record.get("행정동명"));
      scores.add(Double.parseDouble(record.get("MCDA_점수"))); 
    }

    // MCLP 모델링
    MPSolver solver = MPSolver.createSolver("SCIP");
    assert solver != null;

    int facilityCount = 270; // 설치 가능한 시설 수 
    int n = gridIds.size();
    MPVariable[] x = new MPVariable[n];

    for (int i = 0; i < n; i++) {
      x[i] = solver.makeBoolVar("x[" + i + "]");
    }

    // 제약조건 
    MPConstraint constraint = solver.makeConstraint(facilityCount, facilityCount);
    for (int i = 0; i < n; i++) {
      constraint.setCoefficient(x[i], 1);
    }

    // 목적함수
    MPObjective objective = solver.objective();
    for (int i = 0; i < n; i++) {
      objective.setCoefficient(x[i], scores.get(i));
    }
    objective.setMaximization();

    // 모델 해결
    MPSolver.ResultStatus resultStatus = solver.solve();

    if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
      System.out.println("최적 입지 추천 결과:");
      for (int i = 0; i < n; i++) {
        if (x[i].solutionValue() > 0.5) {
          System.out.println("추천 행정동: " + gridIds.get(i));
        }
      }
    } else {
      System.out.println("최적화 문제를 해결하지 못했습니다. 상태: " + resultStatus);
    }

    

    String outputCsvPath = "C:\\Users\\user\\Desktop\\real\\MCLP_추천결과_270개.csv";
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputCsvPath), StandardCharsets.UTF_8));
    CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("grid_id", "MCDA_점수"));

    System.out.println("최적 입지 추천 결과:");
    for (int i = 0; i < n; i++) {
      if (x[i].solutionValue() > 0.5) {
        System.out.println("추천 격자 ID: " + gridIds.get(i));
        csvPrinter.printRecord(gridIds.get(i), scores.get(i));
      }
    }

    csvPrinter.flush();
    csvPrinter.close();

  }
}

