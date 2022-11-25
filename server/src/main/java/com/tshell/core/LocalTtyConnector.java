/*
package com.example.core;

import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import com.pty4j.WinSize;
import com.tshell.core.tty.TtyConnector;
import org.apache.sshd.common.util.OsUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


public class LocalTtyConnector implements TtyConnector {

    protected InputStream myInputStream;
    protected OutputStream myOutputStream;
    protected BufferedReader myReader;
    protected BufferedWriter myWriter;

    private PtyProcess myProcess;

    public LocalTtyConnector(Parameter parameter) {

        try {
            Map<String, String> envs = System.getenv();
            String[] command;
            if (OsUtils.isWin32()) {
                command = new String[]{"cmdText.exe"};
            } else {
                command = new String[]{"/bin/bash", "--login"};
                envs = new HashMap<>(System.getenv());
                envs.put("TERM", "xterm-256color");
            }
            myProcess = new PtyProcessBuilder().setCommand(command).setEnvironment(envs).start();

            myOutputStream = myProcess.getOutputStream();
            myInputStream = myProcess.getInputStream();
            myReader = new BufferedReader(new InputStreamReader(myInputStream, StandardCharsets.UTF_8));
            myWriter = new BufferedWriter(new OutputStreamWriter(myOutputStream, StandardCharsets.UTF_8));


        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

    }


    @Override
    public void close() {
        myProcess.destroy();
        try {
            myInputStream.close();
            myWriter.close();
            myReader.close();
        } catch (IOException ignored) {
        }
    }

    @Override
    public void resize(PtySize ptySize) {
        myProcess.setWinSize(new WinSize(ptySize.columns(), ptySize.lines()));
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        myOutputStream.write(bytes);
        myOutputStream.flush();
    }

    @Override
    public void write(String string) throws IOException {
        myWriter.write(string);
        myWriter.flush();
    }

    @Override
    public int read(char[] chars) throws IOException {
        return myReader.read(chars);
    }

    @Override
    public boolean isConnected() {
        return false;
    }
}
*/
