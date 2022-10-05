package org.cis120;

//import sun.awt.image.ImageWatched;

import java.util.*;

public final class ServerModel {

\
    LinkedList<User> userList = new LinkedList<User>(); // Linked List of all available users on the
                                                        // Server
    LinkedList<Channel> channelList = new LinkedList<Channel>();

    public ServerModel() {

    }


    /**
     * Gets the user ID currently associated with the given
     * nickname. The returned ID is -1 if the nickname is not
     * currently in use.
     *
     * @param nickname The nickname for which to get the associated user ID
     * @return The user ID of the user with the argued nickname if
     *         such a user exists, otherwise -1
     */
    public int getUserId(String nickname) {
        // return userSet.
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getNickname().equals(nickname)) {
                return userList.get(i).getUserId();
            }
        }
        return -1;
    }

    /**
     * Gets the nickname currently associated with the given user
     * ID. The returned nickname is null if the user ID is not
     * currently in use.
     *
     * @param userId The user ID for which to get the associated
     *               nickname
     * @return The nickname of the user with the argued user ID if
     *         such a user exists, otherwise null
     */
    public String getNickname(int userId) {

        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getUserId() == userId) {
                return userList.get(i).getNickname();
            }
        }
        return null;
    }

    /**
     * Gets a collection of the nicknames of all users who are
     * registered with the server. Changes to the returned collection
     * should not affect the server state.
     * 
     * This method is provided for testing.
     *
     * @return The collection of registered user nicknames
     */
    public Collection<String> getRegisteredUsers() {
        LinkedList<String> c = new LinkedList<String>();
        for (int i = 0; i < userList.size(); i++) {
            c.add(userList.get(i).getNickname());
        }
        return c;
    }

    /**
     * Gets a collection of the names of all the channels that are
     * present on the server. Changes to the returned collection
     * should not affect the server state.
     * 
     * This method is provided for testing.
     *
     * @return The collection of channel names
     */
    public Collection<String> getChannels() {
        Collection<String> c = new LinkedList<String>();
        for (int i = 0; i < channelList.size(); i++) {
            c.add(channelList.get(i).getName());
        }

        return c;
    }

    /**
     * Gets a collection of the nicknames of all the users in a given
     * channel. The collection is empty if no channel with the given
     * name exists. Modifications to the returned collection should
     * not affect the server state.
     *
     * @param channelName The channel for which to get member nicknames
     * @return A collection of all user nicknames in the channel
     */
    public Collection<String> getUsersInChannel(String channelName) {

        LinkedList<String> c = new LinkedList<String>();
        for (int i = 0; i < channelList.size(); i++) {
            if (channelList.get(i).getName().equals(channelName)) {
                Channel fChannel = channelList.get(i);
                for (int j = 0; j < fChannel.getChannelUsers().size(); j++) {
                    c.add(fChannel.getChannelUsers().get(j).getNickname());
                }
            }
        }
        return c;

    }

    /**
     * Gets the nickname of the owner of the given channel. The result
     * is {@code null} if no channel with the given name exists.
     *
     *
     * @param channelName The channel for which to get the owner nickname
     * @return The nickname of the channel owner if such a channel
     *         exists; otherwise, return null
     */
    public String getOwner(String channelName) {

        for (int i = 0; i < channelList.size(); i++) {
            if (channelList.get(i).getName().equals(channelName)) {
                return channelList.get(i).getOwner().getNickname();

            }
        }

        return null;
    }

 
    /**
     * This method is automatically called by the backend when a new client
     * connects to the server. It should generate a default nickname with
     * {@link #generateUniqueNickname()}, store the new user's ID and username
     * in your data structures for {@link ServerModel} state, and construct
     * and return a {@link Broadcast} object using
     * {@link Broadcast#connected(String)}}.
     *
     * @param userId The new user's unique ID (automatically created by the
     *               backend)
     * @return The {@link Broadcast} object generated by calling
     *         {@link Broadcast#connected(String)} with the proper parameter
     */
    public Broadcast registerUser(int userId) {
        String nickname = generateUniqueNickname();
        userList.add(new User(userId, nickname));
        return Broadcast.connected(nickname);
    }

    /**
     *
     * Generates a unique nickname of the form "UserX", where X is the
     * smallest non-negative integer that yields a unique nickname for a user.
     * 
     * @return The generated nickname
     */
    private String generateUniqueNickname() {
        int suffix = 0;
        String nickname;
        Collection<String> existingUsers = getRegisteredUsers();
        do {
            nickname = "User" + suffix++;
        } while (existingUsers.contains(nickname));
        return nickname;
    }

    /**
     * This method is automatically called by the backend when a client
     * disconnects from the server. 
     *
     * @param userId The unique ID of the user to deregister
     * @return The {@link Broadcast} object generated by calling
     *         {@link Broadcast#disconnected(String, Collection)} with the proper
     *         parameters
     */

    public Broadcast deregisterUser(int userId) {
        String nickname = "";
        LinkedList<String> c = new LinkedList<String>();

        int size = channelList.size();
        for (int j = 0; j < size; j++) {  // Goes through list of channels
            if (channelList.get(j).getOwner().getUserId() == userId) { // The user was the owner
                                                                      // of this channel
                channelList.remove(j);
                size--;
                j--; // necessary j++?
            }
        }
        size = channelList.size();
        for (int j = 0; j < size; j++) {  // Goes through list of channels
            if (channelList.get(j).userInChannel(userId)) {  // If the user is in this channel
                for (int i = 0; i < channelList.get(j).getChannelUsers().size(); i++) {
                    if (channelList.get(j).getChannelUsers().get(i).getUserId() != userId) {
                        c.add(channelList.get(j).getChannelUsers().get(i).getNickname()); // (1)
                    }
                    if (channelList.get(j).getChannelUsers().get(i).getUserId() == userId) {
                        channelList.get(j).removeUser(userId);
                    }
                    // adds all the users in the channel to
                    // Linked list used later for broadcast
                }
            }
        }
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getUserId() == userId) {  // Goes through list of users
                nickname = userList.get(i).getNickname();
                System.out.println(userList.get(i).getNickname());
                userList.remove(i); // (3) Removes user
            }

        }
        return Broadcast.disconnected(nickname, c); // (4)
    }

    /**
     * This method is called to generate a collection of the nicknames of all users
     * who are in the same
     * channel as one user.
     *
     * @param userNick The String containing the nickname of the user
     * @return The LinkedList<String> consisting of all of the nicknames of users
     *         who are in the same
     *         channel as the parameter user. Will be empty if there are no other
     *         users.
     */
    public LinkedList<String> usersShared(String userNick) {
        LinkedList<String> list = new LinkedList<>();
        for (int i = 0; i < channelList.size(); i++) {
            if (channelList.get(i).userInChannel(userNick)) { // The user is in this server
                for (int j = 0; j < channelList.get(i).getChannelUsers().size(); j++) {
                    list.add(channelList.get(i).getChannelUsers().get(j).getNickname());
                }
            }
        }
        return list;
    }

    /**
     * This method is called to generate a collection of the nicknames of all users
     * who are in the same
     * channel as one user.
     *
     * @param userNick The String containing the nickname of the user
     * @return The LinkedList<String> consisting of all of the nicknames of users
     *         who are in the same
     *         channel as the parameter user. Will be empty if there are no other
     *         users.
     */
    public LinkedList<String> usersShared(int userNick) {
        LinkedList<String> list = new LinkedList<>();
        for (int i = 0; i < channelList.size(); i++) {
            if (channelList.get(i).userInChannel(userNick)) { // The user is in this server
                for (int j = 0; j < channelList.get(i).getChannelUsers().size(); j++) {
                    list.add(channelList.get(i).getChannelUsers().get(j).getNickname());
                }
            }
        }
        return list;
    }

    /**
     * This method is called when a user wants to change their nickname.
     * 
     * @param nickCommand The {@link NicknameCommand} object containing
     *                    all information needed to attempt a nickname change
     * @return The {@link Broadcast} object generated by
     *         {@link Broadcast#okay(Command, Collection)} if the nickname
     *         change is successful. The command should be the original nickCommand
     *         and the collection of recipients should be any clients who
     *         share at least one channel with the sender, including the sender.
     *
     *         If an error occurs, use
     *         {@link Broadcast#error(Command, ServerResponse)} with either:
     *         (1) {@link ServerResponse#INVALID_NAME} if the proposed nickname
     *         is not valid according to
     *         {@link ServerModel#isValidName(String)}
     *         (2) {@link ServerResponse#NAME_ALREADY_IN_USE} if there is
     *         already a user with the proposed nickname
     */
    public Broadcast changeNickname(NicknameCommand nickCommand) {
        String nick = nickCommand.getNewNickname();

        if (!(ServerModel.isValidName(nick))) { // The name is not valid
            return Broadcast.error(nickCommand, ServerResponse.INVALID_NAME);
        }
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getNickname().equals(nick)) { // A user already has that nickname
                return Broadcast.error(nickCommand, ServerResponse.NAME_ALREADY_IN_USE);
            }
        }
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getUserId() == nickCommand.getSenderId()) { // The user wanting to
                                                                           // change their nickname
                // is found
                userList.get(i).setNickname(nick);
            }
        }

        LinkedList<String> c = new LinkedList<String>();

        for (int j = 0; j < channelList.size(); j++) { // Goes through list of channels

            if (channelList.get(j).userInChannel(nickCommand.getSenderId())) { // If the user is in
                                                                               // this channel
                for (int i = 0; i < channelList.get(j).getChannelUsers().size(); i++) {
                    c.add(channelList.get(j).getChannelUsers().get(i).getNickname());
                    // adds all the users in the channel to
                    // Linked list used later for broadcast
                }
            }
        }
        return Broadcast.okay(nickCommand, c);
    }

    /**
     * Determines if a given nickname is valid or invalid (contains at least
     * one alphanumeric character, and no non-alphanumeric characters).
     * (Nothing to do here.)
     * 
     * @param name The channel or nickname string to validate
     * @return true if the string is a valid name
     */
    public static boolean isValidName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        for (char c : name.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method is called when a user wants to create a channel.
     * 
     * @param createCommand The {@link CreateCommand} object containing all
     *                      information needed to attempt channel creation
     * @return The {@link Broadcast} object generated by
     *         {@link Broadcast#okay(Command, Collection)} if the channel
     *         creation is successful. The only recipient should be the new
     *         channel's owner.
     *
     *         If an error occurs, use
     *         {@link Broadcast#error(Command, ServerResponse)} with either:
     *         (1) {@link ServerResponse#INVALID_NAME} if the proposed
     *         channel name is not valid according to
     *         {@link ServerModel#isValidName(String)}
     *         (2) {@link ServerResponse#CHANNEL_ALREADY_EXISTS} if there is
     *         already a channel with the proposed name
     */
    public Broadcast createChannel(CreateCommand createCommand) {

        int ownerID = createCommand.getSenderId();

        if (!(ServerModel.isValidName(createCommand.getChannel()))) { // not Valid (1)
            return Broadcast.error(createCommand, ServerResponse.INVALID_NAME);
        }
        for (int i = 0; i < channelList.size(); i++) {
            if (channelList.get(i).getName().equals(createCommand.getChannel())) { // (2)Channel
                                                                                   // already exists
                                                                                   // with
                                                                                   // the name
                return Broadcast.error(createCommand, ServerResponse.CHANNEL_ALREADY_EXISTS);
            }
        }
        User owner = userList.get(0);
        // Need to find the owner
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getUserId() == ownerID) { // User of owner is found
                owner = userList.get(i);
            }
        }
        LinkedList<String> ownerList = new LinkedList<String>();
        ownerList.add(createCommand.getSender());
        channelList
                .add(new Channel(owner, createCommand.getChannel(), createCommand.isInviteOnly()));
        return Broadcast.okay(createCommand, ownerList);
    }

    /**
     * This method is called when a user wants to join a channel.
     * 
     * @param joinCommand The {@link JoinCommand} object containing all
     *                    information needed for the user's join attempt
     * @return The {@link Broadcast} object generated by
     *         {@link Broadcast#names(Command, Collection, String)} if the user
     *         joins the channel successfully. The recipients should be all
     *         people in the joined channel (including the sender).
     *
     *         If an error occurs, use
     *         {@link Broadcast#error(Command, ServerResponse)} with either:
     *         (1) {@link ServerResponse#NO_SUCH_CHANNEL} if there is no
     *         channel with the specified name
     *         (2) (after Task 5) {@link ServerResponse#JOIN_PRIVATE_CHANNEL} if
     *         the sender is attempting to join a private channel
     */
    public Broadcast joinChannel(JoinCommand joinCommand) {

        String channelName = joinCommand.getChannel();
        boolean channelExists = false;
        for (int i = 0; i < channelList.size(); i++) {
            if (channelList.get(i).getName().equals(channelName)) { // A channel exists with this
                                                                    // name
                channelExists = true;
                if (channelList.get(i).getIsPrivate()) { // true if it is private
                    return Broadcast.error(joinCommand, ServerResponse.JOIN_PRIVATE_CHANNEL);
                }
            }
        }
        if (!channelExists) { // Channel does not exist
            return Broadcast.error(joinCommand, ServerResponse.NO_SUCH_CHANNEL); // (1)
        }

        // Finds user
        User newUser = userList.get(0);
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getUserId() == joinCommand.getSenderId()) {
                newUser = userList.get(i);
            }
        }

        String owner = "";

        LinkedList<String> c = new LinkedList<String>();
        // Adds user
        for (int i = 0; i < channelList.size(); i++) {
            if (channelList.get(i).getName().equals(channelName)) { // Channel is found
                owner = channelList.get(i).getOwner().getNickname();
                channelList.get(i).addUser(newUser);
                for (int j = 0; j < channelList.get(i).getChannelUsers().size(); j++) {
                    c.add(channelList.get(i).getChannelUsers().get(j).getNickname());
                }
            }
        }

        return Broadcast.names(joinCommand, c, owner);
    }

    /**
     * This method is called when a user wants to send a message to a channel.
     * 
     * @param messageCommand The {@link MessageCommand} object containing all
     *                       information needed for the messaging attempt
     * @return The {@link Broadcast} object generated by
     *         {@link Broadcast#okay(Command, Collection)} if the message
     *         attempt is successful. The recipients should be all clients
     *         in the channel.
     *
     *         If an error occurs, use
     *         {@link Broadcast#error(Command, ServerResponse)} with either:
     *         (1) {@link ServerResponse#NO_SUCH_CHANNEL} if there is no
     *         channel with the specified name
     *         (2) {@link ServerResponse#USER_NOT_IN_CHANNEL} if the sender is
     *         not in the channel they are trying to send the message to
     */
    public Broadcast sendMessage(MessageCommand messageCommand) {
        String channelName = messageCommand.getChannel();
        boolean channelExists = false;
        for (int i = 0; i < channelList.size(); i++) {
            if (channelList.get(i).getName().equals(channelName)) {
                // A channel exists with this name
                channelExists = true;
            }
        }
        if (!channelExists) { // Channel does not exist
            return Broadcast.error(messageCommand, ServerResponse.NO_SUCH_CHANNEL); // (1)
        }

        boolean senderInChannel = false;
        for (int i = 0; i < channelList.size(); i++) {

            if (channelList.get(i).getName().equals(channelName)) {
                for (int j = 0; j < channelList.get(i).getChannelUsers().size(); j++) {
                    if (channelList.get(i).getChannelUsers().get(j).getUserId() == messageCommand
                            .getSenderId()) {
                        senderInChannel = true;
                        break;
                    }
                }
            }
        }

        if (!senderInChannel) {  // sender is not in the channel
            return Broadcast.error(messageCommand, ServerResponse.USER_NOT_IN_CHANNEL);
        }

        LinkedList<String> c = new LinkedList<String>();
        // Adds user
        for (int i = 0; i < channelList.size(); i++) {
            if (channelList.get(i).getName().equals(channelName)) { // Channel is found
                for (int j = 0; j < channelList.get(i).getChannelUsers().size(); j++) {
                    c.add(channelList.get(i).getChannelUsers().get(j).getNickname());
                }
            }
        }
        return Broadcast.okay(messageCommand, c);
    }

    /**
     * This method is called when a user wants to leave a channel.
     * 
     * @param leaveCommand The {@link LeaveCommand} object containing all
     *                     information about the user's leave attempt
     * @return The {@link Broadcast} object generated by
     *         {@link Broadcast#okay(Command, Collection)} if the user leaves
     *         the channel successfully. The recipients should be all clients
     *         who were in the channel, including the user who left.
     * 
     *         If an error occurs, use
     *         {@link Broadcast#error(Command, ServerResponse)} with either:
     *         (1) {@link ServerResponse#NO_SUCH_CHANNEL} if there is no
     *         channel with the specified name
     *         (2) {@link ServerResponse#USER_NOT_IN_CHANNEL} if the sender is
     *         not in the channel they are trying to leave
     */
    public Broadcast leaveChannel(LeaveCommand leaveCommand) {
        String channelName = leaveCommand.getChannel();
        boolean channelExists = false;
        for (int i = 0; i < channelList.size(); i++) {
            if (channelList.get(i).getName().equals(channelName)) {
                channelExists = true;
            }
        }
        if (!channelExists) {  // Channel does not exist
            return Broadcast.error(leaveCommand, ServerResponse.NO_SUCH_CHANNEL); // (1)
        }

        boolean senderInChannel = false;
        for (int i = 0; i < channelList.size(); i++) {

            if (channelList.get(i).getName().equals(channelName)) {
                for (int j = 0; j < channelList.get(i).getChannelUsers().size(); j++) {
                    if (channelList.get(i).getChannelUsers().get(j).getUserId() == leaveCommand
                            .getSenderId()) {
                        senderInChannel = true;
                        break;
                    }
                }
            }
        }

        if (!senderInChannel) {  // sender is not in the channel
            return Broadcast.error(leaveCommand, ServerResponse.USER_NOT_IN_CHANNEL);
        }

        LinkedList<String> c = new LinkedList<String>();
        // Leaving the channel
        for (int i = 0; i < channelList.size(); i++) {
            if (channelList.get(i).getName().equals(leaveCommand.getChannel())) { // Channel leaving
                                                                                  // is found
                for (int j = 0; j < channelList.get(i).getChannelUsers().size(); j++) {
                    c.add(channelList.get(i).getChannelUsers().get(j).getNickname());
                }
                channelList.get(i).removeUser(leaveCommand.getSenderId());
            }
        }

        return Broadcast.okay(leaveCommand, c);
    }

    /**
     * This method is called when a channel's owner adds a user to that channel.
     * 
     * @param inviteCommand The {@link InviteCommand} object containing all
     *                      information needed for the invite attempt
     * @return The {@link Broadcast} object generated by
     *         {@link Broadcast#names(Command, Collection, String)} if the user
     *         joins the channel successfully as a result of the invite.
     *         The recipients should be all people in the joined channel
     *         (including the new user).
     *
     *         If an error occurs, use
     *         {@link Broadcast#error(Command, ServerResponse)} with either:
     *         (1) {@link ServerResponse#NO_SUCH_USER} if the invited user
     *         does not exist
     *         (2) {@link ServerResponse#NO_SUCH_CHANNEL} if there is no channel
     *         with the specified name
     *         (3) {@link ServerResponse#INVITE_TO_PUBLIC_CHANNEL} if the
     *         invite refers to a public channel
     *         (4) {@link ServerResponse#USER_NOT_OWNER} if the sender is not
     *         the owner of the channel
     */
    public Broadcast inviteUser(InviteCommand inviteCommand) {

        // Error is being given by one of my Broadcast.errors (I think)

        String channelName = inviteCommand.getChannel();

        // 1
        boolean userExists = false;
        int userIndex = 0;// Finding the user
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getNickname().equals(inviteCommand.getUserToInvite())) {
                userExists = true;
                userIndex = i;
            }
        }
        if (!userExists) { // The user does not exist (1)
            return Broadcast.error(inviteCommand, ServerResponse.NO_SUCH_USER);
        }
        int channelIndex = 0;
        // 2
        boolean channelExists = false;
        for (int i = 0; i < channelList.size(); i++) {
            if (channelList.get(i).getName().equals(channelName)) { // A channel exists with this
                                                                    // name
                if (!channelList.get(i).getIsPrivate()) { // true if it is public
                    // (3)
                    return Broadcast.error(inviteCommand, ServerResponse.INVITE_TO_PUBLIC_CHANNEL);
                }
                if (channelList.get(i).getOwner().getUserId() != inviteCommand.getSenderId()) {
                    return Broadcast.error(inviteCommand, ServerResponse.USER_NOT_OWNER); // (4)
                }
                channelExists = true;
                channelIndex = i;

            }
        }
        if (!channelExists) { // Channel does not exist
            return Broadcast.error(inviteCommand, ServerResponse.NO_SUCH_CHANNEL); // (1)
        }

        channelList.get(channelIndex).addUser(userList.get(userIndex));
        LinkedList<String> c = channelList.get(channelIndex).getUsersNames();


        return Broadcast.names(inviteCommand, c, "User0");
    }

    /**
     * This method is called when a channel's owner removes a user from
     * that channel.
     * 
     * @param kickCommand The {@link KickCommand} object containing all
     *                    information needed for the kick attempt
     * @return The {@link Broadcast} object generated by
     *         {@link Broadcast#okay(Command, Collection)} if the user is
     *         successfully kicked from the channel. The recipients should be
     *         all clients who were in the channel, including the user
     *         who was kicked.
     *
     *         If an error occurs, use
     *         {@link Broadcast#error(Command, ServerResponse)} with either:
     *         (1) {@link ServerResponse#NO_SUCH_USER} if the user being kicked
     *         does not exist
     *         (2) {@link ServerResponse#NO_SUCH_CHANNEL} if there is no channel
     *         with the specified name
     *         (3) {@link ServerResponse#USER_NOT_IN_CHANNEL} if the
     *         user being kicked is not a member of the channel
     *         (4) {@link ServerResponse#USER_NOT_OWNER} if the sender is not
     *         the owner of the channel
     */
    public Broadcast kickUser(KickCommand kickCommand) {
        String channelName = kickCommand.getChannel();

        // 1
        boolean userExists = false;
        int userIndex = 0;
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getNickname().equals(kickCommand.getUserToKick())) {
                userExists = true;
                userIndex = i;
            }
        }
        if (!userExists) { // The user does not exist (1)
            return Broadcast.error(kickCommand, ServerResponse.NO_SUCH_USER);
        }
        int channelIndex = 0;
        // 2
        boolean channelExists = false;
        for (int i = 0; i < channelList.size(); i++) {
            if (channelList.get(i).getName().equals(channelName)) { // A channel exists with this
                                                                    // name
                if (!channelList.get(i).userInChannel(kickCommand.getSenderId())) { // (3)if user is
                                                                                    // not in
                                                                                    // channel
                    return Broadcast.error(kickCommand, ServerResponse.USER_NOT_IN_CHANNEL);
                }
                channelExists = true;
                channelIndex = i;
            }

            if (!channelExists) { // Channel does not exist
                return Broadcast.error(kickCommand, ServerResponse.NO_SUCH_CHANNEL); // (2)
            }

            // (4)
            if (channelList.get(channelIndex).getOwner().getUserId() != kickCommand.getSenderId()) {
                return Broadcast.error(kickCommand, ServerResponse.USER_NOT_OWNER);
            }

        }

        LinkedList<String> c = channelList.get(channelIndex).getUsersNames();

        channelList.get(channelIndex).getChannelUsers().remove(userIndex);

        return Broadcast.okay(kickCommand, c);

    }

}
