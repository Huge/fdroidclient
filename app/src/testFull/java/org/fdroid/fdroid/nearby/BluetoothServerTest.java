package org.fdroid.fdroid.nearby;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;

@RunWith(RobolectricTestRunner.class)
public class BluetoothServerTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testPathTraversalForbidden() throws IOException {
        File webRoot = tempFolder.newFolder("webroot");
        BluetoothServer.ClientConnection client = new BluetoothServer.ClientConnection(null, webRoot);

        // Path traversal attempts
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN,
                client.respond(Collections.emptyMap(), "/..").getStatusCode());
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN,
                client.respond(Collections.emptyMap(), "..").getStatusCode());
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN,
                client.respond(Collections.emptyMap(), "/subdir/..").getStatusCode());
    }

    @Test
    public void testSymlinkOutsideWebRootForbidden() throws IOException {
        File webRoot = tempFolder.newFolder("webroot");
        File outsideDir = tempFolder.newFolder("outside");
        File secretFile = new File(outsideDir, "secret.txt");
        try (FileOutputStream out = new FileOutputStream(secretFile)) {
            out.write("secret-content".getBytes(StandardCharsets.UTF_8));
        }

        File symlink = new File(webRoot, "symlink");
        try {
            Files.createSymbolicLink(symlink.toPath(), outsideDir.toPath());
        } catch (UnsupportedOperationException | IOException e) {
            return;
        }

        BluetoothServer.ClientConnection client = new BluetoothServer.ClientConnection(null, webRoot);
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN,
                client.respond(Collections.emptyMap(), "/symlink/secret.txt").getStatusCode());
    }

    @Test
    public void testServeLegitimateFile() throws IOException {
        File webRoot = tempFolder.newFolder("webroot");
        File file = new File(webRoot, "test.txt");
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write("hello".getBytes(StandardCharsets.UTF_8));
        }

        BluetoothServer.ClientConnection client = new BluetoothServer.ClientConnection(null, webRoot);
        assertEquals(HttpURLConnection.HTTP_OK,
                client.respond(Collections.emptyMap(), "/test.txt").getStatusCode());
    }
}
