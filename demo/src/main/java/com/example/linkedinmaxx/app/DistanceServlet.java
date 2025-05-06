// src/main/java/com/example/linkedinmaxx/app/DistanceServlet.java
package com.example.linkedinmaxx.app;

import com.example.linkedinmaxx.app.dao.FriendshipDao;
import com.example.linkedinmaxx.app.dao.User;
import com.example.linkedinmaxx.app.dao.UserDao;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@WebServlet("/api/distance")
public class DistanceServlet extends HttpServlet {
  private final UserDao       userDao       = new UserDao();
  private final FriendshipDao friendDao     = new FriendshipDao();
  private final Gson          gson          = new Gson();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    HttpSession session = req.getSession(false);
    if (session == null || session.getAttribute("userId") == null) {
      resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
      return;
    }
    int me = (Integer)session.getAttribute("userId");
    String targetName = req.getParameter("target");
    if (targetName == null || targetName.isBlank()) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                     "Missing ?target=<username>");
      return;
    }

    try {
      Optional<User> maybeTarget = userDao.findByUsername(targetName);
      if (maybeTarget.isEmpty()) {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                       "No such user: " + targetName);
        return;
      }
      int goal = maybeTarget.get().getId();

      // BFS
      Queue<Integer>    q     = new ArrayDeque<>();
      Map<Integer,Integer> prev = new HashMap<>();
      q.add(me);
      prev.put(me, null);

      boolean found = false;
      while (!q.isEmpty()) {
        int u = q.remove();
        if (u == goal) { found = true; break; }
        for (int v : friendDao.findFriends(u)) {
          if (!prev.containsKey(v)) {
            prev.put(v, u);
            q.add(v);
          }
        }
      }

      if (!found) {
        // no path
        resp.setContentType("application/json");
        resp.getWriter().write(gson.toJson(
          Map.of("distance", -1, "path", List.of())
        ));
        return;
      }

      // reconstruct path
      List<Integer> rev = new ArrayList<>();
      for (Integer at = goal; at != null; at = prev.get(at)) {
        rev.add(at);
      }
      Collections.reverse(rev);

      // map IDs → usernames
      List<String> pathNames = new ArrayList<>();
      for (int id : rev) {
        pathNames.add(userDao.findById(id).get().getUsername());
      }

      resp.setContentType("application/json");
      resp.getWriter().write(gson.toJson(
        Map.of(
          "distance", rev.size()-1,
          "path",     pathNames
        )
      ));

    } catch (SQLException e) {
      log("DB error in distance", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                     "Database error");
    }
  }
}
