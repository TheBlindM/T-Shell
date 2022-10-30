package com.tshell.core.client;

/**
 * @author TheBlind
 * @date 2022/8/14
 */
public class ClientFactory {


    public static ClientHandler getClient(TtyType ttyType) {

        switch (ttyType) {
            case CMD -> {
                return SingletonClient.CMD_CLIENT.getInstance();
            }
            case LINUX_BASH -> {
                return SingletonClient.BASH_CLIENT.getInstance();
            }
            case UBUNTU_BASH -> {
                return SingletonClient.UBUNTU_CLIENT.getInstance();
            }
            case LINUX_ZSH -> {
                return SingletonClient.ZSH_CLIENT.getInstance();
            }
            default -> throw new IllegalStateException("没有该客户端");
        }
    }

    public static ClientHandler getClient(Integer id) {
        return getClient(TtyType.getById(id));
    }


    public enum SingletonClient {
        CMD_CLIENT(new CmdClientHandler(TtyType.CMD)),
        BASH_CLIENT(new LinuxBashClientHandler(TtyType.LINUX_BASH)),
        ZSH_CLIENT(new LinuxZshClientHandler(TtyType.LINUX_ZSH)),
        UBUNTU_CLIENT(new UbuntuBashClientHandler(TtyType.UBUNTU_BASH));
        private final ClientHandler instance;

        SingletonClient(ClientHandler clientHandler) {
            instance = clientHandler;
        }

        public ClientHandler getInstance() {
            return instance;
        }
    }
}
