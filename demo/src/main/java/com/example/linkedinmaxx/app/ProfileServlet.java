package com.example.linkedinmaxx.app;

import com.example.linkedinmaxx.app.dao.*;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@WebServlet("/api/profile")
public class ProfileServlet extends HttpServlet {
  private final UserDao userDao       = new UserDao();
  private final ExperienceDao expDao  = new ExperienceDao();
  private final SkillDao skillDao     = new SkillDao();
  private final FriendshipDao fd      = new FriendshipDao();
  private final Gson gson              = new Gson();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    HttpSession session = req.getSession(false);
    if (session == null || session.getAttribute("userId") == null) {
      resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
      return;
    }
    int me = (Integer) session.getAttribute("userId");

    String usernameParam = req.getParameter("user");
    if (usernameParam == null) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing user");
      return;
    }

    try {
      // look up the  user by username
      Optional<User> maybeTarget = userDao.findByUsername(usernameParam);
      if (maybeTarget.isEmpty()) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
        return;
      }
      User target = maybeTarget.get();
      int other = target.getId();

      // load their experiences & skills
      List<String> exps   = expDao.findByUser(other);
      List<String> skills = skillDao.findByUser(other);

      // compute shared
      Set<String> myExps   = new HashSet<>(expDao.findByUser(me));
      Set<String> mySkills = new HashSet<>(skillDao.findByUser(me));
      List<String> sharedExps   = new ArrayList<>();
      List<String> sharedSkills = new ArrayList<>();
      for (String e : exps)    if (myExps.contains(e))   sharedExps.add(e);
      for (String s : skills)  if (mySkills.contains(s)) sharedSkills.add(s);

      // BFS to build path
      Map<Integer,Integer> dist = new HashMap<>();
      Map<Integer,Integer> prev = new HashMap<>();
      ArrayDeque<Integer> q = new ArrayDeque<>();
      dist.put(me, 0);
      q.add(me);
      while (!q.isEmpty()) {
        int cur = q.remove();
        if (cur == other) break;
        for (int nb : fd.findFriends(cur)) {
          if (!dist.containsKey(nb)) {
            dist.put(nb, dist.get(cur) + 1);
            prev.put(nb, cur);
            q.add(nb);
          }
        }
      }
      int distance = dist.getOrDefault(other, -1);
      List<Integer> path = new ArrayList<>();
      if (distance >= 0) {
        for (int at = other; at != me; at = prev.get(at)) {
          path.add(at);
        }
        path.add(me);
        Collections.reverse(path);
      }

      // build json
      Map<String,Object> out = new LinkedHashMap<>();
      out.put("id",                 other);
      out.put("username",           target.getUsername());
      out.put("email",              target.getEmail());
      out.put("school",             target.getSchool());
      out.put("major",              target.getMajor());
      out.put("gradYear",           target.getGradYear());
      out.put("interests",          target.getInterests());
      out.put("bio",                target.getBio());
      out.put("experiences",        exps);
      out.put("skills",             skills);
      out.put("sharedExperiences",  sharedExps);
      out.put("sharedSkills",       sharedSkills);
      out.put("distance",           distance);
      // convert the ID‐based path into a username path
List<String> pathNames = new ArrayList<>(path.size());
for (Integer uid : path) {
  userDao.findById(uid)
         .ifPresent(u -> pathNames.add(u.getUsername()));
}
out.put("path", pathNames);

      resp.setContentType("application/json");
      resp.getWriter().write(gson.toJson(out));

    } catch (SQLException e) {
      log("Error loading profile", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                     "Could not load profile");
    }
  }
}
