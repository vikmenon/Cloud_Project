package com.javacodegeeks.enterprise.rest.jersey;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.ImageSearcher;
import net.semanticmetadata.lire.ImageSearcherFactory;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Searcher {
    /**
	 * @param args
	 * @throws IOException
	 */
	public static List<OutputObject> searchImage(String args) throws IOException {
		List<OutputObject> objects = new ArrayList<OutputObject>();
		BufferedImage img = null;
        boolean passed = false;
        //System.out.println("inside search method");
            File f = new File(args);
            if (f.exists()) {
                try {
                    img = ImageIO.read(f);
                    //System.out.println("got the image");
                    passed = true;
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        if (!passed) {
            System.out.println("No image given as first argument.");
            System.out.println("Run \"Searcher <query image>\" to search for <query image>.");
            System.exit(1);
        }
        
        IndexReader ir = DirectoryReader.open(FSDirectory.open(new File("/home/ec2-user/index")));
        //System.out.println("after reading lucent index");
        ImageSearcher searcher = ImageSearcherFactory.createCEDDImageSearcher(10);

        ImageSearchHits hits = searcher.search(img, ir);
        for (int i = 0; i < hits.length(); i++) {
            String fileName = hits.doc(i).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            objects.add(new OutputObject(hits.score(i)+"",fileName));
        }
        return objects;
	}
}
