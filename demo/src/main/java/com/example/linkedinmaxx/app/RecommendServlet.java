package com.example.linkedinmaxx.app;

import com.example.linkedinmaxx.app.dao.UserDao;
import com.example.linkedinmaxx.app.dao.SkillDao;
import com.example.linkedinmaxx.app.dao.ExperienceDao;
import com.example.linkedinmaxx.app.dao.FriendshipDao;
import com.example.linkedinmaxx.app.dao.User;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@WebServlet("/api/recommend")
public class RecommendServlet extends HttpServlet {
  private final UserDao        userDao;
  private final FriendshipDao  friendshipDao;
  private final TfidfService   tfidfService;
  private final Gson           gson = new Gson();

  public RecommendServlet() throws ServletException {
    try {
      this.userDao       = new UserDao();
      this.friendshipDao = new FriendshipDao();
      this.tfidfService  = new TfidfService(
        userDao,
        new SkillDao(),
        new ExperienceDao()
      );
    } catch (Exception e) {
      throw new ServletException("Failed to initialize RecommendServlet", e);
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // Get the current user from da session
    HttpSession session = req.getSession(false);
    if (session == null || session.getAttribute("userId") == null) {
      resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
      return;
    }
    int me = (Integer) session.getAttribute("userId");

    try {
      // Computes the BFS distances using a helper function that I defined below
      Map<Integer,Integer> dist = computeFriendDistances(me);

      // This scores every reachable, non‑self user (excluding friends and people with zero similarity)
      List<Recommendation> recs = new ArrayList<>();
      for (User u : userDao.findAll()) {
        int other = u.getId();
        if (other == me) continue;

        int d = dist.getOrDefault(other, Integer.MAX_VALUE);
        // skip unreachable or direct friends
        if (d == Integer.MAX_VALUE || d <= 1) continue;

        double sim = tfidfService.cosine(me, other);
        // skip if no shared interests
        if (sim <= 0.0) continue;

        double score = sim / d;
        recs.add(new Recommendation(other, u.getUsername(), score));
      }

      // Sort descending by score & take top 2
      recs.sort((a, b) -> Double.compare(b.score, a.score));
      List<Recommendation> top2 = recs.stream().limit(2).toList();

      // Return a JSON
      resp.setContentType("application/json");
      resp.getWriter().write(gson.toJson(top2));

    } catch (SQLException e) {
      log("Database error computing recommendations", e);
      resp.sendError(
        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        "Could not compute recommendations"
      );
    }
  }

  //Standard BFS to get shortest path lengths from `start` to every other user.
  private Map<Integer,Integer> computeFriendDistances(int start) throws SQLException {
    Queue<Integer> q = new ArrayDeque<>();
    Map<Integer,Integer> dist = new HashMap<>();
    dist.put(start, 0);
    q.add(start);

    while (!q.isEmpty()) {
      int cur = q.remove();
      int cd  = dist.get(cur);
      for (int nb : friendshipDao.findFriends(cur)) {
        if (!dist.containsKey(nb)) {
          dist.put(nb, cd + 1);
          q.add(nb);
        }
      }
    }
    return dist;
  }

  // Simple DTO for a recommendation 
  public static class Recommendation {
    public final int    id;
    public final String username;
    public final double score;

    public Recommendation(int id, String username, double score) {
      this.id       = id;
      this.username = username;
      this.score    = score;
    }
  }
}
