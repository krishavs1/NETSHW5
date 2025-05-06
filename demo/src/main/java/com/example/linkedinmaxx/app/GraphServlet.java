// src/main/java/com/example/linkedinmaxx/app/GraphServlet.java
package com.example.linkedinmaxx.app;

import com.example.linkedinmaxx.app.dao.Friendship;
import com.example.linkedinmaxx.app.dao.FriendshipDao;
import com.example.linkedinmaxx.app.dao.User;
import com.example.linkedinmaxx.app.dao.UserDao;
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import java.sql.SQLException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

@WebServlet("/api/graph")
public class GraphServlet extends HttpServlet {
  private final UserDao         userDao       = new UserDao();
  private final FriendshipDao   friendshipDao = new FriendshipDao();
  private final Gson            gson          = new Gson();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setContentType("application/json");
    try {
      List<User> users       = userDao.findAll();
      List<Friendship> all   = friendshipDao.findAll();

      // Build unique undirected edges
      Set<String> seen = new HashSet<>();
      List<Map<String,Integer>> edges = new ArrayList<>();
      for (Friendship f : all) {
        int a = Math.min(f.getUserId(), f.getFriendId());
        int b = Math.max(f.getUserId(), f.getFriendId());
        String key = a + "-" + b;
        if (seen.add(key)) {
          edges.add(Map.of("from", a, "to", b));
        }
      }

      Map<String,Object> graph = new HashMap<>();
      graph.put("nodes", users.stream()
        .map(u -> Map.of("id", u.getId(), "label", u.getUsername()))
        .toList()
      );
      graph.put("edges", edges);

      resp.getWriter().write(gson.toJson(graph));
    } 
    catch (SQLException e) {
      log("DB error building graph", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                     "Could not load social graph");
    }
  }
}
