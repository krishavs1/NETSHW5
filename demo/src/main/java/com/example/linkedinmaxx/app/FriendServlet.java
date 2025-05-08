package com.example.linkedinmaxx.app;

import com.example.linkedinmaxx.app.dao.FriendshipDao;
import com.example.linkedinmaxx.app.dao.UserDao;
import com.example.linkedinmaxx.app.dao.User;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

@WebServlet("/api/friend")
public class FriendServlet extends HttpServlet {
  private final UserDao       userDao       = new UserDao();
  private final FriendshipDao friendshipDao = new FriendshipDao();
  private final Gson          gson          = new Gson();

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    HttpSession session = req.getSession(false);
    if (session == null || session.getAttribute("userId") == null) {
      resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
      return;
    }
    int me = (Integer) session.getAttribute("userId");

    
    Map<String,String> body = gson.fromJson(new BufferedReader(req.getReader()), Map.class);
    String otherName = body.get("user");
    if (otherName == null) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing user");
      return;
    }

    try {
      Optional<User> maybe = userDao.findByUsername(otherName);
      if (maybe.isEmpty()) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
        return;
      }
      int otherId = maybe.get().getId();

      
      friendshipDao.add(me, otherId);
      friendshipDao.add(otherId, me);

      resp.setStatus(HttpServletResponse.SC_OK);
      resp.getWriter().write("{\"status\":\"ok\"}");
    } catch (SQLException e) {
      log("Error creating friendship", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not add friend");
    }
  }
}
