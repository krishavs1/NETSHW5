// src/main/java/com/example/linkedinmaxx/app/TfidfService.java
package com.example.linkedinmaxx.app;

import com.example.linkedinmaxx.app.dao.ExperienceDao;
import com.example.linkedinmaxx.app.dao.SkillDao;
import com.example.linkedinmaxx.app.dao.User;
import com.example.linkedinmaxx.app.dao.UserDao;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.*;

/**
 * Builds TF–IDF vectors for each user’s profile (experiences + skills),
 * then allows cosine similarity lookups.
 */
public class TfidfService {
  private final Map<Integer, RealVector> userVectors = new HashMap<>();
  private final List<String> vocabulary  = new ArrayList<>();

  /**
   * @param userDao   to fetch all users
   * @param skillDao  to fetch each user's skills
   * @param expDao    to fetch each user's experience bullets
   */
  public TfidfService(UserDao userDao,
                      SkillDao skillDao,
                      ExperienceDao expDao)
      throws SQLException, IOException {
    // 1) Gather each user's combined text
    Map<Integer,String> docs = new HashMap<>();
    for (User u : userDao.findAll()) {
      int id = u.getId();
      String expText   = String.join(" ", expDao.findByUser(id));
      String skillText = String.join(" ", skillDao.findByUser(id));
      docs.put(id, expText + " " + skillText);
    }

    // 2) Tokenize all docs, compute TF and DF
    Analyzer analyzer = new StandardAnalyzer();
    Map<String,Integer> docFreq = new HashMap<>();
    Map<Integer, Map<String,Integer>> termFreqs = new HashMap<>();

    for (var entry : docs.entrySet()) {
      int uid = entry.getKey();
      String text = entry.getValue();

      Map<String,Integer> freq = new HashMap<>();
      try (TokenStream ts = analyzer.tokenStream("contents", new StringReader(text))) {
        CharTermAttribute attr = ts.addAttribute(CharTermAttribute.class);
        ts.reset();
        while (ts.incrementToken()) {
          freq.merge(attr.toString(), 1, Integer::sum);
        }
        ts.end();
      }
      termFreqs.put(uid, freq);

      // update document frequencies
      for (String term : freq.keySet()) {
        docFreq.merge(term, 1, Integer::sum);
      }
    }
    analyzer.close();

    // 3) Sort vocabulary
    vocabulary.addAll(docFreq.keySet());
    Collections.sort(vocabulary);

    int numDocs = docs.size();
    // 4) Build each user's TF–IDF vector
    for (var entry : termFreqs.entrySet()) {
      int uid = entry.getKey();
      var freq = entry.getValue();

      double[] vec = new double[vocabulary.size()];
      for (int i = 0; i < vocabulary.size(); i++) {
        String term = vocabulary.get(i);
        int tf = freq.getOrDefault(term, 0);
        if (tf > 0) {
          int df = docFreq.get(term);
          double idf = Math.log((double)numDocs / (double)df);
          vec[i] = tf * idf;
        }
      }
      userVectors.put(uid, new ArrayRealVector(vec, false));
    }
  }

  /**
   * Cosine similarity between two users' TF–IDF vectors.
   * @return a value in [0,1], or 0 if either user is missing / has zero norm
   */
  public double cosine(int u1, int u2) {
    RealVector v1 = userVectors.get(u1);
    RealVector v2 = userVectors.get(u2);
    if (v1 == null || v2 == null) return 0.0;

    double dot  = v1.dotProduct(v2);
    double norm = v1.getNorm() * v2.getNorm();
    return (norm == 0.0 ? 0.0 : dot / norm);
  }
}
