package com.asu.gios;

import java.util.LinkedList;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.HashMap;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import java.io.FileWriter;
import java.io.IOException;

public class WebCrawler{

  public static void crawl(String urlToCrawl, String outfile) throws Exception{

    FileWriter writer = new FileWriter(outfile);
    LinkedList<String> nestedSites = new LinkedList();
    Set<String> urls = new LinkedHashSet();
    Map<String,Boolean> map = new HashMap();
    nestedSites.add(urlToCrawl);
    map.put(urlToCrawl,false);
    while(nestedSites.size()>0){
      String url = nestedSites.remove();
      if(!map.get(url)){
        parseWebPage(url,nestedSites,urls,map);
        map.put(url,true);
      }
    }

    for(String url:urls){
      writer.write(url+"\n");
    }
    writer.close();
  }

  public static void parseWebPage(String urlToCrawl,LinkedList<String> nestedSites,Set<String> sites, Map<String, Boolean> map){
    try{
      Document webpage = Jsoup.connect(urlToCrawl).get();
      Document doc = Jsoup.parse(webpage.toString(), "", Parser.xmlParser());
      Elements urls = doc.select("loc");
      if(urls!=null){
        for(Element url:urls){
          String href = url.text();
          if(href.contains("sitemap.xml") && !map.containsKey(href) ){
            nestedSites.add(href);
            map.put(href,false);
          }else{
            sites.add(href);
          }
        }
      }
    }catch(Exception e){
      e.printStackTrace();
    }
  }
}