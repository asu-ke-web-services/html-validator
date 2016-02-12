package com.asu.gios;

import java.net.URL;
import java.net.URLConnection;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import org.allcolor.yahp.converter.CYaHPConverter;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.Set;
import java.util.LinkedHashSet;

import com.lowagie.text.DocumentException;

public class WebPageValidator{
  /** An handle to a yahp converter */
  private static CYaHPConverter converter = new CYaHPConverter();

  public static void main(String args[]) throws Exception {

    String prefix = "http://localhost:8888/?doc=";
    String outfile="results.pdf";
    String tmpDir="output/";
    String finalPDFDir="pdf/";
    String fileName = "urls.txt";
    new File(finalPDFDir).mkdirs();
/*    Set<String> urlList = new LinkedHashSet();
    urlList.add("https://stardust.asu.edu/sitemap_index.xml");
    urlList.add("https://sustainability.asu.edu/sitemap.xml");*/
    int finalPDFNumber=1;
    for(String urlToCrawl:args){

      WebCrawler.crawl(urlToCrawl,fileName);

      try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {

        String line;
        String url;
        int pdfNumber=1;
        while ((line = br.readLine()) != null) {
          url=prefix+line;
          try {
            new URL(url);
          } // end try
          catch (final Exception e) {
            System.out.println("--url must be a valid URL !");
          } // end catch
          try {
            new File(tmpDir).mkdirs();
            File fout = new File(tmpDir+pdfNumber+".pdf");

            List headerFooterList = new ArrayList();

            FileOutputStream out = new FileOutputStream(fout);

            Map properties = new HashMap();

           converter.convertToPdf(new URL(url),
              IHtmlToPdfTransformer.A4P, headerFooterList, out,
              properties);
            out.flush();
            out.close();
          } // end try
          catch (final Throwable t) {
            t.printStackTrace();
            System.err.println("An error occurs while converting '" +
              url + "' to '" + outfile + "'. Cause : " +
              t.getMessage());
            System.exit(-1);
          } // end catch
          pdfNumber++;
        }

        mergePDFs(tmpDir, finalPDFDir+finalPDFNumber+outfile);
        delete(new File(tmpDir));

      } catch (IOException e) {
        e.printStackTrace();
      }
      finalPDFNumber++;
    }
    System.exit(0);
  }

  public static List<String> listFilesForFolder(final File folder) {
    List<String> list = new ArrayList<String>();
    for (final File fileEntry : folder.listFiles()) {
        if (fileEntry.isDirectory()) {
            listFilesForFolder(fileEntry);
        } else {
            list.add(fileEntry.getName());
        }
    }
    return list;
  }


  public static void delete(File f) throws IOException {
    if (f.isDirectory()) {
      for (File c : f.listFiles())
        delete(c);
    }
    if (!f.delete())
      throw new FileNotFoundException("Failed to delete file: " + f);
}

  public static void mergePDFs(String dir, String outfile){
    final File folder = new File(dir);
    List<InputStream> list = new ArrayList<InputStream>();
        try {
            // Source pdfs
            for(String filename:listFilesForFolder(folder)){
              list.add(new FileInputStream(new File(dir+"/"+filename)));
            }

            // Resulting pdf
            OutputStream out = new FileOutputStream(new File(outfile));

            PDFMerge.doMerge(list, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
  }
}