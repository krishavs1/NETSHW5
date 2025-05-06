package com.example.linkedinmaxx.parser;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A generic résumé–text parser.  Finds any top‑level ALL‑CAPS headings,
 * matches them to one of our Section types, and pulls out each section’s block.
 */
public class ResumeParser {

  public enum Section {
    EXPERIENCE,
    EDUCATION,
    SKILLS,
    AWARDS,
    PROJECTS
  }

  // —— header‑matching patterns for each Section ——  
  private static final Map<Section, List<Pattern>> HEADER_PATTERNS = Map.of(
    Section.EXPERIENCE, List.of(
      Pattern.compile("(?i)^(Work|Professional|Employment)\\s+Experience.*"),
      Pattern.compile("(?i)^Experience(s)?\\b.*"),
      Pattern.compile("(?i)^(Internship|Projects?)\\b.*")
    ),
    Section.EDUCATION, List.of(
      Pattern.compile("(?i)^Education\\b.*"),
      Pattern.compile("(?i)^(Academic|Training)\\s+Background.*")
    ),
    Section.SKILLS, List.of(
      Pattern.compile("(?i)^Skills?\\b.*"),
      Pattern.compile("(?i)^(Technical Skills|Core Competencies)\\b.*"),
      Pattern.compile("(?i)^(Certifications?)\\b.*"),
      Pattern.compile("(?i)^(Tools|Technologies|Tech\\s*Stack)\\b.*")
    ),
    Section.AWARDS, List.of(
      Pattern.compile("(?i)^Awards?\\b.*"),
      Pattern.compile("(?i)^(Honors?|Recognition)\\b.*")
    ),
    Section.PROJECTS, List.of(
      Pattern.compile("(?i)^Projects?\\b.*"),
      Pattern.compile("(?i)^(Leadership|Activities)\\b.*")
    )
  );

  private final String fullText;
  private final Map<Section,String> sections;

  /**
   * Parse the given PDF (or plain‑text) stream into sections.
   */
  public ResumeParser(InputStream pdfStream) throws IOException, TikaException {
    this.fullText = extractText(pdfStream);
    this.sections = splitIntoSections(fullText);
  }

  /** Return the raw block of text under an ALL‑CAPS Experience header (or empty). */
  public List<String> getExperiences() {
    return splitBlock(sections.get(Section.EXPERIENCE));
  }

  /** Return the raw block of text under an ALL‑CAPS Education header (or empty). */
  public List<String> getEducation() {
    return splitBlock(sections.get(Section.EDUCATION));
  }

  /**
   * Returns a de‑duplicated set of skills/tokens by splitting on comma/semicolon/newline.
   */
  public Set<String> getSkills() {
    String blk = sections.get(Section.SKILLS);
    if (blk == null || blk.isBlank()) return Collections.emptySet();

    return Arrays.stream(blk.split("[,;\\r?\\n]+"))
                 .map(String::trim)
                 .filter(tok -> tok.length() > 1)
                 .filter(tok -> !tok.matches("https?://.*"))
                 .filter(tok -> !tok.matches(".*\\.[a-z]{2,4}(/.*)?$"))
                 .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  /** Awards/Honors section as a list of lines/bullets. */
  public List<String> getAwards() {
    return splitBlock(sections.get(Section.AWARDS));
  }

  /** Projects / Leadership block as a list of lines/bullets. */
  public List<String> getProjects() {
    return splitBlock(sections.get(Section.PROJECTS));
  }

  // ───────────────────────────────────────────────────────────────────────────

  /** Use Tika to extract text from PDF (or fall back to raw stream). */
  private static String extractText(InputStream in) throws IOException, TikaException {
    return new Tika().parseToString(in);
  }

  /**
   * 1) Find every ALL‑CAPS line that matches one of our Section patterns.  
   * 2) Chop the text between that header and the next header.  
   */
  private static Map<Section,String> splitIntoSections(String text) {
    String[] lines = text.split("\\r?\\n");
    // collect header‐line indices → Section
    TreeMap<Integer,Section> headers = new TreeMap<>();
    Pattern allCaps = Pattern.compile("^[A-Z0-9 &\\-,'/]+$");
    for (int i = 0; i < lines.length; i++) {
      String L = lines[i].trim();
      if (!allCaps.matcher(L).matches()) continue;
      for (var entry : HEADER_PATTERNS.entrySet()) {
        for (Pattern p : entry.getValue()) {
          if (p.matcher(L).find()) {
            headers.put(i, entry.getKey());
            break;
          }
        }
        if (headers.containsKey(i)) break;
      }
    }

    // now slice out each block
    Map<Section,String> result = new EnumMap<>(Section.class);
    List<Integer> idxs = new ArrayList<>(headers.keySet());
    for (int k = 0; k < idxs.size(); k++) {
      int start = idxs.get(k);
      int end   = (k + 1 < idxs.size() ? idxs.get(k+1) : lines.length);
      Section sec = headers.get(start);

      StringBuilder sb = new StringBuilder();
      for (int j = start+1; j < end; j++) {
        sb.append(lines[j]).append("\n");
      }
      result.put(sec, sb.toString().trim());
    }
    return result;
  }

  /**
   * Turn a block of free‑form text into a list of cleaned bullets/lines.
   *   - splits on newlines (or on commas if it’s one‐liner)
   *   - strips any leading “•” or “-”
   */
  private static List<String> splitBlock(String block) {
    if (block == null || block.isBlank()) return Collections.emptyList();
    String[] raw = block.split("\\r?\\n");
    // if there’s exactly one line and it contains commas, split on commas
    if (raw.length == 1 && raw[0].contains(",")) {
      return Arrays.stream(raw[0].split(","))
                   .map(String::trim)
                   .filter(l -> !l.isEmpty())
                   .collect(Collectors.toList());
    }
    // otherwise treat each line as one item
    List<String> items = new ArrayList<>();
    for (String ln : raw) {
      String t = ln.trim().replaceAll("^[•\\-*]+\\s*", "");
      if (!t.isEmpty()) items.add(t);
    }
    return items;
  }
}
