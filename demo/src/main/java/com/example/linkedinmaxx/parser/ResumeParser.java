package com.example.linkedinmaxx.parser;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A résumé parser that splits the document into logical sections
 * based on flexible header patterns, without requiring strict ALL-CAPS.
 */
public class ResumeParser {

    public enum Section {
        EXPERIENCE,
        EDUCATION,
        SKILLS,
        AWARDS,
        PROJECTS
    }

    // Patterns to detect the start of each section (case-insensitive).
    private static final Map<Section, List<Pattern>> HEADER_PATTERNS = Map.of(
        Section.EXPERIENCE, List.of(
            Pattern.compile("(?i)^\\s*(Work|Professional|Employment)\\s+Experience[:\\s]?.*"),
            Pattern.compile("(?i)^\\s*Experience(s)?[:\\s]?.*"),
            Pattern.compile("(?i)^\\s*(Internship|Projects?)[:\\s]?.*")
        ),
        Section.EDUCATION, List.of(
            Pattern.compile("(?i)^\\s*Education[:\\s]?.*"),
            Pattern.compile("(?i)^\\s*(Academic|Training)\\s+Background[:\\s]?.*")
        ),
        Section.SKILLS, List.of(
            Pattern.compile("(?i)^\\s*Skills?[:\\s]?.*"),
            Pattern.compile("(?i)^\\s*(Technical Skills|Core Competencies)[:\\s]?.*"),
            Pattern.compile("(?i)^\\s*(Certifications?)[:\\s]?.*"),
            Pattern.compile("(?i)^\\s*(Tools|Technologies|Tech\\s*Stack)[:\\s]?.*")
        ),
        Section.AWARDS, List.of(
            Pattern.compile("(?i)^\\s*Awards?[:\\s]?.*"),
            Pattern.compile("(?i)^\\s*(Honors?|Recognition)[:\\s]?.*")
        ),
        Section.PROJECTS, List.of(
            Pattern.compile("(?i)^\\s*Projects?[:\\s]?.*"),
            Pattern.compile("(?i)^\\s*(Leadership|Activities)[:\\s]?.*")
        )
    );

    private final Map<Section, String> sections;
    private final String fullText;

    public ResumeParser(InputStream in) throws IOException, TikaException {
        this.fullText = extractText(in);
        this.sections = splitIntoSections(fullText);
    }

    public List<String> getExperiences() {
        return splitBlock(sections.getOrDefault(Section.EXPERIENCE, ""));
    }

    public List<String> getEducation() {
        return splitBlock(sections.getOrDefault(Section.EDUCATION, ""));
    }

    public Set<String> getSkills() {
        String block = sections.getOrDefault(Section.SKILLS, "");
        if (block.isBlank()) return Collections.emptySet();
        return Arrays.stream(block.split("[,;\\r?\\n]+"))
                .map(String::trim)
                .filter(tok -> tok.length() > 1)
                .filter(tok -> !tok.matches("https?://.*"))
                .filter(tok -> !tok.matches(".*\\.[a-z]{2,4}(/.*)?$"))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public List<String> getAwards() {
        return splitBlock(sections.getOrDefault(Section.AWARDS, ""));
    }

    public List<String> getProjects() {
        return splitBlock(sections.getOrDefault(Section.PROJECTS, ""));
    }

    private static String extractText(InputStream in) throws IOException, TikaException {
        return new Tika().parseToString(in);
    }

    private static Map<Section, String> splitIntoSections(String text) {
        String[] lines = text.split("\\r?\\n");
        Map<Integer, Section> headerIndices = new TreeMap<>();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            for (var entry : HEADER_PATTERNS.entrySet()) {
                for (Pattern pattern : entry.getValue()) {
                    if (pattern.matcher(line).find()) {
                        headerIndices.put(i, entry.getKey());
                        break;
                    }
                }
                if (headerIndices.containsKey(i)) break;
            }
        }

        Map<Section, String> result = new EnumMap<>(Section.class);
        List<Integer> positions = new ArrayList<>(headerIndices.keySet());

        for (int idx = 0; idx < positions.size(); idx++) {
            int start = positions.get(idx);
            int end = (idx + 1 < positions.size() ? positions.get(idx + 1) : lines.length);
            Section sec = headerIndices.get(start);

            StringBuilder sb = new StringBuilder();
            for (int j = start + 1; j < end; j++) {
                sb.append(lines[j]).append("\n");
            }
            result.put(sec, sb.toString().trim());
        }

        return result;
    }

    private static List<String> splitBlock(String block) {
        if (block == null || block.isBlank()) return Collections.emptyList();
        String[] raw = block.split("\\r?\\n");
        if (raw.length == 1 && raw[0].contains(",")) {
            return Arrays.stream(raw[0].split(","))
                    .map(String::trim)
                    .filter(l -> !l.isEmpty())
                    .collect(Collectors.toList());
        }
        List<String> items = new ArrayList<>();
        for (String line : raw) {
            String cleaned = line.trim().replaceAll("^[•\\-*]+\\s*", "");
            if (!cleaned.isEmpty()) items.add(cleaned);
        }
        return items;
    }
}