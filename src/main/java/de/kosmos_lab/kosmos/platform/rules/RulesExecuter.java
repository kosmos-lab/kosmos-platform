package de.kosmos_lab.kosmos.platform.rules;

import de.dfki.baall.helper.webserver.data.IUser;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class RulesExecuter extends Thread {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("RulesExecuter");


    private final IUser user;
    private final RulesService service;
    String pythonCmd = "python";
    private Process pr;
    private boolean stopped = false;

    public RulesExecuter(RulesService service, IUser user) {
        this.service = service;
        this.user = user;
        this.pr = null;


    }

    public void exit() {
        this.stopped = true;
        if (pr != null) {
            this.pr.destroy();
        }
    }

    public void restart() {
        if (pr != null) {
            this.pr.destroy();
        }
    }

    public void run() {
        try {
            Process pr2 = Runtime.getRuntime().exec("python3 --version");
            if (pr2.waitFor() == 0) {
                pythonCmd = "python3";
            }
        } catch (IOException | InterruptedException e) {
            logger.error("could not execute python3 checker", e);
        }
        while (!stopped) {
            BufferedReader in = null;
            try {
                String cmd = "";
                File dir = new File("rules/");
                logger.info("working dir: {}", System.getProperty("user.dir"));

                cmd = pythonCmd + " rules/" + user.getUUID().getLeastSignificantBits() + ".py";


                logger.info("executing: {}", cmd);
                pr = Runtime.getRuntime().exec(cmd, null, dir);

                in = new BufferedReader(new InputStreamReader(pr.getErrorStream(), StandardCharsets.UTF_8));
                String line;

                while ((line = in.readLine()) != null) {

                    logger.warn("User({}), stderr:{}", user.getUUID().getLeastSignificantBits(), line);
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                in = new BufferedReader(new InputStreamReader(pr.getInputStream(), StandardCharsets.UTF_8));

                while ((line = in.readLine()) != null) {

                    logger.warn("User({}), stdout:{}", user.getUUID().getLeastSignificantBits(), line);
                }
                try {
                    pr.waitFor();
                } catch (InterruptedException e) {
                    logger.error("could not wait for python", e);
                }


                in.close();

            } catch (IOException e) {
                logger.error("could not wait execute python", e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.error("could not sleep?", e);
            }

        }

    }


}
