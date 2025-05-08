package com.example.linkedinmaxx.app.dao;

public class Friendship {
  private final int userId;
  private final int friendId;

  public Friendship(int userId, int friendId) {
    this.userId   = userId;
    this.friendId = friendId;
  }

  public int getUserId()   { return userId; }
  public int getFriendId() { return friendId; }
}
