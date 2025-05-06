package com.example.linkedinmaxx.app;

import com.example.linkedinmaxx.app.dao.*;
import com.example.linkedinmaxx.parser.ResumeParser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.apache.tika.exception.TikaException;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Handles new‐user signups: parses their form & uploaded resume,
 * persists everything via DAOs, and returns the parsed resume JSON.
 */
@WebServlet("/api/signup")
@MultipartConfig
public class SignupServlet extends HttpServlet {

    private final UserDao       userDao     = new UserDao();
    private final ExperienceDao expDao      = new ExperienceDao();
    private final SkillDao      skillDao    = new SkillDao();
    private final FriendshipDao friendDao   = new FriendshipDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 1) Extract form fields (no DB calls here)
        String email      = req.getParameter("email");
        String password   = req.getParameter("password");
        String password2  = req.getParameter("password2");
        String username   = req.getParameter("username");
        String school     = req.getParameter("school");
        String major      = req.getParameter("major");
        String gradYearS  = req.getParameter("gradYear");
        String interests  = req.getParameter("interests");
        String bio        = req.getParameter("bio");
        String friendsTxt = req.getParameter("friendsText");

        // 2) Validate required fields (still no DB calls)
        if (email == null || username == null || password == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Email, username & password are required");
            return;
        }
        if (!password.equals(password2)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Passwords do not match");
            return;
        }

        // 3) Parse graduation year
        Integer gradYear = null;
        if (gradYearS != null && !gradYearS.isBlank()) {
            try {
                gradYear = Integer.parseInt(gradYearS);
            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid graduation year");
                return;
            }
        }

        // 4) Parse resume PDF (no DB calls)
        Part resumePart = req.getPart("resumeFile");
        if (resumePart == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Missing resumeFile");
            return;
        }
        List<String> experiences;
        Set<String> skills;
        try (InputStream pdf = resumePart.getInputStream()) {
            ResumeParser parser = new ResumeParser(pdf);
            experiences = parser.getExperiences();
            skills      = parser.getSkills();
        } catch (TikaException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("text/plain");
            resp.getWriter().write("Please attach a valid resume");
            return;
        }

        // Everything that touches the database goes inside this try/catch
        int userId;
        try {
            // 5) Check duplicates
            if (userDao.existsByEmail(email)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("text/plain");
                resp.getWriter().write("That email is already registered");
                return;
            }
            if (userDao.existsByUsername(username)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("text/plain");
                resp.getWriter().write("That username is already taken");
                return;
            }

            // 6) Resolve friends → IDs
            // 4) Resolve friends → IDs, but collect _all_ invalid usernames
List<String> friendUsernames = Arrays.stream(
    Optional.ofNullable(friendsTxt).orElse("")
        .split(","))
  .map(String::trim)
  .filter(s -> !s.isEmpty())
  .collect(Collectors.toList());

List<Integer>  friendIds   = new ArrayList<>();
List<String>   invalid     = new ArrayList<>();

for (String fu : friendUsernames) {
  userDao.findByUsername(fu)
         .ifPresentOrElse(
           user -> friendIds.add(user.getId()),
           ()   -> invalid.add(fu)
         );
}

if (!invalid.isEmpty()) {
    String msg = "These usernames are not valid: " + String.join(", ", invalid);
    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    resp.setContentType("text/plain");
    resp.getWriter().write(msg);
    return;
  }
// at this point friendIds only contains the ones that _did_ exist


            // 7) Hash password & create user
            String hash = BCrypt.hashpw(password, BCrypt.gensalt());
            userId = userDao.create(
                email, hash, username, school, major, gradYear, interests, bio
            );
            HttpSession session = req.getSession(true);
            session.setAttribute("userId", userId);
            session.setAttribute("username", username);

            // 8) Persist resume pieces
            for (String e : experiences) expDao.save(userId, e);
            for (String s : skills)     skillDao.save(userId, s);

            // 9) Persist friendships bidirectionally
            for (int fid : friendIds) {
                friendDao.add(userId, fid);
                friendDao.add(fid, userId);
            }

        } catch (SQLException sqle) {
            log("Database error during signup", sqle);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal server error");
            return;
        }

        // 10) Return parsed JSON
        resp.setContentType("application/json");
        resp.getWriter().write("{" +
                "\"experiences\":" + toJsonArray(experiences) + "," +
                "\"skills\":"      + toJsonArray(skills)       + 
                "}");
    }

    private String toJsonArray(Iterable<String> items) {
        return "[" + StreamSupport.stream(items.spliterator(), false)
                .map(s -> "\"" + s.replace("\"", "\\\"") + "\"")
                .collect(Collectors.joining(","))
                + "]";
    }
}
