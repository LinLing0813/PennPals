package org.cis120;

import java.util.LinkedList;

public class Channel {

    private final User owner;
    private String name; // The name of the channel
    private LinkedList<User> channelUsers; // A collection of the users in the channel
    // owner is always the first member of channelUsers
    private boolean isPrivate; // true for private

    public Channel(User owner, String name) {
        this.owner = owner;
        this.name = name;
        channelUsers = new LinkedList<User>();
        channelUsers.add(owner);
        isPrivate = false;
    }

    public Channel(User owner, String name, boolean isPrivate) {
        this.owner = owner;
        this.name = name;
        channelUsers = new LinkedList<User>();
        channelUsers.add(owner);
        this.isPrivate = isPrivate;
    }

    public boolean getIsPrivate() { // returns isPrivate
        return isPrivate;
    }

    public String getName() { // Return name of the channel
        return name;
    }

    public User getOwner() {
        return owner;
    }

    public void addUser(User newUser) {
        channelUsers.add(newUser);
    }

    // Returns a copy of channelUsers
    public LinkedList<User> getChannelUsers() {

        return channelUsers;
    }

    /**
     * Returns boolean representing if requested user is in the server or not.
     * Uses the user's ID as parameter.
     *
     * @param userID int of the users nickname
     * @return boolean if requested user is in the channel
     */
    public boolean userInChannel(int userID) { // Checks if a user with the given ID is in the
                                               // channel
        for (int i = 0; i < channelUsers.size(); i++) {
            if (channelUsers.get(i).getUserId() == userID) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns boolean representing if requested user is in the server or not.
     * Uses the user's nickname as parameter.
     *
     * @param userName String of the users nickname
     * @return boolean if requested user is in the channel
     */
    public boolean userInChannel(String userName) { // Checks if a user with the given ID is in the
                                                    // channel
        for (int i = 0; i < channelUsers.size(); i++) {
            if (channelUsers.get(i).getNickname().equals(userName)) {
                return true;
            }
        }
        return false;
    }

    public void removeUser(int userID) {
        for (int i = 0; i < channelUsers.size(); i++) {
            if (channelUsers.get(i).getUserId() == userID) {
                channelUsers.remove(i);
                break;
            }
        }
    }

    public LinkedList<String> getUsersNames() {
        // returns a collection consisting of all of the nicknames of the users in
        // channelUsers
        LinkedList<String> c = new LinkedList<>();
        for (int i = 0; i < channelUsers.size(); i++) {
            c.add(channelUsers.get(i).getNickname());
        }
        return c;
    }
}
