package org.example;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class TaskThread extends Thread {

    // TODO: change this to playlists path
    private static final String pathFrom = "C:\\Users\\snk_l\\Documents\\playlists_nas\\playlists_audioStation";
    private static final String pathTo = "C:\\Users\\snk_l\\Documents\\playlists_nas\\playlists_jellyfin";

    private static final String FORMAT_XSLT = "src/main/resources/xslt/format.xslt";

    public void run() {
        long startTime = System.currentTimeMillis();
        System.out.println(this.getName() + ": started");

        Path pathPlaylistFrom = Paths.get(pathFrom + "\\" + this.getName() + ".m3u");
        try {
            BufferedReader reader = Files.newBufferedReader(pathPlaylistFrom);
            reader.readLine(); //ignore #EXTM3U

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            final String pathOutput = pathTo + "\\g_" + this.getName() + "\\playlist.xml";
            try (InputStream is = new FileInputStream(pathOutput)) {
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(is);
                NodeList listOfItem = doc.getElementsByTagName("Item");
                Node item = listOfItem.item(0);
                if (item.getNodeType() == Node.ELEMENT_NODE) {
                    Element playlistItems = doc.createElement("PlaylistItems");
                    String line;
                    while ((line = reader.readLine()) != null) {
                        playlistItems.appendChild(appendLineElement(doc, line));
                    }
                    //item.appendChild(playlistItems);
                    NodeList listOfShares = doc.getElementsByTagName("Shares");
                    item.insertBefore(playlistItems, listOfShares.item(0));
                }

                writeXml(doc, System.out);
                // try (FileOutputStream output =
                //              new FileOutputStream(pathOutput)) {
                //     writeXml(doc, output);
                // }

            } catch (ParserConfigurationException e) {
                throw new RuntimeException(e);
            } catch (SAXException e) {
                throw new RuntimeException(e);
            } catch (TransformerException e) {
                throw new RuntimeException(e);
            }

        } catch (IOException e) {
            System.err.println("Error reading file : " + this.getName() + " : " + e.getMessage());
            return;
        }

        long endTime = System.currentTimeMillis();
        long difference = endTime - startTime;
        System.out.println(this.getName() + ": finished in " + TimeUnit.MILLISECONDS.toSeconds(difference) + " seconds");
    }

    private Element appendLineElement(Document doc, final String line) {
        Element path = doc.createElement("Path");
        String formated = "/" + line.substring(17);
        path.appendChild(doc.createTextNode(formated));
        Element playlistItem = doc.createElement("PlaylistItem");
        playlistItem.appendChild(path);
        return playlistItem;
    }

    // write doc to output stream
    private static void writeXml(Document doc,
                                 OutputStream output)
            throws TransformerException, UnsupportedEncodingException {

        TransformerFactory transformerFactory = TransformerFactory.newInstance();

        // The default add many empty new line, not sure why?
        // https://mkyong.com/java/pretty-print-xml-with-java-dom-and-xslt/
        //Transformer transformer = transformerFactory.newTransformer();

        // add a xslt to remove the extra newlines
        Transformer transformer = transformerFactory.newTransformer(
                new StreamSource(new File(FORMAT_XSLT)));

        // pretty print
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "no");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(output);

        transformer.transform(source, result);
    }
}