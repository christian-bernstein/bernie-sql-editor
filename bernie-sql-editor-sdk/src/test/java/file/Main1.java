package file;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import de.christianbernstein.bernie.sdk.misc.ConsoleLogger;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author Christian Bernstein
 */
public class Main1 {

    public static void main(String[] args) throws Exception {
        upload();
    }

    private static ChannelSftp setupJsch() throws JSchException {
        JSch jsch = new JSch();
        jsch.setKnownHosts("/Users/compu/.ssh/known_hosts");
        Session jschSession = jsch.getSession("root", "173.249.3.132");
        jschSession.setPassword("ktkL9QhGmhBf85");
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        jschSession.setConfig(config);
        jschSession.connect();
        return (ChannelSftp) jschSession.openChannel("sftp");
    }

    public static void upload() throws Exception {
        final ChannelSftp channel = setupJsch();
        channel.connect();

        final String remoteDir = "/remote_sftp_test/";
        final List<Path> files = Files.walk(Paths.get("bernie-sql-editor-sdk/src/main/resources")).collect(Collectors.toList());
        final List<Path> fileList = files.stream().filter(path -> path.toFile().isFile()).collect(Collectors.toList());

        long sizeSum = 0;

        try (final ProgressBar pb = new ProgressBarBuilder().setMaxRenderedLength(200).setInitialMax(fileList.size()).setTaskName("Test").build()) {
            for (final Path path : fileList) {
                pb.step();

                final File file = path.toFile();
                final String localFile = path.toFile().getPath();
                final long size = file.length();

                try {
                    String em = String.format("transferring %s '%s'", humanReadableByteCountSI(size), localFile);
                    em = em.substring(0, Math.min(em.length(), 100));
                    pb.setExtraMessage(em);
                    final String dest = String.format("%s%s", remoteDir, path).replaceAll("\\\\", "/");
                    prepareUpload(channel, dest, true);
                    channel.put(localFile, dest);
                } catch (final SftpException e) {
                    e.printStackTrace();
                }

                sizeSum += size;
            }
        }

        System.out.println(humanReadableByteCountSI(sizeSum));

        // for (int i = 0; i < fileList.size(); i++) {
        //     final Path path = files.get(i);
        //     final File file = path.toFile();
        //     if (!file.isDirectory()) {
        //         final String localFile = file.getPath();
        //         try {
        //             final String dest = String.format("%s%s", remoteDir, path).replaceAll("\\\\", "/");
        //             prepareUpload(channel, dest, true);
        //             channel.put(localFile, dest);
        //         } catch (final SftpException e) {
        //             e.printStackTrace();
        //         }
        //     } else {
        //         System.out.println("No file + " + file.length() + ", " + path);
        //     }
        // }
        // ConsoleLogger.def().log(ConsoleLogger.LogType.SUCCESS, "sftp", "finished");

        channel.exit();
    }

    public static void prepareUpload(ChannelSftp channel, String path, boolean overwrite) throws SftpException {
        String[] folders = path.split("/");
        for (int i = 0; i < folders.length; i++) {
            String folder = i == 0 ? String.format("/%s", folders[i]) : folders[i];
            if (folder.length() > 0 && !folder.contains(".")) {
                // This is a valid folder:
                try {
                    // System.out.println("cd: " + folder);
                    channel.cd(folder);
                } catch (SftpException e) {
                    // No such folder yet:
                    // ConsoleLogger.def().log(ConsoleLogger.LogType.INFO, "sftp", String.format("mkdir %s", folder));
                    channel.mkdir(folder);
                    channel.cd(folder);
                    // ConsoleLogger.def().log(ConsoleLogger.LogType.SUCCESS, "sftp", String.format("dir created %s", folder));
                }
            }
        }
    }

    public static String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        final CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }
}




// Folders ready. Remove such a file if exists:
// if (sftpChannel.ls(path).size() > 0) {
//     if (!overwrite) {
//         System.out.println(
//                 "Error - file " + path + " was not created on server. " +
//                         "It already exists and overwriting is forbidden.");
//     } else {
//         // Delete file:
//         sftpChannel.ls(path); // Search file.
//         sftpChannel.rm(path); // Remove file.
//     }
// }
