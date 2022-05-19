package org.marsofandrew.chat.ui;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.marsofandrew.chat.core.Clients;
import org.marsofandrew.chat.core.exception.ClientNotJoinedToChannelException;
import org.marsofandrew.chat.core.exception.InvalidPasswordException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Handles a server-side channel.
 */
@Slf4j
public class TelnetServerHandler extends SimpleChannelInboundHandler<String> {

    private final Map<String, BiConsumer<List<String>, Channel>> COMMANDS = Map.of(
            "/login", this::login,
            "/leave", this::leave,
            "/join", this::join,
            "/users", this::getUsers);

    private Clients.Client client;

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.writeAndFlush(String.format("ERROR: %s\n", cause.getMessage()));
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        if (msg.startsWith("/")) {
            var params = msg.split(" ");
            log.info("Receive command: {}", params[0]);
            var command = COMMANDS.get(params[0]);
            if (command == null) {
                ctx.writeAndFlush("UNKNOWN OPERATION\n");
                return;
            }
            List<String> args = new ArrayList<>(Arrays.asList(params).subList(1, params.length));
            command.accept(args, ctx.channel());
            return;
        }

        if (msg.startsWith("\\")) {
            msg = msg.substring(1);
        }
        if (client != null) {
            try {
                client.sendMessage(msg);
            } catch (ClientNotJoinedToChannelException err) {
                ctx.writeAndFlush("Client hasn't joined to any channel\n");
            }
        } else {
            ctx.writeAndFlush("You could send messages after you login\n");
        }
    }

    private void login(List<String> args, Channel channel) {
        if (args.size() != 2) {
            channel.writeAndFlush("/login command needs 2 arguments {login} {password}\n");
            return;
        }

        if (client != null) {
            channel.writeAndFlush("You should leave before login to a new client\n");
            return;
        }
        try {
            client = Clients.login(args.get(0), args.get(1))
                    .setChannel(channel);
        } catch (InvalidPasswordException exception) {
            channel.writeAndFlush("Invalid password\n");
        }
        client.updateChat();
    }

    private void leave(List<String> ign, Channel channel) {
        if (client == null) {
            printNotLoginMessage(channel);
            return;
        }
        client.leave();
        client = null;
    }

    private void getUsers(List<String> ign, Channel channel) {
        if (client == null) {
            printNotLoginMessage(channel);
            return;
        }
        try {
            client.getUsers().forEach(user -> channel.writeAndFlush(String.format("%s\n", user)));
        } catch (ClientNotJoinedToChannelException exception){
            channel.writeAndFlush("You should join channel to get users\n");
        }

    }

    private void join(List<String> args, Channel channel) {
        if (args.size() != 1) {
            channel.writeAndFlush("/join command has only 1 argument\n");
            return;
        }
        client.setChannel(channel)
                .joinChannel(args.get(0));
    }

    private static void printNotLoginMessage(Channel channel) {
        channel.writeAndFlush("This command is available only after login\n");
    }

}