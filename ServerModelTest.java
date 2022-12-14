package org.cis120;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class ServerModelTest {
    private ServerModel model;


    @BeforeEach
    public void setUp() {
        // We initialize a fresh ServerModel for each test
        model = new ServerModel();
    }

    @Test
    public void testInvalidNickname() {

        model.registerUser(0);

        Command command = new NicknameCommand(0, "User0", "!nv@l!d!");

        Broadcast expected = Broadcast.error(
                command, ServerResponse.INVALID_NAME
        );

        Broadcast actual = command.updateServerModel(model);

        assertEquals(expected, actual, "Broadcast");

        Collection<String> users = model.getRegisteredUsers();

        assertEquals(1, users.size(), "Number of registered users");

        // We then check if the username was updated to an invalid value
        // (it should not have been).
        assertTrue(users.contains("User0"), "Old nickname still registered");

        assertEquals(
                "User0", model.getNickname(0),
                "User with id 0 nickname unchanged"
        );
    }


    @Test
    public void testDeregisterMultipleUsers() {
        model.registerUser(0);
        model.registerUser(1);
        model.deregisterUser(0);
        model.deregisterUser(1);
        assertTrue(model.getRegisteredUsers().isEmpty(), "No registered users");
    }

    @Test
    public void testDeregisterNonExistentUser() {
        model.deregisterUser(0);
        assertTrue(model.getRegisteredUsers().isEmpty(), "No registered users");
    }
    // createChannel Testing
    @Test
    public void testCreateNewChannelInvalidName() {
        model.registerUser(0);
        Command create = new CreateCommand(0, "User0", "", false);
        Broadcast expected = Broadcast.error(create, ServerResponse.INVALID_NAME);
        assertEquals(expected, create.updateServerModel(model), "broadcast");

        assertFalse(model.getChannels().contains("java"), "channel exists");
        assertFalse(
                model.getUsersInChannel("java").contains("User0"),
                "channel has creator"
        );
        assertNull(model.getOwner("java"), "channel has owner");
    }

    // Creating a channel when that channel already exists
    @Test
    public void testCreateNewChannelExistingChannel() {
        model.registerUser(0);
        Command create = new CreateCommand(0, "User0", "java", false);
        Broadcast expected = Broadcast.okay(create, Collections.singleton("User0"));
        Broadcast expected2 = Broadcast.error(create, ServerResponse.CHANNEL_ALREADY_EXISTS);

        assertEquals(expected, create.updateServerModel(model), "broadcast");
        assertEquals(expected2, create.updateServerModel(model), "broadcast");

        assertTrue(model.getChannels().contains("java"), "channel exists");
        assertTrue(
                model.getUsersInChannel("java").contains("User0"),
                "channel has creator"
        );
        assertEquals("User0", model.getOwner("java"), "channel has owner");
    }

    // Join Channel
    @Test
    public void testJoinChannelDoesNotExist() {
        model.registerUser(0);
        model.registerUser(1);
        Command create = new CreateCommand(0, "User0", "java", false);
        create.updateServerModel(model);

        Command join = new JoinCommand(1, "User1", "python");
        Set<String> recipients = new TreeSet<>();
        recipients.add("User1");
        recipients.add("User0");
        Broadcast expected = Broadcast.error(join, ServerResponse.NO_SUCH_CHANNEL);
        assertEquals(expected, join.updateServerModel(model), "broadcast");

        assertTrue(
                model.getUsersInChannel("java").contains("User0"),
                "User0 in channel"
        );
        assertFalse(
                model.getUsersInChannel("java").contains("User1"),
                "User1 in channel"
        );
        assertEquals(
                1, model.getUsersInChannel("java").size(),
                "num. users in channel"
        );
    }

    @Test
    public void testJoinChannelPrivate() {
        model.registerUser(0);
        model.registerUser(1);
        Command create = new CreateCommand(0, "User0", "java", true);
        create.updateServerModel(model);

        Command join = new JoinCommand(1, "User1", "java");
        Set<String> recipients = new TreeSet<>();
        recipients.add("User1");
        recipients.add("User0");
        Broadcast expected = Broadcast.error(join, ServerResponse.JOIN_PRIVATE_CHANNEL);
        assertEquals(expected, join.updateServerModel(model), "broadcast");

        assertTrue(
                model.getUsersInChannel("java").contains("User0"),
                "User0 in channel"
        );
        assertFalse(
                model.getUsersInChannel("java").contains("User1"),
                "User1 in channel"
        );
        assertEquals(
                1, model.getUsersInChannel("java").size(),
                "num. users in channel"
        );

    }
   

    @Test
    public void testNickBroadcastsToChannelNotMember() {
        model.registerUser(0);
        model.registerUser(1);
        Command create = new CreateCommand(0, "User0", "java", false);
        create.updateServerModel(model);

        Command nick = new NicknameCommand(0, "User0", "Duke");
        Set<String> recipients = new TreeSet<>();
        // recipients.add("User1");
        recipients.add("Duke");
        Broadcast expected = Broadcast.okay(nick, recipients);
        assertEquals(expected, nick.updateServerModel(model), "broadcast");

        assertFalse(
                model.getUsersInChannel("java").contains("User0"),
                "old nick not in channel"
        );
        assertTrue(
                model.getUsersInChannel("java").contains("Duke"),
                "new nick is in channel"
        );
        assertFalse(
                model.getUsersInChannel("java").contains("User1"),
                "unaffected user still in channel"
        );
    }

    @Test
    public void testMesgChannelNotExistsMember() {
        model.registerUser(0);
        model.registerUser(1);
        Command create = new CreateCommand(0, "User0", "java", false);
        create.updateServerModel(model);

        Command mesg = new MessageCommand(1, "User1", "java", "hey whats up hello");
        Broadcast expected = Broadcast.error(mesg, ServerResponse.USER_NOT_IN_CHANNEL);
        assertEquals(expected, mesg.updateServerModel(model), "broadcast");
    }

    @Test
    public void testMesgChannelNotExists() {
        model.registerUser(0);
        model.registerUser(1);
        Command create = new CreateCommand(0, "User0", "java", false);
        create.updateServerModel(model);
        Command join = new JoinCommand(1, "User1", "java");
        join.updateServerModel(model);

        Command mesg = new MessageCommand(0, "User0", "python", "hey whats up hello");

        Broadcast expected = Broadcast.error(mesg, ServerResponse.NO_SUCH_CHANNEL);
        assertEquals(expected, mesg.updateServerModel(model), "broadcast");
    }

    // leaveChannel

    @Test
    public void testLeaveChannelNotMember() {
        model.registerUser(0);
        model.registerUser(1);
        Command create = new CreateCommand(0, "User0", "java", false);
        create.updateServerModel(model);

        Command leave = new LeaveCommand(1, "User1", "java");
        Broadcast expected = Broadcast.error(leave, ServerResponse.USER_NOT_IN_CHANNEL);
        assertEquals(expected, leave.updateServerModel(model), "broadcast");

        assertTrue(
                model.getUsersInChannel("java").contains("User0"),
                "User0 in channel"
        );
        assertFalse(
                model.getUsersInChannel("java").contains("User1"),
                "User1 not in channel"
        );
        assertEquals(
                1, model.getUsersInChannel("java").size(),
                "num. users in channel"
        );
    }

    @Test
    public void testLeaveChannelNotExists() {
        model.registerUser(0);
        model.registerUser(1);
        Command create = new CreateCommand(0, "User0", "java", false);
        create.updateServerModel(model);
        Command join = new JoinCommand(1, "User1", "java");
        join.updateServerModel(model);

        Command leave = new LeaveCommand(1, "User1", "python");
        Broadcast expected = Broadcast.error(leave, ServerResponse.NO_SUCH_CHANNEL);
        assertEquals(expected, leave.updateServerModel(model), "broadcast");

        assertTrue(
                model.getUsersInChannel("java").contains("User0"),
                "User0 in channel"
        );
        assertTrue(
                model.getUsersInChannel("java").contains("User1"),
                "User1 in channel"
        );
        assertEquals(
                2, model.getUsersInChannel("java").size(),
                "num. users in channel"
        );
    }

    // inviteUser

    @Test
    public void testInviteByOwnerUserDoesNotExist() {
        model = new ServerModel();

        model.registerUser(0); // add user with id = 0
        model.registerUser(1); // add user with id = 1

        // this command will create a channel called "java" with "User0" (with id = 0)
        // as the owner
        Command create = new CreateCommand(0, "User0", "java", true);

        // this line *actually* updates the model's state
        create.updateServerModel(model);

        Command invite = new InviteCommand(0, "User0", "java", "User6");

        Broadcast expected = Broadcast.error(invite, ServerResponse.NO_SUCH_USER);
        assertEquals(expected, invite.updateServerModel(model), "broadcast");

        assertEquals(1, model.getUsersInChannel("java").size(), "num. users in channel");
        assertTrue(model.getUsersInChannel("java").contains("User0"), "User0 in channel");
        assertFalse(model.getUsersInChannel("java").contains("User1"), "User1 not in channel");
    }

    @Test
    public void testInviteByOwnerNoSuchChannel() {
        model = new ServerModel();

        model.registerUser(0); // add user with id = 0
        model.registerUser(1); // add user with id = 1

        // this command will create a channel called "java" with "User0" (with id = 0)
        // as the owner
        Command create = new CreateCommand(0, "User0", "java", true);

        // this line *actually* updates the model's state
        create.updateServerModel(model);

        Command invite = new InviteCommand(0, "User0", "python", "User1");
        Broadcast expected = Broadcast.error(invite, ServerResponse.NO_SUCH_CHANNEL);
        assertEquals(expected, invite.updateServerModel(model), "broadcast");

        assertEquals(1, model.getUsersInChannel("java").size(), "num. users in channel");
        assertTrue(model.getUsersInChannel("java").contains("User0"), "User0 in channel");
        assertFalse(model.getUsersInChannel("java").contains("User1"), "User1 not in channel");
    }

    @Test
    public void testInviteByOwnerPublicChannel() {
        model = new ServerModel();

        model.registerUser(0); // add user with id = 0
        model.registerUser(1); // add user with id = 1

        // this command will create a channel called "java" with "User0" (with id = 0)
        // as the owner
        Command create = new CreateCommand(0, "User0", "java", false);

        // this line *actually* updates the model's state
        create.updateServerModel(model);

        Command invite = new InviteCommand(0, "User0", "java", "User1");
        Broadcast expected = Broadcast.error(invite, ServerResponse.INVITE_TO_PUBLIC_CHANNEL);
        assertEquals(expected, invite.updateServerModel(model), "broadcast");

        assertEquals(1, model.getUsersInChannel("java").size(), "num. users in channel");
        assertTrue(model.getUsersInChannel("java").contains("User0"), "User0 in channel");
        assertFalse(model.getUsersInChannel("java").contains("User1"), "User1 not in channel");
    }

    // kickUser

    @Test
    public void testKickOneChannelUserDoesNotExist() {
        model = new ServerModel();

        model.registerUser(0); // add user with id = 0
        model.registerUser(1); // add user with id = 1

        // this command will create a channel called "java" with "User0" (with id = 0)
        // as the owner
        Command create = new CreateCommand(0, "User0", "java", true);

        // this line *actually* updates the model's state
        create.updateServerModel(model);

        Command invite = new InviteCommand(0, "User0", "java", "User1");
        invite.updateServerModel(model);

        Command kick = new KickCommand(0, "User0", "java", "User6");

        Broadcast expected = Broadcast.error(kick, ServerResponse.NO_SUCH_USER);
        assertEquals(expected, kick.updateServerModel(model));
        assertEquals(2, model.getUsersInChannel("java").size(), "num. users in channel");
        assertTrue(model.getUsersInChannel("java").contains("User0"), "User0 still in channel");
        assertTrue(model.getUsersInChannel("java").contains("User1"), "User1 still in channel");
    }

    @Test
    public void testKickOneChannelNoSuchChannel() {
        model = new ServerModel();

        model.registerUser(0); // add user with id = 0
        model.registerUser(1); // add user with id = 1

        // this command will create a channel called "java" with "User0" (with id = 0)
        // as the owner
        Command create = new CreateCommand(0, "User0", "java", true);

        // this line *actually* updates the model's state
        create.updateServerModel(model);

        Command invite = new InviteCommand(0, "User0", "java", "User1");
        invite.updateServerModel(model);

        Command kick = new KickCommand(0, "User0", "python", "User1");

        Broadcast expected = Broadcast.error(kick, ServerResponse.NO_SUCH_CHANNEL);
        assertEquals(expected, kick.updateServerModel(model));
        assertEquals(2, model.getUsersInChannel("java").size(), "num. users in channel");
        assertTrue(model.getUsersInChannel("java").contains("User0"), "User0 still in channel");
        assertTrue(model.getUsersInChannel("java").contains("User1"), "User1 still in channel");
    }
  
    @Test
    public void testKickOneChannelUserNotOwner() {
        model = new ServerModel();

        model.registerUser(0); // add user with id = 0
        model.registerUser(1); // add user with id = 1

        // this command will create a channel called "java" with "User0" (with id = 0)
        // as the owner
        Command create = new CreateCommand(0, "User0", "java", true);

        // this line *actually* updates the model's state
        create.updateServerModel(model);

        Command invite = new InviteCommand(0, "User0", "java", "User1");
        invite.updateServerModel(model);

        Command kick = new KickCommand(1, "User1", "java", "User0");

        Broadcast expected = Broadcast.error(kick, ServerResponse.USER_NOT_OWNER);
        assertEquals(expected, kick.updateServerModel(model));
        assertEquals(2, model.getUsersInChannel("java").size(), "num. users in channel");
        assertTrue(model.getUsersInChannel("java").contains("User0"), "User0 still in channel");
        assertTrue(model.getUsersInChannel("java").contains("User1"), "User1 still in channel");
    }

}
