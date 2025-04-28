package source.model;
import java.util.*;

public class ResumeProfile {
    private Set<String> keywords;

    public ResumeProfile() {
        keywords = new HashSet<>();
    }

    public Set<String> getKeywords() {
        return keywords;
    }


}